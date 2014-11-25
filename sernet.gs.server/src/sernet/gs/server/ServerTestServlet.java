package sernet.gs.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ServerTestServlet extends HttpServlet {
    
    private static final Logger LOG = Logger.getLogger(ServerTestServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 131427514191056452L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		LOG.error("doGet");
		
		resp.setContentType("text/html");
		
		PrintWriter w = resp.getWriter();
		
		w.println("verinice server is running!");
		
		w.flush();
	}

	
}
