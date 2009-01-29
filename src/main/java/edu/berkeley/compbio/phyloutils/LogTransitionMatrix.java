package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.stats.DistributionException;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;


/**
 * A matrix of state-to-state probabilities, in log probability units.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class LogTransitionMatrix//extends TransitionMatrix
	{


	private static final Logger logger = Logger.getLogger(LogTransitionMatrix.class);

	private int states;
	private double[][] logTransitions;

	public String filename;

	public LogTransitionMatrix(String filename) throws FileNotFoundException
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
			r = new FileReader(filename);
			}
		readFromReader(r);
		}

	private void readFromReader(Reader matrix)
		{

		StreamTokenizer input;
		//		StringBuffer buffer = new StringBuffer();
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

			logger.trace("input1 = " + input);

			while (input.ttype != StreamTokenizer.TT_EOL)
				{
				//buffer.append(input.sval.charAt(0));

				input.nextToken();

				logger.trace("input2 = " + input);
				numStates++;
				}

			logger.trace("numStates = " + numStates);

			// Create appropriately-sized matrix

			logTransitions = new double[numStates][numStates];


			while (input.ttype == StreamTokenizer.TT_EOL)
				{
				input.nextToken();
				}

			logger.trace("input3 = " + input);

			// Read in substitution matrix values

			for (i = 0; i < numStates; i++)
				{
				//				buffer.append(input.sval.charAt(0));

				//				logger.debug("input4 = " + input);

				//				input.nextToken();


				for (j = 0; j < numStates; j++)
					{
					logTransitions[i][j] = input.nval;
					input.nextToken();

					logger.trace("input5 = " + input);
					}
				input.nextToken();
				}
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		}

	public LogTransitionMatrix(double[][] probs) throws DistributionException

		{
		states = probs.length;
		logTransitions = probs;// double[states][states];
		for (int i = 0; i < states; i++)
			{
			if (probs[i].length != states)
				{
				throw new DistributionException("Transition matrix must be square");
				}
			//transitions[i] = new MultinomialDistribution(probs[i]);
			}
		}

	/*
	 public int sampleTransition(int fromState) throws DistributionException
		 {
		 return transitions[fromState].sample();
		 }
 */

	public double getTransitionProbability(int e, int g)
		{
		return logTransitions[e][g];
		}

	public double getLogTransitionProbability(int e, int g, Double length)
		{
		// BAD TEMPORARY SIMPLIFICATION: IGNORE BRANCH LENGTH
		return logTransitions[e][g];
		//return getPowerMatrix(length).getTransitionProbability(e, g);
		}

	/*
	 private TransitionMatrix getPowerMatrix(Double length)
		 {
		 return this;
		 }
 */
	}
