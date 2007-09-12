/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class NewickParserTest
	{

	@Test
	public void newickParserReadsAllNodes() throws PhyloUtilsException, FileNotFoundException
		{
		RootedPhylogeny p = NewickParser.read(new FileInputStream("src/test/data/goodNewickTree.nh"));
		assert p.getNodes().size() == 14;
		}


	@Test
	public void phylogenyDistancesAreCorrect() throws PhyloUtilsException, FileNotFoundException
		{
		RootedPhylogeny p = NewickParser.read(new FileInputStream("src/test/data/goodNewickTree.nh"));

		assert p.distanceBetween("raccoon", "bear") == 26;
		assert p.distanceBetween("raccoon", "raccoon") == 0;
		double d = p.distanceBetween("raccoon", "dog");
		assert MathUtils.equalWithinFPError(d, 45.50713);
		d = p.distanceBetween("raccoon", "seal");
		assert MathUtils.equalWithinFPError(d, 43.49541);

		}


	@Test(expectedExceptions = {PhyloUtilsException.class})
	public void newickParserThrowsExceptionOnPrematureTermination() throws PhyloUtilsException, FileNotFoundException
		{
		RootedPhylogeny p = NewickParser.read(new FileInputStream("src/test/data/badNewickTree1.nh"));
		}
	}
