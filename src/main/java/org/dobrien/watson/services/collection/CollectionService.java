package org.dobrien.watson.services.collection;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.developer_cloud.discovery.v1.Discovery;
import com.ibm.watson.developer_cloud.discovery.v1.model.GetCollectionOptions;

import org.dobrien.watson.services.configuration.Configuration;
import org.dobrien.watson.services.configuration.Configurations;
import org.dobrien.watson.services.model.DiscoveryCollection;

public class CollectionService {
	
	private static final Logger logger = Logger.getLogger(CollectionService.class.getName());
	
	private Configuration configuration;
	
	public CollectionService() {
		this(Configurations.get());
	}
	
	public CollectionService(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public DiscoveryCollection getCollection(String collectionName) {
		try {
			// Context.
			Discovery discovery = configuration.getDiscovery();
			
			// Get configuration details.
			DiscoveryCollection collection = configuration.getCollection(collectionName);
			discovery.setUsernameAndPassword(collection.getUsername(), collection.getPassword());
			
			// Get the details.
			GetCollectionOptions getRequest = new GetCollectionOptions.Builder(collection.getEnvironmentId(), collection.getCollectionId()).build();
			com.ibm.watson.developer_cloud.discovery.v1.model.Collection response = discovery.getCollection(getRequest).execute();
		    
			// Convert.
			ObjectMapper objectMapper = new ObjectMapper();
			String responseJSON = response.toString();
			DiscoveryCollection collectionDetails = objectMapper.readValue(responseJSON, DiscoveryCollection.class);
			
			// Backfill parameters not returned.
			collectionDetails.setEnvironmentId(collection.getEnvironmentId());
			collectionDetails.setUsername(collection.getUsername());
			collectionDetails.setPassword(collection.getPassword());
			return collectionDetails;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to get collection details.", e);
			return null;
		}
	}
	
	public DiscoveryCollection getIfRequired(DiscoveryCollection collection) {
		if (collection.getCreated() != null) {
			return collection;
		}
		return getCollection(collection.getName());
	}
}
