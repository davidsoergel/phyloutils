package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.DSStringUtils;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	HashMultimap<String, Integer> nameToIdsMap;// = new HashMap<String, Integer>();

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

	public RootedPhylogeny<Integer> getRandomSubtree(int numTaxa, Double mergeThreshold)
		{
		throw new NotImplementedException();
		}

	private void reloadFromNewick()
		{
		nameToIdsMap = new HashMultimap<String, Integer>();

		NewickTaxonomyService stringTaxonomyService = new NewickTaxonomyService(hugenholtzFilename);

		RootedPhylogeny<String> theStringTree = stringTaxonomyService.getTree();

		theIntegerTree = PhylogenyTypeConverter
				.convertToIDTree(theStringTree, new IntegerNodeNamer(10000000), new TaxonStringIdMapper<Integer>()
				{
				public Integer findTaxidByName(String name) throws NoSuchNodeException
					{
					try
						{
						return new Integer(name);
						}
					catch (NumberFormatException e)
						{
						throw new NoSuchNodeException("Can't convert node name to integer ID: " + name);
						}
					}
				}, nameToIdsMap);


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
			Pattern strainPattern = Pattern.compile("( str.? )|( strain )");
			int skipped = 0;
			int found = 0;
			while ((line = in.readLine()) != null)
				{
				line = line.trim();
				if (line.equals("END"))
					{
					try
						{
						theIntegerTree.getNode(prokMSA_id);
						}
					catch (NoSuchNodeException e)
						{
						if (logger.isTraceEnabled())
							{
							logger.trace(
									"prokMSA_id " + prokMSA_id + " not in tree; " + organism + " " + prokMSAname + " "
											+ source);
							}
						skipped++;
						continue;
						}
					found++;
					if (organism != null)
						{
						nameToIdsMap.put(organism, prokMSA_id);
						String cleanOrganism = strainPattern.matcher(organism).replaceAll(" ");
						if (!cleanOrganism.equals(source))
							{
							nameToIdsMap.put(cleanOrganism, prokMSA_id);
							}
						}
					if (prokMSAname != null)
						{
						nameToIdsMap.put(prokMSAname, prokMSA_id);
						String cleanProkMSAname = strainPattern.matcher(prokMSAname).replaceAll(" ");
						if (!cleanProkMSAname.equals(source))
							{
							nameToIdsMap.put(cleanProkMSAname, prokMSA_id);
							}
						}
					if (source != null)
						{
						nameToIdsMap.put(source, prokMSA_id);
						String cleanSource = strainPattern.matcher(source).replaceAll("");
						if (!cleanSource.equals(source))
							{
							nameToIdsMap.put(cleanSource, prokMSA_id);
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
			logger.info("Found " + found + " taxa in tree, skipped " + skipped);
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
			oos.writeObject(nameToIdsMap);
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
			nameToIdsMap = (HashMultimap<String, Integer>) ois.readObject();
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

	public boolean isLeaf(Integer leafId) throws NoSuchNodeException
		{
		return theIntegerTree.getNode(leafId).isLeaf();
		}

	Map<String, Integer> findTaxidByNameCache = new HashMap<String, Integer>();

	@NotNull
	public Integer findTaxidByName(String name) throws NoSuchNodeException
		{
		Integer result = findTaxidByNameCache.get(name);

		if (result == null)
			{
			try
				{
				Integer id = new Integer(name);
				theIntegerTree.getNode(id);   // throws exception if not present // intToNodeMap.containsKey(id))
				result = id;
				}
			catch (NumberFormatException e)
				{
				// ok, try the next thing
				}
			catch (NoSuchNodeException e)
				{
				// ok, try the next thing
				}

			if (result == null)
				{
				if (!name.contains(";"))
					{
					result = getUniqueNodeForName(name);
					}
				else
					{
					result = getUniqueNodeForMultilevelName(name.split("[; ]+"));
					}
				}
			findTaxidByNameCache.put(name, result);
			}
		return result;
		}

/*	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws PhyloUtilsException
		{

		}
*/


	private Integer commonAncestor(Set<Deque<Integer>> paths) throws NoSuchNodeException
		{
		if (paths.size() == 1)
			{
			final Deque<Integer> path = paths.iterator().next();
			return path.getLast();
			}
		else
			{
			assert paths.size() > 1;
			//	throw new PhyloUtilsRuntimeException("Taxonomy path not unique : " + DSStringUtils.join(taxa, "; "));
			Set<Integer> leafIds = new HashSet<Integer>();
			for (Deque<Integer> path : paths)
				{
				leafIds.add(path.peekLast());
				}
			return theIntegerTree.commonAncestor(leafIds, 0.90);
			}
		}


	// bottom-up search

	@NotNull
	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws NoSuchNodeException
		{
		List<String> reverseTaxa = new ArrayList(Arrays.asList(taxa.clone()));
		Collections.reverse(reverseTaxa);

		//final String firstS = reverseTaxa.remove(0);
		//Collection<Integer> trav = null; // = nameToIdMap.get(firstS);

		/*while (trav.isEmpty())
			{
			logger.warn("IGNORING Node " + s + " not found in " + DSStringUtils.join(taxa, "; "));
			continue;
			}
*/

		Set<Deque<Integer>> paths = null;

		for (String s : reverseTaxa)
			{
			Collection<Integer> matchingNodes = nameToIdsMap.get(s);

			if (matchingNodes.isEmpty())
				{
				logger.debug("IGNORING Node " + s + " not found in " + DSStringUtils.join(taxa, "; "));
				}
			else
				{
				//	Set<Integer> nextTrav = new HashSet<Integer>();
				if (paths == null)
					{
					paths = new HashSet<Deque<Integer>>(matchingNodes.size());
					//nextTrav.addAll(matchingNodes);
					for (Integer node : matchingNodes)
						{
						Deque<Integer> l = new LinkedList<Integer>();
						l.add(node);
						paths.add(l);
						}
					}
				else
					{
					Set<Deque<Integer>> okPaths = new HashSet<Deque<Integer>>();
					for (Deque<Integer> path : paths)
						{
						Integer descendant = path.peek();
						for (Integer ancestor : matchingNodes)
							{
							if (theIntegerTree.isDescendant(ancestor, descendant))
								{
								path.addFirst(ancestor);
								okPaths.add(path);
								}
							}
						}
					paths = okPaths;  // ditch any paths that didn't have an ancestor added this round
					}

				if (paths.isEmpty())
					{
					// we get here only if
					//  a) there was more than one live path on the last round
					//  b) none of those paths are descendants of the matches at the current level
					throw new NoSuchNodeException(
							"Requested classification path does not match tree: " + DSStringUtils.join(taxa, "; "));
					}

				// if all the paths converge on exactly one node, call it a match, even if higher levels of the tree don't match.

				if (matchingNodes.size() == 1)
					{
					return commonAncestor(paths);
					}
				}
			}
		throw new NoSuchNodeException("Multiple distinct matching paths: " + DSStringUtils.join(taxa, "; "));
		//return commonAncestor(paths);
		}

	// top-down search
	/*	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws PhyloUtilsException
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
	 */


	Pattern spaceSuffixPattern = Pattern.compile(" \\S*$");
	//Pattern strainSuffixPattern = Pattern.compile("( (sp.?)|(str.?)|(strain)).*$");

	@NotNull
	public Integer getUniqueNodeForName(String name) throws NoSuchNodeException
		{
		Collection<Integer> matchingIds = nameToIdsMap.get(name);
		/*	if (matchingIds.isEmpty())
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
		   }*/
		if (matchingIds.isEmpty())
			{
			matchingIds = new HashSet<Integer>();
			for (String syn : synonymService.synonymsOfRelaxed(name))
				{
				matchingIds.addAll(nameToIdsMap.get(syn));
				}
			}
		String shortName = name;

		// even when we use synonymsOfRelaxed(shortName), we may not find any matching IDs in the nameToIdMap.
		// that's why we do asecond level of relaxing here.
		// the space-delimited relaxing should automatically incorporate the strainSuffixPattern.

		while (matchingIds.isEmpty() && shortName.contains(" "))
			{
			shortName = spaceSuffixPattern.matcher(shortName).replaceFirst("");
			//shortName = strainSuffixPattern.matcher(shortName).replaceAll("");
			matchingIds = new HashSet<Integer>();
			for (String syn : synonymService.synonymsOfRelaxed(shortName))
				{
				matchingIds.addAll(nameToIdsMap.get(syn));
				}
			if (!matchingIds.isEmpty())
				{
				logger.debug("Relaxed name " + name + " to " + shortName);
				}
			}
		if (matchingIds.isEmpty())
			{
			throw new NoSuchNodeException("Node not found: " + name + "; no id found even for " + shortName);
			}
		if (matchingIds.size() > 1)
			{
			return theIntegerTree.commonAncestor(matchingIds, 0.90);
			//throw new PhyloUtilsException("Name not unique: " + name);
			}
		return matchingIds.iterator().next();
		}

	public boolean isDescendant(Integer ancestor, Integer descendant) throws NoSuchNodeException
		{
		return theIntegerTree.isDescendant(ancestor, descendant);
//		return stringTaxonomyService.isDescendant(intToNodeMap.get(ancestor), intToNodeMap.get(descendant));
		}


	/*public double exactDistanceBetween(Integer a, Integer b)
		{
		return stringTaxonomyService.distanceBetween(intToNodeMap(a), intToNodeMap(b));
		}
*/
	public Double minDistanceBetween(Integer a, Integer b) throws NoSuchNodeException
		{
		return theIntegerTree.distanceBetween(a, b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}

	public Double getDepth(Integer b) throws NoSuchNodeException
		{
		return theIntegerTree.distanceBetween(theIntegerTree.getRoot().getValue(), b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}

	public Integer nearestAncestorWithBranchLength(Integer id) throws NoSuchNodeException
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

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws NoSuchNodeException
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
			throws NoSuchNodeException
		{
		return theIntegerTree.extractTreeWithLeafIDs(ids, ignoreAbsentNodes);
		}
	}
