package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;
import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.stats.UniformDistribution;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public abstract class RootedPhylogenyInterfaceTest extends ContractTestAware<RootedPhylogeny>
	{
	private TestInstanceFactory<? extends RootedPhylogeny<String>> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public void addContractTestsToQueue(Queue<Object> theContractTests)
		{
		theContractTests.add(new PhylogenyNodeInterfaceTest(tif)
		{
		});
		theContractTests.add(new TaxonMergingPhylogenyInterfaceTest(tif)
		{
		});
		}

	// --------------------------- CONSTRUCTORS ---------------------------

	public RootedPhylogenyInterfaceTest(TestInstanceFactory<? extends RootedPhylogeny<String>> tif)
		{
		this.tif = tif;
		}

	@Test
	public void findsCommonAncestorOfTwoNodes() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.commonAncestor("Pair1", "Pair2").equals("PairCommonAncestor");
		}

	@Test
	public void findsCommonAncestorOfManyNodes() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		Set<String> nodeSet = new HashSet<String>(Arrays.asList(new String[]{
				"node1",
				"node2",
				"node3",
				"node4"
		}));
		assert tmp.commonAncestor(nodeSet).equals("PairCommonAncestor");
		}

	@Test
	public void computesDistanceBetweenTwoNodes() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.distanceBetween("Pair1", "Pair2") == 10;
		}

	@Test
	public void returnsAllNodes() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.getNodes().size() == 10;
		}

	@Test
	public void returnsAllLeaves() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.getLeaves().size() == 6;
		}

	@Test
	public void returnsAllNodeValues() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.getNodeValues().size() == 10;
		}

	@Test
	public void returnsAllLeafValues() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.getLeaves().size() == 6;
		}

	@Test
	public void computesTotalBranchLength() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert tmp.getTotalBranchLength() == 50;
		}

	@Test
	public void randomizedLeafWeightsAreNormalized() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		tmp.randomizeLeafWeights(new UniformDistribution(0, 1));
		assert MathUtils.equalWithinFPError(tmp.getWeight(), 1);

		double leafSum = 0;
		for (PhylogenyNode<String> n : tmp.getLeaves())
			{
			leafSum += n.getWeight();
			}

		assert MathUtils.equalWithinFPError(leafSum, 1);
		}

	@Test
	public void findsNearestKnownAncestorInAnotherTree() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void computesIntersectionBetweenTwoSubtrees() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void providesWeightMixedTree() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void copiesWeightsFromAnotherTreeWithPseudocounts() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void setsAndNormalizesLeafWeightsFromMultiset() throws Exception
		{
		RootedPhylogeny<String> tmp = tif.createInstance();
		assert false;
		}
	}
