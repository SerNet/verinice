/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog.EncryptionMethod;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * Utility class for parsing files into DOM trees and writing
 * a DOM tree in memory to an XML file.
 * 
 * @author Andreas Becker <andreas[at]becker[dot]name>
 */
public class DOMUtil
{
	/************************************************************
	 * Parses a document for the given {@code path} using DOM.
	 * Returns the org.w3c.dom.Document if successful. 
	 * 
	 * @param path
	 * @return Document the document
	 * @throws IOException, if parsing fails
	 ************************************************************/
	public static Document parseDocument( String path, boolean decryptInput ) throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware( true );
		DocumentBuilder builder;
		Document doc;
		
		try
		{
			InputStream in = new FileInputStream( path );
			
			if (decryptInput)
			{
				// TODO: Open DecryptionDialog ?! Decrypt using password or private key PEM file...
				// in = decrypt(in, ( password ) OR ( certificate AND private key ) from user selection )
			}
			
			builder = factory.newDocumentBuilder();
			doc = builder.parse( in );
			
		}
		catch(ParserConfigurationException ex)
		{
			throw new IOException(ex);
		}
		catch(SAXException ex)
		{
			throw new IOException(ex);
		}
		
		return doc;
	}
	
	/*********************************************************************
	 * Writes a Dom-tree {@code doc} to a (newly created) file given
	 * by its {@code path}. If {@code encryptOutput} is true, a
	 * decryption dialog is opened, which lets the user choose an
	 * encryption method. In this case, the bytestream will be encrypted
	 * appropriately before being written to the file.
	 * 
	 * @param doc
	 * @param uri
	 *********************************************************************/
	public static void writeDocumentToFile( Document doc, String path, boolean encryptOutput )
	{
		try
		{
			OutputStream os = new FileOutputStream( path );
			
			if (encryptOutput) {
				EncryptionDialog encDialog = new EncryptionDialog(Display.getDefault().getActiveShell());
				if (encDialog.open() == Dialog.OK) {
					IEncryptionService service = Activator.getDefault().getEncryptionService();
					
					EncryptionMethod encMethod = encDialog.getSelectedEncryptionMethod();
					if (encMethod == EncryptionMethod.PASSWORD) {
						os = service.encrypt(os, encDialog.getEnteredPassword());
					} else if (encMethod == EncryptionMethod.X509_CERTIFICATE) {
						os = service.encrypt(os, encDialog.getSelectedX509CertificateFile());
					}
				}
			}
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.transform( new DOMSource( doc ), new StreamResult( os ) );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			return;
		}
	}
}
