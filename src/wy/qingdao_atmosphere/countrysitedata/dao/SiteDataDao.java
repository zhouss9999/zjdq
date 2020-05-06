package wy.qingdao_atmosphere.countrysitedata.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wy.qingdao_atmosphere.countrysitedata.domain.AttachInfoStore;
import wy.qingdao_atmosphere.countrysitedata.domain.CollectParam;
import wy.qingdao_atmosphere.countrysitedata.domain.ConnObjParam;
import wy.qingdao_atmosphere.countrysitedata.domain.CpDir;
import wy.qingdao_atmosphere.countrysitedata.domain.CpinfoObj;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceDo;
import wy.qingdao_atmosphere.countrysitedata.domain.DbConnOid;
import wy.qingdao_atmosphere.countrysitedata.domain.FsjFtpParam;
import wy.qingdao_atmosphere.countrysitedata.domain.Param;
import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.domain.SpaceTable;
import wy.qingdao_atmosphere.countrysitedata.domain.Threshold;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;
import wy.util.datapersistence.ModelAssist;

public interface SiteDataDao {
	
	//同步监测数据到数据库
	public int addCollectData(List<SiteData> list); 
	
	//同步监测数据到数据库
	public int addCollectDataTwo(Map<String, Object> map); 
	
	//获取设备类型关联参数
	public List<Param> getParamByDtid(Map<String, Object> map);
	
	//获取设备类型下参数辅助对象
	public List<ParamAssis> getParamAssisByDtid(Map<String, Object> map);
	
	//删除指定时间之前的监测数据
	public int delCollData(Map<String, Object> map);
	
	//通过objtypeid获取objid
	public List<String> getObjidByOtid(int objtypeid);
	
	//获取子表附件的最大时间
	public String getsubStoreMaxtime(Map<String, Object> map);
	
	//添加微波辐射状态报警信息
	public int addWbfsThrinfo(List<Threshold> list);
    
	//从数据库查询微博辐射计（历史）数据
	public List<Map<String, Object>> queryWeiBoDate(Map<String, Object> map);
	
	//从数据库查询微博辐射计（实时）数据
	public List<Map<String, Object>> querySsWeiBoDate(Map<String, Object> map);
	

	//查询方位角度与俯仰角度
	public Map<String, Object> queryFwAndFy();
    
	//通过设备类型获取oobs监测数据最新时间
	public String getOOBSMaxTimeByDtid(Map<String, Object> map);

	//从数据库查询风廓线监测数据
	public List<Map<String, Object>> queryWindProfileData(
			Map<String, Object> map);
    
	//获取温度平流所需参数数据（辐射计的和风廓线的联合查询）
	public List<Map<String, Object>> getTemperatureParam(Map<String, Object> map);

	//查询站点纬度
	public String qureyLatitude(Map<String, Object> map);
    
	//获取水汽通量
	public List<Map<String, Object>> getWaterVapor(Map<String, Object> map);

	// 根据站点编号查询风廓线站点的objid,objtypeid,devicenumber等 
	public Map<String, Object> queryByStaNum(Map<String, Object> paramMap);
    
	//通过objid查询风廓线站点dataguid相关参数
	public Map<String, Object> getDataguidByOid(Map<String, Object> paramap);
    
	//从数据库获取历史（最近72个小时）的微波辐射计基础数据K的8个通道亮温值数据
	public List<Map<String, Object>> queryTdlwforK(Map<String, Object> map);
	
	//从数据库获取历史（最近72个小时）的微波辐射计基础数据V的8个通道亮温值数据
	public List<Map<String, Object>> queryTdlwforV(Map<String, Object> map);
	
	//从数据库获取微波辐射计通道亮温的云底高度和降雨状态数据
	public List<Map<String, Object>> queryTdlwOtherdata(Map<String, Object> map);

	//微波辐射监测-其他廓线实时数据
	public List<Map<String, Object>> wbOtherKx(Map<String, Object> map);
    
	//查询水平风速，垂直风速，水平方向
	public List<Map<String, Object>> reWriteFKXfile(Map<String, Object> map);

