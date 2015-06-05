package sernet.gs.reveng;

/**
 * MbMassnId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbMassnId implements java.io.Serializable {

	// Fields

	private Integer masImpId;
	private Integer masId;

	// Constructors

	/** default constructor */
	public MbMassnId() {
	}

	/** full constructor */
	public MbMassnId(Integer masImpId, Integer masId) {
		this.masImpId = masImpId;
		this.masId = masId;
	}

	// Property accessors

	public Integer getMasImpId() {
		return this.masImpId;
	}

	public void setMasImpId(Integer masImpId) {
		this.masImpId = masImpId;
	}

	public Integer getMasId() {
		return this.masId;
	}

	public void setMasId(Integer masId) {
		this.masId = masId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbMassnId))
			return false;
		MbMassnId castOther = (MbMassnId) other;

		return ((this.getMasImpId() == castOther.getMasImpId()) || (this
				.getMasImpId() != null
				&& castOther.getMasImpId() != null && this.getMasImpId()
				.equals(castOther.getMasImpId())))
				&& ((this.getMasId() == castOther.getMasId()) || (this
						.getMasId() != null
						&& castOther.getMasId() != null && this.getMasId()
						.equals(castOther.getMasId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getMasImpId() == null ? 0 : this.getMasImpId().hashCode());
		result = 37 * result
				+ (getMasId() == null ? 0 : this.getMasId().hashCode());
		return result;
	}

}