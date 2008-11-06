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

import java.util.Collection;


/**
 * A phylogenetic tree supporting the extraction of subtrees and ignoring branches with no length
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @JavadocOK
 */
public interface TaxonMergingPhylogeny<T>//extends RootedPhylogeny<T>
	{
	/**
	 * Locate the node with the given id, and navigate up the tree if necessary) until a node is found that has a branch
	 * length greater than zero.
	 *
	 * @param id the T identifying the starting node
	 * @return the T identifying the most recent ancestor of the given node with a nonzero branch length (perhaps the node
	 *         itself)
	 * @throws PhyloUtilsException when the target node is not found in the tree, or when no node with a branch length is
	 *                             found
	 */
	T nearestAncestorWithBranchLength(T id) throws PhyloUtilsException;

	/**
	 * Extract a tree which contains exactly those leaves that are requested.  I.e., prunes any branches not leading to
	 * those leaves.  Aggregates chains of nodes with exactly one child each into a single branch of the appropriate
	 * length.  Creates the extracted tree from newly instantiated nodes; does not reuse nodes from the base tree.
	 *
	 * @param ids the Collection<T> of leaves desired for the extracted tree
	 * @return the extracted RootedPhylogeny<T>
	 * @throws PhyloUtilsException when the given collection contains a node id that is not found in the tree
	 */
	RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids) throws PhyloUtilsException;

	/**
	 * Extract a tree which contains exactly those leaves that are requested.  I.e., prunes any branches not leading to
	 * those leaves.  Aggregates chains of nodes with exactly one child each into a single branch of the appropriate
	 * length.  Creates the extracted tree from newly instantiated nodes; does not reuse nodes from the base tree.
	 *
	 * @param ids               the Collection<T> of leaves desired for the extracted tree
	 * @param ignoreAbsentNodes silently ignore requests for leaves that are not present in the tree, simply returning the
	 *                          extracted tree with those leaves that are found.  I.e., intersect the requested leaf id
	 *                          list with the available leaf ids before constructing the result tree.
	 * @return the extracted RootedPhylogeny<T>
	 * @throws PhyloUtilsException when the given collection contains a node id that is not found in the tree and
	 *                             ignoreAbsentNodes is false
	 */
	RootedPhylogeny<T> extractTreeWithLeafIDs(Collection<T> ids, boolean ignoreAbsentNodes) throws PhyloUtilsException;
	}
