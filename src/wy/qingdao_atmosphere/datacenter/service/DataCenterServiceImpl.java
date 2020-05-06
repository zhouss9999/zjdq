package wy.qingdao_atmosphere.datacenter.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


import cmcc.iot.onenet.javasdk.api.datapoints.GetDatapointsListApi;
import cmcc.iot.onenet.javasdk.response.BasicResponse;
import cmcc.iot.onenet.javasdk.response.datapoints.DatapointsList;

import wy.qingdao_atmosphere.datacenter.dao.DataCenterDao;
import wy.qingdao_atmosphere.datacenter.domain.Devicetype;
import wy.qingdao_atmosphere.datacenter.domain.Objtype;
import wy.qingdao_atmosphere.datacenter.domain.Param;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfo;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfoadd;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.util.AirBean;

@SuppressWarnings("all")
@Service("dataCenterService")
public class DataCenterServiceImpl implements DataCenterService{
	private DataCenterDao dataCenterDao;
	
	public DataCenterDao getDataCenterDao() {
		return dataCenterDao;
	}
	
	@Resource
	public void setDataCenterDao(DataCenterDao dataCenterDao) {
		this.dataCenterDao = dataCenterDao;
	}

	//数据中心-参数管理
	public List<Param> getAllparameter(Map<String,Object> map) {
		return dataCenterDao.getAllparameter(map);
	}

	//参数管理-新增
	public void addparam(Param param) {
		dataCenterDao.addparam(param);
	}
	
	//参数管理-修改
	public void updateParam(Param param) {
		dataCenterDao.updateParam(param);
	}
	
	//参数管理-删除或恢复
	public void delparam(Map<String,Object> map) {
		dataCenterDao.delparam(map);
	}

	//对象类型管理 查询
	public List<Objtype> queryObjtype(Map<String, Object> map) {
		return dataCenterDao.queryObjtype(map);
	}
	//对象类型管理 添加
	public void addObjtype(Objtype objtype) {
		 dataCenterDao.addObjtype(objtype);
	}
	//对象类型管理 删除或恢复
	public void delObjtype(Map<String, Object> map) {
		dataCenterDao.delObjtype(map);
	}
	//对象类型管理 修改
	public void updateObjtype(Objtype objtype) {
		dataCenterDao.updateObjtype(objtype);
	}
	//对象安装设备 查询
	public List<Map<String, Object>> queryObjDevice(Map<String,Object> map){
		List<Map<String, Object>> list = dataCenterDao.queryObjDevice(map);
		HashSet<Map<String,Object>> h = new HashSet<Map<String,Object>>();
		if(map.containsKey("isxlbyobj"))
		if(("1").equals(map.get("isxlbyobj").toString())){
			for(Map<String, Object> m:list){
				Map<String,Object> hmap = new HashMap<String, Object>();
				if(!("").equals(m.get("devicenumber").toString())){
					hmap.put("devicenumber", m.get("devicenumber"));
					hmap.put("devicename", m.get("devicename"));
					hmap.put("devicetypeid", m.get("devicetypeid"));
					hmap.put("devicetypename", m.get("devicetypename"));
					h.add(hmap);
				}
			}
			list.clear();
			list.addAll(h);
		}
		return list;
	}
	//对象安装设备  添加
	public void addObjinfo(Map<String,Object> omap){
		String devicenumber = omap.get("devicenumber").toString();
		List<Map<String, Object>> list = dataCenterDao.queryDeviceInfo(devicenumber);
		if(list.size()>0){
			List<Map<String, Object>> addList = new ArrayList<Map<String,Object>>();
			String connectids = "";//恢复的id
			String objid = omap.get("objid").toString();
			Map<String, Object> datamap = new HashMap<String, Object>();
			datamap.put("objids", objid);
			datamap.put("devicenumber", devicenumber);
			//查询曾经卸载的数据
			List<Map<String, Object>> list2 = dataCenterDao.queryObjNotUsed(datamap);
			String objtypeid = omap.get("objtypeid").toString();
			for(int i=0;i<list.size();i++){
				Map<String, Object> map = list.get(i);
				map.put("objid", objid);
				//objtypeid_objid_devicenumber_paramid
				String dataguid = objtypeid+"_"+objid+"_"+map.get("devicenumber")+"_"+map.get("paramid");
				map.put("dataguid", dataguid);
				boolean isuse = false;
				if(list2.size()>0){
					for(int s=0;s<list2.size();s++){
						Map<String, Object> map2 = list2.get(s);
						if((map2.get("dataguid").toString()).equals(dataguid)){
							connectids += map2.get("connectid")+",";
							isuse = true;
							break;
						}
					}
				}
				if(!isuse)
				addList.add(map);
			}
			
			if(addList.size()>0){
				dataCenterDao.addObjinfo(addList);
			}
			if(!connectids.equals("")){
				//恢复
				connectids = connectids.substring(0,connectids.length()-1);
				dataCenterDao.recoveryObj(connectids);
			}
		}
	}
	
