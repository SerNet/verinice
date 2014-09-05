package sernet.verinice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;

import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

public class AccountSearchQueryFactory {

    private static final Logger LOG = Logger.getLogger(AccountSearchQueryFactory.class);
    
    public static DetachedCriteria createCriteria(IAccountSearchParameter parameter) {
        DetachedCriteria crit = DetachedCriteria.forClass(Configuration.class);
        return crit;
    }
    
    public static final HqlQuery createHql(IAccountSearchParameter parameter) {
        StringBuilder sbHql = new StringBuilder();
        List<Object> parameterList = new ArrayList<Object>(10);
        
        sbHql.append("from Configuration as conf");
        
        for (int i = 0; i < parameter.getNumberOfAccountParameter(); i++) {
            sbHql.append(createAccountPropertyJoin(i));          
        }   
        for (int i = 0; i < parameter.getNumberOfPersonParameter(); i++) {
            sbHql.append(createPersonPropertyJoin(i));          
        }
        if(parameter.getScopeId()!=null) {
            sbHql.append(createPersonJoin(parameter.getNumberOfPersonParameter()));
        }
        
        if(parameter.isParameter()) {
            sbHql.append(" where");
        }        
        for (int i = 0; i < parameter.getNumberOfAccountParameter(); i++) {
            sbHql.append(createAccountPropertyWhere(i)); 
            if(((i+1)<parameter.getNumberOfAccountParameter()) || parameter.isPersonParameter()) {
                sbHql.append(" and");
            }
        }
        for (int i = 0; i < parameter.getNumberOfPersonParameter(); i++) {
            sbHql.append(createPersonPropertyWhere(i)); 
            if((i+1)<parameter.getNumberOfPersonParameter()) {
                sbHql.append(" and");
            }
        }
        if(parameter.getScopeId()!=null) {
            if(parameter.isAccountParameter() || parameter.isPersonParameter()) {
                sbHql.append(" and");
            }
            sbHql.append(createPersonWhere(parameter.getNumberOfPersonParameter(),"scopeId"));
        }
        
        if(parameter.getLogin()!=null) {
            parameterList.add(Configuration.PROP_USERNAME);
            parameterList.add(addWildcards(parameter.getLogin()));
        }        
        if(parameter.isAdmin()!=null) {
            parameterList.add(Configuration.PROP_ISADMIN);
            parameterList.add(parameter.isAdmin() ? Configuration.PROP_ISADMIN_YES : Configuration.PROP_ISADMIN_NO);
        }        
        if(parameter.isScopeOnly()!=null) {
            parameterList.add(Configuration.PROP_SCOPE);
            parameterList.add(parameter.isScopeOnly() ? Configuration.PROP_SCOPE_YES : Configuration.PROP_SCOPE_NO);
        }         
        if(parameter.getFirstName()!=null) {
            parameterList.add(PersonIso.PROP_NAME);
            parameterList.add(addWildcards(parameter.getFirstName()));
        }        
        if(parameter.getFamilyName()!=null) {
            parameterList.add(PersonIso.PROP_SURNAME);
            parameterList.add(addWildcards(parameter.getFamilyName()));
        }       
        if(parameter.getScopeId()!=null) {
            parameterList.add(parameter.getScopeId());
        }       
        String hql = sbHql.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Hql: " + hql);
        }       
        return new HqlQuery(hql, parameterList.toArray());
    }

    private static String addWildcards(String login) {
        StringBuilder sb = new StringBuilder();
        sb.append("%").append(login).append("%");
        return sb.toString();
    }

    private static String createAccountPropertyJoin(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" inner join fetch conf.entity as entity_").append(i);
        sb.append(" inner join fetch entity_").append(i).append(".typedPropertyLists as propertyList_").append(i);
        sb.append(" inner join fetch propertyList_").append(i).append(".properties as props_").append(i);
        return sb.toString();
    }
    
    private static String createPersonPropertyJoin(int i) {
        StringBuilder sb = new StringBuilder();      
        sb.append(" inner join fetch conf.person as person_").append(i);
        sb.append(" inner join fetch person_").append(i).append(".entity as pEntity_").append(i);
        sb.append(" inner join fetch pEntity_").append(i).append(".typedPropertyLists as pPropertyList_").append(i);
        sb.append(" inner join fetch pPropertyList_").append(i).append(".properties as pProps_").append(i);
        return sb.toString();
    }
    
    private static String createPersonJoin(int i) {
        StringBuilder sb = new StringBuilder();      
        sb.append(" inner join fetch conf.person as person_").append(i);
        return sb.toString();
    }
    
    private static String createAccountPropertyWhere(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" props_").append(i).append(".propertyType = ?");
        sb.append(" and lower(props_").append(i).append(".propertyValue) like lower(?)");
        return sb.toString();
    }
    
    private static String createPersonPropertyWhere(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" pProps_").append(i).append(".propertyType = ?");
        sb.append(" and lower(pProps_").append(i).append(".propertyValue) like lower(?)");
        return sb.toString();
    }
    
    private static String createPersonWhere(int i, String property) {
        StringBuilder sb = new StringBuilder();
        sb.append(" person_").append(i).append(".").append(property).append(" = ?");
        return sb.toString();
    }
    
    public static final HqlQuery createRetrieveHql(Set<Integer> dbIds) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("from Configuration as conf");
        sb.append(" inner join fetch conf.entity as entity");
        sb.append(" inner join fetch entity.typedPropertyLists as propertyList");
        sb.append(" inner join fetch propertyList.properties as props");
        sb.append(" inner join fetch conf.person as person");
        sb.append(" inner join fetch person.entity as pEntity");
        sb.append(" inner join fetch pEntity.typedPropertyLists as pPropertyList");
        sb.append(" inner join fetch pPropertyList.properties as pProps");
        sb.append(" where conf.dbId in (:dbIds)");
        
        String hql = sb.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Hql: " + hql);
        }
        Object[] values;
        if(dbIds.size()==1) {
            values = dbIds.toArray();
        } else {
            values = new Object[]{dbIds.toArray()};
        }
        return new HqlQuery(hql, values);
    }
}

    
