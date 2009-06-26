package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.EnvironmentUtils;
import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HugenholtzTaxonomyServiceTest
	{
	@Test
	public void fastTreeDisagreesWithDnaDist() throws NoSuchNodeException
		{
		HugenholtzTaxonomyService h = new HugenholtzTaxonomyService();
		double fasttreeDist = h.minDistanceBetween(351896, 159339);
		}

	@BeforeClass
	public void setUp() throws Exception
		{
		EnvironmentUtils.setCacheRoot("/tmp/testCache");
		}
	}
