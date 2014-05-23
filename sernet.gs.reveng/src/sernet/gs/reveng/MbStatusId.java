package sernet.gs.reveng;

/**
 * MbStatusId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbStatusId implements java.io.Serializable {

	// Fields

	private Integer staImpId;
	private Integer staId;

	// Constructors

	/** default constructor */
	public MbStatusId() {
	}

	/** full constructor */
	public MbStatusId(Integer staImpId, Integer staId) {
		this.staImpId = staImpId;
		this.staId = staId;
	}

	// Property accessors

	public Integer getStaImpId() {
		return this.staImpId;
	}

	public void setStaImpId(Integer staImpId) {
		this.staImpId = staImpId;
	}

	public Integer getStaId() {
		return this.staId;
	}

	public void setStaId(Integer staId) {
		this.staId = staId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbStatusId))
			return false;
		MbStatusId castOther = (MbStatusId) other;

		return ((this.getStaImpId() == castOther.getStaImpId()) || (this
				.getStaImpId() != null
				&& castOther.getStaImpId() != null && this.getStaImpId()
				.equals(castOther.getStaImpId())))
				&& ((this.getStaId() == castOther.getStaId()) || (this
						.getStaId() != null
						&& castOther.getStaId() != null && this.getStaId()
						.equals(castOther.getStaId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getStaImpId() == null ? 0 : this.getStaImpId().hashCode());
		result = 37 * result
				+ (getStaId() == null ? 0 : this.getStaId().hashCode());
		return result;
	}

}