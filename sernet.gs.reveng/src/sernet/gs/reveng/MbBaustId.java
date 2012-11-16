package sernet.gs.reveng;

/**
 * MbBaustId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbBaustId implements java.io.Serializable {

	// Fields

	private Integer bauImpId;
	private Integer bauId;

	// Constructors

	/** default constructor */
	public MbBaustId() {
	}

	/** full constructor */
	public MbBaustId(Integer bauImpId, Integer bauId) {
		this.bauImpId = bauImpId;
		this.bauId = bauId;
	}

	// Property accessors

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

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbBaustId))
			return false;
		MbBaustId castOther = (MbBaustId) other;

		return ((this.getBauImpId() == castOther.getBauImpId()) || (this
				.getBauImpId() != null
				&& castOther.getBauImpId() != null && this.getBauImpId()
				.equals(castOther.getBauImpId())))
				&& ((this.getBauId() == castOther.getBauId()) || (this
						.getBauId() != null
						&& castOther.getBauId() != null && this.getBauId()
						.equals(castOther.getBauId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getBauImpId() == null ? 0 : this.getBauImpId().hashCode());
		result = 37 * result
				+ (getBauId() == null ? 0 : this.getBauId().hashCode());
		return result;
	}

}