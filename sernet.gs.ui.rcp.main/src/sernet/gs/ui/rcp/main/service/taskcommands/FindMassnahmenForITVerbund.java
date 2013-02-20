/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.lob.SerializableClob;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * This command loads {@link MassnahmenUmsetzung} instances out of the databse 
 * and wraps them in {@link TodoViewItem}s.
 * 
 * <p>Lists of those objects are needed in the {@link AuditView} and {@link TodoView}.</p>
 * 
 * <p>Since those views should only show the {@link MassnahmenUmsetzung} items for a specific
 * IT-Verbund, this command reflects this behavior.</p>
 * 
 * @author r.schuster@tarent.de
 *
 */
@SuppressWarnings({ "serial", "unchecked" })
public class FindMassnahmenForITVerbund extends FindMassnahmenAbstract {
	
	private transient Logger log = Logger.getLogger(FindMassnahmenForITVerbund.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(FindMassnahmenForITVerbund.class);
        }
        return log;
    }
    
    public static final String FILTER_DATE = "filter-date";
    
    private static final String SQL_DATE = "SELECT cnatreeelement.dbid,properties.propertytype,properties.propertyvalue FROM cnatreeelement " +
    "JOIN entity ON cnatreeelement.entity_id = entity.dbid " +
    "JOIN propertylist ON entity.dbid = propertylist.typedlist_id  JOIN properties ON properties.properties_id = propertylist.dbid  " +
    "JOIN cnatreeelement cnatreeele2_ on cnatreeelement.parent=cnatreeele2_.dbId " +
    "JOIN cnatreeelement cnatreeele3_ on cnatreeele2_.parent=cnatreeele3_.dbId " +
    "JOIN cnatreeelement cnatreeele7_ on cnatreeele3_.parent=cnatreeele7_.dbId " +
    "where (cnatreeele7_.parent = :id or cnatreeele2_.parent = :id2) " +
    "AND cnatreeelement.object_type='massnahmen-umsetzung' " +
    "and properties.propertytype='mnums_umsetzungbis' " +
    "order by properties.propertyvalue";
    
    // PostgreSQL to select all Massnahmen dbids without a mnums_umsetzungbis property
    // because of the aggregat function bool_and this works for PostgreSQL only
    /*
    private static final String SQL = "SELECT dbid FROM (SELECT cnatreeelement.dbid as dbid,bool_and(properties.propertytype!='mnums_umsetzungbis') as nodate " +
    		"FROM cnatreeelement " +
    		"JOIN entity ON cnatreeelement.entity_id = entity.dbid " +
    		"JOIN propertylist ON entity.dbid = propertylist.typedlist_id " +
    		"JOIN properties ON properties.properties_id = propertylist.dbid " +
    		"JOIN cnatreeelement cnatreeele2_ on cnatreeelement.parent=cnatreeele2_.dbId " +
    		"JOIN cnatreeelement cnatreeele3_ on cnatreeele2_.parent=cnatreeele3_.dbId " +
    		"JOIN cnatreeelement cnatreeele7_ on cnatreeele3_.parent=cnatreeele7_.dbId " +
    		"WHERE (cnatreeele7_.parent = :id or cnatreeele2_.parent = :id2) " +
    		"GROUP BY cnatreeelement.dbid) AS result " +
    		"WHERE nodate";
    */
    
    private static final String SQL = "SELECT cnatreeelement.dbid,properties.propertytype,properties.propertyvalue FROM cnatreeelement " +
    "JOIN entity ON cnatreeelement.entity_id = entity.dbid " +
    "JOIN propertylist ON entity.dbid = propertylist.typedlist_id " +
    "JOIN properties ON properties.properties_id = propertylist.dbid " +
    "JOIN cnatreeelement cnatreeele2_ on cnatreeelement.parent=cnatreeele2_.dbId " +
    "JOIN cnatreeelement cnatreeele3_ on cnatreeele2_.parent=cnatreeele3_.dbId " +
    "JOIN cnatreeelement cnatreeele7_ on cnatreeele3_.parent=cnatreeele7_.dbId " +
    "WHERE (cnatreeele7_.parent = :id or cnatreeele2_.parent = :id2) " +
    "AND cnatreeelement.object_type='massnahmen-umsetzung' " +
    "ORDER BY cnatreeelement.dbid";
	
    public static final int LOAD_BLOCK_SIZE = 100;
    
	private Integer itverbundDbId = null;
	
	private int loadBlockNumber;
	
	private Properties filter;
	
	private String sortBy;

    private int number;
	
	public FindMassnahmenForITVerbund(Integer dbId) {
		this(dbId, 1, new Properties(), MassnahmenUmsetzung.P_UMSETZUNGBIS);
	}
	
	/**
     * @param dbId
     * @param loadBlockNumber
     */
    public FindMassnahmenForITVerbund(Integer dbId, int loadBlockNumber, Properties filter, String sortBy) {
        super();
        Logger.getLogger(this.getClass()).debug("Looking up Massnahme for IT-Verbund " + dbId);
        this.itverbundDbId = dbId;
        this.loadBlockNumber = loadBlockNumber;
        this.filter = filter;
        this.sortBy = sortBy;
    }

    public void execute() {
		try {
			long start = System.currentTimeMillis();
			if (getLog().isDebugEnabled()) {
			    getLog().debug("FindMassnahmenForITVerbund, itverbundDbId: " + itverbundDbId);
			}
//			List<MassnahmenUmsetzung> list = new ArrayList<MassnahmenUmsetzung>();
			List<MassnahmenUmsetzung> list = null;
			IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
			list = dao.findByCallback(new FindMassnahmenForITVerbundCallback(itverbundDbId,loadBlockNumber,filter,sortBy));
			
			// create display items:
			fillList(list);
			if(getLog().isDebugEnabled()) {
				long runtime = System.currentTimeMillis() - start;
				getLog().debug("FindMassnahmenForITVerbund runtime: " + runtime + " ms.");
			}
		} catch (Exception e) {
		    getLog().error("Error while executing command", e);
			throw new RuntimeCommandException(e);
		}
	}
    
    public int getLoadBlockNumber() {
        return loadBlockNumber;
    }

    public void setLoadBlockNumber(int loadBlockNumber) {
        this.loadBlockNumber = loadBlockNumber;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

	
	private class FindMassnahmenForITVerbundCallback implements HibernateCallback, Serializable {
		
		private Integer itverbundDbId;

		private int loadBlockNumber;
		
		private Properties filter;
	    
	    private String sortBy;
		
		FindMassnahmenForITVerbundCallback(Integer itverbundID) {
			this(itverbundID,1, new Properties(), MassnahmenUmsetzung.P_UMSETZUNGBIS);
		}

		/**
         * @param itverbundDbId
         * @param loadBlockNumber
         */
        public FindMassnahmenForITVerbundCallback(Integer itverbundDbId, int loadBlockNumber, Properties filter, String sortBy) {
            this.itverbundDbId = itverbundDbId;
            this.loadBlockNumber = loadBlockNumber;
            this.filter = filter;
            this.sortBy = sortBy;
        }

        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            long start = System.currentTimeMillis();
            // select _ALL_ id
            SQLQuery sqlQuery = session.createSQLQuery(SQL);
            sqlQuery.setInteger("id", itverbundDbId).setInteger("id2", itverbundDbId);
            List<TaskItem> idList = createIdList(sqlQuery.list());
            if(getLog().isDebugEnabled()) {
                long runtime = System.currentTimeMillis() - start;
                getLog().debug("FindMassnahmenForITVerbund runtime select all ids: " + runtime + " ms.");
            }
            setNumber(idList.size());
            
            List<MassnahmenUmsetzung> result;
            if(!idList.isEmpty()) {
                // load data for one block number
                start = System.currentTimeMillis();
                int arraySize = (FindMassnahmenForITVerbund.LOAD_BLOCK_SIZE<idList.size()) ? FindMassnahmenForITVerbund.LOAD_BLOCK_SIZE : idList.size();
                Integer[] idArray= new Integer[arraySize];
                int startIndex = (loadBlockNumber-1)*LOAD_BLOCK_SIZE;
                for (int i = 0; i < idArray.length && startIndex+i < idList.size(); i++) {
                    idArray[i] = idList.get(startIndex+i).getId();
                }
                Query query = session.createQuery("FROM MassnahmenUmsetzung mn where mn.dbId IN (:idList)");
                query.setParameterList("idList", idArray);
    			query.setReadOnly(true);
    			result = query.list();
    			if(getLog().isDebugEnabled()) {
                    long runtime = System.currentTimeMillis() - start;
                    getLog().debug("FindMassnahmenForITVerbund runtime load data for " + LOAD_BLOCK_SIZE + " elements: " + runtime + " ms.");
                }
            } else {
                result = Collections.emptyList();
            }
			
			return result;
		}

        /**
         * @param list
         * @return
         */
        private List<TaskItem> createIdList(List result) {
            List<TaskItem> idList = new LinkedList<TaskItem>();
            Integer id = null;
            Properties props = new Properties();
            for (Iterator iterator = result.iterator(); iterator.hasNext();) {
                Object[] row = (Object[]) iterator.next();
                Integer currentId = null;
                if(row[0]!=null) {
                    // for Oracle row[0] is BigDecimal (bug 165)
                    if(row[0] instanceof BigDecimal) {
                        currentId = ((BigDecimal)row[0]).intValue();
                    } else {
                        currentId = (Integer) row[0];
                    }
                }
                if(id==null || (currentId!=null && id.intValue()!=currentId.intValue())) {
                    if(id!=null && checkFilter(props,filter)) {
                        idList.add(new TaskItem(id, props.get(sortBy)));               
                    }
                    id=currentId;               
                    props.clear();          
                }  
                if(row[2]!=null) {
                    props.put(row[1], row[2]);
                }
            }
            if(id!=null && checkFilter(props,filter)) {
                idList.add(new TaskItem(id, props.get(sortBy)));
            }
            Collections.sort(idList);
            return idList;
        }

        /**
         * @param props
         * @param filter
         * @return
         */
        private boolean checkFilter(Properties props, Properties filter) {
            boolean result = false;
            Object filterDate = filter.get(FindMassnahmenForITVerbund.FILTER_DATE);
            result = checkUmsetzungBisFilter(props.get(filterDate),filter.get(filterDate))
            && checkUmsetzungFilter(props.get(MassnahmenUmsetzung.P_UMSETZUNG),filter.get(MassnahmenUmsetzung.P_UMSETZUNG))
            && checkSiegelFilter(props.get(MassnahmenUmsetzung.P_SIEGEL),filter.get(MassnahmenUmsetzung.P_SIEGEL));
            
            return result;
        }

        /**
         * @param object
         * @param object2
         * @return
         */
        private boolean checkSiegelFilter(Object prop, Object filter) {
            // for Oracle prop is SerializableClob (bug 165)
            if(prop instanceof SerializableClob) {
                SerializableClob clob = (SerializableClob) prop;
                try {
                    prop = clob.getSubString(Long.valueOf(1).longValue(), Long.valueOf(clob.length()).intValue());
                } catch (SQLException e) {
                    log.error("Error while getting string value from clob", e);
                }
            }
            return (filter==null) 
                    || ((Set) filter).contains(prop);
        }

        /**
         * @param object
         * @param object2
         */
        private boolean checkUmsetzungFilter(Object prop, Object filter) {
            String value = null;
            // for Oracle prop is SerializableClob (bug 165)
            if(prop instanceof SerializableClob) {
                SerializableClob clob = (SerializableClob) prop;
                try {
                    value = clob.getSubString(Long.valueOf(1).longValue(), Long.valueOf(clob.length()).intValue());
                } catch (SQLException e) {
                    log.error("Error while getting string value from clob", e);
                }
            } else {
                value = (String) prop;
            }
            return (filter==null) 
                    || ((Set) filter).contains(value)
                    || (((Set) filter).contains(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET) && (value==null || value.isEmpty()));

        }

        /**
         * @param props
         * @param filter
         * @return
         */
        private boolean checkUmsetzungBisFilter(Object prop, Object filter) {
            return (filter==null && prop==null) || (filter!=null && prop!=null);
        }
        

	}

}
