package edu.berkeley.compbio.phyloutils.old;

import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * A binary tree.  Nodes are numbered with integers.  The root is node 0, but aside from that all bets are off as to the
 * numbering.
 *
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class BinaryTree
	{
	private static final Logger logger = Logger.getLogger(BinaryTree.class);

	private double[] branchlength;
	private int[] parent;

	// we store the tree in both directions.  Space-inefficient, maybe, but much easier to deal with.

	private int[] child1;
	private int[] child2;


	public static int NONE = -1;
	private int[] breadthFirst = null;

	public BinaryTree(int nodes)
		{
		this.parent = new int[nodes];
		this.branchlength = new double[nodes];
		this.child1 = new int[nodes];
		this.child2 = new int[nodes];

		// start with the whole tree undefined
		for (int i = 0; i < parent.length; i++)
			{
			parent[i] = NONE;
			child1[i] = NONE;
			child2[i] = NONE;
			}
		}


	public int getParent(int n)
		{
		return parent[n];
		}

	public void setBranchLength(int i, double br)
		{
		branchlength[i] = br;
		}


	public void setParent(int c, int p) throws TreeException
		{
		parent[c] = p;
		if (p == -1)
			{
			return;
			}
		if (child1[p] == NONE)
			{
			child1[p] = c;
			}
		else if (child2[p] == NONE)
			{
			// ordering the children may not be strictly necessary but it's nice to have a canonical result
			if (c >= child1[p])
				{
				child2[p] = c;
				}
			else
				{
				child2[p] = child1[p];
				child1[p] = c;
				}
			}
		else
			{
			throw new TreeException("Nodes in a binary tree can't have more than two children");
			}
		}

	public int getNumNodes()
		{
		return parent.length;
		}

	/**
	 * Order the nodes to guarantee that parents come before children.  This is not necessarily a breadth-first traversal.
	 */
	public int[] breadthFirst() throws TreeException

		{
		if (breadthFirst != null)
			{
			return breadthFirst;
			}
		breadthFirst = new int[parent.length];
		breadthFirst[0] = 0;// root is always 0;
		int pos = 1;// the next position to fill
		logger.debug("parents: " + Arrays.toString(parent));
		logger.debug("child1: " + Arrays.toString(child1));
		logger.debug("child2: " + Arrays.toString(child2));

		for (int i = 0; pos < parent.length; i++)
			{
			logger.debug("[" + i + "] breadthFirst: " + Arrays.toString(breadthFirst));
			int c1 = child1[breadthFirst[i]];
			int c2 = child2[breadthFirst[i]];
			if (c1 != NONE)
				{
				breadthFirst[pos] = c1;
				pos++;
				if (c2 == NONE)
					{

					throw new TreeException("child1 has a value but child2 does not");
					}
				breadthFirst[pos] = c2;
				pos++;
				}
			}
		logger.debug("parents: " + Arrays.toString(parent));
		logger.debug("child1: " + Arrays.toString(child1));
		logger.debug("child2: " + Arrays.toString(child2));
		logger.debug("breadthFirst: " + Arrays.toString(breadthFirst));

		return breadthFirst;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()

		{
		StringBuffer sb = new StringBuffer("\n");

		appendSubtree(sb, 0, "");
		return sb.toString();
		}

	private void appendSubtree(StringBuffer sb, int node, String indent)
		{
		if (node == -1)
			{
			return;
			}

		sb.append(indent + "\n");
		sb.append(indent + "---" + node + "     " + nodeInfo(node) + "\n");
		indent += "   |";
		appendSubtree(sb, child1[node], indent);
		appendSubtree(sb, child2[node], indent);
		}

	protected String nodeInfo(int node)
		{
		return "" + branchlength[node];
		}

	public boolean isLeaf(int n)

		{
		return (child1[n] == NONE) && (child2[n] == NONE);
		}
	}