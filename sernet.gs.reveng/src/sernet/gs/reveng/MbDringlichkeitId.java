package sernet.gs.reveng;

/**
 * MbDringlichkeitId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbDringlichkeitId implements java.io.Serializable {

	// Fields

	private Integer drgImpId;
	private Integer drgId;

	// Constructors

	/** default constructor */
	public MbDringlichkeitId() {
	}

	/** full constructor */
	public MbDringlichkeitId(Integer drgImpId, Integer drgId) {
		this.drgImpId = drgImpId;
		this.drgId = drgId;
	}

	// Property accessors

	public Integer getDrgImpId() {
		return this.drgImpId;
	}

	public void setDrgImpId(Integer drgImpId) {
		this.drgImpId = drgImpId;
	}

	public Integer getDrgId() {
		return this.drgId;
	}

	public void setDrgId(Integer drgId) {
		this.drgId = drgId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbDringlichkeitId))
			return false;
		MbDringlichkeitId castOther = (MbDringlichkeitId) other;

		return ((this.getDrgImpId() == castOther.getDrgImpId()) || (this
				.getDrgImpId() != null
				&& castOther.getDrgImpId() != null && this.getDrgImpId()
				.equals(castOther.getDrgImpId())))
				&& ((this.getDrgId() == castOther.getDrgId()) || (this
						.getDrgId() != null
						&& castOther.getDrgId() != null && this.getDrgId()
						.equals(castOther.getDrgId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getDrgImpId() == null ? 0 : this.getDrgImpId().hashCode());
		result = 37 * result
				+ (getDrgId() == null ? 0 : this.getDrgId().hashCode());
		return result;
	}

}