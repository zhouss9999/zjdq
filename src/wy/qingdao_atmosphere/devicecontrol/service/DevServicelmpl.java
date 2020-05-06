package wy.qingdao_atmosphere.devicecontrol.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.domain.DbConnOid;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;
import wy.qingdao_atmosphere.countrysitedata.service.SiteDataService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Service("DevService")
public class DevServicelmpl implements DevService {
	
	
	
	private static final String URL_PREFIX = "http://192.168.0.91:8000";
	private static final String USER_NAME = "test";
	private static final String PASS_WORD = "test";
	
	
	//String listResult = getGeneralUrl(URL_PREFIX + "/open/device/mylist",
	//		"access_token=" + access_token + "&user_id=" + user_id);
	
	
	@Autowired
	private SiteDataService siteSercie;
	
	
	@Autowired
	private SiteDataDao siteDao;
	
	/**
	 * POST请求
	 * 
	 * @param generalUrl
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String postGeneralUrl(String generalUrl, String params) throws Exception {
		URL url = new URL(generalUrl + "?");
		// 打开和URL之间的连接
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		// 设置通用的请求属性
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);

		String encoding = "UTF-8";
		if (generalUrl.contains("nlp")) {
			encoding = "GBK";
		}
		// 得到请求的输出流对象
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.write(params.getBytes(encoding));
		out.flush();
		out.close();

		// 建立实际的连接
		connection.connect();
		// 获取所有响应头字段
		Map<String, List<String>> headers = connection.getHeaderFields();
		// 遍历所有的响应头字段
		for (String key : headers.keySet()) {
			System.err.println(key + "--->" + headers.get(key));
		}
		// 定义 BufferedReader输入流来读取URL的响应
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
		StringBuilder result = new StringBuilder();
		String getLine;
		while ((getLine = in.readLine()) != null) {
			result.append(getLine + "\n");
		}
		in.close();
		return result.toString();
	}
	
	
	/**
	 * GET请求
	 * 
	 * @param generalUrl
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String getGeneralUrl(String generalUrl, String params) throws Exception {
		URL url = new URL(generalUrl + "?" + params);
		// 打开和URL之间的连接
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		// 设置通用的请求属性
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);

		String encoding = "UTF-8";
		//String encoding = "GBK";
		if (generalUrl.contains("nlp")) {
			encoding = "GBK";
		}

		// 建立实际的连接
		connection.connect();
		// 获取所有响应头字段
		Map<String, List<String>> headers = connection.getHeaderFields();
		// 遍历所有的响应头字段
		for (String key : headers.keySet()) {
			System.err.println(key + "--->" + headers.get(key));
		}
		// 定义 BufferedReader输入流来读取URL的响应
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
		System.out.println("encoding:"+encoding);
		StringBuilder result = new StringBuilder();
		String getLine;
		while ((getLine = in.readLine()) != null) {
			result.append(getLine + "\n");
		}
		in.close();

		return result.toString();
	}
    
	/**
     * 操作设备电源 
     * @param cmd  打开（1）或关闭（0）
     * @param request
     * @return
     */
	public Map<String, Object> updatePower(HttpServletRequest request) {
		String cmd = (request.getParameter("cmd")==null||"".equals(request.getParameter("cmd")))?"-1":request.getParameter("cmd");
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		
        String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
		
		Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			map.put("objid", objid);
			
			List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
			System.out.println("connlist:"+connList);
			if(connList.size()>0){ //存在关联objid
				//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
				Integer otherObjid = connList.get(0).getOtherobjid();
				System.out.println("objid:"+objid);
				System.out.println("otherobjid:"+otherObjid);
				WebServer web = new WebServer(); //web服务器信息
				web.setObjid(Integer.parseInt(objid));
				List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
				
					if(webList.size()>0){ //有设备端web程序的服务器信息
						String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+webList.get(0));
				
				    try {
					    String controUser = "pingtai1";
						String resultData = postGeneralUrl(urlPrfix + "/api/power",
								"objid=" + otherObjid + "&contol=" + controUser + "&cmd=" + cmd);
						//int i=5/0;
						map = (Map<String,Object>)JSON.parseObject(resultData);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "平台端操作设备端设备电源状态失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger("").error("平台端操作设备端查询设备电源失败 /"+e.getMessage());
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的Web服务器信息");
					 map.put("code", "500"); 
				 }
				
			}else{//不存在关联objid
				 map.put("data", "");
				 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
				 map.put("code", "500"); 
			}
			
		    
		}else { //2 - 设备端
			//如果是设备端，直接操作设备端的设备
			
			System.out.println(contol+":操作了本设备的电源状态---------");
			
			WebServer fsj = new WebServer();
			fsj.setObjid(Integer.parseInt(objid));
			List<WebServer> list = siteSercie.selectFsjServer(fsj);
			//Map<String,Object> map = new HashMap<String,Object>();
			if(list.size()>0){ //有辐射计控制程序的服务器信息
				String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
				System.out.println("urlPrifix:"+urlPrfix);
				System.out.println("webserver:"+list.get(0));
				 try {
						String listResult = postGeneralUrl(urlPrfix + "/api/login",
						"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
						
						JSONObject userInfo = JSON.parseObject(listResult);
						String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
						System.out.println("listResult:"+listResult);
						System.out.println("rkey:"+rkey);
						
						
						String resultData = postGeneralUrl(urlPrfix + "/api/power",
								"rkey=" + rkey + "&cmd=" + cmd  );
						
						map.put("data",JSON.parseObject(resultData));
						map.put("message", "操作设备电源 成功");
						map.put("code", "200");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "操作设备电源失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger(DevServicelmpl.class).error("操作设备电源失败 /"+e.getMessage());
						return map;
					}
			}
			
		}
		
		
		
		
		
	   
		
		return map;
	}

