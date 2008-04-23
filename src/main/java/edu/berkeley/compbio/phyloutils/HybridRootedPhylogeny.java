/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
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
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.NoResultException;

/* $Id$ */

/**
 * Sometimes we have branch lengths on one phylogeny, but we want to know distances between nodes that are not on that
 * phylogeny.  In that case we may want to travel up a different phylogeny (e.g., the NCBI taxonomy) until we find
 * matching nodes, and thereby get at least a lower bound on the distance.
 *
 * @Author David Soergel
 * @Version 1.0
 */
public class HybridRootedPhylogeny
	{

	public double minDistanceBetween(String speciesNameA, String speciesNameB) throws NcbiTaxonomyException
		{
		return ncbiTaxonomyServiceImpl.minDistanceBetween(speciesNameA, speciesNameB);
		}

	public double minDistanceBetween(int taxIdA, int taxIdB) throws NcbiTaxonomyException
		{
		return ncbiTaxonomyServiceImpl.minDistanceBetween(taxIdA, taxIdB);
		}


	public int nearestKnownAncestor(int taxId) throws NcbiTaxonomyException
		{
		return ncbiTaxonomyServiceImpl.nearestKnownAncestor(taxId);
		}

	public int nearestKnownAncestor(String speciesName) throws NcbiTaxonomyException
		{
		return ncbiTaxonomyServiceImpl.nearestKnownAncestor(speciesName);
		}


	/**
	 * For each species, walk up the NCBI tree until a node that is part of the Ciccarelli tree is found; then return the
	 * Ciccarelli distance.
	 *
	 * @param speciesNameA
	 * @param speciesNameB
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public double minDistanceBetween(RootedPhylogeny tree, String speciesNameA, String speciesNameB) throws NcbiTaxonomyException
		{
		if (speciesNameA.equals(speciesNameB))
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		Integer taxIdA = findTaxidByName(speciesNameA);
		Integer taxIdB = findTaxidByName(speciesNameB);

		return minDistanceBetween(tree, taxIdA, taxIdB);
		}

	/**
	 * For each species, walk up the NCBI tree until a node that is part of the Ciccarelli tree is found; then return the
	 * Ciccarelli distance.
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public double minDistanceBetween(RootedPhylogeny tree, int taxIdA, int taxIdB) throws NcbiTaxonomyException
		{
		if (taxIdA == taxIdB)
			{
			return 0;// account for TreeUtils.computeDistance bug
			}
		taxIdA = nearestKnownAncestor(tree, taxIdA);
		taxIdB = nearestKnownAncestor(tree, taxIdB);
		return tree.distanceBetween(taxIdA, taxIdB);
		}

	/**
	 * Search up the NCBI taxonomy until a node is encountered that is a leaf in the Ciccarelli taxonomy
	 *
	 * @param taxId
	 * @return
	 * @throws PhyloUtilsException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public int nearestKnownAncestor(RootedPhylogeny tree, int taxId) throws NcbiTaxonomyException
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
				throw new NcbiTaxonomyException("Taxon " + taxId + " does not exist in the NCBI taxonomy.");
				}
			while (tree.getNode(n.getId()) == null)
				{
				n = n.getParent();
				if (n.getId() == 1)
					{
					// arrived at root, too bad
					throw new NcbiTaxonomyException("Taxon " + taxId + " not found in tree.");
					}
				//ncbiDb.getEntityManager().refresh(n);
				}
			result = n.getId();
			nearestKnownAncestorCache.put(taxId, result);
			}
		//return n.getId();
		return result;
		}


	@Transactional(propagation = Propagation.REQUIRED)
	public int nearestKnownAncestor(RootedPhylogeny tree, String speciesName) throws NcbiTaxonomyException
		{
		return nearestKnownAncestor(tree, findTaxidByName(speciesName));
		}
	}
