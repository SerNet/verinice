package sernet.gs.ui.rcp.main.service.crudcommands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.LinkKategorie;
import sernet.verinice.model.bsi.MassnahmeKategorie;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import sernet.verinice.model.ds.Zweckbestimmung;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.Organization;

public class LoadReportElements extends GenericCommand implements ICachedCommand{

    private transient Logger log = Logger.getLogger(LoadReportElements.class);
    
    private boolean resultInjectedFromCache = false;
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadReportElements.class);
        }
        return log;
    }

	private String typeId;
    private Integer rootElement;
    private List<CnATreeElement> elements;
    
    private String[] specialGSClasses = new String[]{
            FinishedRiskAnalysis.TYPE_ID,
            BausteinUmsetzung.TYPE_ID,
            Datenverarbeitung.TYPE_ID,
            Personengruppen.TYPE_ID,
            StellungnahmeDSB.TYPE_ID,
            VerantwortlicheStelle.TYPE_ID,
            Verarbeitungsangaben.TYPE_ID,
            Zweckbestimmung.TYPE_ID,
            GefaehrdungsUmsetzung.TYPE_ID
    };
    
    private boolean useScopeID = false;
    
    public LoadReportElements(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}

    public LoadReportElements(String typeId, Integer rootElement, boolean useScopeID){
        this(typeId, rootElement);
        this.useScopeID = useScopeID;
    }
   
    
	public void execute() {
	    elements = new ArrayList<CnATreeElement>(0);
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

	        if(!useScopeID || !hasScopeID(root)){

	            loadElementsRecursive(root);


	        } else {
	            elements = new ArrayList<CnATreeElement>(0);
	            loadElementsUsingScopeId(root);
	        }
	        if(elements != null){
	            sortResult();
	        }
	    }
	}

    private void loadElementsRecursive(CnATreeElement root) {
        //if typeId is that of the root object, just return it itself. else look for children:
        ArrayList<CnATreeElement> items = new ArrayList<CnATreeElement>();
        if (this.typeId.equals(root.getTypeId())) {
            this.elements = items;
            this.elements.add(root);
        }
        else {
            elements = getElements(typeId, root);
        }
    }

    private void loadElementsUsingScopeId(CnATreeElement root) {
        if(root instanceof Organization || root instanceof ITVerbund){
            try {
                LoadCnAElementByScopeId scopeCommand = new LoadCnAElementByScopeId(rootElement, typeId);
                scopeCommand = getCommandService().executeCommand(scopeCommand);
                elements.addAll(scopeCommand.getResults());
            } catch (CommandException e) {
                log.error("Error while retrieving elements via scopeid", e);
            }
        }
    }

    private void sortResult() {
        Collections.sort(elements, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                NumericStringComparator comparator = new NumericStringComparator();
                return comparator.compare(o1.getTitle(), o2.getTitle());
            }
        });
    }

	
	private boolean hasScopeID(CnATreeElement root){
	    return (root instanceof ITVerbund) || (root instanceof Organization);
	}
  

    /**
     * @return the elements
     */
    public List<CnATreeElement> getElements() {
        return elements;
    }

    public List<CnATreeElement> getElements(String typeFilter, CnATreeElement parent) {
        ArrayList<CnATreeElement> children = new ArrayList<CnATreeElement>(0);
        for (CnATreeElement child : parent.getChildren()) {
            if (typeFilter != null && typeFilter.length()>0) {
                if (child.getTypeId().equals(typeFilter)) {
                    children.add(child);
                    child.getParent().getTitle();
                } 
            } else {
                children.add(child);
                child.getParent().getTitle();
            }
            if(child instanceof IISO27kGroup){ // ism element that can contain children
                IISO27kGroup g = (IISO27kGroup)child;
                if(Arrays.asList(g.getChildTypes()).contains(typeFilter) || g.getTypeId().equals(typeFilter) || g instanceof AuditGroup || g instanceof Audit){
                    children.addAll(getElements(typeFilter, child));
                }
            // gs elements that can contain children
            } else if(child instanceof IBSIStrukturKategorie){ 
                if(isGSKategorieAndCanContain((IBSIStrukturKategorie)child, typeFilter) || Arrays.asList(specialGSClasses).contains(typeFilter)){
                    children.addAll(getElements(typeFilter, child));
                }
            } else if(child instanceof IBSIStrukturElement && 
                    (isGSElementAndCanContain((IBSIStrukturElement)child, typeFilter) || Arrays.asList(specialGSClasses).contains(typeFilter))){
                    children.addAll(getElements(typeFilter, child));
            } 
        }
        return children;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(typeId);
        cacheID.append(String.valueOf(rootElement));
        cacheID.append(String.valueOf(useScopeID));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.elements = (ArrayList<CnATreeElement>)result;
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
        return elements;
    }
    
    private boolean isGSElementAndCanContain(IBSIStrukturElement element, String typeId){
        Class[] gsClasses = new Class[]{
                FinishedRiskAnalysis.class,
                BausteinUmsetzung.class,
                LinkKategorie.class,
                Datenverarbeitung.class,
                Personengruppen.class,
                StellungnahmeDSB.class,
                VerantwortlicheStelle.class,
                Verarbeitungsangaben.class,
                Zweckbestimmung.class
        };
        
        CnATreeElement potentialChild = getPotentialChild(typeId, gsClasses);
        
        if(potentialChild != null){
            if(element instanceof Anwendung){
                return ((Anwendung)element).canContain(potentialChild);
            } else if(element instanceof Client){
                return ((Client)element).canContain(potentialChild);
            } else if(element instanceof Gebaeude){
                return ((Gebaeude)element).canContain(potentialChild);
            } else if(element instanceof NetzKomponente){
                return ((NetzKomponente)element).canContain(potentialChild);
            } else if(element instanceof Person){
                return ((Person)element).canContain(potentialChild);
            } else if(element instanceof Raum){
                return ((Raum)element).canContain(potentialChild);
            } else if(element instanceof Server){
                return ((Server)element).canContain(potentialChild);
            } else if(element instanceof SonstIT){
                return ((SonstIT)element).canContain(potentialChild);
            } else if(element instanceof TelefonKomponente){
                return ((TelefonKomponente)element).canContain(potentialChild);
            }
        }
        return false;
    }
    
    private boolean isGSKategorieAndCanContain(IBSIStrukturKategorie kategorie, String typeId){
        
        Class[] gsClasses = new Class[]{Anwendung.class,
                                          Client.class,
                                          Gebaeude.class,
                                          MassnahmenUmsetzung.class,
                                          NetzKomponente.class,
                                          Person.class,
                                          Raum.class,
                                          Server.class,
                                          SonstIT.class,
                                          TelefonKomponente.class
                                          };
        
        CnATreeElement potentialChild = getPotentialChild(typeId, gsClasses);

        if(potentialChild != null){
            if(kategorie instanceof AnwendungenKategorie){
                return ((AnwendungenKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof ClientsKategorie){
                return ((ClientsKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof GebaeudeKategorie){
                return ((GebaeudeKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof MassnahmeKategorie){
                return ((MassnahmeKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof NKKategorie){
                return ((NKKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof PersonenKategorie){
                return ((PersonenKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof RaeumeKategorie){
                return ((RaeumeKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof ServerKategorie){
                return ((ServerKategorie)kategorie).canContain(potentialChild);
            } else if((kategorie instanceof SonstigeITKategorie)){
                return ((SonstigeITKategorie)kategorie).canContain(potentialChild);
            } else if(kategorie instanceof TKKategorie){
                return ((TKKategorie)kategorie).canContain(potentialChild);
            }
        } 
        return false;
    }

    /**
     * @param typeId
     * @param gsClasses
     * @return
     */
    private CnATreeElement getPotentialChild(String typeId, Class[] gsClasses) {
        CnATreeElement potentialChild = null;
        
        for(Class<? extends CnATreeElement> gsClass : gsClasses){
            Object instance;
            try {
                final Class classToInstantiate = Class.forName(gsClass.getCanonicalName());
                instance = classToInstantiate.getConstructor(CnATreeElement.class).newInstance(new Object[]{null});
                if(instance instanceof CnATreeElement){
                    CnATreeElement element = (CnATreeElement)instance;
                    if(element.getTypeId().equals(typeId)){
                        potentialChild = element;
                    }
                }
            } catch (InstantiationException e) {
                getLog().error("Error while instantiating Object", e);
            } catch (IllegalAccessException e) {
                getLog().error("Wrong element access", e);
            } catch (ClassNotFoundException e){
                getLog().error("Wrong class selected", e);
            } catch (NoSuchMethodException e){
                getLog().error("Constructor not found", e);
            } catch (InvocationTargetException e){
                getLog().error("Wrong invocation on target", e);
            }
        }
        return potentialChild;
    }
}
