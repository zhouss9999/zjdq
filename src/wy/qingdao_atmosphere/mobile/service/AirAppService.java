package wy.qingdao_atmosphere.mobile.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import wy.qingdao_atmosphere.mobile.domain.AirCollect;
import wy.qingdao_atmosphere.mobile.domain.AirGoodRate;
import wy.qingdao_atmosphere.mobile.domain.WeatherForecastData;
import wy.qingdao_atmosphere.onemap.domain.KqzlColl;

public interface AirAppService {
	/**===================================================================================================
	 * ================================================一手机端-首页===========================================*/	
	//首页-获取单个城市空气质量监测数据
	public AirCollect getSKQ4City(HttpServletRequest request);
	//首页-获取收藏夹城市列表以及监测数据
	public List<AirCollect> getSKQ4Citys(HttpServletRequest request);
	//首页-获取城市下的站点列表以及监测数据
	public List<KqzlColl> getSKQ4Sites(HttpServletRequest request);
	//首页-获取单个站点监测数据详情
	public Map<String, Object> getSKQ4Site(HttpServletRequest request);
	
	/**===================================================================================================
	 * ================================================一手机端-大气地图===========================================*/
	//大气地图-获取城市所有站点坐标以及监测参数数据(浓度)
	public List<KqzlColl> getMND4Sites(HttpServletRequest request);
	//大气地图-获取单个站点监测数据详情(浓度)
	public Map<String, Object> getMND4Site(HttpServletRequest request);
	//大气地图-获取城市所有站点坐标以及监测参数数据(优良率)
	public List<AirGoodRate> getMYL4Sites(HttpServletRequest request);
	//大气地图-获取单个站点监测数据详情(优良率)
	public Map<String, Object> getMYL4Site(HttpServletRequest request, int day);
	
	/**===================================================================================================
	 * ================================================一手机端-大气排名===========================================*/
	//大气排名-获取城市所有站点实时AQI排名
	public List<KqzlColl> getRAQ4Sites(HttpServletRequest request);
	//大气排名-获取城市所有站点近7天/30天优良率排名(days格式：例"1 day")
	public List<AirGoodRate> getRYLSites(HttpServletRequest request, int day);
	
	//获取手机端天气预报实时或日监测数据
	public List<WeatherForecastData> getMobileWeatherForecastData(HttpServletRequest request);
}
