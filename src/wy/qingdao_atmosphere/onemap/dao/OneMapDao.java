package wy.qingdao_atmosphere.onemap.dao;

import java.util.List;
import java.util.Map;

import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.onemap.domain.KqzlColl;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.qingdao_atmosphere.onemap.domain.OmRank;
import wy.qingdao_atmosphere.onemap.domain.PicInfo;
import wy.qingdao_atmosphere.onemap.domain.WeatherData;
import wy.util.datapersistence.SpaceInfo;

public interface OneMapDao {
	
	//获取目录列表
	public List<OmMenu> getOmMenuList();
	
	//获取(浙江大气)目录列表
	public List<OmMenu> getdqMenuList();
	
	//获取国站+微站空气质量数据
	public List<KqzlColl> getOmAllSiteDatas(Map<String, Object> map);
	
	//获取站点优良率及监测参数均值
	public List<Map<String, Object>> getOmAllSiteGoodRate(Map<String, Object> map);
	
	//通过objid和devicetypeid查询设备编号和设备秘钥
	public List<ParamAssis> getDkList(Map<String, Object> map);
	public ParamAssis getDevidKey(Map<String, Object> map);

	//查询实时数据站点对应的日数据站点--objid为实时数据站点的objid
	public String getDayDataObjid(String objid);
	
	//获取图片信息列表
	public List<PicInfo> getPicInfoList(Map<String, Object> map);
	
	//按站点排名
	public List<OmRank> omRankBySite(Map<String, Object> map);

	//按区域排名
	public List<OmRank> omRankByArea(Map<String, Object> map);
	
	//获取气象监测站空间数据
	public List<SpaceInfo> getQXJCZGeomList(Map<String, Object> map);
	
	//查询气象监测站监测数据
	public List<WeatherData> getWeatherJCZMonitorData(Map<String, Object> map);
	
	//微波辐射报警查询
	public List<Map<String, Object>> getThrList(Map<String, Object> map);
	
	//微站空气质量与气象数据联合查询(一张图-微站监测-微站监测)
	public List<Map<String, Object>> selectKqAndQx(Map<String, Object> map);
}
