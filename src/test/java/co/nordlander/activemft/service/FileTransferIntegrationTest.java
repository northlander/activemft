package co.nordlander.activemft.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import co.nordlander.activemft.Application;
import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;
import co.nordlander.activemft.repository.TransferJobRepository;
import co.nordlander.activemft.service.util.Constants;

/**
 * Integration test that verifies ActiveMFT file transfers.
 * @author petter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class FileTransferIntegrationTest {
	
	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Inject protected ReceiverService receiverService;
	@Inject protected TransferJobRepository jobRepository;
	@Inject protected TransferEventRepository eventRepository;
		
	protected static final String CRON_EXPR_EVERY_SEC = "*/1 * * ? * *";
	
	// Encoding trip-wire: "I can eat glass and it doesn't hurt me." - in old norse. 
	protected static final String SAMPLE_TEXT = "ᛖᚴ ᚷᛖᛏ ᛖᛏᛁ ᚧ ᚷᛚᛖᚱ ᛘᚾ ᚦᛖᛋᛋ ᚨᚧ ᚡᛖ ᚱᚧᚨ ᛋᚨᚱ";
	
	protected EmbeddedSftp sftpServer;
	protected File sftpRootFolder;
	protected final String sftpUsername = "sftpuser";
	protected final String sftpPassword = "sftppassword";
	
	@Before
	public void setup() throws Exception {
		sftpRootFolder = tempFolder.newFolder("sftproot");
		sftpServer = new EmbeddedSftp(0, sftpUsername, sftpPassword, sftpRootFolder);
		sftpServer.start();
	}
	
	@After
	public void cleanup(){
		eventRepository.deleteAll();
		jobRepository.deleteAll();
		
	}
	
	/**
	 * Test transfer from a File source to a File Target.
	 * This includes a test to verify only filtered files are picked up.
	 * @throws Exception
	 */
	@Test
	public void testFileToFile() throws Exception{
		
		final File sourceFolder = tempFolder.newFolder("source");
		final File targetFolder = tempFolder.newFolder("target");
		
		// create one file to be picked up and one to be left.
		FileUtils.write(new File(sourceFolder,"file1.txt"), SAMPLE_TEXT, StandardCharsets.UTF_8);
		FileUtils.write(new File(sourceFolder,"file1.xml"), SAMPLE_TEXT, StandardCharsets.UTF_8);
		
		createTestJob("fileToFileTest1",Constants.FILE_TYPE, sourceFolder.getCanonicalPath(),"*.txt",
										Constants.FILE_TYPE,targetFolder.getCanonicalPath());
		
		Thread.sleep(2000L); // Wait some time for transfer to complete.
		
		// Verify
		final File[] targetFiles = targetFolder.listFiles();
		assertEquals(1,sourceFolder.listFiles().length);
		assertEquals(1,targetFiles.length);
		
		assertEquals("file1.txt",targetFiles[0].getName());
		assertEquals(SAMPLE_TEXT,FileUtils.readFileToString(targetFiles[0], StandardCharsets.UTF_8));
	}
	
	protected TransferJob createTestJob(final String name,final String sourceType, final String sourceUrl, 
										final String pattern, final String targetType, final String targetUrl) 
												throws Exception{
		TransferJob job = new TransferJob();
		job.setEnabled(true);
		job.setName(name);
		job.setSourceType(sourceType);
		job.setSourceUrl(sourceUrl);
		job.setSourceFilepattern(pattern);
		
		job.setTargetType(targetType);
		job.setTargetUrl(targetUrl);
		job.setCronExpression(CRON_EXPR_EVERY_SEC);
		
		job = jobRepository.saveAndFlush(job);
		receiverService.initTransferJob(job);
		return job;
	}
}
