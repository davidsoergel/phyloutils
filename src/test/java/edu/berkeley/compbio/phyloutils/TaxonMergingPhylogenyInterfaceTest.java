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

import com.davidsoergel.dsutils.TestInstanceFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Tests instances of TaxonMergingPhylogeny.  The instances created by the provided factory must have certain properties
 * which we'll then test for; see the comments on each test.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public abstract class TaxonMergingPhylogenyInterfaceTest
	{
	private TestInstanceFactory<? extends TaxonMergingPhylogeny<String>> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public TaxonMergingPhylogenyInterfaceTest(TestInstanceFactory<? extends TaxonMergingPhylogeny<String>> tif)
		{
		this.tif = tif;
		}

	@Test
	public void findsNearestAncestorWithBranchLength() throws Exception
		{
		TaxonMergingPhylogeny tmp = tif.createInstance();
		assert tmp.nearestAncestorWithBranchLength("NoBranchLength").equals("HasBranchLength");
		}

	@Test
	public void extractsTreeCorrectlyGivenBaseLeaves() throws Exception
		{
		TaxonMergingPhylogeny<String> tmp = tif.createInstance();
		Collection<String> leafIDs = Arrays.asList(new String[]{
				"ExtractLeaf1",
				"ExtractLeaf2",
				"ExtractLeaf3",
				"ExtractLeaf4"
		});

		RootedPhylogeny<String> result = tmp.extractTreeWithLeafIDs(leafIDs);

		assert result.getNodes().size() == 7;
		assert false;
		}

	@Test
	public void extractsTreeCorrectlyGivenInternalLeaves() throws Exception
		{
		TaxonMergingPhylogeny<String> tmp = tif.createInstance();
		Collection<String> leafIDs = Arrays.asList(new String[]{
				"ExtractInternalLeaf1",
				"ExtractInternalLeaf2",
				"ExtractInternalLeaf3",
				"ExtractInternalLeaf4"
		});

		RootedPhylogeny<String> result = tmp.extractTreeWithLeafIDs(leafIDs);

		assert result.getNodes().size() == 7;
		assert false;
		}

	@Test(expectedExceptions = NoSuchElementException.class)
	public void treeExtractionThrowsExceptionOnLeafNotFound() throws Exception
		{
		TaxonMergingPhylogeny<String> tmp = tif.createInstance();
		Collection<String> leafIDs = Arrays.asList(new String[]{
				"ExtractInternalLeaf1",
				"ExtractInternalLeaf2",
				"Not Present Node",
				"ExtractInternalLeaf4"
		});

		RootedPhylogeny<String> result = tmp.extractTreeWithLeafIDs(leafIDs);
		}
	}
