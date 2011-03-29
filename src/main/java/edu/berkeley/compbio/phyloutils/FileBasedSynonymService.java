package edu.berkeley.compbio.phyloutils;

import com.davidsoergel.trees.NoSuchNodeException;

import java.io.BufferedReader;
import java.util.Collection;

/**
 * Not implemented
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class FileBasedSynonymService implements TaxonomySynonymService
	{
	public FileBasedSynonymService(BufferedReader in)
		{
		/*	String line;
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
		   }*/
		}

	public Collection<String> synonymsOf(final String name) throws NoSuchNodeException
		{
		return null;
		}

	public Collection<String> synonymsOfRelaxed(final String name) throws NoSuchNodeException
		{
		return null;
		}
	}