	//对象安装设备 安装页 设备查询
	public List<Map<String, Object>> querydeviceName(Map<String, Object> map){
		return dataCenterDao.querydeviceName(map);
	}
	
	//对象安装设备 卸载
	public void delObjinfo(Map<String, Object> map){
		dataCenterDao.delObjinfo(map);
	}
	
	//-----------------------------------设备管理
	
	
	//设备类型管理 查询
	public List<Devicetype> queryDevicetypes(Map<String, Object> map) {
		return dataCenterDao.queryDevicetypes(map);
	}
	//设备类型管理 添加
	public void addDevicetype(Devicetype devicetype) {
		dataCenterDao.addDevicetype(devicetype);
	}
	//设备类型管理 修改
	public void updateDevicetype(Devicetype devicetype) {
		dataCenterDao.updateDevicetype(devicetype);
	}
	//设备类型管理 删除
	public void delDeviceype(Map<String, Object> map) {
		dataCenterDao.delDeviceype(map);
	}
	
	//设备版本添加
	public void fileupload(HttpServletRequest request,HttpServletResponse response) throws Exception{
		//版本
	    String version = request.getParameter("version") == null?"":request.getParameter("version");
	    //操作类型  - 添加或修改
	    String operation = request.getParameter("operation") == null?"":request.getParameter("operation");
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("version", version);
	    String verid = "";
	    if(("update").equals(operation)){
	    	verid = request.getParameter("verid") == null?"":request.getParameter("verid");
	    	map.put("verid", verid);
	    }
	    List<Map<String, Object>> list = dataCenterDao.queryDevVersion(map);
	    if(list.size()>0){
	    	throw new Exception("版本号已存在！");
	    }
		String path = DataCenterService.class.getResource("DataCenterService.class").toString();
        path = path.substring(6, path.indexOf("WEB-INF"));
        File deviceVersion = new File(path+ "upload/deviceVersion/");
    	if  (!deviceVersion .exists()  && !deviceVersion .isDirectory())   
		{
    		deviceVersion.mkdir(); 
		}
		CommonsMultipartFile multipartFile = null;
//		Iterator<String> itr =  request.getFileNames();
//	    String str = itr.next();
//	    multipartFile = (CommonsMultipartFile)request.getFile(str);
//	    String fileName = multipartFile.getOriginalFilename();   //原文件名
//	    String filePath = "";
//	    if(!("").equals(fileName)){
//	    	MultipartFile mpf = request.getFile(str);
//	        InputStream inputStream = mpf.getInputStream();
//	        FileOutputStream outputStream = null;               
//			// 给新文件拼上时间毫秒，防止重名
//			SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HHmmssSSS-");
//			filePath = "file-" + f.format(new Date()) + fileName;
//			File file = new File(path+ "upload/deviceVersion/", filePath);
//			file.createNewFile();
//			outputStream = new FileOutputStream(file);
//			byte temp[] = new byte[1024];
//			int size = -1;
//			while ((size = inputStream.read(temp)) != -1) { // 每次读取1KB，直至读完
//				outputStream.write(temp, 0, size);
//			}
//		    outputStream.close();
//		    inputStream.close();
//	    }
	    
	    
	    String devicetypeid = request.getParameter("devicetypeid") == null?"":request.getParameter("devicetypeid");
	    String fileName = request.getParameter("fileName") == null?"":request.getParameter("fileName");
	    map.put("devicetypeid", devicetypeid);
	    map.put("version", version);
	    map.put("file", fileName);
	    map.put("collecttime", new Date());
	    if(("add").equals(operation)){
	    	dataCenterDao.fileupload(map);
	    }
	    if(("update").equals(operation)){
	    	if(!("").equals(verid)){
	    		map.put("verid", verid);
	    		dataCenterDao.updateDevVersion(map);
	    	}
	    }
	    
	}
	
	//设备版本查询
	public List<Map<String,Object>> queryDevVersion(Map<String,Object> map){
		return dataCenterDao.queryDevVersion(map);
	}
	
	//设备版本删除
	public void delDevVersion(String verid){
		dataCenterDao.delDevVersion(verid);
	}
	
