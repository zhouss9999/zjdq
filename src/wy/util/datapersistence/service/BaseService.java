package wy.util.datapersistence.service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.util.datapersistence.AttachSubinfoStore;
import wy.util.datapersistence.ModelAssist;
import wy.util.datapersistence.SpaceEntuty;
import wy.util.datapersistence.SpaceInfo;
import wy.util.datapersistence.Dao.BaseaddDao;
@SuppressWarnings("all")

@Component
public class BaseService {
	@Autowired
	private BaseaddDao baseaddDao;
	
	@Autowired
	private SiteDataDao siteDataDao;
	
	/**基本信息查询
	该方法针对 cp_attach_info数据 多行转单行操作
	paramMap中objtypeid(对象类型)必须的，sql语句如果没有，传字符 如：sql=""*/
	public List<Map<String,Object>> selectForCpAttachInfoStoreTwo(
			Map<String, Object> paramMap,String sql) {
		List<ModelAssist> modelList = baseaddDao.selectForCpAttachInfoStore(paramMap, sql);
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
		Map<String, String> hm = new LinkedHashMap<String, String>();
		for (int i = 0; i < modelList.size(); i++) {

			ModelAssist epzt = (ModelAssist) modelList.get(i);
			hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
			hm.put("objid", String.valueOf(epzt.getObjid()));
			if (i != modelList.size() - 1) {
				ModelAssist epzttwo = (ModelAssist) modelList.get(i + 1);
				// 如下if else 是针对“列转行”的数据操作
				if (!epzt.getObjid().equals(epzttwo.getObjid())) {
					// 组装的数据Bean返回给前端
					Map<String,Object> datas = new LinkedHashMap<String,Object>();
					
					datas.put("objid", Integer.valueOf(hm.get("objid")));
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objid"))
						datas.put(key, value);
					}
					
					dataList.add(datas);
					hm.clear();
				} else {
					// 循环到最后“一条”数据时，需要再次保存该数据，如上if条件无法保存;或者时只有一条数据的情况
					if (i >= (modelList.size() - 2)) {
						Map<String,Object> datas = new LinkedHashMap<String,Object>();

						datas.put("objid", Integer.valueOf(hm.get("objid")));
						Iterator hmIterator = hm.entrySet().iterator();
						while(hmIterator.hasNext()){
							Entry entry = (Entry) hmIterator.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							if(!key.equals("objid"))
							datas.put(key, value);
						}
						
						dataList.add(datas);
					}
				}

			} else {// 封装最后“一行”数据
				hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
				// 组装的数据Bean返回给前端
				Map<String,Object> datas = new LinkedHashMap<String,Object>();

				datas.put("objid", Integer.valueOf(hm.get("objid")));
				Iterator hmIterator = hm.entrySet().iterator();
				while(hmIterator.hasNext()){
					Entry entry = (Entry) hmIterator.next();
					String key = entry.getKey().toString();
					String value = entry.getValue().toString();
					if(!key.equals("objid"))
					datas.put(key, value);
				}
				
				
				// 判断modelList只有一行数据，封装最后一行数据会出现数组越界异常，因此dataList做添加操作，反之做删除dataList最后一行空数据再进行添加的操作
				if(dataList.size() <= 0){
					dataList.add(datas);
				}else{
					dataList.remove(dataList.size()-1);
					dataList.add(datas);
				}
			}

		}
		return dataList;
	}
	
	/**详情查询
	 * objid 对象objid
	*/
	public List<Map<String,Object>> selectDetailForCpAttachInfoStoreTwo(
			int objid) {
		List<ModelAssist> modelList = baseaddDao.selectForCpAttachInfoStoreDetail(objid);
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
		Map<String, String> hm = new LinkedHashMap<String, String>();
		Map<String,Object> datas = new LinkedHashMap<String,Object>();
		for (int i = 0; i < modelList.size(); i++) {

			ModelAssist epzt = (ModelAssist) modelList.get(i);
			hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
			hm.put("objid", String.valueOf(epzt.getObjid()));
		
		}
		if(hm.size()>0){
			datas.put("objid", Integer.valueOf(hm.get("objid")));
			Iterator hmIterator = hm.entrySet().iterator();
			while(hmIterator.hasNext()){
				Entry entry = (Entry) hmIterator.next();
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				if(!key.equals("objid"))
				datas.put(key, value);
			}
			
			dataList.add(datas);
		}
		
		return dataList;
	}
	
	
	/**子表信息查询
	 * 该方法针对 cp_attach_subinfo数据 多行转单行操作
	 * objid对象objid 必须的，sql语句如果没有，传字符 如：sql=""
	*/
	public List<Map<String,Object>> selectForCpAttachSubinfoStoreTwo(
			int objid,String sql) {
		List<AttachSubinfoStore> modelList = baseaddDao
				.selectForCpAttachSubinfoStore(objid,sql);
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
		Map<String, String> hm = new LinkedHashMap<String, String>();
		for (int i = 0; i < modelList.size(); i++) {

			AttachSubinfoStore epzt = (AttachSubinfoStore) modelList.get(i);
			hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
			hm.put("objid", String.valueOf(epzt.getObjid()));
			hm.put("valueid", String.valueOf(epzt.getValueid()));
			if (i != modelList.size() - 1) {
				AttachSubinfoStore epzttwo = (AttachSubinfoStore) modelList.get(i + 1);
				// 如下if else 是针对“列转行”的数据操作
				if (!epzt.getValueid().equals(epzttwo.getValueid())) {
					// 组装的数据Bean返回给前端
					Map<String,Object> datas = new LinkedHashMap<String,Object>();

					datas.put("objid", Integer.valueOf(hm.get("objid")));
					datas.put("valueid", hm.get("valueid"));
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objid") || !key.equals("valueid"))
						datas.put(key, value);
					}
					
					dataList.add(datas);
					hm.clear();
				} else {
					// 循环到最后“一条”数据时，需要再次保存该数据，如上if条件无法保存;或者时只有一条数据的情况
					if (i >= (modelList.size() - 2)) {
						Map<String,Object> datas = new LinkedHashMap<String,Object>();
						
						datas.put("objid", Integer.valueOf(hm.get("objid")));
						Iterator hmIterator = hm.entrySet().iterator();
						while(hmIterator.hasNext()){
							Entry entry = (Entry) hmIterator.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							if(!key.equals("objid") || !key.equals("valueid"))
							datas.put(key, value);
						}
						
						
						dataList.add(datas);
					}
				}

			} else {// 封装最后“一行”数据
				hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
				// 组装的数据Bean返回给前端
				Map<String,Object> datas = new LinkedHashMap<String,Object>();

				datas.put("objid", Integer.valueOf(hm.get("objid")));
				Iterator hmIterator = hm.entrySet().iterator();
				while(hmIterator.hasNext()){
					Entry entry = (Entry) hmIterator.next();
					String key = entry.getKey().toString();
					String value = entry.getValue().toString();
					if(!key.equals("objid") || !key.equals("valueid"))
					datas.put(key, value);
				}
				
				// 判断modelList只有一行数据，封装最后一行数据会出现数组越界异常，因此dataList做添加操作，反之做删除dataList最后一行空数据再进行添加的操作
				if(dataList.size() <= 0){
					dataList.add(datas);
				}else{
					dataList.remove(dataList.size()-1);
					dataList.add(datas);
				}
			}

		}
		return dataList;
	}
	
	/**
	 * 获取对象名称列表*/
	public List<String> selectObjname(int objtypeid) {
		return baseaddDao.selectObjname(objtypeid);
	}
	
	/**
	 * 附件上传*/
	public int attachUpload(HttpServletRequest request) {
		//添加顺序 文件名称，文件地址，上传人，上传时间
		String objid = request.getAttribute("objid").toString();
		String filePath = request.getParameter("attachfilePath");
		String fieldId = request.getParameter("attachfieldId");
		String uploadman = request.getParameter("attachuploadman");
		String uploadtime = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		int resultNum = 0;
		List<AttachSubinfoStore> paramList = new ArrayList<AttachSubinfoStore>();
		
		if(filePath.contains(",")){
			String[] filePathArr = filePath.split(",");
			for(int i=0;i<filePathArr.length;i++){
				String valueid = baseaddDao.selectMaxValueid(Integer.valueOf(objid));//子表中valueid
				String filePathStr = filePathArr[i];
				if(filePathStr.contains("&")){
					String[] files = filePathStr.split("&");
					String filename = files[0];
					String filepath = files[1];
					
					AttachSubinfoStore attachSubinfoStore = new AttachSubinfoStore();
					attachSubinfoStore.setObjid(Integer.valueOf(objid));
					attachSubinfoStore.setFieldid(Long.valueOf(fieldId.split(",")[0]));
					attachSubinfoStore.setFieldvalue(filename);
					attachSubinfoStore.setValueid(valueid);
					paramList.add(attachSubinfoStore);
					
					AttachSubinfoStore attachSubinfoStore1 = new AttachSubinfoStore();
					attachSubinfoStore1.setObjid(Integer.valueOf(objid));
					attachSubinfoStore1.setFieldid(Long.valueOf(fieldId.split(",")[1]));
					attachSubinfoStore1.setFieldvalue(filepath);
					attachSubinfoStore1.setValueid(valueid);
					paramList.add(attachSubinfoStore1);

					AttachSubinfoStore attachSubinfoStore2 = new AttachSubinfoStore();
					attachSubinfoStore2.setObjid(Integer.valueOf(objid));
					attachSubinfoStore2.setFieldid(Long.valueOf(fieldId.split(",")[2]));
					attachSubinfoStore2.setFieldvalue(uploadman);
					attachSubinfoStore2.setValueid(valueid);
					paramList.add(attachSubinfoStore2);
					
					AttachSubinfoStore attachSubinfoStore3 = new AttachSubinfoStore();
					attachSubinfoStore3.setObjid(Integer.valueOf(objid));
					attachSubinfoStore3.setFieldid(Long.valueOf(fieldId.split(",")[3]));
					attachSubinfoStore3.setFieldvalue(uploadtime);
					attachSubinfoStore3.setValueid(valueid);
					paramList.add(attachSubinfoStore3);
					
					resultNum+=baseaddDao.addDataToCpAttachSubinfoStore(paramList);
					paramList.clear();
				}
			}
		}else{
			if(filePath.contains("&")){
				String[] files = filePath.split("&");
				String filename = files[0];
				String filepath = files[1];
				
				String valueid = baseaddDao.selectMaxValueid(Integer.valueOf(objid));//子表中valueid
				AttachSubinfoStore attachSubinfoStore = new AttachSubinfoStore();
				attachSubinfoStore.setObjid(Integer.valueOf(objid));
				attachSubinfoStore.setFieldid(Long.valueOf(fieldId.split(",")[0]));
				attachSubinfoStore.setFieldvalue(filename);
				attachSubinfoStore.setValueid(valueid);
				paramList.add(attachSubinfoStore);
				
				AttachSubinfoStore attachSubinfoStore1 = new AttachSubinfoStore();
				attachSubinfoStore1.setObjid(Integer.valueOf(objid));
				attachSubinfoStore1.setFieldid(Long.valueOf(fieldId.split(",")[1]));
				attachSubinfoStore1.setFieldvalue(filepath);
				attachSubinfoStore1.setValueid(valueid);
				paramList.add(attachSubinfoStore1);

				AttachSubinfoStore attachSubinfoStore2 = new AttachSubinfoStore();
				attachSubinfoStore2.setObjid(Integer.valueOf(objid));
				attachSubinfoStore2.setFieldid(Long.valueOf(fieldId.split(",")[2]));
				attachSubinfoStore2.setFieldvalue(uploadman);
				attachSubinfoStore2.setValueid(valueid);
				paramList.add(attachSubinfoStore2);
				
				AttachSubinfoStore attachSubinfoStore3 = new AttachSubinfoStore();
				attachSubinfoStore3.setObjid(Integer.valueOf(objid));
				attachSubinfoStore3.setFieldid(Long.valueOf(fieldId.split(",")[3]));
				attachSubinfoStore3.setFieldvalue(uploadtime);
				attachSubinfoStore3.setValueid(valueid);
				paramList.add(attachSubinfoStore3);
				
				resultNum += baseaddDao.addDataToCpAttachSubinfoStore(paramList);
				
			}
		}
		return resultNum;
	}

	/**获取空间表对象坐标*/
	public List<SpaceInfo> getGeomList(String space_tablename, String objid) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("space_tablename", space_tablename);
		map.put("objid", objid);
		return baseaddDao.getGeomList(map);
	}
	
	/**子表信息条件查询
	 * 该方法针对 cp_attach_subinfo数据 多行转单行操作
	 * map里面必须有对象objid 必须的，sql语句如果没有，传字符 如：sql=""
	*/
	public List<Map<String,Object>> selectForCpAttachSubinfoStoreTwo(
			Map<String, Object> map,String sql) {
		List<AttachSubinfoStore> modelList = baseaddDao
				.selectForCpAttachSubinfoStore(map,sql);
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
		Map<String, String> hm = new LinkedHashMap<String, String>();
		for (int i = 0; i < modelList.size(); i++) {

			AttachSubinfoStore epzt = (AttachSubinfoStore) modelList.get(i);
			hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
			hm.put("objid", String.valueOf(epzt.getObjid()));
			hm.put("valueid", String.valueOf(epzt.getValueid()));
			if (i != modelList.size() - 1) {
				AttachSubinfoStore epzttwo = (AttachSubinfoStore) modelList.get(i + 1);
				// 如下if else 是针对“列转行”的数据操作
				if (!epzt.getValueid().equals(epzttwo.getValueid())) {
					// 组装的数据Bean返回给前端
					Map<String,Object> datas = new LinkedHashMap<String,Object>();

					datas.put("objid", Integer.valueOf(hm.get("objid")));
					datas.put("valueid", hm.get("valueid"));
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objid") || !key.equals("valueid"))
						datas.put(key, value);
					}
					
					dataList.add(datas);
					hm.clear();
				} else {
					// 循环到最后“一条”数据时，需要再次保存该数据，如上if条件无法保存;或者时只有一条数据的情况
					if (i >= (modelList.size() - 2)) {
						Map<String,Object> datas = new LinkedHashMap<String,Object>();
						
						datas.put("objid", Integer.valueOf(hm.get("objid")));
						Iterator hmIterator = hm.entrySet().iterator();
						while(hmIterator.hasNext()){
							Entry entry = (Entry) hmIterator.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							if(!key.equals("objid") || !key.equals("valueid"))
							datas.put(key, value);
						}
						
						
						dataList.add(datas);
					}
				}

			} else {// 封装最后“一行”数据
				hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
				// 组装的数据Bean返回给前端
				Map<String,Object> datas = new LinkedHashMap<String,Object>();

				datas.put("objid", Integer.valueOf(hm.get("objid")));
				Iterator hmIterator = hm.entrySet().iterator();
				while(hmIterator.hasNext()){
					Entry entry = (Entry) hmIterator.next();
					String key = entry.getKey().toString();
					String value = entry.getValue().toString();
					if(!key.equals("objid") || !key.equals("valueid"))
					datas.put(key, value);
				}
				
				// 判断modelList只有一行数据，封装最后一行数据会出现数组越界异常，因此dataList做添加操作，反之做删除dataList最后一行空数据再进行添加的操作
				if(dataList.size() <= 0){
					dataList.add(datas);
				}else{
					dataList.remove(dataList.size()-1);
					dataList.add(datas);
				}
			}

		}
		return dataList;
	}
	
	
	/**
	 * 获取GeoJson格式数据
	 * @param space_tablename
	 * 		空间表名
	 * @param objids
	 * 		objid,多个用逗号隔开
	 * @param objList
	 * 		properties展现数据列表
	 * @return 
	 * 		GeoJson格式数据
	 */
	public Map<String, Object> getGeoJsonFormat(String space_tablename, String objids, List<?> objList) {
		
		//查询空间数据
		List<SpaceEntuty> geomList = baseaddDao.getSpaceInfo(space_tablename, objids);
		//List转Map,做优化
		Map<Integer, Object> geomMap = new LinkedHashMap<Integer, Object>();
		if (geomList.size() > 0) {
			for (SpaceEntuty geom : geomList) {
				geomMap.put(geom.getObjid(), JSONObject.parse((geom.getGeometry())));
			}
		}
		
		//拼接GeoJson格式数据
		Map<String, Object> featureCollection = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> features = new ArrayList<Map<String,Object>>();
		featureCollection.put("type", "FeatureCollection");
		if (objList.size() > 0) {
			if (objList.get(0) instanceof Map) {//objList的泛型是Map类型
				for (Object obj : objList) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (LinkedHashMap<String, Object>) obj;
					int objid = map.get("objid") == null ? 0 : Integer.parseInt(map.get("objid").toString());
					
					if (geomMap.containsKey(objid)) {
						Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
						featuresMap.put("type", "Feature");
						featuresMap.put("geometry", geomMap.get(objid));
						featuresMap.put("properties", map);
						features.add(featuresMap);
					}
				}
			} else {//objList的泛型是普通对象类型
				try {
					for (Object obj : objList) {
						
						//利用暴力反射获取指定成员变量的值
						Field field = obj.getClass().getDeclaredField("objid");
						field.setAccessible(true);//去除私有权限
						int objid = field.get(obj) == null ? 0 :Integer.parseInt(field.get(obj).toString());
						
						if (geomMap.containsKey(objid)) {
							Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
							featuresMap.put("type", "Feature");
							featuresMap.put("geometry",geomMap.get(objid));
							featuresMap.put("properties", obj);
							features.add(featuresMap);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		featureCollection.put("features", features);
		
		return featureCollection;
	}
	
    
	/**
	 * 获取GeoJson格式数据
	 * @param space_tablename
	 * 		空间表名
	 * @param objids
	 * 		objid,多个用逗号隔开
	 * @param objList
	 * 		properties展现数据列表
	 * @param infoList
	 *       辐射计各站点各指标的状态
	 * @return 
	 * 		GeoJson格式数据       (new2)原有接口上修改的新接口
	 */
	public Map<String, Object> getGeoJsonFormat_fsjzt(String space_tablename, String objids, List<?> objList,List<Map<String,Object>> infoList) {
		
		//查询空间数据
		List<SpaceEntuty> geomList = baseaddDao.getSpaceInfo(space_tablename, objids);
		//List转Map,做优化
		Map<Integer, Object> geomMap = new LinkedHashMap<Integer, Object>();
		if (geomList.size() > 0) {
			for (SpaceEntuty geom : geomList) {
				geomMap.put(geom.getObjid(), JSONObject.parse((geom.getGeometry())));
			}
		}
		
		//拼接GeoJson格式数据
		Map<String, Object> featureCollection = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> features = new ArrayList<Map<String,Object>>();
		featureCollection.put("type", "FeatureCollection");
		if (objList.size() > 0) {
			if (objList.get(0) instanceof Map) {//objList的泛型是Map类型
				for (Object obj : objList) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (LinkedHashMap<String, Object>) obj;
					int objid = map.get("objid") == null ? 0 : Integer.parseInt(map.get("objid").toString());
					//System.out.println("map.objid:"+objid);
					map.put("jsbz", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("wltx", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("wscg", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示 -1
					map.put("hwgc", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示-1
					map.put("jycg", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("wxjs", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示 -1
					map.put("njdb", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示 
					map.put("sqgc", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("wdgc", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("fyzt", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("fwzt", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("hjkz", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示 -1
					map.put("gdzt", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("zzt", "正常");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					if(infoList!=null&&infoList.size()>0){
						for(Map<String,Object>rmap:infoList){//状态数据
							//System.out.println("out rmap.objid:"+rmap.get("objid"));
							if((""+objid).equals(rmap.get("objid")==null?"-1":rmap.get("objid").toString())){
								if(rmap.get("jsbz")!=null){    //接收本振
									if("1".equals(rmap.get("jsbz").toString())){ //
										map.put("jsbz", "异常");	 //接收本振
									}else if("-1".equals(rmap.get("jsbz").toString())){
										map.put("jsbz", "无此项");	 //接收本振
									}
								}
								
								if(rmap.get("wltx")!=null){    //网络通信状态
									if("1".equals(rmap.get("wltx").toString())){ 
										map.put("wltx", "异常");	 
									}else if("-1".equals(rmap.get("wltx").toString())){
										map.put("wltx", "无此项");	 
									}
								}
								
								if(rmap.get("jycg")!=null){    //降雨传感器
									if("1".equals(rmap.get("jycg").toString())){ 
										map.put("jycg", "异常");	 
									}else if("-1".equals(rmap.get("jycg").toString())){
										map.put("jycg", "无此项");	 
									}
								}
								
								
								if(rmap.get("njdb")!=null){    //内建定标源
									if("1".equals(rmap.get("njdb").toString())){ 
										map.put("njdb", "异常");	 
									}else if("-1".equals(rmap.get("njdb").toString())){
										map.put("njdb", "无此项");	 
									}
								}
								
								
								if(rmap.get("sqgc")!=null){    //水汽观测接收机
									if("1".equals(rmap.get("sqgc").toString())){ 
										map.put("sqgc", "异常");	 
									}else if("-1".equals(rmap.get("sqgc").toString())){
										map.put("sqgc", "无此项");	 
									}
								}
								
								if(rmap.get("wdgc")!=null){    //温度观测接收机
									if("1".equals(rmap.get("wdgc").toString())){ 
										map.put("wdgc", "异常");	 
									}else if("-1".equals(rmap.get("wdgc").toString())){
										map.put("wdgc", "无此项");	 
									}
								}
								
								if(rmap.get("fyzt")!=null){    //俯仰转台
									if("1".equals(rmap.get("fyzt").toString())){ 
										map.put("fyzt", "异常");	 
									}else if("-1".equals(rmap.get("fyzt").toString())){
										map.put("fyzt", "无此项");	 
									}
								}
								
								
								if(rmap.get("fwzt")!=null){    //方位转台
									if("1".equals(rmap.get("fwzt").toString())){ 
										map.put("fwzt", "异常");	 
									}else if("-1".equals(rmap.get("fwzt").toString())){
										map.put("fwzt", "无此项");	 
									}
								}
								
								
								if(rmap.get("gdzt")!=null){    //供电状态
									if("1".equals(rmap.get("gdzt").toString())){ 
										map.put("gdzt", "异常");	 
									}else if("-1".equals(rmap.get("gdzt").toString())){
										map.put("gdzt", "无此项");	 
									}
								}
								
								
								if(rmap.get("zzt")!=null){    //总状态
									if("1".equals(rmap.get("zzt").toString())){ 
										map.put("zzt", "异常");	 
									}else if("-1".equals(rmap.get("zzt").toString())){
										map.put("zzt", "无此项");	 
									}
								}
								
								
							
								break;
							}
						}
					}
					
					if (geomMap.containsKey(objid)) {
						Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
						featuresMap.put("type", "Feature");
						featuresMap.put("geometry", geomMap.get(objid));
						featuresMap.put("properties", map);
						features.add(featuresMap);
					}
				}
			} else {//objList的泛型是普通对象类型
				try {
					for (Object obj : objList) {
						
						//利用暴力反射获取指定成员变量的值
						Field field = obj.getClass().getDeclaredField("objid");
						field.setAccessible(true);//去除私有权限
						int objid = field.get(obj) == null ? 0 :Integer.parseInt(field.get(obj).toString());
						
						if (geomMap.containsKey(objid)) {
							Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
							featuresMap.put("type", "Feature");
							featuresMap.put("geometry",geomMap.get(objid));
							featuresMap.put("properties", obj);
							features.add(featuresMap);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		featureCollection.put("features", features);
		
		return featureCollection;
	}
	
	
	
	
	
	
	
	
	/**
	 * 获取GeoJson格式数据
	 * @param space_tablename
	 * 		空间表名
	 * @param objids
	 * 		objid,多个用逗号隔开
	 * @param objList
	 * 		properties展现数据列表
	 *   融合图
	 * @return 
	 * 		GeoJson格式数据       (new2)原有接口上修改的新接口
	 */
	public Map<String, Object> getGeoJsonFormat_fuse(String space_tablename, String objids, List<?> objList) {
		
		//查询空间数据
		List<SpaceEntuty> geomList = baseaddDao.getSpaceInfo(space_tablename, objids);
		//List转Map,做优化
		Map<Integer, Object> geomMap = new LinkedHashMap<Integer, Object>();
		if (geomList.size() > 0) {
			for (SpaceEntuty geom : geomList) {
				geomMap.put(geom.getObjid(), JSONObject.parse((geom.getGeometry())));
			}
		}
		
		//拼接GeoJson格式数据
		Map<String, Object> featureCollection = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> features = new ArrayList<Map<String,Object>>();
		featureCollection.put("type", "FeatureCollection");
		if (objList.size() > 0) {
			if (objList.get(0) instanceof Map) {//objList的泛型是Map类型
				for (Object obj : objList) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (LinkedHashMap<String, Object>) obj;
					int objid = map.get("objid") == null ? 0 : Integer.parseInt(map.get("objid").toString());
					//System.out.println("map.objid:"+objid);
					Map<String,Object> pmap = new HashMap<String,Object>();
					pmap.put("objid", objid);
					String fkxObjid = siteDataDao.queryFkxOidhByFsjOid(pmap);
					if(fkxObjid!=null){  //说明此站点既有风廓线又有辐射计的
						if (geomMap.containsKey(objid)) {
							Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
							featuresMap.put("type", "Feature");
							featuresMap.put("geometry", geomMap.get(objid));
							featuresMap.put("properties", map);
							features.add(featuresMap);
						}
					}
					
				}
			} else {//objList的泛型是普通对象类型
				try {
					for (Object obj : objList) {
						
						//利用暴力反射获取指定成员变量的值
						Field field = obj.getClass().getDeclaredField("objid");
						field.setAccessible(true);//去除私有权限
						int objid = field.get(obj) == null ? 0 :Integer.parseInt(field.get(obj).toString());
						
						
						Map<String,Object> pmap = new HashMap<String,Object>();
						pmap.put("objid", objid);
						String fkxObjid = siteDataDao.queryFkxOidhByFsjOid(pmap);
						if(fkxObjid!=null){  //说明此站点既有风廓线又有辐射计的
						if (geomMap.containsKey(objid)) {
							Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
							featuresMap.put("type", "Feature");
							featuresMap.put("geometry",geomMap.get(objid));
							featuresMap.put("properties", obj);
							features.add(featuresMap);
						}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		featureCollection.put("features", features);
		
		return featureCollection;
	}
	
	
	
	
	
	
	/**
	 * 获取GeoJson格式数据
	 * @param space_tablename
	 * 		空间表名
	 * @param objids
	 * 		objid,多个用逗号隔开
	 * @param objList
	 * 		properties展现数据列表
	 * @param infoList
	 *       所有风廓线站点某高度某时刻风速风向数据
	 * @return 
	 * 		GeoJson格式数据       (new)原有接口上修改的新接口
	 */
	public Map<String, Object> getGeoJsonFormat_fkx(String space_tablename, String objids, List<?> objList,List<Map<String,Object>> infoList) {
		
		//查询空间数据
		List<SpaceEntuty> geomList = baseaddDao.getSpaceInfo(space_tablename, objids);
		//List转Map,做优化
		Map<Integer, Object> geomMap = new LinkedHashMap<Integer, Object>();
		if (geomList.size() > 0) {
			for (SpaceEntuty geom : geomList) {
				geomMap.put(geom.getObjid(), JSONObject.parse((geom.getGeometry())));
			}
		}
		
		//拼接GeoJson格式数据
		Map<String, Object> featureCollection = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> features = new ArrayList<Map<String,Object>>();
		featureCollection.put("type", "FeatureCollection");
		if (objList.size() > 0) {
			if (objList.get(0) instanceof Map) {//objList的泛型是Map类型
				for (Object obj : objList) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (LinkedHashMap<String, Object>) obj;
					int objid = map.get("objid") == null ? 0 : Integer.parseInt(map.get("objid").toString());
					//System.out.println("map.objid:"+objid);
					map.put("czfs", "");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("spfs", "");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("spfx", "");  //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					for(Map<String,Object>rmap:infoList){//风速，风向数据
						//System.out.println("out rmap.objid:"+rmap.get("objid"));
						if((""+objid).equals(rmap.get("objid")==null?"-1":rmap.get("objid").toString())){
							map.put("czfs", rmap.get("czfs"));
							map.put("spfs", rmap.get("spfs"));
							map.put("spfx", rmap.get("spfx"));
							//System.out.println("in rmap.objid:"+rmap.get("objid"));
							break;
						}
					}
					if (geomMap.containsKey(objid)) {
						Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
						featuresMap.put("type", "Feature");
						featuresMap.put("geometry", geomMap.get(objid));
						featuresMap.put("properties", map);
						features.add(featuresMap);
					}
				}
			} else {//objList的泛型是普通对象类型
				try {
					for (Object obj : objList) {
						
						//利用暴力反射获取指定成员变量的值
						Field field = obj.getClass().getDeclaredField("objid");
						field.setAccessible(true);//去除私有权限
						int objid = field.get(obj) == null ? 0 :Integer.parseInt(field.get(obj).toString());
						
						if (geomMap.containsKey(objid)) {
							Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
							featuresMap.put("type", "Feature");
							featuresMap.put("geometry",geomMap.get(objid));
							featuresMap.put("properties", obj);
							features.add(featuresMap);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		featureCollection.put("features", features);
		
		return featureCollection;
	}
	
	
	

	/**
	 * 获取GeoJson格式数据
	 * @param space_tablename
	 * 		空间表名
	 * @param objids
	 * 		objid,多个用逗号隔开
	 * @param objList
	 * 		properties展现数据列表
	 * @param infoList
	 *       所有风廓线站点某高度某时刻风速风向数据
	 * @return 
	 * 		GeoJson格式数据       (new)原有接口上修改的新接口
	 */
	public Map<String, Object> getGeoJsonFormat_fsj(String space_tablename, String objids, List<?> objList,List<Map<String,Object>> infoList) {
		
		//查询空间数据
		List<SpaceEntuty> geomList = baseaddDao.getSpaceInfo(space_tablename, objids);
		//List转Map,做优化
		Map<Integer, Object> geomMap = new LinkedHashMap<Integer, Object>();
		if (geomList.size() > 0) {
			for (SpaceEntuty geom : geomList) {
				geomMap.put(geom.getObjid(), JSONObject.parse((geom.getGeometry())));
			}
		}
		
		//拼接GeoJson格式数据
		Map<String, Object> featureCollection = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> features = new ArrayList<Map<String,Object>>();
		featureCollection.put("type", "FeatureCollection");
		if (objList.size() > 0) {
			if (objList.get(0) instanceof Map) {//objList的泛型是Map类型
				for (Object obj : objList) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (LinkedHashMap<String, Object>) obj;
					int objid = map.get("objid") == null ? 0 : Integer.parseInt(map.get("objid").toString());
					//System.out.println("map.objid:"+objid);
					map.put("wd", ""); //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					map.put("sd", ""); //默认没有为“”,这样保证每个站点都有此参数，好让前端展示
					for(Map<String,Object>rmap:infoList){//温度，湿度数据
						//System.out.println("out rmap.objid:"+rmap.get("objid"));
						if((""+objid).equals(rmap.get("objid")==null?"-1":rmap.get("objid").toString())){
							map.put("wd", rmap.get("wd"));
							map.put("sd", rmap.get("sd"));
							
							//System.out.println("in rmap.objid:"+rmap.get("objid"));
							break;
						}
					}
					if (geomMap.containsKey(objid)) {
						Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
						featuresMap.put("type", "Feature");
						featuresMap.put("geometry", geomMap.get(objid));
						featuresMap.put("properties", map);
						features.add(featuresMap);
					}
				}
			} else {//objList的泛型是普通对象类型
				try {
					for (Object obj : objList) {
						
						//利用暴力反射获取指定成员变量的值
						Field field = obj.getClass().getDeclaredField("objid");
						field.setAccessible(true);//去除私有权限
						int objid = field.get(obj) == null ? 0 :Integer.parseInt(field.get(obj).toString());
						
						if (geomMap.containsKey(objid)) {
							Map<String, Object> featuresMap = new LinkedHashMap<String, Object>();
							featuresMap.put("type", "Feature");
							featuresMap.put("geometry",geomMap.get(objid));
							featuresMap.put("properties", obj);
							features.add(featuresMap);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		featureCollection.put("features", features);
		
		return featureCollection;
	}
}
