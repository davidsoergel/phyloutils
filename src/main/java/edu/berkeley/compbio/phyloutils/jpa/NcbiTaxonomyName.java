package edu.berkeley.compbio.phyloutils.jpa;


import com.davidsoergel.springjpautils.SpringJpaObject;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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
@NamedQueries({@NamedQuery(
		name = "NcbiTaxonomyName.findByName",
		query = "select n from NcbiTaxonomyName n WHERE n.name = :name")})

// or NONSTRICT_READ_WRITE?
public class NcbiTaxonomyName extends SpringJpaObject
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


	public String toString()
		{
		return getName();

		}

	}
