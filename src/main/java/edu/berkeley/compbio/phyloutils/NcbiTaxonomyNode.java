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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
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
// or NONSTRICT_READ_WRITE?
@NamedQueries({ @NamedQuery(
		name = "NcbiTaxonomyNode.findByTaxId",
		query = "select n from NcbiTaxonomyNode n WHERE id = :taxId")})
//@NamedQuery(name="NcbiTaxonomyNode.findByName",query="select n from NcbiTaxonomyNode n WHERE Name = :name"),
public class NcbiTaxonomyNode extends HibernateObject
	{
	private static Logger logger = Logger.getLogger(NcbiTaxonomyName.class);

	//private int taxId;
	private NcbiTaxonomyNode parent;
	private Set<NcbiTaxonomyName> names;

	private Set<NcbiTaxonomyNode> children = new HashSet<NcbiTaxonomyNode>();

	private String rank;
	private String emblCode;
	private int divisionId;
	private boolean inheritedDivFlag;
	private int geneticCodeId;
	private boolean inheritedGCFlag;
	private int mitachondrialGeneticCodeId;
	private boolean inheritedMGCFlag;
	private boolean genBankHiddenFlag;
	private boolean hiddenSubtreeRootFlag;
	private String comments;

	@Transient
	public long getTaxId()
		{
		return getId();
		}

	@ManyToOne  //(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_tax_id")
	public NcbiTaxonomyNode getParent()
		{
		return parent;
		}

	public void setParent(NcbiTaxonomyNode parent)
		{
		this.parent = parent;
		}


	@OneToMany(mappedBy = "parent") //, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	//, fetch = FetchType.EAGER)
	//, CascadeType.REFRESH})
	//@LazyCollection(LazyCollectionOption.FALSE)
	//@Fetch(value = FetchMode.SUBSELECT)
	public Set<NcbiTaxonomyNode> getChildSets()
		{
		return children;
		}

	public void setChildSets(Set childSets)
		{
		this.children = childSets;
		}


	@OneToMany(mappedBy="taxon")
	public Set<NcbiTaxonomyName> getNames()
		{
		return names;
		}

	public void setNames(Set<NcbiTaxonomyName> names)
		{
		this.names = names;
		}

	@Column(name = "rank")
	public String getRank()
		{
		return rank;
		}

	public void setRank(String rank)
		{
		this.rank = rank;
		}

	@Column(name = "embl_code")
	public String getEmblCode()
		{
		return emblCode;
		}

	public void setEmblCode(String emblCode)
		{
		this.emblCode = emblCode;
		}

	@Column(name = "division_id")
	public int getDivisionId()
		{
		return divisionId;
		}

	public void setDivisionId(int divisionId)
		{
		this.divisionId = divisionId;
		}

	@Column(name = "inherited_div_flag")
	public boolean isInheritedDivFlag()
		{
		return inheritedDivFlag;
		}

	public void setInheritedDivFlag(boolean inheritedDivFlag)
		{
		this.inheritedDivFlag = inheritedDivFlag;
		}

	@Column(name = "genetic_code_id")
	public int getGeneticCodeId()
		{
		return geneticCodeId;
		}

	public void setGeneticCodeId(int geneticCodeId)
		{
		this.geneticCodeId = geneticCodeId;
		}

	@Column(name = "inherited_GC_flag")
	public boolean isInheritedGCFlag()
		{
		return inheritedGCFlag;
		}

	public void setInheritedGCFlag(boolean inheritedGCFlag)
		{
		this.inheritedGCFlag = inheritedGCFlag;
		}

	@Column(name = "mitochondrial_genetic_code_id")
	public int getMitochondrialGeneticCodeId()
		{
		return mitachondrialGeneticCodeId;
		}

	public void setMitochondrialGeneticCodeId(int mitachondrialGeneticCodeId)
		{
		this.mitachondrialGeneticCodeId = mitachondrialGeneticCodeId;
		}

	@Column(name = "inherited_MGC_flag")
	public boolean isInheritedMGCFlag()
		{
		return inheritedMGCFlag;
		}

	public void setInheritedMGCFlag(boolean inheritedMGCFlag)
		{
		this.inheritedMGCFlag = inheritedMGCFlag;
		}

	@Column(name = "GenBank_hidden_flag")
	public boolean isGenBankHiddenFlag()
		{
		return genBankHiddenFlag;
		}

	public void setGenBankHiddenFlag(boolean genBankHiddenFlag)
		{
		this.genBankHiddenFlag = genBankHiddenFlag;
		}

	@Column(name = "hidden_subtree_root_flag")
	public boolean isHiddenSubtreeRootFlag()
		{
		return hiddenSubtreeRootFlag;
		}

	public void setHiddenSubtreeRootFlag(boolean hiddenSubtreeRootFlag)
		{
		this.hiddenSubtreeRootFlag = hiddenSubtreeRootFlag;
		}

	@Column(name = "comments")
	public String getComments()
		{
		return comments;
		}

	public void setComments(String comments)
		{
		this.comments = comments;
		}


	public static NcbiTaxonomyNode findByTaxId(long taxid) throws NoResultException
		{
		return PhyloUtils.getNcbiDb().find(NcbiTaxonomyNode.class, taxid);
		//HibernateDB.getDb().beginTaxn();
		// Status notstarted = Status.findByName("Not Started");
		/*Query q = PhyloUtils.getNcbiDb().createNamedQuery("NcbiTaxonomyNode.findByTaxId");
		q.setMaxResults(1);
		q.setParameter("taxId", taxid);
		NcbiTaxonomyNode result;

		result = (NcbiTaxonomyNode) q.getSingleResult();

		return result;*/
		}
	}