	//设备管理 列表查询
	public List<Map<String, Object>> queryDevice(Map<String, Object> map) {
		return dataCenterDao.queryDevice(map);
	}
	//设备管理 设备类型查询
	public List<Map<String, Object>> queryDevicetypeMap() {
		return dataCenterDao.queryDevicetypeMap();
	}
	//设备管理 设备名称查询
	public List<Map<String, Object>> queryDeviceNameMap(int devicetypeid) {
		return dataCenterDao.queryDeviceNameMap(devicetypeid);
	}
	//设备管理  添加
	public void addDevice(Map<String, Object> map) {
		dataCenterDao.addDevice(map);
	}
	//设备管理  修改
	public int updateDevice(Map<String, Object> map) {
		return dataCenterDao.updateDevice(map);
	}
	//设备管理  删除或恢复
	public void delDevice(Map<String,Object> map){
		dataCenterDao.delDevice(map);
	}
	//设备参数管理  列表查询
	public List<Map<String, Object>> queryDeviceParam(Map<String, Object> map){
		return dataCenterDao.queryDeviceParam(map);
	}
	//设备参数管理  添加 参数查询
	public List<Map<String, Object>> queryParambydeviceType(Map<String, Object> map){
		return dataCenterDao.queryParambydeviceType(map);
	}
	//设备参数管理  添加
	public void addDeviceTypeParam(Map<String, Object> map){
		dataCenterDao.addDeviceTypeParam(map);
	}
	//设备参数管理  修改页查询
	public List<Map<String, Object>> queryDP(int devicetypeid){
		return dataCenterDao.queryDP(devicetypeid);
	}
	//设备参数管理  删除或恢复
	public void delDeviceTypeParam(Map<String, Object> map){
		dataCenterDao.delDeviceTypeParam(map);
	}
	//设备参数管理  修改
	public void updateDP(List<Map<String, Object>> list,String devicetypeid){
		//state=1(添加),0(已添加并删除),-1(未添加)
		//+3    4(不动) 3(恢复)         2(添加)        1(删除)       0(不动)     -1(不动)
		String addDP = "";//新增id
		String delDP1= "";//恢复id
		String delDP0= "";//删除id
		for(int i=0;i<list.size();i++){
			Map<String, Object> map = list.get(i);
			if((map.get("state").toString()).equals("2")){
				addDP+=map.get("paramid")+",";
			}
			if((map.get("state").toString()).equals("3")){
				delDP1+=map.get("connectid")+",";
			}
			if((map.get("state").toString()).equals("1")){
				delDP0+=map.get("connectid")+",";
			}
		}
		if(!addDP.equals("")){
			addDP = addDP.substring(0, addDP.length()-1);
			String[] array = addDP.split(",");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("paramids", array);
			map.put("devicetypeid", devicetypeid);
			dataCenterDao.addDeviceTypeParam(map);
		}
		if(!delDP1.equals("")){
			delDP1 = delDP1.substring(0, delDP1.length()-1);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("connectid", delDP1);
			map.put("isused", 1);
			dataCenterDao.delDeviceTypeParam(map);
		}
		if(!delDP0.equals("")){
			delDP0 = delDP0.substring(0, delDP0.length()-1);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("connectid", delDP0);
			map.put("isused", 0);
			dataCenterDao.delDeviceTypeParam(map);
		}
	}
	
	//获取地理位置信息
	public Map<String, Object> queryobjdt(Map<String, Object> map){
		Map<String, Object> map2 = dataCenterDao.querySpace(map);
		if(!map2.containsKey("spacelayername")){
			return null;
		}
		map.put("tablename", map2.get("spacelayername").toString());
		return dataCenterDao.queryobjdt(map);
	}
	//修改地理位置
	public void updateobjdt(Map<String, Object> map){
		Map<String, Object> map2 = dataCenterDao.querySpace(map);
		if(map2.containsKey("spacelayername")){
			map.put("tablename", map2.get("spacelayername"));
			map.put("spacetypename", map2.get("spacetypename"));
			if(("面").equals(map2.get("spacetypename").toString())){
				String[] shape = (map.get("shape").toString()).split(",");
					if(shape.length>0){
						map.put("shape", map.get("shape").toString()+","+shape[0]);
					}
			}
			Map<String, Object> dtmap = dataCenterDao.queryobjdt(map);
			if(dtmap==null||dtmap.size()==0){
				//add
				dataCenterDao.addobjdt(map);
			}else{
				//update
				dataCenterDao.updateobjdt(map);
			}
		}
		
	}
	//对象管理   对象列表查询
	public List<Map<String, Object>> queryObj(Map<String, Object> map){
		return dataCenterDao.queryData("queryObj", map);
	}
	//对象管理   添加
	public void addObj(Map<String, Object> map){
		Map<String, Object> map2 = dataCenterDao.querySpace(map);
		map.put("tablename", map2.get("spacelayername").toString());
		map.put("spacetypename", map2.get("spacetypename").toString());
		if(("面").equals(map2.get("spacetypename").toString())){
			String[] shape = (map.get("shape").toString()).split(",");
				if(shape.length>0){
					map.put("shape", map.get("shape").toString()+","+shape[0]);
				}
		}
		dataCenterDao.addObj(map);
	}
	//对象管理 基本信息维护--修改
	public void updateInfostore(Map<String, Object> map){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String objid = map.get("objid").toString();
		map.remove("objid");
		for(String k:map.keySet()){
			Map<String, Object> iMap = new HashMap<String, Object>();
			iMap.put("fieldname", k);
			iMap.put("fieldvalue", map.get(k));
			iMap.put("objid", objid);
			list.add(iMap);
		}
		dataCenterDao.updateInfostore(list);
	}
	//对象管理  子表信息维护--修改
	public void updateZBInfostore(Map<String, Object> map){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String objid = map.get("objid").toString();
		String tableid = map.get("tableid").toString();
		map.remove("objid");
		map.remove("tableid");
		for(String k:map.keySet()){
			Map<String, Object> iMap = new HashMap<String, Object>();
			iMap.put("fieldname", k);
			iMap.put("fieldvalue", map.get(k));
			iMap.put("objid", objid);
			iMap.put("tableid", tableid);
			list.add(iMap);
		}
		dataCenterDao.updateZBInfostore(list);
	}
	
