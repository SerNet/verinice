package sernet.gs.reveng;

/**
 * ModZobjBstMassId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstMassId implements java.io.Serializable {

	// Fields

	private Integer zobImpId;
	private Integer zobId;
	private Integer bauImpId;
	private Integer bauId;
	private Integer masImpId;
	private Integer masId;

	// Constructors

	/** default constructor */
	public ModZobjBstMassId() {
	}

	/** full constructor */
	public ModZobjBstMassId(Integer zobImpId, Integer zobId, Integer bauImpId,
			Integer bauId, Integer masImpId, Integer masId) {
		this.zobImpId = zobImpId;
		this.zobId = zobId;
		this.bauImpId = bauImpId;
		this.bauId = bauId;
		this.masImpId = masImpId;
		this.masId = masId;
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

	public Integer getBauImpId() {
		return this.bauImpId;
	}

	public void setBauImpId(Integer bauImpId) {
		this.bauImpId = bauImpId;
	}

	public Integer getBauId() {
		return this.bauId;
	}

	public void setBauId(Integer bauId) {
		this.bauId = bauId;
	}

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
		if (!(other instanceof ModZobjBstMassId))
			return false;
		ModZobjBstMassId castOther = (ModZobjBstMassId) other;

		return ((this.getZobImpId() == castOther.getZobImpId()) || (this
				.getZobImpId() != null
				&& castOther.getZobImpId() != null && this.getZobImpId()
				.equals(castOther.getZobImpId())))
				&& ((this.getZobId() == castOther.getZobId()) || (this
						.getZobId() != null
						&& castOther.getZobId() != null && this.getZobId()
						.equals(castOther.getZobId())))
				&& ((this.getBauImpId() == castOther.getBauImpId()) || (this
						.getBauImpId() != null
						&& castOther.getBauImpId() != null && this
						.getBauImpId().equals(castOther.getBauImpId())))
				&& ((this.getBauId() == castOther.getBauId()) || (this
						.getBauId() != null
						&& castOther.getBauId() != null && this.getBauId()
						.equals(castOther.getBauId())))
				&& ((this.getMasImpId() == castOther.getMasImpId()) || (this
						.getMasImpId() != null
						&& castOther.getMasImpId() != null && this
						.getMasImpId().equals(castOther.getMasImpId())))
				&& ((this.getMasId() == castOther.getMasId()) || (this
						.getMasId() != null
						&& castOther.getMasId() != null && this.getMasId()
						.equals(castOther.getMasId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZobImpId() == null ? 0 : this.getZobImpId().hashCode());
		result = 37 * result
				+ (getZobId() == null ? 0 : this.getZobId().hashCode());
		result = 37 * result
				+ (getBauImpId() == null ? 0 : this.getBauImpId().hashCode());
		result = 37 * result
				+ (getBauId() == null ? 0 : this.getBauId().hashCode());
		result = 37 * result
				+ (getMasImpId() == null ? 0 : this.getMasImpId().hashCode());
		result = 37 * result
				+ (getMasId() == null ? 0 : this.getMasId().hashCode());
		return result;
	}

}