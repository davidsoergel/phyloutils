package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.CacheManager;
import com.davidsoergel.dsutils.DSStringUtils;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.file.StringSetIntMapReader;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.concurrent.ConcurrentHashMap;
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
	private static final String hugenholtzFilename = "275K.nast.ft190.constrained.rooted.allids.gz";
	//private static final String hugenholtzFilename = "greengenes.all.tree.allids.gz";
	private static final String bigGreenGenesFilename = "greengenes16SrRNAgenes.txt.gz";
	private static final String overrideFilename = "overrideNameToProkMSAid.txt";

	private static HugenholtzTaxonomyService instance;// = new CiccarelliUtils();

	private TaxonomySynonymService synonymService;

	private final static Integer NO_VALID_ID = -1;

	public static HugenholtzTaxonomyService getInjectedInstance()
		{
		return instance;
		}

	public Map<Integer, String> getFriendlyLabelMap()
		{
		return null;
		}

	public static void setInjectedInstance(HugenholtzTaxonomyService instance)
		{
		HugenholtzTaxonomyService.instance = instance;
		}

	public synchronized void setSynonymService(TaxonomySynonymService synonymService)
		{
		this.synonymService = synonymService;
		}

	// PERF use ConcurrentMaps and such here instead of synchronizing all the methods

	private BasicRootedPhylogeny<Integer> theIntegerTree;
	private HashMultimap<String, Integer> extraNameToIdsMap;
	// when a node has multiple names separated by "==", store all those after the first here

	private HashMultimap<String, Integer> nameToIdsMap;// = new HashMap<String, Integer>();
	private ConcurrentHashMap<String, Integer> nameToUniqueIdMap; // = new HashMap<String, Integer>();

//	BiMap<Integer, PhylogenyNode<String>> intToNodeMap = new HashBiMap<Integer, PhylogenyNode<String>>();
//	Multimap<String, PhylogenyNode<String>> nameToNodeMap = new HashMultimap<String, PhylogenyNode<String>>();

