package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.stats.DistributionException;

import java.io.FileNotFoundException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public class LogTransitionMatrix extends TransitionMatrix
	{
	public LogTransitionMatrix(String filename) throws FileNotFoundException
		{
		super(filename);
		}

	public LogTransitionMatrix(double[][] logprobs) throws DistributionException
		{
		super(logprobs);
		}

	public double getLogTransitionProbability(int e, int g, Double length)
		{
		//** TEMPORARY SIMPLIFICATION: IGNORE BRANCH LENGTH
		return getTransitionProbability(e, g, length);
		}
	}
