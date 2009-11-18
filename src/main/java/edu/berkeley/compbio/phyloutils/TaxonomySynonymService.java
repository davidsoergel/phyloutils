package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;

import java.util.Collection;

/**
 * Provides a simplified interface for taxonomy sources (e.g., NCBI, or a Newick file) that doesn't provide all the
 * services of a full RootedTree.  In particular none of the API involves PhylogenyNodes; everything is done by ID (of
 * the generic type)
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface TaxonomySynonymService
	{
	Collection<String> synonymsOf(String name) throws NoSuchNodeException;

	Collection<String> synonymsOfParent(String name) throws NoSuchNodeException;

	Collection<String> synonymsOfRelaxed(String name) throws NoSuchNodeException;

//	void saveState();
	}
