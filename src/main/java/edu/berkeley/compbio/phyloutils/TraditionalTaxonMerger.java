/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class TraditionalTaxonMerger
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(TraditionalTaxonMerger.class);

	public static <T extends Serializable> Map<T, Set<T>> merge(Collection<T> leafIds, TaxonomyService<T> basePhylogeny,
	                                                            String levelName)
		{
		throw new NotImplementedException();
		/*
		Map<T, Set<T>> theTaxonsetsByTaxid = new HashMap<T, Set<T>>();

		for (T id : leafIds)
			{
			T requestedLevelId = basePhylogeny.nearestAncestorAtRank(levelName);
			//T knownId = basePhylogeny.nearestAncestorWithBranchLength(id);
			Set<T> currentTaxonset = theTaxonsetsByTaxid.get(requestedLevelId);
			if (currentTaxonset == null)
				{
				currentTaxonset = new HashSet<T>();
				theTaxonsetsByTaxid.put(requestedLevelId, currentTaxonset);
				}

			currentTaxonset.add(id);
			}

		return theTaxonsetsByTaxid;
*/
		}
	}
