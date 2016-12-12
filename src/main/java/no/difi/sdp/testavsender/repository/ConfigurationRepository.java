package no.difi.sdp.testavsender.repository;

import no.difi.sdp.testavsender.domain.Configuration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long>{

}
