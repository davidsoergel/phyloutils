package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.file.IntArrayReader;
import com.davidsoergel.trees.NoSuchNodeException;

import java.io.IOException;
import java.io.PrintWriter;

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

		int[] targetIds = IntArrayReader.read(argv[1]);
		int[] queryIds = IntArrayReader.read(argv[2]);
		double minDistance = Double.parseDouble(argv[3]);  // implement leave-one-out at any level
		String outfileName = argv[4];
		PrintWriter out = new PrintWriter(outfileName);

		out.println("id\tdist");

		for (int queryId : queryIds)
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
			out.println(queryId + "\t" + best);
			}

		out.close();
		}
	}
