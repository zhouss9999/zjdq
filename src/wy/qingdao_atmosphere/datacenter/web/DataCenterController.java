package wy.qingdao_atmosphere.datacenter.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.sun.xml.rpc.processor.modeler.j2ee.xml.string;

import cmcc.iot.onenet.javasdk.api.cmds.SendCmdsApi;
import cmcc.iot.onenet.javasdk.api.datapoints.AddDatapointsApi;
import cmcc.iot.onenet.javasdk.api.datastreams.AddDatastreamsApi;
import cmcc.iot.onenet.javasdk.api.device.AddDevicesApi;
import cmcc.iot.onenet.javasdk.api.device.DeleteDeviceApi;
import cmcc.iot.onenet.javasdk.api.device.GetDeviceApi;
import cmcc.iot.onenet.javasdk.api.device.GetDevicesStatus;
import cmcc.iot.onenet.javasdk.api.device.ModifyDevicesApi;
import cmcc.iot.onenet.javasdk.model.Data;
import cmcc.iot.onenet.javasdk.model.Datapoints;
import cmcc.iot.onenet.javasdk.response.BasicResponse;
import cmcc.iot.onenet.javasdk.response.cmds.NewCmdsResponse;
import cmcc.iot.onenet.javasdk.response.datastreams.NewdatastramsResponse;
import cmcc.iot.onenet.javasdk.response.device.DeviceResponse;
import cmcc.iot.onenet.javasdk.response.device.DevicesStatusList;
import cmcc.iot.onenet.javasdk.response.device.NewDeviceResponse;


import wy.qingdao_atmosphere.countrysitedata.service.SiteDataService;
import wy.qingdao_atmosphere.datacenter.dao.DataCenterDao;
import wy.qingdao_atmosphere.datacenter.domain.Devicetype;
import wy.qingdao_atmosphere.datacenter.domain.Objtype;
import wy.qingdao_atmosphere.datacenter.domain.Param;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfo;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfoadd;
import wy.qingdao_atmosphere.datacenter.service.DataCenterService;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.util.AirBean;
import wy.util.Calc;
import wy.util.DeviceCMD;
import wy.util.FileUtilImpl;
import wy.util.GetDeviceInfo;
import wy.util.GetIPAddress;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.util.datapersistence.service.BaseService;


/**
 * 
 * @author hero
 *
 */
@SuppressWarnings("all")
@Controller
public class DataCenterController {
	private DataCenterService dataCenterService;
	
	public DataCenterService getDataCenterService() {
		return dataCenterService;
	}

	@Resource
	public void setDataCenterService(DataCenterService dataCenterService) {
		this.dataCenterService = dataCenterService;
	}
	
	@Autowired
	private BaseaddDao baseaddDao;
	
	@Autowired
	private FileUtilImpl fileUtilImpl;

	@Autowired 
	private BaseService baseService;
	
	@Autowired
	private DataCenterDao dataCenterDao;