	//根据时间和dataguid查询地面压强
	public String queryDmyqBytime(Map<String, Object> parmap);
	
	
	// 返回数据库里已有的5分钟内离参数最近的时间
	public String queryMaxTimeByTime(Map<String,Object> pMap);
    
	
	//基本信息查询
	public List<ModelAssist>  getfsjInfoList(
			Map<String, Object> paramMap);
	
	//基本信息查询
	public List<ModelAssist>  getfkxInfoList(
				Map<String, Object> paramMap);
    
	//根据时间查稳定度系数
	public Map<String, Object> queryWDDinfo(Map<String, Object> parMap);

	
	//通过objid查辐射计区站编号
	public String queryQzbhByobjid(Map<String, Object> parMap);
	
	//通过objid查风廓线区站编号
	public String queryFkxQzbhByobjid(Map<String, Object> parMap);
    
	
	//查找Z0和Z-20 0度与-20度温度对应的高度
	public Map<String, Object> queryAboutZ(Map<String, Object> parMap);
   
	//根据辐射计objid查对应站点的风廓线的objid 
	public String queryFkxOidhByFsjOid(Map<String, Object> pmap);
	
	
	//根据风廓线objid查对应站点的辐射计的objid 
	public String queryFsjOidhByFkxOid(Map<String, Object> pmap);
	
	
	//风廓线-所有站点规定高度下各个时间的风速，风向信息  new
	public List<Map<String, Object>> getFkxAllSitesInfo(Map<String, Object> pmap);
    
	//辐射计所有某高度某时间的温度，湿度信息  new
	public List<Map<String, Object>> getfsjAllSitesInfo(Map<String, Object> pmap);

	//风廓线，获得风廓线最近俩个小时的时间刻度列表 new
	public List<String> getFkxHoursList(Map<String, Object> map);
    
	//查询辐射计所有站点obj new 
	public List<String> getAllfsjObjids();
    
	
	//查询时间戳表是否有该站点的数据
	public List<Map<String, Object>> querySiteTimestamp(Map<String, Object> map);
    
	//插入一条该站点最新时间的时间戳
	public int addSiteTimestamp(Map<String, Object> map);
    
	//更新该站点最新时间的时间戳
	public int updateSiteTimestamp(Map<String, Object> map);
    
	//获得辐射计某站点最新时间
	public String getFsjMaxTime(Map<String, Object> zmap);
	
	//获得风廓线某站点最新时间
	public String getFkxMaxTime(Map<String, Object> zmap);
   
	//查询辐射计站点设备的状态
	public List<Map<String, Object>> getfsjZtList(Map<String, Object> paramMap);
	
	
	/**
	 * 从数据库查询微博辐射计（实时）温度廓线数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> querySsWeiBowdDate(Map<String, Object> map);
	
	
	/**
	 * 从数据库查询微博辐射计（实时）湿度廓线数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> querySsWeiBosdDate(Map<String, Object> map);
			
	
	/**
	 * 从数据库查询微博辐射计（实时）湿气密度廓线数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> querySsWeiBosqmdDate(Map<String, Object> map);
	
	/**
	 * 从数据库查询微博辐射计（实时）折射率廓线数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> querySsWeiBozslDate(Map<String, Object> map);
		
	/**
	 * 从数据库查询微博辐射计（实时）边界层廓线数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> querySsWeiBobjcDate(Map<String, Object> map);
    
	
	/**
	 * 从数据库获取历史（最近12个小时）的微波辐射计数据(温度二维分布数据)
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryWeiBoWdDate(Map<String, Object> map);
	
	/**
	 * 从数据库获取历史（最近12个小时）的微波辐射计数据(湿度二维分布数据)
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryWeiBoSdDate(Map<String, Object> map);
			
	
	/**
	 * 从数据库获取历史（最近12个小时）的微波辐射计数据(湿气密度二维分布数据)
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryWeiBoSqmdDate(Map<String, Object> map);
	
	/**
	 * 从数据库获取历史（最近12个小时）的微波辐射计数据(折射率二维分布数据)
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryWeiBoZslDate(Map<String, Object> map);
		
	/**
	 * 从数据库获取历史（最近12个小时）的微波辐射计数据(边界层二维分布数据)
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryWeiBoBjcDate(Map<String, Object> map);
	
	
	/**
	 * 查询所有辐射计站点
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> findAllFsj(Map<String, Object> map);
	
	
	/**
	 * 查询所有风廓线站点
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> findAllFkx(Map<String, Object> map);
     
	
	/**
	 * 查找所有去重后的风廓线站点dataguid(spfx,spfs,czfs)
	 * @return
	 */
	public List<String> getAllfkxDataguids(Map<String, Object> map);
	
	
	/**
	 * 查找所有去重后的辐射计站点dataguid(wd,sd)
	 * @return
	 */
	public List<String> getAllfsjDataguids(Map<String, Object> map);

