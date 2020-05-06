package wy.qingdao_atmosphere.reportform.service;

import java.util.List;
import java.util.Map;

public interface ReportformService {
	//查询大气站点的设备
	public List<Map<String, Object>> getwz(Map<String, Object> map);
	//查询实时报数据和日报
	public List<Map<String, Object>> queryrealData(Map<String, Object> map);
	//月报计算
	public List<Map<String, Object>> queryMonthlyReport(List<Map<String, Object>> list) throws Exception;
	//年报计算
	public List<Map<String, Object>> queryYearReport(List<Map<String, Object>> list) throws Exception;
	
	//查询探空曲线站点
	public List<Map<String, Object>> querytkqxobj();
	//查询探空曲线
	public List<Map<String,Object>> queryTkqx(Map<String, Object> map);
	//修改探空曲线
	public void updateTkqx(Map<String, Object> map);
	//AQI日历
	public List<Map<String,Object>> queryAqiCalendar(Map<String, Object> map);
	//计算优良率
	public Double queryGoodrate(Map<String,Object> map);
	//实时监测数据
	public List<Map<String,Object>> queryRealTimeData(Map<String,Object> map);
	//空气质量优良率
	public List<Map<String, Object>> queryGoodratetwo(Map<String,Object> map);
	//气象分析
	public Map<String, Object> queryqxfx(Map<String,Object> map);
	//气象数据同步至oneNet
	public List<Map<String, Object>> queryqxSendOneNet(Map<String,Object> map);
}
