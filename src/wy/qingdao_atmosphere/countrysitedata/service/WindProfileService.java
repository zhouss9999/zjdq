package wy.qingdao_atmosphere.countrysitedata.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;


import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;

import wy.qingdao_atmosphere.countrysitedata.domain.FkXParam;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.domain.WebServer;
import wy.util.CalCZ;
import wy.util.datapersistence.ModelAssist;
import wy.util.datapersistence.Dao.BaseaddDao;
import wy.util.datapersistence.service.BaseService;


@Service
public class WindProfileService {
	
	
	 
	@Autowired
	private SiteDataDao dataDao;
	
	@Autowired
	private BaseService baseService;
	
	
	public void readFile(File firstFile,String maxtime){
		    //File firstFile=new File("C:\\Users\\Lenovo\\Desktop\\风廓线\\20180803\\OHRWIND\\Z_RADA_I_54857_20180803060000_P_WPRD_PB_OOBS.txt");  
		   // File secondFile=new File("C:\\Users\\Lenovo\\Desktop\\copyf.TXT");  
		   
			//System.out.println("maxtime:"+maxtime);
			if(maxtime==null){
				maxtime=""; //转为空字符串，不然compartTo的时候会报空指针异常
			}
			//System.out.println("filetime"+firstFile.getName().split("_")[4]);
			String qzbh = firstFile.getName().split("_")[3]; //区站编号
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("qzbh",qzbh);
			Map<String,Object> map = dataDao.queryByStaNum(paramMap);  //根据区站编号查风廓线dataguid相关参数
			String dataguid = map.get("objtypeid").toString()+"_"+map.get("objid").toString()+"_"+map.get("devicenumber").toString()+"_";
			//System.out.println("dataguid:"+dataguid);
            if(firstFile.getName().split("_")[4].compareTo(maxtime)>0||maxtime==null){ //如果文件时间比数据库已有的最新的数据时间要大，就读取并存入,
		        BufferedReader in = null;                                              //如果maxtime==null，说明数据库第一次读或者没数据，全部读取并插入
		       // System.out.println("filetime>maxtime,进来方法了");
		      //  BufferedWriter out = null;        
		        try {       
		            //加入编码字符集   
		            in = new BufferedReader( new InputStreamReader(new FileInputStream(firstFile), "gbk"));  
		           
		            //加入编码字符集  
		            //out = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(secondFile), "gbk"));  
		           
		            String line = "";  
		            StringBuilder spfx = new StringBuilder();   //用来接收水平方向所对应的的各个高度的值
		            StringBuilder spfs = new StringBuilder();   //用来接收水平风速所对应的的各个高度的值
		            StringBuilder czfs = new StringBuilder();   //用来接收垂直风速所对应的的各个高度的值
		            StringBuilder spfxkxd = new StringBuilder();   //用来接收水平方向可信度所对应的的各个高度的值
		            StringBuilder czfxkxd = new StringBuilder();   //用来接收垂直方向可信度所对应的的各个高度的值
		            StringBuilder cn2 = new StringBuilder();   //用来接收垂直方向Cn2所对应的的各个高度的值
		            
		            
		            
		            spfx.append("{");
		            spfs.append("{");	
              		czfs.append("{");	 
              		spfxkxd.append("{");	 
              		czfxkxd.append("{");	
              		cn2.append("{");	 
		            int num=0;
		            String collecttime="";//观测时间
		            while((line = in.readLine())!=null){  
		        
		            	if(num==1){ //第二行，测站基本参数
		            		String[] arrs = line.replace(" ",",").split(",");
		            		 collecttime = arrs[arrs.length-1];  //获取观测时间
		            		
		            	}
		            	num++;
		            	if(num>3){
		            		if(!"NNNN".equals(line)){  //不拼接最后的结束标志NNNN,产品数据实体部分开始
		            			 String[] arrs = line.replace(" ",",").split(",");
		 	 	                for(int i=0;i<arrs.length;i++){
		 	 	                	
		 	 	                	if(i==0){//采样高度
		 	 	                		spfx.append('"'+"spfx_"+Integer.parseInt(arrs[i])+'"');	 //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
		 	 	                		spfs.append('"'+"spfs_"+Integer.parseInt(arrs[i])+'"');	 //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
		 	 	                		czfs.append('"'+"czfs_"+Integer.parseInt(arrs[i])+'"');	 //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
		 	 	                		spfxkxd.append('"'+"spfxkxd_"+Integer.parseInt(arrs[i])+'"');	 //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
		 	 	                		czfxkxd.append('"'+"czfxkxd_"+Integer.parseInt(arrs[i])+'"');	 //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
		 	 	                		cn2.append('"'+"cn2_"+Integer.parseInt(arrs[i])+'"');	 //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
		 	 	                	}else if(i==1){
		 	 	                		spfx.append(":"+'"'+Float.parseFloat(arrs[i].contains("/")?"0":arrs[i])+'"'+",");  //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
		 	 	                	}else if(i==2){
		 	 	                		spfs.append(":"+'"'+Float.parseFloat(arrs[i].contains("/")?"0":arrs[i])+'"'+",");  //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
		 	 	                	}else if(i==3){
		 	 	                		czfs.append(":"+'"'+Float.parseFloat(arrs[i].contains("/")?"0":arrs[i])+'"'+",");  //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
		 	 	                	}else if(i==4){
		 	 	                		spfxkxd.append(":"+'"'+Float.parseFloat(arrs[i].contains("/")?"0":arrs[i])+'"'+",");  //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
		 	 	                	}else if(i==5){
		 	 	                		czfxkxd.append(":"+'"'+Float.parseFloat(arrs[i].contains("/")?"0":arrs[i])+'"'+",");  //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
		 	 	                	}else if(i==6){
		 	 	                		cn2.append(":"+'"'+Float.parseFloat(arrs[i].contains("/")?"0":arrs[i])+'"'+",");  //在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
		 	 	                	}
		 	 	                }
		            		}
		            		
		            	}
		                //System.out.println(line.replace(" ", ","));  
		           
		                //out.write(line+"\r\n");  
		            }  
		            //spfx.append("}");  //加上右半结束框
		           
	         		List<SiteData> listdata = new ArrayList<SiteData>();//结果集对象集合，要插入到数据库中的
	         		
		            String spfxStr=spfx.toString().substring(0,spfx.toString().length()-1)+"}"; //水平方向  paramid :168
		            addSiteData(spfxStr,collecttime,dataguid+"168",listdata);  //封装为结果对象并添加到结果集集合中
		            
		            String spfsStr=spfs.toString().substring(0,spfs.toString().length()-1)+"}"; //水平风速  paramid :169
		            addSiteData(spfsStr,collecttime,dataguid+"169",listdata);  //封装为结果对象并添加到结果集集合中
		            
		            String czfsStr=czfs.toString().substring(0,czfs.toString().length()-1)+"}";  //垂直风速  paramid :170
		            addSiteData(czfsStr,collecttime,dataguid+"170",listdata);  //封装为结果对象并添加到结果集集合中
		            
		            String spfxkxdStr=spfxkxd.toString().substring(0,spfxkxd.toString().length()-1)+"}"; //水平方向可信度  paramid :171
		            addSiteData(spfxkxdStr,collecttime,dataguid+"171",listdata);  //封装为结果对象并添加到结果集集合中
		            
		            String czfxkxdStr=czfxkxd.toString().substring(0,czfxkxd.toString().length()-1)+"}"; //垂直方向可信度  paramid :172
		            addSiteData(czfxkxdStr,collecttime,dataguid+"172",listdata);  //封装为结果对象并添加到结果集集合中
		            
		            String cn2Str=cn2.toString().substring(0,cn2.toString().length()-1)+"}";  //垂直方向Cn2  paramid :173
		            addSiteData(cn2Str,collecttime,dataguid+"173",listdata);  //封装为结果对象并添加到结果集集合中
		            
		            //将检测到的ROBS实时采样数据插入到数据库中
		          //  System.out.println("listdata.size:"+listdata.size());
		           // dataDao.addCollectDataTwo(listdata);
		         
		           // System.out.println("spfx:"+spfxStr);
		           // System.out.println("spfsStr:"+spfsStr);
		           // System.out.println("czfsStr:"+czfsStr);
		            //System.out.println("spfxkxdStr:"+spfxkxdStr);
		            //System.out.println("czfxkxdStr:"+czfxkxdStr);
		           // System.out.println("cn2Str:"+cn2Str);
		        } catch (FileNotFoundException e) {  
		            //System.out.println("file is not fond");  
		        	Logger.getLogger("").error("找不到此文件");
		        } catch (IOException e) {  
		            //System.out.println("Read or write Exceptioned");  
		        	Logger.getLogger("").error("Read or write Exceptioned");
		        }catch (Exception e) {  
		            e.printStackTrace();
		        }finally{             
		        	closed(in);
		        	//closed(out);
		     }  
	   }else{
		  // System.out.println("数据库已是最新时间");
	   }
	}
	
