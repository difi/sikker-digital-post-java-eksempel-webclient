package no.difi.sdp.webclient.service;

import java.util.UUID;

import javax.annotation.PostConstruct;

import no.difi.sdp.webclient.domain.Configuration;
import no.difi.sdp.webclient.repository.ConfigurationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

	@Autowired
	ConfigurationRepository configurationRepository;

	@PostConstruct // Avoids concurrency issues by initializing at startup
	public Configuration getConfiguration() {
		Configuration configuration = configurationRepository.findOne(1L);
		if (configuration == null) {
			configuration = new Configuration();
			configuration.setId(1L);
			configuration.setMessagePartitionChannel(UUID.randomUUID().toString());
			configurationRepository.save(configuration);
		}
		return configuration;
	}
	
}
