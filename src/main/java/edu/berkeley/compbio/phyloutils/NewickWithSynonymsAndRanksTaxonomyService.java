package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.CacheManager;
import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.file.IntArrayReader;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NewickWithSynonymsAndRanksTaxonomyService extends NewickIntegerTaxonomyService
		implements RankedTaxonomyService<Integer>
	{
	private static final Logger logger = Logger.getLogger(NewickWithSynonymsAndRanksTaxonomyService.class);

	// is this redundant with uniqueIdToNodeMap?  No, that's supposed to be one-to-one, whereas this is many-to-one
	private HashMap<String, Integer> taxIdByName;
	private HashMap<Integer, String> nameByTaxId;

	private HashMap<String, Integer> taxIdByNameRelaxed;

	private HashSet<String> ambiguousNames;


	private String dirName;

	private void init() throws IOException
		{
		// don't bother keeping track of which caches are affected by which inputs; just reload them all if anything changes
		//final String dirName; = newickFilename + ", " + synonymFilename;
		logger.info("Cache key: " + dirName);

		taxIdByName = (HashMap<String, Integer>) CacheManager.get(this, dirName + ".taxIdByName");
		taxIdByNameRelaxed = (HashMap<String, Integer>) CacheManager.get(this, dirName + ".taxIdByNameRelaxed");
		ambiguousNames = (HashSet<String>) CacheManager.get(this, dirName + ".ambiguousNames");
		nameByTaxId = (HashMap<Integer, String>) CacheManager.get(this, dirName + ".nameByTaxId");

		if (taxIdByName == null || taxIdByNameRelaxed == null || ambiguousNames == null || nameByTaxId == null)
			{
			reload();

			CacheManager.put(this, dirName + ".taxIdByName", taxIdByName);
			CacheManager.put(this, dirName + ".taxIdByName", taxIdByNameRelaxed);
			CacheManager.put(this, dirName + ".ambiguousNames", ambiguousNames);
			CacheManager.put(this, dirName + ".nameByTaxId", nameByTaxId);
			}
		}

	public NewickWithSynonymsAndRanksTaxonomyService(String dirName, boolean namedNodesMustBeLeaves) throws IOException
		{
		super(dirName + File.separator + "tree.newick", namedNodesMustBeLeaves);
		this.dirName = dirName;
		init();
		}

	private void reload() throws IOException
		{

		// Perf CACHE

		BufferedReader in = new BufferedReader(new FileReader(dirName + File.separator + "synonyms"));
		String line;
		while ((line = in.readLine()) != null)
			{
			//String[] synonyms = DSStringUtils.split(line, "\t");
			String[] sp = line.split("\t");
			Integer id = new Integer(sp[0]);
			nameByTaxId.put(id, sp[1]);  // scientific name should always be the first entry
			try
				{
				PhylogenyNode<Integer> node = basePhylogeny.getNode(id);

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

	public Integer findTaxidByName(String name) throws NoSuchNodeException
		{
		//PhylogenyNode<String> node = basePhylogeny.getNode(name);  // not needed; nameToNode contains the primary ID too
		Integer taxid = taxIdByName.get(name);
		if (taxid == null)
			{
			throw new NoSuchNodeException(name);
			}
		return taxid;
		}


	public Integer findTaxidByNameRelaxed(String name) throws NoSuchNodeException
		{
		Integer taxid = taxIdByName.get(name);
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


	public Set<Integer> getCachedNamesForId(String id)
		{
		throw new NotImplementedException();
		}

	public String getScientificName(final Integer taxid) throws NoSuchNodeException
		{
		//PhylogenyNode<String> node = basePhylogeny.getNode(name);  // not needed; nameToNode contains the primary ID too
		String name = nameByTaxId.get(taxid);
		if (taxid == null)
			{
			throw new NoSuchNodeException("" + taxid);
			}
		return name;
		}

	public Set<Integer> getTaxIdsWithRank(final String rank)
		{
		try
			{
			return new HashSet<Integer>(
					Arrays.asList(DSArrayUtils.toObject(IntArrayReader.read(dirName + File.separator + rank))));
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		}
	}
