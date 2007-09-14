/* $Id$ */

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

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: soergel Date: May 7, 2007 Time: 2:03:39 PM To change this template use File |
 * Settings | File Templates.
 */
public class PhyloUtilsService
	{
	// ------------------------------ FIELDS ------------------------------

	private PhyloUtilsServiceImpl phyloUtilsServiceImpl;


	// --------------------------- CONSTRUCTORS ---------------------------

	public PhyloUtilsService()
		{
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{
				"phyloutils.xml",
				"phyloutils-db.xml"
		});

		// add a shutdown hook for the above context...
		ctx.registerShutdownHook();

		phyloUtilsServiceImpl = ((PhyloUtilsServiceImpl) ctx.getBean("phyloUtilsServiceImpl"));
		}

	// -------------------------- OTHER METHODS --------------------------

	public double exactDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.exactDistanceBetween(speciesNameA, speciesNameB);
		}

	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.exactDistanceBetween(taxIdA, taxIdB);
		}

	public double minDistanceBetween(String speciesNameA, String speciesNameB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.minDistanceBetween(speciesNameA, speciesNameB);
		}

	public double minDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.minDistanceBetween(taxIdA, taxIdB);
		}

	public int nearestKnownAncestor(int taxId) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.nearestKnownAncestor(taxId);
		}

	public int nearestKnownAncestor(String speciesName) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.nearestKnownAncestor(speciesName);
		}

	public Integer commonAncestorID(Integer taxIdA, Integer taxIdB) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.commonAncestorID(taxIdA, taxIdB);
		}

	public Integer commonAncestorID(Set<Integer> mergeIds) throws PhyloUtilsException
		{
		return phyloUtilsServiceImpl.commonAncestorID(mergeIds);
		}
	}
