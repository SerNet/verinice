package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class LoadDeviationRiskTableCommand extends GenericCommand{

	private List<List<String>> result;
	
	private int chapterId;
	private String chapterName;
	
	public LoadDeviationRiskTableCommand(int chapterId, String chapterName)
	{
		this.chapterId = chapterId;
		this.chapterName = chapterName;
	}
	
	public List<List<String>> getResult()
	{
		return result;
	}

	@Override
	public void execute() {
		ArrayList<String> l = new ArrayList<String>();
		
		// TODO: Actually retrieve those value from the verinice database with the use
		// of the chapter id.
		
		// chapterName
		l.add(chapterName);
		
		// OK: - Low Medium High
		l.add("0");
		l.add("2");
		l.add("5");
		l.add("2");
		
		// Minor: - Low Medium High
		l.add("1");
		l.add("4");
		l.add("1");
		l.add("0");
		
		// Major: - Low Medium High
		l.add("2");
		l.add("3");
		l.add("15");
		l.add("1");
		
		result = new ArrayList<List<String>>();
		result.add(l);
	}
	
}
