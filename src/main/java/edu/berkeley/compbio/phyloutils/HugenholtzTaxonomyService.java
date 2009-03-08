package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.DSStringUtils;
import com.google.common.collect.HashMultimap;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Provides a view onto the Hugenholtz taxonomy using Integer IDs.  The Hugenholtz taxonomy has integer ids
 * (prokMSA_ids) at the leaves, but not at internal nodes.  Some internal nodes have string names, but there is no
 * guarantee of uniqueness.  For various reasons we need a tree with unique Integer IDs throughout.  This tree uses the
 * prokMSA_ids at the leaves and generated IDs at internal nodes.  It also allows looking up nodes by String name,
 * including semicolon-delimited multilevel classifications.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HugenholtzTaxonomyService implements TaxonomyService<Integer> //, TaxonomyService<String>
	{
	private static final Logger logger = Logger.getLogger(HugenholtzTaxonomyService.class);

	//private String ciccarelliFilename = "tree_Feb15_unrooted.txt";
	private static final String hugenholtzFilename = "greengenes.all.tree.gz";
	private static final String bigGreenGenesFilename = "greengenes16SrRNAgenes.txt.gz";

	private static HugenholtzTaxonomyService instance;// = new CiccarelliUtils();

	TaxonomySynonymService synonymService;

	public static HugenholtzTaxonomyService getInjectedInstance()
		{
		return instance;
		}

	public static void setInjectedInstance(HugenholtzTaxonomyService instance)
		{
		HugenholtzTaxonomyService.instance = instance;
		}

	public void setSynonymService(TaxonomySynonymService synonymService)
		{
		this.synonymService = synonymService;
		}

	private BasicRootedPhylogeny<Integer> theIntegerTree;
	HashMultimap<String, Integer> nameToIdMap;// = new HashMap<String, Integer>();

//	BiMap<Integer, PhylogenyNode<String>> intToNodeMap = new HashBiMap<Integer, PhylogenyNode<String>>();
//	Multimap<String, PhylogenyNode<String>> nameToNodeMap = new HashMultimap<String, PhylogenyNode<String>>();

//	NewickTaxonomyService stringTaxonomyService;// = new NewickTaxonomyService(hugenholtzFilename);

	public HugenholtzTaxonomyService() //throws PhyloUtilsException
		{
		if (!readStateIfAvailable())
			{
			reloadFromNewick();
			saveState();
			}
		}


	private void reloadFromNewick()
		{
		nameToIdMap = new HashMultimap<String, Integer>();

		NewickTaxonomyService stringTaxonomyService = new NewickTaxonomyService(hugenholtzFilename);

		RootedPhylogeny<String> theStringTree = stringTaxonomyService.getTree();
		theIntegerTree = PhylogenyTypeConverter
				.convertToIDTree(theStringTree, new IntegerNodeNamer(10000000), new TaxonStringIdMapper<Integer>()
				{
				public Integer findTaxidByName(String name) throws PhyloUtilsException
					{
					try
						{
						return new Integer(name);
						}
					catch (NumberFormatException e)
						{
						throw new PhyloUtilsException("Can't convert node name to integer ID: " + name);
						}
					}
				}, nameToIdMap);

		addStrainNamesToMap();
		}

	private static InputStream getInputStream(String filename) throws PhyloUtilsException, IOException
		{
		//ClassLoader classClassLoader = new NewickParser().getClass().getClassLoader();
		ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
		//ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

		//URL res1 = classClassLoader.getResource(filename);
		URL res = threadClassLoader.getResource(filename);
		//URL res3 = systemClassLoader.getResource(filename);

		if (res == null)
			{
			File f = new File(filename);
			if (f.exists())
				{
				res = new URL("file://" + filename);
				}
			}

		if (res == null)
			{
			logger.error("file not found: " + filename);
			//Get the System Classloader
			//ClassLoader.getSystemClassLoader();

			//Get the URLs
			URL[] urls = ((URLClassLoader) threadClassLoader).getURLs();

			for (int i = 0; i < urls.length; i++)
				{
				logger.warn(urls[i].getFile());
				}

			throw new PhyloUtilsException("file not found: " + filename);
			}


		InputStream is = res.openStream();
		is = filename.endsWith(".gz") ? new GZIPInputStream(is) : is;
		/*if (is == null)
					 {
					 is = new FileInputStream(filename);
					 }*/
		return is;
		}

	/**
	 *
	 */
	private void addStrainNamesToMap()
		{
		// there are much cleaner ways to do this, I know.  I'm in a freaking hurry.

		String organism = null;
		String prokMSAname = null;
		String source = null;
		Integer prokMSA_id = null;
		//	Integer replaced_by = null;

		// for now we ignore replaced_by and put all the IDs (old and new) in the map.
		// the only consequence AFAIK is that the old IDs won't be in the current tree;
		// the benefit is that if an old ID turns up for some reason we can still map it.

		try
			{

			BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream(bigGreenGenesFilename)));
			String line;
			Pattern strainPattern = Pattern.compile(" (str.?)|(strain) ");

			while ((line = in.readLine()) != null)
				{
				line = line.trim();
				if (line.equals("END"))
					{
					if (organism != null)
						{
						nameToIdMap.put(organism, prokMSA_id);
						String cleanOrganism = strainPattern.matcher(organism).replaceAll("");
						if (!cleanOrganism.equals(source))
							{
							nameToIdMap.put(cleanOrganism, prokMSA_id);
							}
						}
					if (prokMSAname != null)
						{
						nameToIdMap.put(prokMSAname, prokMSA_id);
						String cleanProkMSAname = strainPattern.matcher(prokMSAname).replaceAll("");
						if (!cleanProkMSAname.equals(source))
							{
							nameToIdMap.put(cleanProkMSAname, prokMSA_id);
							}
						}
					if (source != null)
						{
						nameToIdMap.put(source, prokMSA_id);
						String cleanSource = strainPattern.matcher(source).replaceAll("");
						if (!cleanSource.equals(source))
							{
							nameToIdMap.put(cleanSource, prokMSA_id);
							}
						}

					organism = null;
					prokMSAname = null;
					source = null;
					prokMSA_id = null;
					}
				else
					{
					String[] sa = line.split("=");
					if (sa[0].equals("organism"))
						{
						organism = sa[1];
						}
					else if (sa[0].equals("source"))
						{
						source = sa[1];
						}
					else if (sa[0].equals("prokMSA_id"))
							{
							prokMSA_id = new Integer(sa[1]);
							}
						else if (sa[0].equals("prokMSAname"))
								{
								prokMSAname = sa[1];
								}
					//	else if (sa[0].equals("replaced_by"))
					//			{
					//			replaced_by = sa[1];
					//			}
					// else ignore
					}
				}
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}

		//try
		//	{
		//	}
		//catch (PhyloUtilsException e)
		//	{
