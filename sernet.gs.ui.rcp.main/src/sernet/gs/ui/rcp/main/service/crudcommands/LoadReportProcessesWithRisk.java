package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Process;

/**
 * Load Process name, CIA, calculated total risk.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportProcessesWithRisk extends GenericCommand implements ICachedCommand{

    private transient Logger log = Logger.getLogger(LoadReportProcessesWithRisk.class);
    
   private List<List<String>> result = new ArrayList<List<String>>();
   
   public static final String[] COLUMNS = {
     "dbid",
     "abbrev",
     "name",
     "C",  
     "I",  
     "A",  
     "risk",  
   };
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadReportProcessesWithRisk.class);
        }
        return log;
    }

	private String typeId = Process.TYPE_ID;
    private Integer rootElement;
    private List<CnATreeElement> elements;

    private boolean resultInjectedFromCache = false;

    public LoadReportProcessesWithRisk( Integer rootElement) {
	    this.rootElement = rootElement;
	}



    public void execute() {
        if(!resultInjectedFromCache){
            getLog().debug("LoadReportElements for root_object " + rootElement);

            LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
            try {
                command = getCommandService().executeCommand(command);
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
            if (command.getElements() == null || command.getElements().size()==0) {
                this.elements = new ArrayList<CnATreeElement>(0);
                return;
            }
            CnATreeElement root = command.getElements().get(0);

            //if typeId is that of the root object, just return it itself. else look for children:
            ArrayList<CnATreeElement> items = new ArrayList<CnATreeElement>();
            if (this.typeId.equals(root.getTypeId())) {
                this.elements = items;
                this.elements.add(root);
            }
            else {
                try {
                    LoadReportElements elementLoader = new LoadReportElements(typeId, root.getDbId(), true);
                    elementLoader = getCommandService().executeCommand(elementLoader);
                    if(elements == null){
                        elements = new ArrayList<CnATreeElement>(0);
                    }
                    this.elements.addAll(elementLoader.getElements());
                } catch (CommandException e1) {
                    getLog().error("Error while getting elements", e1);
                }
            }

            Collections.sort(elements, new Comparator<CnATreeElement>() {
                @Override
                public int compare(CnATreeElement o1, CnATreeElement o2) {
                    NumericStringComparator comparator = new NumericStringComparator();
                    return comparator.compare(o1.getTitle(), o2.getTitle());
                }
            });

            try {
                calculateRisk();
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
        }
    }

	

   

    /**
     * @param elements2
     * @throws CommandException 
     */
    private void calculateRisk() throws CommandException {
        for (CnATreeElement cnATreeElement : elements) {
            if (cnATreeElement.getTypeId().equals(Process.TYPE_ID)) {
                LoadReportLinkedElements loadAssets = new LoadReportLinkedElements(Asset.TYPE_ID, cnATreeElement.getDbId(), true, false);
                loadAssets = getCommandService().executeCommand(loadAssets);
                List<CnATreeElement> assets = loadAssets.getElements();
                
                int totalRisk=0;
                for (CnATreeElement asset : assets) {
                    LoadReportElementWithLinks command2 = new LoadReportElementWithLinks("incident_scenario", asset.getDbId());
                    command2 = getCommandService().executeCommand(command2);
                    List<CnALink> links = command2.getLinkList();
                    
                    for (CnALink link : links) {
                    	if(link.getRiskConfidentiality() != null){
                    		totalRisk += link.getRiskConfidentiality();
                    	}
                    	if(link.getRiskIntegrity() != null){
                    		totalRisk += link.getRiskIntegrity();
                    	}
                    	if(link.getRiskAvailability() != null){
                    		totalRisk += link.getRiskAvailability();
                    	}
                    }
                }
                
                getLog().debug("Total risk for process " + cnATreeElement.getDbId() + ": " + totalRisk);
                
                ArrayList<String> row = new ArrayList<String>();
                AssetValueAdapter process = new AssetValueAdapter(cnATreeElement);
                row.add(Integer.toString(cnATreeElement.getDbId()));
                row.add(cnATreeElement.getEntity().getSimpleValue(Process.PROP_ABBR));
                row.add(cnATreeElement.getEntity().getSimpleValue(Process.PROP_NAME));
                row.add(Integer.toString(process.getVertraulichkeit()));
                row.add(Integer.toString(process.getIntegritaet()));
                row.add(Integer.toString(process.getVerfuegbarkeit()));
                row.add(Integer.toString(totalRisk));
                result.add(row);
            }
        }
    }

    
  

    /**
     * @return the result
     */
    public List<List<String>> getResult() {
        return result;
    }



    public void getElements(String typeFilter, List<CnATreeElement> items, CnATreeElement parent) {
        for (CnATreeElement child : parent.getChildren()) {
            if (typeFilter != null && typeFilter.length()>0) {
                if (child.getTypeId().equals(typeFilter)) {
                    items.add(child);
                    child.getParent().getTitle();
                }
            } else {
                items.add(child);
                child.getParent().getTitle();
            }
            getElements(typeFilter, items, child);
        }
        
    }



    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElement));
        return cacheID.toString();
    }



    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }



    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.result;
    }
}
