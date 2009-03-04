package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.tree.TreeException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NewickTaxonomyService implements TaxonomyService<String>  // extends AbstractRootedPhylogeny<String>
	{
	private static final Logger logger = Logger.getLogger(CiccarelliTaxonomyService.class);

	protected RootedPhylogeny<String> basePhylogeny;

	/*	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		 {
		 return ciccarelliTree.distanceBetween(taxIdA, taxIdB);
		 }
 */

	protected NewickTaxonomyService(String filename)
		{
		try
			{
			/*		URL res = ClassLoader.getSystemResource(ciccarelliFilename);
						if (res == null)
							{
							logger.error("Ciccarelli tree not found: " + ciccarelliFilename);
							//Get the System Classloader
							ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

							//Get the URLs
							URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

							for (int i = 0; i < urls.length; i++)
								{
								logger.warn(urls[i].getFile());
								}

							return;
							}
						InputStream is = res.openStream();*/
			/*if (is == null)
				{
				is = new FileInputStream(filename);
				}*/
			//	ciccarelliTree = new NewickParser<String>().read(is, new StringNodeNamer("UNNAMED NODE "));
			basePhylogeny = NewickParser.readWithStringIds(filename);
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		catch (PhyloUtilsException e)
			{
			logger.error("Error", e);
			}
		}


	public double exactDistanceBetween(String a, String b)
		{
		return basePhylogeny.distanceBetween(a, b);
		}


	public double exactDistanceBetween(PhylogenyNode<String> a, PhylogenyNode<String> b)
		{
		return basePhylogeny.distanceBetween(a, b);
		}

	public double greatestDepth(String a)
		{
		return basePhylogeny.getNode(a).getLargestLengthSpan();
		}

	public RootedPhylogeny<String> extractTreeWithLeafIDs(Collection<String> ids) throws PhyloUtilsException
		{
		return basePhylogeny.extractTreeWithLeafIDs(ids);
		}

	public PhylogenyNode<String> getRoot()
		{
		return basePhylogeny;
		}

	public RootedPhylogeny<String> getTree()
		{
		return basePhylogeny;
		}

	public RootedPhylogeny<String> getRandomSubtree(int numTaxa, Double mergeThreshold)
			throws PhyloUtilsException, TreeException
		{
		Map<String, Set<String>> mergeIdSets =
				TaxonMerger.merge(basePhylogeny.getLeafValues(), basePhylogeny, mergeThreshold);
		Set<String> mergedIds = mergeIdSets.keySet();
		DSCollectionUtils.retainRandom(mergedIds, numTaxa);
		return basePhylogeny.extractTreeWithLeafIDs(mergedIds);
		}

	public String findTaxidByName(String name) throws PhyloUtilsException
		{
		try
			{
			return basePhylogeny.getNode(name).getValue();
			}
		catch (NoSuchElementException e)
			{
			throw new PhyloUtilsException(e);
			}
		}

	public boolean isDescendant(String ancestor, String descendant) throws PhyloUtilsException
		{
		return basePhylogeny.isDescendant(ancestor, descendant);
		}

	public void saveState()
		{
		basePhylogeny.saveState();
		}

	public Double minDistanceBetween(String name1, String name2) throws PhyloUtilsException
		{
		return exactDistanceBetween(name1, name2);
		}

	public String nearestAncestorWithBranchLength(String id) throws PhyloUtilsException
		{
		return basePhylogeny.nearestAncestorWithBranchLength(id);
		}

/*	public RootedPhylogeny<String> extractTreeWithLeaves(Collection<PhylogenyNode<String>> ids)
			throws PhyloUtilsException
		{
		return basePhylogeny.extractTreeWithLeaves(ids);
		}*/

	public RootedPhylogeny<String> extractTreeWithLeafIDs(Collection<String> ids, boolean ignoreAbsentNodes)
			throws PhyloUtilsException
		{
		return basePhylogeny.extractTreeWithLeafIDs(ids, ignoreAbsentNodes);
		}

	public boolean isDescendant(PhylogenyNode<String> ancestor, PhylogenyNode<String> descendant)
			throws PhyloUtilsException
		{
		return basePhylogeny.isDescendant(ancestor, descendant);
		}

	public Double minDistanceBetween(PhylogenyNode<String> node1, PhylogenyNode<String> node2)
			throws PhyloUtilsException
		{
		return exactDistanceBetween(node1, node2);
		}
	}
