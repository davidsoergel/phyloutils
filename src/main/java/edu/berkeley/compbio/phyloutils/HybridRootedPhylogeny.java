/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.AbstractRootedPhylogeny;
import com.davidsoergel.trees.BasicPhylogenyNode;
import com.davidsoergel.trees.BasicRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
import com.davidsoergel.trees.RootedPhylogeny;
import com.davidsoergel.trees.TaxonMergingPhylogeny;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Sometimes we have branch lengths on one phylogeny, but we want to know distances between nodes that are not on that
 * phylogeny.  In that case we may want to travel up a different phylogeny (e.g., the NCBI taxonomy) until we find
 * matching nodes, and thereby get at least a lower bound on the distance.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public class HybridRootedPhylogeny<T extends Serializable>
		implements TaxonMergingPhylogeny<T>//extends AbstractRootedPhylogeny<Integer>
	{
	private static final Logger logger = Logger.getLogger(HybridRootedPhylogeny.class);

	RootedPhylogeny<T> rootPhylogeny;
	RootedPhylogeny<T> leafPhylogeny;

	//private Map<T, T> nearestKnownAncestorCache = new HashMap<T, T>();

	public HybridRootedPhylogeny(RootedPhylogeny<T> rootPhylogeny, RootedPhylogeny<T> leafPhylogeny)
		{
		this.rootPhylogeny = rootPhylogeny;
		this.leafPhylogeny = leafPhylogeny;
		}

	public T nearestKnownAncestor(T leafId) throws NoSuchNodeException
		{
		if (rootPhylogeny.getUniqueIdToNodeMap().containsKey(leafId))
			{
			return leafId;
			}
		return leafPhylogeny.nearestKnownAncestor(rootPhylogeny, leafId);
		}

	/**
	 * {@inheritDoc}
	 */
	public T nearestAncestorWithBranchLength(T id) throws NoSuchNodeException
		{
		T rootId = nearestKnownAncestor(id);
		return rootPhylogeny.nearestAncestorWithBranchLength(rootId);
		}

/*	public PhylogenyNode<T> nearestAncestorWithBranchLength(PhylogenyNode<T> id) throws PhyloUtilsException
		{
		return rootPhylogeny.nearestAncestorWithBranchLength(id);
		}*/

	/**
	 * {@inheritDoc}
	 */
/*	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> integers) throws NoSuchNodeException
		{
		return extractTreeWithLeafIDs(integers, false, false);
		}*/


	/**
	 * Returns the IDs of all the leaf nodes of the tree.
	 *
	 * @return the IDs of all the leaf nodes of the tree.
	 */
	public Set<T> getLeafValues()
		{
		return leafPhylogeny.getLeafValues();
		}

	/*	public RootedPhylogeny<T> extractTreeWithLeaves(Collection<PhylogenyNode<T>> ids) throws PhyloUtilsException
		 {
		 // this ought to work even if some of the requested ids are in the root tree rather than the leaf tree,
		 // as long as the leaf tree also has a node with the same ID (even with the wrong topology)

		 RootedPhylogeny<T> basicLeaf = leafPhylogeny.extractTreeWithLeaves(ids);
		 HybridRootedPhylogeny<T> extractedHybrid = new HybridRootedPhylogeny<T>(rootPhylogeny, basicLeaf);
		 RootedPhylogeny<T> result = extractedHybrid.convertToBasic();

		 // now result has all the requested leaves, and the rootwards topology has been adjusted to match the root tree.
		 // however, there may be stranded branches, i.e. former ancestors of the leaves (according to the leaf tree) that
		 // are no longer ancestors of anything in the hybrid tree.  So, we need to remove those.

		 return result.extractTreeWithLeaves(ids);
		 }
 */
	/**
	 * {@inheritDoc}
	 */
	public BasicRootedPhylogeny<T> extractTreeWithLeafIDs(Set<T> ids, boolean ignoreAbsentNodes,
	                                                      boolean includeInternalBranches)
			throws NoSuchNodeException //, NodeNamer<T> namer
		{
		return extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches,
		                              AbstractRootedPhylogeny.MutualExclusionResolutionMode.EXCEPTION);
		}

	/**
	 * {@inheritDoc}
	 */
	public BasicRootedPhylogeny<T> extractTreeWithLeafIDs(Set<T> ids, boolean ignoreAbsentNodes,
	                                                      boolean includeInternalBranches,
	                                                      AbstractRootedPhylogeny.MutualExclusionResolutionMode mode)
			throws NoSuchNodeException //, NodeNamer<T> namer
		{
		// this ought to work even if some of the requested ids are in the root tree rather than the leaf tree,
		// as long as the leaf tree also has a node with the same ID (even with the wrong topology)

		BasicRootedPhylogeny<T> basicLeaf = leafPhylogeny.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, true, mode);
		ExplicitHybridRootedPhylogeny<T> extractedHybrid = null;
		try
			{
			extractedHybrid = new ExplicitHybridRootedPhylogeny<T>(rootPhylogeny, basicLeaf);
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}


		// now result has all the requested leaves, and the rootwards topology has been adjusted to match the root tree.
		// however, there may be stranded branches, i.e. former ancestors of the leaves (according to the leaf tree) that
		// are no longer ancestors of anything in the hybrid tree.  So, we need to remove those.

		// also, we had to include internal branches so far, but now we can remove them

		return extractedHybrid.extractTreeWithLeafIDs(ids, ignoreAbsentNodes, includeInternalBranches, mode);
		}


	public RootedPhylogeny<T> getRootPhylogeny()
		{
		return rootPhylogeny;
		}


	public boolean isDescendant(T ancestor, T descendant) throws NoSuchNodeException
		{
		T nearestDescendant = nearestKnownAncestor(descendant);

		// if the ancestor is in the root phylogeny, then check that the descendant is below it

		if (rootPhylogeny.getNode(ancestor) != null)
			{
			return ancestor.equals(rootPhylogeny.commonAncestor(ancestor, nearestDescendant));
			}

		// otherwise the ancestor is in the leaf phylogeny.
		// It's not good enough to check ancestry in the leaf phylogeny alone, since the root phylogeny may disagree.

		T nearestAncestor = nearestKnownAncestor(ancestor);
		if (!nearestAncestor.equals(nearestDescendant))
			{
			return false;
			}

		// OK, both ancestor and descendant exist only in the leaf phylogeny, and they're in the same clade wrt the root phylogeny.

		return ancestor.equals(leafPhylogeny.commonAncestor(ancestor, descendant));
		}
/*
	public void saveState()
		{
		leafPhylogeny.saveState();
		}
*/

	public Set<T> selectAncestors(final Collection<T> labels, final T id)
		{
		throw new NotImplementedException();
		}

	public List<T> getAncestorPathIds(final T id) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	@NotNull
	public List<PhylogenyNode<T>> getAncestorPath(final T id) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}

	@NotNull
	public List<BasicPhylogenyNode<T>> getAncestorPathAsBasic(final T id) throws NoSuchNodeException
		{
		throw new NotImplementedException();
		}
	}