//	NewickTaxonomyService stringTaxonomyService;// = new NewickTaxonomyService(hugenholtzFilename);

	public Set<Integer> getLeafIds()
		{
		return theIntegerTree.getLeafValues();
		}

	public HugenholtzTaxonomyService() //throws PhyloUtilsException
		{
		theIntegerTree = (BasicRootedPhylogeny<Integer>) CacheManager.get(this, hugenholtzFilename + ".theIntegerTree");
		nameToIdsMap = (HashMultimap<String, Integer>) CacheManager.get(this, hugenholtzFilename + ".nameToIdsMap");
		extraNameToIdsMap =
				(HashMultimap<String, Integer>) CacheManager.get(this, hugenholtzFilename + ".extraNameToIdsMap");
		nameToUniqueIdMap =
				(ConcurrentHashMap<String, Integer>) CacheManager.get(this, hugenholtzFilename + ".nameToUniqueIdMap");

		if (theIntegerTree == null || nameToIdsMap == null || nameToUniqueIdMap == null)
			{
			reloadFromNewick();
			reloadOverrideMap();
			// ** Note we don't invalidate downstream caches, e.g. for StrainDirectoryLabelChooser and so forth
			// CacheManager.invalidate
			CacheManager.put(this, hugenholtzFilename + ".theIntegerTree", theIntegerTree);
			CacheManager.put(this, hugenholtzFilename + ".nameToIdsMap", nameToIdsMap);
			CacheManager.put(this, hugenholtzFilename + ".extraNameToIdsMap", extraNameToIdsMap);
			CacheManager.put(this, hugenholtzFilename + ".nameToUniqueIdMap", nameToUniqueIdMap);
			}

		/*if (!readStateIfAvailable())
			{
			reloadFromNewick();
			//invalidateDependentCaches();
			saveState();
			}*/
		}

	private synchronized void reloadOverrideMap()
		{
		nameToUniqueIdMap = new ConcurrentHashMap<String, Integer>();

		Map<String, Set<Integer>> overrideNameToIdMap;
		try
			{
			overrideNameToIdMap = StringSetIntMapReader.read(overrideFilename);

			for (Map.Entry<String, Set<Integer>> entry : overrideNameToIdMap.entrySet())
				{
				String key = entry.getKey();
				Set<Integer> valueSet = entry.getValue();

				nameToIdsMap.removeAll(key);
				nameToUniqueIdMap.remove(key);

				nameToIdsMap.putAll(key, valueSet);

				for (Integer id : valueSet)
					{
					nameToUniqueIdMap.put(key, id);
					}
				}
			}
		catch (IOException e)
			{
			throw new Error(e);
			}
		}

	public BasicRootedPhylogeny<Integer> getRandomSubtree(int numTaxa, Double mergeThreshold)
		{
		throw new NotImplementedException();
		}

	public BasicRootedPhylogeny<Integer> getRandomSubtree(int numTaxa, Double mergeThreshold,
	                                                      Integer exceptDescendantsOf)
		{
		throw new NotImplementedException();
		}

	private synchronized void reloadFromNewick()
		{
		nameToIdsMap = HashMultimap.create();
		extraNameToIdsMap = HashMultimap.create();

		//** here we assume that the tree has already been converted to have named nodes at leaves, using the NewickParser command-line tool
		// else we'd need new NewickTaxonomyService(hugenholtzFilename, truel
		// );
		NewickTaxonomyService stringTaxonomyService = new NewickTaxonomyService(hugenholtzFilename, false);

		BasicRootedPhylogeny<String> theStringTree = stringTaxonomyService.getTree();

		//** because the node children are iterated in random order in the course of the depth-first copy,
		// the random IDs won't be consistently assigned from one run to the next.

		theIntegerTree = PhylogenyTypeConverter
				.convertToIDTree(theStringTree, new RequireExistingNodeNamer(false), new TaxonStringIdMapper<Integer>()
				{
				public Integer findTaxidByNameRelaxed(String name) throws NoSuchNodeException
					{
					return findTaxidByName(name);
					}

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

				public Set<String> getCachedNamesForId(Integer id)
					{
					return DSCollectionUtils.setOf("" + id);
					}
				}, nameToIdsMap, extraNameToIdsMap);


		//BAD	addStrainNamesToMap();
		}

	private synchronized static InputStream getInputStream(String filename) throws PhyloUtilsException, IOException
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
				res = f.toURI().toURL(); // new URL("file://" + filename);
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
	private synchronized void addStrainNamesToMap()
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

	/*
	 String cacheFilename = "/phyloutils.hugenholtz.cache";

	 public void saveState()
		 {
		 try
			 {
			 File cacheFile = new File(EnvironmentUtils.getCacheRoot() + cacheFilename);
			 cacheFile.getParentFile().mkdirs();
			 FileOutputStream fout = new FileOutputStream(cacheFile);
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
			 FileInputStream fin = new FileInputStream(EnvironmentUtils.getCacheRoot() + cacheFilename);
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
			 logger.warn("Could not read Hugenholtz cache; rereading source files", e);
			 }
		 catch (ClassNotFoundException e)
			 {// no problem
			 logger.warn("Could not read Hugenholtz cache; rereading source files", e);
			 }
		 return false;
		 }
 */
	public synchronized boolean isLeaf(Integer leafId) throws NoSuchNodeException
		{
		return theIntegerTree.getNode(leafId).isLeaf();
		}

	public synchronized boolean isKnown(Integer leafId) //throws NoSuchNodeException
		{
		try
			{
			theIntegerTree.getNode(leafId);
			return true;
			}
		catch (NoSuchNodeException e)
			{
			return false;
			}
		}


	@NotNull
	public Integer findTaxidByNameRelaxed(String name) throws NoSuchNodeException
		{
		return findTaxidByName(name);
		}

	public synchronized Set<String> getCachedNamesForId(Integer id)
		{
		//PERF, need a BiMultiMap or something
		Set<String> result = new HashSet<String>();
		for (Map.Entry<String, Integer> entry : nameToUniqueIdMap.entrySet())
			{
			if (entry.getValue().equals(id))
				{
				result.add(entry.getKey());
				}
			}
		for (Map.Entry<String, Integer> entry : nameToIdsMap.entries())
			{
			if (entry.getValue().equals(id))
				{
				result.add(entry.getKey());
				}
			}
		return result;
		}

	@NotNull
	public synchronized Integer findTaxidByName(String name) throws NoSuchNodeException
		{
		Integer result = nameToUniqueIdMap.get(name);

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
					try
						{
						BasicRootedPhylogeny<Integer> bTree = findSubtreeByNameRelaxed(name);
						result = bTree.getShallowestLeaf();
						}
					catch (NoSuchNodeException e)
						{
						result = NO_VALID_ID;
						}

					//result = getUniqueNodeForName(name);

					// REVIEW for our present purposes we always want the worst-case node; but in other contexts that may be the wrong thing to do

					//	result = getDeepestNodeForName(name);
					}
				else
					{
					result = getUniqueNodeForMultilevelName(name.split("[; ]+"));
					}
				}
			nameToUniqueIdMap.put(name, result);
			}

		if (result.equals(NO_VALID_ID))
			{
			throw new NoSuchNodeException();
			}

		return result;
		}

