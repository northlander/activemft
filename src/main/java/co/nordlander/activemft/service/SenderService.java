package co.nordlander.activemft.service;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;
import co.nordlander.activemft.repository.TransferJobRepository;

@Service
@Transactional
@TransactionAttribute(TransactionAttributeType.NEVER)
public class SenderService {

	@Inject TransferJobRepository transferJobRepo;
	@Inject TransferEventRepository transferEventRepo;
	@Inject FileSender fileSender;
	
	private static final Logger log = LoggerFactory.getLogger(SenderService.class);
	
	@JmsListener(destination = "transfers")
	public void listenForTransfer(Message msg) throws JMSException{
		
		Long jobId = msg.getLongProperty("jobId");
		Long eventId = msg.getLongProperty("eventId");
		TransferJob transferJob = transferJobRepo.findOne(jobId);
		
		transferEventRepo.flush();
		
		if( !transferEventRepo.exists(eventId)){
			final String errorMsg = String.format("No transfer event with id %d found for job %d.",eventId,jobId);
			log.warn(errorMsg);
			throw new RuntimeException(errorMsg);
		}
		
		// Find the event.
		TransferEvent transferEvent = transferEventRepo.findOne(eventId);
		
		
		transferEvent.setState("sending");
		transferEvent.setTimestamp(new DateTime());
		transferEvent = transferEventRepo.save(transferEvent);
		
		switch(transferJob.getTargetType()){
		case "file":
			fileSender.sendFile(msg, transferJob,transferEvent);
			break;
			default:
				log.warn("Unsupported target type: {}", transferJob.getTargetType());
				break;
		}
		
	}	
}
