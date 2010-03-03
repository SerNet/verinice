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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/


package sernet.gs.ui.rcp.office;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.event.ICloseEvent;
import ag.ion.bion.officelayer.event.ICloseListener;
import ag.ion.bion.officelayer.event.IEvent;
import ag.ion.bion.officelayer.spreadsheet.ISpreadsheetDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextFieldMaster;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.ITextTableCellRange;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.sheet.XCellRangeData;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.text.XText;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

/**
 * High level wrapper for common OpenOffice tasks.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev: 37 $ $LastChangedDate: 2007-10-01 17:51:01 +0200 (Mo, 01 Okt 2007) $ 
 * $LastChangedBy: koderman $
 * 
 */
public class OOWrapper {

	private static final String USERFIELD = "com.sun.star.text.FieldMaster.User.";

	private static final int SKIP_FIRST_ROWS = 9;

	private static final String MARKER_TITEL = "{titel}";

	private String ooPath;

	private IOfficeApplication officeApplication;

	private ITextDocument textDocument;

	private String oldPath;

	private ISpreadsheetDocument spreadsheetDocument;

	public OOWrapper(String ooPath) {
		this.ooPath = ooPath;
		oldPath = System.getProperty("java.library.path");
		String newPath = oldPath + ":" + ooPath + File.separator + "program";
		try {
			setLibraryPath(newPath);
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Initialisieren von OpenOffice.");
		}
	}

	private class OOCloseListener implements ICloseListener {

		public void notifyClosing(ICloseEvent arg0) {
			disconnect();
		}

		public void queryClosing(ICloseEvent arg0, boolean arg1) {
		}

		public void disposing(IEvent arg0) {
		}
	}

