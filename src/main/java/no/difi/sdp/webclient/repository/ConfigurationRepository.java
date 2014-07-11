package no.difi.sdp.webclient.repository;

import no.difi.sdp.webclient.domain.Configuration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long>{

}