//throw new
		//	}
		//super(hugenholtzFilename);

		// walk the entire tree, making an int->node map and a string->int multimap along the way

		// assume that all prokMSA_IDs are less than 10000000, so just start the generated IDs from there


/*		int idGenerator = 10000000;

		for (PhylogenyNode<String> node : stringTaxonomyService.getRoot())
			{
			String stringName = node.getValue();
			Integer id;
			try
				{
//
//				if (stringName == null)
//					{
//					throw new NumberFormatException("");
//					}
//
//				stringName = stringName.trim();
//

				id = new Integer(stringName);
				if (intToNodeMap.containsKey(id))
					{
					throw new PhyloUtilsRuntimeException("Found duplicate prokMSA_id: " + id);
					}
				}
			catch (NumberFormatException e)
				{
				// ok, generate an ID instead
				id = idGenerator++;

				if (stringName != null && !stringName.trim().equals(""))
					{
					nameToNodeMap.put(stringName.trim(), node);
					}

				// note we don't put the string representation of the integer id in the string map
				}

			intToNodeMap.put(id, node);
			}*/
		}


	public void saveState()
		{
		try
			{
			FileOutputStream fout = new FileOutputStream("/tmp/edu.berkeley.compbio.phyloutils.hugenholtz.cache");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			oos.writeObject(stringTaxonomyService);
//			oos.writeObject(intToNodeMap);
//			oos.writeObject(nameToNodeMap);
			oos.writeObject(theIntegerTree);
			oos.writeObject(nameToIdMap);
			oos.close();
			}
		catch (Exception e)
			{
			logger.error("Error", e);
			}
		}

	private boolean readStateIfAvailable()
		{
		try
			{
			FileInputStream fin = new FileInputStream("/tmp/edu.berkeley.compbio.phyloutils.hugenholtz.cache");
			ObjectInputStream ois = new ObjectInputStream(fin);
//			stringTaxonomyService = (NewickTaxonomyService) ois.readObject();
//			intToNodeMap = (BiMap<Integer, PhylogenyNode<String>>) ois.readObject();
//			nameToNodeMap = (Multimap<String, PhylogenyNode<String>>) ois.readObject();
			theIntegerTree = (BasicRootedPhylogeny<Integer>) ois.readObject();
			nameToIdMap = (HashMultimap<String, Integer>) ois.readObject();
			ois.close();
			return true;
			}
		catch (IOException e)
			{// no problem
			logger.warn("Could not read Hugenholtz cache; rereading source files");
			}
		catch (ClassNotFoundException e)
			{// no problem
			logger.warn("Could not read Hugenholtz cache; rereading source files");
			}
		return false;
		}

	public Integer findTaxidByName(String name) throws PhyloUtilsException
		{
		//Integer result = null;
		// could have secondary cache
		// getUniqueNodeForName(name);
		// nameToIdMap.get(name);
		// if (result == null)
		//	{
		try
			{
			Integer id = new Integer(name);
			theIntegerTree.getNode(id);   // throws exception if not present // intToNodeMap.containsKey(id))
			return id;
			}
		catch (NumberFormatException e)
			{
			// ok, try the next thing
			}
		catch (NoSuchElementException e)
			{
			// ok, try the next thing
			}

		if (!name.contains(";"))
			{
			return getUniqueNodeForName(name);
			}

		return getUniqueNodeForMultilevelName(name.split("[; ]+"));
		//	}
		//return result;
		}

