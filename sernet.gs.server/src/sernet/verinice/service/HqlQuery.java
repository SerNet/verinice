package sernet.verinice.service;

class HqlQuery {
    
    String hql;
    String[] names;
    Object[] params;
    
    public HqlQuery(String hql, Object[] params) {
        super();
        this.hql = hql;
        setParams(params);
    }

    public String getHql() {
        return hql;
    }

    public void setHql(String hql) {
        this.hql = hql;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = (names!=null ) ? names.clone() : null;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
         this.params = (params!=null ) ? params.clone() : null;
    }
}