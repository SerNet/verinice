package sernet.gs.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import sernet.gs.server.ServerInitializer;

public class ContextInitializer implements Filter {

	private final Logger log = Logger.getLogger(ContextInitializer.class);
	
	@Override
	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("destroy called...");
		}

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			log.debug("doFilter called...");
		}
		ServerInitializer.inheritVeriniceContextState();
		// proceed along the chain
	    chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		if (log.isDebugEnabled()) {
			log.debug("init called...");
		}

	}

}
