package edu.berkeley.compbio.phyloutils;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonStringIdMapper<T>
	{
	T findTaxidByName(String name) throws PhyloUtilsException;
	}
