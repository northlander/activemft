package co.nordlander.activemft.config;

import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Fires up a Broker. The broker will transactionally store files for queued
 * delivery.
 */
@Configuration
@EnableJms
public class BrokerConfig {

	/**
	 * Embedded Artemis broker.
	 * 
	 * @return the created broker.
	 * @throws Exception
	 */
	@Bean(initMethod = "start")
	public ActiveMQServer activeMqArtemisServer() throws Exception {
		ConfigurationImpl config = new ConfigurationImpl();
		Set<TransportConfiguration> transports = new HashSet<>();
		// TODO figure out if external access is necessary.
		transports.add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
		transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
		config.setAcceptorConfigurations(transports);
		config.setSecurityEnabled(false);
		ActiveMQServer server = new ActiveMQServerImpl(config);
		server.start();
		return server;
	}
	
	@Bean
	@DependsOn(value = "activeMqArtemisServer")
	public ConnectionFactory connectionFactory(){
		TransportConfiguration transportConfiguration = new TransportConfiguration(InVMConnectorFactory.class.getName());
		ConnectionFactory connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.XA_CF,
				transportConfiguration);
		return connectionFactory;
	}

	@Bean
	@DependsOn(value = "connectionFactory")
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
	
		factory.setConcurrency("1");
		return factory;
	}
	
	@Bean
	@DependsOn(value = "connectionFactory")
	public JmsTemplate jmsTemplate(){
		return new JmsTemplate(connectionFactory());
	}
	
}
