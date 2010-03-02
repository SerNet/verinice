/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.office;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;

import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextFieldService;

public class TestOOWrapper extends TestCase {

	public class TestRow implements IOOTableRow {

		private String style;
		private String text;

		TestRow(String text, String style) {
			this.style = style;
			this.text = text;
		}
		
		public double getCellAsDouble(int column) {
			return 0;
		}

		public String getCellAsString(int column) {
			return text+ "_" + column;
		}

		public int getCellType(int column) {
			return IOOTableRow.CELL_TYPE_STRING;
		}

		public int getNumColumns() {
			return 5;
		}

		public String getRowStyle() {
			return style;
		}

	}

	public void testReport() throws Exception {
		OOWrapper inst = new OOWrapper("/opt/openoffice.org2.2");
		ArrayList daten = new ArrayList();
		daten.add(new TestRow("Header", IOOTableRow.ROW_STYLE_HEADER));
		daten.add(new TestRow("Subheader", IOOTableRow.ROW_STYLE_SUBHEADER));
		daten.add(new TestRow("Row", IOOTableRow.ROW_STYLE_ELEMENT));
		daten.add(new TestRow("Row", IOOTableRow.ROW_STYLE_ELEMENT));
		daten.add(new TestRow("Row", IOOTableRow.ROW_STYLE_ELEMENT));


		 inst.openDocument("/home/akoderman/sncaWorkspace/office/report.odt");
		 //inst.createTextReport("Testreport", daten, 5, new IProgressMonitor() {
//		
//		 public void beginTask(String name, int totalWork) {
//		 // TODO Auto-generated method stub
//							
//		 }
//		
//		 public void done() {
//		 // TODO Auto-generated method stub
//							
//		 }
//		
//		 public void internalWorked(double work) {
//		 // TODO Auto-generated method stub
//							
//		 }
//		
//		 public boolean isCanceled() {
//		 // TODO Auto-generated method stub
//		 return false;
//		 }
//		
//		 public void setCanceled(boolean value) {
//		 // TODO Auto-generated method stub
//							
//		 }
//		
//		 public void setTaskName(String name) {
//		 // TODO Auto-generated method stub
//							
//		 }
//		
//		 public void subTask(String name) {
//		 // TODO Auto-generated method stub
//							
//		 }
//		
//		 public void worked(int work) {
//		 // TODO Auto-generated method stub
//							
//		 }
//						
//		 });

		// inst.fillSpreadsheet("Testreport", daten, new IProgressMonitor() {
		// public void beginTask(String arg0, int arg1) {
		// }
		//
		// public void done() {
		// }
		//
		// public void internalWorked(double arg0) {
		// }
		//
		// public boolean isCanceled() {
		// return false;
		// }
		//
		// public void setCanceled(boolean arg0) {
		// }
		//
		// public void setTaskName(String arg0) {
		// // TODO Auto-generated method stub
		//					
		// }
		//
		// public void subTask(String arg0) {
		// // TODO Auto-generated method stub
		//					
		// }
		//
		// public void worked(int arg0) {
		// // TODO Auto-generated method stub
		//					
		// }
		//				
		// });

	}
}