     /**
      * 查询设备电源状态
      */
	public Map<String, Object> queryPower(HttpServletRequest request) {
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
		
		Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			map.put("objid", objid);
			
			List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
			System.out.println("connlist:"+connList);
			if(connList.size()>0){ //存在关联objid
				//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
				Integer otherObjid = connList.get(0).getOtherobjid();
				System.out.println("objid:"+objid);
				System.out.println("otherobjid:"+otherObjid);
				WebServer web = new WebServer(); //web服务器信息
				web.setObjid(Integer.parseInt(objid));
				List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
				
					if(webList.size()>0){ //有设备端web程序的服务器信息
						String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+webList.get(0));
				
				    try {
					    String controUser = "pingtai1";
						String resultData = getGeneralUrl(urlPrfix + "/api/power",
								"objid=" + otherObjid + "&contol=" + controUser );
						//int i=5/0;
						map = (Map<String,Object>)JSON.parseObject(resultData);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "平台端查询设备端设备电源状态失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger("").error("平台端查询设备端查询设备电源失败 /"+e.getMessage());
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的Web服务器信息");
					 map.put("code", "500"); 
				 }
				
			}else{//不存在关联objid
				 map.put("data", "");
				 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
				 map.put("code", "500"); 
			}
			
		    
		}else { //2 - 设备端
			//如果是设备端，直接操作设备端的设备
			
			System.out.println(contol+":查询了本设备的电源状态---------");
			WebServer fsj = new WebServer();
			fsj.setObjid(Integer.parseInt(objid));
			List<WebServer> list = siteSercie.selectFsjServer(fsj); //辐射计控制程序的服务器信息
			
			if(list.size()>0){ //有辐射计控制程序的服务器信息
				String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
				System.out.println("urlPrifix:"+urlPrfix);
				System.out.println("webserver:"+list.get(0));
		
		    try {
				String listResult = postGeneralUrl(urlPrfix + "/api/login",
				"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
				
				JSONObject userInfo = JSON.parseObject(listResult);
				String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
				System.out.println("listResult:"+listResult);
				System.out.println("rkey:"+rkey);
				
				
				String resultData = getGeneralUrl(urlPrfix + "/api/power",
						"rkey=" + rkey  );
				//int i=5/0;
				map.put("data",JSON.parseObject(resultData));
				map.put("message", "查询设备电源状态成功");
				map.put("code", "200");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				map.put("data", "");
				map.put("message", "查询设备电源状态失败 /"+e.getMessage());
				map.put("code", "500");
				e.printStackTrace();
				Logger.getLogger("").error("查询设备电源失败 /"+e.getMessage());
				return map;
			}
		 }else{
			 map.put("data", "");
			 map.put("message", "未查询到该设备的辐射计服务器信息");
			 map.put("code", "500"); 
		 }
			
		}
		
	
		return map;
	}

