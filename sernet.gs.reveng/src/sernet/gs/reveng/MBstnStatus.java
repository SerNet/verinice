package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MBstnStatus entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MBstnStatus implements java.io.Serializable {

	// Fields

	private Short bstId;
	private String link;
	private Integer metaVers;
	private Integer obsoletVers;
	private Short metaNeu;
	private String guid;
	private Date timestamp;
	private Short impNeu;
	private Set modZobjBsts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MBstnStatus() {
	}

	/** minimal constructor */
	public MBstnStatus(Short bstId) {
		this.bstId = bstId;
	}

	/** full constructor */
	public MBstnStatus(Short bstId, String link, Integer metaVers,
			Integer obsoletVers, Short metaNeu, String guid, Date timestamp,
			Short impNeu, Set modZobjBsts) {
		this.bstId = bstId;
		this.link = link;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.metaNeu = metaNeu;
		this.guid = guid;
		this.timestamp = timestamp;
		this.impNeu = impNeu;
		this.modZobjBsts = modZobjBsts;
	}

	// Property accessors

	public Short getBstId() {
		return this.bstId;
	}

	public void setBstId(Short bstId) {
		this.bstId = bstId;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Integer getMetaVers() {
		return this.metaVers;
	}

	public void setMetaVers(Integer metaVers) {
		this.metaVers = metaVers;
	}

	public Integer getObsoletVers() {
		return this.obsoletVers;
	}

	public void setObsoletVers(Integer obsoletVers) {
		this.obsoletVers = obsoletVers;
	}

	public Short getMetaNeu() {
		return this.metaNeu;
	}

	public void setMetaNeu(Short metaNeu) {
		this.metaNeu = metaNeu;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
	}

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

}