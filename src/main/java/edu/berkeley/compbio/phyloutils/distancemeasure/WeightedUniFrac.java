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

package edu.berkeley.compbio.phyloutils.distancemeasure;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import edu.berkeley.compbio.phyloutils.PhylogenyNode;
import edu.berkeley.compbio.phyloutils.RootedPhylogeny;

import java.util.HashSet;
import java.util.Set;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class WeightedUniFrac<T> implements DistanceMeasure<RootedPhylogeny<T>>
	{
	RootedPhylogeny<T> theBasePhylogeny;

	public double distanceFromTo(RootedPhylogeny<T> a, RootedPhylogeny<T> b)
		{
		Set<T> unionLeaves = new HashSet<T>();
		unionLeaves.addAll(a.getLeafValues());
		unionLeaves.addAll(b.getLeafValues());

		RootedPhylogeny<T> unionTree = theBasePhylogeny.extractTreeWithLeaves(unionLeaves);

		double result = 0;
		double totalLength = 0;
		for (PhylogenyNode<T> node : unionTree)
			{
			T id = node.getValue();
			PhylogenyNode<T> aNode = a.getNode(id);
			PhylogenyNode<T> bNode = b.getNode(id);
			double aWeight = aNode == null ? 0 : aNode.getWeight();
			double bWeight = aNode == null ? 0 : bNode.getWeight();
			result += node.getLength() * Math.abs(aWeight - bWeight);
			totalLength += node.getLength();
			}

		return result / totalLength;
		}
	}
