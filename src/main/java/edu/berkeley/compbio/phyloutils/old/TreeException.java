package edu.berkeley.compbio.phyloutils.old;

import com.davidsoergel.dsutils.ChainedException;
import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class TreeException extends ChainedException
	{
	private static Logger logger = Logger.getLogger(TreeException.class);

	public TreeException(String s)
		{
		super(s);
		}

	public TreeException(Throwable e)
		{
		super(e);
		}

	public TreeException(Throwable e, String s)
		{
		super(e, s);
		}
	}
