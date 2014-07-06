/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.trees.NoSuchNodeException;
import org.testng.annotations.Test;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class CiccarelliUtilsTest
	{


	private static final CiccarelliTaxonomyService ciccarelli = CiccarelliTaxonomyService.getInstance();

	@Test
	public void ciccarelliExactDistancesAreComputedCorrectly() throws PhyloUtilsException, NoSuchNodeException
		{
		double d = ciccarelli.exactDistanceBetween("Escherichia coli O6", "Escherichia coli K12");//(217992, 562);
		assert d == 0.00022;

		d = ciccarelli.exactDistanceBetween("Escherichia coli O6",
		                                    "Prochlorococcus marinus CCMP1378");//sp. MED4");//(217992, 59919);
		//logger.warn(d);
		assert MathUtils.equalWithinFPError(d, 1.47741);
		}

	@Test
	public void ciccarelliTreePrettyPrint()
		{
		System.out.println(ciccarelli.getTree().toString());
		}
	}
