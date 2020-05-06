package wy.qingdao_atmosphere.mobile.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import wy.qingdao_atmosphere.mobile.domain.AirCollect;
import wy.qingdao_atmosphere.mobile.domain.WeatherForecastData;
import wy.qingdao_atmosphere.onemap.domain.KqzlColl;

@Repository("airAppDao")
public class AirAppDaoImpl extends SqlSessionDaoSupport implements AirAppDao {
	
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}

	//获取单个城市空气质量监测数据
	public AirCollect getKQ4City(Map<String, Object> map) {
		return this.getSqlSession().selectOne("getKQForCity",map);
	}
	//获取多个城市空气质量监测数据
	public List<AirCollect> getKQ4Citys(Map<String, Object> map) {
		return this.getSqlSession().selectList("getKQForCity",map);
	}
	//获取站点空气质量监测数据 
	public List<KqzlColl> getKQ4Site(Map<String, Object> map) {
		return this.getSqlSession().selectList("getKQForSite",map);
	}
	//查询实时数据站点对应的日数据站点--objid为实时数据站点的objid
	public String getDayDataObjid(String objid) {
		return this.getSqlSession().selectOne("getDayDataObjid",objid);
	}

	public List<KqzlColl> getRank4Site(Map<String, Object> map) {
		return this.getSqlSession().selectList("getRankForSite",map);
	}

	//获取手机端天气预报实时或日监测数据
	public List<WeatherForecastData> getMobileWeatherForecastData(
			Map<String, Object> map) {
		return this.getSqlSession().selectList("getMobileWeatherForecastData",map);
	}
}