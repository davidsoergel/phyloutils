/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.DepthFirstTreeIterator;
import com.davidsoergel.dsutils.tree.TreeException;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @Author David Soergel
 * @Version 1.0
 */
public class TaxonMerger
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(TaxonMerger.class);

	/**
	 * Separates a set of taxon ids into the smallest possible number of disjoint sets such that the maximum pairwise
	 * phylogenetic distance within each set is less than a given threshold.
	 */
	//@Transactional
	//(propagation = Propagation.MANDATORY)
	public static <T> Map<T, Set<T>> merge(Collection<T> leafIds, TaxonMergingPhylogeny<T> basePhylogeny,
	                                       double ciccarelliMergeThreshold) throws TreeException, PhyloUtilsException
		{
		Map<T, Set<T>> theTaxonsetsByTaxid = new HashMap<T, Set<T>>();

		// first merge all those taxa that are at the same leaf in the known distance tree
		for (T id : leafIds)
			{
			T knownId = basePhylogeny.nearestAncestorWithBranchLength(id);
			Set<T> currentTaxonset = theTaxonsetsByTaxid.get(knownId);
			if (currentTaxonset == null)
				{
				currentTaxonset = new HashSet<T>();
				theTaxonsetsByTaxid.put(id, currentTaxonset);
				}

			currentTaxonset.add(id);
			}

		if (ciccarelliMergeThreshold == 0)
			{
			//logger.info("No merging, using " + leafIds.size() + " taxa.");
			return theTaxonsetsByTaxid;
			}

		// now iterate oven the tree, merging subtrees that meet the criterion

		Map<T, Set<T>> theMergedTaxa = new HashMap<T, Set<T>>();

		RootedPhylogeny<T> theTree = basePhylogeny.extractTreeWithLeafIDs(theTaxonsetsByTaxid.keySet());

		DepthFirstTreeIterator<T, LengthWeightHierarchyNode<T>> it = theTree.depthFirstIterator();

		while (it.hasNext())
			{
			LengthWeightHierarchyNode<T> node = it.next();

			// the iterator is depth-first by default

			if (node.getLargestLengthSpan() < ciccarelliMergeThreshold)
				{
				Set<T> mergeTaxa = new HashSet<T>();
				for (LengthWeightHierarchyNode<T> descendant : node)
					{
					// we'll include intermediate nodes even if they aren't p'rt of the query (i.e., not leaves)
					mergeTaxa.add(descendant.getValue());
					}

				// we also need to advance the main iterator, so once we've merged this node,
				// we'll move on to the next sibling (or uncle, etc.)
				it.skipAllDescendants(node);

				theMergedTaxa.put(node.getValue(), mergeTaxa);
				}
			}

		logger.info("Merged " + leafIds.size() + " taxa into " + theMergedTaxa.size() + " groups.");
		return theMergedTaxa;
		}
	}
