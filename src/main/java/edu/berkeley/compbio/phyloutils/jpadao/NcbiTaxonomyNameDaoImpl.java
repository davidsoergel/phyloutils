package edu.berkeley.compbio.phyloutils.jpadao;

import com.davidsoergel.springjpautils.GenericDaoImpl;
import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNameDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyName;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: soergel Date: Mar 7, 2007 Time: 1:47:27 PM To change this template use File |
 * Settings | File Templates.
 */
@Repository
public class NcbiTaxonomyNameDaoImpl extends GenericDaoImpl<NcbiTaxonomyName> implements NcbiTaxonomyNameDao
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

	public NcbiTaxonomyName findById(Integer id)
		{
		return entityManager.find(NcbiTaxonomyName.class, id);
		}

	private Map<String, NcbiTaxonomyName> names = new HashMap<String, NcbiTaxonomyName>();

	@Transactional(noRollbackFor = javax.persistence.NoResultException.class)
	public NcbiTaxonomyName findByName(String name)
		{
		NcbiTaxonomyName result = names.get(name);
		if (result != null)
			{
			return result;
			}
		//HibernateDB.getDb().beginTaxn();
		// Status notstarted = Status.findByName("Waiting");

		/*Query q = PhyloUtils.getNcbiDb().createNamedQuery("NcbiTaxonomyName.findByName");
		q.setMaxResults(1);
		q.setParameter("name", name);

		result = (NcbiTaxonomyName) q.getSingleResult();*/

		result = (NcbiTaxonomyName) (entityManager.createNamedQuery("NcbiTaxonomyName.findByName")
				.setParameter("name", name).getSingleResult());

		names.put(name, result);
		return result;
		}

	public NcbiTaxonomyName findByNameRelaxed(String name)
		{
		NcbiTaxonomyName result = null;
		String oldname = null;
		try
			{
			while (!name.equals(oldname))
				{
				try
					{
					result = findByName(name);
					break;
					}
				catch (NoResultException e)
					{
					oldname = name;
					name = name.substring(0, name.lastIndexOf(" "));
					}
				}
			}
		catch (IndexOutOfBoundsException e)
			{
			return null;
			}
		return result;
		}
	}

