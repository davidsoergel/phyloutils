package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.MultinomialDistribution;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * A matrix of state-to-state probabilities, in probability units.
 *
 * @author lorax
 * @version 1.0
 * @see LogOddsSubstitutionMatrix
 */
public class TransitionMatrix
	{
	private static final Logger logger = Logger.getLogger(TransitionMatrix.class);

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

	private void readFromReader(Reader matrix)
		{

		StreamTokenizer input;
		//	StringBuffer buffer = new StringBuffer();
		int i, j, numStates = 0;
		input = new StreamTokenizer(matrix);
		try
			{
			// Read in residue names

			input.commentChar('#');
			input.wordChars('*', '*');
			input.eolIsSignificant(true);
			input.nextToken();

			while (input.ttype == StreamTokenizer.TT_EOL)
				{
				input.nextToken();
				}

			logger.debug("input1 = " + input);

			while (input.ttype != StreamTokenizer.TT_EOL)
				{
				//buffer.append(input.sval.charAt(0));

				input.nextToken();

				logger.debug("input2 = " + input);
				numStates++;
				}

			logger.debug("numStates = " + numStates);

			// Create appropriately-sized matrix

			transitions = new MultinomialDistribution[numStates];
			for (int k = 0; k < numStates; k++)
				{
				transitions[k] = new MultinomialDistribution();
				}


			while (input.ttype == StreamTokenizer.TT_EOL)
				{
				input.nextToken();
				}

			logger.debug("input3 = " + input);

			// Read in substitution matrix values

			for (i = 0; i < numStates; i++)
				{
				//				buffer.append(input.sval.charAt(0));

				//				logger.debug("input4 = " + input);

				//				input.nextToken();


				for (j = 0; j < numStates; j++)
					{
					transitions[i].add(input.nval);
					input.nextToken();

					logger.debug("input5 = " + input);
					}
				input.nextToken();
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			logger.debug(e);
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			e.printStackTrace();
			}
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
		// BAD TEMPORARY SIMPLIFICATION: IGNORE BRANCH LENGTH
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
