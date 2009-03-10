package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.NoSuchNodeException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonStringIdMapper<T>
	{
	T findTaxidByName(String name) throws NoSuchNodeException; //, PhyloUtilsException;
	}
