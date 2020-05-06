package wy.qingdao_atmosphere.countrysitedata.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

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

@Repository("siteDataDao")
public class SiteDataDaoImpl extends SqlSessionDaoSupport implements SiteDataDao {
	
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}

	public int addCollectData(List<SiteData> list) {
		return this.getSqlSession().insert("addCollectData", list);
	}
	
	public int addCollectDataTwo(Map<String, Object> map) {
		return this.getSqlSession().insert("addCollectDataTwo", map);
	}

	public List<Param> getParamByDtid(Map<String, Object> map) {
		return this.getSqlSession().selectList("getParamByDtid", map);
	}

	public List<ParamAssis> getParamAssisByDtid(Map<String, Object> map) {
		//return this.getSqlSession().selectList("getParamAssisByDtid", map);//单个站点
		return this.getSqlSession().selectList("getParamAssisByDtidTwo", map); //多个站点
	}

	public int delCollData(Map<String, Object> map) {
		return this.getSqlSession().delete("delCollData", map);
	}

	public List<String> getObjidByOtid(int objtypeid) {
		return this.getSqlSession().selectList("getObjidByOtid", objtypeid);
	}

	public String getsubStoreMaxtime(Map<String, Object> map) {
		return this.getSqlSession().selectOne("getsubStoreMaxtime", map);
	}

	public int addWbfsThrinfo(List<Threshold> list) {
		return this.getSqlSession().insert("addWbfsThrinfo", list);
	}

	public List<Map<String, Object>> queryWeiBoDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWeiBoDate",map);
	}

	public Map<String, Object> queryFwAndFy() {
		return this.getSqlSession().selectOne("queryFwAndFy");
	}
    
	public String getOOBSMaxTimeByDtid(Map<String, Object> map) {
		return this.getSqlSession().selectOne("getOOBSMaxTimeByDtid", map);
	}

	public List<Map<String, Object>> queryWindProfileData(
			Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWindProfileData",map);
	}

	public List<Map<String, Object>> getTemperatureParam(Map<String, Object> map) {
		
		return this.getSqlSession().selectList("getTemperatureParam",map);
	}

	public String qureyLatitude(Map<String, Object> map) {
		return this.getSqlSession().selectOne("qureyLatitude",map);
	}

	public List<Map<String, Object>> getWaterVapor(Map<String, Object> map) {
		return this.getSqlSession().selectList("getWaterVapor",map);
	}

	public Map<String, Object> queryByStaNum(Map<String, Object> paramMap) {
		return this.getSqlSession().selectOne("queryByStaNum",paramMap);
	}

	public Map<String, Object> getDataguidByOid(Map<String, Object> paramap) {
		return this.getSqlSession().selectOne("getDataguidByOid",paramap);
	}

	public List<Map<String, Object>> queryTdlwforK(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryTdlwforK",map);
	}

	public List<Map<String, Object>> queryTdlwforV(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryTdlwforV",map);
	}

	public List<Map<String, Object>> queryTdlwOtherdata(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryTdlwOtherdata",map);
	}

	public List<Map<String, Object>> wbOtherKx(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKx",map);
	}
   
	public List<Map<String, Object>> reWriteFKXfile(Map<String, Object> map) {
		return this.getSqlSession().selectList("reWriteFKXfile",map);
	}

	public String queryDmyqBytime(Map<String, Object> parmap) {
		return this.getSqlSession().selectOne("queryDmyqBytime",parmap);
	}

	public String queryMaxTimeByTime(Map<String, Object> pMap) {
		return this.getSqlSession().selectOne("queryMaxTimeByTime",pMap);
	}

	public List<Map<String, Object>> querySsWeiBoDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("querySsWeiBoDate",map);
	}

	public List<ModelAssist> getfsjInfoList(
			Map<String, Object> paramMap) {
		return this.getSqlSession().selectList("getfsjInfoList",paramMap);
	}

	public List<ModelAssist> getfkxInfoList(Map<String, Object> paramMap) {
		return this.getSqlSession().selectList("getfkxInfoList",paramMap);
	}

	public Map<String, Object> queryWDDinfo(Map<String, Object> parMap) {
		return this.getSqlSession().selectOne("queryWDDinfo",parMap);
	}

	public String queryQzbhByobjid(Map<String, Object> parMap) {
		return this.getSqlSession().selectOne("queryQzbhByobjid",parMap);
	}
	
	public String queryFkxQzbhByobjid(Map<String, Object> parMap) {
		return this.getSqlSession().selectOne("queryFkxQzbhByobjid",parMap);
	}
	
	public Map<String, Object> queryAboutZ(Map<String, Object> parMap) {
		return this.getSqlSession().selectOne("queryAboutZ",parMap);
	}

	public String queryFkxOidhByFsjOid(Map<String, Object> pmap) {
		return this.getSqlSession().selectOne("queryFkxOidhByFsjOid",pmap);
	}
	
	public String queryFsjOidhByFkxOid(Map<String, Object> pmap) {
		return this.getSqlSession().selectOne("queryFsjOidhByFkxOid",pmap);
	}
	
	//new
	public List<Map<String, Object>> getFkxAllSitesInfo(Map<String, Object> pmap) {
		return this.getSqlSession().selectList("getFkxAllSitesInfo",pmap);
	}
    //new
	public List<Map<String, Object>> getfsjAllSitesInfo(Map<String, Object> pmap) {
		return this.getSqlSession().selectList("getfsjAllSitesInfo",pmap);
	}
    //new
	public List<String> getFkxHoursList(Map<String, Object> map) {
		return this.getSqlSession().selectList("getFkxNearHours",map);
	}
    //new
	public List<String> getAllfsjObjids() {
		return this.getSqlSession().selectList("getAllfsjObjids");
	}
    
	public List<Map<String, Object>> querySiteTimestamp(Map<String, Object> map) {
		return this.getSqlSession().selectList("querySiteTimestamp",map);
	}
   
	public int addSiteTimestamp(Map<String, Object> map) {
		return this.getSqlSession().insert("addSiteTimestamp", map);
	}

	public int updateSiteTimestamp(Map<String, Object> map) {
		return this.getSqlSession().update("updateSiteTimestamp", map);
	}

	public String getFsjMaxTime(Map<String, Object> zmap) {
		return this.getSqlSession().selectOne("getFsjMaxTime",zmap);
	}

	public String getFkxMaxTime(Map<String, Object> zmap) {
		return this.getSqlSession().selectOne("getFkxMaxTime",zmap);
	}

	public List<Map<String, Object>> getfsjZtList(Map<String, Object> paramMap) {
		return this.getSqlSession().selectList("getfsjZtList",paramMap);
	}

	public List<Map<String, Object>> querySsWeiBowdDate(
			Map<String, Object> map) {
		return this.getSqlSession().selectList("querySsWeiBowdDate",map);
	}
	
	public List<Map<String, Object>> querySsWeiBosdDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("querySsWeiBosdDate",map);
	}

	public List<Map<String, Object>> querySsWeiBosqmdDate(
			Map<String, Object> map) {
		return this.getSqlSession().selectList("querySsWeiBosqmdDate",map);
	}

	public List<Map<String, Object>> querySsWeiBozslDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("querySsWeiBozslDate",map);
	}

	public List<Map<String, Object>> querySsWeiBobjcDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("querySsWeiBobjcDate",map);
	}

	public List<Map<String, Object>> queryWeiBoWdDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWeiBoWdDate",map);
	}

	public List<Map<String, Object>> queryWeiBoSdDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWeiBoSdDate",map);
	}

	public List<Map<String, Object>> queryWeiBoSqmdDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWeiBoSqmdDate",map);
	}

	public List<Map<String, Object>> queryWeiBoZslDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWeiBoZslDate",map);
	}

	public List<Map<String, Object>> queryWeiBoBjcDate(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryWeiBoBjcDate",map);
	}

	public List<Map<String, Object>> findAllFsj(Map<String, Object> map) {
		return this.getSqlSession().selectList("findAllFsj",map);
	}

	public List<Map<String, Object>> findAllFkx(Map<String, Object> map) {
		return this.getSqlSession().selectList("findAllFkx",map);
	}

	public List<String> getAllfkxDataguids(Map<String, Object> map) {
		return this.getSqlSession().selectList("getAllfkxDataguids",map);
	}

	public List<String> getAllfsjDataguids(Map<String, Object> map) {
		return this.getSqlSession().selectList("getAllfsjDataguids",map);
	}

	public List<Map<String, Object>> wbOtherKxHwlw(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKxHwlw",map);
	}

	public List<Map<String, Object>> wbOtherKxYts(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKxYts",map);
	}

	public List<Map<String, Object>> wbOtherKxSqzhl(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKxSqzhl",map);
	}

	public List<Map<String, Object>> wbOtherKxDmwd(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKxDmwd",map);
	}

	public List<Map<String, Object>> wbOtherKxDmsd(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKxDmsd",map);
	}

	public List<Map<String, Object>> wbOtherKxDmyq(Map<String, Object> map) {
		return this.getSqlSession().selectList("wbOtherKxDmyq",map);
	}

	public List<Map<String, Object>> queryDbfxWpSpfs(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDbfxWpSpfs",map);
	}

	public List<Map<String, Object>> queryDbfxWpCzfs(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDbfxWpCzfs",map);
	}

	public List<Map<String, Object>> queryDbfxKxWpSpfs(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDbfxKxWpSpfs",map);
	}

	public List<Map<String, Object>> queryDbfxKxWpSpfx(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDbfxKxWpSpfx",map);
	}

	public List<Map<String, Object>> queryDbfxKxWpCzfs(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDbfxKxWpCzfs",map);
	}

	public List<Map<String, Object>> queryFuseWdSd(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryFuseWdSd",map);
	}

	public List<FsjFtpParam> queryFsjSb(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryFsjSb",map);
	}

	public List<DataSourceDo> queryOtherDataSource(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryOtherDataSource",map);
	}

	public List<CollectParam> findOhterDbCollect(Map<String, Object> map) {
		return this.getSqlSession().selectList("findOhterDbCollect",map);
	}

	/*public int insertCollect(List<CollectParam> list) {
		return this.getSqlSession().insert("insertCollect", list);
	}*/

	
	public int insertCollect(Map<String,Object> map) {
		return this.getSqlSession().insert("insertCollect", map);
	}

	public CpinfoObj selectOtherDbobjByOtp(Map<String, Object> obj) {
		return this.getSqlSession().selectOne("selectOtherDbobjByOtp",obj);
	}

	public SpaceTable selectOtherDbSpaceByOtp(Map<String, Object> table) {
		return this.getSqlSession().selectOne("selectOtherDbSpaceByOtp",table);
	}

	public List<AttachInfoStore> selectOtherDbAttachInfoByOtp(Map<String, Object> info) {
		return this.getSqlSession().selectList("selectOtherDbAttachInfoByOtp",info);
	}

	public Integer addObjTwo(CpinfoObj obj) {
		return this.getSqlSession().insert("addObjTwo", obj);
	}

	public int addZjSpaceInfo(SpaceTable table) {
		return this.getSqlSession().insert("addZjSpaceInfo", table);
	}

	public int addCpAttachInfoStore(List<AttachInfoStore> list) {
		return this.getSqlSession().insert("addCpAttachInfoStore", list);
	}

	public int addConnObjParam(List<ConnObjParam> list) {
		return this.getSqlSession().insert("addConnObjParam", list);
	}
	
	
	public Integer addOtherDb(DataSourceDo ds) {
		return this.getSqlSession().insert("addOtherDb", ds);
	}

	public int addObjIdConnect(Map<String, Object> map) {
		return this.getSqlSession().insert("addObjIdConnect", map);
	}

	public List<ConnObjParam> selectOtherDbConnObjParamByOtp(
			Map<String, Object> info) {
		return this.getSqlSession().selectList("selectOtherDbConnObjParamByOtp",info);
	}

	public void creatCollTable(Map<String, Object> map) {
		 this.getSqlSession().update("creatCollTable", map);
		
	}

	public List<DbConnOid> selectDbConnOid(Map<String, Object> map) {
		return this.getSqlSession().selectList("selectDbConnOid",map);
	}
	
	/** 通过设备类型获取监测数据最新时间*/
	public String getMaxTimeByDtid_three(String time_formt, String devicetypeid, String objid){
		Map<String, Object> map = new HashMap<String, Object>();
		if (time_formt != null && !"".equals(time_formt)) {
			map.put("time_formt", time_formt);//时间格式
		} else {
			map.put("time_formt", "yyyy-MM-dd HH24:MI:ss");//时间格式
		}
		map.put("devicetypeid", devicetypeid);
		//objid不传默认查询设备类型下所有对象监测数据的最新时间
		map.put("objid", objid);
		return this.getSqlSession().selectOne("getMaxTimeByDtid_three", map);
	}

	public List<Map<String, Object>> getFtpInfo(Map<String, Object> map) {
		return this.getSqlSession().selectList("getFtpInfo", map);
	}

	
	
	public int updateFtpInfo(FsjFtpParam param) {
	  return this.getSqlSession().update("updateFtpInfo", param);
	}

	public int delOtherDs(Integer dsId) {
		return this.getSqlSession().delete("delOtherDs", dsId);


}

	public void dropCollTable(Integer newObjid) {
		 this.getSqlSession().delete("dropCollTable", newObjid);
	 }

	public int deleteObj(Integer newObjid) {
		return this.getSqlSession().delete("deleteObj", newObjid);
	}

	public int deleteSpace(Map<String, Object> zMap) {
		return this.getSqlSession().delete("deleteSpace", zMap);
	}

	public int deleteConnParam(Integer newObjid) {
		return this.getSqlSession().delete("deleteConnParam", newObjid);
	}

	public int deleteInfoStore(Integer newObjid) {
		return this.getSqlSession().delete("deleteInfoStore", newObjid);
	}

	public int delConnDbOid(Integer newObjid) {
		return this.getSqlSession().delete("delConnDbOid", newObjid);
	}

	public List<Map<String, Object>> findDirBycity(Map<String, String> pMap) {
		return this.getSqlSession().selectList("findDirBycity", pMap);
	}

	public int addThirdDir(CpDir dir) {
		return this.getSqlSession().insert("addThirdDir", dir);
	}

	public Map<String, Object> queryPlatform() {
		return this.getSqlSession().selectOne("queryPlatform");
	}

	public int delFsjDir(Integer fsjDirid) {
		return this.getSqlSession().delete("delFsjDir", fsjDirid);
	}

	public int delCollData_two(Map<String, Object> map) {
		return this.getSqlSession().delete("delCollData_two", map);
	}

	public int addWebServer(WebServer web) {
		return this.getSqlSession().insert("addWebServer", web);
	}

	public int addFsjServer(WebServer fsj) {
		return this.getSqlSession().insert("addFsjServer", fsj);
	}

	public int updateWebServer(WebServer web) {
		return this.getSqlSession().update("updateWebServer", web);
	}

	public int updateFsjServer(WebServer fsj) {
		return this.getSqlSession().update("updateFsjServer", fsj);
	}

	public List<WebServer> selectFsjServer(WebServer fsj) {
		return this.getSqlSession().selectList("selectFsjServer", fsj);
	}

	public List<WebServer> selectWebServer(WebServer web) {
		return this.getSqlSession().selectList("selectWebServer", web);
	}

	public List<DataSourceDo> selectOtherDb(DataSourceDo ds) {
		return this.getSqlSession().selectList("selectOtherDb", ds);
	}

	public int updateOtherDb(DataSourceDo ds) {
		return this.getSqlSession().update("updateOtherDb", ds);
	}

	public void deleteOtherDb(String ids) {
		 this.getSqlSession().delete("deleteOtherDb", ids);
	}

	public List<AttachInfoStore> selectAttachInfo(Map<String, Object> map) {
		return this.getSqlSession().selectList("selectAttachInfo", map);
	}

	public List<Map<String, Object>> isHasOtherDeviceByCity(
			Map<String, Object> map) {
		return this.getSqlSession().selectList("isHasOtherDeviceByCity", map);
	}

	public int deleteDirBycity(Map<String, Object> map) {
		return this.getSqlSession().delete("deleteDirBycity", map);
	}

	public int deleteFsjServer(WebServer fsj) {
		return this.getSqlSession().delete("deleteFsjServer", fsj);
	}

	public int deleteWebServer(WebServer web) {
		return this.getSqlSession().delete("deleteWebServer", web);
	}

	public List<Map<String, Object>> selectMapParam(Map<String, Object> map) {
		return this.getSqlSession().selectList("selectMapParam", map);
	}

	public int insertMapParam(Map<String, Object> map) {
		return this.getSqlSession().insert("insertMapParam", map);
	}

	public int updateMapParam(Map<String, Object> map) {
		return this.getSqlSession().update("updateMapParam", map);
	}
	
	}
