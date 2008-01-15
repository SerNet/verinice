package sernet.gs.ui.rcp.main.common.model;

/**
 * Class to define build inputs by external callers.
 * 
 * @author koderman@sernet.de
 * 
 * @param <U>
 *            type of build input
 */
public class BuildInput<U> {
		private U bi;

		public BuildInput(U input) {
			bi = input;
		}

		public U getInput() {
			return bi;
		}
}
