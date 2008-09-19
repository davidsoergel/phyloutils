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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads an environment file in the UniFrac format (http://bmf2.colorado.edu/unifrac/tutorial.psp), matching the taxon
 * names against nodes in the given tree, and provides the resulting communities as weighted RootedPhylogenies.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class EnvironmentParser
	{

	public static Collection<RootedPhylogeny<String>> read(InputStream is, RootedPhylogeny<String> tree)
			throws PhyloUtilsException, IOException
		{
		BufferedReader r = new BufferedReader(new InputStreamReader(is));

		Map<String, Multiset<String>> environmentCounts = new HashMap<String, Multiset<String>>();

		String line;
		while ((line = r.readLine()) != null)
			{
			String[] tokens = line.split(" ");
			Multiset<String> env = environmentCounts.get(tokens[1]);
			if (env == null)
				{
				env = new HashMultiset<String>();
				environmentCounts.put(tokens[1], env);
				}
			env.add(tokens[0], Integer.parseInt(tokens[2]));
			}


		Set<RootedPhylogeny<String>> result = new HashSet<RootedPhylogeny<String>>();
		for (Map.Entry<String, Multiset<String>> entry : environmentCounts.entrySet())
			{
			String name = entry.getKey();
			Multiset<String> ids = entry.getValue();
			RootedPhylogeny<String> subtree = tree.extractTreeWithLeafIDs(ids);
			subtree.setValue(name);
			subtree.setLeafWeights(ids);
			result.add(subtree);
			}
		return result;
		}
	}
