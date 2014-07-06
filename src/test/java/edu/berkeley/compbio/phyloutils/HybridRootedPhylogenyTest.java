/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.TestInstanceFactory;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.trees.AbstractRootedPhylogeny;
import com.davidsoergel.trees.BasicRootedPhylogenyTest;
import com.davidsoergel.trees.RootedPhylogeny;
import com.davidsoergel.trees.TaxonMergingPhylogenyInterfaceTest;
import org.testng.annotations.Factory;

import java.util.Queue;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: HybridRootedPhylogenyTest.java 47 2008-09-12 02:25:46Z soergel $
 */

public class HybridRootedPhylogenyTest extends ContractTestAware<HybridRootedPhylogeny>
		implements TestInstanceFactory<HybridRootedPhylogeny>
	{
	//private static final NcbiTaxonomyService ncbiTaxonomyService = NcbiTaxonomyService.getInstance();
	//private static final CiccarelliUtils ciccarelli = CiccarelliUtils.getInstance();

	//	private HybridRootedPhylogeny<Integer> hybridTree;

	/*@Test
	public void nearestKnownAncestorWorks() throws PhyloUtilsException
		{
		hybridTree = new HybridRootedPhylogeny(ciccarelli.getTree(), ncbiTaxonomyService);
		assert hybridTree.nearestKnownAncestor("Vibrio cholerae O1 biovar eltor str. N16961") == 666; //243277)
		}*/

	public HybridRootedPhylogeny createInstance() throws Exception
		{
		RootedPhylogeny leafPhylogeny =
				new BasicRootedPhylogenyTest.BasicRootedPhylogenyWithSpecificNodeHandles().getRootPhylogeny();

		//List<String> leafList = Arrays.asList("a", "aa", "bb", "bbba", "ba", "baa", "c", "ca", "cb"); // can't include internal nodes
		Set<String> leafList = DSCollectionUtils.setOf("aa", "bbba", "baa", "ca", "cb");
		RootedPhylogeny rootPhylogeny = leafPhylogeny.extractTreeWithLeafIDs(leafList, false, false,
		                                                                     AbstractRootedPhylogeny.MutualExclusionResolutionMode.EXCEPTION);

		leafPhylogeny.setAllBranchLengthsTo(null);

		return new HybridRootedPhylogeny(rootPhylogeny, leafPhylogeny);
		}

	@Override
	public void addContractTestsToQueue(Queue theContractTests)
		{
		theContractTests.add(new TaxonMergingPhylogenyInterfaceTest<HybridRootedPhylogeny>(this));
		}

	@Factory
	public Object[] instantiateAllContractTests()
		{
		return super.instantiateAllContractTestsWithName(HybridRootedPhylogeny.class.getCanonicalName());
		}
	}
