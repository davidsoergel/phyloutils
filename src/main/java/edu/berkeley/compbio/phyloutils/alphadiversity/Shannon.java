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
import org.apache.log4j.Logger;

import java.io.Serializable;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class Shannon<T extends Serializable> implements Statistic<RootedPhylogeny<T>>
	{
	private static final Logger logger = Logger.getLogger(Shannon.class);

	/**
	 * {@inheritDoc}
	 */
	public double measure(RootedPhylogeny<T> tree)
		{

		double entropy = 0;
		for (PhylogenyNode<T> node : tree.getLeaves())
			{
			double p = node.getWeight();
			entropy -= p * MathUtils.approximateLog(p);
			}

		entropy /= MathUtils.LOGTWO;//logTwo;// Math.log is base e

		//double information = 2 - entropy;

		return entropy;
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
