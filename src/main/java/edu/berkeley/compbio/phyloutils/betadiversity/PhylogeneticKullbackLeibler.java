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

package edu.berkeley.compbio.phyloutils.betadiversity;

import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.phyloutils.AbstractRootedPhylogeny;
import edu.berkeley.compbio.phyloutils.PhyloUtilsException;
import edu.berkeley.compbio.phyloutils.PhylogenyNode;
import edu.berkeley.compbio.phyloutils.RootedPhylogeny;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class PhylogeneticKullbackLeibler<T> implements DissimilarityMeasure<RootedPhylogeny<T>>
	{
	private static final Logger logger = Logger.getLogger(WeightedUniFrac.class);

	/**
	 * {@inheritDoc}
	 */
	public double distanceFromTo(RootedPhylogeny<T> a, RootedPhylogeny<T> b)
		{
		try
			{
			RootedPhylogeny<T> theBasePhylogeny = a.getBasePhylogeny();
			if (theBasePhylogeny != b.getBasePhylogeny())
				{
				throw new PhyloUtilsException(
						"Phylogenetic K-L can be computed only between trees extracted from the same underlying tree");
				}

			Set<T> unionLeaves = new HashSet<T>();
			unionLeaves.addAll(a.getLeafValues());
			unionLeaves.addAll(b.getLeafValues());

			RootedPhylogeny<T> unionTree = theBasePhylogeny.extractTreeWithLeafIDs(unionLeaves, false, false,
			                                                                       AbstractRootedPhylogeny.MutualExclusionResolutionMode.EXCEPTION);

			RootedPhylogeny<T> aTreeSmoothed = unionTree.clone();
			aTreeSmoothed.smoothWeightsFrom(a, .000001);

			RootedPhylogeny<T> bTreeSmoothed = unionTree.clone();
			bTreeSmoothed.smoothWeightsFrom(b, .000001);

			return klDivergenceBelow(unionTree, aTreeSmoothed, bTreeSmoothed);
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		}

	protected double klDivergenceBelow(PhylogenyNode<T> u, PhylogenyNode<T> a, PhylogenyNode<T> b)
			throws NoSuchNodeException, PhyloUtilsException
		{
		double divergence = 0;
		for (PhylogenyNode<T> node : u.getChildren())
			{
			T id = node.getValue();
			PhylogenyNode<T> aNode = a.getChild(id);
			PhylogenyNode<T> bNode = b.getChild(id);
			double aWeight = aNode == null ? 0 : aNode.getWeight();
			double bWeight = bNode == null ? 0 : bNode.getWeight();

			// the provided weights are absolute, not conditional

			double p = aWeight == 0 ? 0 : aWeight / aNode.getParent().getWeight();
			double q = bWeight == 0 ? 0 : bWeight / bNode.getParent().getWeight();

			// weight the contribution of each node to the divergence by the branch length leading to it
			divergence += node.getLength() * p * MathUtils.approximateLog2(p / q);

			// information at each node below this one is weighted by the probability of
			// getting there in the first place, according to realTree
			divergence += p * klDivergenceBelow(node, aNode, bNode);
			}

		return divergence;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
		{
		String shortname = getClass().getName();
		shortname = shortname.substring(shortname.lastIndexOf(".") + 1);
		return shortname;
		}
	}
