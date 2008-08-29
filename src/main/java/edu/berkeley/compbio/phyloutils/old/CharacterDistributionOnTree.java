package edu.berkeley.compbio.phyloutils.old;

import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.stats.DistributionException;
import edu.berkeley.compbio.phyloutils.TransitionMatrix;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * The distribution of character values per node for a single character on a Tree. If the value is fixed at a node,
 * that's just a distribution with all the weight in one bin.
 *
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class CharacterDistributionOnTree
	{
	private static final Logger logger = Logger.getLogger(CharacterDistributionOnTree.class);

	private static int UNKNOWN = -1;

	private double[][] valueProbability;

	// MAYBE Map chars, strings etc. to integer values
	private int values;
	private AnnotatedBinaryTree theTree;

	public CharacterDistributionOnTree(AnnotatedBinaryTree t, int values)
		{
		this.theTree = t;
		this.values = values;
		valueProbability = new double[t.getNumNodes()][values];
		for (double[] da : valueProbability)
			{
			Arrays.fill(da, UNKNOWN);
			}
		}

	public void setRootValue(int v)
		{
		setValue(0, v);
		}

	public void setValue(int n, int v)
		{
		for (int i = 0; i < values; i++)
			{
			if (i == v)
				{
				valueProbability[n][i] = 1;
				}
			else
				{
				valueProbability[n][i] = 0;
				}
			}
		}

	public void setRootDistribution(double[] v)
		{
		setDistribution(0, v);
		}

	public void setDistribution(int n, double[] v)
		{
		System.arraycopy(v, 0, valueProbability[n], 0, values);
		}

	public void evolveAlongTree(TransitionMatrix m) throws DistributionException, TreeException
		{
		// the root value must be set already
		for (int n : theTree.breadthFirst())
			{
			if (n == 0)
				{
				continue;
				}
			setValue(n, m.sampleTransition(ArrayUtils.argmax(valueProbability[theTree.getParent(n)])));
			}
		}


	public void inferAncestorsML()

		{
		}

	public void inferUnknownSumProduct()

		{
		}

	public int get(int node)
		{
		return ArrayUtils.argmax(valueProbability[node]);
		}

	public void setAncestorsUnknown()

		{
		for (int i = 0; i < theTree.getNumNodes(); i++)
			{
			if (theTree.isLeaf(i))
				{
				setValue(i, UNKNOWN);
				}
			}
		}

	/**
	 * recursively calculate the log likelihood of the current annotation distribution given the model (the transition
	 * matrix), starting from the root.
	 *
	 * @param tm
	 * @return
	 */
	public double logLikelihood(TransitionMatrix tm)

		{
		// assert the tree is completely annotated
		return logLikelihood(0, tm);
		}

	/**
	 * recursively calculate the log likelihood of the current annotation distribution given the model (the transition
	 * matrix), starting from the given node.
	 *
	 * @param node
	 * @param tm
	 * @return
	 */
	public double logLikelihood(int node, TransitionMatrix tm)

		{
		// MAYBE uncomment and complete
		/*		if(theTree.isLeaf(node)) { return 0; }
				double result = 0;
				int child1 = theTree.getChild1(node);
				result +=
						log(DotProduct(MatrixMultiply(valueProbability[node],
												  valueProbability[child1]),
								   tm));
				result += logLikelihood(child1, tm);

				int child2 = theTree.getChild2(node);
				result +=
						log(DotProduct(MatrixMultiply(valueProbability[node],
												  valueProbability[child2]),
								   tm));

				result += logLikelihood(child1, tm);

				return result;*/
		return 0;
		}
	}