	/**
	 * 关闭读入流
	 * @param in
	 */
	public void closed( BufferedReader in){
		 if(null!=in){   
			 try {
				in.close();
			} catch (IOException e) {
				//System.out.println("关闭读入流失败");
				Logger.getLogger("").error("关闭读入流失败");
				e.printStackTrace();
			}
         }  
	}
	/**
	 * 关闭写出流
	 * @param out
	 */
	public void closed( BufferedWriter out){
		 if(null!=out){   
			 try {
				 out.close();
			} catch (IOException e) {
				Logger.getLogger("").error("关闭写出流失败");
				//System.out.println("关闭写出流失败");
				e.printStackTrace();
			}
        }  
	}
	
	/**
	 * 将结果集对象添加到集合中
	 * @param datavalue 参数对应的数据
	 * @param collecttime 监测时间
	 * @param list   要返回的结果集集合
	 * @param dataguid   dataguid
	 * @return
	 */
	public  List<SiteData> addSiteData(String datavalue,String collecttime,String dataguid,List<SiteData> list){
		//String dataguid="28_1414_75301414_";   //dataguid
		SiteData data = new SiteData();
		data.setCollecttime(collecttime);
		if(collecttime==null||"".equals(collecttime)){
			data.setCollecttime("19701111000000");  //时间为空则默认1970年
		}
		data.setDatavalue(datavalue);
		data.setDataguid(dataguid);
		list.add(data);
		return list;
		
	}
	
	@Autowired
	public BaseaddDao baseaddDao;
	
	/**
	 * 加载指定文件夹下的文件
	 * @param fileaddress
	 */
	public void loadFile(File file){
		//获取数据库中辐射监测数据的最新时间
		//String maxtime = getOOBSMaxTimeByDtid("yyyyMMddHH24MIss", "30", "1414");
		String maxtime = baseaddDao.getMaxTimeByDtid("yyyyMMddHH24MIss", "30", "1414");
		if(file.isDirectory()){
			//System.out.println("file  is a directory!");
		    File[] listFiles = file.listFiles();
		    for(File f:listFiles){
		    	if(f.isFile()&&f.length()>0){
		    		//System.out.println("file is file ");
		    		if(f.getName().contains("ROBS")){//每小时产品采样数据
		    			//System.out.println("file is contais ROBS ");
		    		readFile(f,maxtime);
		    		}
		    	}
		    	
		    }
	   }
		
	}
	
	
	/** 通过设备类型获取oobs监测数据最新时间*/
	public String getOOBSMaxTimeByDtid(String time_formt, String devicetypeid, String objid){
		Map<String, Object> map = new HashMap<String, Object>();
		if (time_formt != null && !"".equals(time_formt)) {
			map.put("time_formt", time_formt);//时间格式
		} else {
			map.put("time_formt", "yyyy-MM-dd HH24:MI:ss");//时间格式
		}
		map.put("devicetypeid", devicetypeid);
		//objid不传默认查询设备类型下所有对象监测数据的最新时间
		map.put("objid", objid);
		return dataDao.getOOBSMaxTimeByDtid(map);
	}
	
	@Autowired
	private SiteDataDao siteDao;
	
	@Autowired 
	private WeiBoService wb;
	
	/** 重写风廓线文件以调用exe文件生成风矢风羽图*/
	public String reWriteFKXfile(String objid,String qzbh){
		String paramid="168,169,170";
		Map<String,Object> paramap = new HashMap<String,Object>();
		paramap.put("objid", objid);
		Map<String,Object> zmap = siteDao.getDataguidByOid(paramap);//通过objid查询风廓线站点dataguid相关参数
		String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
		//获取数据库辐射数据近近10分钟时间（方法内部实现是取近10分钟）
		String[] colMinMaxTime = colMinMaxTimeByMinute("30", 12, objid, paramid, "");
		//String[] colMinMaxTime = colMinMaxTimeByHour("30", 14, objid, paramid, "");
		
		//按时间查询 格式：yyyy-MM-dd
		String begintime = colMinMaxTime[0];
		String endtime = colMinMaxTime[1];
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("begintime", begintime);
		map.put("endtime", endtime);
		map.put("objid", objid);
		//map.put("dataguid", "28_1414_75301414_");
		map.put("dataguid", dataguid);
		//查询水平风速，垂直风速，水平方向
		 List<Map<String, Object>> dataList = dataDao.reWriteFKXfile(map);
		// System.out.println("刚查出来的dataList.size:"+dataList.size());
		// String qzbh = siteDao.queryFkxQzbhByobjid(paramap); //根据objid查询风廓线区站编号
		 //String qzbh="58646";
		 fkxReWritePacking(dataList,qzbh);
		 return "success";
	}
	
	
	       
