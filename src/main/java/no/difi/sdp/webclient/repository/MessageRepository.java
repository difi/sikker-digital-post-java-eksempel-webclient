package no.difi.sdp.webclient.repository;

import java.util.List;

import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.domain.MessageStatus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long>{

	public List<Message> findByConversationId(String conversationId);
	
	public List<Message> findByStatus(MessageStatus status);
	
}
