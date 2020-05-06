package wy.qingdao_atmosphere.onemap.web;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import wy.qingdao_atmosphere.onemap.domain.KqzlColl;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.qingdao_atmosphere.onemap.domain.OmRank;
import wy.qingdao_atmosphere.onemap.domain.WeatherData;
import wy.qingdao_atmosphere.onemap.service.OneMapService;
import wy.util.datapersistence.service.BaseService;

@Controller
public class OneMapController {
	
	@Autowired 
	private BaseService baseService;
	
	@Autowired
	private OneMapService oneMapService;
	
	
	/**===================================================================================================
	 * ================================================一张图-目录===========================================*/	
	/**
	 * @author 五易科技
	 * @description 一张图-获取目录列表
	 */
	@RequestMapping(value = "/getOmMenuList.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmMenu> getOmMenuList(HttpServletRequest request){
		Logger.getLogger("").info("-----------一张图-获取目录列表---");
		
		List<OmMenu> menuList = oneMapService.getOmMenuList();
		
		return menuList;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-获取浙江大气目录列表
	 */
	@RequestMapping(value = "/getdqMenuList.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmMenu> getMenuList(HttpServletRequest request){
		Logger.getLogger("").info("-----------一张图-获取浙江大气目录列表---");
		
		List<OmMenu> menuList = oneMapService.getdqMenuList();
		
		return menuList;
	}
	
	
	/**===================================================================================================
	 * ================================================一张图-搜索===========================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-搜索-通过关键字(站点名称)查询国站微站列表
	 * 
	 */
	@RequestMapping(value = "/getOmSearch.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<Map<String,Object>> getOmSearch(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-搜索-通过关键字(站点名称)查询国站微站列表--");
		
		//关键字
		String keyword = request.getParameter("keyword") == null ? "" : request.getParameter("keyword");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("objtypeid", "1,5");
		paramMap.put("keyword", keyword);
		
		List<Map<String,Object>> siteList = baseService.selectForCpAttachInfoStoreTwo(paramMap, "getOmObjInfoList");
		
		return siteList;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-搜索-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getOmAllGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmAllGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-搜索-坐标信息及其他--");
		
		Map<String, Object> siteGeom = oneMapService.getOmAllSiteGeom(request, "search");
		
		return siteGeom;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-搜索-站点基本信息详情
	 */
	@RequestMapping(value = "/omAllDetail.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omAllDetail(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-搜索-站点基本信息详情---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("recently", "1 second");
		paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
		paramMap.put("sitenumberid", "1010001,5010001");
		
		List<KqzlColl> siteDetail = oneMapService.getOmAllSiteForKqzl(request, paramMap);

		return siteDetail;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-搜索-站点实时数据(近24小时)
	 */
	@RequestMapping(value = "/omAllCurrent.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omAllCurrent(HttpServletRequest request){
		Logger.getLogger("").info("------------ 一张图-搜索-站点实时数据(近24小时)---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("recently", "24 hour");
		paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
		paramMap.put("sitenumberid", "1010001,5010001");
		
		List<KqzlColl> currentList = oneMapService.getOmAllSiteForKqzl(request, paramMap);
		
		return currentList;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-搜索-站点历史数据
	 */
	@RequestMapping(value = "/omAllHistory.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omAllHistory(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-搜索-站点历史数据---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		paramMap.put("history", "history");
		
		List<KqzlColl> historyList = oneMapService.getOmAllSiteForKqzl(request, paramMap);
		
		return historyList;
	}
	
	
	/**===================================================================================================
	 * ================================================一张图-微站监测-国家监测站================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-国家监测站-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getOmGzGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmGzGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-国家监测站-坐标信息及其他--");
		
		Map<String, Object> gzGeom = oneMapService.getOmSiteGeom(request, "gz");
		
		return gzGeom;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-国家监测站-站点基本信息详情
	 */
	@RequestMapping(value = "/omGzDetail.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omGzDetail(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-国家监测站-站点基本信息详情---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("recently", "1 second");
		paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
		paramMap.put("sitenumberid", "5010001");
		
		List<KqzlColl> siteDetail = oneMapService.getOmGzSiteForKqzl(request, paramMap);

		return siteDetail;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-国家监测站-站点实时数据(近24小时)
	 */
	@RequestMapping(value = "/omGzCurrent.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omGzCurrent(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-国家监测站-站点实时数据(近24小时)---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("recently", "24 hour");
		paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
		paramMap.put("sitenumberid", "5010001");
		
		List<KqzlColl> currentList = oneMapService.getOmGzSiteForKqzl(request, paramMap);
		
		return currentList;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-国家监测站-站点历史数据
	 */
	@RequestMapping(value = "/omGzHistory.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omGzHistory(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-国家监测站-站点历史数据---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		paramMap.put("history", "history");
		
		List<KqzlColl> historyList = oneMapService.getOmGzSiteForKqzl(request, paramMap);
		
		return historyList;
	}
	
	
	/**===================================================================================================
	 * ================================================一张图-微站监测-微站监测=================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-坐标及其他
	 * 
	 */
	@RequestMapping(value = "/getOmWzGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmWzGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-坐标及其他--");
		
		Map<String, Object> wzGeom = oneMapService.getOmSiteGeom(request, "wz");
		
		return wzGeom;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-站点基本信息详情
	 */
	@RequestMapping(value = "/omWzDetail.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omWzDetail(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-站点基本信息详情---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("recently", "1 second");
		paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
		paramMap.put("sitenumberid", "1010001");
		
		List<KqzlColl> siteDetail = oneMapService.getOmWzSiteForKqzl(request, paramMap);

		return siteDetail;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-站点实时数据(近24小时)
	 */
	@RequestMapping(value = "/omWzCurrent.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omWzCurrent(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-站点实时数据(近24小时)---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("recently", "24 hour");
		paramMap.put("dateformat", "yyyy-mm-dd HH24:MI:SS");
		paramMap.put("sitenumberid", "1010001");
		
		List<KqzlColl> currentList = oneMapService.getOmWzSiteForKqzl(request, paramMap);
		
		return currentList;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-站点历史数据
	 */
	@RequestMapping(value = "/omWzHistory.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<KqzlColl> omWzHistory(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-站点历史数据---");
		
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		paramMap.put("history", "history");
		
		List<KqzlColl> historyList = oneMapService.getOmWzSiteForKqzl(request, paramMap);
		
		return historyList;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-微站空气质量与气象数据联合查询
	 */
	@RequestMapping(value = "/selectKqAndQx.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> selectKqAndQx(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-微站空气质量与气象数据联合查询---");
		
		Map<String, Object> resultMap = oneMapService.selectKqAndQx(request);
		
		return resultMap;
	}
	
	
	/**===================================================================================================
	 * ================================================一张图-微站监测-探空监测================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-探空监测-坐标及其他
	 * 
	 */
	@RequestMapping(value = "/getOmTkGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmTkGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-探空监测-坐标及其他--");
		
		Map<String, Object> siteGeom = oneMapService.getOmSiteGeom(request, "tk");
		
		return siteGeom;
	}
	
	/**===================================================================================================
	 * ================================================一张图-微站监测-气象监测站================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-气象监测站-坐标及其他
	 * 
	 */
	@RequestMapping(value = "/getQXOmTkGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public String getQXOmTkGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-气象监测站-坐标及其他--");
		
		StringWriter siteGeom = oneMapService.getQXOmSiteGeom(request);
		
		return siteGeom.toString();
	}
	
	/**
	 * @author 五易科技
	 * @description 一张图-微站监测-气象监测站-查询24小时实时数据和历史监测数据
	 * 
	 */
	@RequestMapping(value = "/getWeatherJCZMonitorData.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<WeatherData> getWeatherJCZMonitorData(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微站监测-气象监测站-查询24小时实时数据和历史监测数据--");
		return oneMapService.getWeatherJCZMonitorData(request);
	}
	
	/**===================================================================================================
	 * ================================================一张图--微站监测-微波辐射监测=============================*/
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getOmFsGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmFsGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微波辐射监测-坐标信息及其他--");
		
		Map<String, Object> gzGeom = oneMapService.getOmSiteGeom(request, "fs");
		
		return gzGeom;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-站点基本信息详情
	 */
	@RequestMapping(value = "/omFsDetail.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<Map<String, Object>> omFsDetail(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微波辐射监测-站点基本信息详情---");
		
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
		List<Map<String, Object>> siteDetail = baseService.selectDetailForCpAttachInfoStoreTwo(Integer.parseInt(objid));
		
		return siteDetail;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-廓线时空分布图
	 */
	@RequestMapping(value = "/omFsKxSkFb.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> omFsKxSkFb(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微波辐射监测-廓线时空分布图---");
		
		String datastreamIds = "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		Map<String, Object> resultMap = oneMapService.omFsData(request,datastreamIds,"87,88,89,90,91,49,50");
		
		for(Map.Entry<String, Object> entry : resultMap.entrySet()){
			if ("jc_fwjd".equals(entry.getKey()) || "jc_fyjd".equals(entry.getKey())) {
				resultMap.put(entry.getKey(), ((JSONArray)entry.getValue()).getJSONObject(0).get("value"));
			}
        }
		
		return resultMap;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-廓线实时数据
	 */
	@RequestMapping(value = "/omFsKxSsSj.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> omFsKxSsSj(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微波辐射监测-廓线实时数据---");
		
		String datastreamIds = "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		Map<String, Object> map = oneMapService.omFsDataSecond(request,datastreamIds,"87,88,89,90,91,49,50");
		
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		for(Map.Entry<String, Object> entry : map.entrySet()){
			if ("jc_fwjd".equals(entry.getKey()) || "jc_fyjd".equals(entry.getKey())) {
				resultMap.put(entry.getKey(), ((JSONArray)entry.getValue()).getJSONObject(0).get("value"));
			} else {
				resultMap.put(entry.getKey(), ((JSONArray)entry.getValue()).getJSONObject(0));
			}
        }
		
		return resultMap;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-通道亮温
	 */
	@RequestMapping(value = "/omFsTdlwSj.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> omFsTdlwSj(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微波辐射监测-通道亮温---");
		
		String param_k = "jc_k1,jc_k2,jc_k3,jc_k4,jc_k5,jc_k6,jc_k7,jc_k8";
		Map<String, Object> kmap = oneMapService.omFsData(request,param_k,"53,54,55,56,57,58,59,60");
		
		String param_v = "jc_v1,jc_v2,jc_v3,jc_v4,jc_v5,jc_v6,jc_v7,jc_v8";
		Map<String, Object> vmap = oneMapService.omFsData(request,param_v,"61,62,63,64,65,66,67,68");
		
		String param_o = "jc_jyzt,fy_ydgd,jc_fwjd,jc_fyjd";
		Map<String, Object> resultMap = oneMapService.omFsData(request,param_o,"45,71,49,50");
		
		for(Map.Entry<String, Object> entry : resultMap.entrySet()){
			if ("jc_fwjd".equals(entry.getKey()) || "jc_fyjd".equals(entry.getKey())) {
				resultMap.put(entry.getKey(), ((JSONArray)entry.getValue()).getJSONObject(0).get("value"));
			}
        }
		
		if (!kmap.isEmpty() || !vmap.isEmpty()) {
			resultMap.put("ktd", kmap);
			resultMap.put("vtd", vmap);
		}
		
		return resultMap;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-其他廓线实时数据
	 */
	@RequestMapping(value = "/omFsOtherKx.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> omFsOtherKx(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-微波辐射监测-其他廓线实时数据---");
		
		String datastreamIds = "fy_sqzhl,fy_ljytszhl,fy_hwlw,jc_dmwd,jc_dmsd,jc_dmyq,jc_fwjd,jc_fyjd";
		Map<String, Object> resultMap = oneMapService.omFsData(request,datastreamIds,"69,70,72,46,47,48,49,50");
		
		for(Map.Entry<String, Object> entry : resultMap.entrySet()){
			if ("jc_fwjd".equals(entry.getKey()) || "jc_fyjd".equals(entry.getKey())) {
				resultMap.put(entry.getKey(), ((JSONArray)entry.getValue()).getJSONObject(0).get("value"));
			}
        }
				
		return resultMap;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-报警信息查询
	 */
	@RequestMapping(value = "/getThrList.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<Map<String, Object>> getThrList(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-微波辐射监测-报警信息查询---");
		
		List<Map<String, Object>> resultList = oneMapService.getThrList(request);
		
		return resultList;
	}
	
	
	/**===================================================================================================
	 * ================================================一张图-微站监测-卫星云图================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-卫星云图-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getOmWxGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmWxGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-卫星云图-坐标信息及其他--");
		
		Map<String, Object> wxGeom = oneMapService.getOmAllSiteGeom(request, "wx");
		
		return wxGeom;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-卫星云图-站点基本信息详情
	 */
	@RequestMapping(value = "/omWxDetail.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<Map<String, Object>> omWxDetail(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-卫星云图-站点基本信息详情---");
		
		List<Map<String, Object>> siteDetail = oneMapService.getOmPicList(request, "13");

		return siteDetail;
	}	
	
	
	/**===================================================================================================
	 * ================================================一张图-热点分析========================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-热点分析-坐标及其他
	 * 
	 */
	@RequestMapping(value = "/getOmRdGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmRdGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-热点分析-坐标及其他--");
		
		Map<String, Object> siteGeom = oneMapService.getOmAllSiteGeom(request, "rd");
		
		return siteGeom;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-热点分析-站点基本信息/监测数据(优良率,其他参数数据)详情
	 */
	@RequestMapping(value = "/omRdDetail.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> omRdDetail(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-热点分析-站点基本信息/监测数据(优良率,其他参数数据)详情---");
		
		Map<String, Object> siteDetail = oneMapService.getOmSiteYll(request);

		return siteDetail;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-热点分析-空间差值图片信息
	 */
	@RequestMapping(value = "/omRdSpatialDv.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> omRdSpatialDv(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-热点分析-空间差值图片信息---");
		
		Map<String, Object> spatialDv = oneMapService.omRdSpatialDv(request);
		
		return spatialDv;
	}
	
	
	/**===================================================================================================
	 * ================================================一张图-精细化========================================*/
	/**
	 * @author 五易科技
	 * @description 一张图-精细化-坐标及其他
	 * 
	 */
	@RequestMapping(value = "/getOmJxGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getOmJxGeom(HttpServletRequest request){
		Logger.getLogger("").info("------------一张图-精细化-坐标及其他--");
		
		Map<String, Object> siteGeom = oneMapService.getOmAllSiteGeom(request, "jx");
		
		return siteGeom;
	}	
	
	
	/**===================================================================================================
	 * ================================================一张图-排名========================================*/
	
	/**
	 * @author 五易科技
	 * @description 一张图-站点排名-实时排名(包括历史排名中的时均值)
	 * 
	 */
	@RequestMapping(value = "/getSRcurByH.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmRank> getSRcurByH(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-站点排名-实时排名(包括历史排名中的时均值)--");
		
		List<OmRank> ranks = oneMapService.omRankBySite(request, "hour");
		
		return ranks;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-站点排名-历史排名(日均值)
	 * 
	 */
	@RequestMapping(value = "/getSRhisByD.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmRank> getSRhisByD(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-站点排名-历史排名(日均值)--");
		
		List<OmRank> ranks = oneMapService.omRankBySite(request, "day");
		
		return ranks;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-站点排名-历史排名(月优良率)
	 * 
	 */
	@RequestMapping(value = "/getSRhisByM.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmRank> getSRhisByM(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-站点排名-历史排名(月优良率)--");
		
		List<OmRank> ranks = oneMapService.omRankBySite(request, "month");
		
		return ranks;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-区域排名-实时排名(包括历史排名中的时均值)
	 * 
	 */
	@RequestMapping(value = "/getARcurByH.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmRank> getARcurByH(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-区域排名-实时排名(包括历史排名中的时均值)--");
		
		List<OmRank> ranks = oneMapService.omRankByArea(request, "hour");
		
		return ranks;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-区域排名-历史排名(日均值)
	 * 
	 */
	@RequestMapping(value = "/getARhisByD.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmRank> getARhisByD(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-区域排名-历史排名(日均值)--");
		
		List<OmRank> ranks = oneMapService.omRankByArea(request, "day");
		
		return ranks;
	}
	
	
	/**
	 * @author 五易科技
	 * @description 一张图-区域排名-历史排名(月优良率)
	 * 
	 */
	@RequestMapping(value = "/getARhisByM.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public List<OmRank> getARhisByM(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-区域排名-历史排名(月优良率)--");
		
		List<OmRank> ranks = oneMapService.omRankByArea(request, "month");
		
		return ranks;
	}

	
	/**
	 * @author 五易科技
	 * @description 一张图-市区域坐标信息以及基本信息查询
	 */
	@RequestMapping(value = "/getCityAreaGeom.do",method = RequestMethod.GET,produces={"application/json;charset=utf-8"})
	@ResponseBody
	public Map<String, Object> getCityAreaGeom(HttpServletRequest request){
		
		Logger.getLogger("").info("------------一张图-市区域坐标信息以及基本信息查询----");
		Map<String, Object> resultMap = oneMapService.getCityAreaGeom(request);
		
		return resultMap;
	}
	
}
