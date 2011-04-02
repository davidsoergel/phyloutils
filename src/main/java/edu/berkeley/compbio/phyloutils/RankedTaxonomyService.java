package edu.berkeley.compbio.phyloutils;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface RankedTaxonomyService<T extends Serializable> extends TaxonomyService<T> //, TaxonomySynonymService
	{

	Set<T> getTaxIdsWithRank(String rank);
	}
