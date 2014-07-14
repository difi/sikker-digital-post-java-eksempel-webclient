package no.difi.sdp.webclient.repository;

import no.difi.sdp.webclient.domain.Document;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
