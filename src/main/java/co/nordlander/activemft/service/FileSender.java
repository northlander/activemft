package co.nordlander.activemft.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;

/**
 * Sender to store files in target folders.
 * May be local file system, but more likely, a remote file system managed by the OS.
 * I.e. Could be a mapped network drive on Windows or a NFS mount on unix, etc.
 */
@Service
public class FileSender extends Sender{

	private static final Logger log = LoggerFactory.getLogger(FileSender.class);
	
	@Inject
	protected TransferEventRepository eventRepo;

	@Override
	public void sendFile(Message msg, TransferJob job, TransferEvent event) {
		try {
			String targetUrl = job.getTargetUrl();
			final String path = targetUrl.startsWith("file://") ? targetUrl.substring("file://".length()) : targetUrl;
			final String filename = msg.getStringProperty("filename");
			// TODO make it possible to rename the file according to some generic pattern.
			//job.getTargetFilename() + RandomStringUtils.randomAlphanumeric(5);
			log.debug("Saving file to: {}, job: {}", path + "/" + filename, job.getName() + "(" + job.getId() + ")");
			File outputFile = new File(path, filename);
			FileOutputStream fos;

			fos = new FileOutputStream(outputFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			// This will block until the entire content is saved on disk
			msg.setObjectProperty("JMS_AMQ_SaveStream", bos);
			bos.close();
			fos.close();
			event.setState("done");
			event.setTimestamp(new DateTime());
			event = eventRepo.saveAndFlush(event);
			log.debug("File saved to: {}, job: {}", path + "/" + filename, job.getName() + "(" + job.getId() + ")");
		} catch (JMSException | IOException e) {
			event.setState("send failed");
			event.setTimestamp(new DateTime());
			event = eventRepo.save(event);
			log.warn("Error sending file {}, job: {}", event.getFilename(), job.getName() + "(" + job.getId() + ")");
			log.warn("Error descr", e);

			throw new RuntimeException("Rollback plz");
		}
	}
}
