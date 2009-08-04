package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.NoSuchNodeException;

import java.io.Serializable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SerializableRootedPhylogeny<T extends Serializable> extends RootedPhylogeny<T>, Serializable
	{

	SerializablePhylogenyNode<T> getNode(T name) throws NoSuchNodeException;

	SerializablePhylogenyNode<T> getFirstBranchingNode();

	//Map<T, SerializablePhylogenyNode<T>> getUniqueIdToNodeMap();  //** Map<T, ? extends PhylogenyNode<T>> ??
	}