    /**
     * 鼓风机/接收机风扇查询
     */
	public Map<String, Object> queryFans(HttpServletRequest request) {
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		
        String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
		
		Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			map.put("objid", objid);
			
			List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
			System.out.println("connlist:"+connList);
			if(connList.size()>0){ //存在关联objid
				//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
				Integer otherObjid = connList.get(0).getOtherobjid();
				System.out.println("objid:"+objid);
				System.out.println("otherobjid:"+otherObjid);
				WebServer web = new WebServer(); //web服务器信息
				web.setObjid(Integer.parseInt(objid));
				List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
				
					if(webList.size()>0){ //有设备端web程序的服务器信息
						String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+webList.get(0));
				
				    try {
					    String controUser = "pingtai1";
						String resultData = getGeneralUrl(urlPrfix + "/api/fans",
								"objid=" + otherObjid + "&contol=" + controUser );
						//int i=5/0;
						map = (Map<String,Object>)JSON.parseObject(resultData);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "平台端鼓风机/接收机风扇控制查询失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger("").error("平台端鼓风机/接收机风扇控制查询失败 /"+e.getMessage());
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的Web服务器信息");
					 map.put("code", "500"); 
				 }
				
			}else{//不存在关联objid
				 map.put("data", "");
				 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
				 map.put("code", "500"); 
			}
			
		    
		}else { //2 - 设备端
			System.out.println(contol+":查询了本设备的鼓风机/接收机风扇控制查询---------");
			
			WebServer fsj = new WebServer();
			fsj.setObjid(Integer.parseInt(objid));
			List<WebServer> list = siteSercie.selectFsjServer(fsj);
			//Map<String,Object> map = new HashMap<String,Object>();
			if(list.size()>0){ //有辐射计控制程序的服务器信息
				String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
				System.out.println("urlPrifix:"+urlPrfix);
				System.out.println("webserver:"+list.get(0));
		
		    try {
				String listResult = postGeneralUrl(urlPrfix + "/api/login",
				"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
				
				JSONObject userInfo = JSON.parseObject(listResult);
				String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
				System.out.println("listResult:"+listResult);
				System.out.println("rkey:"+rkey);
				
				
				String resultData = getGeneralUrl(urlPrfix + "/api/fans",
						"rkey=" + rkey  );
				
				map.put("data",JSON.parseObject(resultData));
				map.put("message", "鼓风机/接收机风扇控制查询成功");
				map.put("code", "200");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				map.put("data", "");
				map.put("message", "鼓风机/接收机风扇控制查询失败 /"+e.getMessage());
				map.put("code", "500");
				e.printStackTrace();
				return map;
			}
		 }else{
			 map.put("data", "");
			 map.put("message", "未查询到该设备的辐射计服务器信息");
			 map.put("code", "500"); 
		 }
					
		}
		
		
		
		
		return map;
	}
	
	
	/**
	 * 鼓风机/接收机风扇控制
	 * @param request
	 * @return
	 */
	public Map<String, Object> updateFans(HttpServletRequest request) {
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		String blower = request.getParameter("blower")==null?"":request.getParameter("blower");
		String fanK = request.getParameter("fanK")==null?"":request.getParameter("fanK");
		String fanV = request.getParameter("fanV")==null?"":request.getParameter("fanV");
		
		 String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
			
			Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
				System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				map.put("objid", objid);
				
				List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
				System.out.println("connlist:"+connList);
				if(connList.size()>0){ //存在关联objid
					//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
					Integer otherObjid = connList.get(0).getOtherobjid();
					System.out.println("objid:"+objid);
					System.out.println("otherobjid:"+otherObjid);
					WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(objid));
					List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
							System.out.println("urlPrifix:"+urlPrfix);
							System.out.println("webserver:"+webList.get(0));
					
					    try {
						    String controUser = "pingtai1";
							String resultData = postGeneralUrl(urlPrfix + "/api/fans",
									"objid=" + otherObjid + "&contol=" + controUser 
									+ "&blower=" + blower+ "&fanK=" + fanK
									+ "&fanV=" + fanV);
							//int i=5/0;
							map = (Map<String,Object>)JSON.parseObject(resultData);
							
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							map.put("data", "");
							map.put("message", "平台端鼓风机/接收机风扇控制失败 /"+e.getMessage());
							map.put("code", "500");
							e.printStackTrace();
							Logger.getLogger("").error("平台端鼓风机/接收机风扇控制失败 /"+e.getMessage());
							return map;
						}
					 }else{
						 map.put("data", "");
						 map.put("message", "未查询到该设备的Web服务器信息");
						 map.put("code", "500"); 
					 }
					
				}else{//不存在关联objid
					 map.put("data", "");
					 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
					 map.put("code", "500"); 
				}
				
			    
			}else { //2 - 设备端
				System.out.println(contol+":操作了本设备的鼓风机/接收机风扇控制---------");
				
				WebServer fsj = new WebServer();
				fsj.setObjid(Integer.parseInt(objid));
				List<WebServer> list = siteSercie.selectFsjServer(fsj);
				//Map<String,Object> map = new HashMap<String,Object>();
				if(list.size()>0){ //有辐射计控制程序的服务器信息
					String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
					System.out.println("urlPrifix:"+urlPrfix);
					System.out.println("webserver:"+list.get(0));
			
			    try {
					String listResult = postGeneralUrl(urlPrfix + "/api/login",
					"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
					
					JSONObject userInfo = JSON.parseObject(listResult);
					String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
					System.out.println("listResult:"+listResult);
					System.out.println("rkey:"+rkey);
					
					
					String resultData = postGeneralUrl(urlPrfix + "/api/fans",
							"rkey=" + rkey  + "&blower=" + blower+ "&fanK=" + fanK
							+ "&fanV=" + fanV);
					
					map.put("data",JSON.parseObject(resultData));
					map.put("message", "鼓风机/接收机风扇控制成功");
					map.put("code", "200");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					map.put("data", "");
					map.put("message", "鼓风机/接收机风扇控制失败 /"+e.getMessage());
					map.put("code", "500");
					e.printStackTrace();
					return map;
				}
			 }else{
				 map.put("data", "");
				 map.put("message", "未查询到该设备的辐射计服务器信息");
				 map.put("code", "500"); 
			 }
		
			}
		
		
		return map;
	}
	
	


	/**
	 * 伺服角度查询
	 * @param request
	 * @return
	 */
	public Map<String, Object> queryServo(HttpServletRequest request) {
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		
		 String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
			
		 Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
				System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				map.put("objid", objid);
				
				List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
				System.out.println("connlist:"+connList);
				if(connList.size()>0){ //存在关联objid
					//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
					Integer otherObjid = connList.get(0).getOtherobjid();
					System.out.println("objid:"+objid);
					System.out.println("otherobjid:"+otherObjid);
					WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(objid));
					List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
							System.out.println("urlPrifix:"+urlPrfix);
							System.out.println("webserver:"+webList.get(0));
					
					    try {
						    String controUser = "pingtai1";
							String resultData = getGeneralUrl(urlPrfix + "/api/servo",
									"objid=" + otherObjid + "&contol=" + controUser );
							//int i=5/0;
							map = (Map<String,Object>)JSON.parseObject(resultData);
							
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							map.put("data", "");
							map.put("message", "平台端伺服角度查询失败 /"+e.getMessage());
							map.put("code", "500");
							e.printStackTrace();
							Logger.getLogger("").error("平台端伺服角度查询失败 /"+e.getMessage());
							return map;
						}
					 }else{
						 map.put("data", "");
						 map.put("message", "未查询到该设备的Web服务器信息");
						 map.put("code", "500"); 
					 }
					
				}else{//不存在关联objid
					 map.put("data", "");
					 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
					 map.put("code", "500"); 
				}
				
			    
			}else { //2 - 设备端
				System.out.println(contol+":查询了本设备的伺服角度查询---------");
				
				WebServer fsj = new WebServer();
				fsj.setObjid(Integer.parseInt(objid));
				List<WebServer> list = siteSercie.selectFsjServer(fsj);
				//Map<String,Object> map = new HashMap<String,Object>();
				if(list.size()>0){ //有辐射计控制程序的服务器信息
					String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
					System.out.println("urlPrifix:"+urlPrfix);
					System.out.println("webserver:"+list.get(0));
			    try {
					String listResult = postGeneralUrl(urlPrfix + "/api/login",
					"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
					
					JSONObject userInfo = JSON.parseObject(listResult);
					String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
					System.out.println("listResult:"+listResult);
					System.out.println("rkey:"+rkey);
					
					
					String resultData = getGeneralUrl(urlPrfix + "/api/servo",
							"rkey=" + rkey  );
					
					map.put("data",JSON.parseObject(resultData));
					map.put("message", "伺服角度查询成功");
					map.put("code", "200");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					map.put("data", "");
					map.put("message", "伺服角度查询失败 /"+e.getMessage());
					map.put("code", "500");
					e.printStackTrace();
					return map;
				}
			 }else{
				 map.put("data", "");
				 map.put("message", "未查询到该设备的辐射计服务器信息");
				 map.put("code", "500"); 
			 }
				
				
				
				
			}
		
		
		
		
		
		
		return map;
	}
	
	
	/**
	 * 伺服控制
	 */
	public Map<String, Object> updateServo(HttpServletRequest request) {
		//辐射计objid
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		//开始或停止	0 停止,1 开始
		String cmd = request.getParameter("cmd")==null?"":request.getParameter("cmd");
		//方位角	0~360 度,精度 0.1
		String azimuth = request.getParameter("azimuth")==null?"":request.getParameter("azimuth");
		//aop	俯仰角	0~270 度,精度 0.1
		String aop = request.getParameter("aop")==null?"":request.getParameter("aop");
		
		
		 String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
			
		 Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
				System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				map.put("objid", objid);
				
				List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
				System.out.println("connlist:"+connList);
				if(connList.size()>0){ //存在关联objid
					//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
					Integer otherObjid = connList.get(0).getOtherobjid();
					System.out.println("objid:"+objid);
					System.out.println("otherobjid:"+otherObjid);
					WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(objid));
					List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
							System.out.println("urlPrifix:"+urlPrfix);
							System.out.println("webserver:"+webList.get(0));
					
					    try {
						    String controUser = "pingtai1";
							String resultData = postGeneralUrl(urlPrfix + "/api/servo",
									"objid=" + otherObjid + "&contol=" + controUser 
									+ "&cmd=" + cmd+ "&azimuth=" + azimuth
									+ "&aop=" + aop);
							//int i=5/0;
							map = (Map<String,Object>)JSON.parseObject(resultData);
							
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							map.put("data", "");
							map.put("message", "平台端伺服控制失败 /"+e.getMessage());
							map.put("code", "500");
							e.printStackTrace();
							Logger.getLogger("").error("平台端伺服控制失败 /"+e.getMessage());
							return map;
						}
					 }else{
						 map.put("data", "");
						 map.put("message", "未查询到该设备的Web服务器信息");
						 map.put("code", "500"); 
					 }
					
				}else{//不存在关联objid
					 map.put("data", "");
					 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
					 map.put("code", "500"); 
				}
				
			    
			}else { //2 - 设备端
				System.out.println(contol+":操作了本设备的伺服控制---------");
				
				WebServer fsj = new WebServer();
				fsj.setObjid(Integer.parseInt(objid));
				List<WebServer> list = siteSercie.selectFsjServer(fsj);
				//Map<String,Object> map = new HashMap<String,Object>();
				if(list.size()>0){ //有辐射计控制程序的服务器信息
					String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
					System.out.println("urlPrifix:"+urlPrfix);
					System.out.println("webserver:"+list.get(0));
			    try {
					String listResult = postGeneralUrl(urlPrfix + "/api/login",
					"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
					
					JSONObject userInfo = JSON.parseObject(listResult);
					String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
					System.out.println("listResult:"+listResult);
					System.out.println("rkey:"+rkey);
					
					
					String resultData = postGeneralUrl(urlPrfix + "/api/servo",
							"rkey=" + rkey + "&cmd=" + cmd+ "&azimuth=" + azimuth
							+ "&aop=" + aop);
					
					map.put("data",JSON.parseObject(resultData));
					map.put("message", "伺服控制成功");
					map.put("code", "200");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					map.put("data", "");
					map.put("message", "伺服控制失败 /"+e.getMessage());
					map.put("code", "500");
					e.printStackTrace();
					return map;
				}
			 }else{
				 map.put("data", "");
				 map.put("message", "未查询到该设备的辐射计服务器信息");
				 map.put("code", "500"); 
			 } 
				
				
			}
		
		
		
		return map;
	}
	
	
	
	
	/**
	 * 定向观测
	 */
	public Map<String, Object> updateDirectObs(HttpServletRequest request) {
		//辐射计objid
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		//开始或停止	0 停止,1 开始
		String cmd = request.getParameter("cmd")==null?"":request.getParameter("cmd");
		//方位角	0~360 度,精度 0.1
		String azimuth = request.getParameter("azimuth")==null?"":request.getParameter("azimuth");
		//aop	俯仰角	0~270 度,精度 0.1
		String aop = request.getParameter("aop")==null?"":request.getParameter("aop");
		
		
		 String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
			
		 Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
				System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				map.put("objid", objid);
				
				List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
				System.out.println("connlist:"+connList);
				if(connList.size()>0){ //存在关联objid
					//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
					Integer otherObjid = connList.get(0).getOtherobjid();
					System.out.println("objid:"+objid);
					System.out.println("otherobjid:"+otherObjid);
					WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(objid));
					List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
							System.out.println("urlPrifix:"+urlPrfix);
							System.out.println("webserver:"+webList.get(0));
					
					    try {
						    String controUser = "pingtai1";
							String resultData = postGeneralUrl(urlPrfix + "/api/directObs",
									"objid=" + otherObjid + "&contol=" + controUser 
									+ "&cmd=" + cmd+ "&azimuth=" + azimuth
									+ "&aop=" + aop);
							//int i=5/0;
							map = (Map<String,Object>)JSON.parseObject(resultData);
							
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							map.put("data", "");
							map.put("message", "平台端定向观测伺服控制失败 /"+e.getMessage());
							map.put("code", "500");
							e.printStackTrace();
							Logger.getLogger("").error("平台端定向观测控制失败 /"+e.getMessage());
							return map;
						}
					 }else{
						 map.put("data", "");
						 map.put("message", "未查询到该设备的Web服务器信息");
						 map.put("code", "500"); 
					 }
					
				}else{//不存在关联objid
					 map.put("data", "");
					 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
					 map.put("code", "500"); 
				}
				
			    
			}else { //2 - 设备端
				System.out.println(contol+":操作了本设备的定向观测---------");
				
				WebServer fsj = new WebServer();
				fsj.setObjid(Integer.parseInt(objid));
				List<WebServer> list = siteSercie.selectFsjServer(fsj);
				//Map<String,Object> map = new HashMap<String,Object>();
				if(list.size()>0){ //有辐射计控制程序的服务器信息
					String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
					System.out.println("urlPrifix:"+urlPrfix);
					System.out.println("webserver:"+list.get(0));
			    try {
					String listResult = postGeneralUrl(urlPrfix + "/api/login",
					"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
					
					JSONObject userInfo = JSON.parseObject(listResult);
					String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
					System.out.println("listResult:"+listResult);
					System.out.println("rkey:"+rkey);
					
					
					String resultData = postGeneralUrl(urlPrfix + "/api/directObs",
							"rkey=" + rkey + "&cmd=" + cmd+ "&azimuth=" + azimuth
							+ "&aop=" + aop);
					
					map.put("data",JSON.parseObject(resultData));
					map.put("message", "定向观测成功");
					map.put("code", "200");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					map.put("data", "");
					map.put("message", "定向观测失败 /"+e.getMessage());
					map.put("code", "500");
					e.printStackTrace();
					return map;
				}
			 }else{
				 map.put("data", "");
				 map.put("message", "未查询到该设备的辐射计服务器信息");
				 map.put("code", "500"); 
			 }
				
			}
		
	
		
		return map;
	}
	
	
	
	/**
	 * 扫描观测
	 * @param request
	 * @return
	 */
	public Map<String, Object> scanObs(HttpServletRequest request) {
		//辐射计objid
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		//开始或停止	0 停止,1 开始
		String cmd = request.getParameter("cmd")==null?"":request.getParameter("cmd");
		//扫描方式	1 垂直扫描, 2 扫描扫描, 3 全天空扫描 当 cmd 为 1 时必填,否则以下参数可选
		String scanMode = request.getParameter("scanMode")==null?"":request.getParameter("scanMode");
		//扫描次数	整数
		String scanTimes = request.getParameter("scanTimes")==null?"":request.getParameter("scanTimes");
		
		//开始方位	0~360 度,精度 0.1
		String startAzimuth = request.getParameter("startAzimuth")==null?"":request.getParameter("startAzimuth");
		
		//结束方位	0~360 度,精度 0.1	开始小于结束,垂直扫描选填
		String endAzimuth = request.getParameter("endAzimuth")==null?"":request.getParameter("endAzimuth");
		
		//方位间隔	0~360 度,精度 0.1	范围要能被间隔整除,垂直扫描选填
		String stepAzimuth = request.getParameter("stepAzimuth")==null?"":request.getParameter("stepAzimuth");
						
		//开始俯仰	0~270 度,精度 0.1
		String startAop = request.getParameter("startAop")==null?"":request.getParameter("startAop");
		
		//开始俯仰	0~270 度,精度 0.1
		String endAop = request.getParameter("endAop")==null?"":request.getParameter("endAop");
		
		//俯仰间隔	0~270 度,精度 0.1	范围要能被间隔整除,水平扫描时选填
		String stepAop = request.getParameter("stepAop")==null?"":request.getParameter("stepAop");
		
		//水平先扫	0 代表否,1 代表是	只有全天空扫描填写,其他方式选填
		String hFirst = request.getParameter("hFirst")==null?"":request.getParameter("hFirst");
		
		

		 String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
			
		 Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
				System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				map.put("objid", objid);
				
				List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
				System.out.println("connlist:"+connList);
				if(connList.size()>0){ //存在关联objid
					//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
					Integer otherObjid = connList.get(0).getOtherobjid();
					System.out.println("objid:"+objid);
					System.out.println("otherobjid:"+otherObjid);
					WebServer web = new WebServer(); //web服务器信息
					web.setObjid(Integer.parseInt(objid));
					List<WebServer> webList = siteSercie.selectWebServer(web); //Web服务器信息
					
						if(webList.size()>0){ //有设备端web程序的服务器信息
							String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
							System.out.println("urlPrifix:"+urlPrfix);
							System.out.println("webserver:"+webList.get(0));
					
					    try {
						    String controUser = "pingtai1";
							String resultData = postGeneralUrl(urlPrfix + "/api/scanObs",
									"objid=" + otherObjid + "&contol=" + controUser 
									+ "&cmd=" + cmd+ "&scanMode=" + scanMode
									+ "&scanTimes=" + scanTimes + "&startAzimuth=" + startAzimuth 
									+ "&endAzimuth=" + endAzimuth + "&stepAzimuth=" + stepAzimuth
									+ "&startAop=" + startAop + "&endAop=" + endAop + "&stepAop=" + stepAop
									+ "&hFirst=" + hFirst);
							//int i=5/0;
							map = (Map<String,Object>)JSON.parseObject(resultData);
							
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							map.put("data", "");
							map.put("message", "平台端扫描观测控制失败 /"+e.getMessage());
							map.put("code", "500");
							e.printStackTrace();
							Logger.getLogger("").error("平台端扫描观测控制失败 /"+e.getMessage());
							return map;
						}
					 }else{
						 map.put("data", "");
						 map.put("message", "未查询到该设备的Web服务器信息");
						 map.put("code", "500"); 
					 }
					
				}else{//不存在关联objid
					 map.put("data", "");
					 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
					 map.put("code", "500"); 
				}
				
			    
			}else { //2 - 设备端
				System.out.println(contol+":操作了本设备的扫描观测---------");
		
				WebServer fsj = new WebServer();
				fsj.setObjid(Integer.parseInt(objid));
				List<WebServer> list = siteSercie.selectFsjServer(fsj);
				//Map<String,Object> map = new HashMap<String,Object>();
				if(list.size()>0){ //有辐射计控制程序的服务器信息
					String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
					System.out.println("urlPrifix:"+urlPrfix);
					System.out.println("webserver:"+list.get(0));
			    try {
			    	//登录
					String listResult = postGeneralUrl(urlPrfix + "/api/login",
					"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
					
					JSONObject userInfo = JSON.parseObject(listResult);
					//登录成功后返回的rkey
					String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
					System.out.println("listResult:"+listResult);
					System.out.println("rkey:"+rkey);
					
					
					String resultData = postGeneralUrl(urlPrfix + "/api/scanObs",
							"rkey=" + rkey + "&cmd=" + cmd+ "&scanMode=" + scanMode
							+ "&scanTimes=" + scanTimes + "&startAzimuth=" + startAzimuth 
							+ "&endAzimuth=" + endAzimuth + "&stepAzimuth=" + stepAzimuth
							+ "&startAop=" + startAop + "&endAop=" + endAop + "&stepAop=" + stepAop
							+ "&hFirst=" + hFirst );
					
					map.put("data",JSON.parseObject(resultData));
					map.put("message", "扫描观测成功");
					map.put("code", "200");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					map.put("data", "");
					map.put("message", "扫描观测失败 /"+e.getMessage());
					map.put("code", "500");
					e.printStackTrace();
					return map;
				}
			 }else{
				 map.put("data", "");
				 map.put("message", "未查询到该设备的辐射计服务器信息");
				 map.put("code", "500"); 
			 }
			}
		
		
		
		return map;
	}

	/**
	 * 查询当前设备工作状态
	 * @param request
	 * @return
	 */
	public Map<String, Object> queryWorkMode(HttpServletRequest request) {
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		
		
		String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
		
		Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			map.put("objid", objid);
			
			List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
			System.out.println("connlist:"+connList);
			if(connList.size()>0){ //存在关联objid
				//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
				Integer otherObjid = connList.get(0).getOtherobjid();
				System.out.println("objid:"+objid);
				System.out.println("otherobjid:"+otherObjid);
				WebServer web = new WebServer(); //web服务器信息
				web.setObjid(Integer.parseInt(objid));
				List<WebServer> webList = siteSercie.selectWebServer(web); //设备端web程序的服务器信息
				
					if(webList.size()>0){ //有设备端web程序的服务器信息
						String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+webList.get(0));
				
				    try {
					    String controUser = "pingtai1";
						String resultData = getGeneralUrl(urlPrfix + "/api/workMode",
								"objid=" + otherObjid + "&contol=" + controUser );
						//int i=5/0;
						map = (Map<String,Object>)JSON.parseObject(resultData);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "平台端查询当前设备工作状态失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger("").error("平台端查询当前设备工作状态失败 /"+e.getMessage());
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的Web服务器信息");
					 map.put("code", "500"); 
				 }
				
			}else{//不存在关联objid
				 map.put("data", "");
				 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
				 map.put("code", "500"); 
			}
			
		    
		}else { //2 - 设备端
			System.out.println(contol+":查询了本设备的当前设备工作状态---------");
			
			WebServer fsj = new WebServer();
			fsj.setObjid(Integer.parseInt(objid));
			List<WebServer> list = siteSercie.selectFsjServer(fsj);
			//Map<String,Object> map = new HashMap<String,Object>();
			if(list.size()>0){ //有辐射计控制程序的服务器信息
				String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
				System.out.println("urlPrifix:"+urlPrfix);
				System.out.println("webserver:"+list.get(0));
		    try {
				String listResult = postGeneralUrl(urlPrfix + "/api/login",
				"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
				
				JSONObject userInfo = JSON.parseObject(listResult);
				String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
				System.out.println("listResult:"+listResult);
				System.out.println("rkey:"+rkey);
				
				
				String resultData = getGeneralUrl(urlPrfix + "/api/workMode",
						"rkey=" + rkey  );
				
				map.put("data",JSON.parseObject(resultData));
				map.put("message", "查询当前设备工作状态成功");
				map.put("code", "200");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				map.put("data", "");
				map.put("message", "查询当前设备工作状态失败 /"+e.getMessage());
				map.put("code", "500");
				e.printStackTrace();
				return map;
			}
		 }else{
			 map.put("data", "");
			 map.put("message", "未查询到该设备的辐射计服务器信息");
			 map.put("code", "500"); 
		 }
			
		}
		
		
		
		return map;
	}

    /**
     * 获取日志文件
     */
	public Map<String, Object> queryLogs(HttpServletRequest request) {
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		String startTime = request.getParameter("startTime")==null?"":request.getParameter("startTime");
		String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//如20200220
		
		if("".equals(startTime)){
			startTime=sdf.format(new Date());
		}
		System.out.println("startTime:"+startTime);
		
		
		
		Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			map.put("objid", objid);
			
			List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
			System.out.println("connlist:"+connList);
			if(connList.size()>0){ //存在关联objid
				//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
				Integer otherObjid = connList.get(0).getOtherobjid();
				System.out.println("objid:"+objid);
				System.out.println("otherobjid:"+otherObjid);
				WebServer web = new WebServer(); //web服务器信息
				web.setObjid(Integer.parseInt(objid));
				List<WebServer> webList = siteSercie.selectWebServer(web); //设备端web程序的服务器信息
				
					if(webList.size()>0){ //有设备端web程序的服务器信息
						String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+webList.get(0));
				
				    try {
					    String controUser = "pingtai1";
						String resultData = getGeneralUrl(urlPrfix + "/api/logs",
								"objid=" + otherObjid + "&contol=" + controUser+"&startTime=" +startTime );
						//int i=5/0;
						map = (Map<String,Object>)JSON.parseObject(resultData);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "平台端查询设备日志失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger("").error("平台端查询设备日志失败 /"+e.getMessage());
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的Web服务器信息");
					 map.put("code", "500"); 
				 }
				
			}else{//不存在关联objid
				 map.put("data", "");
				 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
				 map.put("code", "500"); 
			}
			
		    
		}else { //2 - 设备端
			System.out.println(contol+":查询了本设备的日志---------");
			WebServer fsj = new WebServer();
			fsj.setObjid(Integer.parseInt(objid));
			List<WebServer> list = siteSercie.selectFsjServer(fsj);
			//Map<String,Object> map = new HashMap<String,Object>();
			if(list.size()>0){ //有辐射计控制程序的服务器信息
				String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
				System.out.println("urlPrifix:"+urlPrfix);
				System.out.println("webserver:"+list.get(0));
		    try {
				String listResult = postGeneralUrl(urlPrfix + "/api/login",
				"userName=" + USER_NAME + "&passWord=" + PASS_WORD );
				
				JSONObject userInfo = JSON.parseObject(listResult);
				String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
				System.out.println("listResult:"+listResult);
				System.out.println("rkey:"+rkey);
				
				
				String resultData = getGeneralUrl(urlPrfix + "/api/logs",
						"rkey=" + rkey +"&startTime=" +startTime );
				JSONObject jsonData = JSON.parseObject(resultData);
				
				//map.put("data",jsonData);
				JSONObject message = jsonData.getJSONObject("message");
				JSONObject data = jsonData.getJSONObject("message").getJSONObject("data");
				String content = jsonData.getJSONObject("message").getJSONObject("data").getString("content");
				String newContent = content.replace("\r\n", "<br/>");
				//System.out.println("content:"+content);
				data.put("content", newContent);
				message.put("data", data);
				jsonData.put("message",message);
				
				map.put("data",jsonData);
		       //System.out.println("newContent:"+newContent);
				
				//System.out.println("--------------------------------------------:");
				//System.out.println("jsonData:"+jsonData);
				
				
				map.put("message", "获取日志文件成功");
				map.put("code", "200");
				//System.out.println("resultData:"+resultData);
				//System.out.println("logs:"+JSON.parseObject(resultData));
				//System.out.println("logsStr:"+new String(resultData.getBytes("utf-8"),"utf-8"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				map.put("data", "");
				map.put("message", "获取日志文件失败 /"+e.getMessage());
				map.put("code", "500");
				e.printStackTrace();
				return map;
			}
		 }else{
			 map.put("data", "");
			 map.put("message", "未查询到该设备的辐射计服务器信息");
			 map.put("code", "500"); 
		 }
		
		
		}
		
		
		
		return map;
	}

    /**
     * 定标
     */
	public Map<String, Object> calibrate(HttpServletRequest request) {
		//辐射计objid
				String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
				//开始或停止	0 停止,1 开始
				String cmd = request.getParameter("cmd")==null?"":request.getParameter("cmd");
				//定标方式	4 黑体定标, 5 噪声定标,6 天空斜定标,7(外定标)液氮定标
				String mode = request.getParameter("mode")==null?"":request.getParameter("mode");
				
				
				String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
				
				Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
				
				Map<String,Object> map = new HashMap<String,Object>();
				
				if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
					System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
					map.put("objid", objid);
					
					List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
					System.out.println("connlist:"+connList);
					if(connList.size()>0){ //存在关联objid
						//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
						Integer otherObjid = connList.get(0).getOtherobjid();
						System.out.println("objid:"+objid);
						System.out.println("otherobjid:"+otherObjid);
						WebServer web = new WebServer(); //web服务器信息
						web.setObjid(Integer.parseInt(objid));
						List<WebServer> webList = siteSercie.selectWebServer(web); //设备端web程序的服务器信息
						
							if(webList.size()>0){ //有设备端web程序的服务器信息
								String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
								System.out.println("urlPrifix:"+urlPrfix);
								System.out.println("webserver:"+webList.get(0));
						
						    try {
							    String controUser = "pingtai1";
								String resultData = postGeneralUrl(urlPrfix + "/api/calibrate",
										"objid=" + otherObjid + "&contol=" + controUser 
										+ "&cmd=" + cmd+ "&mode=" + mode);
								//int i=5/0;
								map = (Map<String,Object>)JSON.parseObject(resultData);
								
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								map.put("data", "");
								map.put("message", "平台端查询当前设备工作状态失败 /"+e.getMessage());
								map.put("code", "500");
								e.printStackTrace();
								Logger.getLogger("").error("平台端查询当前设备工作状态失败 /"+e.getMessage());
								return map;
							}
						 }else{
							 map.put("data", "");
							 map.put("message", "未查询到该设备的Web服务器信息");
							 map.put("code", "500"); 
						 }
						
					}else{//不存在关联objid
						 map.put("data", "");
						 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
						 map.put("code", "500"); 
					}
					
				    
				}else { //2 - 设备端
					System.out.println(contol+":操作了本设备的定标---------");
					
					WebServer fsj = new WebServer();
					fsj.setObjid(Integer.parseInt(objid));
					List<WebServer> list = siteSercie.selectFsjServer(fsj);
					//Map<String,Object> map = new HashMap<String,Object>();
					if(list.size()>0){ //有辐射计控制程序的服务器信息
						String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+list.get(0));
				    try {
						String listResult = postGeneralUrl(urlPrfix + "/api/login",
						"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
						
						JSONObject userInfo = JSON.parseObject(listResult);
						String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
						System.out.println("listResult:"+listResult);
						System.out.println("rkey:"+rkey);
						
						
						String resultData = postGeneralUrl(urlPrfix + "/api/calibrate",
								"rkey=" + rkey + "&cmd=" + cmd+ "&mode=" + mode
								);
						
						map.put("data",JSON.parseObject(resultData));
						map.put("message", "定标成功");
						map.put("code", "200");
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "定标失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的辐射计服务器信息");
					 map.put("code", "500"); 
				 }
					
					
				}
				
				
				
				
				return map;
	}


	/**
	 * 远程升级
	 */
	public Map<String, Object> update(HttpServletRequest request) {
		//辐射计objid
		String objid = (request.getParameter("objid")==null||"".equals(request.getParameter("objid")))?"-1":request.getParameter("objid");
		//待升级软件描述文件的存储地址	ftp 或者 http 格式的网址	ftp://username:password@url
		//服务端将软件升级包放到某个固定的位置,并且提供 http 下载服务(或某 ftp 文件夹下)
		//如 ftp://username:password@url 或者http://192.168.1.111:8000/xxx.txt
		String url = request.getParameter("url")==null?"":request.getParameter("url");
		
		
		
		String contol = (request.getParameter("contol")==null||"".equals(request.getParameter("contol")))?"benshebei":request.getParameter("contol");
		
		Map<String, Object> platform = siteSercie.queryPlatform(); //查询该设备是设备端还是平台端
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(platform!=null&&"1".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
			System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
			map.put("objid", objid);
			
			List<DbConnOid> connList = siteDao.selectDbConnOid(map);  //根据本地数据源的objid查找其他数据源关联的objid
			System.out.println("connlist:"+connList);
			if(connList.size()>0){ //存在关联objid
				//如果是平台端，调的是设备端的web端操作设备，而不是直接操作设备端的设备
				Integer otherObjid = connList.get(0).getOtherobjid();
				System.out.println("objid:"+objid);
				System.out.println("otherobjid:"+otherObjid);
				WebServer web = new WebServer(); //web服务器信息
				web.setObjid(Integer.parseInt(objid));
				List<WebServer> webList = siteSercie.selectWebServer(web); //设备端web程序的服务器信息
				
					if(webList.size()>0){ //有设备端web程序的服务器信息
						String urlPrfix = "http://"+webList.get(0).getIp()+":"+webList.get(0).getPort()+webList.get(0).getUrl();
						System.out.println("urlPrifix:"+urlPrfix);
						System.out.println("webserver:"+webList.get(0));
				
				    try {
					    String controUser = "pingtai1";
						String resultData = postGeneralUrl(urlPrfix + "/api/update",
								"objid=" + otherObjid + "&contol=" + controUser 
								+ "&url=" + url);
						//int i=5/0;
						map = (Map<String,Object>)JSON.parseObject(resultData);
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						map.put("data", "");
						map.put("message", "平台端远程升级设备端失败 /"+e.getMessage());
						map.put("code", "500");
						e.printStackTrace();
						Logger.getLogger("").error("平台端远程升级设备端失败 /"+e.getMessage());
						return map;
					}
				 }else{
					 map.put("data", "");
					 map.put("message", "未查询到该设备的Web服务器信息");
					 map.put("code", "500"); 
				 }
				
			}else{//不存在关联objid
				 map.put("data", "");
				 map.put("message", "该平台端没有查询到与其他数据源关联的objid");
				 map.put("code", "500"); 
			}
			
		    
		}else { //2 - 设备端
			System.out.println(contol+":操作了本设备的远程升级---------");
			
			WebServer fsj = new WebServer();
			fsj.setObjid(Integer.parseInt(objid));
			List<WebServer> list = siteSercie.selectFsjServer(fsj);
			//Map<String,Object> map = new HashMap<String,Object>();
			if(list.size()>0){ //有辐射计控制程序的服务器信息
				String urlPrfix = "http://"+list.get(0).getIp()+":"+list.get(0).getPort();
				System.out.println("urlPrifix:"+urlPrfix);
				System.out.println("webserver:"+list.get(0));
		    try {
				String listResult = postGeneralUrl(urlPrfix + "/api/login",
				"userName=" + USER_NAME + "&passWord=" + PASS_WORD);
				
				JSONObject userInfo = JSON.parseObject(listResult);
				String rkey = userInfo.getJSONObject("message").getJSONObject("data").getString("rKey");
				System.out.println("listResult:"+listResult);
				System.out.println("rkey:"+rkey);
				
				
				String resultData = postGeneralUrl(urlPrfix + "/api/update",
						"rkey=" + rkey + "&url=" + url
						);
				
				map.put("data",JSON.parseObject(resultData));
				map.put("message", "远程升级成功");
				map.put("code", "200");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				map.put("data", "");
				map.put("message", "远程升级失败 /"+e.getMessage());
				map.put("code", "500");
				e.printStackTrace();
				return map;
			}
		 }else{
			 map.put("data", "");
			 map.put("message", "未查询到该设备的辐射计服务器信息");
			 map.put("code", "500"); 
		 }
			
			
		}
		
		
		
		
		return map;
	}

	


	

	
	
	
	
	

}
