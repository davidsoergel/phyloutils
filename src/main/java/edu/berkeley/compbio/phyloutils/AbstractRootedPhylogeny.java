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

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.stats.ContinuousDistribution1D;
import com.google.common.collect.Multiset;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * Abstract implementation of the RootedPhylogeny interface, providing all required functionality that is not
 * implementation-specific.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public abstract class AbstractRootedPhylogeny<T> implements RootedPhylogeny<T>
	{
	private static final Logger logger = Logger.getLogger(AbstractRootedPhylogeny.class);
	private RootedPhylogeny<T> basePhylogeny = null;

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	public T commonAncestor(Set<T> knownMergeIds)
		{
		Set<List<PhylogenyNode<T>>> theAncestorLists = new HashSet<List<PhylogenyNode<T>>>();
		for (T id : knownMergeIds)
			{
			theAncestorLists.add(getNode(id).getAncestorPath());
			}
		PhylogenyNode<T> commonAncestor = null;

		while (DSCollectionUtils.allFirstElementsEqual(theAncestorLists))
			{
			commonAncestor = DSCollectionUtils.removeAllFirstElements(theAncestorLists);
			}

		if (commonAncestor == null)
			{
			return null;
			}

		return commonAncestor.getValue();
		}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
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


	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids) throws PhyloUtilsException
		{
		return extractTreeWithLeafIDs(ids, false);
		}

	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException

		{
		Set<PhylogenyNode<T>> theLeaves = new HashSet<PhylogenyNode<T>>();
		for (T id : ids)
			{
			PhylogenyNode<T> n = getNode(id);
			if (n == null)
				{
				if (!ignoreAbsentNodes)
					{
					throw new NoSuchElementException("Can't extract tree; requested node " + id + " not found");
					}
				}
			else
				{
				theLeaves.add(n);
				}
			}
		RootedPhylogeny<T> result = extractTreeWithLeaves(theLeaves);
		Collection<T> gotLeaves = result.getLeafValues();
		Collection<T> gotNodes = result.getNodeValues();

		// all the leaves that were found were leaves that were requested
		assert ids.containsAll(gotLeaves);

		// some requested leaves may turn out to be internal nodes, but at least they should all be accounted for
		assert gotNodes.containsAll(ids);

		//assert gotLeaves.containsAll(ids);
		return result;
		}


	public RootedPhylogeny<T> extractTreeWithLeaves(Collection<PhylogenyNode<T>> leaves) throws PhyloUtilsException
		{
		Set<List<PhylogenyNode<T>>> theAncestorLists = new HashSet<List<PhylogenyNode<T>>>();
		for (PhylogenyNode<T> leaf : leaves)
			{
			theAncestorLists.add(leaf.getAncestorPath());
			}

		BasicPhylogenyNode<T> commonAncestor = null;
		try
			{
			commonAncestor = extractTreeWithLeafPaths(theAncestorLists);
			}
		catch (PhyloUtilsException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}

		// always use the same root, even if it has only one child
		BasicRootedPhylogeny<T> newTree = new BasicRootedPhylogeny<T>(this.getValue());

		if (commonAncestor.getValue() != this.getValue())
			{
			// add a single branch descending from the root to the common ancestor
			commonAncestor.setParent(newTree.getRoot());
			//newRoot = new BasicPhylogenyNode<T>(newRoot, commonAncestor.getValue(), commonAncestor.getLength());
			}
		else
			{
			newTree.setRoot(commonAncestor);
			}

		// now the root is a copy of the common ancestor node.
		// also need to deep copy the whole tree.

		//deepCopy(commonAncestor, newRoot);

		newTree.updateNodes(null);
		newTree.setBasePhylogeny(this);

		//		assert newTree.getNodes().containsAll(leaves);
		//		assert CollectionUtils.isEqualCollection(newTree.getLeaves(),leaves);

		return newTree;
		}

	/*
	private void deepCopy(PhylogenyNode<T> from, BasicPhylogenyNode<T> to)
		{
		for (PhylogenyNode<T> fromChild : from.getChildren())
			{
			BasicPhylogenyNode<T> toChild = new BasicPhylogenyNode<T>(to, fromChild);// may produce ClassCastException
			deepCopy(fromChild, toChild);
			//child.setParent(newRoot);
			}
		}
	*/

	/**
	 * note this does not allow for the case where one path terminates at an internal node of another path.  Wait, yes it
	 * does...??
	 *
	 * @param theAncestorLists
	 * @return
	 * @throws PhyloUtilsException
	 */
	protected BasicPhylogenyNode<T> extractTreeWithLeafPaths(Set<List<PhylogenyNode<T>>> theAncestorLists)
			throws PhyloUtilsException
		{
		double accumulatedLength = 0;

		// use this as a marker to test that the provided lists were actually consistent
		PhylogenyNode<T> commonAncestor = null;


		while (DSCollectionUtils.allFirstElementsEqual(theAncestorLists))
			{
			commonAncestor = DSCollectionUtils.removeAllFirstElements(theAncestorLists);
			Double d = commonAncestor.getLength();
			if (d == null)
				{
				//logger.warn("Ignoring null length at node " + commonAncestor);
				}
			else
				{
				accumulatedLength += d;
				}
			}
		/*
		  for(List<PhylogenyNode<T>> ancestorList : theAncestorLists)
			  {
			  if(ancestorList.isEmpty())
				  {
				  logger.warn("")
				  }
			  }
  */

		if (commonAncestor == null)
			{
			throw new PhyloUtilsException("Provided ancestor lists do not have a common root");
			}

		BasicPhylogenyNode<T> node = new BasicPhylogenyNode<T>();
		node.setLength(accumulatedLength);

		// the commonAncestor is now the most recent one, so that's the most sensible name for the new node
		node.setValue(commonAncestor.getValue());

		Collection<Set<List<PhylogenyNode<T>>>> childAncestorLists = separateFirstAncestorSets(theAncestorLists);

		for (Set<List<PhylogenyNode<T>>> childAncestorList : childAncestorLists)
			{
			BasicPhylogenyNode<T> child = extractTreeWithLeafPaths(childAncestorList);
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

	/**
	 * Gets the root node of the tree
	 *
	 * @return
	 */
	public abstract PhylogenyNode<T> getRoot();

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public double getTotalBranchLength()
		{
		double result = 0;
		for (PhylogenyNode<T> node : getNodes())
			{
			if (node.getLength() != null)// count null length as zero
				{
				result += node.getLength();
				}
			}
		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setAllBranchLengthsToNull()
		{
		double result = 0;
		for (PhylogenyNode<T> node : getNodes())
			{
			node.setLength(null);
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public void randomizeLeafWeights(
			ContinuousDistribution1D speciesAbundanceDistribution)//throws DistributionException
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			leaf.setWeight(speciesAbundanceDistribution.sample());
			}

		normalizeWeights();
		}

	/**
	 * {@inheritDoc}
	 */
	public void uniformizeLeafWeights()//throws DistributionException
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			leaf.setWeight(1.);
			}

		normalizeWeights();
		}

	/**
	 * {@inheritDoc}
	 */
	public void setLeafWeights(Multiset<T> leafWeights)
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			leaf.setWeight(new Double(leafWeights.count(leaf.getValue())));
			}

		normalizeWeights();
		}

	/**
	 * {@inheritDoc}
	 */
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

		//propagateWeightFromBelow();
		}


	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> getBasePhylogeny()
		{
		return basePhylogeny;
		}

	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> getBasePhylogenyRecursive()
		{
		if (basePhylogeny == null)
			{
			return this;
			}
		return basePhylogeny.getBasePhylogenyRecursive();
		}

	public void setBasePhylogeny(RootedPhylogeny<T> basePhylogeny)
		{
		this.basePhylogeny = basePhylogeny;
		}

	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> extractIntersectionTree(Collection<T> leafIdsA, Collection<T> leafIdsB)
			throws PhyloUtilsException
		{
		Set<PhylogenyNode<T>> allTreeNodesA = new HashSet<PhylogenyNode<T>>();
		for (T id : leafIdsA)
			{
			allTreeNodesA.addAll(getNode(id).getAncestorPath());
			}

		Set<PhylogenyNode<T>> allTreeNodesB = new HashSet<PhylogenyNode<T>>();
		for (T id : leafIdsB)
			{
			allTreeNodesB.addAll(getNode(id).getAncestorPath());
			}

		allTreeNodesA.retainAll(allTreeNodesB);

		// now allTreeNodesA contains all nodes that are in common between the two input leaf sets, including internal nodes

		// remove internal nodes
		for (PhylogenyNode<T> node : new HashSet<PhylogenyNode<T>>(allTreeNodesA))
			{
			allTreeNodesA.remove(node.getParent());
			}


		return extractTreeWithLeaves(allTreeNodesA);
		}

	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> mixWith(RootedPhylogeny<T> otherTree, double mixingProportion) throws PhyloUtilsException
		{
		if (mixingProportion < 0 || mixingProportion > 1)
			{
			throw new PhyloUtilsException("Mixing proportion must be between 0 and 1");
			}

		//RootedPhylogeny<T> theBasePhylogeny = getBasePhylogeny();
		if (basePhylogeny == null || basePhylogeny != otherTree.getBasePhylogeny())
			{
			throw new PhyloUtilsException(
					"Phylogeny mixtures can be computed only between trees extracted from the same underlying tree");
			}

		Set<T> unionLeaves = new HashSet<T>();
		unionLeaves.addAll(getLeafValues());
		unionLeaves.addAll(otherTree.getLeafValues());

		RootedPhylogeny<T> unionTree = basePhylogeny.extractTreeWithLeafIDs(unionLeaves);

		for (PhylogenyNode<T> node : getLeaves())
			{
			unionTree.getNode(node.getValue()).setWeight(node.getWeight() * mixingProportion);
			}

		for (PhylogenyNode<T> node : otherTree.getLeaves())
			{
			unionTree.getNode(node.getValue()).incrementWeightBy(node.getWeight() * (1. - mixingProportion));
			}

		unionTree.normalizeWeights();
		return unionTree;
		}


	/**
	 * {@inheritDoc}
	 */
	public void smoothWeightsFrom(RootedPhylogeny<T> otherTree, double smoothingFactor)//throws PhyloUtilsException
		{
		/*RootedPhylogeny<T> theBasePhylogeny = getBasePhylogeny();
		if (theBasePhylogeny != otherTree.getBasePhylogeny())
			{
			throw new PhyloUtilsException(
					"Phylogeny mixtures can be computed only between trees extracted from the same underlying tree");
			}
*/

		//** if the otherTree has leaves that are not present in this tree, we'll ignore them and never know.
		// That circumstance should probably throw an exception, but it's a bit of a drag to test for it.


		for (PhylogenyNode<T> leaf : getLeaves())//theBasePhylogeny.getLeaves())
			{
			T leafId = leaf.getValue();
			PhylogenyNode<T> otherLeaf = otherTree.getNode(leafId);
			getNode(leafId).setWeight((otherLeaf == null ? 0 : otherLeaf.getWeight()) + smoothingFactor);
			}

		normalizeWeights();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract RootedPhylogeny<T> clone();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()

		{
		StringBuffer sb = new StringBuffer("\n");

		appendSubtree(sb, "");
		return sb.toString();
		}

	public void saveState()
		{
		}
	}
