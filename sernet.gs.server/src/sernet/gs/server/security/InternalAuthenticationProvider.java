/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import java.util.IdentityHashMap;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.server.ServerInitializer;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.service.HibernateCommandService;

/**
 * {@link AuthenticationProvider} implementation which authenticates
 * internal {@link ICommand} instances.
 * 
 * <p>An in-depth explanation of how this works and why it is needed
 * can be found in the <code>veriniceserver-security-plain.xml</code>
 * file.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
public final class InternalAuthenticationProvider implements AuthenticationProvider {
	
	private static final Object lock = new Object();
	
	private static InternalAuthenticationProvider instance;
	
	private IdentityHashMap<ICommand, ICommand> allowedInstances;

	/**
	 * The {@link Authentication} instance which gives user and admin privileges.
	 */
	private InternalAuthentication authentication = new InternalAuthentication(
			"$internaluser$", "$notused$",
			new GrantedAuthority[] { new GrantedAuthorityImpl(
					ApplicationRoles.ROLE_USER),
					new GrantedAuthorityImpl(
							ApplicationRoles.ROLE_ADMIN)});
	
	public InternalAuthenticationProvider()
	{
		synchronized (lock)
		{
			if (instance != null)
				throw new IllegalStateException("Only one instance of this class allowed.");
			
			instance = this;
		}
	}

	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {
		if (auth != authentication)
			return null;

		auth.setAuthenticated(true);

		return auth;
	}

	public boolean supports(Class arg) {
		return arg == InternalAuthentication.class;
	}

	private final class InternalAuthentication extends
			UsernamePasswordAuthenticationToken {
		private InternalAuthentication(Object principal, Object credentials,
				GrantedAuthority[] authorities) {
			super(principal, credentials, authorities);
		}

		public void setAuthenticated(boolean b) {
			// Allow being authenticated only when the caller is an
			// InternalAuthenticationProvider instance.
			StackTraceElement[] t = Thread.currentThread().getStackTrace();
			if (b && t.length >= 1) {
				if (InternalAuthenticationProvider.class.getName().equals(
						t[1].getClassName())
						&& "authenticate".equals(t[1].getMethodName())) {
					super.setAuthenticated(true);
				}
			}
		}
	}

	public void setAllowedInstances(Set<ICommand> allowedInstances) {
		// Prevent that anyone modifies the set after it has first been set. 
		if (allowedInstances == null
				|| this.allowedInstances != null)
			throw new IllegalArgumentException();
		
		this.allowedInstances = new IdentityHashMap<ICommand, ICommand>();
		for (ICommand o : allowedInstances)
		{
			this.allowedInstances.put(o, o);
		}
	}

	/**
	 * This method is called upon invocation of the {@link HibernateCommandService#executeCommand(ICommand)}
	 * method.
	 * 
	 * <p>Is purpose is to provide privileged and non-privileged access for {@link ICommand} instances
	 * that are started by the server itself.</p>
	 * 
	 * <p>Another use-case is preliminary authentication when the real authentication scheme is not
	 * completed.</p>
	 * 
	 * <p>To achieve this this method checks whether no authentication information is provided yet.
	 * If that is the case it will test whether the argument to the <code>executeCommand</code> is
	 * a well-known instance. If the test is positive an {@link Authentication} instance is put into the
	 * {@link SecurityContext}.</p>
	 * 
	 * <p>The instances which are allowed to be passed to the <code>executeCommand</code> method are
	 * configured through this class' Spring configuration.</p>
	 * 
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	public Object doInsertAuthentication(ProceedingJoinPoint pjp)
	throws Throwable
	{
		SecurityContext ctx = SecurityContextHolder.getContext();
		
		Authentication auth = ctx.getAuthentication();
		if (ctx.getAuthentication() == null || !auth.isAuthenticated())
		{
			Object arg = pjp.getArgs()[0];
			if (!(arg instanceof ICommand))
				throw new IllegalStateException("Argument is either null or not of type " + ICommand.class.getName() + ".");
			
			if (!allowedInstances.containsKey(arg))
				throw new IllegalStateException("It was not configured that this instance can receive an Authentication instance.");
			
			ctx.setAuthentication(authentication);
		}
		
		Object result = null;
		
		try
		{
			result = pjp.proceed();
		}
		finally
		{
			// Whatever 'auth' was before (null or something that is not authenticated yet)
			// we need to put it back.
			ctx.setAuthentication(auth);
		}
		
		return result;
	}

}
