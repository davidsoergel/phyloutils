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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class PhylogenyNode
	{
	protected PhylogenyNode parent;
	protected Set<PhylogenyNode> children = new HashSet<PhylogenyNode>();
	protected String name = "";
	protected Double length = null;// distinguish null from zero
	protected double bootstrap;


	public PhylogenyNode(PhylogenyNode parent)
		{
		this.parent = parent;
		if (parent != null)
			{
			parent.children.add(this);
			}
		}

	public PhylogenyNode getParent()
		{
		return parent;
		}

	public void setParent(PhylogenyNode parent)
		{
		this.parent = parent;
		}

	public Set<PhylogenyNode> getChildren()
		{
		return children;
		}

	public String getName()
		{
		return name;
		}

	/*	public void setName(String name)
	   {
	   this.name = name;
	   }*/

	public void appendToName(String s)
		{
		name = name + s;
		}

	public void appendToName(int i)
		{
		name = name + i;
		}

	public Double getLength()
		{
		return length;
		}

	public void setLength(Double length)
		{
		this.length = length;
		}

	protected void addSubtreeToMap(Map<String, PhylogenyNode> nodes) throws PhyloUtilsException
		{
		if (!hasName())
			{
			name = "UNNAMED_" + nodes.size();
			}

		else if (nodes.get(name) != null)
			{
			throw new PhyloUtilsException("Node names must be unique");
			}

		nodes.put(name, this);


		for (PhylogenyNode n : children)
			{
			n.addSubtreeToMap(nodes);
			}
		}

	public List<PhylogenyNode> getAncestorPath()
		{
		List<PhylogenyNode> result = new LinkedList<PhylogenyNode>();
		PhylogenyNode trav = this;

		while (trav != null)
			{
			result.add(0, trav);
			trav = trav.getParent();
			}

		return result;
		}

	public boolean hasName()
		{
		return name != null && !name.equals("");
		}

	public void setBootstrap(double bootstrap)
		{
		this.bootstrap = bootstrap;
		}
	}
