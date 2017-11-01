package com.example.poc;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Clase de configuracion, encargada de inyectar los beans en el 
 * contenedor spring.
 * 
 * @author motero
 *
 */
@Configuration
public class ApplicationConfig {
    
    /** The Constant CAMEL_URL_MAPPING. Contiene la ruta base publicada del servicio rest */
    private static final String CAMEL_URL_MAPPING = "/*";
    
    /** The Constant CAMEL_SERVLET_NAME. Nombre del servlet que va a ser publicado */
    private static final String CAMEL_SERVLET_NAME = "CamelServlet";
    
    /**
     * Servlet registration bean, Bean usado para la publicacion del servicio rest en camel con Spring Boot.
     *
     * @return ServletRegistrationBean Objeto del tipo ServletRegistrationBean
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), CAMEL_URL_MAPPING);
        registration.setName(CAMEL_SERVLET_NAME);
        return registration;
    }
    
}
