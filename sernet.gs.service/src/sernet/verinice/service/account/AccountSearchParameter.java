package sernet.verinice.service.account;

import java.io.Serializable;

import sernet.verinice.interfaces.IAccountSearchParameter;

public class AccountSearchParameter implements IAccountSearchParameter, Serializable {

    private String login;
    private String firstName;
    private String familyName;
    private Boolean isAdmin;
    private Boolean isScopeOnly;
    private Boolean isDeactivated;
    private Integer scopeId;
    private String accountGroup;

    

    public static AccountSearchParameter newInstance() {
        return new AccountSearchParameter();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAccountSearchParameter#getLogin()
     */
    public String getLogin() {
        return login;
    }

    public IAccountSearchParameter setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getAccountGroup(){
        return this.accountGroup;
    }

    public IAccountSearchParameter setAccountGroup(String accountGroup){
        this.accountGroup = accountGroup;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public IAccountSearchParameter setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public IAccountSearchParameter setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public Boolean isAdmin() {
        return isAdmin;
    }

    public IAccountSearchParameter setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
        return this;
    }

    public Boolean isScopeOnly() {
        return isScopeOnly;
    }

    public IAccountSearchParameter setIsScopeOnly(Boolean isScopeOnly) {
        this.isScopeOnly = isScopeOnly;
        return this;
    }

    public Boolean isDeactivated() {
        return isDeactivated;
    }

    public IAccountSearchParameter setIsDeactivated(Boolean isDeactivated) {
        this.isDeactivated = isDeactivated;
        return this;
    }
    
    public Integer getScopeId() {
        return scopeId;
    }

    public IAccountSearchParameter setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
        return this;
    }
    
    @Override
    public IAccountSearchParameter setParameter(String name, Object value) {
        if(FAMILY_NAME.equals(name)) {
            setFamilyName((String) value);
        }
        if(FIRST_NAME.equals(name)) {
            setFirstName((String) value);
        }
        if(LOGIN.equals(name)) {
            setLogin((String) value);
        }
        if(IS_ADMIN.equals(name)) {
            setIsAdmin((Boolean) value);
        }
        if(IS_DEACTIVATED.equals(name)) {
            setIsDeactivated((Boolean) value);
        }
        if(IS_SCOPE_ONLY.equals(name)) {
            setIsScopeOnly((Boolean) value);
        }
        if(SCOPE_ID.equals(name)) {
            setScopeId((Integer) value);
        }
        return this;
    }

    @Override
    public int getNumberOfAccountParameter() {
        int n = 0;
        if(getLogin()!=null) {
            n++;
        }
        if(getAccountGroup()!=null){
            n++;
        }
        if(isAdmin()!=null) {
            n++;
        }
        if(isDeactivated()!=null) {
            n++;
        }
        if(isScopeOnly()!=null) {
            n++;
        }
        return n;
    }
    
    @Override
    public int getNumberOfPersonParameter() {
        int n = 0;
        if(getFamilyName()!=null) {
            n++;
        }
        if(getFirstName()!=null) {
            n++;
        }
        return n;
    }

    @Override
    public boolean isParameter() {
        return isAccountParameter() || isPersonParameter() || getScopeId()!=null;
    } 
    
    @Override
    public boolean isAccountParameter() {
        return getNumberOfAccountParameter()>0 ;
    } 
    
    @Override
    public boolean isPersonParameter() {
        return getNumberOfPersonParameter()>0;
    }

}
