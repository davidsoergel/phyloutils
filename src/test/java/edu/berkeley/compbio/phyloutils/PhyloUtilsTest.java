package edu.berkeley.compbio.phyloutils;

import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNameDao;
import org.springframework.test.jpa.AbstractJpaTests;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Nov 6, 2006
 * Time: 4:29:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhyloUtilsTest extends AbstractJpaTests
	{
	protected String[] getConfigLocations()
		{
		return new String[]{
				"classpath:phyloutils.xml",
				"classpath:phyloutils-testdb.xml"
		};
		}

	private NcbiTaxonomyNameDao ncbiTaxonomyNameDao;
	private PhyloUtilsService phyloUtilsService;

	public void setNcbiTaxonomyNameDao(NcbiTaxonomyNameDao ncbiTaxonomyNameDao)
		{
		this.ncbiTaxonomyNameDao = ncbiTaxonomyNameDao;
		}

	public void setPhyloUtilsService(PhyloUtilsService phyloUtilsService)
		{
		this.phyloUtilsService = phyloUtilsService;
		}

	@BeforeTest
	public void launchSetup() throws Exception
		{
		setUp();
		}

	@AfterTest
	public void launchTearDown() throws Exception
		{
		tearDown();
		}


	@Test
	public void ciccarelliExactDistancesAreComputedCorrectly()
		{
		double d = phyloUtilsService.exactDistanceBetween(217992, 562);
		assert d == 0.000221;

		d = phyloUtilsService.exactDistanceBetween(217992, 59919);
		assert d == 1.47739;
		}

	@Test
	public void nearestKnownAncestorWorks() throws PhyloUtilsException
		{
		assert phyloUtilsService.nearestKnownAncestor(243277) == 666;
		}

	@Test
	public void findTaxonByNameWorks()
		{
		assert ncbiTaxonomyNameDao.findByName("Myxococcus xanthus").getTaxon().getTaxId() == 34;
		}


	}
