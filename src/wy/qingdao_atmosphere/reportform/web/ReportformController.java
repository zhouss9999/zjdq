package wy.qingdao_atmosphere.reportform.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import wy.qingdao_atmosphere.reportform.service.ReportformService;
import wy.qingdao_atmosphere.weatheranalyze.service.WeatherAnalyzeService;
import wy.util.AirBean;
import wy.util.Calc;
import wy.util.FtpUtil;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.util.datapersistence.service.BaseService;

@SuppressWarnings("all")
@Controller
public class ReportformController {

	@Autowired
	private BaseaddDao baseaddDao;

	@Autowired 
	private BaseService baseService;
	
	@Autowired
	private ReportformService reportformService;
	
	/**
	 * 查询微站站点
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getwz.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> getwz(HttpServletRequest request){
		String objtypeid = request.getParameter("objtypeid") == null?"":request.getParameter("objtypeid").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", objtypeid);
		List<Map<String,Object>> list = reportformService.getwz(map);
		LinkedHashSet<Map<String,Object>> h = new LinkedHashSet<Map<String,Object>>();
		for(Map<String,Object> m:list){
			m.remove("devicename");
			m.remove("devicenumber");
			m.remove("rmk1");
			m.remove("objtypeid");
			h.add(m);
		}
		list.clear();
		list.addAll(h);
		return list;
	}
	
	//微站小时数据和日数据查询
	@RequestMapping(value = "/queryrealTime.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> queryrealTime(HttpServletRequest request) throws Exception{
		Logger.getLogger("").info("业务报表 微站小时数据和日数据查询");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		String objtypeid = request.getParameter("objtypeid") == null?"":request.getParameter("objtypeid").toString();
		String begintime = request.getParameter("begintime") == null?"":request.getParameter("begintime").toString();
		String endtime = request.getParameter("endtime") == null?"":request.getParameter("endtime").toString();
		if(begintime.equals("")&&endtime.equals("")){
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
			String time = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd HH24:00:00", "1,4", "9", objtypeid, objid);
			endtime = time;
			Calendar calendartime = Calendar.getInstance();
			calendartime.setTime(f.parse(time));
			Calendar calendar = Calendar.getInstance();
			// 设置时间,当前时间不用设置
			calendar.set(Calendar.YEAR, calendartime.get(Calendar.YEAR));
			calendar.set(Calendar.MONTH, calendartime.get(Calendar.MONTH));
			calendar.set(Calendar.DAY_OF_MONTH, calendartime.get(Calendar.DAY_OF_MONTH)-3);
			begintime = f.format(calendar.getTime());
		}else{
			if(objtypeid.contains("2")&&endtime!=""){
				endtime+=" 23:59:59";
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		map.put("begintime", begintime);
		map.put("endtime", endtime);
		map.put("objtypeid", objtypeid);//1,5监测站点实时数据     2,6监测站点日数据
		List<Map<String,Object>> list = reportformService.queryrealData(map);
		return list;
	}
	
	//月报
	@RequestMapping(value = "/queryMonthlyReport.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> queryMonthlyReport(HttpServletRequest request) throws Exception{
		Logger.getLogger("").info("业务报表 月报");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		if(objid.equals("")){
			objid = getobjs();
		}
		String begintime = request.getParameter("begintime") == null?"":request.getParameter("begintime").toString();
		String endtime = request.getParameter("endtime") == null?"":request.getParameter("endtime").toString();
		if(!begintime.equals("")){
			begintime += "-01";
		}
		if(!endtime.equals("")){
			endtime = getDateLastDay(endtime)+"23:59:59";
		}
		String [] objids = objid.split(",");
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(String s:objids){
			map.put("objid", s);
			map.put("begintime", begintime);
			map.put("endtime", endtime);
			map.put("objtypeid", "2,6");// 2,6监测站点日数据
			List<Map<String,Object>> slist = reportformService.queryrealData(map);
			list.addAll(reportformService.queryMonthlyReport(slist));
		}
		for(Map<String,Object> m:list){
			DecimalFormat df = new DecimalFormat("#0.0");
			m.put("so2", df.format(m.get("so2")));
			m.put("o3", df.format(m.get("o3")));
			m.put("co", df.format(m.get("co")));
			m.put("no2", df.format(m.get("no2")));
			m.put("pm10", df.format(m.get("pm10")));
			m.put("pm25", df.format(m.get("pm25")));
			m.put("yl", df.format(m.get("yl")));
			
			m.put("wd", df.format(m.get("wd")));
			m.put("sd", df.format(m.get("sd")));
			m.put("fl", df.format(m.get("fl")));
			m.put("qy", df.format(m.get("qy")));
			m.put("jsl", df.format(m.get("jsl")));
		}
		return list;
	}
	
	//年报
	@RequestMapping(value = "/queryYearReport.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String,Object>> queryYearReport(HttpServletRequest request) throws Exception{
		Logger.getLogger("").info("业务报表 年报");
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		if(objid.equals("")){
			objid = getobjs();
		}
		String begintime = request.getParameter("begintime") == null?"":request.getParameter("begintime").toString();
		String endtime = request.getParameter("endtime") == null?"":request.getParameter("endtime").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		String [] objids = objid.split(",");
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(String s:objids){
			map.put("objid", s);
			map.put("begintime", begintime);
			map.put("endtime", endtime);
			map.put("objtypeid", "2,6");// 2监测站点日数据
			List<Map<String,Object>> slist = reportformService.queryrealData(map);
			list.addAll(reportformService.queryYearReport(slist));
		}
		for(Map<String,Object> m:list){
			DecimalFormat df = new DecimalFormat("#0.0");
			m.put("so2", df.format(m.get("so2")));
			m.put("o3", df.format(m.get("o3")));
			m.put("co", df.format(m.get("co")));
			m.put("no2", df.format(m.get("no2")));
			m.put("pm10", df.format(m.get("pm10")));
			m.put("pm25", df.format(m.get("pm25")));
			m.put("yl", df.format(m.get("yl")));
			
			m.put("wd", df.format(m.get("wd")));
			m.put("sd", df.format(m.get("sd")));
			m.put("fl", df.format(m.get("fl")));
			m.put("qy", df.format(m.get("qy")));
			m.put("jsl", df.format(m.get("jsl")));
		}
		return list;
	}
	
	//月报设置时间--获取月份的最后一天
	public String getDateLastDay(String date) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM");
		Date time = f.parse(date);
		Calendar calendar = Calendar.getInstance();
		// 设置时间,当前时间不用设置
		calendar.set(Calendar.YEAR, time.getYear()+1900);
		calendar.set(Calendar.MONTH, time.getMonth());
		calendar.set(Calendar.DAY_OF_MONTH, 1); 
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
		return format.format(calendar.getTime());
	}
	
	//计算去年的今天
	public String getDateLastYear(String date) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendartime = Calendar.getInstance();
		calendartime.setTime(f.parse(date));
		Calendar calendar = Calendar.getInstance();
		// 设置时间,当前时间不用设置
		calendar.set(Calendar.YEAR, calendartime.get(Calendar.YEAR)-1);
		calendar.set(Calendar.MONTH, calendartime.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, calendartime.get(Calendar.DAY_OF_MONTH));
//		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
		return format.format(calendar.getTime());
	}
	//计算去年的今天
	public String getDateLastMonth(String date) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendartime = Calendar.getInstance();
		calendartime.setTime(f.parse(date));
		Calendar calendar = Calendar.getInstance();
		// 设置时间,当前时间不用设置
		calendar.set(Calendar.YEAR, calendartime.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, calendartime.get(Calendar.MONTH)-1);
		calendar.set(Calendar.DAY_OF_MONTH, calendartime.get(Calendar.DAY_OF_MONTH));
//			calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
		return format.format(calendar.getTime());
	}
	
	//获取obj
	public String getobjs(){
		String objids = "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objtypeid", "2,6");
		List<Map<String,Object>> wzlist = reportformService.getwz(map);
		HashSet<Map<String,Object>> h = new HashSet<Map<String,Object>>();
		for(Map<String,Object> m:wzlist){
			m.remove("devicename");
			m.remove("devicenumber");
			m.remove("rmk1");
			m.remove("objtypeid");
			m.remove("objname");
			h.add(m);
		}
		wzlist.clear();
		wzlist.addAll(h);
		for(int i=0;i<wzlist.size();i++){
			objids += wzlist.get(i).get("objid").toString();
			if(i<wzlist.size()-1){
				objids+=",";
			}
		}
		return objids;
	}
	
	/**
	 * 探空曲线查询
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryTkqx.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public String queryTkqx(HttpServletRequest request){
		Logger.getLogger("").info("查询探空曲线");
		String collecttime = request.getParameter("collecttime") == null?"":request.getParameter("collecttime").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("collecttime", collecttime);
		List<Map<String, Object>> list = reportformService.queryTkqx(map);
		if(list.size()>0){
			return list.get(0).get("datavalue").toString();
		}else{
			return "[]";
		}
		
	}
	
	//AQI日历
	@RequestMapping(value = "/queryAqiCalendar.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryAqiCalendar(HttpServletRequest request) throws Exception{
		Logger.getLogger("").info("AQI日历");
		String time = request.getParameter("time") == null?"":request.getParameter("time").toString();
		String begintime = time+"-01 00:00:00";
		String endtime = getDateLastDay(time)+"23:59:59";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("begintime", begintime);
		map.put("endtime", endtime);
		return reportformService.queryAqiCalendar(map);
	}
	
	//国站空气质量优良率
	@RequestMapping(value = "/queryGoodrate.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String,Object> queryGoodrate(HttpServletRequest request) throws ParseException{
		Logger.getLogger("").info("国站空气质量优良率");
		String type = request.getParameter("type") == null?"":request.getParameter("type").toString();
		Map<String,Object> map = new HashMap<String, Object>();
		Map<String, Object> resultmap = new HashMap<String, Object>();
		DecimalFormat df = new DecimalFormat("#0.00");
		String time = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-01 00:00:00", "1", "1", "6", "");
		if(("当月").equals(type)){
			//当月
			map.put("begintime", time);
			map.put("endtime", getDateLastDay(time)+"23:59:59");
			Double goodrate1 = reportformService.queryGoodrate(map);
			//去年当月
			map.put("begintime", getDateLastYear(time));
			map.put("endtime", getDateLastYear(getDateLastDay(time)+"23:59:59"));
			Double goodrate2 = reportformService.queryGoodrate(map);
			//上月
			map.put("begintime", getDateLastMonth(time));
			map.put("endtime", getDateLastMonth(getDateLastDay(time)+"23:59:59"));
			Double goodrate3 = reportformService.queryGoodrate(map);
			SimpleDateFormat ff = new SimpleDateFormat("yyyy-MM");
			resultmap.put("time", ff.format(ff.parse(time)));
			resultmap.put("goodrate", goodrate1);//当月优良率
			resultmap.put("tongbi", goodrate2==0D?"无":df.format((goodrate1-goodrate2)/goodrate2*100));
			resultmap.put("huanbi", goodrate3==0D?"无":df.format((goodrate1-goodrate3)/goodrate3*100));
		}else if(("当年").equals(type)){
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-01 00:00:00");
			calendar.setTime(f.parse(time));
			int year = calendar.get(Calendar.YEAR);
			//当年
			map.put("begintime", year+"-01-01 00:00:00");
			map.put("endtime", year+"-12-31 23:59:59");
			Double goodrate1 = reportformService.queryGoodrate(map);
			//去年
			map.put("begintime", (year-1)+"-01-01 00:00:00");
			map.put("endtime", (year-1)+"-12-31 23:59:59");
			Double goodrate2 = reportformService.queryGoodrate(map);
			resultmap.put("time", year);
			resultmap.put("goodrate", goodrate1);//当年优良率
			resultmap.put("huanbi", goodrate2==0D?"无":df.format((goodrate1-goodrate2)/goodrate2*100));
		}
		return resultmap;
	}
	
	//实时监测数据
	@RequestMapping(value = "/queryRealTimeData.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Object> queryRealTimeData(HttpServletRequest request){
		Logger.getLogger("").info("实时监测数据");
		List<Object> resultList = new ArrayList<Object>();
		String objid = request.getParameter("objid") == null?"":request.getParameter("objid").toString();
		String time = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd HH24:00:00", "1,4", "1", "1,5", "");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("begintime", time);
		map.put("objid", objid);
		List<Map<String,Object>> list = reportformService.queryRealTimeData(map);
		map.remove("objid");
		resultList.add(map);
		resultList.add(list);
		return resultList;
	}
	
	//空气质量优良率(时，月，年)
	@RequestMapping(value = "/queryGoodratetwo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Object> queryGoodratetwo(HttpServletRequest request) throws Exception{
		Logger.getLogger("").info("空气质量优良率(时，月，年)");
		String type = request.getParameter("type") == null?"":request.getParameter("type").toString();
		List<Object> resultList = new ArrayList<Object>();
		String time = "";
		Map<String, Object> map = new HashMap<String, Object>();
		if(("时").equals(type)){
			time = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd HH24:00:00", "4", "9", "1", "");
			map.put("objtypeid", 1);
			map.put("begintime", time);
			map.put("endtime", time);
		}else if(("日").equals(type)){
			time = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd HH24:00:00", "4", "9", "2", "");
			map.put("objtypeid", 2);
			map.put("begintime", time);
			map.put("endtime", time);
		}else if(("月").equals(type)){
//			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-01 00:00:00");
			time = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-01 00:00:00", "4", "9", "2", "");
			map.put("objtypeid", 2);
			map.put("begintime", time);
			map.put("endtime", getDateLastDay(time)+"23:59:59");
		}
		List<Map<String,Object>> list = reportformService.queryGoodratetwo(map);
		Map<String,Object> m = new HashMap<String, Object>();
		m.put("time", time);
		resultList.add(m);
		resultList.add(list);
		return resultList;
	}

	//气象分析
	@RequestMapping(value = "/queryqxfx.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryqxfx(HttpServletRequest request){
		String objname = request.getParameter("objname") == null?"":request.getParameter("objname").toString();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objname", objname);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap = reportformService.queryqxfx(map);
		if(resultMap==null){
			return new HashMap<String, Object>();
		}
		return resultMap;
	}
	
	private final static String ip = "121.43.164.143";
	private final static int port = 21;
	private final static String username = "ymm";
	private final static String password = "123456";
	
	private WeatherAnalyzeService weatherAnalyzeService;
	
	public WeatherAnalyzeService getWeatherAnalyzeService() {
		return weatherAnalyzeService;
	}
	@Resource
	public void setWeatherAnalyzeService(WeatherAnalyzeService weatherAnalyzeService) {
		this.weatherAnalyzeService = weatherAnalyzeService;
	}

	//空气质量预报
	@RequestMapping(value = "/queryKqzlyb.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<AirBean> queryKqzlyb(HttpServletRequest request) throws Exception{
		List<AirBean> list = new ArrayList<AirBean>();
		FTPClient ftpClient = FtpUtil.ftpConnection(ip, port, username, password);
		String pathName = "/home/gxq/CALPUFF/qingdao/RESULT/conc/SO2/";
		String dateString = "";
		List<String> forecastDate = new ArrayList<String>();//动态读取ftp中的日期
        if (pathName.startsWith("/") && pathName.endsWith("/")) {
            //更换目录到当前目录
        	ftpClient.changeWorkingDirectory(pathName);
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (file.isDirectory()) {
                	forecastDate.add(file.getName());
                }
            }
        }
		String[] weathertype = {"CO","NOX","PM2.5","PM10","SO2","O3"};
		for(int i=0;i<forecastDate.size();i++){
			AirBean airBean = new AirBean(0,null,null,0,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D);
			for(int j=0;j<weathertype.length;j++){
				Map<String, Object> dayData = weatherAnalyzeService.getWeatherDayData(ftpClient, weathertype[j], forecastDate.get(i), 0D, "CALPUF");
				for(String s:dayData.keySet()){
					if(("CO").equals(weathertype[j])){
						try {
							airBean.setCo(Double.parseDouble(dayData.get(s).toString()));
						} catch (Exception e) {
							airBean.setCo(0D);
						}
					}
					if(("NOX").equals(weathertype[j])){
						try {
							airBean.setNo2(Double.parseDouble(dayData.get(s).toString()));
						} catch (Exception e) {
							airBean.setNo2(0D);
						}
					}
					if(("PM2.5").equals(weathertype[j])){
						try {
							airBean.setPm25(Double.parseDouble(dayData.get(s).toString()));
						} catch (Exception e) {
							airBean.setPm25(0D);
						}
					}
					if(("PM10").equals(weathertype[j])){
						try {
							airBean.setPm10(Double.parseDouble(dayData.get(s).toString()));
						} catch (Exception e) {
							airBean.setPm10(0D);
						}
					}
					if(("SO2").equals(weathertype[j])){
						try {
							airBean.setSo2(Double.parseDouble(dayData.get(s).toString()));
						} catch (Exception e) {
							airBean.setSo2(0D);
						}
					}
					if(("O3").equals(weathertype[j])){
						try {
							airBean.setO3(Double.parseDouble(dayData.get(s).toString()));
						} catch (Exception e) {
							airBean.setO3(0D);
						}
					}
				}
			}
			airBean.setCollecttime(forecastDate.get(i));
			airBean = Calc.AirQ(airBean);//计算AQI,污染物,空气质量等级
			list.add(airBean);
		}
		return list;
	}
	
	/**
	 * onenet推送服务接口确认（测试）
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/Pushtest.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Object Pushtest(HttpServletRequest request) throws IOException{

		String msg = request.getParameter("msg");
		
		return msg;
	}
	
	/**
	 * onenet推送服务接收数据（测试）
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/Pushtest.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public void Pushtest(HttpServletRequest request,HttpServletResponse response) throws IOException{
		String resultStr = "";
		String readLine;
		StringBuffer sb = new StringBuffer();
		BufferedReader responseReader = null;
		OutputStream outputStream = null;
		try {
			responseReader = new BufferedReader(new InputStreamReader(
					request.getInputStream(), "UTF-8"));
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
		System.out.println(resultStr);
	}
}