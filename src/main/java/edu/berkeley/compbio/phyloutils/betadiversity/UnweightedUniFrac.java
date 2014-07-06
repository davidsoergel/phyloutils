/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils.betadiversity;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.trees.AbstractRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.RootedPhylogeny;
import com.davidsoergel.trees.TreeException;
import edu.berkeley.compbio.phyloutils.PhyloUtilsException;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class UnweightedUniFrac<T extends Serializable> implements DissimilarityMeasure<RootedPhylogeny<T>>
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
		catch (TreeException e)
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

