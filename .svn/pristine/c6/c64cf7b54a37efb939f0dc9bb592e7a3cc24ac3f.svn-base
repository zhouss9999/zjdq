package wy.qingdao_atmosphere.impExcelData.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public interface IImpexcelService {
	//基本数据添加-有空间表数据
	public LinkedHashSet<String> addExcelDatas(String filePath, String basePath, int sheetNum, int start, int columnkey);
	
	//传输对象类型id查询基础信息数据
	public List<List<Object>> selectBasicDataByObjtypeidService(String objtypeid, String objid, String spacename);
	
	//查询监测数据
	public List<Map<String, Object>> selectMonitorDataService(Map<String,Object> map);
	
	//Excel导入监测数据到数据库
	public LinkedHashSet<String> addMonitorDatasForActual(String filePath, String basePath, int sheetNum, int start, int columnKey);
	
	//根据对象名称获取objid
	public List<Integer> getObjidByObjnameAndObjtypeidService(String objtypeid,String objname);
}