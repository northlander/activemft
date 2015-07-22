package co.nordlander.activemft.service;

import java.io.FileNotFoundException;
import java.text.ParseException;

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
import org.springframework.transaction.annotation.Transactional;

import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferJobRepository;

/**
 * File receiver service. Will read files from various places.
 * @author Petter Nordlander
 */
@Service
public class ReceiverService {
	
	Scheduler scheduler;
	@Inject SchedulerFactoryBean schedulerFactory; 
	@Inject TransferJobRepository transferJobRepo;
	@Inject FileReceiver fileReceiver;

    private final Logger log = LoggerFactory.getLogger(ReceiverService.class);
   
    /**
     * Run a file receive job.
     * @param transferJob job to trigger.
     * @throws Exception 
     * @throws FileNotFoundException 
     */
    public void runReceiveJob(TransferJob transferJob) throws FileNotFoundException, Exception{
    	log.debug("Transfer schedule triggered for job {}",transferJob.getName());
    	// reload job from factory
    	// TODO some row lock similar feature to keep only one instance of the job running.
    	// I.e. If another trigger fires while the first is still working (slow file system..) then ignore 2nd trigger.
    	transferJob = transferJobRepo.findOne(transferJob.getId());
    	
    	final String type = transferJob.getSourceType();
    	switch( type ){
    	case "file":
    		fileReceiver.receiveFiles(transferJob);
    		break;
    		
    	case "ftp":
    		
    		break;
    	}
    }
    
    @PostConstruct
    public void initReceiver() throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException{
    	scheduler = schedulerFactory.getScheduler();
    	for(TransferJob transferJob : transferJobRepo.findAll()){
    		initTransferJob(transferJob);
    	}
    }
    
    public void reinitTransferJob(TransferJob transferJob) throws SchedulerException, ClassNotFoundException, NoSuchMethodException, ParseException{
    	deinitTransferJob(transferJob);
    	initTransferJob(transferJob);
    }
    
    public void deinitTransferJob(TransferJob transferJob) throws SchedulerException, ClassNotFoundException, NoSuchMethodException, ParseException{
    	scheduler.deleteJob(new JobKey(transferJob.getId() + "_job"));
    	log.debug("Transfer job: {}({}) deinitialized.",transferJob.getName(),transferJob.getId());
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
    	log.debug("Transfer job: {}({}) initialized", transferJob.getName(),transferJob.getId());
	}
}
