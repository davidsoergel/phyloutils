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

import com.davidsoergel.dsutils.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public abstract class AbstractRootedPhylogeny<T> implements RootedPhylogeny<T>
	{
	private static final Logger logger = Logger.getLogger(AbstractRootedPhylogeny.class);

	public T commonAncestor(Set<T> knownMergeIds)
		{
		Set<List<PhylogenyNode<T>>> theAncestorLists = new HashSet<List<PhylogenyNode<T>>>();
		for (T id : knownMergeIds)
			{
			theAncestorLists.add(getNode(id).getAncestorPath());
			}
		PhylogenyNode<T> commonAncestor = null;

		while (CollectionUtils.allFirstElementsEqual(theAncestorLists))
			{
			commonAncestor = CollectionUtils.removeAllFirstElements(theAncestorLists);
			}

		if (commonAncestor == null)
			{
			return null;
			}

		return commonAncestor.getValue();
		}

	public T commonAncestor(T nameA, T nameB)
		{
		PhylogenyNode<T> a = getNode(nameA);
		PhylogenyNode<T> b = getNode(nameB);

		List<PhylogenyNode<T>> ancestorsA = a.getAncestorPath();
		List<PhylogenyNode<T>> ancestorsB = b.getAncestorPath();

		PhylogenyNode<T> commonAncestor = null;
		while (ancestorsA.size() > 0 && ancestorsB.size() > 0 && ancestorsA.get(0) == ancestorsB.get(0))
			{
			commonAncestor = ancestorsA.remove(0);
			ancestorsB.remove(0);
			}

		if (commonAncestor == null)
			{
			return null;
			}

		return commonAncestor.getValue();
		}


	public RootedPhylogeny<T> extractTreeWithLeaves(Collection<T> ids)
		{
		Set<List<PhylogenyNode<T>>> theAncestorLists = new HashSet<List<PhylogenyNode<T>>>();
		for (T id : ids)
			{
			theAncestorLists.add(getNode(id).getAncestorPath());
			}

		PhylogenyNode<T> commonAncestor = null;
		try
			{
			commonAncestor = extractTreeWithPaths(theAncestorLists);
			}
		catch (PhyloUtilsException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}

		BasicRootedPhylogeny<T> newRoot = new BasicRootedPhylogeny<T>();
		//newRoot.setLength(new Double(0));  // implicit
		newRoot.setValue(commonAncestor.getValue());

		for (PhylogenyNode<T> child : commonAncestor.getChildren())
			{
			new BasicPhylogenyNode<T>(newRoot.getRoot(), child);// may produce ClassCastException
			//child.setParent(newRoot);
			}

		return newRoot;
		}


	protected BasicPhylogenyNode<T> extractTreeWithPaths(Set<List<PhylogenyNode<T>>> theAncestorLists)
			throws PhyloUtilsException
		{
		double accumulatedLength = 0;

		// use this as a marker to test that the provided lists were actually consistent
		PhylogenyNode<T> commonAncestor = null;

		while (CollectionUtils.allFirstElementsEqual(theAncestorLists))
			{
			commonAncestor = CollectionUtils.removeAllFirstElements(theAncestorLists);
			Double d = commonAncestor.getLength();
			if (d == null)
				{
				logger.warn("Ignoring null length at node " + commonAncestor);
				}
			else
				{
				accumulatedLength += d;
				}
			}

		if (commonAncestor == null)
			{
			throw new PhyloUtilsException("Provided ancestor lists do not have a common root");
			}

		BasicPhylogenyNode<T> node = new BasicPhylogenyNode<T>(null);
		node.setLength(accumulatedLength);

		// the commonAncestor is now the most recent one, so that's the most sensible name for the new node
		node.setValue(commonAncestor.getValue());

		Collection<Set<List<PhylogenyNode<T>>>> childAncestorLists = separateFirstAncestorSets(theAncestorLists);

		for (Set<List<PhylogenyNode<T>>> childAncestorList : childAncestorLists)
			{
			BasicPhylogenyNode<T> child = extractTreeWithPaths(childAncestorList);
			node.getChildren().add(child);
			child.setParent(node);
			}

		return node;
		}


	private Collection<Set<List<PhylogenyNode<T>>>> separateFirstAncestorSets(
			Set<List<PhylogenyNode<T>>> theAncestorLists)
		{
		// assert allFirstElementsEqual(theAncestorLists);

		Map<PhylogenyNode<T>, Set<List<PhylogenyNode<T>>>> theSeparatedSets =
				new HashMap<PhylogenyNode<T>, Set<List<PhylogenyNode<T>>>>();

		for (List<PhylogenyNode<T>> theAncestorList : theAncestorLists)
			{
			if (theAncestorList.isEmpty())
				{
				//we've arrived at one of the originally requested nodes.

				// if it's a leaf, then theAncestorLists should contain only one (empty) list.
				// no problem, we just return an empty set since there are no children.

				// if it's an internal node, we can just ignore it since it's already accounted for in the subtree extraction.
				// we do want to process any descendants though.

				// in either case, we just ignore this situation.
				}
			else
				{
				PhylogenyNode<T> commonAncestor = theAncestorList.get(0);
				Set<List<PhylogenyNode<T>>> theChildList = theSeparatedSets.get(commonAncestor);
				if (theChildList == null)
					{
					theChildList = new HashSet<List<PhylogenyNode<T>>>();
					theSeparatedSets.put(commonAncestor, theChildList);
					}
				theChildList.add(theAncestorList);
				}
			}

		return theSeparatedSets.values();
		}


	public abstract PhylogenyNode<T> getRoot();

	public double distanceBetween(T nameA, T nameB)
		{
		PhylogenyNode a = getNode(nameA);
		PhylogenyNode b = getNode(nameB);

		List<BasicPhylogenyNode> ancestorsA = a.getAncestorPath();
		List<BasicPhylogenyNode> ancestorsB = b.getAncestorPath();

		while (ancestorsA.size() > 0 && ancestorsB.size() > 0 && ancestorsA.get(0) == ancestorsB.get(0))
			{
			ancestorsA.remove(0);
			ancestorsB.remove(0);
			}

		double dist = 0;
		for (BasicPhylogenyNode n : ancestorsA)
			{
			dist += n.getLength();
			}
		for (BasicPhylogenyNode n : ancestorsB)
			{
			dist += n.getLength();
			}

		return dist;
		}

	public double getTotalBranchLength()
		{
		double result = 0;
		for (PhylogenyNode<T> node : getNodes())
			{
			result += node.getLength();
			}
		return result;
		}

	public void normalizeWeights()
		{
		// first normalize at the leaves
		double total = 0;

		for (PhylogenyNode<T> leaf : getLeaves())
			{
			total += leaf.getWeight();
			}

		for (PhylogenyNode<T> leaf : getLeaves())
			{
			leaf.setWeight(leaf.getWeight() / total);
			}

		// then propagate up

		propagateWeightFromBelow();

		}
	}