	/**
	 * 封装风廓线要重写的文件数据(生成风羽，风矢图片所需要的数据)
	 * @param dataList
	 * @param qzbh 区站编号
	 * @return
	 */
	public Map<String, Object> fkxReWritePacking(
		List<Map<String, Object>> dataList,String qzbh) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    //System.out.println("进入fkxReWritePacking：包装数据");
	    PrintWriter out = null;
		for(Map<String,Object> map:dataList){
			
			//for(Map.Entry<String, Object>zmap:map.entrySet()){
			List<FkXParam> fkxTxt = new ArrayList<FkXParam>(); //封装的参数集合和，一个List相当于一个txt文件
			 
			String collecttime =  map.get("collecttime").toString();
			//System.out.println("原始的collecttime:"+collecttime);
			String time = collecttime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
			//System.out.println("去除-和：还有空格后的时间:"+time);
			//System.out.println("spfx.value:"+map.get("spfx").toString());
			//水平风向Map
			LinkedHashMap<String, Object> spfxMap =  JSON.parseObject(map.get("spfx").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
			//水平风速Map
			LinkedHashMap<String, Object> spfsMap =  JSON.parseObject(map.get("spfs").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
			//垂直风速Map
			LinkedHashMap<String, Object> czfsMap =  JSON.parseObject(map.get("czfs").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
			
			Set<Entry<String, Object>> spfxSet = spfxMap.entrySet(); //水平风向Map
			Set<Entry<String, Object>> spfsSet = spfsMap.entrySet(); //水平风速Map
			Set<Entry<String, Object>> czfsSet = czfsMap.entrySet(); //垂直风速Map
			//System.out.println("spfxSet.size:"+spfxSet.size());
			//System.out.println("spfsSet.size:"+spfsSet.size());
			//System.out.println("czfsSet.size:"+czfsSet.size());
			//添加水平风向
			for(Entry<String, Object> fx:spfxSet){
				FkXParam fkxLine = new FkXParam(); //一个txt里的一行数据   -> height spfx spfs czfs
				String height = fx.getKey().split("_")[1];  //高度 spfx_120 ->120
				fkxLine.setHeight(height);
				//System.out.println("height:"+height);
				fkxLine.setSpfx(fx.getValue().toString()); 
				fkxTxt.add(fkxLine); //此处的每一行只加了height 和spfx 的值
			}
			//System.out.println("加完水平方向后的fkxTxt的长度："+fkxTxt.size());
			//添加水平风速
			int num=0;
			for(Entry<String, Object> spfs:spfsSet){
				if(num<fkxTxt.size()){ //这个size也就是相当于spfxSet的size
					FkXParam fkXParam = fkxTxt.get(num); //去集合中的第num个
					//System.out.println("spfs中param.getHeight:"+fkXParam.getHeight());
					//System.out.println("spfs的第"+num+"个height:"+spfs.getKey());
					if(spfs.getKey().contains(fkXParam.getHeight())){ //如果同等位数的spfs的高度和spfx一致 
						fkXParam.setSpfs(spfs.getValue().toString()); //给原先的每一行(spfx,height)基础上加上spfs	
					}else{
						fkXParam.setSpfs("/"); //如过同位置的spfs的高度和spfx的高度不一样。则设0
					}	
				}
				num++;
			}
			//添加垂直风速
			int num2=0;
			for(Entry<String, Object> czfs:czfsSet){
				if(num2<fkxTxt.size()){ //这个size也就是相当于spfxSet的size
					FkXParam fkXParam = fkxTxt.get(num2); //去集合中的第num个
					//System.out.println("czfs中param.getHeight:"+fkXParam.getHeight());
					//System.out.println("czfs的第"+num+"个height:"+czfs.getKey());
					if(czfs.getKey().contains(fkXParam.getHeight())){ //如果同等位数的czfs的高度和spfx一致 
						fkXParam.setCzfs(czfs.getValue().toString()); //给原先的每一行(spfx,height,spfs)基础上加上czfs	
					}else{
						fkXParam.setCzfs("/"); //如过同位置的czfs的高度和spfx的高度不一样。则设0
					}	
				}
				num2++;
			}
			//生成文件，写内容
			
			String dirpath = "E:\\indata\\FKX"+qzbh;
			File dir = new File(dirpath);
            if(!dir.exists()){
            	dir.mkdirs();
            }
            String fileName = qzbh+"_"+time+"_PB_ROBS.txt";
			try {
				out=new PrintWriter(new File(dirpath+"\\"+fileName));
				for(FkXParam param:fkxTxt){
					if(Integer.valueOf(param.getHeight())<=10000){//高度限定在10000米以下
						if(!param.getCzfs().contains("/")&&!param.getSpfs().contains("/")&&!param.getSpfx().contains("/")){
							//System.out.println("fkxparam:"+param);
							out.println(param.toString());	
						}
					}
				
				}
				out.flush();//刷新，将文本写入文件里
				File file = new File(dirpath+"\\"+fileName);
				if(file.length()<=0){ //如果文件为空
					//file.delete();
					out.println(0);
				}
				
			} catch (FileNotFoundException e) {
				Logger.getLogger("").error("找不到此文件");
				//System.out.println("找不到此文件");
				e.printStackTrace();
			}finally{
				if(out!=null){
					out.close();
				}
			}
			//System.out.println("----------------------------------分割线-------------------------------");
			//break; //不加break则为读全部文件，加break是为了读了第一个文件就结束，是为了调试而加
		//}
		}
		
		
		resultMap.put("message", "重写文件成功");
		return resultMap;
}
            /**
             * 一张图-风廓线监测-坐标信息及其他
             * @param request
             * @param string
             * @return
             */
			public Map<String, Object> getfkx(HttpServletRequest request,
					String string) {
				//给出默认值:默认给国站的
				int objtypeid = 28;
				//空间表名
				String space_tablename = "space_windprofile";
				
				
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
				
				Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
				if(!"".equals(city)||!"".equals(objid)){ //如果city参数为空，则不查,返回空
				//查辐射计站点基本信息
				List<ModelAssist> modelList = siteDao.getfkxInfoList(paramMap);
				//将返回的数据行转列
				List<Map<String,Object>> dataList=packLineData(modelList);
				//封装空间表数据及站点信息
				resultMap = baseService.getGeoJsonFormat(space_tablename, objid, dataList);
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
             * 一张图-风廓线监测-坐标信息及其他
             * @param request
             * @param string
             * @return   
             * new   (实况监测)
             */
			public Map<String, Object> getfkx_Two(HttpServletRequest request,
					String string) {
				//给出默认值:默认给国站的
				int objtypeid = 28;
				//空间表名
				String space_tablename = "space_windprofile";
				
				
				//传参Map
				Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
				
				paramMap.put("objtypeid", objtypeid);
				
				Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
			
				//查风廓线站点基本信息
				List<ModelAssist> modelList = siteDao.getfkxInfoList(paramMap);
				//将返回的数据行转列
				List<Map<String,Object>> dataList=packLineData(modelList);
				
				Map<String,Object> pamap = new HashMap<String,Object>();
				String height = request.getParameter("height")==null?"":request.getParameter("height");
				String collecttime = request.getParameter("collecttime")==null?"":request.getParameter("collecttime");
				pamap.put("height", height); //高度，应从request里获取
				pamap.put("collecttime", collecttime); //时间，应从request里获取
				
				List<String> fkxObjids = siteDao.getObjidByOtid(objtypeid);
				Map<String,Object> zMap = new HashMap<String,Object>();
				zMap.put("list", fkxObjids);
				System.out.println("fkxObjids:"+fkxObjids);
				
				
				List<String> data = siteDao.getAllfkxDataguids(zMap);//查找所有去重后的风廓线站点dataguid(spfx,spfs,czfs)
				
				pamap.put("list", fkxObjids);
				
				if(data!=null&&data.size()>0){
					StringBuilder dataguids = new StringBuilder();
					//dataguids.append("(");
					for(int i=0;i<data.size();i++){
						if(i==data.size()-1){
							dataguids.append("'"+data.get(i)+"'");  //最后一个不加，
						}else{
							dataguids.append("'"+data.get(i)+"',");
						}
						
					}
					//dataguids.append(")");
					
					String  dataStr= dataguids.toString();
					
					//System.out.println("dataStr:"+dataStr);
					
					pamap.put("dataguids", dataStr);
				
				//风廓线所有某高度某时间的风向风速信息
				List<Map<String, Object>> infoList = getFkxAllSitesInfo(pamap);
				//封装空间表数据及站点信息
				resultMap = baseService.getGeoJsonFormat_fkx(space_tablename, null, dataList,infoList);
				}
				return resultMap;
			}
			
			
			
			
			/**
			 * 风廓线-所有站点规定高度下各个时间的风速，风向信息
			 * new  -实况监测
			 * @param 高度，时间
			 */
			public List<Map<String, Object>> getFkxAllSitesInfo( Map<String,Object> parMap) {
				//Map<String,Object> pmap = new HashMap<String,Object>();
				String height=parMap.get("height").toString();
				/*String collecttime = parMap.get("collecttime").toString();
				pmap.put("collecttime", collecttime);*/
				 List<Map<String, Object>> dataList = siteDao.getFkxAllSitesInfo(parMap);
				 List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();//封装的需要返回的结果集
				 if(dataList!=null&&dataList.size()>0){
					
					 for(Map<String,Object>map:dataList){
						 Map<String,Object> resultMap = new HashMap<String,Object>();//单条结果集，封装固定高度的风速，风向，objid
						 String objid = map.get("objid").toString();
						// System.out.println("当前objid为:"+objid);
						 //resultMap.put("objid",objid);//添加objid
						 for(Map.Entry<String, Object>entry:map.entrySet()){ //遍历参数结果集
								
								if("czfs".equals(entry.getKey().toString())){
									String lastHt = "0"; //上个高度，目标高度前一个(比它小的)插值算法要用到
									String lastvl = "0"; //上个高度的值，目标高度前一个(比它小的)插值算法要用到
									//使解析后的数据有序，跟数据库存储的字符串顺序对应
									LinkedHashMap<String, Object> root=JSON.parseObject(entry.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
									for(Map.Entry<String, Object>spfxVal:root.entrySet()){ //垂直风速的各高度值
										String key = spfxVal.getKey().split("_")[1]; //高度
										String value = spfxVal.getValue().toString();
										//System.out.println("czfs_key:"+key);
										//System.out.println("czfs_value:"+value);
										 if(Integer.parseInt(height)<=Integer.parseInt(key)){
												
												//if(!value.contains("/")){
												 if(Integer.parseInt(height)==(Integer.parseInt(key))){
 													resultMap.put("czfs", value);
 													resultMap.put("objid",objid);//添加objid
 													///System.out.println("------------------start");
																
 													//System.out.println("wd_result:"+value);
     												//System.out.println("wd_key:"+key);
     												//System.out.println("wd_value:"+value);
     												//System.out.println("wd_height:"+height);
     												//System.out.println("------------------end");
 													break;
 												}else{
 													if("/".equals(lastvl)||"/".equals(value)){
 														 resultMap.put("czfs", value);
 	      												 resultMap.put("objid",objid);//添加objid
 	      												break;
 													}else{
 														//resultMap.put("wd", value);
 	     												float result =CalCZ.calCZ(lastHt, key, height, lastvl, value);
 	     												
 	     												 BigDecimal   b  =   new  BigDecimal(result);  
 	     												 result    =  b.setScale(3,  BigDecimal.ROUND_HALF_UP).floatValue(); //保留三位小数 
 	     												 resultMap.put("czfs", result);
 	      												 resultMap.put("objid",objid);//添加objid
 	     												//System.out.println("wd_result:"+result);
 	     												//System.out.println("wd_key:"+key);
 	     												//System.out.println("wd_value:"+value);
 	     												//System.out.println("wd_height:"+height);
 	     												break;
 													}
 													
 												}
												
												//}
											}else{
												lastHt=key; //上个高度
												lastvl =value; //上个高度的值
											}
										
									}
									//System.out.println("wd_lastHt:"+lastHt);
									//System.out.println("wd_lastvl:"+lastvl);
			
								}else if("spfs".equals(entry.getKey().toString())){
									String lastHt = "0"; //上个高度，目标高度前一个(比它小的)插值算法要用到
									String lastvl = "0"; //上个高度的值，目标高度前一个(比它小的)插值算法要用到
									//使解析后的数据有序，跟数据库存储的字符串顺序对应
									LinkedHashMap<String, Object> root=JSON.parseObject(entry.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
									for(Map.Entry<String, Object>spfxVal:root.entrySet()){ //水平风速的各高度值
										String key = spfxVal.getKey().split("_")[1]; //高度;
										String value = spfxVal.getValue().toString();
										//System.out.println("spfs_key:"+key);
										//System.out.println("spfs_value:"+value);
								
										 if(Integer.parseInt(height)<=Integer.parseInt(key)){
												
												//if(!value.contains("/")){
												 if(Integer.parseInt(height)==(Integer.parseInt(key))){
  													resultMap.put("spfs", value);
  													
  													//System.out.println("------------------start");
																	
  													//System.out.println("spfs_result:"+value);
      												//System.out.println("spfs_key:"+key);
      												//System.out.println("spfs_value:"+value);
      												//System.out.println("spfs_height:"+height);
      												//System.out.println("------------------end");
  													break;
  												}else{
  													
  													if("/".equals(lastvl)||"/".equals(value)){ //斜杠参与插值运算会报错
 														 resultMap.put("spfs", value);
 	      										
 	      												break;
 													}else{
  													//resultMap.put("wd", value);
      												float result =CalCZ.calCZ(lastHt, key, height, lastvl, value);
      												
      												 BigDecimal   b  =   new  BigDecimal(result);  
      												 result    =  b.setScale(3,  BigDecimal.ROUND_HALF_UP).floatValue(); //保留三位小数 
      												 resultMap.put("spfs", result);
       												
      												//System.out.println("spfs_result:"+result);
      												//System.out.println("spfs_key:"+key);
      												//System.out.println("spfs_value:"+value);
      												//System.out.println("spfs_height:"+height);
      												break;
  												}
  												}
												//}
											}else{
												lastHt=key; //上个高度
												lastvl =value; //上个高度的值
											}
									}
									//System.out.println("spfs_lastHt:"+lastHt);
									//System.out.println("spfs_lastvl:"+lastvl);
									
								}else if("spfx".equals(entry.getKey().toString())){
									String lastHt = "0"; //上个高度，目标高度前一个(比它小的)插值算法要用到
									String lastvl = "0"; //上个高度的值，目标高度前一个(比它小的)插值算法要用到
									//使解析后的数据有序，跟数据库存储的字符串顺序对应
									LinkedHashMap<String, Object> root=JSON.parseObject(entry.getValue().toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
									for(Map.Entry<String, Object>spfxVal:root.entrySet()){ //水平风向的各高度值
										String key = spfxVal.getKey().split("_")[1]; //高度;
										String value = spfxVal.getValue().toString();
										//System.out.println("spfx_key:"+key);
										//System.out.println("spfx_value:"+value);
								
										 if(Integer.parseInt(height)<=Integer.parseInt(key)){
												
												//if(!value.contains("/")){
												 if(Integer.parseInt(height)==(Integer.parseInt(key))){
  													resultMap.put("spfx", value);
  													
  													//System.out.println("------------------start");
																	
  													//System.out.println("spfx_result:"+value);
      												//System.out.println("spfx_key:"+key);
      												//System.out.println("spfx_value:"+value);
      												//System.out.println("spfx_height:"+height);
      												//System.out.println("------------------end");
  													break;
  												}else{
  													
  													if("/".equals(lastvl)||"/".equals(value)){ //斜杠参与插值运算会报错
 														 resultMap.put("spfx", value);
 	      										
 	      												break;
 													}else{
  													//resultMap.put("wd", value);
      												float result =CalCZ.calCZ(lastHt, key, height, lastvl, value);
      												
      												 BigDecimal   b  =   new  BigDecimal(result);  
      												 result    =  b.setScale(3,  BigDecimal.ROUND_HALF_UP).floatValue(); //保留三位小数 
      												 resultMap.put("spfx", result);
       												
      												//System.out.println("spfx_result:"+result);
      												//System.out.println("spfx_key:"+key);
      												//System.out.println("spfx_value:"+value);
      												//System.out.println("spfx_height:"+height);
      												break;
  												}
  												}
												//}
											}else{
												lastHt=key; //上个高度
												lastvl =value; //上个高度的值
											}
									}
									//System.out.println("spfx_lastHt:"+lastHt);
									//System.out.println("spfx_lastvl:"+lastvl);
									
								}
								
							}
						 resultList.add(resultMap);  //将单条结果（objid,spfx,spfs,czfs）插入结果集中
					 }
				 }
				/*Map<String,Object> tempMap = new HashMap<String,Object>();
				tempMap.put("data", resultList);*/
				return resultList;
			}
            
			//风廓线，获得风廓线最近俩个小时的时间刻度列表  new
			public List<String> getFkxHoursList(HttpServletRequest request) {
				Map<String, Object> map= new HashMap<String,Object>();
				
				return siteDao.getFkxHoursList(map);
			}
			
			
			/**
			 * 风廓线，获得各个时刻间隔下的风矢图（6分钟，30分钟，60分钟，120分钟）new2
			 */
			public Map<String, Object> getFkxFspic(HttpServletRequest request,String paramid) {
				 //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				
				Map<String,Object> paramp = new HashMap<String,Object>();
				paramp.put("objid", objid);
				 String qzbh = siteDao.queryFkxQzbhByobjid(paramp); //根据objid查询风廓线区站编号
				//获取数据库风廓线数据近2小时时间
				//String[] colMinMaxTime = colMinMaxTimeByHour("30", 2, objid, paramid, ""); //此处应查最新时间戳
				 Map<String,Object> kmap = new HashMap<String,Object>(); //要返回的各个时刻间隔的图片地址的集合
			     System.out.println("kmap.size:"+kmap.size());
			       
			     //-1则说明没有该时间段的风矢风羽图
				 kmap.put("fs6","-1");
				 kmap.put("fs30","-1");
				 kmap.put("fs60","-1");
			     kmap.put("fs120","-1");
			    
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramp);//通过objid查询风廓线站点dataguid相关参数
                if(zmap!=null){
                	//dataguid前缀
    				String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
    				zmap.put("dataguid", dataguid);
    				String zmaxTime = siteDao.getFkxMaxTime(zmap);  //获得风廓线某站点最新时间
    				
    				//按时间查询 格式：yyyy-MM-dd
    				String begintime = "";
    				String endtime =  "";
    				//是否有带时间查询（true则代表实时查询）
    				boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
    				if(isexist){//实时查询
    					
    					 //endtime   = colMinMaxTime[1] ;
    					 endtime   = zmaxTime ;
    				}else{//带时间参数的查询
    					
    					endtime = request.getParameter("endtime");
    					
    				}
    				
    				//System.out.println("endtime:"+endtime);
    			      
    			        Map<String,Object> pMap = new HashMap<String,Object>();
    			        pMap.put("devicetypeid", "30");
    			        pMap.put("objid", objid);
    			        pMap.put("paramid", paramid);
    			        pMap.put("objtypeid", "");
    			        pMap.put("endtime", endtime);
    			        //根据时间参数查询（5分钟内）数据库已有的最接近参数的时间
    			        String maxTime = siteDao.queryMaxTimeByTime(pMap);
    			       
    			        
    			        
    			       // System.out.println("maxTime:"+maxTime);
    			        if(maxTime!=null){
    			        	//String time = maxTime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
    			        	//System.out.println("time:"+time);
    			        	
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
				        	System.out.println("最新时间："+time);
				        	System.out.println("上个时间："+lastTime);
				        	
				        	
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
										System.out.println("urlPrifix:"+urlPrfix);
										System.out.println("webserver:"+webList.get(0));
										String fs6Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FS6.png";
										String fs30Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FS30.png";
										String fs60Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FS60.png";
										String fs120Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FS120.png";
										System.out.println("fs6Url:"+fs6Url);
										System.out.println("fs30Url:"+fs30Url);
										System.out.println("fs60Url:"+fs60Url);
										System.out.println("fs120Url:"+fs120Url);
										
										kmap.put("fs6",fs6Url);
										kmap.put("fs30",fs30Url);
										kmap.put("fs60",fs60Url);
										kmap.put("fs120",fs120Url);
										
										
									}else{
										System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
									}
				    			
				        	}else{ //设备端
				        		System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				    			//如果是设备端按照以前的写法
				    			System.out.println("为设备端：获取设备端的imgurl");
				        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
				    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
				        		System.out.println("urlPrefix:"+urlPrfix);
				        		
				        		  //获取风羽风矢图的路径
						        String imgdir = "E:\\outdata\\";
						        File dir = new File(imgdir);
						        if(!dir.exists()){
						        	dir.mkdirs();
						        }
						        String[] nameList = dir.list();
						        for(String name:nameList){
						        	
						        	if(name.contains(qzbh)&&name.contains("FS6.png")){
						        		
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fs6 = urlPrfix+"/img/"+name;
						        			kmap.put("fs6",fs6);
						        			System.out.println("设备端的FS6URl为："+fs6);		
						        			
						        		}
						        	}
						        	if(name.contains(qzbh)&&name.contains("FS30")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fs30 = urlPrfix+"/img/"+name;
						        			kmap.put("fs30",fs30);
						        			System.out.println("设备端的FS30URl为："+fs30);	
						        			
						        		}
						        		
						        	}
						        	if(name.contains(qzbh)&&name.contains("FS60")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fs60 = urlPrfix+"/img/"+name;
						        			kmap.put("fs60",fs60);
						        			System.out.println("设备端的FS60URl为："+fs60);	
						
						        		}
						        		
						        	}
						        	if(name.contains(qzbh)&&name.contains("FS120")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fs120 = urlPrfix+"/img/"+name;
						        			kmap.put("fs120",fs120);
						        			System.out.println("设备端的FS120URl为："+fs120);	
						        			
						        		}
						        		
						        	}
						        	
						        }
						        	
				        	}
				        	
				        	
				        	
				        	
				        	
				        
    			        } 
			     }
				
			
				return kmap;
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
			 * 空气质量objtypeid：实时数据(1,5),日数据(2,6)  new2
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
				String mintime = sdf.format(calendar.getTime());
				
				String[] times = {mintime,maxtime};
				
				return times;
			}
			
			
			
