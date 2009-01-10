package sernet.gs.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetHitroConfig
 */
public class GetHitroConfig extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// copy binary data using 100K buffer:
	static final int BUFF_SIZE = 100000;

	static final byte[] buffer = new byte[BUFF_SIZE];
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetHitroConfig() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		InputStream in = this.getClass().getResourceAsStream("SNCA.xml");
		OutputStream out = response.getOutputStream();
		try {
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

}
