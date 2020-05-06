package wy.qingdao_atmosphere.countrysitedata.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import wy.qingdao_atmosphere.countrysitedata.domain.CollectParam;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceDo;
import wy.qingdao_atmosphere.countrysitedata.domain.FsjFtpParam;
import wy.qingdao_atmosphere.countrysitedata.domain.Param;
import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.domain.TemperatureParam;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;

public interface SiteDataService {

	//同步监测数据到数据库
	public int addCollectData(List<SiteData> list);
	
	//获取设备类型关联参数
	public List<Param> getParamByDtid(String devicetypeid);
	
	//获取设备类型下参数辅助对象
	public List<ParamAssis> getParamAssisByDtid(String devicetypeid,String objid);
	
	//删除指定时间之前的监测数据、
	public int delCollData(String collecttime, String devicetypeid);
	
	//获取子表附件的最大时间
	public String getsubStoreMaxtime(String objid, String fieldid, String timeformat);
	
	//获取温度平流所需参数数据（辐射计的和风廓线的联合查询）
	public List<TemperatureParam> getTemperatureParam(Map<String,Object> map);
    
	//获取水汽通量（辐射计的和风廓线的联合查询）
	public List<TemperatureParam> getWaterVapor(Map<String, Object> map);
    
	
	//根据风廓线objid查对应站点的辐射计的objid 
	public String queryFsjOidhByFkxOid(Map<String, Object> pmap);
	
	//根据辐射计objid查对应站点的风廓线的objid 
    public String queryFkxOidhByFsjOid(Map<String, Object> pmap);
	
	//风廓线-所有站点规定高度下各个时间的风速，风向信息   new
	public List<Map<String, Object>> getFkxAllSitesInfo(HttpServletRequest request);
   
    //一张图-风廓线，获得实况监测目录（高度及下面的时刻列表）  new
	public Map<String,Object> getMoinitorMenu(HttpServletRequest request);
    
	//查询时间戳表是否有该站点的数据
	public List<Map<String, Object>> querySiteTimestamp(Map<String, Object> map);
    
	//插入一条该站点最新时间的时间戳
	public int addSiteTimestamp(Map<String, Object> map);
    
	//更新该站点最新时间的时间戳
	public int updateSiteTimestamp(Map<String, Object> map);
  
	//获得辐射计某站点最新时间
	public String getFsjMaxTime(Map<String, Object> zmap);
	
	//查询所有辐射计站点
	public List<Map<String, Object>> findAllFsj(Map<String, Object> zmap);
		
		
	//查询所有风廓线站点
	public List<Map<String, Object>> findAllFkx(Map<String, Object> zmap);
    
	//查询所有融合图站点目录
	public List<Map<String, Object>> getDbfxFuseMenu(
			HashMap<String, Object> hashMap);
	
	
	/**
	 * 查询其他数据库的实时数据
	 * @param map
	 * @return
	 */
	public List<CollectParam> findOhterDbCollect(Map<String,Object> map);
	
	/**
	 * 插入实时数据
	 * @param list
	 * @return
	 */
	//public int insertCollect(List<CollectParam> list);
	public int insertCollect(Map<String,Object>map);
	
	/**
     * .
     * 分批插入实时监测数据
     * TODO 递归:分割长List为 subNum/段。
     * @param thesisList 论文list(总)
     * @param subNum 每段长度 (最小1)
     * @return
     * @throws Exception
     */
	public   int recurSub(List<CollectParam> thesisList,int subNum,Integer baseObjid) throws Exception;
    
	
	/**
	 * 查询设备的数据来源的ftp服务器信息
	 * @param request
	 * @return
	 */
	public List<Map<String, Object>> getFtpInfo(HttpServletRequest request);
	
	
	/**
	 * 更新设备数据来源的ftp服务器信息
	 * @param param
	 * @return
	 */
	public int updateFtpInfo(FsjFtpParam param);

	
	/**
	 * 添加其他数据源的时候进行的一系列相关操作
	 * @param ds
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> addOtherDb(DataSourceDo ds) throws Exception /*throws Exception*/;
	
	
	/**
	 * 添加其他数据源的时候进行的一系列相关操作
	 * 多数据源下使用事务注解无法切换数据源
	 * @param ds
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> addOtherDb_two(DataSourceDo ds) throws Exception /*throws Exception*/;

	public int usedFtpServer(FsjFtpParam param);

	/**
	 * 查询该站点时属于平台端还是设备端
	 * @return
	 */
	public Map<String, Object> queryPlatform();

	/**
	 * //删除指定时间之前的监测数据、
	 * @param collecttime
	 * @param objid
	 * @return
	 */
	public int delCollData_two(String collecttime, String objid);

	/**
	 * 添加web服务器信息
	 * @param web
	 * @return
	 */
	public int addWebServer(WebServer web);
	
	
	/**
	 * 添加fsj服务器信息
	 * @param web
	 * @return
	 */
	public int addFsjServer(WebServer fsj);

	
	/**
	 * 修改web服务器信息
	 * @param web
	 * @return
	 */
	public int updateWebServer(WebServer web);
	
	
	
	/**
	 * 修改fsj服务器信息
	 * @param web
	 * @return
	 */
	public int updateFsjServer(WebServer fsj);
    
	/**
	 * 查询辐射计服务器信息
	 * @param fsj
	 * @return
	 */
	public List<WebServer> selectFsjServer(WebServer fsj);

	/**
	 * 添加web服务器信息
	 * @param web
	 * @return
	 */
	public List<WebServer> selectWebServer(WebServer web);

	/**
	 * 查询其他数据源信息
	 * @param ds
	 * @return
	 */
	public List<DataSourceDo> selectOtherDb(DataSourceDo ds);

	
	/**
	 * 修改其他数据源信息
	 * @param ds
	 * @return
	 */
	public int updateOtherDb(DataSourceDo ds);
    
	public Map<String,Object> deleteOtherDb(Integer integer);

	public int deleteFsjServer(WebServer fsj);

	public int deleteWebServer(WebServer web);
    
	/**
	 * 查询地图参数配置
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> selectMapParam(Map<String, Object> map);

	public int insertMapParam(Map<String, Object> map);

	public int updateMapParam(Map<String, Object> map);
	
}