	//对象管理 基本信息维护--添加
	public void insertInfostore(Map<String, Object> map){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String objid = map.get("objid").toString();
		map.remove("objid");
		for(String k:map.keySet()){
			Map<String, Object> iMap = new HashMap<String, Object>();
			iMap.put("fieldname", k);
			iMap.put("fieldvalue", map.get(k));
			iMap.put("objid", objid);
			list.add(iMap);
		}
		dataCenterDao.insertInfostore(list);
	}
	//对象管理 基本信息维护--添加
	public void insertZBInfostore(Map<String, Object> map){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String objid = map.get("objid").toString();
		String tableid = map.get("tableid").toString();
		map.remove("objid");
		map.remove("tableid");
		for(String k:map.keySet()){
			Map<String, Object> iMap = new HashMap<String, Object>();
			iMap.put("fieldname", k);
			iMap.put("fieldvalue", map.get(k));
			iMap.put("objid", objid);
			iMap.put("tableid", tableid);
			list.add(iMap);
		}
		dataCenterDao.insertZBInfostore(list);
	}
	//对象管理 基本表字段查询
	public List<Map<String, Object>> queryFieldName(String objid){
		return dataCenterDao.queryFieldName(objid);
	}
	//对象管理 查询对象子表字段
	public List<Map<String, Object>> queryZBFieldName(Map<String,Object> map){
		return dataCenterDao.queryData("queryZBFieldName", map);
	}
	//对象管理 删除或恢复
	public void delObj(Map<String, Object> map){
		dataCenterDao.delObj(map);
	}
	//对象管理  对象基本信息查询
	public List<Map<String,Object>> queryInformation(Map<String, Object> map){
		return dataCenterDao.queryData("queryInformation", map);
	}
	//对象管理  对象基本信息查询   修改时查询
	public List<Map<String,Object>> queryInformation2(Map<String, Object> map){
		return dataCenterDao.queryData("queryInformation2", map);
	}
	//查询对应字段信息
	public List<Map<String,Object>> queryFieldshowname(Map<String, Object> map){
		return dataCenterDao.queryData("queryFieldshowname", map);
	}
	
	//触发器保存实时数据
	public void addwzData(Map<String, Object> map){
		map.put("datavalue", map.get("datavalue").toString());
		List<Map<String, Object>> list = dataCenterDao.querywzDatas(map);
		if(list!=null&&list.size()==0){
			dataCenterDao.addwzData(map);
		}else{
			System.out.println("数据已存在");
		}
	}
	
	//分包
	public List<byte[]> byteList(String filename) throws Exception{
		File file = new File(filename);
        InputStream in = new FileInputStream(file);
		int sum = in.available();//共有多少字节
		System.out.println("文件总字节数："+sum);
        in = new FileInputStream(file);  
        int tempbyte;
        byte[] b = new byte[sum];
        int aa = 0;
		while ((tempbyte = in.read()) != -1) {
        	b[aa++] = (byte)tempbyte;
        }
		in.close();
		List<byte[]> list = new ArrayList<byte[]>();
		int c = 0;
		int bytelength = 512;
		byte[] by = new byte[bytelength];
		for(int i=0;i<b.length;i++){
			by[c++] = b[i];
			if(c==bytelength||i==b.length-1){
//				System.out.println("================"+c+"============"+i);
				c = 0;
				list.add(by);
				if(b.length-i<bytelength){
					bytelength = b.length-i;
				}
				by = new byte[bytelength];
			}
		}
		return list;
	}
	
	//实时数据查询
	public List<Map<String, Object>> queryActualData(Map<String, Object> map){
		return dataCenterDao.queryData("queryActualData", map); 
	}
	//触发器修改实时数据
	public void updateTriggerData(Map<String, Object> map){
		dataCenterDao.updateTriggerData(map);
	}
	
