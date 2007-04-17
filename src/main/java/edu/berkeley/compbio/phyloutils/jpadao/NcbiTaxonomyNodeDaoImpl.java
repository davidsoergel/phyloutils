package edu.berkeley.compbio.phyloutils.jpadao;

import com.davidsoergel.springjpautils.GenericDaoImpl;
import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNodeDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyNode;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by IntelliJ IDEA. User: soergel Date: Mar 7, 2007 Time: 1:47:27 PM To change this template use File |
 * Settings | File Templates.
 */
@Repository
public class NcbiTaxonomyNodeDaoImpl extends GenericDaoImpl<NcbiTaxonomyNode> implements NcbiTaxonomyNodeDao
	{

	private EntityManager entityManager;

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager)
		{
		this.entityManager = entityManager;
		}

	public EntityManager getEntityManager()
		{
		return entityManager;
		}

	public NcbiTaxonomyNode findById(Integer id)
		{
		return entityManager.find(NcbiTaxonomyNode.class, id);
		}

	@Transactional(noRollbackFor = javax.persistence.NoResultException.class)
	public NcbiTaxonomyNode findByTaxId(int taxid)
		{
		return (NcbiTaxonomyNode) (entityManager.createNamedQuery("NcbiTaxonomyNode.findByTaxId")
				.setParameter("taxid", taxid).getSingleResult());
		}

	}
