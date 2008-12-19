package sernet.gs.ui.rcp.gsimport;

public interface IProgress {
	public static final int UNKNOWN_WORK = -1;
	
	public void beginTask(String name, int totalWork);
	public void  worked(int work);
	public void done();
	public void subTask(String name);
}
