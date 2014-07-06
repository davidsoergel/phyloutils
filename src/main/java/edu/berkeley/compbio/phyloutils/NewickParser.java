/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.BasicPhylogenyNode;
import com.davidsoergel.trees.BasicRootedPhylogeny;
import com.davidsoergel.trees.IntegerGeneratingNodeNamer;
import com.davidsoergel.trees.IntegerNodeNamer;
import com.davidsoergel.trees.NodeNamer;
import com.davidsoergel.trees.StringIntegerNodeNamer;
import com.davidsoergel.trees.StringNodeNamer;
import com.davidsoergel.trees.TreeException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;


/**
 * Parser for New Hampshire (aka Newick) tree files.  Does not yet handle quoted labels or NHX extensions.  Simple state
 * machine implementation.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public class NewickParser<T extends Serializable>
	{
	private static final Logger logger = Logger.getLogger(NewickParser.class);
	// -------------------------- OTHER METHODS --------------------------

	public static void main(String[] argv)
		{
		// add integer ids to unknown nodes

		try
			{
			InputStream is = getInputStream(argv[0]);
			boolean namedNodesMustBeLeaves = Boolean.parseBoolean(argv[1]);
			BasicRootedPhylogeny<String> theTree = new NewickParser<String>()
					.read(is, new StringIntegerNodeNamer("", false, 10000000, namedNodesMustBeLeaves));

			String prefix = argv[2];
			String tab = argv[3];
			prefix = prefix.replaceAll(Matcher.quoteReplacement("\\n"), "\n");
			prefix = prefix.replaceAll(Matcher.quoteReplacement("\\t"), "\t");
			prefix = prefix.replaceAll("\"", "");
			prefix = prefix.replaceAll("\'", "");
			tab = tab.replaceAll(Matcher.quoteReplacement("\\n"), "\n");
			tab = tab.replaceAll(Matcher.quoteReplacement("\\t"), "\t");
			tab = tab.replaceAll("\"", "");
			tab = tab.replaceAll("\'", "");

			//StringBuffer sb = new StringBuffer();

			//	System.out.println(sb);

			//FileOutputStream foo = new FileOutputStream(argv[2]);
			FileWriter fw = new FileWriter(argv[4]);
			theTree.toNewick(fw, prefix, tab, 0, 0);
			//fw.write(sb.toString());
			fw.close();
			}
		catch (TreeException e)
			{
			logger.error("Error", e);
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		}

	public static BasicRootedPhylogeny<String> readWithStringIds(String filename, boolean namedNodesMustBeLeaves)
			throws TreeException, IOException
		{
		InputStream is = getInputStream(filename);

		return new NewickParser<String>().read(is, new StringNodeNamer("UNNAMED ", false, namedNodesMustBeLeaves));
		}

	public static BasicRootedPhylogeny<Integer> readWithIntegerIds(String filename, boolean generateIds,
	                                                               boolean namedNodesMustBeLeaves)
			throws TreeException, IOException
		{
		InputStream is = getInputStream(filename);

		NodeNamer<Integer> namer =
				generateIds ? new IntegerGeneratingNodeNamer(10000000, namedNodesMustBeLeaves) : new IntegerNodeNamer();
		return new NewickParser<Integer>().read(is, namer);
		}

	private static InputStream getInputStream(String filename) throws PhyloUtilsException, IOException
		{
		//ClassLoader classClassLoader = new NewickParser().getClass().getClassLoader();
		ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
		//ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

		//URL res1 = classClassLoader.getResource(filename);
		URL res = threadClassLoader.getResource(filename);
		//URL res3 = systemClassLoader.getResource(filename);

		if (res == null)
			{
			File f = new File(filename);
			if (f.exists())
				{
				res = f.toURI().toURL();//new URL("file://" + filename);
				}
			}

		if (res == null)
			{
			logger.error("tree not found: " + filename);
			//Get the System Classloader
			//ClassLoader.getSystemClassLoader();

			//Get the URLs
			URL[] urls = ((URLClassLoader) threadClassLoader).getURLs();

			for (int i = 0; i < urls.length; i++)
				{
				logger.warn(urls[i].getFile());
				}

			throw new PhyloUtilsException("tree not found: " + filename);
			}
		InputStream is = res.openStream();

		is = filename.endsWith(".gz") ? new GZIPInputStream(is) : is;
		/*if (is == null)
					 {
					 is = new FileInputStream(filename);
					 }*/
		return is;
		}

	public BasicRootedPhylogeny<T> read(InputStream is, NodeNamer<T> namer) throws TreeException
		{
		Reader r = new BufferedReader(new InputStreamReader(is));
		StreamTokenizer st = new StreamTokenizer(r);

		st.lowerCaseMode(false);
		st.eolIsSignificant(false);
		st.slashSlashComments(false);
		st.slashStarComments(false);
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('/', '/');


		// allow = within node names for the sake of synonym1==synonym2
		st.wordChars('=', '=');

		BasicRootedPhylogeny<T> theTree = new BasicRootedPhylogeny<T>();
		BasicPhylogenyNode<T> currentNode = theTree.getRoot();
		//List<PhylogenyNode> path = new LinkedList<PhylogenyNode>();
		//path.add(currentNode);

		State state = State.NEWNODE;
		State prevState = null;// only used for comments

		try
			{
			st.nextToken();

			// allow whitespace before the tree
			//while (((char) st.ttype == '['))

			// allow comments before the tree
			while (((char) st.ttype == '['))
				{
				do
					{
					st.nextToken();
					}
				while ((((char) st.ttype != ']')));
				st.nextToken();
				}

			if (!((char) st.ttype == '('))
				{
				throw new PhyloUtilsException(
						"Tree must begin with an open parenthesis; found '" + (char) st.ttype + "''.");
				}

			currentNode = new BasicPhylogenyNode(currentNode);

			/*	boolean eof = false;
						boolean finished = false;
						boolean expectingNumber = false;*/

			while (!(state == State.EOF))
				{
				switch (st.nextToken())
					{
					case StreamTokenizer.TT_EOF:
						if (!(state == State.FINISHED))
							{
							throw new PhyloUtilsException("Premature end of tree at " + st.lineno());
							}
						state = State.EOF;
						break;

					case StreamTokenizer.TT_EOL:
						continue;

					case StreamTokenizer.TT_NUMBER:
						if (state == State.EXPECTING_NUMBER)
							{
							currentNode.setLength(st.nval);
							state = State.NODEEND;
							}
						else if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode.appendToValue((int) st.nval,
							                          namer);// handle labels with integers in them, but not doubles
							state = State.NAME;
							}
						else if (state == State.POST_CHILDREN)
							{
							//currentNode.setBootstrap(st.nval);
							currentNode.appendToValue((int) st.nval, namer);
							state = State.POST_CHILDREN;// unchanged
							}
						else if (state == State.COMMENT)
							{
							//** we ignored this previously...
							currentNode.setBootstrap(st.nval);
							}
						else
							{
							throw new PhyloUtilsException(
									"Number " + st.nval + " in an unexpected place at line " + st.lineno());
							}
						break;

					//** note underscore vs space issues here... elsewhere, space is a legitimate character, different from _
					// but in the ITOL newick files at least, all underscores are really spaces


					case StreamTokenizer.TT_WORD:
						if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode.appendToValue(st.sval.replace('_', ' '), namer);
							state = State.NAME;
							}
						else if (state == State.POST_CHILDREN)
							{
							currentNode.appendToValue(st.sval.replace('_', ' '), namer);
							state = State.POST_CHILDREN;
							}
						else if (state == State.COMMENT)
							{
							// ignore
							}
						else
							{
							throw new PhyloUtilsException(
									"String " + st.sval + " in an unexpected place at line " + st.lineno());
							}
						break;

					case '<':
						if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode.appendToValue("<", namer);
							state = State.NAME;
							}
						else if (state == State.POST_CHILDREN)
							{
							currentNode.appendToValue("<", namer);
							state = State.POST_CHILDREN;
							}
						else if (state == State.COMMENT)
							{
							// ignore
							}
						else
							{
							throw new PhyloUtilsException("String < in an unexpected place at line " + st.lineno());
							}
						break;


					case '>':
						if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode.appendToValue(">", namer);
							state = State.NAME;
							}
						else if (state == State.POST_CHILDREN)
							{
							currentNode.appendToValue(">", namer);
							state = State.POST_CHILDREN;
							}
						else if (state == State.COMMENT)
							{
							// ignore
							}
						else
							{
							throw new PhyloUtilsException("String > in an unexpected place at line " + st.lineno());
							}
						break;

					case '(':
						if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode = new BasicPhylogenyNode(currentNode);
							state = State.NEWNODE;
							}
						else
							{
							throw new PhyloUtilsException("Open paren in an unexpected place at line " + st.lineno());
							}
						break;

					case ')':
						if (state == State.NAME || state == State.POST_CHILDREN || state == State.NODEEND)
							{
							currentNode = currentNode.getParent();
							state = State.POST_CHILDREN;
							}
						else
							{
							throw new PhyloUtilsException("Close paren in an unexpected place at line " + st.lineno());
							}
						break;

					case ',':
						if (state == State.NAME || state == State.POST_CHILDREN || state == State.NODEEND)
							{
							currentNode = new BasicPhylogenyNode(currentNode.getParent());
							state = State.NEWNODE;
							}
						else
							{
							throw new PhyloUtilsException("Comma in an unexpected place at line " + st.lineno());
							}
						break;

					case ':':
						if (state == State.NAME || state == State.POST_CHILDREN)
							{
							state = State.EXPECTING_NUMBER;
							}
						else
							{
							throw new PhyloUtilsException("Colon in an unexpected place at line " + st.lineno());
							}
						break;

					case ';':
						if (currentNode != theTree.getRoot())
							{
							throw new PhyloUtilsException("Premature end of tree at " + st.lineno());
							}
						state = State.FINISHED;
						break;

					case '[':
						prevState = state;
						state = State.COMMENT;
						break;

					case ']':
						if (state == State.COMMENT)
							{
							state = prevState;// likely NODEEND anyway
							}
						else
							{
							throw new PhyloUtilsException("End comment in an unexpected place at line " + st.lineno());
							}
						break;


					case '\'':
						// ignore single quotes entirely
						break;

					case '\"':
						// ignore double quotes entirely
						break;

					case '*':
						// ignore asterisks entirely
						break;

					default:
						throw new PhyloUtilsException(
								"Illegal character " + (char) st.ttype + " at line " + st.lineno());
					}
				}
			}
		catch (IOException e)
			{
			throw new PhyloUtilsException(e, "Could not read Newick tree at line " + st.lineno());
			}

		// don't do unnecessary work
		// it's premature, too, if we're going to reset the branch lengths anyway
		/*
		logger.info("read tree with maximum branch-length depth " + theTree.getGreatestBranchLengthDepthBelow()
		            + ", maximum span " + theTree.getLargestLengthSpan() + ", and maximum node depth " + theTree
				.getGreatestNodeDepthBelow());
		*/

		theTree.assignUniqueIds(namer);
		return theTree;
		}

	// -------------------------- ENUMERATIONS --------------------------

	private static enum State
		{
			NEWNODE, NAME, POST_CHILDREN, EXPECTING_NUMBER, NODEEND, EOF, FINISHED, COMMENT
		}
	}
