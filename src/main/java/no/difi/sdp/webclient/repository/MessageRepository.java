package no.difi.sdp.webclient.repository;

import java.util.List;

import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.domain.MessageStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long>{

	public List<Message> findByConversationId(String conversationId);
	
	public List<Message> findByStatus(MessageStatus status);
	
	@Query("select m.status, count(*) from Message m group by m.status")
	public List<Object[]> countByStatus();
	
	@Query("select m.conversationId, m.ssn, m.postboxVendorOrgNumber, m.postboxAddress, m.status, m.date, r.type, r.date from Message m left join m.receipts r")
	public List<Object[]> getReport();

	@Query("select m.id as id, m.date as date, m.ssn, m.document.title from Message m order by m.id desc")
	public List<Object[]> list();

	@Query("select m.id as id, m.date as date, m.ssn, m.document.title from Message m where m.status=?1 order by m.id desc")
	public List<Object[]> list(MessageStatus messageStatus);
	
	@Query("select distinct m.keyPairAlias from Message m where m.status=no.difi.sdp.webclient.domain.MessageStatus.WAITING_FOR_RECEIPT or m.status=no.difi.sdp.webclient.domain.MessageStatus.WAITING_FOR_OPENED_RECEIPT")
	public List<String> waitingClients();
	
}