	@SuppressWarnings("unchecked")
	private void connect() throws OfficeApplicationException {
		if (officeApplication == null) {
			HashMap configuration = new HashMap();
			configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooPath);
			configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY,
					IOfficeApplication.LOCAL_APPLICATION);
			officeApplication = OfficeApplicationRuntime
					.getApplication(configuration);
			officeApplication.activate();
		}
	}

	private void disconnect() {
		try {
			setLibraryPath(oldPath);
		} catch (OfficeApplicationException e) {
			ExceptionUtil.log(e, "Fehler beim Trennen von OpenOffice.");
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Trennen von OpenOffice.");
		}
	}

	public void testConnection() {
		try {
			connect();
			IDocumentService documentService = officeApplication
					.getDocumentService();
			IDocument document = documentService.constructNewDocument(
					IDocument.WRITER, DocumentDescriptor.DEFAULT);
			ITextDocument textDocument = (ITextDocument) document;
			StringBuffer buff = new StringBuffer();
			buff.append("Verbindung hergestellt.\n");
			buff.append("Mem free: " + Runtime.getRuntime().freeMemory()
					+ " / " + Runtime.getRuntime().totalMemory() + "\n");
			buff.append((new GregorianCalendar()).getTime() + "\n");
			buff.append("Application Type: "
					+ officeApplication.getApplicationType() + "\n");
			textDocument.getTextService().getText().setText(buff.toString());
		} catch (RuntimeException e) {
			ExceptionUtil.log(e, "Fehler beim Initialisieren von OpenOffice.");
		} catch (OfficeApplicationException e) {
			ExceptionUtil.log(e, "Fehler beim Initialisieren von OpenOffice.");
		} catch (NOAException e) {
			ExceptionUtil.log(e, "Fehler beim Initialisieren von OpenOffice.");
		} finally {
			disconnect();
		}
	}

	/**
	 * Fill new or existing spreadsheet document.
	 * 
	 * @param daten
	 * @param mon
	 * @throws Exception
	 */
	public void fillSpreadsheet(String title, ArrayList<IOOTableRow> daten,
			IProgressMonitor mon) throws Exception {
		connect();
		try {
			if (spreadsheetDocument == null) {
				IDocumentService documentService = officeApplication
						.getDocumentService();
				IDocument document = documentService.constructNewDocument(
						IDocument.CALC, DocumentDescriptor.DEFAULT);
				spreadsheetDocument = (ISpreadsheetDocument) document;
			}

			XSpreadsheets sheets = spreadsheetDocument.getSpreadsheetDocument()
					.getSheets();
			XIndexAccess sheets_xindexaccess = cast(XIndexAccess.class, sheets);

			Object sheet = sheets_xindexaccess.getByIndex(0);
			XSpreadsheet sheet_xspreadsheet = cast(XSpreadsheet.class, sheet);

			// for (IOOTableRow row : daten) {
			// fillSpreadsheetRow(sheet_xspreadsheet, SKIP_FIRST_ROWS, row);
			// mon.worked(1);
			// ++rowIdx;
			// }

			setTitle(sheet_xspreadsheet, title);
			fillSpreadsheetArea(sheet_xspreadsheet, daten, SKIP_FIRST_ROWS);

		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Fehler beim Export.", e);
			throw new Exception(e);
		} finally {
			disconnect();
		}
	}

	/**
	 * Fill multiple rows from a 2-dimensional array, which is the fast way to
	 * do it.
	 * 
	 * @param sheet
	 * @param daten
	 * @param skip
	 * @throws Exception
	 */
	private void fillSpreadsheetArea(XSpreadsheet sheet,
			ArrayList<IOOTableRow> daten, int skip) throws Exception {
		int rows = daten.size();
		int columns = maxColumns(daten);
		XCellRange cellRange = sheet.getCellRangeByPosition(0, skip,
				columns - 1, skip + rows - 1);
		XCellRangeData rangeData = cast(XCellRangeData.class, cellRange);
		rangeData.setDataArray(toArray(daten, rows, columns));

		applyStyles(cellRange, daten);
		setOptimalColumnWidth(cellRange);

	}
	
	private void applyTextTableStyles(ITextTable xTable, 
			ArrayList<IOOTableRow> daten, int cols) 
	throws Exception {
		ITextTableCellRange cellRange = xTable.getCellRange(0, 0, cols - 1, daten.size() - 1);
		XPropertySet xPropSet = cast(XPropertySet.class, cellRange);
		XColumnRowRange xColRowRange = cast(XColumnRowRange.class, cellRange);
		XEnumerationAccess xRowEnumAccess = cast(XEnumerationAccess.class,
				xColRowRange.getRows());
		XEnumeration xRowEnum = xRowEnumAccess.createEnumeration();
		Object tableRowService;
		int idx = 0;
		while (xRowEnum.hasMoreElements()) {
			tableRowService = xRowEnum.nextElement();
			xPropSet = (XPropertySet) UnoRuntime.queryInterface(
					XPropertySet.class, tableRowService);
			xPropSet.setPropertyValue("CellStyle", daten.get(idx).getRowStyle());
			idx++;
		}
	}

	private void applyStyles(XCellRange cellRange, ArrayList<IOOTableRow> daten)
			throws Exception {
		XPropertySet xPropSet = cast(XPropertySet.class, cellRange);
		XColumnRowRange xColRowRange = cast(XColumnRowRange.class, cellRange);
		XEnumerationAccess xRowEnumAccess = cast(XEnumerationAccess.class,
				xColRowRange.getRows());
		XEnumeration xRowEnum = xRowEnumAccess.createEnumeration();
		Object tableRowService;
		int idx = 0;
		while (xRowEnum.hasMoreElements()) {
			tableRowService = xRowEnum.nextElement();
			// only apply style to headers and subheaders:
			if (!daten.get(idx).getRowStyle().equals(IOOTableRow.ROW_STYLE_ELEMENT)) {
				xPropSet = (XPropertySet) UnoRuntime.queryInterface(
						XPropertySet.class, tableRowService);
				xPropSet.setPropertyValue("CellStyle", daten.get(idx).getRowStyle());
			}
			idx++;
		}
	}

	private void setOptimalColumnWidth(XCellRange cellRange) throws Exception {

		XPropertySet xPropSet = cast(XPropertySet.class, cellRange);
		XColumnRowRange xColRowRange = cast(XColumnRowRange.class, cellRange);
		XEnumerationAccess xColEnumAccess = cast(XEnumerationAccess.class,
				xColRowRange.getColumns());
		XEnumeration xColEnum = xColEnumAccess.createEnumeration();
		Object tableColumnService;
		while (xColEnum.hasMoreElements()) {
			tableColumnService = xColEnum.nextElement();
			xPropSet = (XPropertySet) UnoRuntime.queryInterface(
					XPropertySet.class, tableColumnService);
			xPropSet.setPropertyValue("OptimalWidth", new Boolean(true));
		}

	}

	private int maxColumns(ArrayList<IOOTableRow> daten) {
		int max = 0;
		for (IOOTableRow row : daten) {
			if (row.getNumColumns() > max)
				max = row.getNumColumns();
		}
		return max;
	}

	private Object[][] toArray(ArrayList<IOOTableRow> daten, int rows,
			int columns) {
		Object[][] result = new Object[rows][];

		int rowIdx = 0;
		for (Iterator iter = daten.iterator(); iter.hasNext(); rowIdx++) {
			IOOTableRow row = (IOOTableRow) iter.next();
			result[rowIdx] = new Object[columns];
			for (int colIdx = 0; colIdx < row.getNumColumns(); colIdx++) {
				result[rowIdx][colIdx] = getCellByType(row, colIdx);
			}
		}

		// replace null cells:
		for (rowIdx = 0; rowIdx < result.length; rowIdx++) {
			for (int colIdx = 0; colIdx < result[rowIdx].length; colIdx++) {
				if (result[rowIdx][colIdx] == null)
					result[rowIdx][colIdx] = "";
			}
		}
		return result;
	}

	private Object getCellByType(IOOTableRow row, int col) {
		if (row.getCellType(col) == IOOTableRow.CELL_TYPE_STRING) {
			// Logger.getLogger(this.getClass())
			// .debug("Cell<String>: " + row.getCellAsString(col));
			return row.getCellAsString(col);
		}
		return "";
	}

	/**
	 * This works, but is slow. Use only for updating single rows.
	 * 
	 * @param sheet
	 * @param rowIdx
	 * @param row
	 * @throws Exception
	 */
	private void fillSpreadsheetRow(XSpreadsheet sheet, int rowIdx,
			IOOTableRow row) throws Exception {
		for (int colIdx = 0; colIdx < row.getNumColumns(); ++colIdx) {
			if (row.getCellType(colIdx) == IOOTableRow.CELL_TYPE_STRING) {
				XCell cell = sheet.getCellByPosition(colIdx, rowIdx);
				XText xCellText = cast(XText.class, cell);
				xCellText.setString(row.getCellAsString(colIdx));
			}
		}
	}

	private void setTitle(XSpreadsheet sheet, String title) {
		try {
			XText text = findCellByString(sheet, MARKER_TITEL);
			if (text != null) {
				text.setString(title);
			}
		} catch (IndexOutOfBoundsException e) {
			Logger.getLogger(this.getClass()).error(
					"Konnte Report-Titel nicht setzen.", e);
		}
	}

	private XText findCellByString(XSpreadsheet sheet, String term)
			throws IndexOutOfBoundsException {

		for (int rows = 0; rows < 10; ++rows) {
			for (int cols = 0; cols < 20; ++cols) {
				XCell cell = sheet.getCellByPosition(cols, rows);
				XText xCellText = cast(XText.class, cell);
				if (xCellText.getString().indexOf(term) > -1)
					return xCellText;
			}
		}
		return null;
	}

	public void createTextReport(String titel, ArrayList<IOOTableRow> rows, int cols,
			IProgressMonitor mon) throws Exception  {
		
		try {
			connect();
			IDocumentService documentService = officeApplication
					.getDocumentService();
			if (this.textDocument == null) {
				IDocument document = documentService.constructNewDocument(
						IDocument.WRITER, DocumentDescriptor.DEFAULT);
				textDocument = (ITextDocument) document;
			}
			ITextFieldService textFieldService = textDocument.getTextFieldService();
			setUserField(textFieldService, "titel", titel);
			refreshTextFields();
			constructAndFillTextTable(rows, cols, mon);
		} catch (Exception e) {
			throw e;
		} finally {
			disconnect();
		}
	}

	private void constructAndFillTextTable(ArrayList daten, int cols,
			IProgressMonitor mon) throws Exception {
		// create the table
		ITextTable textTable = textDocument.getTextTableService()
				.constructTextTable(daten.size(), cols);
		textDocument.getTextService().getTextContentService()
				.insertTextContent(textTable);

		ITextTableCellRange cellRange = textTable.getCellRange(0, 0, cols - 1,
				daten.size() - 1);
		cellRange.setData(toArray(daten, daten.size(), cols));

		int rowIdx = 0;
		for (Iterator iter = daten.iterator(); iter.hasNext();) {
			IOOTableRow row = (IOOTableRow) iter.next();
			fillTextTableRow(textTable, rowIdx, row);
			mon.worked(1);
			++rowIdx;
		}
		applyTextTableStyles(textTable, daten, cols);
	}

	private void fillTextTableRow(ITextTable textTable, int rowIdx,
			IOOTableRow row) throws TextException {
		for (int colIdx = 0; colIdx < row.getNumColumns(); ++colIdx) {
			if (row.getCellType(colIdx) == IOOTableRow.CELL_TYPE_STRING) {
				textTable.getCell(colIdx, rowIdx).getTextService().getText()
						.setText(row.getCellAsString(colIdx));
			} else if (row.getCellType(colIdx) == IOOTableRow.CELL_TYPE_DOUBLE) {
				textTable.getCell(colIdx, rowIdx).setValue(
						row.getCellAsDouble(colIdx));
			}
		}
	}

	public void openDocument(String url) {
		try {
			connect();
			IDocumentService documentService = officeApplication
					.getDocumentService();
			IDocument document = documentService.loadDocument(url);
			textDocument = (ITextDocument) document;
			textDocument.addCloseListener(new OOCloseListener());
		} catch (OfficeApplicationException e) {
			ExceptionUtil
					.log(e, "Fehler beim Öffnen des OpenOffice Dokuments.");
		} catch (DocumentException e) {
			ExceptionUtil
					.log(e, "Fehler beim Öffnen des OpenOffice Dokuments.");
		}
	}

	public void openSpreadhseet(String url) {
		try {
			connect();
			IDocumentService documentService = officeApplication
					.getDocumentService();
			IDocument document = documentService.loadDocument(url);
			spreadsheetDocument = (ISpreadsheetDocument) document;
			spreadsheetDocument.addCloseListener(new OOCloseListener());
		} catch (OfficeApplicationException e) {
			ExceptionUtil
					.log(e, "Fehler beim Öffnen des OpenOffice Dokuments.");
		} catch (DocumentException e) {
			ExceptionUtil
					.log(e, "Fehler beim Öffnen des OpenOffice Dokuments.");
		}
	}

	public void fillLetters(ArrayList apRows, IProgressMonitor mon,
			String toPath, String prefix) {
		mon.beginTask("Erstelle Briefe...", apRows.size());
		try {
			ITextFieldService textFieldService = textDocument
					.getTextFieldService();

			// get field names as column headers:
			IOOTableRow header = (IOOTableRow) apRows.get(0);

			// for all rows:
			int i = 0;
			for (Iterator iter = apRows.iterator(); iter.hasNext();) {
				IOOTableRow row = (IOOTableRow) iter.next();
				mon.subTask(row.getCellAsString(0));
				fillLetter(textFieldService, header, row);
				textDocument.getPersistenceService().store(
						toPath + File.separator + prefix + ++i + ".odt");
				mon.worked(1);
			}
		} catch (TextException e) {
			ExceptionUtil.log(e,
					"Fehler beim Erstellen der OpenOffice Dokumente.");
		} catch (DocumentException e) {
			ExceptionUtil.log(e,
					"Fehler beim Erstellen der OpenOffice Dokumente.");
		} finally {
			mon.done();
		}
	}

	/**
	 * Fill single form letter from template and save.
	 * 
	 * @param textFieldService
	 * @param header
	 * @param row
	 * @throws TextException
	 */
	private void fillLetter(ITextFieldService textFieldService,
			IOOTableRow header, IOOTableRow row) throws TextException {

		// ITextField[] userTextFields = textFieldService.getUserTextFields();
		// for (int i = 0; i < userTextFields.length; i++) {
		// String name = userTextFields[i].getTextFieldMaster().getName();
		// System.out.println(name);
		// }
		//		
		// fill columns:
		for (int colIdx = 0; colIdx < header.getNumColumns(); ++colIdx) {
			setUserField(textFieldService, header.getCellAsString(colIdx), row
					.getCellAsString(colIdx));
		}
		refreshTextFields();
	}

	private void setUserField(ITextFieldService textFieldService, String field,
			String text) throws TextException {
		ITextFieldMaster master = textFieldService
				.getUserTextFieldMaster(field);
		if (master == null) {
			ExceptionUtil.log(new Exception("OO Vorlage fehlerhaft"),
					"Feld im OO Dokument nicht gefunden: " + field);
		} else
			master.setContent(text);
	}

	/**
	 * Refresh user defined fields in document.
	 * 
	 */
	private void refreshTextFields() {
		XTextFieldsSupplier textFieldsSupplier = cast(
				XTextFieldsSupplier.class, textDocument.getXTextDocument());
		XRefreshable refreshable = cast(XRefreshable.class, textFieldsSupplier
				.getTextFields());
		refreshable.refresh();
	}

	/**
	 * A hack so we do not have to set java-library-path on startup. This way we
	 * can choose the office version and directory whenever we want during
	 * runtime. Which rocks.
	 * 
	 * @author koderman[at]sernet[dot]de
	 * @param path
	 * @throws Exception
	 */
	private void setLibraryPath(String newPath) throws Exception {
		Class clazz = ClassLoader.class;
		Field field;
		try {
			field = clazz.getDeclaredField("sys_paths");
			field.setAccessible(true);
			// Reset it to null so that whenever "System.loadLibrary" is called,
			// it will be reconstructed with the changed value.
			field.set(clazz, null);
			System.setProperty("java.library.path", newPath);
			Logger.getLogger(this.getClass())
					.debug(
							"Setting old VM library path:\n" + oldPath + " to:\n "
									+ newPath);
		} catch (SecurityException e) {
			Logger.getLogger(this.getClass()).error(e);
			throw new Exception("Konnte OO-Library Pfad nicht setzen", e);
		} catch (NoSuchFieldException e) {
			Logger.getLogger(this.getClass()).error(e);
			throw new Exception("Konnte OO-Library Pfad nicht setzen", e);
		}
	}

	@SuppressWarnings("unchecked")
	static <T> T cast(Class<T> c, Object o) {
		return (T) UnoRuntime.queryInterface(c, o);
	}
}
