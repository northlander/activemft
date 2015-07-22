package co.nordlander.activemft.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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


@Service
public class FileReceiver {

	@Inject TransferEventRepository eventRepo;
	@Inject JmsTemplate jmsTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(FileReceiver.class);
	/**
	 * List files.
	 * @param path path to directory.
	 * @param fileage min file age to get file.
	 * @param pattern empty or * for all files.
	 * @return
	 */
	public Collection<File> listFiles(String path,Long fileage, String pattern){
		File directory = new File(path);
		return FileUtils.listFiles(directory, new FileReceiveFilter(fileage, pattern), FalseFileFilter.INSTANCE);
	}
	
	public void receiveFiles(TransferJob job) throws FileNotFoundException, Exception{
		final String pattern = job.getSourceFilepattern();
		final String url = job.getSourceUrl();
		final String path = url.startsWith("file://") ? url.substring("file://".length()) : url;

		final Long fileage = 100L;
		Collection<File> files = listFiles(path , fileage, pattern);
		
		log.debug("{} file found for job {}",files.size(),job.getName());
		for(File file : files){
			log.debug("Processing: {}",file.getCanonicalPath());
			queueFile(job,file);
			file.delete();
		}
	}
	
	public void queueFile(final TransferJob job, File file) throws Exception{

		final InputStream stream = new FileInputStream(file);
		final String filename = file.getCanonicalPath();
		final String transferId = UUID.randomUUID().toString();
		
		TransferEvent event = new TransferEvent();
		event.setTransferJob(job);
		event.setTransferId(transferId);
		event.setFilename(filename);
		event.setSize((int) FileUtils.sizeOf(file));
		
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
				return msg;
			});

		stream.close();
		log.debug("File queued: {}",filename);
		event.setTimestamp(new DateTime());
		event.setState("received");
		eventRepo.saveAndFlush(event);
	}
}
