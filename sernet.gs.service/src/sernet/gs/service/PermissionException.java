package sernet.gs.service;

public class PermissionException extends RuntimeException {

	public PermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public PermissionException(String message) {
		super(message);
	}

}
