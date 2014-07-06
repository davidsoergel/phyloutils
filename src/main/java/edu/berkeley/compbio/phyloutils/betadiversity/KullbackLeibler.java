/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils.betadiversity;

import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.trees.AbstractRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
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

public class KullbackLeibler<T extends Serializable> implements DissimilarityMeasure<RootedPhylogeny<T>>
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
						"K-L can be computed only between trees extracted from the same underlying tree");
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

			return klDivergence(unionTree, a, b);
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

	protected double klDivergence(RootedPhylogeny<T> unionTree, RootedPhylogeny<T> a, RootedPhylogeny<T> b)
			throws NoSuchNodeException, PhyloUtilsException
		{
		double divergence = 0;

		for (PhylogenyNode<T> node : unionTree.getLeaves())
			{
			T id = node.getPayload();
			PhylogenyNode<T> aNode = a.getChildWithPayload(id);
			PhylogenyNode<T> bNode = b.getChildWithPayload(id);
			double p = aNode == null ? 0 : aNode.getWeight();
			double q = bNode == null ? 0 : bNode.getWeight();

			// the provided weights are absolute, not conditional, and that's just what we want

			divergence += p * MathUtils.approximateLog2(p / q);
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
