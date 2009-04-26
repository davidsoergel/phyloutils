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
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.davidsoergel.stats.ContinuousDistribution1D;
import com.google.common.collect.Multiset;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
	protected transient RootedPhylogeny<T> basePhylogeny = null;

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public T commonAncestor(Collection<T> knownMergeIds) throws NoSuchNodeException
		{
		return commonAncestor(knownMergeIds, 1.0);
		}

	public RootedPhylogeny<T> asRootedPhylogeny()
		{
		return this;
		}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public T commonAncestor(Collection<T> knownMergeIds, double proportion) throws NoSuchNodeException
		{
		Set<List<PhylogenyNode<T>>> theAncestorLists = new HashSet<List<PhylogenyNode<T>>>();
		for (T id : knownMergeIds)
			{
			try
				{
				PhylogenyNode<T> node = getNode(id);
				theAncestorLists.add(node.getAncestorPath());
				}
			catch (NoSuchNodeException e)
				{
				logger.debug("Node not found with id " + id + " when looking for common ancestor; ignoring");
				}
			}

		int numberThatMustAgree = (int) Math.ceil(theAncestorLists.size() * proportion);

		PhylogenyNode<T> commonAncestor = null;

		try
			{
			while (true)
				{
				commonAncestor = DSCollectionUtils.getDominantFirstElement(theAncestorLists,
				                                                           numberThatMustAgree);  // throws NoSuchElementException
				theAncestorLists = DSCollectionUtils.filterByAndRemoveFirstElement(theAncestorLists, commonAncestor);
				}
			}
		catch (NoSuchElementException e)
			{
			// good, broke the loop, leaving commonAncestor and theAncestorLists in the most recent valid state
			}


		/*
		while (DSCollectionUtils.allFirstElementsEqual(theAncestorLists))
			{
			commonAncestor = DSCollectionUtils.removeAllFirstElements(theAncestorLists);
			}
*/

		if (commonAncestor == null)
			{
			throw new NoSuchNodeException("Nodes have no common ancestor");
			//return null;
			}

		return commonAncestor.getValue();
		}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public T commonAncestor(T nameA, T nameB) throws NoSuchNodeException
		{
		PhylogenyNode<T> a = getNode(nameA);
		PhylogenyNode<T> b = getNode(nameB);
		return commonAncestor(a, b).getValue();
		}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public PhylogenyNode<T> commonAncestor(@NotNull PhylogenyNode<T> a, @NotNull PhylogenyNode<T> b)
			throws NoSuchNodeException
		{
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
			throw new NoSuchNodeException("Nodes have no common ancestor");
			}

		return commonAncestor;
		}


	public boolean isDescendant(T ancestor, T descendant)
		{
		try
			{
			return ancestor.equals(commonAncestor(ancestor, descendant));
			}
		catch (NoSuchNodeException e)
			{
			return false;
			}
		}

	public boolean isDescendant(PhylogenyNode<T> ancestor, PhylogenyNode<T> descendant)
		{
		try
			{
			return ancestor.equals(commonAncestor(ancestor, descendant));
			}
		catch (NoSuchNodeException e)
			{
			return false;
			}
		}

	/**
	 * {@inheritDoc}
	 */