	//1.获取事务控制管理器
	@Autowired
    DataSourceTransactionManager transactionManager;
    //2.获取事务定义
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    
    
    
	/**
	 * 参数管理：列表查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/parameterManage.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Param> parameterManage(HttpServletRequest request){
		Logger.getLogger("").info("------------参数管理-参数信息列表--");
		String dataname = (request.getParameter("dataname") == null || request.getParameter("dataname") == "")?"":request.getParameter("dataname");
		String isused = (request.getParameter("isused") == null || request.getParameter("isused") == "")?"":request.getParameter("isused");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("dataname", dataname);
		map.put("isused", isused);
		return dataCenterService.getAllparameter(map);
	}
	
	/**
	 * 参数管理：增加
	 * @param request
	 */
	@RequestMapping(value = "/addParam.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> addParam(Param param){
		Logger.getLogger("").info("------------参数管理-参数信息  增加--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("paramname", param.getParamname());
			List<Param> list = dataCenterService.getAllparameter(map);
			if(list.size()>0){
				returnMap.put("message", "该参数名称已存在，请更改！");
				returnMap.put("state", false);
			}else{
				dataCenterService.addparam(param);
				returnMap.put("message", "添加成功！");
				returnMap.put("state", true);
				//日志存储
				baseaddDao.addUserOperateLog("一般操作","参数管理", "参数信息增加->增加的参数为："+param.getParamname(), returnMap.get("message").toString(),returnMap.get("state").toString(), "", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加异常！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","参数管理", "参数信息增加->增加的参数为："+param.getParamname(), returnMap.get("message").toString(),returnMap.get("state").toString(), e.getMessage(), "");
		}
			
		return returnMap;
	}
	
	/**
	 * 参数管理：修改
	 * @param param
	 */
	@RequestMapping(value = "/updateParam.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> updateParam(Param param){
		Logger.getLogger("").info("------------参数管理-参数信息列表  修改--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String,Object> map1 = new HashMap<String, Object>();
			map1.put("paramname", param.getParamname());
			List<Param> list1 = dataCenterService.getAllparameter(map1);
			if(list1.size()>0&&(list1.get(0).getParamid()!=param.getParamid())){
				returnMap.put("message", "该参数名称已存在，请更改！");
				returnMap.put("state", false);
			}else{
				dataCenterService.updateParam(param);
				returnMap.put("message", "修改成功！");
				returnMap.put("state", true);			
				//日志存储
				baseaddDao.addUserOperateLog("一般操作","参数管理", "参数信息修改->修改的参数id为："+param.getParamid(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","参数管理", "参数信息修改->修改的参数id为："+param.getParamid(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	/**
	 * 参数管理 删除
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> delParam(HttpServletRequest request){
		Logger.getLogger("").info("------------参数管理-参数信息列表  删除或恢复--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String paramid = (request.getParameter("paramid") == null || request.getParameter("paramid") == "")?"":request.getParameter("paramid");
		String isused = (request.getParameter("isused") == null || request.getParameter("isused") == "")?"":request.getParameter("isused");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("isused", isused);
		map.put("paramid", paramid);
		try {
			dataCenterService.delparam(map);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","参数管理", "参数信息删除/恢复-> 删除/恢复的参数id为："+paramid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			returnMap.put("message", "操作异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","参数管理", "参数信息删除/恢复-> 删除/恢复的参数id为："+paramid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	//==============================对象类型
	@RequestMapping(value = "/queryObjtype.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Objtype> queryObjtype(HttpServletRequest request){
		Logger.getLogger("").info("------------对象类型管理  查询--");
		String objtypename = (request.getParameter("objtypename") == null || request.getParameter("objtypename") == "")?"":request.getParameter("objtypename");
		String isused = (request.getParameter("isused") == null || request.getParameter("isused") == "")?"":request.getParameter("isused");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("objtypename", objtypename);
		map.put("isused", isused);
		List<Objtype> list = dataCenterService.queryObjtype(map);
		return list;
	}
	/**
	 * 对象类型：增加
	 * @param request
	 */
	@RequestMapping(value = "/addObjtype.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> addObjtype(Objtype objtype){
		Logger.getLogger("").info("------------对象类型管理  增加--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("objtypename", objtype.getObjtypename());
			List<Objtype> list = dataCenterService.queryObjtype(map);
			if(list.size()>0){
				returnMap.put("message", "该参数名称已存在，请更改！");
				returnMap.put("state", false);
			}else{
				objtype.setSpacelayername("space_"+objtype.getSpacelayername());
				dataCenterService.addObjtype(objtype);
				returnMap.put("message", "添加成功！");
				returnMap.put("state", true);
				//日志存储
				baseaddDao.addUserOperateLog("一般操作","对象类型管理", "对象类型新增->新增的对象类型为："+objtype.getObjtypename(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			}
		} catch (BadSqlGrammarException e) {
			returnMap.put("message", "数据库已经存在该表名！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象类型管理", "对象类型新增->新增的对象类型为："+objtype.getObjtypename(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象类型管理", "对象类型新增->新增的对象类型为："+objtype.getObjtypename(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	/**
	 * 对象类型管理 删除或恢复
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delObjtype.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> delObjtype(HttpServletRequest request){
		Logger.getLogger("").info("------------对象类型管理  删除或恢复--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String objtypeid = (request.getParameter("objtypeid") == null || request.getParameter("objtypeid") == "")?"":request.getParameter("objtypeid");
		String isused = (request.getParameter("isused") == null || request.getParameter("isused") == "")?"":request.getParameter("isused");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("isused", isused);
		map.put("objtypeid", objtypeid);
		try {
			dataCenterService.delObjtype(map);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象类型管理", "对象类型删除/恢复->删除/恢复的对象类型id为："+objtypeid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			returnMap.put("message", "操作异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象类型管理", "对象类型删除/恢复->删除/恢复的对象类型id为："+objtypeid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	/**
	 * 对象类型管理：修改
	 * @param param
	 */
	@RequestMapping(value = "/updateObjtype.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> updateObjtype(Objtype objtype){
		Logger.getLogger("").info("------------对象类型管理管理  修改--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String,Object> map1 = new HashMap<String, Object>();
			map1.put("isobjtypename", objtype.getObjtypename());
			List<Objtype> list1 = dataCenterService.queryObjtype(map1);
			if(list1.size()>0&&(list1.get(0).getObjtypeid()!=objtype.getObjtypeid())){
				returnMap.put("message", "该参数名称已存在，请更改！");
				returnMap.put("state", false);
			}else{
				dataCenterService.updateObjtype(objtype);
				returnMap.put("message", "修改成功！");
				returnMap.put("state", true);
				//日志存储
				baseaddDao.addUserOperateLog("一般操作","对象类型管理", "对象类型信息修改->修改的对象类型id为："+objtype.getObjtypeid(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象类型管理", "对象类型信息修改->修改的对象类型id为："+objtype.getObjtypeid(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 对象类型   对象安装设备  列表查询  by 对象类型
	 * @return
	 */
	@RequestMapping(value = "/queryObjInfo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryObjInfo(HttpServletRequest request){
		// TODO: 查询
		Logger.getLogger("").info("------------对象安装设备  列表查询 --");
		String objtypeid = (request.getParameter("objtypeid") == null)?"":request.getParameter("objtypeid");
		String objid = (request.getParameter("objid") == null)?"":request.getParameter("objid");
		String isxlbyobj = (request.getParameter("isxlbyobj") == null)?"":request.getParameter("isxlbyobj");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", objtypeid);
		map.put("objid", objid);
		map.put("isxlbyobj", isxlbyobj);
		List<Map<String, Object>> list = dataCenterService.queryObjDevice(map);
		return list;
	}
	
	/**
	 * 对象类型   对象安装设备  添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addObjinfo.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addObjinfo(HttpServletRequest request){
		Logger.getLogger("").info("------------对象安装设备  添加 --");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		String objtypeid = request.getParameter("objtypeid") == null?"":request.getParameter("objtypeid");
		String devicenumber = request.getParameter("devicenumber") == null?"":request.getParameter("devicenumber");
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("devicenumber", devicenumber);
			map.put("objid", objid);
			map.put("objtypeid", objtypeid);
			dataCenterService.addObjinfo(map);
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象安装设备", "对象安装设备->设备id为："+devicenumber+",对象为："+objid, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象安装设备", "对象安装设备->设备id为："+devicenumber+",对象为："+objid, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}return returnMap;
	}
	
	/**
	 * 对象安装设备  安装页面   设备类型查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/querydeviceType.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Object> querydeviceType(HttpServletRequest request){
		List<Object> resultList = new ArrayList<Object>();
		String objtypeid = (request.getParameter("objtypeid") == null)?"":request.getParameter("objtypeid");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", objtypeid);
		map.put("isxlbyobj", "1");
		//查询对象类型下是否有设备
		List<Map<String, Object>> devlist = dataCenterService.queryObjDevice(map);
		if(devlist.size()==0){
			String isused = request.getParameter("isused") == null?"":request.getParameter("isused");
			Map<String, Object> dmap = new HashMap<String, Object>();
			dmap.put("isused", "1");
			List<Devicetype> devTypelist = dataCenterService.queryDevicetypes(dmap);
			resultList.addAll(devTypelist);
		}else{
			HashSet<Map<String,Object>> h = new HashSet<Map<String,Object>>();
			for(Map<String, Object> m:devlist){
				Map<String,Object> hmap = new HashMap<String, Object>();
				hmap.put("devicetypeid", m.get("devicetypeid"));
				hmap.put("devicetypename", m.get("devicetypename"));
				h.add(hmap);
			}
			devlist.clear();
			devlist.addAll(h);
			resultList.addAll(devlist);
		}
		return resultList;
	}
	
	
	/**
	 * 对象类型 对象安装设备  安装页面 设备列表查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/querydeviceName.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> querydeviceName(HttpServletRequest request){
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid");
//		String isBatch = request.getParameter("isBatch") == null?"":request.getParameter("isBatch");
		//根据设备类型  查   设备
		Map<String, Object> dmap = new HashMap<String, Object>();
		dmap.put("devicetypeid", devicetypeid);
		return dataCenterService.querydeviceName(dmap);
	}
	/**
	 * 对象类型 对象安装设备 卸载
	 * @return
	 */
	@RequestMapping(value = "/delObjinfo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delObjinfo(HttpServletRequest request){
		Logger.getLogger("").info("------------对象安装设备  卸载 --");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String data = request.getParameter("data") == null?"":request.getParameter("data");
		try {
			JSONArray json = JSONArray.fromObject(data);
			for(int i=0;i<json.size();i++){
				String objid = json.getJSONObject(i).getString("objid");
				String devicenumber = json.getJSONObject(i).getString("devicenumber");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("objid", objid);
				map.put("devicenumber", devicenumber);
				map.put("isused", 0);
				dataCenterService.delObjinfo(map);
			}
			returnMap.put("message", "卸载成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象安装设备", "对象卸载设备->对象为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "卸载失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象安装设备", "对象卸载设备->对象为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	
	//=====================================设备类型管理
	/**
	 * 设备类型管理 查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDevicetypes.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Devicetype> queryDevicetypes(HttpServletRequest request){
		Logger.getLogger("").info("------------设备类型管理  查询--");
		String dataname = request.getParameter("dataname") == null?"":request.getParameter("dataname");
		String isused = request.getParameter("isused") == null?"":request.getParameter("isused");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dataname", dataname);
		map.put("isused", isused);
		List<Devicetype> list = dataCenterService.queryDevicetypes(map);
		return list;
	}
	
	/**
	 * 设备类型管理 添加
	 * @param devicetype
	 * @return
	 */
	@RequestMapping(value = "/addDevicetype.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addDevicetype(Devicetype devicetype){
		Logger.getLogger("").info("------------设备类型管理  添加--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			dataCenterService.addDevicetype(devicetype);
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备类型管理", "设备类型添加->添加的设备类型为："+devicetype.getDevicetypename(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备类型管理", "设备类型添加->添加的设备类型为："+devicetype.getDevicetypename(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	/**
	 * 设备类型管理  修改
	 * @param devicetype
	 * @return
	 */
	@RequestMapping(value = "/updateDevicetype.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateDevicetype(Devicetype devicetype){
		Logger.getLogger("").info("------------设备类型管理  修改--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			dataCenterService.updateDevicetype(devicetype);
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备类型管理", "设备类型信息修改->修改的设备类型id为："+devicetype.getDevicetypeid(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备类型管理", "设备类型信息修改->修改的设备类型id为："+devicetype.getDevicetypeid(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 设备类型管理  删除或恢复
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delDevicetye.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delDevicetye(HttpServletRequest request){
		Logger.getLogger("").info("------------设备类型管理  删除或恢复--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String devicetypeid = (request.getParameter("devicetypeid") == null || request.getParameter("devicetypeid") == "")?"":request.getParameter("devicetypeid");
		String isused = (request.getParameter("isused") == null || request.getParameter("isused") == "")?"":request.getParameter("isused");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("isused", isused);
		map.put("devicetypeid", devicetypeid);
		try {
			dataCenterService.delDeviceype(map);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备类型管理", "设备类型删除/恢复->删除/恢复的设备类型id为："+devicetypeid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			returnMap.put("message", "操作异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备类型管理", "设备类型删除/恢复->删除/恢复的设备类型id为："+devicetypeid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 设备类型管理   版本添加
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/fileupload.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> fileupload(HttpServletRequest request,HttpServletResponse response){
		Logger.getLogger("").info("设备类型管理 设备版本添加");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			dataCenterService.fileupload(request, response);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "操作失败！"+e.getMessage());
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	@RequestMapping(value = "/fileup.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> fileup(HttpServletRequest request){
		Map<String, Object> map =new HashMap<String, Object>();
		String filename = fileUtilImpl.attachUpload(request, "deviceVersion");
		map.put("files", filename.substring(filename.indexOf("deviceVersion/")+"deviceVersion/".length(), filename.length()));
		return map;
	}
	
	/**
	 * 版本删除
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delDevVersion.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delDevVersion(HttpServletRequest request){
		Logger.getLogger("").info("设备类型管理 设备版本删除");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String verid = request.getParameter("verid") == null?"":request.getParameter("verid");
		try {
			dataCenterService.delDevVersion(verid);
			returnMap.put("message", "删除成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "删除失败！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	/**
	 * 设备类型管理 版本查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/querydevVersion.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> querydevVersion(HttpServletRequest request){
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("devicetypeid", devicetypeid);
		return dataCenterService.queryDevVersion(map);
	}
	

	//=========================================设备管理
	/**
	 * 设备管理   列表查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDevice.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDevice(HttpServletRequest request){
		Logger.getLogger("").info("设备管理  列表查询");
		Map<String,Object> map = new HashMap<String, Object>();
		String devicetypename = (request.getParameter("devicetypename") == null || request.getParameter("devicetypename") == "")?"":request.getParameter("devicetypename");
		String devicename = (request.getParameter("devicename") == null || request.getParameter("devicename") == "")?"":request.getParameter("devicename");
		String isused = (request.getParameter("isused") == null || request.getParameter("isused") == "")?"":request.getParameter("isused");
		map.put("devicetypename", devicetypename);
		map.put("devicename", devicename);
		map.put("isused", isused);
		List<Map<String, Object>> list = dataCenterService.queryDevice(map);
		for(Map<String, Object> device:list){
			//查询设备在线状态
			String key = device.get("rmk1")==null?"":device.get("rmk1").toString();
	        String devIds= device.get("devicenumber")==null?"":device.get("devicenumber").toString();
//	        String 
	        if(key.equals("YA2pIfTi0aVAn=nQc9Hc2Ywg8BM=")){
	        	/**
		    	 * 批量查询设备状态
		         * 参数顺序与构造函数顺序一致
		    	 * @param devIds:设备id用逗号隔开, 限制1000个设备
		    	 * @param key :masterkey 或者 设备apikey,String
		    	 */
		        GetDevicesStatus api = new GetDevicesStatus(devIds,key);
		        BasicResponse<DevicesStatusList> response = api.executeApi();
//		        System.out.println(""errno:"+response.errno+" error:"+response.error);
		        if(response.error.equals("succ")){
		        	boolean online = response.data.getDevices().get(0).getIsonline();
			        device.put("online", online);
				}else{
					device.put("online", false);
				}
	        }
		}
		return list;
	}
	/**
	 * 设备管理   列表查询---设备类型查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDevicetypeMap.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDevicetypeMap(HttpServletRequest request){
		return dataCenterService.queryDevicetypeMap();
	}
	/**
	 * 设备管理 设备名称查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDeviceNameMap.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDeviceNameMap(HttpServletRequest request){
		int devicetypeid = request.getParameter("devicetypeid") == null?-1:(Integer.parseInt(request.getParameter("devicetypeid").toString()));
		return dataCenterService.queryDeviceNameMap(devicetypeid);
	}
	
	/**
	 * 设备管理 添加
	 * @param request
	 */
	@RequestMapping(value = "/addDevice.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> addDevice(HttpServletRequest request){
		Logger.getLogger("").info("------------设备管理  添加--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid").toString();
		String devicename = request.getParameter("devicename") == null?"":request.getParameter("devicename").toString();
		String isentity = request.getParameter("isentity") == null?"":request.getParameter("isentity").toString();
		//OneNET产品key
		String key = request.getParameter("key") == null?"":request.getParameter("key").toString();
		
		if(("").equals(key)){
			Map<String,Object> bdmap = new HashMap<String, Object>();
			bdmap.put("devicetypeid", devicetypeid);
			bdmap.put("devicename", devicename);
			bdmap.put("isentity", isentity);
			bdmap.put("key", null);
			bdmap.put("position", null);
			//随机生成devicenumber
			int devicenumber = 0;
			while (true) {
				devicenumber = (int)((Math.random()*9+1)*1000000);
				Map<String, Object> devMap = new HashMap<String, Object>();
				devMap.put("devicenumber", devicenumber+"");
				//检查设备编号唯一性
				List<Map<String, Object>> devNumberList = dataCenterService.queryDevice(devMap);
				if(devNumberList.size()==0){
					break;
				}
			}
			bdmap.put("devicenumber", devicenumber+"");
			dataCenterService.addDevice(bdmap);//添加不绑定OneNet的设备
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
			//日志
			baseaddDao.addUserOperateLog("一般操作","设备管理", "设备添加->添加的设备类型id为："+devicetypeid+",设备名称为："+devicename, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		}else{
			//同步添加oneNet
			//接入协议
			String protocol = request.getParameter("protocol") == null?"":request.getParameter("protocol").toString();
			//鉴权信息
			String auth_info = request.getParameter("position") == null?"":request.getParameter("position").toString();
			try {
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("position", auth_info);
				map.put("devicetypeid", devicetypeid);
				//检查鉴权信息唯一性
				List<Map<String, Object>> positionList = dataCenterService.queryDevice(map);
				if(positionList.size()>0){
					returnMap.put("message", "该产品下鉴权信息已存在！");
					returnMap.put("state", false);
				}else{
					map.put("devicetypeid", devicetypeid);
					map.put("devicename", devicename);
					map.put("isentity", isentity);
					map.put("key", key);
						/****
						 * 设备新增
						 * 参数顺序与构造函数顺序一致
						 * @param title： 设备名，String
						 * @param protocol： 接入协议（可选，默认为HTTP）,String
						 * @param desc： 设备描述（可选）,String
						 * @param tags： 设备标签（可选，可为一个或多个）,List<String>
						 * @param location： 设备位置{"纬度", "精度", "高度"}（可选）,Location类型
						 * @param isPrivate： 设备私密性,Boolean类型（可选，默认为ture）
						 * @param authInfo： 设备唯一编号 ,Object
						 * @param other： 其他信息,Map<String, Object>（可选，可自定义）
						 * @param interval： MODBUS设备 下发命令周期,Integer类型，秒（可选）
						 * @param key： masterkey,String
						 */
						List<String> tags = new ArrayList<String>();  
						tags.add("数据中心");
						boolean isPrivate = true;
//						String auth_info = UUID.randomUUID().toString();
						AddDevicesApi api = new AddDevicesApi(devicename, protocol, null, tags, null, isPrivate, auth_info, null, null, key);
						BasicResponse<NewDeviceResponse> response = api.executeApi();
						System.out.println("errno:"+response.errno+" error:"+response.error);
						System.out.println(response.getJson());
						if(response.error.contains("succ")){
							map.put("devicenumber", response.data.DeviceId);
							Map<String, Object> devMap = new HashMap<String, Object>();
							devMap.put("devicenumber", response.data.DeviceId);
							//检查设备编号唯一性
							List<Map<String, Object>> devNumberList = dataCenterService.queryDevice(devMap);
							if(devNumberList.size()>0){
								returnMap.put("message", "OneNet自动生成设备编号已存在！请重新创建。");
								returnMap.put("state", false);
								DeleteDeviceApi delapi = new DeleteDeviceApi(response.data.DeviceId, key);//删除OneNet上的设备
								BasicResponse<Void> delresponse = delapi.executeApi();
							}else{
								try{
									dataCenterService.addDevice(map);//后台添加设备
									//查询设备类型是否绑定参数
									Map<String, Object> paramMap = new HashMap<String, Object>();
									paramMap.put("devicetypeid", devicetypeid);
									List<Map<String, Object>> list = dataCenterService.queryDeviceParam(paramMap);
									for(Map<String, Object> paramMap2:list){
										String id = paramMap2.get("paramname").toString();
										String devId = response.data.DeviceId;
										String unit = paramMap2.get("dataunit").toString();
										String unitSymbol = paramMap2.get("dataunit").toString();
										/**
										 * OneNet数据流新增
										 * @param id：数据流名称 ，String
										 * @param devId:设备ID,String
										 * @param tags:数据流标签（可选，可以为一个或多个）,List<Stirng>
										 * @param unit:单位（可选）,String
										 * @param unitSymbol:单位符号（可选）,String
										 * @param cmd:MODBUS设备填写，周期下发命令，16进制字节字符串
										 * @param interval:MODBUS设备填写，采集间隔，秒,Integer
										 * @param formula:MODBUS设备填写，寄存器计算公式（可选）,String
										 * @param key:masterkey 或者 设备apikey
										 */
										AddDatastreamsApi addapi = new AddDatastreamsApi(id, devId, tags, unit, unitSymbol, null, null, null, key);
										BasicResponse<NewdatastramsResponse> addresponse = addapi.executeApi();
										System.out.println("绑定数据流"+id+"errno:"+addresponse.errno+" error:"+addresponse.error);
									}
									returnMap.put("message", "添加成功！");
									returnMap.put("state", true);
									//日志存储
									baseaddDao.addUserOperateLog("一般操作","设备管理", "设备添加->添加的设备类型id为："+devicetypeid+",设备名称为："+devicename, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
								}catch(Exception e){
									e.printStackTrace();
									returnMap.put("message", "数据中心添加失败！");
									returnMap.put("state", false);
									DeleteDeviceApi api1 = new DeleteDeviceApi(response.data.DeviceId, key);
									BasicResponse<Void> response1 = api1.executeApi();
									System.out.println("设备管理"+"errno:"+response1.errno+" error:"+response1.error);
									//日志存储
									baseaddDao.addUserOperateLog("异常","设备管理", "设备添加->添加的设备类型id为："+devicetypeid+",设备名称为："+devicename, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
								}
							}
						}else{
							returnMap.put("message", "OneNE平台添加失败！"+response.error);
							returnMap.put("state", false);
						}
				}
			} catch (Exception e) {
				e.printStackTrace();
				returnMap.put("message", "添加失败！");
				returnMap.put("state", false);
				//日志存储
				baseaddDao.addUserOperateLog("异常","设备管理", "设备添加->添加的设备类型id为："+devicetypeid+",设备名称为："+devicename, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
			}
		}
		return returnMap;
	}
	
	/**
	 * 设备管理 修改
	 * @param request
	 */
	@RequestMapping(value = "/updateDevice.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> updateDevice(HttpServletRequest request){
		Logger.getLogger("").info("------------设备管理  修改--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		//3.设置事务隔离级别，开启新事务
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        //4.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid").toString();
		String devicename = request.getParameter("devicename") == null?"":request.getParameter("devicename").toString();
		String isentity = request.getParameter("isentity") == null?"":request.getParameter("isentity").toString();
		String deviceid = request.getParameter("deviceid") == null?"":request.getParameter("deviceid").toString();
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("devicetypeid", devicetypeid);
			map.put("devicename", devicename);
			map.put("isentity", isentity);
			map.put("deviceid", deviceid);
			int a = dataCenterService.updateDevice(map);
			if(a==1){
				Map<String,Object> deviceMap = dataCenterService.queryDevice(map).get(0);
				String id = deviceMap.get("devicenumber").toString();
				String title = devicename;
				String key = deviceMap.get("rmk1").toString();
				
				/***
				 * 设备更新
				 * 参数顺序与构造函数顺序一致
				 * @param id： 设备ID,String
				 * @param title： 设备名，String
				 * @param protocol： 接入协议（可选，默认为HTTP），String
				 * @param desc： 设备描述（可选），String
				 * @param tags： 设备标签（可选，可为一个或多个），List<String>
				 * @param location： 设备位置{"纬度", "精度", "高度"}（可选）,Location类型
				 * @param isPrivate： 设备私密性，Boolean类型
				 * @param authInfo： 设备唯一编号 ，Object
				 * @param other：  其他信息，Map<String, Object>
				 * @param interval： MODBUS设备 下发命令周期,Integer类型
				 * @param key ：masterkey 或者 设备apikey,String
				 */
				if(!key.equals("")){
					ModifyDevicesApi api = new ModifyDevicesApi(id, title, null, null, null,null,null,null, null, null,key);
					BasicResponse<Void> response = api.executeApi();
//					System.out.println("OneNET设备修改："+"errno:"+response.errno+" error:"+response.error);
//					if(!response.error.equals("succ")){
//						throw new Exception();
//					}
				}
			}
			transactionManager.commit(status);
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备管理", "设备信息修改->修改的设备类型id为："+devicetypeid+",设备id为："+deviceid, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			transactionManager.rollback(status);
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备管理", "设备信息修改->修改的设备类型id为："+devicetypeid+",设备id为："+deviceid, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * OneNet 设备的数据流同步
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/oneNetsynchro.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> oneNetsynchro(HttpServletRequest request){
		Logger.getLogger("").info("------------设备的数据流同步--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			String deviceid = request.getParameter("deviceid") == null?"":request.getParameter("deviceid").toString();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("deviceid", deviceid);
			Map<String, Object> deviceMap = dataCenterService.queryDevice(map).get(0);
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("devicetypeid", deviceMap.get("devicetypeid"));
			String key = deviceMap.get("rmk1").toString();
			List<Map<String, Object>> list = dataCenterService.queryDeviceParam(paramMap);
			boolean iserror = false;
			String error = "";
			for(Map<String, Object> paramMap2:list){
				String id = paramMap2.get("paramname").toString();
				String devId = deviceMap.get("devicenumber").toString();
				String unit = paramMap2.get("dataunit").toString();
				String unitSymbol = paramMap2.get("dataunit").toString();
				/**
				 * 数据流新增
				 * @param id：数据流名称 ，String
				 * @param devId:设备ID,String
				 * @param tags:数据流标签（可选，可以为一个或多个）,List<Stirng>
				 * @param unit:单位（可选）,String
				 * @param unitSymbol:单位符号（可选）,String
				 * @param cmd:MODBUS设备填写，周期下发命令，16进制字节字符串
				 * @param interval:MODBUS设备填写，采集间隔，秒,Integer
				 * @param formula:MODBUS设备填写，寄存器计算公式（可选）,String
				 * @param key:masterkey 或者 设备apikey
				 */
				AddDatastreamsApi addapi = new AddDatastreamsApi(id, devId, null, unit, unitSymbol, null, null, null, key);
				BasicResponse<NewdatastramsResponse> addresponse = addapi.executeApi();
				System.out.println("同步绑定数据流"+id+"errno:"+addresponse.errno+" error:"+addresponse.error);
				if(!addresponse.error.equals("succ")&&addresponse.errno!=11){
					iserror = true;
					error = addresponse.error;
				}
			}
			if(iserror){
				returnMap.put("message", error);
				returnMap.put("state", false);
			}else{
				returnMap.put("message", "同步成功！");
				returnMap.put("state", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "同步失败！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	/**
	 * CMD指令下发
	 *   cmdText
	 *   deviceid
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/insertCMD.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> insertCMD(HttpServletRequest request){
		Logger.getLogger("").info("------------OneNET cmd指令下发--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		//设备主键id
		String deviceid = request.getParameter("deviceid") == null?"":request.getParameter("deviceid").toString();
		String cmdType = request.getParameter("cmdType") == null?"":request.getParameter("cmdType").toString();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("deviceid", deviceid);
			Map<String, Object> deviceMap = dataCenterService.queryDevice(map).get(0);
			String devId = deviceMap.get("devicenumber").toString();//设备id
			String key = deviceMap.get("rmk1").toString();//设备key
			Random r = new Random();
			int random = r.nextInt(60000);//随机数
			System.out.println("命令序列号："+random);
			DeviceCMD deviceCMD = new DeviceCMD();
			if(("CMC_S_TimeRcv").equals(cmdType)){
				deviceCMD.CMC_S_TimeRcv(random, devId, key);
			}
			if(("CMC_S_FileRcv").equals(cmdType)){
				//文件地址
				String FileName = request.getParameter("filename") == null?"":request.getParameter("filename").toString();
				String version = request.getParameter("version") == null?"":request.getParameter("version").toString();
				if(version.equals("")){
					version = request.getParameter("ftpFileName") == null?"":request.getParameter("ftpFileName").toString();
				}
				String path = DataCenterService.class.getResource("DataCenterService.class").toString();
		        path = path.substring(6, path.indexOf("WEB-INF"));
		        File file = new File(path+ "upload/deviceVersion/"+FileName);
		        if(!file .exists()&&!file.isDirectory())   
				{
		    		throw new Exception("文件不存在！");
				}
				List<byte[]> bytelist = new ArrayList<byte[]>();//包集合
        		bytelist = dataCenterService.byteList(path+ "upload/deviceVersion/"+FileName);
				deviceCMD.CMC_S_FileRcv(random, devId, key,bytelist);
				Map<String, Object> updateMap = new HashMap<String, Object>();
				updateMap.put("deviceid", deviceid);
				updateMap.put("using", version);
				try {
					dataCenterService.updateDevice(updateMap);
				} catch (Exception e) {
					throw new Exception("指令发送完成，记录版本失败！");
				}
				
			}
			if(("CMC_S_SetParms").equals(cmdType)){
//				String zdh = request.getParameter("zdh") == null?"":request.getParameter("zdh").toString();
				String zdh = deviceMap.get("position")==null?"":deviceMap.get("position").toString();
				
				String timejg = request.getParameter("timejg") == null?"":request.getParameter("timejg").toString();
				String cpid = request.getParameter("cpid") == null?"":request.getParameter("cpid").toString();
				String jiaoben = request.getParameter("jiaoben") == null?"":request.getParameter("jiaoben").toString();
				String ip = request.getParameter("ip") == null?"":request.getParameter("ip").toString();
				String port = request.getParameter("port") == null?"":request.getParameter("port").toString();
				/**
				 * 
				 * @param random  随机数序列号
				 * @param dev_id  设备id
				 * @param key     产品key
				 * @param zdh	    站点号
				 * @param timejg  时间间隔 int
				 * @param cpid	    产品id  int
				 * @param jiaoben 脚本名称
				 * @param ip      ip地址
				 * @param port    端口号     int
				 */
				deviceCMD.CMC_S_SetParms(random, devId, key,zdh,Integer.parseInt(timejg),Integer.parseInt(cpid),jiaoben,ip,Integer.parseInt(port));
			}
			if(("CMC_S_Restart").equals(cmdType)){
				deviceCMD.CMC_S_Restart(random, devId, key);
			}
			if(("CMC_S_DataCheck").equals(cmdType)){
				//传感器类型
				String cgqType = request.getParameter("cgqType") == null?"":request.getParameter("cgqType").toString();
				//参数类型
				String csType = request.getParameter("csType") == null?"":request.getParameter("csType").toString();
				String val = request.getParameter("val") == null?"":request.getParameter("val").toString();
				deviceCMD.CMC_S_DataCheck(random, devId, key, Integer.parseInt(cgqType), Integer.parseInt(csType), Float.parseFloat(val));
			}
			returnMap.put("message", "发送成功！");
			returnMap.put("state", true);
			try {
				baseaddDao.addUserOperateLog("一般操作","OneNET", "OneNET->cmd指令下发,指令类型为："+cmdType+",指令序列号为："+random+",设备id为："+deviceid, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			} catch (Exception e) {
				throw new Exception("指令发送完成，日志记录失败！");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			returnMap.put("message", "发送失败！"+e.getMessage());
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","OneNET", "OneNET->cmd指令下发,指令类型为："+cmdType+",设备id为："+deviceid, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 设备管理 删除或恢复
	 * @param request
	 */
	@RequestMapping(value = "/delDevice.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> delDevice(HttpServletRequest request){
		Logger.getLogger("").info("------------设备管理  删除 或 恢复--");
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String isused = request.getParameter("isused") == null?"":request.getParameter("isused").toString();
		String deviceid = request.getParameter("deviceid") == null?"":request.getParameter("deviceid").toString();
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("isused", isused);
			map.put("deviceid", deviceid);
			dataCenterService.delDevice(map);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备管理", "设备删除/恢复->删除/恢复的设备id为："+deviceid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "操作失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备管理", "设备删除/恢复->删除/恢复的设备id为："+deviceid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 设备参数管理 列表查询
	 * @param request
	 */
	@RequestMapping(value = "/queryDeviceTypeParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	 public List<Map<String, Object>> queryDeviceTypeParam(HttpServletRequest request){
		Logger.getLogger("").info("------------设备参数管理   列表查询--");
		//设备类型id
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid").toString();
		//参数名称
		String paramname = request.getParameter("paramname") == null?"":request.getParameter("paramname").toString();
		String isused = request.getParameter("isused") == null?"":request.getParameter("isused").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("devicetypeid", devicetypeid);
		map.put("paramname", paramname);
		map.put("isused", isused);
		List<Map<String, Object>> list = dataCenterService.queryDeviceParam(map);
		return list;
	 }
	
	/**
	 * 设备参数管理 添加-参数查询
	 * @return
	 */
	@RequestMapping(value = "/queryParambydeviceType.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryParambydeviceType(HttpServletRequest request){
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("devicetypeid", devicetypeid);
		return dataCenterService.queryParambydeviceType(map);
	}
	/**
	 * 设备参数管理 添加
	 * @return
	 */
	@RequestMapping(value = "/addDeviceTypeParam.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addDeviceTypeParam(String paramids,int devicetypeid){
		Logger.getLogger("").info("------------设备参数管理   添加--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String[] array = (paramids==null?"":paramids).split(",");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("paramids", array);
			map.put("devicetypeid", devicetypeid);
			dataCenterService.addDeviceTypeParam(map);
			
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备参数管理", "设备参数添加->设备类型id为："+devicetypeid+",参数id为："+paramids, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备参数管理", "设备参数添加->设备类型id为："+devicetypeid+",参数id为："+paramids, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 设备参数管理   修改页  查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDP.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDP(int devicetypeid){
		Logger.getLogger("").info("------------设备参数管理   修改页查询--");
		return dataCenterService.queryDP(devicetypeid);
	}
	/**
	 * 设备参数管理   修改
	 * @param request / devicetypeid  、paramids
	 * @return
	 */
	@RequestMapping(value = "/updateDeviceTypeParam.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateDeviceTypeParam(HttpServletRequest request){
		Logger.getLogger("").info("------------设备参数管理   修改--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid").toString();
		String paramids = request.getParameter("paramids") == null?"":request.getParameter("paramids").toString();
		try {
			String[] array = paramids.split(",");
			List<Map<String, Object>> rootList = dataCenterService.queryDP(Integer.parseInt(devicetypeid));
			List<Map<String, Object>> changeList = new ArrayList<Map<String,Object>>();
			for(int i=0;i<rootList.size();i++){
				Map<String, Object> map = rootList.get(i);
				for(String param:array){
					if((map.get("paramid").toString()).equals(param)){
						map.put("state", Integer.parseInt(map.get("state").toString())+3);
						break;
					}
				}
				changeList.add(map);
			}
			dataCenterService.updateDP(changeList,devicetypeid);
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备参数管理", "设备参数修改->设备类型id为："+devicetypeid+",参数id为："+paramids, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备参数管理", "设备参数修改->设备类型id为："+devicetypeid+",参数id为："+paramids, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 设备参数管理  删除或恢复
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delDeviceTypeParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delDeviceTypeParam(HttpServletRequest request){
		Logger.getLogger("").info("------------设备参数管理  删除或恢复--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String paramid = request.getParameter("paramid") == null?"":request.getParameter("paramid").toString();
		String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid").toString();
		String isused = request.getParameter("isused") == null?"":request.getParameter("isused").toString();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("paramid", paramid);
			map.put("devicetypeid", devicetypeid);
			map.put("isused", isused);
			dataCenterService.delDeviceTypeParam(map);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","设备参数管理", "设备参数删除/恢复->关联表类型id-paramid为："+devicetypeid+"-"+paramid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "操作失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","设备参数管理", "设备参数删除/恢复->关联表类型id-paramid为："+devicetypeid+"-"+paramid+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	//==================================对象管理========================
	/**
	 * 对象管理   列表查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryObj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryObj(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  列表查询--");
		String objtypeid = request.getParameter("objtypeid") == null?"":request.getParameter("objtypeid").toString();
		String isused = request.getParameter("isused") == null?"":request.getParameter("isused").toString();
		String objname = request.getParameter("objname") == null?"":request.getParameter("objname").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", objtypeid);
		map.put("isused", isused);
		map.put("objname", objname);
		return dataCenterService.queryObj(map);
	}
	/**
	 * 对象管理  基本信息查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryInformation.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> queryInformation(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  基本信息查询--");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
//		List<Map<String,Object>> List = baseService.selectForCpAttachInfoStoreTwo(map, "queryInformation");
		List<Map<String,Object>> list = dataCenterService.queryInformation(map);
		if(list.size()==0){
			map.put("tabletype", "基本表");
			list = dataCenterService.queryFieldshowname(map);
			for(Map<String, Object> m:list){
				m.put("fieldvalue", "");
			}
		}else{
			for(Map<String, Object> m:list){
				if(!m.containsKey("fieldvalue")){
					m.put("fieldvalue", "");
				}
			}
		}
		return list;
	}
	
	/**
	 * 对象管理  子表下拉查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryObjZB.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> queryObjZB(HttpServletRequest request){
		String objtypeid = request.getParameter("objtypeid") == null?"":request.getParameter("objtypeid").toString();
		return dataCenterService.queryObjZB(objtypeid);
	}
	/**
	 * 对象管理  子表信息查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryZBInformation.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> queryZBInformation(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  字表查询--");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		String tableid = request.getParameter("tableid") == null?"":request.getParameter("tableid").toString();
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		map.put("tableid", tableid);
		List<Map<String,Object>> list = dataCenterService.queryZBInformation(map);
		if(list.size()==0){
			map.put("tabletype", "子表");
			list = dataCenterService.queryFieldshowname(map);
			for(Map<String, Object> m:list){
				m.put("fieldvalue", "");
			}
		}else{
			for(Map<String, Object> m:list){
				if(!m.containsKey("fieldvalue")){
					m.put("fieldvalue", "");
				}
			}
		}
		return list;
	}
	/**
	 * 对象管理   对象添加
	 * @return
	 */
	@RequestMapping(value = "/addObj.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addObj(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  对象添加--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String objtypeid = request.getParameter("objtypeid") == null?"":request.getParameter("objtypeid").toString();
		String objname = request.getParameter("objname") == null?"":request.getParameter("objname").toString();
		String shape = request.getParameter("shape") == null?"":request.getParameter("shape").toString();

		try{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objtypeid", objtypeid);
			map.put("objname", objname);
			map.put("shape", shape);
			List<Map<String, Object>> list = dataCenterService.queryObj(map);
			if(list.size()==0){
				dataCenterService.addObj(map);
				returnMap.put("message", "添加成功！");
				returnMap.put("state", true);
				//日志存储
				baseaddDao.addUserOperateLog("一般操作","对象管理", "对象添加->添加的对象名称为："+objname+",对象类型id为："+objtypeid+",空间数据为："+shape, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			}else{
				returnMap.put("message", "添加失败,对象名称已存在");
				returnMap.put("state", false);
			}
		}catch(Exception e){
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象管理", "对象添加->添加的对象名称为："+objname+",对象类型id为："+objtypeid+",空间数据为："+shape, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	//坐标获取 地理位置
	@RequestMapping(value = "/queryobjdt.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryobjdt(HttpServletRequest request){
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		return dataCenterService.queryobjdt(map);
	}
	//修改坐标 地理位置
	@RequestMapping(value = "/updateobjdt.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateobjdt(HttpServletRequest request){
		Logger.getLogger("").info("对象管理");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		String shape = request.getParameter("shape") == null?"":request.getParameter("shape").toString();
		
//		if(!shape.equals("")){
//			String[] shapes = shape.split(",");
//			shape += ","+shapes[0];
//		}
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objid", objid);
			map.put("shape", shape);
			System.out.println("shape++:"+shape);
			dataCenterService.updateobjdt(map);
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	/**
	 * 对象管理  基本信息维护
	 * @return
	 */
	@RequestMapping(value = "/updateInfostoreTwo.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateInfostoreTwo(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  基本信息维护--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String data = request.getParameter("data") == null?"":request.getParameter("data");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			Map<String,Object> insertmap = new HashMap<String, Object>();
			List<Map<String, Object>> fieldList = dataCenterService.queryFieldName(objid);
			JSONObject json = JSONObject.fromObject(data);
			map.put("objid", objid);
			insertmap.put("objid", objid);
			List<Map<String,Object>> list = dataCenterService.queryInformation2(map);
			for(int i=0;i<fieldList.size();i++){
				String fieldname = fieldList.get(i).get("fieldname").toString();
				map.put(fieldname, json.get(fieldname));
				boolean isField = false;
				for(int j=0;j<list.size();j++){
					String listfieldname = list.get(j).get("fieldname").toString();
					if(fieldname.equals(listfieldname)){
						isField = true;
						break;
					}
				}
				if(!isField){
					insertmap.put(fieldname, json.get(fieldname));
				}
			}
			if(list.size()==0){
				//新增
				dataCenterService.insertInfostore(map);
			}else{
				if(list.size()-fieldList.size()==0){
					//修改
					dataCenterService.updateInfostore(map);
				}else{
					dataCenterService.updateInfostore(map);
					dataCenterService.insertInfostore(insertmap);
				}
			}
			String lon = json.getString("lon");  //精度
			
			String lat = json.getString("lat");  //维度
			
			String sitename = json.getString("sitename");  //站点名称
			
			
			//更新obj表的objname和空间表的精度和维度
			
			String shape = lon+" "+lat; //121.0234 29.1236
			
			System.out.println("shape:"+shape);
			map.put("objid", objid);
			System.out.println("objid:"+objid);
			
			Map<String, Object> map2 = dataCenterDao.querySpace(map);
			
			//map.put("spacetypename", map2.get("spacetypename").toString());
			
			//String spaceName = "space_wbstation"; //辐射计   
			
			Map<String,Object> pmap = new HashMap<String,Object>();
			
			pmap.put("shape", shape);
			
			pmap.put("spaceName", map2.get("spacelayername").toString());
			
			pmap.put("sitename", sitename);
			
			pmap.put("objid", objid);
			
			
			baseaddDao.updateObjAndSpace(pmap);
			
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象管理", "对象基本信息维护->对象id为："+objid+",数据为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象管理", "对象基本信息维护->对象id为："+objid+",数据为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		
		return returnMap;
	}
	
	@Autowired
	private SiteDataService  siteService;
	
	/**
	 * 对象管理  基本信息维护
	 * @return
	 */
	@RequestMapping(value = "/updateInfostore.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateInfostore(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  基本信息维护--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String data = request.getParameter("data") == null?"":request.getParameter("data");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		//辐射计 -fsj   风廓线-fkx
		//String devtype = request.getParameter("devtype") == null?"":request.getParameter("devtype");
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			Map<String,Object> insertmap = new HashMap<String, Object>();
			List<Map<String, Object>> fieldList = dataCenterService.queryFieldName(objid);
			JSONObject json = JSONObject.fromObject(data);
			map.put("objid", objid);
			insertmap.put("objid", objid);
			List<Map<String,Object>> list = dataCenterService.queryInformation2(map);
			for(int i=0;i<fieldList.size();i++){
				String fieldname = fieldList.get(i).get("fieldname").toString();
				map.put(fieldname, json.get(fieldname));
				boolean isField = false;
				for(int j=0;j<list.size();j++){
					String listfieldname = list.get(j).get("fieldname").toString();
					if(fieldname.equals(listfieldname)){
						isField = true;
						break;
					}
				}
				if(!isField){
					insertmap.put(fieldname, json.get(fieldname));
				}
			}
			if(list.size()==0){
				//新增
				dataCenterService.insertInfostore(map);
			}else{
				if(list.size()-fieldList.size()==0){
					//修改
					dataCenterService.updateInfostore(map);
				}else{
					dataCenterService.updateInfostore(map);
					dataCenterService.insertInfostore(insertmap);
				}
			}
			String lon = json.getString("lon");  //精度
			
			String lat = json.getString("lat");  //维度
			
			String sitename = json.getString("sitename");  //站点名称
			
			
			//更新obj表的objname和空间表的精度和维度
			
			String shape = lon+" "+lat; //121.0234 29.1236
			
			System.out.println("shape:"+shape);
			map.put("objid", objid);
			System.out.println("objid:"+objid);
			
			Map<String, Object> map2 = dataCenterDao.querySpace(map);
			
			
			
			//map.put("spacetypename", map2.get("spacetypename").toString());
			
			//String spaceName = "space_wbstation"; //辐射计   
			
			Map<String,Object> pmap = new HashMap<String,Object>();
			
			pmap.put("shape", shape);
			
			pmap.put("spaceName", map2.get("spacelayername").toString());
			
			pmap.put("sitename", sitename);
			
			pmap.put("objid", objid);
			
			
			baseaddDao.updateObjAndSpace(pmap);
			
			map.put("objid", objid);
            map.put("isused", 1);
			
			List<Map<String, Object>> objList = dataCenterService.queryObj(map);
			String objtypeid = "-1";
			if(objList!=null&&objList.size()>0){
				objtypeid = objList.get(0).get("objtypeid")==null?"-1": objList.get(0).get("objtypeid").toString();
			}
			System.out.println("objtypeid:"+objtypeid);
			
			Map<String,Object> platform =siteService.queryPlatform(); 
			
			 //如果不是平台端才能进行修改市级目录操作
			if(platform!=null&&"2".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
				System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			//修改目录
			String city = json.get("city").toString();
			map2.clear();
			map2.put("cityname", city);
			int num=0;
			if("12".equals(objtypeid)){ //辐射计
				map2.put("devtype",101); //devtype 这里查的是目录表里市级菜单的上级id
				num = dataCenterService.updateDir(map2);
				//融合图
				map2.put("devtype",301); //devtype 这里查的是目录表里市级菜单的上级id
				num = dataCenterService.updateDir(map2);
			}else if("28".equals(objtypeid)){ //风廓线站
				map2.put("devtype",201);
				num = dataCenterService.updateDir(map2);
			}else{
				map2.put("devtype",-1);  //没有-1,也就是不修改
			}
			
			}
		
			//修改目录
			/*String city = json.get("city").toString();
			map2.clear();
			map2.put("cityname", city);
			devtype = "fsj";
			if("fsj".equals(devtype)){ 
				map2.put("devtype",101); //devtype 这里查的是目录表里市级菜单的上级id
				System.out.println("devtype为fsj");
			}else if("fkx".equals(devtype)){
				map2.put("devtype",201);
			}else{
				map2.put("devtype",-1);  //没有-1,也就是不修改
			}
			int num = dataCenterService.updateDir(map2);*/
			
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象管理", "对象基本信息维护->对象id为："+objid+",数据为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象管理", "对象基本信息维护->对象id为："+objid+",数据为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		
		return returnMap;
	}
	
	
	//子表信息添加或修改
	@RequestMapping(value = "/updateZBInfostore.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateZBInfostore(HttpServletRequest request){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String data = request.getParameter("data") == null?"":request.getParameter("data");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		//子表的id
		String tableid=request.getParameter("tableid") == null?"":request.getParameter("tableid");
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			Map<String,Object> insertmap = new HashMap<String, Object>();
			map.put("objid", objid);
			map.put("tableid", tableid);
			List<Map<String, Object>> fieldList = dataCenterService.queryZBFieldName(map);
			JSONObject json = JSONObject.fromObject(data);
			
			insertmap.put("objid", objid);
			List<Map<String,Object>> list = dataCenterService.queryZBInformation(map);
			for(int i=0;i<fieldList.size();i++){
				String fieldname = fieldList.get(i).get("fieldname").toString();
				map.put(fieldname, json.get(fieldname));
				boolean isField = false;
				for(int j=0;j<list.size();j++){
					String listfieldname = list.get(j).get("fieldname").toString();
					if(fieldname.equals(listfieldname)){
						isField = true;
						break;
					}
				}
				if(!isField){
					insertmap.put(fieldname, json.get(fieldname));
				}
			}
			if(list.size()==0){
				//新增
				dataCenterService.insertZBInfostore(map);
			}else{
				if(list.size()-fieldList.size()==0){
					//修改
					dataCenterService.updateZBInfostore(map);
				}else{
					dataCenterService.updateZBInfostore(map);
					dataCenterService.insertZBInfostore(insertmap);
				}
			}
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象管理", "对象基本信息维护->对象id为："+objid+",数据为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象管理", "对象基本信息维护->对象id为："+objid+",数据为："+data, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	/**
	 * 对象管理  删除或恢复
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delObj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delObj(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  删除或恢复--");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String objids = request.getParameter("objids") == null?"":request.getParameter("objids");
		String isused = request.getParameter("isused") == null?"":request.getParameter("isused");
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objids", objids);
			map.put("isused", isused);
			dataCenterService.delObj(map);
			returnMap.put("message", "操作成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","对象管理", "对象删除/恢复->删除/恢复的对象id为："+objids+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "操作失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","对象管理", "对象删除/恢复->删除/恢复的对象id为："+objids+",操作状态为："+isused, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 查询子表信息
	 * @return
	 */
	@RequestMapping(value = "/queryZbInfo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryZbInfo(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  查询子表信息--");
		Map<String, Object> map = new HashMap<String, Object>();
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		String tableid = request.getParameter("tableid") == null?"":request.getParameter("tableid");
		map.put("objid", objid);
		map.put("tableid", tableid);
		List<Map<String, Object>> list = baseService.selectForCpAttachSubinfoStoreTwo(map,"queryZbInfo");
		return list;
	}
	
	/**
	 * 查询字段的属性
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryFieldshowname.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryFieldshowname(HttpServletRequest request){
		Logger.getLogger("").info("------------对象管理  查询字段属性--");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		String tabletype = request.getParameter("tabletype") == null?"":request.getParameter("tabletype");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		map.put("tabletype", tabletype);
		return dataCenterService.queryFieldshowname(map);
	}
	
	
	/**
	 * 触发器接收
	 * @param request
	 * @throws Exception 
	 */
	@RequestMapping(value = "/receiveData.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public void receiveData(HttpServletRequest request) throws Exception{
		Logger.getLogger("").info("------------触发器接收数据接口--");
		String resultStr = "";
        String readLine;
        StringBuffer sb = new StringBuffer();
        BufferedReader responseReader = null;
        OutputStream outputStream = null;
        try {
            responseReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine).append("\n");
            }
            responseReader.close();
            resultStr = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
            outputStream.close();
            }
        }
        JSONObject jsonObject = JSONObject.fromObject(resultStr);
        JSONArray jsonArray = jsonObject.getJSONArray("current_data");
        String dev_id = jsonArray.getJSONObject(0).getString("dev_id");//设备id
        String ds_id = jsonArray.getJSONObject(0).getString("ds_id");//参数名称
        String at = jsonArray.getJSONObject(0).getString("at");//数据时间
        String value = jsonArray.getJSONObject(0).getString("value");//数据的值
        System.out.println("触发器接收"+resultStr);
        if(ds_id.equals("order")){
        	byte[] buffer = null;
        	DeviceCMD deviceCMD = new DeviceCMD();
        	java.util.Random r=new java.util.Random();
        	int random = r.nextInt(60000);//随机数
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("devicenumber", dev_id);
//        	List<Map<String, Object>> list = dataCenterService.queryDevice(map);
//    		String key = list.get(0).get("rmk1").toString();
        	String key = "X5mYpEsaEvURqAjlVb0MAo0StYs=";
        	//CMC_S_TimeRcv
        	if(value.equals("165")){
        		deviceCMD.CMC_S_TimeRcv(random, dev_id, key);
        	}
        	//CMC_S_FileRcv
        	if(value.equals("242")){
//        		String ftpFileName = "E:/TEST/TEST.bin";//文件地址
//            	List<byte[]> bytelist = new ArrayList<byte[]>();//包集合
//        		bytelist = dataCenterService.byteList(ftpFileName);
//        		deviceCMD.CMC_S_FileRcv(random, dev_id, key, bytelist);
        	}
        
        }else{
        	//其它操作
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("devicenumber", dev_id);
        	map.put("objtypeid", 1);
        	map.put("isxlbyobj", "");
        	List<Map<String, Object>> list = dataCenterService.queryObjDevice(map);
        	map.put("paramname", ds_id);
//        	List<Map<String, Object>> list = dataCenterService.queryActualData(map);
        	map.put("datavalue", value);
    		map.put("collecttime", at);
    		for(int i = 0;i<list.size();i++){
    			map.put("objid", list.get(i).get("objid"));
    			dataCenterService.addwzData(map);
    		}
        }
	}
	
	
	public static void main(String[] args) {
//		byte转16进制
//		String str = "abcde";
//        char[] chars = "0123456789ABCDEF".toCharArray();  
//        StringBuilder sb = new StringBuilder("");
//        byte[] bs = str.getBytes();  
//        int bit;  
//        for (int i = 0; i < bs.length; i++) {  
//            bit = (bs[i] & 0x0f0) >> 4;  
//            sb.append(chars[bit]);  
//            bit = bs[i] & 0x0f;  
//            sb.append(chars[bit]);  
//        }  
//        System.out.println(sb.toString());
		
		//int转16进制
		int time = 1565839650;
//		System.out.println( (time & 0xFF));
//		System.out.println( (time >> 8 & 0xFF));
//		System.out.println((time >> 16 & 0xFF));
//		System.out.println( (time >> 24 & 0xFF));
		System.out.println(String.format("%02x", time));//2表示需要两个16进行数
		
		System.out.println(String.format("%08x", time));
		
//		String zdh = "CESHI";
//		for (int i = 0; i < zdh.length(); i++) {
//			System.out.println((byte)(zdh.charAt(i)));
//		}
	}
	
	/**
	 * 报警阀值  查询所选对象的相同的参数
	 * @return
	 */
	@RequestMapping(value = "/getobjParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getobjParam(HttpServletRequest request){
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		return dataCenterService.getobjParam(objid);
	}
	
	/**
	 * 报警阀值添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addThreshold.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> addThreshold(HttpServletRequest request){
		Logger.getLogger("").info("------------报警阀值添加-----------");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		String data = request.getParameter("data") == null?"":request.getParameter("data");
		try {
			JSONArray json = JSONArray.fromObject(data);
			String [] obj = objid.split(",");
			boolean iscz = true;
			for(String o:obj){
				for(int i=0;i<json.size();i++){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("objid", o);
					map.put("paramid", json.getJSONObject(i).get("paramid"));
//					map.put("val", json.getJSONObject(i).get("val"));
					List<Map<String, Object>> list = dataCenterService.queryThreshold(map);
					if(list!=null&&list.size()>0){
						returnMap.put("message", "操作失败，"+list.get(0).get("objname")+"-"+list.get(0).get("paramname")+"已存在！");
						returnMap.put("state", true);
						iscz = false;
					}
				}
			}
			if(iscz){
				dataCenterService.addThreshold(objid,json);
				returnMap.put("message", "操作成功！");
				returnMap.put("state", true);
				//日志存储
				baseaddDao.addUserOperateLog("一般操作","报警阀值管理", "新增对象id为："+objid, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "操作失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","报警阀值管理", "新增的对象id为："+objid, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 查询报警阀值
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryThreshold.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryThreshold(HttpServletRequest request){
		Logger.getLogger("").info("------------报警阀值查询-----------");
		String objName = request.getParameter("objname") == null?"":request.getParameter("objname");
		String paramName = request.getParameter("paramname") == null?"":request.getParameter("paramname");
		String thresholdid = request.getParameter("thresholdid") == null?"":request.getParameter("thresholdid");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objName", objName);
		map.put("paramName", paramName);
		map.put("thresholdid", thresholdid);
		List<Map<String, Object>> list = dataCenterService.queryThreshold(map);
		for(Map<String, Object> m:list){
			m.put("check", "");
		}
		return list;
	}
	
	/**
	 * 删除阀值
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delThreshold.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delThreshold(HttpServletRequest request){
		Logger.getLogger("").info("------------报警阀值删除-----------");
		String thresholdid = request.getParameter("thresholdid") == null?"":request.getParameter("thresholdid");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			dataCenterService.delThreshold(thresholdid);
			returnMap.put("message", "删除成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","报警阀值管理>删除", "thresholdid为："+thresholdid, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "删除失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","报警阀值管理>删除", "thresholdid为："+thresholdid, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 报警阀值修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateThreshold.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> updateThreshold(HttpServletRequest request){
		Logger.getLogger("").info("------------报警阀值修改-----------");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String thresholdid = request.getParameter("thresholdid") == null?"":request.getParameter("thresholdid");
		String val = request.getParameter("val") == null?"":request.getParameter("val");
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("thresholdid", thresholdid);
			map.put("val", val);
			dataCenterService.updateThreshold(map);
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","报警阀值管理>修改", "thresholdid为："+thresholdid, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","报警阀值管理>修改", "thresholdid为："+thresholdid, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		
		return returnMap;
	}
	
	/**
	 * 一张图   报警  查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryThr.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryThr(HttpServletRequest request){
		Logger.getLogger("").info("------------报警  查询-----------");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid");
		String fieldvalue = request.getParameter("fieldvalue") == null?"":request.getParameter("fieldvalue");
		String objname = request.getParameter("objname") == null?"":request.getParameter("objname");
		String timeType = request.getParameter("timeType") == null?"":request.getParameter("timeType");
		Map<String, Object> map = new HashMap<String, Object>();
		if(timeType.equals("1")){
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			map.put("begintime", f.format(new Date()));
		}
		if(timeType.equals("3")){
			Calendar cal = Calendar.getInstance();
		    cal.add(Calendar.DAY_OF_MONTH, -3);
		    int year = cal.get(Calendar.YEAR);
		    int month = cal.get(Calendar.MONTH) + 1;
		    int day = cal.get(Calendar.DATE);
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			map.put("begintime", year+"-"+month+"-"+day+" 00:00:00");
		}
		map.put("objid", objid);
		map.put("objname", objname);
		map.put("fieldvalue", fieldvalue);
		return dataCenterService.queryThr(map);
	}
	
	//站点管理 添加
	@RequestMapping(value = "/addobj2obj.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addobj2obj(HttpServletRequest request){
		Logger.getLogger("").info("站点管理 添加");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String gObjid = request.getParameter("objid1") == null?"":request.getParameter("objid1").toString();
		String wObjids= request.getParameter("objid2") == null?"":request.getParameter("objid2").toString();
		try {
			String[] objids = wObjids.split(",");
			for(String s:objids){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("objtypeid1", 5);
				map.put("objid1", gObjid);
				map.put("objtypeid2", 1);
				map.put("objid2", s);
				List<Map<String, Object>> list = dataCenterService.queryObj2Obj(map);
				if(list.size()==0){
					dataCenterService.addObj2Obj(map);
				}
			}
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","站点管理>添加", "", returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","站点管理>添加", e.getMessage(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		}
		return returnMap;
	}
	
	/**
	 * 站点管理查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryObj2Obj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryObj2Obj(HttpServletRequest request){
		String objid1 = request.getParameter("objid1") == null?"":request.getParameter("objid1").toString();
		String objname= request.getParameter("objname") == null?"":request.getParameter("objname").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid1", objid1);
		map.put("objname", objname);
		List<Map<String, Object>> list = dataCenterService.queryObj2Obj(map);
		for(Map<String, Object> m:list){
			m.put("check", "");
		}
		return list;
	}
	
	//站点管理 修改
	@RequestMapping(value = "/updateObj2Obj.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateObj2Obj(HttpServletRequest request){
		Logger.getLogger("").info("站点管理修改");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String gObjid = request.getParameter("objid1") == null?"":request.getParameter("objid1").toString();
		String wObjids= request.getParameter("objid2") == null?"":request.getParameter("objid2").toString();
		String[] objids = wObjids.split(",");
		try {
			for(String s:objids){
				if(s!=""){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("objtypeid1", 5);
					map.put("objid1", gObjid);
					map.put("objtypeid2", 1);
					map.put("objid2", s);
					List<Map<String, Object>> list = dataCenterService.queryObj2Obj(map);
					if(list.size()==0){
						dataCenterService.addObj2Obj(map);
					}
				}
				
			}
			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap.put("objid1", gObjid);
			List<Map<String, Object>> list = dataCenterService.queryObj2Obj(resMap);
			if(list.size()>objids.length){
				for(int i=list.size()-1;i>=0;i--){
					Map<String,Object> m = list.get(i);
					for(String s:objids){
						if((s).equals(m.get("objid2").toString())){
							list.remove(i);
						}
					}
				}
				for(Map<String, Object> m:list){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("objid1", m.get("objid1"));
					map.put("objid2", m.get("objid2"));
					dataCenterService.delObj2Obj(map);
				}
			}
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","站点管理>修改", "", returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("异常","站点管理>修改", e.getMessage(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		}
		return returnMap;
	}
	
	//站点管理 删除
	@RequestMapping(value = "/delObj2Obj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> delObj2Obj(HttpServletRequest request){
		Logger.getLogger("").info("站点管理 删除");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String objid1 = request.getParameter("objid1") == null?"":request.getParameter("objid1").toString();
			String objid2 = request.getParameter("objid2") == null?"":request.getParameter("objid2").toString();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objid1", objid1);
			map.put("objid2", objid2);
			dataCenterService.delObj2Obj(map);
			returnMap.put("message", "删除成功！");
			returnMap.put("state", true);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","站点管理>删除", "", returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "删除失败！");
			returnMap.put("state", false);
			//日志存储
			baseaddDao.addUserOperateLog("一般操作","站点管理>删除", e.getMessage(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
		}
		return returnMap;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 获取对象类型的信息表列表/查询功能
	 * @param objtypeid 对象类型ID
	 * @param isused 是否删除
	 * @param tableshowname 表名称
	 * @param tabletype 表类型
	 */
	@RequestMapping( value = "/getTableList.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Tableinfoadd> getTableList(HttpServletRequest request){
		Logger.getLogger("").info("字段管理->获取对象类型的信息表列表/查询功能");
		//获取前端传递的objtypeid
		int objtypeid = (request.getParameter("objtypeid") == null || request.getParameter("objtypeid").equals("")) ? 0 : Integer.parseInt(request.getParameter("objtypeid"));
		//获取前端传递的isused
		int isused = (request.getParameter("isused") == null || request.getParameter("isused").equals("")) ? 2 : Integer.parseInt(request.getParameter("isused"));
		//获取前端传递的tableshowname
		String tableshowname = (request.getParameter("tableshowname") == null || request.getParameter("tableshowname").equals("")) ? "" : request.getParameter("tableshowname");
		//获取前端传递的tabletype
		String tabletype = (request.getParameter("tabletype") == null || request.getParameter("tabletype").equals("")) ? "" : request.getParameter("tabletype");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", objtypeid);
		map.put("isused", isused);
		map.put("tableshowname", tableshowname);
		map.put("tabletype", tabletype);
		return dataCenterService.getTableListService(map);
	}
	

	/**
	 * 对象类型表新增
	 * @param table Tableinfoadd对象
	 */
	@RequestMapping( value = "/tableinfoadd.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> tableinfoadd(Tableinfoadd table){
		Logger.getLogger("").info("字段管理->对象类型表新增");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			Map<String, Object> parmmap = new HashMap<String, Object>();
			parmmap.put("objtypeid", table.getObjtypeid());
			parmmap.put("isused", 2);
			parmmap.put("tableshowname", table.getTableshowname());
			parmmap.put("tabletype", "");
			//根据objtypeid和表名称判断对象类型下的表名是否存在
			List<Tableinfoadd> listnameCount = dataCenterService.getTableListService(parmmap);
			//重新赋值参数，判断是否有一个基本表
			if(table.getTabletype().equals("基本表")){
				parmmap.put("tableshowname", "");
				parmmap.put("isused", 1);
				parmmap.put("tabletype", "基本表");
			}
			List<Tableinfoadd> listCount = dataCenterService.getTableListService(parmmap);
			if(listnameCount.size() > 0){	//判断对象类型下表是否重名
				returnMap.put("message", "该表名已存在，添加失败！");
				returnMap.put("state", false);
			}else if(listCount.size() > 0){	//判断对象类型下是否存在基本表
				returnMap.put("message", "一个对象类型只能添加一个基本表，添加失败！");
				returnMap.put("state", false);
			}else{	//添加表
				int num = dataCenterService.tableinfoaddService(table);
				if(num > 0){
					returnMap.put("message", "添加成功！");
					returnMap.put("state", true);
					//日志存储
					baseaddDao.addUserOperateLog("一般操作","字段管理", "对象类型表新增->对象objtypeid为："+table.getObjtypeid()+",表类型为："+table.getTabletype()+",表名称为："+table.getTableshowname(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
				}else{
					returnMap.put("message", "添加失败！");
					returnMap.put("state", true);
				}
			}
		} catch (Exception e) {
			returnMap.put("message", "添加异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "对象类型表新增->对象objtypeid为："+table.getObjtypeid()+",表类型为："+table.getTabletype()+",表名称为："+table.getTableshowname(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 对象类型表修改
	 * @param table Tableinfoadd对象
	 */
	@RequestMapping( value = "/tableinfoupdate.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> tableinfoupdate(Tableinfoadd table){
		Logger.getLogger("").info("字段管理->对象类型表修改");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			if(table.getTableid() != 0 && !"".equals(table.getTableid()) && table.getTableshowname() != null && !"".equals(table.getTableshowname())){
				Map<String, Object> parmmap = new HashMap<String, Object>();	
				parmmap.put("tableid", table.getTableid());
				parmmap.put("tableshowname", table.getTableshowname());
				//根据objtypeid和表名称判断对象类型下的表名是否存在
				List<Tableinfoadd> listnameCount = dataCenterService.getTableListService(parmmap);
				if (listnameCount.size() > 0) {	//判断对象类型下表是否重名
					returnMap.put("message", "该表名已存在，修改失败！");
					returnMap.put("state", false);
				} else {
					int num = dataCenterService.tableinfoupdateService(table);
					if (num > 0) {
						returnMap.put("message", "修改成功！");
						returnMap.put("state", true);
						//日志存储
						baseaddDao.addUserOperateLog("一般操作","字段管理", "对象类型表信息修改->对象objtypeid为："+table.getObjtypeid(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
					} else {
						returnMap.put("message", "修改失败！");
						returnMap.put("state", false);
					}
				}	
			} else {
				returnMap.put("message", "修改失败！");
				returnMap.put("state", false);
			}
		} catch (Exception e) {
			returnMap.put("message", "修改异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "对象类型表信息修改->对象objtypeid为："+table.getObjtypeid(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 对象类型表批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param tableid 表id
	 */
	@RequestMapping( value = "/tableBatchRemoveAndRecover.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> tableBatchRemoveAndRecover(HttpServletRequest request){
		Logger.getLogger("").info("字段管理->对象类型表删除/恢复");
		//state状态（传参‘删除’或者‘恢复’）
		String state = (request.getParameter("state") == null || "".equals(request.getParameter("state"))) ? "" : request.getParameter("state");
		//tableid多个tableid用逗号隔开
		String tableid = (request.getParameter("tableid") == null || "".equals(request.getParameter("tableid"))) ? "" : request.getParameter("tableid");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			if (!"".equals(state) && !"".equals(tableid)) {
				Map<String, Object> parmmap = new HashMap<String, Object>();
				parmmap.put("tableid", tableid);
				if (state.equals("删除")) {
					int code = 0;
					parmmap.put("state", code);
					int num = dataCenterService.tableBatchRemoveAndRecoverService(parmmap);
					if (num > 0) {
						returnMap.put("message", "操作成功！");
						returnMap.put("state", true);
						//日志存储
						baseaddDao.addUserOperateLog("一般操作","字段管理", "对象类型表删除/恢复->删除/恢复的对象objtypeid为："+tableid+",操作状态为："+state, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
					} else {
						returnMap.put("message", "操作失败！");
						returnMap.put("state", false);
					}
				} else if (state.equals("恢复")) {
					int code = 1;
					parmmap.put("state", code);
					int num = dataCenterService.tableBatchRemoveAndRecoverService(parmmap);
					if (num > 0) {
						returnMap.put("message", "操作成功！");
						returnMap.put("state", true);
						//日志存储
						baseaddDao.addUserOperateLog("一般操作","字段管理", "对象类型表删除/恢复->删除/恢复的对象objtypeid为："+tableid+",操作状态为："+state, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
					} else {
						returnMap.put("message", "操作失败！");
						returnMap.put("state", false);
					}
				} else {
					returnMap.put("message", "操作失败！");
					returnMap.put("state", false);
				}
			} else {
				returnMap.put("message", "操作失败！参数不能为空！");
				returnMap.put("state", false);
			}
		} catch (Exception e) {
			returnMap.put("message", "操作异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "对象类型表删除/恢复->删除/恢复的对象objtypeid为："+tableid+",操作状态为："+state, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 查看表的字段信息
	 * @param tableid 表信息id
	 * @param isused 是否删除
	 * @param fieldname 字段名称
	 * @param fieldshowname 字段显示名称
	 */
	@RequestMapping( value = "/getFieldinfoList.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Tableinfo> getFieldinfoList(HttpServletRequest request){
		Logger.getLogger("").info("字段管理->获取信息表字段列表");
		//获取前端传递的tableid
		String tableid = (request.getParameter("tableid") == null || "".equals(request.getParameter("tableid"))) ? "" : request.getParameter("tableid");
		//获取前端传递的isused
		String isused = (request.getParameter("isused") == null || "".equals(request.getParameter("tableid"))) ? "" : request.getParameter("isused");
		//获取前端传递的fieldname
		String fieldname = (request.getParameter("fieldname") == null || "".equals(request.getParameter("fieldname"))) ? "" : request.getParameter("fieldname");
		//获取前端传递的tabletype
		String fieldshowname = (request.getParameter("fieldshowname") == null || "".equals(request.getParameter("fieldshowname"))) ? "" : request.getParameter("fieldshowname");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableid", tableid);
		map.put("isused", isused);
		map.put("fieldname", fieldname);
		map.put("fieldshowname", fieldshowname);
		return dataCenterService.getFieldinfoListService(map);
	}
	
	/**
	 * 表字段信息新增
	 * @param table Tableinfo对象
	 */
	@RequestMapping( value = "/fieldinfoadd.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> fieldinfoadd(Tableinfo table){
		Logger.getLogger("").info("字段管理->表字段新增");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			if(table.getTableid() != 0 && table.getFieldname() != null && !"".equals(table.getFieldname())
			   && table.getFieldshowname() != null && !"".equals(table.getFieldshowname())
			   && table.getFielddatatype() != null && !"".equals(table.getFielddatatype())){
				Map<String, Object> parmmap = new HashMap<String, Object>();
				parmmap.put("tableid", table.getTableid());
				parmmap.put("fieldname", table.getFieldname());
				parmmap.put("fieldshowname", table.getFieldshowname());
				//根据tableid和字段名称和字段显示名称判断字段名称或者字段显示名称是否存在
				List<Tableinfo> listnameCount = dataCenterService.getFieldinfoListService(parmmap);
				if(listnameCount.size() > 0){	//判断字段是否重名
					returnMap.put("message", "该字段名或者字段描述已存在，界面无展示，请到回收站恢复。添加失败！");
					returnMap.put("state", false);
				}else{	//添加字段
					int num = dataCenterService.fieldinfoaddService(table);
					if(num > 0){
						returnMap.put("message", "添加成功！");
						returnMap.put("state", true);
						//日志存储
						baseaddDao.addUserOperateLog("一般操作","字段管理", "表字段新增->对象表tableid为："+table.getTableid()+",字段名称为："+table.getFieldname()+",字段描述为："+table.getFieldshowname()+",字段类型为："+table.getFielddatatype(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
					}else{
						returnMap.put("message", "添加失败！");
						returnMap.put("state", false);
					}
				}
			}else{
				returnMap.put("message", "数据不能为空！添加失败！");
				returnMap.put("state", false);
			}
		} catch (Exception e) {
			returnMap.put("message", "添加异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "表字段新增->对象表tableid为："+table.getTableid()+",字段名称为："+table.getFieldname()+",字段描述为："+table.getFieldshowname()+",字段类型为："+table.getFielddatatype(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 表字段信息修改
	 * @param table Tableinfo对象
	 */
	@RequestMapping( value = "/fieldinfoupdate.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> fieldinfoupdate(Tableinfo table){
		Logger.getLogger("").info("字段管理->字段信息修改");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			if(table.getFieldid() != 0 && !"".equals(table.getFieldid()) 
			   && table.getFieldname() != null && !"".equals(table.getFieldname())
			   && table.getFieldshowname() != null && !"".equals(table.getFieldshowname())
			   && table.getFielddatatype() != null && !"".equals(table.getFieldshowname())){
				int num = dataCenterService.fieldinfoupdateService(table);
				if (num > 0) {
					returnMap.put("message", "修改成功！");
					returnMap.put("state", true);
					//日志存储
					baseaddDao.addUserOperateLog("一般操作","字段管理", "字段信息修改->表字段fieldid为："+table.getFieldid(), returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
				} else {
					returnMap.put("message", "修改失败！");
					returnMap.put("state", false);
				}
			} else {
				returnMap.put("message", "数据不能为空！修改失败！");
				returnMap.put("state", false);
			}
		} catch (Exception e) {
			returnMap.put("message", "修改异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "字段信息修改->表字段fieldid为："+table.getFieldid(), returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 表字段排序
	 * @param fieldid 字段id
	 * @param orderintable 排序值
	 * @param order 排序（降序/升序）
	 */
	@RequestMapping( value = "/fieldorderby.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> fieldorderby(HttpServletRequest request){
		Logger.getLogger("").info("字段管理->字段排序");
		//获取前端传递的filedid
		String fieldid = (request.getParameter("fieldid") == null || "".equals(request.getParameter("fieldid"))) ? "" : request.getParameter("fieldid");
		//获取前端传递的orderintable
		String orderintable = (request.getParameter("orderintable") == null || "".equals(request.getParameter("orderintable"))) ? "" : request.getParameter("orderintable");
		//获取前端传递的order
		String order = (request.getParameter("order") == null || "".equals(request.getParameter("order"))) ? "" : request.getParameter("order");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		Map<String, Object> parmmap = new HashMap<String, Object>();
		try {
			if(!"".equals(fieldid) && !"".equals(orderintable) && !"".equals(order)){
				parmmap.put("fieldid", fieldid);
				parmmap.put("orderintable", orderintable);
				if(order.equals("降序")){
					parmmap.put("order", "+");
				}else if(order.equals("升序")){
					parmmap.put("order", "-");
				}else{
					parmmap.put("order", "");
				}
				int num = dataCenterService.fieldorderbyService(parmmap);
				if (num > 0) {
					returnMap.put("message", "排序成功！");
					returnMap.put("state", true);
					//日志存储
					baseaddDao.addUserOperateLog("一般操作","字段管理", "字段排序->表字段fieldid为："+fieldid+",排序状态为："+order, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
				} else {
					returnMap.put("message", "排序失败！");
					returnMap.put("state", false);
				}
			} else {
				returnMap.put("message", "数据不能为空！排序失败！");
				returnMap.put("state", false);
			}
		} catch (Exception e) {
			returnMap.put("message", "排序异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "字段排序->表字段fieldid为："+fieldid+",排序状态为："+order, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 字段批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param fieldid 字段id
	 */
	@RequestMapping( value = "/fieldBatchRemoveAndRecover.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> fieldBatchRemoveAndRecover(HttpServletRequest request){
		Logger.getLogger("").info("字段管理->表字段删除/恢复");
		//state状态（传参‘删除’或者‘恢复’）
		String state = (request.getParameter("state") == null || "".equals(request.getParameter("state"))) ? "" : request.getParameter("state");
		//tableid多个tableid用逗号隔开
		String fieldid = (request.getParameter("fieldid") == null || "".equals(request.getParameter("fieldid"))) ? "" : request.getParameter("fieldid");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			if (!"".equals(state) && !"".equals(fieldid)) {
				Map<String, Object> parmmap = new HashMap<String, Object>();
				parmmap.put("fieldid", fieldid);
				if (state.equals("删除")) {
					int code = 0;
					parmmap.put("state", code);
					int num = dataCenterService.fieldBatchRemoveAndRecoverService(parmmap);
					if (num > 0) {
						returnMap.put("message", "操作成功！");
						returnMap.put("state", true);
						//日志存储
						baseaddDao.addUserOperateLog("一般操作","字段管理", "表字段删除/恢复->删除/恢复的表字段fieldid为："+fieldid+",操作状态为："+state, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
					} else {
						returnMap.put("message", "操作失败！");
						returnMap.put("state", false);
					}
				} else if (state.equals("恢复")) {
					int code = 1;
					parmmap.put("state", code);
					int num = dataCenterService.fieldBatchRemoveAndRecoverService(parmmap);
					if (num > 0) {
						returnMap.put("message", "操作成功！");
						returnMap.put("state", true);
						//日志存储
						baseaddDao.addUserOperateLog("一般操作","字段管理", "表字段删除/恢复->删除/恢复的表字段fieldid为："+fieldid+",操作状态为："+state, returnMap.get("message").toString(), returnMap.get("state").toString(), "", "");
					} else {
						returnMap.put("message", "操作失败！");
						returnMap.put("state", false);
					}
				} else {
					returnMap.put("message", "操作失败！");
					returnMap.put("state", false);
				}
			} else {
				returnMap.put("message", "操作失败！参数不能为空！");
				returnMap.put("state", false);
			}
		} catch (Exception e) {
			returnMap.put("message", "操作异常！");
			returnMap.put("state", false);
			e.printStackTrace();
			//日志存储
			baseaddDao.addUserOperateLog("异常","字段管理", "表字段删除/恢复->删除/恢复的表字段fieldid为："+fieldid+",操作状态为："+state, returnMap.get("message").toString(), returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	/**
	 * 添加目录
	 * @param request
	 * @return
	 */
	@RequestMapping( value = "/addmenu.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object addmenu(HttpServletRequest request){
		//目录显示名称
		String dirname = request.getParameter("dirname") == null ? "" : request.getParameter("dirname");
		//目录级别
		String dirlevel= request.getParameter("dirlevel") == null ? "" : request.getParameter("dirlevel");
		//上级目录id
		String higherlevelid=request.getParameter("higherlevelid") == null ? "" : request.getParameter("higherlevelid");
		//显示顺序
		String displayorder = request.getParameter("displayorder") == null ? "" : request.getParameter("displayorder");
		//目录结构名称的id
		String dirtypeid = request.getParameter("dirtypeid") == null ? "" : request.getParameter("dirtypeid");
		//目录结构名称的id
		String dirtypename = request.getParameter("dirtypename") == null ? "" : request.getParameter("dirtypename");
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("dirname", dirname);
			map.put("dirlevel", dirlevel);
			Map<String,Object> rmap = dataCenterService.querycontrol(map);//查询该目录层级下最大的id
			map.put("higherlevelid", higherlevelid);
			map.put("dirtypeid", dirtypeid);
			int id = 0;
			Map<String,Object> rmap2 = dataCenterService.querycontrol(map);//查询目录层级
			if(rmap2!=null){
				map.put("displayorder", Integer.parseInt(rmap2.get("displayorder").toString())+1);
			}else{
				map.put("displayorder", 0);
			}
			
			if(dirlevel.equals("1")){
				if(rmap!=null||rmap.size()!=0){
					id = Integer.parseInt(rmap.get("id").toString())+1;
				}else{
					id = 1;
				}
				map.put("displayorder",0);
			}else if(dirlevel.equals("2")){
				if(rmap!=null||rmap.size()!=0){
					id = Integer.parseInt(rmap.get("id").toString())+1;
				}else{
					id = Integer.parseInt(higherlevelid)*200+1;
				}
			}else if(dirlevel.equals("3")){
				if(rmap!=null||rmap.size()!=0){
					id = Integer.parseInt(rmap.get("id").toString())+1;
				}else{
					id = Integer.parseInt(higherlevelid)*200+1;
				}
			}
			map.put("id", id);
			map.put("dirtypename", dirtypename);
			dataCenterService.addcontrol(map);
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加异常！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	/**
	 * 删除目录
	 * @param request
	 * @return
	 */
	@RequestMapping( value = "/delmenu.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object delmenu(HttpServletRequest request){
		Map<String, Object> returnMap = new HashMap<String,Object>();
		try {
			String id = request.getParameter("id") == null ? "" : request.getParameter("id");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", id);
			dataCenterService.delcontrol(map);
			returnMap.put("message", "删除成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "删除异常！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	/**
	 * 目录管理结构查询
	 * @return
	 */
	@RequestMapping( value = "/queryMenujg.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object queryMenujg(){
		return dataCenterService.queryMenujg();
	}
	
	/**
	 * 目录结构添加
	 * @param request
	 * @return
	 */
	@RequestMapping( value = "/addMenujg.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object addMenujg(HttpServletRequest request){
		String dirtypename = request.getParameter("dirtypename") == null ? "" : request.getParameter("dirtypename");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dirtypename", dirtypename);
		Map<String, Object> returnMap = new HashMap<String,Object>();
		try {
			dataCenterService.addMenujg(map);
			returnMap.put("message", "添加成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "添加异常！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	/**
	 * 删除目录结构
	 * @param request
	 * @return
	 */
	@RequestMapping( value = "/delMenujg.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object delMenujg(HttpServletRequest request){
		String dirtypeid = request.getParameter("dirtypeid") == null ? "" : request.getParameter("dirtypeid");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dirtypeid", dirtypeid);
		Map<String, Object> returnMap = new HashMap<String,Object>();
		try {
			dataCenterService.delMenujg(map);
			returnMap.put("message", "删除成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "删除异常！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	/**
	 * 目录名称修改
	 * @param request
	 * @return
	 */
	@RequestMapping( value = "/updateMenuName.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object updateMenuName(HttpServletRequest request){
		String dirtypeid = request.getParameter("dirtypeid") == null ? "" : request.getParameter("dirtypeid");
		String id = request.getParameter("id") == null ? "" : request.getParameter("id");
		String dirname = request.getParameter("dirname") == null ? "" : request.getParameter("dirname");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dirtypeid", dirtypeid);
		map.put("id", id);
		map.put("dirname", dirname);
		Map<String, Object> returnMap = new HashMap<String,Object>();
		try {
			dataCenterService.updateMenuName(map);
			returnMap.put("message", "修改成功！");
			returnMap.put("state", true);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "修改异常！");
			returnMap.put("state", false);
		}
		return returnMap;
	}
	
	/**
	 * @author 五易科技
	 * @description 目录管理-获取目录列表
	 */
	@RequestMapping(value = "/getMenuList.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmMenu> getMenuList(HttpServletRequest request){
		Logger.getLogger("").info("---目录管理-获取目录列表---");
		String dirtypeid = request.getParameter("dirtypeid") == null ? "" : request.getParameter("dirtypeid");
		List<OmMenu> menuList = dataCenterService.getMenuList(dirtypeid);
		return menuList;
	}
	/**
	 * 依据目录等级和结构查询目录
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryMenuBylevel.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Object queryMenuBylevel(HttpServletRequest request){
		//结构
		String dirtypeid = request.getParameter("dirtypeid") == null ? "" : request.getParameter("dirtypeid");
		//等级
		String dirlevel = request.getParameter("dirlevel") == null ? "" : request.getParameter("dirlevel");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dirtypeid", dirtypeid);
		map.put("dirlevel", dirlevel);
		return dataCenterService.queryMenuBylevel(map);
	}
	
	/**
	 * 水文数据点新增
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/SWAddDatapointsApi.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Object SWAddDatapointsApi(HttpServletRequest request) throws Exception {
		//设备编号
		String devid = request.getParameter("devid") == null ? "" : request.getParameter("devid");
		//参数时间
		String datetime = request.getParameter("datetime") == null ? "" : request.getParameter("datetime");
		//参数名称
		String datename = request.getParameter("dataname") == null ? "" : request.getParameter("dataname");
		//数据内容
		String oval = request.getParameter("oval") == null ? "" : request.getParameter("oval");
		String key = "VAvSN3ADMhvfnK6LR0MvHyJwQfU=";
		List<Datapoints> list = new ArrayList<Datapoints>();
		List<Data> dl = new ArrayList<Data>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dl.add(new Data(format.format(f.parse(datetime)), oval));
		list.add(new Datapoints(datename, dl));
		Map<String, List<Datapoints>> map = new HashMap<String, List<Datapoints>>();
		map.put("datastreams", list);
		AddDatapointsApi api = new AddDatapointsApi(map, null, null, devid, key);
		BasicResponse<Void> response = api.executeApi();
		System.out.println("errno:"+response.errno+" error:"+response.error);
		Map<String, Object> Rmap = new HashMap<String, Object>();
		Rmap.put("errno", response.errno);
		Rmap.put("error", response.error);
		return Rmap;
	}
	
	/**
	 * 气象设备生成
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/EstablishDevice.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Object EstablishDevice(HttpServletRequest request){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", 19);
		List<Map<String, Object>> list = dataCenterService.queryObj(map);
		for(int i=0;i<list.size();i++){
			Map<String, Object> objMap = list.get(i);
			System.out.println("当前对象："+objMap.get("objname")+" index:"+i+" sum:"+list.size());
			//接入协议
			String protocol = "HTTP";
			
			try {
				//鉴权信息
				String auth_info = objMap.get("objid").toString();
				String devicename =objMap.get("objname").toString();
				String key = "oFiXvOMYlE4ohV=mVCn6IEd=9zM=";
				/****
				 * 设备新增
				 */
				List<String> tags = new ArrayList<String>();  
				tags.add("气象数据");
				boolean isPrivate = true;
				AddDevicesApi api = new AddDevicesApi(objMap.get("objname").toString(), protocol, null, tags, null, isPrivate, auth_info, null, null, key);
				BasicResponse<NewDeviceResponse> response = api.executeApi();
				System.out.println("errno:"+response.errno+" error:"+response.error);
				System.out.println(response.getJson());
				if(response.error.contains("succ")){
					Map<String, Object> devMap = new HashMap<String, Object>();
					devMap.put("devicenumber", response.data.DeviceId);
					//检查设备编号唯一性
					List<Map<String, Object>> devNumberList = dataCenterService.queryDevice(devMap);
					if(devNumberList.size()>0){
						returnMap.put("message", "OneNet自动生成设备编号已存在！请重新创建。");
						returnMap.put("state", false);
						DeleteDeviceApi delapi = new DeleteDeviceApi(response.data.DeviceId, key);//删除OneNet上的设备
						BasicResponse<Void> delresponse = delapi.executeApi();
					}else{
						try{
							Map<String, Object> deviceMap = new HashMap<String, Object>();
							deviceMap.put("devicenumber", response.data.DeviceId);
							deviceMap.put("position", auth_info);
							deviceMap.put("devicetypeid", 26);
							deviceMap.put("devicename", devicename);
							deviceMap.put("isentity", 1);
							deviceMap.put("key", key);
							dataCenterService.addDevice(deviceMap);//后台添加设备
							//查询设备类型是否绑定参数
							Map<String, Object> paramMap = new HashMap<String, Object>();
							paramMap.put("devicetypeid", 16);
							paramMap.put("isused", 1);
							List<Map<String, Object>> paramlist = dataCenterService.queryDeviceParam(paramMap);
							for(Map<String, Object> paramMap2:paramlist){
								String id = paramMap2.get("paramname").toString();
								String devId = response.data.DeviceId;
								String unit = paramMap2.get("dataunit").toString();
								String unitSymbol = paramMap2.get("dataunit").toString();
								/**
								 * OneNet数据流新增
								 * @param id：数据流名称 ，String
								 * @param devId:设备ID,String
								 * @param tags:数据流标签（可选，可以为一个或多个）,List<Stirng>
								 * @param unit:单位（可选）,String
								 * @param unitSymbol:单位符号（可选）,String
								 * @param cmd:MODBUS设备填写，周期下发命令，16进制字节字符串
								 * @param interval:MODBUS设备填写，采集间隔，秒,Integer
								 * @param formula:MODBUS设备填写，寄存器计算公式（可选）,String
								 * @param key:masterkey 或者 设备apikey
								 */
								AddDatastreamsApi addapi = new AddDatastreamsApi(id, devId, tags, unit, unitSymbol, null, null, null, key);
								BasicResponse<NewdatastramsResponse> addresponse = addapi.executeApi();
								System.out.println("绑定数据流"+id+"errno:"+addresponse.errno+" error:"+addresponse.error);
							}
							returnMap.put("message", "添加成功！");
							returnMap.put("state", true);
						}catch(Exception e){
							e.printStackTrace();
							returnMap.put("message", "数据中心添加失败！");
							returnMap.put("state", false);
							DeleteDeviceApi api1 = new DeleteDeviceApi(response.data.DeviceId, key);
							BasicResponse<Void> response1 = api1.executeApi();
							System.out.println("设备管理"+"errno:"+response1.errno+" error:"+response1.error);
						}
					}
				}else{
					returnMap.put("message", "OneNE平台添加失败！"+response.error);
					returnMap.put("state", false);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@RequestMapping(value = "/queryTime.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Object queryTime(){
		//获取当前时间
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("time",f.format(new Date()));
        return map;
	}
	
}
