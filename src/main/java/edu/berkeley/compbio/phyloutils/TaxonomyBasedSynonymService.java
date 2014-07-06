/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * The point of this is to insulate the caller from the internal IDs.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class TaxonomyBasedSynonymService implements TaxonomySynonymService
	{
	private static final Logger logger = Logger.getLogger(TaxonomyBasedSynonymService.class);

	private TaxonomyService taxonomy;

/*	public FileBasedSynonymService(BufferedReader in)
		{
			String line;
	   while ((line = in.readLine()) != null)
		   {
		   //String[] synonyms = DSStringUtils.split(line, "\t");
		   String[] sp = line.split("\t");
		   String id = sp[0];
		   for (int i = 1; i < sp.length; i++)
			   {
			   forwarw
			   reverse.put(sp[i], id);
			   }
		   }
		}*/

	public TaxonomyBasedSynonymService()
		{
		}

	public TaxonomyBasedSynonymService(final TaxonomyService taxonomy)
		{
		this.taxonomy = taxonomy;
		}

	public void setTaxonomy(final TaxonomyService taxonomy)
		{
		this.taxonomy = taxonomy;
		}

	public Collection<String> synonymsOf(final String name) throws NoSuchNodeException
		{

		return taxonomy.getAllNamesForIds(taxonomy.findMatchingIds(name));
/*
		//PhylogenyNode<String> node = basePhylogeny.getNode(name);  // not needed; nameToNode contains the primary ID too
		Collection<String> synonyms = synonymsByName.get(name);
		if (synonyms == null)
			{
			throw new NoSuchNodeException(name);
			}
		return synonyms;*/
		}

	public Collection<String> synonymsOfRelaxed(final String name) throws NoSuchNodeException
		{

		return taxonomy.getAllNamesForIds(taxonomy.findMatchingIdsRelaxed(name));
		}

	/*public Collection<String> synonymsOfRelaxed(final String name) throws NoSuchNodeException
		{
		Collection<String> synonyms = synonymsByName.get(name);
		if (synonyms == null)
			{
			synonyms = synonymsByNameRelaxed.get(name);
			if (synonyms == null)
				{
				String origName = name;
				String oldname = null;
				try
					{
					while (!name.equals(oldname))
						{
						synonyms = synonymsByName.get(name);
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
				synonymsByNameRelaxed.put(name, synonyms);
				}
			}
		return synonyms;
		}*/
	}
