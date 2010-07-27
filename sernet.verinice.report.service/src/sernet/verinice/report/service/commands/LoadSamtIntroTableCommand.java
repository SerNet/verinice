package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class LoadSamtIntroTableCommand extends GenericCommand{

	private List<List<String>> result;
	
	
	public List<List<String>> getResult()
	{
		return result;
	}

	@Override
	public void execute() {
		List<List<String>> l = new ArrayList<List<String>>();
		
		l.add(mvars("Company", "companyName"));
		l.add(mvars("CEO", "ceo"));
		l.add(mvars("IT Manager", "itManager"));
		l.add(mvars("CISO", "ciso"));
		l.add(mvars("Auditor", "auditor"));
		l.add(mvars("Scope", "scope"));
		l.add(mvars("Basis", "basis"));
		l.add(mvars("Date", "date"));
		l.add(mvars("Language", "language"));
		l.add(mvars("Version", "version"));
		
		result = l;
	}
	
	private List<String> mvars(String label, String key)
	{
		List<String> l = new ArrayList<String>();
		l.add(label);
		l.add("'" + key + "'");
		
		return l;
	}
	
}
