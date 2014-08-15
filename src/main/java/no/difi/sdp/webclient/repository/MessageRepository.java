package no.difi.sdp.webclient.repository;

import java.util.List;

import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.domain.MessageStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long>{

	public List<Message> findByConversationId(String conversationId);
	
	public List<Message> findByStatus(MessageStatus status);
	
	@Query("select count(*) from Message m where m.status=:status")
	public int countByStatus(@Param("status") MessageStatus status);

	@Query("select m.status, count(*) from Message m group by m.status")
	public List<Object[]> countByStatus();
	
	@Query("select m.conversationId, m.ssn, m.postboxVendorOrgNumber, m.postboxAddress, m.status, m.date, r.type, r.date from Message m left join m.receipts r")
	public List<Object[]> getReport();

}
