package edu.berkeley.compbio.phyloutils.jpadao;

import com.davidsoergel.springjpautils.GenericDaoImpl;
import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNodeDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyNode;

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Mar 7, 2007
 * Time: 1:47:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class NcbiTaxonomyNodeDaoImpl extends GenericDaoImpl<NcbiTaxonomyNode> implements NcbiTaxonomyNodeDao
	{

	public NcbiTaxonomyNode findById(Integer id)
		{
		return getJpaTemplate().find(NcbiTaxonomyNode.class, id);
		}

	public NcbiTaxonomyNode findByTaxId(int taxid)
		{
		return (NcbiTaxonomyNode) (getJpaTemplate().findByNamedQuery("NcbiTaxonomyNode.findByTaxId", taxid).get(0));
		}

	}
