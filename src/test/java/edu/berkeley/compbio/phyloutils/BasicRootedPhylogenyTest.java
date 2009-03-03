package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;
import org.apache.log4j.Logger;
import org.testng.annotations.Factory;

import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BasicRootedPhylogenyTest extends ContractTestAware<BasicRootedPhylogeny>
		implements TestInstanceFactory<BasicRootedPhylogeny>
	{
	private BasicRootedPhylogenyWithSpecificNodeHandles testInstance;


	public BasicRootedPhylogeny createInstance() throws Exception
		{
		return new BasicRootedPhylogenyWithSpecificNodeHandles().rootPhylogeny;
		}

	@Override
	public void addContractTestsToQueue(Queue theContractTests)
		{
		theContractTests.add(new AbstractRootedPhylogenyAbstractTest<BasicRootedPhylogeny>(this));
		}

	@Factory
	public Object[] instantiateAllContractTests()
		{
		return super.instantiateAllContractTestsWithName(BasicRootedPhylogeny.class.getCanonicalName());
		}

	public static class BasicRootedPhylogenyWithSpecificNodeHandles
		{
		private static final Logger logger = Logger.getLogger(BasicRootedPhylogenyWithSpecificNodeHandles.class);

		BasicRootedPhylogeny<String> rootPhylogeny = new BasicRootedPhylogeny<String>("root");


		BasicPhylogenyNode<String> root = rootPhylogeny.getRoot();
		//new BasicPhylogenyNode<String>(null, "root", 0);

		BasicPhylogenyNode<String> a = new BasicPhylogenyNode<String>(root, "a", 10);
		BasicPhylogenyNode<String> b = new BasicPhylogenyNode<String>(root, "b", 4);
		BasicPhylogenyNode<String> c = new BasicPhylogenyNode<String>(root, "c", 1);

		BasicPhylogenyNode<String> aa = new BasicPhylogenyNode<String>(a, "aa", 20);
		BasicPhylogenyNode<String> ab = new BasicPhylogenyNode<String>(a, "ab", 30);


		BasicPhylogenyNode<String> aaa = new BasicPhylogenyNode<String>(aa, "aaa");
		BasicPhylogenyNode<String> aaaa = new BasicPhylogenyNode<String>(aaa, "aaaa");

		//	PhylogenyNode<String> aaa = new PhylogenyNode<String>("aaa", aa, 1);
		//	PhylogenyNode<String> aab = new PhylogenyNode<String>("aab", aa, 1);


		BasicPhylogenyNode<String> ba = new BasicPhylogenyNode<String>(b, "ba", 1.1);
		BasicPhylogenyNode<String> bb = new BasicPhylogenyNode<String>(b, "bb", 1.2);


		BasicPhylogenyNode<String> baa = new BasicPhylogenyNode<String>(ba, "baa", 2.1);
		BasicPhylogenyNode<String> bab = new BasicPhylogenyNode<String>(ba, "bab", 2.2);


		BasicPhylogenyNode<String> bba = new BasicPhylogenyNode<String>(bb, "bba", 3.1);
		BasicPhylogenyNode<String> bbb = new BasicPhylogenyNode<String>(bb, "bbb", 3.2);


		BasicPhylogenyNode<String> bbba = new BasicPhylogenyNode<String>(bbb, "bbba", 4.1);


		BasicPhylogenyNode<String> ca = new BasicPhylogenyNode<String>(c, "ca", 2);
		BasicPhylogenyNode<String> cb = new BasicPhylogenyNode<String>(c, "cb", 3);

		StringNodeNamer namer = new StringNodeNamer("UNKNOWN NODE");

		public BasicRootedPhylogenyWithSpecificNodeHandles()
			{
			try
				{
				rootPhylogeny.assignUniqueIds(namer);
				rootPhylogeny.uniformizeLeafWeights();
				}
			catch (PhyloUtilsException e)
				{
				// impossible
				logger.error("Error", e);
				throw new Error(e);
				}
			}
		}
	}
