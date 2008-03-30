/*
 * Copyright (c) 2008 Regents of the University of California
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

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class PhylogenyNodeTest
	{
	private static final Logger logger = Logger.getLogger(PhylogenyNodeTest.class);

	PhylogenyNode<String> root = new PhylogenyNode<String>("root", null, 0);

	PhylogenyNode<String> a = new PhylogenyNode<String>("a", root, 10);
	PhylogenyNode<String> b = new PhylogenyNode<String>("b", root, 4);
	PhylogenyNode<String> c = new PhylogenyNode<String>("c", root, 1);


	PhylogenyNode<String> aa = new PhylogenyNode<String>("aa", a, 20);
	PhylogenyNode<String> ab = new PhylogenyNode<String>("ab", a, 30);


	//	PhylogenyNode<String> aaa = new PhylogenyNode<String>("aaa", aa, 1);
	//	PhylogenyNode<String> aab = new PhylogenyNode<String>("aab", aa, 1);


	PhylogenyNode<String> ba = new PhylogenyNode<String>("ba", b, 1.1);
	PhylogenyNode<String> bb = new PhylogenyNode<String>("bb", b, 1.2);


	PhylogenyNode<String> baa = new PhylogenyNode<String>("baa", ba, 2.1);
	PhylogenyNode<String> bab = new PhylogenyNode<String>("bab", ba, 2.2);


	PhylogenyNode<String> bba = new PhylogenyNode<String>("bba", bb, 3.1);
	PhylogenyNode<String> bbb = new PhylogenyNode<String>("bbb", bb, 3.2);


	PhylogenyNode<String> bbbb = new PhylogenyNode<String>("bbbb", bbb, 4.1);


	PhylogenyNode<String> ca = new PhylogenyNode<String>("ca", c, 2);
	PhylogenyNode<String> cb = new PhylogenyNode<String>("cb", c, 3);

	@Test
	public void leafSpanEqualsZero()
		{
		assert cb.getLargestLengthSpan() == 0;
		}

	@Test
	public void atomicSpanEqualsLengthSum()
		{
		assert c.getLargestLengthSpan() == 5;
		}

	@Test
	public void singleChildSpanEqualsLength()
		{
		assert bbb.getLargestLengthSpan() == 4.1;
		}

	@Test
	public void singleChildTakenIntoAccountCorrectlyForHigherLevelSpan()
		{
		assert bb.getLargestLengthSpan() == 10.4;
		}


	@Test
	public void multiLevelSpanEqualsLengthSum()
		{
		assert b.getLargestLengthSpan() == 11.8;
		//the cross-branch span
		// the single-branch span via bb would have been 11.6
		}

	@Test
	public void singleBranchSpanCanTrumpMultiBranchSpan()
		{
		// the span at a plus 10 for a itself
		assert root.getLargestLengthSpan() == 60;

		// not the depth via a (40) + depth via b (13.5) = 53.5
		}

	@Test
	public void spansAreRecalculatedAfterTreeModification()
		{
		assert b.getLargestLengthSpan() == 11.8;

		PhylogenyNode<String> extraNode = new PhylogenyNode<String>("bac", ba, 10);

		assert b.getLargestLengthSpan() == 19.6;

		ba.removeChild(extraNode);

		assert b.getLargestLengthSpan() == 11.8;
		}


	@Test
	public void treeIteratorProvidesAllNodes()
		{
		int i = 0;
		for (PhylogenyNode<String> node : root)
			{
			logger.info("Tree iterator provided node: " + node);
			i++;
			}
		assert i == 15;
		}

	@Test
	public void skipAllDescendantsWorksProperly() throws PhyloUtilsException
		{
		PhylogenyIterator<String> it = root.iterator();
		assert it.next() == root;

		int i = 0;
		while (it.hasNext())
			{
			PhylogenyNode<String> topLevelNode = it.next();

			if (topLevelNode == b)
				{
				// OK, continue to the children
				}
			else if (topLevelNode == a || topLevelNode == ba || topLevelNode == bb || topLevelNode == c)
				{
				i++;
				it.skipAllDescendants(topLevelNode);
				}
			else
				{
				assert false;
				}
			}
		assert i == 4;
		}

	@Test
	public void individualNodeIteratorsWorkProperly()
		{
		PhylogenyIterator<String> it = root.iterator();
		assert it.next() == root;

		while (it.hasNext())
			{
			PhylogenyNode<String> topLevelNode = it.next();

			int i = 0;
			PhylogenyIterator<String> sub = topLevelNode.iterator();
			while (sub.hasNext())
				{
				sub.next();
				i++;
				}

			if (topLevelNode == a)
				{
				assert i == 3;
				}
			else if (topLevelNode == b)
				{
				assert i == 8;
				}
			else if (topLevelNode == c)
				{
				assert i == 3;
				}
			}
		}

	@Test
	public void extractTreeWithPathsWorks() throws PhyloUtilsException
		{
		Set<List<PhylogenyNode<String>>> theAncestorLists = new HashSet<List<PhylogenyNode<String>>>();

		theAncestorLists.add(baa.getAncestorPath());
		theAncestorLists.add(bbbb.getAncestorPath());
		theAncestorLists.add(ca.getAncestorPath());

		PhylogenyNode<String> tree = root.extractTreeWithPaths(theAncestorLists);

		for (PhylogenyNode<String> node : tree)
			{
			if (node.getName().equals("root"))
				{
				assert node.getChildren().size() == 2;
				}
			else if (node.getName().equals("baa"))
				{
				assert node.getLength() == 3.2;
				}
			else if (node.getName().equals("b"))
				{
				assert node.getLength() == 4;
				}
			else if (node.getName().equals("bbbb"))
				{
				assert node.getLength() == 8.5;
				}
			else if (node.getName().equals("ca"))
				{
				assert node.getLength() == 3;
				}
			else
				{
				assert false;
				}
			}
		}
	}

