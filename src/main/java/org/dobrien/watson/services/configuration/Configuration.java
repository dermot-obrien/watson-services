package org.dobrien.watson.services.configuration;

import java.util.List;

import org.dobrien.watson.services.model.DiscoveryCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.watson.developer_cloud.discovery.v1.Discovery;

public class Configuration {
	
	@JsonProperty("versionDate")
	private String versionDate;
	
	@JsonProperty("discoveryCollections")
	private List<DiscoveryCollection> discoveryCollections = null;

	public List<DiscoveryCollection> getDiscoveryCollections() {
		return discoveryCollections;
	}

	public void setDiscoveryCollections(List<DiscoveryCollection> discoveryCollections) {
		this.discoveryCollections = discoveryCollections;
	}

	public DiscoveryCollection getCollection(String collectionName) {
		if (discoveryCollections == null || discoveryCollections.size() == 0) return null; 
		for (DiscoveryCollection discoveryCollection : discoveryCollections) {
			if (discoveryCollection.getName().equals(collectionName)) {
				return discoveryCollection;
			}
		}
		return null;
	}
	
	public Discovery getDiscovery() {
		Discovery discovery = new Discovery(versionDate);
		return discovery;
	}	

}