/*	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws PhyloUtilsException
		{

		}
*/

	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws PhyloUtilsException
		{
		//List<Integer> intTaxa = new ArrayList<Integer>(taxa.length);
		Integer trav = theIntegerTree.getRoot().getValue();
		for (String s : taxa)
			{
			Collection<Integer> matchingNodes = nameToIdMap.get(s);

			if (matchingNodes.isEmpty())
				{
				throw new PhyloUtilsException("Node " + s + " not found in " + DSStringUtils.join(taxa, "; "));
				}

			for (Iterator<Integer> iter = matchingNodes.iterator(); iter.hasNext();)
				{
				Integer node = iter.next();

				try
					{
					if (!theIntegerTree.isDescendant(trav, node))
						{
						iter.remove();
						}
					}
				catch (NoSuchElementException e)  // probably the requested node is not in the tree (i.e., it's unclassified, but had an organism name associated anyway)
					{
					iter.remove();
					}
				}

			if (matchingNodes.isEmpty())
				{
				throw new PhyloUtilsException(
						"Requested classification path does not match tree: " + DSStringUtils.join(taxa, "; "));
				}

			if (matchingNodes.size() == 1)
				{
				trav = matchingNodes.iterator().next();
				}
			else
				{
				// check descendants pairwise

				for (Iterator<Integer> iter = matchingNodes.iterator(); iter.hasNext();)
					{
					Integer descendant = iter.next();
					for (Integer ancestor : matchingNodes)
						{
						if (ancestor != descendant && theIntegerTree.isDescendant(ancestor, descendant))
							{
							iter.remove();
							break;
							}
						}
					}
				if (matchingNodes.size() == 1)
					{
					trav = matchingNodes.iterator().next();
					}
				else
					{
					// sadly this is too strict; there are 7 distinct "Bacteria" clades!
					// OK, don't parse the "organism" field, use "prokMSAname" instead.

					throw new PhyloUtilsException(
							"Node " + s + " not unique at " + trav + " in " + DSStringUtils.join(taxa, "; "));
					}
				}
			}

		return trav; //intToNodeMap.inverse().get(trav);
		}

	public Integer getUniqueNodeForName(String name) throws PhyloUtilsException
		{
		Collection<Integer> matchingIds = nameToIdMap.get(name);
		if (matchingIds.isEmpty())
			{
			matchingIds = new HashSet<Integer>();
			for (String syn : synonymService.synonymsOf(name))
				{
				matchingIds.addAll(nameToIdMap.get(syn));
				}
			}
		if (matchingIds.isEmpty())
			{
			matchingIds = new HashSet<Integer>();
			for (String syn : synonymService.synonymsOfParent(name))
				{
				matchingIds.addAll(nameToIdMap.get(syn));
				}
			}
		if (matchingIds.isEmpty())
			{
			return null;
			}
		if (matchingIds.size() > 1)
			{
			return theIntegerTree.commonAncestor(matchingIds);
			//throw new PhyloUtilsException("Name not unique: " + name);
			}
		return matchingIds.iterator().next();
		}

	public boolean isDescendant(Integer ancestor, Integer descendant) throws PhyloUtilsException
		{
		return theIntegerTree.isDescendant(ancestor, descendant);
//		return stringTaxonomyService.isDescendant(intToNodeMap.get(ancestor), intToNodeMap.get(descendant));
		}


	/*public double exactDistanceBetween(Integer a, Integer b)
		{
		return stringTaxonomyService.distanceBetween(intToNodeMap(a), intToNodeMap(b));
		}
*/
	public Double minDistanceBetween(Integer a, Integer b) throws PhyloUtilsException
		{
		return theIntegerTree.distanceBetween(a, b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}


	public Integer nearestAncestorWithBranchLength(Integer id) throws PhyloUtilsException
		{
		return theIntegerTree.nearestAncestorWithBranchLength(id);
//		return intToNodeMap.inverse().get(intToNodeMap.get(id).nearestAncestorWithBranchLength());
		}

	/*
	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		return stringTaxonomyService
				.extractTreeWithLeaves(DSCollectionUtils.mapAll(intToNodeMap, ids), ignoreAbsentNodes);
		}

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws PhyloUtilsException
		{
		return stringTaxonomyService.extractTreeWithLeaves(DSCollectionUtils.mapAll(intToNodeMap, ids));
		}
*/

	/*	public boolean isDescendant(PhylogenyNode<Integer> ancestor, PhylogenyNode<Integer> descendant)
			 throws PhyloUtilsException
		 {
		 throw new NotImplementedException();
		 }

	 public Double minDistanceBetween(PhylogenyNode<Integer> node1, PhylogenyNode<Integer> node2)
			 throws PhyloUtilsException
		 {
		 throw new NotImplementedException();
		 }

	 public PhylogenyNode<Integer> getRoot()
		 {
		 throw new NotImplementedException();
		 }

	 public PhylogenyNode<Integer> nearestAncestorWithBranchLength(PhylogenyNode<Integer> id) throws PhyloUtilsException
		 {
		 throw new NotImplementedException();
		 }
 */

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws PhyloUtilsException
		{
		return extractTreeWithLeafIDs(ids, false);
		}

	/*
	 public RootedPhylogeny<Integer> extractTreeWithLeaves(Collection<PhylogenyNode<Integer>> ids)
			 throws PhyloUtilsException
		 {
		 throw new NotImplementedException();
		 }
 */
	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		return theIntegerTree.extractTreeWithLeafIDs(ids, ignoreAbsentNodes);
		}
	}
