package wy.qingdao_atmosphere.countrysitedata.web;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.domain.AttachInfoStore;
import wy.qingdao_atmosphere.countrysitedata.domain.CollectParam;
import wy.qingdao_atmosphere.countrysitedata.domain.ConnObjParam;
import wy.qingdao_atmosphere.countrysitedata.domain.CpinfoObj;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceBean;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceBean.DataSourceBeanBuilder;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceDo;
import wy.qingdao_atmosphere.countrysitedata.domain.DbConnOid;
import wy.qingdao_atmosphere.countrysitedata.domain.FsjFtpParam;
import wy.qingdao_atmosphere.countrysitedata.domain.SpaceTable;
import wy.qingdao_atmosphere.countrysitedata.domain.TemperatureParam;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;
import wy.qingdao_atmosphere.countrysitedata.service.SiteDataService;

import wy.qingdao_atmosphere.countrysitedata.service.WeiBoService;
import wy.qingdao_atmosphere.countrysitedata.service.WindProfileService;
import wy.qingdao_atmosphere.datacenter.service.DataCenterService;
import wy.qingdao_atmosphere.dynamic.DataSourceContext;
import wy.qingdao_atmosphere.dynamic.DynamicDataSource;
import wy.util.CalDJR;
import wy.util.CalFH;
import wy.util.FkxFtpUtils;
import wy.util.FkxFtpUtilsgz;
import wy.util.FsjFtpUtils;
import wy.util.FsjFtpUtilsgz;
import wy.util.FsjFtpUtilsgzTwo;
import wy.util.ListUtils;

//@CrossOrigin
@Controller
public class SiteDataController {

	/**
	 * @author zzb
	 * @description 启动定时器
	 */
	@RequestMapping(value = "/startTimer.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public void startTimer(HttpServletRequest request) {

		Logger.getLogger("").info("------zjdq_sjtb启动定时器---------");

	}

	@Autowired
	private WeiBoService wb;

	@Autowired
	private WindProfileService wpf;



	@Autowired
	private SiteDataService siteService;

	@Autowired
	private FkxFtpUtils fkxFtp;

	@Autowired
	private FsjFtpUtils fsjFtp;

	@Autowired
	private FsjFtpUtilsgz fsjFtpgz;

	@Autowired
	private SiteDataDao siteDao;

	@Autowired
	private SiteDataDao siteD;

	@Autowired
	private FkxFtpUtilsgz fkxFtpgz;
	
	
	@Autowired
	FsjFtpUtilsgzTwo fsjFtpgz2;

