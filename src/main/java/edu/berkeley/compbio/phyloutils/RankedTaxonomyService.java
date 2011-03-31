package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface RankedTaxonomyService<T extends Serializable> extends TaxonomyService<T> //, TaxonomySynonymService
	{
	String getScientificName(T from) throws NoSuchNodeException;

	Set<T> getTaxIdsWithRank(String rank);
	}
