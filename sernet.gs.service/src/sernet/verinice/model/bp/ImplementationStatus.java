package sernet.verinice.model.bp;

/**
 * The implementation status of a safeguard or requirement
 */
public enum ImplementationStatus {
    YES, NO, PARTIALLY, NOT_APPLICABLE;

    private final String label;

    private ImplementationStatus() {
        label = Messages.getString(getClass().getSimpleName() + "." + this.name());
    }

    public String getLabel() {
        return label;
    }
}
