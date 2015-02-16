package no.difi.sdp.webclient.repository;

import java.util.List;

import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.domain.MessageStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long>{

	public List<Message> findByConversationId(String conversationId);
	
	public List<Message> findByStatus(MessageStatus status);
	
	@Query("select m.status, count(*) from Message m group by m.status")
	public List<Object[]> countByStatus();
	
	@Query("select m.conversationId, m.digital, m.ssn, m.fysiskPost.adressat.navn, m.digitalPost.postboxVendorOrgNumber, m.digitalPost.postboxAddress, m.status, m.date, m.requestSentDate, m.responseReceivedDate, m.completedDate, r.type, r.date, r.requestSentDate, r.responseReceivedDate, r.completedDate, r.ackRequestSentDate, r.ackResponseReceivedDate, r.postboxDate from Message m left join m.receipts r")
	public List<Object[]> getReport();

	@Query("select m.id as id, m.date as date, m.ssn, m.document.title, m.digital, m.fysiskPost.adressat.navn from Message m order by m.id desc")
	public Page<Object[]> list(Pageable pageable);
	
	@Query("select m.id as id, m.date as date, m.ssn, m.document.title, m.digital, m.fysiskPost.adressat.navn from Message m where m.status=?1 order by m.id desc")
	public Page<Object[]> list(MessageStatus messageStatus, Pageable pageable);

	@Query("select distinct m.keyPairAlias from Message m where m.status=no.difi.sdp.webclient.domain.MessageStatus.WAITING_FOR_RECEIPT or m.status=no.difi.sdp.webclient.domain.MessageStatus.WAITING_FOR_OPENED_RECEIPT or m.status=no.difi.sdp.webclient.domain.MessageStatus.WAITING_FOR_DELIVERED_RECEIPT")
	public List<String> waitingClients();

}
