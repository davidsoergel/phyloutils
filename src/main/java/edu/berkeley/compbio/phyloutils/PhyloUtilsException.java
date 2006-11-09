package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ChainedException;
import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
public class PhyloUtilsException extends ChainedException
	{
	private static Logger logger = Logger.getLogger(PhyloUtilsException.class);

	public PhyloUtilsException(String s)
		{
		super(s);
		}

	public PhyloUtilsException(Exception e, String s)
		{
		super(e, s);
		}

	public PhyloUtilsException(Exception e)
		{
		super(e);
		}
	}
