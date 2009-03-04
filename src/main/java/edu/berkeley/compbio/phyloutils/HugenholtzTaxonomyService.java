package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.DSStringUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
	Multimap<String, PhylogenyNode<String>> nameToNodeMap = new HashMultimap<String, PhylogenyNode<String>>();

	NewickTaxonomyService stringTaxonomyService = new NewickTaxonomyService(hugenholtzFilename);

	public HugenholtzTaxonomyService() //throws PhyloUtilsException
		{
		//super(hugenholtzFilename);

		// walk the entire tree, making an int->node map and a string->int multimap along the way

		// assume that all prokMSA_IDs are less than 10000000, so just start the generated IDs from there

		int idGenerator = 10000000;

		for (PhylogenyNode<String> node : stringTaxonomyService.getRoot())
			{
			String stringName = node.getValue();
			Integer id;
			try
				{
				/*
				if (stringName == null)
					{
					throw new NumberFormatException("");
					}

				stringName = stringName.trim();
				*/

				id = new Integer(stringName);
				if (intToNodeMap.containsKey(id))
					{
					throw new PhyloUtilsRuntimeException("Found duplicate prokMSA_id: " + id);
					}
				}
			catch (NumberFormatException e)
				{
				// ok, generate an ID instead
				id = idGenerator++;
				}

			intToNodeMap.put(id, node);

			if (stringName != null && stringName.trim().equals(""))
				{
				nameToNodeMap.put(stringName.trim(), node);
				}

			// note we don't put the string representation of the integer id in the string map
			}
		}


	public Integer findTaxidByName(String name) throws PhyloUtilsException
		{
		//Integer result = null; // could have secondary cache // getUniqueNodeForName(name); //nameToIdMap.get(name);
		//if (result == null)
		//	{
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
		//	}
		//return result;
		}

	private Integer getUniqueNodeForMultilevelName(String[] taxa) throws PhyloUtilsException
		{
		List<Integer> intTaxa = new ArrayList<Integer>(taxa.length);
		PhylogenyNode<String> trav = stringTaxonomyService.getRoot();
		for (String s : taxa)
			{
			Collection<PhylogenyNode<String>> matchingNodes = nameToNodeMap.get(s);

			if (matchingNodes.isEmpty())
				{
				throw new PhyloUtilsException("Node " + s + " not found in " + DSStringUtils.join(taxa, "; "));
				}

			for (Iterator<PhylogenyNode<String>> iter = matchingNodes.iterator(); iter.hasNext();)
				{
				PhylogenyNode<String> node = iter.next();

				if (!stringTaxonomyService.isDescendant(trav, node))
					{
					iter.remove();
					}
				}

			if (matchingNodes.isEmpty())
				{
				throw new PhyloUtilsException(
						"Requested classification path does not match tree: " + DSStringUtils.join(taxa, "; "));
				}

			if (matchingNodes.size() == 1)
				{
				trav = matchingNodes.iterator().next();
				}
			else
				{
				for (PhylogenyNode<String> ancestor : matchingNodes)
					{
					for (Iterator<PhylogenyNode<String>> iter = matchingNodes.iterator(); iter.hasNext();)
						{
						PhylogenyNode<String> descendant = iter.next();

						if (stringTaxonomyService.isDescendant(ancestor, descendant))
							{
							iter.remove();
							}
						}
					}
				}
			if (matchingNodes.size() == 1)
				{
				trav = matchingNodes.iterator().next();
				}
			else
				{
				throw new PhyloUtilsException(
						"Node " + s + " not unique at " + trav + " in " + DSStringUtils.join(taxa, "; "));
				}
			}

		return intToNodeMap.inverse().get(trav);
		}


	public Integer getUniqueNodeForName(String name) throws PhyloUtilsException
		{
		Collection<PhylogenyNode<String>> matchingNodes = nameToNodeMap.get(name);
		if (matchingNodes.isEmpty())
			{
			return null;
			}
		if (matchingNodes.size() > 1)
			{
			throw new PhyloUtilsException("Name not unique: " + name);
			}
		return intToNodeMap.inverse().get(matchingNodes.iterator().next());
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
		return intToNodeMap.inverse().get(intToNodeMap.get(id).nearestAncestorWithBranchLength());
		}

	/*
	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		return stringTaxonomyService
				.extractTreeWithLeaves(DSCollectionUtils.mapAll(intToNodeMap, ids), ignoreAbsentNodes);
		}

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws PhyloUtilsException
		{
		return stringTaxonomyService.extractTreeWithLeaves(DSCollectionUtils.mapAll(intToNodeMap, ids));
		}
*/

	public boolean isDescendant(PhylogenyNode<Integer> ancestor, PhylogenyNode<Integer> descendant)
			throws PhyloUtilsException
		{
		throw new NotImplementedException();
		}

	public Double minDistanceBetween(PhylogenyNode<Integer> node1, PhylogenyNode<Integer> node2)
			throws PhyloUtilsException
		{
		throw new NotImplementedException();
		}

	public PhylogenyNode<Integer> getRoot()
		{
		throw new NotImplementedException();
		}

	public PhylogenyNode<Integer> nearestAncestorWithBranchLength(PhylogenyNode<Integer> id) throws PhyloUtilsException
		{
		throw new NotImplementedException();
		}

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids) throws PhyloUtilsException
		{
		throw new NotImplementedException();
		}

	public RootedPhylogeny<Integer> extractTreeWithLeaves(Collection<PhylogenyNode<Integer>> ids)
			throws PhyloUtilsException
		{
		throw new NotImplementedException();
		}

	public RootedPhylogeny<Integer> extractTreeWithLeafIDs(Collection<Integer> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		throw new NotImplementedException();
		}
	}