/*	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws PhyloUtilsException
		{

		}
*/


	private synchronized Integer commonAncestor(Set<Deque<Integer>> paths) throws NoSuchNodeException
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
			return theIntegerTree.commonAncestor(leafIds, 0.75);
			}
		}


	// bottom-up search

	@NotNull
	private synchronized Integer getUniqueNodeForMultilevelName(String[] taxa) throws NoSuchNodeException
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
				matchingNodes = extraNameToIdsMap.get(s);
				}
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

/*	@NotNull
	private Integer getUniqueNodeForName(String name) throws NoSuchNodeException
		{
		return findSubtreeByName(name).getValue();
		}

	public RootedPhylogeny<Integer> findSubtreeByName(String name) throws NoSuchNodeException
		{
		Collection<Integer> matchingIds = findMatchingIds(name);

		if (matchingIds.size() == 0)
			{
			throw new NoSuchNodeException();
			}

		return findSubtreeWithIds(matchingIds, name);
		}
*/


	private synchronized BasicRootedPhylogeny<Integer> findSubtreeByNameRelaxed(String name) throws NoSuchNodeException
		{
		Set<Integer> matchingIds = findMatchingIdsRelaxed(name);

		if (matchingIds.size() == 0)
			{
			throw new NoSuchNodeException();
			}

		return findCompactSubtreeWithIds(matchingIds, name);
		}


	/*
	 @NotNull
	 public Integer getDeepestNodeForName(String name) throws NoSuchNodeException
		 {
		 ///Integer result;
		 Collection<Integer> matchingIds = findMatchingIdsRelaxed(name);

		 //	PhylogenyNode<Integer> deepestNode;
		 Integer deepestId = null;
		 double deepestDepth = Double.NEGATIVE_INFINITY;

		 for (Integer id : matchingIds)
			 {
			 //PhylogenyNode<Integer> n = theIntegerTree.getNode(id);
			 double depth = getDepthFromRoot(id);
			 if (depth > deepestDepth)
				 {
				 deepestDepth = depth;
				 deepestId = id;
				 }
			 }

		 //assert theIntegerTree.getNode(deepestId).isLeaf();

		 return deepestId;
		 }

	 @NotNull
	 public Integer getShallowestNodeForName(String name) throws NoSuchNodeException
		 {
		 ///Integer result;
		 Collection<Integer> matchingIds = findMatchingIdsRelaxed(name);

		 //	PhylogenyNode<Integer> deepestNode;
		 Integer shallowestId = null;
		 double shallowestDepth = Double.POSITIVE_INFINITY;

		 for (Integer id : matchingIds)
			 {
			 //PhylogenyNode<Integer> n = theIntegerTree.getNode(id);
			 double depth = getDepthFromRoot(id);
			 if (depth < shallowestDepth)
				 {
				 shallowestDepth = depth;
				 shallowestId = id;
				 }
			 }

		 //assert theIntegerTree.getNode(deepestId).isLeaf();

		 return shallowestId;
		 }
 */
	public synchronized Set<Integer> findMatchingIds(String name) throws NoSuchNodeException
		{
		Set<Integer> matchingIds = nameToIdsMap.get(name);
		if (matchingIds.isEmpty())
			{
			throw new NoSuchNodeException("Node not found: " + name);
			}
		return matchingIds;
		}

	public synchronized Set<Integer> findMatchingIdsRelaxed(String name) throws NoSuchNodeException
		{
		Set<Integer> matchingIds = nameToIdsMap.get(name);
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
				logger.warn("Relaxed name " + name + " to " + shortName);
				}
			}
		if (matchingIds.isEmpty())
			{
			throw new NoSuchNodeException("Node not found: " + name + "; no id found even for " + shortName);
			}
		shortNames.put(name, shortName);
		return matchingIds;
		}

