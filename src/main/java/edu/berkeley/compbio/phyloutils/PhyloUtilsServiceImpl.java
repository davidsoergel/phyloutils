/* $Id$ */

/*
 * Copyright (c) 2007 Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.compbio.phyloutils;

import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNameDao;
import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNodeDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyNode;
import org.apache.log4j.Logger;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: soergel Date: Nov 6, 2006 Time: 2:21:41 PM To change this template use File |
 * Settings | File Templates.
 */
public class PhyloUtilsServiceImpl
	{
	// ------------------------------ FIELDS ------------------------------

	protected static Logger logger = Logger.getLogger(PhyloUtilsServiceImpl.class);
	private NcbiTaxonomyNameDao ncbiTaxonomyNameDao;
	private NcbiTaxonomyNodeDao ncbiTaxonomyNodeDao;

	private Map<String, Integer> taxIdByNameRelaxed = new HashMap<String, Integer>();
	private Map<String, Integer> taxIdByName = new HashMap<String, Integer>();
	private Map<Integer, Integer> nearestKnownAncestorCache = new HashMap<Integer, Integer>();

	private RootedPhylogeny<Integer> ciccarelliTree;
	private String ciccarelliFilename = "tree_Feb15_unrooted.txt";


	// --------------------------- CONSTRUCTORS ---------------------------

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
			ciccarelliTree = new NewickParser<Integer>().read(is, new IntegerNodeNamer(100000000));
			}
		catch (IOException e)
			{
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			logger.error(e);
			}
		catch (PhyloUtilsException e)
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

	// --------------------- GETTER / SETTER METHODS ---------------------

	public void setNcbiTaxonomyNameDao(NcbiTaxonomyNameDao ncbiTaxonomyNameDao)
		{
		this.ncbiTaxonomyNameDao = ncbiTaxonomyNameDao;
		}

	public void setNcbiTaxonomyNodeDao(NcbiTaxonomyNodeDao ncbiTaxonomyNodeDao)
		{
		this.ncbiTaxonomyNodeDao = ncbiTaxonomyNodeDao;
		}

	// -------------------------- OTHER METHODS --------------------------

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
		Integer taxIdA = findTaxidByName(speciesNameA);
		Integer taxIdB = findTaxidByName(speciesNameB);

		return minDistanceBetween(taxIdA, taxIdB);
		}

	private Integer findTaxidByName(String speciesNameA) throws PhyloUtilsException
		{
		Integer taxIdA = taxIdByNameRelaxed.get(speciesNameA);
		if (taxIdA == null)
			{
			taxIdA = ncbiTaxonomyNameDao.findByNameRelaxed(speciesNameA).getTaxon().getId();
			taxIdByNameRelaxed.put(speciesNameA, taxIdA);
			}
		return taxIdA;
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

	public int nearestKnownAncestor(String speciesName) throws PhyloUtilsException
		{
		return nearestKnownAncestor(findTaxidByName(speciesName));
		}

	/**
	 * Search up the NCBI taxonomy until a node is encountered that is a leaf in the Ciccarelli taxonomy
	 *
	 * @param taxId
	 * @return
	 * @throws PhyloUtilsException
	 */
	public int nearestKnownAncestor(int taxId) throws PhyloUtilsException
		{
		Integer result = nearestKnownAncestorCache.get(taxId);
		if (result == null)
			{
			NcbiTaxonomyNode n;
			try
				{
				n = ncbiTaxonomyNodeDao.findByTaxId(taxId);
				}
			catch (NoResultException e)
				{
				throw new PhyloUtilsException("Taxon " + taxId + " does not exist in the NCBI taxonomy.");
				}
			while (ciccarelliTree.getNode(n.getId()) == null)
				{
				n = n.getParent();
				if (n.getId() == 1)
					{
					// arrived at root, too bad
					throw new PhyloUtilsException("Taxon " + taxId + " not found in tree.");
					}
				//ncbiDb.getEntityManager().refresh(n);
				}
			result = n.getId();
			nearestKnownAncestorCache.put(taxId, result);
			}
		//return n.getId();
		return result;
		}

	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{

		return ciccarelliTree.distanceBetween(taxIdA, taxIdB);

		}

	/*
   public static HibernateDB getNcbiDb()
	   {
	   return ncbiDb;
	   }*/

	public int commonAncestorID(Integer taxIdA, Integer taxIdB) throws PhyloUtilsException
		{
		if (taxIdA == null)
			{
			return taxIdB;
			}
		if (taxIdB == null)
			{
			return taxIdA;
			}

		taxIdA = nearestKnownAncestor(taxIdA);
		taxIdB = nearestKnownAncestor(taxIdB);


		if (taxIdA == taxIdB)
			{
			return taxIdA;
			}

		return ciccarelliTree.commonAncestor(taxIdA, taxIdB);
		}


	public Integer commonAncestorID(Set<Integer> mergeIds) throws PhyloUtilsException
		{
		mergeIds.remove(null);
		Set<Integer> knownMergeIds = new HashSet<Integer>();

		for (Integer id : mergeIds)
			{
			knownMergeIds.add(nearestKnownAncestor(id));
			}

		if (knownMergeIds.size() == 1)
			{
			return knownMergeIds.iterator().next();
			}

		return ciccarelliTree.commonAncestor(knownMergeIds);
		}
	}
