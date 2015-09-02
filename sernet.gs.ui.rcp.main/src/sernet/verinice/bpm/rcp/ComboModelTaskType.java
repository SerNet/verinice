/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.interfaces.bpm.KeyMessage;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ComboModelTaskType extends ComboModel<KeyMessage> {

    public static final String PROPERTY_SUFFIX = ".withGroup"; //$NON-NLS-1$
    
    /**
     * @param labelProvider
     */
    public ComboModelTaskType() {
        super(new IComboModelLabelProvider<KeyMessage>() {
            @Override
            public String getLabel(KeyMessage keyMessage) {
                return keyMessage.getValue();
            }
        });
        
        add(createKeyMessage(IIsaQmProcess.TASK_IQM_SET_ASSIGNEE)); 
        add(createKeyMessage(IIsaQmProcess.TASK_IQM_CHECK));       
        
        add(createKeyMessage(IIsaExecutionProcess.TASK_SET_ASSIGNEE)); 
        add(createKeyMessage(IIsaExecutionProcess.TASK_WRITE_PERMISSION)); 
        add(createKeyMessage(IIsaExecutionProcess.TASK_IMPLEMENT)); 
        add(createKeyMessage(IIsaExecutionProcess.TASK_ESCALATE));
        add(createKeyMessage(IIsaExecutionProcess.TASK_CHECK_IMPLEMENTATION));       
        
        add(createKeyMessage(IIndividualProcess.TASK_ASSIGN)); 
        add(createKeyMessage(IIndividualProcess.TASK_CHECK)); 
        add(createKeyMessage(IIndividualProcess.TASK_DEADLINE)); 
        add(createKeyMessage(IIndividualProcess.TASK_EXECUTE));  
        add(createKeyMessage(IIndividualProcess.TASK_EXTENSION));  
        add(createKeyMessage(IIndividualProcess.TASK_NOT_RESPOSIBLE));
        
        add(createKeyMessage(IGsmIsmExecuteProzess.TASK_EXECUTE));
                  
        sort();
        addNoSelectionObject(Messages.ComboModelTaskType_1);
    }

    private KeyMessage createKeyMessage(String key) {
        return new KeyMessage(
                key,
                sernet.verinice.model.bpm.Messages.getString(key + PROPERTY_SUFFIX)
        );
    }

}
