/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.BasicPhylogenyNode;
import com.davidsoergel.trees.BasicRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
import com.davidsoergel.trees.RootedPhylogeny;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ExplicitHybridRootedPhylogeny<T extends Serializable> extends BasicRootedPhylogeny<T>
	{
	private BasicRootedPhylogeny<T> leafPhylogeny;
	private RootedPhylogeny<T> rootPhylogeny;

	public ExplicitHybridRootedPhylogeny(final RootedPhylogeny<T> rootPhylogeny,
	                                     final BasicRootedPhylogeny<T> basicLeaf)
			throws NoSuchNodeException, PhyloUtilsException
		{
		super();
		this.leafPhylogeny = basicLeaf;
		this.rootPhylogeny = rootPhylogeny;

		reconcile();
		}

	Set<PhylogenyNode<T>> reconciledLeafNodes;

	private void reconcile() throws NoSuchNodeException, PhyloUtilsException
		{
		reconciledLeafNodes = new HashSet<PhylogenyNode<T>>();

		// make sure the root ID matches
		//	leafPhylogeny.setValue(rootPhylogeny.getValue());

		for (T leafId : leafPhylogeny.getLeafValues())
			{
			T joinId = nearestKnownAncestor(rootPhylogeny, leafId);

			PhylogenyNode<T> leafJoinNode = leafPhylogeny.getNode(joinId);
			PhylogenyNode<T> rootJoinNode = rootPhylogeny.getNode(joinId);
			reconcileLeafPhylogenyAt(leafJoinNode, rootJoinNode);
			}
		}


	/**
	 * reorganize the root-ward nodes of the leaf phylogeny so that it agrees with the root phylogeny, creating new nodes
	 * as needed.  Note this is kind of a hack in that the leaf phylogeny gets munged.
	 *
	 * @param leafJoinNode
	 * @param rootJoinNode
	 */
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
			T parentId = rootParent.getPayload();
			PhylogenyNode<T> leafParent;
			try
				{
				leafParent = leafPhylogeny.getNode(parentId);
				}
			catch (NoSuchNodeException e)
				{
				BasicPhylogenyNode<T> newLeafParent = new BasicPhylogenyNode<T>();
				newLeafParent.setPayload(parentId);
				//leafPhylogeny.getUniqueIdToNodeMap().put(parentId, newLeafParent);
				leafPhylogeny.putUniqueIdToNode(parentId, newLeafParent);

				leafParent = newLeafParent;
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
	}