//	WeightedSet<String> depthsBelow = new HashWeightedSet<String>(); // for debugging

	private Map<String, String> shortNames = new HashMap<String, String>();

	public synchronized String getRelaxedName(String name)
		{
		return shortNames.get(name);
		}

	/*	public void printDepthsBelow()
		{
		for (String name : depthsBelow.keysInDecreasingWeightOrder())
			{
			double depthBelow = depthsBelow.get(name);
			String shortName = shortNames.get(name);

			logger.info(String.format("Depth below = %.3f for %s relaxed from %s", depthBelow, shortName, name));
			}
		}*/

	public synchronized boolean isDescendant(Integer ancestor, Integer descendant) throws NoSuchNodeException
		{
		return theIntegerTree.isDescendant(ancestor, descendant);
//		return stringTaxonomyService.isDescendant(intToNodeMap.get(ancestor), intToNodeMap.get(descendant));
		}

	public Set<Integer> selectAncestors(final Set<Integer> labels, final Integer id)
		{
		return theIntegerTree.selectAncestors(labels, id);
		}

	/*public double exactDistanceBetween(Integer a, Integer b)
		{
		return stringTaxonomyService.distanceBetween(intToNodeMap(a), intToNodeMap(b));
		}
*/
	public synchronized double minDistanceBetween(Integer a, Integer b) throws NoSuchNodeException
		{
		return theIntegerTree.distanceBetween(a, b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}

	public synchronized double getDepthFromRoot(Integer b) throws NoSuchNodeException
		{
		return theIntegerTree.distanceBetween(theIntegerTree.getRoot().getPayload(), b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}

	public synchronized double getGreatestDepthBelow(Integer taxid) throws NoSuchNodeException
		{
		return theIntegerTree.getNode(taxid).getGreatestBranchLengthDepthBelow();
		}

	private Double maxDistance = null;

	public synchronized double maxDistance()
		{
		if (maxDistance == null)
			{
			maxDistance = 2.0 * theIntegerTree.getRoot().getGreatestBranchLengthDepthBelow();
			}
		return maxDistance;
		}

	/**
	 * Just return the argument even if the branch length is zero
	 *
	 * @param id the T identifying the starting node
	 * @return
	 * @throws NoSuchNodeException
	 */
	public synchronized Integer nearestAncestorWithBranchLength(Integer id) throws NoSuchNodeException
		{
		//checkNodeExists(id);
		theIntegerTree.getNode(id);  // test exists
		return id;

		//return theIntegerTree.nearestAncestorWithBranchLength(id);
//		return intToNodeMap.inverse().get(intToNodeMap.get(id).nearestAncestorWithBranchLength());
		}


	public List<Integer> getAncestorPathIds(final Integer id) throws NoSuchNodeException
		{
		return theIntegerTree.getAncestorPathIds(id);
		}

	public List<PhylogenyNode<Integer>> getAncestorPath(final Integer id) throws NoSuchNodeException
		{
		return theIntegerTree.getAncestorPath(id);
		}

	public List<BasicPhylogenyNode<Integer>> getAncestorPathAsBasic(final Integer id) throws NoSuchNodeException
		{
		return theIntegerTree.getAncestorPathAsBasic(id);
		}

/*	public Integer nearestAncestorAtRank(final String rankName, Integer leafId) throws NoSuchNodeException
		{
		if(synonymService)
		}
*/

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

/*	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws NoSuchNodeException
		{
		return extractTreeWithLeafIDs(ids, false, false);
		}*/

	/*
	 public RootedPhylogeny<Integer> extractTreeWithLeaves(Collection<PhylogenyNode<Integer>> ids)
			 throws PhyloUtilsException
		 {
		 throw new NotImplementedException();
		 }
 */

	public synchronized BasicRootedPhylogeny<Integer> extractTreeWithLeafIDs(Set<Integer> ids,
	                                                                         boolean ignoreAbsentNodes,
	                                                                         boolean includeInternalBranches,
	                                                                         AbstractRootedPhylogeny.MutualExclusionResolutionMode mode)
			throws NoSuchNodeException //, NodeNamer<Integer> namer
		{
		return theIntegerTree.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches, mode); //, namer);
		}

	public synchronized BasicRootedPhylogeny<Integer> extractTreeWithLeafIDs(Set<Integer> ids,
	                                                                         boolean ignoreAbsentNodes,
	                                                                         boolean includeInternalBranches)
			throws NoSuchNodeException //, NodeNamer<Integer> namer
		{
		return theIntegerTree.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches); //, namer);
		}


	@Override
	public synchronized String toString()
		{
		String shortname = getClass().getName();
		shortname = shortname.substring(shortname.lastIndexOf(".") + 1);
		return shortname;
		}
