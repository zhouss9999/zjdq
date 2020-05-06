package wy.qingdao_atmosphere.impExcelData.dao;

import java.util.List;
import java.util.Map;

import wy.qingdao_atmosphere.impExcelData.domain.AttachInfoStore;
import wy.qingdao_atmosphere.impExcelData.domain.InfoObj;
import wy.qingdao_atmosphere.impExcelData.domain.ModelAssist;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataAssist;
import wy.qingdao_atmosphere.impExcelData.domain.MonitorDataExcelTitle;

public interface IImpexcelDao {
	//===================================基础数据模块==================================================================
	
	//cp_info_obj添加数据
	public abstract int addExcelDatasForObj(InfoObj infoObj);
	
	//cp_info_obj更新数据
	public abstract int updateExcelDatasForObj(InfoObj infoObj);
	
	//cp_attachinfo_store添加数据 
	public abstract int addExcelDatasForAttach(List<AttachInfoStore> attachInfoStoreList);
	
	//cp_attachinfo_store更新数据 
	public abstract int updateExcelDatasForAttach(List<AttachInfoStore> attachInfoStoreList);
	
	//添加空间数据
	public int addDatasForSpace(Map<String, Object> paramMap);
	
	//更新空间数据
	public int updateDatasForSpace(Map<String, Object> paramMap);
	
	//根据objid查询空间数据是否存在
	public List<Integer> countSpaceDataByObjid(Map<String, Object> paramMap);
	
	//根据对象名称获取objid
	public List<Integer> getObjidByObjnameAndObjtypeid(Map<String,Object> paramMap);
	
	//传输对象类型id查询基本表字段信息
	public List<String> selectFieldinfoByObjtypeid(String objtypeid);
	
	//传输对象类型id查询基础信息数据
	public List<ModelAssist> selectBasicDataByObjtypeid(Map<String, Object> paramMap);
	
	//===================================监测数据模块==================================================================
	
	//从Excel读取监测数据添加到数据库
	public int addExcelDatasForActual(Map<String, Object> paramMap);
	
	//从Excel读取监测数据更新到数据库
	public int updateExcelDatasForActual(Map<String, Object> paramMap);
	
	//传输对象类型id查询对象监测数据参数信息
	public List<MonitorDataExcelTitle> selectParaminfoByObjtypeid(Map<String, Object> map);
	
	//查询监测数据
	public List<MonitorDataAssist> selectMonitorData(Map<String,Object> map);
	
	//根据objid获取对象名称及objid
	public List<Object> selectObjnameAndObjid(Map<String,Object> map);
	
	//根据dataguid和监测时间查询实时数据表有无数据
	public int countActualDataByDataguidAndCollecttime(Map<String, Object> map);
}