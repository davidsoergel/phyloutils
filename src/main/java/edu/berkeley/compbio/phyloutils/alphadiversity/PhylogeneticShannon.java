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

package edu.berkeley.compbio.phyloutils.alphadiversity;

import com.davidsoergel.dsutils.MathUtils;
import edu.berkeley.compbio.ml.Statistic;
import edu.berkeley.compbio.phyloutils.PhylogenyNode;
import edu.berkeley.compbio.phyloutils.RootedPhylogeny;



/**
 * @Author David Soergel
 * @Version 1.0
 */
public class PhylogeneticShannon<T> implements Statistic<RootedPhylogeny<T>>
	{
	public double measure(RootedPhylogeny<T> tree)
		{
		return informationBelow(tree);
		}

	// -------------------------- OTHER METHODS --------------------------

	private double informationBelow(PhylogenyNode<T> node)
		{
		double entropy = 0;
		double nodeWeight = node.getWeight();

		for (PhylogenyNode<T> child : node.getChildren())
			{
			double p = child.getWeight() / nodeWeight;

			// weight the contribution of each node to the entropy by the branch length leading to it
			entropy -= child.getLength() * p * MathUtils.approximateLog(p);
			}

		entropy /= MathUtils.LOGTWO;//logTwo;// Math.log is base e

		double information = 2 - entropy;

		// information at each node below this one is weighted by the probability of
		// getting there in the first place

		for (PhylogenyNode<T> child : node.getChildren())
			{
			information += child.getWeight() * informationBelow(child);
			}

		return information;
		}

	public String toString()
		{
		String shortname = getClass().getName();
		shortname = shortname.substring(shortname.lastIndexOf(".") + 1);
		return shortname;
		}
	}