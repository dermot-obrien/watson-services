package org.dobrien.watson.services.configuration;

import java.util.List;

import org.dobrien.watson.services.model.DiscoveryCollection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
	
	@JsonProperty("discoveryCollections")
	private List<DiscoveryCollection> discoveryCollections = null;

	public List<DiscoveryCollection> getDiscoveryCollections() {
		return discoveryCollections;
	}

	public void setDiscoveryCollections(List<DiscoveryCollection> discoveryCollections) {
		this.discoveryCollections = discoveryCollections;
	}

}
