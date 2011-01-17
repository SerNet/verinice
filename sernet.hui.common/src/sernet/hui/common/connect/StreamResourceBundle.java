package sernet.hui.common.connect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Provides a Resource Bundle for messages
 *
 */
public class StreamResourceBundle extends ResourceBundle {
	private Properties props;
	
	/**
	 * Load properties from the input stream
	 */
	public StreamResourceBundle(InputStream stream) throws IOException {
	    props = new Properties();
	    props.load(stream);	
	}

	/**
	 * Override
	 */
	public Enumeration<String> getKeys() {
	    Set<String> handleKeys = props.stringPropertyNames();
	    return Collections.enumeration(handleKeys);
	}

	/**
	 * Override
	 */
	protected Object handleGetObject(String key) {
	    return props.getProperty(key);	
	}

}
