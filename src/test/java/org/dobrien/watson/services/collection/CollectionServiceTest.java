package org.dobrien.watson.services.collection;

import static org.junit.Assert.*;

import org.junit.Test;

import org.dobrien.watson.services.configuration.Configuration;
import org.dobrien.watson.services.configuration.ConfigurationTest;
import org.dobrien.watson.services.model.DiscoveryCollection;
import org.dobrien.watson.services.model.DocumentCounts;

public class CollectionServiceTest {

	private void getCollection(String collectionName) {
		Configuration configuration = ConfigurationTest.getTestConfiguration();
		CollectionService collectionService = new CollectionService(configuration);
		DiscoveryCollection collection  = collectionService.getCollection(collectionName);
		assertNotNull(collection);
		DocumentCounts documentCounts = collection.getDocumentCounts();
		System.out.println(collectionName+" available documents: "+documentCounts.getAvailable());
		int count = documentCounts.getAvailable().intValue();
		assertEquals(999,count);
	}

	@Test
	public void airbnbCollection() {
		String collectionName = "Airbnb Reviews";
		getCollection(collectionName);
	}
	
}
