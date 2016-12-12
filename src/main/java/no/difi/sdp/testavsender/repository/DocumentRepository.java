package no.difi.sdp.testavsender.repository;

import no.difi.sdp.testavsender.domain.Document;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
