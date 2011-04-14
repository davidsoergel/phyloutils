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
