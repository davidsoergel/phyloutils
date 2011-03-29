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
public class NewickTaxonomyService extends RootedPhylogenyAsService<String>
		implements Serializable  // extends AbstractRootedPhylogeny<String>
	{
	private static final Logger logger = Logger.getLogger(NewickTaxonomyService.class);

	private String newickFilename;  // only for toString
	private boolean namedNodesMustBeLeaves;

	/*	public double exactDistanceBetween(int taxIdA, int taxIdB) throws PhyloUtilsException
		 {
		 return ciccarelliTree.distanceBetween(taxIdA, taxIdB);
		 }
 */

	protected NewickTaxonomyService(String filename, boolean namedNodesMustBeLeaves)
		{
		this.newickFilename = filename;
		this.namedNodesMustBeLeaves = namedNodesMustBeLeaves;

		init();
		}

	private void init()
		{
		final String cacheKey = newickFilename + ", " + namedNodesMustBeLeaves;
		logger.info("Cache key: " + cacheKey);

		basePhylogeny = (RootedPhylogeny<String>) CacheManager.get(this, cacheKey);


		if (basePhylogeny == null)
			{
			reload();

			CacheManager.put(this, cacheKey + ".basePhylogeny", basePhylogeny);
			}
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
			basePhylogeny = NewickParser.readWithStringIds(newickFilename, namedNodesMustBeLeaves);
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

	public String findTaxidByName(String name) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(name).getPayload();
		}

	public String findTaxidByNameRelaxed(String name) throws NoSuchNodeException
		{
		return basePhylogeny.getNode(name).getPayload();
		}

	public Set<String> getCachedNamesForId(String id)
		{
		String s = null;
		try
			{
			s = basePhylogeny.getNode(id).getPayload();
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			return new HashSet<String>();
			}
		return DSCollectionUtils.setOf(s);
		}

	@Override
	public String toString()
		{
		return "NewickTaxonomyService{" + newickFilename + '}';
		}
	}
