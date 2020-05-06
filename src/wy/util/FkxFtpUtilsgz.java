package wy.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.service.WindProfileService;
import wy.util.datapersistence.Dao.BaseaddDao;

@Component
public class FkxFtpUtilsgz {
	
	/*//新版本站点微波辐射数据同步参数(FTP)
		private final String FTP_IP = "60.12.107.77";
		private final int    FTP_PORT = 21;
		private final String FTP_USERNAME = "xiaoshuidian";
		private final String FTP_PASSWORD = "sysc123.,/";
		
		
		
		private final String FTP_PATH="/home/xiaoshuidian"; //文件目录
*/		
		//老Ftp站点微波辐射数据同步参数(FTP)
				private final String FTP_IP = "10.135.30.187";
				private final int    FTP_PORT = 21;
				private final String FTP_USERNAME = "fkxradar";
				private final String FTP_PASSWORD = "fkxradar";
				
				

				
				
				private final String FTP_PATH="/"; //文件目录
		
		
		

		@Autowired
		public BaseaddDao baseaddDao;
		@Autowired
		public FKXFileUtil  fkxUtil;
		
		
		@Autowired
		private SiteDataDao dataDao;
		
		@Autowired
		private  WindProfileService wpfService;
		
		
		@Autowired 
		private FsjFtpUtilsgz fsjUtil;
	
	/**
	 *  从ftp上根据区站编号读取各站点数据信息，并入库
	* @param qzbh 站点区站标号
    * @param objid 站点objid
	* @param ip ftp服务器ip
	* @param port ftp服务器端口
	* @param username ftp服务器用户名
	* @param password ftp服务器密码
	* @param ftpPath  ftp服务器文件夹根目录
	 * @return
	 */
	public  int[] addFKXDataTwo_all(String qzbh,String objid,String ip,String port,String username,String password,String ftpPath) {
		//获取数据库中辐射监测数据的最新时间
		// String maxtime = baseaddDao.getMaxTimeByDtid("yyyyMMddHH24MIss", "30", "2880");
		// System.out.println("maxtime:"+maxtime);

		//连接FTP服务器
		FTPClient ftpClient = FtpUtil.ftpConnection(ip, Integer.parseInt(port), username, password);
		 //System.out.println("before FtpUtil.getFileList ");
		//获取根目录下1小时之内的所有辐射数据
		//Map<String, List<SiteData>> fileNameMap = getFileList(ftpClient,1,maxtime);
		Map<String, List<SiteData>> fileNameMap = getFileList(ftpClient,9,qzbh,objid,ftpPath); //由于风廓线是世界时，所以ftp上的时间比数据库里的少8个小时，所以此处的9实际上市取最近一个小时的而数据
		//System.out.println("fileNameMap.size()"+fileNameMap.size());
        //System.out.println("after FtpUtil.getFileList ");
		//断开FTP服务,防止FTP超时报异常
		FtpUtil.ftpClose(ftpClient);
		
		List<SiteData> robsList = fileNameMap.get("robs");//robs实时观测数据集合
		//List<SiteData> radList = fileNameMap.get("rad");//rad径向数据集合 还没写
	
		
		//int status=0;
		int robStatus = 0;   //插入robs实时观测数据的条数
		int radStatus = 0;   //插入rad径向数据的条数
		if (robsList!=null&&robsList.size() > 0) {
			//robStatus = dataDao.addCollectDataTwo(robsList);
			//分表后的风廓线数据插入
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("list", robsList);
			map.put("objid", objid);
			robStatus = dataDao.addCollectDataTwo(map);
		}
		/*if (radList.size() > 0) {
			radStatus = dataDao.addCollectDataTwo(robsList);   //径向数据暂没写
		}*/
        int[] results = new int[]{robStatus,radStatus};
        
           //查一下此时风廓线该站点的最新时间
      		//String maxTime = baseaddDao.getMaxTimeByDtid("yyyy-MM-dd HH24:MI:ss", "5", objid);
      	    
        //insertFkxTimestamp( objid);  //插入风廓线某站点最新时间的时间戳
        
		return results;
	
	
	
	}
	
	
	
