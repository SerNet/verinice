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
import sernet.gs.ui.rcp.main.service.HibernateCommandService;
import sernet.gs.ui.rcp.main.service.commands.ICommand;

@SuppressWarnings("serial")
public final class InternalAuthenticationProvider implements AuthenticationProvider {
	
	private IdentityHashMap<ICommand, ICommand> allowedInstances;

	private InternalAuthentication authentication = new InternalAuthentication(
			"$internaluser$", "$notused$",
			new GrantedAuthority[] { new GrantedAuthorityImpl(
					ApplicationRoles.ROLE_USER),
					new GrantedAuthorityImpl(
							ApplicationRoles.ROLE_ADMIN)});

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
