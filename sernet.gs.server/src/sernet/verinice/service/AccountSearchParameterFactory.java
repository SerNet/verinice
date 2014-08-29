package sernet.verinice.service;

import sernet.verinice.interfaces.IAccountSearchParameter;

public final class AccountSearchParameterFactory {

    public static IAccountSearchParameter createLoginParameter(String login) {
        return AccountSearchParameter.newInstance().setLogin(login);
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
}
