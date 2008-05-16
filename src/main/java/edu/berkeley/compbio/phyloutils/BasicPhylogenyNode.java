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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @Author David Soergel
 * @Version 1.0
 */
public class BasicPhylogenyNode<T> implements PhylogenyNode<T>
	{
	private static final Logger logger = Logger.getLogger(BasicPhylogenyNode.class);
	// ------------------------------ FIELDS ------------------------------

	protected BasicPhylogenyNode<T> parent;
	protected Set<BasicPhylogenyNode<T>> children = new HashSet<BasicPhylogenyNode<T>>();
	protected T value = null;
	protected Double length = null;// distinguish null from zero
	protected Double weight = null;// distinguish null from zero
	protected double bootstrap;


	// --------------------------- CONSTRUCTORS ---------------------------

	public BasicPhylogenyNode()
		{
		}

	public BasicPhylogenyNode(T value)
		{
		this();
		setValue(value);
		}

	public BasicPhylogenyNode(BasicPhylogenyNode<T> parent)
		{
		this();
		if (parent != null)
			{
			parent.addChild(this);// automatically sets this.parent as well
			}
		}

	public BasicPhylogenyNode(BasicPhylogenyNode<T> parent, double length)
		{
		this(parent);
		this.length = length;
		}

	public BasicPhylogenyNode(BasicPhylogenyNode<T> parent, T value, double length)
		{
		this(parent);
		this.value = value;
		this.length = length;
		}

	public BasicPhylogenyNode(BasicPhylogenyNode<T> parent, PhylogenyNode<T> child)
		{
		this(parent, child.getValue(), child.getLength());
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	public Collection<BasicPhylogenyNode<T>> getChildren()
		{
		return children;
		}

	public PhylogenyNode<T> getChild(T id)
		{
		// We could map the children collection as a Map; but that's some hassle, and since there are generally just 2 children anyway, this is simpler

		// also, the child id is often not known when it is added to the children Set, so putting the child into a children Map wouldn't work

		for (PhylogenyNode<T> child : children)
			{
			if (child.getValue() == id)
				{
				return child;
				}
			}
		return null;
		}

	public boolean isLeaf()
		{
		return children.size() == 0;
		}

	/*	public void appendToName(int i)
	   {
	   name = name + i;
	   }*/

	public Double getLength()
		{
		return length;
		}

	public void setLength(Double length)
		{
		this.length = length;
		if (parent != null)
			{
			parent.invalidateAggregatedChildInfo();
			}
		}

	public void setWeight(Double weight)
		{
		this.weight = weight;
		if (parent != null)
			{
			parent.invalidateAggregatedChildInfo();
			}
		}

	public double getWeight()
		{
		return weight;
		}

	public void setWeight(double v)
		{
		weight = v;
		}

	public void incrementWeightBy(double v)
		{
		weight = weight == null ? v : weight + v;
		}

	public void propagateWeightFromBelow()
		{
		if (!isLeaf())
			{
			weight = 0.;
			for (BasicPhylogenyNode<T> child : children)
				{
				child.propagateWeightFromBelow();
				weight += child.getWeight();
				}
			}
		}

	public double distanceToRoot()
		{
		return length + (parent == null ? 0 : parent.distanceToRoot());
		}

	public T getValue()
		{
		return value;
		}

	public void setValue(T value)
		{
		this.value = value;
		}

	public BasicPhylogenyNode<T> getParent()
		{
		return parent;
		}

	public HierarchyNode<? extends T> newChild()
		{
		BasicPhylogenyNode<T> child = new BasicPhylogenyNode<T>();
		addChild(child);
		return child;
		}


	public void setParent(HierarchyNode<? extends T> parent)//BasicPhylogenyNode parent)
		{

		this.parent = (BasicPhylogenyNode<T>) parent;// may produce ClassCastException
		if (parent != null)
			{
			this.parent.invalidateAggregatedChildInfo();
			}
		}

	public void setBootstrap(double bootstrap)
		{
		this.bootstrap = bootstrap;
		}

	// -------------------------- OTHER METHODS --------------------------

	protected void addSubtreeToMap(Map<T, PhylogenyNode<T>> nodes, NodeNamer<T> namer) throws PhyloUtilsException
		{
		if (!hasValue())
			{
			if (namer == null)
				{
				throw new PhyloUtilsException("Need to name a node, but no namer was provided");
				}
			value = namer.nameInternal(nodes.size());
			}

		else if (nodes.get(value) != null)
			{
			throw new PhyloUtilsException("Node names must be unique");
			}

		nodes.put(value, this);


		for (BasicPhylogenyNode<T> n : children)
			{
			n.addSubtreeToMap(nodes, namer);
			}
		}

	public boolean hasValue()
		{
		return value != null;// && !name.equals("");
		}

	/*	public void setName(String name)
	   {
	   this.name = name;
	   }*/

	public void appendToValue(String s, NodeNamer<T> namer) throws PhyloUtilsException
		{
		if (value == null)
			{
			value = namer.create(s);
			}
		else
			{
			value = namer.merge(value, s);
			}
		}

	public void appendToValue(Integer s, NodeNamer<T> namer) throws PhyloUtilsException
		{
		if (value == null)
			{
			value = namer.create(s);
			}
		else
			{
			value = namer.merge(value, s);
			}
		}

	public List<PhylogenyNode<T>> getAncestorPath()
		{
		List<PhylogenyNode<T>> result = new LinkedList<PhylogenyNode<T>>();
		BasicPhylogenyNode<T> trav = this;

		while (trav != null)
			{
			result.add(0, trav);
			trav = trav.getParent();
			}

		return result;
		}


	public PhylogenyIterator<T> iterator()
		{
		return new DepthFirstIterator();
		}


	Double greatestDepth = null;
	Double secondGreatestDepth = null;
	Double largestLengthSpan = null;

	public Double getLargestLengthSpan()
		{
		computeDepthsIfNeeded();
		return largestLengthSpan;
		}

	private void computeDepthsIfNeeded()
		{
		if (greatestDepth == null)
			{
			greatestDepth = 0.;
			secondGreatestDepth = 0.;
			largestLengthSpan = 0.;

			// if there are no children, then both depths and the span are just 0

			// note we want the greatest and second-greatest depths to be on different branches from this node!
			// if they were on the same branch, then we'd double-count the common portion in computing the span.


			for (BasicPhylogenyNode<T> child : children)
				{
				child.computeDepthsIfNeeded();

				// case 1: the child replaces the greatest depth

				if (child.length + child.greatestDepth > greatestDepth)
					{
					secondGreatestDepth = greatestDepth;// must be from a different child, or 0
					greatestDepth = child.length + child.greatestDepth;
					}

				// case 2: the child replaces the second-greatest depth

				else if (child.length + child.greatestDepth > secondGreatestDepth)
					{
					secondGreatestDepth = child.length + child.greatestDepth;
					}

				// the child's second-greatest depth should never figure in to the second-greatest depth at this level,
				// because of the different-branches constraint!
				// however, it may contribute to the maximum span.

				// assume by default that the maximum span spans branches
				// need to take the max in case it's already been overridden by the spanViaChild on a previous branch
				largestLengthSpan = Math.max(largestLengthSpan, greatestDepth + secondGreatestDepth);

				// then check if this child overrides it, counting the common portion only once

				double spanViaChild = child.length + child.largestLengthSpan;

				largestLengthSpan = Math.max(largestLengthSpan, spanViaChild);
				}
			}

		// if there is exactly one child, then
		// greatestDepth = child.length + child.greatestDepth;
		// secondGreatestDepth = 0;
		// largestLengthSpan = child.length + child.greatestDepth;
		}

	public void removeChild(LengthWeightHierarchyNode<T> child)
		{
		children.remove(child);
		child.setParent(null);
		invalidateAggregatedChildInfo();
		}


	public void addChild(LengthWeightHierarchyNode<T> child)
		{
		children.add((BasicPhylogenyNode<T>) child);
		child.setParent(this);
		invalidateAggregatedChildInfo();
		}

	private void invalidateAggregatedChildInfo()
		{
		greatestDepth = null;
		secondGreatestDepth = null;
		largestLengthSpan = null;
		weight = null;

		if (parent != null)
			{
			parent.invalidateAggregatedChildInfo();
			}
		}


	/**
	 * Returns the nodes in the tree in depth-first order.  The branches from a given node have no ordering, though, so the
	 * ordering is not guaranteed; the depth-first guarantee is only that, once a node is provided, all of its descendants
	 * will be provided before any of its siblings.
	 */
	private class DepthFirstIterator implements PhylogenyIterator<T>
		{

		Iterator<BasicPhylogenyNode<T>> breadthIterator = null;
		PhylogenyIterator<T> subtreeIterator = null;

		/**
		 * Returns <tt>true</tt> if the iteration has more elements. (In other words, returns <tt>true</tt> if <tt>next</tt>
		 * would return an element rather than throwing an exception.)
		 *
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext()
			{
			return
					// we haven't yet returned this node
					breadthIterator == null

							// there is a child that we haven't yet returned
							|| (subtreeIterator != null && subtreeIterator.hasNext())

							// the current child has pending nodes
							|| breadthIterator.hasNext();
			}

		/**
		 * Returns the next element in the iteration.  Calling this method repeatedly until the {@link #hasNext()} method
		 * returns false will return each element in the underlying collection exactly once.
		 *
		 * @return the next element in the iteration.
		 * @throws java.util.NoSuchElementException
		 *          iteration has no more elements.
		 */
		public PhylogenyNode<T> next()
			{
			// this whole class, and especially this method, are pretty confusing.
			// expanded the logic a bit for clarity

			if (breadthIterator == null)
				{
				// we haven't yet returned this node

				// prep the iterators for the next call
				breadthIterator = children.iterator();

				if (breadthIterator.hasNext())
					{
					subtreeIterator = breadthIterator.next().iterator();
					}
				// else this node has no children

				return BasicPhylogenyNode.this;
				}
			else if (subtreeIterator == null)
				{
				// this node has no children, and we've already returned the node itself.
				return null;
				}
			else
				{
				// the currently selected subtree has more nodes
				if (subtreeIterator.hasNext())
					{
					return subtreeIterator.next();
					}
				else
					// there is a currently selected subtree, but it's exhausted.  Try the next one.
					{
					if (breadthIterator.hasNext())
						{
						// note the next subtreeIterator is guaranteed to have at least one node: the immediate child itself,
						// even if it has no descendants
						subtreeIterator = breadthIterator.next().iterator();
						return subtreeIterator.next();
						}
					else
						{
						// the currently selected subtree is exhausted, and there aren't any more.
						return null;
						}
					}
				}
			}

		// the requested node must be on the current path
		public void skipAllDescendants(PhylogenyNode<T> node) throws PhyloUtilsException
			{
			if (node == BasicPhylogenyNode.this)
				{
				// we want to produce the situation that causes next() to fire the breadthIterator.
				// if there is no sibling, then hasNext() will detect the same situation so the parent iterator will fire its breadthIterator, etc.

				// The conditions are:
				// the breadthIterator must exist, indicating that this node itself has been consumed.
				if (breadthIterator == null)
					{
					throw new PhyloUtilsException("Can't skip descendants of a node that hasn't been returned yet");
					}

				// the subtreeIterator must equal null, making it look like the node has no children
				subtreeIterator = null;

				// the breadthIterator must be exhausted, making it look like all the subtrees have been processed

				while (breadthIterator.hasNext())// annoying way to consume the children
					{
					breadthIterator.next();
					}
				}
			else
				{
				if (subtreeIterator != null)
					{
					subtreeIterator.skipAllDescendants(node);
					}
				else
					{
					// we haven't found the requested node on the path so far, and there is no further selected subtree from here.
					throw new PhyloUtilsException("Can't skip descendants of a node that is not on the current path");
					}
				}
			}


		/**
		 * Removes from the underlying collection the last element returned by the iterator (optional operation).  This method
		 * can be called only once per call to <tt>next</tt>.  The behavior of an iterator is unspecified if the underlying
		 * collection is modified while the iteration is in progress in any way other than by calling this method.
		 *
		 * @throws UnsupportedOperationException if the <tt>remove</tt> operation is not supported by this Iterator.
		 * @throws IllegalStateException         if the <tt>next</tt> method has not yet been called, or the <tt>remove</tt>
		 *                                       method has already been called after the last call to the <tt>next</tt>
		 *                                       method.
		 */
		public void remove()
			{
			throw new UnsupportedOperationException();
			}
		}

	public String toString()
		{
		return value == null ? "null" : value.toString();
		}

	public BasicPhylogenyNode<T> clone()
		{
		BasicPhylogenyNode<T> result = new BasicPhylogenyNode<T>();

		result.setLength(length);
		result.setValue(value);//** value.clone() ??
		result.setBootstrap(bootstrap);

		for (BasicPhylogenyNode<T> child : children)
			{
			result.addChild((BasicPhylogenyNode<T>) child.clone());
			}

		// set weight after children, since it would get wiped
		result.setWeight(weight);

		// we don't set the parent here; addChild takes care of that, except for the root, where the parent is null anyway

		return result;
		}
	}
