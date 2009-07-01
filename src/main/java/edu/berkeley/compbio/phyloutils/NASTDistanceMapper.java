package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.StringListDoubleMapReader;
import org.apache.commons.collections15.map.MultiKeyMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Estimate the phylogenetic distance on one of Morgan Price's FastTree + GreenGenes trees between a NAST-aligned
 * fragment pair
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NASTDistanceMapper
	{
	int[] positions;
	int[] widths;
	MultiKeyMap<Integer, Double> slopeTable = new MultiKeyMap<Integer, Double>();
	//** make configurable
	final String filename = "nast.constraints.10k..4.slopes.txt";

	public NASTDistanceMapper() throws IOException
		{
		Map<String, List<Double>> map = StringListDoubleMapReader.read(filename);

		positions = DSArrayUtils.castToInt(map.get("Positions").toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
		widths = DSArrayUtils.castToInt(map.get("NonGapWidths").toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));

		List<Double> rowMajorSlopes = map.get("Slopes");
		int numPositions = positions.length;

		for (int widthIndex = 0; widthIndex < widths.length; widthIndex++)
			{
			for (int positionIndex = 0; positionIndex < positions.length; positionIndex++)
				{
				// see GreenGenesFragmentDistanceCorrelationGrid:145
				//int gridIndex = widthQuantizedIndex * positions.length + positionIndex;

				int slopeIndex = widthIndex * numPositions + positionIndex;
				//slopeTable.put(widths[widthIndex], positions[positionIndex], rowMajorSlopes.get(slopeIndex));
				slopeTable.put(widthIndex, positionIndex, rowMajorSlopes.get(slopeIndex));
				}
			}
		}

	public double map(final int position, final int width, final double dnadist)
		{
		// find the floor of the position and width among the known entries

		int widthQuantizedIndex = Arrays.binarySearch(widths, width);
		if (widthQuantizedIndex < 0)
			{
			//BAD test me
			widthQuantizedIndex = -(widthQuantizedIndex + 1) - 1;
			}

		int positionQuantizedIndex = Arrays.binarySearch(positions, position);
		if (positionQuantizedIndex < 0)
			{
			//BAD test me
			positionQuantizedIndex = -(positionQuantizedIndex + 1) - 1;
			}

		double slope = slopeTable.get(widthQuantizedIndex, positionQuantizedIndex);
		return dnadist * slope;
		}
	}
