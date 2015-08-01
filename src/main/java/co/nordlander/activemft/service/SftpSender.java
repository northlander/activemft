package co.nordlander.activemft.service;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Message;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;

/**
 * SFTP sender.
 * Sends transfers to SFTP server.
 * @author petter
 *
 */
@Service
public class SftpSender extends Sender{

private static final Logger log = LoggerFactory.getLogger(FileSender.class);
	
	@Inject
	protected TransferEventRepository eventRepo;
	
	protected SshClient sshClient;
	
	@PostConstruct
	protected void setup(){
		sshClient = SshClient.setUpDefaultClient();
		sshClient.start();
	}

	@Override
	public void sendFile(Message msg, TransferJob job, TransferEvent event) {
		try {
			final String filename = msg.getStringProperty("filename");			
			final URI url = new URI(job.getTargetUrl());
			final String hostname = url.getHost();
			final int port = url.getPort() != -1 ? url.getPort() : 22;
			final String path = url.getPath();
			
	        ConnectFuture connectFuture = sshClient.connect(job.getTargetUsername(), hostname, port);
			
	        ClientSession session = connectFuture.await().getSession();
	        session.addPasswordIdentity(job.getTargetPassword());
	        session.auth().await();
	        SftpClient sftpClient = session.createSftpClient();
	        
			// TODO make it possible to rename the file according to some generic pattern.
			//job.getTargetFilename() + RandomStringUtils.randomAlphanumeric(5);
			log.debug("Saving file to SFTP: {}, job: {}", hostname + ":" + port +  path + "/" + filename, job.getName() + "(" + job.getId() + ")");
			OutputStream os = sftpClient.write(path + "/" + filename);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			// This will block until the entire content is saved on disk
			msg.setObjectProperty("JMS_AMQ_SaveStream", bos);
			bos.close();
			os.close();
			event.setState("done");
			event.setTimestamp(new DateTime());
			event = eventRepo.saveAndFlush(event);
			log.debug("File saved to SFTP: {}, job: {}", path + "/" + filename, job.getName() + "(" + job.getId() + ")");
		} catch (Exception e) {
			event.setState("send failed");
			event.setTimestamp(new DateTime());
			event = eventRepo.save(event);
			log.warn("Error sending file {}, job: {}", event.getFilename(), job.getName() + "(" + job.getId() + ")");
			log.warn("Error descr", e);

			throw new RuntimeException("Rollback SFTP transaction");
		}
	}
}
