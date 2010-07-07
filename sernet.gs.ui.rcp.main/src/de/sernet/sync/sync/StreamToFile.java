package de.sernet.sync.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author: joosm
 * 
 */
public class StreamToFile {

	public static File convert(ZipInputStream ips) throws IOException {
			File file;
			file = File.createTempFile(UUID.randomUUID().toString(), "");
			
			IOUtils.copy(ips, new FileOutputStream(file));
		
			return file;
	}
}
