package edu.berkeley.compbio.phyloutils;

/**
 * Provides a simplified interface for taxonomy sources (e.g., NCBI, or a Newick file) that doesn't provide all the
 * services of a full RootedTree.  In particular none of the API involves PhylogenyNodes; everything is done by ID (of
 * the generic type)
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonomyService<T> extends TaxonMergingPhylogeny<T>
	{
	T findTaxidByName(String name) throws PhyloUtilsException;

	boolean isDescendant(T ancestor, T descendant) throws PhyloUtilsException;

	//boolean isDescendant(PhylogenyNode<T> ancestor, PhylogenyNode<T> descendant) throws PhyloUtilsException;

	void saveState();

	Double minDistanceBetween(T name1, T name2) throws PhyloUtilsException;

	//Double minDistanceBetween(PhylogenyNode<T> node1, PhylogenyNode<T> node2) throws PhyloUtilsException;

	//PhylogenyNode<T> getRoot();
	}
