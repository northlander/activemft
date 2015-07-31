package co.nordlander.activemft.service;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferJobRepository;
import co.nordlander.activemft.service.util.Constants;

/**
 * File receiver service. Will read files from various places.
 * @author Petter Nordlander
 */
@Service
public class ReceiverService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);
   
	protected Scheduler scheduler;
	@Inject protected SchedulerFactoryBean schedulerFactory; 
	@Inject protected TransferJobRepository transferJobRepo;
	@Inject protected FileReceiver fileReceiver;
	@Inject protected SftpReceiver sftpReceiver;

	protected Map<String,Receiver> receivers = new HashMap<>();
	protected void mapSourceTypesWithReceivers(){
		receivers.put(Constants.FILE_TYPE, fileReceiver);
		receivers.put(Constants.SFTP_TYPE, sftpReceiver);
	}

    @PostConstruct
    protected void initReceiver() throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException{
    	mapSourceTypesWithReceivers();
    	
    	scheduler = schedulerFactory.getScheduler();
    	for(TransferJob transferJob : transferJobRepo.findAll()){
    		initTransferJob(transferJob);
    	}
    }
    
    /**
     * Run a file receive job.
     * @param transferJob job to trigger.
     * @throws Exception 
     * @throws FileNotFoundException 
     */
    public void runReceiveJob(TransferJob transferJob) throws FileNotFoundException, Exception{
    	LOGGER.debug("Transfer schedule triggered for job {}",transferJob.getName());

    	// TODO some row lock similar feature to keep only one instance of the job running.
    	// I.e. If another trigger fires while the first is still working (slow file system..) then ignore 2nd trigger.
    	
    	// reload job from factory
    	transferJob = transferJobRepo.findOne(transferJob.getId());
    	Receiver receiver = receivers.get(transferJob.getSourceType());
    	if( receiver != null){
    		receiver.receiveFiles(transferJob);
    	}else{
    		throw new IllegalArgumentException("No source type of type '" + transferJob.getSourceType() + "' is available");
    	}
    }
    
    public void reinitTransferJob(TransferJob transferJob) throws SchedulerException, ClassNotFoundException, NoSuchMethodException, ParseException{
    	deinitTransferJob(transferJob);
    	initTransferJob(transferJob);
    }
    
    public void deinitTransferJob(TransferJob transferJob) throws SchedulerException, ClassNotFoundException, NoSuchMethodException, ParseException{
    	scheduler.deleteJob(new JobKey(transferJob.getId() + "_job"));
    	LOGGER.debug("Transfer job: {}({}) deinitialized.",transferJob.getName(),transferJob.getId());
    }

	public void initTransferJob(TransferJob transferJob) throws ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException {
		MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
    	jobDetailFactory.setTargetObject(this);
    	jobDetailFactory.setTargetMethod("runReceiveJob");
    	jobDetailFactory.setConcurrent(false);
    	jobDetailFactory.setArguments(new Object[]{transferJob});
    	jobDetailFactory.setName(transferJob.getId() + "_job");
    	jobDetailFactory.afterPropertiesSet();
    	JobDetail jobDetail = jobDetailFactory.getObject();
    	
    	CronTriggerFactoryBean triggerFactory = new CronTriggerFactoryBean();
    	triggerFactory.setCronExpression(transferJob.getCronExpression());
    	triggerFactory.setName(transferJob.getName() + "_trigger");
    	triggerFactory.setJobDetail(jobDetail);
    	
    	triggerFactory.afterPropertiesSet();
    	scheduler.scheduleJob(jobDetail,triggerFactory.getObject());
    	LOGGER.debug("Transfer job: {}({}) initialized", transferJob.getName(),transferJob.getId());
	}
}
