package edu.berkeley.compbio.phyloutils;

import org.apache.commons.collections15.map.MultiKeyMap;

/**
 * Estimate the phylogenetic distance on one of Morgan Price's FastTree + GreenGenes trees between a NAST-aligned
 * fragment pair
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NASTDistanceMapper
	{
	MultiKeyMap<Integer, Double> slopeTable = new MultiKeyMap<Integer, Double>();
	//** make configurable
	final String filename = "";

	public NASTDistanceMapper()
		{
		}

	public double map(final int nastBegin, final int nastWidth, final double dnadist)
		{
		double slope = slopeTable.get(nastBegin, nastWidth);
		return dnadist * slope;
		}
	}
