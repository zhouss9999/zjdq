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
public class FKXFtpUtilsTwo {
	
	private final String FTP_IP = "10.135.30.211";
	private final int    FTP_PORT = 21;
	private final String FTP_USERNAME = "radaread";
	private final String FTP_PASSWORD = "radaread";
	

	private final String FTP_PATH="/"; //文件目录




	@Autowired
	public BaseaddDao baseaddDao;
	@Autowired
	public FKXFileUtil  fkxUtil;
	
	
	@Autowired
	private SiteDataDao dataDao;
	
	@Autowired
	private  WindProfileService wpfService;
	
	/**
	* 从ftp上根据区站编号读取各站点数据信息，并入库
	* @param qzbh 区站编号
	* @param objid objid
	* @return
	*/
	public  int[] addFKXDataTwo_all(String qzbh,String objid) {
	//获取数据库中辐射监测数据的最新时间
	// String maxtime = baseaddDao.getMaxTimeByDtid("yyyyMMddHH24MIss", "30", "2880");
	// System.out.println("maxtime:"+maxtime);
	
	//连接FTP服务器
	FTPClient ftpClient = FtpUtil.ftpConnection(FTP_IP, FTP_PORT, FTP_USERNAME, FTP_PASSWORD);
	//System.out.println("before FtpUtil.getFileList ");
	//获取根目录下1小时之内的所有辐射数据()
	//Map<String, List<SiteData>> fileNameMap = getFileList(ftpClient,1,maxtime);
	Map<String, List<SiteData>> fileNameMap = getFileList(ftpClient,9,qzbh); //由于风廓线是世界时，所以ftp上的时间比数据库里的少8个小时，所以此处的9实际上市取最近一个小时的而数据
	//System.out.println("fileNameMap.size()"+fileNameMap.size());
	//System.out.println("after FtpUtil.getFileList ");
	//断开FTP服务,防止FTP超时报异常
	FtpUtil.ftpClose(ftpClient);
	
	List<SiteData> robsList = fileNameMap.get("robs");//robs实时观测数据集合
	//List<SiteData> radList = fileNameMap.get("rad");//rad径向数据集合 还没写
	//System.out.println(qzbh+"最后的robsList的长度为:"+robsList.size());
	
	//int status=0;
	int robStatus = 0;   //插入robs实时观测数据的条数
	int radStatus = 0;   //插入rad径向数据的条数
	if (robsList.size() > 0) {
	//robStatus = dataDao.addCollectDataTwo(robsList);
	}
	/*if (radList.size() > 0) {
	radStatus = dataDao.addCollectDataTwo(robsList);   //径向数据暂没写
	}*/
	
	//查一下此时风廓线该站点的最新时间
		//String maxTime = baseaddDao.getMaxTimeByDtid("yyyy-MM-dd HH24:MI:ss", "5", objid);
	
	//insertFkxTimestamp(objid);	 //插入风廓线某站点最新时间的时间戳
	
	int[] results = new int[]{robStatus,radStatus};
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
	
	public  Map<String, List<SiteData>> getFileList(FTPClient ftpClient, int hour,String qzbh){
	 
	// 获取两小时之内的数据
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	Date date = new Date();
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)
		- hour);// 取几小时的数据
	String time = sdf.format(calendar.getTime());
	
	// 创建map，用于返回
	Map<String, List<SiteData>> fileMap = new LinkedHashMap<String, List<SiteData>>();
	
	
	// 获得指定目录下所有文件名
	FTPFile[] ftpFiles = null;
	try {
	ftpFiles = ftpClient.listFiles(FTP_PATH);
	} catch (Exception e) {
	e.printStackTrace();
	}
	// System.out.println("刚从ftp读下来的文件个数:"+ftpFiles.length);
	
	List<String> robsList = new ArrayList<String>();// 实时产品采样数据
	List<String> radList = new ArrayList<String>();// 径向数据
	
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
	
