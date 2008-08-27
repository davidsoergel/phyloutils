package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;

import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public abstract class AbstractRootedPhylogenyAbstractTest extends ContractTestAware<AbstractRootedPhylogeny>

	{
	private TestInstanceFactory<? extends AbstractRootedPhylogeny<String>> tif;

	public AbstractRootedPhylogenyAbstractTest(TestInstanceFactory<? extends AbstractRootedPhylogeny<String>> tif)
		{
		this.tif = tif;
		}

	public void addContractTestsToQueue(Queue<Object> theContractTests)
		{
		theContractTests.add(new RootedPhylogenyInterfaceTest(tif)
		{
		});
		}
	}
