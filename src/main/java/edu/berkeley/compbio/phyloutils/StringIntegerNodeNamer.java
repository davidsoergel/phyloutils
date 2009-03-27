package edu.berkeley.compbio.phyloutils;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class StringIntegerNodeNamer extends StringNodeNamer
	{
	public StringIntegerNodeNamer(String unknownBasis, boolean allowNull)
		{
		super(unknownBasis, allowNull);
		}

	public StringIntegerNodeNamer(String unknownBasis, boolean allowNull, int startId)
		{
		super(unknownBasis, allowNull, startId);
		}

	public boolean isAcceptable(String value)
		{
		if (value == null)
			{
			return allowNull;
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
