package sernet.gs.reveng;

/**
 * MbDringlichkeitTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbDringlichkeitTxtId implements java.io.Serializable {

	// Fields

	private Integer drgImpId;
	private Integer drgId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MbDringlichkeitTxtId() {
	}

	/** full constructor */
	public MbDringlichkeitTxtId(Integer drgImpId, Integer drgId, Short sprId) {
		this.drgImpId = drgImpId;
		this.drgId = drgId;
		this.sprId = sprId;
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
		if (!(other instanceof MbDringlichkeitTxtId))
			return false;
		MbDringlichkeitTxtId castOther = (MbDringlichkeitTxtId) other;

		return ((this.getDrgImpId() == castOther.getDrgImpId()) || (this
				.getDrgImpId() != null
				&& castOther.getDrgImpId() != null && this.getDrgImpId()
				.equals(castOther.getDrgImpId())))
				&& ((this.getDrgId() == castOther.getDrgId()) || (this
						.getDrgId() != null
						&& castOther.getDrgId() != null && this.getDrgId()
						.equals(castOther.getDrgId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getDrgImpId() == null ? 0 : this.getDrgImpId().hashCode());
		result = 37 * result
				+ (getDrgId() == null ? 0 : this.getDrgId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}