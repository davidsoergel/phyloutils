/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
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
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.tree.TreeException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class CiccarelliUtils
	{
	private static final Logger logger = Logger.getLogger(CiccarelliUtils.class);

	private RootedPhylogeny<String> ciccarelliTree;
	//private String ciccarelliFilename = "tree_Feb15_unrooted.txt";
	private String ciccarelliFilename = "itol080605_newick.txt";

	private static CiccarelliUtils instance;// = new CiccarelliUtils();


	public static CiccarelliUtils getInstance()
		{
		if (instance == null)
			{
			instance = new CiccarelliUtils();
			}
		return instance;
		}


	public CiccarelliUtils()// throws PhyloUtilsException
		{
		try
			{
			/*		URL res = ClassLoader.getSystemResource(ciccarelliFilename);
						if (res == null)
							{
							logger.error("Ciccarelli tree not found: " + ciccarelliFilename);
							//Get the System Classloader
							ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

							//Get the URLs
							URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

							for (int i = 0; i < urls.length; i++)
								{
								logger.warn(urls[i].getFile());
								}

							return;
							}
						InputStream is = res.openStream();*/
			/*if (is == null)
				{
				is = new FileInputStream(filename);
				}*/
			//	ciccarelliTree = new NewickParser<String>().read(is, new StringNodeNamer("UNNAMED NODE "));
			ciccarelliTree = NewickParser.readWithStringIds(ciccarelliFilename);
			}
		catch (IOException e)
			{
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			logger.error(e);
			}
		catch (PhyloUtilsException e)
			{
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			logger.error(e);
			}
		}

	/*	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		 {
		 return ciccarelliTree.distanceBetween(taxIdA, taxIdB);
		 }
 */
	public double exactDistanceBetween(String a, String b)
		{
		return ciccarelliTree.distanceBetween(a, b);
		}

	public RootedPhylogeny<String> extractTreeWithLeafIDs(Collection<String> ids) throws PhyloUtilsException
		{
		return ciccarelliTree.extractTreeWithLeafIDs(ids);
		}

	public RootedPhylogeny<String> getTree()
		{
		return ciccarelliTree;
		}

	public RootedPhylogeny<String> getRandomSubtree(int numTaxa, Double ciccarelliMergeThreshold)
			throws PhyloUtilsException, TreeException
		{
		Map<String, Set<String>> mergeIdSets =
				TaxonMerger.merge(ciccarelliTree.getLeafValues(), ciccarelliTree, ciccarelliMergeThreshold);
		Set<String> mergedIds = mergeIdSets.keySet();
		DSCollectionUtils.retainRandom(mergedIds, numTaxa);
		return ciccarelliTree.extractTreeWithLeafIDs(mergedIds);
		}
	}
