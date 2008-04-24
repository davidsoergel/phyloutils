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

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* $Id$ */

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
	 *
	 * @param files
	 * @return
	 */
	@Transactional
	//(propagation = Propagation.MANDATORY)
	public Map<Integer, Set<Integer>> merge(Collection<Integer> leafIds, double ciccarelliMergeThreshold)
		{
		Map<Integer, Set<Integer>> theTaxonsetsByTaxid = new HashMap<Integer, Set<Integer>>();
		try
			{
			// first merge all those taxa that are at the same leaf in the known distance tree
			for (Integer id : leafIds)
				{
				int knownId = NcbiCiccarelliHybridService.nearestKnownAncestor(id);
				Set<Integer> currentTaxonset = theTaxonsetsByTaxid.get(knownId);
				if (currentTaxonset == null)
					{
					theTaxonsetsByTaxid.put(id, new HashSet<Integer>());
					}

				currentTaxonset.add(id);
				}

			if (ciccarelliMergeThreshold == 0)
				{
				logger.info("No merging, using " + leafIds.size() + " taxa.");
				return theTaxonsetsByTaxid;
				}

			// now iterate oven the tree, merging subtrees that meet the criterion

			Map<Integer, Set<Integer>> theMergedTaxa = new HashMap<Integer, Set<Integer>>();

			RootedPhylogeny<Integer> theTree =
					NcbiCiccarelliHybridService.extractTreeWithLeaves(theTaxonsetsByTaxid.keySet());

			PhylogenyIterator<Integer> it = theTree.phylogenyIterator();

			while (it.hasNext())
				{
				PhylogenyNode<Integer> node = it.next();

				// the iterator is depth-first by default

				if (node.getLargestLengthSpan() < ciccarelliMergeThreshold)
					{
					Set<Integer> mergeTaxa = new HashSet<Integer>();
					for (PhylogenyNode<Integer> descendant : node)
						{
						// we'll include intermediate nodes even if they aren't p'rt of the query (i.e., not leaves)
						mergeTaxa.add(descendant.getValue());
						}

					// we also need to advance the main iterator, so once we've merged this node,
					// we'll move on to the next sibling (or uncle, etc.)
					it.skipAllDescendants(node);

					theMergedTaxa.add(mergeTaxa);
					}
				}

			logger.info("Merged " + leafIds.size() + " taxa into " + theMergedTaxa.size() + " groups.");
			return theMergedTaxa;
			}
		catch (IOException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new MsensrRuntimeException(e);
			}
		catch (PhyloUtilsException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new MsensrRuntimeException(e);
			}
		}
	}
