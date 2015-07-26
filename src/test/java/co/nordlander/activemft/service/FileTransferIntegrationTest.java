package co.nordlander.activemft.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Collection;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import co.nordlander.activemft.config.BrokerConfig;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferEventRepository;

/**
 * Test that {@link FileReceiver} is able to pick up files.
 * @author petter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class FileReceiverTest {
	
	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Inject protected JmsTemplate jmsTemplate;
	
	protected FileReceiver fileReceiver = new FileReceiver();
	
	@Before
	public void setupTest(){
		TransferEventRepository eventRepo = mock(TransferEventRepository.class);
		fileReceiver.eventRepo = eventRepo;
		fileReceiver.jmsTemplate = jmsTemplate;	
	}
	
	@Test
	public void testFileListing() throws Exception{
		tempFolder.newFile("file1.txt");
		tempFolder.newFile("file2.xml"); // should not be found.
		Collection<File> listedFiles = fileReceiver.listFiles(tempFolder.getRoot().getCanonicalPath(), 0L, "*.txt");
		assertEquals(1,listedFiles.size());
		File foundFile = listedFiles.iterator().next();
		assertEquals("file1.txt",foundFile.getName());
		assertEquals(tempFolder.getRoot().getCanonicalPath(),foundFile.getParentFile().getCanonicalPath());
		
		// TODO test file age listing.
	}
	
	@Test
	public void testFileReceive(){
		TransferJob job = new TransferJob();
		job.setId(1L);
		job.setEnabled(true);
		job.setName("thaJob");
		job.setSourceType("file");
		fileReceiver.receiveFiles(job);
	}
}
