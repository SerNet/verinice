/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
 *  
 * This program is free software: you can redistribute it and/or   
 * modify it under the terms of the GNU Lesser General Public License   
 * as published by the Free Software Foundation, either version 3   
 * of the License, or (at your option) any later version.  
 * This program is distributed in the hope that it will be useful,      
 * but WITHOUT ANY WARRANTY; without even the implied warranty   
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    
 * See the GNU Lesser General Public License for more details.  
 *  
 * You should have received a copy of the GNU Lesser General Public License  
 * along with this program.   
 * If not, see <http://www.gnu.org/licenses/>.  
 *   
 * Contributors:  
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.service.commands.templates;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElement.TemplateType;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.service.commands.AttachmentFileCreationFactory;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.FindRiskAnalysisListsByParentID;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachments;
import sernet.verinice.service.commands.Messages;
import sernet.verinice.service.commands.PropertyLoader;
import sernet.verinice.service.commands.SaveElement;

/**
 * A duplicate of {@link sernet.verinice.service.commands.CopyCommand}, used
 * when property module.templates.active in
 * {@link veriniceserver-plain.properties} is true.
 * 
 * Copies a list of elements with all children to a group. Element types in
 * BLACKLIST are ignored.
 *
 * CopyTempalteCommand uses command SaveElement to save element copies and
 * CreateTemplateImplementationElement to save template implementations.
 * SaveElement and CreateTemplateImplementationElement are a
 * IChangeLoggingCommand and logs all changes from CopyTempalteCommand
 *
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class CopyCommand extends GenericCommand {

    private static final long serialVersionUID = -269076325994387265L;
    private transient Logger log = Logger.getLogger(CopyCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CopyCommand.class);
        }
        return log;
    }

    private static final int FLUSH_LEVEL = 10;
    private final boolean flush = false;

    private String uuidGroup;
    private boolean copyAttachments = false;

    private transient CnATreeElement groupToPasteTo;

    private List<String> uuidList;

    private List<IPostProcessor> postProcessorList;

    private int number = 0;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    private List<String> newElements;

    public CopyCommand(final String uuidGroup, final List<String> uuidList) {
        this(uuidGroup, uuidList, new ArrayList<IPostProcessor>());
    }

    public CopyCommand(final String uuidGroup, final List<String> uuidList, final List<IPostProcessor> postProcessorList) {
        this(uuidGroup, uuidList, postProcessorList, false);
    }

    public CopyCommand(final String uuidGroup, final List<String> uuidList, final List<IPostProcessor> postProcessorList, final boolean copyLinks) {
        super();
        this.uuidGroup = uuidGroup;
        this.uuidList = uuidList;
        this.postProcessorList = postProcessorList;
    }

    /**
     * Copies the elements from uuidList to group with uuidGroup. Calls
     * recursive method copy to copy all children of an element.
     *
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        getLog().debug("Starting execution of CopyCommand");
        try {
            newElements = new ArrayList<String>(0);
            number = 0;
            List<CnATreeElement> rootElementsToCopy = createInsertList(uuidList);
            groupToPasteTo = getDao().findByUuid(uuidGroup, RetrieveInfo.getChildrenInstance().setParent(true).setProperties(true));
            final Map<String, String> sourceDestMap = new HashMap<>();
            for (final CnATreeElement copyElement : rootElementsToCopy) {
                final CnATreeElement newElement = copy(groupToPasteTo, copyElement, sourceDestMap);
                if (newElement != null && newElement.getUuid() != null) {
                    newElements.add(newElement.getUuid());
                    createTaskWhileApplyingTemplateCommand(copyElement, newElement);
                }
            }
            if (getPostProcessorList() != null && !getPostProcessorList().isEmpty()) {
                getDao().flush();
                getDao().clear();
                final List<String> copyElementUuidList = new ArrayList<String>(rootElementsToCopy.size());
                for (final CnATreeElement element : rootElementsToCopy) {
                    copyElementUuidList.add(element.getUuid());
                }
                for (final IPostProcessor postProcessor : getPostProcessorList()) {
                    postProcessor.process(getCommandService(), copyElementUuidList, sourceDestMap);
                }
            }
        } catch (final Exception e) {
            getLog().error("Error while copying element", e); //$NON-NLS-1$
            throw new RuntimeException("Error while copying element", e); //$NON-NLS-1$
        }
    }

    private CnATreeElement copy(final CnATreeElement groupToCopyTo, final CnATreeElement elementToCopy, final Map<String, String> sourceDestMap) throws CommandException, IOException {
        CnATreeElement elementCopy = elementToCopy;
        if (elementToCopy != null && elementToCopy.getTypeId() != null && groupToCopyTo.canContain(elementToCopy)) {
            if (elementToCopy instanceof FinishedRiskAnalysis) {
                elementCopy = copyRiskAnalysis(groupToCopyTo, elementToCopy, sourceDestMap);
                afterCopy(elementToCopy, elementCopy, sourceDestMap);
            } else {
                elementCopy = saveCopy(groupToCopyTo, elementToCopy);
                number++;
                afterCopy(elementToCopy, elementCopy, sourceDestMap);
                copyChildrenIfExistant(elementToCopy, sourceDestMap, elementCopy);
            }
        } else if (elementToCopy != null) {
            getLog().warn("Can not copy element with pk: " + elementToCopy.getDbId() + " to group with pk: " + groupToPasteTo.getDbId()); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            getLog().warn("Can not copy element. Element is null");
        }
        return elementCopy;
    }

    private CnATreeElement copyRiskAnalysis(CnATreeElement group, CnATreeElement finishedRiskAnalysis, Map<String, String> sourceDestMap) throws CommandException, IOException {
        CnATreeElement copyOfFinishedRiskAnalysis = saveCopy(group, finishedRiskAnalysis);
        number++;
        copyFinishedRiskAnalysisLists((FinishedRiskAnalysis) finishedRiskAnalysis, (FinishedRiskAnalysis) copyOfFinishedRiskAnalysis, sourceDestMap);

        return copyOfFinishedRiskAnalysis;
    }

    private void copyFinishedRiskAnalysisLists(FinishedRiskAnalysis oldFinishedRiskAnalysis, FinishedRiskAnalysis copyOfFinishedRiskAnalysis, Map<String, String> sourceDestMap) throws CommandException, IOException {
        FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(oldFinishedRiskAnalysis.getDbId());
        command = getCommandService().executeCommand(command);
        FinishedRiskAnalysisLists listsToCopy = command.getFoundLists();
        FinishedRiskAnalysisLists newLists = new FinishedRiskAnalysisLists();
        newLists.setFinishedRiskAnalysisId(copyOfFinishedRiskAnalysis.getDbId());
        copyAssociatedGefaehrdungen(copyOfFinishedRiskAnalysis, sourceDestMap, listsToCopy, newLists);
        saveFinishedRiskAnalysisLists(newLists);

    }

    private void copyAssociatedGefaehrdungen(FinishedRiskAnalysis copyOfFinishedRiskAnalysis, Map<String, String> sourceDestMap, FinishedRiskAnalysisLists listsToCopy, FinishedRiskAnalysisLists newLists) throws CommandException, IOException {

        for (GefaehrdungsUmsetzung gefaehrdung : listsToCopy.getAssociatedGefaehrdungen()) {
            GefaehrdungsUmsetzung newGefaehrdung = (GefaehrdungsUmsetzung) copy(copyOfFinishedRiskAnalysis, gefaehrdung, sourceDestMap);
            newLists.getAssociatedGefaehrdungen().add(newGefaehrdung);
            addToRAWizardListsIfNeccessary(listsToCopy, newLists, gefaehrdung, newGefaehrdung);
            SaveElement<GefaehrdungsUmsetzung> saveCommand = new SaveElement<>(newGefaehrdung);
            getCommandService().executeCommand(saveCommand);
            number++;
        }
    }

    private void addToRAWizardListsIfNeccessary(FinishedRiskAnalysisLists listsToCopy, FinishedRiskAnalysisLists newLists, GefaehrdungsUmsetzung gefaehrdung, GefaehrdungsUmsetzung newGefaehrdung) {
        if (gefaehrdung.getParent() == null) {
            newGefaehrdung.setParent(null);
            return;
        }
        if (listsToCopy.getAllGefaehrdungsUmsetzungen().contains(gefaehrdung)) {
            newLists.getAllGefaehrdungsUmsetzungen().add(newGefaehrdung);
        }
        if (listsToCopy.getNotOKGefaehrdungsUmsetzungen().contains(gefaehrdung)) {
            newLists.getNotOKGefaehrdungsUmsetzungen().add(newGefaehrdung);
        }
    }

    private void saveFinishedRiskAnalysisLists(FinishedRiskAnalysisLists newLists) throws CommandException {
        SaveElement<FinishedRiskAnalysisLists> saveCommand = new SaveElement<>(newLists);
        getCommandService().executeCommand(saveCommand);
    }

    private void afterCopy(CnATreeElement original, CnATreeElement copy, Map<String, String> sourceDestMap) {
        sourceDestMap.put(original.getUuid(), copy.getUuid());
        if (number % FLUSH_LEVEL == 0) {
            getDao().flush();
        }
    }

    private void copyChildrenIfExistant(CnATreeElement element, Map<String, String> sourceDestMap, CnATreeElement elementCopy) throws CommandException, IOException {
        if (element.getChildren() != null) {
            for (CnATreeElement child : element.getChildren()) {
                copy(elementCopy, child, sourceDestMap);
            }
        }
    }

    private CnATreeElement saveCopy(CnATreeElement toGroup, CnATreeElement elementToCopy) throws CommandException, IOException {
        // TODO is this needed?
        CnATreeElement copyElement = getDao().initializeAndUnproxy(elementToCopy);
        CnATreeElement newElement = saveNew(toGroup, copyElement);

        if (copyElement.isTemplateOrImplementation()) {
            newElement.setEntity(copyElement.getEntity());
            newElement.setTemplateType(TemplateType.IMPLEMENTATION);
            newElement.getImplementedTemplateUuids().add(copyElement.getUuid());
        } else if (newElement.getEntity() != null) {
            newElement.getEntity().copyEntity(copyElement.getEntity());
        }
        if (newElement.getEntity() != null) {
            if (copyElement.getIconPath() != null) {
                newElement.setIconPath(copyElement.getIconPath());
            }

            if (toGroup.getChildren() != null && toGroup.getChildren().size() > 0) {
                if (newElement instanceof GefaehrdungsUmsetzung) {
                    final String title = newElement.getTitle();
                    final String copyGefaehrdungtitle = ((GefaehrdungsUmsetzung) newElement).getText();
                    final Set<CnATreeElement> siblings = toGroup.getChildren();
                    siblings.remove(newElement);
                    newElement.setTitel(getUniqueTitle(title, copyGefaehrdungtitle, siblings, 0));
                } else {
                    final String title = newElement.getTitle();
                    final Set<CnATreeElement> siblings = toGroup.getChildren();
                    siblings.remove(newElement);
                    newElement.setTitel(getUniqueTitle(title, title, siblings, 0));
                }
            }
        }

        SaveElement<CnATreeElement> saveCommand = new SaveElement<>(newElement);
        saveCommand = getCommandService().executeCommand(saveCommand);
        newElement = saveCommand.getElement();
        newElement.setParentAndScope(toGroup);
        if (isCopyAttachments()) {
            LoadAttachments attachmentLoader = new LoadAttachments(copyElement.getDbId());
            copyAttachments(newElement, getCommandService().executeCommand(attachmentLoader).getAttachmentList());
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Copy created: " + newElement.getTitle()); //$NON-NLS-1$
        }
        newElement.setChildren(new HashSet<CnATreeElement>());
        return newElement;
    }

    private void copyAttachments(final CnATreeElement destinationElement, final Collection<Attachment> attachmentsToCopy) throws CommandException, IOException {
        IBaseDao<Attachment, Serializable> dao = getDaoFactory().getDAO(Attachment.class);

        for (final Attachment attachment : attachmentsToCopy) {
            handleSourceAttachment(destinationElement, dao, attachment);
        }
    }

    /**
     * creates a copy of a given @param attachment (including the
     * {@link AttachmentFile} and references it from the newly created
     * {@link CnATreeElement} @param destinationElement
     * 
     * @throws IOException
     */
    private void handleSourceAttachment(final CnATreeElement destinationElement, IBaseDao<Attachment, Serializable> dao, final Attachment attachment) throws CommandException, IOException {
        final Attachment newAttachmentEntity = createAttachmentCopy(destinationElement, attachment);
        dao.saveOrUpdate(newAttachmentEntity);
        LoadAttachmentFile attachmentFileLoader = new LoadAttachmentFile(attachment.getDbId());
        attachmentFileLoader = getCommandService().executeCommand(attachmentFileLoader);
        AttachmentFileCreationFactory.createAttachmentFile(newAttachmentEntity, attachmentFileLoader.getAttachmentFile().getFileData());
    }

    /**
     * sets (copies) the data of the to be created {@link Attachment} (excluding
     * {@link AttachmentFile} fileData)
     */
    private Attachment createAttachmentCopy(final CnATreeElement destinationElement, final Attachment sourceAttachment) {
        final Attachment newAttachmentEntity = new Attachment();
        newAttachmentEntity.getEntity().copyEntity(sourceAttachment.getEntity());
        newAttachmentEntity.setCnATreeElementId(destinationElement.getDbId());
        newAttachmentEntity.setCnAElementTitel(destinationElement.getTitle());
        return newAttachmentEntity;
    }

    private CnATreeElement saveNew(final CnATreeElement container, final CnATreeElement element) throws CommandException {
        final String title = HitroUtil.getInstance().getTypeFactory().getMessage(element.getTypeId());
        final CnATreeElement child;
        if (element.isTemplateOrImplementation()) {
            CreateTemplateImplementationElement<CnATreeElement> saveCommand = new CreateTemplateImplementationElement<CnATreeElement>(container, (Class<CnATreeElement>) element.getClass(), title, true, false);
            saveCommand.setInheritAuditPermissions(true);
            saveCommand = getCommandService().executeCommand(saveCommand);
            child = saveCommand.getCreatedElement();
        } else {
            CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(container, (Class<CnATreeElement>) element.getClass(), title, true, false);
            saveCommand.setInheritAuditPermissions(true);
            saveCommand = getCommandService().executeCommand(saveCommand);
            child = saveCommand.getNewElement();
        }
        container.addChild(child);
        child.setParentAndScope(container);
        return child;
    }

    /**
     * Creates a list of elements. First all elements are loaded by UUID. A
     * child will be removed from the list if it's parent is already a member.
     *
     * @param uuidList
     *            A list of element UUID
     * @return List of elements
     */
    protected List<CnATreeElement> createInsertList(final List<String> uuidList) {
        final List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        final List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        final int depth = 0;
        final int removed = 0;
        if (uuidList.size() > 1) {
            for (final String uuid : uuidList) {
                final CnATreeElement element = getDao().findByUuid(uuid, RetrieveInfo.getChildrenInstance());
                createInsertList(element, tempList, insertList, depth, removed);
            }
        } else {
            final CnATreeElement element = getDao().findByUuid(uuidList.get(0), RetrieveInfo.getChildrenInstance());
            insertList.add(element);
        }
        return insertList;
    }

    private void createInsertList(final CnATreeElement element, final List<CnATreeElement> tempList, final List<CnATreeElement> insertList, final int depth, int removed) {
        if (!tempList.contains(element)) {
            tempList.add(element);
            int depthLocal = depth;
            if (depthLocal == 0) {
                insertList.add(element);
            }
            if (containsChildrenToCopy(element)) {
                depthLocal++;
                for (final CnATreeElement child : element.getChildren()) {
                    createInsertList(child, tempList, insertList, depthLocal, removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }

    private boolean containsChildrenToCopy(final CnATreeElement element) {
        return (element instanceof IISO27kGroup || element instanceof BausteinUmsetzung) && element.getChildren() != null;
    }

    /**
     * Returns a unique title compared to titles of all siblings
     *
     * @param Title
     *            A title of an element
     * @param siblings
     *            Siblings of the element
     * @return A unique title
     */
    private String getUniqueTitle(final String title, final String copyTitle, final Set<CnATreeElement> siblings, final int n) {
        final String result = copyTitle;
        int nLocal = n;
        for (final CnATreeElement cnATreeElement : siblings) {
            if (cnATreeElement != null && cnATreeElement.getTitle() != null && (cnATreeElement.getTitle().equals(copyTitle))) {
                nLocal++;
                return getUniqueTitle(title, getCopyTitle(title, nLocal), siblings, nLocal);
            }
        }
        return result;
    }

    private String getCopyTitle(final String title, final int n) {
        return Messages.getString("CopyCommand.0", title, n); //$NON-NLS-1$
    }

    private void createTaskWhileApplyingTemplateCommand(final CnATreeElement selectedModelingTemplate, final CnATreeElement applyToElement) {
        if (PropertyLoader.isModelingTemplateActive() && selectedModelingTemplate.isTemplate() && !MassnahmenUmsetzung.TYPE_ID.equals(selectedModelingTemplate.getTypeId())) {
            try {
                CreateTaskWhileApplyingTemplateCommand command = new CreateTaskWhileApplyingTemplateCommand(selectedModelingTemplate, applyToElement);
                getCommandService().executeCommand(command);
            } catch (Exception e) {
                getLog().warn("Could not create task while applying template.", e); // $NON-NLS-1$
            }
        }
    }

    public String getUuidGroup() {
        return uuidGroup;
    }

    public void setUuidGroup(final String uuidGroup) {
        this.uuidGroup = uuidGroup;
    }

    public List<String> getUuidList() {
        return uuidList;
    }

    public void setUuidList(final List<String> uuidList) {
        this.uuidList = uuidList;
    }

    public List<IPostProcessor> getPostProcessorList() {
        return postProcessorList;
    }

    public void addPostProcessor(final IPostProcessor task) {
        if (postProcessorList == null) {
            postProcessorList = new LinkedList<IPostProcessor>();
        }
        postProcessorList.add(task);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    public List<String> getNewElements() {
        return newElements;
    }

    public boolean isCopyAttachments() {
        return copyAttachments;
    }

    public void setCopyAttachments(final boolean copyAttachments) {
        this.copyAttachments = copyAttachments;
    }
}