	//获取数据库中辐射监测数据的最新时间
	Map<String,Object> paramMap = new HashMap<String,Object>();
	paramMap.put("qzbh",qzbh);
	Map<String,Object> map = dataDao.queryByStaNum(paramMap);  //根据区站编号查风廓线dataguid相关参数
	String objid = map.get("objid").toString();
	String dbMaxTime=null;
	if(objid!=null){
	dbMaxTime = baseaddDao.getMaxTimeByDtid("yyyyMMddHH24MIss", "30", objid);
	}
	// System.out.println("objid:"+objid);
	//System.out.println("dbMaxTime:"+dbMaxTime);
	
	
	//解析robs数据文件
	fileMap.put("robs",
		readFile(ftpClient, getFileListByTime(robsList, dbMaxTime),qzbh));
	
	//此处应采用专门的rad径向文件解析方法解析，暂时还没写
	/*fileMap.put("rad",
		readFile(dir, getFileListByTime(radList, dbMaxTime)));*/
	
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
	   int[] status = addFKXDataTwo_all(qzbh,objid);
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
	//wpfService.reWriteFKXfile(objid); //重写风廓线文件生成风矢风羽图
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
	tbFtpFkx("58459","2880");//同步萧山区风廓线数据 
	
	Long endtime = System.currentTimeMillis();
	//System.out.println("萧山的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("萧山的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");

	}
	
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_ca(){
	Long startTime = System.currentTimeMillis();
	tbFtpFkx("58543","2881");//同步淳安县风廓线数据 
	
	Long endtime = System.currentTimeMillis();
	//System.out.println("淳安的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("淳安的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	
	
	/*Long sTime = System.currentTimeMillis();
	
    tbFtpFkx("58448","2882");//同步临安区风廓线数据
	
	Long etime = System.currentTimeMillis();
	//System.out.println("临安的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("临安的风廓线站从读取-解析-入库-重写共用时间为："+(etime-sTime)/1000+"秒");*/

	}
	
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_la(){
	Long startTime = System.currentTimeMillis();
	tbFtpFkx("58448","2882");//同步临安区风廓线数据
	
	Long endtime = System.currentTimeMillis();
	//System.out.println("临安的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("临安的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");

	}
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_dt(){
	Long startTime = System.currentTimeMillis();
	tbFtpFkx("58760","2883");//同步洞头区风廓线数据
	
	Long endtime = System.currentTimeMillis();
	//System.out.println("洞头的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("洞头的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
/*
	Long sTime = System.currentTimeMillis();
	tbFtpFkx("58452","2884");//同步嘉兴市风廓线数据
	
	Long etime = System.currentTimeMillis();
	//System.out.println("嘉兴的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("嘉兴的风廓线站从读取-解析-入库-重写共用时间为："+(etime-sTime)/1000+"秒");*/
	
	
	}
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_jx(){
	Long startTime = System.currentTimeMillis();
	tbFtpFkx("58452","2884");//同步嘉兴市风廓线数据
	
	Long endtime = System.currentTimeMillis();
	//System.out.println("嘉兴的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("嘉兴的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");

	}
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
	//@Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_hz(){
	Long startTime = System.currentTimeMillis();
	tbFtpFkx("58450","2885");//同步湖州市风廓线数据
	
	Long endtime = System.currentTimeMillis();
	//System.out.println("湖州的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	
	Logger.getLogger("").info("湖州的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	
	/*Long sTime = System.currentTimeMillis();
	tbFtpFkx("58557","2886");//同步义乌市风廓线数据
	// tbFtpFkx("58548","2887");//同步兰溪县风廓线数据
	Long etime = System.currentTimeMillis();
	//System.out.println("义乌的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("义乌的风廓线站从读取-解析-入库-重写共用时间为："+(etime-sTime)/1000+"秒");*/


	}
	/**  
	* 每个整点执行一次  ,站点风廓线数据数据
	*/    
   // @Scheduled(cron = "0 0/6 * * * ?")
	public void tbFkxTask_yw(){
	Long startTime = System.currentTimeMillis();
	
	tbFtpFkx("58557","2886");//同步义乌市风廓线数据
	// tbFtpFkx("58548","2887");//同步兰溪县风廓线数据
	Long endtime = System.currentTimeMillis();
	//System.out.println("义乌的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");
	Logger.getLogger("").info("义乌的风廓线站从读取-解析-入库-重写共用时间为："+(endtime-startTime)/1000+"秒");

	}
	
	
	

}


