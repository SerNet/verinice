/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de>- initial API and implementation
 ******************************************************************************/
package sernet.verinice.server.vna;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import de.sernet.service.vna.FileType;
import de.sernet.service.vna.Response;
import de.sernet.service.vna.Vna;
import de.sernet.service.vna_service.VnaService;

/**
 * Apache CXF Web service implementation which imports VNAs.
 * WSDL: sernet.gs.service:sernet/verinice/service/vna/vna-service.wsdl
 * 
 * CXF / Spring configuration: veriniceserver-webservice.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@WebService(targetNamespace = "http://www.sernet.de/service/vna-service", name = "vna-service")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class VnaServiceImpl implements VnaService {

    private static final Logger LOG = Logger.getLogger(VnaServiceImpl.class);
    
    private static final Map<String, Integer> FORMAT_MAP;
    
    static {
        FORMAT_MAP = new Hashtable<String, Integer>();
        FORMAT_MAP.put(FileType.VNA.name(),SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        FORMAT_MAP.put(FileType.XML.name(),SyncParameter.EXPORT_FORMAT_XML_PURE);
    }
    
    
    private ICommandService commandService;

    /* (non-Javadoc)
     * @see de.sernet.service.vna_service.VnaService#importVna(de.sernet.service.vna.Vna)
     */
    @Override
    public Response importVna(Vna request) {
        Response response = new Response();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("importVna called...");
            }          
            doImport(request, response);
        } catch (Exception e) {
            LOG.error("Error while executing web service method importVna: ", e);
            response.getMessage().add("Error while executing web service method importVna: " + e.getMessage());
        }
        return response;
    }

    protected void doImport(Vna request, Response response) throws IOException, CommandException, SyncParameterException {
        if(request!=null && request.getData()!=null) {        
            DataHandler handler = request.getData();
            InputStream is = handler.getInputStream();
            byte[] bytes = IOUtils.toByteArray(is);
            
            String message = "File recieved: " + request.getName() + ", type: " + request.getType().toString() + ", size: " + bytes.length + " bytes.";
            if (LOG.isInfoEnabled()) {
                LOG.info(message);
            }             
            response.getMessage().add(message);
            SyncParameter parameter = getParameterForRequest(request);
            SyncCommand command = doImport(parameter, bytes);
            createResponse(command, response);
        }
    }

    private SyncCommand doImport(SyncParameter parameter, byte[] fileData) throws CommandException {
        SyncCommand command = new SyncCommand(parameter, fileData); 
        return getCommandService().executeCommand(command);
    }
    
    private void createResponse(SyncCommand command, Response response) {
        response.setInserted(command.getInserted());
        response.setUpdated(command.getPotentiallyUpdated());
        response.setDeleted(command.getDeleted());
    }
    
    private static SyncParameter getParameterForRequest(Vna request) throws SyncParameterException {
        SyncParameter parameter = new SyncParameter(
                request.isInsert(), 
                request.isUpdate(), 
                request.isDelete(), 
                request.isIntegrate(), 
                FORMAT_MAP.get(request.getType().name()));
        return parameter;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

}
