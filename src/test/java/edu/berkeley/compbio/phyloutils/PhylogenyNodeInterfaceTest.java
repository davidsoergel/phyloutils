package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;

import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public abstract class PhylogenyNodeInterfaceTest extends ContractTestAware<LengthWeightHierarchyNode>
	{
	private TestInstanceFactory<? extends PhylogenyNode> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public PhylogenyNodeInterfaceTest(TestInstanceFactory<? extends PhylogenyNode> tif)
		{
		this.tif = tif;
		}

	public void addContractTestsToQueue(Queue<Object> theContractTests)
		{
		theContractTests.add(new LengthWeightHierarchyNodeInterfaceTest(tif)
		{
		});
		}
	}

