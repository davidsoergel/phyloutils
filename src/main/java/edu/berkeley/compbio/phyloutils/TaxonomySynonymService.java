/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;

import java.util.Collection;

/**
 * Map taxonomic names to synonymous taxonomic names.  Does not expose a unique ID per taxon.  Note this is not needed
 * to solve the problem of finding a node id matching a given name; that is dealt with TaxonStringIdMapper.  The reason
 * this is done this way is that we want to use the Ncbi synonym table to help map names to prokmsa_ids, but we don't
 * want to get ncbi taxids in the mix.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonomySynonymService
	{
	/**
	 * Map taxonomic names to synonymous taxonomic names.  Does not expose a unique ID per taxon.
	 *
	 * @param name
	 * @return
	 * @throws NoSuchNodeException
	 */
	Collection<String> synonymsOf(String name) throws NoSuchNodeException;

	//Collection<String> synonymsOfParent(String name) throws NoSuchNodeException;

	/**
	 * Map taxonomic names to synonymous taxonomic names, with relaxed matching of the query Re.g. removing overly specific
	 * suffixes as needed).  Does not expose a unique ID per taxon.
	 *
	 * @param name
	 * @return
	 * @throws NoSuchNodeException
	 */
	Collection<String> synonymsOfRelaxed(String name) throws NoSuchNodeException;

//	void saveState();
	}