	/**
	 * 从数据库里查询微波辐射计历史数据(12个小时) 包含了温度廓线，相对湿度廓线，边界层廓线，水汽密度廓线，液态水廓线
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryWeiBoLsdata.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryWeiBoLsdata(HttpServletRequest request) {

		Logger.getLogger("").info("------测试微波辐射计---------");
		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoLsdata(request,"87,88,89,90,91,49,50");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap = wb.queryWeiBoLsdata(request, "87,88,89,90,91,49,50");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询风廓线数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultMap;

	}

	/**
	 * 从数据库里查询微波辐射计实时数据(1个小时以内的最新一条数据) 包含了温度廓线，相对湿度廓线，边界层廓线，水汽密度廓线，液态水廓线
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryWeiBoSsdata.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryWeiBoSsdata(HttpServletRequest request) {

		Logger.getLogger("").info("------测试微波辐射计---------");
		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap = wb.queryWeiBoSsdata(request, "87,88,89,90,91,49,50");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询风廓线数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultMap;

	}

	/**
	 * 从数据库里查询风廓线数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryWindProfile.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryWindProfile(HttpServletRequest request) {

		Logger.getLogger("").info("------测试查询风廓线数据---------");
		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap = wb.queryWindProfile(request, "168,169,170,171,172,173");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询风廓线数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultMap;

	}

	

	/**
	 * 获取温度平流数据（辐射计的和风廓线的联合查询）
	 * 
	 * @param request
	 *            getTemperatureData.do
	 * @return
	 */
	@RequestMapping(value = "/getTemperatureData.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getTemperatureData(
			HttpServletRequest request) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		// 辐射计对应站点objid
		String fsjObjid = request.getParameter("objid") == null ? "" : request
				.getParameter("objid");
		String endtime = request.getParameter("endtime") == null ? "" : request
				.getParameter("endtime");
		Map<String, Object> pmap = new HashMap<String, Object>();
		String fkxObjid = null;
		try {
			if (!"".equals(fsjObjid)) {
				pmap.put("objid", fsjObjid);
				// 根据辐射计objid查对应站点的风廓线的objid
				fkxObjid = siteService.queryFkxOidhByFsjOid(pmap);
			}
			if (fkxObjid != null) { // 如果有对应区站编号的风廓线站点，则继续查温度平流，否则也没必要了，因为这是融合数据
				map.put("fsjObjid", fsjObjid);
				map.put("endtime", endtime);
				map.put("fkxObjid", fkxObjid);
				List<TemperatureParam> dataList = siteService
						.getTemperatureParam(map);
				if (dataList != null && dataList.size() > 0) {
					for (TemperatureParam param : dataList) {
						Map<String, Object> resultMap = new HashMap<String, Object>();
						resultMap.put("collecttime", param.getCollecttime());
						// 通过算法计算返回各高度的温度平流值
						int length = param.getHz().length;
						if (length > 0) {
							// 通过算法计算返回各高度的温度平流值
							Map<String, BigDecimal[]> zmap = CalDJR
									.calTempFlue(param.getHz(), param.getD(),
											param.getV(), length,
											param.getFai(), param.getT0());

							resultMap.put("value", zmap.get("Dt"));// 各高度的温度平流值集合
							resultMap.put("height", zmap.get("Ht"));// 各高度的集合
							resultList.add(resultMap);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询温度平流出错了");
			resultList = new ArrayList<Map<String, Object>>();
		}

		return resultList;

	}

	/**
	 * 获取水汽通量（辐射计的和风廓线的联合查询）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getWaterVapor.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getWaterVapor(HttpServletRequest request) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		// 辐射计对应站点objid
		String fsjObjid = request.getParameter("objid") == null ? "" : request
				.getParameter("objid");
		String endtime = request.getParameter("endtime") == null ? "" : request
				.getParameter("endtime");
		Map<String, Object> pmap = new HashMap<String, Object>();
		String fkxObjid = null;
		try {
			if (!"".equals(fsjObjid)) {
				pmap.put("objid", fsjObjid);
				// 根据辐射计objid查对应站点的风廓线的objid
				fkxObjid = siteService.queryFkxOidhByFsjOid(pmap);
			}
			if (fkxObjid != null) { // 如果有对应区站编号的风廓线站点，则继续查水汽通量，否则也没必要了，因为这是融合数据
				map.put("fsjObjid", fsjObjid);
				map.put("endtime", endtime);
				map.put("fkxObjid", fkxObjid);
				List<TemperatureParam> dataList = siteService
						.getWaterVapor(map);
				if (dataList != null && dataList.size() > 0) {
					for (TemperatureParam param : dataList) {
						Map<String, Object> resultMap = new HashMap<String, Object>();
						resultMap.put("collecttime", param.getCollecttime());
						resultMap.put("height", param.getHz());
						// 通过算法计算返回各高度的水汽通量值
						Map<String, BigDecimal[]> zmap = CalFH.calFH(
								param.getHz(), param.getD(), param.getVz(),
								param.getT(), param.getRH(),
								param.getHighsize(), param.getP0(),
								param.getT0());
						// resultMap.put("yq", zmap.get("Pb"));//廓线高度上压强点
						resultMap.put("spsq", zmap.get("FH"));// 廓线高度点上的水平水汽通量
						resultMap.put("czsq", zmap.get("FZ"));// 廓线高度点上的垂直水汽通量
						resultList.add(resultMap);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询水汽通量出错了");
			resultList = new ArrayList<Map<String, Object>>();
		}

		return resultList;

	}

	

	/**
	 * @author 五易科技
	 * @description -微波辐射监测-通道亮温
	 */
	@RequestMapping(value = "/queryWeiBoTdlw.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryWeiBoTdlw(HttpServletRequest request) {
		Logger.getLogger("").info("------------一张图-微波辐射监测-通道亮温---");

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 8个k通道
			String param_k = "jc_k1,jc_k2,jc_k3,jc_k4,jc_k5,jc_k6,jc_k7,jc_k8";
			Map<String, Object> kmap = wb.queryTdlwforKdata(request, param_k,
					"53,54,55,56,57,58,59,60");
			// resultMap.put("ktd", kmap);

			// 8个v通道
			String param_v = "jc_v1,jc_v2,jc_v3,jc_v4,jc_v5,jc_v6,jc_v7,jc_v8";
			Map<String, Object> vmap = wb.queryTdlwforVdata(request, param_v,
					"61,62,63,64,65,66,67,68");
			// resultMap.put("vtd", vmap);

			// 降雨状态，云底温度和方位角度，俯仰角度
			String param_o = "jc_jyzt,fy_ydgd,jc_fwjd,jc_fyjd";
			resultMap = new HashMap<String, Object>();
			Map<String, Object> zmap = wb.queryTdlwOtherdata(request, param_o,
					"45,71,49,50");
			if (zmap != null && zmap.size() > 0) {
				resultMap = zmap;
			}
			if (kmap != null && kmap.size() > 0) {
				resultMap.put("ktd", kmap);
			}
			if (vmap != null && vmap.size() > 0) {
				resultMap.put("vtd", vmap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询通道亮温出错了");
			resultMap = new HashMap<String, Object>();
		}

		return resultMap;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-其他廓线实时数据
	 */
	@RequestMapping(value = "/wbOtherKx.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> omFsOtherKx(HttpServletRequest request) {
		Logger.getLogger("").info("------------一张图-微波辐射监测-其他廓线实时数据---");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String datastreamIds = "fy_sqzhl,fy_ljytszhl,fy_hwlw,jc_dmwd,jc_dmsd,jc_dmyq,jc_fwjd,jc_fyjd";
			resultMap = wb.wbOtherKx(request, datastreamIds,
					"69,70,72,46,47,48,49,50");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询其他廓线实时数据");
			resultMap = new HashMap<String, Object>(); // 返回空
		}

		return resultMap;
	}

	

	/**
	 * @author 五易科技
	 * @description 一张图-查辐射计艾玛图的地址
	 */
	@RequestMapping(value = "/queryFsjAima.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryFsjAima(HttpServletRequest request) {
		Logger.getLogger("").info("------------根据objid查找艾玛图---");

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = wb.queryFsjAima(request);
		} catch (Exception e) {
			Logger.getLogger("").error("/queryFsjAima.do - 查找艾玛图出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}
		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-微波辐射监测-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getfsj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getfsj(HttpServletRequest request) {
		Logger.getLogger("").info("------------一张图-微波辐射监测-坐标信息及其他--");

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = wb.getfsj(request, "fs");
		} catch (Exception e) {
			Logger.getLogger("").error("/getfsj.do - 查找微波辐射监测-坐标信息及其他出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}
		// Map<String, Object> gzGeom = wb.getfsj(request, "fs");

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-风廓线监测-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getfkx.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getfkx(HttpServletRequest request) {
		Logger.getLogger("").info("------------一张图-风廓线监测-坐标信息及其他--");
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = wpf.getfkx(request, "fkx");
		} catch (Exception e) {
			Logger.getLogger("").error("/getfkx.do - 查找风廓线监测-坐标信息及其他出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}
		// Map<String, Object> gzGeom = wpf.getfkx(request, "fs");

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-风廓线-所有站点规定高度下各个时间的风速，风向信息 new-实况监测
	 */
	@RequestMapping(value = "/getFkxAllSitesInfo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getFkxAllSitesInfo(HttpServletRequest request) {
		Logger.getLogger("").info(
				"------------实况监测-风廓线-所有站点规定高度下各个时间的风速，风向信息--");
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = wpf.getfkx_Two(request, "fkx");
		} catch (Exception e) {
			Logger.getLogger("").error(
					"/getFkxAllSitesInfo.do - 查找风廓线所有站点规定高度下各个时间的风速，风向信息出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}
		// Map<String, Object> gzGeom = wpf.getfkx_Two(request, "fkx");

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-辐射计-所有站点规定高度下各个时间的风速，风向信息 new-实况监测
	 */
	@RequestMapping(value = "/getFsjAllSitesInfo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getFsjAllSitesInfo(HttpServletRequest request) {
		Logger.getLogger("").info(
				"------------实况监测-辐射计-所有站点规定高度下各个时间的风速，风向信息--");

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = wb.getfsj_two(request, "fs");
		} catch (Exception e) {
			Logger.getLogger("").error(
					"/getFsjAllSitesInfo.do - 查找辐射计-所有站点规定高度下各个时间的风速，风向信息出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}
		// Map<String, Object> gzGeom = wb.getfsj_two(request, "fs");

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-风廓线，获得风廓线最近俩个小时的时间刻度列表 new-实况监测
	 */
	@RequestMapping(value = "/getFkxHoursList.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getFkxHoursList(HttpServletRequest request) {
		Logger.getLogger("").info("------------实况监测-风廓线，获得风廓线最近俩个小时的时间刻度列表--");

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<String> list = wpf.getFkxHoursList(request);
			map = new HashMap<String, Object>();
			map.put("data", list);
		} catch (Exception e) {
			Logger.getLogger("").error(
					"/getFkxHoursList.do - 查找风廓线，获得风廓线最近俩个小时的时间刻度列表出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}

		/*
		 * List<String> list= wpf.getFkxHoursList(request); Map<String, Object>
		 * map= new HashMap<String,Object>(); map.put("data", list);
		 */
		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-风廓线，获得实况监测目录（高度及下面的时刻列表） new-实况监测
	 */
	@RequestMapping(value = "/getMoinitorMenu.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getMoinitorMenu(HttpServletRequest request) {
		Logger.getLogger("").info("------------一张图-风廓线，获得实况监测目录（高度及下面的时刻列表）--");
		Map<String, Object> map = new HashMap<String, Object>();
		try {

			map = siteService.getMoinitorMenu(request);

		} catch (Exception e) {
			Logger.getLogger("").error(
					"/getMoinitorMenu.do - 查找风廓线，获得实况监测目录（高度及下面的时刻列表）出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}

		// Map<String,Object> map= siteService.getMoinitorMenu(request);

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-风廓线，获得各个时刻间隔下的风矢风羽图（6分钟，30分钟，60分钟，120分钟） new2-
	 */
	@RequestMapping(value = "/getFkxFypic.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getFkxFypic(HttpServletRequest request) {
		Logger.getLogger("").info(
				"------------风廓线，获得各个时刻间隔下的风羽图（5分钟，30分钟，60分钟，120分钟）--");

		Map<String, Object> map = wpf.getFkxFypic(request,
				"168,169,170,171,172,173");

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-风廓线，获得各个时刻间隔下的风羽图（6分钟，30分钟，60分钟，120分钟） new2-
	 */
	@RequestMapping(value = "/getFkxFspic.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getFkxFspic(HttpServletRequest request) {
		Logger.getLogger("").info(
				"------------风廓线，获得各个时刻间隔下的风矢图（5分钟，30分钟，60分钟，120分钟）--");

		Map<String, Object> map = wpf.getFkxFspic(request,
				"168,169,170,171,172,173");

		return map;
	}

	/**
	 * @author 五易科技
	 * @description 一张图-实况监测，获得某个高度某个时刻下的风廓线和辐射计的数据 new2-
	 */
	@RequestMapping(value = "/getSkjcAll.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getSkjcAll(HttpServletRequest request) {
		Logger.getLogger("").info(
				"--------实况监测----获得实况监测某个高度某个时刻下的风廓线和辐射计的数据--");

		Map<String, Object> map = new HashMap<String, Object>();

		// 辐射计
		Map<String, Object> fsjMap = wb.getfsj_two(request, "fs");
		Object fsobj = fsjMap.get("features");
		List<Map<String, Object>> fsTempList = (List<Map<String, Object>>) fsobj;

		// 风廓线
		Map<String, Object> fkxMap = wpf.getfkx_Two(request, "fkx");
		Object fkobj = fkxMap.get("features");
		String type = fkxMap.get("type").toString();
		List<Map<String, Object>> fkTempList = (List<Map<String, Object>>) fkobj; // 返回给前端的站点数据将以此为基础

		int jflag = 0; // 标志，为了减少赋值次数

		if (fsTempList != null && fsTempList.size() > 0) {
			for (Map<String, Object> fstemp : fsTempList) { // 外层遍历辐射计站点的数据
															// 辐射计站点数少，放在外层可以减少遍历次数
				int flag = 0; // 一个标志，是否存在与该辐射计区站标号一致的风廓线区站标号，也就是位于同一站点 0
								// 为不存在，将该辐射计站点数据返回
								// ， 1为存在，将湿度和温度数据拼接到风廓线站点中，返回风廓线站点即可

				Map<String, Object> fsSiteInfo = (Map<String, Object>) (fstemp
						.get("properties")); // 辐射计的站点信息数据

				fsSiteInfo.put("spfs", ""); // 为了统一参数格式，给前端显示
				fsSiteInfo.put("spfx", "");
				fsSiteInfo.put("czfs", "");

				for (Map<String, Object> fktemp : fkTempList) { // 遍历风廓线站点的数据

					Map<String, Object> fkSiteInfo = (Map<String, Object>) (fktemp
							.get("properties")); // 风廓线的站点信息数据
					if (jflag == 0) {
						fkSiteInfo.put("sd", ""); // 第一次循环的时候赋值，减少赋值次数，为了同意参数格式，给前端显示
						fkSiteInfo.put("wd", "");

					}
					if (fsSiteInfo != null && fkSiteInfo != null) {
						if (fkSiteInfo
								.get("sitenumber")
								.toString()
								.equals(fsSiteInfo.get("sitenumber").toString())) { // 如果风廓线和辐射计区站编号相同，说明是同一个站点
							fkSiteInfo.put("sd", fsSiteInfo.get("sd")
									.toString()); // 为同一个站点，就将辐射计的温度和湿度叠加到该风廓线站点中。
							fkSiteInfo.put("wd", fsSiteInfo.get("wd")
									.toString());
							// System.out.println("sd："+fsSiteInfo.get("sd").toString());
							// System.out.println("wd："+fsSiteInfo.get("wd").toString());
							// System.out.println("区站编号："+fkSiteInfo.get("sitenumber").toString());
							flag = 1; // 将标志变为1，不返回该辐射计站点，返回添加了湿度和温度数据的风廓线站点即可
							if (jflag != 0) { // 确保每个风廓线站点至少都赋值了一遍wd和sd
								break; // 退出内存循环
							}

						}
					}
				}
				jflag = 1;
				if (flag == 0) {// 该站点只有辐射计站点，将此辐射计站点追加返回的数据里
					fkTempList.add(fstemp);
				}
			}
		}

		map.put("type", type);
		map.put("features", fkTempList);
		return map;
	}

	/**
	 * 对比分析 根据type查询单个指标的二维数据 （温度，相对湿度，边界层，水汽密度，液态水）
	 * 从数据库里查询微波辐射计历史数据之温度二维分布(12个小时)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryWeiBoLsEwdata.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryWeiBoLsEwdata(
			HttpServletRequest request) {

		Logger.getLogger("").info("---对比分析之---查询微波辐射计历史数据二维分布(24个小时)---------");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		boolean isexist = request.getParameter("endtime") == null
				|| "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wb.queryWeiBoLsEwdata(objid, isexist,
								endtime, type, "87,88,89,90,91,49,50");
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wb.queryWeiBoLsEwdata(objids, isexist, endtime,
							type, "87,88,89,90,91,49,50");
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询对比分析之微波辐射计历史数据二维分布(24个小时)出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}

		return resultList;

	}

	/**
	 * 对比分析 从数据库里查询微波辐射计实时数据(1个小时以内的最新一条数据) 根据type查询单条廓线
	 * （温度廓线，相对湿度廓线，边界层廓线，水汽密度廓线，液态水廓线）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryWeiBoSsKxdata.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryWeiBoSsKxdata(
			HttpServletRequest request) {

		Logger.getLogger("").info("------对比分析之----根据type查询辐射计单条廓线数据 ---------");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		// boolean isexist = request.getParameter("endtime") == null ||
		// "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wb.queryWeiBoSsKxdata(objid, endtime, type,
								"87,88,89,90,91,49,50");
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wb.queryWeiBoSsKxdata(objids, endtime, type,
							"87,88,89,90,91,49,50");
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询对比分析之根据type查询辐射计单条廓线数据 出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultList;

	}

	/**
	 * 查询所有辐射计站点
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/findAllFsj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> findAllFsj(HttpServletRequest request) {

		Logger.getLogger("").info("----对比分析之--查询所有辐射计站点信息---------");

		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			resultList = siteService.findAllFsj(new HashMap<String, Object>());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询所有辐射计站点信息出错了");
			resultList = new ArrayList<Map<String, Object>>(); // 返回空
		}
		return resultList;

	}

	/**
	 * 从数据库里查询微波辐射计温度廓线实时温度数据(1个小时以内的最新一条数据)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/findAllFkx.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> findAllFkx(HttpServletRequest request) {

		Logger.getLogger("").info("--对比分析之----查询所有风廓线站点信息---------");
		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			resultList = siteService.findAllFkx(new HashMap<String, Object>());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询所有风廓线站点信息出错了");
			resultList = new ArrayList<Map<String, Object>>();
			; // 返回空
		}
		return resultList;

	}

	/**
	 * @author 五易科技
	 * @description 对比 分析-其他廓线实时数据
	 */
	@RequestMapping(value = "/wbOtherKxDbfx.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> wbOtherKxDbfx(HttpServletRequest request) {
		Logger.getLogger("").info("------------对比 分析-其他廓线实时数据---");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		boolean isexist = request.getParameter("endtime") == null
				|| "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wb.wbOtherKx_two(objid, isexist, endtime,
								type, "69,70,72,46,47,48,49,50");
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wb.wbOtherKx_two(objids, isexist, endtime,
							type, "69,70,72,46,47,48,49,50");
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询对比 分析-其他廓线实时数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultList;
	}

	/**
	 * 对比分析 从数据库里查询风廓线数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDbfxWP.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDbfxWP(HttpServletRequest request) {

		Logger.getLogger("").info("------对比分析-测试查询风廓线数据---------");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		boolean isexist = request.getParameter("endtime") == null
				|| "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wpf.queryDbfxWp(objid, isexist, endtime,
								type, "168,169,170,171,172,173");
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wpf.queryDbfxWp(objids, isexist, endtime, type,
							"168,169,170,171,172,173");
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询风廓线数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultList;

	}

	/**
	 * 对比分析 从数据库里查询风廓线数据(廓线数据-单个时间点)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDbfxKxWP.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDbfxKxWP(HttpServletRequest request) {

		Logger.getLogger("").info(
				"------对比分析-从数据库里查询风廓线数据(廓线数据-单个时间点)---------");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		boolean isexist = request.getParameter("endtime") == null
				|| "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wpf.queryDbfxKxWp(objid, isexist, endtime,
								type, "168,169,170,171,172,173");
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wpf.queryDbfxKxWp(objids, isexist, endtime,
							type, "168,169,170,171,172,173");
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询风廓线数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultList;

	}

	/**
	 * @author 五易科技
	 * @description 对比分析-风廓线，获得各个时刻间隔下的风矢风羽图（6分钟，30分钟，60分钟，120分钟） new2-
	 */
	@RequestMapping(value = "/getDbfxFypic.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getDbfxFypic(HttpServletRequest request) {
		Logger.getLogger("").info(
				"------------对比分析，获得各个时刻间隔下的风羽图（5分钟，30分钟，60分钟，120分钟）--");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		boolean isexist = request.getParameter("endtime") == null
				|| "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wpf.getDbfxFypic(request, objid, isexist,
								endtime, type, "168,169,170,171,172,173");
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wpf.getDbfxFypic(request, objids, isexist,
							endtime, type, "168,169,170,171,172,173");
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询风廓线数据出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultList;

	}

	/**
	 * @author 五易科技
	 * @description 对比分析-查辐射计艾玛图的地址
	 */
	@RequestMapping(value = "/queryDbfxAima.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> queryDbfxAima(HttpServletRequest request) {
		Logger.getLogger("").info("--------对比分析----根据objid查找艾玛图---");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 必传
		String objids = (request.getParameter("objid") == null || ""
				.equals(request.getParameter("objid"))) ? "" : request
				.getParameter("objid");
		// 要查的类型，为了重用
		String type = (request.getParameter("type") == null) ? "" : request
				.getParameter("type");
		// 是否有带时间查询（true则代表实时查询）
		boolean isexist = request.getParameter("endtime") == null
				|| "".equals(request.getParameter("endtime"));
		// 时间参数
		String endtime = request.getParameter("endtime");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultMap = wb.queryDbfxAima(request, objid, isexist,
								endtime);
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						resultList.add(resultMap);
					}

				} else { // 单个站点

					resultMap = wb.queryDbfxAima(request, objids, isexist,
							endtime);
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					resultList.add(resultMap);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("--------对比分析----根据objid查找艾玛图---");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultList;
	}

	/**
	 * 获取水汽通量（辐射计的和风廓线的联合查询）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getDbfxWaterVapor.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<List<Map<String, Object>>> getDbfxWaterVapor(
			HttpServletRequest request) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		List<List<Map<String, Object>>> dataList = new ArrayList<List<Map<String, Object>>>();
		// 辐射计对应站点objid
		String objids = request.getParameter("objid") == null ? "" : request
				.getParameter("objid");
		String endtime = request.getParameter("endtime") == null ? "" : request
				.getParameter("endtime");
		Logger.getLogger("").info("--------对比分析----根据objid查找水汽通量--");

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						resultList = wb.getWaterVapor(objid, endtime);

						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						dataList.add(resultList);
					}

				} else { // 单个站点

					resultList = wb.getWaterVapor(objids, endtime);
					/*
					 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
					 * resultList.add(resultMap); }
					 */
					dataList.add(resultList);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("--------对比分析----根据objid查找水汽通量失败--");
			dataList = new ArrayList<List<Map<String, Object>>>();
		}
		return dataList;

	}

	/**
	 * 对比分析 获取温度平流数据（辐射计的和风廓线的联合查询）
	 * 
	 * @param request
	 *            getTemperatureData.do
	 * @return
	 */
	@RequestMapping(value = "/getDbfxTemperature.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getDbfxTemperature(
			HttpServletRequest request) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		// 辐射计对应站点objid
		String objids = request.getParameter("objid") == null ? "" : request
				.getParameter("objid");
		String endtime = request.getParameter("endtime") == null ? "" : request
				.getParameter("endtime");
		Logger.getLogger("").info("--------对比分析----根据objid查找温度平流--");

		try {
			if (!"".equals(objids) && objids != null) { // objid有值
				if (objids.contains(",")) { // 如果是多个站点
					String[] oids = objids.split(",");

					for (String objid : oids) { // 循环查每个站点的数据
						Map<String, Object> resultMap = new HashMap<String, Object>();
						Map<String, Object> pmap = new HashMap<String, Object>();
						pmap.put("objid", objid);
						resultList = wb.getDbfxTemperature(objid, endtime);

						Map<String, Object> infoMap = siteDao.findAllFsj(pmap) == null ? null
								: siteDao.findAllFsj(pmap).get(0); // 此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点

						resultMap.put("siteInfo", "");
						if (infoMap != null) {
							// 通过objid查区站编号
							String qzbh = siteDao.queryQzbhByobjid(pmap);
							infoMap.put("qzbh", qzbh);
							resultMap.put("siteInfo", infoMap);
						}
						resultMap.put("data", resultList);
						/*
						 * if(resultMap.size()>0){ //如果有数据，才添加到集合里
						 * resultList.add(resultMap); }
						 */
						dataList.add(resultMap);
					}

				} else { // 单个站点
					Map<String, Object> resultMap = new HashMap<String, Object>();
					Map<String, Object> pmap = new HashMap<String, Object>();
					pmap.put("objid", objids);
					resultList = wb.getDbfxTemperature(objids, endtime);
					Map<String, Object> infoMap = siteDao.findAllFsj(pmap) == null ? null
							: siteDao.findAllFsj(pmap).get(0); // 此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点

					resultMap.put("siteInfo", "");
					if (infoMap != null) {
						// 通过objid查区站编号
						String qzbh = siteDao.queryQzbhByobjid(pmap);
						infoMap.put("qzbh", qzbh);
						resultMap.put("siteInfo", infoMap);
					}
					resultMap.put("data", resultList);
					dataList.add(resultMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("--------对比分析----根据objid查找温度平流失败--");
			dataList = new ArrayList<Map<String, Object>>();
		}
		return dataList;

	}

	/**
	 * @author 五易科技
	 * @description 一张图-融合图列表-坐标信息及其他
	 * 
	 */
	@RequestMapping(value = "/getFuseMenu.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> getFuseMenu(HttpServletRequest request) {
		Logger.getLogger("").info("------------融合图列表-坐标信息及其他--");

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = wb.getFuseMenu(request, "fuse");
		} catch (Exception e) {
			Logger.getLogger("").error("/getFuseMenu.do - 查找融合图列表-坐标信息及其他出错");
			e.printStackTrace();
			map = new HashMap<String, Object>();
		}
		// Map<String, Object> gzGeom = wb.getfsj(request, "fs");

		return map;
	}

	/**
	 * 从数据库里查询微波辐射计历史数据(12个小时)此需求需要将微波数据和风羽图叠在一起 包含了温度廓线，相对湿度廓线，
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryFuseWdSd.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryFuseWdSd(HttpServletRequest request) {

		Logger.getLogger("").info("------测试融合图，温度，湿度二维分布---------");
		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoLsdata(request,"87,88,89,90,91,49,50");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap = wb.queryFuseWdSd(request, "87,88,89,90,91,49,50");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询测试融合图，温度，湿度二维分布出错了");
			resultMap = new HashMap<String, Object>(); // 返回空
		}
		return resultMap;

	}

	/**
	 * 查询所有融合图站点目录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getDbfxFuseMenu.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getDbfxFuseMenu(HttpServletRequest request) {

		Logger.getLogger("").info("----对比分析之--查询所有融合图站点目录---------");

		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			resultList = siteService
					.getDbfxFuseMenu(new HashMap<String, Object>());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询所有融合图站点目录失败");
			resultList = new ArrayList<Map<String, Object>>(); // 返回空
		}
		return resultList;

	}

	/**
	 * 查询设备的数据来源的ftp服务器信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getFtpInfo.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> getFtpInfo(HttpServletRequest request) {

		Logger.getLogger("").info("----后台管理--查询设备的数据来源的ftp服务器信息---------");

		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			resultList = siteService.getFtpInfo(request);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("查询设备的数据来源的ftp服务器信息失败");
			resultList = new ArrayList<Map<String, Object>>(); // 返回空
		}
		return resultList;

	}

	/**
	 * 更新设备的数据来源的ftp服务器信息
	 * 
	 * @param request
	 *            不传 isused ，这个字段单独作为一个接口来判断是否启用
	 * @return
	 */
	@RequestMapping(value = "/updateFtpInfo.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateFtpInfo(FsjFtpParam param) {

		Logger.getLogger("").info("----后台管理--更新设备的数据来源的ftp服务器信息---------");

		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		Map<String, Object> map = new HashMap<String, Object>();
        //System.out.println("param.getisUseid:" + param.getIsused());
		try {
			int i = siteService.updateFtpInfo(param);
			if (i > 0) {
				map.put("message", "更新成功");
				map.put("code", "200");
			} else if (i == -1) { // ftp服务器连接失败
				map.put("message", "ftp服务器连接失败，请检查ip，端口，用户名和密码是否正确！");
				map.put("code", "500");
			} else {
				map.put("message", "更新失败");
				map.put("code", "200");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("更新设备的数据来源的ftp服务器信息失败");
			map.put("message", "更新失败");
			map.put("code", "500");
		}
		return map;

	}

	/**
	 * 更新设备的数据来源的ftp服务器信息
	 * 
	 * @param request
	 *            不传 isused ，这个字段单独作为一个接口来判断是否启用
	 * @return
	 */
	@RequestMapping(value = "/usedFtpServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> usedFtpServer(FsjFtpParam param) {

		Logger.getLogger("").info("----后台管理--启用ftp服务器---------");

		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		Map<String, Object> map = new HashMap<String, Object>();
		//System.out.println("param.getisUseid:" + param.getIsused());
		try {
			int i = siteService.usedFtpServer(param);
			if (i > 0) {
				map.put("message", "更新成功");
				map.put("code", "200");
			} else {
				map.put("message", "更新失败");
				map.put("code", "200");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("更新设备的数据来源的ftp服务器信息失败");
			map.put("message", "更新失败");
			map.put("code", "500");
		}
		return map;

	}

	/**
	 * 测试改版辐射计(动态添加设备的)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/testTbFsj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public List<Map<String, Object>> testTbFsj(HttpServletRequest request) {

		Logger.getLogger("").info("----测试改版辐射计(动态添加设备的)---------");

		// String datastreamIds =
		// "kx_sqmdkx,kx_wdkx,kx_xdsdkx,kx_bjcwdkx,jc_fwjd,jc_fyjd";
		// Map<String,Object> resultMap =
		// wb.queryWeiBoSsdata(request,"87,88,89,90,91,49,50");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			wb.testTbFsj(new HashMap<String, Object>());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").error("测试改版辐射计(动态添加设备的)");
			resultList = new ArrayList<Map<String, Object>>(); // 返回空
		}
		return resultList;

	}

	@Autowired
	DataCenterService dataService;

	/**
	 * 平台端定时各设备端删除一年前的数据
	 */
	 @Scheduled(cron = "0 0/6 * * * ?")
	public void deleteData() {

		Logger.getLogger("").info("----平台端定时删除一年前的数据---------");

		Map<String, Object> platform = siteService.queryPlatform(); // 查询该设备是设备端还是平台端

		if (platform != null
				&& "1".equals(platform.get("isplatform").toString())) { // 1-平台端
																		// 2-设备端
			//System.out.println(platform.get("isplatform").toString() + ":"
					//+ platform.get("showname").toString());
			Logger.getLogger("").info("----此设备是平台端，开始定时删除一年前的数据---------");
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("isused", 1);
				List<Map<String, Object>> objList = dataService.queryObj(map);
				if (objList.size() > 0) {
					for (Map<String, Object> obj : objList) {
						if (obj.get("objid") != null) {
							String objid = obj.get("objid").toString();
							fsjFtpgz2.delBeforeYear(1, objid);
							//System.out.println("删除数据的objid为：" + objid);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger("").error("平台端定时删除一年前的数据失败");

			}

		} else {
			Logger.getLogger("").info("----此设备为设备端，不用执行平台端的定时删除数据方法---------");

		}

	}

	// 测试同步辐射计（多数据源改版单机版）
	 @Scheduled(cron = "0 0/6 * * * ?")
	public void TbFsj2() {

		Logger.getLogger("").info("----改版辐射计(从ftp服务器上解析数据同步到设备端)---------");

		Map<String, Object> platform = siteService.queryPlatform(); // 查询该设备是设备端还是平台端

		if (platform != null
				&& "2".equals(platform.get("isplatform").toString())) { // 1-平台端
																		// 2-设备端
			//System.out.println(platform.get("isplatform").toString() + ":"
			//		+ platform.get("showname").toString());
			Logger.getLogger("").info(
					"----此设备为设备端(开始定时从ftp服务器上解析辐射计数据同步到设备端)---------");
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("objtypeid", 12); // 微波辐射计 28-风廓线
				List<FsjFtpParam> fsjList = siteDao.queryFsjSb(map);

				if (fsjList != null && fsjList.size() > 0) {
					for (FsjFtpParam param : fsjList) {
						//System.out.println("fsjFtpParam:" + param);
						//新版辐射计数据同步
						fsjFtpgz2.tbFtpFsj(param.getSitenumber(),
								param.getObjid(), param.getIp(),
								param.getPort(), param.getUsername(),
								param.getPassword(), param.getFilepath());
						fsjFtpgz2.start(); // 启动matlib exe文件开始生成图片
					}

					// 不注释这行会打印出问题，应该是先执行关闭线程操作了
					// executor.shutdown();
				} else {
					Logger.getLogger("").info("未查询到已启用的辐射计类型的ftp服务器信息");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger("").error("辐射计(动态添加设备的)从ftp服务器上读取数据入库失败");

			}

		} else {
			Logger.getLogger("").info(
					"----此设备为平台端端(不用定时从ftp服务器上解析辐射计数据同步到设备端)---------");

		}

	}

	// 测试同步风廓线（多数据源改版单机版）
	 @Scheduled(cron = "0 0/6 * * * ?")
	public void TbFkx2() {

		Logger.getLogger("").info("----改版风廓线(从ftp服务器上解析数据同步到设备端)---------");

		Map<String, Object> platform = siteService.queryPlatform(); // 查询该设备是设备端还是平台端

		if (platform != null
				&& "2".equals(platform.get("isplatform").toString())) { // 1-平台端
																		// 2-设备端
			//System.out.println(platform.get("isplatform").toString() + ":"
			//		+ platform.get("showname").toString());
			Logger.getLogger("").info(
					"----此设备为设备端(开始定时从ftp服务器上解析风廓线数据同步到设备端)---------");

			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("objtypeid", 28); // 微波辐射计 28-风廓线
				List<FsjFtpParam> fsjList = siteDao.queryFsjSb(map); // 查询风廓线一起用的ftp服务器设备（辐射计风廓线可通用）

				if (fsjList != null && fsjList.size() > 0) {
					for (FsjFtpParam param : fsjList) {
						//System.out.println("fsjFtpParam:" + param);
						// fsjFtpgz.tbFtpFsj(param.getSitenumber(),
						// param.getObjid(), param.getIp(), param.getPort(),
						// param.getUsername(), param.getPassword(),
						// param.getFilepath());
						fkxFtpgz.tbFtpFkxTwo(param.getSitenumber(),
								param.getObjid(), param.getIp(),
								param.getPort(), param.getUsername(),
								param.getPassword(), param.getFilepath());

					}

					// 不注释这行会打印出问题，应该是先执行关闭线程操作了
					// executor.shutdown();
				} else {
					Logger.getLogger("").info("未查询到已启用的风廓线类型的ftp服务器信息");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger("").error("风廓线(动态添加设备)从ftp服务器上读取数据入库失败");

			}
		} else {
			Logger.getLogger("").info(
					"----此设备为平台端(不用定时从ftp服务器上解析风廓线数据同步到设备端)---------");

		}

	}

	/**
	 * 测试改版辐射计(动态添加设备的)
	 * 
	 * @param request
	 * @return
	 */
	// @RequestMapping(value="/TbSjkJcData.do", method =
	// RequestMethod.GET,produces={"application/json;charset=utf-8"})
	// @ResponseBody
	// @Scheduled(cron = "0 0/6 * * * ?")
	public void TbSjkJcData() {

		Logger.getLogger("").info("----zjdq_sjtb同步其他数据库的信息---------");

		Logger.getLogger("").info("浙江大气二期辅助项目开始同步其他数据了...");

		Map<String, Object> map = new HashMap<String, Object>();
		// map.put("databaseType", "1"); //1辐射计 2.风廓线

		// 1.从表中获取需要切换的数据源信息
		List<DataSourceDo> dataSourceD0List = siteD.queryOtherDataSource(map);

		/*System.out
				.println("dataSourceD0List.size():" + dataSourceD0List.size());

		System.out.println("dataSourceD0List:" + dataSourceD0List);
*/
		map.clear();
		// 辐射计
		map.put("objtypeid", "12"); // 设备对象类型， 12-辐射计 28-风廓线
		List<DbConnOid> fsjDbList = siteD.selectDbConnOid(map); // 多数据源的辐射计设备与本地objid关联信息

		Map<Integer, String> fsjTimeMaps = new HashMap<Integer, String>(); // 存储各个数据源辐射计设备在本地数据库对应的最新时间

		for (DbConnOid conn : fsjDbList) {
			// 获取本地数据库各个数据源的数据最新时间
			String otherObjid = conn.getOtherobjid() + "";
			String objid = conn.getObjid() + "";
			Integer dsId = conn.getDsid();
			String dbMaxTime = siteD.getMaxTimeByDtid_three(
					"yyyy-MM-dd HH24:MI:ss", "5", objid);
			fsjTimeMaps.put(dsId, dbMaxTime);
			//System.out.println(objid + ":fsj:" + dbMaxTime + ":" + dsId);
		}

		//System.out.println("fsjDbConnOidList:" + fsjDbList);

		// 风廓线
		map.put("objtypeid", "28"); // 设备对象类型， 12-辐射计 28-风廓线
		List<DbConnOid> fkxDbList = siteD.selectDbConnOid(map); // 多数据源的风廓线设备与本地objid关联信息

		Map<Integer, String> fkxTimeMaps = new HashMap<Integer, String>(); // 存储各个数据源风廓线设备在本地数据库对应的最新时间

		for (DbConnOid conn : fkxDbList) {
			// 获取本地数据库各个数据源的数据最新时间
			String otherObjid = conn.getOtherobjid() + "";
			String objid = conn.getObjid() + "";
			Integer dsId = conn.getDsid();
			String dbMaxTime = siteD.getMaxTimeByDtid_three(
					"yyyy-MM-dd HH24:MI:ss", "30", objid);
			fkxTimeMaps.put(dsId, dbMaxTime);
			//System.out.println(objid + ":fkx:" + dbMaxTime + ":" + dsId);
		}

		//System.out.println("fkxDbConnOidList:" + fkxDbList);

		// 2.循环遍历,切换数据源,执行操作,切完就得切回默认数据源
		for (DataSourceDo dataSourceDO : dataSourceD0List) {
			DataSourceBean dataSourceBean = new DataSourceBean(
					new DataSourceBeanBuilder(dataSourceDO.getDataSourceName(),
							dataSourceDO.getDatabaseIp(),
							dataSourceDO.getDatabasePort(),
							dataSourceDO.getDatabaseName(),
							dataSourceDO.getDatabaseUsername(),
							dataSourceDO.getPassword()));

			// XXX你的操作XXX
			//System.out.println("datasourceDO:" + dataSourceDO);
			//System.out.println("datasourceBean:" + dataSourceBean);

			// request.setAttribute("city", "'温州市','衢州市','金华市','绍兴市','丽水市'");
			// request.setAttribute("objid", "2872");
			// map = wb.getfsj(request, "fs");
			List<CollectParam> cList = new ArrayList<CollectParam>(); // 辐射计的数据

			List<CollectParam> fList = new ArrayList<CollectParam>(); // 风廓线的数据

			String fsjBaseObjid = "-2"; // 给个默认值先

			String fkxBaseObjid = "-1";

			// 查询该数据源辐射计数据
			for (DbConnOid conn : fsjDbList) { // 查找其他数据源在本地数据库的关联的objid
				if (conn.getDsid() == dataSourceDO.getId()) {
					// 切换到该数据源
					DataSourceContext.setDataSource(dataSourceBean);

					fsjBaseObjid = conn.getObjid() + ""; // 该数据源的设备objid在本地服务器相关联的objid

					String otherObjid = conn.getOtherobjid() + ""; // 新数据源的设备objid

					//System.out.println("dsid:" + conn.getDsid());
					//System.out.println("fsjBaseObjid:" + fsjBaseObjid);
					//System.out.println("otherObjid:" + otherObjid);

					map.clear();

					map.put("objid", otherObjid);

					String dbMaxTime = fsjTimeMaps.get(conn.getDsid());// 本地数据库已有的该数据源的设备的最新监测数据时间

					for (Map.Entry<Integer, String> entry : fsjTimeMaps
							.entrySet()) {
						//System.out.println("fsj time map:" + entry.getKey()
						//		+ ":" + entry.getValue());

					}

					// System.out.println("map -7:"+fsjTimeMaps.get(7));
					// System.out.print("map.getkey:"+fsjTimeMaps.get(conn.getDsid()));

					map.put("dbMaxTime", dbMaxTime); // 定时从其他数据源更新最新的数据（时间大于dbMaxTime）的

					//System.out.println("dbMaxTime:" + dbMaxTime);

					Long startTime = System.currentTimeMillis();

					cList = siteService.findOhterDbCollect(map); // 其他数据库的实时数据

					/*
					 * try { int i = siteService.recurSub(cList, 5000,
					 * conn.getOtherobjid()); } catch (Exception e) { // TODO
					 * Auto-generated catch block e.printStackTrace(); }
					 */
					// 分批插入实时监测数据表

					//System.out.println(fsjBaseObjid + ":" + dbMaxTime
					//		+ "-----size: " + cList.size());

					/*
					 * DataSourceContext.toDefault(); //切回到默认数据库 map.put("list",
					 * cList); int i = siteService.insertCollect(cList); int i =
					 * siteService.insertCollect(map);
					 * System.out.println("插入完成："+i);
					 */
					long endTime = System.currentTimeMillis();
					//System.out.println(dataSourceDO.getDataSourceName() + ":"
					//		+ dataSourceDO.getId() + "读取共耗时为："
					//		+ (endTime - startTime) / 1000 + "秒");
					break;
				}
			}

			// 查询该数据源风廓线数据
			for (DbConnOid conn : fkxDbList) { // 查找其他数据源在本地数据库的关联的objid
				if (conn.getDsid() == dataSourceDO.getId()) {
					// 切换到该数据源
					DataSourceContext.setDataSource(dataSourceBean);

					fkxBaseObjid = conn.getObjid() + ""; // 该数据源的设备objid在本地服务器相关联的objid

					String otherObjid = conn.getOtherobjid() + ""; // 新数据源的设备objid

					//System.out.println("dsid:" + conn.getDsid());
					//System.out.println("fkxBaseObjid:" + fkxBaseObjid);
					//System.out.println("otherObjid:" + otherObjid);
					map.clear();

					map.put("objid", otherObjid);

					String dbMaxTime = fkxTimeMaps.get(conn.getDsid());// 本地数据库已有的该数据源的设备的最新监测数据时间

					for (Map.Entry<Integer, String> entry : fkxTimeMaps
							.entrySet()) {
						//System.out.println("fkx time map:" + entry.getKey()
						//		+ ":" + entry.getValue());

					}
					//System.out.println("map -7:" + fkxTimeMaps.get(7));
					//System.out.print("map.getkey:"
					//		+ fkxTimeMaps.get(conn.getDsid()));

					map.put("dbMaxTime", dbMaxTime); // 定时从其他数据源更新最新的数据（时间大于dbMaxTime）的

					//System.out.println("dbMaxTime:" + dbMaxTime);

					Long startTime = System.currentTimeMillis();

					fList = siteService.findOhterDbCollect(map); // 其他数据库的实时数据

					/*
					 * try { int i = siteService.recurSub(cList, 5000,
					 * conn.getOtherobjid()); } catch (Exception e) { // TODO
					 * Auto-generated catch block e.printStackTrace(); }
					 */
					// 分批插入实时监测数据表

					//System.out.println(fkxBaseObjid + ":" + dbMaxTime
					//		+ "-----size: " + fList.size());

					/*
					 * DataSourceContext.toDefault(); //切回到默认数据库 map.put("list",
					 * cList); int i = siteService.insertCollect(cList); int i =
					 * siteService.insertCollect(map);
					 * System.out.println("插入完成："+i);
					 */
					long endTime = System.currentTimeMillis();
					//System.out.println(dataSourceDO.getDataSourceName() + ":"
					//		+ dataSourceDO.getId() + "读取共耗时为："
					//		+ (endTime - startTime) / 1000 + "秒");
					break;
				}
			}

			// 同步辐射计数据

			if (cList.size() > 0) { // 辐射计有监测数据

				DataSourceContext.toDefault(); // 切回到默认数据库
				// 修改dataguid，插入本地数据库
				Long startTime = System.currentTimeMillis();

				String[] dataguids = cList.get(0).getDataguid().split("_");
				String dataguid = dataguids[0] + "_" + fsjBaseObjid + "_"
						+ dataguids[2] + "_";

				//System.out.println(fsjBaseObjid + "new dataguid:" + dataguid);
				Long sTime = System.currentTimeMillis();
				for (CollectParam cParam : cList) {
					cParam.setDataguid(dataguid
							+ cParam.getDataguid().split("_")[3]); // 换成新的dataguid
				}
				Long eTime = System.currentTimeMillis();
				//System.out.println("修改辐射计原数据源" + cList.size()
				//		+ "条数据的dataguid耗时为：" + (eTime - sTime) / 1000 + "秒");
				// map.put("list", cList);
				// int i = siteService.insertCollect(cList);
				// map.put("objid", baseObjid); //本地服务器的数据库的objid
				// int i = siteService.insertCollect(map); //插入本地数据库

				int i = 0;
				try {
					i = siteService.recurSub(cList, 5000,
							Integer.parseInt(fsjBaseObjid));// 分批插入实时监测数据表
				} catch (Exception e) {
					System.out.println(fsjBaseObjid + "分批插入辐射计实时监测数据出错了");
					e.printStackTrace();
				}
				//System.out.println("插入完成：" + i);
				//System.out.println(fsjBaseObjid + "辐射计实时监测数据同步成功！同步数据为：" + i
				//		+ "条");

				long endTime = System.currentTimeMillis();
				//System.out.println("辐射计-" + fsjBaseObjid + ":"
				//		+ dataSourceDO.getDataSourceName() + ":"
				//		+ dataSourceDO.getId() + "读取插入共耗时为："
				//		+ (endTime - startTime) / 1000 + "秒");

			} else {
				System.out
						.println("新增的数据源中未查询到更新的辐射计设备监测数据,请确认该数据源是否有数据或者数据是否有在更新");

			}

			// 同步风廓线数据

			if (fList.size() > 0) { // 风廓线有监测数据

				DataSourceContext.toDefault(); // 切回到默认数据库
				// 修改dataguid，插入本地数据库

				Long startTime = System.currentTimeMillis();

				String[] dataguids = fList.get(0).getDataguid().split("_");
				String dataguid = dataguids[0] + "_" + fkxBaseObjid + "_"
						+ dataguids[2] + "_";

				//System.out.println(fkxBaseObjid + "new dataguid:" + dataguid);
				Long sTime = System.currentTimeMillis();
				for (CollectParam cParam : fList) {
					cParam.setDataguid(dataguid
							+ cParam.getDataguid().split("_")[3]); // 换成新的dataguid
				}
				Long eTime = System.currentTimeMillis();
				//System.out.println("修改原数据源" + fList.size() + "条数据的dataguid耗时为："
				//		+ (eTime - sTime) / 1000 + "秒");
				// map.put("list", cList);
				// int i = siteService.insertCollect(cList);
				// map.put("objid", baseObjid); //本地服务器的数据库的objid
				// int i = siteService.insertCollect(map); //插入本地数据库

				int i = 0;
				try {
					i = siteService.recurSub(fList, 5000,
							Integer.parseInt(fkxBaseObjid));// 分批插入实时监测数据表
				} catch (Exception e) {
					System.out.println(fkxBaseObjid + "分批插入风廓线实时监测数据出错了");
					e.printStackTrace();
				}
				//System.out.println("插入完成：" + i);
				//System.out.println(fkxBaseObjid + "风廓线实时监测数据同步成功！同步数据为：" + i
				//		+ "条");

				long endTime = System.currentTimeMillis();
				//System.out.println("辐射计-" + fkxBaseObjid + ":"
				//		+ dataSourceDO.getDataSourceName() + ":"
				//		+ dataSourceDO.getId() + "读取插入共耗时为："
				//		+ (endTime - startTime) / 1000 + "秒");

			} else {
				System.out
						.println("新增的数据源中未查询到更新的风廓线设备监测数据,请确认该数据源是否有数据或者数据是否有在更新");

			}

		}
		// return map;

	}

	

	/**
	 * 测试添加其他数据源
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/addOtherDb.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addOtherDb(DataSourceDo ds) {
		Map<String, Object> map;
		try {
			// map = siteService.addOtherDb(ds);
			// 查询该站点时属于平台端还是设备端
			Map<String, Object> platform = siteService.queryPlatform();
			// 如果是平台端才能进行增加数据源操作
			if (platform != null
					&& "1".equals(platform.get("isplatform").toString())) { // 1-平台端
																			// 2-设备端
				//System.out.println(platform.get("isplatform").toString() + ":"
				//		+ platform.get("showname").toString());

				Map<String, Object> zmap = new HashMap<String, Object>();
				zmap.put("databaseIp", ds.getDatabaseIp());
				zmap.put("databasePort", ds.getDatabasePort());
				zmap.put("databaseName", ds.getDatabaseName());
				List<DataSourceDo> list = siteDao.queryOtherDataSource(zmap);
				//System.out.println("dslist:" + list);
				if (list != null && list.size() > 0) { // 说明此数据源已经有了
					Logger.getLogger("").info("该数据源已经存在，请勿重复添加");
					map = new HashMap<String, Object>();
					map.put("code", "500");
					map.put("message", "该数据源已经存在，请勿重复添加");

				} else { // 不存在，可以添加

					Logger.getLogger("").info("该数据源没有添加过，可以添加");
					// 增加数据源及其他数据同步的相关操作
					map = siteService.addOtherDb_two(ds);
				}

			} else {
				Logger.getLogger("").info("非平台端不能进行数据源添加操作");
				map = new HashMap<String, Object>();
				map.put("code", "500");
				map.put("message", "非平台端不能进行数据源添加操作");
			}

		} catch (Exception e) {
			map = new HashMap<String, Object>();
			map.put("code", "500");
			map.put("message", e.getMessage());
			e.printStackTrace();
		}

		return map;

	}

	@RequestMapping(value = "/testTbFsjJcData.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> testTbFsjJcData(String dsid) {

		Logger.getLogger("").info("----测试同步辐射计信息---------");

		Map<String, Object> rMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("id", dsid);
		// 1.从表中获取需要切换的数据源信息
		List<DataSourceDo> list = siteD.queryOtherDataSource(map);

		//System.out.println("dataSourceD0List.size():" + list.size());

		map.put("dsid", dsid);
		List<DbConnOid> dbList = siteD.selectDbConnOid(map);
		if (dbList.size() > 0) {// 查到了其他数据源与本数据库的objid的关联信息
			Integer baseObjid = dbList.get(0).getObjid(); // 本地的objid

			Integer otherObjid = dbList.get(0).getOtherobjid(); // 其他数据源的objid

			String dbMaxTime = siteD.getMaxTimeByDtid_three(
					"yyyy-MM-dd HH24:MI:ss", "5", baseObjid + "");
			//System.out.println("dataSourceD0List:" + list);
			// 2.循环遍历,切换数据源,执行操作,切完就得切回默认数据源
			if (list.size() > 0) { // 查询新增的数据源成功

				DataSourceDo dataSourceDO = list.get(0); // 刚刚新增的数据源
				DataSourceBean dataSourceBean = new DataSourceBean(
						new DataSourceBeanBuilder(
								dataSourceDO.getDataSourceName(),
								dataSourceDO.getDatabaseIp(),
								dataSourceDO.getDatabasePort(),
								dataSourceDO.getDatabaseName(),
								dataSourceDO.getDatabaseUsername(),
								dataSourceDO.getPassword()));
				DataSourceContext.setDataSource(dataSourceBean);

				// XXX你的操作XXX
				//System.out.println("datasourceDO:" + dataSourceDO);
				//System.out.println("datasourceBean:" + dataSourceBean);
				// request.setAttribute("city",
				// "'温州市','衢州市','金华市','绍兴市','丽水市'");
				// request.setAttribute("objid", "2872");
				// map = wb.getfsj(request, "fs");

				map.put("objid", otherObjid + "");

				map.put("dbMaxTime", dbMaxTime);
				Long startTime = System.currentTimeMillis();
				List<CollectParam> cList = siteService.findOhterDbCollect(map); // 其他数据库的实时数据
				//System.out.println("cList.size():" + cList.size());
				/*
				 * System.out.println("map.size:"+map.size());
				 * for(Map.Entry<String, Object> entry :map.entrySet()){
				 * System.out.println(entry.getKey()+":"+entry.getValue()); }
				 */

				DataSourceContext.toDefault(); // 切回到默认数据库

				if (cList.size() > 0) { // 有监测数据
					String[] dataguids = cList.get(0).getDataguid().split("_");
					String dataguid = dataguids[0] + "_" + baseObjid + "_"
							+ dataguids[2] + "_";
					//System.out.println("new dataguid:" + dataguid);
					Long sTime = System.currentTimeMillis();
					for (CollectParam cParam : cList) {
						cParam.setDataguid(dataguid
								+ cParam.getDataguid().split("_")[3]); // 换成新的dataguid
					}
					Long eTime = System.currentTimeMillis();
					//System.out
						//	.println("修改原数据源" + cList.size()
						//			+ "条数据的dataguid耗时为：" + (eTime - sTime)
						//			/ 1000 + "秒");
					map.put("list", cList);
					// int i = siteService.insertCollect(cList);
					/*
					 * map.put("objid", baseObjid); //本地服务器的数据库的objid int i =
					 * siteService.insertCollect(map); //插入本地数据库
					 */
					int i = 0;
					try {
						i = siteService.recurSub(cList, 5000, baseObjid);// 分批插入实时监测数据表
					} catch (Exception e) {

						e.printStackTrace();
					}
					System.out.println("插入完成：" + i);
					rMap.put("message", "实时监测数据同步成功！同步数据为：" + i + "条");
					rMap.put("code", 200);

					long endTime = System.currentTimeMillis();
					System.out.println(dataSourceDO.getDataSourceName()
							+ "读取插入共耗时为：" + (endTime - startTime) / 1000 + "秒");
				} else {
					rMap.put("message", "新增的数据源中为查询到监测数据,请确认该数据源是否有数据");
					rMap.put("code", 200);
				}

			} else {
				rMap.put("message", "未查询到新增的数据源,同步失败");
				rMap.put("code", 500);
			}
		} else {
			rMap.put("message", "未查找到本地数据库与其他数据源相关联的objid,同步失败");
			rMap.put("code", 500);
		}

		return rMap;

	}

	/**
	 * 查询此设备是设备端还是平台端
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/queryPlatform.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> queryPlatform() {
		Map<String, Object> map;
		Logger.getLogger("").info("-----------查询该设备是否为平台---------");
		// map = siteService.addOtherDb(ds);
		// 查询该站点时属于平台端还是设备端
		// isplatform //1-平台端 2-设备端
		Map<String, Object> platform = siteService.queryPlatform();

		return platform;
	}

	/**
	 * 添加服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/addWebServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addWebServer(WebServer web) {
		Map<String, Object> map = new HashMap<String, Object>();
		;
		Logger.getLogger("").info("-----------添加WEb服务器信息---------");

		try {

			int statu = siteService.addWebServer(web);
			if (statu > 0) {
				map.put("message", "添加web服务器信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("添加web服务器信息成功");
			} else {
				map.put("message", "添加web服务器信息失败，请检查所填端口等信息是否正确");
				map.put("code", 500);
				Logger.getLogger("").info("添加web服务器信息失败，请检查所填端口等信息是否正确");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "添加web服务器信息失败，请检查所填端口等信息是否正确");
			map.put("code", 500);
			Logger.getLogger("").info("添加web服务器信息失败，请检查所填端口等信息是否正确");
			return map;
		}

	}

	/**
	 * 修改服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/updateWebServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateWebServer(WebServer web) {
		Map<String, Object> map = new HashMap<String, Object>();
		;
		Logger.getLogger("").info("-----------更新web服务器信息---------");

		try {

			int statu = siteService.updateWebServer(web);
			if (statu > 0) {
				map.put("message", "修改web服务器信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("修改web服务器信息成功");
			} else {
				map.put("message", "修改web服务器信息失败，请检查所填端口等信息是否正确");
				map.put("code", 500);
				Logger.getLogger("").info("修改web服务器信息失败，请检查所填端口等信息是否正确");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "修改web服务器信息失败，请检查所填端口等信息是否正确");
			map.put("code", 500);
			Logger.getLogger("").info("修改web服务器信息失败，请检查所填端口等信息是否正确");
			return map;
		}

	}

	/**
	 * 修改服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/updateFsjServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateFsjServer(WebServer fsj) {
		Map<String, Object> map = new HashMap<String, Object>();
		;
		Logger.getLogger("").info("-----------更新辐射计服务器信息---------");

		try {

			int statu = siteService.updateFsjServer(fsj);
			if (statu > 0) {
				map.put("message", "修改fsj服务器信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("修改fsj服务器信息成功");
			} else {
				map.put("message", "修改fsj服务器信息失败，请检查所填端口等信息是否正确");
				map.put("code", 500);
				Logger.getLogger("").info("修改fsj服务器信息失败，请检查所填端口等信息是否正确");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "修改fsj服务器信息失败，请检查所填端口等信息是否正确");
			map.put("code", 500);
			Logger.getLogger("").info("修改fsj服务器信息失败，请检查所填端口等信息是否正确");
			return map;
		}

	}

	/**
	 * 添加服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/addFsjServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> addFsjServer(WebServer fsj) {
		Map<String, Object> map = new HashMap<String, Object>();
		;
		Logger.getLogger("").info("-----------添加辐射计服务器信息---------");

		try {

			int statu = siteService.addFsjServer(fsj);
			if (statu > 0) {
				map.put("message", "添加fsj服务器信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("添加fsj服务器信息成功");
			} else {
				map.put("message", "添加fsj服务器信息失败，请检查所填端口等信息是否正确");
				map.put("code", 500);
				Logger.getLogger("").info("添加fsj服务器信息失败，请检查所填端口等信息是否正确");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "添加fsj服务器信息失败，请检查所填端口等信息是否正确");
			map.put("code", 500);
			Logger.getLogger("").info("添加fsj服务器信息失败，请检查所填端口等信息是否正确");
			return map;
		}

	}

	/**
	 * 删除fsj服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/deleteFsjServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> deleteFsjServer(WebServer fsj) {
		Map<String, Object> map = new HashMap<String, Object>();
		;
		Logger.getLogger("").info("-----------删除辐射计程序服务器信息---------");

		try {

			int statu = siteService.deleteFsjServer(fsj);
			if (statu > 0) {
				map.put("message", "删除fsj服务器信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("添加fsj服务器信息成功");
			} else {
				map.put("message", "删除fsj服务器信息失败");
				map.put("code", 500);
				Logger.getLogger("").info("删除fsj服务器信息失败");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "删除fsj服务器信息失败，请检查所填端口等信息是否正确");
			map.put("code", 500);
			Logger.getLogger("").info("删除fsj服务器信息失败，请检查所填端口等信息是否正确");
			return map;
		}

	}

	/**
	 * 删除fsj服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/deleteWebServer.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> deleteWebServer(WebServer web) {
		Map<String, Object> map = new HashMap<String, Object>();
		Logger.getLogger("").info("-----------删除web服务器信息---------");

		try {

			int statu = siteService.deleteWebServer(web);
			if (statu > 0) {
				map.put("message", "删除web服务器信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("添加web服务器信息成功");
			} else {
				map.put("message", "删除web服务器信息失败");
				map.put("code", 500);
				Logger.getLogger("").info("删除web服务器信息失败");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "删除web服务器信息失败");
			map.put("code", 500);
			Logger.getLogger("").info("删除web服务器信息失败");
			return map;
		}

	}

	/**
	 * 查询辐射计服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/selectFsjServer.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> selectFsjServer(WebServer fsj) {
		Map<String, Object> map = new HashMap<String, Object>();
		;
		Logger.getLogger("").info("-----------查询辐射计程序服务器---------");

		try {

			List<WebServer> list = siteService.selectFsjServer(fsj);
			map.put("message", "查询fsj服务器信息成功");
			map.put("code", 200);
			map.put("data", list);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "查询fsj服务器信息失败");
			map.put("code", 500);
			map.put("data", new ArrayList<String>());
			Logger.getLogger("").info("查询fsj服务器信息失败");
			return map;
		}

	}

	/**
	 * 查询web服务器信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/selectWebServer.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> selectWebServer(WebServer web) {
		Map<String, Object> map = new HashMap<String, Object>();
		Logger.getLogger("").info("-----------查询Web服务器---------");

		try {

			List<WebServer> list = siteService.selectWebServer(web);
			map.put("message", "查询web服务器信息成功");
			map.put("code", 200);
			map.put("data", list);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "查询web服务器信息失败");
			map.put("code", 500);
			map.put("data", new ArrayList<String>());
			Logger.getLogger("").info("查询web服务器信息失败");
			return map;
		}

	}

	/**
	 * 查询其他数据源信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/selectOtherDb.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> selectOtherDb(DataSourceDo ds) {
		Map<String, Object> map = new HashMap<String, Object>();
		Logger.getLogger("").info("-----------查询其他数据源信息---------");

		try {

			List<DataSourceDo> list = siteService.selectOtherDb(ds);
			map.put("message", "查询其他数据源信息成功");
			map.put("code", 200);
			map.put("data", list);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "查询其他数据源信息失败");
			map.put("code", 500);
			map.put("data", new ArrayList<String>());
			Logger.getLogger("").info("查询其他数据源信息失败");
			return map;
		}

	}

	/**
	 * 更新其他数据源信息
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/updateOtherDb.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> updateOtherDb(DataSourceDo ds) {
		Map<String, Object> map = new HashMap<String, Object>();
		;

		Logger.getLogger("").info("-----------修改其他数据源信息---------");
		try {

			int statu = siteService.updateOtherDb(ds);
			if (statu > 0) {
				map.put("message", "修改其他数据源信息成功");
				map.put("code", 200);
				Logger.getLogger("").info("修改其他数据源信息成功");
			} else {
				map.put("message", "修改其他数据源信息失败");
				map.put("code", 500);
				Logger.getLogger("").info("修改其他数据源信息失败");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "修改其他数据源信息失败");
			map.put("code", 500);
			Logger.getLogger("").info("修改其他数据源信息失败");
			return map;
		}

	}

	/**
	 * 删除其他数据源
	 * 
	 * @param ds
	 * @return
	 */
	@RequestMapping(value = "/deleteOtherDb.do", method = RequestMethod.POST, produces = { "application/json;charset=utf-8" })
	@ResponseBody
	public Map<String, Object> deleteOtherDb(DataSourceDo ds) {

		Logger.getLogger("").info("-----------删除其他数据源信息---------");
		Map<String, Object> map = new HashMap<String, Object>();
		;

		try {
			// 删除其他数据源
			map = siteService.deleteOtherDb(ds.getId());

			Logger.getLogger("").info("删除其他数据源信息成功");

			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("message", "删除其他数据源信息失败");
			map.put("code", 500);
			Logger.getLogger("").info("删除其他数据源信息失败");
			return map;
		}

	}

	

	@ResponseBody
	@RequestMapping(value = "/testFsj.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	public void druidError(HttpServletRequest request) {
		FsjFtpParam param = new FsjFtpParam();
		param.setSitenumber("58362"); // 58548 58362
		param.setIp("60.12.107.77");
		param.setFilepath("/home/xiaoshuidian/fsjdata");
		param.setObjid("2872");
		param.setPassword("sysc123.,/");
		param.setPort("21");
		param.setUsername("xiaoshuidian");
		fsjFtpgz2.tbFtpFsj(param.getSitenumber(), param.getObjid(),
				param.getIp(), param.getPort(), param.getUsername(),
				param.getPassword(), param.getFilepath());

	}
	
	
	
	/**
	 * 查询地图配置参数
	 * @param request
	 */
	@ResponseBody
	@RequestMapping(value = "/selectMapParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	public Map<String, Object> selectMapParam(HttpServletRequest request) {
		Map<String,Object> map = new HashMap<String,Object>();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		String id = request.getParameter("id")==null?"":request.getParameter("id");
		map.put("id", id);
		list = siteService.selectMapParam(map);
		
		map.clear();
		map.put("data", list);
		return map;

	}
	
	
	
	/**
	 * 插入地图配置参数
	 * @param request
	 */
	@ResponseBody
	@RequestMapping(value = "/insertMapParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	public Map<String, Object> insertMapParam(HttpServletRequest request) {
		Map<String,Object> map = new HashMap<String,Object>();
		//List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		//String id = request.getParameter("id")==null?"":request.getParameter("id");
		String lat = request.getParameter("lat")==null?"":request.getParameter("lat");
		String lon = request.getParameter("lon")==null?"":request.getParameter("lon");
		String level = request.getParameter("level")==null?"":request.getParameter("level");
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("level", level);
		try{
			int i = siteService.insertMapParam(map);
			map.clear();
			if(i>0){
				//System.out.println("添加配置成功");
				map.put("msg","添加配置成功" );
				map.put("code", 200);
			}else{
				map.put("msg","添加配置失败" );
				map.put("code", 500);
			}
		}catch(Exception e){
			map.clear();
			e.printStackTrace();
			//System.out.println("添加配置失败");
			map.put("msg","添加配置失败" );
			map.put("code", 500);
		}
		
		
		return map;

	}
	
	
	
	
	/**
	 * 更新地图配置参数
	 * @param request
	 */
	@ResponseBody
	@RequestMapping(value = "/updateMapParam.do", method = RequestMethod.GET, produces = { "application/json;charset=utf-8" })
	public Map<String, Object> updateMapParam(HttpServletRequest request) {
		Map<String,Object> map = new HashMap<String,Object>();
		//List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		String id = request.getParameter("id")==null?"":request.getParameter("id");
		String lat = request.getParameter("lat")==null?"":request.getParameter("lat");
		String lon = request.getParameter("lon")==null?"":request.getParameter("lon");
		String level = request.getParameter("level")==null?"":request.getParameter("level");
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("level", level);
		map.put("id", id);
		try{
			int i = siteService.updateMapParam(map);
			map.clear();
			if(i>0){
				System.out.println("更新配置成功");
				map.put("msg","更新配置成功" );
				map.put("code", 200);
			}else{
				map.put("msg","更新配置失败" );
				map.put("code", 500);
			}
		}catch(Exception e){
			map.clear();
			e.printStackTrace();
			System.out.println("更新配置失败");
			map.put("msg","更新配置失败" );
			map.put("code", 500);
		}
		
		
		return map;

	}
	
	
	
	

}
