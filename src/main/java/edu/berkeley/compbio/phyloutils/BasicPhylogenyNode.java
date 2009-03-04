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

import com.davidsoergel.dsutils.tree.DepthFirstTreeIterator;
import com.davidsoergel.dsutils.tree.DepthFirstTreeIteratorImpl;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * A basic implementation of the PhylogenyNode interface, where the generic type indicates the type of node IDs that are
 * used (typically Strings or Integers).
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class BasicPhylogenyNode<T> implements PhylogenyNode<T>, Serializable//, HierarchyNode<T, BasicPhylogenyNode<T>>
		// really T should extend Serializable here but let's see if we can get away without it
	{
	private static final Logger logger = Logger.getLogger(BasicPhylogenyNode.class);
	// ------------------------------ FIELDS ------------------------------

	protected transient BasicPhylogenyNode<T> parent;

	protected Set<BasicPhylogenyNode<T>> children = new HashSet<BasicPhylogenyNode<T>>();
	protected T value = null;
	protected Double length = null;// distinguish null from zero
	protected Double weight = null;// distinguish null from zero
	protected double bootstrap;

	public void toNewick(StringBuffer sb, int minClusterSize, double minLabelProb)
		{
		// (children)name:length

		if (!children.isEmpty())
			{
			sb.append("(");
			Iterator<BasicPhylogenyNode<T>> i = children.iterator();
			while (i.hasNext())
				{
				sb.append(i.next());
				if (i.hasNext())
					{
					sb.append(",");
					}
				}
			sb.append(")");
			}

		sb.append(value.toString());

		if (bootstrap != 0)
			{
			sb.append(":").append(bootstrap);
			}
		}

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
		setParent(parent);
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

	public BasicPhylogenyNode(BasicPhylogenyNode<T> parent, T value)
		{
		this(parent);
		this.value = value;
		}

	/*
	 public BasicPhylogenyNode(BasicPhylogenyNode<T> parent, PhylogenyNode<T> child)
		 {
		 this(parent, child.getValue(), child.getLength());
		 }
 */
	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * {@inheritDoc}
	 */
	public Set<BasicPhylogenyNode<T>> getChildren()
		{
		return children;
		}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
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
		throw new NoSuchElementException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLeaf()
		{
		return children.size() == 0;
		}

	/*	public void appendToName(int i)
	   {
	   name = name + i;
	   }*/

	/**
	 * {@inheritDoc}
	 */
	public Double getLength()
		{
		return length;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setLength(Double length)
		{
		this.length = length;
		if (parent != null)
			{
			parent.invalidateAggregatedChildInfo();
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public void setWeight(Double weight)
		{
		this.weight = weight;
		if (parent != null)
			{
			parent.invalidateAggregatedChildInfo();
			}
		}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	public Double getWeight()
		{
		if (weight == null)
			{
			if (isLeaf())
				{
				return null;
				//throw new PhyloUtilsRuntimeException("Node has undefined weight: " + getValue());
				}
			propagateWeightFromBelow();
			}
		return weight;
		}

	/**
	 * {@inheritDoc}
	 */
	public Double getCurrentWeight()
		{
		return weight;
		}

	/**
	 * {@inheritDoc}
	 */
	public void incrementWeightBy(double v)
		{
		weight = weight == null ? v : weight + v;
		}

	/**
	 * {@inheritDoc}
	 */
	private void propagateWeightFromBelow()
		{
		if (!isLeaf())
			{
			weight = 0.;
			for (PhylogenyNode<T> child : children)
				{
				//child.propagateWeightFromBelow();
				Double w = child.getWeight();
				if (w == null)
					{
					// undefined
					weight = null;
					return;
					}
				incrementWeightBy(w);
				}
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public double distanceToRoot()
		{
		return (length == null ? 0 : length) + (parent == null ? 0 : parent.distanceToRoot());
		}

	/**
	 * {@inheritDoc}
	 */
	public T getValue()
		{
		return value;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(T value)
		{
		if (value != null && value.equals(new Integer(-1)))
			{
			logger.error("wtf");
			}
		this.value = value;
		}

	/**
	 * {@inheritDoc}
	 */
	public BasicPhylogenyNode<T> getParent()
		{
		return parent;
		}

	/**
	 * {@inheritDoc}
	 */
	public BasicPhylogenyNode<T> newChild()
		{
		BasicPhylogenyNode<T> child = new BasicPhylogenyNode<T>();
		child.setParent(this);
		//addChild(child);
		return child;
		}


	/**
	 * {@inheritDoc}
	 */
	public final void setParent(PhylogenyNode<T> parent)//BasicPhylogenyNode parent)
		{
		if (this.parent != null)
			{
			this.parent.unregisterChild(this);
			}
		this.parent = (BasicPhylogenyNode<T>) parent;// may produce ClassCastException
		if (this.parent != null)
			{
			this.parent.registerChild(this);
			}
		}

	public void setBootstrap(double bootstrap)
		{
		this.bootstrap = bootstrap;
		}

	// -------------------------- OTHER METHODS --------------------------

	protected void addSubtreeToMap(Map<T, BasicPhylogenyNode<T>> nodes, NodeNamer<T> namer) throws PhyloUtilsException
		{
		// if a node has no name, we assign one using the namer
		if (!hasValue()) //|| nodes.get(value) != null)
			{
			if (namer == null)
				{
				throw new PhyloUtilsException("Need to name a node, but no namer was provided");
				}
			value = namer.generate();//nameInternal(nodes.size());
			}

		else if (nodes.get(value) != null)
			{
			// more confusingly, if a node name is nonunique, then after the first time it's seen the name is thrown out and replaced by a name generated by the namer

			final T uniqueValue = namer.uniqueify(value);
			logger.warn("Name " + value + " not unique, substituting " + uniqueValue);
			value = uniqueValue;
// 	throw new PhyloUtilsException("Node names must be unique");
			}


		nodes.put(value, this);


		for (BasicPhylogenyNode<T> n : children)
			{
			n.addSubtreeToMap(nodes, namer);
			}
		}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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


	/**
	 * {@inheritDoc}
	 */
	public Iterator<PhylogenyNode<T>> iterator()
		{
		return new DepthFirstTreeIteratorImpl(this);
		}

	/**
	 * {@inheritDoc}
	 */
	public DepthFirstTreeIterator<T, PhylogenyNode<T>> depthFirstIterator()
		{
		return new DepthFirstTreeIteratorImpl(this);
		}


	Double greatestDepth = null;
	Double secondGreatestDepth = null;
	Double largestLengthSpan = null;


	/**
	 * {@inheritDoc}
	 */
	public Double getLargestLengthSpan()
		{
		computeDepthsIfNeeded();
		return largestLengthSpan;
		}

	/**
	 * {@inheritDoc}
	 */
	public Double getGreatestDepth()
		{
		computeDepthsIfNeeded();
		return greatestDepth;
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
				if (child.length != null)
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
			}

		// if there is exactly one child, then
		// greatestDepth = child.length + child.greatestDepth;
		// secondGreatestDepth = 0;
		// largestLengthSpan = child.length + child.greatestDepth;
		}

	public void removeChild(BasicPhylogenyNode<T> child)
		{
		children.remove(child);
		child.setParent(null);
		invalidateAggregatedChildInfo();
		}


	/**
	 * {@inheritDoc}
	 */
	public void registerChild(PhylogenyNode<T> child)
		{
		children.add((BasicPhylogenyNode<T>) child);
		//((BasicPhylogenyNode<T>) child).setParent(this);
		invalidateAggregatedChildInfo();
		}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterChild(PhylogenyNode<T> child)
		{
		children.remove((BasicPhylogenyNode<T>) child);
		//((BasicPhylogenyNode<T>) child).setParent(this);
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
		{
		return value == null ? "null" : value.toString();
		}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public BasicPhylogenyNode<T> clone()
		{
		BasicPhylogenyNode<T> result = null;// new BasicPhylogenyNode<T>();
		try
			{
			result = (BasicPhylogenyNode<T>) super.clone();


			parent = null;
			children = new HashSet<BasicPhylogenyNode<T>>();
			// note the value is shallow-copied, not cloned

			//result.setValue(value);//** value.clone() ??

			for (BasicPhylogenyNode<T> child : children)
				{
				child.clone().setParent(result);
				}

			// reset weight after children, since it got wiped
			result.setWeight(weight);

			// we don't set the parent here; addChild takes care of that, except for the root, where the parent is null anyway

			return result;
			}
		catch (CloneNotSupportedException e)
			{
			logger.error("Error", e);
			throw new Error("cloneability required");
			}
		}

	/**
	 * {@inheritDoc}
	 */
	public BasicPhylogenyNode<T> getSelfNode()
		{
		return this;
		}


	public void appendSubtree(StringBuffer sb, String indent)
		{


		sb.append(indent + "\n");
		sb.append(indent + "---" + weight + " " + length + "     " + value + "\n");
		indent += "   |";
		for (PhylogenyNode n : getChildren())
			{
			n.appendSubtree(sb, indent);
			}
		}


	/**
	 * {@inheritDoc}
	 */
	/* recursive solution
	 public PhylogenyNode<T> nearestAncestorWithBranchLength() throws PhyloUtilsException
		 {
		 if (getLength() != null)
			 {
			 return this;
			 }

		 PhylogenyNode<T> n = getParent();
		 if (n == null)
			 {
			 throw new Error("should be impossible?");
			 // arrived at root, too bad
			 throw new PhyloUtilsException("No ancestor of " + leafId + " has a branch length.");
			 }
		 return n.nearestAncestorWithBranchLength();
		 }
 */
	/**
	 * {@inheritDoc}
	 */
// iterative solution
	public PhylogenyNode<T> nearestAncestorWithBranchLength() throws PhyloUtilsException
		{
		PhylogenyNode<T> n = this;

		while (n.getLength() == null)
			{
			n = n.getParent();
			if (n == null)
				{
				// arrived at root, too bad
				throw new PhyloUtilsException("No ancestor of " + getValue() + " has a branch length.");
				}
			}

		return n; //.getValue();
		}
	}


