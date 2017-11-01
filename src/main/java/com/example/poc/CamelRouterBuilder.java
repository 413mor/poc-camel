package com.example.poc;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;


/**
 * Clase que representa un componente de spring. Contiene la definicion de rutas
 * de los servicios a exponer con apache camel
 * 
 * @author motero
 *
 */
@Component
public class CamelRouterBuilder extends RouteBuilder {

	/**
	 * Constante que contiene el valor del componente de configuracion de camel
	 */
	private static final String COMPONENT_CAMEL = "servlet";
	/** La Constante LOG. Obtiene el Logger de la clase */
	private static final Logger LOG = LoggerFactory.getLogger(CamelRouterBuilder.class);
	
	@Autowired
	private ApplicationContext applicationContext;


	@Override
	public void configure() throws Exception {

		LOG.info("::Inicia configure()::");

		restConfiguration().component(COMPONENT_CAMEL)
				// se habilita cors para los servicios a desplegar
				.enableCORS(true);
		
		//Servicio rest desplegado inicialmente ruta de acceso: /poc/addRoute
		rest("/poc").get("/addRoute")
					.route().log("start addRoute")
					.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				
				// Se agrega ruta en tiempo de ejecucion
				RouteDefinition routeDefinition = new RouteDefinition();
				routeDefinition.from("direct:newRoute").log("newRoute").end();
				exchange.getContext().addRouteDefinition(routeDefinition);
				
				// Se agrega ruta jms
				AbstractApplicationContext abstractApplicationContext = (AbstractApplicationContext) applicationContext;
				DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) abstractApplicationContext
						.getBeanFactory();
				JmsConfiguration configuration = new JmsConfiguration();
				configuration.setTransacted(false);
				configuration.setConcurrentConsumers(1);
				configuration.setMaxConcurrentConsumers(5);
				configuration.setAcceptMessagesWhileStopping(false);
				configuration.setAcknowledgementModeName("AUTO_ACKNOWLEDGE");
				configuration.setCacheLevelName("CACHE_NONE");
				configuration.setRecoveryInterval(0);

				MQQueueConnectionFactory mqFactory = new MQQueueConnectionFactory();
				mqFactory.setTransportType(1);
				mqFactory.setChannel("SMOV.CLT.D0");
				mqFactory.setHostName("180.176.28.44");
				mqFactory.setPort(5919);
				mqFactory.setQueueManager("QXSMOVD");
				mqFactory.setAsyncExceptions(JmsConstants.ASYNC_EXCEPTIONS_CONNECTIONBROKEN);

				CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(mqFactory);
				cachingConnectionFactory.setSessionCacheSize(10);
				cachingConnectionFactory.setCacheConsumers(false);
				configuration.setConnectionFactory(cachingConnectionFactory);
				defaultListableBeanFactory.registerBeanDefinition("CICS1",
						BeanDefinitionBuilder.genericBeanDefinition(JmsComponent.class)
								.addPropertyValue("configuration", configuration).getBeanDefinition());
				routeDefinition = new RouteDefinition();
				routeDefinition.from("direct:coreDomain-CICS1")
				        .bean(BeanCamelJMS.class, "createRequestMessage")
						.to("log:com.example.poc?showAll=true&multiline=true")
						.setHeader("CamelJmsDestinationName",
								constant("queue:///" + "SMOV.QR.REQUEST.D" + "?CCSID=" + "819"
										+ "&targetClient=1"))
						.to("CICS1" + ":queue:" + "SMOV.QR.REQUEST.D" + "?replyTo=" + "SMOV.QL.ANSWER.D"
								+ "&useMessageIDAsCorrelationID=true&jmsMessageType=Text&deliveryPersistent=false&timeToLive="
								+ "10s" + "&requestTimeout=" + "10s")
						.to("log:com.example.poc?showAll=true&multiline=true")
						.bean(BeanCamelJMS.class, "createResponseMap");
				exchange.getContext().addRouteDefinition(routeDefinition);
				
				// Se agrega ruta de tipo rest en tiempo de ejecucion
				RestDefinition restDefinition = new RestDefinition();
				restDefinition.path("/test").get("/newRestRoute").to("direct:coreDomain-CICS1");	
				exchange.getContext().addRouteDefinitions(restDefinition.asRouteDefinition(exchange.getContext()));
			}
		}).log("end addRoute").end();
		
		LOG.info("::Termina configure()::");
	}
	
	

}