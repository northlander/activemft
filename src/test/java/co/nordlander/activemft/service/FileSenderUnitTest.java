package co.nordlander.activemft.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import co.nordlander.activemft.config.BrokerConfig;
import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;
import co.nordlander.activemft.service.util.Constants;

/**
 * Unit/Integration test for FileSender Module.
 * Embedds JMS broker.
 * @author petter
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class FileSenderUnitTest {
	
	private static final Logger log = LoggerFactory.getLogger(FileSenderUnitTest.class);
	
	@ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Inject JmsTemplate jmsTemplate;
	
	protected FileReceiver fileReceiver;

	// Encoding trip-wire: "I can eat glass and it doesn't hurt me." - in old norse. 
	protected static final String SAMPLE_TEXT = "ᛖᚴ ᚷᛖᛏ ᛖᛏᛁ ᚧ ᚷᛚᛖᚱ ᛘᚾ ᚦᛖᛋᛋ ᚨᚧ ᚡᛖ ᚱᚧᚨ ᛋᚨᚱ";

	protected File sourceFolder; // Physical path to file.

	protected TransferEventRepository mockedEventRepo;
	
	@Before
	public void setupTest(){
		fileReceiver = new FileReceiver();
		
		mockedEventRepo = mock(TransferEventRepository.class);
		
		// Make saveAndFlush return whatever is supposed to be saved to avoid NPEs.
		when(mockedEventRepo.saveAndFlush(any(TransferEvent.class))).then( new Answer<TransferEvent>(){
			public TransferEvent answer(InvocationOnMock invocation) throws Throwable {
				TransferEvent event =  invocation.getArgumentAt(0, TransferEvent.class);
				event.setId( event.getId() != null ? event.getId() : 1L);
				return event;
			}
		});
		
		jmsTemplate.setReceiveTimeout(1000L);
		
		fileReceiver.eventRepo = mockedEventRepo;
		fileReceiver.jmsTemplate = jmsTemplate;
		
		sourceFolder = temporaryFolder.getRoot();
	}
	
	/**
	 * A long test that verifies a receive of two files from File with filter.
	 */
	@Test
	public void testFileReceive() throws FileNotFoundException, Exception{
		
		final File file1 = new File(sourceFolder,"file1.xml");
		final File file2 = new File(sourceFolder,"file2.xml");
		final File notIncludedFile = new File(sourceFolder,"file1.txt");
		
		FileUtils.write(file1, SAMPLE_TEXT);
		FileUtils.write(file2, SAMPLE_TEXT + SAMPLE_TEXT);
		FileUtils.write(notIncludedFile, "not important");
		
		final long file1Size = file1.length();
		final long file2Size = file2.length();
		
		TransferJob job = fileJobTemplate();
		job.setSourceUrl("file://"+sourceFolder.getCanonicalPath());
		job.setSourceFilepattern("*.xml");
		
		fileReceiver.receiveFiles(job);
		
		Message resultMsg1 = jmsTemplate.receive("transfers");
		Message resultMsg2 = jmsTemplate.receive("transfers");
		Message shouldBeNull = jmsTemplate.receive("transfers");
		
		assertNotNull(resultMsg1);
		assertNotNull(resultMsg2);
		assertNull(shouldBeNull);

		assertEquals("test-job",resultMsg1.getStringProperty("jobName"));
		assertNotNull(resultMsg1.getStringProperty("jobId"));
		assertEquals(file1.getCanonicalPath(),resultMsg1.getStringProperty("filepath"));
		assertEquals("file1.xml",resultMsg1.getStringProperty("filename"));
		assertEquals(file1Size,resultMsg1.getLongProperty("size"));
		assertEquals(SAMPLE_TEXT,readMessageContentUTF8(resultMsg1));
		
		assertEquals("test-job",resultMsg2.getStringProperty("jobName"));
		assertNotNull(resultMsg2.getStringProperty("jobId"));
		assertEquals(file2.getCanonicalPath(),resultMsg2.getStringProperty("filepath"));
		assertEquals("file2.xml",resultMsg2.getStringProperty("filename"));
		assertEquals(file2Size,resultMsg2.getLongProperty("size"));
		assertEquals(SAMPLE_TEXT + SAMPLE_TEXT,readMessageContentUTF8(resultMsg2));
	
		assertFalse(file1.exists());
		assertFalse(file2.exists());
		assertTrue(notIncludedFile.exists());
	}
	
	/**
	 * Assure nothing is saved in event database if there are no files to transfer.
	 */
	@Test
	public void testNoFilesToReceive() throws FileNotFoundException, Exception{
		TransferJob job = fileJobTemplate();
		job.setSourceUrl("file://"+sourceFolder.getCanonicalPath());
		job.setSourceFilepattern("*.xml");
		
		fileReceiver.receiveFiles(job);
		Message shouldBeNull = jmsTemplate.receive("transfers");
		assertNull(shouldBeNull);
		
		verify(mockedEventRepo,never()).saveAndFlush(any());
	}
	
	protected TransferJob fileJobTemplate(){
		TransferJob job = new TransferJob();
		job.setEnabled(true);
		job.setId(1L);
		job.setName("test-job");
		job.setCronExpression("*/5 * * ? * *");
		job.setSourceFilepattern("*");
		job.setSourceType(Constants.FILE_TYPE);
		return job;
	}
	
	/**
	 * Read Large message content from Artemis message.
	 * @param msg the message to read large message stream from 
	 * @return the large message content parsed as UTF-8 string.
	 * @throws JMSException
	 * @throws IOException
	 */
	protected String readMessageContentUTF8(Message msg) throws JMSException, IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		msg.setObjectProperty("JMS_AMQ_SaveStream", bos);
		return IOUtils.toString(bos.toByteArray(),StandardCharsets.UTF_8.name());
	}

}
