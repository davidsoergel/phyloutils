package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.CacheManager;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NewickWithSynonymsTaxonomyService extends NewickTaxonomyService
	{
	private static final Logger logger = Logger.getLogger(NewickWithSynonymsTaxonomyService.class);

	// is this redundant with uniqueIdToNodeMap?  No, that's supposed to be one-to-one, whereas this is many-to-one
	private HashMap<String, String> taxIdByName;

	private HashMap<String, String> taxIdByNameRelaxed;

	private HashSet<String> ambiguousNames;


	private String newickFilename;
	private String synonymFilename;

	private void init() throws IOException
		{
		// don't bother keeping track of which caches are affected by which inputs; just reload them all if anything changes
		final String allFilenames = newickFilename + ", " + synonymFilename;
		logger.info("Cache key: " + allFilenames);

		taxIdByName = (HashMap<String, String>) CacheManager.get(this, allFilenames + ".taxIdByName");
		taxIdByNameRelaxed = (HashMap<String, String>) CacheManager.get(this, allFilenames + ".taxIdByNameRelaxed");
		ambiguousNames = (HashSet<String>) CacheManager.get(this, allFilenames + ".ambiguousNames");

		if (taxIdByName == null || taxIdByNameRelaxed == null || ambiguousNames == null)
			{
			reload();

			CacheManager.put(this, allFilenames + ".taxIdByName", taxIdByName);
			CacheManager.put(this, allFilenames + ".taxIdByName", taxIdByNameRelaxed);
			CacheManager.put(this, allFilenames + ".ambiguousNames", ambiguousNames);
			}
		}

	protected NewickWithSynonymsTaxonomyService(String filename, boolean namedNodesMustBeLeaves, String synonymFilename)
			throws IOException
		{
		super(filename, namedNodesMustBeLeaves);

		init();
		}

	private void reload() throws IOException
		{

		// Perf CACHE

		BufferedReader in = new BufferedReader(new FileReader(synonymFilename));
		String line;
		while ((line = in.readLine()) != null)
			{
			//String[] synonyms = DSStringUtils.split(line, "\t");
			String[] sp = line.split("\t");
			String id = sp[0];
			try
				{
				PhylogenyNode<String> node = basePhylogeny.getNode(id);

				// note the initial canonical ID is itself included as a name
				for (int i = 0; i < sp.length; i++)
					{
					// ignore names already determined to be ambiguous
					if (!ambiguousNames.contains(sp[i]))
						{

						if (taxIdByName.containsKey(sp[i]))
							{
							taxIdByName.remove(sp[i]);
							ambiguousNames.add(sp[i]);
							}
						else
							{
							taxIdByName.put(sp[i], node.getPayload());
							}
						}
					}
				}
			catch (NoSuchNodeException e)
				{
				logger.error("Error", e);
				}
			}
		}

	public String findTaxidByName(String name) throws NoSuchNodeException
		{
		//PhylogenyNode<String> node = basePhylogeny.getNode(name);  // not needed; nameToNode contains the primary ID too
		String taxid = taxIdByName.get(name);
		if (taxid == null)
			{
			throw new NoSuchNodeException(name);
			}
		return taxid;
		}


	public String findTaxidByNameRelaxed(String name) throws NoSuchNodeException
		{
		String taxid = taxIdByName.get(name);
		if (taxid == null)
			{


			taxid = taxIdByNameRelaxed.get(name);
			if (taxid == null)
				{
				String origName = name;
				String oldname = null;
				try
					{
					while (!name.equals(oldname))
						{
						taxid = taxIdByName.get(name);
						if (name != null)
							{
							break;
							}

						oldname = name;
						name = name.substring(0, name.lastIndexOf(" "));
						}
					}
				catch (IndexOutOfBoundsException e)
					{
					throw new NoSuchNodeException("Could not find taxon: " + name);
					}
				if (!name.equals(origName))
					{
					logger.warn("Relaxed name " + origName + " to " + name);
					}
				taxIdByNameRelaxed.put(name, taxid);
				}
			}
		return taxid;
		}


	public Set<String> getCachedNamesForId(String id)
		{
		throw new NotImplementedException();
		}
	}
