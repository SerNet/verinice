package sernet.gs.reveng;

/**
 * MSchutzbedarfkategTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MSchutzbedarfkategTxtId implements java.io.Serializable {

	// Fields

	private Integer sbkImpId;
	private Short sbkId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MSchutzbedarfkategTxtId() {
	}

	/** full constructor */
	public MSchutzbedarfkategTxtId(Integer sbkImpId, Short sbkId, Short sprId) {
		this.sbkImpId = sbkImpId;
		this.sbkId = sbkId;
		this.sprId = sprId;
	}

	// Property accessors

	public Integer getSbkImpId() {
		return this.sbkImpId;
	}

	public void setSbkImpId(Integer sbkImpId) {
		this.sbkImpId = sbkImpId;
	}

	public Short getSbkId() {
		return this.sbkId;
	}

	public void setSbkId(Short sbkId) {
		this.sbkId = sbkId;
	}

	public Short getSprId() {
		return this.sprId;
	}

	public void setSprId(Short sprId) {
		this.sprId = sprId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MSchutzbedarfkategTxtId))
			return false;
		MSchutzbedarfkategTxtId castOther = (MSchutzbedarfkategTxtId) other;

		return ((this.getSbkImpId() == castOther.getSbkImpId()) || (this
				.getSbkImpId() != null
				&& castOther.getSbkImpId() != null && this.getSbkImpId()
				.equals(castOther.getSbkImpId())))
				&& ((this.getSbkId() == castOther.getSbkId()) || (this
						.getSbkId() != null
						&& castOther.getSbkId() != null && this.getSbkId()
						.equals(castOther.getSbkId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getSbkImpId() == null ? 0 : this.getSbkImpId().hashCode());
		result = 37 * result
				+ (getSbkId() == null ? 0 : this.getSbkId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}