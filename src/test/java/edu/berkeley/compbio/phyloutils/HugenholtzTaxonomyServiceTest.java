package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.BasicRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import edu.berkeley.compbio.ncbitaxonomy.service.NcbiTaxonomyClient;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HugenholtzTaxonomyServiceTest
	{
/*	@Test
	public void fastTreeDisagreesWithDnaDist() throws NoSuchNodeException
		{
		HugenholtzTaxonomyService h = new HugenholtzTaxonomyService();
		double fasttreeDist = h.minDistanceBetween(351896, 159339);
		}

	@BeforeClass
	public void setUp() throws Exception
		{
		EnvironmentUtils.setCacheRoot("/tmp/testCache");
		}*/

	private static final Logger logger = Logger.getLogger(HugenholtzTaxonomyServiceTest.class);
	HugenholtzTaxonomyService service;

	@BeforeTest
	public void setUp()
		{
		service = new HugenholtzTaxonomyService();
		//BAD test hack
		service.setGreengenesRawFilename("/Users/lorax/n/gg.ex.raw");
		service.setHugenholtzFilename("/Users/lorax/gg.ex.aligned.masked.tree.renum");
		service.setSynonymService(NcbiTaxonomyClient.getInstance());
		service.init();
		}

	@Test
	public void testRothia() throws NoSuchNodeException
		{
		String name = "Rothia mucilaginosa DY-18";
		int id = service.findTaxidByName(name);
		logger.info(name + " -> " + id);
		Set<Integer> idBSet;

		//idBSet = service.findMatchingIds(name);

		//if (idBSet == null || idBSet.isEmpty())
		//	{
		idBSet = service.findMatchingIdsRelaxed(name);
		// this is the only source of NoSuchNodeException that should produce NOTFOUND
		//	}
		BasicRootedPhylogeny<Integer> bTree = service.findCompactSubtreeWithIds(idBSet, name);
		Integer idB = bTree.getShallowestLeaf();

		logger.info(name + " shallowest leaf -> " + idB);
		}
	}