	/**
	 * 插入风廓线某站点最新时间的时间戳
	 */
	public void insertFkxTimestamp(String objid){
		String maxTime = baseaddDao.getMaxTimeByDtid_two( "30", objid);
  		Logger.getLogger("").info("maxTime:"+maxTime);
          if(maxTime!=null){
          	Map<String,Object> map = new HashMap<String,Object>();
          	map.put("maxTime",maxTime );
          	map.put("objid",objid );
          	List<Map<String,Object>> list = dataDao.querySiteTimestamp(map);//查询时间戳表是否有该站点的数据，也就是是否是第一次
          	if(list==null||list.size()<=0){ //说明是第一次，执行插入操作
          	
          		try{
          			int i = dataDao.addSiteTimestamp(map); //插入一条该站点最新时间的时间戳
          			Logger.getLogger("").info("插入风廓线站点"+objid+"时间戳成功");
          		}catch(Exception e){
          			e.printStackTrace();
          			Logger.getLogger("").info("插入风廓线站点"+objid+"时间戳失败");
          		}
          		
          	}else{
          		String id = list.get(0).get("id").toString(); //要更新的记录id
          		map.put("id", id);
          		try{
          			int i = dataDao.updateSiteTimestamp(map); //更新该站点最新时间的时间戳
          			Logger.getLogger("").info("更新风廓线站点"+objid+"时间戳成功");
          		}catch(Exception e){
          			e.printStackTrace();
          			Logger.getLogger("").info("更新风廓线站点"+objid+"时间戳失败");
          		}
          		
          	}
          	
          }
	}
	
	/**
	 * 
	 * @param ftpClient
	 * @param hour取几个小时之内的数据
	 * @param qzbh 区站编号
	 * @return
	 */
	
