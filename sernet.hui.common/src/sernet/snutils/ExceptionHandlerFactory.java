
package sernet.snutils;


/**
 * @author prack
 */
public class ExceptionHandlerFactory {
	
	private static IExceptionHandler defaultHandler = new IExceptionHandler()
			{
				public void handleException(Exception e) {
					System.err.println(e.toString());
				}
			}; 
	
	public static IExceptionHandler getDefaultHandler() {
		return defaultHandler;
	}
	
	public static void registerHandler(IExceptionHandler handler) {
		defaultHandler = handler;
	}
}
