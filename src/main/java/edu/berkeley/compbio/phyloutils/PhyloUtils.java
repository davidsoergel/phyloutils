package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.hibernateutils.HibernateDB;
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

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Nov 6, 2006
 * Time: 2:21:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhyloUtils
	{
	protected static Logger logger = Logger.getLogger(PhyloUtils.class);

	private static Tree ciccarelliTree;
	private static String ciccarelliFilename = "tree_Feb15_unrooted.txt";
	private static HibernateDB ncbiDb;

	static
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

	private static void init() throws PhyloUtilsException
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

		ncbiDb = new HibernateDB("ncbiTaxonomy");
		if (ncbiDb == null)
			{
			throw new PhyloUtilsException("Couldn't connect to NCBI Taxonomy database");
			}
		}


	public static double exactDistanceBetween(String speciesNameA, String speciesNameB)
		{
		int taxIdA = NcbiTaxonomyName.findByName(speciesNameA).getTaxon().getId();
		int taxIdB = NcbiTaxonomyName.findByName(speciesNameB).getTaxon().getId();

		logger.error(speciesNameA + " -> " + taxIdA);
		logger.error(speciesNameB + " -> " + taxIdB);

		return exactDistanceBetween(taxIdA, taxIdB);
		}

	/**
	 * For each species, walk up the NCBI tree until a node that is part of the Ciccarelli tree is found; then return the Ciccarelli distance.
	 *
	 * @param speciesNameA
	 * @param speciesNameB
	 * @return
	 */
	public static double minDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		if (speciesNameA.equals(speciesNameB))
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		int taxIdA = NcbiTaxonomyName.findByNameRelaxed(speciesNameA).getTaxon().getId();
		int taxIdB = NcbiTaxonomyName.findByNameRelaxed(speciesNameB).getTaxon().getId();
		return minDistanceBetween(taxIdA, taxIdB);
		}

	public static double exactDistanceBetween(int taxIdA, int taxIdB)
		{
		if (taxIdA == taxIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		int treeIdA = ciccarelliTree.whichIdNumber("" + taxIdA);
		int treeIdB = ciccarelliTree.whichIdNumber("" + taxIdB);
		logger.error("" + taxIdA + " -> " + treeIdA);
		logger.error("" + taxIdB + " -> " + treeIdB);
		if (treeIdA == treeIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		return TreeUtils.computeDistance(ciccarelliTree, treeIdA, treeIdB);
		}


	/**
	 * For each species, walk up the NCBI tree until a node that is part of the Ciccarelli tree is found; then return the Ciccarelli distance.
	 */
	public static double minDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		if (taxIdA == taxIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		taxIdA = nearestKnownAncestor(taxIdA);
		taxIdB = nearestKnownAncestor(taxIdB);
		return exactDistanceBetween(taxIdA, taxIdB);

		}

	public static int nearestKnownAncestor(int taxId) throws PhyloUtilsException
		{
		NcbiTaxonomyNode n = NcbiTaxonomyNode.findByTaxId(taxId);
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


	public static HibernateDB getNcbiDb()
		{
		return ncbiDb;
		}
	}
