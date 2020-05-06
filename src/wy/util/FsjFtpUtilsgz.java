package wy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.domain.ParamAssis;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.domain.Threshold;
import wy.qingdao_atmosphere.countrysitedata.service.SiteDataService;
import wy.qingdao_atmosphere.countrysitedata.service.WeiBoService;
import wy.qingdao_atmosphere.onemap.dao.OneMapDao;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.util.datapersistence.service.BaseService;

@Component
public class FsjFtpUtilsgz {
	
	//新版本站点微波辐射数据同步参数(FTP)
			/*private final String FTP_IP = "60.12.107.77";
			private final int    FTP_PORT = 21;
			private final String FTP_USERNAME = "xiaoshuidian";
			private final String FTP_PASSWORD = "sysc123.,/";
			
			
			
			private final String FTP_PATH="/home/xiaoshuidian"; //文件目录
*/       
	private final String FTP_IP = "10.135.30.187";
	private final int    FTP_PORT = 21;
	private final String FTP_USERNAME = "weibo";
	private final String FTP_PASSWORD = "weibo";
	
	
	
	private final String FTP_PATH="/"; //文件目录
			
			
			
			
			
			@Autowired
			public BaseaddDao baseaddDao;

			@Autowired
			public BaseService baseService;

			@Autowired
			public SiteDataService siteDataService;

			@Autowired
			public SiteDataDao siteDataDao;

			@Autowired
			public OneMapDao oneMapDao;
			
			@Autowired
			public FKXFileUtil  fkxUtil;
			
			@Autowired 
			private WeiBoService wb;
			
			@Autowired
			private SiteDataDao  siteDao;
		
	
	/**
	 * 根据辐射计站点objid和qzbh区站编号添加微博辐射计数据（不同步到onenet上）
	 * @param qzbh 站点区站标号
	 * @param objid 站点objid
	 * @param ip ftp服务器ip
	 * @param port ftp服务器端口
	 * @param username ftp服务器用户名
	 * @param password ftp服务器密码
	 * @param ftpPath  ftp服务器文件夹根目录
	 * @return
	 */
	
	private int addFSJDataTwo_all(String qzbh,String objid,String ip,String port,String username,String password,String ftpPath) {
		//获取数据库中辐射监测数据的最新时间
		//String maxtime = baseaddDao.getMaxTimeByDtid("yyyyMMddHH24MIss", "5", "2870");
		//System.out.println("maxtime:"+maxtime);

		//连接FTP服务器
		//FTPClient ftpClient = FtpUtil.ftpConnection(FTP_IP, FTP_PORT, FTP_USERNAME, FTP_PASSWORD);
		
		FTPClient ftpClient = FtpUtil.ftpConnection(ip, Integer.parseInt(port), username, password);
		 //System.out.println("before FtpUtil.getFileList ");
		//获取根目录下2小时之内的所有辐射数据
		Map<String, List<String>> fileNameMap = getlsFileList(ftpClient,2,qzbh,objid,ftpPath);
		//System.out.println("fileNameMap.size()"+fileNameMap.size());
       // System.out.println("after FtpUtil.getFileList ");
		//断开FTP服务,防止FTP超时报异常
		FtpUtil.ftpClose(ftpClient);

		//获取各种数据文件中的内容列表(格式：站点编号,时间,监测值)
		List<String> jcList = fileNameMap.get("jcList");//基础数据内容列表
		List<String> fyList = fileNameMap.get("fyList");//反演数据内容列表
		List<String> wdList = fileNameMap.get("wdList");//稳定系统数据内容列表
		List<String> ztList = fileNameMap.get("ztList");//状态数据内容列表
		List<String> kxList = fileNameMap.get("kxList");//廓线数据内容列表
		
		System.out.println("通过筛选后的jcList.size:"+jcList.size());
        System.out.println("通过筛选后的fyList.size:"+fyList.size());
        System.out.println("通过筛选后的wdList.size:"+wdList.size());
        System.out.println("通过筛选后的ztList.size:"+ztList.size());
        System.out.println("通过筛选后的kxList.size:"+kxList.size());

		//参数Map
		Map<String, Object> paramMap = new HashMap<String, Object>();
		//获取站点对象
		paramMap.put("objtypeid", 12);
		paramMap.put("objid", objid);//新加的，多个站点，按objid查
		List<Map<String,Object>> sitelist = baseService.selectForCpAttachInfoStoreTwo(paramMap, "");
		Map<String, Map<String,Object>> siteMap = new LinkedHashMap<String, Map<String,Object>>();
		if (sitelist.size() > 0) {
			for (Map<String,Object> site : sitelist) {
				siteMap.put(site.get("sitenumber").toString(), site);
			}
		}

		//获取参数辅助对象
		List<ParamAssis> parAssList = siteDataService.getParamAssisByDtid("5",objid);//43 objid新加的，多个站点下查
		Map<String, ParamAssis> pMap = new LinkedHashMap<String, ParamAssis>();
		if (parAssList.size() > 0) {
			for (ParamAssis parAss : parAssList) {
				pMap.put(parAss.getObjid() + "_" + parAss.getParamname(), parAss);
			}
		}

		//监测数据对象集合
		List<SiteData> dataList = new ArrayList<SiteData>();

		//基础数据封装并同步到OneNet
		String[] jcparam = {"站点编号","监测时间","jc_hwlw","jc_jyzt","jc_dmwd","jc_dmsd","jc_dmyq",
				"jc_fwjd","jc_fyjd","jc_fs","jc_fx",
				"jc_k1","jc_k2","jc_k3","jc_k4","jc_k5","jc_k6","jc_k7","jc_k8",
				"jc_v1","jc_v2","jc_v3","jc_v4","jc_v5","jc_v6","jc_v7","jc_v8"};
		insertDataListTwo(jcList, siteMap, pMap, dataList, jcparam,qzbh); //区站编号参数时为了测试新加的，这样貌似也可以，到时改不改看心情吧
		//System.out.println("jc_dataList.size:"+dataList.size());
		
		//反演数据封装不同步到OneNet
		String[] fyparam = {"站点编号","监测时间","fy_sqzhl","fy_ljytszhl","fy_ydgd","fy_hwlw","fy_fwjd","fy_fyjd"};
		insertDataListTwo(fyList, siteMap, pMap, dataList, fyparam,qzbh); //区站编号参数时为了测试新加的，这样貌似也可以，到时改不改看心情吧
		//System.out.println("fy_dataList.size:"+dataList.size());
		
		//稳定系统数据封装并不同步到OneNet
		String[] wdparam = {"站点编号","监测时间","wd_AI","wd_KI","wd_TTI","wd_LI","wd_SI","wd_LCL","wd_LFC","wd_EC","wd_BLH"};
		insertDataListTwo(wdList, siteMap, pMap, dataList, wdparam,qzbh); //区站编号参数时为了测试新加的，这样貌似也可以，到时改不改看心情吧
		//System.out.println("wd_dataList.size:"+dataList.size());

		//状态数据封装不同步到OneNet
		String[] ztparam = {"站点编号","监测时间","zt_jd","zt_wd","zt_status"};
		insertDataListTwo(ztList, siteMap, pMap, dataList, ztparam,qzbh);//区站编号参数时为了测试新加的，这样貌似也可以，到时改不改看心情吧
		//System.out.println("zt_dataList.size:"+dataList.size());
		
		//同步状态报警信息
		wbthrEntity(ztList, siteMap, pMap, ztparam,qzbh); //区站编号参数时为了测试新加的，这样貌似也可以，到时改不改看心情吧

		//廓线数据封装并不同步到OneNet
		insertDataListTwo(kxList, siteMap, pMap, dataList, null,qzbh);//区站编号参数时为了测试新加的，这样貌似也可以，到时改不改看心情吧
		//System.out.println("即将进入adddata了");
		System.out.println("kx_dataList.size:"+dataList.size());
		System.out.println("dataList:"+dataList);
		int status = 0;
		if (dataList.size() > 0) {
			System.out.println("进入adddata了");
			//未分表时的插入监测数据
			//status = siteDataService.addCollectData(dataList);
			//分表后的插入监测数据
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("list", dataList);
			map.put("objid", objid);
			status = siteDao.insertCollect(map);
		}
		//查一下此时辐射计该站点的最新时间
		//String maxTime = baseaddDao.getMaxTimeByDtid("yyyy-MM-dd HH24:MI:ss", "5", objid);
		
		//insertFsjTimeStamp(objid);  //插入该站点最新数据的时间戳
	   
		return status;
	}
	
	
	
