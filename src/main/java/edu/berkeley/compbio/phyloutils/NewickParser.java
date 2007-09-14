/*
 * Copyright (c) 2007 Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.compbio.phyloutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

/* $Id$ */

/**
 * Parser for New Hampshire (aka Newick) tree files.  Does not yet handle quoted labels or NHX extensions.  Simple state
 * machine implementation.
 *
 * @Author David Soergel
 * @Version 1.0
 */
public class NewickParser<T>
	{
	private static enum State
		{
			NEWNODE, NAME, POST_CHILDREN, EXPECTING_NUMBER, NODEEND, EOF, FINISHED
		}

	;


	public RootedPhylogeny<T> read(InputStream is, NodeNamer<T> namer) throws PhyloUtilsException
		{
		Reader r = new BufferedReader(new InputStreamReader(is));
		StreamTokenizer st = new StreamTokenizer(r);

		st.lowerCaseMode(false);
		st.eolIsSignificant(false);
		st.slashSlashComments(false);
		st.slashStarComments(false);
		st.wordChars('_', '_');
		st.wordChars('-', '-');

		RootedPhylogeny<T> theTree = new RootedPhylogeny();
		PhylogenyNode<T> currentNode = theTree;
		//List<PhylogenyNode> path = new LinkedList<PhylogenyNode>();
		//path.add(currentNode);

		State state = State.NEWNODE;

		try
			{
			st.nextToken();
			if (!((char) st.ttype == '('))
				{
				throw new PhyloUtilsException("Tree must begin with an open parenthesis");
				}

			currentNode = new PhylogenyNode(currentNode);

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
							currentNode
									.appendToName((int) st.nval,
									              namer);// handle labels with integers in them, but not doubles
							state = State.NAME;
							}
						else if (state == State.POST_CHILDREN)
							{
							currentNode.setBootstrap(st.nval);
							state = State.POST_CHILDREN;// unchanged
							}
						else
							{
							throw new PhyloUtilsException(
									"Number " + st.nval + " in an unexpected place at line " + st.lineno());
							}
						break;

					case StreamTokenizer.TT_WORD:
						if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode.appendToName(st.sval, namer);
							state = State.NAME;
							}
						else
							{
							throw new PhyloUtilsException(
									"String " + st.sval + " in an unexpected place at line " + st.lineno());
							}
						break;

					case'(':
						if (state == State.NEWNODE || state == State.NAME)
							{
							currentNode = new PhylogenyNode(currentNode);
							state = State.NEWNODE;
							}
						else
							{
							throw new PhyloUtilsException("Open paren in an unexpected place at line " + st.lineno());
							}
						break;

					case')':
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

					case',':
						if (state == State.NAME || state == State.POST_CHILDREN || state == State.NODEEND)
							{
							currentNode = new PhylogenyNode(currentNode.getParent());
							state = State.NEWNODE;
							}
						else
							{
							throw new PhyloUtilsException("Comma in an unexpected place at line " + st.lineno());
							}
						break;

					case':':
						if (state == State.NAME || state == State.POST_CHILDREN)
							{
							state = State.EXPECTING_NUMBER;
							}
						else
							{
							throw new PhyloUtilsException("Colon in an unexpected place at line " + st.lineno());
							}
						break;

					case';':
						if (currentNode != theTree)
							{
							throw new PhyloUtilsException("Premature end of tree at " + st.lineno());
							}
						state = State.FINISHED;
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

		theTree.updateNodes(namer);
		return theTree;
		}
	}