/*	@NotNull
	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids) throws NoSuchNodeException
		{
		return extractTreeWithLeafIDs(ids, false, false);
		}*/


	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids, boolean ignoreAbsentNodes,
	                                                 boolean includeInternalBranches)
			throws NoSuchNodeException //, NodeNamer<T> namer

		{
		List<PhylogenyNode<T>> theLeaves = idsToLeaves(ids, ignoreAbsentNodes);

		if (theLeaves.isEmpty())
			{
			throw new NoSuchNodeException("No leaves found for ids: " + ids);
			}

		RootedPhylogeny<T> result = extractTreeWithLeaves(theLeaves, includeInternalBranches);
		Collection<T> gotLeaves = result.getLeafValues();
		//Collection<T> gotNodes = result.getNodeValues();

		// all the leaves that were found were leaves that were requested
		assert ids.containsAll(gotLeaves);

		/*
		if (includeInternalBranches)
			{
			// some requested leaves may turn out to be internal nodes, but at least they should all be accounted for
			//assert gotNodes.containsAll(ids);
			}
		*/

		if (!ignoreAbsentNodes)
			{
			// any requested leaves that turned out to be internal nodes should have had a phantom leaf added
			assert gotLeaves.containsAll(ids);
			}
		return result;
		}

	private List<PhylogenyNode<T>> idsToLeaves(Collection<T> ids, boolean ignoreAbsentNodes) throws NoSuchNodeException
		{
		// don't use HashSet, to avoid calling hashcode since that requires a transaction
		//Set<PhylogenyNode<T>> theLeaves = new HashSet<PhylogenyNode<T>>();
		List<PhylogenyNode<T>> theLeaves = new ArrayList<PhylogenyNode<T>>();
		for (T id : ids)
			{
			try
				{
				PhylogenyNode<T> n = getNode(id);
				theLeaves.add(n);
				}
			catch (NoSuchNodeException e)
				{
				if (!ignoreAbsentNodes)
					{
					throw new NoSuchNodeException("Can't extract tree; requested node " + id + " not found");
					}
				}
			}

		return theLeaves;
		}


	@NotNull
	public BasicRootedPhylogeny<T> extractTreeWithLeaves(Collection<PhylogenyNode<T>> leaves,
	                                                     boolean includeInternalBranches) //, NodeNamer<T> namer)
		{
		Set<List<PhylogenyNode<T>>> theAncestorLists = new HashSet<List<PhylogenyNode<T>>>();
		for (PhylogenyNode<T> leaf : leaves)
			{
			theAncestorLists.add(leaf.getAncestorPath());
			}

		BasicPhylogenyNode<T> commonAncestor = null;
		try
			{
			commonAncestor = extractTreeWithLeafPaths(theAncestorLists, includeInternalBranches); //, namer);
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}

		// always use the same root, even if it has only one child
		BasicRootedPhylogeny<T> newTree = new BasicRootedPhylogeny<T>(this.getValue());

		if (!commonAncestor.getValue().equals(this.getValue()))
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

		newTree.assignUniqueIds(new RequireExistingNodeNamer<T>(true));
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
	 * Builds a fresh tree containg all of the requested leaves, which are the last elements in the provided AncestorLists.
	 * Each AncestorList describes the path from the root to one of the leaves.  The roots (the first element of each list)
	 * must be equal; a copy of that root provides the root of the newly built tree. If includeInternalBranches is set,
	 * then all elements of the AncestorLists will be included in the resulting tree even if there is no branching at that
	 * node.
	 *
	 * @param theAncestorLists
	 * @return
	 * @throws PhyloUtilsException
	 */
	@NotNull
	protected BasicPhylogenyNode<T> extractTreeWithLeafPaths(Set<List<PhylogenyNode<T>>> theAncestorLists,
	                                                         boolean includeInternalBranches)
			throws NoSuchNodeException  //, NodeNamer<T> namer)
		{
		// this was spaghetti before when I tried to handle both modes together
		if (includeInternalBranches)
			{
			return extractTreeWithLeafPathsIncludingInternal(theAncestorLists);
			}
		else
			{
			return extractTreeWithLeafPathsExcludingInternal(theAncestorLists);
			}
		}

	private BasicPhylogenyNode<T> extractTreeWithLeafPathsExcludingInternal(
			Set<List<PhylogenyNode<T>>> theAncestorLists) throws NoSuchNodeException
		{
		double accumulatedLength = 0;

		// use this as a marker to test that the provided lists were actually consistent
		PhylogenyNode<T> commonAncestor = null;
		BasicPhylogenyNode<T> bottomOfChain = null;

		// first consume any common prefix on the ancestor lists

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

		// now the lists must differ in their first position, and commonAncestor is set to the immediate parent of whatever the list heads are

		if (commonAncestor == null)  // only possible if allFirstElementsEqual == false on the first attempt
			{
			throw new NoSuchNodeException("Provided ancestor lists do not have a common root");
			}

		// since we are not including internal branches, we now need to create the branching node

		BasicPhylogenyNode<T> node = new BasicPhylogenyNode<T>();
		node.setLength(accumulatedLength);

		// the commonAncestor is now the most recent one, so that's the most sensible name for the new node
		node.setValue(commonAncestor.getValue());
		node.setWeight(commonAncestor.getWeight());
		bottomOfChain = node;

		checkNoInternalNodeRequested(theAncestorLists);

		// split the ancestor lists into sets with a common head

		Collection<Set<List<PhylogenyNode<T>>>> childAncestorLists = separateFirstAncestorSets(theAncestorLists);
		assert childAncestorLists.size() != 1; // otherwise there should be no branch here

		// recurse

		for (Set<List<PhylogenyNode<T>>> childAncestorList : childAncestorLists)
			{
			PhylogenyNode<T> child = extractTreeWithLeafPathsExcludingInternal(childAncestorList);
			child.setParent(bottomOfChain);
			}

		return bottomOfChain.findRoot();
		}


	private BasicPhylogenyNode<T> extractTreeWithLeafPathsIncludingInternal(
			Set<List<PhylogenyNode<T>>> theAncestorLists) throws NoSuchNodeException
		{
		// use this as a marker to test that the provided lists were actually consistent
		PhylogenyNode<T> commonAncestor = null;
		BasicPhylogenyNode<T> bottomOfChain = null;

		// first consume any common prefix on the ancestor lists

		while (DSCollectionUtils.allFirstElementsEqual(theAncestorLists))
			{
			commonAncestor = DSCollectionUtils.removeAllFirstElements(theAncestorLists);

			// copy the common ancestor to the new tree

			BasicPhylogenyNode<T> node = new BasicPhylogenyNode<T>();
			node.setLength(commonAncestor.getLength());
			node.setValue(commonAncestor.getValue());

			//** avoid isLeaf due to ncbi lazy initialization issue
			//if (commonAncestor.isLeaf())
			//	{
			// don't bother with internal weights; they'll get recalculated on demand anyway
			if (bottomOfChain != null)
				{
				bottomOfChain.setWeight(null); // just to be sure
				}
			try
				{
				node.setWeight(commonAncestor.getWeight());
				}
			catch (NotImplementedException e)
				{
				node.setWeight(1.0);
				}

			node.setParent(bottomOfChain);
			bottomOfChain = node;
			checkNoInternalNodeRequested(theAncestorLists);
			}

		// now the lists must differ in their first position, and commonAncestor is set to the immediate parent of whatever the list heads are

		if (commonAncestor == null)  // only possible if allFirstElementsEqual == false on the first attempt
			{
			throw new NoSuchNodeException("Provided ancestor lists do not have a common root");
			}

		// split the ancestor lists into sets with a common head

		Collection<Set<List<PhylogenyNode<T>>>> childAncestorLists = separateFirstAncestorSets(theAncestorLists);

		// recurse

		for (Set<List<PhylogenyNode<T>>> childAncestorList : childAncestorLists)
			{
			PhylogenyNode<T> child = extractTreeWithLeafPathsIncludingInternal(childAncestorList);
			child.setParent(bottomOfChain);
			}

		return bottomOfChain.findRoot();
		}


/*
	private void addPhantomLeafIfNeeded(Set<List<PhylogenyNode<T>>> theAncestorLists, BasicPhylogenyNode<T> node, NodeNamer<T> namer)
		{
		// check if we need a leaf node here
		boolean needStubLeafNode = false;
		for (List<PhylogenyNode<T>> ancestorList : theAncestorLists)
			{
			if (ancestorList.isEmpty())
				{
				needStubLeafNode = true;
				break;
				}
			}

		if (needStubLeafNode)
			{
			// an internal node was requested as a leaf.
			// add a phantom leaf to honor the request, and then continue with the other paths

			BasicPhylogenyNode<T> leaf = new BasicPhylogenyNode<T>();
			leaf.setLength(0.0);
			leaf.setValue(node.getValue());
			leaf.setParent(node);

			//** changing the ID of the internal node may cause trouble later, e.g. when trying to make an intersection tree
			node.setValue(namer.uniqueify(node.getValue()));
			// note we leave bottomOfChain intact
			}
		}
*/

	private void checkNoInternalNodeRequested(Set<List<PhylogenyNode<T>>> theAncestorLists)
		{
		// if there is only one list left, and it's empty, that's OK, we just finished a branch
		// but if there's more than one, and one of them is empty, then we asked for a node as a leaf that turns out to be an ancestor of another leaf

		// if we give the same path twice, that causes a failure here
		if (theAncestorLists.size() > 1)
			{
			for (List<PhylogenyNode<T>> ancestorList : theAncestorLists)
				{
				if (ancestorList.isEmpty())
					{
					throw new PhyloUtilsRuntimeException("Requested extraction of an internal node as a leaf");
					}
				}
			}
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
	 * {@inheritDoc}
	 */
	public double distanceBetween(T nameA, T nameB) throws NoSuchNodeException
		{
		PhylogenyNode a = getNode(nameA);
		PhylogenyNode b = getNode(nameB);
		return distanceBetween(a, b);
		}

	/**
	 * {@inheritDoc}
	 */
	public double distanceBetween(PhylogenyNode<T> a, PhylogenyNode<T> b) throws NoSuchNodeException
		{
		List<PhylogenyNode<T>> ancestorsA = a.getAncestorPath();
		List<PhylogenyNode<T>> ancestorsB = b.getAncestorPath();

		int commonAncestors = 0;

		while (!ancestorsA.isEmpty() && !ancestorsB.isEmpty() && ancestorsA.get(0).equals(ancestorsB.get(0)))
			{
			ancestorsA.remove(0);
			ancestorsB.remove(0);
			commonAncestors++;
			}

		if (commonAncestors == 0)
			{
			throw new NoSuchNodeException("Can't compute distance between nodes with no common ancestor");
			}

		double dist = 0;
		for (PhylogenyNode<T> n : ancestorsA)
			{
			dist += n.getLength();
			}
		for (PhylogenyNode<T> n : ancestorsB)
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
		for (PhylogenyNode<T> node : getUniqueIdToNodeMap().values())
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
		for (PhylogenyNode<T> node : getUniqueIdToNodeMap().values())
			{
			node.setLength(null);
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public void randomizeLeafWeights(
			ContinuousDistribution1D speciesAbundanceDistribution) //throws PhyloUtilsException//throws DistributionException
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			leaf.setWeight(speciesAbundanceDistribution.sample());
			}

		try
			{
			normalizeWeights();
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			throw new Error("Impossible");
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public void uniformizeLeafWeights() // throws PhyloUtilsException//throws DistributionException
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			leaf.setWeight(1.);
			}

		try
			{
			normalizeWeights();
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			throw new Error("Impossible");
			}
		}

	public Map<T, Double> distributeInternalWeightsToLeaves(Map<T, Double> taxIdToWeightMap) throws NoSuchNodeException
		{
		WeightedSet<T> result = new HashWeightedSet<T>();
		for (Map.Entry<T, Double> entry : taxIdToWeightMap.entrySet())
			{
			T id = entry.getKey();
			Double weight = entry.getValue();
			try
				{
				PhylogenyNode<T> n = getNode(id);
				distributeWeight(n, weight, result);
				}
			catch (NoSuchNodeException e)
				{
				// this can only happen if we already issued a warning about "node not found"
				logger.warn("Requested member weight dropped: " + id + " " + weight);
				}
			}
		return result.getItemNormalizedMap();
		}

	private void distributeWeight(PhylogenyNode<T> n, Double weight, WeightedSet<T> result) throws NoSuchNodeException
		{
		if (n.isLeaf())
			{
			result.add(n.getValue(), weight);
			result.incrementItems();
			}
		else
			{
			List<? extends PhylogenyNode<T>> children = n.getChildren();
			double childWeight = weight / children.size();
			for (PhylogenyNode<T> child : children)
				{
				distributeWeight(child, childWeight, result);
				}
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public void setLeafWeights(Multiset<T> leafWeights) throws PhyloUtilsException
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			int value = leafWeights.count(leaf.getValue());
			leaf.setWeight(new Double(value));
			}

		normalizeWeights();
		}

	/**
	 * {@inheritDoc}
	 */
	public void setLeafWeights(Map<T, Double> leafWeights) throws PhyloUtilsException
		{
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			Double value = leafWeights.get(leaf.getValue());
			if (value == null)
				{
				throw new PhyloUtilsException("No leaf weight provided for " + leaf);
				}
			leaf.setWeight(value);
			}

		normalizeWeights();
		}

	public Map<T, Double> getLeafWeights() //throws PhyloUtilsException
		{
		Map<T, Double> result = new HashMap<T, Double>();
		for (PhylogenyNode<T> leaf : getLeaves())
			{
			result.put(leaf.getValue(), leaf.getWeight());
			}
		return result;
		}

	public Map<T, Double> getNodeWeights() //throws PhyloUtilsException
		{
		Map<T, Double> result = new HashMap<T, Double>();
		for (PhylogenyNode<T> node : this)
			{
			result.put(node.getValue(), node.getWeight());
			}
		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	public void normalizeWeights() throws PhyloUtilsException
		{
		// first normalize at the leaves
		double total = 0;

		for (PhylogenyNode<T> leaf : getLeaves())
			{
			Double w = leaf.getWeight();
			if (w == null)
				{
				throw new PhyloUtilsException("Can't normalize when a leaf weight is null");
				}
			total += w;
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
	public RootedPhylogeny<T> extractIntersectionTree(Collection<T> leafIdsA, Collection<T> leafIdsB,
	                                                  NodeNamer<T> namer)
			throws NoSuchNodeException, PhyloUtilsException
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

		return extractTreeWithLeaves(allTreeNodesA, false);
		}

	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> mixWith(RootedPhylogeny<T> otherTree, double mixingProportion) throws PhyloUtilsException
		//NoSuchNodeException
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

		try
			{
			Set<T> unionLeaves = new HashSet<T>();
			unionLeaves.addAll(getLeafValues());
			unionLeaves.addAll(otherTree.getLeafValues());


			RootedPhylogeny<T> unionTree = basePhylogeny.extractTreeWithLeafIDs(unionLeaves, false, false);
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
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		}


	/**
	 * {@inheritDoc}
	 */
	public void smoothWeightsFrom(RootedPhylogeny<T> otherTree, double smoothingFactor)
			throws PhyloUtilsException //throws PhyloUtilsException
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

		try
			{
			for (PhylogenyNode<T> leaf : getLeaves())//theBasePhylogeny.getLeaves())
				{
				T leafId = leaf.getValue();
				PhylogenyNode<T> otherLeaf = null;
				final PhylogenyNode<T> node = getNode(leafId);
				try
					{
					otherLeaf = otherTree.getNode(leafId);
					node.setWeight(otherLeaf.getWeight() + smoothingFactor);
					}
				catch (NoSuchNodeException e)
					{
					node.setWeight(smoothingFactor);
					}
				}
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
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
/*
	public void saveState()
		{
		}
*/

	@NotNull
	public T getShallowestLeaf()
		{
		try
			{
			T shallowestId = null;
			double shallowestDepth = Double.POSITIVE_INFINITY;

			for (PhylogenyNode<T> n : getLeaves())
				{
				//PhylogenyNode<Integer> n = theIntegerTree.getNode(id);
				double depth = distanceBetween(getRoot(), n);
				T nId = n.getValue();

				// BAD if two depths are exactly equal, then the result is nondeterministic

				// try to impose a deterministic order using the id hashcodes
				if (depth < shallowestDepth || (depth == shallowestDepth && nId.hashCode() < shallowestId.hashCode()))
					{
					shallowestDepth = depth;
					shallowestId = nId;
					}
				}
			return shallowestId;
			}
		catch (NoSuchNodeException e)
			{
			throw new Error("Impossible");
			}
		}

	public PhylogenyNode<T> getFirstBranchingNode()
		{
		PhylogenyNode<T> r = getRoot();
		while (r.getChildren().size() == 1)
			{
			r = r.getChildren().iterator().next();
			}
		return r;
		}
	}
