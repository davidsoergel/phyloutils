package edu.berkeley.compbio.phyloutils;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class StringIntegerNodeNamer extends StringNodeNamer
	{
	public StringIntegerNodeNamer(String unknownBasis)
		{
		super(unknownBasis);
		}

	public StringIntegerNodeNamer(String unknownBasis, int startId)
		{
		super(unknownBasis, startId);
		}

	public boolean isAcceptable(String value)
		{
		if (value == null)
			{
			return false;
			}

		try
			{
			Integer.parseInt(value);
			return true;
			}
		catch (NumberFormatException e)
			{
			return false;
			}
		}
	}
