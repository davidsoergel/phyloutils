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

import com.davidsoergel.dsutils.tree.DepthFirstTreeIterator;
import com.davidsoergel.dsutils.tree.HierarchyNode;
import com.davidsoergel.dsutils.tree.TreeException;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @Author David Soergel
 * @Version 1.0
 */
public class BasicPhylogenyNodeTest
	{
	private static final Logger logger = Logger.getLogger(BasicPhylogenyNodeTest.class);

	BasicRootedPhylogeny<String> rootPhylogeny = new BasicRootedPhylogeny<String>("root");


	BasicPhylogenyNode<String> root = rootPhylogeny.getRoot();
	//new BasicPhylogenyNode<String>(null, "root", 0);

	BasicPhylogenyNode<String> a = new BasicPhylogenyNode<String>(root, "a", 10);
	BasicPhylogenyNode<String> b = new BasicPhylogenyNode<String>(root, "b", 4);
	BasicPhylogenyNode<String> c = new BasicPhylogenyNode<String>(root, "c", 1);


	BasicPhylogenyNode<String> aa = new BasicPhylogenyNode<String>(a, "aa", 20);
	BasicPhylogenyNode<String> ab = new BasicPhylogenyNode<String>(a, "ab", 30);


	//	PhylogenyNode<String> aaa = new PhylogenyNode<String>("aaa", aa, 1);
	//	PhylogenyNode<String> aab = new PhylogenyNode<String>("aab", aa, 1);


	BasicPhylogenyNode<String> ba = new BasicPhylogenyNode<String>(b, "ba", 1.1);
	BasicPhylogenyNode<String> bb = new BasicPhylogenyNode<String>(b, "bb", 1.2);


	BasicPhylogenyNode<String> baa = new BasicPhylogenyNode<String>(ba, "baa", 2.1);
	BasicPhylogenyNode<String> bab = new BasicPhylogenyNode<String>(ba, "bab", 2.2);


	BasicPhylogenyNode<String> bba = new BasicPhylogenyNode<String>(bb, "bba", 3.1);
	BasicPhylogenyNode<String> bbb = new BasicPhylogenyNode<String>(bb, "bbb", 3.2);


	BasicPhylogenyNode<String> bbbb = new BasicPhylogenyNode<String>(bbb, "bbbb", 4.1);


	BasicPhylogenyNode<String> ca = new BasicPhylogenyNode<String>(c, "ca", 2);
	BasicPhylogenyNode<String> cb = new BasicPhylogenyNode<String>(c, "cb", 3);

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

		BasicPhylogenyNode<String> extraNode = new BasicPhylogenyNode<String>(ba, "bac", 10);

		assert b.getLargestLengthSpan() == 19.6;

		ba.removeChild(extraNode);

		assert b.getLargestLengthSpan() == 11.8;
		}


	@Test
	public void treeIteratorProvidesAllNodes()
		{
		int i = 0;
		for (LengthWeightHierarchyNode<String> node : root)
			{
			logger.info("Tree iterator provided node: " + node);
			i++;
			}
		assert i == 15;
		}

	@Test
	public void skipAllDescendantsWorksProperly() throws TreeException
		{
		DepthFirstTreeIterator<String, LengthWeightHierarchyNode<String>> it = root.depthFirstIterator();
		assert it.next() == root;

		int i = 0;
		while (it.hasNext())
			{
			HierarchyNode<String, LengthWeightHierarchyNode<String>> topLevelNode = it.next();

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
		DepthFirstTreeIterator<String, LengthWeightHierarchyNode<String>> it = root.depthFirstIterator();
		assert it.next() == root;

		while (it.hasNext())
			{
			HierarchyNode<String, LengthWeightHierarchyNode<String>> topLevelNode = it.next();

			int i = 0;
			DepthFirstTreeIterator<String, LengthWeightHierarchyNode<String>> sub =
					((BasicPhylogenyNode) topLevelNode).depthFirstIterator();
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

		BasicPhylogenyNode<String> tree = rootPhylogeny.extractTreeWithLeafPaths(theAncestorLists);

		for (LengthWeightHierarchyNode<String> xnode : tree)
			{
			BasicPhylogenyNode<String> node = (BasicPhylogenyNode<String>) xnode;
			if (node.getValue().equals("root"))
				{
				assert node.getChildren().size() == 2;
				}
			else if (node.getValue().equals("baa"))
				{
				assert node.getLength() == 3.2;
				}
			else if (node.getValue().equals("b"))
				{
				assert node.getLength() == 4;
				}
			else if (node.getValue().equals("bbbb"))
				{
				assert node.getLength() == 8.5;
				}
			else if (node.getValue().equals("ca"))
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

