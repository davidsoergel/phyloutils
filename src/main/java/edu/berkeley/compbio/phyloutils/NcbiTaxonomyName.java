package edu.berkeley.compbio.phyloutils;


import com.davidsoergel.hibernateutils.HibernateObject;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Nov 6, 2006
 * Time: 2:30:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "names")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
// or NONSTRICT_READ_WRITE?
@NamedQueries({@NamedQuery(
		name = "NcbiTaxonomyName.findByName",
		query = "select n from NcbiTaxonomyName n WHERE n.name = :name")})
public class NcbiTaxonomyName extends HibernateObject
	{
	private static Logger logger = Logger.getLogger(NcbiTaxonomyName.class);


	@ManyToOne
	@JoinColumn(name = "tax_id")
	private NcbiTaxonomyNode taxon;

	@Column(name = "name_txt")
	private String name;

	@Column(name = "unique_name")
	private String uniqueName;

	@Column(name = "name_class")
	private String nameClass;

	public NcbiTaxonomyNode getTaxon()
		{
		return taxon;
		}

	public void setTaxon(NcbiTaxonomyNode taxon)
		{
		this.taxon = taxon;
		}

	public String getName()
		{
		return name;
		}

	public void setName(String name)
		{
		this.name = name;
		}

	public String getUniqueName()
		{
		return uniqueName;
		}

	public void setUniqueName(String uniqueName)
		{
		this.uniqueName = uniqueName;
		}

	public String getNameClass()
		{
		return nameClass;
		}

	public void setNameClass(String nameClass)
		{
		this.nameClass = nameClass;
		}

	private static Map<String, NcbiTaxonomyName> names = new HashMap<String, NcbiTaxonomyName>();

	public static NcbiTaxonomyName findByName(String name) throws NoResultException
		{
		NcbiTaxonomyName result = names.get(name);
		if (result != null)
			{
			return result;
			}
		//HibernateDB.getDb().beginTaxn();
		// Status notstarted = Status.findByName("Not Started");
		Query q = PhyloUtils.getNcbiDb().createNamedQuery("NcbiTaxonomyName.findByName");
		q.setMaxResults(1);
		q.setParameter("name", name);

		result = (NcbiTaxonomyName) q.getSingleResult();
		names.put(name, result);
		return result;
		}

	public static NcbiTaxonomyName findByNameRelaxed(String name) throws NoResultException
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


	public String toString()
		{
		return getName();

		}

	}
