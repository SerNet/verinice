package sernet.gs.reveng;

/**
 * NmbNotizId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class NmbNotizId implements java.io.Serializable {

	// Fields

	private Integer notizImpId;
	private Integer notizId;

	// Constructors

	/** default constructor */
	public NmbNotizId() {
	}

	/** full constructor */
	public NmbNotizId(Integer notizImpId, Integer notizId) {
		this.notizImpId = notizImpId;
		this.notizId = notizId;
	}

	// Property accessors

	public Integer getNotizImpId() {
		return this.notizImpId;
	}

	public void setNotizImpId(Integer notizImpId) {
		this.notizImpId = notizImpId;
	}

	public Integer getNotizId() {
		return this.notizId;
	}

	public void setNotizId(Integer notizId) {
		this.notizId = notizId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof NmbNotizId))
			return false;
		NmbNotizId castOther = (NmbNotizId) other;

		return ((this.getNotizImpId() == castOther.getNotizImpId()) || (this
				.getNotizImpId() != null
				&& castOther.getNotizImpId() != null && this.getNotizImpId()
				.equals(castOther.getNotizImpId())))
				&& ((this.getNotizId() == castOther.getNotizId()) || (this
						.getNotizId() != null
						&& castOther.getNotizId() != null && this.getNotizId()
						.equals(castOther.getNotizId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37
				* result
				+ (getNotizImpId() == null ? 0 : this.getNotizImpId()
						.hashCode());
		result = 37 * result
				+ (getNotizId() == null ? 0 : this.getNotizId().hashCode());
		return result;
	}

}