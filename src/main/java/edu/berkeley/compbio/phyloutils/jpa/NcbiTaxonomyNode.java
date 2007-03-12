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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: soergel
 * Date: Nov 6, 2006
 * Time: 2:30:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "nodes")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedQueries({@NamedQuery(
		name = "NcbiTaxonomyNode.findByTaxId",
		query = "select n from NcbiTaxonomyNode n WHERE id = ?1")})

// or NONSTRICT_READ_WRITE?
//@NamedQuery(name="NcbiTaxonomyNode.findByName",query="select n from NcbiTaxonomyNode n WHERE Name = :name"),
public class NcbiTaxonomyNode extends SpringJpaObject
	{
	private static Logger logger = Logger.getLogger(NcbiTaxonomyName.class);

	//private int taxId;
	@ManyToOne
	//(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_tax_id")
	private NcbiTaxonomyNode parent;

	@OneToMany(mappedBy = "taxon")
	private Set<NcbiTaxonomyName> names;

	@OneToMany(mappedBy = "parent")
	//, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	//, fetch = FetchType.EAGER)
	//, CascadeType.REFRESH})
	//@LazyCollection(LazyCollectionOption.FALSE)
	//@Fetch(value = FetchMode.SUBSELECT)
	private Set<NcbiTaxonomyNode> children = new HashSet<NcbiTaxonomyNode>();

	@Column(name = "rank")
	private String rank;

	@Column(name = "embl_code")
	private String emblCode;

	@Column(name = "division_id")
	private int divisionId;

	@Column(name = "inherited_div_flag")
	private boolean inheritedDivFlag;

	@Column(name = "genetic_code_id")
	private int geneticCodeId;

	@Column(name = "inherited_GC_flag")
	private boolean inheritedGCFlag;

	@Column(name = "mitochondrial_genetic_code_id")
	private int mitachondrialGeneticCodeId;

	@Column(name = "inherited_MGC_flag")
	private boolean inheritedMGCFlag;

	@Column(name = "GenBank_hidden_flag")
	private boolean genBankHiddenFlag;

	@Column(name = "hidden_subtree_root_flag")
	private boolean hiddenSubtreeRootFlag;

	@Column(name = "comments")
	private String comments;

	//@Transient
	public long getTaxId()
		{
		return getId();
		}

	public NcbiTaxonomyNode getParent()
		{
		return parent;
		}

	public void setParent(NcbiTaxonomyNode parent)
		{
		this.parent = parent;
		}


	public Set<NcbiTaxonomyNode> getChildSets()
		{
		return children;
		}

	public void setChildSets(Set<NcbiTaxonomyNode> childSets)
		{
		this.children = childSets;
		}


	public Set<NcbiTaxonomyName> getNames()
		{
		return names;
		}

	public void setNames(Set<NcbiTaxonomyName> names)
		{
		this.names = names;
		}

	public String getRank()
		{
		return rank;
		}

	public void setRank(String rank)
		{
		this.rank = rank;
		}

	public String getEmblCode()
		{
		return emblCode;
		}

	public void setEmblCode(String emblCode)
		{
		this.emblCode = emblCode;
		}

	public int getDivisionId()
		{
		return divisionId;
		}

	public void setDivisionId(int divisionId)
		{
		this.divisionId = divisionId;
		}

	public boolean isInheritedDivFlag()
		{
		return inheritedDivFlag;
		}

	public void setInheritedDivFlag(boolean inheritedDivFlag)
		{
		this.inheritedDivFlag = inheritedDivFlag;
		}

	public int getGeneticCodeId()
		{
		return geneticCodeId;
		}

	public void setGeneticCodeId(int geneticCodeId)
		{
		this.geneticCodeId = geneticCodeId;
		}

	public boolean isInheritedGCFlag()
		{
		return inheritedGCFlag;
		}

	public void setInheritedGCFlag(boolean inheritedGCFlag)
		{
		this.inheritedGCFlag = inheritedGCFlag;
		}

	public int getMitochondrialGeneticCodeId()
		{
		return mitachondrialGeneticCodeId;
		}

	public void setMitochondrialGeneticCodeId(int mitachondrialGeneticCodeId)
		{
		this.mitachondrialGeneticCodeId = mitachondrialGeneticCodeId;
		}

	public boolean isInheritedMGCFlag()
		{
		return inheritedMGCFlag;
		}

	public void setInheritedMGCFlag(boolean inheritedMGCFlag)
		{
		this.inheritedMGCFlag = inheritedMGCFlag;
		}

	public boolean isGenBankHiddenFlag()
		{
		return genBankHiddenFlag;
		}

	public void setGenBankHiddenFlag(boolean genBankHiddenFlag)
		{
		this.genBankHiddenFlag = genBankHiddenFlag;
		}

	public boolean isHiddenSubtreeRootFlag()
		{
		return hiddenSubtreeRootFlag;
		}

	public void setHiddenSubtreeRootFlag(boolean hiddenSubtreeRootFlag)
		{
		this.hiddenSubtreeRootFlag = hiddenSubtreeRootFlag;
		}

	public String getComments()
		{
		return comments;
		}

	public void setComments(String comments)
		{
		this.comments = comments;
		}


	}
