package wy.qingdao_atmosphere.countrysitedata;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskConfigration {
	@Bean
    public ScheduledThreadPoolExecutor scheduledExecutorService() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(15);
        return executor;
    }


}
