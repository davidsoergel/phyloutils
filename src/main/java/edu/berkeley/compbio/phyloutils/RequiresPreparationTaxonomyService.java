package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.NoSuchNodeException;

import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface RequiresPreparationTaxonomyService<T>
	{

	void prepare(Set<T> allLabels) throws NoSuchNodeException;
	}
