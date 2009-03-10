package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.dsutils.tree.TreeException;

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

	void saveState();

	Double minDistanceBetween(T name1, T name2) throws NoSuchNodeException;

	//Double minDistanceBetween(PhylogenyNode<T> node1, PhylogenyNode<T> node2) throws PhyloUtilsException;

	//PhylogenyNode<T> getRoot();

	void setSynonymService(TaxonomySynonymService taxonomySynonymService);


	RootedPhylogeny<T> getRandomSubtree(int numTaxa, Double mergeThreshold) throws NoSuchNodeException, TreeException;
	}
