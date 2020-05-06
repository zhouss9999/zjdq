package wy.qingdao_atmosphere.onemap.service;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.math.RoundingMode;

import javax.servlet.http.HttpServletRequest;

import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.onemap.dao.OneMapDao;
import wy.qingdao_atmosphere.onemap.domain.KqzlColl;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.qingdao_atmosphere.onemap.domain.OmRank;
import wy.qingdao_atmosphere.onemap.domain.PicInfo;
import wy.qingdao_atmosphere.onemap.domain.WeatherData;
import wy.util.AirBean;
import wy.util.Calc;
import wy.util.DatapointsApiThreadUtil;
import wy.util.OperatLinuxNcl;
import wy.util.RedisTemplateUtil;
import wy.util.datapersistence.SpaceInfo;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.util.datapersistence.service.BaseService;


@Service("oneMapService")
public class OneMapServiceImpl implements OneMapService {

	@Autowired
	private BaseaddDao baseaddDao;
	
	@Autowired 
	private BaseService baseService;
	
	@Autowired
	private OneMapDao oneMapDao;
	
	@Autowired
	private RedisTemplateUtil redisTemplate;


	public List<OmMenu> getOmMenuList() {
		//原始数据
		List<OmMenu> rootlist = oneMapDao.getOmMenuList();
		//创建list，存放最后的结果
		List<OmMenu> menuList = new ArrayList<OmMenu>();
		
		if (rootlist.size() > 0) {
			//先找到所有的一级目录
			for (OmMenu menu : rootlist) {
				//一级目录的父级目录id为'0'
				if ("0".equals(menu.getParentid())) {
					menuList.add(menu);
				}
			}
			
			//为一级目录设置子目录,getChild是递归调用的
			for (OmMenu menu : menuList) {
				menu.setChildList(getChild(String.valueOf(menu.getId()),rootlist));
			}
		}
		
		return menuList;
	}
	
	/**
	 * 获取浙江大气的目录
	 * @return
	 */
	public List<OmMenu> getdqMenuList() {
		//原始数据
		List<OmMenu> rootlist = oneMapDao.getdqMenuList();
		//创建list，存放最后的结果
		List<OmMenu> menuList = new ArrayList<OmMenu>();
		
		if (rootlist.size() > 0) {
			//先找到所有的一级目录
			for (OmMenu menu : rootlist) {
				//一级目录的父级目录id为'0'
				if ("0".equals(menu.getParentid())) {
					menuList.add(menu);
				}
			}
			
			//为一级目录设置子目录,getChild是递归调用的
			for (OmMenu menu : menuList) {
				menu.setChildList(getChild(String.valueOf(menu.getId()),rootlist));
			}
		}
		
		return menuList;
	}

	
	/**
	 * 递归查找子菜单
	 * @param id 当前目录id
	 * @param rootMenu 要查找的列表
	 * @return childList
	 */
	private List<OmMenu> getChild(String id, List<OmMenu> rootMenu) {
	    //子目录
	    List<OmMenu> childList = new ArrayList<OmMenu>();
	    for (OmMenu menu : rootMenu) {
	        // 遍历所有节点，将父目录id与传过来的id比较
	        if (!("0".equals(menu.getParentid()))) {
	            if (menu.getParentid().equals(id)) {
	                childList.add(menu);
	            }
	        }
	    }
	    // 把子目录的子目录再循环一遍
	    for (OmMenu menu : childList) {
            // 递归
            menu.setChildList(getChild(String.valueOf(menu.getId()), rootMenu));
	    } 
	    // 递归退出条件
	    if (childList.size() == 0) {
	        return new ArrayList<OmMenu>();
	    }
	    return childList;
	}
	
	
	public List<KqzlColl> getOmAllSiteForKqzl(HttpServletRequest request, Map<String, Object> paramMap) {
		
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		
		paramMap.put("objid", objid);
		paramMap.put("objtypeid", "1,5");
		
		if (paramMap.containsKey("history")) {
			//时间格式：yyyy-MM-dd
			String begintime = request.getParameter("begintime") == null ? "" : request.getParameter("begintime");
			String endtime = request.getParameter("endtime") == null ? "" : request.getParameter("endtime");
			
			if ("".equals(begintime) && "".equals(endtime)) {
				paramMap.put("recently", "24 hour");
				paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
				paramMap.put("sitenumberid", "1010001,5010001");
			} else {
				if (!"".equals(begintime) && !"".equals(endtime)) {
					if ((begintime).equals(endtime)) {//同一天
						begintime = begintime +" 00:00:00";
						endtime = endtime +" 23:59:59";
						
						paramMap.put("begintime", begintime);
						paramMap.put("endtime", endtime);
						paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
						paramMap.put("sitenumberid", "1010001,5010001");
					} else {//不同一天
						begintime = begintime +" 00:00:00";
						endtime = endtime +" 23:59:59";
						
						paramMap.put("begintime", begintime);
						paramMap.put("endtime", endtime);
						paramMap.put("dateformat", "yyyy-mm-dd");
						paramMap.put("sitenumberid", "2010002,6010002");
					}
				} else {
					if (!"".equals(begintime)) {
						begintime = begintime +" 00:00:00";
					}
					if (!"".equals(endtime)) {
						endtime = endtime +" 23:59:59";
					}
					
					paramMap.put("begintime", begintime);
					paramMap.put("endtime", endtime);
					paramMap.put("dateformat", "yyyy-mm-dd");
					paramMap.put("sitenumberid", "2010002,6010002");
				}
			}
		}
		
		List<KqzlColl> kqzlList = oneMapDao.getOmAllSiteDatas(paramMap);
		
		return kqzlList;
	}