	/**
	 * 对比分析-其他廓线实时数据之红外亮温
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> wbOtherKxHwlw(Map<String, Object> map);
    
	/**
	 * 其他廓线实时数据之液态水
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> wbOtherKxYts(Map<String, Object> map);
  
	
	/**
	 * 对比分析-其他廓线实时数据之水汽总含量
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> wbOtherKxSqzhl(Map<String, Object> map);
    
	/**
	 * 其他廓线实时数据之地面温度
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> wbOtherKxDmwd(Map<String, Object> map);
	
    /**
     * 对比分析-其他廓线实时数据之地面湿度
     * @param map
     * @return
     */
	public List<Map<String, Object>> wbOtherKxDmsd(Map<String, Object> map);

	
	/**
	 * 对比分析-其他廓线实时数据之地面压强
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> wbOtherKxDmyq(Map<String, Object> map);
		
	
	/**
	 * 对比分析
	 * 从数据库查询风廓线水平风速
	 * @param map
	 * @return
	 */
   public List<Map<String, Object>> queryDbfxWpSpfs(
				Map<String, Object> map);
   
   
	   /**
		 * 对比分析
		 * 从数据库查询风廓线垂直风速
		 * @param map
		 * @return
		 */
	  public List<Map<String, Object>> queryDbfxWpCzfs(
					Map<String, Object> map);
  
	  /**
		 * 对比分析
		 * 风廓线雷达对比分析的水平风速廓线的数据
		 * @param map
		 * @return
		 */
	public List<Map<String, Object>> queryDbfxKxWpSpfs(
					Map<String, Object> map);
		
	
	/**
	 * 对比分析
	 * 风廓线雷达对比分析的水平风向廓线的数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryDbfxKxWpSpfx(
				Map<String, Object> map);
	
	
	/**
	 * 对比分析
	 * 风廓线雷达对比分析的垂直风速廓线的数据
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryDbfxKxWpCzfs(
				Map<String, Object> map);
	
	
	
	/**
	 * 从数据库查询微博辐射计（历史）数据,只获取温度和湿度
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryFuseWdSd(Map<String, Object> map);
	
	
	
	/**
	 * 查询辐射计已起用的ftp服务器
	 * @param map
	 * @return
	 */
	public List<FsjFtpParam> queryFsjSb(Map<String, Object> map);
	
	
	
	
	
	/**
	 * 查询其他数据库配置信息
	 * @param map
	 * @return
	 */
	public List<DataSourceDo> queryOtherDataSource(Map<String,Object> map);
	
	
	
	
	/**
	 * 查询其他数据库的实时数据
	 * @param map
	 * @return
	 */
	public List<CollectParam> findOhterDbCollect(Map<String,Object> map);
	
	
	/**
	 * 插入实时数据
	 * @param map
	 * @return
	 */
	//public  int insertCollect(List<CollectParam> list);
	
	/**
	 * 插入实时数据
	 * @param map
	 * @return
	 */
	public  int insertCollect(Map<String,Object> map);
	
	/**
	 * 根据objtype查找其它数据库的站点obj信息（规定只有单个设备-非平台）
	 * @param obj
	 * @return
	 */
	public CpinfoObj  selectOtherDbobjByOtp (Map<String,Object> obj);
	
	
	/**
	 * 根据objtype查找其它数据库的站点空间表信息（规定只有单个设备-非平台
	 * @param obj
	 * @return
	 */
	public SpaceTable  selectOtherDbSpaceByOtp (Map<String,Object> table);
	
	
	
