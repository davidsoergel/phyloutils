/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

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
