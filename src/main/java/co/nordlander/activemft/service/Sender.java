package co.nordlander.activemft.service;

import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;

/**
 * Abstract base class for Senders.
 * @author petter
 *
 */
public abstract class Sender {
	
	private static final Logger log = LoggerFactory.getLogger(Sender.class);
	
	public abstract void sendFile(Message msg, TransferJob job, TransferEvent event);

}
