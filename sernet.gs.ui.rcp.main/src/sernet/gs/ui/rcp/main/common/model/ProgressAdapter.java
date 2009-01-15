package sernet.gs.ui.rcp.main.common.model;

import org.eclipse.core.runtime.IProgressMonitor;

public class ProgressAdapter implements IProgress {


		private IProgressMonitor monitor;
		
		public ProgressAdapter(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public void beginTask(String name, int totalWork) {
			monitor.beginTask(name, totalWork);
		}

		public void done() {
			monitor.done();
		}

		public void worked(int work) {
			monitor.worked(work);
		}

		public void setTaskName(String string) {
			monitor.setTaskName(string);
		}

		public void subTask(String string) {
			monitor.subTask(string);
		}

}