	 /**
     * 取ftp上的微波辐射文件列表
     * @param FTPClient
     * @param hour 取几小时的数据
     * @param qzbh 辐射计站点区站编号
     * @param objid 辐射计站点objid
     * @return fileMap
     */
    public  Map<String, List<String>> getlsFileList(FTPClient ftpClient, int hour, String qzbh,String objid,String ftpPath) {
    	
    	 //System.out.println("in FtpUtil.getFileList ");
    	
    	String paramid="87,88,89,90,91,49,50";  
		
		//String[] colMinMaxTime = wb.colMinMaxTimeByHour("5", 1, objid, paramid, "");
    	//获取数据库内该设备的最新时间，如果为null 则说明没有数据，同步所有的数据 ,如果不为null,说明有数据了则获取规定时间内的文件再进行比较同步就行
		String maxtime = baseaddDao.getMaxTimeByDtOtid("yyyy-MM-dd HH24:MI:ss", "5", paramid, "", objid);
		
		
    	//获取两小时之内的数据
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		//calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-hour);//取几小时的数据
		calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-60);//取FTP上30分钟的数据
		String time = sdf.format(calendar.getTime());
    	
		//创建map，用于返回
        Map<String, List<String>> fileMap = new LinkedHashMap<String, List<String>>();
        
