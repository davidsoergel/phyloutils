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

import java.util.Collection;
import java.util.HashSet;
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
public class HybridRootedPhylogeny<T> implements TaxonMergingPhylogeny<T>//extends AbstractRootedPhylogeny<Integer>
	{

	RootedPhylogeny<T> rootPhylogeny;
	RootedPhylogeny<T> leafPhylogeny;

	//private Map<T, T> nearestKnownAncestorCache = new HashMap<T, T>();

	public HybridRootedPhylogeny(RootedPhylogeny<T> rootPhylogeny, RootedPhylogeny<T> leafPhylogeny)
		{
		this.rootPhylogeny = rootPhylogeny;
		this.leafPhylogeny = leafPhylogeny;
		}

	public T nearestKnownAncestor(T leafId) throws PhyloUtilsException
		{
		return leafPhylogeny.nearestKnownAncestor(rootPhylogeny, leafId);
		}

	/**
	 * {@inheritDoc}
	 */
	public T nearestAncestorWithBranchLength(T id) throws PhyloUtilsException
		{
		T rootId = nearestKnownAncestor(id);
		return rootPhylogeny.nearestAncestorWithBranchLength(rootId);
		}

	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> integers) throws PhyloUtilsException
		{
		return extractTreeWithLeafIDs(integers, false);
		}


	/**
	 * {@inheritDoc}
	 */
	public RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		// this ought to work even if some of the requested ids are in the root tree rather than the leaf tree,
		// as long as the leaf tree also has a node with the same ID (even with the wrong topology)

		RootedPhylogeny<T> basicLeaf = leafPhylogeny.extractTreeWithLeafIDs(ids, ignoreAbsentNodes);
		HybridRootedPhylogeny<T> extractedHybrid = new HybridRootedPhylogeny<T>(rootPhylogeny, basicLeaf);
		RootedPhylogeny<T> result = extractedHybrid.convertToBasic();

		// now result has all the requested leaves, and the rootwards topology has been adjusted to match the root tree.
		// however, there may be stranded branches, i.e. former ancestors of the leaves (according to the leaf tree) that
		// are no longer ancestors of anything in the hybrid tree.  So, we need to remove those.

		return result.extractTreeWithLeafIDs(ids);
		}

	private RootedPhylogeny<T> convertToBasic() throws PhyloUtilsException
		{
		reconciledLeafNodes = new HashSet<PhylogenyNode<T>>();

		// make sure the root ID matches
		//	leafPhylogeny.setValue(rootPhylogeny.getValue());

		for (T leafId : leafPhylogeny.getLeafValues())
			{
			T joinId = nearestKnownAncestor(leafId);

			PhylogenyNode<T> leafJoinNode = leafPhylogeny.getNode(joinId);
			PhylogenyNode<T> rootJoinNode = rootPhylogeny.getNode(joinId);
			reconcileLeafPhylogenyAt(leafJoinNode, rootJoinNode);
			}
		return leafPhylogeny;
		}

	private Set<PhylogenyNode<T>> reconciledLeafNodes;

	private void reconcileLeafPhylogenyAt(PhylogenyNode<T> leafJoinNode, PhylogenyNode<T> rootJoinNode)
		{
		if (reconciledLeafNodes.contains(leafJoinNode))
			{
			return;
			}

		leafJoinNode.setLength(rootJoinNode.getLength());
		leafJoinNode.setWeight(rootJoinNode.getWeight());

		PhylogenyNode<T> rootParent = rootJoinNode.getParent();

		if (rootParent != null)
			{
			T parentId = rootParent.getValue();
			PhylogenyNode<T> leafParent = leafPhylogeny.getNode(parentId);

			if (leafParent == null)
				{
				leafParent = new BasicPhylogenyNode<T>();
				leafParent.setValue(parentId);
				}

			//PhylogenyNode<T> obsoleteLeafParent = leafJoinNode.getParent();
			leafJoinNode.setParent(leafParent);

			reconcileLeafPhylogenyAt(leafParent, rootParent);

			//leafPhylogeny.removeNode(obsoleteLeafParent);
			}
		/*		else
		   {
		   // if the root parent is null at this node, then the leaf node must also be the root.

		   leafJoinNode.setValue(rootJoinNode.getValue());
		   }*/
		reconciledLeafNodes.add(leafJoinNode);
		}

	public RootedPhylogeny<T> getRootPhylogeny()
		{
		return rootPhylogeny;
		}


	public boolean isDescendant(T ancestor, T descendant) throws PhyloUtilsException
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

	public void saveState()
		{
		leafPhylogeny.saveState();
		}
	}
