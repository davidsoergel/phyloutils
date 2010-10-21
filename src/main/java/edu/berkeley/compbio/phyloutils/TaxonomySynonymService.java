package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;

import java.util.Collection;

/**
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
