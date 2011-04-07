package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.CacheManager;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.RootedPhylogeny;
import com.davidsoergel.trees.TreeException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NewickIntegerTaxonomyService extends RootedPhylogenyAsService<Integer>
		implements Serializable  // extends AbstractRootedPhylogeny<String>
	{
	private static final Logger logger = Logger.getLogger(NewickStringTaxonomyService.class);

	private String newickFilename;  // only for toString
	private boolean namedNodesMustBeLeaves;
	private Double setAllBranchLengthsTo;

	/*	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		 {
		 return ciccarelliTree.distanceBetween(taxIdA, taxIdB);
		 }
 */

	public NewickIntegerTaxonomyService()
		{
		}

	public void setNamedNodesMustBeLeaves(final boolean namedNodesMustBeLeaves)
		{
		this.namedNodesMustBeLeaves = namedNodesMustBeLeaves;
		}

	public void setNewickFilename(final String newickFilename)
		{
		this.newickFilename = newickFilename;
		}

	public void setAllBranchLengthsTo(final Double setAllBranchLengthsTo)
		{
		this.setAllBranchLengthsTo = setAllBranchLengthsTo;
		}

	protected NewickIntegerTaxonomyService(String filename, boolean namedNodesMustBeLeaves,
	                                       Double setAllBranchLengthsTo)
		{
		this.newickFilename = filename;
		this.namedNodesMustBeLeaves = namedNodesMustBeLeaves;
		this.setAllBranchLengthsTo = setAllBranchLengthsTo;

		init();
		}

	protected void init()
		{
		final String cacheKey = newickFilename + ", " + namedNodesMustBeLeaves;
		logger.info("Cache key: " + cacheKey);

		basePhylogeny = (RootedPhylogeny<Integer>) CacheManager.get(this, cacheKey);


		if (basePhylogeny == null)
			{
			reload();

			CacheManager.put(this, cacheKey + ".basePhylogeny", basePhylogeny);
			}

		logger.info("loaded tree with maximum branch-length depth " + basePhylogeny.getGreatestBranchLengthDepthBelow()
		            + " and maximum span " + basePhylogeny.getLargestLengthSpan());
		}

	private void reload()
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
			basePhylogeny = NewickParser.readWithIntegerIds(newickFilename, false, namedNodesMustBeLeaves);

			if (setAllBranchLengthsTo != null)
				{
				basePhylogeny.setAllBranchLengthsTo(setAllBranchLengthsTo.doubleValue());
				}
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		catch (TreeException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		}

	public Integer findTaxidByName(String name) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(new Integer(name)).getPayload();
		}

	public Integer findTaxidByNameRelaxed(String name) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(new Integer(name)).getPayload();
		}

	public Set<Integer> getCachedNamesForId(String id)
		{
		Integer s = null;
		try
			{
			s = basePhylogeny.getNode(new Integer(id)).getPayload();
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			return new HashSet<Integer>();
			}
		return DSCollectionUtils.setOf(s);
		}

	@Override
	public String toString()
		{
		return "NewickTaxonomyService{" + newickFilename + '}';
		}
	}
