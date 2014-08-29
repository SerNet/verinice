package sernet.verinice.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.engine.query.HQLQueryPlan;

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
        List<String> parameterList = new ArrayList<String>(10);
        
        sbHql.append("from Configuration as conf");
        
        if(parameter.getLogin()!=null) {
            sbHql.append(createPropertyJoin(1));
            sbHql.append(" where");
            sbHql.append(createWhere(1));
            parameterList.add(Configuration.PROP_USERNAME);
            parameterList.add(parameter.getLogin());
        }
        
        String hql = sbHql.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Hql: " + hql);
        }
        
        return new HqlQuery(hql, parameterList.toArray());
    }

    private static String createPropertyJoin(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" inner join fetch conf.entity as entity_").append(i);
        sb.append(" inner join fetch entity_").append(i).append(".typedPropertyLists as propertyList_").append(i);
        sb.append(" inner join fetch propertyList_").append(i).append(".properties as props_").append(i);
        return sb.toString();
    }
    
    private static String createWhere(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" props_").append(i).append(".propertyType = ?");
        sb.append(" and props_").append(i).append(".propertyValue like ?");
        return sb.toString();
    }
}

    
