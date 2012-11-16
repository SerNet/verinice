package sernet.gs.reveng;

/**
 * NZobSbId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NZobSbId implements java.io.Serializable {

	// Fields

	private Integer zobImpId;
	private Integer zobId;

	// Constructors

	/** default constructor */
	public NZobSbId() {
	}

	/** full constructor */
	public NZobSbId(Integer zobImpId, Integer zobId) {
		this.zobImpId = zobImpId;
		this.zobId = zobId;
	}

	// Property accessors

	public Integer getZobImpId() {
		return this.zobImpId;
	}

	public void setZobImpId(Integer zobImpId) {
		this.zobImpId = zobImpId;
	}

	public Integer getZobId() {
		return this.zobId;
	}

	public void setZobId(Integer zobId) {
		this.zobId = zobId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof NZobSbId))
			return false;
		NZobSbId castOther = (NZobSbId) other;

		return ((this.getZobImpId() == castOther.getZobImpId()) || (this
				.getZobImpId() != null
				&& castOther.getZobImpId() != null && this.getZobImpId()
				.equals(castOther.getZobImpId())))
				&& ((this.getZobId() == castOther.getZobId()) || (this
						.getZobId() != null
						&& castOther.getZobId() != null && this.getZobId()
						.equals(castOther.getZobId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZobImpId() == null ? 0 : this.getZobImpId().hashCode());
		result = 37 * result
				+ (getZobId() == null ? 0 : this.getZobId().hashCode());
		return result;
	}

}