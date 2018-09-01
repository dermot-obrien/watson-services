package org.dobrien.watson.services.util;

@FunctionalInterface
public interface ExponentialBackOffFunction<T> {
	T execute();
}
