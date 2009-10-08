package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.dsutils.tree.TreeException;
import org.apache.commons.lang.NotImplementedException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class RootedPhylogenyAsService<T extends Serializable> implements TaxonomyService<T>
	{
	protected RootedPhylogeny<T> basePhylogeny;

	public RootedPhylogenyAsService(final RootedPhylogeny<T> basePhylogeny)
		{
		this.basePhylogeny = basePhylogeny;
		}

	public RootedPhylogenyAsService()
		{
		}

	public Set<T> getLeafIds()
		{
		return basePhylogeny.getLeafValues();
		}


	public double exactDistanceBetween(T a, T b) throws NoSuchNodeException
		{
		return basePhylogeny.distanceBetween(a, b);
		}


	public double exactDistanceBetween(PhylogenyNode<T> a, PhylogenyNode<T> b) throws NoSuchNodeException
		{
		return basePhylogeny.distanceBetween(a, b);
		}

	public double getGreatestDepthBelow(T a) throws NoSuchNodeException
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

	public PhylogenyNode<T> getRoot()
		{
		return basePhylogeny;
		}

	public boolean isLeaf(T leafId) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(leafId).isLeaf();
		}

	public synchronized boolean isKnown(T leafId) //throws NoSuchNodeException
		{
		try
			{
			basePhylogeny.getNode(leafId);
			return true;
			}
		catch (NoSuchNodeException e)
			{
			return false;
			}
		}

	public RootedPhylogeny<T> getTree()
		{
		return basePhylogeny;
		}

	public BasicRootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold)
			throws NoSuchNodeException, TreeException
		{
		return getRandomSubtree(numTaxa, mergeThreshold, null);
		}

	public BasicRootedPhylogeny<T> getRandomSubtree(int numTaxa, T exceptDescendantsOf)
			throws TreeException, NoSuchNodeException
		{
		return getRandomSubtree(numTaxa, null, exceptDescendantsOf);
		}

	public BasicRootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold, T exceptDescendantsOf)
			throws TreeException, NoSuchNodeException
		{
		Set<T> mergedIds;
		if (mergeThreshold != null)
			{
			Map<T, Set<T>> mergeIdSets = TaxonMerger.merge(basePhylogeny.getLeafValues(), this, mergeThreshold);
			mergedIds = mergeIdSets.keySet();
			}
		else
			{
			mergedIds = basePhylogeny.getLeafValues();
			}

		if (exceptDescendantsOf != null)
			{
			for (Iterator<T> iter = mergedIds.iterator(); iter.hasNext();)
				{
				T id = iter.next();
				if (isDescendant(exceptDescendantsOf, id))
					{
					iter.remove();
					}
				}
			}

		DSCollectionUtils.retainRandom(mergedIds, numTaxa);
		return basePhylogeny.extractTreeWithLeafIDs(mergedIds, false, true);
		}


	public boolean isDescendant(T ancestor, T descendant) throws NoSuchNodeException
		{
		return basePhylogeny.isDescendant(ancestor, descendant);
		}

	public Set<T> selectAncestors(final Collection<T> labels, final T id)
		{
		return basePhylogeny.selectAncestors(labels, id);
		}

	/*
	 public void saveState()
		 {
		 basePhylogeny.saveState();
		 }
 */
	public double minDistanceBetween(T name1, T name2) throws NoSuchNodeException //throws PhyloUtilsException
		{
		return exactDistanceBetween(name1, name2);
		}

	public double getDepthFromRoot(T b) throws NoSuchNodeException
		{
		return exactDistanceBetween(getRoot().getPayload(), b);
		//return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}

	public T nearestAncestorWithBranchLength(T id) throws NoSuchNodeException
		{
		return basePhylogeny.nearestAncestorWithBranchLength(id);
		}


	public List<T> getAncestorPathIds(final T id) throws NoSuchNodeException
		{
		return basePhylogeny.getAncestorPathIds(id);
		}

	/*	public List<PhylogenyNode<String>> getAncestorPath(final String id) throws NoSuchNodeException
		 {
		 return basePhylogeny.getAncestorPath(id);
		 }
 */
	public List<BasicPhylogenyNode<T>> getAncestorPathAsBasic(final T id) throws NoSuchNodeException
		{
		return basePhylogeny.getAncestorPathAsBasic(id);
		}

/*	public RootedPhylogeny<String> extractTreeWithLeaves(Collection<PhylogenyNode<String>> ids)
			throws PhyloUtilsException
		{
		return basePhylogeny.extractTreeWithLeaves(ids);
		}*/

	public BasicRootedPhylogeny<T> extractTreeWithLeafIDs(Set<T> ids, boolean ignoreAbsentNodes,
	                                                      boolean includeInternalBranches)
			throws NoSuchNodeException  //, NodeNamer<String> namer
		{
		return basePhylogeny.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches); //, namer);
		}

	public BasicRootedPhylogeny<T> extractTreeWithLeafIDs(Set<T> ids, boolean ignoreAbsentNodes,
	                                                      boolean includeInternalBranches,
	                                                      AbstractRootedPhylogeny.MutualExclusionResolutionMode mode)
			throws NoSuchNodeException  //, NodeNamer<String> namer
		{
		return basePhylogeny.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches, mode); //, namer);
		}

	public boolean isDescendant(PhylogenyNode<T> ancestor, PhylogenyNode<T> descendant) throws PhyloUtilsException
		{
		return basePhylogeny.isDescendant(ancestor, descendant);
		}

	public Double minDistanceBetween(PhylogenyNode<T> node1, PhylogenyNode<T> node2)
			throws PhyloUtilsException, NoSuchNodeException
		{
		return exactDistanceBetween(node1, node2);
		}


	public void setSynonymService(TaxonomySynonymService taxonomySynonymService)
		{
		throw new NotImplementedException(
				"Newick taxonomy doesn't currently use other synonym services for any purpose");
		}

	/*	public RootedPhylogeny<Integer> findSubtreeByName(String name) throws NoSuchNodeException
		 {
		 throw new NotImplementedException();
		 }

	 public RootedPhylogeny<Integer> findSubtreeByNameRelaxed(String name) throws NoSuchNodeException
		 {
		 throw new NotImplementedException();
		 }
 */
	public String getRelaxedName(String name)
		{
		throw new NotImplementedException();
		}
/*
	public RootedPhylogeny<Integer> findTreeForName(String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public String findTaxIdOfShallowestLeaf(String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}


	public int getNumNodesForName(String name)
		{
		throw new NotImplementedException();
		}*/


	public BasicRootedPhylogeny<T> findCompactSubtreeWithIds(Set<T> matchingIds, String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public Set<T> findMatchingIds(String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public Set<T> findMatchingIdsRelaxed(String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public String nearestAncestorAtRank(final String rankName, Integer leafId) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public T getLeafAtApproximateDistance(final T aId, final double minDesiredTreeDistance,
	                                      final double maxDesiredTreeDistance) throws NoSuchNodeException
		{
		return basePhylogeny.getLeafAtApproximateDistance(aId, minDesiredTreeDistance, maxDesiredTreeDistance);
		}

	public Map<T, String> getFriendlyLabelMap()
		{
		return null;
		}

	public T findTaxidByName(final String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public T findTaxidByNameRelaxed(final String name) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	public Set<String> getCachedNamesForId(final T id)
		{
		throw new NotImplementedException();
		}
	}