	public Map<String, Object> getOmSiteGeom(HttpServletRequest request, String mark) {
		
		//给出默认值:默认给国站的
		int objtypeid = 5;
		//空间表名
		String space_tablename = "space_countrysite";
		
		if ("wz".equals(mark)) {						//微站
			objtypeid = 1;
			space_tablename = "space_site";
		} else if ("fs".equals(mark)) {					//辐射站
			objtypeid = 12;
			space_tablename = "space_wbstation";
		} else if ("tk".equals(mark)) {					//探空站
			objtypeid = 11;
			space_tablename = "space_cityaircurve";
		}	
		
		//传参Map
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("objtypeid", objtypeid);
		
		/**========================================请求参数=========================================================*/
		//行政区域(多个用逗号隔开)
		//原青岛（默认只查青岛）
		//String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "青岛" : request.getParameter("city");
		//city为空，默认都查
		String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "" : request.getParameter("city");

		//微站监测类型(多个用逗号隔开)
		String monitortypes = request.getParameter("monitortype") == null ? "" : request.getParameter("monitortype");
		String monitortype = "";
		if (!"".equals(monitortypes)) {
			for (int i = 0; i < monitortypes.split(",").length; i++) {
				if (i != monitortypes.split(",").length - 1) {
					monitortype += "'"+ monitortypes.split(",")[i] + "',";
				} else {
					monitortype += "'"+ monitortypes.split(",")[i] + "'";
				}
			}		
		}
		
		//站点objid(多个用逗号隔开)
		String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
		/**======================================================================================================*/
		
		paramMap.put("city", city);
		paramMap.put("monitortype", monitortype);
		paramMap.put("objid", objid);
		
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		if ("fs".equals(mark)) {//辐射站点坐标信息
			//查站点基本信息
			List<Map<String,Object>> siteList = baseService.selectForCpAttachInfoStoreTwo(paramMap, "getOmObjInfoList");
			resultMap = baseService.getGeoJsonFormat(space_tablename, objid, siteList);
		} else if ("tk".equals(mark)) {//探空曲线坐标信息
			List<Map<String,Object>> siteList = baseService.selectForCpAttachInfoStoreTwo(paramMap, "getOmObjInfoList");
			resultMap = baseService.getGeoJsonFormat(space_tablename, objid, siteList);
		}else {//国站微站坐标信息
			paramMap.put("recently", "1 second");
			paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");		
			
			List<KqzlColl> siteList = new ArrayList<KqzlColl>();
			if ("wz".equals(mark)) {
				paramMap.put("sitenumberid", "1010001");
				siteList = oneMapDao.getOmAllSiteDatas(paramMap);
			} else {
				paramMap.put("sitenumberid", "5010001");
				siteList = oneMapDao.getOmAllSiteDatas(paramMap);
			}
			resultMap = baseService.getGeoJsonFormat(space_tablename, objid, siteList);
		}
		
		return resultMap;
	}
	
	public StringWriter getQXOmSiteGeom(HttpServletRequest request) {
		
		//气象监测站objtypeid
		int objtypeid = 19;
		//空间表名
		String space_tablename = "space_weather";	
		
		//传参Map
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("objtypeid", objtypeid);
		
		/**========================================请求参数=========================================================*/
		//行政区域(多个用逗号隔开)
		String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "青岛" : request.getParameter("city");
		//站点objid(多个用逗号隔开)
		String objid = request.getParameter("objid") == null ? "927,591,590,926,928" : request.getParameter("objid");
		/**======================================================================================================*/
		
		paramMap.put("city", city);
		paramMap.put("objid", objid);
		
		//站点空间位置信息列表(list转map，优化程序)
		List<SpaceInfo> siteGeomList = getQXJCZGeomList(space_tablename, objid);
		Map<Integer, String> siteGeomMap = new LinkedHashMap<Integer, String>();
		if (siteGeomList.size() > 0) {
			for (SpaceInfo spaceInfo : siteGeomList) {
				siteGeomMap.put(spaceInfo.getObjid(), spaceInfo.getShape());
			}
		}
		
		StringWriter writer = new StringWriter();
		
		try {
			SimpleFeatureType TYPE = DataUtilities.createType("Link", "geometry:Point,objid:Integer,jcz_name:String,"+
					"jcz_id:String,jcz_sheng:String,jcz_shi:String,jcz_jd:String,jcz_wd:String,jcz_x:String,jcz_y:String,jcz_webid:String,"
				);
				List<Map<String,Object>> siteList = baseService.selectForCpAttachInfoStoreTwo(paramMap, "getOmObjInfoList");
				
				writer = getSiteGeoJson(TYPE, siteList, siteGeomMap);
				
		} catch (SchemaException e) {
			e.printStackTrace();
		}
		
		
		return writer;
	}
	

	public List<KqzlColl> getOmGzSiteForKqzl(HttpServletRequest request, Map<String, Object> paramMap) {
		
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		
		paramMap.put("objid", objid);
		paramMap.put("objtypeid", "5");
		
		if (paramMap.containsKey("history")) {
			//时间格式：yyyy-MM-dd
			String begintime = request.getParameter("begintime") == null ? "" : request.getParameter("begintime");
			String endtime = request.getParameter("endtime") == null ? "" : request.getParameter("endtime");
			
			if ("".equals(begintime) && "".equals(endtime)) {
				paramMap.put("recently", "24 hour");
				paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
				paramMap.put("sitenumberid", "5010001");
			} else {
				if (!"".equals(begintime) && !"".equals(endtime)) {
					if ((begintime).equals(endtime)) {//同一天
						begintime = begintime +" 00:00:00";
						endtime = endtime +" 23:59:59";
						
						paramMap.put("begintime", begintime);
						paramMap.put("endtime", endtime);
						paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
						paramMap.put("sitenumberid", "5010001");
					} else {//不同一天
						begintime = begintime +" 00:00:00";
						endtime = endtime +" 23:59:59";
						
						paramMap.put("begintime", begintime);
						paramMap.put("endtime", endtime);
						paramMap.put("dateformat", "yyyy-mm-dd");
						paramMap.put("sitenumberid", "6010002");
					}
				} else {
					if (!"".equals(begintime)) {
						begintime = begintime +" 00:00:00";
					}
					if (!"".equals(endtime)) {
						endtime = endtime +" 23:59:59";
					}
					
					paramMap.put("begintime", begintime);
					paramMap.put("endtime", endtime);
					paramMap.put("dateformat", "yyyy-mm-dd");
					paramMap.put("sitenumberid", "6010002");
				}
			}
		}
		
		List<KqzlColl> kqzlList = oneMapDao.getOmAllSiteDatas(paramMap);
		
		return kqzlList;
	}
	
	
	public List<KqzlColl> getOmWzSiteForKqzl(HttpServletRequest request, Map<String, Object> paramMap) {
		
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		
		paramMap.put("objid", objid);
		paramMap.put("objtypeid", "1");
		
		if (paramMap.containsKey("history")) {
			//时间格式：yyyy-MM-dd
			String begintime = request.getParameter("begintime") == null ? "" : request.getParameter("begintime");
			String endtime = request.getParameter("endtime") == null ? "" : request.getParameter("endtime");
			
			if ("".equals(begintime) && "".equals(endtime)) {
				paramMap.put("recently", "24 hour");
				paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
				paramMap.put("sitenumberid", "1010001");
			} else {
				if (!"".equals(begintime) && !"".equals(endtime)) {
					if ((begintime).equals(endtime)) {//同一天
						begintime = begintime +" 00:00:00";
						endtime = endtime +" 23:59:59";
						
						paramMap.put("begintime", begintime);
						paramMap.put("endtime", endtime);
						paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
						paramMap.put("sitenumberid", "1010001");
					} else {//不同一天
						begintime = begintime +" 00:00:00";
						endtime = endtime +" 23:59:59";
						
						paramMap.put("begintime", begintime);
						paramMap.put("endtime", endtime);
						paramMap.put("dateformat", "yyyy-mm-dd");
						paramMap.put("sitenumberid", "2010002");
					}
				} else {
					if (!"".equals(begintime)) {
						begintime = begintime +" 00:00:00";
					}
					if (!"".equals(endtime)) {
						endtime = endtime +" 23:59:59";
					}
					
					paramMap.put("begintime", begintime);
					paramMap.put("endtime", endtime);
					paramMap.put("dateformat", "yyyy-mm-dd");
					paramMap.put("sitenumberid", "2010002");
				}
			}
		}
		
		List<KqzlColl> kqzlList = oneMapDao.getOmAllSiteDatas(paramMap);
		
		return kqzlList;
	}


