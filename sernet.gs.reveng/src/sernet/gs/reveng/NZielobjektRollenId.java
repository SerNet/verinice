package sernet.gs.reveng;

/**
 * NZielobjektRollenId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NZielobjektRollenId implements java.io.Serializable {

	// Fields

	private Integer zobImpId;
	private Integer zobId;
	private Integer rolImpId;
	private Integer rolId;

	// Constructors

	/** default constructor */
	public NZielobjektRollenId() {
	}

	/** full constructor */
	public NZielobjektRollenId(Integer zobImpId, Integer zobId,
			Integer rolImpId, Integer rolId) {
		this.zobImpId = zobImpId;
		this.zobId = zobId;
		this.rolImpId = rolImpId;
		this.rolId = rolId;
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

	public Integer getRolImpId() {
		return this.rolImpId;
	}

	public void setRolImpId(Integer rolImpId) {
		this.rolImpId = rolImpId;
	}

	public Integer getRolId() {
		return this.rolId;
	}

	public void setRolId(Integer rolId) {
		this.rolId = rolId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof NZielobjektRollenId))
			return false;
		NZielobjektRollenId castOther = (NZielobjektRollenId) other;

		return ((this.getZobImpId() == castOther.getZobImpId()) || (this
				.getZobImpId() != null
				&& castOther.getZobImpId() != null && this.getZobImpId()
				.equals(castOther.getZobImpId())))
				&& ((this.getZobId() == castOther.getZobId()) || (this
						.getZobId() != null
						&& castOther.getZobId() != null && this.getZobId()
						.equals(castOther.getZobId())))
				&& ((this.getRolImpId() == castOther.getRolImpId()) || (this
						.getRolImpId() != null
						&& castOther.getRolImpId() != null && this
						.getRolImpId().equals(castOther.getRolImpId())))
				&& ((this.getRolId() == castOther.getRolId()) || (this
						.getRolId() != null
						&& castOther.getRolId() != null && this.getRolId()
						.equals(castOther.getRolId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZobImpId() == null ? 0 : this.getZobImpId().hashCode());
		result = 37 * result
				+ (getZobId() == null ? 0 : this.getZobId().hashCode());
		result = 37 * result
				+ (getRolImpId() == null ? 0 : this.getRolImpId().hashCode());
		result = 37 * result
				+ (getRolId() == null ? 0 : this.getRolId().hashCode());
		return result;
	}

}