/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import org.apache.log4j.Logger;

/**
 * Reads a Newick tree file containing the Tree of Life (Ciccarelli 2006, http://itol.embl.de) and provides it as a
 * RootedPhylogeny.  Also provides convenience methods for a few common operations such as computing the tree distance
 * between two species.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class CiccarelliTaxonomyService extends NewickStringTaxonomyService //<String>
	{
	private static final Logger logger = Logger.getLogger(CiccarelliTaxonomyService.class);

	//private String ciccarelliFilename = "tree_Feb15_unrooted.txt";

	// this tree has integer taxids and precomputed fake ids for unnamed internal nodes: "itol090314_name.tree.taxids"
	// but we don't want that, we just want the names

	private static final String ciccarelliFilename = "itol090314_name.tree";

	private static CiccarelliTaxonomyService instance;// = new CiccarelliUtils();


	public static CiccarelliTaxonomyService getInstance()
		{
		if (instance == null)
			{
			instance = new CiccarelliTaxonomyService();
			}
		return instance;
		}


	public CiccarelliTaxonomyService()// throws PhyloUtilsException
	{
	super(ciccarelliFilename, false);
	}

	@Override
	public String toString()
		{
		String shortname = getClass().getName();
		shortname = shortname.substring(shortname.lastIndexOf(".") + 1);
		return shortname;
		}
	}
