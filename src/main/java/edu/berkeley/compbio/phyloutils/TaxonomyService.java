package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.dsutils.tree.TreeException;

import java.util.Map;
import java.util.Set;

/**
 * Provides a simplified interface for taxonomy sources (e.g., NCBI, or a Newick file) that doesn't provide all the
 * services of a full RootedTree.  In particular none of the API involves PhylogenyNodes; everything is done by ID (of
 * the generic type)
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonomyService<T> extends TaxonMergingPhylogeny<T>, TaxonStringIdMapper<T>
	{
	boolean isDescendant(T ancestor, T descendant) throws NoSuchNodeException;

	//boolean isDescendant(PhylogenyNode<T> ancestor, PhylogenyNode<T> descendant) throws PhyloUtilsException;

//	void saveState();

	double minDistanceBetween(T name1, T name2) throws NoSuchNodeException;

	//Double minDistanceBetween(PhylogenyNode<T> node1, PhylogenyNode<T> node2) throws PhyloUtilsException;

	//PhylogenyNode<T> getRoot();

	void setSynonymService(TaxonomySynonymService taxonomySynonymService);

	RootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold) throws NoSuchNodeException, TreeException;

	RootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold, T exceptDescendantsOf)
			throws NoSuchNodeException, TreeException;

	boolean isLeaf(T leafId) throws NoSuchNodeException;

	double getDepthFromRoot(T taxid) throws NoSuchNodeException;

	double getGreatestDepthBelow(T taxid) throws NoSuchNodeException;

	double maxDistance();

//	void printDepthsBelow();

	//RootedPhylogeny<Integer> findSubtreeByName(String name) throws NoSuchNodeException;

//	RootedPhylogeny<Integer> findSubtreeByNameRelaxed(String name) throws NoSuchNodeException;

	String getRelaxedName(String name);

	//T findTaxIdOfShallowestLeaf(String name) throws NoSuchNodeException;

	//RootedPhylogeny<Integer> findTreeForName(String name) throws NoSuchNodeException;

	RootedPhylogeny<T> findCompactSubtreeWithIds(Set<T> matchingIds, String name) throws NoSuchNodeException;

//	int getNumNodesForName(String name);

//	Set<Integer> getAllIdsForName(String name);

	Set<T> findMatchingIds(String name) throws NoSuchNodeException;

	Set<T> findMatchingIdsRelaxed(String name) throws NoSuchNodeException;

//	T nearestAncestorAtRank(String levelName, Integer leafId) throws NoSuchNodeException;

	Set<T> selectAncestors(Set<T> labels, T id);

//	 List<T> getAncestorPathIds(T id) throws NoSuchNodeException;

	Set<T> getLeafIds();

	Map<T, String> getFriendlyLabelMap();
	}
