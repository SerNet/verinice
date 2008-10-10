package sernet.gs.ui.rcp.gsimport;

public interface IProgress {
	public void beginTask(String name, int totalWork);
	public void  worked(int work);
	public void done();
}