	public  Map<String, List<SiteData>> getFileList(FTPClient ftpClient, int hour,String qzbh,String objid,String ftpPath){
			 
		// 获取FTP上1小时之内的数据(由于ftp上的文件是世界时，所以9-8实际上是1个小时)
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");  
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)
				- hour);// 取几小时的数据,正式环境用9小时，因为风廓线的时间取得是格林威治时间，比北京时间慢8小时 ,测试，取俩年前的数据
		String time = sdf.format(calendar.getTime());

		// 创建map，用于返回
		Map<String, List<SiteData>> fileMap = new LinkedHashMap<String, List<SiteData>>();
        
		
		 // 获得指定目录下所有文件名
        FTPFile[] ftpFiles = null;
        try {
        	ftpFiles = ftpClient.listFiles(ftpPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
       // System.out.println("刚从ftp读下来的文件个数:"+ftpFiles.length);

		List<String> robsList = new ArrayList<String>();// 实时产品采样数据
		List<String> radList = new ArrayList<String>();// 径向数据

		
		
		//获取数据库中辐射监测数据的最新时间
		/*Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("qzbh",qzbh);
		Map<String,Object> map = dataDao.queryByStaNum(paramMap);  //根据区站编号查风廓线dataguid相关参数
		if(map!=null){
			
			
	    String objid = map.get("objid").toString();*/
			String dbMaxTime=null;
			if(objid!=null){
				dbMaxTime = dataDao.getMaxTimeByDtid_three("yyyyMMddHH24MIss", "30", objid);
			}
			
			if(dbMaxTime==null){ //没有数据，说明第一次同步，则在读取ftp文件的时候不再做时间过滤操作，全部读取
				for (FTPFile f : ftpFiles) {
					if (f.isFile() && f.getSize() > 0) {
						//System.out.println("file is file ");
						
							if (f.getName().contains("ROBS")) {// 每小时产品采样数据
								//System.out.println("file is contais ROBS ");
								if(qzbh.equals(f.getName().split("_")[3])){ //正式环境下， 54857应换成动态的qzbh,此处没数据，是为了测试所以写死了
									//System.out.println("file's qzbh is  "+qzbh);
									//if(f.getName().split("_")[4].compareTo(time)>0){ //此处应该是>号，目前没数据，是为了做测试
										//System.out.println(f.getName()+".filetime is <"+time);
										robsList.add(f.getName());
									//}
								}
							} else if (f.getName().endsWith("RAD.TXT")) {// 每小时产品采样数据
								//System.out.println("file is contais ROBS ");
								radList.add(f.getName());
							}
					}

				}
			}else{
				for (FTPFile f : ftpFiles) {
					if (f.isFile() && f.getSize() > 0) {
						//System.out.println("file is file ");
						
							if (f.getName().contains("ROBS")) {// 每小时产品采样数据
								//System.out.println("file is contais ROBS ");
								if(qzbh.equals(f.getName().split("_")[3])){ //正式环境下， 54857应换成动态的qzbh,此处没数据，是为了测试所以写死了
									//System.out.println("file's qzbh is  "+qzbh);
									if(f.getName().split("_")[4].compareTo(time)>0){ //此处应该是>号，目前没数据，是为了做测试
										//System.out.println(f.getName()+".filetime is <"+time);
										robsList.add(f.getName());
									}
								}
							} else if (f.getName().endsWith("RAD.TXT")) {// 每小时产品采样数据
								//System.out.println("file is contais ROBS ");
								radList.add(f.getName());
							}
					}

				}
			}
			
			
			
	        //解析robs数据文件
			fileMap.put("robs",
					readFile(ftpClient, getFileListByTime(robsList, dbMaxTime),qzbh));
			
			//此处应采用专门的rad径向文件解析方法解析，暂时还没写
			/*fileMap.put("rad",
					readFile(dir, getFileListByTime(radList, dbMaxTime)));*/	
		//}
		
     
		return fileMap;
	}
	
	/**
	 * 获取大于数据库时间的数据
	 * 
	 * @param goupList
	 *            分组后的文件名列表
	 * @param dbMaxTime
	 *            数据库最大时间
	 * @return fileLists
	 */
	public static List<String> getFileListByTime(List<String> goupList,
			String dbMaxTime) {
		List<String> groupFileList = new ArrayList<String>();
		for (String filename : goupList) {
			if ((dbMaxTime != null) && (!"".equals(dbMaxTime))) {
				String time = filename.split("_")[4]; // 获取观测时间
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date;
				try {
					date = sdf.parse(time);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)
							+8);// 在ftp风廓线文件的时间基础上加8个小时，也就是由时间时改成北京时
					String collecttime = sdf.format(calendar.getTime()); //加8个小时后的时间
					if (collecttime.compareTo(dbMaxTime) > 0) {
						groupFileList.add(filename);
					}
				} catch (ParseException e) {
					Logger.getLogger("").info("文件名上的时间格式转换失败");
					e.printStackTrace();
				}
			} else {
				groupFileList.add(filename);
			}

		}

		return groupFileList;
	}

	/**
	 * 取ftp上的文件内容
	 * @param qzbh  区站编号
	 * @param FTPClient
	 * @param fileLists
	 * @return list
	 */
	public  List<SiteData> readFile(FTPClient ftpClient,
			List<String> filelist,String qzbh) {
       
		List<SiteData> listdata = new ArrayList<SiteData>();// 结果集对象集合，要插入到数据库中的
		
		if (filelist.size() > 0) {
			for (String filename : filelist) {
				
				//String qzbh = filename.split("_")[3]; //区站编号
				//String qzbh="58459";
				Map<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("qzbh",qzbh);
				Map<String,Object> map = dataDao.queryByStaNum(paramMap);  //根据区站编号查风廓线dataguid相关参数
				String dataguid = map.get("objtypeid").toString()+"_"+map.get("objid").toString()+"_"+map.get("devicenumber").toString()+"_";
				//System.out.println("dataguid:"+dataguid);
				
				
				InputStream ins = null;
				try {
					
					 // 从服务器上读取指定的文件
				    ins = ftpClient.retrieveFileStream(filename);
					BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
					String line;
					StringBuilder spfx = new StringBuilder(); // 用来接收水平方向所对应的的各个高度的值
					StringBuilder spfs = new StringBuilder(); // 用来接收水平风速所对应的的各个高度的值
					StringBuilder czfs = new StringBuilder(); // 用来接收垂直风速所对应的的各个高度的值
					StringBuilder spfxkxd = new StringBuilder(); // 用来接收水平方向可信度所对应的的各个高度的值
					StringBuilder czfxkxd = new StringBuilder(); // 用来接收垂直方向可信度所对应的的各个高度的值
					StringBuilder cn2 = new StringBuilder(); // 用来接收垂直方向Cn2所对应的的各个高度的值

					spfx.append("{");
					spfs.append("{");
					czfs.append("{");
					spfxkxd.append("{");
					czfxkxd.append("{");
					cn2.append("{");
					int num = 0;
					String collecttime = "";// 观测时间
					while ((line = reader.readLine()) != null) {
                   
						if (num == 1) { // 第二行，测站基本参数
							
							String[] arrs = line.replace(" ", ",").split(",");
							String time = arrs[arrs.length - 1]; // 获取观测时间
							
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							Date date = sdf.parse(time);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)
									+8);// 在ftp风廓线文件的时间基础上加8个小时，也就是由时间时改成北京时
							collecttime = sdf.format(calendar.getTime()); //加8个小时后的时间

						}
						num++;
						if (num > 3) {
							if (!"NNNN".equals(line)) { // 不拼接最后的结束标志NNNN,产品数据实体部分开始
								String[] arrs = line.replace(" ", ",").split(
										",");
								for (int i = 0; i < arrs.length; i++) {
									if(Integer.parseInt(arrs[0])<=10000){ //高度限定在10000米

									if (i == 0) {// 采样高度
										spfx.append('"' + "spfx_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										spfs.append('"' + "spfs_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										czfs.append('"' + "czfs_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										spfxkxd.append('"' + "spfxkxd_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										czfxkxd.append('"' + "czfxkxd_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										cn2.append('"' + "cn2_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
									} else if (i == 1) {
										spfx.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i]))+ '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 2) {
										spfs.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 3) {
										czfs.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 4) {
										spfxkxd.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 5) {
										czfxkxd.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} /*else if (i == 6) {
										cn2.append(":"
												+ '"'
												+ (arrs[i]
														.contains("/") ? "/"
														: NumberCastUtil.object2Str(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									}*/
									else if (i == 6) {
										cn2.append(":"
												+ '"'
												+ (arrs[i]
														.contains("/") ? "/"
														: 10*NumberCastUtil.log10(Double.parseDouble(arrs[i]))) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									}
								}
								}
							}

						}
						//System.out.println(line.replace(" ", ","));

					}
					
					
					reader.close();
					   if (ins != null) {
						   ins.close();
					   }
					   // 主动调用一次getReply()把接下来的226消费掉. 这样做是可以解决这个返回null问题
					   ftpClient.getReply();
					// spfx.append("}"); //加上右半结束框

					String spfxStr = spfx.toString().substring(0,
							spfx.toString().length() - 1)
							+ "}"; // 水平方向 paramid :168
					wpfService.addSiteData(spfxStr, collecttime, dataguid+"168",
							listdata); // 封装为结果对象并添加到结果集集合中

					String spfsStr = spfs.toString().substring(0,
							spfs.toString().length() - 1)
							+ "}"; // 水平风速 paramid :169
					wpfService.addSiteData(spfsStr, collecttime, dataguid+"169",
							listdata); // 封装为结果对象并添加到结果集集合中

					String czfsStr = czfs.toString().substring(0,
							czfs.toString().length() - 1)
							+ "}"; // 垂直风速 paramid :170
					wpfService.addSiteData(czfsStr, collecttime, dataguid+"170",
							listdata); // 封装为结果对象并添加到结果集集合中

					String spfxkxdStr = spfxkxd.toString().substring(0,
							spfxkxd.toString().length() - 1)
							+ "}"; // 水平方向可信度 paramid :171
					wpfService.addSiteData(spfxkxdStr, collecttime, dataguid+"171",
							listdata); // 封装为结果对象并添加到结果集集合中

					String czfxkxdStr = czfxkxd.toString().substring(0,
							czfxkxd.toString().length() - 1)
							+ "}"; // 垂直方向可信度 paramid :172
					wpfService.addSiteData(czfxkxdStr, collecttime, dataguid+"172",
							listdata); // 封装为结果对象并添加到结果集集合中

					String cn2Str = cn2.toString().substring(0,
							cn2.toString().length() - 1)
							+ "}"; // 垂直方向Cn2 paramid :173
					wpfService
							.addSiteData(cn2Str, collecttime, dataguid+"173", listdata); // 封装为结果对象并添加到结果集集合中

					// 将检测到的ROBS实时采样数据插入到数据库中
					//System.out.println("listdata.size:" + listdata.size());
					//dataDao.addCollectDataTwo(listdata);

					//System.out.println("spfx:" + spfxStr);
					//System.out.println("spfsStr:" + spfsStr);
					//System.out.println("czfsStr:" + czfsStr);
					//System.out.println("spfxkxdStr:" + spfxkxdStr);
					//System.out.println("czfxkxdStr:" + czfxkxdStr);
					//System.out.println("cn2Str:" + cn2Str);
				} catch (FileNotFoundException e) {
					//System.out.println("file is not fond");
					Logger.getLogger("").error("file is not fond");
				} catch (IOException e) {
					//System.out.println("Read or write Exceptioned");
					Logger.getLogger("").error("Read or write Exceptioned");
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
		}
		return listdata;
	}
	
	
	
	
	/**
	 * 从ftp上同步风廓线数据到数据库里并重写
	 * 
	 * @param qzbh  风廓线区站编号
	 * @param objid 风廓线objid
	 */
   public void tbFtpFkx(String qzbh,String objid){
	   Logger.getLogger("").info("------"+qzbh+"_"+objid+"站点同步风廓线数据开始---------");
		long starttime=System.currentTimeMillis();
	    //File file=new File("C:\\Users\\Lenovo\\Desktop\\风廓线\\20180803\\WIND");  
		//wpf.readFile(firstFile);
	    try{
	    	   int[] status = new int[2];//addFKXDataTwo_all(qzbh,objid);
	    	   if(status[0]>0){
	    		   Logger.getLogger("").info("------"+qzbh+"_"+objid+"站点同步风廓线数据成功---------");
	    	   }else{
	    		   Logger.getLogger("").info(qzbh+"_"+objid+"------数据数据库已是最新数据---------");
	    	   }
	    	   if(status[1]>0){
		    		  Logger.getLogger("").info("------rad入库成功---------");
		    	   }else{
		    		   Logger.getLogger("").info("------rad数据数据库已是最新数据---------");
		    	   }
	    }catch(Exception e){
	    	 Logger.getLogger("").error("------"+qzbh+"_"+objid+"站点同步风廓线数据失败---------");
	    	e.printStackTrace();
	    }
	    
	    long endtime=System.currentTimeMillis();
	    Logger.getLogger("").info(qzbh+"_"+objid+"------读取入库共耗时---------"+(endtime-starttime));
	    
	    //---------------------------------重写风廓线----------------
	    try{
	    	//Map<String,Object> map = new HashMap<String,Object>();
	    	//String objid ="1414";
	    	wpfService.reWriteFKXfile(objid,qzbh); //重写风廓线文件生成风矢风羽图
	    }catch(Exception e){
	    	e.printStackTrace();
	    	//Logger.getLogger("").error("重写风廓线文件出错");
	    	Logger.getLogger("").error("------"+qzbh+"_"+objid+"重写风廓线文件出错---------");
	    }
	    
	    
	  //插入该站点最新数据的时间戳
		try{
		 //Map<String,Object> map =new HashMap<String,Object>();

			insertFkxTimestamp(objid);  //插入该站点最新数据的时间戳
		}catch(Exception e){
			e.printStackTrace();
			
			Logger.getLogger("").error("站点"+objid+"插入该站点最新数据的时间戳出错");
		}
	    
   }
   
   
   
	
	/**  
	 * 每个整点执行一次  ,站点风廓线数据数据
	 */    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_xs(){
		Long startTime = System.currentTimeMillis();
		
		tbFtpFkx("58566","2890");//同步象山区风廓线数据 
		
		Long endtime = System.currentTimeMillis();
		//System.out.println("象山的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		Logger.getLogger("").info("象山的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	}
	
	/**  
	 * 每个整点执行一次  ,站点风廓线数据数据
	 */    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_fh(){
		Long startTime = System.currentTimeMillis();
		
		tbFtpFkx("58565","2891");//同步奉化区风廓线数据 
		
		Long endtime = System.currentTimeMillis();
		System.out.println("奉化区的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	}
	
	
	/**  
	 * 每个整点执行一次  ,站点风廓线数据数据
	 */    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_yy(){
		Long startTime = System.currentTimeMillis();
		
		tbFtpFkx("58468","2892");//同步余姚区风廓线数据
		//tbFtpFkx("58565","2891");//同步奉化区风廓线数据 
		Long endtime = System.currentTimeMillis();
		//System.out.println("余姚的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		Logger.getLogger("").info("余姚的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		
		//FsjFtpUtils.start(); //启动exe文件
		
		
	}
	
	
	
	/**  
	 * 每个整点执行一次  ,站点风廓线数据数据
	 */    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_lx(){
		Long startTime = System.currentTimeMillis();
	
		tbFtpFkx("58548","2887");//同步兰溪县风廓线数据
		
		Long endtime = System.currentTimeMillis();
		//System.out.println("兰溪的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		Logger.getLogger("").info("兰溪的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	}
	/**  
	 * 每个整点执行一次  ,站点风廓线数据数据
	 */    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_kh(){
		Long startTime = System.currentTimeMillis();
		
		tbFtpFkx("58537","2888");//同步开化县风廓线数据
		
		Long endtime = System.currentTimeMillis();
		Logger.getLogger("").info("开化的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	}
	/**  
	 * 每个整点执行一次  ,站点风廓线数据数据
	 */    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_cn(){
		Long startTime = System.currentTimeMillis();
		
		tbFtpFkx("58755","2889");//同步苍南县风廓线数据
		Long endtime = System.currentTimeMillis();
		//System.out.println("苍南的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
		Logger.getLogger("").info("苍南的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	}
	
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_ls(){
	Long startTime = System.currentTimeMillis();
	tbFtpFkx("58646","1414");//同步丽水市风廓线数据  丽水的qzbh是58646   现在写成54857是为了测试

	Long endtime = System.currentTimeMillis();
	//System.out.println("丽水的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("丽水的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");

	}


	
	/**
	 * 从ftp服务器上读取风廓线数据
	 * @param qzbh 站点区站标号
	 * @param objid 站点objid
	 * @param ip ftp服务器ip
	 * @param port ftp服务器端口
	 * @param username ftp服务器用户名
	 * @param password ftp服务器密码
	 * @param ftpPath  ftp服务器文件夹根目录
	 */

	public void tbFtpFkxTwo(String qzbh, String objid, String ip,
			String port, String username, String password, String filepath) {
		 Logger.getLogger("").info("------"+qzbh+"_"+objid+"站点同步风廓线数据开始---------");
			long starttime=System.currentTimeMillis();
		    //File file=new File("C:\\Users\\Lenovo\\Desktop\\风廓线\\20180803\\WIND");  
			//wpf.readFile(firstFile);
		    try{
		    	   int[] status = addFKXDataTwo_all(qzbh,objid,ip,port,username,password,filepath);
		    	   if(status[0]>0){
		    		   Logger.getLogger("").info("------"+qzbh+"_"+objid+"站点同步风廓线数据成功---------");
		    	   }else{
		    		   Logger.getLogger("").info(qzbh+"_"+objid+"------数据数据库已是最新数据---------");
		    	   }
		    	   if(status[1]>0){
			    		  Logger.getLogger("").info("------rad入库成功---------");
			    	   }else{
			    		   Logger.getLogger("").info("------rad数据数据库已是最新数据---------");
			    	   }
		    	   
		    	   fsjUtil.delBeforeYear(1, objid);
		    	   
		    }catch(Exception e){
		    	 Logger.getLogger("").error("------"+qzbh+"_"+objid+"站点同步风廓线数据失败---------");
		    	e.printStackTrace();
		    }
		    
		    long endtime=System.currentTimeMillis();
		    Logger.getLogger("").info(qzbh+"_"+objid+"------读取入库共耗时---------"+(endtime-starttime));
		    
		    //---------------------------------重写风廓线----------------
		    try{
		    	//Map<String,Object> map = new HashMap<String,Object>();
		    	//String objid ="1414";
		    	wpfService.reWriteFKXfile(objid,qzbh); //重写风廓线文件生成风矢风羽图
		    }catch(Exception e){
		    	e.printStackTrace();
		    	//Logger.getLogger("").error("重写风廓线文件出错");
		    	Logger.getLogger("").error("------"+qzbh+"_"+objid+"重写风廓线文件出错---------");
		    }
		    
		    
		  //插入该站点最新数据的时间戳
			try{
			 //Map<String,Object> map =new HashMap<String,Object>();

				insertFkxTimestamp(objid);  //插入该站点最新数据的时间戳
			}catch(Exception e){
				e.printStackTrace();
				
				Logger.getLogger("").error("站点"+objid+"插入该站点最新数据的时间戳出错");
			}
	}

}
