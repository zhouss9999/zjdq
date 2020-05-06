package wy.qingdao_atmosphere.impExcelData.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import wy.qingdao_atmosphere.impExcelData.domain.AttachInfoStore;
import wy.qingdao_atmosphere.impExcelData.domain.InfoObj;
import wy.qingdao_atmosphere.impExcelData.domain.ModelAssist;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataAssist;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataExcelTitle;

@Repository("impexcelDao")
public class ImpexcelDaoImpl extends SqlSessionDaoSupport implements
		IImpexcelDao {

	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}

	//===================================基础数据模块==================================================================
	
	// cp_info_obj添加数据
	public int addExcelDatasForObj(InfoObj infoObj) {
		return this.getSqlSession().insert("addExcelDatasForObj", infoObj);
	}
	
	// cp_info_obj更新数据
	public int updateExcelDatasForObj(InfoObj infoObj){
		return this.getSqlSession().update("updateExcelDatasForObj", infoObj);
	}

	// cp_attachinfo_store添加数据
	public int addExcelDatasForAttach(List<AttachInfoStore> attachInfoStoreList) {
		return this.getSqlSession().insert("addExcelDatasForAttach",
				attachInfoStoreList);
	}

	// cp_attachinfo_store更新数据 
	public int updateExcelDatasForAttach(List<AttachInfoStore> attachInfoStoreList){
		return this.getSqlSession().update("updateExcelDatasForAttach", attachInfoStoreList);
	}
	
	// 添加空间数据
	public int addDatasForSpace(Map<String, Object> paramMap) {
		return this.getSqlSession().insert("addDatasForSpace", paramMap);
	}
	
	// 更新空间数据
	public int updateDatasForSpace(Map<String, Object> paramMap){
		return this.getSqlSession().update("updateDatasForSpace", paramMap);
	}

	//根据objid查询空间数据是否存在
	public List<Integer> countSpaceDataByObjid(Map<String, Object> paramMap){
		return this.getSqlSession().selectList("countSpaceDataByObjid", paramMap);
	}
	
	// 根据对象名称获取objid
	public List<Integer> getObjidByObjnameAndObjtypeid(Map<String, Object> paramMap) {
		return this.getSqlSession().selectList("getObjidByObjnameAndObjtypeid", paramMap);
	}
	
	// 传输对象类型id查询基本表字段信息
	public List<String> selectFieldinfoByObjtypeid(String objtypeid) {
		return this.getSqlSession().selectList("selectFieldinfoByObjtypeid", objtypeid);
	}

	// 传输对象类型id查询基础信息数据
	public List<ModelAssist> selectBasicDataByObjtypeid(
			Map<String, Object> paramMap) {
		return this.getSqlSession().selectList("selectBasicDataByObjtypeid", paramMap);
	}

	//===================================监测数据模块==================================================================
	
	// 从Excel读取监测数据添加到数据库
	public int addExcelDatasForActual(Map<String, Object> paramMap) {
		return this.getSqlSession().insert("addExcelDatasForActual", paramMap);
	}

	// 从Excel读取监测数据更新到数据库
	public int updateExcelDatasForActual(Map<String, Object> paramMap) {
		return this.getSqlSession().update("updateExcelDatasForActual", paramMap);
	}
	// 传输对象类型id查询对象监测数据参数信息
	public List<MonitorDataExcelTitle> selectParaminfoByObjtypeid(
			Map<String, Object> map) {
		return this.getSqlSession().selectList("selectParaminfoByObjtypeid", map);
	}

	// 查询监测数据
	public List<MonitorDataAssist> selectMonitorData(Map<String, Object> map) {
		return this.getSqlSession().selectList("selectMonitorData", map);
	}

	// 根据objid获取对象名称及objid
	public List<Object> selectObjnameAndObjid(Map<String, Object> map) {
		return this.getSqlSession().selectList("selectObjnameAndObjid", map);
	}

	// 根据dataguid和监测时间查询实时数据表有无数据
	public int countActualDataByDataguidAndCollecttime(Map<String, Object> map) {
		return this.getSqlSession().selectOne("countActualDataByDataguidAndCollecttime", map);
	}
}