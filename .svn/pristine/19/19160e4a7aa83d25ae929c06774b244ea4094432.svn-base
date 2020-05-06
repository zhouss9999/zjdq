package wy.qingdao_atmosphere.mobile.dao;

import java.util.List;
import java.util.Map;

import wy.qingdao_atmosphere.mobile.domain.AirCollect;
import wy.qingdao_atmosphere.mobile.domain.WeatherForecastData;
import wy.qingdao_atmosphere.onemap.domain.KqzlColl;

public interface AirAppDao {
	
	//获取单个城市空气质量监测数据
	public AirCollect getKQ4City(Map<String, Object> map);
	
	//获取多个城市空气质量监测数据
	public List<AirCollect> getKQ4Citys(Map<String, Object> map);
	
	//获取站点空气质量监测数据
	public List<KqzlColl> getKQ4Site(Map<String, Object> map);
	
	//查询实时数据站点对应的日数据站点--objid为实时数据站点的objid
	public String getDayDataObjid(String objid);
	
	//大气排名
	public List<KqzlColl> getRank4Site(Map<String, Object> map);
	
	//获取手机端天气预报实时或日监测数据
	public List<WeatherForecastData> getMobileWeatherForecastData(Map<String, Object> map);
}
