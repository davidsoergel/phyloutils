/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils.alphadiversity;

import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.stats.Statistic;
import com.davidsoergel.trees.PhylogenyNode;
import com.davidsoergel.trees.RootedPhylogeny;
import edu.berkeley.compbio.phyloutils.PhyloUtilsException;
import edu.berkeley.compbio.phyloutils.PhyloUtilsRuntimeException;
import org.apache.log4j.Logger;

import java.io.Serializable;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class PhylogeneticShannon<T extends Serializable> implements Statistic<RootedPhylogeny<T>>
	{
	private static final Logger logger = Logger.getLogger(PhylogeneticShannon.class);

	/**
	 * {@inheritDoc}
	 */
	public double measure(RootedPhylogeny<T> tree)
		{
		try
			{
			return informationBelow(tree);
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		}

	// -------------------------- OTHER METHODS --------------------------

	private double informationBelow(PhylogenyNode<T> node) throws PhyloUtilsException
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
