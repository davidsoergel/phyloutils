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
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
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
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public class BasicRootedPhylogeny<T> extends AbstractRootedPhylogeny<T> implements Serializable
	{
	private static final Logger logger = Logger.getLogger(BasicRootedPhylogeny.class);
	// ------------------------------ FIELDS ------------------------------

	transient private Map<T, BasicPhylogenyNode<T>> nodes;
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

	/**
	 * Make a complete copy of the provided phylogeny using BasicPhylogenyNodes.  It's probably better to use clone() when
	 * possible, but this copy constructor allows translating from phylogenies with different node types.
	 *
	 * @param original
	 */
	/*	public BasicRootedPhylogeny(HybridRootedPhylogeny<T> original)
	   {
root = new BasicPhylogenyNode<T>(original.);
	   }*/

	/**
	 * {@inheritDoc}
	 */
	public PhylogenyNode<T> getNode(T name)
		{
		return nodes.get(name);
		}

	/**
	 * {@inheritDoc}
	 */
	public Collection<BasicPhylogenyNode<T>> getNodes()
		{
		return nodes.values();
		}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public Collection<T> getNodeValues()
		{
		return nodes.keySet();
		}

	// we can't do this while building, since the names might change
	public void updateNodes(NodeNamer<T> namer) throws PhyloUtilsException
		{
		nodes = new HashMap<T, BasicPhylogenyNode<T>>();
		root.addSubtreeToMap(nodes, namer);
		}


	/**
	 * {@inheritDoc}
	 */
	public Set<BasicPhylogenyNode<T>> getChildren()
		{
		return root.getChildren();
		}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public PhylogenyNode<T> getChild(T id)
		{
		return root.getChild(id);
		}

	/**
	 * {@inheritDoc}
	 */
	public T getValue()
		{
		return root.getValue();
		}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	public PhylogenyNode getParent()
		{
		return null;
		}

	/**
	 * {@inheritDoc}
	 */
	public PhylogenyNode<T> newChild()
		{
		return root.newChild();
		}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(T contents)
		{
		root.setValue(contents);
		}

	/**
	 * {@inheritDoc}
	 */
	public void setParent(PhylogenyNode<T> parent)
		{
		logger.error("Can't set the parent of the root node");
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasValue()
		{
		return root.hasValue();
		}

	/**
	 * {@inheritDoc}
	 */
	public List<PhylogenyNode<T>> getAncestorPath()
		{
		// this is the root node
		List<PhylogenyNode<T>> result = new LinkedList<PhylogenyNode<T>>();

		result.add(0, root);

		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	public Double getLength()
		{
		return 0.;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setLength(Double d)
		{
		logger.error("Can't set length of the root node");
		}

	/**
	 * {@inheritDoc}
	 */
	public Double getLargestLengthSpan()
		{
		return root.getLargestLengthSpan();
		}

	/**
	 * {@inheritDoc}
	 */
	public void registerChild(PhylogenyNode<T> a)
		{
		root.registerChild(a);
		}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterChild(PhylogenyNode<T> a)
		{
		root.unregisterChild(a);
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

	/**
	 * {@inheritDoc}
	 */
	public DepthFirstTreeIterator<T, PhylogenyNode<T>> depthFirstIterator()
		{
		return root.depthFirstIterator();
		}

	/**
	 * {@inheritDoc}
	 */
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
				if (n == null)
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

	/**
	 * {@inheritDoc}
	 */
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
			if (n == null)
				{
				// arrived at root, too bad
				throw new PhyloUtilsException("No ancestor of " + leafId + " has a branch length.");
				}
			}

		return n.getValue();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BasicPhylogenyNode<T> getRoot()
		{
		return root;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLeaf()
		{
		return root.isLeaf();
		}

	/**
	 * {@inheritDoc}
	 */
	public Double getWeight()
		{
		return 1.;
		}

	/**
	 * {@inheritDoc}
	 */
	public Double getCurrentWeight()
		{
		return 1.;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setWeight(Double v)
		{
		if (v != 1.)
			{
			throw new Error("Can't set root weight to anything other than 1");
			}
		}

	/*	public void setWeight(double v)
		 {
		 if (v != 1.)
			 {
			 throw new Error("Can't set root weight to anything other than 1");
			 }
		 }
 */

	/**
	 * {@inheritDoc}
	 */
	public void incrementWeightBy(double v)
		{
		throw new Error("Can't increment root weight");
		}


	/**
	 * {@inheritDoc}
	 */
	/*	public void propagateWeightFromBelow()
	   {
	   root.propagateWeightFromBelow();
	   }*/

	/**
	 * {@inheritDoc}
	 */
	public double distanceToRoot()
		{
		return 0;
		}

	public void setRoot(BasicPhylogenyNode<T> root)
		{
		this.root = root;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	/**
	 * Test whether the given object is the same as this one.  Differs from equals() in that implementations of this
	 * interface may contain additional state which make them not strictly equal; here we're only interested in whether
	 * they're equal as far as this interface is concerned, i.e., for purposes of clustering.
	 *
	 * @param other The clusterable object to compare against
	 * @return True if they are equivalent, false otherwise
	 */
	public boolean equalValue(RootedPhylogeny<T> other)
		{
		throw new NotImplementedException();
		}

	/**
	 * Returns a String identifying this object.  Ideally each clusterable object being analyzed should have a unique
	 * identifier.
	 *
	 * @return a unique identifier for this object
	 */
	public String getId()
		{
		return root.getValue().toString();
		}

	/**
	 * Get the primary classification label, if available (optional operation)
	 *
	 * @return a Strings describing this object
	 */
	/*	public String getLabel()
	   {
	   return null;
	   }*/
	public BasicPhylogenyNode<T> getSelfNode()
		{
		return root;
		}

	public void appendSubtree(StringBuffer sb, String indent)
		{
		root.appendSubtree(sb, indent);
		}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
		{
		root = (BasicPhylogenyNode<T>) stream.readObject();

		nodes = new HashMap<T, BasicPhylogenyNode<T>>();

		try
			{
			// populate the nodes map
			updateNodes(null);  // all the nodes should have names already, don't need a namer
			}
		catch (PhyloUtilsException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new NotSerializableException("PhyloUtilsException: " + e);
			}


		for (BasicPhylogenyNode<T> p : nodes.values())
			{
			for (BasicPhylogenyNode<T> c : p.getChildren())
				{
				c.parent = p;
				}
			}
		}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
		{
		stream.writeObject(root);
		}

	private void readObjectNoData() throws ObjectStreamException
		{
		throw new NotSerializableException();
		}
	}



