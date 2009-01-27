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

package edu.berkeley.compbio.phyloutils.old;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @version 1.0
 */
public class BinaryTreeTest
	{
	private static Logger logger = Logger.getLogger(BinaryTreeTest.class);


	int[] treeScrambled = {
			-1,
			12,
			17,
			7,
			5,
			17,
			15,
			0,
			12,
			18,
			8,
			1,
			7,
			8,
			5,
			0,
			18,
			15,
			1
	};
	int[] treeOK = {
			0,
			7,
			15,
			3,
			12,
			6,
			17,
			1,
			8,
			2,
			5,
			11,
			18,
			10,
			13,
			4,
			14,
			9,
			16
	};
	BinaryTree bt;

	@BeforeSuite
	protected void setUp()
		{
		bt = new BinaryTree(19);
		try
			{
			for (int i = 0; i < treeScrambled.length; i++)
				{
				bt.setParent(i, treeScrambled[i]);
				}
			}
		catch (TreeException e)
			{
			logger.error(e);
			}
		}

	@Test
	public void breadthFirstTraversalWorks() throws TreeException
		{
		int[] result = bt.breadthFirst();
		assert Arrays.equals(result, treeOK);
		}
	}
