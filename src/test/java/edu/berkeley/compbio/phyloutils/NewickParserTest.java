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

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.MathUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class NewickParserTest
	{
	private static final Logger logger = Logger.getLogger(NewickParserTest.class);
	// -------------------------- OTHER METHODS --------------------------

	@Test
	public void newickParserReadsAllNodes() throws PhyloUtilsException, IOException
		{
		//	logger.warn("starting newickParserReadsAllNodes");
		URL url = ClassLoader.getSystemResource("goodNewickTree.nh");
		// We're in a separate classloader under Maven2 / surefire
		//	logger.warn("Classloader: " + getClass().getClassLoader().toString());
		//	logger.warn("file: " + getClass().getClassLoader().getResource("goodNewickTree.sh"));
		//	URL url = getClass().getResource("goodNewickTree.sh");
		//	logger.warn("Got file: " + url);
		RootedPhylogeny p = new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_"));
		//	logger.warn("Parsed tree with " + p.getNodes().size() + " nodes.");
		assert p.getNodes().size() == 14;
		//	logger.warn("all done");
		}

	@Test(expectedExceptions = {PhyloUtilsException.class})
	public void newickParserThrowsExceptionOnPrematureTermination() throws PhyloUtilsException, IOException
		{
		URL url = ClassLoader.getSystemResource("badNewickTree1.nh");
		RootedPhylogeny p = new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_"));
		}

	@Test
	public void phylogenyDistancesAreCorrect() throws PhyloUtilsException, IOException
		{
		URL url = ClassLoader.getSystemResource("goodNewickTree.nh");
		RootedPhylogeny p = new NewickParser<String>().read(url.openStream(), new StringNodeNamer("NONAME_"));

		assert p.distanceBetween("raccoon", "bear") == 26;
		assert p.distanceBetween("raccoon", "raccoon") == 0;
		double d = p.distanceBetween("raccoon", "dog");
		assert MathUtils.equalWithinFPError(d, 45.50713);
		d = p.distanceBetween("raccoon", "seal");
		assert MathUtils.equalWithinFPError(d, 43.49541);
		}
	}
