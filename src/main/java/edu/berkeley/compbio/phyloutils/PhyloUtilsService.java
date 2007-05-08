package edu.berkeley.compbio.phyloutils;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by IntelliJ IDEA. User: soergel Date: May 7, 2007 Time: 2:03:39 PM To change this template use File |
 * Settings | File Templates.
 */
public class PhyloUtilsService
	{
	private PhyloUtilsServiceImpl phyloUtilsServiceImpl;

	public PhyloUtilsService()
		{
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{
				"phyloutils.xml",
				"phyloutils-db.xml"
		});

		// add a shutdown hook for the above context...
		ctx.registerShutdownHook();

		phyloUtilsServiceImpl = ((PhyloUtilsServiceImpl) ctx.getBean("phyloUtilsServiceImpl"));
		}

	public double exactDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.exactDistanceBetween(speciesNameA, speciesNameB);
		}

	public double minDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.minDistanceBetween(speciesNameA, speciesNameB);
		}


	public double exactDistanceBetween(int taxIdA, int taxIdB)
		{
		return phyloUtilsServiceImpl.exactDistanceBetween(taxIdA, taxIdB);
		}


	public double minDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.minDistanceBetween(taxIdA, taxIdB);
		}


	public int nearestKnownAncestor(int taxId) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.nearestKnownAncestor(taxId);
		}

	}
