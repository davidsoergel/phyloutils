/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;
import com.davidsoergel.dsutils.file.IntArrayReader;
import com.davidsoergel.trees.NoSuchNodeException;
import org.apache.commons.collections15.iterators.ArrayIterator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Load a list of "known" nodes, eg the isolates.  Then for each query sequence (eg from an environment), find the
 * closest known node, and print the distance (along with whatever other stats are desired, e.g. subtree span).
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NearestNodeFinder
	{
	static HugenholtzTaxonomyService service = new HugenholtzTaxonomyService();

	public static void main(String[] argv) throws IOException, NoSuchNodeException
		{
		//service.setGreengenesRawFilename(argv[0]);
		service.setHugenholtzFilename(argv[0]);
		//service.setSynonymService(NcbiTaxonomyClient.getInstance());
		service.init();

		final int[] targetIds = IntArrayReader.read(argv[1]);
		int[] queryIds = IntArrayReader.read(argv[2]);
		final double minDistance = Double.parseDouble(argv[3]);  // implement leave-one-out at any level


		Map<Integer, Double> results = Parallel.map(new ArrayIterator(queryIds), new Function<Integer, Double>()
		{
		public Double apply(Integer queryId)
			{
			try
				{
				double best = Double.MAX_VALUE;
				for (int targetId : targetIds)
					{
					double d = service.minDistanceBetween(queryId, targetId);
					if (d >= minDistance)
						{
						best = Math.min(best, d);
						}
					}
				return best;
				}
			catch (NoSuchNodeException e)
				{
				throw new Error(e);
				}
			}
		});

		String outfileName = argv[4];
		PrintWriter out = new PrintWriter(outfileName);
		out.println("id\tdist");

		for (Map.Entry<Integer, Double> entry : results.entrySet())
			{
			out.println(entry.getKey() + "\t" + entry.getValue());
			}
		out.close();
		}
	}
