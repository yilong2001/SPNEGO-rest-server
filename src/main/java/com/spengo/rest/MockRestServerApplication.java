package com.spengo.rest;

/**
 * Created by yilong on 2017/10/17.
 */

import com.spengo.rest.auth.filter.HttpAuthFilter;
import com.spengo.rest.auth.kbr.KbrAuthHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@SpringBootApplication
//@EnableDiscoveryClient
public class MockRestServerApplication {
    private static Logger LOG = LoggerFactory.getLogger(MockRestServerApplication.class);

    private static final KbrAuthHandler handler = new KbrAuthHandler();
    private static final Timer timer = new Timer();
    private static final PeriodicTask periodicTask = new PeriodicTask();

    @Bean
    public AlwaysSampler defaultSampler(){
        return new AlwaysSampler();
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        HttpAuthFilter httpBasicFilter = new HttpAuthFilter(handler);
        registrationBean.setFilter(httpBasicFilter);
        List<String> urlPatterns = new ArrayList<String>();
        urlPatterns.add("/*");
        registrationBean.setUrlPatterns(urlPatterns);

        return registrationBean;
    }

    public static void main(String[] args){
        timer.schedule(periodicTask, 0);
        SpringApplication.run(MockRestServerApplication.class, args);
    }

    private static class PeriodicTask extends TimerTask {
        @Override
        public void run() {
            try {
                handler.init();
                LOG.info("KbrAuthHandler init ... ");
                Thread.sleep(60 * 60 * 1000);
            } catch(InterruptedException e) {
                LOG.error(e.getMessage(), e);
            } catch (Exception e1) {
                LOG.error(e1.getMessage(), e1);
            }

            timer.schedule(periodicTask, 10 * 1000);
        }
    }
}
