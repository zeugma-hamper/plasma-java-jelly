package com.oblong.util;

import com.oblong.util.logging.ObLog;

/**
 * Created with IntelliJ IDEA.
 * User: valeria
 * Date: 8/21/13
 * Time: 3:34 PM
 *
 */
public abstract class ExceptionHandler {

	private static final ObLog logger = ObLog.get(ExceptionHandler.class);
	public static final String NO_INSTALLED_EXCEPTION_HANDLER = "(no installed ExceptionHandler)";

	private static volatile ExceptionHandler exceptionHandler;

	private static boolean DEFAULT_HANDLER_PRINTS = false;
	private static boolean DEFAULT_HANDLER_RETHROWS = false;

	public static void handleException(Throwable e)  {
		handleException(e, " Unspecified ");
	}

	public static void handleException(Throwable e, String duringMsg)  {
		if (exceptionHandler != null) {
			String msg = createMessage(duringMsg);
			exceptionHandler.handleExceptionImpl(e, msg);
		} else {
			defaultHandleException(e, duringMsg);
		}
	}

	private static String createMessage(String duringMsg) {
		return "Exception: " + duringMsg;
	}

	private static void defaultHandleException(Throwable e, String duringMsg) {
		if ( DEFAULT_HANDLER_PRINTS ) {
			final String prefix = "========= ";
			System.err.println(prefix + createMessage(duringMsg));
			logger.error(prefix + createMessage(duringMsg));
			if ( e != null ) {
				e.printStackTrace();
			}
			logger.error(NO_INSTALLED_EXCEPTION_HANDLER);
		}
		if ( DEFAULT_HANDLER_RETHROWS ) {
			throw new RuntimeException(createMessage(duringMsg),e);
		}
	}

	public abstract void handleExceptionImpl(Throwable e, String syntheticMsg) ;

	public static void setExceptionHandler(ExceptionHandler exceptionHandlerParam) {
		exceptionHandler = exceptionHandlerParam;
	}

	public static <T> void assertEquals(String message, T expected, T actual)  {
		if ( ! expected.equals(actual) ) { /* TODO: use safeEquals */
			handleException("assertEquals failed: expected: " + expected + " ; actual: " + actual+" message : "+message);
		}
	}

	public static void handleException(String msg)  {
		handleException(new JustToGetStackTrace(), msg);
	}

	public static void handleError(String message, Object details) {
		handleException(message + "; details: " + details);
	}

	public static void debug(String message) {
		if(exceptionHandler!=null) {
			exceptionHandler.handleDebugMessageImpl(message, new JustToGetStackTrace());
		} else {
			logger.d(NO_INSTALLED_EXCEPTION_HANDLER+" : "+message);
		}
	}

	protected abstract void handleDebugMessageImpl(String message, JustToGetStackTrace tr);

	public static void logStackTrace(String msg) {
		exceptionHandler.logStackTraceImpl(msg, new JustToGetStackTrace());
	}

	protected abstract void logStackTraceImpl(String msg, JustToGetStackTrace stackTrace);

	public static class JustToGetStackTrace extends Exception { /*nothing*/ }

	public static void testException() {
		try {
			throw new Exception("testing exception");
		} catch ( Exception e ) {
			ExceptionHandler.handleException(e);
		}
	}


}
