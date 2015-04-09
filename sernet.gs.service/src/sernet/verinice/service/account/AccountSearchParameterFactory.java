package sernet.verinice.service.account;

import sernet.verinice.interfaces.IAccountSearchParameter;

public final class AccountSearchParameterFactory {
    
    private AccountSearchParameterFactory() {
        super();
    }

    public static IAccountSearchParameter create() {
        return AccountSearchParameter.newInstance();
    }
    
    public static IAccountSearchParameter createLoginParameter(String login) {
        return AccountSearchParameter.newInstance().setLogin(login);
    }
    
    public static IAccountSearchParameter createAccountGroupParameter(String accountGroup) {
        return AccountSearchParameter.newInstance().setAccountGroup(accountGroup);
    }

    public static IAccountSearchParameter createFirstNameParameter(String firstName) {
        return AccountSearchParameter.newInstance().setFirstName(firstName);
    }
    
    public static IAccountSearchParameter createFamilyNameParameter(String familyName) {
        return AccountSearchParameter.newInstance().setFamilyName(familyName);
    }
    
    public static IAccountSearchParameter createIsAdminParameter(Boolean isAdmin) {
        return AccountSearchParameter.newInstance().setIsAdmin(isAdmin);
    }
    
    public static IAccountSearchParameter createIsScopeOnlyParameter(Boolean isScopeOnly) {
        return AccountSearchParameter.newInstance().setIsScopeOnly(isScopeOnly);
    }
    
    public static IAccountSearchParameter createScopeParameter(Integer scopeId) {
        return AccountSearchParameter.newInstance().setScopeId(scopeId);
    }
}
