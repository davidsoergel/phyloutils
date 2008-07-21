package edu.berkeley.compbio.phyloutils.old;


import com.davidsoergel.dsutils.math.MersenneTwisterFast;
import com.davidsoergel.stats.ContinuousDistribution1D;
import com.davidsoergel.stats.DistributionException;
import edu.berkeley.compbio.phyloutils.TransitionMatrix;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lorax
 * @version 1.0
 */
public class TreeFactory
	{
	private static Logger logger = Logger.getLogger(TreeFactory.class);

	private static MersenneTwisterFast mtf = new MersenneTwisterFast();

	public static BinaryTree randomTree(int nodes, ContinuousDistribution1D branchLengthDistribution)
			throws TreeException
		{
		BinaryTree t = new BinaryTree(nodes);

		randomizeTopology(t, branchLengthDistribution);

		return t;
		}

	public static AnnotatedBinaryTree randomAnnotatedTree(int nodes, ContinuousDistribution1D branchLengthDistribution,
	                                                      int characters)
		{
		AnnotatedBinaryTree t = new AnnotatedBinaryTree(nodes, characters);
		try
			{
			randomizeTopology(t, branchLengthDistribution);
			}
		catch (TreeException e)
			{
			logger.debug(e);
			}
		return t;
		}

	private static void randomizeTopology(BinaryTree t, ContinuousDistribution1D branchLengthDistribution)
			throws TreeException
		{

		List<Integer> remainingNodes = new LinkedList<Integer>();
		List<Integer> possibleParents = new LinkedList<Integer>();

		for (int i = 0; i < t.getNumNodes(); i++)
			{
			remainingNodes.add(i);
			}
		Collections.shuffle(remainingNodes);

		// the root node must be part of the tree
		possibleParents.add(0);
		remainingNodes.remove(remainingNodes.indexOf(0));
		remainingNodes.add(0, 0);

		//Iterator<Integer> remainingParentsIterator = remainingNodes.iterator();
		try
			{
			int parent;
			double newBranchLength;
			// the root is always node 0, so we skip it here.  Every other node must be a child of somebody.
			for (int i = 1; i < t.getNumNodes(); i++)
				{
				int parentIndex = mtf.nextInt(possibleParents.size());
				parent = possibleParents.get(parentIndex);
				possibleParents.remove(parentIndex);
				t.setParent(i, parent);
				do
					{
					newBranchLength = branchLengthDistribution.sample();
					}
				while (newBranchLength < 0);
				t.setBranchLength(i, newBranchLength);
				possibleParents.add(i);

				// if a parent has one child, then it must also have a second.
				i++;
				t.setParent(i, parent);
				do
					{
					newBranchLength = branchLengthDistribution.sample();
					}
				while (newBranchLength < 0);
				t.setBranchLength(i, newBranchLength);
				possibleParents.add(i);
				}
			// ** assert the root has two children
			// ** assert every node has either zero or two children
			// ** assert the tree is fully connected
			// ** assert there are no loops
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			}
		}


	public static AnnotatedBinaryTree randomEvolvedAnnotatedTree(int nodes,
	                                                             ContinuousDistribution1D branchLengthDistribution,
	                                                             int characters, int characterValues,
	                                                             TransitionMatrix tm)
			throws TreeException, DistributionException

		{
		AnnotatedBinaryTree t = randomAnnotatedTree(nodes, branchLengthDistribution, characters);

		for (int i = 0; i < characters; i++)
			{
			CharacterDistributionOnTree cdt = new CharacterDistributionOnTree(t, characterValues);
			t.setCharacter(i, cdt);
			}

		for (CharacterDistributionOnTree cdt : t.getCharacters())
			{
			cdt.setRootValue(mtf.nextInt(characterValues));
			cdt.evolveAlongTree(tm);
			}
		return t;
		}
	}
