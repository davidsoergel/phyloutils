/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonStringIdMapper<T>
	{
	T findTaxidByName(String name) throws NoSuchNodeException; //, PhyloUtilsException;

	@NotNull
	T findTaxidByNameRelaxed(String name) throws NoSuchNodeException;

	Set<String> getCachedNamesForId(T id);
	}
