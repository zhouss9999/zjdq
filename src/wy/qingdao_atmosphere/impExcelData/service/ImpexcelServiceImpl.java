package wy.qingdao_atmosphere.impExcelData.service;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wy.util.ReadExcel;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataAssist;
import wy.qingdao_atmosphere.impExcelData.dao.IImpexcelDao;
import wy.qingdao_atmosphere.impExcelData.domain.AttachInfoStore;
import wy.qingdao_atmosphere.impExcelData.domain.InfoObj;
import wy.qingdao_atmosphere.impExcelData.domain.ModelAssist;

@Service("impexcelService")
@SuppressWarnings("all")
public class ImpexcelServiceImpl implements IImpexcelService {
	
    private IImpexcelDao impexcelDao;
    
    @Autowired
    private ReadExcel readExcel;
    
	public IImpexcelDao getImpexcelDao() {
		return impexcelDao;
	}

	@Resource
	public void setImpexcelDao(IImpexcelDao impexcelDao) {
		this.impexcelDao = impexcelDao;
	}
	
	//基本数据添加-有空间表数据
	public LinkedHashSet<String> addExcelDatas(String filePath, String basePath, int sheetNum, int start, int columnkey) {
		int addObjNum = 0;	 //添加对象的条数
		int	updateObjNum = 0;//更新对象的条数
		File file = null;
		LinkedHashSet<String> messageSet = new LinkedHashSet<String>();
		try {
			if(filePath.startsWith("http")){
				URL url = new URL(filePath);
				basePath=basePath.substring(0,basePath.lastIndexOf("\\"));
				file=new File(basePath+url.getFile());
				
			}else{
				file = new File(filePath);
			}
			//读取Excel数据输出成List<List<String>>数据
			List<List<String>> excelBeanList = readExcel.exportListFromExcel(file, sheetNum);
			String[] key = null; //key为标题&后面拼接的字段id，key[0]:对象objtypeid
			for(int i=start;i<excelBeanList.size();i++){
				if(i==start){
					key = new String[excelBeanList.get(start).size()];
					List<String> column = excelBeanList.get(start);
					for(int j=0;j<column.size();j++){
						key[j]=column.get(j).split("&")[1];
					}
				}
				//从数据行开始读
				if(i<excelBeanList.size()-1){
					//得到每一行的数据
					List<String> column = excelBeanList.get(i+1);
					String objtypeid = key[0]; //excel标题第一列对象名称&后面拼接的objtypeid
					String objname = column.get(0).split("&")[0];//数据第一列对象名称
					String jwd = column.get(column.size()-1); //经纬度
					String spacename= key[key.length-1];	//空间表名称
					//传入对象类型id和对象名称进去查询对象表中的数量
					List<Integer> objidlist = this.getObjidByObjnameAndObjtypeidService(objtypeid, objname);
					//cp_info_obj添加数据
					InfoObj infoObj = new InfoObj();
					infoObj.setObjname(objname);
					infoObj.setObjtypeid(Integer.parseInt(objtypeid));
					//判断对象名称在数据库中存在几个，有一个进行更新操作，两个及以上不进行任何操作，提示信息给用户，没有则做添加操作
					if(objidlist.size() > 0 && objidlist.size() < 2){
						infoObj.setObjid(objidlist.get(0));
						updateObjNum += impexcelDao.updateExcelDatasForObj(infoObj);
					}else if(objidlist.size() <= 0){
						addObjNum += impexcelDao.addExcelDatasForObj(infoObj);
					}else{
						messageSet.add(objname+"对象在数据库中存在两个或以上的同名对象！请更正为唯一对象名称！");
					}
					//获取对象表objid
					int objid = infoObj.getObjid();
					//cp_attachinfo_store添加数据
					List<AttachInfoStore> paramList = new ArrayList<AttachInfoStore>();
					for(int j=columnkey;j<column.size()-1;j++){
						AttachInfoStore attachInfoStore = new AttachInfoStore();
						attachInfoStore.setFieldid(Integer.valueOf(key[j]));
						attachInfoStore.setFieldvalue((column.get(j) == null || "".equals(column.get(j))) ? "/" : column.get(j).toString());
						attachInfoStore.setObjid(objid);
						paramList.add(attachInfoStore);
					}
					//判断对象名称在数据库中存在几个，有一个进行更新基础信息操作，没有则做添加基础信息操作
					if(objidlist.size() > 0 && objidlist.size() < 2){
						impexcelDao.updateExcelDatasForAttach(paramList); //更新基础数据
					}
					if(objidlist.size() <= 0){
						impexcelDao.addExcelDatasForAttach(paramList); //添加基础数据
					}
					//space添加空间数据
					if(jwd!=null && jwd!=""){
						Map<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("objid", objid);
						paramMap.put("id", objid);
						paramMap.put("jwd", jwd);
						paramMap.put("spacename", spacename);
						try {
							//判断对象名称在数据库中存在几个，有一个进行更新空间信息操作，没有则做添加空间信息操作
							if(objidlist.size() > 0 && objidlist.size() < 2){
								//根据objid查询空间表是否有空间数据，如果有做添加操作，反之做更新操作
								List<Integer> spacelist = impexcelDao.countSpaceDataByObjid(paramMap);
								if(spacelist.size() > 0){
									impexcelDao.updateDatasForSpace(paramMap);
								}else{
									impexcelDao.addDatasForSpace(paramMap);
								}
							}
							if(objidlist.size() <= 0){
								impexcelDao.addDatasForSpace(paramMap);
							}
						} catch (Exception e) {
							messageSet.add(objname+"的经纬度数据格式错误！");
						}
					}
				}
				
			}
			messageSet.add("基础数据添加成功的条数："+addObjNum+"，基础数据更新成功的条数："+updateObjNum);
		} catch (Exception e) {
			e.printStackTrace();
			messageSet.add("请勿修改基础数据模板内容！");
			messageSet.add("基础数据添加成功的条数：0，基础数据更新成功的条数：0");
		}
		return messageSet;
	}

