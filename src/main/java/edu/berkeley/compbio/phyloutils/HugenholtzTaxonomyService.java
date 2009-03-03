package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides a view onto the Hugenholtz taxonomy using Integer IDs.  The Hugenholtz taxonomy has integer ids
 * (prokMSA_ids) at the leaves, but not at internal nodes.  Some internal nodes have string names, but there is no
 * guarantee of uniqueness.  For various reasons we need a tree with unique Integer IDs throughout.  This tree uses the
 * prokMSA_ids at the leaves and generated IDs at internal nodes.  It also allows looking up nodes by String name,
 * including semicolon-delimited multilevel classifications.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HugenholtzTaxonomyService implements TaxonomyService<Integer> //, TaxonomyService<String>
	{
	private static final Logger logger = Logger.getLogger(CiccarelliTaxonomyService.class);

	//private String ciccarelliFilename = "tree_Feb15_unrooted.txt";
	private static final String hugenholtzFilename = "greengenes.all.tree";

	private static HugenholtzTaxonomyService instance;// = new CiccarelliUtils();

	public static HugenholtzTaxonomyService getInstance()
		{
		if (instance == null)
			{
			instance = new HugenholtzTaxonomyService();
			}
		return instance;
		}


	BiMap<Integer, PhylogenyNode<String>> intToNodeMap = new HashBiMap<Integer, PhylogenyNode<String>>();
	Multimap<String, Integer> nameToIdMap = new HashMultimap<String, Integer>();

	TaxonomyService<String> stringTaxonomyService = new NewickTaxonomyService(hugenholtzFilename);

	public HugenholtzTaxonomyService()// throws PhyloUtilsException
		{
		//super(hugenholtzFilename);

		// walk the entire tree, making an int->node map and a string->int multimap along the way

		}


	public Integer findTaxidByName(String name) throws PhyloUtilsException
		{
		Integer result = nameToIdMap.get(name);
		if (result == null)
			{
			try
				{
				Integer id = new Integer(name);
				if (intToNodeMap.containsKey(id))
					{
					return id;
					}
				}
			catch (NumberFormatException e)
				{
				// ok, try the next thing
				}

			if (!name.contains(";"))
				{
				return getUniqueNodeForName(name);
				}

			return getUniqueNodeForMultilevelName(name.split("; "));
			}
		return result;
		}

	private Integer getUniqueNodeForMultilevelName(String[] taxa)
		{
		List<Integer> intTaxa = new ArrayList<Integer>(taxa.length);
		for (String s : taxa)
			{
			Collection<Integer> matchingNodes = nameToIdMap.get(s);

			Integer node =
			if (node == null)
				{
				throw new PhyloUtilsException("Node " + s + " not found in " + name);
				}
			intTaxa.add(node);
			}

		if (!isAncestryList(intTaxa))
			{
			throw new PhyloUtilsException("Requested classification path does not match tree: " + name);
			}
		}


	public Integer getUniqueNodeForName(String name) throws PhyloUtilsException
		{
		Collection<Integer> matchingNodes = nameToIdMap.get(name);
		if (matchingNodes.isEmpty())
			{
			return null;
			}
		if (matchingNodes.size() > 1)
			{
			throw new PhyloUtilsException("Name not unique: " + name);
			}
		return matchingNodes.iterator().next();
		}

	public boolean isDescendant(Integer ancestor, Integer descendant) throws PhyloUtilsException
		{
		return stringTaxonomyService.isDescendant(intToNodeMap.get(ancestor), intToNodeMap.get(descendant));
		}

	public void saveState()
		{
		}

	/*public double exactDistanceBetween(Integer a, Integer b)
		{
		return stringTaxonomyService.distanceBetween(intToNodeMap(a), intToNodeMap(b));
		}
*/
	public Double minDistanceBetween(Integer a, Integer b) throws PhyloUtilsException
		{
		return stringTaxonomyService.minDistanceBetween(intToNodeMap.get(a), intToNodeMap.get(b));
		//	return exactDistanceBetween(name1, name2);
		}


	public Integer nearestAncestorWithBranchLength(Integer id) throws PhyloUtilsException
		{
		return stringTaxonomyService.nearestAncestorWithBranchLength(intToNodeMap.get(id));
		}

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		return stringTaxonomyService
				.extractTreeWithLeafIDs(DSCollectionUtils.mapAll(intToNodeMap, ids), ignoreAbsentNodes);
		}

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws PhyloUtilsException
		{
		return stringTaxonomyService.extractTreeWithLeafIDs(DSCollectionUtils.mapAll(intToNodeMap, ids));
		}
	}
