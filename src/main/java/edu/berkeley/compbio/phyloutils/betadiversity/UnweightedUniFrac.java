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

import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.phyloutils.AbstractRootedPhylogeny;
import edu.berkeley.compbio.phyloutils.PhyloUtilsException;
import edu.berkeley.compbio.phyloutils.RootedPhylogeny;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class UnweightedUniFrac<T> implements DissimilarityMeasure<RootedPhylogeny<T>>
	{
	private static final Logger logger = Logger.getLogger(UnweightedUniFrac.class);

	/**
	 * {@inheritDoc}
	 */
	public double distanceFromTo(RootedPhylogeny<T> a, RootedPhylogeny<T> b)
		{
		try
			{
			//double branchLengthA = a.getTotalBranchLength();
			//double branchLengthB = b.getTotalBranchLength();

			RootedPhylogeny<T> theBasePhylogeny = a.getBasePhylogeny();
			if (theBasePhylogeny != b.getBasePhylogeny())
				{
				throw new PhyloUtilsException(
						"UniFrac can be computed only between trees extracted from the same underlying tree");
				}

			Set<T> unionLeafIDs = new HashSet<T>();
			unionLeafIDs.addAll(a.getLeafValues());
			unionLeafIDs.addAll(b.getLeafValues());

			RootedPhylogeny<T> unionTree = theBasePhylogeny.extractTreeWithLeafIDs(unionLeafIDs, false, true,
			                                                                       AbstractRootedPhylogeny.MutualExclusionResolutionMode.EXCEPTION);

			// careful: the "intersection" tree needs to contain branches terminating at internal nodes that are common between the two trees,
			// even if there are no leaves in common below that node.

			// i.e., this is completely wrong, since it only considers leaves in common between the two trees
			//RootedPhylogeny<T> intersectionTree = unionTree.extractTreeWithLeaves(a.getLeafValues(), true);
			//intersectionTree = intersectionTree.extractTreeWithLeaves(b.getLeafValues(), true);

			// we must do this starting from the union tree because there may be intermediate branch points that are collapsed in the individual trees
			RootedPhylogeny<T> intersectionTree =
					unionTree.extractIntersectionTree(a.getLeafValues(), b.getLeafValues(), null);

			double unionLength = unionTree.getTotalBranchLength();
			double intersectionLength = intersectionTree.getTotalBranchLength();

			return 1. - (intersectionLength / unionLength);
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

