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

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class CiccarelliUtils
	{
	private static final Logger logger = Logger.getLogger(CiccarelliUtils.class);

	private RootedPhylogeny<Integer> ciccarelliTree;
	private String ciccarelliFilename = "tree_Feb15_unrooted.txt";


	public CiccarelliUtils()// throws PhyloUtilsException
		{
		try
			{

			URL res = ClassLoader.getSystemResource(ciccarelliFilename);
			InputStream is = res.openStream();
			/*if (is == null)
				{
				is = new FileInputStream(filename);
				}*/
			ciccarelliTree = new NewickParser<Integer>().read(is, new IntegerNodeNamer(100000000));
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

	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		return ciccarelliTree.distanceBetween(taxIdA, taxIdB);
		}



	public RootedPhylogeny<Integer> extractTreeWithLeaves(Set<Integer> ids)
		{
		return ciccarelliTree.extractTreeWithLeaves(ids);
		}
	}
