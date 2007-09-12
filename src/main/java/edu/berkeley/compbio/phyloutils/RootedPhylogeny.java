/*
 * Copyright (c) 2007 Regents of the University of California
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class RootedPhylogeny extends PhylogenyNode
	{
	private Map<String, PhylogenyNode> nodes;

	public RootedPhylogeny()
		{
		super(null);
		}

	// we can't do this while building, since the names might change
	public void updateNodes() throws PhyloUtilsException
		{
		nodes = new HashMap<String, PhylogenyNode>();
		addSubtreeToMap(nodes);
		}


	public PhylogenyNode getNode(String name)
		{
		return nodes.get(name);
		}

	public double distanceBetween(String nameA, String nameB)
		{
		PhylogenyNode a = getNode(nameA);
		PhylogenyNode b = getNode(nameB);

		List<PhylogenyNode> ancestorsA = a.getAncestorPath();
		List<PhylogenyNode> ancestorsB = b.getAncestorPath();

		while (ancestorsA.size() > 0 && ancestorsB.size() > 0 && ancestorsA.get(0) == ancestorsB.get(0))
			{
			ancestorsA.remove(0);
			ancestorsB.remove(0);
			}

		double dist = 0;
		for (PhylogenyNode n : ancestorsA)
			{
			dist += n.getLength();
			}
		for (PhylogenyNode n : ancestorsB)
			{
			dist += n.getLength();
			}

		return dist;
		}

	public Collection<PhylogenyNode> getNodes()
		{
		return nodes.values();
		}
	}
