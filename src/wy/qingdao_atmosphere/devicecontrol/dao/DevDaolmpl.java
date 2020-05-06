package wy.qingdao_atmosphere.devicecontrol.dao;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

@Repository("devDao")
public class DevDaolmpl extends SqlSessionDaoSupport implements DevDao {
	
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}

}
