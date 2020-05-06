package wy.qingdao_atmosphere.onemap.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.onemap.domain.KqzlColl;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.qingdao_atmosphere.onemap.domain.OmRank;
import wy.qingdao_atmosphere.onemap.domain.PicInfo;
import wy.qingdao_atmosphere.onemap.domain.WeatherData;
import wy.util.datapersistence.SpaceInfo;

@Repository("oneMapDao")
public class OneMapDaoImpl extends SqlSessionDaoSupport implements OneMapDao {
	
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}
	
	public List<OmMenu> getOmMenuList() {
		return this.getSqlSession().selectList("getOmMenuList");
	}
	
	public List<KqzlColl> getOmAllSiteDatas(Map<String, Object> map) {
		return this.getSqlSession().selectList("getOmAllSiteDatas", map);
	}

	public List<Map<String, Object>> getOmAllSiteGoodRate(Map<String, Object> map) {
		return this.getSqlSession().selectList("getOmAllSiteGoodRate", map);
	}

	
	public List<ParamAssis> getDkList(Map<String, Object> map) {
		return this.getSqlSession().selectList("getDevidKey", map);
	}

	public ParamAssis getDevidKey(Map<String, Object> map) {
		return this.getSqlSession().selectOne("getDevidKey", map);
	}

	public String getDayDataObjid(String objid) {
		return this.getSqlSession().selectOne("getDayDataObjid",objid);
	}

	public List<PicInfo> getPicInfoList(Map<String, Object> map) {
		return this.getSqlSession().selectList("getPicInfoList", map);
	}
	
	public List<OmRank> omRankBySite(Map<String, Object> map) {
		return this.getSqlSession().selectList("omRankBySite", map);
	}

	public List<OmRank> omRankByArea(Map<String, Object> map) {
		return this.getSqlSession().selectList("omRankByArea", map);
	}
	
	public List<SpaceInfo> getQXJCZGeomList(Map<String, Object> map) {
		return this.getSqlSession().selectList("getQXJCZGeomList", map);
	}
	
	public List<WeatherData> getWeatherJCZMonitorData(Map<String, Object> map) {
		return this.getSqlSession().selectList("getWeatherJCZMonitorData", map);
	}

	public List<Map<String, Object>> getThrList(Map<String, Object> map) {
		return this.getSqlSession().selectList("getThrList", map);
	}
	
	public List<Map<String, Object>> selectKqAndQx(Map<String, Object> map){
		return this.getSqlSession().selectList("selectKqAndQx", map);
	}

	public List<OmMenu> getdqMenuList() {
		return this.getSqlSession().selectList("getdqMenuList");
	}
}
