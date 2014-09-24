package sernet.verinice.rcp.account;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.MultiselectWidget;

public class AccountGroupMultiselectWidget extends MultiselectWidget<AccountGroup> {

    private static final Logger LOG = Logger.getLogger(AccountGroupMultiselectWidget.class);
    
    private Configuration account;
    
    private IAccountService accountService;
    
    public AccountGroupMultiselectWidget(Composite parent, Configuration account) {
        this.account = account;
        try{
            initData();
            initGui(parent);
        } catch( CommandException e ) {
           String message = "Error while creating widget."; //$NON-NLS-1$
           LOG.error(message, e);
           throw new RuntimeException(message, e);
        }
    }

    @Override
    protected String getLabel(AccountGroup accountGroup) {
        return accountGroup.getName();
    }

    @Override
    protected void initData() throws CommandException {
        itemList = getAccountService().listGroups();
        itemList = sortItems(itemList);
        Set<String> rolesOfAccount = account.getRoles(false);
        for (AccountGroup group : itemList) {
            if(rolesOfAccount.contains(group.getName())) {
                preSelectedElements.add(group);
            }
        }
        if(rolesOfAccount.isEmpty()) {
            setShowOnlySelected(false);
        }
    }
    
    public IAccountService getAccountService() {
        if (accountService == null) {
            accountService = createAccountServive();
        }
        return accountService;
    }

    private IAccountService createAccountServive() {
        return ServiceFactory.lookupAccountService();
    }

    @Override
    protected List<AccountGroup> sortItems(List<AccountGroup> list) {
        Collections.sort(list);
        return list;
    }

    public void setAccount(Configuration account) {
        this.account = account;
    }

    
}
