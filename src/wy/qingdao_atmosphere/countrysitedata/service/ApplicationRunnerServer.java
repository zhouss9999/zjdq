package wy.qingdao_atmosphere.countrysitedata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
public class ApplicationRunnerServer /*implements ApplicationListener<ContextRefreshedEvent>*//* implements InitializingBean*/ {
	
	
	//@Autowired
	private WeiBoService wb;
	
	//@PostConstruct
	public void test() {
		/*
		 * 为什么先做判断，因为Spring存在两个容器，一个是root application context ,
		 * 另一个就是我们自己的 projectName-servlet context（作为root application context的子容器）。
		 * 这种情况下，就会造成onApplicationEvent方法被执行两次 .
		 * 为了避免上面提到的问题，我们可以只在root application context初始化完成后调用逻辑代码，
		 * 其他的容器的初始化完成，则不做任何处理。
		 */
		// if (contextRefreshedEvent.getApplicationContext().getParent() == null) {//保证只执行一次
		System.out.println("项目启动执行，开始测试改版辐射计....");
		try{
		       wb.testTbFsj(new HashMap<String,Object>());
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("测试改版辐射计(动态添加设备的)");
			
		}
		System.out.println("测试改版辐射计");
	            //需要执行的方法
	     //   }
	}

	public void afterPropertiesSet() throws Exception {
		System.out.println("InitializingBean  执行了。。。。。。。。。");
		
	}
	

}
