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

import com.davidsoergel.stats.ContinuousDistribution1D;
import com.davidsoergel.stats.DistributionException;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.Set;


public interface RootedPhylogeny<T>
		extends PhylogenyNode<T>, TaxonMergingPhylogeny<T>//, Clusterable<RootedPhylogeny<T>>
	{
	T commonAncestor(Set<T> knownMergeIds);

	T commonAncestor(T nameA, T nameB);

	double distanceBetween(T nameA, T nameB);

	PhylogenyNode<T> getNode(T name);

	Collection<PhylogenyNode<T>> getNodes();

	Collection<PhylogenyNode<T>> getLeaves();

	//RootedPhylogeny<T> extractTreeWithLeaves(Collection<T> ids);

	T nearestKnownAncestor(RootedPhylogeny<T> rootPhylogeny, T leafId) throws PhyloUtilsException;

	//T nearestAncestorWithBranchLength(T leafId) throws PhyloUtilsException;

	Collection<T> getLeafValues();

	Collection<T> getNodeValues();


	double getTotalBranchLength();

	void randomizeLeafWeights(ContinuousDistribution1D speciesAbundanceDistribution) throws DistributionException;

	void normalizeWeights();

	RootedPhylogeny<T> getBasePhylogeny();

	RootedPhylogeny<T> getBasePhylogenyRecursive();

	RootedPhylogeny<T> extractIntersectionTree(Collection<T> leafValues, Collection<T> leafValues1)
			throws PhyloUtilsException;

	RootedPhylogeny<T> mixWith(RootedPhylogeny<T> phylogeny, double mixingProportion) throws PhyloUtilsException;

	void smoothWeightsFrom(RootedPhylogeny<T> otherTree, double smoothingFactor) throws PhyloUtilsException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	RootedPhylogeny<T> clone();

	void setLeafWeights(Multiset<T> ids);
	}
