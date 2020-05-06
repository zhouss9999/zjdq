package wy.qingdao_atmosphere.reportform.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

@Repository("reportformDao")
public class ReportformDaoImpl extends SqlSessionDaoSupport implements ReportformDao{

	//创建session工厂
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}
		
	public List<Map<String, Object>> getwz(Map<String, Object> map) {
		return this.getSqlSession().selectList("getwz",map);
	}

	//查询实时报数据
	public List<Map<String, Object>> queryrealData(Map<String, Object> map){
		return this.getSqlSession().selectList("queryrealData",map);
	}
	
	//查询探空曲线站点
	public List<Map<String, Object>> querytkqxobj(){
		return this.getSqlSession().selectList("querytkqxobj");
	}
	
	//查询探空曲线
	public List<Map<String,Object>> queryTkqx(Map<String, Object> map){
		return this.getSqlSession().selectList("queryTkqx",map);
	}
	
	//修改探空曲线
	public void updateTkqx(Map<String, Object> map){
		this.getSqlSession().update("updateTkqx",map);
	}
	
	//AQI日历
	public List<Map<String,Object>> queryAqiCalendar(Map<String, Object> map){
		return this.getSqlSession().selectList("queryAqiCalendar",map);
	}
	
	//计算优良率
	public Double queryGoodrate(Map<String,Object> map){
		String result = this.getSqlSession().selectOne("queryGoodrate", map);
		return  result==null?0:Double.parseDouble(result);
	}
	
	//实时监测数据
	public List<Map<String,Object>> queryRealTimeData(Map<String,Object> map){
		return this.getSqlSession().selectList("queryRealTimeData",map);
	}
	
	//空气质量优良率
	public List<Map<String, Object>> queryGoodratetwo(Map<String,Object> map){
		return this.getSqlSession().selectList("queryGoodratetwo",map);
	}
	
	//气象分析
	public Map<String, Object> queryqxfx(Map<String,Object> map){
		return this.getSqlSession().selectOne("queryqxfx",map);
	}
	
	//气象数据同步至oneNet
	public List<Map<String, Object>> queryqxSendOneNet(Map<String,Object> map){
		return this.getSqlSession().selectList("queryqxSendOneNet",map);
	}
}
