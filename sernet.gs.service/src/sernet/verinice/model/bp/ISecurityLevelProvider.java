package sernet.verinice.model.bp;

/**
 * Extended marker interface to identify CnATreeElements which suffice a bsi
 * security level. Such elements shall be filtered depending on the IT network's
 * proceeding.
 */
public interface ISecurityLevelProvider {
    SecurityLevel getSecurityLevel();
}
