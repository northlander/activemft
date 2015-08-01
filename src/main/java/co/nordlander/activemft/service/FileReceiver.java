package co.nordlander.activemft.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.filters.FileReceiveFilter;
import co.nordlander.activemft.repository.TransferEventRepository;

/**
 * Grabs files from local file system.
 * @author petter
 *
 */
@Service
public class FileReceiver extends Receiver{
	
	private static final Logger log = LoggerFactory.getLogger(FileReceiver.class);
	
	@Inject TransferEventRepository eventRepo;
	@Inject JmsTemplate jmsTemplate;
	
	public void receiveFiles(TransferJob job) throws FileNotFoundException, Exception{
		final String pattern = job.getSourceFilepattern();
		final URI url = new URI(job.getSourceUrl());
		final String path = url.getPath();
		
		final Long fileage = 100L;
		
		Collection<File> files = FileUtils.listFiles(new File(path), new FileReceiveFilter(fileage, pattern), FalseFileFilter.INSTANCE);
		
		log.debug("{} file found for job {}",files.size(),job.getName());
		for(File file : files){
			log.debug("Processing: {}",file.getCanonicalPath());
			queueFile(job,file);
			file.delete();
		}
	}
	
	public void queueFile(final TransferJob job, File file) throws Exception{

		final InputStream stream = new FileInputStream(file);
		final String filename = file.getName();
		final String filepath = file.getCanonicalPath();
		final String transferId = UUID.randomUUID().toString();
		
		TransferEvent event = new TransferEvent();
		event.setTransferJob(job);
		event.setTransferId(transferId);
		event.setFilename(filename);
		event.setSize((int) FileUtils.sizeOf(file));
		final Long fileSize = file.length();
		
		event.setState("started");
		event.setTimestamp(new DateTime());
		event = eventRepo.saveAndFlush(event);
		final Long eventId = event.getId();
		
		ServerLocator locator = ActiveMQClient.createServerLocatorWithoutHA(new TransportConfiguration(
                InVMConnectorFactory.class.getName()));
	
		locator.setMinLargeMessageSize(2048);
		
		jmsTemplate.send("transfers", (Session session) -> {
				Message msg = session.createStreamMessage();
				msg.setObjectProperty("JMS_AMQ_InputStream", stream); // blocks until data streamed to server.
				msg.setStringProperty("jobName", job.getName());
				msg.setLongProperty("jobId", job.getId());
				msg.setLongProperty("eventId", eventId);
				msg.setStringProperty("filename", filename);
				msg.setStringProperty("filepath", filepath);
				msg.setLongProperty("size", fileSize);
				return msg;
			});

		stream.close();
		log.debug("File queued: {}",filename);
		event.setTimestamp(new DateTime());
		event.setState("received");
		eventRepo.saveAndFlush(event);
	}
}
