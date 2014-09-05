package sernet.verinice.interfaces;

public interface IAccountSearchParameter {

    int getNumberOfAccountParameter();
    int getNumberOfPersonParameter();
    boolean isParameter();
    boolean isAccountParameter();
    boolean isPersonParameter();
    
    String getLogin();
    IAccountSearchParameter setLogin(String login);
    
    String getFirstName();
    IAccountSearchParameter setFirstName(String firstName);
    
    String getFamilyName();
    IAccountSearchParameter setFamilyName(String familyName);
    
    Boolean isAdmin();
    IAccountSearchParameter setIsAdmin(Boolean isAdmin);
    
    Boolean isScopeOnly();
    IAccountSearchParameter setIsScopeOnly(Boolean isScopeOnly);
    
    Boolean isDeactivated();
    IAccountSearchParameter setIsDeactivated(Boolean isDeactivated);
    
    Integer getScopeId();
    IAccountSearchParameter setScopeId(Integer scopeId);
    
}
