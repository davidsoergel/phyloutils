package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.tree.NoSuchNodeException;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;

public class PhylogenyTypeConverter
	{
	private static final Logger logger = Logger.getLogger(PhylogenyTypeConverter.class);

	/**
	 * Maps String names in the given tree to their corresponding taxids, and returns a tree with Integer ids.
	 *
	 * @param stringTree
	 * @param namer      a NodeNamer with which to generate IDs for nodes that don't have them
	 * @param idMapper   guarantee that the IDs are consistent with those provided by taxonomyService.getTaxidByName
	 * @return
	 */
	public static <T> BasicRootedPhylogeny<T> convertToIDTree(RootedPhylogeny<String> stringTree, NodeNamer<T> namer,
	                                                          TaxonStringIdMapper<T> idMapper,
	                                                          Multimap<String, T> nameToIdMap) //throws PhyloUtilsException
		//    ,Multimap<String, T> nameToIdMap)
		//	throws NcbiTaxonomyException
		{
		if (stringTree.getBasePhylogeny() != null)
			{
			logger.warn("Converting an extracted subtree from String IDs to Integer IDs; base phylogeny gets lost");
			}

		if (stringTree.getParent() != null)
			{
			logger.warn(
					"Rooted phylogeny shouldn't have a parent; dropping it in conversion from String IDs to Integer IDs");
			}

		// this duplicates convertToIntegerIDNode just so we operate on a BasicRootedPhylogeny instead of a PhylogenyNode

		BasicRootedPhylogeny<T> result = new BasicRootedPhylogeny<T>();
		copyValuesToNode(stringTree, result.getSelfNode(), idMapper, nameToIdMap, namer); //, nameToIdMap
		//NodeNamer<T> namer = new IntegerNodeNamer(10000000);

		// name the nodes with null ids.  Note these don't get added to the nameToIdMap.
		result.assignUniqueIds(namer);

		return result;
		}

	private static <T> PhylogenyNode<T> convertToIDNode(PhylogenyNode<String> stringNode,
	                                                    TaxonStringIdMapper<T> idMapper,
	                                                    Multimap<String, T> nameToIdMap,
	                                                    NodeNamer<T> namer) //throws PhyloUtilsException//, Multimap<String, T> nameToIdMap)//throws NcbiTaxonomyException
		{
		PhylogenyNode<T> result = new BasicPhylogenyNode<T>();
		copyValuesToNode(stringNode, result, idMapper, nameToIdMap, namer);//,nameToIdMap);
		return result;
		}

	private static <T> void copyValuesToNode(PhylogenyNode<String> stringNode, PhylogenyNode<T> result,
	                                         TaxonStringIdMapper<T> idMapper, Multimap<String, T> nameToIdMap,
	                                         NodeNamer<T> namer) //throws PhyloUtilsException
		//, Multimap<String, T> nameToIdMap)
		{
		result.setLength(stringNode.getLength());

		result.setWeight(stringNode.getCurrentWeight());


		T id = null;
		String name = stringNode.getValue();
		try
			{
			id = idMapper.findTaxidByName(name);
			}
		catch (NoSuchNodeException e)
			{
			//logger.debug("Integer ID not found for name: " + stringNode.getValue());

			// previously I thought unique ID generation had to happen at the end, I don't know why...
			id = namer.generate(); //nameInternal(unknownCount)
			}
		if (name != null)
			{
			nameToIdMap.put(name, id);
			}
		result.setValue(id);
		//nameToIdMap.put(stringNode.getValue(), id);

		for (PhylogenyNode<String> node : stringNode.getChildren())
			{
			//result.addChild(convertToIntegerIDNode(node));
			convertToIDNode(node, idMapper, nameToIdMap, namer).setParent(result);  //,nameToIdMap
			}
		}
	}