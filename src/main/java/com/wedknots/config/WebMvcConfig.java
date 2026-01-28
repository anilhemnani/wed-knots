package com.wedknots.config;

import com.wedknots.web.TrafficLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TrafficLoggingInterceptor trafficLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trafficLoggingInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve event-specific about pages and images
        registry.addResourceHandler("/events/*/about/**")
                .addResourceLocations("classpath:/static/events/");
        registry.addResourceHandler("/events/*/wedding_location_nav.png")
                .addResourceLocations("classpath:/static/events/");
    }
}
