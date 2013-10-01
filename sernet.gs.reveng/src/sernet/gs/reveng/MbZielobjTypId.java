package sernet.gs.reveng;

/**
 * MbZielobjTypId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjTypId implements java.io.Serializable {

	// Fields

	private Integer zotImpId;
	private Integer zotId;

	// Constructors

	/** default constructor */
	public MbZielobjTypId() {
	}

	/** full constructor */
	public MbZielobjTypId(Integer zotImpId, Integer zotId) {
		this.zotImpId = zotImpId;
		this.zotId = zotId;
	}

	// Property accessors

	public Integer getZotImpId() {
		return this.zotImpId;
	}

	public void setZotImpId(Integer zotImpId) {
		this.zotImpId = zotImpId;
	}

	public Integer getZotId() {
		return this.zotId;
	}

	public void setZotId(Integer zotId) {
		this.zotId = zotId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbZielobjTypId))
			return false;
		MbZielobjTypId castOther = (MbZielobjTypId) other;

		return ((this.getZotImpId() == castOther.getZotImpId()) || (this
				.getZotImpId() != null
				&& castOther.getZotImpId() != null && this.getZotImpId()
				.equals(castOther.getZotImpId())))
				&& ((this.getZotId() == castOther.getZotId()) || (this
						.getZotId() != null
						&& castOther.getZotId() != null && this.getZotId()
						.equals(castOther.getZotId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZotImpId() == null ? 0 : this.getZotImpId().hashCode());
		result = 37 * result
				+ (getZotId() == null ? 0 : this.getZotId().hashCode());
		return result;
	}

}