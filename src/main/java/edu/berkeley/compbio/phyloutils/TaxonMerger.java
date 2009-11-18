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

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.trees.AbstractRootedPhylogeny;
import com.davidsoergel.trees.BasicRootedPhylogeny;
import com.davidsoergel.trees.DepthFirstTreeIterator;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
import com.davidsoergel.trees.TreeException;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	 * <p/>
	 * If one of the requested ids is an ancestor of another, then the whole subtree is merged at the ancestor level,
	 * regardless of its span.
	 */
	//@Transactional
	//(propagation = Propagation.MANDATORY)
	public static <T extends Serializable> Map<T, Set<T>> merge(final Set<T> requestedLeafIds,
	                                                            final TaxonomyService<T> basePhylogeny,
	                                                            // TaxonMergingPhylogeny
	                                                            final double branchSpanMergeThreshold)
			throws TreeException, NoSuchNodeException// , PhyloUtilsException
		{
		// ** hack to allow NcbiTaxonomyWithUnitBranchLengths
		// REVIEW we have to ignoreAbsentNodes in order to use ANCESTOR mode, because we may drop the leaves.  Is that OK??
		BasicRootedPhylogeny<T> theCompleteTree = basePhylogeny.extractTreeWithLeafIDs(requestedLeafIds, false, true,
		                                                                               AbstractRootedPhylogeny.MutualExclusionResolutionMode.BOTH);

		Map<T, Set<T>> theTaxonsetsByTaxid = new HashMap<T, Set<T>>();

		// first merge all those taxa that are at the same leaf in the known distance tree
		for (T id : requestedLeafIds)
			{
			T knownId = theCompleteTree.nearestAncestorWithBranchLength(id);
			Set<T> currentTaxonset = theTaxonsetsByTaxid.get(knownId);
			if (currentTaxonset == null)
				{
				currentTaxonset = new HashSet<T>();
				theTaxonsetsByTaxid.put(knownId, currentTaxonset);
				}

			currentTaxonset.add(id);
			}

		// can't do this, because we may still need to drop nodes below
		/*
		if (ciccarelliMergeThreshold == 0)
			{
			//logger.info("No merging, using " + leafIds.size() + " taxa.");
			return theTaxonsetsByTaxid;
			}
*/

		Map<T, Set<T>> theMergedTaxa = new HashMap<T, Set<T>>();

		// REVIEW we have to ignoreAbsentNodes in order to use ANCESTOR mode, because we may drop the leaves.  Is that OK??
		BasicRootedPhylogeny<T> thePrunedTree = theCompleteTree
				.extractTreeWithLeafIDs(theTaxonsetsByTaxid.keySet(), true, true,
				                        AbstractRootedPhylogeny.MutualExclusionResolutionMode.ANCESTOR);

		// if there were any ancestor-descendant issues, merge the subtrees up to the ancestor level

		for (T ancestorId : thePrunedTree.getLeafValues())
			{
			PhylogenyNode<T> ancestor = theCompleteTree.getNode(ancestorId);
			if (!ancestor.isLeaf())
				{
				Set<T> currentTaxonset = theTaxonsetsByTaxid.get(ancestorId);
				assert currentTaxonset != null;

				Iterator<PhylogenyNode<T>> iter = ancestor.iterator();
				assert iter.next() == ancestor;

				while (iter.hasNext())
					{
					PhylogenyNode<T> descendant = iter.next();
					assert descendant != ancestor;

					T descendantId = descendant.getPayload();
					Set<T> alreadyMergedAtDescendant = theTaxonsetsByTaxid.get(descendantId);
					if (alreadyMergedAtDescendant != null)
						{
						currentTaxonset.addAll(alreadyMergedAtDescendant);
						theTaxonsetsByTaxid.remove(descendantId);
						}
					}
				}
			}


		assert theTaxonsetsByTaxid.keySet().containsAll(thePrunedTree.getLeafValues());

		// this may not be true unless we use MutualExclusionResolutionMode.BOTH above
		// we do want to make sure all mergable taxa are included, whether or not they are leaves
		assert thePrunedTree.getNodeValues().containsAll(theTaxonsetsByTaxid.keySet());


		// now iterate over the tree, merging subtrees that meet the criterion


		DepthFirstTreeIterator<T, PhylogenyNode<T>> it = thePrunedTree.depthFirstIterator();

		// for sanity checking only
		List<T> allMergedTaxa = new ArrayList<T>();

		int dropped = 0;

		while (it.hasNext())
			{
			PhylogenyNode<T> node = it.next();

			double span = node.getLargestLengthSpan();
			if (span <= branchSpanMergeThreshold)
				{
				Set<T> mergeTaxa = new HashSet<T>();
				for (PhylogenyNode<T> descendant : node)
					{
					// we'll include intermediate nodes even if they aren't part of the query (i.e., not leaves)
					T id = descendant.getPayload();
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

				theMergedTaxa.put(node.getPayload(), mergeTaxa);
				allMergedTaxa.addAll(mergeTaxa);
				}
			else
				{
				T id = node.getPayload();
				Set<T> subIds = theTaxonsetsByTaxid.remove(id);
				if (subIds != null)
					{
					// this happens when some of the input leaves connect up to the known-distance tree at a high node.
					// In this case the node has a known span greater than the threshold, but the leaves that hooked up
					// to it cannot be more finely assorted into known-distance nodes.
					// Thus it is not possible to group them at the appropriate level: if we merge them all together, the
					// resulting clade will be too big; if we don't, neighboring strains may be considered separately.
					// So for now we just drop them entirely.

					dropped += subIds.size();

					logger.warn("Dropping " + subIds.size() + " taxa at node " + id + " with span " + span + " > "
					            + branchSpanMergeThreshold + " (i.e., our base tree is not detailed enough)");
					}
				}
			}

		assert theTaxonsetsByTaxid.isEmpty();
		//assert allMergedTaxa.containsAll(leafIds);

		//** be paranoid?
		//testSetsReallyDisjoint(theCompleteTree, theMergedTaxa, thePrunedTree);


		final int includedTaxa = requestedLeafIds.size() - dropped;

		// the merged taxa are disjoint and unique
		assert new HashSet<T>(allMergedTaxa).size() == allMergedTaxa.size();

		// this is not true because allMergedTaxa includes intermediate nodes
		//assert allMergedTaxa.size() == includedTaxa;

		logger.info("Merged " + includedTaxa + " taxa into " + theMergedTaxa.size() + " groups; dropped " + dropped);

		return theMergedTaxa;
		}

	private static <T extends Serializable> void testSetsReallyDisjoint(final BasicRootedPhylogeny<T> theCompleteTree,
	                                                                    final Map<T, Set<T>> theMergedTaxa,
	                                                                    final BasicRootedPhylogeny<T> thePrunedTree)
			throws NoSuchNodeException
		{
		// make sure the sets are really disjoint
		final Set<T> distinctTaxonHeads = theMergedTaxa.keySet();
		for (Map.Entry<T, Set<T>> entry : theMergedTaxa.entrySet())
			{
			// each set of merged taxa should contain exactly one entry of the head set, corresponding to the subtree root
			T headId = entry.getKey();
			final Set<T> taxonMembers = entry.getValue();
			Collection<T> intersection = DSCollectionUtils.intersection(distinctTaxonHeads, taxonMembers);
			assert intersection.size() == 1; // the key itself
			assert intersection.iterator().next().equals(headId);

			// this subtree should have no nodes in common with any other subtree
			for (Map.Entry<T, Set<T>> entry2 : theMergedTaxa.entrySet())
				{
				final T headId2 = entry2.getKey();

				if (headId2 != headId)
					{
					assert !theCompleteTree.isDescendant(headId, headId2);
					assert !theCompleteTree.isDescendant(headId2, headId);
					assert DSCollectionUtils.intersection(taxonMembers, entry2.getValue()).size() == 0;
					}
				}

			// more to the point, this subtree should not descend from the root of any other subtree

			PhylogenyNode<T> node = thePrunedTree.getNode(headId);
			for (PhylogenyNode<T> ancestor : node.getAncestorPath())
				{
				T ancestorId = ancestor.getPayload();
				assert ancestorId.equals(headId) || !distinctTaxonHeads.contains(ancestorId);
				}
			}
		}
	}
