/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.TreeException;
import org.apache.log4j.Logger;

/**
 * This exception is thrown when something goes wrong in the phyloutils package.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class PhyloUtilsException extends TreeException
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(PhyloUtilsException.class);


	// --------------------------- CONSTRUCTORS ---------------------------

	public PhyloUtilsException(String s)
		{
		super(s);
		}

	public PhyloUtilsException(Exception e)
		{
		super(e);
		}

	public PhyloUtilsException(Exception e, String s)
		{
		super(e, s);
		}
	}
