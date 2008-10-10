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
 * Groups closely related leaves on a tree into larger taxa.  Useful in cases where the available tree has finer
 * phylogenetic resolution than is applicable to the question at hand.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class TaxonMerger
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(TaxonMerger.class);


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
				theTaxonsetsByTaxid.put(knownId, currentTaxonset);
				}

			currentTaxonset.add(id);
			}

		if (ciccarelliMergeThreshold == 0)
			{
			//logger.info("No merging, using " + leafIds.size() + " taxa.");
			return theTaxonsetsByTaxid;
			}

		// now iterate over the tree, merging subtrees that meet the criterion

		Map<T, Set<T>> theMergedTaxa = new HashMap<T, Set<T>>();

		RootedPhylogeny<T> theTree = basePhylogeny.extractTreeWithLeafIDs(theTaxonsetsByTaxid.keySet());

		assert theTaxonsetsByTaxid.keySet().containsAll(theTree.getLeafValues());
		assert theTree.getNodeValues().containsAll(theTaxonsetsByTaxid.keySet());

		DepthFirstTreeIterator<T, PhylogenyNode<T>> it = theTree.depthFirstIterator();

		// for sanity checking only
		Set<T> allMergedTaxa = new HashSet<T>();

		while (it.hasNext())
			{
			PhylogenyNode<T> node = it.next();

			// the iterator is depth-first by default

			double span = node.getLargestLengthSpan();
			if (span < ciccarelliMergeThreshold)
				{
				Set<T> mergeTaxa = new HashSet<T>();
				for (PhylogenyNode<T> descendant : node)
					{
					// we'll include intermediate nodes even if they aren't part of the query (i.e., not leaves)
					T id = descendant.getValue();
					mergeTaxa.add(id);

					Set<T> subIds = theTaxonsetsByTaxid.remove(id);
					if (subIds != null)
						{
						mergeTaxa.addAll(subIds);
						}
					}

				// we also need to advance the main iterator, so once we've merged this node,
				// we'll move on to the next sibling (or uncle, etc.)
				it.skipAllDescendants(node);

				theMergedTaxa.put(node.getValue(), mergeTaxa);
				allMergedTaxa.addAll(mergeTaxa);
				}
			else
				{
				T id = node.getValue();
				Set<T> subIds = theTaxonsetsByTaxid.remove(id);
				if (subIds != null)
					{
					// this happens when some of the input leaves connect up to the known-distance tree at a high node.
					// In this case the node has a known span greater than the threshold, but the leaves that hooked up
					// to it cannot be more finely assorted into known-distance nodes.
					// Thus it is not possible to group them at the appropriate level: if we merge them all together, the
					// resulting clade will be too big; if we don't, neighboring strains may be considered separately.
					// So for now we just drop them entirely.

					logger.warn("Dropping " + subIds.size() + " taxa at node " + id + " with span " + span + " > "
							+ ciccarelliMergeThreshold + " (i.e., our base tree is not detailed enough)");
					}
				}
			}

		assert theTaxonsetsByTaxid.isEmpty();
		//assert allMergedTaxa.containsAll(leafIds);

		logger.info("Merged " + leafIds.size() + " taxa into " + theMergedTaxa.size() + " groups.");
		return theMergedTaxa;
		}
	}
