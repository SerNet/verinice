package sernet.gs.reveng;

/**
 * MbSchichtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbSchichtId implements java.io.Serializable {

	// Fields

	private Integer schImpId;
	private Integer schId;

	// Constructors

	/** default constructor */
	public MbSchichtId() {
	}

	/** full constructor */
	public MbSchichtId(Integer schImpId, Integer schId) {
		this.schImpId = schImpId;
		this.schId = schId;
	}

	// Property accessors

	public Integer getSchImpId() {
		return this.schImpId;
	}

	public void setSchImpId(Integer schImpId) {
		this.schImpId = schImpId;
	}

	public Integer getSchId() {
		return this.schId;
	}

	public void setSchId(Integer schId) {
		this.schId = schId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbSchichtId))
			return false;
		MbSchichtId castOther = (MbSchichtId) other;

		return ((this.getSchImpId() == castOther.getSchImpId()) || (this
				.getSchImpId() != null
				&& castOther.getSchImpId() != null && this.getSchImpId()
				.equals(castOther.getSchImpId())))
				&& ((this.getSchId() == castOther.getSchId()) || (this
						.getSchId() != null
						&& castOther.getSchId() != null && this.getSchId()
						.equals(castOther.getSchId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getSchImpId() == null ? 0 : this.getSchImpId().hashCode());
		result = 37 * result
				+ (getSchId() == null ? 0 : this.getSchId().hashCode());
		return result;
	}

}