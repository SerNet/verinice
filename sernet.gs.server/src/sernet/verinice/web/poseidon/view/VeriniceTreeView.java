/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.view;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.apache.log4j.Logger;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.web.Util;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.tree.ElementManager;
import sernet.verinice.service.iso27k.LoadModel;
import sernet.verinice.web.EditBean;
import sernet.verinice.web.ElementInformation;
import sernet.verinice.web.LinkBean;
import sernet.verinice.web.TaskBean;
import sernet.verinice.web.TreeBean;

/**
 * Lazy loading backing bean for verinice objects data tree.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "veriniceTreeView")
public class VeriniceTreeView {

    private final Logger log = Logger.getLogger(VeriniceTreeView.class);

    private DefaultTreeNode root;

    private DefaultTreeNode selected;

    @ManagedProperty("#{edit}")
    private EditBean editBean;

    @ManagedProperty("#{link}")
    private LinkBean linkBean;

    private ElementManager elementManager;

    @PostConstruct
    public void init() {
        this.elementManager = new ElementManager();
        this.root = new DefaultTreeNode(new ElementInformation(loadIsoModel()), null);
        fetchChildren(root);
        for (TreeNode node : root.getChildren()) {
            fetchChildren(node);
        }
    }

    private ISO27KModel loadIsoModel() {
        ISO27KModel model = null;
        try {
            LoadModel loadModel = new LoadModel();
            loadModel = ServiceFactory.lookupCommandService().executeCommand(loadModel);
            model = loadModel.getModel();

        } catch (Exception e) {
            log.error("Error while loading model", e);
            throw new RuntimeException("Error while loading model", e);
        }
        return model;
    }

    public void onExpand(NodeExpandEvent event) {
        if (event.getTreeNode() != null) {
            TreeNode node = event.getTreeNode();
            for (TreeNode child : node.getChildren()) {
                fetchChildren(child);
            }
        }
    }

    void fetchChildren(TreeNode parent) {

        ElementInformation data = (ElementInformation) parent.getData();
        try {
            data.setElement(elementManager.loadElementWithChildren(data.getElement()));
        } catch (CommandException commandException) {
            log.debug("loading children failed", commandException);
        }

        for (CnATreeElement e : data.getElement().getChildren()) {
            ElementInformation eInformation = new ElementInformation(e);
            new DefaultTreeNode(eInformation, parent);
            log.debug("add child: " + eInformation.getTitle());
        }
    }

    public void openElement() {
        try {

            if (selected == null) {
                getEditBean().clear();
                getLinkBean().clear();
                return;
            }

            ElementInformation elementInformation = (ElementInformation) selected.getData();
            CnATreeElement element = elementInformation.getElement();

            getEditBean().setSaveMessage(Util.getMessage(TreeBean.BOUNDLE_NAME, "elementSaved"));
            getEditBean().setVisibleTags(Arrays.asList(EditBean.TAG_ALL));
            getEditBean().setSaveButtonHidden(true);
            getEditBean().setUuid(element.getUuid());
            String title = element.getTitle();
            if (title.length() > TaskBean.MAX_TITLE_LENGTH) {
                title = title.substring(0, TaskBean.MAX_TITLE_LENGTH - 1) + "...";
            }
            getEditBean().setTitle(title);
            getEditBean().setTypeId(element.getTypeId());
            getEditBean().addNoLabelType(SamtTopic.PROP_DESC);
            getEditBean().init();
            getEditBean().clearActionHandler();

            getLinkBean().setSelectedLink(null);
            getLinkBean().setSelectedLinkTargetName(null);
            getLinkBean().setSelectedLinkType(null);

        } catch (Exception t) {
            log.error("Error while opening element", t); //$NON-NLS-1$
            Util.addError("elementTable", Util.getMessage("tree.open.failed")); //$NON-NLS-1$
        }
    }

    public DefaultTreeNode getRoot() {
        return root;
    }

    public void setRoot(DefaultTreeNode root) {
        this.root = root;
    }

    public DefaultTreeNode getSelected() {
        return selected;
    }

    public void setSelected(DefaultTreeNode selected) {
        this.selected = selected;
    }

    public EditBean getEditBean() {
        return editBean;
    }

    public void setEditBean(EditBean editBean) {
        this.editBean = editBean;
    }

    public LinkBean getLinkBean() {
        return linkBean;
    }

    public void setLinkBean(LinkBean linkBean) {
        this.linkBean = linkBean;
    }

}
