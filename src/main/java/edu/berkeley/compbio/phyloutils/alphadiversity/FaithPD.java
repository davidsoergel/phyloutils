/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils.alphadiversity;

import com.davidsoergel.stats.Statistic;
import com.davidsoergel.trees.RootedPhylogeny;

import java.io.Serializable;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class FaithPD<T extends Serializable> implements Statistic<RootedPhylogeny<T>>
	{
	/**
	 * {@inheritDoc}
	 */
	public double measure(RootedPhylogeny<T> p)
		{
		return p.getTotalBranchLength();
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
