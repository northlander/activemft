package co.nordlander.activemft.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class SchedulingConfiguration {
	
	@Inject DataSource dataSource;

	@Bean public SchedulerFactoryBean schedulerFactor(){
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		Properties quartzProperties = new Properties();
		quartzProperties.setProperty("org.quartz.jobStore.class","org.quartz.impl.jdbcjobstore.JobStoreTX");
		quartzProperties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		quartzProperties.setProperty("org.quartz.jobStore.tablePrefix","QRTZ_");
	//	schedulerFactory.setDataSource(dataSource);
		//schedulerFactory.setQuartzProperties(quartzProperties);
		return schedulerFactory;
	}
}
