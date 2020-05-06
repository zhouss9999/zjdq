package wy.qingdao_atmosphere.countanalysis.dao;

import java.util.List;
import java.util.Map;

import wy.qingdao_atmosphere.countanalysis.domain.AreaAlarmCount;
import wy.qingdao_atmosphere.countanalysis.domain.AreaSiteCount;
import wy.qingdao_atmosphere.countanalysis.domain.Fitting;
import wy.qingdao_atmosphere.onemap.domain.OmRank;

public interface CountDao {
	
	/*获取区域名称列表*/
	public List<String> getAreaName(String city);
	/*获取区域微站站点数*/
	public List<AreaSiteCount> getAreaSiteCount(String city);
	/*超标天数统计*/
	public String overDays(String year, String area);
	/*获取最新报警时间*/
	public String getAlarmMaxtime();
	/*区域报警统计*/
	public List<AreaAlarmCount> areaAlarmCount(Map<String, Object> map);
	/*历史查询*/
	public List<OmRank> getSitesHistory(Map<String, Object> map);
	/*拟合优度*/
	public List<Fitting> getLineFit(String objid, String datetime);
	

}
