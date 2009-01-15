package sernet.gs.ui.rcp.main.common.model;

public interface IProgress {
	public static final int UNKNOWN_WORK = -1;
	
		public void beginTask(String name, int totalWork);
		public void worked(int work);
		public void done();
		public void setTaskName(String string);
		public void subTask(String string);
}
