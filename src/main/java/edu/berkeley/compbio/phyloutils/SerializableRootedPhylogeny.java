package edu.berkeley.compbio.phyloutils;

import java.io.Serializable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SerializableRootedPhylogeny<T extends Serializable> extends RootedPhylogeny<T>, Serializable
	{
	}
