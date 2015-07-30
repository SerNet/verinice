/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;

import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;

/**
 * Authenticates a server job outside of the spring security chain.
 *
 * <p>
 * This is useful if a task is not triggered by a servlet. Then this thread is
 * not able to provide a valid security context and this is where this
 * {@link Callable} comes into play. This callable will look up authentication
 * information and if not set it will provide a dummy authentication.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Since this {@link Callable} is running within a thread
 * pool and and threads within this pool are recycled, it is important to
 * cleanup the {@link ThreadLocal} after the task is finished. This is done by
 * this class automatically.
 * </p>
 *
 * @see CompletionService
 * @see ExecutorService
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
abstract public class DummyAuthenticatorCallable<T> implements Callable<T> {

    private static DummyAuthentication DUMMY_AUTHENTICATION = new DummyAuthentication();

    /**
     * Never call this method directly. It makes sure that a security is set and
     * cleared after the job is done. Use instead {@link #doCall()}.
     *
     * Usually this method is executed by an {@link CompletionService}.
     */
    @Override
    final public T call() throws Exception {
        try {
            initializeSecurityContext();
            T t = doCall();
            return t;
        } finally {
            removeSecurityContext();
        }

    }

    abstract public T doCall();

    private void initializeSecurityContext() {
        if (isNoAuthenticationAvailable()) {
            SecurityContext ctx = new SecurityContextImpl();
            ctx.setAuthentication(DUMMY_AUTHENTICATION);
            SecurityContextHolder.setContext(ctx);
        }
    }

    private boolean isNoAuthenticationAvailable() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void removeSecurityContext() {
        SecurityContextHolder.clearContext();
    };
}