	/**
	 * 根据objtype查找其它数据库的站点基本信息（规定只有单个设备-非平台
	 * @param obj
	 * @return
	 */
	public List<AttachInfoStore>  selectOtherDbAttachInfoByOtp (Map<String,Object> info);
	
	
	/**
	 * 根据objtype查找其它数据库的对象和参数关联表信息（规定只有单个设备-非平台
	 * @param obj
	 * @return
	 */
	public List<ConnObjParam>  selectOtherDbConnObjParamByOtp (Map<String,Object> info);
		
	
	/**
	 * 新增obj信息,
	 * @param obj
	 * @return 
	 */
	public Integer  addObjTwo (CpinfoObj obj);
	
	
	/**
	 * 新增空间表信息,
	 * @param obj
	 * @return 
	 */
	public int  addZjSpaceInfo (SpaceTable table);
	
	
	
	/**
	 * 新增基本信息表信息,
	 * @param obj
	 * @return 
	 */
	public int  addCpAttachInfoStore (List<AttachInfoStore> list);
	
	
	/**
	 * 新增对象参数关联表信息
	 * @param obj
	 * @return 
	 */
	public int  addConnObjParam (List<ConnObjParam> list);
	
	
	/**
	 * 新增其它数据源s
	 * @param obj
	 * @return 
	 */
	public Integer  addOtherDb (DataSourceDo ds);
	
	
	
	/**
	 * 插入本地数据库与其他数据源的objid关联表
	 * @param map
	 * @return
	 */
	public  int addObjIdConnect(Map<String,Object> map);
	
	
	/**
	 * 新建实时数据监测表
	 */
	public void creatCollTable(Map<String,Object> map);
	
	
	/**
	 * 动态添加设备
	 * 查找其他数据源与本地数据库的objid的关联关系
	 * @param map
	 * @return
	 */
	public List<DbConnOid> selectDbConnOid(
				Map<String, Object> map);
	
	
	/** 通过设备类型获取监测数据最新时间*/
	public String getMaxTimeByDtid_three(String time_formt, String devicetypeid, String objid);
    
	
	/**
	 * 查询设备数据来源的ftp服务器信息
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> getFtpInfo(Map<String, Object> map);
	
	
	/**
	 * 更新设备数据来源的ftp服务器信息
	 * @param param
	 * @return
	 */
	public int updateFtpInfo(FsjFtpParam param);

	/**
	 * 删除数据源记录
	 * @param dsId
	 */
	public int delOtherDs(Integer dsId);

	public void dropCollTable(Integer newObjid);

	public int deleteObj(Integer newObjid);

	public int deleteSpace(Map<String, Object> zMap);

	public int deleteConnParam(Integer newObjid);

	public int deleteInfoStore(Integer newObjid);

	public int delConnDbOid(Integer newObjid);

	public List<Map<String, Object>> findDirBycity(Map<String, String> pMap);

	public int addThirdDir(CpDir dir);

	public Map<String, Object> queryPlatform();

	public int delFsjDir(Integer fsjDirid);

	public int delCollData_two(Map<String, Object> map);

	public int addWebServer(WebServer web);

	public int addFsjServer(WebServer fsj);

	public int updateWebServer(WebServer web);

	public int updateFsjServer(WebServer fsj);

	public List<WebServer> selectFsjServer(WebServer fsj);

	public List<WebServer> selectWebServer(WebServer web);

	public List<DataSourceDo> selectOtherDb(DataSourceDo ds);

	public int updateOtherDb(DataSourceDo ds);
    /**
     * 删除其他数据源的一些列相关信息
     * @param ids
     */
	public void deleteOtherDb(String ids);
    
	
	/**
	 * 根据objid查出设备基本信息
	 * @param map
	 * @return
	 */
	public List<AttachInfoStore> selectAttachInfo(Map<String, Object> map);
	
	
    /**
     * 通过objid和cityName查询是否还存在该市的设备
     * @param map
     */
	public List<Map<String,Object>> isHasOtherDeviceByCity(Map<String, Object> map);

	
	/**
	 * 删除辐射计设备的该市级目录
	 * @param map
	 * @return
	 */
	public int deleteDirBycity(Map<String, Object> map);

	public int deleteFsjServer(WebServer fsj);

	public int deleteWebServer(WebServer web);

	public List<Map<String, Object>> selectMapParam(Map<String, Object> map);

	public int insertMapParam(Map<String, Object> map);

	public int updateMapParam(Map<String, Object> map);
	
	
	}
