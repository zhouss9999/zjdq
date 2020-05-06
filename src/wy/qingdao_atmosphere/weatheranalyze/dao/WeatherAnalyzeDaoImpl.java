package wy.qingdao_atmosphere.weatheranalyze.dao;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * @author xsq
 * @description 气象分析Dao层接口实现类
 */
@Repository("weatherAnalyzeDao")
public class WeatherAnalyzeDaoImpl extends SqlSessionDaoSupport implements WeatherAnalyzeDao {
	//创建session工厂
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}
}