	//经纬度转换
	public String castJwd(String longitude,String lagitude) {
		String jwd="";
		int jdd=0; int jdf=0; double jdm=0d; 
		int wdd=0; int wdf=0; double wdm=0d;
		if(longitude.contains(",")){
			String[] longitudeArr = longitude.split(",");
			jdd=Integer.valueOf(longitudeArr[0].trim());
			jdf=Integer.valueOf(longitudeArr[1].trim());
			jdm=Double.valueOf(longitudeArr[2].trim());
		}
		if(lagitude.contains(",")){
			String[] lagitudeArr = lagitude.split(",");
			wdd=Integer.valueOf(lagitudeArr[0].trim());
			wdf=Integer.valueOf(lagitudeArr[1].trim());
			wdm=Double.valueOf(lagitudeArr[2].trim());
		}
		jwd = castJwdTwo(jdd, jdf, jdm, wdd, wdf, wdm);
		
		return jwd;
	}
	
	//经纬度转换
	public String castJwdTwo(int jdd, int jdf, Double jdm, int wdd, int wdf,
			double wdm) {
		
		String jwd = (jdd+(double)jdf/60+(double)jdm/3600)+" "+(wdd+(double)wdf/60+(double)wdm/3600);
		return jwd;
	}
	
	//根据对象名称获取objid
	public List<Integer> getObjidByObjnameAndObjtypeidService(String objtypeid,String objname){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("objtypeid", objtypeid);
		map.put("objname", objname);
		return impexcelDao.getObjidByObjnameAndObjtypeid(map);
	}
	
