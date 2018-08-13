package org.dobrien.watson.services.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dobrien.watson.services.model.DiscoveryCollection;
import org.junit.Test;

public class ConfigurationTest {
	
	private static Logger logger = Logger.getLogger(ConfigurationTest.class.getName());
	
	public static Configuration getTestConfiguration() {
		try {
			URI uri = ConfigurationTest.class.getClassLoader().getResource("test-configuration.json").toURI();
			Configuration configuration = Configurations.get(uri);
			return configuration;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to get test configuration.", e);
			return null;
		}
	}
	
	@Test
	public void defaultConfiguration() throws URISyntaxException {
		Path path = Paths.get(getClass().getClassLoader()
			      .getResource("configuration.json").toURI());
		
		File configurationFile = path.toFile();
		System.out.println(configurationFile.getAbsolutePath());
		assertTrue(configurationFile.exists());
		Configuration configuration = Configurations.get();
		assertNotNull(configuration);
	}

	@Test
	public void testConfiguration() throws URISyntaxException {
		Configuration configuration = getTestConfiguration();
		assertNotNull(configuration);
		assertNotNull(configuration.getDiscoveryCollections());
		assertTrue(configuration.getDiscoveryCollections().size() > 0);
		assertEquals(1,configuration.getDiscoveryCollections().size());
		DiscoveryCollection discoveryCollection = configuration.getDiscoveryCollections().get(0);
		assertEquals("tests-1",discoveryCollection.getName());
	}
}
