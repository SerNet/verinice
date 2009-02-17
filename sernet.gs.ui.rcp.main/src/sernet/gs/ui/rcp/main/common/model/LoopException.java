package sernet.gs.ui.rcp.main.common.model;

public class LoopException extends RuntimeException {

	private Object loopedObject;

	public LoopException(Object loopedObject) {
		this.loopedObject = loopedObject;
	}

	public Object getLoopedObject() {
		return loopedObject;
	}

}
