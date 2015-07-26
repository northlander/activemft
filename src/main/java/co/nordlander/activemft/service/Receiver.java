package co.nordlander.activemft.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

import javax.inject.Inject;

import org.springframework.jms.core.JmsTemplate;

import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;

public abstract class AbstractReceiver {
	@Inject TransferEventRepository eventRepo;
	@Inject JmsTemplate jmsTemplate;
	
	public abstract Collection<File> listFiles(String path,Long fileage, String pattern);
	
	public abstract void receiveFiles(TransferJob job) throws FileNotFoundException, Exception;
}
