package edu.berkeley.compbio.phyloutils.jpadao;

import com.davidsoergel.springjpautils.GenericDaoImpl;
import edu.berkeley.compbio.phyloutils.dao.NcbiTaxonomyNameDao;
import edu.berkeley.compbio.phyloutils.jpa.NcbiTaxonomyName;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Mar 7, 2007
 * Time: 1:47:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class NcbiTaxonomyNameDaoImpl extends GenericDaoImpl<NcbiTaxonomyName> implements NcbiTaxonomyNameDao
	{
	public NcbiTaxonomyName findById(Integer id)
		{
		return getJpaTemplate().find(NcbiTaxonomyName.class, id);
		}

	private Map<String, NcbiTaxonomyName> names = new HashMap<String, NcbiTaxonomyName>();

	public NcbiTaxonomyName findByName(String name)
		{
		NcbiTaxonomyName result = names.get(name);
		if (result != null)
			{
			return result;
			}
		//HibernateDB.getDb().beginTaxn();
		// Status notstarted = Status.findByName("Not Started");

		/*Query q = PhyloUtils.getNcbiDb().createNamedQuery("NcbiTaxonomyName.findByName");
		q.setMaxResults(1);
		q.setParameter("name", name);

		result = (NcbiTaxonomyName) q.getSingleResult();*/

		result = (NcbiTaxonomyName) (getJpaTemplate().findByNamedQuery("NcbiTaxonomyName.findByName", name).get(0));

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
				catch (DataRetrievalFailureException e)
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

