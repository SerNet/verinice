/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.HashMap;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads all findings for a given {@link CnATreeElement} (?).
 * 
 * <p>TODO samt: The current implementation generates random values which can change
 * each time the command is invoked. The ids used in here correspond to those used
 * in the {@link LoadChapterListCommand}.</p> 
 *
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadAllFindingsCommand extends GenericCommand {

	private Object[][] result;

	private int id;
	
	private static int MAX = 10;
	
	static HashMap<Integer, Object[][]> hardcodedData = new HashMap<Integer, Object[][]>();
	
	static
	{
		// Manual ISO/IEC chapter
		makeEntry(10011, new Object[][] {
				makeFinding(1001101,
						"An information security policy document should be approved by management, and published and communicated to all employees and relevant external parties.",
						"Policy document is available but not signed by the management until now. Some security documents from A are taken, modified to local requirements, translated into german and will be signed in January by the management. Users know partly about the documents. ",
						2, 1, 1, "Take the ISSO documents and translate it CISO by 2006/08/31 into german for publishing in the intranet and communication to all employees. In case of any specific additions use the appendix of these documents. Let the policy document sign by the group board manager.", "CISO by 2006/08/31"),
		});
		
		// ISO/IEC chapters
		makeRandomEntry(10021);
		
		makeRandomEntry(10031);
		makeRandomEntry(10032);
		makeRandomEntry(10033);
		
		makeRandomEntry(10041);
		makeRandomEntry(10042);
		
		makeRandomEntry(10051);
		makeRandomEntry(10052);
		
		makeRandomEntry(10061);
		makeRandomEntry(10062);
		makeRandomEntry(10063);
		makeRandomEntry(10064);
		
		makeRandomEntry(10071);
		makeRandomEntry(10072);
		
		makeRandomEntry(10081);
		makeRandomEntry(10082);
		makeRandomEntry(10083);
		makeRandomEntry(10084);
		makeRandomEntry(10085);
		
		makeRandomEntry(10091);
		makeRandomEntry(10092);
		makeRandomEntry(10093);
		
		makeRandomEntry(10101);
		makeRandomEntry(10102);
		
		// IT rooms
		makeRandomEntry(2001);
		makeRandomEntry(2002);
		makeRandomEntry(2003);
		makeRandomEntry(2004);
		
		// Office places
		makeRandomEntry(3001);
		makeRandomEntry(3002);
		
		// System checks
		makeRandomEntry(4001);
	}
	
	private static void makeRandomEntry(int baseId)
	{
		ArrayList<Object[]> al = new ArrayList<Object[]>();
		final int amount = (int) (Math.random() * (MAX + 1));
		for (int i = 0; i < amount; i++)
		{
			al.add(makeRandomFinding(baseId, i));
		}
		
		hardcodedData.put(baseId, al.toArray(new Object[al.size()][]));
	}
	
	private static Object[] makeRandomFinding(int baseId, int countingId) {
		return makeFinding(
				baseId * 100 + countingId,
				String.format("control %s-%s", baseId, countingId),
				String.format("finding %s-%s", baseId, countingId),
				(int) (Math.random() * 6),
				(int) (Math.random() * 3),
				(int) (Math.random() * 3),
				String.format("measure %s-%s", baseId, countingId),
				String.format("p. in charge %s-%s", baseId, countingId));
	}
	
	private static Object[] makeFinding(int id, String control, String finding,
			int maturityLevel, int deviation, int risk, String measure, String personInCharge) {
		return new Object[] { id, control, finding, maturityLevel, deviation, risk, measure, personInCharge };
	}
	
	private static void makeEntry(int id, Object[][] data)
	{
		hardcodedData.put(id, data);
	}

	public LoadAllFindingsCommand(int id) {
		this.id = id;
	}

	public Object[][] getResult() {
		return result;
	}

	@Override
	public void execute() {
		result = hardcodedData.get(id);
	}

}
