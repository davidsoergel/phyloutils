package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.MultinomialDistribution;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A matrix of state-to-state probabilities, in probability units.
 *
 * @author lorax
 * @version 1.0
 * @see LogOddsSubstitutionMatrix
 */
public class TransitionMatrix
	{
	private static Logger logger = Logger.getLogger(TransitionMatrix.class);

	private int states;
	private MultinomialDistribution[] transitions;

	public String filename;

	public TransitionMatrix(String filename) throws FileNotFoundException
		{
		this.filename = filename;
		init();
		}

	public void init() throws FileNotFoundException
		{
		Reader r;
		try
			{
			r = new InputStreamReader(getClass().getResourceAsStream(filename));
			}
		catch (Exception e)
			{
			//  logger.error(e);
			//  e.printStackTrace();
			r = new FileReader(filename);
			}
		readFromReader(r);
		}

	private void readFromReader(Reader r)
		{
		throw new NotImplementedException();
		}

	public TransitionMatrix(double[][] probs) throws DistributionException

		{
		states = probs.length;
		transitions = new MultinomialDistribution[states];
		for (int i = 0; i < states; i++)
			{
			if (probs[i].length != states)
				{
				throw new DistributionException("Transition matrix must be square");
				}
			transitions[i] = new MultinomialDistribution(probs[i]);
			}
		}

	public int sampleTransition(int fromState) throws DistributionException
		{
		return transitions[fromState].sample();
		}


	public double getTransitionProbability(int e, int g)
		{
		return transitions[e].getProbs()[g];
		}

	public double getTransitionProbability(int e, int g, Double length)
		{
		//** TEMPORARY SIMPLIFICATION: IGNORE BRANCH LENGTH
		return transitions[e].getProbs()[g];
		//return getPowerMatrix(length).getTransitionProbability(e, g);
		}

	/*
	 private TransitionMatrix getPowerMatrix(Double length)
		 {
		 return this;
		 }
 */
	}