	//获取指定时间OneNET某参数的平均值
	public Double getAvg(Map<String, Object> map){
		String datastreamids = map.get("datastreamids").toString();//必须值
		String devid = map.get("devicenumber").toString();//必须值
		String key = map.get("rmk1").toString();//必须值
		String start = map.get("start").toString();//必须值
		String end = map.get("end").toString();//必须值
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list = GetDatapointsApi(datastreamids,devid,key,start,end,null,list);
//		System.out.println(list.size());
		double sum = 0;
		if(list.size()>0){
			for(int i=0;i<list.size();i++){
				sum+=Double.parseDouble(list.get(i).get("value").toString());
			}
//			System.out.println(datastreamids+":sum:"+sum);
			return sum/list.size();
		}else{
//			System.out.println("OneNet无数据");
			return null;
		}
	}
	
	public List<Map<String, Object>> GetDatapointsApi(String datastreamids,String devid,String key,String start ,String end,String cursor,List<Map<String, Object>> list) {
		GetDatapointsListApi api = new GetDatapointsListApi(datastreamids, start, end, devid, null, 500, cursor, null,
				null, null, null, key);
		BasicResponse<DatapointsList> response = api.executeApi();
		JSONObject json = JSONObject.fromObject(response.getJson()).getJSONObject("data");
//		System.out.println(json.toString());
		int count = json.getInt("count");
		if(count>0){
			JSONArray datapoints = ((json.getJSONArray("datastreams")).getJSONObject(0)).getJSONArray("datapoints");
			list.addAll(datapoints);
		}
		if(json.has("cursor")){
			cursor = json.getString("cursor")==null?"":json.getString("cursor");
			return GetDatapointsApi(datastreamids,devid,key,start,end,cursor,list);
		}else{
			return list;
		}
		
	}
	
