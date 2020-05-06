package wy.qingdao_atmosphere.impExcelData.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import wy.util.ExcelData;
import wy.util.ExportExcelUtils;
import wy.util.FileUtilImpl;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataAssist;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataExcelTitle;
import wy.qingdao_atmosphere.impExcelData.dao.IImpexcelDao;
import wy.qingdao_atmosphere.impExcelData.service.IImpexcelService;

@SuppressWarnings("all")
@Controller
public class ImpExcelController {
	
	@Autowired
	private FileUtilImpl fileUtilImpl;
	
	@Autowired
	private BaseaddDao baseaddDao;
	
	private IImpexcelDao impexcelDao;
	
	public IImpexcelDao getImpexcelDao() {
		return impexcelDao;
	}
	@Resource
	public void setImpexcelDao(IImpexcelDao impexcelDao) {
		this.impexcelDao = impexcelDao;
	}

	private IImpexcelService impexcelService;
	
	public IImpexcelService getImpexcelService() {
		return impexcelService;
	}

	@Resource
	public void setImpexcelService(IImpexcelService impexcelService) {
		this.impexcelService = impexcelService;
	}

	//基础数据模板导出
	@RequestMapping(value = "/ExportBasicDataExcel.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
    @ResponseBody
	public Map<String, Object> ExportBasicDataExcel(HttpServletResponse response, HttpServletRequest request) {
		//对象类型id
		String objtypeid = (request.getParameter("objtypeid") == null || "".equals(request.getParameter("objtypeid"))) ? "0" : request.getParameter("objtypeid");
		//对象objid
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		//空间表名称
		String spacename = (request.getParameter("spacename") == null || "".equals(request.getParameter("spacename"))) ? "" : request.getParameter("spacename");
    	//对象类型名称
		String objtypename = (request.getParameter("objtypename") == null || "".equals(request.getParameter("objtypename"))) ? "" : request.getParameter("objtypename");
    	//是否是模板
		String istemplate = (request.getParameter("istemplate") == null || "".equals(request.getParameter("istemplate"))) ? "" : request.getParameter("istemplate");
    	Map<String,Object> returnMap = new HashMap<String, Object>();
		String loginfo = "";
		try{
			//查询基础表字段
        	List<String> datalist = impexcelDao.selectFieldinfoByObjtypeid(objtypeid);
        	datalist.add(0,"对象名称&"+objtypeid);
        	if(!"".equals(spacename)){
        		datalist.add("空间数据(经纬度格式为经度空格纬度，例如：121.231 29.897,无可不填)&"+spacename);
        	}
        	if(datalist.size() > 0){
        		List<List<String>> list = new ArrayList<List<String>>();
                list.add(datalist);
                ExcelData data = new ExcelData();
                data.setName("基础数据");
                data.setTitles(datalist);	//生成表头
                if(!"".equals(istemplate) && "否".equals(istemplate)){
                	loginfo = "基础数据导出_";
	                //查询模板基础数据
	                List<List<Object>> rows = impexcelService.selectBasicDataByObjtypeidService(objtypeid, objid, spacename);
	                //生成数据表
	                data.setRows(rows);
                }else{
                    loginfo = "基础数据模板导出_";
                	List<List<Object>> rows = new ArrayList<List<Object>>();
                	data.setRows(rows);
                }
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String dateTime = format.format(date);
                ExportExcelUtils.exportExcel(response,objtypename+loginfo+dateTime+".xlsx",data);
                returnMap.put("message", "导出模板成功！");
        		returnMap.put("state", true);
        	}else{
        		returnMap.put("message", "该对象类型没有基础数据字段！");
        		returnMap.put("state", false);
        	}
            
        }catch (Exception e){
            e.printStackTrace();
            returnMap.put("message", "导出模板异常！");
    		returnMap.put("state", false);
        }
    	Logger.getLogger("").info(objtypename+loginfo);
		return returnMap;
    }
	
	/**
	 * 导入基础数据
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/ImportBasicDataExcel.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> ImportBasicDataExcel(Model model, HttpServletRequest request,
			HttpServletResponse response) {
		Logger.getLogger("").info("导入基础数据");
		//String filePath = "E:\\排污申报基础数据模板导出_20180823_104724.xlsx";
		String basePath = request.getRealPath("");
		String DataSource = "exceldata";
		String filePath = fileUtilImpl.fileUpload(request, DataSource);//exceld地址
		int sheetNum = 0;//excel工作簿 
		int start = 0;//从第一行开始读
		int columnKey = 1;//从第二列开始读
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			LinkedHashSet<String> resultmessage = impexcelService.addExcelDatas(filePath,basePath, sheetNum, start, columnKey);
			returnMap.put("message", "数据导入成功！");
     		returnMap.put("state", true);
     		returnMap.put("log", resultmessage);
     		//日志存储
			baseaddDao.addUserOperateLog("一般操作","系统管理", "数据管理->导入基础数据："+returnMap.get("log"), returnMap.get("message").toString(),returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "数据导入异常！");
     		returnMap.put("state", false);
     		returnMap.put("log", "");
     		//日志存储
			baseaddDao.addUserOperateLog("异常","系统管理", "数据管理->导入基础数据："+returnMap.get("log"), returnMap.get("message").toString(),returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	//监测数据模板导出
	@RequestMapping(value = "/ExportMonitorDataExcel.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
    @ResponseBody
	public Map<String, Object> ExportMonitorDataExcel(HttpServletResponse response, HttpServletRequest request) {
		//对象类型id
		String objtypeid = (request.getParameter("objtypeid") == null || "".equals(request.getParameter("objtypeid"))) ? "0" : request.getParameter("objtypeid");
		//对象类型名称
		String objtypename = (request.getParameter("objtypename") == null || "".equals(request.getParameter("objtypename"))) ? "" : request.getParameter("objtypename");
    	//devicenumber设备id
		String devicenumber = (request.getParameter("devicenumber") == null || "".equals(request.getParameter("devicenumber"))) ? "" : request.getParameter("devicenumber");
		//参数id
		String paramid = (request.getParameter("paramid") == null || "".equals(request.getParameter("paramid"))) ? "" : request.getParameter("paramid");
		//objid
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		//开始时间
		String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? "" : request.getParameter("begintime");
		//结束时间
		String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? "" : request.getParameter("endtime");			
		//是否是模板
		String istemplate = (request.getParameter("istemplate") == null || "".equals(request.getParameter("istemplate"))) ? "" : request.getParameter("istemplate");
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("objtypeid", objtypeid);
    	paramMap.put("objtypename", objtypename);
    	paramMap.put("devicenumber", devicenumber);
    	paramMap.put("paramid", paramid);
    	paramMap.put("objid", objid);
    	paramMap.put("begintime", begintime);
    	paramMap.put("endtime", endtime);
    	Map<String,Object> returnMap = new HashMap<String, Object>();
		String loginfo = "";
		try{
			//查询监测数据参数表头信息
        	List<MonitorDataExcelTitle> datalist = impexcelDao.selectParaminfoByObjtypeid(paramMap);
        	if(datalist.size() > 0){
        		List<String> titlelist = new ArrayList<String>();
        		titlelist.add(0, "对象名称&"+objtypeid);
        		for(MonitorDataExcelTitle title : datalist){
        			titlelist.add(title.getParamkey());
        		}
        		titlelist.add("监测时间&格式：年月日时分秒(2018-08-01 00:00:00)");
                ExcelData data = new ExcelData();
                data.setName("监测数据");
                data.setTitles(titlelist);	//生成表头
                if(!"".equals(istemplate) && "否".equals(istemplate)){
	                //查询模板监测数据
	                List<Map<String, Object>> Mdatalist = impexcelService.selectMonitorDataService(paramMap);
	                //数据表集合list
	                List<List<Object>> rows = new ArrayList<List<Object>>();
	                for(int i = 0; i < Mdatalist.size(); i++){
	    				Map<String, Object> map = Mdatalist.get(i);
	    				List<Object> row = new ArrayList<Object>();
	    				row.add(map.get("objname"));//开头插入对象名称
	    				for(MonitorDataExcelTitle title : datalist){ //循环表头的paramid
	    					if(map.containsKey(String.valueOf(title.getParamid()))){ //判断表头的paramid是否与map的key相同，是则插入数据否则插入空值
	    						row.add(map.get(String.valueOf(title.getParamid())));
	    					}else{
	    						row.add("");
	    					}
	    				}
	    				row.add(map.get("collecttime"));//结尾插入监测时间
	    				//将处理好的一行数据插入数据表list
	    				rows.add(row);
	    			}
	                //生成数据表
	                data.setRows(rows);
	                loginfo = "监测数据导出_";
                }else{
                	List<List<Object>> rows = new ArrayList<List<Object>>();
                	//查询对象名称拼入模板中
                	List<Object> objnamelist = impexcelDao.selectObjnameAndObjid(paramMap);
                	if(objnamelist.size() > 0){
                		for(int i = 0; i < objnamelist.size(); i++){
                			Object objname = objnamelist.get(i);
                			List<Object> objlist = new ArrayList<Object>();
                			objlist.add(objname);
                			for(int j = 1; j < titlelist.size(); j++){
                				objlist.add("");
                			}
                        	rows.add(objlist);
                		}
                	}
                	data.setRows(rows);
                	loginfo = "监测数据模板导出_";
                }
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String dateTime = format.format(date);
                ExportExcelUtils.exportExcel(response,objtypename+loginfo+dateTime+".xlsx",data);
                returnMap.put("message", "导出模板成功！");
        		returnMap.put("state", true);
        	}else{
        		returnMap.put("message", "该对象类型没有设置监测参数！");
        		returnMap.put("state", false);
        	}
            
        }catch (Exception e){
            e.printStackTrace();
            returnMap.put("message", "导出模板异常！");
    		returnMap.put("state", false);
        }
		Logger.getLogger("").info(objtypename+loginfo);
		return returnMap;
    }
	
	/**
	 * 导入监测数据
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/ImportMonitorDataExcel.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> ImportMonitorDataExcel(Model model, HttpServletRequest request,
			HttpServletResponse response) {
		Logger.getLogger("").info("导入监测数据");
		//String filePath = "E:\\监测断面监测数据.xlsx";
		String basePath = request.getRealPath("");
		String DataSource = "exceldata";
		String filePath = fileUtilImpl.fileUpload(request, DataSource);//exceld地址
		int sheetNum = 0;//excel工作簿 ，断面
		int start = 0;//监测数据所在行
		int columnKey = 1;//待录入数据所在列
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			LinkedHashSet<String> resultmessage = impexcelService.addMonitorDatasForActual(filePath, basePath, sheetNum, start, columnKey);
			returnMap.put("message", "数据导入成功！");
     		returnMap.put("state", true);
     		returnMap.put("log", resultmessage);
     		//日志存储
			baseaddDao.addUserOperateLog("一般操作","系统管理", "数据管理->导入监测数据："+returnMap.get("log"), returnMap.get("message").toString(),returnMap.get("state").toString(), "", "");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("message", "数据导入异常！");
     		returnMap.put("state", false);
     		returnMap.put("log", "");
     		//日志存储
			baseaddDao.addUserOperateLog("异常","系统管理", "数据管理->导入监测数据："+returnMap.get("log"), returnMap.get("message").toString(),returnMap.get("state").toString(), e.getMessage(), "");
		}
		return returnMap;
	}
	
	//监测数据查询
	@RequestMapping(value = "/selectMonitorData.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
    @ResponseBody
	public List<MonitorDataAssist> selectMonitorData(HttpServletRequest request) {
		Logger.getLogger("").info("监测数据查询");
		//对象类型id
		String objtypeid = (request.getParameter("objtypeid") == null || "".equals(request.getParameter("objtypeid"))) ? "0" : request.getParameter("objtypeid");
		//devicenumber设备id
		String devicenumber = (request.getParameter("devicenumber") == null || "".equals(request.getParameter("devicenumber"))) ? "" : request.getParameter("devicenumber");
		//参数id
		String paramid = (request.getParameter("paramid") == null || "".equals(request.getParameter("paramid"))) ? "" : request.getParameter("paramid");
		//objid
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		//开始时间
		String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? "" : request.getParameter("begintime");
		//结束时间
		String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? "" : request.getParameter("endtime");			
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("objtypeid", objtypeid);
    	paramMap.put("devicenumber", devicenumber);
    	paramMap.put("paramid", paramid);
    	paramMap.put("objid", objid);
    	paramMap.put("begintime", begintime);
    	paramMap.put("endtime", endtime);
    	return impexcelDao.selectMonitorData(paramMap);
	}
	
	/*public static void main(String[] args) {
		MonitorDataExcelTitle m1 = new MonitorDataExcelTitle();
		m1.setParamid(4);
		MonitorDataExcelTitle m2 = new MonitorDataExcelTitle();
		m2.setParamid(2);
		MonitorDataExcelTitle m3 = new MonitorDataExcelTitle();
		m3.setParamid(7);
		List<MonitorDataExcelTitle> datalist = new ArrayList<MonitorDataExcelTitle>();
		datalist.add(m1); datalist.add(m2); datalist.add(m3);
		Map<String, Object> map1 = new LinkedHashMap<String, Object>();
		map1.put("objname", "aaa");
		map1.put("2", "123");
		map1.put("4", "456");
		map1.put("collecttime", "2018-06-01 00:00:00");
		Map<String, Object> map2 = new LinkedHashMap<String, Object>();
		map2.put("objname", "bbb");
		map2.put("2", "123");
		map2.put("4", "456");
		map2.put("7", "789");
		map2.put("collecttime", "2018-07-01 00:00:00");
		Map<String, Object> map3 = new LinkedHashMap<String, Object>();
		map3.put("objname", "ccc");
		map3.put("2", "123");
		map3.put("4", "456");
		map3.put("7", "789");
		map3.put("collecttime", "2018-08-01 00:00:00");
		List<Map<String, Object>> Mdatalist = new ArrayList<Map<String,Object>>();
		Mdatalist.add(map1); Mdatalist.add(map2); Mdatalist.add(map3);
		//数据表集合list
        List<List<Object>> rows = new ArrayList<List<Object>>();
			for(int i = 0; i < Mdatalist.size(); i++){
				Map<String, Object> map = Mdatalist.get(i);
				List<Object> row = new ArrayList<Object>();
				row.add(map.get("objname"));//开头插入对象名称
				for(MonitorDataExcelTitle title : datalist){
					if(map.containsKey(String.valueOf(title.getParamid()))){
						row.add(map.get(String.valueOf(title.getParamid())));
					}
					else{
						row.add("/");
					}
				}
				row.add(map.get("collecttime"));//结尾插入监测时间
				//将处理好的一行数据插入数据表list
				rows.add(row);
			}
        
        for(List<Object> rowslist : rows){
        	System.out.println(rowslist);
        }
	}*/
}