        // 获得指定目录下所有文件名
        FTPFile[] ftpFiles = null;
        try {
        	//ftpFiles = ftpClient.listFiles("/home/xiaoshuidian");
        	//ftpFiles = ftpClient.listFiles(FTP_PATH);
        	
        	ftpFiles = ftpClient.listFiles(ftpPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("刚从ftp读下来的文件个数:"+ftpFiles.length);
        List<String> jcList = new ArrayList<String>();//基础数据
		List<String> fyList = new ArrayList<String>();//反演数据
		List<String> wdList = new ArrayList<String>();//稳定系统数据
		List<String> ztList = new ArrayList<String>();//状态数据
		List<String> kxList = new ArrayList<String>();//廓线数据
		
		if(maxtime==null){ //没有数据，说明第一次同步，则在读取ftp文件的时候不再做时间过滤操作，全部读取
			 for (int i = 0; ftpFiles != null && i < ftpFiles.length; i++) {
		            FTPFile file = ftpFiles[i];
		            if (file.isFile()&&file.getSize()>0) {//不是空文件
		            	if(file.getName().startsWith(qzbh)){ //丽水站点编号 此处应该是动态的qzbh，但目前没数据，为了测试，先写死
		            	//if (file.getName().split("_")[1].compareTo(time) > 0) {  //此处应该是>号，由于没数据此时为了测试写的<
		            		System.out.println("maxtime=null，说明第一次同步");
		            		if (file.getName().contains("_JC")) {
		            			jcList.add(file.getName());
		            		} else if (file.getName().contains("_FY")) {
		            			fyList.add(file.getName());
		            		} else if (file.getName().contains("_WDD")) {
		            			wdList.add(file.getName());
		            		} else if (file.getName().contains("_ZT")) {
		            			ztList.add(file.getName());
		            		} else if (file.getName().contains("_KX")) {
		            			kxList.add(file.getName());
		            		}
		    			//}
		            	}
		            }
		        }
		}else{//说明有数据，对ftp上的文件做一下时间过滤操作
			 for (int i = 0; ftpFiles != null && i < ftpFiles.length; i++) {
		            FTPFile file = ftpFiles[i];
		            if (file.isFile()&&file.getSize()>0) {//不是空文件
		            	if(file.getName().startsWith(qzbh)){ //丽水站点编号 此处应该是动态的qzbh，但目前没数据，为了测试，先写死
		            	if (file.getName().split("_")[1].compareTo(time) > 0) {  //此处应该是>号，由于没数据此时为了测试写的<
		            		System.out.println("进入时间>0了");
		            		if (file.getName().contains("_JC")) {
		            			jcList.add(file.getName());
		            		} else if (file.getName().contains("_FY")) {
		            			fyList.add(file.getName());
		            		} else if (file.getName().contains("_WDD")) {
		            			wdList.add(file.getName());
		            		} else if (file.getName().contains("_ZT")) {
		            			ztList.add(file.getName());
		            		} else if (file.getName().contains("_KX")) {
		            			kxList.add(file.getName());
		            		}
		    			}
		            	}
		            }
		        }
		}
        
       
       // System.out.println("刚从ftp读下来的jcList.size:"+jcList.size());
       // System.out.println("刚从ftp读下来的fyList.size:"+fyList.size());
       // System.out.println("刚从ftp读下来的wdList.size:"+wdList.size());
       // System.out.println("刚从ftp读下来的ztList.size:"+ztList.size());
       // System.out.println("刚从ftp读下来的kxList.size:"+kxList.size());
      //未分表的获取数据库中辐射监测数据的最新时间
       //String dbMaxTime = baseaddDao.getMaxTimeByDtid("yyyyMMddHH24MIss", "5", objid);
        //分表后的获取数据库中辐射监测数据的最新时间
       String dbMaxTime = siteDao.getMaxTimeByDtid_three("yyyyMMddHH24MIss", "5", objid);
       
      // System.out.println("maxtime:"+dbMaxTime);
        
       // System.out.println("刚传进来的dbMaxtime:"+dbMaxTime);
        fileMap.put("jcList", readFile(ftpClient,getFileListByTime(jcList,dbMaxTime)));
        fileMap.put("fyList", readFile(ftpClient,getFileListByTime(fyList,dbMaxTime)));
        fileMap.put("wdList", readFile(ftpClient,getFileListByTime(wdList,dbMaxTime)));
        fileMap.put("ztList", readFile(ftpClient,getFileListByTime(ztList,dbMaxTime)));
        fileMap.put("kxList", readFile(ftpClient,getFileListByTime(kxList,dbMaxTime)));
        
        return fileMap;
    }
    
    
    /**
     * 获取大于数据库时间的数据
     * @param goupList 分组后的文件名列表
     * @param dbMaxTime 数据库最大时间
     * @return fileLists
     */
    public  List<String> getFileListByTime(List<String> goupList, String dbMaxTime) {
    	List<String> groupFileList = new ArrayList<String>();
    	for (String filename : goupList) {
    		if ((dbMaxTime != null) && (!"".equals(dbMaxTime))) {
    			//System.out.println("进入if分支：dbMaxtime!=null:"+dbMaxTime);
    			if (filename.split("_")[1].compareTo(dbMaxTime) > 0) {
    				groupFileList.add(filename);
    			}
			} else {
				//System.out.println("进入if分支：dbMaxtime=null:"+dbMaxTime);
				groupFileList.add(filename);
			}
			
		}
    	
    	return groupFileList;
    }
    
    
    /**
	 * 取ftp上的文件内容
	 * @param FTPClient
	 * @param fileLists
	 * @return list
	 */
   public  List<String> readFile(FTPClient ftpClient, List<String> filelist){
	   System.out.println("进入readfile 方法，fileList.size="+filelist.size());
	   List<String> list = new ArrayList<String>();
	   
	   if (filelist.size() > 0) {
		   System.out.println("readfile fileList.size>0"+filelist.size());
		   for (String filename : filelist) {
			  System.out.println("readfile -filename"+filename);
			   InputStream ins = null;
			   StringBuilder builder = null;
			   try {
				   
				   // 从服务器上读取指定的文件
				   ins = ftpClient.retrieveFileStream(filename);
				   BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
				   String line;
				   builder = new StringBuilder(150);
				   while ((line = reader.readLine()) != null) {
					   builder.append(line.replaceAll("\\p{C}", ""));//去除不可见字符
					   builder.append(";");//多组数据用分号隔开
					   //builder.append("\r\n");
				   }
				   reader.close();
				   if (ins != null) {
					   ins.close();
				   }
				   // 主动调用一次getReply()把接下来的226消费掉. 这样做是可以解决这个返回null问题
				   ftpClient.getReply();
			   } catch (Exception e) {
				   System.out.println("文件读取失败");
				   e.printStackTrace();
			   }
			   
			   String text = builder.toString().replace(";;NNNN", "").replace(";;", ";");
			   String content ;   
			   if(text.length()>0) {//不为空
				   content = text.substring(0, text.length()-1); 
			   }else {
				   content =text.substring(0, text.length());  
			   }
			   if (content.contains(";")) {
				   String[] conArr = content.split(";");
				   for (int i = 0; i < conArr.length; i++) {
					   list.add(filename.split("_")[0]+","+conArr[i]);
				   }
			   } else {
				   list.add(filename.split("_")[0]+","+content);
			   }
		   }
	   }
	   return list;
    }
   
   
   /**
	 * 将数据封装到监测数据对象、不同步到OneNet
	 * @param ftpFileList 文件内容列表
	 * @param siteMap 站点对象
	 * @param pMap 参数辅助对象
	 * @param dataList 监测数据对象集合
	 * @param qzbh 区站编号，数据不够，此参数时此时为了测试而加，正式环境可去掉
	 * @param paramArr
	 */
	private void insertDataListTwo(List<String> ftpFileList,Map<String, Map<String, Object>> siteMap,
			Map<String, ParamAssis> pMap, List<SiteData> dataList,String[] paramArr,String qzbh) {
		if (paramArr != null) {
			for (String ftpFile : ftpFileList) {
				String[] fileArr = ftpFile.split(",");
				//if (siteMap.containsKey(fileArr[0])) {//这是原来的写法，是对的  
				//System.out.println("fileArr.length:"+fileArr.length);
				
				
				if (siteMap.containsKey(qzbh)) {//这是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
					//OneNet数据集合
					//List<Datapoints> onenetList = new ArrayList<Datapoints>();

					for (int i = 2, length = fileArr.length; i < length; i++) {
						//以下是原来的写法，是对的  
						//String mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_" + paramArr[i];
						//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
						String mapKey = siteMap.get(qzbh).get("objid").toString() + "_" + paramArr[i];
						if (pMap.containsKey(mapKey)) {
							//数据库封装
							SiteData sd = new SiteData();
							sd.setDataguid(pMap.get(mapKey).getDataguid());
							sd.setDatavalue(fileArr[i]);
							sd.setCollecttime(fileArr[1]);
							dataList.add(sd);

							//同步到OneNet
							/*List<Data> dl = new ArrayList<Data>();
							dl.add(new Data(fileArr[1].replace(" ", "T"), fileArr[i]));
							onenetList.add(new Datapoints(paramArr[i], dl));*/

						};
					}

					//同步到OneNet
					/*Map<String, List<Datapoints>> onmap = new HashMap<String, List<Datapoints>>();
					onmap.put("datastreams", onenetList);

					ParamAssis pa = pMap.get(siteMap.get(fileArr[0]).get("objid").toString() + "_" + paramArr[2]);
					String devid = pa.getDevicenumber();
					String key = pa.getRmk1();

					AddDatapointsApi api = new AddDatapointsApi(onmap, null, null, devid, key);
					BasicResponse<Void> response = api.executeApi();
					Logger.getLogger("").info("errno:"+response.errno+" error:"+response.error);*/
				}
			}
		} else {
			String[] kxflag = {"wd","xdsd","sqmd","zsl","bjcwd"};
			String[] kxparam0 = { //包含类型(1:温度廓线2:相对湿度廓线3:水汽密度廓线4:折射率廓线)
					"站点编号","监测时间","_type","_fc",
					"_0","_100","_200","_300","_400","_500","_600","_700","_800","_900",
					"_1000","_1100","_1200","_1300","_1400","_1500","_1600","_1700","_1800","_1900",
					"_2000","_2250","_2500","_2750","_3000","_3250","_3500","_3750","_4000","_4250",
					"_4500","_4750","_5000","_5250","_5500","_5750","_6000","_6250","_6500","_6750",
					"_7000","_7250","_7500","_7750","_8000","_8250","_8500","_8750","_9000","_9250",
					"_9500","_9750","_10000"
			};
			String[] kxparam1 = {//包含类型(5:边界层温度廓线)
					"站点编号","监测时间","_type","_fc","_0","_25","_50","_75","_100",
					"_125","_150","_175","_200","_225","_250","_275","_300","_325",
					"_350","_375","_400","_425","_450","_475","_500","_550","_600",
					"_650","_700","_750","_800","_850","_900","_950","_1000","_1100",
					"_1200","_1300","_1400","_1500","_1600","_1700","_1800","_1900","_2000"
			};

			for (String ftpFile : ftpFileList) {
				String[] fileArr = ftpFile.split(",");
				//if (siteMap.containsKey(fileArr[0])) {原来的
				if (siteMap.containsKey(qzbh)) {//这是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
					//OneNet数据集合
					//List<Datapoints> onenetList = new ArrayList<Datapoints>();

					String mapKey = "";
					StringBuilder datavalue = new StringBuilder();
					datavalue.append("{");
					if ("1".equals(fileArr[2])) {
						//原来的
						//mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_kx_wdkx";
						//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
						 mapKey = siteMap.get(qzbh).get("objid").toString() + "_kx_wdkx";
						for (int i = 2, length = fileArr.length; i < length; i++) {
							if (i != length - 1) {
								datavalue.append("\""+kxflag[0]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\",");
							} else {
								datavalue.append("\""+kxflag[0]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\"}");
							}
						}
					} else if ("2".equals(fileArr[2])){
						//原来的
						//mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_kx_xdsdkx";
						//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
						mapKey = siteMap.get(qzbh).get("objid").toString() + "_kx_xdsdkx";
						for (int i = 2, length = fileArr.length; i < length; i++) {
							if (i != length - 1) {
								datavalue.append("\""+kxflag[1]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\",");
							} else {
								datavalue.append("\""+kxflag[1]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\"}");
							}
						}
					} else if ("3".equals(fileArr[2])){
						//原来的
						//mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_kx_sqmdkx";
						//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
						mapKey = siteMap.get(qzbh).get("objid").toString() + "_kx_sqmdkx";
						for (int i = 2, length = fileArr.length; i < length; i++) {
							if (i != length - 1) {
								datavalue.append("\""+kxflag[2]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\",");
							} else {
								datavalue.append("\""+kxflag[2]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\"}");
							}
						}
					} else if ("4".equals(fileArr[2])){
						//原来的
						//mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_kx_zslkx";
						//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
						mapKey = siteMap.get(qzbh).get("objid").toString() + "_kx_zslkx";
						for (int i = 2, length = fileArr.length; i < length; i++) {
							if (i != length - 1) {
								datavalue.append("\""+kxflag[3]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\",");
							} else {
								datavalue.append("\""+kxflag[3]+kxparam0[i]+"\":" + "\"" + fileArr[i] + "\"}");
							}
						}
					} else if ("5".equals(fileArr[2])){
						//原来的
						//mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_kx_bjcwdkx";
						//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
						mapKey = siteMap.get(qzbh).get("objid").toString() + "_kx_bjcwdkx";
						for (int i = 2, length = fileArr.length; i < length; i++) {
							if (i != length - 1) {
								datavalue.append("\""+kxflag[4]+kxparam1[i]+"\":" + "\"" + fileArr[i] + "\",");
							} else {
								datavalue.append("\""+kxflag[4]+kxparam1[i]+"\":" + "\"" + fileArr[i] + "\"}");
							}
						}
					}

					//封装数据
					if (pMap.containsKey(mapKey)) {
						//数据库
						SiteData sd = new SiteData();
						sd.setDataguid(pMap.get(mapKey).getDataguid());
						sd.setDatavalue(datavalue.toString());
						sd.setCollecttime(fileArr[1]);
						dataList.add(sd);

						//OneNet
						/*List<Data> dl = new ArrayList<Data>();
						dl.add(new Data(fileArr[1].replace(" ", "T"), datavalue.toString()));
						onenetList.add(new Datapoints(pMap.get(mapKey).getParamname(), dl));*/

						//同步到OneNet
						/*Map<String, List<Datapoints>> onmap = new HashMap<String, List<Datapoints>>();
						onmap.put("datastreams", onenetList);

						AddDatapointsApi api = new AddDatapointsApi(onmap, null, null, pMap.get(mapKey).getDevicenumber(), pMap.get(mapKey).getRmk1());
						BasicResponse<Void> response = api.executeApi();
						Logger.getLogger("").info("errno:"+response.errno+" error:"+response.error);*/
					}
				}
			}
		}

	}
	
	
	/**
	 * 封装微波辐射报警数据
	 * @param ftpFileList 文件内容列表
	 * @param siteMap 站点对象
	 * @param pMap 参数辅助对象
	 * @param paramArr
	 * @param qzbh 为了测试而加的参数  区站编号
	 */
	public void wbthrEntity(List<String> ftpFileList,Map<String, Map<String, Object>> siteMap, Map<String, ParamAssis> pMap,String[] paramArr,String qzbh) {

		Map<String, Object> outmap = new LinkedHashMap<String, Object>(); //外层判断优化程序
		Map<Integer, Object> inmap = new LinkedHashMap<Integer, Object>(); 

		for (String ftpFile : ftpFileList) {
			String[] fileArr = ftpFile.split(",");
			//if (siteMap.containsKey(fileArr[0])) { //原来的
			if (siteMap.containsKey(qzbh)) {//这是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
				//原来的
				//String mapKey = siteMap.get(fileArr[0]).get("objid").toString() + "_" + paramArr[paramArr.length-1];
				//以下是为了测试，而新加的qzbh参数，正式环境可不用（用原来的），虽然貌似这样也可以
				String mapKey = siteMap.get(qzbh).get("objid").toString() + "_" + paramArr[paramArr.length-1];
				if (pMap.containsKey(mapKey)) {

					//设备状态报警存入数据库
					if (!"0".equals(fileArr[paramArr.length-1])) {
						if (outmap.containsKey(fileArr[paramArr.length-1])) {
							continue;
						} else {
							outmap.put(fileArr[paramArr.length-1], fileArr[paramArr.length-1]);
							//参数1：dataguid	参数2：时间	参数3：值	参数4：故障map
							addThreshold(pMap.get(mapKey).getDataguid(), fileArr[1], fileArr[paramArr.length-1], inmap);
						}

					}
				};
			}
		}
	}
	
	/**
	 * 将微波辐射报警值存入数据库
	 * @param dataguid		(对象类型id_objid_设备编号_参数id)
	 * @param collecttime	监测时间
	 * @param value			状态值
	 * @param map			故障map,用于判断是否存在故障
	 */
	public void addThreshold (String dataguid, String collecttime, String value,Map<Integer, Object> map) {

		//将设备状态值转成二进制数
		String status = Integer.toBinaryString(Math.abs(Integer.parseInt(value)));

		//去除符号位，不足补零
		while(status.length() < 7){
			status = "0"+status;
		}

		//反转
		String reStatus = new StringBuffer(status).reverse().toString();
		if (reStatus.length() > 7) {
			reStatus = reStatus.substring(0,7);
		}

		//遍历二进制数
		for (int i = 0; i < reStatus.length(); i++) {
			String sc = String.valueOf(reStatus.charAt(i));
			if ("1".equals(sc)) {
				String[] dgid = dataguid.split("_");
				List<Threshold> list = new ArrayList<Threshold>();
				if (i == 0) {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "设备GPS异常","GPS");
						list.add(thr);
					}
				} else if (i == 1) {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "上位机存储异常","上位机存储");
						list.add(thr);
					}
				} else if (i == 2) {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "下位机存储异常","下位机存储");
						list.add(thr);
					}
				} else if (i == 3) {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "上下位机网络通讯异常","上下位机网络通讯");
						list.add(thr);
					}
				} else if (i == 4) {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "CAN通讯异常","CAN通讯");
						list.add(thr);
					}
				} else if (i == 5) {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "温控异常","温控");
						list.add(thr);
					}
				} else {
					if (map.containsKey(i)) {
						continue;
					} else {
						map.put(i, i);
						Threshold thr = new Threshold(dgid[1], collecttime, dgid[3], sc, "0", "设备供电异常","设备供电");
						list.add(thr);
					}
				}

				if (list.size() > 0) {
					siteDataDao.addWbfsThrinfo(list);
				}
			}

		}
	}
	
	
	
	/**
	 * 重写丽水辐射计文件生成艾玛图
	 * @param qzbh 区站编号
	 * @param objid objid
	 * @return
	 */
  public String fsjReWrite(String qzbh,String objid) {
		
		//必传
		//String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
	    //String objid = "2780";
	    String paramid="87,88,89,90,91,49,50";
		//获取数据库辐射数据近1小时时间
		String[] colMinMaxTime = wb.colMinMaxTimeByHour("5", 1, objid, paramid, "");
		
		//按时间查询 格式：yyyy-MM-dd HH:mm:ss
		//String begintime = (request.getParameter("begintime") == null || "".equals(request.getParameter("begintime"))) ? colMinMaxTime[0] : request.getParameter("begintime");
		//String endtime = (request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"))) ? colMinMaxTime[1] : request.getParameter("endtime");
		String begintime = colMinMaxTime[0];
		String endtime = colMinMaxTime[1];
		
		Map<String,Object> paramap = new HashMap<String,Object>();
		paramap.put("objid", objid);
		Map<String,Object> zmap = siteDataDao.getDataguidByOid(paramap);//通过objid查询辐射计站点dataguid相关参数
		//dataguid前缀
		String dataguid= "";
		
		if(zmap!=null){
			//dataguid前缀
			 dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("begintime", begintime);
		map.put("endtime", endtime);
		map.put("objid", objid);
		map.put("dataguid", dataguid);
       //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> dataList = siteDataDao.queryWeiBoDate(map); //从数据库获取实时的（最近1个小时）微波辐射计数据
		if(dataList!=null&&dataList.size()>0){
			Map<String,Object> smap = reWriteFSJ(dataList,qzbh,objid,dataguid);  //重写从数据库获取出来的监测数据生成新的txt文件调用客户提供的exe生成艾玛图
		}
		
		return  "success";
	}
	
	
	/**
	 * 重写从数据库获取出来的监测数据生成新的txt文件调用客户提供的exe生成艾玛图
	 * @param dataList 从数据库查出来的辐射计监测数据
	 * @param qzbh 区站编号
	 * @param dataguid  dataguid前缀   如：12_2780_7786269_
	 * @param objid  objid
	 * @return
	 */
	public Map<String, Object> reWriteFSJ(
			List<Map<String, Object>> dataList,String qzbh,String objid,String dataguid) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Map<String,Object> map = dataList.get(0); //只取第一条，最新的数据
			if(map!=null){
			
			PrintWriter out = null;
			String collecttime =  map.get("collecttime").toString();
			//System.out.println("原始的collecttime:"+collecttime);
			String time = collecttime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
			//System.out.println("去除-和：还有空格后的时间:"+time);
			//System.out.println("kx_wdkx.value:"+map.get("kx_wdkx").toString());
			//水平风向Map
			LinkedHashMap<String, Object> wdkxMap =  JSON.parseObject(map.get("kx_wdkx").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
			//水平风速Map
			LinkedHashMap<String, Object> xdsdkxMap =  JSON.parseObject(map.get("kx_xdsdkx").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
			
			
			Set<Entry<String, Object>> wdkxSet = wdkxMap.entrySet(); //温度廓线Map
			Set<Entry<String, Object>> xdsdkxSet = xdsdkxMap.entrySet(); //相对湿度廓线Map
			
			//System.out.println("wdkxSet.size:"+wdkxSet.size());
			//System.out.println("xdsdkxSet.size:"+xdsdkxSet.size());
			StringBuilder wdBuild = new StringBuilder();
			StringBuilder sdBuild = new StringBuilder();
			StringBuilder dmyqBuild = new StringBuilder();
			//添加温度廓线
			for(Entry<String, Object> wdkx:wdkxSet){
				if(!"wd_fc".equals(wdkx.getKey())&&!"wd_type".equals(wdkx.getKey())){
					
					//System.out.println("wdkx.getKey():"+wdkx.getKey());
					//System.out.println("wdkx.getValue():"+wdkx.getValue().toString());
					wdBuild.append(wdkx.getValue().toString()).append(",");
					
				}
				
			}
			//添加湿度廓线
			for(Entry<String, Object> sdkx:xdsdkxSet){
				if(!"xdsd_fc".equals(sdkx.getKey())&&!"xdsd_type".equals(sdkx.getKey())){
					
					//System.out.println("sdkx.getKey():"+sdkx.getKey());
					//System.out.println("sdkx.getValue():"+sdkx.getValue().toString());
					sdBuild.append(sdkx.getValue().toString()).append(",");
					
				}
				
			}
			
			//System.out.println("温度廓线："+wdBuild.toString());
			//System.out.println("湿度廓线："+sdBuild.toString());
			
			/*Map<String,Object> paramap = new HashMap<String,Object>();
			paramap.put("objid", objid);
			Map<String,Object> zmap = siteDataDao.getDataguidByOid(paramap);//通过objid查询辐射计站点dataguid相关参数
			String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";*/
			
			Map<String,Object> smap = new HashMap<String,Object>(); 
			smap.put("collecttime", collecttime);
			//smap.put("dataguid", "12_2780_7786269_");
			smap.put("dataguid", dataguid);
			String dmyq = siteDataDao.queryDmyqBytime(smap);
			if(dmyq==null||"".equals(dmyq)||"null".equals(dmyq)){
				dmyq="1009.2";
			}
			dmyqBuild.append(dmyq);
			for(int i=0;i<52;i++){
				dmyqBuild.append(",0");
			}
			//System.out.println("地面压强："+dmyqBuild.toString());
			
			String dirpath = "E:\\indata\\FSJ"+qzbh;
			File dir = new File(dirpath);
            if(!dir.exists()){
            	dir.mkdirs();
            }
            String fileName = qzbh+"_"+time+"_aima.txt";
			try {
				out=new PrintWriter(new File(dirpath+"\\"+fileName));
				out.println(wdBuild.toString().substring(0,wdBuild.toString().length()-1)); //去掉最后一个逗号   温度廓线
				out.println(sdBuild.toString().substring(0,sdBuild.toString().length()-1)); //去掉最后一个逗号   湿度廓线
				out.println(dmyqBuild.toString()); //地面压强
				
				out.flush();//刷新，将文本写入文件里
				File file = new File(dirpath+"\\"+fileName);
				if(file.length()<=0){//如果文件为空
					//file.delete();//删掉此文件
					out.println(0);
				}
			} catch (FileNotFoundException e) {
				System.out.println("找不到此文件");
				e.printStackTrace();
			}finally{
				if(out!=null){
					out.close();
				}
			}
			}
			
		
		return resultMap;
	}
	
	
	    /**
	     * 从ftp上同步丽水辐射计数据到数据库里
	     * @param qzbh 站点区站标号
	     * @param objid 站点objid
	     * @param ip ftp服务器ip
	     * @param port ftp服务器端口
	     * @param username ftp服务器用户名
	     * @param password ftp服务器密码
	     * @param ftpPath  ftp服务器文件夹根目录
	     */
	  public void tbFtpFsj(String qzbh,String objid,String ip,String port,String username,String password,String ftpPath){
		  Logger.getLogger("").info("-----------站点"+objid+"_"+qzbh+"微波辐射数据同步任务开始-------------");

			try {
				long begin = System.currentTimeMillis();

				//同步辐射数据(到onenet)
				//int status = addFSData();
				//同步辐射数据(不到onenet)
				int status = addFSJDataTwo_all(qzbh,objid,ip,port,username,password,ftpPath);

				long end = System.currentTimeMillis();
				if (status > 0) {
					Logger.getLogger("").info("站点"+objid+"微波辐射数据同步任务结束，共耗时：[" + (end - begin)/1000 + "]秒");
				} else {
					Logger.getLogger("").info("站点"+objid+"微波辐射数据已经是最新数据");
				}

				//删除72小时之前的辐射数据
				//delBefore72();
				delBeforeYear(1,objid); //删除1年前的数据

			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger("").error("站点"+objid+"微波辐射数据同步服务异常");
			}
			
			//重写辐射计图片生成艾玛图
					try{
					 //Map<String,Object> map =new HashMap<String,Object>();

					 fsjReWrite(qzbh, objid);
					}catch(Exception e){
						e.printStackTrace();
						//Logger.getLogger("").error("重写丽水辐射计文件出错");
						Logger.getLogger("").error("站点"+objid+"重写辐射计文件出错");
					}
				//插入该站点最新数据的时间戳
				try{
				 //Map<String,Object> map =new HashMap<String,Object>();

					insertFsjTimeStamp(objid);  //插入该站点最新数据的时间戳
				}catch(Exception e){
					e.printStackTrace();
					//Logger.getLogger("").error("重写丽水辐射计文件出错");
					Logger.getLogger("").error("站点"+objid+"插入该站点最新数据的时间戳出错");
				}
					
					
	  }
	  
	  
	  
	  /**
		 * 删除多少年前的数据
		 * @param year 多少年
		 * @param objid 站点objid
		 */
		public void delBeforeYear(int year,String objid){
			try {
				//获取数据库中辐射监测数据的最新时间
				//String maxtime = baseaddDao.getMaxTimeByDtid("yyyy-MM-dd HH24:MI:ss", "5", "");
				//删除72小时之前的辐射数据
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				/*if (maxtime != null && "".equals(maxtime)) {
					date = sdf.parse(maxtime);
				}*/
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-year);//一年之前的数据
				String collecttime = sdf.format(calendar.getTime());
				System.out.println(year+"年前的时间为："+sdf.format(calendar.getTime()));
				//删除year年之前的辐射监测数据
				int num = siteDataService.delCollData_two(collecttime, objid);
				Logger.getLogger("").info("-----------"+year+"年前的设备数据删除成功，共删除"+num+"条数据-------------");
			} catch (Exception e) {
				Logger.getLogger("").info("站点微波辐射数据删除异常");
			}
		}
	  
	  
	  
	  /**
		 * 插入某站点更新的最新数据的时间戳
		 * @param objid
		 */
		public void insertFsjTimeStamp(String objid){
			Map<String,Object> zmap = new HashMap<String,Object>();
			 
			   zmap.put("objid", objid);
			   Map<String,Object> vmap = siteDao.getDataguidByOid(zmap);//通过objid查询风廓线站点dataguid相关参数
				//dataguid前缀
				String dataguid = vmap.get("objtypeid").toString()+"_"+vmap.get("objid").toString()+"_"+vmap.get("devicenumber").toString()+"_";
			    zmap.put("dataguid", dataguid);
				String maxTime = siteDataService.getFsjMaxTime(zmap);  //获得辐射计某站点最新时间
				Logger.getLogger("").info("fsj_时间戳 maxTime:"+maxTime);
		        if(maxTime!=null){
		        	Map<String,Object> map = new HashMap<String,Object>();
		        	map.put("maxTime",maxTime );
		        	map.put("objid",objid );
		        	List<Map<String,Object>> list = siteDataService.querySiteTimestamp(map);//查询时间戳表是否有该站点的数据，也就是是否是第一次
		        	if(list==null||list.size()<=0){ //说明是第一次，执行插入操作
		        	
		        		try{
		        			int i = siteDataService.addSiteTimestamp(map); //插入一条该站点最新时间的时间戳
		        			Logger.getLogger("").info("插入辐射计站点"+objid+"时间戳成功");
		        		}catch(Exception e){
		        			e.printStackTrace();
		        			Logger.getLogger("").info("插入辐射计站点"+objid+"时间戳失败");
		        		}
		        		
		        	}else{
		        		String id = list.get(0).get("id").toString(); //要更新的记录id
		        		map.put("id", id);
		        		try{
		        			int i = siteDataService.updateSiteTimestamp(map); //更新该站点最新时间的时间戳
		        			Logger.getLogger("").info("更新辐射计站点"+objid+"时间戳成功");
		        		}catch(Exception e){
		        			e.printStackTrace();
		        			Logger.getLogger("").info("更新辐射计站点"+objid+"时间戳失败");
		        		}
		        		
		        	}
		        	
		        }
		}
	  
	  
	   
		/**  
		 * 每个整点执行一次  ,站点微波辐射数据
		 */    
		//@Scheduled(cron = "0 0/6 * * * ?")
		public void tbFsjTask_ls(){
			Long startTime = System.currentTimeMillis();
			//tbFtpFsj("58646","2780");//同步丽水市辐射计数据 （为了辐射计和风廓线的联合查询，特意改了丽水辐射计的数据，所以暂不解析新的数据入库）
			
			Long endtime = System.currentTimeMillis();
			System.out.println("丽水的辐射计数据从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		}
		/**  
		 * 每个整点执行一次  ,站点微波辐射数据
		 */    
		/*@Scheduled(cron = "0 0/6 * * * ?")
		public void tbFsjTask_lx(){
			Long startTime = System.currentTimeMillis();
			
			tbFtpFsj("58548","2872");//同步兰溪县辐射计数据
		
			Long endtime = System.currentTimeMillis();
			System.out.println("兰溪的辐射计数据从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		}*/
		/**  
		 * 每个整点执行一次  ,站点微波辐射数据
		 */    
		//@Scheduled(cron = "0 0/6 * * * ?")
		public void tbFsjTask_cn(){
			Long startTime = System.currentTimeMillis();
			
			//tbFtpFsj("58755","2873");//同步苍南县辐射计数据
			//tbFtpFsj("58548","2872");//同步兰溪县辐射计数据
			//tbFtpFsj("58537","2874");//同步开化县辐射计数据
			Long endtime = System.currentTimeMillis();
			System.out.println("开化，苍南和兰溪的的辐射计数据从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
			start(); //启动exe文件
		}
		/**  
		 * 每个整点执行一次  ,站点微波辐射数据
		 */    
		/*@Scheduled(cron = "0 0/6 * * * ?")
		public void tbFsjTask_kh(){
			Long startTime = System.currentTimeMillis();
			
			tbFtpFsj("58537","2874");//同步开化县辐射计数据
			
			Long endtime = System.currentTimeMillis();
			System.out.println("开化的辐射计数据从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		}*/
	
		 /**  
		 * 每个整点执行一次  ,站点微波辐射数据
		 */    
		//@Scheduled(cron = "0 0/6 * * * ?")
		public void tbFsjTask_sy(){
			Long startTime = System.currentTimeMillis();
			
			//tbFtpFsj("58553","2878");//同步上虞县辐射计数据
			//tbFtpFsj("58553","2878");//同步上虞县辐射计数据
			Long endtime = System.currentTimeMillis();
			System.out.println("上虞的辐射计数据从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		}
		
		 /**  
		 * 每个整点执行一次  ,站点微波辐射数据
		 */    
		//@Scheduled(cron = "0 0/6 * * * ?")
		public void tbFsjTask_ra(){
			Long startTime = System.currentTimeMillis();
		
			//tbFtpFsj("58752","2879");//同步瑞安县辐射计数据
			
			Long endtime = System.currentTimeMillis();
			System.out.println("瑞安的辐射计数据从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
			
			
			Long sTime = System.currentTimeMillis();
			
			deleteImg(1);//删除一个月之前的图片
			
			Long etime = System.currentTimeMillis();
			System.out.println("删除一个月以前的图片耗时为："+(etime-sTime)/1000+"秒");
		}
		
		
		 /**  
		 * 每隔6分钟执行一次，删除一个月以前的图片
		 */    
		//@Scheduled(cron = "0 0/6 * * * ?")
		public void delete(){
			
			 Map<String, Object> platform = siteDataService.queryPlatform(); //查询该设备是设备端还是平台端
				
				
				if(platform!=null&&"2".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
					System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
					
					Logger.getLogger("").info("----此设备为设备端(开始定时删除一个月以前的图片)---------");
					
					Long startTime = System.currentTimeMillis();
					
					deleteImg(1);//删除一个月之前的图片
					
					Long endtime = System.currentTimeMillis();
					System.out.println("删除一个月以前的图片耗时为："+(endtime-startTime)/1000+"秒");
					
					
				}
		
			
		}
		
		
		
		 /**  
		 * 每隔俩天的晚上10点执行一次，删除matlab缓存信息
		 */    
		//@Scheduled(cron = "0 0 22 */2 * ?")
		public void deleteMatlab(){
			
			 Map<String, Object> platform = siteDataService.queryPlatform(); //查询该设备是设备端还是平台端
				
				
				if(platform!=null&&"2".equals(platform.get("isplatform").toString())){ //1-平台端  2-设备端
					System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
					
					Logger.getLogger("").info("----此设备为设备端(开始定时删除matlib的缓存)---------");
					Long startTime = System.currentTimeMillis();
					
					 String imgdir = "D:\\MATdata\\";
				        File dir = new File(imgdir);
				        if(dir.exists()){
				        	String[] list = dir.list();
				        	for(String file:list){
				        		if(file.endsWith("mat")){
				        				File f = new File(imgdir+file);
				        				if(f.isFile()){
				        					f.delete();
				        					Logger.getLogger("").info(imgdir+"删除matlab缓存文件成功");
				        				}
				        		}
				        	}
				        }else{
				        	Logger.getLogger("").info(imgdir+"matlab缓存信息路径不存在");
				        }
					
					Long endtime = System.currentTimeMillis();
					System.out.println("删除matlab缓存信息耗时为："+(endtime-startTime)/1000+"秒");
				}
			
			
		}
		
		
		/**
		 * 删除一个月之前的图片
		 * @param month  多少月
		 */
		
		private  void deleteImg(int month) {
			 String imgdir = "D:\\outdata\\";
		        File dir = new File(imgdir);
		        if(dir.exists()){
		        	Date date = new Date();
		        	Calendar calendar = Calendar.getInstance();
		        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		        	calendar.setTime(date);

		        	//calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-2);
		        	calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-month);
		        	String time = sdf.format(calendar.getTime()); //一个月之前的时间
		        	
		        	//System.out.println("date:"+date);
		        	//System.out.println("time:"+time);
		        	String[] list = dir.list();
		        	for(String file:list){
		        		if(file.contains("png")){
		        			if(file.split("_")[1].compareTo(time)<0){ //一个月之前的时间
		        				//System.out.println("filename:"+file);
		        				File f = new File(imgdir+file);
		        				f.delete();
		        			}
		        		}
		        	}
		        }
			
		}
		
		/*public static void main(String[] args) {
			deleteImg(30);
		}*/



		/**  
		 * 调用exe文件生成图片
		 */    
		//@Scheduled(cron = "0 0/7 * * * ?")
		public static  void start(){
			Long startTime = System.currentTimeMillis();
		
			startExe(); //启动exe文件
			
			Long endtime = System.currentTimeMillis();
			System.out.println("调用exe文件共用时间为："+(endtime-startTime)/1000+"秒");
		}
		
		/**
		 * 用于启动exe文件生成风羽，风矢，艾玛图
		 */
		public static void startExe(){
			//调用exe程序生成图片
			 Runtime r = Runtime.getRuntime();
	        // System.out.println("要条用exe生成图片了");
			 Logger.getLogger("").info("要调用exe生成图片了");
			//应用程序所在的路径

			 //String str_path = "D:\\Program Files\\matlabCode_main\\application\\plotMainFunc.exe";
			 //String str_path = "D:\\plotMainFunc\\application\\plotMainFunc.exe";
			 String str_path = "D:\\programs\\matlabpro_02\\plotMainFunc_02.exe";
			
	        Process pro = null;

			try {

	      //该方法开启一个新的进程

			 pro = r.exec(str_path);

			}catch (IOException e) {

			 //System.err.println("打开应用程序失败");
			 Logger.getLogger("").error("打开应用程序失败");

			}finally{
				 //利用该方法结束开启的进程

				//pro.destroy();
			}
		}
		
		
		

}
