package org.dobrien.watson.services.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Configurations {

	private static final Logger logger = Logger.getLogger(Configurations.class.getName());

	private static Map<URI,Configuration> configurations;
	
	public static Configuration get() {
		try {
			URI uri = Configurations.class.getClassLoader().getResource("configuration.json").toURI();
			return get(uri);
		}
		catch (URISyntaxException e) {
			logger.log(Level.SEVERE,e.getMessage());
			return null;
		}
	}

	public static Configuration get(URI uri) {
		if (configurations == null) configurations = new HashMap<URI,Configuration>();
		Configuration configuration = configurations.get(uri);
		if (configuration != null) return configuration;
		configuration = load(uri);
		if (configuration != null) {
			configurations.put(uri, configuration);
			return configuration;
		}
		return null;
	}

	private static Configuration load(URI uri) {
		try {
			Path path = Paths.get(uri);
			ObjectMapper objectMapper = new ObjectMapper();
			Configuration configuration = objectMapper.readValue(path.toFile(), Configuration.class);  
			return configuration;
		}
		catch (Exception e) {
			logger.log(Level.SEVERE,"Unable to complete read from configuration file at \"" + uri + "\". "+e.getMessage());
			return null;
		}
	}
}
