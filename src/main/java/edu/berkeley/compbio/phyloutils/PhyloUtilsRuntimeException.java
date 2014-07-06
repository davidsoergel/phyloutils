/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ChainedRuntimeException;
import org.apache.log4j.Logger;

/**
 * This exception is thrown when something goes wrong in the phyloutils package.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class PhyloUtilsRuntimeException extends ChainedRuntimeException
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(PhyloUtilsRuntimeException.class);


	// --------------------------- CONSTRUCTORS ---------------------------

	public PhyloUtilsRuntimeException(String s)
		{
		super(s);
		}

	public PhyloUtilsRuntimeException(Exception e)
		{
		super(e);
		}

	public PhyloUtilsRuntimeException(Exception e, String s)
		{
		super(e, s);
		}
	}