	public Map<String, Object> omFsData(HttpServletRequest request,String datastreamIds, String paramid) {
		
		//必传
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
		
		//获取数据库辐射数据近72小时时间
		String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
		
		//按时间查询 格式：yyyy-MM-dd
		String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime") +" 00:00:00";
		String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime") +" 23:59:59";
		
		/* 方式一：不使用Redis
		Map<String, Object> resultMap = getDataMap("5", objid, datastreamIds, begintime, endtime, null);
		return resultMap;*/
		
		/*方式二：使用Redis*/
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		//Redis数据库的key			yyyy-MM-dd hh
		String key = objid+datastreamIds.replace(",", "")
				+paramid.replace(",", "")
				+begintime.substring(0, 13).replace(" ", "")
				+endtime.substring(0, 13).replace(" ", "");
		//查询这个Redis数据库是否有这个key
		//boolean existsCathValue = redisTemplate.existsValue(1,key);
		boolean existsCathValue=false; //由于此时redis连不上，所以我先写为false,不然会报错
		//判断Redis数据库是否有key有则取Redis缓存数据，否则就往Redis数据库添加数据-----------
		if(existsCathValue){
			//取Redis数据库的数据
			String str = redisTemplate.getCatchForStringTwo(key);
			if (str.contains("=")) {
				String[] strArr = ((str.substring(1,str.length()-1)).replace("],", "],-")).split(",-");
				
				//将字符串转Map
				for (int i = 0; i < strArr.length; i++) {
					String[] resultArr = strArr[i].split("=");
					resultMap.put(resultArr[0].trim(), JSONArray.fromObject(resultArr[1].trim()));
				}
			}
		}else{
			//根据时间获取辐射数据信息
			resultMap = getDataMap("5", objid, datastreamIds, begintime, endtime, null);
			//往Redis数据库存储数据，时间定为2小时
			//**redis现在有问题，先注释--------------------
			//redisTemplate.setCatchByStringTwo(60*2, resultMap.toString(), key);
		}	
		
		return resultMap;
	}
	
	
	public Map<String, Object> omFsDataSecond(HttpServletRequest request,String datastreamIds, String paramid) {
		
		//必传
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
		
		//获取数据库辐射数据近1小时时间
		String[] colMinMaxTime = colMinMaxTimeByHour("5", 1, objid, paramid, "");
		
		//按时间查询 格式：yyyy-MM-dd HH:mm:ss
		String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime");
		String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime");
		
		/* 方式一：不使用Redis
		Map<String, Object> resultMap = getDataMap("5", objid, datastreamIds, begintime, endtime, null);
		return resultMap;*/
		
		/*方式二：使用Redis*/
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		//Redis数据库的key			yyyy-MM-dd hh
		String key = objid+datastreamIds.replace(",", "")
				+paramid.replace(",", "")
				+begintime.substring(0, 13).replace(" ", "")
				+endtime.substring(0, 13).replace(" ", "");
		//查询这个Redis数据库是否有这个key
		boolean existsCathValue = redisTemplate.existsValue(1,key);
		//判断Redis数据库是否有key有则取Redis缓存数据，否则就往Redis数据库添加数据
		if(existsCathValue){
			//取Redis数据库的数据
			String str = redisTemplate.getCatchForStringTwo(key);
			if (str.contains("=")) {
				String[] strArr = ((str.substring(1,str.length()-1)).replace("],", "],-")).split(",-");
				
				//将字符串转Map
				for (int i = 0; i < strArr.length; i++) {
					String[] resultArr = strArr[i].split("=");
					resultMap.put(resultArr[0].trim(), JSONArray.fromObject(resultArr[1].trim()));
				}
			}
		}else{
			//根据时间获取辐射数据信息
			resultMap = getDataMap("5", objid, datastreamIds, begintime, endtime, null);
			//往Redis数据库存储数据，时间定为2小时
			redisTemplate.setCatchByStringTwo(60*2, resultMap.toString(), key);
		}	
		
		return resultMap;
	}

	
	public List<Map<String, Object>> getThrList(HttpServletRequest request){
		
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		//报警类型
		String thrtype = request.getParameter("thrtype") == null ? "" : request.getParameter("thrtype");
		//时间类型(当天传"1",近三天传"3")
		String timetype = (request.getParameter("timetype") == null || "".equals(request.getParameter("timetype"))) ? "1" : request.getParameter("timetype"); 
		//站点名称
		String sitename = request.getParameter("sitename") == null ? "" : request.getParameter("sitename");
		//报警id(多个用逗号隔开)
		String thrid = request.getParameter("thrid") == null ? "" : request.getParameter("thrid");
		//站点objid(多个用逗号隔开)
		String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
				
		//报警时间
		String begintime = "";
		String endtime = "";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String time = sdf.format(date);
		if ("1".equals(timetype)) {
			begintime = time + " 00:00:00";
			endtime = time + " 23:59:59";
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-3);
			
			begintime = sdf.format(calendar.getTime()) + " 00:00:00";
			endtime = time + " 23:59:59";
		}
				
		paramMap.put("thrtype", thrtype);		
		paramMap.put("begintime", begintime);		
		paramMap.put("endtime", endtime);		
		paramMap.put("sitename", sitename);		
		paramMap.put("thrid", thrid);		
		paramMap.put("objid", objid);		
				
		List<Map<String, Object>>	resultList = oneMapDao.getThrList(paramMap);
				
