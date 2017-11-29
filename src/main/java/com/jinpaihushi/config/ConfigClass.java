package com.jinpaihushi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations = { "classpath:spring-email.xml" })
public class ConfigClass {

}
