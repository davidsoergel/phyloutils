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

import com.davidsoergel.dsutils.tree.HierarchyNode;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Really this wants to extend BasicPhylogenyNode, but we can't (multiple inheritance, etc.), so we just facade a root
 * node for many methods.
 *
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

	public BasicRootedPhylogeny()
		{
		root = new BasicPhylogenyNode<T>();
		}

	public BasicRootedPhylogeny(T rootValue)
		{
		root = new BasicPhylogenyNode<T>(null, rootValue, 0);
		}

	public PhylogenyNode<T> getNode(T name)
		{
		return nodes.get(name);
		}

	public Collection<PhylogenyNode<T>> getNodes()
		{
		return nodes.values();
		}

	public Collection<PhylogenyNode<T>> getLeaves()
		{
		Set<PhylogenyNode<T>> result = new HashSet<PhylogenyNode<T>>();
		for (T t : nodes.keySet())
			{
			PhylogenyNode<T> node = nodes.get(t);
			if (node.isLeaf())
				{
				result.add(node);
				}
			}
		return result;
		}

	public Collection<T> getLeafValues()
		{
		Set<T> result = new HashSet<T>();
		for (T t : nodes.keySet())
			{
			if (nodes.get(t).isLeaf())
				{
				result.add(t);
				}
			}
		return result;
		}

	// we can't do this while building, since the names might change
	public void updateNodes(NodeNamer<T> namer) throws PhyloUtilsException
		{
		nodes = new HashMap<T, PhylogenyNode<T>>();
		root.addSubtreeToMap(nodes, namer);
		}


	public Collection<? extends PhylogenyNode<T>> getChildren()
		{
		return root.getChildren();
		}

	public PhylogenyNode<T> getChild(T id)
		{
		return root.getChild(id);
		}

	public T getValue()
		{
		return root.getValue();
		}

	public PhylogenyNode getParent()
		{
		return null;
		}

	public HierarchyNode<? extends T> newChild()
		{
		return root.newChild();
		}

	public void setValue(T contents)
		{
		root.setValue(contents);
		}

	public void setParent(HierarchyNode<? extends T> parent)
		{
		logger.error("Can't set the parent of the root node");
		}

	public boolean hasValue()
		{
		return root.hasValue();
		}

	public List<PhylogenyNode<T>> getAncestorPath()
		{
		// this is the root node
		List<PhylogenyNode<T>> result = new LinkedList<PhylogenyNode<T>>();

		result.add(0, root);

		return result;
		}

	public Double getLength()
		{
		return 0.;
		}

	public Double getLargestLengthSpan()
		{
		return root.getLargestLengthSpan();
		}


	/**
	 * Returns an iterator over a set of elements of type T.
	 *
	 * @return an Iterator.
	 */
	public Iterator<PhylogenyNode<T>> iterator()
		{
		return root.iterator();
		}

	public PhylogenyIterator<T> phylogenyIterator()
		{
		return root.iterator();
		}

	public T nearestKnownAncestor(RootedPhylogeny<T> rootPhylogeny, T leafId) throws PhyloUtilsException
		{
		T result = null;//nearestKnownAncestorCache.get(leafId);
		if (result == null)
			{
			PhylogenyNode<T> n = getNode(leafId);

			if (n == null)
				{
				throw new PhyloUtilsException("Leaf phylogeny does not contain node " + leafId + ".");
				}

			while (rootPhylogeny.getNode(n.getValue()) == null)
				{
				n = n.getParent();
				if (n.getParent() == null)
					{
					// arrived at root, too bad
					throw new PhyloUtilsException("Taxon " + leafId + " not found in tree.");
					}
				//ncbiDb.getEntityManager().refresh(n);
				}
			result = n.getValue();
			//	nearestKnownAncestorCache.put(leafId, result);
			}
		//return n.getId();
		return result;
		}

	public T nearestAncestorWithBranchLength(T leafId) throws PhyloUtilsException
		{
		PhylogenyNode<T> n = getNode(leafId);

		if (n == null)
			{
			throw new PhyloUtilsException("Leaf phylogeny does not contain node " + leafId + ".");
			}

		while (n.getLength() == null)
			{
			n = n.getParent();
			if (n.getParent() == null)
				{
				// arrived at root, too bad
				throw new PhyloUtilsException("No ancestor of " + leafId + " has a branch length.");
				}
			}

		return n.getValue();
		}

	public BasicPhylogenyNode<T> getRoot()
		{
		return root;
		}

	public boolean isLeaf()
		{
		return root.isLeaf();
		}

	public double getWeight()
		{
		return 1;
		}

	public void setWeight(double v)
		{
		if (v != 1.)
			{
			throw new Error("Can't set root weight to anything other than 1");
			}
		}


	public void incrementWeightBy(double v)
		{
		throw new Error("Can't increment root weight");
		}


	public void propagateWeightFromBelow()
		{
		root.propagateWeightFromBelow();
		}

	public double distanceToRoot()
		{
		return 0;
		}

	public void setRoot(BasicPhylogenyNode<T> root)
		{
		this.root = root;
		}

	public RootedPhylogeny<T> clone()
		{
		try
			{
			BasicRootedPhylogeny<T> result = new BasicRootedPhylogeny<T>();
			result.setRoot(root.clone());
			result.setBasePhylogeny(getBasePhylogeny());
			result.updateNodes(null);
			return result;
			}
		catch (PhyloUtilsException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}
		}
	}