		return resultList;		
	}
	
	
	public Map<String, Object> getOmAllSiteGeom(HttpServletRequest request, String mark) {
		
		String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "青岛" : request.getParameter("city");
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");	
		
		//获取所有站点坐标(空间表)
		String space_tablename = "space_site union select objid,id,ST_AsGeoJSON(shape) as geometry from space_countrysite";
		
		if ("wx".equals(mark)) {
			space_tablename = "space_satellite";
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("city", city);
		paramMap.put("objid", objid);
		
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if ("rd".equals(mark)) {//热点分析
			//各个站点近30天的优良率及监测参数均值
			//paramMap.put("recently", "30 day");
			//各个站点近7天(一周)的非优良率()及监测参数均值
			paramMap.put("recently", "7 day");
			paramMap.put("dateformat", "yyyy-mm-dd");
			paramMap.put("objtypeid", "1,5");
			paramMap.put("sitenumberid", "2010002,6010002");
			
			
			List<Map<String, Object>> siteList = oneMapDao.getOmAllSiteGoodRate(paramMap);
			
			DecimalFormat df = new DecimalFormat("#0.0");//保留一位小数(奇进偶舍)
			df.setRoundingMode(RoundingMode.HALF_UP);//四舍五入
			for (int i = 0; i < siteList.size(); i++) {
				Map<String, Object> map = siteList.get(i);
				double badrate = 100 - Double.parseDouble(String.valueOf(map.get("goodrate")));
				
				map.put("badrate", Double.parseDouble(df.format(badrate)));
			}
						
			resultMap = baseService.getGeoJsonFormat(space_tablename, objid, siteList);
		} else if ("wx".equals(mark)){//卫星云图
			paramMap.put("objtypeid", "13");
			//站点基本信息列表
			List<Map<String,Object>> siteList = baseService.selectForCpAttachInfoStoreTwo(paramMap, "getOmObjInfoList");
			
			resultMap = baseService.getGeoJsonFormat(space_tablename, objid, siteList);
		} else {//精细化,搜索
			paramMap.put("recently", "1 second");
			paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
			paramMap.put("objtypeid", "1,5");
			paramMap.put("sitenumberid", "1010001,5010001");
			
			//各个站点最新的监测数据
			List<KqzlColl> siteList = oneMapDao.getOmAllSiteDatas(paramMap);
			resultMap = baseService.getGeoJsonFormat(space_tablename, objid, siteList);
		}
		
		return resultMap;
	}


	public List<Map<String, Object>> getOmPicList(HttpServletRequest request, String objtypeid) {
		
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "" : request.getParameter("objid");
		String begintime = request.getParameter("begintime") == null ? "" : request.getParameter("begintime");
		String endtime = request.getParameter("endtime") == null ? "" : request.getParameter("endtime");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objtypeid", objtypeid);
		paramMap.put("objid", objid);
		paramMap.put("begintime", begintime);
		paramMap.put("endtime", endtime);
		if ("".equals(begintime) || "".equals(endtime)) {
			paramMap.put("notime", "24 hour");
		} else {
			paramMap.put("yestime", "yestime");
		}
		
		List<Map<String, Object>> siteDetail = baseService.selectDetailForCpAttachInfoStoreTwo(Integer.parseInt(objid));
		
		for (int i = 0; i < siteDetail.size(); i++) {
			paramMap.put("objid", siteDetail.get(i).get("objid").toString());
			//查询图片信息
			List<PicInfo> picInfoList = oneMapDao.getPicInfoList(paramMap);
			siteDetail.get(i).put("picInfoList", picInfoList);
		}
		
		return siteDetail;
	}


	public Map<String, Object> getOmSiteYll(HttpServletRequest request) {
		
		//必传
		String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		paramMap.put("recently", "30 day");
		paramMap.put("dateformat", "yyyy-mm-dd");
		paramMap.put("objtypeid", "1,5");
		paramMap.put("objid", objid);
		paramMap.put("sitenumberid", "2010002,6010002");
		
		//近day天的监测数据
		List<KqzlColl> daylist = oneMapDao.getOmAllSiteDatas(paramMap);
		
		int good = 0;
		int size = daylist.size();
		if (daylist.size() > 0) {
			for (KqzlColl kc : daylist) {
				if ("优".equals(kc.getQuality()) || "良".equals(kc.getQuality())) {
					good += 1;
				}
			}
		} else {
			size = 1;
		}
		
		DecimalFormat df = new DecimalFormat("#0.0");//保留一位小数(奇进偶舍)
		df.setRoundingMode(RoundingMode.HALF_UP);//四舍五入
		
		paramMap.clear();
		paramMap.put("goodday", good);//优良天数
		paramMap.put("goodrate", df.format(((double)good/size)*100));//优良率
		paramMap.put("daydata", daylist);//近day天的空气质量监测值
		
		return paramMap;
	}


	public List<OmRank> omRankBySite(HttpServletRequest request, String timetype) {
		
		//默认显示实时数据
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "青岛" : request.getParameter("city");
		
		paramMap.put("city", city);
		//paramMap.put("objtypeid", "1,5");//国站微站
		paramMap.put("objtypeid", "1");//微站
		
		//时间
		String datetime = (request.getParameter("datetime") == null) ? "" : request.getParameter("datetime");
		
		String[] hourMaxTime = new String[2];
		String[] dayMaxTime = new String[2];
		if ("".equals(datetime)) {
			//获取微站数据库最大时间
			hourMaxTime = colMinMaxTimeByHour("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "1");
			dayMaxTime = colMinMaxTimeByDay("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "2");
			/*//获取国站微站数据库最大时间
			hourMaxTime = colMinMaxTimeByHour("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "1,5");
			dayMaxTime = colMinMaxTimeByDay("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "2,6");*/
		}
		
		//创建返回对象
		List<OmRank> ranks = new ArrayList<OmRank>();
		
		if ("day".equals(timetype)) {				//日AQI排名
			//时间格式：yyyy-MM-dd
			if ("".equals(datetime)) {
				paramMap.put("begintime", dayMaxTime[0]);
				paramMap.put("endtime", dayMaxTime[1]);
			} else {
				paramMap.put("begintime", datetime +" 00:00:00");
				paramMap.put("endtime", datetime +" 23:59:59");
			}
			
			paramMap.put("dateformat", "yyyy-mm-dd");
			//paramMap.put("sitenumberid", "2010002,6010002");//国站微站
			paramMap.put("sitenumberid", "2010002");//微站
			paramMap.put("aqi", "aqi");
			
			ranks = oneMapDao.omRankBySite(paramMap);
			
		} else if ("month".equals(timetype)) {		//月优良率排名
			//时间格式：yyyy-MM
			if ("".equals(datetime)) {
				paramMap.put("begintime", dayMaxTime[1].substring(0, 7) +"-01 00:00:00");
				paramMap.put("endtime", getLastDayOfMonth(dayMaxTime[1].substring(0, 7)));
			} else {
				paramMap.put("begintime", datetime +"-01 00:00:00");
				paramMap.put("endtime", getLastDayOfMonth(datetime));
			}
			
			paramMap.put("dateformat", "yyyy-mm");
			//paramMap.put("sitenumberid", "2010002,6010002");//国站微站
			paramMap.put("sitenumberid", "2010002");//微站
			paramMap.put("goodrate", "goodrate");
			
			ranks = oneMapDao.omRankBySite(paramMap);
			
		} else {									//实时AQI排名
			//时间格式：yyyy-MM-dd HH
			if ("".equals(datetime)) {
				paramMap.put("begintime", hourMaxTime[1]);
				paramMap.put("endtime", hourMaxTime[1]);
			} else {
				paramMap.put("begintime", datetime +":00:00");
				paramMap.put("endtime", datetime +":00:00");
			}
			
			paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
			//paramMap.put("sitenumberid", "1010001,5010001");//国站微站
			paramMap.put("sitenumberid", "1010001");//微站
			paramMap.put("aqi", "aqi");
			
			ranks = oneMapDao.omRankBySite(paramMap);
		}
		
		return ranks;
	}


	public List<OmRank> omRankByArea(HttpServletRequest request, String timetype) {
		
		//默认显示实时数据
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "青岛" : request.getParameter("city");
		
		paramMap.put("city", city);
		//paramMap.put("objtypeid", "1,5");//国站微站
		paramMap.put("objtypeid", "1");//微站
		
		String datetime = (request.getParameter("datetime") == null) ? "" : request.getParameter("datetime");
		
		String[] hourMaxTime = new String[2];
		String[] dayMaxTime = new String[2];
		if ("".equals(datetime)) {
			//获取微站数据库最大时间
			hourMaxTime = colMinMaxTimeByHour("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "1");
			dayMaxTime = colMinMaxTimeByDay("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "2");
			/*//获取国站微站数据库最大时间
			hourMaxTime = colMinMaxTimeByHour("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "1,5");
			dayMaxTime = colMinMaxTimeByDay("1,4", 0, "", "1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9", "2,6");*/
		}
		
		//创建返回对象
		List<OmRank> ranks = new ArrayList<OmRank>();
		
		if ("day".equals(timetype)) {			//日AQI排名
			//时间格式：yyyy-MM-dd
			if ("".equals(datetime)) {
				paramMap.put("begintime", dayMaxTime[0]);
				paramMap.put("endtime", dayMaxTime[1]);
			} else {
				paramMap.put("begintime", datetime +" 00:00:00");
				paramMap.put("endtime", datetime +" 23:59:59");
			}
			
			paramMap.put("dateformat", "yyyy-mm-dd");
			//paramMap.put("sitenumberid", "2010002,6010002");//国站微站
			paramMap.put("sitenumberid", "2010002");//微站
			paramMap.put("aqi", "aqi");
			
			ranks = oneMapDao.omRankByArea(paramMap);
			
		} else if ("month".equals(timetype)) { //月优良率排名
			//时间格式：yyyy-MM
			if ("".equals(datetime)) {
				paramMap.put("begintime", dayMaxTime[1].substring(0, 7) +"-01 00:00:00");
				paramMap.put("endtime", getLastDayOfMonth(dayMaxTime[1].substring(0, 7)));
			} else {
				paramMap.put("begintime", datetime +"-01 00:00:00");
				paramMap.put("endtime", getLastDayOfMonth(datetime));
			}
			
			paramMap.put("dateformat", "yyyy-mm");
			//paramMap.put("sitenumberid", "2010002,6010002");//国站微站
			paramMap.put("sitenumberid", "2010002");//微站
			paramMap.put("goodrate", "goodrate");
			
			ranks = oneMapDao.omRankByArea(paramMap);
			
		} else {					//实时AQI排名
			//时间格式：yyyy-MM-dd HH
			if ("".equals(datetime)) {
				paramMap.put("begintime", hourMaxTime[1]);
				paramMap.put("endtime", hourMaxTime[1]);
			} else {
				paramMap.put("begintime", datetime +":00:00");
				paramMap.put("endtime", datetime +":00:00");
			}
			
			paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
			//paramMap.put("sitenumberid", "1010001,5010001");//国站微站
			paramMap.put("sitenumberid", "1010001");//微站
			paramMap.put("aqi", "aqi");
			
			ranks = oneMapDao.omRankByArea(paramMap);
			
		}
		
		return ranks;
	}

	
	public Map<String, Object> getCityAreaGeom(HttpServletRequest request){
		
		String cityname = (request.getParameter("cityname") == null && "".equals(request.getParameter("cityname"))) ? "青岛" : request.getParameter("cityname");
		String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		//paramMap.put("objtypeid", 24);
		paramMap.put("objtypeid", 16);
		paramMap.put("cityname", cityname);
		paramMap.put("objid", objid);
		
		//县市区域基本信息列表
		List<Map<String,Object>> siteList = baseService.selectForCpAttachInfoStoreTwo(paramMap, "getOmObjInfoList");
		
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		//resultMap = baseService.getGeoJsonFormat("space_cityarea", objid, siteList);
		resultMap = baseService.getGeoJsonFormat("space_area", objid, siteList);
		
		return resultMap;
	}
	
	
	/**
	 * 站点列表及坐标信息转换为geoJson格式
	 * 
	 * @param TYPE:定义坐标系及字段
	 * @param siteList:站点基本信息列表
	 * @param siteGeomMap:站点坐标列表
	 * 
	 * @return writer
	 */
	private StringWriter getSiteGeoJson(SimpleFeatureType TYPE, List<Map<String,Object>> siteList, Map<Integer, String> siteGeomMap) {
		
		StringWriter writer = new StringWriter();
		try {
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
			GeometryFactory geometryFactory = new GeometryFactory();
			WKTReader reader = new WKTReader(geometryFactory);
			FeatureJSON fjson = new FeatureJSON();
			List<SimpleFeature> features = new ArrayList<SimpleFeature>();
			SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
			
			if (siteList.size() > 0) {
				for (int i = 0; i < siteList.size(); i++) {
					Map<String, Object> siteMap = siteList.get(i);
					
					int geoMapKey = Integer.parseInt(siteMap.get("objid").toString());
					if (siteGeomMap.containsKey(geoMapKey)) {
						
						Point point = (Point)reader.read(siteGeomMap.get(geoMapKey));
						featureBuilder.add(point);//坐标
						
						for(Map.Entry<String, Object> entry : siteMap.entrySet()){
							if ("objid".equals(entry.getKey())) {
								featureBuilder.add(geoMapKey);//objid
							} else {
								featureBuilder.add(entry.getValue().toString());
							}
				        }
						
						SimpleFeature feature = featureBuilder.buildFeature(null);
						
						features.add(feature);
						
					}
				} 
			}
			
			fjson.writeFeatureCollection(collection, writer);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (com.vividsolutions.jts.io.ParseException e) {
			e.printStackTrace();
		}
		
		return writer;
	}
	
	/**
	 * 获取数据库监测数据最大时间(maxtime)和(maxtime-n小时)的时间
	 * @param devicetypeid 设备类型id
	 * @param hour 小时
	 * @param objid 对象id
	 * @param paramid 参数id
	 * @param objtypeid 对象类型id
	 * @return {mintime,maxtime}
	 * 
	 * 空气质量paramid：1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9 -->
	 * 空气质量objtypeid：实时数据(1,5),日数据(2,6) 
	 */
	private String[] colMinMaxTimeByHour(String devicetypeid, int hour, String objid, String paramid, String objtypeid){
		//起始时间
		//获取数据库中辐射监测数据的最新时间
		String maxtime = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd HH24:MI:ss", devicetypeid, paramid, objtypeid, objid);
		//获取最大时间往前hour小时的时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			if (maxtime != null && !"".equals(maxtime)) {
					date = sdf.parse(maxtime);
			} else {
				maxtime = sdf.format(date);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-hour);//(maxtime-n小时)的时间
		String mintime = sdf.format(calendar.getTime());
		
		String[] times = {mintime,maxtime};
		
		return times;
	}
	
	
	/**
	 * 获取数据库监测数据最大时间(maxtime)和(maxtime-n天)的时间
	 * @param devicetypeid 设备类型id
	 * @param day 天
	 * @param objid 对象id
	 * @param paramid 参数id
	 * @param objtypeid 对象类型id
	 * @return {mintime,maxtime}
	 * 
	 * 空气质量paramid：1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9 -->
	 * 空气质量objtypeid：实时数据(1,5),日数据(2,6)
	 */
	private String[] colMinMaxTimeByDay(String devicetypeid, int day, String objid, String paramid, String objtypeid){
		//起始时间
		//获取数据库中辐射监测数据的最新时间
		String maxtime = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd", devicetypeid, paramid, objtypeid, objid);
		//获取最大时间往前hour小时的时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		try {
			if (maxtime != null && !"".equals(maxtime)) {
				date = sdf.parse(maxtime);
			} else {
				maxtime = sdf.format(date);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-day);//(maxtime-n天)的时间
		String mintime = sdf.format(calendar.getTime());
		
		String[] times = {mintime + " 00:00:00",maxtime + " 23:59:59"};
		
		return times;
	}
	
	
	/**
     * 获取指定年月的最后一天
     * @param year
     * @param month
     * @return
     */
    private static String getLastDayOfMonth(String yearmonth) {     
        if(!"".equals(yearmonth) && yearmonth.contains("-")){
        	String[] YMArr = yearmonth.split("-");
	    	Calendar cal = Calendar.getInstance();     
	        //设置年份  
	        cal.set(Calendar.YEAR, Integer.parseInt(YMArr[0]));  
	        //设置月份  
	        cal.set(Calendar.MONTH, Integer.parseInt(YMArr[1])-1); 
	        //获取某月最大天数
	        int lastDay = cal.getActualMaximum(Calendar.DATE);
	        //设置日历中月份的最大天数  
	        cal.set(Calendar.DAY_OF_MONTH, lastDay);  
	        //格式化日期
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	        return sdf.format(cal.getTime())+" 23:59:59";
        }else{
        	return "";
        }
    }
    
	
	/**
	 * 数据点查询(单个站点)
	 * @param devicetypeid:设备类型id,String
	 * @param objid:对象id,String
	 * @param datastreamids:查询的数据流，多个数据流之间用逗号分隔（可选）,String
	 * @param startTime:提取数据点的开始时间（可选）,String
	 * @param endTime:提取数据点的结束时间（可选）,String
	 * @param cursor:指定本次请求继续从cursor位置开始提取数据（可选）,String
	 * 
	 * @return dataMap
	 */
	private Map<String, Object> getDataMap(String devicetypeid, String objid, String datastreamIds, String startTime, String endTime,String cursor){
		
		//传参map
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("devicetypeid", devicetypeid);
		paramMap.put("objid", objid);
		ParamAssis dk = oneMapDao.getDevidKey(paramMap);
		
		//用于存放数据返回
		Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
		
		dataMap = DatapointsApiThreadUtil.getDatapointsApi(datastreamIds, dk.getDevicenumber(), dk.getRmk1(), 
				startTime, endTime, cursor, "DESC", dataMap);
		
		//计算时间间隔
		long diffhours = getDistanceHours(startTime, endTime);
		if (diffhours == 0) {
			diffhours = 1;
		}
		double interval = Math.ceil(diffhours/(double)24);//向上取整
		
		//按指定间隔显示数据
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
			JSONArray jsonArr = (JSONArray) entry.getValue();
			JSONArray resultJA = new JSONArray();
			
			int i = 0;
			int jaSize = jsonArr.size();
			while(i < jaSize){
				resultJA.add(jsonArr.getJSONObject(i));
				i += interval;
				if (i >= jaSize - 1) {
					i = jaSize - 1;
					resultJA.add(jsonArr.getJSONObject(i));
					break;
				}
			}
			
			resultMap.put(entry.getKey(), resultJA);
		}
		
		return resultMap;
	}
     
	//获取气象监测站空间数据
	public List<SpaceInfo> getQXJCZGeomList(String space_tablename,String objid){
		//传参map
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("space_tablename", space_tablename);
		paramMap.put("objid", objid);
		return oneMapDao.getQXJCZGeomList(paramMap);
	}
	
	//获取气象监测站实时或者历史数据
	public List<WeatherData> getWeatherJCZMonitorData(HttpServletRequest request) {
		//objid
		String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
		//起始时间
		String begintime = request.getParameter("begintime") == null ? "" : request.getParameter("begintime");
		//结束时间
		String endtime = request.getParameter("endtime") == null ? "" : request.getParameter("endtime");
		//类型
		String type = request.getParameter("type") == null ? "" : request.getParameter("type");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objid", objid);
		paramMap.put("begintime", begintime);
		paramMap.put("endtime", endtime);
		paramMap.put("type", type);
		return oneMapDao.getWeatherJCZMonitorData(paramMap);
	}


	public Map<String, Object> omRdSpatialDv(HttpServletRequest request) {

		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		resultMap.put("x0", OperatLinuxNcl.x0);
		resultMap.put("y0", OperatLinuxNcl.y0);
		resultMap.put("x1", OperatLinuxNcl.x1);
		resultMap.put("y1", OperatLinuxNcl.y1);
		
		resultMap.put("aqi", "-");
		resultMap.put("pm25", "-");
		resultMap.put("pm10", "-");
		resultMap.put("co", "-");
		resultMap.put("no2", "-");
		resultMap.put("so2", "-");
		resultMap.put("o3", "-");
		resultMap.put("o38", "-");
		
		//气象五参
		resultMap.put("wd", "-");
		resultMap.put("sd", "-");
		resultMap.put("fl", "-");
		resultMap.put("jsl", "-");
		resultMap.put("qy", "-");
		
		//优良率
		resultMap.put("goodrate", "-");
		
		//获取图片路径父目录
		//服务器图片物理路径
		String filePath = request.getSession().getServletContext().getRealPath("/upload/nclpng");
		//服务器图片web路径
		String realpath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() + "/upload/nclpng";
		File file = new File(filePath);
		
		if (file.isDirectory()) {
			String[] filenames = file.list();
			for (String fname : filenames) {
				if (fname.contains("aqi")) {
					resultMap.put("aqi", realpath+"/"+fname);
				}
				if (fname.contains("pm25")) {
					resultMap.put("pm25", realpath+"/"+fname);
				}
				if (fname.contains("pm10")) {
					resultMap.put("pm10", realpath+"/"+fname);
				}
				if (fname.contains("co")) {
					resultMap.put("co", realpath+"/"+fname);
				}
				if (fname.contains("no2")) {
					resultMap.put("no2", realpath+"/"+fname);
				}
				if (fname.contains("so2")) {
					resultMap.put("so2", realpath+"/"+fname);
				}
				if (fname.contains("o3.")) {//防止放入O38的数据
					resultMap.put("o3", realpath+"/"+fname);
				}
				if (fname.contains("o38")) {
					resultMap.put("o38", realpath+"/"+fname);
				}
				
				//气象五参
				if (fname.contains("wd")) {
					resultMap.put("wd", realpath+"/"+fname);
				}
				if (fname.contains("sd")) {
					resultMap.put("sd", realpath+"/"+fname);
				}
				if (fname.contains("fl")) {
					resultMap.put("fl", realpath+"/"+fname);
				}
				if (fname.contains("jsl")) {//防止放入O38的数据
					resultMap.put("jsl", realpath+"/"+fname);
				}
				if (fname.contains("qy")) {
					resultMap.put("qy", realpath+"/"+fname);
				}
				
				//优良率
				if (fname.contains("goodrate")) {
					resultMap.put("goodrate", realpath+"/"+fname);
				}
				
			}
		}
		
		return resultMap;
	}
	
	
	/**
	 * 计算两个字符串时间相差多少个小时
	 * @param begintime 开始时间(时间格式：yyyy-MM-dd HH:mm:ss)
	 * @param endtime	结束时间(时间格式：yyyy-MM-dd HH:mm:ss)
	 * @return hours 四舍五入计算相差小时数
	 */
	public static long getDistanceHours(String begintime, String endtime) {
		 
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date begin;
		Date end;
		long hours = 0;
		try {
			begin = df.parse(begintime);
			end = df.parse(endtime);
			 
			long diff = 0;
			long bgtime = begin.getTime();
			long edtime = end.getTime();
			if (bgtime > edtime) {
				diff = bgtime - edtime;
			}
			diff = edtime - bgtime;
			//四舍五入
			hours = Math.round((double)diff / (1000 * 60 * 60));
		} catch (ParseException e) {
			return hours;
		}
			 
		return hours;
	}
	
	
	public Map<String, Object> selectKqAndQx(HttpServletRequest request){
		
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		//对象objid
		String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
		//时间格式:yyyy-MM-dd
		String time = request.getParameter("time") == null ? "" : request.getParameter("time");
		
		String begintime = "";
		String endtime = "";
		if ("".equals(time)) {
			//查询数据库监测数据最新时间
			String[] times = colMinMaxTimeByDay("4", 0, objid, "1,29,30,21,22,23,24,97,9,14,15,16,13,10,19", "1");
			begintime = times[0];
			endtime = times[1];
		} else {
			begintime = time + " 00:00:00";
			endtime = time + " 23:59:59";
		}
		
		String dateformat = "yyyy-MM-dd";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("dateformat", dateformat);
		paramMap.put("objid", objid);
		paramMap.put("begintime", begintime);
		paramMap.put("endtime", endtime);
		
		//日均值
		List<Map<String, Object>> avgList = oneMapDao.selectKqAndQx(paramMap);
		for (int i = 0; i < avgList.size(); i++) {
			Map<String, Object> avgMap = avgList.get(i);
			avgMap.put("quality", airLevelByAQI(avgMap.get("aqi").toString()));
			avgMap.put("fx", windDirection(avgMap.get("fx").toString()));
			avgMap.put("fsdj", windLevel(avgMap.get("fsdj").toString()));
		}
		
		//日实时数据
		dateformat = "yyyy-mm-dd HH24:MI:SS";
		paramMap.put("dateformat", dateformat);
		List<Map<String, Object>> allList = oneMapDao.selectKqAndQx(paramMap);
		
		List<Map<String, Object>> wdList = new ArrayList<Map<String,Object>>();		//温度
		List<Map<String, Object>> sdList = new ArrayList<Map<String,Object>>();		//湿度
		List<Map<String, Object>> jyList = new ArrayList<Map<String,Object>>();		//降雨量
		List<Map<String, Object>> kqzsList = new ArrayList<Map<String,Object>>();	//空气指数
		List<Map<String, Object>> fjList = new ArrayList<Map<String,Object>>();		//风级
		List<Map<String, Object>> aqiList = new ArrayList<Map<String,Object>>();	//空气质量(AQI)
		List<Map<String, Object>> dqyList = new ArrayList<Map<String,Object>>();	//大气压
		
		String keys = "objid,sitename,quality,fx";

		for (int i = 0; i < allList.size(); i++) {
			Map<String, Object> allMap = allList.get(i);
			allMap.put("fsdj", windLevel(allMap.get("fsdj").toString()));
			
			Map<String, Object> wdMap = new LinkedHashMap<String, Object>();	//温度
			Map<String, Object> sdMap = new LinkedHashMap<String, Object>();	//湿度
			Map<String, Object> jyMap = new LinkedHashMap<String, Object>();	//降雨量
			Map<String, Object> kqzsMap = new LinkedHashMap<String, Object>();	//空气指数
			Map<String, Object> fjMap = new LinkedHashMap<String, Object>();	//风级
			Map<String, Object> aqiMap = new LinkedHashMap<String, Object>();	//空气质量(AQI)
			Map<String, Object> dqyMap = new LinkedHashMap<String, Object>();	//大气压
			
			//遍历map
			for(Map.Entry<String, Object> entry : allMap.entrySet()){
				if (!keys.contains(entry.getKey())) {
					if ("collecttime".equals(entry.getKey())) {
						wdMap.put(entry.getKey(), entry.getValue());
						sdMap.put(entry.getKey(), entry.getValue());
						jyMap.put(entry.getKey(), entry.getValue());
						kqzsMap.put(entry.getKey(), entry.getValue());
						fjMap.put(entry.getKey(), entry.getValue());
						aqiMap.put(entry.getKey(), entry.getValue()); 
						dqyMap.put(entry.getKey(), entry.getValue()); 
					} else if ("aqi".equals(entry.getKey())) {
						kqzsMap.put(entry.getKey(), entry.getValue());
						aqiMap.put(entry.getKey(), entry.getValue()); 
					} else if ("wd".equals(entry.getKey())) {
						wdMap.put(entry.getKey(), entry.getValue());
					} else if ("sd".equals(entry.getKey())) {
						sdMap.put(entry.getKey(), entry.getValue());
					} else if ("jyl".equals(entry.getKey())) {
						jyMap.put(entry.getKey(), entry.getValue());
					} else if ("fsdj".equals(entry.getKey())) {
						fjMap.put(entry.getKey(), entry.getValue());
					} else if ("dqy".equals(entry.getKey())) {
						dqyMap.put(entry.getKey(), entry.getValue());
					} else {
						if ("co".equals(entry.getKey())) {
							kqzsMap.put(entry.getKey(), entry.getValue()+"mg/m³");
						} else {
							kqzsMap.put(entry.getKey(), entry.getValue()+"μg/m³");
						}
						
					}
				}
	        }
			
			//计算空气质量指数
			AirBean airBean = new AirBean(Integer.parseInt(objid), String.valueOf(allMap.get("sitename")), String.valueOf(allMap.get("collecttime")), 0,
					Double.parseDouble(String.valueOf(allMap.get("so2"))), Double.parseDouble(String.valueOf(allMap.get("no2"))), 
					Double.parseDouble(String.valueOf(allMap.get("pm10"))), Double.parseDouble(String.valueOf(allMap.get("pm25"))), 
					Double.parseDouble(String.valueOf(allMap.get("co"))), Double.parseDouble(String.valueOf(allMap.get("o3"))), 
					Double.parseDouble(String.valueOf(allMap.get("o38"))), 
					0D, 0D, 0D, 0D, 0D, 0D, 0D);
			airBean = Calc.AirQ(airBean);
			
			kqzsMap.put("ipm25",airBean.getIpm25());
			kqzsMap.put("ipm10",airBean.getIpm10());
			kqzsMap.put("ico",airBean.getIco());
			kqzsMap.put("ino2",airBean.getIno2());
			kqzsMap.put("iso2",airBean.getIso2());
			kqzsMap.put("io3",airBean.getIo3());
			kqzsMap.put("io38",airBean.getIo38());
			
			wdList.add(wdMap);
			sdList.add(sdMap);
			jyList.add(jyMap);
			kqzsList.add(kqzsMap);
			fjList.add(fjMap);
			aqiList.add(aqiMap); 
			dqyList.add(dqyMap); 
		}
		
		resultMap.put("avgList", avgList);
		resultMap.put("wdList", wdList);
		resultMap.put("sdList", sdList);
		resultMap.put("jyList", jyList);
		resultMap.put("kqzsList", kqzsList);
		resultMap.put("fjList", fjList);
		resultMap.put("aqiList", aqiList);
		resultMap.put("dqyList", dqyList);
		
		return resultMap;
	}
	
	
	/**
	 * 根据空气质量类别int类型，
	 * 返回对应空气质量类别的String类型
	 * @param intLevel
	 */
	public String airLevelbyInt(int intLevel) {
		String airLevel = "-";
		if (intLevel == 1) {
			airLevel = "优";
		} else if (intLevel == 2) {
			airLevel = "良";
		} else if (intLevel == 3) {
			airLevel = "轻度污染";
		} else if (intLevel == 4) {
			airLevel = "中度污染";
		} else if (intLevel == 5) {
			airLevel = "重度污染";
		} else {
			airLevel = "严重污染";
		}
		
		return airLevel;
	}
	
	
	/**
	 * 根据AQI值判断空气质量等级
	 * @param aqi
	 * @return
	 */
	public String airLevelByAQI(String aqis){
		int aqii = Integer.parseInt(aqis);
		String airLevel = "-";
		
		if (aqii <= 50) {
			airLevel = "优";
		} else if (aqii > 50 && aqii <= 100) {
			airLevel = "良";
		} else if (aqii > 100 && aqii <= 150) {
			airLevel = "轻度污染";
		} else if (aqii > 150 && aqii <= 200) {
			airLevel = "中度污染";
		} else if (aqii > 200 && aqii <= 300) {
			airLevel = "重度污染";
		} else {
			airLevel = "严重污染";
		}
		
		return airLevel;
	}
	
	
	/**
	 * 根据风向角度判断风向
	 * @param azimuth
	 * @return 风向
	 * 
	 * @from https://blog.csdn.net/lishirong/article/details/41674509
	 */
	public String windDirection(String azimuths){
		double azimuthd = Double.parseDouble(azimuths);
		String windDirection = "无";
		if (azimuthd == 0 || azimuthd == 360) {
			windDirection = "北";
		} else if (azimuthd > 0.0 &&  azimuthd < 45) {
			windDirection = "东北偏北";
		} else if (azimuthd == 45) {
			windDirection = "东北";
		} else if (azimuthd > 45 &&  azimuthd < 90) {
			windDirection = "东北偏东";
		} else if (azimuthd == 90) {
			windDirection = "东";
		} else if (azimuthd > 90 &&  azimuthd < 135) {
			windDirection = "东南偏东";
		} else if (azimuthd == 135) {
			windDirection = "东南";
		} else if (azimuthd > 135 &&  azimuthd < 180) {
			windDirection = "东南偏南";
		} else if (azimuthd == 180) {
			windDirection = "南";
		} else if (azimuthd > 180 &&  azimuthd < 225) {
			windDirection = "西南偏南";
		} else if (azimuthd == 225) {
			windDirection = "西南";
		} else if (azimuthd > 225 &&  azimuthd < 270) {
			windDirection = "西南偏西";
		} else if (azimuthd == 270) {
			windDirection = "西";
		} else if (azimuthd > 270 &&  azimuthd < 315) {
			windDirection = "西北偏西";
		} else if (azimuthd == 315) {
			windDirection = "西北";
		} else {
			windDirection = "西北偏北";
		}
		
		return windDirection;
	}
	
	
	
	
	/**
	 * 根据风速判断风力风级
	 * @param speeds
	 * @return 风力等级
	 * 
	 * @from https://baike.baidu.com/item/%E9%A3%8E%E5%8A%9B%E7%AD%89%E7%BA%A7/8477486
	 */
	public int windLevel(String speeds){
		double speedd = Double.parseDouble(speeds);
		int windLevel = 0;
		if (speedd < 0.3) {
			windLevel = 0;
		} else if (speedd >= 0.3 && speedd < 1.6) {
			windLevel = 1;
		} else if (speedd >= 1.6 && speedd < 3.3) {
			windLevel = 2;
		} else if (speedd >= 3.3 && speedd < 5.5) {
			windLevel = 3;
		} else if (speedd >= 5.5 && speedd < 8.0) {
			windLevel = 4;
		} else if (speedd >= 8.0 && speedd < 10.8) {
			windLevel = 5;
		} else if (speedd >= 10.8 && speedd < 13.9) {
			windLevel = 6;
		} else if (speedd >= 13.9 && speedd < 17.2) {
			windLevel = 7;
		} else if (speedd >= 17.2 && speedd < 20.8) {
			windLevel = 8;
		} else if (speedd >= 20.8 && speedd < 24.5) {
			windLevel = 9;
		} else if (speedd >= 24.5 && speedd < 28.5) {
			windLevel = 10;
		} else if (speedd >= 28.5 && speedd < 32.7) {
			windLevel = 11;
		} else if (speedd >= 32.7 && speedd < 37.0) {
			windLevel = 12;
		} else if (speedd >= 37.0 && speedd < 41.5) {
			windLevel = 13;
		} else if (speedd >= 41.5 && speedd < 46.2) {
			windLevel = 14;
		} else if (speedd >= 46.2 && speedd < 51.0) {
			windLevel = 15;
		} else if (speedd >= 51.0 && speedd < 56.1) {
			windLevel = 16;
		} else {
			windLevel = 17;
		}
		
		return windLevel;
	}
	
}