	/**
	 * 计算AirBean中的平均值
	 */
	public AirBean getAirBean(AirBean airBean,String devicenumber,String rmk1) throws Exception{
		int objid = airBean.getObjid();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		List<Map<String, Object>> list = queryObjDevice(map);
		if(list.size()==0||("").equals(list.get(0).get("devicenumber"))||("").equals(list.get(0).get("rmk1"))){
			return null;//站点未绑定设备或者未绑定onenet无法计算
		}
		map.put("devicenumber", devicenumber);//设备编号
		map.put("rmk1", rmk1);//设备key
		if(airBean.getType()==0){
			//0 小时数据
			String collecttime = airBean.getCollecttime();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat OneNETformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String end = OneNETformat.format(format.parse(collecttime));
			//一小时
			String start = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000))));
			map.put("start", start);
			map.put("end", end);
			map.put("datastreamids", "so2");
			airBean.setSo2(getAvg(map));
			map.put("datastreamids", "no2");
			airBean.setNo2(getAvg(map));
			map.put("datastreamids", "pm10");
			airBean.setPm10(getAvg(map));
			map.put("datastreamids", "pm25");
			airBean.setPm25(getAvg(map));
			map.put("datastreamids", "co");
			airBean.setCo(getAvg(map));
			map.put("datastreamids", "o3");
			airBean.setO3(getAvg(map));//o3一小时平均值
			airBean.setO38(getO38(map,collecttime,8));//o38  8小时内滑动平均值
			//24小时
			start = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000*24))));
			end = OneNETformat.format(format.parse(collecttime));
			map.put("start", start);
			map.put("end", end);
			map.put("datastreamids", "pm10");
			airBean.setPm10_24h(getAvg(map));
			map.put("datastreamids", "pm25");
			airBean.setPm25_24h(getAvg(map));
			if(airBean.getSo2()==null&&airBean.getNo2()==null&&airBean.getPm10()==null&&airBean.getPm25()==null&&airBean.getCo()==null&&airBean.getO3()==null){
				return null;
			}else{
				if(airBean.getSo2()==null){ airBean.setSo2(0D); }
				if(airBean.getNo2()==null){ airBean.setNo2(0D);}
				if(airBean.getPm10()==null){airBean.setPm10(0D);}
				if(airBean.getPm25()==null){airBean.setPm25(0D);}
				if(airBean.getCo()==null){ airBean.setCo(0D);}
				if(airBean.getO3()==null){airBean.setO3(0D);}
			}
		}else if(airBean.getType()==1){
			//1 日数据(24小时)
			String collecttime = airBean.getCollecttime();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat OneNETformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String end = OneNETformat.format(format.parse(collecttime));
			//24小时
			String start = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000*24))));
			map.put("start", start);
			map.put("end", end);
			map.put("datastreamids", "so2");
			airBean.setSo2_24h(getAvg(map));
			map.put("datastreamids", "no2");
			airBean.setNo2_24h(getAvg(map));
			map.put("datastreamids", "pm10");
			airBean.setPm10_24h(getAvg(map));
			map.put("datastreamids", "pm25");
			airBean.setPm25_24h(getAvg(map));
			map.put("datastreamids", "co");
			airBean.setCo_24h(getAvg(map));
			
			
			map.put("datastreamids", "o3");
			airBean.setO3_24h(getO38(map,collecttime,24,0));//o3      要求：最大的一小时平均值
			airBean.setO38_24h(getO38(map,collecttime,24,1));//o38  要求：每天的最大8小时滑动平均值
			if(airBean.getSo2_24h()==null&&airBean.getNo2_24h()==null&&airBean.getPm10_24h()==null&&airBean.getPm25_24h()==null&&airBean.getCo_24h()==null){
				return null;
			}else{
				if(airBean.getSo2_24h()==null){ airBean.setSo2_24h(0D); }
				if(airBean.getNo2_24h()==null){ airBean.setNo2_24h(0D);}
				if(airBean.getPm10_24h()==null){airBean.setPm10_24h(0D);}
				if(airBean.getPm25_24h()==null){airBean.setPm25_24h(0D);}
				if(airBean.getCo_24h()==null){ airBean.setCo_24h(0D);}
				if(airBean.getO3_24h()==null){airBean.setO3_24h(0D);}
				if(airBean.getO38_24h()==null){airBean.setO38_24h(0D);}
			}
		}
		return airBean;
	}
	
	//o38平均滑动值（1小时）
	public Double getO38(Map<String, Object> map,String collecttime,int hours) throws Exception{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat OneNETformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Double sumo38 = 0D;
		int shijiCount = 0;
		for (int i = 1; i <= hours; i++) {
			String start = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000*i))));
			String end = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000*(i-1)))));
			map.put("datastreamids", "o3");
			map.put("start", start);
			map.put("end", end);
			Double xiaoshiAvg= getAvg(map);
			if(xiaoshiAvg!=null)
			if(xiaoshiAvg!=0){
				sumo38 +=xiaoshiAvg;
				shijiCount++;
			}
		}
		if(shijiCount==0){
			return 0D;
		}
		return sumo38/shijiCount;
	}
	
	//o38平均滑动值（24小时）resulttype为1返回的是o3最大一小时数据，为2返回的是o38最大8小时的滑动平均值
	public Double getO38(Map<String, Object> map,String collecttime,int hours,int resulttype) throws Exception{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat OneNETformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Double[] sumo38 = new Double[24];
		for(int i=0;i<24;i++){
			sumo38[i] = 0D;
		}
		int shijiCount = 0;
		for (int i = 1; i <= hours; i++) {
			String start = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000*i))));
			String end = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000*(i-1)))));
			map.put("datastreamids", "o3");
			map.put("start", start);
			map.put("end", end);
			Double xiaoshiAvg= getAvg(map);
			
			if(xiaoshiAvg!=null){
				sumo38[i-1] = xiaoshiAvg;
				shijiCount++;
			}
		}
		//冒泡排序
		for(int i=0;i<sumo38.length-1;i++){
			for(int j=0;j<sumo38.length-1-i;j++){
				if(sumo38[j]<sumo38[j+1]){
					Double temp=sumo38[j];
					sumo38[j]=sumo38[j+1];
					sumo38[j+1]=temp;
				}
			}
		}
		if(resulttype==0){
			return sumo38[0];
		}else{
			Double sum8 = 0D;
			for(int i=0;i<8;i++){
				sum8 += sumo38[i];
			}
			if(shijiCount<8){
				return sum8/shijiCount;
			}else{
				return sum8/8;
			}
			
		}
	}
	
	//查询对象绑定的参数（含过滤）
	public List<Map<String, Object>> getobjParam(String objid){
		String [] obj = objid.split(",");
		if(obj.length==1){
			return dataCenterDao.getobjParam(objid);
		}else{
			List<Map<String, Object>> list1 = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> list2 = new ArrayList<Map<String,Object>>();
			for(int i=0;i<obj.length;i++){
				list1 = dataCenterDao.getobjParam(obj[i]);
				if(list2.size()==0){
					list2 = list1;
				}else{
					list2 = getIntersection2(list1,list2);
				}
			}
			return list2;
		}
		
	}
	//求list交集
	public <T> List<T> getIntersection2(List<T> list1, List<T> list2){
        List<T> list = new ArrayList<T>();
        Map<T, Boolean> map = new HashMap<T, Boolean>();
        for (T t : list1) {
            map.put(t, false);
        }
        for (T t : list2) {
            if (map.keySet().contains(t)) {
                map.put(t, true);
            }
        }
        for (T t : map.keySet()) {
            if (map.get(t) == true) {
                list.add(t);
            }
        }
        return list;
    }
	
	//添加阈值
	public void addThreshold(String objid,JSONArray json){
		String [] obj = objid.split(",");
		for(String o:obj){
			for(int i=0;i<json.size();i++){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("objid", o);
				map.put("paramid", json.getJSONObject(i).get("paramid"));
				map.put("val", json.getJSONObject(i).get("val"));
				dataCenterDao.addThreshold(map);
			}
		}
	}
	//报警阈值查询
	public List<Map<String,Object>> queryThreshold(Map<String,Object> map){
		return dataCenterDao.queryThreshold(map);
	}
	//删除报警阈值
	public void delThreshold(String thresholdid){
		dataCenterDao.delThreshold(thresholdid);
	}
	//修改报警阈值
	public void updateThreshold(Map<String,Object> map){
		dataCenterDao.updateThreshold(map);
	}
	
	//对象管理  子表下拉查询
	public List<Map<String,Object>> queryObjZB(String objtypeid){
		return dataCenterDao.queryObjZB(objtypeid);
	}
	//对象管理  子表信息查询
	public List<Map<String,Object>> queryZBInformation(Map<String,Object> map){
		return dataCenterDao.queryData("queryZBInformation", map);
	}
	
	//阈值报警判断
	public void isThreshold(AirBean airBean){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", airBean.getObjid());
		List<Map<String, Object>> list = queryThreshold(map);
		for(Map<String, Object> m:list){
			String paramname = m.get("paramname").toString();
			Double val = Double.parseDouble(m.get("val").toString());
			map.put("collecttime", airBean.getCollecttime());
			map.put("paramid", m.get("paramid"));
			map.put("thresholdval", val);
			boolean iscb = false;
			if(paramname.equals("so2")){
				Double so2 = airBean.getSo2();
				if(so2>val){
					iscb = true;
					map.put("value", so2);
					map.put("describe", m.get("objname").toString()+"当前空气质量"+airBean.getAqilevel()+"类，超标参数so2");
				}
			}
			if(paramname.equals("no2")){
				Double no2 = airBean.getNo2();
				if(no2>val){
					iscb = true;
					map.put("value", no2);
					map.put("describe", m.get("objname").toString()+"当前空气质量"+airBean.getAqilevel()+"类，超标参数no2");
				}
			}
			if(paramname.equals("pm10")){
				Double pm10 = airBean.getPm10();
				if(pm10>val){
					iscb = true;
					map.put("value", pm10);
					map.put("describe", m.get("objname").toString()+"当前空气质量"+airBean.getAqilevel()+"类，超标参数pm10");
				}
			}
			if(paramname.equals("pm25")){
				Double pm25 = airBean.getPm25();
				if(pm25>val){
					iscb = true;
					map.put("value", pm25);
					map.put("describe", m.get("objname").toString()+"当前空气质量"+airBean.getAqilevel()+"类，超标参数pm25");
				}
			}
			if(paramname.equals("co")){
				Double co = airBean.getCo();
				if(co>val){
					iscb = true;
					map.put("value", co);
					map.put("describe", m.get("objname").toString()+"当前空气质量"+airBean.getAqilevel()+"类，超标参数co");
				}
			}
			if(paramname.equals("o3")){
				Double o3 = airBean.getO3();
				if(o3>val){
					iscb = true;
					map.put("value", o3);
					map.put("describe", m.get("objname").toString()+"当前空气质量"+airBean.getAqilevel()+"类，超标参数o3");
				}
			}
			if(iscb){
				dataCenterDao.addthrinfo(map);
			}
		}
	}
	
	
	//报警  查询
	public List<Map<String,Object>> queryThr(Map<String,Object> map){
		return dataCenterDao.queryThr(map);
	}
	
	//站点管理添加
	public void addObj2Obj(Map<String,Object> map){
		dataCenterDao.addObj2Obj(map);
	}
	//站点管理查询
	public List<Map<String, Object>> queryObj2Obj(Map<String,Object> map){
		return dataCenterDao.queryData("queryObj2Obj", map);
	}
	//站点管理 删除
	public void delObj2Obj(Map<String,Object> map){
		dataCenterDao.delObj2Obj(map);
	}
	//查询目录id
	public Map<String,Object> querycontrol(Map<String,Object> map){
		return dataCenterDao.querycontrol(map);
	}
	//目录添加
	public void addcontrol(Map<String,Object> map){
		dataCenterDao.addcontrol(map);
	}
	//目录删除
	public void delcontrol(Map<String,Object> map){
		dataCenterDao.delcontrol(map);
	}
	//目录结构查询
	public List<Map<String,Object>> queryMenujg(){
		return dataCenterDao.queryMenujg();
	}
	
	public List<OmMenu> getMenuList(String dirtypeid) {
		//原始数据
		List<OmMenu> rootlist = dataCenterDao.getMenuList(dirtypeid);
		//创建list，存放最后的结果
		List<OmMenu> menuList = new ArrayList<OmMenu>();
		
		if (rootlist.size() > 0) {
			//先找到所有的一级目录
			for (OmMenu menu : rootlist) {
				//一级目录的父级目录id为'0'
				if ("0".equals(menu.getParentid())) {
					menuList.add(menu);
				}
			}
			
			//为一级目录设置子目录,getChild是递归调用的
			for (OmMenu menu : menuList) {
				menu.setChildList(getChild(String.valueOf(menu.getId()),rootlist));
			}
		}
		
		return menuList;
	}
	
	/**
	 * 递归查找子菜单
	 * @param id 当前目录id
	 * @param rootMenu 要查找的列表
	 * @return childList
	 */
	private List<OmMenu> getChild(String id, List<OmMenu> rootMenu) {
	    //子目录
	    List<OmMenu> childList = new ArrayList<OmMenu>();
	    for (OmMenu menu : rootMenu) {
	        // 遍历所有节点，将父目录id与传过来的id比较
	        if (!("0".equals(menu.getParentid()))) {
	            if (menu.getParentid().equals(id)) {
	                childList.add(menu);
	            }
	        }
	    }
	    // 把子目录的子目录再循环一遍
	    for (OmMenu menu : childList) {
            // 递归
            menu.setChildList(getChild(String.valueOf(menu.getId()), rootMenu));
	    } 
	    // 递归退出条件
	    if (childList.size() == 0) {
	        return new ArrayList<OmMenu>();
	    }
	    return childList;
	}
	
	//添加目录结构
	public void addMenujg (Map<String,Object> map){
		dataCenterDao.addMenujg(map);
	}
	//依据目录等级和结构查询目录
	public List<Map<String,Object>> queryMenuBylevel(Map<String,Object> map){
		return dataCenterDao.queryMenuBylevel(map);
	}
	//目录结构删除
	public void delMenujg(Map<String,Object> map){
		dataCenterDao.delMenujg(map);
	}
	//目录名称修改
	public void updateMenuName(Map<String,Object> map){
		dataCenterDao.updateMenuName(map);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//=========================对象类型表管理和字段管理================================
	
	/**
	 * 获取对象类型的信息表列表/查询功能
	 * @param objtypeid 对象类型ID
	 * @param isused 是否删除
	 * @param tableshowname 表名称
	 * @param tabletype 表类型
	 */
	public List<Tableinfoadd> getTableListService(Map<String, Object> map){
		return dataCenterDao.getTableList(map);
	}
	
	/**
	 * 对象类型表新增
	 * @param table Tableinfoadd对象
	 */
	public int tableinfoaddService(Tableinfoadd table){
		return dataCenterDao.tableinfoadd(table);
	}
	
	/**
	 * 对象类型表修改
	 * @param table Tableinfoadd对象
	 */
	public int tableinfoupdateService(Tableinfoadd table){
		return dataCenterDao.tableinfoupdate(table);
	}
	
	/**
	 * 对象类型表批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param tableid 表id
	 */
	public int tableBatchRemoveAndRecoverService(Map<String, Object> map){
		return dataCenterDao.tableBatchRemoveAndRecover(map);
	}
	
	/**
	 * 查看表的字段信息
	 * @param tableid 表信息id
	 * @param isused 是否删除
	 * @param fieldname 字段名称
	 * @param fieldshowname 字段显示名称
	 */
	public List<Tableinfo> getFieldinfoListService(Map<String, Object> map){
		return dataCenterDao.getFieldinfoList(map);
	}
	
	/**
	 * 表字段信息新增
	 * @param table Tableinfo对象
	 */
	public int fieldinfoaddService(Tableinfo table){
		return dataCenterDao.fieldinfoadd(table);
	}
	
	/**
	 * 表字段信息修改
	 * @param table Tableinfo对象
	 */
	public int fieldinfoupdateService(Tableinfo table){
		return dataCenterDao.fieldinfoupdate(table);
	}
	
	/**
	 * 表字段排序
	 * @param fieldid 字段id
	 * @param orderintable 排序值
	 * @param order 排序（降序/升序）
	 */
	public int fieldorderbyService(Map<String, Object> map){
		return dataCenterDao.fieldorderby(map);
	}
	
	/**
	 * 字段批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param fieldid 字段id
	 */
	public int fieldBatchRemoveAndRecoverService(Map<String, Object> map){
		return dataCenterDao.fieldBatchRemoveAndRecover(map);
	}

	public int updateDir(Map<String, Object> map2) {
		return dataCenterDao.updateDir(map2);
	}

	
}
