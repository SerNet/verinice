package sernet.verinice.encryption;

/**
 * Runtime exception that can be thrown when something went wrong during the en-
 * or decryption process.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public class EncryptionException extends RuntimeException {

	private static final long serialVersionUID = 7347723539184218433L;

	public EncryptionException() {
		super();
	}

	public EncryptionException(String message) {
		super(message);
	}

	public EncryptionException(Throwable cause) {
		super(cause);
	}
	
	public EncryptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
