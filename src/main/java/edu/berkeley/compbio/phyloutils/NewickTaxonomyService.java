package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.dsutils.tree.TreeException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NewickTaxonomyService
		implements TaxonomyService<String>, Serializable  // extends AbstractRootedPhylogeny<String>
	{
	private static final Logger logger = Logger.getLogger(NewickTaxonomyService.class);

	protected RootedPhylogeny<String> basePhylogeny;
	private String filename;  // only for toString

	/*	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		 {
		 return ciccarelliTree.distanceBetween(taxIdA, taxIdB);
		 }
 */

	protected NewickTaxonomyService(String filename, boolean namedNodesMustBeLeaves)// throws  PhyloUtilsException
		{
		this.filename = filename;
		try
			{
			/*		URL res = ClassLoader.getSystemResource(ciccarelliFilename);
						if (res == null)
							{
							logger.error("Ciccarelli tree not found: " + ciccarelliFilename);
							//Get the System Classloader
							ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

							//Get the URLs
							URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

							for (int i = 0; i < urls.length; i++)
								{
								logger.warn(urls[i].getFile());
								}

							return;
							}
						InputStream is = res.openStream();*/
			/*if (is == null)
				{
				is = new FileInputStream(filename);
				}*/
			//	ciccarelliTree = new NewickParser<String>().read(is, new StringNodeNamer("UNNAMED NODE "));
			basePhylogeny = NewickParser.readWithStringIds(filename, namedNodesMustBeLeaves);
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
		}


	public double exactDistanceBetween(String a, String b) throws NoSuchNodeException
		{
		return basePhylogeny.distanceBetween(a, b);
		}


	public double exactDistanceBetween(PhylogenyNode<String> a, PhylogenyNode<String> b) throws NoSuchNodeException
		{
		return basePhylogeny.distanceBetween(a, b);
		}

	public double getGreatestDepthBelow(String a) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(a).getGreatestBranchLengthDepthBelow();
		}

	private Double maxDistance = null;

	public double maxDistance()
		{
		if (maxDistance == null)
			{
			maxDistance = 2.0 * getRoot().getGreatestBranchLengthDepthBelow();
			}
		return maxDistance;
		}

	public void printDepthsBelow()
		{
		}
/*
	public RootedPhylogeny<String> extractTreeWithLeafIDs(Collection<String> ids) throws NoSuchNodeException
		{
		return basePhylogeny.extractTreeWithLeafIDs(ids);
		}*/

	public PhylogenyNode<String> getRoot()
		{
		return basePhylogeny;
		}

	public boolean isLeaf(String leafId) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(leafId).isLeaf();
		}

	public RootedPhylogeny<String> getTree()
		{
		return basePhylogeny;
		}

	public RootedPhylogeny<String> getRandomSubtree(int numTaxa, Double mergeThreshold)
			throws NoSuchNodeException, TreeException
		{
		return getRandomSubtree(numTaxa, mergeThreshold, null);
		}

	public RootedPhylogeny<String> getRandomSubtree(int numTaxa, String exceptDescendantsOf)
			throws TreeException, NoSuchNodeException
		{
		return getRandomSubtree(numTaxa, null, exceptDescendantsOf);
		}

	public RootedPhylogeny<String> getRandomSubtree(int numTaxa, Double mergeThreshold, String exceptDescendantsOf)
			throws TreeException, NoSuchNodeException
		{
		Collection<String> mergedIds;
		if (mergeThreshold != null)
			{
			Map<String, Set<String>> mergeIdSets =
					TaxonMerger.merge(basePhylogeny.getLeafValues(), this, mergeThreshold);
			mergedIds = mergeIdSets.keySet();
			}
		else
			{
			mergedIds = basePhylogeny.getLeafValues();
			}

		if (exceptDescendantsOf != null)
			{
			for (Iterator<String> iter = mergedIds.iterator(); iter.hasNext();)
				{
				String id = iter.next();
				if (isDescendant(exceptDescendantsOf, id))
					{
					iter.remove();
					}
				}
			}

		DSCollectionUtils.retainRandom(mergedIds, numTaxa);
		return basePhylogeny.extractTreeWithLeafIDs(mergedIds, false, true);
		}

	public String findTaxidByName(String name) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(name).getValue();
		}

	public String findTaxidByNameRelaxed(String name) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(name).getValue();
		}

	public Set<String> getCachedNamesForId(String id)
		{
		String s = null;
		try
			{
			s = basePhylogeny.getNode(id).getValue();
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			return new HashSet<String>();
			}
		return DSCollectionUtils.setOf(s);
		}

	public boolean isDescendant(String ancestor, String descendant) throws NoSuchNodeException
		{
		return basePhylogeny.isDescendant(ancestor, descendant);
		}

	/*
	 public void saveState()
		 {
		 basePhylogeny.saveState();
		 }
 */
	public double minDistanceBetween(String name1, String name2) throws NoSuchNodeException //throws PhyloUtilsException
		{
		return exactDistanceBetween(name1, name2);
		}

	public double getDepthFromRoot(String b) throws NoSuchNodeException
		{
		return exactDistanceBetween(getRoot().getValue(), b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}

	public String nearestAncestorWithBranchLength(String id) throws NoSuchNodeException
		{
		return basePhylogeny.nearestAncestorWithBranchLength(id);
		}

/*	public RootedPhylogeny<String> extractTreeWithLeaves(Collection<PhylogenyNode<String>> ids)
			throws PhyloUtilsException
		{
		return basePhylogeny.extractTreeWithLeaves(ids);
		}*/

	public RootedPhylogeny<String> extractTreeWithLeafIDs(Collection<String> ids, boolean ignoreAbsentNodes,
	                                                      boolean includeInternalBranches)
			throws NoSuchNodeException  //, NodeNamer<String> namer
		{
		return basePhylogeny.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches); //, namer);
		}

	public boolean isDescendant(PhylogenyNode<String> ancestor, PhylogenyNode<String> descendant)
			throws PhyloUtilsException
		{
		return basePhylogeny.isDescendant(ancestor, descendant);
		}

	public Double minDistanceBetween(PhylogenyNode<String> node1, PhylogenyNode<String> node2)
			throws PhyloUtilsException, NoSuchNodeException
		{
		return exactDistanceBetween(node1, node2);
		}

	@Override
	public String toString()
		{
		return "NewickTaxonomyService{" + filename + '}';
		}


	public void setSynonymService(TaxonomySynonymService taxonomySynonymService)
		{
		throw new NotImplementedException(
				"Newick taxonomy doesn't currently use other synonym services for any purpose");
		}

	public RootedPhylogeny<Integer> findSubtreeByName(String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public RootedPhylogeny<Integer> findSubtreeByNameRelaxed(String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public String getRelaxedName(String name)
		{
		throw new NotImplementedException();
		}
	}
