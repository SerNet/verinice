package sernet.verinice.interfaces;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de> 
 */
public interface IAccountSearchParameter {

    public static final String LOGIN = "login";
    public static final String FIRST_NAME = "firstName";
    public static final String FAMILY_NAME = "familyName";
    public static final String IS_ADMIN = "isAdmin";
    public static final String IS_LOCAL_ADMIN = "isLocalAdmin";
    public static final String IS_SCOPE_ONLY = "isScopeOnly";
    public static final String IS_DEACTIVATED = "isDeactivated";
    public static final String SCOPE_ID = "scopeId";
    
    int getNumberOfAccountParameter();
    int getNumberOfPersonParameter();
    boolean isParameter();
    boolean isAccountParameter();
    boolean isPersonParameter();
    
    IAccountSearchParameter setParameter(String name, Object value);
    
    String getLogin();
    IAccountSearchParameter setLogin(String login);
    
    String getAccountGroup();
    IAccountSearchParameter setAccountGroup(String accountGroup);

    String getFirstName();
    IAccountSearchParameter setFirstName(String firstName);
    
    String getFamilyName();
    IAccountSearchParameter setFamilyName(String familyName);
    
    Boolean isAdmin();
    IAccountSearchParameter setIsAdmin(Boolean isAdmin);
    
    Boolean isLocalAdmin();

    IAccountSearchParameter setIsLocalAdmin(Boolean isLocalAdmin);

    Boolean isScopeOnly();
    IAccountSearchParameter setIsScopeOnly(Boolean isScopeOnly);
    
    Boolean isDeactivated();
    IAccountSearchParameter setIsDeactivated(Boolean isDeactivated);
    
    Integer getScopeId();
    IAccountSearchParameter setScopeId(Integer scopeId);
    
}