			/**
			 * 获取数据库监测数据最大时间(maxtime)和(maxtime-n分钟)的时间
			 * @param devicetypeid 设备类型id
			 * @param hour 小时
			 * @param objid 对象id
			 * @param paramid 参数id
			 * @param objtypeid 对象类型id
			 * @return {mintime,maxtime}
			 * 
			 * 空气质量paramid：1,29,2,30,3,21,4,22,5,23,6,24,7,97,28,8,9 -->
			 * 空气质量objtypeid：实时数据(1,5),日数据(2,6)  new2
			 */
			public  String[] colMinMaxTimeByMinute(String devicetypeid, int minute, String objid, String paramid, String objtypeid){
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

				calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-minute);//从数据库里取近10分钟的数据
				//calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-hour);//(maxtime-n小时)的时间
				String mintime = sdf.format(calendar.getTime());
				
				String[] times = {mintime,maxtime};
				
				return times;
			}
			
			
			
           
			
			/**
			 * 风廓线，获得各个时刻间隔下的风羽图（6分钟，30分钟，60分钟，120分钟）new2
			 */
			public Map<String, Object> getFkxFypic(HttpServletRequest request,
					String paramid) {
				 //必传
				String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				
				Map<String,Object> paramp = new HashMap<String,Object>();
				paramp.put("objid", objid);
				 String qzbh = siteDao.queryFkxQzbhByobjid(paramp); //根据objid查询风廓线区站编号
				//获取数据库风廓线数据近2小时时间
				//String[] colMinMaxTime = colMinMaxTimeByHour("30", 2, objid, paramid, ""); //此处应查最新时间戳
				Map<String,Object> kmap = new HashMap<String,Object>(); //要返回的各个时刻间隔的图片地址的集合
				//-1则说明没有该时间段的风羽图
		        kmap.put("fy6","-1");
		        kmap.put("fy30","-1");
		        kmap.put("fy60","-1");
		        kmap.put("fy120","-1");
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramp);//通过objid查询风廓线站点dataguid相关参数
				if(zmap!=null){
					//dataguid前缀
					String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
					zmap.put("dataguid", dataguid);
					zmap.put("objid", objid);
					String zmaxTime = siteDao.getFkxMaxTime(zmap);  //获得风廓线某站点最新时间
					
					//按时间查询 格式：yyyy-MM-dd
					String begintime = "";
					String endtime =  "";
					//是否有带时间查询（true则代表实时查询）
					boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
					if(isexist){//实时查询
						
						 //endtime   = colMinMaxTime[1] ;
						endtime   = zmaxTime ;
					}else{//带时间参数的查询
						
						endtime = request.getParameter("endtime");
						
					}
					
					System.out.println("endtime:"+endtime);
				      
				        Map<String,Object> pMap = new HashMap<String,Object>();
				        pMap.put("devicetypeid", "30");
				        pMap.put("objid", objid);
				        pMap.put("paramid", paramid);
				        pMap.put("objtypeid", "");
				        pMap.put("endtime", endtime);
				        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
				        String maxTime = siteDao.queryMaxTimeByTime(pMap);
				       
				        //System.out.println("kmap.size:"+kmap.size());
				       
				        	
				       
				        
				        //System.out.println("maxTime:"+maxTime);
				        if(maxTime!=null){
				        	//String time = maxTime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
				        	
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
				        	System.out.println("最新时间："+time);
				        	System.out.println("上个时间："+lastTime);
				        	
				        	
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
										System.out.println("urlPrifix:"+urlPrfix);
										System.out.println("webserver:"+webList.get(0));
										String fy6Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY6.png";
										String fy30Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY30.png";
										String fy60Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY60.png";
										String fy120Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY120.png";
										System.out.println("fy6Url:"+fy6Url);
										System.out.println("fy30Url:"+fy30Url);
										System.out.println("fy60Url:"+fy60Url);
										System.out.println("fy120Url:"+fy120Url);
										
										kmap.put("fy6",fy6Url);
										kmap.put("fy30",fy30Url);
										kmap.put("fy60",fy60Url);
										kmap.put("fy120",fy120Url);
										
										
									}else{
										System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
									}
				    			
				        	}else{ //设备端
				        		System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				    			//如果是设备端按照以前的写法
				    			System.out.println("为设备端：获取设备端的imgurl");
				        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
				    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
				        		System.out.println("urlPrefix:"+urlPrfix);
				        		
				        		  //获取风羽风矢图的路径
						        String imgdir = "E:\\outdata\\";
						        File dir = new File(imgdir);
						        if(!dir.exists()){
						        	dir.mkdirs();
						        }
						        String[] nameList = dir.list();
						        for(String name:nameList){
						        	
						        	if(name.contains(qzbh)&&name.contains("FY6.png")){
						        		
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy6 = urlPrfix+"/img/"+name;
						        			kmap.put("fy6",fy6);
						        			System.out.println("设备端的fy6URl为："+fy6);		
						        			
						        		}
						        	}
						        	if(name.contains(qzbh)&&name.contains("FY30")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy30 = urlPrfix+"/img/"+name;
						        			kmap.put("fy30",fy30);
						        			System.out.println("设备端的fy30URl为："+fy30);	
						        			
						        		}
						        		
						        	}
						        	if(name.contains(qzbh)&&name.contains("FY60")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy60 = urlPrfix+"/img/"+name;
						        			kmap.put("fy60",fy60);
						        			System.out.println("设备端的fy60URl为："+fy60);	
						
						        		}
						        		
						        	}
						        	if(name.contains(qzbh)&&name.contains("FY120")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy120 = urlPrfix+"/img/"+name;
						        			kmap.put("fy120",fy120);
						        			System.out.println("设备端的fy120URl为："+fy120);	
						        			
						        		}
						        		
						        	}
						        	
						        }
						   	}
				        	
				        	
				        	
				        	
				        
				        }
				}
			
				return kmap;
			}
			
			
			
			/**
			 * 从数据库获取最近的风廓线射计数据(对比分析)
			 * @param objid 站点objid
		     * @param isexist 时间参数是否为空或不存在
		     * @param ptime 时间参数
		     * @param type  要查的数据类型 
			 * @return
			 */
			public  Map<String,Object>  queryDbfxWp(String objid,boolean isexist,String ptime,String type, String paramid){
				        //必传
						//String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
						Map<String,Object> map = new HashMap<String,Object>();
						Map<String,Object> paramp = new HashMap<String,Object>();
						paramp.put("objid", objid);
						Map<String,Object> kmap = new HashMap<String,Object>();
						Map<String,Object> zmap = siteDao.getDataguidByOid(paramp);//通过objid查询风廓线站点dataguid相关参数
						if(zmap!=null){
							String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
							//获取数据库风廓线数据近12小时时间
							String[] colMinMaxTime = colMinMaxTimeByHour("30", 24, objid, paramid, "");
							
							//按时间查询 格式：yyyy-MM-dd
							String begintime = "";
							String endtime =  "";
							//是否有带时间查询（true则代表实时查询）
							//boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
							if(isexist){//实时查询
								 begintime =  colMinMaxTime[0];
								 endtime   = colMinMaxTime[1] ;
			                    
							}else{//带时间参数的查询
								
								//endtime = request.getParameter("endtime");
								endtime = ptime;
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
								calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-24);//取几小时的数据
								begintime = sdf.format(calendar.getTime());
								
							}
						
							map.put("begintime", begintime);
							map.put("endtime", endtime);
							map.put("objid", objid);
							map.put("dataguid", dataguid);
					        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
							List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
					      
					        /*Map<String,Object> pMap = new HashMap<String,Object>();
					        pMap.put("devicetypeid", "30");
					        pMap.put("objid", objid);
					        pMap.put("paramid", paramid);
					        pMap.put("objtypeid", "");
					        pMap.put("endtime", endtime);
					        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
					        String maxTime = siteDao.queryMaxTimeByTime(pMap);*/
							
							
					        if("spfs".equals(type)){ //地面湿度
								 dataList = siteDao.queryDbfxWpSpfs(map); //对比分析-水平风速
							}
							
							if("czfs".equals(type)){ //地面压强
								 dataList = siteDao.queryDbfxWpCzfs(map); //对比分析-垂直风速
							}
						
							if(dataList!=null&&dataList.size()>0){
							  //kmap = wbOtherKxPacking(dataList);  //封装从数据库获取出来的监测数据并返回
								 kmap = wpfDbfxPacking(dataList,type,1);  //对比分析中其他数据的封装 
							}
							
							if(kmap.size()>0){ //如果该站点有数据，再查相关站点信息
								//站点信息
								
								Map<String,Object> infoMap = siteDao.findAllFkx(map)==null?null:siteDao.findAllFkx(map).get(0); //此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点
								
								kmap.put("siteInfo", "");
								if(infoMap!=null){
									String qzbh = siteDao.queryFkxQzbhByobjid(paramp);//根据objid查询区站编号
									infoMap.put("qzbh", qzbh);
									kmap.put("siteInfo",infoMap);
								}
							}
						}
							
							//zmap.put("ktd",kmap);
							return  kmap;
			
						
			}
			
			
			
			/**
			 * 从数据库获取最近的风廓线射计数据(对比分析) 廓线数据，单个时间点
			 * @param objid 站点objid
		     * @param isexist 时间参数是否为空或不存在
		     * @param ptime 时间参数
		     * @param type  要查的数据类型 
			 * @return
			 */
			public  Map<String,Object>  queryDbfxKxWp(String objid,boolean isexist,String ptime,String type, String paramid){
				        //必传
						//String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
						Map<String,Object> map = new HashMap<String,Object>();
						Map<String,Object> paramp = new HashMap<String,Object>();
						paramp.put("objid", objid);
						Map<String,Object> kmap = new HashMap<String,Object>();
						Map<String,Object> zmap = siteDao.getDataguidByOid(paramp);//通过objid查询风廓线站点dataguid相关参数
						if(zmap!=null){
							String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
							//获取数据库风廓线数据近2小时时间
							String[] colMinMaxTime = colMinMaxTimeByHour("30", 2, objid, paramid, "");
							
							//按时间查询 格式：yyyy-MM-dd
							String begintime = "";
							String endtime =  "";
							//是否有带时间查询（true则代表实时查询）
							//boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
							if(isexist){//实时查询
								 begintime =  colMinMaxTime[0];
								 endtime   = colMinMaxTime[1] ;
			                    
							}else{//带时间参数的查询
								
								//endtime = request.getParameter("endtime");
								endtime = ptime;
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
						
							map.put("begintime", begintime);
							map.put("endtime", endtime);
							map.put("objid", objid);
							map.put("dataguid", dataguid);
					        //List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
							List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
					      
					        /*Map<String,Object> pMap = new HashMap<String,Object>();
					        pMap.put("devicetypeid", "30");
					        pMap.put("objid", objid);
					        pMap.put("paramid", paramid);
					        pMap.put("objtypeid", "");
					        pMap.put("endtime", endtime);
					        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
					        String maxTime = siteDao.queryMaxTimeByTime(pMap);*/
							
							
					        if("spfs".equals(type)){ //水平风速
								 dataList = siteDao.queryDbfxKxWpSpfs(map); //对比分析-水平风速
							}
							
							if("czfs".equals(type)){ //垂直风速
								 dataList = siteDao.queryDbfxKxWpCzfs(map); //对比分析-垂直风速
							}
							
							if("spfx".equals(type)){ //水平风向
								 dataList = siteDao.queryDbfxKxWpSpfx(map); //对比分析-水平风向
							}
						
							if(dataList!=null&&dataList.size()>0){
							  //kmap = wbOtherKxPacking(dataList);  //封装从数据库获取出来的监测数据并返回
								 kmap = wpfDbfxPacking(dataList,type,0);  //对比分析中其他数据的封装
							}
							
							if(kmap.size()>0){ //如果该站点有数据，再查相关站点信息
								//站点信息
								
								Map<String,Object> infoMap = siteDao.findAllFkx(map)==null?null:siteDao.findAllFkx(map).get(0); //此接口返回的是个集合，不传参返回所有风廓线站点，传objid返回某个站点
								
								kmap.put("siteInfo", "");
								if(infoMap!=null){
									String qzbh = siteDao.queryFkxQzbhByobjid(paramp);//根据objid查询区站编号
									infoMap.put("qzbh", qzbh);
									kmap.put("siteInfo",infoMap);
								}
							}
						}
							
							//zmap.put("ktd",kmap);
							return  kmap;
			
						
			}
			
			
			
			//对比分析-封装从数据库获取出来的风廓线监测数据并返回
			/**
			 * 
			 * @param dataList
			 * @param type 类型
			 * @param flag 廓线（0）还是二维（1）
			 * @return
			 */
			public Map<String, Object> wpfDbfxPacking(
				List<Map<String, Object>> dataList,String type,int flag) {
				Map<String,Object> resultMap = new HashMap<String,Object>();
				//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();  //spfx
				
				
				for(Map<String,Object> map:dataList){
					Map<String,Object> map1=new HashMap<String,Object>();  //kx_bjcwdkx
					
					
					map1.put("at", map.get("collecttime").toString());
					
					
					//LinkedHashMap<String, Object> dataMap =  JSON.parseObject(map.get("czfs").toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					LinkedHashMap<String, Object> dataMap =  JSON.parseObject(map.get(type).toString(),new TypeReference<LinkedHashMap<String, Object>>(){} );
					
					
					Set<String> highList = new HashSet<String>();  //各指标中含有/的高度，将被舍去掉
					
					Set<Entry<String, Object>> dataSet = dataMap.entrySet(); //
					
					
					//以下指标中含有/的高度，将被舍去掉（spfs,czfs,spfx）
					for(Entry<String, Object> fx:dataSet){
						 
						if("/".equals(fx.getValue().toString())){  //如果值为 /， 记录该高度
							highList.add(fx.getKey().split("_")[1]);
						}
						
					}
					
					
					//以下是真正取值了，不含/的
					if(highList.size()==0){ //随便添加一个不存在的高度，是为了防止当此步的值为0时，后面的不遍历
						highList.add("20");
					}
					//System.out.println("highlist.size:"+highList.size());
					//System.out.println("highlist:"+highList);
					
					Map<String,Object> zmap1=new LinkedHashMap<String,Object>();  //
					
					
						for(Entry<String, Object> fx:dataSet){//内层为各指标
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
						
						
					/*
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
					}*/
					list1.add(map1);
					
				}
				if(list1.size()>0){
					//resultMap.put("data", list1);
					if(flag==1){ //二维的
						resultMap.put("data", list1);	
					}else{//廓线的
						resultMap.put("data", list1.get(0));
					}
					
				}
				
				
				return resultMap;
		}
			
			
			/**
			 * @param objid 站点objid
		     * @param isexist 时间参数是否为空或不存在
		     * @param ptime 时间参数
		     * @param type  要查的数据类型 
			 * 对比分析，获得各个时刻间隔下的风羽图（6分钟，30分钟，60分钟，120分钟）new2
			 */
			public Map<String, Object> getDbfxFypic(HttpServletRequest request,String objid,boolean isexist,String ptime,String type, String paramid) {
				 //必传
				//String objid = (request.getParameter("objid") == null || "".equals(request.getParameter("objid"))) ? "0" : request.getParameter("objid");
				
				Map<String,Object> paramp = new HashMap<String,Object>();
				 paramp.put("objid", objid);
				 String qzbh = siteDao.queryFkxQzbhByobjid(paramp); //根据objid查询风廓线区站编号
				//获取数据库风廓线数据近2小时时间
				//String[] colMinMaxTime = colMinMaxTimeByHour("30", 2, objid, paramid, ""); //此处应查最新时间戳
				Map<String,Object> kmap = new HashMap<String,Object>(); //要返回的各个时刻间隔的图片地址的集合
				//-1则说明没有该时间段的风羽图
		        kmap.put("fy6","-1");
		        kmap.put("fy30","-1");
		        kmap.put("fy60","-1");
		        kmap.put("fy120","-1");
		        kmap.put("siteInfo", "");
				Map<String,Object> zmap = siteDao.getDataguidByOid(paramp);//通过objid查询风廓线站点dataguid相关参数
				if(zmap!=null){
					//dataguid前缀
					String dataguid = zmap.get("objtypeid").toString()+"_"+zmap.get("objid").toString()+"_"+zmap.get("devicenumber").toString()+"_";
					zmap.put("dataguid", dataguid);
					String zmaxTime = siteDao.getFkxMaxTime(zmap);  //获得风廓线某站点最新时间
					
					//按时间查询 格式：yyyy-MM-dd
					String begintime = "";
					String endtime =  "";
					//是否有带时间查询（true则代表实时查询）
					//boolean isexist = request.getParameter("endtime") == null || "".equals(request.getParameter("endtime"));
					if(isexist){//实时查询
						
						 //endtime   = colMinMaxTime[1] ;
						endtime   = zmaxTime ;
					}else{//带时间参数的查询
						
						endtime = ptime;
						
					}
					
					//System.out.println("endtime:"+endtime);
				      
				        Map<String,Object> pMap = new HashMap<String,Object>();
				        pMap.put("devicetypeid", "30");
				        pMap.put("objid", objid);
				        pMap.put("paramid", paramid);
				        pMap.put("objtypeid", "");
				        pMap.put("endtime", endtime);
				        //根据时间参数查询（6分钟内）数据库已有的最接近参数的时间
				        String maxTime = siteDao.queryMaxTimeByTime(pMap);
				       // System.out.println("kmap.size:"+kmap.size());
				       
				        //System.out.println("maxTime:"+maxTime);
				       
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
				        	System.out.println("最新时间："+time);
				        	System.out.println("上个时间："+lastTime);
				        	//String time = maxTime.replace("-", "").replace(":", "").replace(" ", "").substring(0,12);
				        	//System.out.println("time:"+time);
				        	
				        	
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
										System.out.println("urlPrifix:"+urlPrfix);
										System.out.println("webserver:"+webList.get(0));
										String fy6Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY6.png";
										String fy30Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY30.png";
										String fy60Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY60.png";
										String fy120Url = urlPrfix+"/img/"+qzbh+"_"+time+"_FY120.png";
										System.out.println("fy6Url:"+fy6Url);
										System.out.println("fy30Url:"+fy30Url);
										System.out.println("fy60Url:"+fy60Url);
										System.out.println("fy120Url:"+fy120Url);
										
										kmap.put("fy6",fy6Url);
										kmap.put("fy30",fy30Url);
										kmap.put("fy60",fy60Url);
										kmap.put("fy120",fy120Url);
										
										
									}else{
										System.out.println("没获取到"+objid+"的web服务器信息,所以也没获取到图片的地址信息");
									}
				    			
				        	}else{ //设备端
				        		System.out.println(platform.get("isplatform").toString()+":"+platform.get("showname").toString());
				    			//如果是设备端按照以前的写法
				    			System.out.println("为设备端：获取设备端的imgurl");
				        		//String urlPrfix = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
				    			String urlPrfix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
				        		System.out.println("urlPrefix:"+urlPrfix);
				        		
				        		  //获取风羽风矢图的路径
						        String imgdir = "E:\\outdata\\";
						        File dir = new File(imgdir);
						        if(!dir.exists()){
						        	dir.mkdirs();
						        }
						        String[] nameList = dir.list();
						        for(String name:nameList){
						        	
						        	if(name.contains(qzbh)&&name.contains("FY6.png")){
						        		
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy6 = urlPrfix+"/img/"+name;
						        			kmap.put("fy6",fy6);
						        			System.out.println("设备端的fy6URl为："+fy6);		
						        			
						        		}
						        	}
						        	if(name.contains(qzbh)&&name.contains("FY30")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy30 = urlPrfix+"/img/"+name;
						        			kmap.put("fy30",fy30);
						        			System.out.println("设备端的fy30URl为："+fy30);	
						        			
						        		}
						        		
						        	}
						        	if(name.contains(qzbh)&&name.contains("FY60")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy60 = urlPrfix+"/img/"+name;
						        			kmap.put("fy60",fy60);
						        			System.out.println("设备端的fy60URl为："+fy60);	
						
						        		}
						        		
						        	}
						        	if(name.contains(qzbh)&&name.contains("FY120")){
						        		if(name.contains(time)||name.contains(lastTime)){
						        			String fy120 = urlPrfix+"/img/"+name;
						        			kmap.put("fy120",fy120);
						        			System.out.println("设备端的fy120URl为："+fy120);	
						        			
						        		}
						        		
						        	}
						        	
						        }
						   	}
				        	
					        

							//站点信息
							
							Map<String,Object> infoMap = siteDao.findAllFkx(paramp)==null?null:siteDao.findAllFkx(paramp).get(0); //此接口返回的是个集合，不传参返回所有辐射计站点，传objid返回某个站点
						
							if(infoMap!=null){
								//String qzbh = siteDao.queryFkxQzbhByobjid(paramp);//根据objid查询区站编号
								infoMap.put("qzbh", qzbh);
								kmap.put("siteInfo",infoMap);
							}
				        }
				      
				}
			
				return kmap;
			}
		
		

}
