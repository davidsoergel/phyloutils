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
public class BasicRootedPhylogeny<T> extends AbstractRootedPhylogeny<T>
	{
	private static final Logger logger = Logger.getLogger(BasicRootedPhylogeny.class);
	// ------------------------------ FIELDS ------------------------------

	private Map<T, PhylogenyNode<T>> nodes;
	BasicPhylogenyNode<T> root;

	// -------------------------- OTHER METHODS --------------------------


	public PhylogenyNode<T> getNode(T name)
		{
		return nodes.get(name);
		}

	public Collection<PhylogenyNode<T>> getNodes()
		{
		return nodes.values();
		}

	// we can't do this while building, since the names might change
	public void updateNodes(NodeNamer<T> namer) throws PhyloUtilsException
		{
		nodes = new HashMap<T, PhylogenyNode<T>>();
		root.addSubtreeToMap(nodes, namer);
		}


	public RootedPhylogeny<T> extractTreeWithLeaves(Set<T> ids)
		{
		Set<List<BasicPhylogenyNode<T>>> theAncestorLists = new HashSet<List<BasicPhylogenyNode<T>>>();
		for (T id : ids)
			{
			theAncestorLists.add(getNode(id).getAncestorPath());
			}

		PhylogenyNode<T> commonAncestor = null;
		try
			{
			commonAncestor = root.extractTreeWithPaths(theAncestorLists);
			}
		catch (PhyloUtilsException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}

		BasicRootedPhylogeny<T> newRoot = new BasicRootedPhylogeny<T>();
		newRoot.setLength(new Double(0));
		newRoot.setValue(commonAncestor.getValue());

		for (PhylogenyNode<T> child : commonAncestor.getChildren())
			{
			newRoot.getChildren().add(new BasicPhylogenyNode(child));// may produce ClassCastException
			child.setParent(newRoot);
			}

		return newRoot;
		}
	}