/*
	public Integer findTaxIdOfShallowestLeaf(String name) throws NoSuchNodeException
		{
		RootedPhylogeny<Integer> bTree = findTreeForName(name);
		return bTree.getShallowestLeaf();
		}
*/
/*	public int getNumNodesForName(String name)

		{
		int mappedBIds = 0;
		Set<Integer> idBSet = nameToIdsMap.get(name);
		if (idBSet != null)
			{
			mappedBIds = idBSet.size();
			}
		return mappedBIds;
		}
*/


	/*
	 public RootedPhylogeny<Integer> findTreeForName(String name) throws NoSuchNodeException
		 {
		 Set<Integer> idBSet = nameToIdsMap.get(name);

		 RootedPhylogeny<Integer> bTree;

		 if (idBSet == null)
			 {
			 //logger.warn("No mapping for ID: " + idA);
			 //System.err.printf("%s\t%d\tNOMAP\t0\t0\t0\t0\n", name, idA);
			 try
				 {
				 bTree = findSubtreeByName(name);
				 }
			 catch (NoSuchNodeException e)
				 {
				 //logger.warn("No leaf IDs are classified on the tree: " + name);
				 //System.err.printf("%s\t%d\tUNCLASSIFIED\t0\t0\t0\t0\n", name, idA);
				 bTree = findSubtreeByNameRelaxed(name);
				 }
			 }
		 else
			 {
			 try
				 {
				 bTree = extractTreeWithLeafIDs(idBSet, true, true);
				 PhylogenyNode<Integer> r = bTree.getFirstBranchingNode();
				 bTree = r.asRootedPhylogeny();
				 }
			 catch (NoSuchNodeException e)
				 {
				 //logger.warn("No leaf IDs are classified on the tree: " + name);
				 //System.err.printf("%s\t%d\tUNCLASSIFIED\t0\t0\t0\t0\n", name, idA);
				 bTree = findSubtreeByNameRelaxed(name);
				 }
			 }
		 return bTree;
		 }
 */

	public synchronized BasicRootedPhylogeny<Integer> findTreeForIds(Set<Integer> idBSet)
		{
		try
			{
			BasicRootedPhylogeny<Integer> bTree = extractTreeWithLeafIDs(idBSet, true, true,
			                                                             AbstractRootedPhylogeny.MutualExclusionResolutionMode.BOTH);
			BasicPhylogenyNode<Integer> r = bTree.getFirstBranchingNode();
			bTree = r.asRootedPhylogeny();
			return bTree;
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			throw new Error("Impossible");
			}
		}


	public synchronized BasicRootedPhylogeny<Integer> findCompactSubtreeWithIds(Set<Integer> matchingIds, String name)
			throws NoSuchNodeException
		{
		BasicRootedPhylogeny<Integer> tree = extractTreeWithLeafIDs(matchingIds, true, true,
		                                                            AbstractRootedPhylogeny.MutualExclusionResolutionMode.BOTH);
		BasicPhylogenyNode<Integer> result = tree.getFirstBranchingNode();

		double span = result.getLargestLengthSpan();
		if (span > 0.1)
			{
			logger.warn("Subtree for " + name + " has span = " + span + ", trying 75% solution");
			Integer sub = tree.commonAncestor(matchingIds, 0.75);
			result = tree.getNode(sub);
			span = result.getLargestLengthSpan();
			logger.warn("75% subtree for " + name + " has span = " + span);
			}

		result = result;

		//result = tree.commonAncestor(matchingIds, 0.75);
		//throw new PhyloUtilsException("Name not unique: " + name);


		//double depthBelow = theIntegerTree.getNode(result).getGreatestBranchLengthDepthBelow();

		//depthsBelow.add(name, depthBelow);

		//logger.info("Node found for name " + name + " has depth below = " + jdepthBelow);

		return result.asRootedPhylogeny();
		}

	public Integer getLeafAtApproximateDistance(final Integer aId, final double minDesiredTreeDistance,
	                                            final double maxDesiredTreeDistance) throws NoSuchNodeException
		{
		return theIntegerTree.getLeafAtApproximateDistance(aId, minDesiredTreeDistance, maxDesiredTreeDistance);
		}
	}
