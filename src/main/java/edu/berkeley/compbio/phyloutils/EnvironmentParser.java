/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.AbstractRootedPhylogeny;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.RootedPhylogeny;
import com.davidsoergel.trees.TreeException;
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
			throws IOException, TreeException, NoSuchNodeException
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
				env = HashMultiset.create();
				environmentCounts.put(tokens[1], env);
				}
			env.add(tokens[0], Integer.parseInt(tokens[2]));
			}


		Set<RootedPhylogeny<String>> result = new HashSet<RootedPhylogeny<String>>();
		for (Map.Entry<String, Multiset<String>> entry : environmentCounts.entrySet())
			{
			String name = entry.getKey();
			Multiset<String> ids = entry.getValue();
			RootedPhylogeny<String> subtree = tree.extractTreeWithLeafIDs(ids.elementSet(), false, false,
			                                                              AbstractRootedPhylogeny.MutualExclusionResolutionMode.EXCEPTION);
			subtree.setPayload(name);
			subtree.setLeafWeights(ids);
			result.add(subtree);
			}
		return result;
		}
	}
