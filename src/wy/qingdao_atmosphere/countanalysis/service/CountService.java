package wy.qingdao_atmosphere.countanalysis.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import wy.qingdao_atmosphere.countanalysis.domain.AreaAlarmCount;
import wy.qingdao_atmosphere.onemap.domain.OmRank;

public interface CountService {
	
	/*获取区域名称列表*/
	public List<String> getAreaName(HttpServletRequest request);
	
	/*统计分析-城市空气质量*/
	public Map<String, Object> getRankByAQI(HttpServletRequest request);//AQI排名
	public Map<String, Object> getAQIByYear(HttpServletRequest request);//微站年度空气质量情况
	public List<OmRank> getAQICalendar(HttpServletRequest request);		//AQI日历
	public List<OmRank> getAirGoodrate(HttpServletRequest request);		//空气质量优良率
	
	/*统计分析-区域统计*/
	public Map<String, Object> getAirAnalysis(HttpServletRequest request);	//空气质量分析
	public Map<String, Object> getAreaSiteCount(HttpServletRequest request);//微站区域分析
	public String getAlarmMaxtime();										//获取最新报警时间
	public List<AreaAlarmCount> getAreaAlarm(HttpServletRequest request);	//区域报警次数
	
	/*统计分析-微站分析*/
	public List<OmRank> sitesAnalysis(HttpServletRequest request);		//多站点分析
	public List<OmRank> sitePpAnalysis(HttpServletRequest request);		//站点首要污染物分析
	public Map<String, Object> siteAnalysis(HttpServletRequest request);//单站点分析
	
	/*统计分析-历史查询*/
	public List<OmRank> sitesHistory(HttpServletRequest request);
	
	/*统计分析-国控对比*/
	public Map<String, Object> getAQICalendars(HttpServletRequest request);		//AQI日历
	public Map<String, Object> getLineFit(HttpServletRequest request);			//拟合优度
	public Map<String, Object> getAirGoodrates(HttpServletRequest request);		//空气优良率
	
}
