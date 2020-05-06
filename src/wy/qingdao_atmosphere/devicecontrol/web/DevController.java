package wy.qingdao_atmosphere.devicecontrol.web;


import java.util.HashMap;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import wy.qingdao_atmosphere.devicecontrol.service.DevService;


//@CrossOrigin
@Controller
public class DevController {
	
	
	@Autowired
	
	private DevService devService;
	
	/**
	 * 操作设备电源
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/power", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> updatePower(HttpServletRequest request){
	
		Logger.getLogger("").info("------操作设备电源---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.updatePower(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("操作设备电源出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	
	
	
	
	/**
	 * 从数据库里查询微波辐射计历史数据(12个小时)
	 * 包含了温度廓线，相对湿度廓线，边界层廓线，水汽密度廓线，液态水廓线
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/power", method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> queryPower(HttpServletRequest request){
	
		Logger.getLogger("").info("------查询设备电源---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.queryPower(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("查询设备电源出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	/**
	 * 鼓风机/接收机风扇控制
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/fans", method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> queryFans(HttpServletRequest request){
	
		Logger.getLogger("").info("------鼓风机/接收机风扇查询---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.queryFans(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("鼓风机/接收机风扇查询出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 鼓风机/接收机风扇控制
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/fans", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> updateFans(HttpServletRequest request){
	
		Logger.getLogger("").info("------鼓风机/接收机风扇控制---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.updateFans(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("鼓风机/接收机风扇控制出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 伺服角度查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/servo", method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> queryServo(HttpServletRequest request){
	
		Logger.getLogger("").info("------伺服角度查询---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.queryServo(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("伺服角度查询出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	/**
	 * 伺服控制
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/servo", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> updateServo(HttpServletRequest request){
	
		Logger.getLogger("").info("------伺服控制---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.updateServo(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("伺服控制出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 定向观测
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/directObs", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> updateDirectObs(HttpServletRequest request){
	
		Logger.getLogger("").info("------定向观测---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.updateDirectObs(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("定向观测出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 扫描观测
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/scanObs", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> scanObs(HttpServletRequest request){
	
		Logger.getLogger("").info("------扫描观测---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.scanObs(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("扫描观测出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 定标
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/calibrate", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> calibrate(HttpServletRequest request){
	
		Logger.getLogger("").info("------定标---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.calibrate(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("定标出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 查询当前设备工作状态
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/workMode", method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> queryWorkMode(HttpServletRequest request){
	
		Logger.getLogger("").info("------查询当前设备工作状态---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.queryWorkMode(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("查询当前设备工作状态出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	/**
	 * 获取日志文件
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/logs", method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> queryLogs(HttpServletRequest request){
	
		Logger.getLogger("").info("------获取日志文件---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.queryLogs(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("获取日志文件出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
	
	
	/**
	 * 伺服控制
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/api/update", method = RequestMethod.POST,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String,Object> update(HttpServletRequest request){
	
		Logger.getLogger("").info("------远程升级---------");
		
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		try{
			resultMap=devService.update(request);
		}catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("").error("远程升级出错了");
			resultMap =  new HashMap<String,Object>(); //返回空
		}
		return resultMap;
		
	}
	
		
		
	
		
		
}
