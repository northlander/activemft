package co.nordlander.activemft.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.SftpClient.DirEntry;
import org.apache.sshd.client.future.ConnectFuture;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;

/**
 * Downloading files from SFTP servers. (SSH based file transfer).
 *  
 * @author petter
 *
 */
@Service
public class SftpReceiver extends Receiver {

	private static final Logger log = LoggerFactory.getLogger(SftpReceiver.class);
	protected SshClient sshClient;
	
	@Inject TransferEventRepository eventRepo;
	@Inject JmsTemplate jmsTemplate;
	
	@PostConstruct
	protected void setup(){
		sshClient = SshClient.setUpDefaultClient();
		sshClient.start();
	}
	
	@Override
	public void receiveFiles(TransferJob job) throws FileNotFoundException, Exception {
		
		log.debug("Receiving files from {}",job.getSourceUrl());
		// TODO error handle URI parsing.
		final URI url = new URI(job.getSourceUrl());
		final String hostname = url.getHost();
		final int port = url.getPort() != -1 ? url.getPort() : 22;
		final String path = url.getPath();
        ConnectFuture connectFuture = sshClient.connect(job.getSourceUsername(), hostname, port);
		
        ClientSession session = connectFuture.await().getSession();
        session.addPasswordIdentity(job.getSourcePassword());
        session.auth().await();
        SftpClient sftpClient = session.createSftpClient();
        
        for(DirEntry dirEntry : sftpClient.readDir(path)){
        	
        	if( dirEntry.attributes.isRegularFile() 
        			&& FilenameUtils.wildcardMatch(dirEntry.filename, job.getSourceFilepattern())){
        		queueFile(dirEntry,path,sftpClient,job);
        	}
        }
        
        sftpClient.close();
        session.close(false);
	}

	protected void queueFile(DirEntry dirEntry, String path, SftpClient sftpClient,TransferJob job) throws IOException {
		final String filepath = path + "/" + dirEntry.filename;
		final String filename = dirEntry.filename;
		final String transferId = UUID.randomUUID().toString();

		final InputStream stream = sftpClient.read(filepath);
		
		TransferEvent event = new TransferEvent();
		event.setTransferJob(job);
		event.setTransferId(transferId);
		event.setFilename(filename);
		event.setSize((int) dirEntry.attributes.size);
		
		event.setState("started");
		event.setTimestamp(new DateTime());
		event = eventRepo.saveAndFlush(event);
		final Long eventId = event.getId();
		
		ServerLocator locator = ActiveMQClient.createServerLocatorWithoutHA(new TransportConfiguration(
                InVMConnectorFactory.class.getName()));
	
		locator.setMinLargeMessageSize(2048);
		
		// TODO update event+log nicely if transfer error!!
		jmsTemplate.send("transfers", (Session session) -> {
				Message msg = session.createStreamMessage();
				msg.setObjectProperty("JMS_AMQ_InputStream", stream); // blocks until data streamed to server.
				msg.setStringProperty("jobName", job.getName());
				msg.setLongProperty("jobId", job.getId());
				msg.setLongProperty("eventId", eventId);
				msg.setStringProperty("filename", filename);
				msg.setStringProperty("filepath", filepath);
				msg.setLongProperty("size", dirEntry.attributes.size);
				return msg;
			});

		stream.close();
		log.debug("File queued: {}",filename);
		event.setTimestamp(new DateTime());
		event.setState("received");
		eventRepo.saveAndFlush(event);
		sftpClient.remove(filepath);
	}

}
