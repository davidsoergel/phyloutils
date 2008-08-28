package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;
import org.testng.annotations.Test;

import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class PhylogenyNodeInterfaceTest<T extends PhylogenyNode> extends ContractTestAware<PhylogenyNode<String>>
	{
	private TestInstanceFactory<T> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public PhylogenyNodeInterfaceTest(TestInstanceFactory<T> tif)
		{
		this.tif = tif;
		}

	public void addContractTestsToQueue(Queue theContractTests)
		{
		theContractTests.add(new LengthWeightHierarchyNodeInterfaceTest<T>(tif));
		}

	@Test
	public void getChildWorksIfChildIsPresent() throws Exception
		{
		PhylogenyNode tmp = tif.createInstance();
		tmp.getChild("Node Present");
		}

	@Test(expectedExceptions = NoSuchElementException.class)
	public void getChildThrowsExceptionIfChildIsAbsent() throws Exception
		{
		PhylogenyNode tmp = tif.createInstance();
		tmp.getChild("Node Absent");
		}


	@Test
	public void propagateWeightFromBelowUpdatesAllDescendants() throws Exception
		{
		PhylogenyNode<Object> tmp = tif.createInstance();
		assert tmp.getWeight() == 0;
		for (LengthWeightHierarchyNode n : tmp)
			{
			assert n.isLeaf() || n.getWeight() == 0;
			}
		tmp.propagateWeightFromBelow();
		for (LengthWeightHierarchyNode n : tmp)
			{
			assert n.getWeight() != 0;
			}
		}
	}

