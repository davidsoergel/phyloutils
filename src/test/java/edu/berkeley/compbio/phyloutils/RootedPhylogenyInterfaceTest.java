package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAwareContractTest;
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
 * @version $Id$
 */
public class RootedPhylogenyInterfaceTest<T extends RootedPhylogeny>
		extends ContractTestAwareContractTest<RootedPhylogeny>
	{
	private TestInstanceFactory<T> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public void addContractTestsToQueue(Queue theContractTests)
		{
		theContractTests.add(new PhylogenyNodeInterfaceTest<T>(tif));
		theContractTests.add(new TaxonMergingPhylogenyInterfaceTest<T>(tif));
		}

	// --------------------------- CONSTRUCTORS ---------------------------

	public RootedPhylogenyInterfaceTest(TestInstanceFactory<T> tif)
		{
		this.tif = tif;
		}

	@Test
	public void findsCommonAncestorOfTwoNodes() throws Exception
		{
		T tmp = tif.createInstance();
		assert tmp.commonAncestor("baa", "bbba").equals("b");
		}

	@Test
	public void findsCommonAncestorOfManyNodes() throws Exception
		{
		T tmp = tif.createInstance();
		Set nodeSet = new HashSet(Arrays.asList(new String[]{
				"baa",
				"ba",
				"bba",
				"bbba"
		}));
		assert tmp.commonAncestor(nodeSet).equals("b");
		}

	@Test
	public void computesDistanceBetweenTwoNodes() throws Exception
		{
		T tmp = tif.createInstance();
		assert tmp.distanceBetween("baa", "cb") == 11.2;
		}

	@Test
	public void returnsAllNodes() throws Exception
		{
		T tmp = tif.createInstance();
		assert tmp.getNodes().size() == 17;
		}

	@Test
	public void returnsAllLeaves() throws Exception
		{
		T tmp = tif.createInstance();
		assert tmp.getLeaves().size() == 8;
		}

	@Test
	public void returnsAllNodeValues() throws Exception
		{
		T tmp = tif.createInstance();
		assert tmp.getNodeValues().size() == 17;
		}

	@Test
	public void returnsAllLeafValues() throws Exception
		{
		T tmp = tif.createInstance();
		assert tmp.getLeaves().size() == 8;
		}

	@Test
	public void computesTotalBranchLength() throws Exception
		{
		T tmp = tif.createInstance();
		assert MathUtils.equalWithinFPError(tmp.getTotalBranchLength(), 87);
		}

	@Test
	public void randomizedLeafWeightsAreNormalized() throws Exception
		{
		RootedPhylogeny<Object> tmp = tif.createInstance();
		tmp.randomizeLeafWeights(new UniformDistribution(0, 1));
		assert MathUtils.equalWithinFPError(tmp.getWeight(), 1);

		double leafSum = 0;
		for (PhylogenyNode n : tmp.getLeaves())
			{
			leafSum += n.getWeight();
			}

		assert MathUtils.equalWithinFPError(leafSum, 1);
		}

	@Test
	public void findsNearestKnownAncestorInAnotherTree() throws Exception
		{
		T tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void computesIntersectionBetweenTwoSubtrees() throws Exception
		{
		T tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void providesWeightMixedTree() throws Exception
		{
		T tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void copiesWeightsFromAnotherTreeWithPseudocounts() throws Exception
		{
		T tmp = tif.createInstance();
		assert false;
		}

	@Test
	public void setsAndNormalizesLeafWeightsFromMultiset() throws Exception
		{
		T tmp = tif.createInstance();
		assert false;
		}
	}
