package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.BasicRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.TaxonMergingPhylogeny;
import com.davidsoergel.trees.TreeException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides a simplified interface for taxonomy sources (e.g., NCBI, or a Newick file) that doesn't provide all the
 * services of a full RootedTree.  In particular none of the API involves PhylogenyNodes; everything is done by ID (of
 * the generic type).  Well, that's not entirely true anymore due to returning RootedPhylogenies, but at least those
 * should all be Serializable.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonomyService<T extends Serializable> extends TaxonMergingPhylogeny<T>, TaxonStringIdMapper<T>
	{
	boolean isDescendant(T ancestor, T descendant) throws NoSuchNodeException;

	//boolean isDescendant(PhylogenyNode<T> ancestor, PhylogenyNode<T> descendant) throws PhyloUtilsException;

//	void saveState();

	double minDistanceBetween(T name1, T name2) throws NoSuchNodeException;

	//Double minDistanceBetween(PhylogenyNode<T> node1, PhylogenyNode<T> node2) throws PhyloUtilsException;

	//PhylogenyNode<T> getRoot();

	void setSynonymService(TaxonomySynonymService taxonomySynonymService);

	BasicRootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold)
			throws NoSuchNodeException, TreeException;

	BasicRootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold, T exceptDescendantsOf)
			throws NoSuchNodeException, TreeException;

	boolean isLeaf(T leafId) throws NoSuchNodeException;

	double getDepthFromRoot(T taxid) throws NoSuchNodeException;

	double getGreatestDepthBelow(T taxid) throws NoSuchNodeException;

	double getLargestLengthSpan(T taxid) throws NoSuchNodeException;

	double maxDistance();

//	void printDepthsBelow();

	//RootedPhylogeny<Integer> findSubtreeByName(String name) throws NoSuchNodeException;

//	RootedPhylogeny<Integer> findSubtreeByNameRelaxed(String name) throws NoSuchNodeException;

	String getRelaxedName(String name);

	//T findTaxIdOfShallowestLeaf(String name) throws NoSuchNodeException;

	//RootedPhylogeny<Integer> findTreeForName(String name) throws NoSuchNodeException;

	BasicRootedPhylogeny<T> findCompactSubtreeWithIds(Set<T> matchingIds, String name) throws NoSuchNodeException;

//	int getNumNodesForName(String name);

//	Set<Integer> getAllIdsForName(String name);

	// may as well make this a synonym service too since scientific names are in the same file
	String getScientificName(T from) throws NoSuchNodeException;

	Collection<String> getAllNamesForIds(Set<T> ids);

	Set<T> findMatchingIds(String name) throws NoSuchNodeException;

	Set<T> findMatchingIdsRelaxed(String name) throws NoSuchNodeException;

//	T nearestAncestorAtRank(String levelName, Integer leafId) throws NoSuchNodeException;

	Set<T> selectAncestors(Collection<T> labels, T id);

//	 List<T> getAncestorPathIds(T id) throws NoSuchNodeException;

	Set<T> getLeafIds();

	Map<T, String> getFriendlyLabelMap();

	/**
	 * Try to find a leaf at the given distance from the given node
	 *
	 * @param aId
	 * @param minDesiredTreeDistance
	 * @param maxDesiredTreeDistance
	 * @return
	 */
	public T getLeafAtApproximateDistance(final T aId, final double minDesiredTreeDistance,
	                                      final double maxDesiredTreeDistance) throws NoSuchNodeException;

	boolean isKnown(T value);
	}
