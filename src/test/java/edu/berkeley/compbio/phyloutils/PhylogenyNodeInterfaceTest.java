package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;
import org.testng.annotations.Test;

import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public abstract class PhylogenyNodeInterfaceTest extends ContractTestAware<PhylogenyNode<String>>
	{
	private TestInstanceFactory<? extends PhylogenyNode<String>> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public PhylogenyNodeInterfaceTest(TestInstanceFactory<? extends PhylogenyNode<String>> tif)
		{
		this.tif = tif;
		}

	public void addContractTestsToQueue(Queue<Object> theContractTests)
		{
		theContractTests.add(new LengthWeightHierarchyNodeInterfaceTest(tif)
		{
		});
		}

	@Test
	public void getChildWorksIfChildIsPresent() throws Exception
		{
		PhylogenyNode<String> tmp = tif.createInstance();
		tmp.getChild("Node Present");
		}

	@Test(expectedExceptions = NoSuchElementException.class)
	public void getChildThrowsExceptionIfChildIsAbsent() throws Exception
		{
		PhylogenyNode<String> tmp = tif.createInstance();
		tmp.getChild("Node Absent");
		}


	@Test
	public void propagateWeightFromBelowUpdatesAllDescendants() throws Exception
		{
		PhylogenyNode<String> tmp = tif.createInstance();
		assert tmp.getWeight() == 0;
		for (LengthWeightHierarchyNode<String> n : tmp)
			{
			assert n.isLeaf() || n.getWeight() == 0;
			}
		tmp.propagateWeightFromBelow();
		for (LengthWeightHierarchyNode<String> n : tmp)
			{
			assert n.getWeight() != 0;
			}
		}
	}

