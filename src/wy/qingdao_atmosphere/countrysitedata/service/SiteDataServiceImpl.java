package wy.qingdao_atmosphere.countrysitedata.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDaoImpl;
import wy.qingdao_atmosphere.countrysitedata.domain.AttachInfoStore;
import wy.qingdao_atmosphere.countrysitedata.domain.CollectParam;
import wy.qingdao_atmosphere.countrysitedata.domain.ConnObjParam;
import wy.qingdao_atmosphere.countrysitedata.domain.CpDir;
import wy.qingdao_atmosphere.countrysitedata.domain.CpinfoObj;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceBean;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceDo;
import wy.qingdao_atmosphere.countrysitedata.domain.DbConnOid;
import wy.qingdao_atmosphere.countrysitedata.domain.FsjFtpParam;
import wy.qingdao_atmosphere.countrysitedata.domain.Param;
import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.domain.SpaceTable;
import wy.qingdao_atmosphere.countrysitedata.domain.TemperatureParam;
import wy.qingdao_atmosphere.countrysitedata.domain.DataSourceBean.DataSourceBeanBuilder;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;
import wy.qingdao_atmosphere.dynamic.DataSourceContext;
import wy.util.CalCZ;
import wy.util.FtpUtil;
import wy.util.datapersistence.Dao.BaseaddDao;

@Service("SiteDataService")
public class SiteDataServiceImpl implements SiteDataService {

	@Autowired
	private SiteDataDao siteDataDao;

	@Autowired
	public BaseaddDao baseaddDao;

	@Autowired
	private WeiBoService wb;

	public int addCollectData(List<SiteData> list) {
		return siteDataDao.addCollectData(list);
	}

	public List<Param> getParamByDtid(String devicetypeid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("devicetypeid", devicetypeid);
		return siteDataDao.getParamByDtid(map);
	}

	public List<ParamAssis> getParamAssisByDtid(String devicetypeid,
			String objid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("devicetypeid", devicetypeid);
		map.put("objid", objid);
		return siteDataDao.getParamAssisByDtid(map);
	}

	public int delCollData(String collecttime, String devicetypeid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("collecttime", collecttime);
		map.put("devicetypeid", devicetypeid);
		return siteDataDao.delCollData(map);
	}

