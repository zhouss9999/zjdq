package wy.qingdao_atmosphere.countrysitedata.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;

import wy.qingdao_atmosphere.countrysitedata.domain.FsjFtpParam;
import wy.qingdao_atmosphere.countrysitedata.domain.TemperatureParam;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;
import wy.util.CalCZ;
import wy.util.CalDJR;
import wy.util.CalFH;
import wy.util.FsjFtpUtilsgz;
import wy.util.datapersistence.ModelAssist;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.util.datapersistence.service.BaseService;


@Service("WeiBoService")
public class WeiBoService {
	
	
	@Autowired
	private SiteDataDao  siteDao;
	
	@Autowired
	private BaseaddDao baseaddDao;
	
	@Autowired
	private BaseService baseService;
	
	@Autowired
	private SiteDataService  siteService;
	
	/**
	 * 从数据库获取历史（最近12个小时）的微波辐射计数据
	 * @return
	 */
	public Map<String,Object>  queryWeiBoLsdata(HttpServletRequest request,String paramid){
		
		        //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				Map<String,Object> map = new HashMap<String,Object>();
				//获取数据库辐射数据近12小时时间
				String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
				
				//按时间查询 格式：yyyy-MM-dd
				//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime") +" 00:00:00";
				//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime") +" 23:59:59";
				
				Map<String,Object> paramap = new HashMap<String,Object>();
				paramap.put("objid", objid);
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
				
				//String qzbh = siteDao.queryQzbhByobjid(paramap);//根据objid查询区站编号
				//dataguid前缀
				String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
				
				
				//按时间查询 格式：yyyy-MM-dd
				String begintime = "";
				String endtime =  "";
				//是否有带时间查询（true则代表实时查询）
				boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))
						          ||request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"));
				if(isexist){//实时查询
					 begintime =  colMinMaxTime[0];
					 endtime   = colMinMaxTime[1] ;
		            
				}else{//带时间参数的查询
					
					endtime = request.getParameter("endtime");
					//获取12小时之内的数据
			    	
					
					begintime= request.getParameter("begintime");
					
				}
				
				map.put("begintime", begintime);
				map.put("endtime", endtime);
				map.put("objid", objid);
				map.put("dataguid", dataguid);
		        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> dataList = siteDao.queryWeiBoDate(map); //从数据库获取历史（最近12个小时）的微波辐射计数据
				Map<String,Object> kmap=new HashMap<String,Object>();
				if("2780".equals(objid)){
					 kmap = WeiBopackingDataLs(dataList);  //(丽水的，比较特殊)封装从数据库获取出来的监测数据并返回
				}else{
					// kmap = WeiBopackingData(dataList);  //封装从数据库获取出来的监测数据并返回	
					 kmap = WeiBopackingDataLs(dataList);  //72个小时的数据，数据量会很多，中间截一部分
				}
				//System.out.println("begintime:"+begintime);
				//System.out.println("endtime:"+endtime);
			       // 获取数据库辐射数据近12小时时间
			      
			 
				
				return  kmap;
	}
	
	
	


	/**
	 * 从数据库获取实时的（最近1个小时的最新一条）微波辐射计数据
	 * @return
	 */
   public Map<String,Object> queryWeiBoSsdata(HttpServletRequest request, String paramid) {
		
		//必传
		String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
		
		//获取数据库辐射数据近1小时时间
		//String[] colMinMaxTime = colMinMaxTimeByHour("5", 1, objid, paramid, "");
		//获取已有的最新时间戳
		// String zmaxTime = baseaddDao.getMaxTimeByDtid_two( "5", objid);
		Map<String,Object> paramap = new HashMap<String,Object>();
		paramap.put("objid", objid);
		Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
		//dataguid前缀
		String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
		zmap.put("dataguid", dataguid);
		zmap.put("objid", objid);
		String zmaxTime = siteDao.getFsjMaxTime(zmap);  //获得辐射计某站点最新时间
		//按时间查询 格式：yyyy-MM-dd HH:mm:ss
		//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime");
		//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime");
		String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? zmaxTime : request.getParameter("endtime");

		Map<String,Object> map = new HashMap<String,Object>();
		//map.put("begintime", begintime);
		map.put("endtime", endtime);
		map.put("objid", objid);
		map.put("dataguid", dataguid);
       
       // System.out.println("zmaxTime:"+zmaxTime);
       // System.out.println("endtime:"+endtime);
        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		//List<Map<String, Object>> dataList = siteDao.queryWeiBoDate(map); //从数据库获取实时的（最近1个小时）微波辐射计数据
		List<Map<String, Object>> dataList = siteDao.querySsWeiBoDate(map); //从数据库获取实时的（某分钟）微波辐射计数据
		Map<String,Object> smap = new HashMap<String,Object>();
		if(dataList!=null&&dataList.size()>0){
			smap = WeiBopackingDataOne(dataList);  //封装从数据库获取出来的监测数据并返回
		}
		
		
		return  smap;
	}
   
   
  
   
   
   
   
	
	
	/**
	 * 从数据库获取微波辐射计的8个K值的通道亮温数据
	 * @return
	 */
	public Map<String,Object>  queryTdlwforKdata(HttpServletRequest request,String paramname,String paramid){
		        //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				//Map<String,Object> zmap = new HashMap<String,Object>();//要返回的Map集合
				//获取数据库辐射数据近12小时时间
				String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
				
				//按时间查询 格式：yyyy-MM-dd
				//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime") +" 00:00:00";
				//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime") +" 23:59:59";
				
				Map<String,Object> paramap = new HashMap<String,Object>();
				paramap.put("objid", objid);
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
				//dataguid前缀
				String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
				
				//按时间查询 格式：yyyy-MM-dd
				String begintime = "";
				String endtime =  "";
				//是否有带时间查询（true则代表实时查询）
				boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))
						||request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"));
				if(isexist){//实时查询
					 begintime =  colMinMaxTime[0];
					 endtime   = colMinMaxTime[1] ;
		            
				}else{//带时间参数的查询
					
					endtime = request.getParameter("endtime");
					
					begintime=request.getParameter("begintime");
					
				}
				
				
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("begintime", begintime);
				map.put("endtime", endtime);
				map.put("objid", objid);
				map.put("dataguid", dataguid);
		        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> dataList = siteDao.queryTdlwforK(map); //从数据库获取历史（最近12个小时）的微波辐射计基础数据K的8个通道亮温值数据
				Map<String,Object>  kmap = new HashMap<String,Object>();
				if(dataList!=null&&dataList.size()>0){
					kmap = TdlwforKpacking(dataList);  //封装从数据库获取出来的监测数据并返回
				}
				//zmap.put("ktd",kmap);
				return  kmap;
	}
	
	
	/**
	 * 从数据库获取微波辐射计的8个V值的通道亮温数据
	 * @return
	 */
	public Map<String,Object>  queryTdlwforVdata(HttpServletRequest request,String paramname,String paramid){
		        //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				
				//Map<String,Object> zmap = new HashMap<String,Object>();//要返回的Map集合
				//获取数据库辐射数据近72小时时间
				String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
				
				//按时间查询 格式：yyyy-MM-dd
				//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime") +" 00:00:00";
				//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime") +" 23:59:59";


				Map<String,Object> paramap = new HashMap<String,Object>();
				paramap.put("objid", objid);
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
				//dataguid前缀
				String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
				
				//按时间查询 格式：yyyy-MM-dd
				String begintime = "";
				String endtime =  "";
				//是否有带时间查询（true则代表实时查询）
				boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))
						||request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"));
				if(isexist){//实时查询
					 begintime =  colMinMaxTime[0];
					 endtime   = colMinMaxTime[1] ;
		            
				}else{//带时间参数的查询
					
					endtime = request.getParameter("endtime");
					
					begintime = request.getParameter("begintime");
					
				}
				
				
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("begintime", begintime);
				map.put("endtime", endtime);
				map.put("objid", objid);
				map.put("dataguid", dataguid);
		        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> dataList = siteDao.queryTdlwforV(map); //从数据库获取历史（最近72个小时）的微波辐射计基础数据K的8个通道亮温值数据
				Map<String,Object>  kmap = new HashMap<String,Object>();
				if(dataList!=null&&dataList.size()>0){
					kmap = TdlwforVpacking(dataList);  //封装从数据库获取出来的监测数据并返回
				}
				//zmap.put("ktd",kmap);
				return  kmap;
	}
	
	
	/**
	 * 从数据库获取微波辐射计通道亮温的云底高度和降雨状态数据
	 * @return
	 */
	public Map<String,Object>  queryTdlwOtherdata(HttpServletRequest request,String paramname,String paramid){
		        //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
			
				//Map<String,Object> zmap = new HashMap<String,Object>();//要返回的Map集合
				//获取数据库辐射数据近72小时时间
				String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
				
				//按时间查询 格式：yyyy-MM-dd
				//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime") +" 00:00:00";
				//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime") +" 23:59:59";
				
				Map<String,Object> paramap = new HashMap<String,Object>();
				paramap.put("objid", objid);
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
				//dataguid前缀
				String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
				
				//按时间查询 格式：yyyy-MM-dd
				String begintime = "";
				String endtime =  "";
				//是否有带时间查询（true则代表实时查询）
				boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))
						||request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"));
				if(isexist){//实时查询
					 begintime =  colMinMaxTime[0];
					 endtime   = colMinMaxTime[1] ;
		            
				}else{//带时间参数的查询
					
					endtime = request.getParameter("endtime");
					
					begintime = request.getParameter("begintime");
					
				}
				
				
				
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("begintime", begintime);
				map.put("endtime", endtime);
				map.put("objid", objid);
				map.put("dataguid", dataguid);
		        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> dataList = siteDao.queryTdlwOtherdata(map); //从数据库获取微波辐射计通道亮温的云底高度和降雨状态数据
				Map<String,Object>  kmap = new HashMap<String,Object>();
				if(dataList!=null&&dataList.size()>0){
					kmap = TdlwOtherPacking(dataList);  //封装从数据库获取出来的监测数据并返回
				}
				 
				//zmap.put("ktd",kmap);
				return  kmap;
	}
	
	/**
	 * 从数据库获取最近的风廓线射计数据
	 * @return
	 */
	public  Map<String,Object>  queryWindProfile(HttpServletRequest request,String paramid){
		        //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				Map<String,Object> map = new HashMap<String,Object>();
				Map<String,Object> paramp = new HashMap<String,Object>();
				paramp.put("objid", objid);
				Map<String,Object> kmap = new HashMap<String,Object>();
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramp);//通过objid查询风廓线站点dataguid相关参数
				if(zmap!=null){
					String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
					//获取数据库风廓线数据近12小时时间
					String[] colMinMaxTime = colMinMaxTimeByHour("30", 12, objid, paramid, "");
					
					//按时间查询 格式：yyyy-MM-dd
					String begintime = "";
					String endtime =  "";
					//是否有带时间查询（true则代表实时查询）
					boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
					if(isexist){//实时查询
						 begintime =  colMinMaxTime[0];
						 endtime   = colMinMaxTime[1] ;
	                    
					}else{//带时间参数的查询
						
						endtime = request.getParameter("endtime");
						//获取12小时之内的数据
				    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//Date date = new Date();
				    	Date date = new Date();
						try {
							date = sdf.parse(endtime);
						} catch (ParseException e) {
							//System.out.println("时间查询参数-endtime格式转换失败");
							Logger.getLogger("").error("时间查询参数-endtime格式转换失败");
							e.printStackTrace();
						}
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-12);//取几小时的数据
						begintime = sdf.format(calendar.getTime());
						
					}
					//System.out.println("starttime:"+begintime);
					//System.out.println("endtime:"+endtime);
				
					map.put("begintime", begintime);
					map.put("endtime", endtime);
					map.put("objid", objid);
					map.put("dataguid", dataguid);
			        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			        List<Map<String, Object>> dataList =siteDao.queryWindProfileData(map);
			        //System.out.println("dataList.size:"+dataList.size());
			       // kmap = wpfDataPacking(dataList);  //封装从数据库获取出来的风廓线监测数据并返回
			        kmap = wpfDataPacking_two(dataList);  //封装从数据库获取出来的风廓线监测数据并返回(修改后的去掉斜杠后，并统一高度的数据,为了和风羽风矢图相契合)
			       // System.out.println("endtime:"+endtime);
			     
			        Map<String,Object> pMap = new HashMap<String,Object>();
			        pMap.put("devicetypeid", "30");
			        pMap.put("objid", objid);
			        pMap.put("paramid", paramid);
			        pMap.put("objtypeid", "");
			        pMap.put("endtime", endtime);
			        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
			        String maxTime = siteDao.queryMaxTimeByTime(pMap);
			        //System.out.println("kmap.size:"+kmap.size());
			        if(kmap.size()>0){
			        	//-1则说明没有该时间段的风矢风羽图
				        kmap.put("fyUrl","-1");
				        kmap.put("fsUrl","-1");
			        }
			        //根据风廓线objid查询区站编号
			        String qzbh = siteDao.queryFkxQzbhByobjid(pMap);
			     
			        if(maxTime!=null){
			        	
						//获取6分钟之前的时间（是为了当此时的风雨图还每画完时，先用上次那个风雨图）
				    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//Date date = new Date();
				    	Date date = new Date();
						try {
							date = sdf.parse(maxTime);
						} catch (ParseException e) {
							//System.out.println("时间查询参数-endtime格式转换失败");
							Logger.getLogger("").error("时间查询参数-maxTime格式转换失败");
							e.printStackTrace();
						}
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-6);//取6分钟之前的时间
						String last = sdf.format(calendar.getTime());
			        	
			        	String time = maxTime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
			        	String lastTime = last.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
			        	//System.out.println("最新时间："+time);
			        	//System.out.println("上个时间："+lastTime);
			        	//System.out.println("time:"+time);
			        	
			        	//判断此设备是设备端还是平台端
			        	Map<String, Object> platform =siteDao.queryPlatform();
			        	if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			    			//System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			    			//根据风廓线objid查找辐射计objid
			    			Map<String,Object> jmap = new HashMap<String,Object>();
			    			jmap.put("objid", objid);
			    			//查找辐射计objid
			    			String fjsObjid = siteDao.queryFsjOidhByFkxOid(jmap);
			    			fjsObjid=(fjsObjid==null?"0":fjsObjid);
			    			//System.out.println("fsjObjid:"+fjsObjid);
			    			//如果是平台端则获取objid（也就是设备端）的web服务器信息
			    		//	System.out.println("为平台端：获取设备端的imgurl");
			    			WebServer web = new WebServer(); //web服务器信息
							web.setObjid(Integer.parseInt(fjsObjid));
							List<WebServer> webList = siteDao.selectWebServer(web); //设备端web程序的服务器信息
							
								if(webList.size()>0){ //有设备端web程序的服务器信息
									String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort();
									//System.out.println("urlPrifix:"+urlPrfix);
									//System.out.println("webserver:"+webList.get(0));
									String fyUrl = urlPrfix+"/img/"+qzbh+"_"+time+"_FY0.png";
									String fsUrl = urlPrfix+"/img/"+qzbh+"_"+time+"_FS0.png";
									//System.out.println("fyUrl:"+fyUrl);
									//System.out.println("fsUrl:"+fsUrl);
									kmap.put("fyUrl",fyUrl);
									kmap.put("fsUrl",fsUrl);
									
								}else{
									System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
								}
			    			
			        	}else{ //设备端
			        		//System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			    			//如果是设备端按照以前的写法
			    			//System.out.println("为设备端：获取设备端的imgurl");
			        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
			    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
			        	//	System.out.println("urlPrefix:"+urlPrfix);
			        		
			        		  //获取风羽风矢图的路径
					        String imgdir = "E:\\outdata\\";
					        File dir = new File(imgdir);
					        if(!dir.exists()){
					        	dir.mkdirs();
					        }
					        String[] nameList = dir.list();
					        for(String name:nameList){
					        	if(name.contains(qzbh)&&name.contains("FY0")){
					        		if(name.contains(time)||name.contains(lastTime)){
					        			String fyUrl = urlPrfix+"/img/"+name;
					        			kmap.put("fyUrl",fyUrl);
					        			//System.out.println("设备端的fyUrl为："+fyUrl);
					        		}
					        		
					        	}
					        	if(name.contains(qzbh)&&name.contains("FS0")){
					        		if(name.contains(time)||name.contains(lastTime)){
					        			String fsUrl = urlPrfix+"/img/"+name;
					        			kmap.put("fsUrl",fsUrl);
					        		//	System.out.println("设备端的fsUrl为："+fsUrl);		
					        		}
					        		
					        	}
					        }
					        		
			        	}
			        	 
			        }
			      
				}
			
		        return kmap;
	}
	      //封装从数据库获取出来的风廓线监测数据并返回
			public Map<String, Object> wpfDataPacking(
				List<Map<String, Object>> dataList) {
				Map<String,Object> resultMap = new HashMap<String,Object>();
				//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //spfx
				List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //kx_sqmdkx
				List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //kx_wdkx
				List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //kx_xdsdkx
				List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //kx_zslkx
				List<Map<String,Object>> list6 = new ArrayList<Map<String,Object>>();  //kx_zslkx
				
				
				
				
				for(Map<String,Object> map:dataList){
					Map<String,Object> map1=null ;  //kx_bjcwdkx
					Map<String,Object> map2=null;   //spfs
					Map<String,Object> map3=null ;  //czfs
					Map<String,Object> map4=null ;  //spfxkxd
					Map<String,Object> map5=null ;  //czfxkxd
					Map<String,Object> map6=null ;  //cn2
					 map1 = new HashMap<String,Object>();
					 map2 = new HashMap<String,Object>();
					 map3 = new HashMap<String,Object>();
					 map4 = new HashMap<String,Object>();
					 map5 = new HashMap<String,Object>();
					 map6 = new HashMap<String,Object>();
					
					map1.put("at", map.get("collecttime").toString());
					map2.put("at", map.get("collecttime").toString());
					map3.put("at", map.get("collecttime").toString());
					map4.put("at", map.get("collecttime").toString());
					map5.put("at", map.get("collecttime").toString());
					map6.put("at", map.get("collecttime").toString());
					//int height=0;//为了让各个指标的高度一致
					//int flag=0;
					for(Map.Entry<String, Object>zmap:map.entrySet()){
						
						if("spfx".equals(zmap.getKey().toString())){
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							//System.out.println("spfx.getkey:"+zmap.getKey().toString());
							//System.out.println("spfx.getvalue:"+zmap.getValue().toString());
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
							
							
						
						}else if("spfs".equals(zmap.getKey().toString())){
							//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							
							map2.put("value", root);
							
							
						}else if("czfs".equals(zmap.getKey().toString())){
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map3.put("value", root);
							
							
							
						}else if("spfxkxd".equals(zmap.getKey())){
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map4.put("value", root);
							
						}else if("czfxkxd".equals(zmap.getKey().toString())){
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map5.put("value", root);
							
							
						}else if("cn2".equals(zmap.getKey().toString())){
							
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map6.put("value", root);
							
							
						}
					}
					list1.add(map1);
					list2.add(map2);
					list3.add(map3);
					list4.add(map4);
					list5.add(map5);
					list6.add(map6);
				}
				if(list1.size()>0){
					resultMap.put("spfx", list1);
				}
				if(list2.size()>0){
					resultMap.put("spfs", list2);
				}
				if(list3.size()>0){
					resultMap.put("czfs", list3);
				}
				if(list4.size()>0){
					resultMap.put("spfxkxd", list4);
				}
				if(list5.size()>0){
					resultMap.put("czfxkxd", list5);
				}
				if(list6.size()>0){
					resultMap.put("cn2", list6);
				}
				
				return resultMap;
		}
			
			
			
			
			
			//封装从数据库获取出来的风廓线监测数据并返回
			public Map<String, Object> wpfDataPacking_two(
				List<Map<String, Object>> dataList) {
				Map<String,Object> resultMap = new HashMap<String,Object>();
				//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //spfx
				List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //kx_sqmdkx
				List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //kx_wdkx
				List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //kx_xdsdkx
				List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //kx_zslkx
				List<Map<String,Object>> list6 = new ArrayList<Map<String,Object>>();  //kx_zslkx
				
				
				for(Map<String,Object> map:dataList){
					Map<String,Object> map1=new HashMap<String,Object>();  //kx_bjcwdkx
					Map<String,Object> map2=new HashMap<String,Object>(); //spfs
					Map<String,Object> map3=new HashMap<String,Object>();//czfs
					Map<String,Object> map4=new HashMap<String,Object>();  //spfxkxd
					Map<String,Object> map5=new HashMap<String,Object>(); //czfxkxd
					Map<String,Object> map6=new HashMap<String,Object>();  //cn2
					
					
					map1.put("at", map.get("collecttime").toString());
					map2.put("at", map.get("collecttime").toString());
					map3.put("at", map.get("collecttime").toString());
					map4.put("at", map.get("collecttime").toString());
					map5.put("at", map.get("collecttime").toString());
					map6.put("at", map.get("collecttime").toString());
					
					
					LinkedHashMap<String, Object> spfxMap =  JSON.parseObject(map.get("spfx").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					//水平风速Map
					LinkedHashMap<String, Object> spfsMap =  JSON.parseObject(map.get("spfs").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					//垂直风速Map
					LinkedHashMap<String, Object> czfsMap =  JSON.parseObject(map.get("czfs").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					//水平风速Map
					LinkedHashMap<String, Object> spfxkxdMap =  JSON.parseObject(map.get("spfxkxd").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					
					LinkedHashMap<String, Object> czfxkxdMap =  JSON.parseObject(map.get("czfxkxd").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					//垂直风速Map
					LinkedHashMap<String, Object> cn2Map =  JSON.parseObject(map.get("cn2").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					
					Set<String> highList = new HashSet<String>();  //各指标中含有/的高度，将被舍去掉
					
				
					Set<Entry<String, Object>> spfxSet = spfxMap.entrySet(); //水平风向Map
					Set<Entry<String, Object>> spfsSet = spfsMap.entrySet(); //水平风速Map
					Set<Entry<String, Object>> czfsSet = czfsMap.entrySet(); //垂直风速Map
					
					Set<Entry<String, Object>> spfxkxdSet = spfxkxdMap.entrySet(); 
					Set<Entry<String, Object>> czfxkxdSet = czfxkxdMap.entrySet(); 
					Set<Entry<String, Object>> cn2Set = cn2Map.entrySet(); 
					
					//以下指标中含有/的高度，将被舍去掉（spfs,czfs,spfx）
					for(Entry<String, Object> fx:spfxSet){
						 
						if("/".equals(fx.getValue().toString())){  //如果值为 /， 记录该高度
							highList.add(fx.getKey().split("_")[1]);
						}
						
					}
					for(Entry<String, Object> spfs:spfsSet){
						 
						if("/".equals(spfs.getValue().toString())){  //如果值为 /， 记录该高度
							highList.add(spfs.getKey().split("_")[1]);
						}
						
					}
					
					for(Entry<String, Object> czfs:czfsSet){
						 
						if("/".equals(czfs.getValue().toString())){  //如果值为 /， 记录该高度
							highList.add(czfs.getKey().split("_")[1]);
						}
						
					}
					
					//以下是真正取值了，不含/的
					if(highList.size()==0){ //随便添加一个不存在的高度，是为了防止当此步的值为0时，后面的不遍历
						highList.add("20");
					}
					
					
					Map<String,Object> zmap1=new LinkedHashMap<String,Object>();  //kx_bjcwdkx
					Map<String,Object> zmap2=new LinkedHashMap<String,Object>(); //spfs
					Map<String,Object> zmap3=new LinkedHashMap<String,Object>();//czfs
					Map<String,Object> zmap4=new LinkedHashMap<String,Object>();  //spfxkxd
					Map<String,Object> zmap5=new LinkedHashMap<String,Object>(); //czfxkxd
					Map<String,Object> zmap6=new LinkedHashMap<String,Object>();  //cn2
					
						for(Entry<String, Object> fx:spfxSet){//内层为各指标
							boolean isexist =false;  //是否存在含/的高度
							for(String high:highList){ //外层为含/的高度集合
							 if(Integer.parseInt(fx.getKey().split("_")[1])==Integer.parseInt(high)){ //不是含/的高度
								 //System.out.println("************fx.getKey()"+fx.getKey().split("_")[1]);
								 isexist=true; //说明此高度含有/
								 break;
							 }
							}
							if(!isexist){//如果此高度不含/
								zmap1.put(fx.getKey(), fx.getValue().toString()); //按以前返回的格式封装
							}
						}
						map1.put("value", zmap1);
						
						for(Entry<String, Object> fs:spfsSet){//内层为各指标
							boolean isexist =false;  //是否存在含/的高度
							for(String high:highList){ //外层为含/的高度集合
							 if(Integer.parseInt(fs.getKey().split("_")[1])==Integer.parseInt(high)){ //不是含/的高度
								 isexist=true; //说明此高度含有/
								 break;
							 }
							}
							if(!isexist){//如果此高度不含/
								 zmap2.put(fs.getKey(), fs.getValue().toString()); //按以前返回的格式封装
							}
						}
						map2.put("value", zmap2);
						
						for(Entry<String, Object> czfs:czfsSet){//内层为各指标
							boolean isexist =false;  //是否存在含/的高度
							for(String high:highList){ //外层为含/的高度集合
							 if(high.equals(czfs.getKey().split("_")[1])){ //不是含/的高度
								 isexist=true; //说明此高度含有/
								 break;
							 }
							}
							if(!isexist){//如果此高度不含/
								 zmap3.put(czfs.getKey(), czfs.getValue().toString()); //按以前返回的格式封装
							}
						}
						map3.put("value", zmap3);
						
						for(Entry<String, Object> spfxkxd:spfxkxdSet){//内层为各指标
							boolean isexist =false;  //是否存在含/的高度
							for(String high:highList){ //外层为含/的高度集合
							 if(high.equals(spfxkxd.getKey().split("_")[1])){ //不是含/的高度
								 isexist=true; //说明此高度含有/
								 break;
								
							 }
							}
							if(!isexist){//如果此高度不含/
								 zmap4.put(spfxkxd.getKey(), spfxkxd.getValue().toString()); //按以前返回的格式封装
							}
						}
						map4.put("value", zmap4);
						
						for(Entry<String, Object> czfxkxd:czfxkxdSet){//内层为各指标
							boolean isexist =false;  //是否存在含/的高度
							for(String high:highList){ //外层为含/的高度集合
							 if(high.equals(czfxkxd.getKey().split("_")[1])){ //不是含/的高度
								 isexist=true; //说明此高度含有/
								 break;
								
							 }
							}
							if(!isexist){//如果此高度不含/
								 zmap5.put(czfxkxd.getKey(), czfxkxd.getValue().toString()); //按以前返回的格式封装
							}
						}
						map5.put("value", zmap5);
						
						for(Entry<String, Object> cn2:cn2Set){//内层为各指标
							boolean isexist =false;  //是否存在含/的高度
							for(String high:highList){ //外层为含/的高度集合
							 if(high.equals(cn2.getKey().split("_")[1])){ //不是含/的高度
								 isexist=true; //说明此高度含有/
								 break;
							 }
							}
							if(!isexist){//如果此高度不含/
								 zmap6.put(cn2.getKey(), cn2.getValue().toString()); //按以前返回的格式封装
							}
						}
						map6.put("value", zmap6);
					
					
					
					list1.add(map1);
					list2.add(map2);
					list3.add(map3);
					list4.add(map4);
					list5.add(map5);
					list6.add(map6);
				}
				if(list1.size()>0){
					resultMap.put("spfx", list1);
				}
				if(list2.size()>0){
					resultMap.put("spfs", list2);
				}
				if(list3.size()>0){
					resultMap.put("czfs", list3);
				}
				if(list4.size()>0){
					resultMap.put("spfxkxd", list4);
				}
				if(list5.size()>0){
					resultMap.put("czfxkxd", list5);
				}
				if(list6.size()>0){
					resultMap.put("cn2", list6);
				}
				
				
				return resultMap;
		}
		
		
		


		/**
		 * 查询方位角度与俯仰角度
		 * @return
		 */
		public Map<String,Object> queryFwAndFy(){
			
			return siteDao.queryFwAndFy();
			
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
		public  String[] colMinMaxTimeByHour(String devicetypeid, int hour, String objid, String paramid, String objtypeid){
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
			//calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-10);//从数据库里取近20分钟的数据
			String mintime = sdf.format(calendar.getTime());
			
			String[] times = {mintime,maxtime};
			
			return times;
		}
	
		/**
		 * 封装从数据库获取出来的监测数据并返回
		 * @param dataList 从数据库查出来的辐射计监测数据
		 * @return
		 */
		public Map<String, Object> WeiBopackingData(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //kx_bjcwdkx
			List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //kx_sqmdkx
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //kx_wdkx
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //kx_xdsdkx
			List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //kx_zslkx
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			for(Map<String,Object> map:dataList){
				Map<String,Object> map1=null ;  //kx_bjcwdkx
				Map<String,Object> map2=null;   //kx_sqmdkx
				Map<String,Object> map3=null ;  //kx_wdkx
				Map<String,Object> map4=null ;  //kx_xdsdkx
				Map<String,Object> map5=null ;  //kx_zslkx
				 map1 = new HashMap<String,Object>();
				 map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				 map5 = new HashMap<String,Object>();
				
				map1.put("at", map.get("collecttime").toString());
				map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				map5.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("kx_bjcwdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map1.put("value", root);
						
					}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
						//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map2.put("value", root);
					}else if("kx_wdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map3.put("value", root);
					}else if("kx_xdsdkx".equals(zmap.getKey())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map4.put("value", root);
					}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map5.put("value", root);
					}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
						
						fwList.add(zmap.getValue().toString());
						
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
						
						fyList.add(zmap.getValue().toString());
						
					}
				}
				list1.add(map1);
				list2.add(map2);
				list3.add(map3);
				list4.add(map4);
				//list5.add(map5);
			}
			if(list1.size()>0){
				resultMap.put("kx_bjcwdkx", list1);
			}
			if(list2.size()>0){
				resultMap.put("kx_sqmdkx", list2);
			}
			if(list3.size()>0){
				resultMap.put("kx_wdkx", list3);
			}
			if(list4.size()>0){
				resultMap.put("kx_xdsdkx", list4);
			}
			if(fwList.size()>0){
				resultMap.put("fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("fyjd", fyList.get(0));
			}
			
			return resultMap;
		}
		
		
		
		/**
		 * 封装从数据库获取出来的监测数据并返回 只查温度和湿度
		 * @param dataList 从数据库查出来的辐射计监测数据
		 * @return
		 */
		public Map<String, Object> FuseWdSdpacking(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //kx_bjcwdkx
			//List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //kx_sqmdkx
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //kx_wdkx
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //kx_xdsdkx
			
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			for(Map<String,Object> map:dataList){
				//Map<String,Object> map1=null ;  //kx_bjcwdkx
				//Map<String,Object> map2=null;   //kx_sqmdkx
				Map<String,Object> map3=null ;  //kx_wdkx
				Map<String,Object> map4=null ;  //kx_xdsdkx
				//Map<String,Object> map5=null ;  //kx_zslkx
				// map1 = new HashMap<String,Object>();
				// map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				// map5 = new HashMap<String,Object>();
				
				//map1.put("at", map.get("collecttime").toString());
				//map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				//map5.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					/*if("kx_bjcwdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map1.put("value", root);
						
					}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
						//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map2.put("value", root);
					}else*/ if("kx_wdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map3.put("value", root);
					}else if("kx_xdsdkx".equals(zmap.getKey())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map4.put("value", root);
					}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map5.put("value", root);
					}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
						
						fwList.add(zmap.getValue().toString());
						
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
						
						fyList.add(zmap.getValue().toString());
						
					}
				}
				//list1.add(map1);
				//list2.add(map2);
				list3.add(map3);
				list4.add(map4);
				//list5.add(map5);
			}
			/*if(list1.size()>0){
				resultMap.put("kx_bjcwdkx", list1);
			}
			if(list2.size()>0){
				resultMap.put("kx_sqmdkx", list2);
			}*/
			if(list3.size()>0){
				resultMap.put("kx_wdkx", list3);
			}
			if(list4.size()>0){
				resultMap.put("kx_xdsdkx", list4);
			}
			if(fwList.size()>0){
				resultMap.put("fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("fyjd", fyList.get(0));
			}
			
			return resultMap;
		}
		
		
		/**
		 * 封装从数据库获取出来的监测数据并返回 只查温度和湿度 丽水的，丽水的比较特殊
		 * 每30秒上传一次ftp，所以要截取一下
		 * @param dataList 从数据库查出来的辐射计监测数据
		 * @return
		 */
		public Map<String, Object> FuseWdSdpackingLs(
				List<Map<String, Object>> dataList) {
			
			Map<String,Object> resultMap = new HashMap<String,Object>();
			
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //kx_bjcwdkx
			//List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //kx_sqmdkx
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //kx_wdkx
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //kx_xdsdkx
			
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			int i=1;
			for(Map<String,Object> map:dataList){
				if(i%12==0||i==1||i==dataList.size()){//每12个截一个,第一个和最后一个也加上去
				//Map<String,Object> map1=null ;  //kx_bjcwdkx
				//Map<String,Object> map2=null;   //kx_sqmdkx
				Map<String,Object> map3=null ;  //kx_wdkx
				Map<String,Object> map4=null ;  //kx_xdsdkx
				//Map<String,Object> map5=null ;  //kx_zslkx
				// map1 = new HashMap<String,Object>();
				// map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				// map5 = new HashMap<String,Object>();
				
				//map1.put("at", map.get("collecttime").toString());
				//map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				//map5.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					/*if("kx_bjcwdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map1.put("value", root);
						
					}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
						//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map2.put("value", root);
					}else*/ if("kx_wdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map3.put("value", root);
					}else if("kx_xdsdkx".equals(zmap.getKey())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map4.put("value", root);
					}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map5.put("value", root);
					}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
						
						fwList.add(zmap.getValue().toString());
						
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
						
						fyList.add(zmap.getValue().toString());
						
					}
				}
				//list1.add(map1);
				//list2.add(map2);
				list3.add(map3);
				list4.add(map4);
				//list5.add(map5);
			}
			i++;
			}
			/*if(list1.size()>0){
				resultMap.put("kx_bjcwdkx", list1);
			}
			if(list2.size()>0){
				resultMap.put("kx_sqmdkx", list2);
			}*/
			if(list3.size()>0){
				resultMap.put("kx_wdkx", list3);
			}
			if(list4.size()>0){
				resultMap.put("kx_xdsdkx", list4);
			}
			if(fwList.size()>0){
				resultMap.put("fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("fyjd", fyList.get(0));
			}
			
			return resultMap;
		}
		
		
		
		
		
		
		/**
		 * 重写辐射计文件生成艾玛图
		 * @param dataList 
		 * @return
		 */
		public Map<String, Object> WeiBopackingDataOne(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Map<String,Object> map = dataList.get(0); //只取第一条，最新的数据
				Map<String,Object> map1=null ;  //kx_bjcwdkx
				Map<String,Object> map2=null;   //kx_sqmdkx
				Map<String,Object> map3=null ;  //kx_wdkx
				Map<String,Object> map4=null ;  //kx_xdsdkx
				Map<String,Object> map5=null ;  //kx_zslkx
				List<String> fwList = new ArrayList<String>();  //jc_fwjd
				List<String> fyList = new ArrayList<String>();  //jc_fyjd
				
				 map1 = new HashMap<String,Object>();
				 map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				 map5 = new HashMap<String,Object>();
				
				map1.put("at", map.get("collecttime").toString());
				map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				map5.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("kx_bjcwdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
						}
			
					}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
						//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map2.put("value", root);
						}
						
					}else if("kx_wdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
                        if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                        	LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
    						map3.put("value", root);
						}
						
					}else if("kx_xdsdkx".equals(zmap.getKey())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
                       if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                    	   LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
   						   map4.put("value", root);
						}
						
					}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map5.put("value", root);
					}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
                       if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                    	   fwList.add(zmap.getValue().toString());
						}
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
                       if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                    	   fyList.add(zmap.getValue().toString());
   						
						}
					}
				}
				if(map1.size()>0){
					resultMap.put("kx_bjcwdkx", map1);
				}
				if(map2.size()>0){
					resultMap.put("kx_sqmdkx", map2);
				}
				if(map3.size()>0){
					resultMap.put("kx_wdkx", map3);
				}
				if(map4.size()>0){
					resultMap.put("kx_xdsdkx", map4);
				}
				if(fwList.size()>0){
					resultMap.put("fwjd", fwList.get(0));
				}
				if(fyList.size()>0){
					resultMap.put("fyjd", fyList.get(0));
				}
				
				//resultMap.put("kx_zslkx", list5);
			
			return resultMap;
		}
		
		
		
		
		/**
		 * 封装对比分析各站点的各廓线数据
		 * @param dataList 
		 * @return
		 */
		public Map<String, Object> WeiBopackingSskxData(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Map<String,Object> map = dataList.get(0); //只取第一条，最新的数据
				Map<String,Object> map1=new HashMap<String,Object>(); ;  //kx_bjcwdkx
				
				List<String> fwList = new ArrayList<String>();  //jc_fwjd
				List<String> fyList = new ArrayList<String>();  //jc_fyjd
				
				 map1 = new HashMap<String,Object>();
				
				
				map1.put("at", map.get("collecttime").toString());
				
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("kx_bjcwdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
						}
			
					}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
						//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
						}
						
					}else if("kx_wdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
                        if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                        	LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
                        	map1.put("value", root);
						}
						
					}else if("kx_xdsdkx".equals(zmap.getKey())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
                       if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                    	   LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
                    	   map1.put("value", root);
						}
						
					}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map5.put("value", root);
					}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
                       if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                    	   fwList.add(zmap.getValue().toString());
						}
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
                       if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
                    	   fyList.add(zmap.getValue().toString());
   						
						}
					}
				}
				if(map1.size()>0){
					resultMap.put("data", map1);
				}
				
				if(fwList.size()>0){
					resultMap.put("fwjd", fwList.get(0));
				}
				if(fyList.size()>0){
					resultMap.put("fyjd", fyList.get(0));
				}
				
				//resultMap.put("kx_zslkx", list5);
			
			return resultMap;
		}
		
		
		/**
		 * 封装从数据库获取出来的通道亮温数据并返回
		 * @param dataList 从数据库查出来的辐射计通道亮温数据
		 * @return
		 */
		public Map<String, Object> TdlwforKpacking(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //jc_k1
			List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //jc_k2
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //jc_k3
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //jc_k4
			List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //jc_k5
			List<Map<String,Object>> list6 = new ArrayList<Map<String,Object>>();  //jc_k6
			List<Map<String,Object>> list7 = new ArrayList<Map<String,Object>>();  //jc_k7
			List<Map<String,Object>> list8 = new ArrayList<Map<String,Object>>();  //jc_k8
			int i=1;
			for(Map<String,Object> map:dataList){
				if(i%10==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
				Map<String,Object> map1=null ;  //jc_k1
				Map<String,Object> map2=null;   //jc_k2
				Map<String,Object> map3=null ;  //jc_k3
				Map<String,Object> map4=null ;  //jc_k4
				Map<String,Object> map5=null ;  //jc_k5
				Map<String,Object> map6=null ;  //jc_k6
				Map<String,Object> map7=null ;  //jc_k7
				Map<String,Object> map8=null ;  //jc_k8
				 map1 = new HashMap<String,Object>();
				 map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				 map5 = new HashMap<String,Object>();
				 map6 = new HashMap<String,Object>();
				 map7 = new HashMap<String,Object>();
				 map8 = new HashMap<String,Object>();
				
				map1.put("at", map.get("collecttime").toString());
				map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				map5.put("at", map.get("collecttime").toString());
				map6.put("at", map.get("collecttime").toString());
				map7.put("at", map.get("collecttime").toString());
				map8.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("jc_k1".equals(zmap.getKey().toString())){
						
						map1.put("value", zmap.getValue().toString());
						
					}else if("jc_k2".equals(zmap.getKey().toString())){
						
						map2.put("value", zmap.getValue().toString());
						
					}else if("jc_k3".equals(zmap.getKey().toString())){
						map3.put("value", zmap.getValue().toString());
						
					}else if("jc_k4".equals(zmap.getKey())){
						
						map4.put("value", zmap.getValue().toString());
						
					}else if("jc_k5".equals(zmap.getKey().toString())){
						
						map5.put("value", zmap.getValue().toString());
						
					}else if("jc_k6".equals(zmap.getKey().toString())){
						
						map6.put("value", zmap.getValue().toString());
						
					}else if("jc_k7".equals(zmap.getKey().toString())){
						
						map7.put("value", zmap.getValue().toString());
						
					}else if("jc_k8".equals(zmap.getKey().toString())){
						
						map8.put("value", zmap.getValue().toString());
						
					}
				}
				list1.add(map1);
				list2.add(map2);
				list3.add(map3);
				list4.add(map4);
				list5.add(map5);
				list6.add(map6);
				list7.add(map7);
				list8.add(map8);
			  }
			i++;
			}
			resultMap.put("jc_k1", list1);
			resultMap.put("jc_k2", list2);
			resultMap.put("jc_k3", list3);
			resultMap.put("jc_k4", list4);
			resultMap.put("jc_k5", list5);
			resultMap.put("jc_k6", list6);
			resultMap.put("jc_k7", list7);
			resultMap.put("jc_k8", list8);
			return resultMap;
		}
		
		
		
		/**
		 * 封装从数据库获取出来的通道亮温V数据并返回
		 * @param dataList 从数据库查出来的辐射计通道亮温V数据
		 * @return
		 */
		public Map<String, Object> TdlwforVpacking(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //jc_v1
			List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //jc_v2
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //jc_v3
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //jc_v4
			List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //jc_v5
			List<Map<String,Object>> list6 = new ArrayList<Map<String,Object>>();  //jc_v6
			List<Map<String,Object>> list7 = new ArrayList<Map<String,Object>>();  //jc_v7
			List<Map<String,Object>> list8 = new ArrayList<Map<String,Object>>();  //jc_v8
			int i=1;
			for(Map<String,Object> map:dataList){
				if(i%10==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
				Map<String,Object> map1=null ;  //jc_v1
				Map<String,Object> map2=null;   //jc_v2
				Map<String,Object> map3=null ;  //jc_v3
				Map<String,Object> map4=null ;  //jc_v4
				Map<String,Object> map5=null ;  //jc_v5
				Map<String,Object> map6=null ;  //jc_v6
				Map<String,Object> map7=null ;  //jc_v7
				Map<String,Object> map8=null ;  //jc_v8
				 map1 = new HashMap<String,Object>();
				 map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				 map5 = new HashMap<String,Object>();
				 map6 = new HashMap<String,Object>();
				 map7 = new HashMap<String,Object>();
				 map8 = new HashMap<String,Object>();
				
				map1.put("at", map.get("collecttime").toString());
				map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				map5.put("at", map.get("collecttime").toString());
				map6.put("at", map.get("collecttime").toString());
				map7.put("at", map.get("collecttime").toString());
				map8.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("jc_v1".equals(zmap.getKey().toString())){
						
						map1.put("value", zmap.getValue().toString());
						
					}else if("jc_v2".equals(zmap.getKey().toString())){
						
						map2.put("value", zmap.getValue().toString());
						
					}else if("jc_v3".equals(zmap.getKey().toString())){
						map3.put("value", zmap.getValue().toString());
						
					}else if("jc_v4".equals(zmap.getKey())){
						
						map4.put("value", zmap.getValue().toString());
						
					}else if("jc_v5".equals(zmap.getKey().toString())){
						
						map5.put("value", zmap.getValue().toString());
						
					}else if("jc_v6".equals(zmap.getKey().toString())){
						
						map6.put("value", zmap.getValue().toString());
						
					}else if("jc_v7".equals(zmap.getKey().toString())){
						
						map7.put("value", zmap.getValue().toString());
						
					}else if("jc_v8".equals(zmap.getKey().toString())){
						
						map8.put("value", zmap.getValue().toString());
						
					}
				}
				list1.add(map1);
				list2.add(map2);
				list3.add(map3);
				list4.add(map4);
				list5.add(map5);
				list6.add(map6);
				list7.add(map7);
				list8.add(map8);
			    }
			i++;
			}
			resultMap.put("jc_v1", list1);
			resultMap.put("jc_v2", list2);
			resultMap.put("jc_v3", list3);
			resultMap.put("jc_v4", list4);
			resultMap.put("jc_v5", list5);
			resultMap.put("jc_v6", list6);
			resultMap.put("jc_v7", list7);
			resultMap.put("jc_v8", list8);
			return resultMap;
		}
		
		/**
		 * 封装从数据库获取出来的云底高度和降雨状态
		 * @param dataList 从数据库获取出来的云底高度和降雨状态
		 * @return
		 */
		public Map<String, Object> TdlwOtherPacking(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> ydList = new ArrayList<Map<String,Object>>();  //云底高度
			List<Map<String,Object>> jyList = new ArrayList<Map<String,Object>>();  //降雨状态
			List<String> fwList = new ArrayList<String>();  //方位角度
			List<String> fyList = new ArrayList<String>();  //俯仰角度
			
			int i=1;
			for(Map<String,Object> map:dataList){
				if(i%10==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
				Map<String,Object> ydMap=new HashMap<String,Object>(); ;  //云底高度
				Map<String,Object> jyMap=new HashMap<String,Object>();;   //降雨状态
				//Map<String,Object> map3=new HashMap<String,Object>(); ;  //方位角度
				//Map<String,Object> map4=new HashMap<String,Object>(); ;  //俯仰角度
				
				jyMap.put("at", map.get("collecttime").toString());
				ydMap.put("at", map.get("collecttime").toString());
				//map3.put("at", map.get("collecttime").toString());
				//map4.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("jc_jyzt".equals(zmap.getKey().toString())){//降雨状态
						
						jyMap.put("value", zmap.getValue().toString());
						
					}else if("fy_ydgd".equals(zmap.getKey().toString())){//云底高度
						
						ydMap.put("value", zmap.getValue().toString());
						
					}else if("jc_fwjd".equals(zmap.getKey().toString())){//方位角度
						fwList.add(zmap.getValue().toString());
						
					}else if("jc_fyjd".equals(zmap.getKey())){//俯仰角度
						
						fyList.add(zmap.getValue().toString());
						
					}
				}
				ydList.add(ydMap);
				jyList.add(jyMap);
				
			}
			i++;
				
			}
			resultMap.put("fy_ydgd", ydList);
			resultMap.put("jc_jyzt", jyList);
			resultMap.put("jc_fwjd", fwList.get(0)); //取第一个
			resultMap.put("jc_fyjd", fyList.get(0));//取第一个
			return resultMap;
		}




       /**
        * -微波辐射监测-其他廓线实时数据
        * @param request
        * @param datastreamIds
        * @param string
        * @return
        */
		public Map<String, Object> wbOtherKx(HttpServletRequest request,
				String datastreamIds, String paramid) {
				        //必传
						String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
					
						//Map<String,Object> zmap = new HashMap<String,Object>();//要返回的Map集合
						//获取数据库辐射数据近12小时时间
						String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
						
						//按时间查询 格式：yyyy-MM-dd
						//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime") +" 00:00:00";
						//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime") +" 23:59:59";
						
						Map<String,Object> paramap = new HashMap<String,Object>();
						paramap.put("objid", objid);
						Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
						//dataguid前缀
						String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
						
						//按时间查询 格式：yyyy-MM-dd
						String begintime = "";
						String endtime =  "";
						//是否有带时间查询（true则代表实时查询）
						boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))
								||request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"));
						if(isexist){//实时查询
							 begintime =  colMinMaxTime[0];
							 endtime   = colMinMaxTime[1] ;
				            
						}else{//带时间参数的查询
							
							endtime = request.getParameter("endtime");
							//获取12小时之内的数据
					    	/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							//Date date = new Date();
					    	Date date = new Date();
							try {
								date = sdf.parse(endtime);
							} catch (ParseException e) {
								System.out.println("时间查询参数-endtime格式转换失败");
								e.printStackTrace();
							}
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-72);//取几小时的数据
							begintime = sdf.format(calendar.getTime());*/
							begintime = request.getParameter("begintime");
							
						}
						
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("begintime", begintime);
						map.put("endtime", endtime);
						map.put("objid", objid);
						map.put("dataguid", dataguid);
				        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
						List<Map<String, Object>> dataList = siteDao.wbOtherKx(map); //微波辐射监测-其他廓线实时数据
						Map<String,Object> kmap=new HashMap<String,Object>();
						if(dataList!=null&&dataList.size()>0){
						  kmap = wbOtherKxPacking(dataList);  //封装从数据库获取出来的监测数据并返回
						}
						
						//zmap.put("ktd",kmap);
						return  kmap;
			
		}
		
		
		
		
		/**
		 * -对比分析-其他气象数据
		 * 
		 * @param objid
		 *            站点objid
		 * @param isexist
		 *            时间参数是否为空或不存在
		 * @param ptime
		 *            时间参数
		 * @param type
		 *            要查的数据类型
		 * @return
		 */
		public Map<String, Object> wbOtherKx_two(String objid, boolean isexist,
				String ptime, String type, String paramid) {
			// 必传
			// String objid = (request.getParameter("objid") == null ||
			// "".equals(request.getParameter("objid"))) ? "0" :
			// request.getParameter("objid");

			// Map<String,Object> zmap = new HashMap<String,Object>();//要返回的Map集合
			// 获取数据库辐射数据近12小时时间
			String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid,
					"");

			// 按时间查询 格式：yyyy-MM-dd
			// String begintime = (request.getParameter("begintime") == null ||
			// "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] :
			// request.getParameter("begintime") +" 00:00:00";
			// String endtime = (request.getParameter("endtime") == null ||
			// "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] :
			// request.getParameter("endtime") +" 23:59:59";

			Map<String, Object> paramap = new HashMap<String, Object>();
			paramap.put("objid", objid);
			Map<String, Object> zmap = siteDao.getDataguidByOid(paramap);// 通过objid查询风廓线站点dataguid相关参数
			// dataguid前缀
			String dataguid = zmap.get("objtypeid").toString() + "_"
					+ zmap.get("objid").toString() + "_"
					+ zmap.get("devicenumber").toString() + "_";

			// 按时间查询 格式：yyyy-MM-dd
			String begintime = "";
			String endtime = "";
			// 是否有带时间查询（true则代表实时查询）
			// boolean isexist = request.getParameter("endtime") == null ||
			// "".equals(request.getParameter("endtime"));
			if (isexist) {// 实时查询
				begintime = colMinMaxTime[0];
				endtime = colMinMaxTime[1];

			} else {// 带时间参数的查询

				// endtime = request.getParameter("endtime");
				endtime = ptime;
				// 获取12小时之内的数据
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				// Date date = new Date();
				Date date = new Date();
				try {
					date = sdf.parse(endtime);
				} catch (ParseException e) {
					System.out.println("时间查询参数-endtime格式转换失败");
					e.printStackTrace();
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY,
						calendar.get(Calendar.HOUR_OF_DAY) - 72);// 取几小时的数据
				begintime = sdf.format(calendar.getTime());

			}

			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("begintime", begintime);
			map.put("endtime", endtime);
			map.put("objid", objid);
			map.put("dataguid", dataguid);
			// List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			if ("hwlw".equals(type)) { // 红外量温（云低温度）
				dataList = siteDao.wbOtherKxHwlw(map); // 对比分析-其他廓线实时数据之红外亮温
			}
			if ("yts".equals(type)) { // 液态水
				dataList = siteDao.wbOtherKxYts(map); // 对比分析-其他廓线实时数据之液态水
			}
			if ("sqzhl".equals(type)) { // 水汽总含量
				dataList = siteDao.wbOtherKxSqzhl(map); // 对比分析-其他廓线实时数据之水汽总含量
			}
			if ("dmwd".equals(type)) { // 地面温度
				dataList = siteDao.wbOtherKxDmwd(map); // 对比分析-其他廓线实时数据之地面温度
			}
			if ("dmsd".equals(type)) { // 地面湿度
				dataList = siteDao.wbOtherKxDmsd(map); // 对比分析-其他廓线实时数据之地面湿度
			}

			if ("dmyq".equals(type)) { // 地面压强
				dataList = siteDao.wbOtherKxDmyq(map); // 对比分析-其他廓线实时数据之地面压强
			}

			Map<String, Object> kmap = new HashMap<String, Object>();
			if (dataList != null && dataList.size() > 0) {
				// kmap = wbOtherKxPacking(dataList); //封装从数据库获取出来的监测数据并返回
				if ("2780".equals(objid)) {// 丽水站点的数据太多（30秒上传一次数据）
					kmap = wbOtherKxPacking_two(dataList, 40); // 对比分析中其他数据的封装
				} else {
					kmap = wbOtherKxPacking_two(dataList, 4); // 对比分析中其他数据的封装
				}

			}

			if (kmap.size() > 0) { // 如果该站点有数据，再查相关站点信息
				// 站点信息

				Map<String, Object> infoMap = siteDao.findAllFsj(map) == null ? null
						: siteDao.findAllFsj(map).get(0); // 此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点

				kmap.put("siteInfo", "");
				if (infoMap != null) {
					String qzbh = siteDao.queryQzbhByobjid(paramap);// 根据objid查询区站编号
					infoMap.put("qzbh", qzbh);
					kmap.put("siteInfo", infoMap);
				}
			}

			// zmap.put("ktd",kmap);
			return kmap;

		}
		
		/**
		 * 封装从数据库获取出来的微波辐射计的其他廓线数据
		 * @param dataList 微波辐射计的其他廓线数据
		 * @return
		 */
		public Map<String, Object> wbOtherKxPacking(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //fy_sqzhl
			List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //fy_ljytszhl
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //fy_hwlw
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //jc_dmwd
			List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //jc_dmsd
			List<Map<String,Object>> list6 = new ArrayList<Map<String,Object>>();  //jc_dmyq
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			int i=1;
			for(Map<String,Object> map:dataList){
				if(i%10==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
				Map<String,Object> map1=null ;  //jc_v1
				Map<String,Object> map2=null;   //jc_v2
				Map<String,Object> map3=null ;  //jc_v3
				Map<String,Object> map4=null ;  //jc_v4
				Map<String,Object> map5=null ;  //jc_v5
				Map<String,Object> map6=null ;  //jc_v6
				
				 map1 = new HashMap<String,Object>();
				 map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				 map5 = new HashMap<String,Object>();
				 map6 = new HashMap<String,Object>();
				
				
				map1.put("at", map.get("collecttime").toString());
				map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				map5.put("at", map.get("collecttime").toString());
				map6.put("at", map.get("collecttime").toString());
				
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("fy_sqzhl".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
						
					}else if("fy_ljytszhl".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map2.put("value", zmap.getValue().toString());
						}
						
					}else if("fy_hwlw".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map3.put("value", zmap.getValue().toString());
						}
							
					}else if("jc_dmwd".equals(zmap.getKey())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map4.put("value", zmap.getValue().toString());
						}
					
					}else if("jc_dmsd".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map5.put("value", zmap.getValue().toString());
						}
						
					}else if("jc_dmyq".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map6.put("value", zmap.getValue().toString());
						}
						
					}else if("jc_fwjd".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							fwList.add(zmap.getValue().toString());
						}
						
						
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							fyList.add(zmap.getValue().toString());
						}
						
					}
				}
				if(map1.size()>0){
					list1.add(map1);
				}
				if(map2.size()>0){
					list2.add(map2);
				}
				if(map3.size()>0){
					list3.add(map3);
				}
				if(map4.size()>0){
					list4.add(map4);
				}
				if(map5.size()>0){
					list5.add(map5);
				}
				if(map6.size()>0){
					list6.add(map6);
				}
				
			 }
			i++;
			}
			
			//fy_sqzhl,fy_ljytszhl,fy_hwlw,jc_dmwd,jc_dmsd,jc_dmyq,jc_fwjd,jc_fyjd
			if(list1.size()>0){
				resultMap.put("fy_sqzhl", list1);
			}
			if(list2.size()>0){
				resultMap.put("fy_ljytszhl", list2);
			}
			if(list3.size()>0){
				resultMap.put("fy_hwlw", list3);
			}
			if(list4.size()>0){
				resultMap.put("jc_dmwd", list4);
			}
			if(list5.size()>0){
				resultMap.put("jc_dmsd", list5);
			}
			if(list6.size()>0){
				resultMap.put("jc_dmyq", list6);
			}
			if(fwList.size()>0){
				resultMap.put("jc_fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("jc_fyjd", fyList.get(0));
			}
		
			return resultMap;
		}
		
		
		/**
		 * 封装从数据库获取出来的微波辐射计的其他廓线数据
		 * @param dataList 对比分析中 微波辐射计的其他廓线数据
		 * @param size  隔几条取一次，对比分析中用，截取数据，丽水的数据量太多，跟其他的站点size不一样
		 * @return
		 */
		public Map<String, Object> wbOtherKxPacking_two(
				List<Map<String, Object>> dataList,int size) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //fy_sqzhl
			
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			int i=1;
			for(Map<String,Object> map:dataList){
				
				if(i%size==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
				Map<String,Object> map1=new HashMap<String,Object>();//
				map1.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("fy_sqzhl".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
						
					}else if("fy_ljytszhl".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
						
					}else if("fy_hwlw".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
							
					}else if("jc_dmwd".equals(zmap.getKey())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
					
					}else if("jc_dmsd".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
						
					}else if("jc_dmyq".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							map1.put("value", zmap.getValue().toString());
						}
						
					}else if("jc_fwjd".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							fwList.add(zmap.getValue().toString());
						}
						
						
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
						if(zmap.getValue()!=null&&!"".equals(zmap.getValue())){
							fyList.add(zmap.getValue().toString());
						}
						
					}
				}
				
				if(map1.size()>0){
					list1.add(map1);
				}
				
				}
				i++;
				
			}
			
			//fy_sqzhl,fy_ljytszhl,fy_hwlw,jc_dmwd,jc_dmsd,jc_dmyq,jc_fwjd,jc_fyjd
			if(list1.size()>0){
				resultMap.put("data", list1);
			}
			
			if(fwList.size()>0){
				resultMap.put("jc_fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("jc_fyjd", fyList.get(0));
			}
		
			return resultMap;
		}



   
          /*  获得艾玛图地址*/
		public Map<String, Object> queryFsjAima(HttpServletRequest request) {
			 String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ?"0":request.getParameter("objid") ;
			 //String objid = "2780";
			 String paramid="87,88,89,90,91,49,50";
			
	        // 获取数据库辐射数据近12小时时间
			 Map<String,Object> kmap = new HashMap<String,Object>();
			//获取数据库辐射数据近1小时时间
			//String[] colMinMaxTime = colMinMaxTimeByHour("5", 1, objid, paramid, "");
			 //获取已有的最新时间戳
			 
			 //String zmaxTime = baseaddDao.getMaxTimeByDtid_two( "5", objid);
			Map<String,Object> paramap = new HashMap<String,Object>();
		    paramap.put("objid", objid);
			Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
				//dataguid前缀
			String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
			zmap.put("dataguid", dataguid);
			String zmaxTime = siteDao.getFsjMaxTime(zmap);  //获得辐射计某站点最新时间
			//System.out.println("zmaxTime:"+zmaxTime);
			String endtime;
			boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
			if(isexist){
				//endtime=colMinMaxTime[1];
				endtime=zmaxTime;
			}else{
				 String time = request.getParameter("endtime");
				 Map<String,Object> pMap = new HashMap<String,Object>();
			        pMap.put("devicetypeid", "5");
			   
			        pMap.put("objid", objid);     //如果一致，那就直接查辐射计的时间
			        pMap.put("paramid", paramid);
			        pMap.put("objtypeid", "");
			        pMap.put("endtime", time);
			        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
			        endtime = siteDao.queryMaxTimeByTime(pMap);
			       
			        System.out.println("endtime:"+endtime);
			}
			 
		       if(endtime!=null){
		    	    Map<String,Object> parMap  = new HashMap<String,Object>();
		    	    parMap.put("objid", objid);
		    	    /*Map<String,Object> zmap = siteDao.getDataguidByOid(parMap);//通过objid查询站点dataguid相关参数
					//dataguid前缀
					String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";*/
					parMap.put("endtime", endtime);
		    	    parMap.put("dataguid", dataguid);
		    	    //根据时间查找稳定度系数
				    Map<String,Object> vMap = siteDao.queryWDDinfo(parMap);
				    if(vMap!=null){
				    	kmap.put("data", vMap);
				    	kmap.put("aimaUrl","-1"); //默认没有艾玛图
				    }
				    //System.out.println("vMap:"+vMap);
				   
				    
				    //查找Z0和Z-20 0度与-20度温度对应的高度
				    Map<String,Object> zMap = siteDao.queryAboutZ(parMap);
				    
                    if(zMap!=null){
                    	LinkedHashMap<String, Object> wdkxMap =  JSON.parseObject(zMap.get("datavalue").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
    					
    					Set<Entry<String, Object>> wdkxSet = wdkxMap.entrySet(); //温度廓线Map
    					
    					//System.out.println("wdkxSet.size:"+wdkxSet.size());
    				
    					
    					//0度左右对应的高度
    					for(Entry<String, Object> wdkx:wdkxSet){
    						if(!"wd_fc".equals(wdkx.getKey())&&!"wd_type".equals(wdkx.getKey())&&!"wd_0".equals(wdkx.getKey())){
    							
    							if(Float.parseFloat(wdkx.getValue().toString())<=0.0){//取最接近-20度的第一个
    								vMap.put("Z0",wdkx.getKey().split("_")[1]); //取当前高度
    									//System.out.println("wdkx.getKey():"+wdkx.getKey());
    	    							//System.out.println("wdkx.getValue():"+wdkx.getValue().toString());
    								 break; //退出循环
    							}
    						}
    					}
    					
    					//-20度左右对应的高度
    					for(Entry<String, Object> wdkx:wdkxSet){
    						if(!"wd_fc".equals(wdkx.getKey())&&!"wd_type".equals(wdkx.getKey())&&!"wd_0".equals(wdkx.getKey())){
    							
    							if(Float.parseFloat(wdkx.getValue().toString())<=-20.0){//取最接近-20度的第一个
    								vMap.put("Z-20",wdkx.getKey().split("_")[1]); //取当前高度
    									//System.out.println("wdkx.getKey():"+wdkx.getKey());
    	    							//System.out.println("wdkx.getValue():"+wdkx.getValue().toString());
    								 break; //退出循环
    							}
    						}
    					}
                    }
				    
				
				    //通过objid查区站编号
				    String qzbh = siteDao.queryQzbhByobjid(parMap);
				    
				    
				    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//Date date = new Date();
			    	Date date = new Date();
					try {
						date = sdf.parse(endtime);
					} catch (ParseException e) {
						//System.out.println("时间查询参数-endtime格式转换失败");
						Logger.getLogger("").error("时间查询参数-maxTime格式转换失败");
						e.printStackTrace();
					}
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-6);//取6分钟之前的时间
					String last = sdf.format(calendar.getTime());
		        	
					String maxTime = endtime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
		        	String lastTime = last.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
		        	System.out.println("最新时间："+maxTime);
		        	System.out.println("上个时间："+lastTime);
				    
				  
		        	
		          	//判断此设备是设备端还是平台端
		        	Map<String, Object> platform =siteDao.queryPlatform();
		        	if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
		    			//System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
		    			
		    			
		    			
		    			//如果是平台端则获取objid（也就是设备端）的web服务器信息
		    			//System.out.println("为平台端：获取设备端的imgurl");
		    			WebServer web = new WebServer(); //web服务器信息
						web.setObjid(Integer.parseInt(objid));
						//System.out.println("fsjObjid:"+objid);
						List<WebServer> webList = siteDao.selectWebServer(web); //设备端web程序的服务器信息
						
							if(webList.size()>0){ //有设备端web程序的服务器信息
								String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort();
							//	System.out.println("urlPrifix:"+urlPrfix);
							//	System.out.println("webserver:"+webList.get(0));
								String aimaUrl = urlPrfix+"/img/"+qzbh+"_"+maxTime+"_aima.png";
								
							//	System.out.println("aimaUrl:"+aimaUrl);
							
								
								kmap.put("aimaUrl",aimaUrl);
							
								
								
							}else{
								System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
							}
		    			
		        	}else{ //设备端
		        		//System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
		    			//如果是设备端按照以前的写法
		    			//System.out.println("为设备端：获取设备端的imgurl");
		        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
		    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
		        		//System.out.println("urlPrefix:"+urlPrfix);
		        		
		        		  //获取风羽风矢图的路径
				        String imgdir = "E:\\outdata\\";
				        File dir = new File(imgdir);
				        if(!dir.exists()){
				        	dir.mkdirs();
				        }
				        String[] nameList = dir.list();
				        for(String name:nameList){
				        	
				        	if(name.contains(qzbh)&&name.contains("a")&&name.contains("aima")){
				        		if(name.contains(maxTime)||name.contains(lastTime)){
				        			String aimaUrl = urlPrfix+"/img/"+name;
				        			kmap.put("aimaUrl",aimaUrl);
				        			System.out.println("设备端的aimaUrl为："+aimaUrl);		
				        			
				        		}
				        	}	
				        	
				        }
				        	
		        	}
		        	
		        	
		        	       
			        
		       }
			return kmap;
		}



		/**
         * 一张图-微波辐射监测-坐标信息及其他
         * @param request
         * @param string
         * @return
         */
		public Map<String, Object> getfsj(HttpServletRequest request,
				String remark) {
			//给出默认值:默认给国站的
			int objtypeid = 12;
			//空间表名
			String space_tablename = "space_wbstation";
			
			
			//传参Map
			Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
			
			paramMap.put("objtypeid", objtypeid);
			
			/**========================================请求参数=========================================================*/
			//行政区域(多个用逗号隔开)
			//原青岛（默认只查青岛）
			//String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "青岛" : request.getParameter("city");
			//city为空，默认都查
			String city = (request.getParameter("city") == null || "".equals(request.getParameter("city"))) ? "" : request.getParameter("city");
	        
			//站点objid(多个用逗号隔开)
			String objid = request.getParameter("objid") == null ? "" : request.getParameter("objid");
			/**======================================================================================================*/
			
			paramMap.put("city", city);
			
			paramMap.put("objid", objid);
			//System.out.println("objid:"+objid);
			
			Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
			if(!"".equals(city)||!"".equals(objid)){ //如果city参数为空，则不查,返回空
			//查辐射计站点基本信息
			List<ModelAssist> modelList = siteDao.getfsjInfoList(paramMap);
			//将返回的数据行转列
			List<Map<String,Object>> dataList=packLineData(modelList);
			//封装空间表数据及站点信息
			//resultMap = baseService.getGeoJsonFormat(space_tablename, objid, dataList);
			List<Map<String,Object>> ztList = new ArrayList<Map<String,Object>>();
			if(!"".equals(objid)){
				ztList = siteDao.getfsjZtList(paramMap);//查询辐射计站点设备的状态
			}else{
				ztList=null;
			}
			
			
			resultMap = baseService.getGeoJsonFormat_fsjzt(space_tablename, objid, dataList,ztList);
			
			}
			return resultMap;
		}




        /**
         * 将返回的数据行转列
         * @param modelList
         * @return
         */
		public List<Map<String, Object>> packLineData(
				List<ModelAssist> modelList) {
			List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
			// 定义临时的Map,接收dao层返回的数据，充当列转行的角色
			Map<String, String> hm = new LinkedHashMap<String, String>();
			for (int i = 0; i < modelList.size(); i++) {

				ModelAssist epzt = modelList.get(i);
				hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
				hm.put("objid", String.valueOf(epzt.getObjid()));
				if (i != modelList.size() - 1) {
					ModelAssist epzttwo = modelList.get(i + 1);
					// 如下if else 是针对“列转行”的数据操作
					if (!epzt.getObjid().equals(epzttwo.getObjid())) {
						// 组装的数据Bean返回给前端
						Map<String,Object> datas = new LinkedHashMap<String,Object>();
						
						datas.put("objid", Integer.valueOf(hm.get("objid")));
						Iterator hmIterator = hm.entrySet().iterator();
						while(hmIterator.hasNext()){
							Entry entry = (Entry) hmIterator.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							if(!key.equals("objid"))
							datas.put(key, value);
						}
						
						dataList.add(datas);
						hm.clear();
					} else {
						// 循环到最后“一条”数据时，需要再次保存该数据，如上if条件无法保存;或者时只有一条数据的情况
						if (i >= (modelList.size() - 2)) {
							Map<String,Object> datas = new LinkedHashMap<String,Object>();

							datas.put("objid", Integer.valueOf(hm.get("objid")));
							Iterator hmIterator = hm.entrySet().iterator();
							while(hmIterator.hasNext()){
								Entry entry = (Entry) hmIterator.next();
								String key = entry.getKey().toString();
								String value = entry.getValue().toString();
								if(!key.equals("objid"))
								datas.put(key, value);
							}
							
							dataList.add(datas);
						}
					}

				} else {// 封装最后“一行”数据
					hm.put(String.valueOf(epzt.getFieldname()), epzt.getFieldvalue());
					// 组装的数据Bean返回给前端
					Map<String,Object> datas = new LinkedHashMap<String,Object>();

					datas.put("objid", Integer.valueOf(hm.get("objid")));
					Iterator hmIterator = hm.entrySet().iterator();
					while(hmIterator.hasNext()){
						Entry entry = (Entry) hmIterator.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						if(!key.equals("objid"))
						datas.put(key, value);
					}
					
					
					// 判断modelList只有一行数据，封装最后一行数据会出现数组越界异常，因此dataList做添加操作，反之做删除dataList最后一行空数据再进行添加的操作
					if(dataList.size() <= 0){
						dataList.add(datas);
					}else{
						dataList.remove(dataList.size()-1);
						dataList.add(datas);
					}
				}

			}
			return dataList;
		}
		
		
		/**
		 * 一张图-微波辐射监测-坐标信息及其他 (new) 此接口为了新需求而在已有接口上改的(实况监测)，不传objid和city参数，默认是查所有的
		 * 
		 * @param request
		 * @param string
		 * @return
		 */
		public Map<String, Object> getfsj_two(HttpServletRequest request,
				String remark) {
			// 给出默认值:默认给国站的
			int objtypeid = 12;
			// 空间表名
			String space_tablename = "space_wbstation";

			// 传参Map
			Map<String, Object> paramMap = new LinkedHashMap<String, Object>();

			paramMap.put("objtypeid", objtypeid);

			Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

			// 查辐射计站点基本信息
			List<ModelAssist> modelList = siteDao.getfsjInfoList(paramMap);
			// 将返回的数据行转列
			List<Map<String, Object>> dataList = packLineData(modelList);

			Map<String, Object> pamap = new HashMap<String, Object>();
			String height = request.getParameter("height") == null ? "" : request
					.getParameter("height");
			String collecttime = request.getParameter("collecttime") == null ? ""
					: request.getParameter("collecttime");
			pamap.put("height", height); // 高度，应从request里获取
			pamap.put("collecttime", collecttime); // 时间，应从request里获取

			List<String> fsjObjids = siteDao.getObjidByOtid(objtypeid);
			Map<String, Object> zMap = new HashMap<String, Object>();
			zMap.put("list", fsjObjids);
			//System.out.println("fsjobjids:" + fsjObjids);
			List<String> data = siteDao.getAllfsjDataguids(zMap);// 查找所有去重后的辐射计站点dataguid(wd,sd)

			pamap.put("list", fsjObjids);

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

				pamap.put("dataguids", dataStr);
				// 辐射计所有某高度某时间的温度，湿度信息
				List<Map<String, Object>> infoList = getfsjAllSitesInfo(pamap,fsjObjids);
				// 封装空间表数据及站点信息(原有接口上修改的新接口)
				resultMap = baseService.getGeoJsonFormat_fsj(space_tablename, null,
						dataList, infoList);

			}
			return resultMap;
		}




         
		/**
		 * 辐射计所有某高度某时间的温度，湿度信息
		 * 
		 * @param pamap
		 * @return new
		 */
		private List<Map<String, Object>> getfsjAllSitesInfo(
				Map<String, Object> parMap,List<String> objids) {
			// Map<String,Object> pmap = new HashMap<String,Object>();

			String height = parMap.get("height").toString();
			// String collecttime = parMap.get("collecttime").toString();
			// pmap.put("collecttime", collecttime);
			List<Map<String, Object>> dataList = siteDao.getfsjAllSitesInfo(parMap);
			//List<String> objids = siteDao.getAllfsjObjids();// 查询辐射计所有站点obj
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();// 封装的需要返回的结果集
			if (objids != null && objids.size() > 0) { // 由于各站点辐射计时间不统一，所以现在采用的逻辑是取各站点在某个高度下离某时间点（往前推）最近的一个时间点的数据
				if (dataList != null && dataList.size() > 0) {
					for (String oid : objids) { // 各个站点objid
						for (Map<String, Object> map : dataList) { // 离某时间点最近的各个站点的数据（按时间倒序排列），以下的逻辑是取每个站点的第一条数据

							String objid = map.get("objid").toString();
							// System.out.println("当前objid为:"+objid);
							if (oid.equals(objid)) {// 当前数据为该站点的数据（第一条，因为如果满足条件，那么该次逻辑结束后，会结束这个循环，开启下一个站点的循环）
								Map<String, Object> resultMap = new HashMap<String, Object>();// 单条结果集，封装固定高度的风速，风向，objid
								// resultMap.put("objid",objid);//添加objid
								for (Map.Entry<String, Object> entry : map
										.entrySet()) { // 遍历参数结果集

									if ("wdkx".equals(entry.getKey().toString())) {
										String lastHt = "0"; // 上个高度，目标高度前一个(比它小的)插值算法要用到
										String lastvl = "0"; // 上个高度的值，目标高度前一个(比它小的)插值算法要用到
										// 使解析后的数据有序，跟数据库存储的字符串顺序对应(也就是各高度顺序有序)
										LinkedHashMap<String, Object> root = JSON
												.parseObject(
														entry.getValue().toString(),
														new TypeReference<LinkedHashMap<String, Object>>() {
														});
										for (Map.Entry<String, Object> wdVal : root
												.entrySet()) { // 垂直风速的各高度值
											String key = wdVal.getKey().split("_")[1]; // 高度
											String value = wdVal.getValue()
													.toString();

											// System.out.println("wd_key:"+key);
											// System.out.println("wd_value:"+value);
											if (!"type".equals(key)
													&& !"fc".equals(key)) {
												if (Integer.parseInt(height) <= Integer
														.parseInt(key)) {

													// if(!value.contains("/")){
													if (Integer.parseInt(height) == (Integer
															.parseInt(key))) {
														resultMap.put("wd", value);
														resultMap.put("objid",
																objid);// 添加objid
														// System.out.println("------------------start");

														// System.out.println("wd_result:"+value);
														// System.out.println("wd_key:"+key);
														// System.out.println("wd_value:"+value);
														// System.out.println("wd_height:"+height);
														// System.out.println("------------------end");
														break;
													} else {
														if ("/".equals(lastvl)
																|| "/".equals(value)) {
															resultMap.put("wd",
																	value);
															resultMap.put("objid",
																	objid);// 添加objid
															break;
														} else {
															// resultMap.put("wd",
															// value);
															float result = CalCZ
																	.calCZ(lastHt,
																			key,
																			height,
																			lastvl,
																			value);

															BigDecimal b = new BigDecimal(
																	result);
															result = b
																	.setScale(
																			3,
																			BigDecimal.ROUND_HALF_UP)
																	.floatValue(); // 保留三位小数
															resultMap.put("wd",
																	result);
															resultMap.put("objid",
																	objid);// 添加objid
															// System.out.println("wd_result:"+result);
															// /System.out.println("wd_key:"+key);
															// System.out.println("wd_value:"+value);
															// System.out.println("wd_height:"+height);
															break;
														}
													}
													// }
												} else {
													lastHt = key; // 上个高度
													lastvl = value; // 上个高度的值
												}
											}

										}
										// System.out.println("wd_lastHt:"+lastHt);
										// System.out.println("wd_lastvl:"+lastvl);

									} else if ("sdkx".equals(entry.getKey()
											.toString())) {
										String lastHt = "0"; // 上个高度，目标高度前一个(比它小的)插值算法要用到
										String lastvl = "0"; // 上个高度的值，目标高度前一个(比它小的)插值算法要用到
										// 使解析后的数据有序，跟数据库存储的字符串顺序对应
										LinkedHashMap<String, Object> root = JSON
												.parseObject(
														entry.getValue().toString(),
														new TypeReference<LinkedHashMap<String, Object>>() {
														});
										for (Map.Entry<String, Object> sdVal : root
												.entrySet()) { // 水平风速的各高度值
											String key = sdVal.getKey().split("_")[1]; // 高度;
											String value = sdVal.getValue()
													.toString();
											// System.out.println("sd_key:"+key);
											// System.out.println("sd_value:"+value);

											if (!"type".equals(key)
													&& !"fc".equals(key)) {

												if (Integer.parseInt(height) <= Integer
														.parseInt(key)) {

													// if(!value.contains("/")){
													if (Integer.parseInt(height) == (Integer
															.parseInt(key))) {
														resultMap.put("sd", value);

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
															resultMap.put("sd",
																	value);

															break;
														} else {
															// resultMap.put("wd",
															// value);
															float result = CalCZ
																	.calCZ(lastHt,
																			key,
																			height,
																			lastvl,
																			value);

															BigDecimal b = new BigDecimal(
																	result);
															result = b
																	.setScale(
																			3,
																			BigDecimal.ROUND_HALF_UP)
																	.floatValue(); // 保留三位小数
															resultMap.put("sd",
																	result);

															// System.out.println("wd_result:"+result);
															// System.out.println("wd_key:"+key);
															// System.out.println("wd_value:"+value);
															// System.out.println("wd_height:"+height);
															break;
														}
													}
													// }
												} else {
													lastHt = key; // 上个高度
													lastvl = value; // 上个高度的值
												}
											}

										}
										// System.out.println("wd_lastHt:"+lastHt);
										// System.out.println("wd_lastvl:"+lastvl);

									}

								}
								resultList.add(resultMap); // 将单条结果（objid,wd,sd）插入结果集中
								break; // 取到了某站点的第一条数据，退出该站点其他数据的循环，开始其他站点的循环
							}

						}
					}
				}
			}
			/*
			 * if(dataList!=null&&dataList.size()>0){
			 * 
			 * for(Map<String,Object>map:dataList){ Map<String,Object> resultMap =
			 * new HashMap<String,Object>();//单条结果集，封装固定高度的风速，风向，objid String objid
			 * = map.get("objid").toString(); System.out.println("当前objid为:"+objid);
			 * //resultMap.put("objid",objid);//添加objid for(Map.Entry<String,
			 * Object>entry:map.entrySet()){ //遍历参数结果集
			 * 
			 * if("wdkx".equals(entry.getKey().toString())){
			 * 
			 * //使解析后的数据有序，跟数据库存储的字符串顺序对应 LinkedHashMap<String, Object>
			 * root=JSON.parseObject(entry.getValue().toString(),new
			 * TypeReference<LinkedHashMap<String, Object>>(){} );
			 * for(Map.Entry<String, Object>wdVal:root.entrySet()){ //垂直风速的各高度值
			 * String key = wdVal.getKey(); String value =
			 * wdVal.getValue().toString(); System.out.println("wd_key:"+key);
			 * System.out.println("wd_value:"+value); if("wd_100".equals(key)){
			 * //if(!value.contains("/")){ resultMap.put("wd", value);
			 * resultMap.put("objid",objid);//添加objid break; //} }
			 * 
			 * }
			 * 
			 * }else if("sdkx".equals(entry.getKey().toString())){
			 * 
			 * //使解析后的数据有序，跟数据库存储的字符串顺序对应 LinkedHashMap<String, Object>
			 * root=JSON.parseObject(entry.getValue().toString(),new
			 * TypeReference<LinkedHashMap<String, Object>>(){} );
			 * for(Map.Entry<String, Object>sdVal:root.entrySet()){ //水平风速的各高度值
			 * String key = sdVal.getKey(); String value =
			 * sdVal.getValue().toString(); System.out.println("sd_key:"+key);
			 * System.out.println("sd_value:"+value);
			 * 
			 * if("xdsd_100".equals(key)){ //if(!value.contains("/")){
			 * resultMap.put("sd", value); break; //} } }
			 * 
			 * }
			 * 
			 * } resultList.add(resultMap); //将单条结果（objid,wd,sd）插入结果集中 } }
			 */
			/*
			 * Map<String,Object> tempMap = new HashMap<String,Object>();
			 * tempMap.put("data", resultList);
			 */
			return resultList;
		}
		
		
		/**
		 * 从数据库获取实时的（最近1个小时的最新一条）微波辐射计的温度廓线数据
		 * 
		 * @param objid
		 *            站点objid
		 * @param ptime
		 *            时间参数
		 * @param type
		 *            要查的辐射计指标
		 * @return
		 */
		public Map<String, Object> queryWeiBoSsKxdata(String objid, String ptime,
				String type, String paramid) {

			// 必传
			// String objid = (request.getParameter("objid") == null ||
			// "".equals(request.getParameter("objid"))) ? "0" :
			// request.getParameter("objid");

			Map<String, Object> paramap = new HashMap<String, Object>();
			paramap.put("objid", objid);
			Map<String, Object> zmap = siteDao.getDataguidByOid(paramap);// 通过objid查询风廓线站点dataguid相关参数
			// dataguid前缀
			String dataguid = zmap.get("objtypeid").toString() + "_"
					+ zmap.get("objid").toString() + "_"
					+ zmap.get("devicenumber").toString() + "_";
			zmap.put("dataguid", dataguid);
			// String zmaxTime = siteDao.getFsjMaxTime(zmap); //获得辐射计某站点最新时间

			// String endtime = ptime == null || "".equals(ptime) ? zmaxTime :
			// ptime;

			Map<String, Object> map = new HashMap<String, Object>();
			// map.put("begintime", begintime);
			// map.put("endtime", endtime);
			map.put("objid", objid);
			map.put("dataguid", dataguid);

			// System.out.println("zmaxTime:"+zmaxTime);
			// System.out.println("endtime:"+endtime);

			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			if ("wd".equals(type)) { // 地面温度
				dataList = siteDao.querySsWeiBowdDate(map); // 从数据库获取实时的（某分钟）微波辐射计的温度廓线的数据
			}
			if ("xdsd".equals(type)) { // 相对湿度
				dataList = siteDao.querySsWeiBosdDate(map); // 从数据库获取实时的（某分钟）微波辐射计的相对湿度廓线的数据
			}
			if ("sqmd".equals(type)) { // 水汽密度
				dataList = siteDao.querySsWeiBosqmdDate(map); // 从数据库获取实时的（某分钟）微波辐射计的水汽密度廓线的数据
			}
			if ("bjc".equals(type)) { // 边界层
				dataList = siteDao.querySsWeiBobjcDate(map); // 从数据库获取实时的（某分钟）微波辐射计的边界层廓线的数据
			}
			if ("yts".equals(type)) { // 液态水 目前还没数据
				dataList = siteDao.querySsWeiBosqmdDate(map); // 从数据库获取实时的（某分钟）微波辐射计的液态水廓线的数据
			}

			Map<String, Object> kmap = new HashMap<String, Object>();
			if (dataList != null && dataList.size() > 0) {
				// kmap = WeiBopackingDataOne(dataList); //封装从数据库获取出来的监测数据并返回
				kmap = WeiBopackingSskxData(dataList); // 封装从数据库获取出来的监测数据并返回
				if (kmap.size() > 0) { // 如果该站点有数据，再查相关站点信息
					// 站点信息
					Map<String, Object> infoMap = siteDao.findAllFsj(map) == null ? null
							: siteDao.findAllFsj(map).get(0); // 此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点
					kmap.put("siteInfo", "");
					if (infoMap != null) {
						String qzbh = siteDao.queryQzbhByobjid(paramap);// 根据objid查询区站编号
						infoMap.put("qzbh", qzbh);
						kmap.put("siteInfo", infoMap);
					}
				}
			}

			return kmap;
		}
		
		
	   
	   /**
		 * 从数据库获取历史（最近12个小时）的微波辐射计二维分布数据（根据type）
		 * @param objid 站点objid
		 * @param isexist 时间参数是否为空或不存在
		 * @param ptime 时间参数
		 * 
		 * @param type 要查的辐射计指标
		 * @return
		 */
		public Map<String,Object>  queryWeiBoLsEwdata(String objid,boolean isexist,String ptime,String type,String paramid){
			
			        //必传
					//String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
					Map<String,Object> map = new HashMap<String,Object>();
					//获取数据库辐射数据近12小时时间
					String[] colMinMaxTime = colMinMaxTimeByHour("5", 72, objid, paramid, "");
				
					
					Map<String,Object> paramap = new HashMap<String,Object>();
					paramap.put("objid", objid);
					Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
					
					if(zmap==null){ //如果没有该站点的数据，直接返回各空集合，后面的操作不再继续
						return new  HashMap<String,Object>();
					}
					String qzbh = siteDao.queryQzbhByobjid(paramap);//根据objid查询区站编号
					//dataguid前缀
					String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
					
					
					//按时间查询 格式：yyyy-MM-dd
					String begintime = "";
					String endtime =  "";
					//是否有带时间查询（true则代表实时查询）
					//boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
					if(isexist){//实时查询
						 begintime =  colMinMaxTime[0];
						 endtime   = colMinMaxTime[1] ;
			            
					}else{//带时间参数的查询
						
						endtime = ptime;
						//获取12小时之内的数据
				    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//Date date = new Date();
				    	Date date = new Date();
						try {
							date = sdf.parse(endtime);
						} catch (ParseException e) {
							System.out.println("时间查询参数-endtime格式转换失败");
							e.printStackTrace();
						}
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-72);//取几小时的数据
						begintime = sdf.format(calendar.getTime());
						
					}
					
					map.put("begintime", begintime);
					map.put("endtime", endtime);
					map.put("objid", objid);
					map.put("dataguid", dataguid);
			        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					List<Map<String, Object>> dataList=new ArrayList<Map<String,Object>>();
					if("wd".equals(type)){ //地面温度
						dataList = siteDao.queryWeiBoWdDate(map); //从数据库获取历史（最近12个小时）的微波辐射计数据(地面温度二维分布数据)
					}
					if("xdsd".equals(type)){ //相对湿度
						dataList = siteDao.queryWeiBoSdDate(map); //从数据库获取历史（最近12个小时）的微波辐射计数据(相对湿度二维分布数据)
					}
					if("sqmd".equals(type)){ //水汽密度
						dataList = siteDao.queryWeiBoSqmdDate(map); //从数据库获取历史（最近12个小时）的微波辐射计数据(谁其密度二维分布数据)
					}
					if("bjc".equals(type)){ //边界层
						dataList = siteDao.queryWeiBoBjcDate(map); //从数据库获取历史（最近12个小时）的微波辐射计数据(边界层温度二维分布数据)
					}
					if("yts".equals(type)){ //液态水  目前还没数据
						dataList = siteDao.queryWeiBoSqmdDate(map); //从数据库获取历史（最近12个小时）的微波辐射计数据(液态水二维分布数据)
					}
					
					Map<String,Object> kmap = new HashMap<String,Object>();
					
					if(dataList!=null&&dataList.size()>0){
						  //kmap = wbOtherKxPacking(dataList);  //封装从数据库获取出来的监测数据并返回
						kmap = WeiBopackingData_two(dataList);  //封装从数据库获取出来的监测数据并返回-对比分析中用的
						}
					
					if(kmap.size()>0){ //如果该站点有数据，再查相关站点信息
						//站点信息
						
						Map<String,Object> infoMap = siteDao.findAllFsj(map)==null?null:siteDao.findAllFsj(map).get(0); //此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点
						
						kmap.put("siteInfo", "");
						if(infoMap!=null){
							infoMap.put("qzbh", qzbh);
							kmap.put("siteInfo",infoMap);
						}
					}
					
					  System.out.println("endtime:"+endtime);
				       // 获取数据库辐射数据近12小时时间
					  
				       //暂时可能不需要 查覆盖用的风羽风矢图
				       /* Map<String,Object> pMap = new HashMap<String,Object>();
				        pMap.put("devicetypeid", "5");
				       
				        pMap.put("objid", objid);     //如果一致，那就直接查辐射计的时间
				        pMap.put("paramid", paramid);
				        pMap.put("objtypeid", "");
				        pMap.put("endtime", endtime);
				        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
				        String maxTime = siteDao.queryMaxTimeByTime(pMap);
				        System.out.println("kmap.size:"+kmap.size());
				        if(kmap.size()>0){
				        	//-1则说明没有该时间段的风矢风羽图
					        kmap.put("fyUrl","-1");
					        kmap.put("fsUrl","-1");
				        }
				        
				        System.out.println("maxTime:"+maxTime);
				        if(maxTime!=null){
				        	String time = maxTime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
				        	System.out.println("time:"+time);
				        	  //获取风羽风矢图的路径
					        String imgdir = "D:\\outdata\\";
					        File dir = new File(imgdir);
					        if(!dir.exists()){
					        	dir.mkdirs();
					        }
					        String[] nameList = dir.list();
					        for(String name:nameList){
					        	if(name.contains(qzbh)&&name.contains(time)&&name.contains("P")&&name.contains("FY")){
					        		kmap.put("fyUrl","/img/"+name);
					        	}
					        	if(name.contains(qzbh)&&name.contains(time)&&name.contains("P")&&name.contains("FS")){
					        		kmap.put("fsUrl","/img/"+name);
					        	}
					        }
				        }*/
					
					return  kmap;
		}
	  
		
		/**
		 * 封装从数据库获取出来的监测数据并返回（12个小时）
		 * (各种用于多个站点对比分析的二维分布数据，返回了120个点，中间要砍掉一些,返回30个左右)
		 * @param dataList 从数据库查出来的辐射计监测数据
		 * @return
		 */
		public Map<String, Object> WeiBopackingData_two(
				List<Map<String, Object>> dataList) {
			
			Map<String,Object> resultMap = new HashMap<String,Object>();
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //kx_bjcwdkx
			
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			int i=1;
			for(Map<String,Object> map:dataList){
				
				if(i%4==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
					Map<String,Object> map1=new HashMap<String,Object>() ;  //kx_bjcwdkx
					
					
					map1.put("at", map.get("collecttime").toString());
					
					for(Map.Entry<String, Object>zmap:map.entrySet()){
						 
						if("kx_bjcwdkx".equals(zmap.getKey().toString())){
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
							
						}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
							//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
						}else if("kx_wdkx".equals(zmap.getKey().toString())){
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
						}else if("kx_xdsdkx".equals(zmap.getKey())){
							//使解析后的数据有序，跟数据库存储的字符串顺序对应
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map1.put("value", root);
						}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
							LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
							map5.put("value", root);
						}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
							
							fwList.add(zmap.getValue().toString());
							
						}else if("jc_fyjd".equals(zmap.getKey().toString())){
							
							fyList.add(zmap.getValue().toString());
							
						}
					}
					if(map1.size()>0){
						list1.add(map1);
					}
					
				}
				i++;
				
			}
			if(list1.size()>0){
				resultMap.put("data", list1);
			}
			
			if(fwList.size()>0){
				resultMap.put("fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("fyjd", fyList.get(0));
			}
			
			return resultMap;
		}
		
		
		/**
		 * 对比分析-获得艾玛图地址
		 * 
		 * @param objid
		 *            站点objid
		 * @param isexist
		 *            时间参数是否为空或不存在
		 * @param ptime
		 *            时间参数
		 * 
		 * @return
		 */
		public Map<String, Object> queryDbfxAima(HttpServletRequest request,String objid, boolean isexist,
				String ptime) {
			// String objid = (request.getParameter("objid") == null ||
			// "".equals(request.getParameter("objid")))
			// ?"0":request.getParameter("objid") ;
			// String objid = "2780";
			String paramid = "87,88,89,90,91,49,50";

			// 获取数据库辐射数据近12小时时间
			Map<String, Object> kmap = new HashMap<String, Object>();
			// 获取数据库辐射数据近1小时时间
			// String[] colMinMaxTime = colMinMaxTimeByHour("5", 1, objid, paramid,
			// "");
			// 获取已有的最新时间戳

			// String zmaxTime = baseaddDao.getMaxTimeByDtid_two( "5", objid);
			Map<String, Object> paramap = new HashMap<String, Object>();
			paramap.put("objid", objid);
			Map<String, Object> zmap = siteDao.getDataguidByOid(paramap);// 通过objid查询风廓线站点dataguid相关参数
			// dataguid前缀
			String dataguid = zmap.get("objtypeid").toString() + "_"
					+ zmap.get("objid").toString() + "_"
					+ zmap.get("devicenumber").toString() + "_";
			zmap.put("dataguid", dataguid);
			String zmaxTime = siteDao.getFsjMaxTime(zmap); // 获得辐射计某站点最新时间
			//System.out.println("zmaxTime:" + zmaxTime);
			String endtime;
			// boolean isexist = request.getParameter("endtime") == null ||
			// "".equals(request.getParameter("endtime"));
			if (isexist) {
				// endtime=colMinMaxTime[1];
				endtime = zmaxTime;
			} else {
				String time = ptime;
				Map<String, Object> pMap = new HashMap<String, Object>();
				pMap.put("devicetypeid", "5");

				pMap.put("objid", objid); // 如果一致，那就直接查辐射计的时间
				pMap.put("paramid", paramid);
				pMap.put("objtypeid", "");
				pMap.put("endtime", time);
				// 根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
				endtime = siteDao.queryMaxTimeByTime(pMap);

				System.out.println("endtime:" + endtime);
			}

			if (endtime != null) {
				Map<String, Object> parMap = new HashMap<String, Object>();
				parMap.put("objid", objid);
				/*
				 * Map<String,Object> zmap =
				 * siteDao.getDataguidByOid(parMap);//通过objid查询站点dataguid相关参数
				 * //dataguid前缀 String dataguid =
				 * zmap.get("objtypeid").toString()+"_"
				 * +zmap.get("objid").toString()+
				 * "_"+zmap.get("devicenumber").toString()+"_";
				 */
				parMap.put("endtime", endtime);
				parMap.put("dataguid", dataguid);
				// 根据时间查找稳定度系数
				Map<String, Object> vMap = siteDao.queryWDDinfo(parMap);
				if (vMap != null) {
					kmap.put("data", vMap);
					kmap.put("aimaUrl", "-1"); // 默认没有艾玛图
				}
				// System.out.println("vMap:"+vMap);

				// 查找Z0和Z-20 0度与-20度温度对应的高度
				Map<String, Object> zMap = siteDao.queryAboutZ(parMap);

				if (zMap != null) {
					LinkedHashMap<String, Object> wdkxMap = JSON.parseObject(zMap
							.get("datavalue").toString(),
							new TypeReference<LinkedHashMap<String, Object>>() {
							});

					Set<Entry<String, Object>> wdkxSet = wdkxMap.entrySet(); // 温度廓线Map

					// System.out.println("wdkxSet.size:"+wdkxSet.size());

					// 0度左右对应的高度
					for (Entry<String, Object> wdkx : wdkxSet) {
						if (!"wd_fc".equals(wdkx.getKey())
								&& !"wd_type".equals(wdkx.getKey())
								&& !"wd_0".equals(wdkx.getKey())) {

							if (Float.parseFloat(wdkx.getValue().toString()) <= 0.0) {// 取最接近-20度的第一个
								vMap.put("Z0", wdkx.getKey().split("_")[1]); // 取当前高度
								// System.out.println("wdkx.getKey():"+wdkx.getKey());
								// System.out.println("wdkx.getValue():"+wdkx.getValue().toString());
								break; // 退出循环
							}
						}
					}

					// -20度左右对应的高度
					for (Entry<String, Object> wdkx : wdkxSet) {
						if (!"wd_fc".equals(wdkx.getKey())
								&& !"wd_type".equals(wdkx.getKey())
								&& !"wd_0".equals(wdkx.getKey())) {

							if (Float.parseFloat(wdkx.getValue().toString()) <= -20.0) {// 取最接近-20度的第一个
								vMap.put("Z-20", wdkx.getKey().split("_")[1]); // 取当前高度
								// System.out.println("wdkx.getKey():"+wdkx.getKey());
								// System.out.println("wdkx.getValue():"+wdkx.getValue().toString());
								break; // 退出循环
							}
						}
					}
				}

				// 通过objid查区站编号
				String qzbh = siteDao.queryQzbhByobjid(parMap);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				// Date date = new Date();
				Date date = new Date();
				try {
					date = sdf.parse(endtime);
				} catch (ParseException e) {
					// System.out.println("时间查询参数-endtime格式转换失败");
					Logger.getLogger("").error("时间查询参数-maxTime格式转换失败");
					e.printStackTrace();
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 6);// 取6分钟之前的时间
				String last = sdf.format(calendar.getTime());

				String maxTime = endtime.replace("-", "").replace(":", "")
						.replace(" ", "").substring(0, 12);
				String lastTime = last.replace("-", "").replace(":", "")
						.replace(" ", "").substring(0, 12);
				System.out.println("最新时间：" + maxTime);
				System.out.println("上个时间：" + lastTime);

				// System.out.println(objid+"的区站编号："+qzbh);
				// String maxTime = endtime.replace("-", "").replace(":",
				// "").replace(" ", "").substring(0,12);
				// System.out.println("colMinMaxTime2[1]:"+colMinMaxTime[1]);
				// System.out.println("maxTime:"+maxTime);
				//判断此设备是设备端还是平台端
	        	Map<String, Object> platform =siteDao.queryPlatform();
	        	if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
	    			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
	    			
	    			//根据风廓线objid查找辐射计objid
	    			/*Map<String,Object> jmap = new HashMap<String,Object>();
	    			jmap.put("objid", objid);
	    			//查找辐射计objid
	    			String fjsObjid = siteDao.queryFsjOidhByFkxOid(jmap);
	    			fjsObjid=(fjsObjid==null?"0":fjsObjid);
	    			System.out.println("fsjObjid:"+fjsObjid);*/
	    			
	    			//如果是平台端则获取objid（也就是设备端）的web服务器信息
	    			System.out.println("为平台端：获取设备端的imgurl");
	    			WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(objid));
					System.out.println("fsjObjid:"+objid);
					List<WebServer> webList = siteDao.selectWebServer(web); //设备端web程序的服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort();
							//System.out.println("urlPrifix:"+urlPrfix);
							//System.out.println("webserver:"+webList.get(0));
							String aimaUrl = urlPrfix+"/img/"+qzbh+"_"+maxTime+"_aima.png";
							
							//System.out.println("aimaUrl:"+aimaUrl);
						
							
							kmap.put("aimaUrl",aimaUrl);
						
							
							
						}else{
							System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
						}
	    			
	        	}else{ //设备端
	        		//System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
	    			//如果是设备端按照以前的写法
	    			//System.out.println("为设备端：获取设备端的imgurl");
	        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
	    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
	        		//System.out.println("urlPrefix:"+urlPrfix);
	        		
	        		  //获取风羽风矢图的路径
			        String imgdir = "E:\\outdata\\";
			        File dir = new File(imgdir);
			        if(!dir.exists()){
			        	dir.mkdirs();
			        }
			        String[] nameList = dir.list();
			        for(String name:nameList){
			        	
			        	if(name.contains(qzbh)&&name.contains("a")&&name.contains("aima")){
			        		if(name.contains(maxTime)||name.contains(lastTime)){
			        			String aimaUrl = urlPrfix+"/img/"+name;
			        			kmap.put("aimaUrl",aimaUrl);
			        			System.out.println("设备端的aimaUrl为："+aimaUrl);		
			        			
			        		}
			        	}	
			        	
			        }
			        	
	        	}

				Map<String, Object> infoMap = siteDao.findAllFsj(paramap) == null ? null
						: siteDao.findAllFsj(paramap).get(0); // 此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点

				kmap.put("siteInfo", "");
				if (infoMap != null) {
					infoMap.put("qzbh", qzbh);
					kmap.put("siteInfo", infoMap);
				}

			}
			return kmap;
		}

		
		
		/**
		 * 对比分析-获取水汽通量（辐射计的和风廓线的联合查询）
		 * @param map
		 * @return
		 */
		public List<Map<String,Object>> getWaterVapor(String fsjObjid,String endtime){
			Map<String,Object> map = new HashMap<String,Object>();
			List<Map<String,Object>> resultList  = new ArrayList<Map<String,Object>>();
			Map<String,Object> pmap = new HashMap<String,Object>();
			String fkxObjid=null;
			try{
				if(!"".equals(fsjObjid)){
					pmap.put("objid", fsjObjid);
					//根据辐射计objid查对应站点的风廓线的objid 
					 fkxObjid = siteService.queryFkxOidhByFsjOid(pmap);
				}
				if(fkxObjid!=null){ //如果有对应区站编号的风廓线站点，则继续查水汽通量，否则也没必要了，因为这是融合数据
					map.put("fsjObjid", fsjObjid);
					map.put("endtime", endtime);
					map.put("fkxObjid", fkxObjid);
					List<TemperatureParam> dataList =  siteService.getWaterVapor(map);
				    if(dataList!=null&&dataList.size()>0){
				    	for(TemperatureParam param:dataList){
				    	    Map<String,Object> resultMap = new HashMap<String,Object>();
				    	    resultMap.put("collecttime", param.getCollecttime());
				    	    resultMap.put("height", param.getHz());
				    	    //通过算法计算返回各高度的水汽通量值
				    		Map<String,BigDecimal[]> zmap = CalFH.calFH(param.getHz(),param.getD(),param.getVz(),param.getT(),param.getRH(),param.getHighsize(),param.getP0(),param.getT0());
				    		//resultMap.put("yq", zmap.get("Pb"));//廓线高度上压强点
				    		resultMap.put("spsq", zmap.get("FH"));//廓线高度点上的水平水汽通量
				    		resultMap.put("czsq", zmap.get("FZ"));//廓线高度点上的垂直水汽通量
				    		
				    		//System.out.println("spsq:"+Arrays.asList(zmap.get("FH")));
				    		//System.out.println("czsq:"+Arrays.asList(zmap.get("FZ")));
				    		//System.out.println("getHighsize:"+param.getHighsize());
				    		//System.out.println("param:"+param);
				    		
				    		Map<String,Object> infoMap = siteDao.findAllFsj(pmap)==null?null:siteDao.findAllFsj(pmap).get(0); //此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点
							
				    		resultMap.put("siteInfo", "");
							if(infoMap!=null){
								 //通过objid查区站编号
							    String qzbh = siteDao.queryQzbhByobjid(pmap);
								infoMap.put("qzbh", qzbh);
								resultMap.put("siteInfo",infoMap);
							}
				    		
				    		resultList.add(resultMap);
				    		
				    	}
				    }
				}
			}catch(Exception e){
				e.printStackTrace();
				Logger.getLogger("").error("查询水汽通量出错了");
				resultList=new ArrayList<Map<String,Object>>();
			}
			
		    return resultList;
		}
		
		
		/**
		 * 对比分析-获取获取温度平流（辐射计的和风廓线的联合查询）
		 * @param map
		 * @return
		 */
		public List<Map<String,Object>> getDbfxTemperature(String fsjObjid,String endtime){
			List<Map<String,Object>> resultList  = new ArrayList<Map<String,Object>>();
			Map<String,Object> map = new HashMap<String,Object>();
		
			Map<String,Object> pmap = new HashMap<String,Object>();
			String fkxObjid=null;
			try{
				if(!"".equals(fsjObjid)){
					pmap.put("objid", fsjObjid);
					//根据辐射计objid查对应站点的风廓线的objid 
					fkxObjid = siteService.queryFkxOidhByFsjOid(pmap);
			  }
			if(fkxObjid!=null){ //如果有对应区站编号的风廓线站点，则继续查温度平流，否则也没必要了，因为这是融合数据
				map.put("fsjObjid", fsjObjid);
				map.put("endtime", endtime);
				map.put("fkxObjid", fkxObjid);
				List<TemperatureParam> dataList =  siteService.getTemperatureParam(map);
			    if(dataList!=null&&dataList.size()>0){
			    	for(TemperatureParam param:dataList){
			    	    Map<String,Object> resultMap = new HashMap<String,Object>();
			    	    resultMap.put("collecttime", param.getCollecttime());
			    	    //通过算法计算返回各高度的温度平流值
			    	    int length = param.getHz().length;
			    	    if(length>0){
			    	    	 //通过算法计算返回各高度的温度平流值
				    		Map<String,BigDecimal[]> zmap = CalDJR.calTempFlue(param.getHz(),param.getD(),param.getV(),length,param.getFai(),param.getT0());
				    		
				    		resultMap.put("value", zmap.get("Dt"));//各高度的温度平流值集合
				    		resultMap.put("height", zmap.get("Ht"));//各高度的集合
				    		
                          /*  Map<String,Object> infoMap = siteDao.findAllFsj(pmap)==null?null:siteDao.findAllFsj(pmap).get(0); //此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点
							
				    		resultMap.put("siteInfo", "");
							if(infoMap!=null){
								 //通过objid查区站编号
							    String qzbh = siteDao.queryQzbhByobjid(pmap);
								infoMap.put("qzbh", qzbh);
								resultMap.put("siteInfo",infoMap);
							}
				    		*/
				    		resultList.add(resultMap);
			    	    }
			    	}
			    }
			}
			}catch(Exception e){
				e.printStackTrace();
				Logger.getLogger("").error("查询温度平流出错了");
				resultList=new ArrayList<Map<String,Object>>();
			}
			
		    
		    return resultList;
		}
		
		
		/**
		 * 一张图-融合图列表-坐标信息及其他
		 * 
		 * @param request
		 * @param string
		 * @return
		 */
		public Map<String, Object> getFuseMenu(HttpServletRequest request,
				String remark) {
			// 辐射计
			int objtypeid = 12;
			// 空间表名
			String space_tablename = "space_wbstation";

			// 传参Map
			Map<String, Object> paramMap = new LinkedHashMap<String, Object>();

			paramMap.put("objtypeid", objtypeid);

			/**
			 * ========================================请求参数========================
			 * =================================
			 */
			// 行政区域(多个用逗号隔开)
			// 原青岛（默认只查青岛）
			// String city = (request.getParameter("city") == null ||
			// "".equals(request.getParameter("city"))) ? "青岛" :
			// request.getParameter("city");
			// city为空，默认都查
			String city = (request.getParameter("city") == null || ""
					.equals(request.getParameter("city"))) ? "" : request
					.getParameter("city");

			// 站点objid(多个用逗号隔开)
			String objid = request.getParameter("objid") == null ? "" : request
					.getParameter("objid");
			/**
			 * ====================================================================
			 * ==================================
			 */

			paramMap.put("city", city);

			paramMap.put("objid", objid);

			Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
			if (!"".equals(city) || !"".equals(objid)) { // 如果city参数为空，则不查,返回空
				// 查辐射计站点基本信息
				List<ModelAssist> modelList = siteDao.getfsjInfoList(paramMap);
				// 将返回的数据行转列
				List<Map<String, Object>> dataList = packLineData(modelList);
				// 封装空间表数据及站点信息
				// resultMap = baseService.getGeoJsonFormat(space_tablename, objid,
				// dataList);
				// List<Map<String,Object>> ztList = new
				// ArrayList<Map<String,Object>>();
				// ztList = siteDao.getfsjZtList(paramMap);//查询辐射计站点设备的状态

				resultMap = baseService.getGeoJsonFormat_fuse(space_tablename,
						objid, dataList);

			}
			return resultMap;
		}

		
		
		/**
		 * 从数据库获取历史（最近12个小时）的微波辐射计数据 融合图，温度，湿度二维分布
		 * 
		 * @return
		 */
		public Map<String, Object> queryFuseWdSd(HttpServletRequest request,
				String paramid) {

			// 必传
			String objid = (request.getParameter("objid") == null || ""
					.equals(request.getParameter("objid"))) ? "0" : request
					.getParameter("objid");
			Map<String, Object> map = new HashMap<String, Object>();
			// 获取数据库辐射数据近12小时时间
			String[] colMinMaxTime = colMinMaxTimeByHour("5", 12, objid, paramid,
					"");

			// 按时间查询 格式：yyyy-MM-dd
			// String begintime = (request.getParameter("begintime") == null ||
			// "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] :
			// request.getParameter("begintime") +" 00:00:00";
			// String endtime = (request.getParameter("endtime") == null ||
			// "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] :
			// request.getParameter("endtime") +" 23:59:59";

			Map<String, Object> paramap = new HashMap<String, Object>();
			paramap.put("objid", objid);
			Map<String, Object> zmap = siteDao.getDataguidByOid(paramap);// 通过objid查询风廓线站点dataguid相关参数

			String qzbh = siteDao.queryQzbhByobjid(paramap);// 根据objid查询区站编号
			// dataguid前缀
			String dataguid = zmap.get("objtypeid").toString() + "_"
					+ zmap.get("objid").toString() + "_"
					+ zmap.get("devicenumber").toString() + "_";

			// 按时间查询 格式：yyyy-MM-dd
			String begintime = "";
			String endtime = "";
			String maxtime = "";// 查风羽图的时候用到的
			// 是否有带时间查询（true则代表实时查询）
			boolean isexist = request.getParameter("endtime") == null
					|| "".equals(request.getParameter("endtime"));
			if (isexist) {// 实时查询
				begintime = colMinMaxTime[0];
				endtime = colMinMaxTime[1];
				Map<String, Object> pmap = new HashMap<String, Object>();
				pmap.put("objid", objid);
				String fkxObjid = siteDao.queryFkxOidhByFsjOid(pmap);
				pmap.put("objid", fkxObjid);
				Map<String, Object> vmap = siteDao.getDataguidByOid(pmap);// 通过objid查询风廓线站点dataguid相关参数

				// dataguid前缀 风廓线
				String fkxdataguid = vmap.get("objtypeid").toString() + "_"
						+ vmap.get("objid").toString() + "_"
						+ vmap.get("devicenumber").toString() + "_";

				pmap.put("dataguid", fkxdataguid);

				maxtime = siteDao.getFkxMaxTime(pmap); // 风羽图的图片的时间

			} else {// 带时间参数的查询

				endtime = request.getParameter("endtime");

				// 获取12小时之内的数据
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				// Date date = new Date();
				Date date = new Date();
				try {
					date = sdf.parse(endtime);
				} catch (ParseException e) {
					System.out.println("时间查询参数-endtime格式转换失败");
					e.printStackTrace();
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY,
						calendar.get(Calendar.HOUR_OF_DAY) - 12);// 取几小时的数据
				begintime = sdf.format(calendar.getTime());

				Map<String, Object> pMap = new HashMap<String, Object>();
				pMap.put("objid", objid);
				String fkxObjid = siteDao.queryFkxOidhByFsjOid(pMap);

				// maxtime =endtime; //风羽图的图片的时间
				Map<String, Object> pmap = new HashMap<String, Object>();
				pmap.put("devicetypeid", "30");
				pmap.put("objid", fkxObjid); // 此处这个objid应是当前辐射计站点所对应的风廓线站点objid(需要另查)我这是考虑风廓线和辐射计时间不一致的情况下
				String fkxParamid = "168,169,170,171,172,173";
				// pmap.put("objid", objid); //如果一致，那就直接查辐射计的时间
				pmap.put("paramid", fkxParamid);
				pmap.put("objtypeid", "");
				pmap.put("endtime", endtime);
				// 根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
				maxtime = siteDao.queryMaxTimeByTime(pmap);

			}

			map.put("begintime", begintime);
			map.put("endtime", endtime);
			map.put("objid", objid);
			map.put("dataguid", dataguid);
			// List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

			// List<Map<String, Object>> dataList = siteDao.queryWeiBoDate(map);
			// //从数据库获取历史（最近12个小时）的微波辐射计数据

			List<Map<String, Object>> dataList = siteDao.queryFuseWdSd(map);// 只获取温度和湿度的数据

			// Map<String,Object> kmap = FuseWdSdpacking(dataList);
			// //封装从数据库获取出来的监测数据并返回
			Map<String, Object> kmap = new HashMap<String, Object>();
			if ("2780".equals(objid)) {
				kmap = FuseWdSdpackingLs(dataList); // (丽水的，比较特殊)封装从数据库获取出来的监测数据并返回
			} else {
				kmap = FuseWdSdpacking(dataList); // 封装从数据库获取出来的监测数据并返回
			}

			// System.out.println("endtime:"+endtime);
			// 获取数据库辐射数据近12小时时间
			// String[] colMinMaxTime2 = colMinMaxTimeByHour("30", 12, objid,
			// paramid, "");
			// String maxTime = colMinMaxTime2[1].replace("-", "").replace(":",
			// "").replace(" ", "").substring(0,12);
			// System.out.println("colMinMaxTime2[1]:"+colMinMaxTime2[1]);

			if (kmap.size() > 0) {
				// -1则说明没有该时间段的风矢风羽图
				kmap.put("fyUrl", "-1");
				kmap.put("fsUrl", "-1");
			}

			// System.out.println("maxTime:"+maxtime);
			if (maxtime != null) {
				// String time = maxtime.replace("-", "").replace(":",
				// "").replace(" ", "").substring(0,12);

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				// Date date = new Date();
				Date date = new Date();
				try {
					date = sdf.parse(maxtime);
				} catch (ParseException e) {
					// System.out.println("时间查询参数-endtime格式转换失败");
					Logger.getLogger("").error("时间查询参数-maxTime格式转换失败");
					e.printStackTrace();
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 6);// 取6分钟之前的时间
				String last = sdf.format(calendar.getTime());

				String time = maxtime.replace("-", "").replace(":", "")
						.replace(" ", "").substring(0, 12);
				String lastTime = last.replace("-", "").replace(":", "")
						.replace(" ", "").substring(0, 12);
				System.out.println("最新时间：" + time);
				System.out.println("上个时间：" + lastTime);
				
				
				

				//判断此设备是设备端还是平台端
	        	Map<String, Object> platform =siteDao.queryPlatform();
	        	if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
	    			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
	    			//根据风廓线objid查找辐射计objid
	    			Map<String,Object> jmap = new HashMap<String,Object>();
	    			jmap.put("objid", objid);
	    			//查找辐射计objid
	    			String fjsObjid = siteDao.queryFsjOidhByFkxOid(jmap);
	    			fjsObjid=(fjsObjid==null?"0":fjsObjid);
	    			System.out.println("fsjObjid:"+fjsObjid);
	    			//如果是平台端则获取objid（也就是设备端）的web服务器信息
	    			System.out.println("为平台端：获取设备端的imgurl");
	    			WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(fjsObjid));
					List<WebServer> webList = siteDao.selectWebServer(web); //设备端web程序的服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort();
							//System.out.println("urlPrifix:"+urlPrfix);
							//System.out.println("webserver:"+webList.get(0));
							String fyUrl = urlPrfix+"/img/"+qzbh+"_"+time+"_FY0.png";
							String fsUrl = urlPrfix+"/img/"+qzbh+"_"+time+"_FS0.png";
							//System.out.println("fyUrl:"+fyUrl);
							//System.out.println("fsUrl:"+fsUrl);
							kmap.put("fyUrl",fyUrl);
							kmap.put("fsUrl",fsUrl);
							
						}else{
							System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
						}
	    			
	        	}else{ //设备端
	        		//System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
	    			//如果是设备端按照以前的写法
	    			//System.out.println("为设备端：获取设备端的imgurl");
	        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
	    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
	        		//System.out.println("urlPrefix:"+urlPrfix);
	        		
	        		  //获取风羽风矢图的路径
			        String imgdir = "E:\\outdata\\";
			        File dir = new File(imgdir);
			        if(!dir.exists()){
			        	dir.mkdirs();
			        }
			        String[] nameList = dir.list();
			        for(String name:nameList){
			        	if(name.contains(qzbh)&&name.contains("FY0")){
			        		if(name.contains(time)||name.contains(lastTime)){
			        			String fyUrl = urlPrfix+"/img/"+name;
			        			kmap.put("fyUrl",fyUrl);
			        			System.out.println("设备端的fyUrl为："+fyUrl);
			        		}
			        		
			        	}
			        	if(name.contains(qzbh)&&name.contains("FS0")){
			        		if(name.contains(time)||name.contains(lastTime)){
			        			String fsUrl = urlPrfix+"/img/"+name;
			        			kmap.put("fsUrl",fsUrl);
			        			System.out.println("设备端的fsUrl为："+fsUrl);		
			        		}
			        		
			        	}
			        }
			        		
	        	}
	        	
				
				
			}

			return kmap;
		}
		
		
		
		/**
		 * 封装从数据库获取出来的监测数据并返回(丽水的辐射计，丽水比较特殊，ftp上是30秒上传一次，12个小时数据量太大,要截一下)
		 * @param dataList 从数据库查出来的辐射计监测数据
		 * @return
		 */
		public Map<String, Object> WeiBopackingDataLs(
				List<Map<String, Object>> dataList) {
			Map<String,Object> resultMap = new HashMap<String,Object>();
			
			
			//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //kx_bjcwdkx
			List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();  //kx_sqmdkx
			List<Map<String,Object>> list3 = new ArrayList<Map<String,Object>>();  //kx_wdkx
			List<Map<String,Object>> list4 = new ArrayList<Map<String,Object>>();  //kx_xdsdkx
			List<Map<String,Object>> list5 = new ArrayList<Map<String,Object>>();  //kx_zslkx
			List<String> fwList = new ArrayList<String>();  //jc_fwjd
			List<String> fyList = new ArrayList<String>();  //jc_fyjd
			
			int i=1;
			for(Map<String,Object> map:dataList){
				if(i%10==0||i==1||i==dataList.size()){//每四个截一个,第一个和最后一个也加上去
				Map<String,Object> map1=null ;  //kx_bjcwdkx
				Map<String,Object> map2=null;   //kx_sqmdkx
				Map<String,Object> map3=null ;  //kx_wdkx
				Map<String,Object> map4=null ;  //kx_xdsdkx
				Map<String,Object> map5=null ;  //kx_zslkx
				 map1 = new HashMap<String,Object>();
				 map2 = new HashMap<String,Object>();
				 map3 = new HashMap<String,Object>();
				 map4 = new HashMap<String,Object>();
				 map5 = new HashMap<String,Object>();
				
				map1.put("at", map.get("collecttime").toString());
				map2.put("at", map.get("collecttime").toString());
				map3.put("at", map.get("collecttime").toString());
				map4.put("at", map.get("collecttime").toString());
				map5.put("at", map.get("collecttime").toString());
				
				for(Map.Entry<String, Object>zmap:map.entrySet()){
					
					if("kx_bjcwdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map1.put("value", root);
						
					}else if("kx_sqmdkx".equals(zmap.getKey().toString())){
						//LinkedHashMap<String, Object> contentMap = JSON.parseObject(zmap.getValue(), LinkedHashMap.class, Feature.OrderedField);
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map2.put("value", root);
					}else if("kx_wdkx".equals(zmap.getKey().toString())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map3.put("value", root);
					}else if("kx_xdsdkx".equals(zmap.getKey())){
						//使解析后的数据有序，跟数据库存储的字符串顺序对应
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map4.put("value", root);
					}/*else if("kx_zslkx".equals(zmap.getKey().toString())){
						LinkedHashMap<String, Object> root=JSON.parseObject(zmap.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
						map5.put("value", root);
					}*/else if("jc_fwjd".equals(zmap.getKey().toString())){
						
						fwList.add(zmap.getValue().toString());
						
					}else if("jc_fyjd".equals(zmap.getKey().toString())){
						
						fyList.add(zmap.getValue().toString());
						
					}
				}
				list1.add(map1);
				list2.add(map2);
				list3.add(map3);
				list4.add(map4);
				//list5.add(map5);
				}  
				i++;
			}
			if(list1.size()>0){
				resultMap.put("kx_bjcwdkx", list1);
			}
			if(list2.size()>0){
				resultMap.put("kx_sqmdkx", list2);
			}
			if(list3.size()>0){
				resultMap.put("kx_wdkx", list3);
			}
			if(list4.size()>0){
				resultMap.put("kx_xdsdkx", list4);
			}
			if(fwList.size()>0){
				resultMap.put("fwjd", fwList.get(0));
			}
			if(fyList.size()>0){
				resultMap.put("fyjd", fyList.get(0));
			}
			
			return resultMap;
		}



		
		@Autowired
		private FsjFtpUtilsgz fsjFtpgz;
		
		//线程池
		
		ScheduledExecutorService executor=null;

         /**
          * 测试改版同步辐射计站点
          * @param hashMap
          */
		public void testTbFsj(HashMap<String, Object> hashMap) {
			
			
			  
			 final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			 //System.out.println("时间:" + sdf.format(new Date()) );
			 Map<String,Object> map = new HashMap<String,Object> ();
			 map.put("objtypeid", 12);  //微波辐射计   28-风廓线	 
			 List<FsjFtpParam> fsjList = siteDao.queryFsjSb(map);
			// System.out.println("fsjList.size:"+fsjList.size());
			 if(executor!=null){ //已经赋值，先停掉线程池
				// System.out.println("已有线程池");
				 executor.shutdown();//关闭线程池
				 executor=null;
				// System.out.println("已关老闭线程池: executor=:"+executor);
			 }
			 executor = Executors.newScheduledThreadPool(fsjList.size()>0&&fsjList.size()<15?fsjList.size():15);
			 
			// System.out.println("已创建新新线程池，线程池大小为："+ (fsjList.size()>0&&fsjList.size()<15?fsjList.size():15));
			 if(fsjList!=null && fsjList.size()>0){
				 for(final FsjFtpParam param:fsjList){
					// System.out.println("fsjFtpParam:"+param);
					 executor.scheduleAtFixedRate(new Runnable() {
			                
			                public void run() {
			                	   
			                    try {
			                    	 fsjFtpgz.tbFtpFsj(param.getSitenumber(), param.getObjid(), param.getIp(), param.getPort(), param.getUsername(), param.getPassword(), param.getFilepath());
				                    // System.out.println("时间:" + sdf.format(new Date())+" 线程" + Thread.currentThread().getName() + " 执行了task: " );
				                  
			                    } catch (Exception e) {
			                        e.printStackTrace();
			                        Logger.getLogger("").error(e.getMessage());
			                        
			                    }
			                }
			            },  0, 2, TimeUnit.MINUTES);

			
				 }
				 
				//不注释这行会打印出问题，应该是先执行关闭线程操作了
		        //executor.shutdown();
			 }else{
				 Logger.getLogger("").info("未查询到已启用的该类型的ftp服务器信息");
			 }
			
			
		}
		
}
