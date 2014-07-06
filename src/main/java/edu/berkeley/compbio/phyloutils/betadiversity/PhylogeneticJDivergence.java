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

//@PropertyConsumer
public class PhylogeneticJDivergence<T extends Serializable> implements DissimilarityMeasure<RootedPhylogeny<T>>
	{
	private static final Logger logger = Logger.getLogger(WeightedUniFrac.class);

	//@Property(defaultvalue = "edu.berkeley.compbio.phyloutils.betadiversity.PhylogeneticKullbackLeibler")
	public PhylogeneticKullbackLeibler<T> kl = new PhylogeneticKullbackLeibler<T>();

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

			return 0.5 * (kl.klDivergenceBelow(unionTree, aTreeSmoothed, bTreeSmoothed) + kl
					.klDivergenceBelow(unionTree, bTreeSmoothed, aTreeSmoothed));
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