	//传输对象类型id查询基础信息数据
	public List<List<Object>> selectBasicDataByObjtypeidService(String objtypeid, String objid, String spacename){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objtypeid", objtypeid);
		paramMap.put("objid", objid);
		paramMap.put("spacename", spacename);
		List<ModelAssist> modelList = impexcelDao.selectBasicDataByObjtypeid(paramMap);
		List<List<Object>> dataList = new ArrayList<List<Object>>();
		// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
		Map<String, String> hm = new LinkedHashMap<String, String>();
		for (int i = 0; i < modelList.size(); i++) {

			ModelAssist epzt = (ModelAssist) modelList.get(i);
			hm.put(String.valueOf(epzt.getFieldid()), epzt.getFieldvalue());
			hm.put("objname", epzt.getObjname());
			hm.put("locations", epzt.getLocations());
			if (i != modelList.size() - 1) {
				ModelAssist epzttwo = (ModelAssist) modelList.get(i + 1);
				// 如下if else 是针对“列转行”的数据操作
				if (!epzt.getObjid().equals(epzttwo.getObjid())) {
					// 组装的数据Bean返回给前端
					//Map<String,Object> datas = new LinkedHashMap<String,Object>();
					List<Object> liststring = new ArrayList<Object>();
					liststring.add(hm.get("objname"));//头部添加对象名称
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objname")&&!key.equals("locations"))
						//datas.put(key, value);
						liststring.add(String.valueOf(value));
					}
					liststring.add(hm.get("locations"));//尾部添加
					dataList.add(liststring);
					hm.clear();
				} else {
					// 循环到最后“一条”数据时，需要再次保存该数据，如上if条件无法保存;或者时只有一条数据的情况
					if (i >= (modelList.size() - 2)) {
						//Map<String,Object> datas = new LinkedHashMap<String,Object>();
						List<Object> liststring = new ArrayList<Object>();
						liststring.add(hm.get("objname"));//头部添加对象名称
						//datas.put("objid", Integer.valueOf(hm.get("objid")));
						Iterator hmIterator = hm.entrySet().iterator();
						while(hmIterator.hasNext()){
							Entry entry = (Entry) hmIterator.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							if(!key.equals("objname")&&!key.equals("locations"))
							//datas.put(key, value);
							liststring.add(String.valueOf(value));
						}
						liststring.add(hm.get("locations"));//尾部添加
						dataList.add(liststring);
					}
				}

			} else {// 封装最后“一行”数据
				hm.put(String.valueOf(epzt.getFieldid()), epzt.getFieldvalue());
				// 组装的数据Bean返回给前端
				//Map<String,Object> datas = new LinkedHashMap<String,Object>();
				List<Object> liststring = new ArrayList<Object>();
				liststring.add(hm.get("objname"));//头部添加对象名称
				//datas.put("objid", Integer.valueOf(hm.get("objid")));
				Iterator hmIterator = hm.entrySet().iterator();
				while(hmIterator.hasNext()){
					Entry entry = (Entry) hmIterator.next();
					String key = entry.getKey().toString();
					String value = entry.getValue().toString();
					if(!key.equals("objname")&&!key.equals("locations"))
					//datas.put(key, value);
					liststring.add(String.valueOf(value));
				}
				liststring.add(hm.get("locations"));//尾部添加
				// 判断modelList只有一行数据，封装最后一行数据会出现数组越界异常，因此dataList做添加操作，反之做删除dataList最后一行空数据再进行添加的操作
				if(dataList.size() <= 0){
					dataList.add(liststring);
				}else{
					dataList.remove(dataList.size()-1);
					dataList.add(liststring);
				}
			}

		}
		return dataList;
	}
	
	//查询监测数据
	public List<Map<String, Object>> selectMonitorDataService(Map<String,Object> map){
		List<MonitorDataAssist> modelList = impexcelDao.selectMonitorData(map);
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
		Map<String, String> hm = new LinkedHashMap<String, String>();
		for (int i = 0; i < modelList.size(); i++) {

			MonitorDataAssist epzt = (MonitorDataAssist) modelList.get(i);
			hm.put(String.valueOf(epzt.getParamid()), epzt.getDatavalue());
			hm.put("objname", epzt.getObjname()+"&"+epzt.getObjid());
			hm.put("collecttime", epzt.getCollecttime());
			if (i != modelList.size() - 1) {
				MonitorDataAssist epzttwo = (MonitorDataAssist) modelList.get(i + 1);
				// 如下if else 是针对“列转行”的数据操作
				if (epzt.getObjid().equals(epzttwo.getObjid()) && !epzt.getCollecttime().equals(epzttwo.getCollecttime())) { 	//objid相同时间不同切割一条数据
					// 组装的数据Bean返回给前端
					Map<String,Object> datas = new LinkedHashMap<String,Object>();
					datas.put("objname", hm.get("objname"));	//开头插入对象名称
					//创建迭代器插入监测数据
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objname")&&!key.equals("collecttime"))
						datas.put(key, value);
					}
					datas.put("collecttime", hm.get("collecttime")); //结尾插入监测时间
					dataList.add(datas);
					hm.clear();
				}
				
				if (!epzt.getObjid().equals(epzttwo.getObjid())) {	//objid不同切割一条数据
					// 组装的数据Bean返回给前端
					Map<String,Object> datas = new LinkedHashMap<String,Object>();
					datas.put("objname", hm.get("objname"));	//开头插入对象名称
					//创建迭代器插入监测数据
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objname")&&!key.equals("collecttime"))
						datas.put(key, value);
					}
					datas.put("collecttime", hm.get("collecttime")); //结尾插入监测时间
					dataList.add(datas);
					hm.clear();
				} else {
					// 循环到最后“一条”数据时，需要再次保存该数据，如上if条件无法保存;或者时只有一条数据的情况
					if (i >= (modelList.size() - 2)) {
						// 组装的数据Bean返回给前端
						Map<String,Object> datas = new LinkedHashMap<String,Object>();
						datas.put("objname", hm.get("objname"));	//开头插入对象名称
						//创建迭代器插入监测数据
						Iterator hmIterator = hm.entrySet().iterator();
						while(hmIterator.hasNext()){
							Entry entry = (Entry) hmIterator.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							if(!key.equals("objname")&&!key.equals("collecttime"))
							datas.put(key, value);
						}
						datas.put("collecttime", hm.get("collecttime")); //结尾插入监测时间
						dataList.add(datas);
					}
				}

			} else {// 封装最后“一行”数据
				hm.put(String.valueOf(epzt.getParamid()), epzt.getDatavalue());
				// 组装的数据Bean返回给前端
				Map<String,Object> datas = new LinkedHashMap<String,Object>();
				datas.put("objname", hm.get("objname"));	//开头插入对象名称
				//创建迭代器插入监测数据
				Iterator hmIterator = hm.entrySet().iterator();
				while(hmIterator.hasNext()){
					Entry entry = (Entry) hmIterator.next();
					String key = entry.getKey().toString();
					String value = entry.getValue().toString();
					if(!key.equals("objname")&&!key.equals("collecttime"))
					datas.put(key, value);
				}
				datas.put("collecttime", hm.get("collecttime")); //结尾插入监测时间
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
		
	public static void main(String[] args) {
		double a = 18.3/123;
		double b = 0.14878048780487804*123;
		String str = "18.3";
		System.out.println(a);
		System.out.println(b);
		System.out.println(str);
	}
	
	/**
	 * 读取Excel的监测数据插入数据库
	 * @param filePath:文件地址
	 * @param sheetNum:工作表序号(从0开始)
	 * @param start:从第几行开始读(从0开始)
	 * @param columnKey:从第几列开始读(从0开始)
	 */
	public LinkedHashSet<String> addMonitorDatasForActual(String filePath, String basePath, int sheetNum, int start, int columnKey) {
		int successNum = 0; //添加成功的条数
		int updateNum = 0;	//更细成功的条数
		File file = null;
		LinkedHashSet<String> messageSet = new LinkedHashSet<String>();
		try {
			if(filePath.startsWith("http")){
				URL url = new URL(filePath);
				basePath=basePath.substring(0,basePath.lastIndexOf("\\"));
				file=new File(basePath+url.getFile());
				
			}else{
				file = new File(filePath);
			}
			//读取Excel数据输出成List<List<String>>数据
			List<List<String>> dataList = readExcel.exportListFromExcel(file, sheetNum);
			//key为标题&后面拼接的参数
			//key[0]:对象objtypeid
			//paramtype:为标题&参数&后面拼接的数据类型
			String[] key = null;
			String[] paramtype = null;
			String[] paramname = null;
			for(int i=start;i<dataList.size();i++){
				if(i==start){
					key = new String[dataList.get(start).size()-1]; //读取对象名称和所有的参数，时间字段不读取，故做size-1
					paramtype = new String[dataList.get(start).size()-2];//不读取对象名称和时间，故做size-2
					paramname = new String[dataList.get(start).size()-2];//不读取对象名称和时间，故做size-2
					List<String> column = dataList.get(start);
					for(int j=0;j<column.size()-1;j++){
						key[j] = column.get(j).split("&")[1];
						if(j > 0){
							paramtype[j-1] = column.get(j).split("&")[2];
							paramname[j-1] = column.get(j).split("&")[0];
						}
					}
				}
				//从数据行开始读
				if(i < dataList.size() -1){
					Map<String,Object> paramMap = new HashMap<String,Object>();
					List<String> column = dataList.get(i+1); //得到每一行的数据
					String objtypeid = key[0]; //excel标题第一列对象名称&后面拼接的objtypeid
					String objid = column.get(0).split("&")[1];//数据第一列对象名称&后面拼接的objid
					String objname = column.get(0).split("&")[0];//数据第一列对象名称&后面拼接的objid
					for(int j=columnKey;j<column.size()-1;j++){
						//dataguid组合：(objtypeid+"_"+objid+"_"+devicenumber+"_"+paramid)
						//key[j]:标题&后面跟的devicenunmber+paramid的组合
						String dataguid=objtypeid+"_"+objid+"_"+key[j]; 
						//监测值
						String jcdataValue = column.get(j).toString();
						//监测时间
						String collecttime = column.get(column.size()-1);
						//参数的数据类型
						String paramdatatype = paramtype[j-1];
						//参数的名称
						String dataparamname = paramname[j-1];
						if(jcdataValue != null && !"".equals(jcdataValue)){
							if(collecttime != null && !"".equals(collecttime)){
								try {
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(collecttime);
									if(paramdatatype.equals("double")){
										try {
											Double.valueOf(jcdataValue);
											//将获取好的数据参数放入map集合
											paramMap.put("dataguid", dataguid);
											paramMap.put("datavalue", jcdataValue);
											paramMap.put("collecttime", collecttime);
											//传入dataguid和collecttime查询实时数据有无这个点的记录，count大于零就是有数据，则做更新，反之添加
											int count = impexcelDao.countActualDataByDataguidAndCollecttime(paramMap);
											if(count > 0){
												updateNum+=impexcelDao.updateExcelDatasForActual(paramMap);
											}else{
												successNum+=impexcelDao.addExcelDatasForActual(paramMap);
											}
										} catch (Exception e) {
											messageSet.add(objname+collecttime+"的"+dataparamname+"的值必须为数值！");
										}
									}else{
										//将获取好的数据参数放入map集合
										paramMap.put("dataguid", dataguid);
										paramMap.put("datavalue", jcdataValue);
										paramMap.put("collecttime", collecttime);
										//传入dataguid和collecttime查询实时数据有无这个点的记录，count大于零就是有数据，则做更新，反之添加
										int count = impexcelDao.countActualDataByDataguidAndCollecttime(paramMap);
										if(count > 0){
											updateNum+=impexcelDao.updateExcelDatasForActual(paramMap);
										}else{
											successNum+=impexcelDao.addExcelDatasForActual(paramMap);
										}
									}
								} catch (Exception e) {
									messageSet.add(objname+"的监测时间格式不正确！");
								}
							}else{
								messageSet.add(objname+"的监测时间不能为空！");
							}
						}
					}
				}
			}
			messageSet.add("监测数据添加成功的条数："+successNum+"，监测数据更新成功的条数："+updateNum);
		} catch (Exception e) {
			messageSet.add("请勿修改监测数据模板内容！");
			messageSet.add("监测数据添加成功的条数：0，监测数据更新成功的条数：0");
		}
		return messageSet;
	}
}