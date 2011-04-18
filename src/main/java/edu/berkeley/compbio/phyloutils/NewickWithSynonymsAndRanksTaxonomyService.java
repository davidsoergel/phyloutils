package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.dsutils.CacheManager;
import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.file.IntArrayReader;
import com.davidsoergel.trees.NoSuchNodeException;
import com.davidsoergel.trees.PhylogenyNode;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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
	// it does assume that no two nodes share a name, though.
	/*
	private HashMap<String, Integer> taxIdByName;
	private HashMap<Integer, String> nameByTaxId;
*/
	private HashMap<String, Integer> taxIdByNameRelaxed;
/*
	private HashSet<String> ambiguousNames;
	private HashMap<Integer, String[]> allNamesByTaxId;
	*/

	private CacheManager.LazyStub taxIdByNameStub;
	private CacheManager.LazyStub nameByTaxIdStub;

	//private CacheManager.LazyStub taxIdByNameRelaxedStub;

	private CacheManager.LazyStub ambiguousNamesStub;
	private CacheManager.LazyStub allNamesByTaxIdStub;

	private String dirName;

	public void setDirName(final String dirName)
		{
		this.dirName = dirName;
		}

	public NewickWithSynonymsAndRanksTaxonomyService()
		{
		}

	protected void init()
		{

		setNewickFilename(dirName + File.separator + "tree.newick");
		super.init();

		// don't bother keeping track of which caches are affected by which inputs; just reload them all if anything changes
		//final String dirName; = newickFilename + ", " + synonymFilename;
		logger.info("Cache key: " + dirName);

		//taxIdByNameRelaxed = (HashMap<String, Integer>) CacheManager.get(this, dirName + ".taxIdByNameRelaxed");

		taxIdByNameRelaxed = (HashMap<String, Integer>) CacheManager
				.getAccumulatingMapAssumeSerializable(this, dirName + ".taxIdByNameRelaxed");

		taxIdByNameStub = CacheManager.getLazy(this, dirName + ".taxIdByName");
		ambiguousNamesStub = CacheManager.getLazy(this, dirName + ".ambiguousNames");
		nameByTaxIdStub = CacheManager.getLazy(this, dirName + ".nameByTaxId");
		allNamesByTaxIdStub = CacheManager.getLazy(this, dirName + ".allNamesByTaxId");

		if (taxIdByNameStub == null || ambiguousNamesStub == null || nameByTaxIdStub == null
		    || allNamesByTaxIdStub == null)
			{
			logger.info("Caches not found for " + dirName + ", reloading...");

			reload();

			/*
			CacheManager.put(this, dirName + ".taxIdByName", taxIdByNameStub.get());
			//CacheManager.put(this, dirName + ".taxIdByNameRelaxed", taxIdByNameRelaxed);
			CacheManager.put(this, dirName + ".ambiguousNames", ambiguousNamesStub.get());
			CacheManager.put(this, dirName + ".nameByTaxId", nameByTaxIdStub.get());
			CacheManager.put(this, dirName + ".allNamesByTaxId", allNamesByTaxIdStub.get());
			*/
			}
		else
			{
			logger.info("Loaded caches for " + dirName);
			}
		}

	public NewickWithSynonymsAndRanksTaxonomyService(String dirName, boolean namedNodesMustBeLeaves) throws IOException
		{
		this.dirName = dirName;
		setNamedNodesMustBeLeaves(namedNodesMustBeLeaves);
		init();
		}

	private void reload()
		{

		HashMap<String, Integer> taxIdByName = new HashMap<String, Integer>();
		HashMap<Integer, String> nameByTaxId = new HashMap<Integer, String>();
		//taxIdByNameRelaxed = new HashMap<String, Integer>();
		HashSet<String> ambiguousNames = new HashSet<String>();
		HashMap<Integer, String[]> allNamesByTaxId = new HashMap<Integer, String[]>();

		try
			{
			BufferedReader in = new BufferedReader(new FileReader(dirName + File.separator + "synonyms"));
			String line;
			while ((line = in.readLine()) != null)
				{
				//String[] synonyms = DSStringUtils.split(line, "\t");
				String[] sp = line.split("\t");
				Integer id = new Integer(sp[0]);
				nameByTaxId.put(id, sp[1]);  // scientific name should always be the first entry
				allNamesByTaxId.put(id, sp);  // track synonyms

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

			taxIdByNameStub.put(taxIdByName);
			nameByTaxIdStub.put(nameByTaxId);
			ambiguousNamesStub.put(ambiguousNames);
			allNamesByTaxIdStub.put(allNamesByTaxId);
			}
		catch (NoSuchNodeException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		catch (FileNotFoundException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new PhyloUtilsRuntimeException(e);
			}
		}

	public Collection<String> getAllNamesForIds(final Set<Integer> ids)
		{
		HashMap<Integer, String[]> allNamesByTaxId = (HashMap<Integer, String[]>) allNamesByTaxIdStub.get();
		Set<String> result = new HashSet<String>();
		for (Integer id : ids)
			{
			result.addAll(Arrays.asList(allNamesByTaxId.get(id)));
			}
		return result;
		}

	public Integer findTaxidByName(String name) throws NoSuchNodeException
		{
		HashMap<String, Integer> taxIdByName = (HashMap<String, Integer>) taxIdByNameStub.get();
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
		HashMap<String, Integer> taxIdByName = (HashMap<String, Integer>) taxIdByNameStub.get();

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
		if (taxid == null)
			{
			throw new NoSuchNodeException("Could not find taxon: " + name);
			}
		return taxid;
		}


	public Set<Integer> getCachedNamesForId(String id)
		{
		throw new NotImplementedException();
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

	public String getScientificName(final Integer taxid) throws NoSuchNodeException
		{
		HashMap<Integer, String> nameByTaxId = (HashMap<Integer, String>) nameByTaxIdStub.get();
		//PhylogenyNode<String> node = basePhylogeny.getNode(name);  // not needed; nameToNode contains the primary ID too
		String name = nameByTaxId.get(taxid);
		if (taxid == null)
			{
			throw new NoSuchNodeException("" + taxid);
			}
		return name;
		}

	public synchronized Set<Integer> findMatchingIds(String name) throws NoSuchNodeException
		{
		// note that ambiguous names (matching more than one node) were set aside and ignored
		return DSCollectionUtils.setOf(findTaxidByName(name));
		}

	public synchronized Set<Integer> findMatchingIdsRelaxed(String name) throws NoSuchNodeException
		{
		// note that ambiguous names (matching more than one node) were set aside and ignored
		return DSCollectionUtils.setOf(findTaxidByNameRelaxed(name));
		}
	}
