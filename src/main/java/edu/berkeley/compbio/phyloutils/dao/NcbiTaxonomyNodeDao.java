package edu.berkeley.compbio.phyloutils.dao;

import com.davidsoergel.springjpautils.GenericDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyNode;

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Mar 7, 2007
 * Time: 1:44:56 PM
 * To change this template use File | Settings | File Templates.
 */
public interface NcbiTaxonomyNodeDao extends GenericDao<NcbiTaxonomyNode, Integer>
	{
	public NcbiTaxonomyNode findByTaxId(int taxid);

	}