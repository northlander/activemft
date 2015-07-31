package co.nordlander.activemft.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;
import co.nordlander.activemft.service.util.Constants;

public class SftpReceiverUnitTest {

	
	private static final Logger log = LoggerFactory.getLogger(SftpReceiverUnitTest.class);
	protected SftpReceiver sftpReceiver;
	
	@ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	static protected EmbeddedSftp sftpServer;
	static File sftpRootFolder;
	
	static protected String sftpUsername = "sftpuser";
	static protected String sftpPassword = "sftpPassword";
	// Encoding trip-wire: "I can eat glass and it doesn't hurt me." - in old norse. 
	protected static final String SAMPLE_TEXT = "ᛖᚴ ᚷᛖᛏ ᛖᛏᛁ ᚧ ᚷᛚᛖᚱ ᛘᚾ ᚦᛖᛋᛋ ᚨᚧ ᚡᛖ ᚱᚧᚨ ᛋᚨᚱ";
	
	
	@BeforeClass
	public static void setupSftp() throws IOException{
		sftpRootFolder = temporaryFolder.newFolder("sftproot");
		sftpServer = new EmbeddedSftp(0, sftpUsername, sftpPassword, sftpRootFolder);
		log.info("SftpReceiverUnitTest class setup done" );
	}
	
	@AfterClass
	public static void tearDownSftp(){
		sftpServer.stop();
		sftpServer.close();
	}
	
	@Before
	public void setupTest(){
		
		sftpReceiver = new SftpReceiver();
		
		JmsTemplate mockJmsTemplate = mock(JmsTemplate.class);
		TransferEventRepository mockedEventRepo = mock(TransferEventRepository.class);
		when(mockedEventRepo.saveAndFlush(any(TransferEvent.class))).then( new Answer<TransferEvent>(){
			public TransferEvent answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgumentAt(0, TransferEvent.class);
			}
		});
		
		sftpReceiver.eventRepo = mockedEventRepo;
		sftpReceiver.jmsTemplate = mockJmsTemplate;
		
		sftpReceiver.setup();
		log.info("Test setup done");
	}
	
	@Test
	public void test1() throws FileNotFoundException, Exception{
		
		File sourceFolder = new File(sftpRootFolder,"files");
		sourceFolder.mkdir();
		
		FileUtils.write(new File(sourceFolder,"file1.xml"), SAMPLE_TEXT);
		FileUtils.write(new File(sourceFolder,"file2.xml"), SAMPLE_TEXT + SAMPLE_TEXT);
		FileUtils.write(new File(sourceFolder,"file1.txt"), "not important");
		
		TransferJob job = sftpJobTemplate();
		job.setSourceUrl("sftp://localhost:"+sftpServer.getPort()+"/files");
		job.setSourceFilepattern("*.xml");
		
		sftpReceiver.receiveFiles(job);
		
		Thread.sleep(2500L);
		verify(sftpReceiver.jmsTemplate,times(2)).send(anyString(), any(MessageCreator.class));
		
	}
	
	protected TransferJob sftpJobTemplate(){
		TransferJob job = new TransferJob();
		job.setEnabled(true);
		job.setId(1L);
		job.setName("test-job");
		job.setCronExpression("*/5 * * ? * *");
		job.setSourceFilepattern("*");
		job.setSourceType(Constants.SFTP_TYPE);
		job.setSourceUsername(sftpUsername);
		job.setSourcePassword(sftpPassword);
		return job;
	}
	
}
