package no.difi.sdp.webclient.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class Configuration {

	@Id
	private Long id;
	
	@NotNull
	private String messagePartitionChannel;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getMessagePartitionChannel() {
		return messagePartitionChannel;
	}

	public void setMessagePartitionChannel(String messagePartitionChannel) {
		this.messagePartitionChannel = messagePartitionChannel;
	}

}
