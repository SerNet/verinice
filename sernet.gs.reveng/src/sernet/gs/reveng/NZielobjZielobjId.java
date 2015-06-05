package sernet.gs.reveng;

/**
 * NZielobjZielobjId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NZielobjZielobjId implements java.io.Serializable {

	// Fields

	private Integer zobImpId;
	private Integer zobId1;
	private Integer zobId2;

	// Constructors

	/** default constructor */
	public NZielobjZielobjId() {
	}

	/** full constructor */
	public NZielobjZielobjId(Integer zobImpId, Integer zobId1, Integer zobId2) {
		this.zobImpId = zobImpId;
		this.zobId1 = zobId1;
		this.zobId2 = zobId2;
	}

	// Property accessors

	public Integer getZobImpId() {
		return this.zobImpId;
	}

	public void setZobImpId(Integer zobImpId) {
		this.zobImpId = zobImpId;
	}

	public Integer getZobId1() {
		return this.zobId1;
	}

	public void setZobId1(Integer zobId1) {
		this.zobId1 = zobId1;
	}

	public Integer getZobId2() {
		return this.zobId2;
	}

	public void setZobId2(Integer zobId2) {
		this.zobId2 = zobId2;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof NZielobjZielobjId))
			return false;
		NZielobjZielobjId castOther = (NZielobjZielobjId) other;

		return ((this.getZobImpId() == castOther.getZobImpId()) || (this
				.getZobImpId() != null
				&& castOther.getZobImpId() != null && this.getZobImpId()
				.equals(castOther.getZobImpId())))
				&& ((this.getZobId1() == castOther.getZobId1()) || (this
						.getZobId1() != null
						&& castOther.getZobId1() != null && this.getZobId1()
						.equals(castOther.getZobId1())))
				&& ((this.getZobId2() == castOther.getZobId2()) || (this
						.getZobId2() != null
						&& castOther.getZobId2() != null && this.getZobId2()
						.equals(castOther.getZobId2())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZobImpId() == null ? 0 : this.getZobImpId().hashCode());
		result = 37 * result
				+ (getZobId1() == null ? 0 : this.getZobId1().hashCode());
		result = 37 * result
				+ (getZobId2() == null ? 0 : this.getZobId2().hashCode());
		return result;
	}

}