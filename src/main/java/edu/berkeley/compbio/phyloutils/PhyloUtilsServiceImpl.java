package edu.berkeley.compbio.phyloutils;

import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNameDao;
import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNodeDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyNode;
import org.apache.log4j.Logger;
import pal.tree.ReadTree;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import pal.tree.TreeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: soergel Date: Nov 6, 2006 Time: 2:21:41 PM To change this template use File |
 * Settings | File Templates.
 */
public class PhyloUtilsServiceImpl
	{
	private NcbiTaxonomyNameDao ncbiTaxonomyNameDao;
	private NcbiTaxonomyNodeDao ncbiTaxonomyNodeDao;

	private Map<String, Integer> taxIdByNameRelaxed = new HashMap<String, Integer>();
	private Map<String, Integer> taxIdByName = new HashMap<String, Integer>();

	public void setNcbiTaxonomyNameDao(NcbiTaxonomyNameDao ncbiTaxonomyNameDao)
		{
		this.ncbiTaxonomyNameDao = ncbiTaxonomyNameDao;
		}

	public void setNcbiTaxonomyNodeDao(NcbiTaxonomyNodeDao ncbiTaxonomyNodeDao)
		{
		this.ncbiTaxonomyNodeDao = ncbiTaxonomyNodeDao;
		}

	protected static Logger logger = Logger.getLogger(PhyloUtilsServiceImpl.class);

	private Tree ciccarelliTree;
	private String ciccarelliFilename = "tree_Feb15_unrooted.txt";
	//private static HibernateDB ncbiDb;

	/*
		{
		try
			{
			logger.info("Initializing NCBI taxonomy database connection...");
			init();
			}
		catch (PhyloUtilsException e)
			{
			e.printStackTrace();
			logger.error(e);
			}
		}
*/

	public PhyloUtilsServiceImpl()// throws PhyloUtilsException
		{
		try
			{
			URL res = ClassLoader.getSystemResource(ciccarelliFilename);
			InputStream is = res.openStream();
			/*if (is == null)
				{
				is = new FileInputStream(filename);
				}*/
			ciccarelliTree = new ReadTree(new PushbackReader(new InputStreamReader(is)));
			}
		catch (IOException e)
			{
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			logger.error(e);
			}
		catch (TreeParseException e)
			{
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			logger.error(e);
			}

		/*
		ncbiDb = new HibernateDB("ncbiTaxonomy");
		if (ncbiDb == null)
			{
			throw new PhyloUtilsException("Couldn't connect to NCBI Taxonomy database");
			}
			*/
		}


	public double exactDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		Integer taxIdA = taxIdByName.get(speciesNameA);
		if (taxIdA == null)
			{
			taxIdA = ncbiTaxonomyNameDao.findByName(speciesNameA).getTaxon().getId();
			taxIdByName.put(speciesNameA, taxIdA);
			}
		Integer taxIdB = taxIdByName.get(speciesNameB);
		if (taxIdB == null)
			{
			taxIdB = ncbiTaxonomyNameDao.findByName(speciesNameB).getTaxon().getId();
			taxIdByName.put(speciesNameB, taxIdB);
			}
		//logger.error(speciesNameA + " -> " + taxIdA);
		//logger.error(speciesNameB + " -> " + taxIdB);

		return exactDistanceBetween(taxIdA, taxIdB);
		}

	/**
	 * For each species, walk up the NCBI tree until a node that is part of the Ciccarelli tree is found; then return the
	 * Ciccarelli distance.
	 *
	 * @param speciesNameA
	 * @param speciesNameB
	 * @return
	 */
	public double minDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		if (speciesNameA.equals(speciesNameB))
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		Integer taxIdA = taxIdByNameRelaxed.get(speciesNameA);
		if (taxIdA == null)
			{
			taxIdA = ncbiTaxonomyNameDao.findByNameRelaxed(speciesNameA).getTaxon().getId();
			taxIdByNameRelaxed.put(speciesNameA, taxIdA);
			}
		Integer taxIdB = taxIdByNameRelaxed.get(speciesNameB);
		if (taxIdB == null)
			{
			taxIdB = ncbiTaxonomyNameDao.findByNameRelaxed(speciesNameB).getTaxon().getId();
			taxIdByNameRelaxed.put(speciesNameB, taxIdB);
			}

		return minDistanceBetween(taxIdA, taxIdB);
		}

	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		if (taxIdA == taxIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		int treeIdA = ciccarelliTree.whichIdNumber("" + taxIdA);
		int treeIdB = ciccarelliTree.whichIdNumber("" + taxIdB);
		//logger.error("" + taxIdA + " -> " + treeIdA);
		//logger.error("" + taxIdB + " -> " + treeIdB);
		if (treeIdA == treeIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		try
			{
			return TreeUtils.computeDistance(ciccarelliTree, treeIdA, treeIdB);
			}
		catch (Exception e)
			{
			logger.debug(e);
			throw new PhyloUtilsException(e);
			}
		}


	/**
	 * For each species, walk up the NCBI tree until a node that is part of the Ciccarelli tree is found; then return the
	 * Ciccarelli distance.
	 */
	public double minDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		if (taxIdA == taxIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		taxIdA = nearestKnownAncestor(taxIdA);
		taxIdB = nearestKnownAncestor(taxIdB);
		return exactDistanceBetween(taxIdA, taxIdB);

		}

	public int nearestKnownAncestor(int taxId) throws PhyloUtilsException
		{
		NcbiTaxonomyNode n = ncbiTaxonomyNodeDao.findByTaxId(taxId);
		while (ciccarelliTree.whichIdNumber("" + n.getId()) == -1)
			{
			n = n.getParent();
			if (n.getId() == 1)
				{
				// arrived at root, too bad
				throw new PhyloUtilsException("Taxon " + taxId + " not found in tree.");
				}
			//ncbiDb.getEntityManager().refresh(n);
			}
		return n.getId();
		}

	/*
   public static HibernateDB getNcbiDb()
	   {
	   return ncbiDb;
	   }*/
	}