	public String getsubStoreMaxtime(String objid, String fieldid,
			String timeformat) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		map.put("fieldid", fieldid);
		map.put("timeformat", timeformat);
		return siteDataDao.getsubStoreMaxtime(map);
	}

	/**
	 * 获取温度平流所需参数数据（辐射计的和风廓线的联合查询）
	 */
	public List<TemperatureParam> getTemperatureParam(Map<String, Object> map) {
		String fsjObjid = map.get("fsjObjid").toString(); // 辐射计objid
		String endtime = map.get("endtime").toString(); // 查询时间
		String fkxObjid = map.get("fkxObjid").toString(); // 风廓线objid
		// 查辐射计站点dataguid前缀
		Map<String, Object> fsjMap = new HashMap<String, Object>();
		fsjMap.put("objid", fsjObjid);
		Map<String, Object> fm = siteDataDao.getDataguidByOid(fsjMap);// 通过objid查询辐射计站点dataguid相关参数

		String fsjDataguid = "";
		if (fm != null && fm.size() > 0) {
			fsjDataguid = fm.get("objtypeid").toString() + "_"
					+ fm.get("objid").toString() + "_"
					+ fm.get("devicenumber").toString() + "_";
		}
		// 查风廓线站点dataguid前缀
		Map<String, Object> fkxMap = new HashMap<String, Object>();
		fkxMap.put("objid", fkxObjid);
		Map<String, Object> fkm = siteDataDao.getDataguidByOid(fkxMap);// 通过objid查询风廓线站点dataguid相关参数

		String fkxDataguid = "";
		if (fkm != null && fkm.size() > 0) {
			fkxDataguid = fkm.get("objtypeid").toString() + "_"
					+ fkm.get("objid").toString() + "_"
					+ fkm.get("devicenumber").toString() + "_";
		}
		String paramid = "168,169,170,171,172,173";

		// 获取数据库辐射数据近12小时时间
		String[] colMinMaxTime = wb.colMinMaxTimeByHour("30", 12, fkxObjid,
				paramid, "");

		// 按时间查询 格式：yyyy-MM-dd
		String begintime = "";

		if ("".equals(endtime)) {// 实时查询
			begintime = colMinMaxTime[0];
			endtime = colMinMaxTime[1];
		} else {// 带条件参数的查询 （将时间条件参数往前推12个小时）
				// 获取12小时之内的数据
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// Date date = new Date();
			Date date = new Date();
			try {
				date = sdf.parse(endtime);
			} catch (ParseException e) {
				// System.out.println("时间查询参数-endtime格式转换失败");
				Logger.getLogger("").error("时间查询参数-endtime格式转换失败");
				e.printStackTrace();
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY,
					calendar.get(Calendar.HOUR_OF_DAY) - 12);// 取几小时的数据
			begintime = sdf.format(calendar.getTime());
		}

		Map<String, Object> pmap = new HashMap<String, Object>();
		pmap.put("fkxDataguid", fkxDataguid);
		pmap.put("fsjDataguid", fsjDataguid);
		pmap.put("endtime", endtime);
		pmap.put("begintime", begintime);

		pmap.put("fsjObjid", fsjObjid);
		pmap.put("fkxObjid", fkxObjid);

		List<Map<String, Object>> list = siteDataDao.getTemperatureParam(pmap);
		List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
		List<TemperatureParam> listTemp = new ArrayList<TemperatureParam>(); // 温度平流参数对象集合
		for (Map<String, Object> paramMap : list) {
			TemperatureParam temp = new TemperatureParam(); // 温度平流参数对象
			// List<BigDecimal> spfsList = new ArrayList<BigDecimal>(); //水平风速集合
			String weidu = siteDataDao.qureyLatitude(map);// 查询站点纬度
			temp.setFai(new BigDecimal(weidu)); // 纬度
			String collecttime = paramMap.get("collecttime").toString();
			String wd = paramMap.get("wd").toString(); // 某一时刻地表温度
			temp.setCollecttime(collecttime);
			temp.setT0(new BigDecimal(wd));
			Map<String, Object> zMap = new HashMap<String, Object>(); // 包含一条结果集参数的map
			for (Map.Entry<String, Object> entry : paramMap.entrySet()) { // 遍历参数结果集

				if ("spfx".equals(entry.getKey().toString())) {
					List<BigDecimal> highList = new ArrayList<BigDecimal>(); // 高度集合
					List<BigDecimal> spfxList = new ArrayList<BigDecimal>(); // 水平方向集合
					// 使解析后的数据有序，跟数据库存储的字符串顺序对应
					LinkedHashMap<String, Object> root = JSON.parseObject(entry
							.getValue().toString(),
							new TypeReference<LinkedHashMap<String, Object>>() {
							});
					for (Map.Entry<String, Object> spfxVal : root.entrySet()) { // 水平方向的各高度值
						String key = spfxVal.getKey();
						String value = spfxVal.getValue().toString();
						// System.out.println("spfx_key:"+key);
						// System.out.println("spfx_value:"+value);
						// highList.add(new BigDecimal(key.split("_")[1]));
						// //将高度添加到高度集合里
						if (!value.contains("/")) {
							highList.add(new BigDecimal(key.split("_")[1])); // 将高度添加到高度集合里
							spfxList.add(new BigDecimal(value)); // 将该高度对应的水平方向值添加到水平方向集合里
						}

					}
					// System.out.println("水平方向list:"+spfxList);
					// System.out.println("高度list:"+highList);
					temp.setHz(highList.toArray(new BigDecimal[highList.size()])); // 高度集合转数组
					temp.setV(spfxList.toArray(new BigDecimal[spfxList.size()])); // 水平风向集合转数组
				} else if ("spfs".equals(entry.getKey().toString())) {
					// List<BigDecimal> highList = new ArrayList<BigDecimal>();
					// //高度集合
					List<BigDecimal> spfsList = new ArrayList<BigDecimal>(); // 水平风速集合
					// 使解析后的数据有序，跟数据库存储的字符串顺序对应
					LinkedHashMap<String, Object> root = JSON.parseObject(entry
							.getValue().toString(),
							new TypeReference<LinkedHashMap<String, Object>>() {
							});
					for (Map.Entry<String, Object> spfxVal : root.entrySet()) { // 水平风速的各高度值
						String key = spfxVal.getKey();
						String value = spfxVal.getValue().toString();
						// System.out.println("spfs_key:"+key);
						// System.out.println("spfs_value:"+value);

						// spfsList.add(new BigDecimal(value));
						// //将该高度对应的水平风速值添加到水平风速集合里
						if (!value.contains("/")) {
							spfsList.add(new BigDecimal(value)); // 将该高度对应的水平风速值添加到水平风速集合里
						}
					}
					// System.out.println("水平风速list:"+spfsList);
					// System.out.println("高度list:"+highList);
					// temp.setHz(highList.toArray(new
					// BigDecimal[highList.size()])); //高度集合转数组
					temp.setD(spfsList.toArray(new BigDecimal[spfsList.size()])); // 水平风速集合转数组
				}
			}
			listTemp.add(temp); // 将temp参数对象添加到集合中
		}

		// System.out.println("------------------------分割线--------------------------");
		/*
		 * for(TemperatureParam decimal:listTemp){
		 * System.out.println("paramobject:"+decimal); }
		 */
		return listTemp;
	}

	/**
	 * 获取水汽通量
	 */
	public List<TemperatureParam> getWaterVapor(Map<String, Object> map) {
		String fsjObjid = map.get("fsjObjid").toString(); // 辐射计objid
		String endtime = map.get("endtime").toString(); // 查询时间
		String fkxObjid = map.get("fkxObjid").toString(); // 风廓线objid
		// 查辐射计站点dataguid前缀
		Map<String, Object> fsjMap = new HashMap<String, Object>();
		fsjMap.put("objid", fsjObjid);
		Map<String, Object> fm = siteDataDao.getDataguidByOid(fsjMap);// 通过objid查询辐射计站点dataguid相关参数

		String fsjDataguid = "";
		if (fm != null && fm.size() > 0) {
			fsjDataguid = fm.get("objtypeid").toString() + "_"
					+ fm.get("objid").toString() + "_"
					+ fm.get("devicenumber").toString() + "_";
		}
		// 查风廓线站点dataguid前缀
		Map<String, Object> fkxMap = new HashMap<String, Object>();
		fkxMap.put("objid", fkxObjid);
		Map<String, Object> fkm = siteDataDao.getDataguidByOid(fkxMap);// 通过objid查询风廓线站点dataguid相关参数

		String fkxDataguid = "";
		if (fkm != null && fkm.size() > 0) {
			fkxDataguid = fkm.get("objtypeid").toString() + "_"
					+ fkm.get("objid").toString() + "_"
					+ fkm.get("devicenumber").toString() + "_";
		}
		if ("".equals(endtime)) {
			// endtime = baseaddDao.getMaxTimeByDtid("yyyy-MM-dd HH24:MI:ss",
			// "5", fsjObjid);
			Map<String, Object> zmap = new HashMap<String, Object>();
			zmap.put("dataguid", fkxDataguid);
			zmap.put("objid", fkxObjid);
			endtime = siteDataDao.getFkxMaxTime(zmap); // 获得风廓线某站点最新时间
		}

		Map<String, Object> pmap = new HashMap<String, Object>();
		pmap.put("fkxDataguid", fkxDataguid);
		pmap.put("fsjDataguid", fsjDataguid);
		pmap.put("endtime", endtime);

		pmap.put("fsjObjid", fsjObjid);
		pmap.put("fkxObjid", fkxObjid);

		System.out.println("endtime:" + endtime);
		// System.out.println("fkxDataguid"+ fkxDataguid);
		// System.out.println("fsjDataguid"+ fsjDataguid);
		List<Map<String, Object>> list = siteDataDao.getWaterVapor(pmap); // 查水汽通量的数据(czfs,spfs,wd,yq,time,wdkx,sdkx)
		// List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();
		List<TemperatureParam> listTemp = new ArrayList<TemperatureParam>(); // 温度平流参数对象集合
		for (Map<String, Object> paramMap : list) {
			TemperatureParam temp = new TemperatureParam(); // 温度平流参数对象
			// List<BigDecimal> spfsList = new ArrayList<BigDecimal>(); //水平风速集合
			String weidu = siteDataDao.qureyLatitude(map);// 查询站点纬度
			temp.setFai(new BigDecimal(weidu)); // 纬度
			String collecttime = paramMap.get("collecttime").toString();
			String wd = paramMap.get("wd").toString(); // 某一时刻地表温度
			String yq = paramMap.get("yq").toString(); // 某一时刻地面压强
			temp.setCollecttime(collecttime);
			temp.setT0(new BigDecimal(wd));
			// temp.setT0(new BigDecimal(23)); //地表温度
			// temp.setP0(new BigDecimal(1025)); //压强
			temp.setP0(new BigDecimal(yq));
			Map<String, Object> zMap = new HashMap<String, Object>(); // 包含一条结果集参数的map

			List<BigDecimal> fshighList = new ArrayList<BigDecimal>(); // 垂直风速高度集合（水平风速的高度集合应该是一致的）
			List<BigDecimal> sdhighList = new ArrayList<BigDecimal>(); // 湿度廓线高度集合

			// czfs(单独提出来时为了提前获得风速集合，为辐射计的温度和湿度的插值算法做准备,其他指标也可以提出来，但我懒得改了)
			List<BigDecimal> czfsList = new ArrayList<BigDecimal>(); // 垂直风速集合
			String czfs = paramMap.get("czfs").toString();// czfs
			// 使解析后的数据有序，跟数据库存储的字符串顺序对应
			LinkedHashMap<String, Object> root1 = JSON.parseObject(czfs,
					new TypeReference<LinkedHashMap<String, Object>>() {
					});
			for (Map.Entry<String, Object> czfsVal : root1.entrySet()) { // 水平方向的各高度值
				String key = czfsVal.getKey();
				String value = czfsVal.getValue().toString();
				// System.out.println("spfx_key:"+key);
				// System.out.println("spfx_value:"+value);
				// System.out.println("czfs_key:"+key+"---------------------------------------");
				if (!value.contains("/")) {
					fshighList.add(new BigDecimal(key.split("_")[1])); // 将高度添加到高度集合里
					czfsList.add(new BigDecimal(value)); // 将该高度对应的垂直风速值添加到水平方向集合里
				}

			}
			// System.out.println("垂直风速list:"+czfsList);
			// System.out.println("垂直风速高度list:"+fshighList.size());
			// System.out.println("垂直风速高度list:"+fshighList);
			// System.out.println("************************************************");
			temp.setHz(fshighList.toArray(new BigDecimal[fshighList.size()])); // 高度集合转数组
			temp.setVz(czfsList.toArray(new BigDecimal[czfsList.size()])); // 垂直风速集合转数组
			temp.setHighsize(fshighList.size());// 垂直风速层数

			for (Map.Entry<String, Object> entry : paramMap.entrySet()) { // 遍历参数结果集

				if ("czfs".equals(entry.getKey().toString())) {
					// List<BigDecimal> highList = new ArrayList<BigDecimal>();
					// //高度集合
					/*
					 * List<BigDecimal> czfsList = new ArrayList<BigDecimal>();
					 * //垂直风速集合 //使解析后的数据有序，跟数据库存储的字符串顺序对应 LinkedHashMap<String,
					 * Object>
					 * root=JSON.parseObject(entry.getValue().toString(),new
					 * TypeReference<LinkedHashMap<String, Object>>(){} );
					 * for(Map.Entry<String, Object>spfxVal:root.entrySet()){
					 * //水平方向的各高度值 String key = spfxVal.getKey(); String value =
					 * spfxVal.getValue().toString();
					 * //System.out.println("spfx_key:"+key);
					 * //System.out.println("spfx_value:"+value);
					 * System.out.println
					 * ("spfs_key:"+key+"---------------------------------------"
					 * ); if(!value.contains("/")){ fshighList.add(new
					 * BigDecimal(key.split("_")[1])); //将高度添加到高度集合里
					 * czfsList.add(new BigDecimal(value));
					 * //将该高度对应的垂直风速值添加到水平方向集合里 }
					 * 
					 * } //System.out.println("垂直风速list:"+czfsList);
					 * System.out.println("垂直风速高度list:"+fshighList.size());
					 * System.out.println("垂直风速高度list:"+fshighList);
					 * System.out.println
					 * ("************************************************");
					 * temp.setHz(fshighList.toArray(new
					 * BigDecimal[fshighList.size()])); //高度集合转数组
					 * temp.setVz(czfsList.toArray(new
					 * BigDecimal[czfsList.size()])); //垂直风速集合转数组
					 * temp.setHighsize(fshighList.size());//垂直风速层数
					 */} else if ("spfs".equals(entry.getKey().toString())) {
					// List<BigDecimal> highList = new ArrayList<BigDecimal>();
					// //高度集合
					List<BigDecimal> spfsList = new ArrayList<BigDecimal>(); // 水平风速集合
					// 使解析后的数据有序，跟数据库存储的字符串顺序对应
					LinkedHashMap<String, Object> root = JSON.parseObject(entry
							.getValue().toString(),
							new TypeReference<LinkedHashMap<String, Object>>() {
							});
					for (Map.Entry<String, Object> spfxVal : root.entrySet()) { // 水平风速的各高度值
						String key = spfxVal.getKey();
						String value = spfxVal.getValue().toString();
						// System.out.println("spfs_key:"+key);
						// System.out.println("spfs_value:"+value);

						// spfsList.add(new BigDecimal(value));
						// //将该高度对应的水平风速值添加到水平风速集合里
						if (!value.contains("/")) {
							spfsList.add(new BigDecimal(value)); // 将该高度对应的水平风速值添加到水平风速集合里
						}
					}
					// System.out.println("水平风速list:"+spfsList);
					// System.out.println("高度list:"+highList);
					// temp.setHz(highList.toArray(new
					// BigDecimal[highList.size()])); //高度集合转数组
					temp.setD(spfsList.toArray(new BigDecimal[spfsList.size()])); // 水平风速集合转数组
				} else if ("wdkx".equals(entry.getKey().toString())) {
					// List<BigDecimal> highList = new ArrayList<BigDecimal>();
					// //温度廓线高度集合
					List<BigDecimal> wdkxList = new ArrayList<BigDecimal>(); // 温度廓线集合

					String lastHt = "0"; // 上个高度，目标高度前一个(比它小的)插值算法要用到
					String lastvl = "0"; // 上个高度的值，目标高度前一个(比它小的)插值算法要用到
					// 使解析后的数据有序，跟数据库存储的字符串顺序对应
					LinkedHashMap<String, Object> root = JSON.parseObject(entry
							.getValue().toString(),
							new TypeReference<LinkedHashMap<String, Object>>() {
							});
					for (BigDecimal height : fshighList) { // 遍历垂直风速高度集合
						for (Map.Entry<String, Object> spfxVal : root
								.entrySet()) { // 温度廓线的各高度值
							String key = spfxVal.getKey();
							String value = spfxVal.getValue().toString();
							// System.out.println("wd_key:"+key);
							// System.out.println("wd_value:"+value);

							// spfsList.add(new BigDecimal(value));
							// //将该高度对应的温度廓线值添加到温度廓线集合里
							if (!"wd_type".equals(key) && !"wd_fc".equals(key)) {
								if (!value.contains("/")) {

									if (Integer.parseInt(key.split("_")[1]) <= 10000) { // 将高度限定在10000米以内
										// sdhighList.add(new
										// BigDecimal(key.split("_")[1]));
										// //将高度添加到高度集合里

										// for(BigDecimal height:fshighList){
										// //遍历垂直风速高度集合
										if (Integer.parseInt(height
												.toPlainString()) <= Integer
												.parseInt(key.split("_")[1])) {

											// System.out.println("heigt.tostring:"+height);
											// if(!value.contains("/")){
											if (Integer.parseInt(height
													.toPlainString()) == (Integer
													.parseInt(key.split("_")[1]))) {

												wdkxList.add(new BigDecimal(
														value)); // 将该高度对应的湿度廓线值添加到湿度廓线集合里

												// System.out.println("------------------start");

												// System.out.println("wd_result:"+value);
												// System.out.println("wd_key:"+key);
												// /System.out.println("wd_value:"+value);
												// System.out.println("wd_height:"+height);
												// System.out.println("------------------end");
												break;
											} else {

												if ("/".equals(lastvl)
														|| "/".equals(value)) { // 斜杠参与插值运算会报错
													wdkxList.add(new BigDecimal(
															value)); // 将该高度对应的湿度廓线值添加到湿度廓线集合里

													break;
												} else {
													// resultMap.put("wd",
													// value);
													float result = CalCZ.calCZ(
															lastHt,
															key.split("_")[1],
															height.toString(),
															lastvl, value);

													BigDecimal b = new BigDecimal(
															result);
													String r = b
															.setScale(
																	3,
																	BigDecimal.ROUND_HALF_UP)
															.toString(); // 保留三位小数

													wdkxList.add(new BigDecimal(
															r)); // 将该高度对应的湿度廓线值添加到湿度廓线集合里
													// System.out.println("wd_result:"+result);
													// System.out.println("wd_key:"+key);
													// System.out.println("wd_value:"+value);
													// System.out.println("wd_height:"+height);
													break;
												}
											}
											// }
										} else {
											lastHt = key.split("_")[1]; // 上个高度
											lastvl = value; // 上个高度的值
										}

									}
									// wdkxList.add(new BigDecimal(value));
									// //将该高度对应的温度廓线值添加到温度廓线集合里
								}

							}
						}

					}
					// System.out.println("水平风速list:"+spfsList);

					// System.out.println("温度廓线高度list:"+highList.size());

					// System.out.println("温度廓线高度list:"+highList);
					// temp.setHz(highList.toArray(new
					// BigDecimal[highList.size()])); //温度廓线高度集合转数组
					temp.setT(wdkxList.toArray(new BigDecimal[wdkxList.size()])); // 温度廓线集合转数组
					// System.out.println("温度廓线高度list:"+temp.getHz().length);
					// System.out.println("温度廓线高度list.size:"+temp.getT().length);
					// System.out.println("温度廓线高度list:"+Arrays.asList(temp.getT()));
				} else if ("sdkx".equals(entry.getKey().toString())) {
					// List<BigDecimal> sdhighList = new
					// ArrayList<BigDecimal>(); //湿度廓线高度集合
					List<BigDecimal> sdkxList = new ArrayList<BigDecimal>(); // 湿度廓线集合
					String lastHt = "0"; // 上个高度，目标高度前一个(比它小的)插值算法要用到
					String lastvl = "0"; // 上个高度的值，目标高度前一个(比它小的)插值算法要用到
					// 使解析后的数据有序，跟数据库存储的字符串顺序对应
					LinkedHashMap<String, Object> root = JSON.parseObject(entry
							.getValue().toString(),
							new TypeReference<LinkedHashMap<String, Object>>() {
							});
					for (BigDecimal height : fshighList) { // 遍历垂直风速高度集合
						for (Map.Entry<String, Object> spfxVal : root
								.entrySet()) { // 湿度廓线的各高度值
							String key = spfxVal.getKey();
							String value = spfxVal.getValue().toString();
							// System.out.println("sd_key:"+key);
							// System.out.println("sd_value:"+value);

							// spfsList.add(new BigDecimal(value));
							// //将该高度对应的湿度廓线值添加到湿度廓线集合里
							if (!"xdsd_type".equals(key)
									&& !"xdsd_fc".equals(key)) {
								if (!value.contains("/")) {
									if (Integer.parseInt(key.split("_")[1]) <= 10000) { // 将高度限定在10000米以内
										sdhighList.add(new BigDecimal(key
												.split("_")[1])); // 将高度添加到高度集合里

										// for(BigDecimal height:fshighList){
										// //遍历垂直风速高度集合
										if (Integer.parseInt(height
												.toPlainString()) <= Integer
												.parseInt(key.split("_")[1])) {

											// System.out.println("heigt.tostring:"+height);
											// if(!value.contains("/")){
											if (Integer.parseInt(height
													.toPlainString()) == (Integer
													.parseInt(key.split("_")[1]))) {

												sdkxList.add(new BigDecimal(
														value)); // 将该高度对应的湿度廓线值添加到湿度廓线集合里

												// System.out.println("------------------start");

												// System.out.println("wd_result:"+value);
												// System.out.println("wd_key:"+key);
												// /System.out.println("wd_value:"+value);
												// System.out.println("wd_height:"+height);
												// System.out.println("------------------end");
												break;
											} else {

												if ("/".equals(lastvl)
														|| "/".equals(value)) { // 斜杠参与插值运算会报错
													sdkxList.add(new BigDecimal(
															value)); // 将该高度对应的湿度廓线值添加到湿度廓线集合里

													break;
												} else {
													// resultMap.put("wd",
													// value);
													float result = CalCZ.calCZ(
															lastHt,
															key.split("_")[1],
															height.toString(),
															lastvl, value);

													BigDecimal b = new BigDecimal(
															result);
													String r = b
															.setScale(
																	3,
																	BigDecimal.ROUND_HALF_UP)
															.toString();

													sdkxList.add(new BigDecimal(
															r)); // 将该高度对应的湿度廓线值添加到湿度廓线集合里
													// System.out.println("wd_result:"+result);
													// System.out.println("wd_key:"+key);
													// System.out.println("wd_value:"+value);
													// System.out.println("wd_height:"+height);
													break;
												}
											}
											// }
										} else {
											lastHt = key.split("_")[1]; // 上个高度
											lastvl = value; // 上个高度的值
										}

									}

								}

							}
						}
					}
					// System.out.println("水平风速list:"+spfsList);

					// temp.setHz(highList.toArray(new
					// BigDecimal[highList.size()])); //湿度廓线高度集合转数组
					temp.setRH(sdkxList.toArray(new BigDecimal[sdkxList.size()])); // 湿度廓线集合转数组

					// System.out.println("湿度廓线高度list:"+temp.getHz().length);
					// System.out.println("湿度廓线高度list长度:"+temp.getRH().length);
					// System.out.println("湿度廓线高度list:"+Arrays.asList(temp.getRH()));

					// System.out.println("sdHighList.size:"+sdhighList.size());
					// System.out.println("fshighList.size:"+fshighList.size());
					if (sdhighList.size() < fshighList.size()) { // 选湿度集合的数组长度（选较小的一个集合长度）因为风廓线和辐射计的高度集合不一致
						// System.out.println("湿度廓线高度层数较小");
						// System.out.println("风速廓线高度层数："+sdhighList.size());
						// System.out.println("温度廓线高度层数："+fshighList.size());
						System.out.println("sdHighList.size比较小："
								+ sdhighList.size());
						temp.setHighsize(sdhighList.size()); // 垂直风速层数
					}
				}

			}

			// 假数据 温度廓线，湿度廓线
			/*
			 * int length = temp.getHz().length; BigDecimal[] t = new
			 * BigDecimal[temp.getHz().length]; for(int i=0;i<length;i++){ t[i]
			 * = temp.getT0().subtract(new
			 * BigDecimal(6.5).multiply(temp.getHz()[i]).divide(new
			 * BigDecimal(1000))); } BigDecimal[] RH = new BigDecimal[length];
			 * for(int i=0;i<length;i++){ RH[i] = new
			 * BigDecimal(65).subtract(new
			 * BigDecimal(0.75).multiply(temp.getHz()[i]).divide(new
			 * BigDecimal(100))); } temp.setT(t); //温度廓线 temp.setRH(RH); //湿度廓线
			 */
			// 以上是假数据部分
			listTemp.add(temp); // 将temp参数对象添加到集合中
		}

		// System.out.println("------------------------分割线--------------------------");
		/*
		 * for(TemperatureParam decimal:listTemp){
		 * System.out.println("paramobject:"+decimal); }
		 */
		return listTemp;
	}

	/**
	 * 根据辐射计objid查对应站点的风廓线的objid
	 */
	public String queryFkxOidhByFsjOid(Map<String, Object> pmap) {
		return siteDataDao.queryFkxOidhByFsjOid(pmap);
	}
	
	/**
	 * 根据风廓线objid查对应站点的辐射计的objid
	 */
	public String queryFsjOidhByFkxOid(Map<String, Object> pmap) {
		return siteDataDao.queryFsjOidhByFkxOid(pmap);
	}

	/**
	 * 风廓线-所有站点规定高度下各个时间的风速，风向信息 new
	 */
	public List<Map<String, Object>> getFkxAllSitesInfo(
			HttpServletRequest request) {
		Map<String, Object> pmap = new HashMap<String, Object>();
		List<Map<String, Object>> dataList = siteDataDao
				.getFkxAllSitesInfo(pmap);
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();// 封装的需要返回的结果集
		if (dataList != null && dataList.size() > 0) {

			for (Map<String, Object> map : dataList) {
				Map<String, Object> resultMap = new HashMap<String, Object>();// 单条结果集，封装固定高度的风速，风向，objid
				String objid = map.get("objid").toString();
				// System.out.println("当前objid为:"+objid);
				// resultMap.put("objid",objid);//添加objid
				for (Map.Entry<String, Object> entry : map.entrySet()) { // 遍历参数结果集

					if ("czfs".equals(entry.getKey().toString())) {

						// 使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root = JSON
								.parseObject(
										entry.getValue().toString(),
										new TypeReference<LinkedHashMap<String, Object>>() {
										});
						for (Map.Entry<String, Object> spfxVal : root
								.entrySet()) { // 垂直风速的各高度值
							String key = spfxVal.getKey();
							String value = spfxVal.getValue().toString();
							// System.out.println("czfs_key:"+key);
							// System.out.println("czfs_value:"+value);
							if ("czfs_60".equals(key)) {
								// if(!value.contains("/")){
								resultMap.put("czfs", value);
								resultMap.put("objid", objid);// 添加objid
								break;
								// }
							}

						}

					} else if ("spfs".equals(entry.getKey().toString())) {

						// 使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root = JSON
								.parseObject(
										entry.getValue().toString(),
										new TypeReference<LinkedHashMap<String, Object>>() {
										});
						for (Map.Entry<String, Object> spfxVal : root
								.entrySet()) { // 水平风速的各高度值
							String key = spfxVal.getKey();
							String value = spfxVal.getValue().toString();
							// System.out.println("spfs_key:"+key);
							// System.out.println("spfs_value:"+value);

							if ("spfs_60".equals(key)) {
								// if(!value.contains("/")){
								resultMap.put("spfs", value);
								break;
								// }
							}
						}

					} else if ("spfx".equals(entry.getKey().toString())) {

						// 使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root = JSON
								.parseObject(
										entry.getValue().toString(),
										new TypeReference<LinkedHashMap<String, Object>>() {
										});
						for (Map.Entry<String, Object> spfxVal : root
								.entrySet()) { // 水平风向的各高度值
							String key = spfxVal.getKey();
							String value = spfxVal.getValue().toString();
							// System.out.println("spfx_key:"+key);
							// System.out.println("spfx_value:"+value);

							if ("spfx_60".equals(key)) {
								// if(!value.contains("/")){
								resultMap.put("spfx", value);
								break;
								// }
							}
						}

					}

				}
				resultList.add(resultMap); // 将单条结果（objid,spfx,spfs,czfs）插入结果集中
			}
		}
		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap.put("data", resultList);
		return resultList;
	}

	/**
	 * 一张图-风廓线，获得实况监测目录（高度及下面的时刻列表） new
	 */
	public Map<String, Object> getMoinitorMenu(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> fkxList = new ArrayList<Map<String, Object>>();

		int objtypeid = 28;
		Map<String, Object> parMap = new HashMap<String, Object>();
		List<String> fkxObjids = siteDataDao.getObjidByOtid(objtypeid);
		Map<String, Object> zMap = new HashMap<String, Object>();

		zMap.put("list", fkxObjids);
		List<String> data = siteDataDao.getAllfkxDataguids(zMap);// 查找所有去重后的风廓线站点dataguid(spfx,spfs,czfs)
		if (data != null && data.size() > 0) {
			StringBuilder dataguids = new StringBuilder();
			// dataguids.append("(");
			for (int i = 0; i < data.size(); i++) {
				if (i == data.size() - 1) {
					dataguids.append("'" + data.get(i) + "'"); // 最后一个不加，
				} else {
					dataguids.append("'" + data.get(i) + "',");
				}

			}
			// dataguids.append(")");

			String dataStr = dataguids.toString();

			// System.out.println("dataStr:"+dataStr);

			parMap.put("dataguids", dataStr);

			List<String> objids = siteDataDao.getObjidByOtid(objtypeid);

			parMap.put("list", objids);

			System.out.println("objlist:" + objids);

			List<String> hoursList = siteDataDao.getFkxHoursList(parMap); // 风廓线和辐射计统一查风廓线的时刻列表（风廓线的时间的统一的，辐射计不统一）

			String[] fkxH = { "120", "300", "480", "660", "840", "1020",
					"1200", "1380", "1560", "1720", "1900", "2080", "2260",
					"2440", "2620", "2800", "2980", "3160", "3340", "3520",
					"3700", "3880", "4060", "4240", "4420", "4600", "4780",
					"4960", "5140", "5320", "5500", "5680", "5860", "6020",
					"6200", "6380", "6560", "6740", "6920", "7100", "7280",
					"7460" }; // 规定的 风廓线的高度列表
			// String[] fsjH = {"100","300","500","700","900","1100"}; //规定的
			// 辐射计的高度列表

			// List<Map<String,Object>> fsjList = new
			// ArrayList<Map<String,Object>>();
			for (int i = 0; i < fkxH.length; i++) {
				Map<String, Object> zmap = new HashMap<String, Object>();
				List<String> dataList = new ArrayList<String>();
				for (int j = 0; j < hoursList.size(); j++) {
					dataList.add(hoursList.get(j));
				}
				zmap.put(fkxH[i], dataList);
				fkxList.add(zmap);
			}
		}

		/*
		 * for(int i=0;i<fsjH.length;i++){ Map<String,Object> zmap = new
		 * HashMap<String,Object>(); List<String> dataList=new
		 * ArrayList<String>(); for(int j=0;j<hoursList.size();j++){
		 * dataList.add(hoursList.get(j)); } zmap.put(fsjH[i], dataList);
		 * fsjList.add(zmap); }
		 */
		// map.put("风廓线雷达", fkxList);

		// map.put("微波辐射计", fsjList);

		map.put("实况监测", fkxList);
		return map;
	}

	/**
	 * 查询时间戳表是否有该站点的数据
	 */
	public List<Map<String, Object>> querySiteTimestamp(Map<String, Object> map) {
		return siteDataDao.querySiteTimestamp(map);
	}

	/**
	 * 插入一条该站点最新时间的时间戳
	 */
	public int addSiteTimestamp(Map<String, Object> map) {
		return siteDataDao.addSiteTimestamp(map);
	}

	/**
	 * 更新该站点最新时间的时间戳
	 */
	public int updateSiteTimestamp(Map<String, Object> map) {
		return siteDataDao.updateSiteTimestamp(map);
	}

	/**
	 * 获得辐射计某站点最新时间
	 */
	public String getFsjMaxTime(Map<String, Object> zmap) {
		return siteDataDao.getFsjMaxTime(zmap);
	}

	/**
	 * 查询所有辐射计站点
	 */
	public List<Map<String, Object>> findAllFsj(Map<String, Object> zmap) {
		return siteDataDao.findAllFsj(zmap);
	}

	/**
	 * 查询所有风廓线站点
	 */
	public List<Map<String, Object>> findAllFkx(Map<String, Object> zmap) {
		return siteDataDao.findAllFkx(zmap);
	}

	/**
	 * 查询所有融合图站点目录
	 */
	public List<Map<String, Object>> getDbfxFuseMenu(
			HashMap<String, Object> hashMap) {

		List<Map<String, Object>> list = siteDataDao.findAllFsj(hashMap);
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : list) {
			int objid = map.get("objid") == null ? 0 : Integer.parseInt(map
					.get("objid").toString());
			// System.out.println("map.objid:"+objid);
			Map<String, Object> pmap = new HashMap<String, Object>();
			pmap.put("objid", objid);
			String fkxObjid = siteDataDao.queryFkxOidhByFsjOid(pmap);
			if (fkxObjid != null) { // 说明此站点既有风廓线又有辐射计的
				resultList.add(map);
			}
		}
		return resultList;
	}

	/**
	 * 查询其他数据库的实时数据
	 */

	public List<CollectParam> findOhterDbCollect(Map<String, Object> map) {
		return siteDataDao.findOhterDbCollect(map);
	}

	/*
	 * public int insertCollect(List<CollectParam> list) { return
	 * siteDataDao.insertCollect(list); }
	 */

	public int insertCollect(Map<String, Object> map) {
		return siteDataDao.insertCollect(map);
	}

	/**
	 * . 分批插入实时监测数据 TODO 递归:分割长List为 subNum/段。
	 * 
	 * @param thesisList
	 *            论文list(总)
	 * @param subNum
	 *            每段长度 (最小1)
	 * @return
	 * @throws Exception
	 */
	public int recurSub(List<CollectParam> thesisList, int subNum,
			Integer baseObjid) throws Exception {
		// 参数合法性判断:
		if (thesisList.isEmpty())
			return 0;
		if (subNum < 1)
			return 0;

		// 大于subNum，进入分割
		if (thesisList.size() > subNum) {// && !(thesisList.isEmpty())
			// 将前subNum分出来，直接插入到数据库。
			List<CollectParam> toInsert = thesisList.subList(0, subNum);
			// 将subNum至最后 (剩余部分) 继续进行递归分割
			List<CollectParam> toRecurSub = thesisList.subList(subNum,
					thesisList.size());
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("list", toInsert);
			// int i = siteService.insertCollect(cList);
			map.put("objid", baseObjid);
			// 将前subNum分出来，直接插入到数据库 && 将subNum至最后 (剩余部分) 继续进行递归分割 。统计数量
			return siteDataDao.insertCollect(map)
					+ recurSub(toRecurSub, subNum, baseObjid);

			// 少于subNum，直接插入数据库 (递归出口)
		} else {

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("list", thesisList);
			// int i = siteService.insertCollect(cList);
			map.put("objid", baseObjid);
			// 插入到数据库。统计数量
			return siteDataDao.insertCollect(map);
		}
	}

	public List<Map<String, Object>> getFtpInfo(HttpServletRequest request) {
		String objid = request.getParameter("objid") == null ? "" : request
				.getParameter("objid");
		String objtypeid = request.getParameter("objtypeid") == null ? ""
				: request.getParameter("objtypeid");
		String id = request.getParameter("id") == null ? "" : request
				.getParameter("id");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objid", objid);
		map.put("objtypeid", objtypeid);
		map.put("id", id);
		return siteDataDao.getFtpInfo(map);
	}

	public int updateFtpInfo(FsjFtpParam param) {

		// FTPClient ftpClient = FtpUtil.ftpConnection(param.getIp(),
		// Integer.parseInt(param.getPort()), param.getUsername(),
		// param.getPassword());
		// 测试该ftp服务器地址是否能连上
		FTPClient ftpClient = new FTPClient();
		ftpClient.setControlEncoding("UTF8");
		try {
			ftpClient.connect(param.getIp(), Integer.parseInt(param.getPort()));
			ftpClient.login(param.getUsername(), param.getPassword());
			ftpClient.enterLocalPassiveMode();// 设置为被动模式
			int replyCode = ftpClient.getReplyCode(); // 是否成功登录服务器
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				ftpClient.disconnect();
				System.out.println("--ftp连接失败--");
				// System.exit(1);
				return -1; // 连接ftp失败
			}
			// ftpClient.enterLocalPassiveMode();//这句最好加告诉服务器开一个端口
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FtpUtil.ftpClose(ftpClient);
		return siteDataDao.updateFtpInfo(param);
	}

	public int usedFtpServer(FsjFtpParam param) {
		return siteDataDao.updateFtpInfo(param);
	}

	/**
	 * 添加其他数据源信息,同步其他数据源站点相关信息
	 * 
	 * @throws Exception
	 */
	// @Transactional(rollbackFor=Exception.class)
	public Map<String, Object> addOtherDb(DataSourceDo ds) throws Exception {
		Map<String, Object> rMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		rMap.put("code", "200");
		StringBuilder sb = new StringBuilder(); // 拼接返回的结果信息

		ds.setDataSourceName(System.currentTimeMillis() + "_"
				+ (int) (Math.random() * 1000));// 防止数据源重名

		Integer dbid = siteDataDao.addOtherDb(ds); // 新增数据源
		if (dbid > 0) {// 新增数据源成功
			sb.append("新增数据源成功!\n");
			System.out.println("dbid:" + dbid);
			System.out.println("ds.objid:" + ds.getId());// 新增数据源后返回的id
			// 查询新的数据源信息,并切换数据源，查询需要的该数据源的站点的相关信息
			map.put("id", ds.getId());
			List<DataSourceDo> list = siteDataDao.queryOtherDataSource(map);
			System.out.println("dslist:" + list);
			if (list.size() > 0) { // 查询新增的数据源成功
				sb.append("查询到了新增的数据源!\n");

				DataSourceDo newDs = list.get(0); // 刚刚新增的数据源

				// 切换到新增的数据源
				DataSourceBean dataSourceBean = new DataSourceBean(
						new DataSourceBeanBuilder(newDs.getDataSourceName(),
								newDs.getDatabaseIp(), newDs.getDatabasePort(),
								newDs.getDatabaseName(),
								newDs.getDatabaseUsername(),
								newDs.getPassword()));
				try {
					DataSourceContext.setDataSource(dataSourceBean);
					System.out.println("切换数据源到：" + newDs.getDataSourceName());
					System.out.println("datasourceBean:" + dataSourceBean);
					// int i = 5/0;
					sb.append("尝试切换到新增的数据源成功!\n");

				} catch (Exception e) {
					// e.printStackTrace();
					sb.append("尝试切换到新增的数据源失败!，请检查新增的数据源信息是否正确有效\n");
					rMap.put("message", sb.toString());
					throw new Exception(sb.toString()); // 直接退出
				}

				map.clear();

				map.put("objtypeid", 12); // 12-辐射计
											// 28风廓线(非平台的数据源里面最多只有一个辐射计和风廓线设备)

				CpinfoObj obj = siteDataDao.selectOtherDbobjByOtp(map); // 通过objtypeid查找新数据源中的设备对象信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的)
				List<AttachInfoStore> infoList = siteDataDao
						.selectOtherDbAttachInfoByOtp(map); // 通过objtypeid查找新数据源中的设备基本信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的）
				List<ConnObjParam> conList = siteDataDao
						.selectOtherDbConnObjParamByOtp(map); // 通过objtypeid查找对象和参数的关联信息
				map.put("spaceTable", "space_wbstation"); // 辐射计的空间表 ，风廓线的为
															// space_windprofile
				SpaceTable space = siteDataDao.selectOtherDbSpaceByOtp(map); // 通过objtypeid查找新数据源中的设备空间信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的）
				DataSourceContext.toDefault(); // 切回到默认数据库
				System.out.println("切回到默认数据库-----------------");
				System.out.println("obj:" + obj);
				System.out.println("info:" + infoList);
				System.out.println("space:" + space);
				if (obj != null) { // 该新数据源下有设备（辐射计或风廓线）
					sb.append("该数据源下找到了该类型的设备!\n");
					// 将该obj(设备)插入到此台服务器的数据库中
					CpinfoObj insertObj = new CpinfoObj(); // 要新增到本地服务器的设备
					insertObj.setObjname(obj.getObjname());
					insertObj.setObjtypeid(obj.getObjtypeid());
					insertObj.setObjnum(obj.getObjnum());
					insertObj.setRemark(obj.getRemark());
					insertObj.setIsused(obj.getIsused());
					Integer status = siteDataDao.addObjTwo(insertObj); // 将新数据源下的设备插入到本地数据库中成功后返回新增的objid
					if (status > 0) {// 插入设备到本地数据库成功
						sb.append("将该数据源下的设备obj信息同步到本地服务器成功!\n");
						Integer newObjid = insertObj.getObjid(); // 新增的本地数据库的
																	// 设备objid
						Integer oldObjid = obj.getObjid();// 新增的数据源中的原objid
						System.out.println("newObjid:" + newObjid);
						System.out.println("oldObjid:" + oldObjid);
						map.clear();
						map.put("objid", newObjid);
						map.put("otherobjid", oldObjid);
						map.put("dsname", newDs.getDataSourceName());
						map.put("dsid", newDs.getId());
						System.out.println("dsid:" + newDs.getId());
						System.out.println("dsname:"
								+ newDs.getDataSourceName());
						int i = siteDataDao.addObjIdConnect(map);
						if (i > 0) { // 插入objid关联表成功
							// 插入objid关联表成功
							sb.append("建立该数据源与本地数据库的objid关联成功!\n");
						} else {
							rMap.put("code", "500");
							sb.append("建立该数据源与本地数据库的objid关联失败\n");
						}

						// 插入空间表
						if (space != null) { //
							sb.append("该数据源空间表内存在该设备的数据!\n");
							SpaceTable spTable = new SpaceTable();
							spTable.setTablename("space_wbstation");
							spTable.setObjid(newObjid); // 改成本地服务器中新增的objid
							spTable.setId(newObjid); // 改成本地服务器中新增的objid

							// space.getShape()
							// {"type":"Point","coordinates":[118.24,29.08]}
							LinkedHashMap<String, Object> root = JSON
									.parseObject(
											space.getShape(),
											new TypeReference<LinkedHashMap<String, Object>>() {
											});
							// [118.24,29.08]
							String coordinates = root.get("coordinates")
									.toString();
							// 118.24,29.08
							// shape: 105.45871 35.016500
							String shape = coordinates.replace("[", "")
									.replace("]", "").replace(",", " ");

							System.out.println("space.getShape():"
									+ space.getShape());
							System.out.println("coordinates:" + coordinates);
							System.out.println("shape:" + shape);
							spTable.setShape(shape); // 用新增数据源中的设备空间位置信息

							int j = siteDataDao.addZjSpaceInfo(spTable); // 插入空间表

							if (j > 0) {

								sb.append("同步该数据源设备空间表数据到本地服务器成功!\n");
							} else {
								sb.append("同步该数据源设备空间表到本地服务器失败\n");
								rMap.put("code", "500");
							}
						} else {
							sb.append("该数据源空间表内不存在该设备的数据，同步空间表数据失败!\n");
							rMap.put("code", "500");
						}

						// 插入基本信息表
						if (infoList.size() > 0) {
							sb.append("该数据源内存在对象基本信息数据! 数据条数为:"
									+ infoList.size() + "\n");
							for (AttachInfoStore info : infoList) {
								info.setObjid(newObjid + "");
								System.out.println("info.getobjid:"
										+ info.getObjid());
							}
							int k = siteDataDao.addCpAttachInfoStore(infoList); // 同步新数据源中的设备基本信息到本地服务器中

							if (k > 0) {// 同步设备对象基本信息成功
								sb.append("同步该数据源设备对象基本信息数据到本地服务器成功!\n");

							} else {
								sb.append("同步该数据源设备对象基本信息数据到本地服务器失败!\n");
								rMap.put("code", "500");
							}
						} else {
							sb.append("该数据源内未查询到对象基本信息数据! 同步数据失败\n");
							rMap.put("code", "500");
						}

						// 插入对象参数关联表
						if (conList.size() > 0) {
							sb.append("该数据源内存在对象参数关联数据! 数据条数为:"
									+ infoList.size() + "\n");
							for (ConnObjParam param : conList) {
								param.setObjid(newObjid + ""); // 插入本地数据库新增的objid
								param.setDataguid("12_" + newObjid + "_"
										+ param.getDevicenumber() + "_"
										+ param.getParamid());
							}
							int z = siteDataDao.addConnObjParam(conList);

							if (z > 0) {// 同步对象参数关联信息成功
								sb.append("同步该数据源设备对象参数关联信息数据到本地服务器成功!\n");
							} else {// 同步对象参数关联信息失败
								sb.append("同步该数据源设备对象参数关联信息数据到本地服务器失败!\n");
								rMap.put("code", "500");
							}
						} else {
							sb.append("该数据源内未查询到对象参数关联数据! 同步数据失败\n");
							rMap.put("code", "500");
						}

						// 新建监测数据表（cp_collect_actual_objid）分表
						map.put("objid", newObjid);
						try {
							siteDataDao.creatCollTable(map);
							// int a = 5/0;
							sb.append("新建实时数据检测表成功!\n");
						} catch (Exception e) {
							// e.printStackTrace();
							sb.append("新建实时数据检测表失败!\n");
							rMap.put("message", sb.toString());
							// throw new Exception(sb.toString()); //直接退出
							return rMap;
						}

					} else {// 插入设备到本地数据库失败
						rMap.put("code", "500");
						sb.append("将该数据源下的设备obj信息同步到本地服务器失败\n");
					}
				} else {
					rMap.put("code", "500");
					sb.append("该数据源下对象表中未找到该类型的设备数据，同步对象表失败\n");
				}

			} else {// 查询新增的数据源失败
				rMap.put("code", "500");
				sb.append("未查询到新增的数据源，同步相关数据失败\n");
			}

		} else {// 新增数据源失败
			rMap.put("code", "500");
			sb.append("新增数据源失败\n");
		}

		rMap.put("message", sb.toString());
		/*
		 * CpinfoObj obj = new CpinfoObj(); obj.setObjname("test");
		 * obj.setObjtypeid("12"); Integer objid = siteD.addObjTwo(obj);
		 * System.out.println("objid:"+objid);
		 * System.out.println("obj.getObjid:"+obj.getObjid());
		 */

		return rMap;
	}

	/**
	 * 添加其他数据源信息,同步其他数据源站点相关信息 多数据源下配上事务不能切换数据源问题
	 * 
	 * @throws Exception
	 */
	// @Transactional(rollbackFor=Exception.class)
	public Map<String, Object> addOtherDb_two(DataSourceDo ds) throws Exception {
		Map<String, Object> rMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		rMap.put("code", "200");
		StringBuilder sb = new StringBuilder(); // 拼接返回的结果信息

		/*
		 * try { //Properties properties = loadPropertiesFile("db.properties");
		 * Properties pro = new Properties(); InputStream ins =
		 * SiteDataDaoImpl.class.getClassLoader().getResourceAsStream(
		 * "wy/qingdao_atmosphere/druid.properties"); pro.load(ins);
		 * 
		 * DruidDataSource druidDataSource = (DruidDataSource)
		 * DruidDataSourceFactory.createDataSource(pro); // DruidDataSrouce工厂模式
		 * // TODO 调试配置，用完删除 druidDataSource.setRemoveAbandoned(true);
		 * druidDataSource.setRemoveAbandonedTimeout(600);
		 * druidDataSource.setLogAbandoned(true); //
		 * druidDataSource.setBreakAfterAcquireFailure(true);
		 * druidDataSource.setTimeBetweenConnectErrorMillis(60000); //
		 * druidDataSource.setConnectionErrorRetryAttempts(0);
		 * 
		 * } catch (Exception e) { System.out.println("出错了"); return null; }
		 */

		ds.setDataSourceName(System.currentTimeMillis() + "_"
				+ (int) (Math.random() * 1000));// 防止数据源重名

		/*
		 * Integer dbid = siteDataDao.addOtherDb(ds); //新增数据源
		 * if(dbid>0){//新增数据源成功 sb.append("新增数据源成功!\n");
		 * System.out.println("dbid:"+dbid);
		 * System.out.println("ds.objid:"+ds.getId());//新增数据源后返回的id
		 * //查询新的数据源信息,并切换数据源，查询需要的该数据源的站点的相关信息 map.put("id",ds.getId());
		 * List<DataSourceDo> list = siteDataDao.queryOtherDataSource(map);
		 * System.out.println("dslist:"+list); if(list.size()>0){ //查询新增的数据源成功
		 * sb.append("查询到了新增的数据源!\n");
		 * 
		 * DataSourceDo newDs = list.get(0); //刚刚新增的数据源
		 */
		// 切换到新增的数据源
		DataSourceBean dataSourceBean = new DataSourceBean(
				new DataSourceBeanBuilder(ds.getDataSourceName(),
						ds.getDatabaseIp(), ds.getDatabasePort(),
						ds.getDatabaseName(), ds.getDatabaseUsername(),
						ds.getPassword()));

		// 辐射计相关变量
		List<AttachInfoStore> fsjInfoList;
		List<ConnObjParam> fsjConList;

		SpaceTable fsjSpace;
		CpinfoObj fsjObj;

		// 风廓线相关变量
		List<AttachInfoStore> fkxInfoList;
		List<ConnObjParam> fkxConList;

		SpaceTable fkxSpace;
		CpinfoObj fkxObj;

		try {
			DataSourceContext.setDataSource(dataSourceBean);
			System.out.println("切换数据源到：" + ds.getDataSourceName());
			System.out.println("datasourceBean:" + dataSourceBean);
			// int i = 5/0;

			// 查询该站点时属于平台端还是设备端
			Map<String, Object> platform = siteDataDao.queryPlatform();
			// 如果是平台端,就提示不能添加，并结束方法
			if (platform != null
					&& "1".equals(platform.get("isplatform").toString())) { // 1-平台端
																			// 2-设备端
				sb.append("该数据源为平台端，不能添加此数据源\n");
				rMap.put("message", sb.toString());
				rMap.put("code", 500);
				System.out.println("出现exception了:" + sb.toString());
				return rMap;
			}

			// 辐射计的相关信息
			map.put("objtypeid", 12); // 12-辐射计 28风廓线(非平台的数据源里面最多只有一个辐射计和风廓线设备)
			// System.out.println("111111");
			fsjObj = siteDataDao.selectOtherDbobjByOtp(map); // 通过objtypeid查找新数据源中的设备对象信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的)
			// System.out.println("2222222");
			fsjInfoList = siteDataDao.selectOtherDbAttachInfoByOtp(map); // 通过objtypeid查找新数据源中的设备基本信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的）
			fsjConList = siteDataDao.selectOtherDbConnObjParamByOtp(map); // 通过objtypeid查找对象和参数的关联信息
			map.put("spaceTable", "space_wbstation"); // 辐射计的空间表 ，风廓线的为
														// space_windprofile
			fsjSpace = siteDataDao.selectOtherDbSpaceByOtp(map); // 通过objtypeid查找新数据源中的设备空间信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的）

			map.clear();

			// 风廓线的相关信息
			map.put("objtypeid", 28); // 12-辐射计 28-风廓线(非平台的数据源里面最多只有一个辐射计和风廓线设备)
			// System.out.println("111111");
			fkxObj = siteDataDao.selectOtherDbobjByOtp(map); // 通过objtypeid查找新数据源中的设备对象信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的)
			// System.out.println("2222222");
			fkxInfoList = siteDataDao.selectOtherDbAttachInfoByOtp(map); // 通过objtypeid查找新数据源中的设备基本信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的）
			fkxConList = siteDataDao.selectOtherDbConnObjParamByOtp(map); // 通过objtypeid查找对象和参数的关联信息
			map.put("spaceTable", "space_windprofile"); // 风廓线的空间表 ，辐射计的为
														// space_wbstation
			fkxSpace = siteDataDao.selectOtherDbSpaceByOtp(map); // 通过objtypeid查找新数据源中的设备空间信息,非平台类型规定只有一台设备（要添加到此台服务器本地数据库的）

			sb.append("尝试切换到新增的数据源成功!\n");

		} catch (Exception e) {
			// e.printStackTrace();
			sb.append("尝试切换到新增的数据源失败!，请检查新增的数据源信息是否正确有效\n");
			rMap.put("message", sb.toString());
			System.out.println("出现exception了:" + e.getMessage());
			DataSourceContext.toDefault(); // 切回到默认数据库
			Logger.getLogger("").error("出现exception了:切回到默认数据库" );
			throw new Exception(sb.toString()); // 直接退出
		} catch (Error e) {
			// e.printStackTrace();
			sb.append("尝试切换到新增的数据源失败!，请检查新增的数据源信息是否正确有效\n");
			rMap.put("message", sb.toString());
			
			Logger.getLogger("").error("出现error了:" + e.getMessage());
			throw new Exception(sb.toString()); // 直接退出
		}

		System.out.println("-------");
		DataSourceContext.toDefault(); // 切回到默认数据库
		System.out.println("切回到默认数据库-----------------");
		System.out.println("fsjobj:" + fsjObj);
		System.out.println("fsjinfo:" + fsjInfoList);
		System.out.println("fsjspace:" + fsjSpace);
		System.out.println("fkxobj:" + fkxObj);
		System.out.println("fkxinfo:" + fkxInfoList);
		System.out.println("fkxspace:" + fkxSpace);
		// 能走到这里已经说明新增的数据源能够连接
		// 将新增数据源中的相关信息同步到本地数据库来，开了事务，如果报错了会回滚并返回报错信息
		rMap = tbDsData(ds, fsjObj, fsjInfoList, fsjConList, fsjSpace, rMap,
				sb, fkxObj, fkxInfoList, fkxConList, fkxSpace);

		/*
		 * }else{//查询新增的数据源失败 rMap.put("code", "500");
		 * sb.append("未查询到新增的数据源，同步相关数据失败\n"); }
		 * 
		 * }else{//新增数据源失败 rMap.put("code", "500"); sb.append("新增数据源失败\n"); }
		 */

		/*
		 * CpinfoObj obj = new CpinfoObj(); obj.setObjname("test");
		 * obj.setObjtypeid("12"); Integer objid = siteD.addObjTwo(obj);
		 * System.out.println("objid:"+objid);
		 * System.out.println("obj.getObjid:"+obj.getObjid());
		 */

		return rMap;
	}

	/**
	 * 
	 * @param ds
	 *            新增的数据源信息
	 * @param fsjObj
	 *            新增的数据源中的obj表中的对象信息 (辐射计)
	 * @param fsjInfoList
	 *            新增的数据源中的对象的基本信息（辐射计）
	 * @param fsJconList
	 *            新增的数据源中的对象参数的信息（辐射计）
	 * @param fsJSpace
	 *            新增的数据源中空间表的信息（辐射计）
	 * @param rMap
	 *            返回的包装提示信息的map
	 * @param sb
	 *            返回的提示信息
	 * @param fkxObj
	 *            新增的数据源中的obj表中的对象信息 (风廓线)
	 * @param fkxInfoList
	 *            新增的数据源中的对象的基本信息（风廓线）
	 * @param fkxConList
	 *            新增的数据源中的对象参数的信息（风廓线）
	 * @param fkxSpace
	 *            新增的数据源中空间表的信息（辐射计）
	 * @return
	 * @throws Exception
	 */
	// @Transactional(rollbackFor=Exception.class)
	public Map<String, Object> tbDsData(DataSourceDo ds, CpinfoObj fsjObj,
			List<AttachInfoStore> fsjInfoList, List<ConnObjParam> fsjConList,
			SpaceTable fsjSpace, Map<String, Object> rMap, StringBuilder sb,
			CpinfoObj fkxObj, List<AttachInfoStore> fkxInfoList,
			List<ConnObjParam> fkxConList, SpaceTable fkxSpace)
			throws Exception {

		/*
		 * ClassPathXmlApplicationContext contextLoader = new
		 * ClassPathXmlApplicationContext( new String[] {
		 * "classpath*:/wy/qingdao_atmosphere/mybatis.xml",
		 * "classpath*:WEB-INF/qingdao_atmosphere-servlet.xml"});
		 * 
		 * //1.获取事务控制管理器 DataSourceTransactionManager transactionManager =
		 * contextLoader.getBean( "transactionManager",
		 * DataSourceTransactionManager.class); //2.获取事务定义
		 * DefaultTransactionDefinition def = new
		 * DefaultTransactionDefinition(); //3.设置事务隔离级别，开启新事务
		 * def.setPropagationBehavior
		 * (TransactionDefinition.PROPAGATION_REQUIRES_NEW); //4.获得事务状态
		 * TransactionStatus transactionStatus =
		 * transactionManager.getTransaction(def); //MessageTpl messageTpl =
		 * null;
		 */

		Map<String, Object> map = new HashMap<String, Object>();
		Integer fsjNewObjid = 0; // 回滚操作用
		Integer fkxNewObjid = 0; // 回滚操作用
		Integer dsId = 0; // 回滚本次添加的数据源操作用
		Integer fsjDirid = -1; // 回滚本次添加的辐射计市级目录（3级目录）操作用
		Integer fkxDirid = -1; // 回滚本次添加的风廓线市级目录（3级目录）操作用
		Integer webId = -1; // 回滚本次添加的web服务器信息用
		try {// 整个大的逻辑，如果出错了直接退出并返回报错信息
			Integer dbid = siteDataDao.addOtherDb(ds); // 新增数据源
			if (dbid > 0) {// 新增数据源成功
				dsId = ds.getId(); //
				sb.append("新增数据源成功!\n");
				System.out.println("dbid:" + dbid);
				System.out.println("ds.objid:" + ds.getId());// 新增数据源后返回的id
				// 查询新的数据源信息,并切换数据源，查询需要的该数据源的站点的相关信息
				map.put("id", ds.getId());
				List<DataSourceDo> list = siteDataDao.queryOtherDataSource(map);
				System.out.println("dslist:" + list);
				if (list.size() > 0) { // 查询新增的数据源成功
					sb.append("查询到了新增的数据源!\n");

					DataSourceDo newDs = list.get(0); // 刚刚新增的数据源

					// 辐射计站点相关信息同步

					if (fsjObj != null) { // 该新数据源下有设备（辐射计或风廓线）
						sb.append("该数据源下找到了辐射计类型的设备!\n");
						// 将该obj(设备)插入到此台服务器的数据库中
						CpinfoObj insertObj = new CpinfoObj(); // 要新增到本地服务器的设备
						insertObj.setObjname(fsjObj.getObjname());
						insertObj.setObjtypeid(fsjObj.getObjtypeid());
						insertObj.setObjnum(fsjObj.getObjnum());
						insertObj.setRemark(fsjObj.getRemark());
						insertObj.setIsused(fsjObj.getIsused());
						Integer status = siteDataDao.addObjTwo(insertObj); // 将新数据源下的设备插入到本地数据库中成功后返回新增的objid
						if (status > 0) {// 插入设备到本地数据库成功
							sb.append("将该数据源下的辐射计设备obj信息同步到本地服务器成功!\n");
							fsjNewObjid = insertObj.getObjid(); // 新增的本地数据库的
																// 设备objid
							Integer oldObjid = fsjObj.getObjid();// 新增的数据源中的原objid
							System.out.println("newObjid:" + fsjNewObjid);
							System.out.println("oldObjid:" + oldObjid);
							// 添加Web服务器信息
							WebServer web = new WebServer();
							web.setName(fsjObj.getObjname());
							web.setObjid(fsjNewObjid);
							web.setPort(8080);
							web.setUrl("/zjdq_sb");
							int n = siteDataDao.addWebServer(web);
							webId = web.getId();
							if (n > 0) { // 插入objid关联表成功
								// 插入objid关联表成功
								sb.append("新增Web服务器信息成功!\n");
							} else {
								rMap.put("code", "500");
								sb.append("新增Web服务器信息失败\n");
							}
							map.clear();
							map.put("objid", fsjNewObjid);
							map.put("otherobjid", oldObjid);
							map.put("dsname", newDs.getDataSourceName());
							map.put("dsid", newDs.getId());
							map.put("objtypeid", 12);
							System.out.println("dsid:" + newDs.getId());
							System.out.println("dsname:"
									+ newDs.getDataSourceName());
							int i = siteDataDao.addObjIdConnect(map);
							if (i > 0) { // 插入objid关联表成功
								// 插入objid关联表成功
								sb.append("建立该数据源辐射计设备与本地数据库的objid关联成功!\n");
							} else {
								rMap.put("code", "500");
								sb.append("建立该数据源与本地数据库的objid关联失败\n");
							}

							// 插入空间表
							if (fsjSpace != null) { //
								sb.append("该数据源空间表内存在该辐射计设备的数据!\n");
								SpaceTable spTable = new SpaceTable();
								spTable.setTablename("space_wbstation");
								spTable.setObjid(fsjNewObjid); // 改成本地服务器中新增的objid
								spTable.setId(fsjNewObjid); // 改成本地服务器中新增的objid

								// space.getShape()
								// {"type":"Point","coordinates":[118.24,29.08]}
								LinkedHashMap<String, Object> root = JSON
										.parseObject(
												fsjSpace.getShape(),
												new TypeReference<LinkedHashMap<String, Object>>() {
												});
								// [118.24,29.08]
								String coordinates = root.get("coordinates")
										.toString();
								// 118.24,29.08
								// shape: 105.45871 35.016500
								String shape = coordinates.replace("[", "")
										.replace("]", "").replace(",", " ");

								System.out.println("space.getShape():"
										+ fsjSpace.getShape());
								System.out
										.println("coordinates:" + coordinates);
								System.out.println("shape:" + shape);
								spTable.setShape(shape); // 用新增数据源中的设备空间位置信息

								int j = siteDataDao.addZjSpaceInfo(spTable); // 插入空间表

								if (j > 0) {

									sb.append("同步该数据源设备空间表数据到本地服务器成功!\n");
								} else {
									sb.append("同步该数据源设备空间表到本地服务器失败\n");
									rMap.put("code", "500");
								}
							} else {
								sb.append("该数据源空间表内不存在该设备的数据，同步空间表数据失败!\n");
								rMap.put("code", "500");
							}

							// 插入基本信息表
							if (fsjInfoList.size() > 0) {
								sb.append("该数据源内存在辐射计对象基本信息数据! 数据条数为:"
										+ fsjInfoList.size() + "\n");
								for (AttachInfoStore info : fsjInfoList) {
									info.setObjid(fsjNewObjid + "");
									System.out.println("info.getobjid:"
											+ info.getObjid());
								}
								int k = siteDataDao
										.addCpAttachInfoStore(fsjInfoList); // 同步新数据源中的设备基本信息到本地服务器中

								if (k > 0) {// 同步设备对象基本信息成功
									sb.append("同步该数据源辐射计设备对象基本信息数据到本地服务器成功!\n");

								} else {
									sb.append("同步该数据源辐射计设备对象基本信息数据到本地服务器失败!\n");
									rMap.put("code", "500");
								}

								//
								for (AttachInfoStore info : fsjInfoList) {
									if ("12010004".equals(info.getFieldid())) { // 辐射计的城市字段
										String city = info.getFieldvalue();
										Map<String, String> pMap = new HashMap<String, String>();
										pMap.put("dirname", city);
										pMap.put("higherlevelid", "101");// 101-辐射计
																			// 201-风廓线
										pMap.put("fieldid", "12010004");
										List<Map<String, Object>> dirList = siteDataDao
												.findDirBycity(pMap);
										if (dirList == null
												|| !(dirList.size() > 0)) { // 没有这个市级，需要添加，不然站点无法在网页上显示出来
											// 添加辐射计市级目录
											CpDir dir = new CpDir();
											dir.setDirname(city);
											dir.setFieldid("12010004");
											dir.setHigherlevelid("101"); // 101-辐射计
																			// 201-风廓线

											int z = siteDataDao
													.addThirdDir(dir);// 添加市级三级目录

											fsjDirid = dir.getId() == null
													|| "".equals(dir.getId()) ? -1
													: dir.getId();

											System.out.println("sjdir.getid:"
													+ dir.getId());
											System.out.println("fsjDirId:"
													+ fsjDirid);

											// int z=
											// siteDataDao.addThirdDir(pMap);//添加市级三级目录
											Logger.getLogger("").info(
													"辐射计-101没有" + city
															+ "这个3级目录");
											if (z > 0) {
												Logger.getLogger("").info(
														"辐射计-101添加" + city
																+ "这个3级目录成功");
											} else {
												Logger.getLogger("").info(
														"辐射计-101添加" + city
																+ "这个3级目录失败");
											}

											// 添加融合图3级目录
											CpDir dir2 = new CpDir();
											dir2.setDirname(city);
											dir2.setFieldid("12010004");
											dir2.setHigherlevelid("301"); // 101-辐射计
																			// 201-风廓线
																			// 301-融合图

											int h = siteDataDao
													.addThirdDir(dir2);// 添加融合图市级三级目录
											if (h > 0) {
												Logger.getLogger("").info(
														"融合图-301添加" + city
																+ "这个3级目录成功");
											} else {
												Logger.getLogger("").info(
														"融合图-301添加" + city
																+ "这个3级目录失败");
											}

										} else {
											Logger.getLogger("").info(
													"辐射计-101有" + city
															+ "这个3级目录，不需要进行操作");
										}

									}
								}

							} else {
								sb.append("该数据源内未查询到辐射计设备对象基本信息数据! 同步数据失败\n");
								rMap.put("code", "500");
							}

							// 插入对象参数关联表
							if (fsjConList.size() > 0) {
								sb.append("该数据源内存在辐射计设备对象参数关联数据! 数据条数为:"
										+ fsjConList.size() + "\n");
								for (ConnObjParam param : fsjConList) {
									param.setObjid(fsjNewObjid + ""); // 插入本地数据库新增的objid
									param.setDataguid("12_" + fsjNewObjid + "_"
											+ param.getDevicenumber() + "_"
											+ param.getParamid());
								}
								int z = siteDataDao.addConnObjParam(fsjConList);

								if (z > 0) {// 同步对象参数关联信息成功
									sb.append("同步该数据源辐射计设备对象参数关联信息数据到本地服务器成功!\n");
								} else {// 同步对象参数关联信息失败
									sb.append("同步该数据源辐射计设备对象参数关联信息数据到本地服务器失败!\n");
									rMap.put("code", "500");
								}
							} else {
								sb.append("该数据源内未查询到辐射计设备的对象参数关联数据! 同步数据失败\n");
								rMap.put("code", "500");
							}

							// 新建监测数据表（cp_collect_actual_objid）分表
							map.put("objid", fsjNewObjid);
							try {
								siteDataDao.creatCollTable(map);
								// int a = 5/0;
								sb.append("新建辐射计实时数据检测表成功!\n");
							} catch (Exception e) {
								// e.printStackTrace();
								sb.append("新建辐射计实时数据检测表失败!\n");
								rMap.put("message", sb.toString());
								throw new Exception(sb.toString()); // 直接退出
								// return rMap;
							}

						} else {// 插入设备到本地数据库失败
							rMap.put("code", "500");
							sb.append("将该数据源下的辐射计设备obj信息同步到本地服务器失败\n");
						}
					} else {
						rMap.put("code", "500");
						sb.append("该数据源下对象表中未找到辐射计类型的设备数据，同步对象表失败\n");
					}

					// 风廓线站点相关信息同步

					if (fkxObj != null) { // 该新数据源下有设备（辐射计或风廓线）
						sb.append("该数据源下找到了风廓线类型的设备!\n");
						// 将该obj(设备)插入到此台服务器的数据库中
						CpinfoObj insertObj = new CpinfoObj(); // 要新增到本地服务器的设备
						insertObj.setObjname(fkxObj.getObjname());
						insertObj.setObjtypeid(fkxObj.getObjtypeid());
						insertObj.setObjnum(fkxObj.getObjnum());
						insertObj.setRemark(fkxObj.getRemark());
						insertObj.setIsused(fkxObj.getIsused());
						Integer status = siteDataDao.addObjTwo(insertObj); // 将新数据源下的设备插入到本地数据库中成功后返回新增的objid
						if (status > 0) {// 插入设备到本地数据库成功
							sb.append("将该数据源下的风廓线设备obj信息同步到本地服务器成功!\n");
							fkxNewObjid = insertObj.getObjid(); // 新增的本地数据库的
																// 设备objid
							Integer oldObjid = fkxObj.getObjid();// 新增的数据源中的原objid
							System.out.println("fkxnewObjid:" + fkxNewObjid);
							System.out.println("oldObjid:" + oldObjid);
							map.clear();
							map.put("objid", fkxNewObjid);
							map.put("otherobjid", oldObjid);
							map.put("dsname", newDs.getDataSourceName());
							map.put("dsid", newDs.getId());
							map.put("objtypeid", 28);
							System.out.println("dsid:" + newDs.getId());
							System.out.println("dsname:"
									+ newDs.getDataSourceName());
							int i = siteDataDao.addObjIdConnect(map);
							if (i > 0) { // 插入objid关联表成功
								// 插入objid关联表成功
								sb.append("建立该数据源风廓线设备与本地数据库的objid关联成功!\n");
							} else {
								rMap.put("code", "500");
								sb.append("建立该数据源风廓线设备与本地数据库的objid关联失败\n");
							}

							// 插入空间表
							if (fkxSpace != null) { //
								sb.append("该数据源空间表内存在风廓线设备的数据!\n");
								SpaceTable spTable = new SpaceTable();
								spTable.setTablename("space_windprofile");
								spTable.setObjid(fkxNewObjid); // 改成本地服务器中新增的objid
								spTable.setId(fkxNewObjid); // 改成本地服务器中新增的objid

								// space.getShape()
								// {"type":"Point","coordinates":[118.24,29.08]}
								LinkedHashMap<String, Object> root = JSON
										.parseObject(
												fkxSpace.getShape(),
												new TypeReference<LinkedHashMap<String, Object>>() {
												});
								// [118.24,29.08]
								String coordinates = root.get("coordinates")
										.toString();
								// 118.24,29.08
								// shape: 105.45871 35.016500
								String shape = coordinates.replace("[", "")
										.replace("]", "").replace(",", " ");

								System.out.println("fkxSpace.getShape():"
										+ fkxSpace.getShape());
								System.out
										.println("coordinates:" + coordinates);
								System.out.println("shape:" + shape);
								spTable.setShape(shape); // 用新增数据源中的设备空间位置信息

								int j = siteDataDao.addZjSpaceInfo(spTable); // 插入空间表

								if (j > 0) {

									sb.append("同步该数据源风廓线设备空间表数据到本地服务器成功!\n");
								} else {
									sb.append("同步该数据源风廓线设备空间表到本地服务器失败\n");
									rMap.put("code", "500");
								}
							} else {
								sb.append("该数据源空间表内不存在该设备的数据，同步空间表数据失败!\n");
								rMap.put("code", "500");
							}

							// 插入基本信息表
							if (fkxInfoList.size() > 0) {
								sb.append("该数据源内存在风廓线对象基本信息数据! 数据条数为:"
										+ fkxInfoList.size() + "\n");
								for (AttachInfoStore info : fkxInfoList) {
									info.setObjid(fkxNewObjid + "");
									System.out.println("info.getobjid:"
											+ info.getObjid());
								}
								int k = siteDataDao
										.addCpAttachInfoStore(fkxInfoList); // 同步新数据源中的设备基本信息到本地服务器中

								if (k > 0) {// 同步设备对象基本信息成功
									sb.append("同步该数据源风廓线设备对象基本信息数据到本地服务器成功!\n");

								} else {
									sb.append("同步该数据源风廓线设备对象基本信息数据到本地服务器失败!\n");
									rMap.put("code", "500");
								}

								//
								for (AttachInfoStore info : fkxInfoList) {
									if ("28010009".equals(info.getFieldid())) { // 风廓线的城市字段
										String city = info.getFieldvalue();

										Map<String, String> pMap = new HashMap<String, String>();
										System.out.println("city:" + city);
										pMap.put("dirname", city);
										pMap.put("higherlevelid", "201");// 101-辐射计
																			// 201-风廓线
										pMap.put("fieldid", "28010009");
										List<Map<String, Object>> dirList = siteDataDao
												.findDirBycity(pMap);

										if (dirList == null
												|| !(dirList.size() > 0)) { // 没有这个市级，需要添加，不然站点无法在网页上显示出来
											CpDir dir = new CpDir();
											dir.setDirname(city);
											dir.setFieldid("28010009");
											dir.setHigherlevelid("201"); // 101-辐射计
																			// 201-风廓线

											int z = siteDataDao
													.addThirdDir(dir);// 添加市级三级目录

											fkxDirid = dir.getId() == null
													|| "".equals(dir.getId()) ? -1
													: dir.getId();

											System.out.println("fkxdir.getid:"
													+ dir.getId());
											System.out.println("fkxDirId:"
													+ fkxDirid);

											Logger.getLogger("").info(
													"风廓线201没有" + city
															+ "这个3级目录");
											if (z > 0) {
												Logger.getLogger("").info(
														"风廓线201添加" + city
																+ "这个3级目录成功");
											} else {
												Logger.getLogger("").info(
														"风廓线201添加" + city
																+ "这个3级目录失败");
											}
										} else {
											Logger.getLogger("").info(
													"风廓线201有" + city
															+ "这个3级目录，不需要进行操作");
										}

									}
								}

							} else {
								sb.append("该数据源内未查询到风廓线对象基本信息数据! 同步数据失败\n");
								rMap.put("code", "500");
							}

							// 插入对象参数关联表
							if (fkxConList.size() > 0) {
								sb.append("该数据源内存在风廓线对象参数关联数据! 数据条数为:"
										+ fkxConList.size() + "\n");
								for (ConnObjParam param : fkxConList) {
									param.setObjid(fkxNewObjid + ""); // 插入本地数据库新增的objid
									param.setDataguid("28_" + fkxNewObjid + "_"
											+ param.getDevicenumber() + "_"
											+ param.getParamid());
								}
								int z = siteDataDao.addConnObjParam(fkxConList);

								if (z > 0) {// 同步对象参数关联信息成功
									sb.append("同步该数据源风廓线设备对象参数关联信息数据到本地服务器成功!\n");
								} else {// 同步对象参数关联信息失败
									sb.append("同步该数据源风廓线设备对象参数关联信息数据到本地服务器失败!\n");
									rMap.put("code", "500");
								}
							} else {
								sb.append("该数据源内未查询到风廓线对象参数关联数据! 同步数据失败\n");
								rMap.put("code", "500");
							}

							// 新建监测数据表（cp_collect_actual_objid）分表
							map.put("objid", fkxNewObjid);
							try {
								siteDataDao.creatCollTable(map);
								// int a = 5/0;
								sb.append("新建实时数据检测表cp_collect_actual_"
										+ fkxNewObjid + "成功!\n");
							} catch (Exception e) {
								// e.printStackTrace();
								sb.append("新建风廓线设备实时数据检测表cp_collect_actual_"
										+ fkxNewObjid + "失败!\n");
								rMap.put("message", sb.toString());
								throw new Exception(sb.toString()); // 直接退出
								// return rMap;
							}

						} else {// 插入设备到本地数据库失败
							rMap.put("code", "500");
							sb.append("将该数据源下的风廓线设备obj信息同步到本地服务器失败\n");
						}
					} else {
						rMap.put("code", "500");
						sb.append("该数据源下对象表中未找到风廓线类型的设备数据，同步对象表失败\n");
					}

				} else {// 查询新增的数据源失败
					rMap.put("code", "500");
					sb.append("未查询到新增的数据源，同步相关数据失败\n");
				}

			} else {// 新增数据源失败
				rMap.put("code", "500");
				sb.append("新增数据源失败\n");
			}
			// int i=5/0;
			sb.append("新增数据源成功\n");

		} catch (Exception e) { // 整个大的逻辑，如果出错了手动删除前面添加的信息然后退出并返回报错信息
			// e.printStackTrace();
			System.out.println("出错了。。。");
			sb.append("未知错误" + e.getMessage() + "，开始回滚操作\n");
			sb.append("本次添加失败!\n");
			e.printStackTrace();
			// 手动模拟回滚操作
			// transactionManager.rollback(transactionStatus);
			int i = siteDataDao.delOtherDs(dsId); // 删除该条数据源记录
			System.out.println("出错回滚，删除了：" + i + "条数据源记录，id为：" + dsId);

			// 辐射计相关方法回退
			i = siteDataDao.deleteObj(fsjNewObjid); // 删除对象表
			System.out.println("出错回滚，删除了obj表：" + i + "条记录，辐射计objid为："
					+ fsjNewObjid);

			Map<String, Object> zMap = new HashMap<String, Object>();
			zMap.put("tablename", "space_wbstation");
			zMap.put("objid", fsjNewObjid);

			i = siteDataDao.deleteSpace(zMap); // 删除空间表记录

			System.out.println("出错回滚，删除了空间表：" + i + "条记录，辐射计objid为："
					+ fsjNewObjid);

			i = siteDataDao.deleteConnParam(fsjNewObjid); // 删除对象参数关联记录
			System.out.println("出错回滚，删除了对象参数关联表：" + i + "条记录，辐射计objid为："
					+ fsjNewObjid);

			i = siteDataDao.deleteInfoStore(fsjNewObjid); // 删除基本信息数据
			System.out.println("出错回滚，删除了基本信息表：" + i + "条记录，辐射计objid为："
					+ fsjNewObjid);

			i = siteDataDao.delConnDbOid(fsjNewObjid); // 删除数据库对象关联objid
			System.out.println("出错回滚，删除了数据库对象objid关联表：" + i + "条记录，辐射计objid为："
					+ fsjNewObjid);

			i = siteDataDao.delFsjDir(fsjDirid); // 删除数据库目录
			System.out.println("出错回滚，删除了数据库目录表：" + i + "条记录，辐设计目录id为："
					+ fsjDirid);

			siteDataDao.dropCollTable(fsjNewObjid); // 删除该监测数据表
			System.out.println("出错回滚，删除了数据库监测数据表表，表后缀为为：" + fsjNewObjid);

			WebServer web = new WebServer();
			web.setId(webId);
			System.out.println("webId:" + webId);
			i = siteDataDao.deleteWebServer(web);
			System.out.println("出错回滚，删除了Web服务器信息表：" + i + "条记录，记录id为：" + webId);

			// 风廓线相关方法回退
			i = siteDataDao.deleteObj(fkxNewObjid); // 删除对象表
			System.out.println("出错回滚，删除了obj表：" + i + "条记录，风廓线objid为："
					+ fkxNewObjid);

			// Map<String,Object> zMap = new HashMap<String,Object>();
			zMap.clear();
			zMap.put("tablename", "space_windprofile");
			zMap.put("objid", fkxNewObjid);

			i = siteDataDao.deleteSpace(zMap); // 删除空间表记录

			System.out.println("出错回滚，删除了空间表：" + i + "条记录，风廓线objid为："
					+ fkxNewObjid);

			i = siteDataDao.deleteConnParam(fkxNewObjid); // 删除对象参数关联记录
			System.out.println("出错回滚，删除了对象参数关联表：" + i + "条记录，风廓线objid为："
					+ fkxNewObjid);

			i = siteDataDao.deleteInfoStore(fkxNewObjid); // 删除基本信息数据
			System.out.println("出错回滚，删除了基本信息表：" + i + "条记录，风廓线objid为："
					+ fkxNewObjid);

			i = siteDataDao.delConnDbOid(fkxNewObjid); // 删除数据库对象关联objid
			System.out.println("出错回滚，删除了数据库对象objid关联表：" + i + "条记录，风廓线objid为："
					+ fkxNewObjid);

			i = siteDataDao.delFsjDir(fkxDirid); // 删除数据库目录
			System.out.println("出错回滚，删除了数据库目录表：" + i + "条记录，风廓线目录id为："
					+ fkxDirid);

			siteDataDao.dropCollTable(fkxNewObjid); // 删除该监测数据表
			throw new Exception(sb.toString()); // 直接退出

			// return rMap;
		}/*
		 * finally { transactionManager.commit(transactionStatus); }
		 */

		rMap.put("message", sb.toString());
		return rMap;

	}

	/**
	 * 查询该站点时属于平台端还是设备端
	 * 
	 * @return
	 */
	public Map<String, Object> queryPlatform() {
		return siteDataDao.queryPlatform();
	}

	public int delCollData_two(String collecttime, String objid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("collecttime", collecttime);
		map.put("objid", objid);
		return siteDataDao.delCollData_two(map);
	}

	public int addWebServer(WebServer web) {
		return siteDataDao.addWebServer(web);
	}

	public int addFsjServer(WebServer fsj) {
		return siteDataDao.addFsjServer(fsj);
	}

	public int updateWebServer(WebServer web) {
		return siteDataDao.updateWebServer(web);
	}

	public int updateFsjServer(WebServer fsj) {
		return siteDataDao.updateFsjServer(fsj);
	}

	public List<WebServer> selectFsjServer(WebServer fsj) {
		return siteDataDao.selectFsjServer(fsj);
	}

	/**
	 * 添加web服务器信息
	 * 
	 * @param web
	 * @return
	 */
	public List<WebServer> selectWebServer(WebServer web) {
		return siteDataDao.selectWebServer(web);
	}

	public List<DataSourceDo> selectOtherDb(DataSourceDo ds) {
		return siteDataDao.selectOtherDb(ds);
	}

	public int updateOtherDb(DataSourceDo ds) {
		return siteDataDao.updateOtherDb(ds);
	}

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> deleteOtherDb(Integer id) {
		Integer dsid = id == null || "".equals(id) ? 0 : id;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dsid", dsid);
		List<DbConnOid> list = siteDataDao.selectDbConnOid(map);
		String ids = "";
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					ids = ids + list.get(i).getObjid();
					System.out.println("list.getobjid:"
							+ list.get(i).getObjid());
				} else {
					ids = ids + "," + list.get(i).getObjid();
					System.out.println("list.getobjid:"
							+ list.get(i).getObjid());
				}
			}
			System.out.println("list:" + list);
			System.out.println("ids:" + ids);

			if ("".equals(ids)) {// 为空，默认设为0，防止数据库报错
				ids = "0";
			}
			// 删除其他数据源
			int r = siteDataDao.delOtherDs(dsid);

			if (r > 0) {
				System.out.println("删除其他数据源记录：" + dsid + "成功");
			} else {
				System.out.println("删除其他数据源记录：" + dsid + "失败");
			}

			// 取辐射计或风廓线其中一个，查出它的市级信息即可，由此决定需不要删除市级目录（如果除了该站点外没有设备属于该市了，则删除，否则不删除）
			Integer objid = list.get(0).getObjid();
			map.put("objid", objid);
			System.out.println("objid:" + objid);
			// 通过objid查出设备基本信息，查出是哪个市的
			List<AttachInfoStore> infoList = siteDataDao.selectAttachInfo(map);
			System.out.println("infolist:" + infoList);
			if (infoList.size() > 0) {
				for (AttachInfoStore info : infoList) { // 12010004-辐射计
														// 28010009-风廓线
					if ("12010004".equals(info.getFieldid())
							|| "28010009".equals(info.getFieldid())) { // 因为不确定该设备是辐射计还是风廓线
						String cityName = info.getFieldvalue();
						System.out.println("删除的数据源中的obj所属cityName:" + cityName);
						map.clear();
						map.put("objid", objid);
						map.put("cityName", cityName);
						// 通过objid和cityName查询是否还存在该市的设备，存在则不删该市级目录，不存在则删了
						List<Map<String, Object>> dataList = siteDataDao
								.isHasOtherDeviceByCity(map);
						if (dataList.size() == 0) { // 该市级目录下不存在其他设备
							System.out.println(cityName + "下不存在其他设备,可以删除该市级目录");
							// 正常来讲，辐射计或风廓线设备只要其中一种不存在，另一个也不存在，所以都删除
							map.clear();
							map.put("higherlevelid", "101"); // 101-辐射计 201-风廓线
							map.put("dirname", cityName);
							// 删除辐射计设备的该市级目录
							int i = siteDataDao.deleteDirBycity(map);

							if (i > 0) {
								System.out.println("删除辐射计" + cityName + "成功");
							} else {
								System.out.println("删除辐射计" + cityName + "失败");
							}

							map.clear();
							map.put("higherlevelid", "201"); // 101-辐射计 201-风廓线
							map.put("dirname", cityName);
							// 删除风廓线设备的该市级目录
							int k = siteDataDao.deleteDirBycity(map);

							if (k > 0) {
								System.out.println("删除风廓线" + cityName + "成功");
							} else {
								System.out.println("删除风廓线" + cityName + "失败");
							}
							
							map.clear();
							map.put("higherlevelid", "301"); // 101-辐射计 201-风廓线   301-融合图
							map.put("dirname", cityName);
							// 删除融合图的该市级目录
							 k = siteDataDao.deleteDirBycity(map);

							if (k > 0) {
								System.out.println("删除融合图" + cityName + "成功");
							} else {
								System.out.println("删除融合图" + cityName + "失败");
							}

						} else { // 该市级目录下存在其他设备
							System.out.println(cityName + "存在其他设备,不删除该市级目录");
							System.out.println(cityName + "下其他的设备：" + dataList);
						}

					}
				}
			}

			// 删除其他数据源的一些列相关信息(obj,objconnectparam,space,infostore,dbconnObjid,webServer)
			siteDataDao.deleteOtherDb(ids); // 通过objid删除相关信息

			map.put("message", "删除其他数据源信息成功");
			map.put("code", 200);
			
			
			//删除辐射计，风廓线实时数据检测表
			String[] objids = ids.split(",");
			
			for(String oid:objids){
				siteDataDao.dropCollTable(Integer.parseInt(oid));
				Logger.getLogger("").info("删除了后缀为："+oid+"的实时数据检测表");
			}
			

		} else {
			map.clear();
			map.put("message", "没有该数据源关联的对象objid,不存在或已删除");
			map.put("code", 200);
		}

		return map;

	}

	public int deleteFsjServer(WebServer fsj) {
		// TODO Auto-generated method stub
		return siteDataDao.deleteFsjServer(fsj);
	}

	public int deleteWebServer(WebServer web) {
		// TODO Auto-generated method stub
		return siteDataDao.deleteWebServer(web);
	}

	public List<Map<String, Object>> selectMapParam(Map<String, Object> map) {
		
		return siteDataDao.selectMapParam(map);
	}

	public int insertMapParam(Map<String, Object> map) {
		return siteDataDao.insertMapParam(map);
	}

	public int updateMapParam(Map<String, Object> map) {
		return siteDataDao.updateMapParam(map);
	}

	/*
	 * private void Init() { try { //Properties properties =
	 * loadPropertiesFile("db.properties"); Properties pro = new Properties();
	 * InputStream ins =
	 * SiteDataDaoImpl.class.getClassLoader().getResourceAsStream
	 * ("druid.properties"); pro.load(ins);
	 * 
	 * DruidDataSource druidDataSource = (DruidDataSource)
	 * DruidDataSourceFactory.createDataSource(pro); // DruidDataSrouce工厂模式 //
	 * TODO 调试配置，用完删除 druidDataSource.setRemoveAbandoned(true);
	 * druidDataSource.setRemoveAbandonedTimeout(600);
	 * druidDataSource.setLogAbandoned(true); //
	 * druidDataSource.setBreakAfterAcquireFailure(true);
	 * druidDataSource.setTimeBetweenConnectErrorMillis(60000); //
	 * druidDataSource.setConnectionErrorRetryAttempts(0);
	 * 
	 * } catch (Exception e) { System.out.println("出错了"); }
	 * 
	 * }
	 */
}
