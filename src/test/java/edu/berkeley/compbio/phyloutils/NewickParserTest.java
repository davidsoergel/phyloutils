/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.RootedPhylogeny;
import com.davidsoergel.trees.StringNodeNamer;
import com.davidsoergel.trees.TreeException;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class NewickParserTest
	{
	private static final Logger logger = Logger.getLogger(NewickParserTest.class);
	// -------------------------- OTHER METHODS --------------------------

	@Test
	public void newickParserReadsAllNodes() throws TreeException, IOException
		{
		//	logger.warn("starting newickParserReadsAllNodes");
		URL url = ClassLoader.getSystemResource("goodNewickTree.nh");
		// We're in a separate classloader under Maven2 / surefire
		//	logger.warn("Classloader: " + getClass().getClassLoader().toString());
		//	logger.warn("file: " + getClass().getClassLoader().getResource("goodNewickTree.sh"));
		//	URL url = getClass().getResource("goodNewickTree.sh");
		//	logger.warn("Got file: " + url);
		RootedPhylogeny p =
				new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_", false, false));
		//	logger.warn("Parsed tree with " + p.getNodes().size() + " nodes.");
		assert p.getUniqueIdToNodeMap().size() == 14;
		//	logger.warn("all done");
		}

	@Test
	public void newickParserSkipsComments() throws TreeException, IOException
		{
		//	logger.warn("starting newickParserReadsAllNodes");
		URL url = ClassLoader.getSystemResource("goodNewickTreeWithComments.nh");
		// We're in a separate classloader under Maven2 / surefire
		//	logger.warn("Classloader: " + getClass().getClassLoader().toString());
		//	logger.warn("file: " + getClass().getClassLoader().getResource("goodNewickTree.sh"));
		//	URL url = getClass().getResource("goodNewickTree.sh");
		//	logger.warn("Got file: " + url);
		RootedPhylogeny p =
				new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_", false, false));
		//	logger.warn("Parsed tree with " + p.getNodes().size() + " nodes.");
		assert p.getUniqueIdToNodeMap().size() == 14;
		//	logger.warn("all done");
		}

	@Test(expectedExceptions = {PhyloUtilsException.class})
	public void newickParserThrowsExceptionOnPrematureTermination() throws TreeException, IOException
		{
		URL url = ClassLoader.getSystemResource("badNewickTree1.nh");
		RootedPhylogeny p =
				new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_", false, false));
		}

	@Test
	public void phylogenyDistancesAreCorrect() throws TreeException, IOException, NoSuchNodeException
		{
		URL url = ClassLoader.getSystemResource("goodNewickTree.nh");
		RootedPhylogeny p =
				new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_", false, false));

		assert p.distanceBetween("raccoon", "bear") == 26;
		assert p.distanceBetween("raccoon", "raccoon") == 0;
		double d = p.distanceBetween("raccoon", "dog");
		assert MathUtils.equalWithinFPError(d, 45.50713);
		d = p.distanceBetween("raccoon", "seal");
		assert MathUtils.equalWithinFPError(d, 43.49541);
		}
	}
