package org.dobrien.watson.services.util;

import java.util.List;
import java.util.logging.Logger;

import org.dobrien.watson.services.document.DocumentService;

public final class ExponentialBackOff {

	private static final Logger logger = Logger.getLogger(DocumentService.class.getName());
	
	private ExponentialBackOff() {
	}

	public static <T> T execute(ExponentialBackOffFunction<T> fn, List<Class<? extends Exception>> expectedErrors,String waitMessage,int maxSeconds) {
		for (int attempt = 0; fibionacci(attempt) < maxSeconds; attempt++) {
			try {
				return fn.execute();
			} 
			catch (Exception e) {
				handleFailure(attempt, e,expectedErrors,waitMessage);
			}
		}
		throw new RuntimeException("Failed to complete.");
	}

	private static int fibionacci(int index) {
		if (index <= 1)  return 1;
		return fibionacci(index-1) + fibionacci(index-2);
	}
     
	private static void handleFailure(int attempt, Exception e,List<Class<? extends Exception>> expectedErrors,String waitMessage) {
		if (e.getCause() != null && !expectedErrors.contains(e.getCause().getClass()))
			throw new RuntimeException(e);
		logger.info(waitMessage+" Waiting "+fibionacci(attempt)+" seconds.");
		doWait(attempt);
	}

	private static void doWait(int attempt) {
		try {
			Thread.sleep(fibionacci(attempt) * 1000);
		} 
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}