package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class InputUtil {

	public static String streamToString(InputStream in, String charset) throws IOException {
		StringBuffer out = new StringBuffer();
		    byte[] b = new byte[4096];
		    for (int n; (n = in.read(b)) != -1;) {
		        out.append(new String(b, 0, n, charset));
		    }
		    new String();
		    return out.toString();
	}

}
