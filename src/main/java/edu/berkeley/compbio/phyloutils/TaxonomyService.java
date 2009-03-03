package edu.berkeley.compbio.phyloutils;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonomyService<T> extends TaxonMergingPhylogeny<T>
	{
	T findTaxidByName(String name) throws PhyloUtilsException;

	boolean isDescendant(T label, T synonym) throws PhyloUtilsException;

	void saveState();

	Double minDistanceBetween(T name1, T name2) throws PhyloUtilsException;

	//RootedPhylogeny<T> getTree();
	}
