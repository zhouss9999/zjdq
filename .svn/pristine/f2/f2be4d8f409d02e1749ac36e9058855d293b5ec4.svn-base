package wy.qingdao_atmosphere.weatheranalyze.web;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


/**
 * @author xsq
 * @description
 * 
 */
public class TestWeather {
	
	private final String ip = "121.196.198.37";
	private final int port = 22;
	private final String username = "wyftp";
	private final String password = "wy!@#456";
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度和经纬度获取网格数据信息
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param lonlat 经纬度
	 * @param forecastTime 时间
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWRFOrCALPUFGriddingDataInfo(String weathertype, String lonlat, String forecastTime, double height, String patternType){
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if(!"".equals(weathertype) && !"".equals(lonlat) && !"".equals(forecastTime) && !"".equals(patternType)){
			try {
				//ftp连接
				FTPClient ftpClient = ftpConnection(ip, port, username, password);
				//ftp目录串
				String ftpLatLonUrl = "";
				//ftp网格数据路径
				String ftpGridDataUrl = "";
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap") || weathertype.equals("TemperatureMap")){
						ftpGridDataUrl = "/WRF/"+weathertype+"/"+forecastTime.substring(0, 8)+"08/"+getHeightFolder(height, patternType)+"GFS"+forecastTime+".txt";
					}else if(weathertype.equals("PressureMap") || weathertype.equals("RHMap")){
						ftpGridDataUrl = "/WRF/"+weathertype+"/"+forecastTime.substring(0, 8)+"08/"+"GFS"+forecastTime+".txt";
					}else if(weathertype.equals("RainMap")){
						//传入规定格式时间，相加一个小时的算法
						SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHH"); 
						Date date = formatDate.parse(forecastTime); 
						//date的毫秒数 
						Long nowValue = date.getTime();
						//date加一个小时的毫秒数 
						Long afterHour = nowValue+60*60*1000;
						Date afterHourDate = new Date(afterHour);
						String rainfilename = "GFS"+forecastTime+"T"+formatDate.format(afterHourDate)+".txt";
						ftpGridDataUrl = "/WRF/"+weathertype+"/"+forecastTime.substring(0, 8)+"08/"+rainfilename;
					}
				}else if(patternType.equals("CALPUF")){
					//CALPUF格式日期目录处理
					String calpufdate = forecastTime.substring(0, 4)+"_M"+forecastTime.substring(4, 6)+"_D"+forecastTime.substring(6, 8)+"_0700/";
					//CALPUF格式文件名处理
					String calpufFileName = forecastTime.substring(0, 4)+"_M"+forecastTime.substring(4, 6)+"_D"+forecastTime.substring(6, 8)+"_"+forecastTime.substring(8, 10)+"00";
					if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
						ftpLatLonUrl = "/CALPUF/conc/latlon";
						ftpGridDataUrl = "/CALPUF/conc/"+weathertype+"/"+calpufdate+calpufFileName+".txt";
					}else if(weathertype.equals("temp") || weathertype.equals("wind")){
						ftpLatLonUrl = "/CALPUF/"+weathertype+"/latlon";
						ftpGridDataUrl = "/CALPUF/"+weathertype+"/"+getHeightFolder(height, patternType)+calpufdate+calpufFileName;
					}
				}
				String lat = lonlat.split(",")[1]; //纬度
				String lon = lonlat.split(",")[0]; //经度
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap")){
						//根据经纬度查找WRF模式下最近的网格的下标
						int index  = Integer.parseInt(getIndexByWRFGridLonlat(lonlat).get("index").toString());
						if(index > -1){
							//读取风向网格文件数据
							String gridfileconWDR = readFile(ftpClient, ftpGridDataUrl.substring(0, ftpGridDataUrl.length() - 4)+".wdr");
							//读取风速网格文件数据
							String gridfileconWSP = readFile(ftpClient, ftpGridDataUrl.substring(0, ftpGridDataUrl.length() - 4)+".wsp");
							String[] gridDataWDR = gridfileconWDR.replaceAll(" +", "").split(";");
							String[] gridDataWSP = gridfileconWSP.replaceAll(" +", "").split(";");
							//风向度数
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							double wdrdata = Double.parseDouble(gridDataWDR[index - 1]);
							//风向
							String wdr = "无";
							//风向判断
							if((wdrdata >= 348.76 && wdrdata <= 360) || (wdrdata >= 0 && wdrdata <= 11.25)){
								wdr = "北风";
							}else if(wdrdata >= 33.76 && wdrdata <= 56.25){
								wdr = "东北风";
							}else if(wdrdata >= 78.76 && wdrdata <= 101.25){
								wdr = "东风";
							}else if(wdrdata >= 123.76 && wdrdata <= 146.25){
								wdr = "东南风";
							}else if(wdrdata >= 168.76 && wdrdata <= 191.25){
								wdr = "南风";
							}else if(wdrdata >= 213.76 && wdrdata <= 236.25){
								wdr = "西南风";
							}else if(wdrdata >= 258.76 && wdrdata <= 281.25){
								wdr = "西风";
							}else if(wdrdata >= 303.76 && wdrdata <= 326.25){
								wdr = "西北风";
							}
							Map<String, Object> map = new LinkedHashMap<String, Object>();
							map.put("风向", wdr);
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							map.put("风速", gridDataWSP[index - 1]);
							resultMap.put("result", map);
							//网格左下角经纬度
							resultMap.put("returnZXLonlat", getIndexByWRFGridLonlat(lonlat).get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", getIndexByWRFGridLonlat(lonlat).get("returnYSLonlat").toString());
						}else{
							resultMap.put("result", "无");
							resultMap.put("returnZXLonlat", getIndexByWRFGridLonlat(lonlat).get("returnZXLonlat").toString());
							resultMap.put("returnYSLonlat", getIndexByWRFGridLonlat(lonlat).get("returnZXLonlat").toString());
						}
					}else{
						//读取网格文件数据
						String gridfilecon = readFile(ftpClient, ftpGridDataUrl);
						//去掉数据空格按分号分隔每组数据
						String[] gridData = gridfilecon.replace(" ", "").split(";");
						//根据经纬度查找WRF模式下最近的网格的下标
						int index  = Integer.parseInt(getIndexByWRFGridLonlat(lonlat).get("index").toString());
						if(index > -1){
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							resultMap.put("result", gridData[index - 1]);
							//网格左下角经纬度
							resultMap.put("returnZXLonlat", getIndexByWRFGridLonlat(lonlat).get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", getIndexByWRFGridLonlat(lonlat).get("returnYSLonlat").toString());
						}else{
							resultMap.put("result", "无");
							resultMap.put("returnZXLonlat", getIndexByWRFGridLonlat(lonlat).get("returnZXLonlat").toString());
							resultMap.put("returnYSLonlat", getIndexByWRFGridLonlat(lonlat).get("returnZXLonlat").toString());
						}
					}
				}else if(patternType.equals("CALPUF")){
					//读取经纬度文件数据
					String latlonfilecon = readLatLonFile(ftpClient, ftpLatLonUrl);
					//分隔每组经纬度
					String[] content = latlonfilecon.split(";");
					//去除空数据的list
					List<String> disList = new ArrayList<String>();
					for(int i = 0; i < content.length; i++){
						if(! content[i].equals(" ")){
							disList.add(content[i]);
						}
					}
					//初始化经纬度数组
					String[] latarr = new String[disList.size()];
					String[] lonarr = new String[disList.size()];
					//格式化经度不足六位的经纬度，不够自动补0
					DecimalFormat df = new DecimalFormat("0.000000");
					//拆分每组经纬度，按空格分隔，分别存进经度数组和纬度数组
					for(int i = 0; i < disList.size(); i++){
						latarr[i] = df.format(Double.parseDouble(disList.get(i).replaceAll(" +", " ").split(" ")[0])).toString();
						lonarr[i] = df.format(Double.parseDouble(disList.get(i).replaceAll(" +", " ").split(" ")[1])).toString();
					}
					//模糊查找匹配的经纬度组合的下标，index找不到会返回-1
					int index  = dimGetLonlatIndex(lonarr, latarr, lon, lat);
					if(index > -1){
						if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
							//读取网格文件数据
							String gridfilecon = readFile(ftpClient, ftpGridDataUrl);
							//将所有的空格替换为1个空格按分号分隔每组数据
							String[] gridData = gridfilecon.replaceAll(" +", " ").split(";");
							resultMap.put("result", gridData[index].split(" ")[1]);
						}else if(weathertype.equals("temp")){
							//读取网格文件数据
							String gridfilecon = readFile(ftpClient, ftpGridDataUrl+".temp");
							//将所有的空格替换为1个空格按分号分隔每组数据
							String[] gridData = gridfilecon.replaceAll(" +", "").split(";");
							resultMap.put("result", gridData[index]);
						}else if(weathertype.equals("wind")){
							//读取风向网格文件数据
							String gridfileconWDR = readFile(ftpClient, ftpGridDataUrl+".wdr");
							//读取风速网格文件数据
							String gridfileconWSP = readFile(ftpClient, ftpGridDataUrl+".wsp");
							String[] gridDataWDR = gridfileconWDR.replaceAll(" +", "").split(";");
							String[] gridDataWSP = gridfileconWSP.replaceAll(" +", "").split(";");
							//风向度数
							double wdrdata = Double.parseDouble(gridDataWDR[index]);
							//风向
							String wdr = "无";
							//风向判断
							if((wdrdata >= 348.76 && wdrdata <= 360) || (wdrdata >= 0 && wdrdata <= 11.25)){
								wdr = "北风";
							}else if(wdrdata >= 33.76 && wdrdata <= 56.25){
								wdr = "东北风";
							}else if(wdrdata >= 78.76 && wdrdata <= 101.25){
								wdr = "东风";
							}else if(wdrdata >= 123.76 && wdrdata <= 146.25){
								wdr = "东南风";
							}else if(wdrdata >= 168.76 && wdrdata <= 191.25){
								wdr = "南风";
							}else if(wdrdata >= 213.76 && wdrdata <= 236.25){
								wdr = "西南风";
							}else if(wdrdata >= 258.76 && wdrdata <= 281.25){
								wdr = "西风";
							}else if(wdrdata >= 303.76 && wdrdata <= 326.25){
								wdr = "西北风";
							}
							Map<String, Object> map = new LinkedHashMap<String, Object>();
							map.put("风向", wdr);
							map.put("风速", gridDataWSP[index]);
							resultMap.put("result", map);
						}
					}else{
						resultMap.put("result", "无");
					}
				}
				ftpClose(ftpClient);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("result", "无");
			}
		}
		return resultMap;
	}
	
	/**
	 * 获取WRF模式或者CALPUF模式下的高层目录
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return
	 */
	public static String getHeightFolder(double height, String patternType){
		String hf = "";
		if(patternType.equals("CALPUF")){
			if(height >= 0 && height <= 20){
				hf = "h0/";
			}else if(height > 20 && height <= 40){
				hf = "h1/";
			}else if(height > 40 && height <= 80){
				hf = "h2/";
			}else if(height > 80 && height <= 160){
				hf = "h3/";
			}else if(height > 160 && height <= 320){
				hf = "h4/";
			}else if(height > 320 && height <= 640){
				hf = "h5/";
			}else if(height > 640 && height <= 1000){
				hf = "h6/";
			}else if(height > 1000 && height <= 1500){
				hf = "h7/";
			}else if(height > 1500 && height <= 2200){
				hf = "h8/";
			}else if(height > 2200 && height <= 3000){
				hf = "h9/";
			}else if(height > 3000){
				hf = "h9/";
			}
		}else if(patternType.equals("WRF")){
			if(height >= 0 && height <= 40.583344){
				hf = "H0/";
			}else if(height > 40.583344 && height <= 97.787079){
				hf = "H1/";
			}else if(height > 97.787079 && height <= 163.710526){
				hf = "H2/";
			}else if(height > 163.710526 && height <= 246.891144){
				hf = "H3/";
			}else if(height > 246.891144 && height <= 364.695068){
				hf = "H4/";
			}else if(height > 364.695068 && height <= 518.181641){
				hf = "H5/";
			}else if(height > 518.181641 && height <= 708.659912){
				hf = "H6/";
			}else if(height > 708.659912 && height <= 911.105591){
				hf = "H7/";
			}else if(height > 911.105591 && height <= 1135.059082){
				hf = "H8/";
			}else if(height > 1135.059082 && height <= 1400.102905){
				hf = "H9/";
			}else if(height > 1400.102905 && height <= 1689.964233){
				hf = "H10/";
			}else if(height > 1689.964233 && height <= 1986.999390){
				hf = "H11/";
			}else if(height > 1986.999390 && height <= 2311.648682){
				hf = "H12/";
			}else if(height > 2311.648682 && height <= 2738.505371){
				hf = "H13/";
			}else if(height > 2738.505371 && height <= 3312.104004){
				hf = "H14/";
			}else if(height > 3312.104004 && height <= 4056.484375){
				hf = "H15/";
			}else if(height > 4056.484375 && height <= 5064.833008){
				hf = "H16/";
			}else if(height > 5064.833008 && height <= 6464.004883){
				hf = "H17/";
			}else if(height > 6464.004883 && height <= 8107.146973){
				hf = "H18/";
			}else if(height > 8107.146973 && height <= 10104.801758){
				hf = "H19/";
			}else if(height > 10104.801758 && height <= 12123.376953){
				hf = "H20/";
			}else if(height > 12123.376953 && height <= 14357.728516){
				hf = "H21/";
			}else if(height > 14357.728516){
				hf = "H22/";
			}
		}
		return hf;
	}
	
	/**
	 * 根据经纬度查找WRF模式下最近的网格的下标
	 * @param lonlat 经纬度
	 * @return index 返回第多少个格子 returnZXLonlat 方格左下角经纬度  returnYSLonlat 方格右上角经纬度
	 */
	public static Map<String, Object> getIndexByWRFGridLonlat(String lonlat){
		Map<String, Object> map = new HashMap<String, Object>();
		String lat = lonlat.split(",")[1]; //纬度
		String lon = lonlat.split(",")[0]; //经度
		//给出大网格左下角经纬度，右上角经纬度，造出58行57列的小网格经纬度数据
		double[][] lonarr = new double[58][57]; //初始化经度二维数组
		double[][] latarr = new double[58][57]; //初始化纬度二维数组
		double zxlon = 118.683899; //左下角经度
		double zxlat = 35.244465;  //左下角纬度
		double yslon = 121.432983; //右上角经度
		double yslat = 37.490185;  //右上角纬度
		double jdc = (yslon - zxlon)/56; //经度差:右上角经度减去左下角经度除以网格的列数-1(列数减一的处理是为了递增的时候原始的经度第一次不用做递增，存储在下标0)
		double wdc = (yslat - zxlat)/57; //纬度差:右上角纬度减去左下角纬度除以网格的行数-1(行数减一的处理是为了递增的时候原始的纬度第一次不用做递增，存储在下标0)
		//循环递增数据，一行内的纬度一致，经度递增，不同行的纬度递增
		for(int i = 0; i < 58; i++){
			double x = zxlon;
			double y = i > 0 ? zxlat += wdc : zxlat;
			for(int j = 0; j < 57; j++){
				lonarr[i][j] = j > 0 ? x += jdc : x;
				latarr[i][j] = y;
			}
		}
		int y = -1; //纬度下标
		int x = -1; //经度下标
		int index = -1; //返回第几个网格
		double returnLon = 0; //返回经度
		double returnLat = 0; //返回纬度
		String returnZXLonlat = "0,0"; //方格左下角经纬度
		String returnYSLonlat = "0,0"; //方格右上角经纬度
		//算出离纬度最近的下标
		for(int i = 0; i < latarr.length; i++){
			if(latarr[i][0] < (Double.parseDouble(lat) + wdc/2) && latarr[i][0] > (Double.parseDouble(lat) - wdc/2)){
				y = i;
				returnLat = latarr[i][0];
				break;
			}
		}
		//算出离经度最近的下标
		for(int i = 0; i < lonarr[0].length; i++){
			if(lonarr[0][i] < (Double.parseDouble(lon) + jdc/2) && lonarr[0][i] > (Double.parseDouble(lon) - jdc/2)){
				x = i;
				returnLon = lonarr[0][i];
				break;
			}
		}
		if(returnLon != 0 && returnLat != 0){
			//方格左下角经纬度,返回的经度和纬度分别 减去网格经度差和网格纬度差
			returnZXLonlat = (returnLon - jdc) +","+ (returnLat - wdc);
			//方格右上角经纬度,返回的经度和纬度分别 加上网格经度差和网格纬度差
			returnYSLonlat = (returnLon + jdc) +","+ (returnLat + wdc);
		}
		if(x != -1 && y != -1){
			//根据纬度确定的行数和经度确定的列数，来计算是第多少个第格子，公式：(行数下标 + 1) * 列数 - (列数 - (列数下标 + 1))
			index = (y + 1) * 57 - (57 - (x + 1));
		}
		map.put("returnZXLonlat", returnZXLonlat);
		map.put("returnYSLonlat", returnYSLonlat);
		map.put("index", index);
		return map;
	}
	
	/**
	 * 模糊查找匹配经纬度
	 * @param lonarr 经度数组
	 * @param latarr 纬度数组 
	 * @param lon 经度
	 * @param lat 纬度
	 * @return index 返回匹配的下标
	 */
	public static int dimGetLonlatIndex(String[] lonarr, String[] latarr, String lon, String lat){
		int index = -1;
		//循环截取经纬度后面的位数和传进来的经纬度进行模糊匹配
		getindex:for(int i = 0; i < 5; i++){
			for(int j = 0; j < latarr.length; j++){
				//相等直接返回下标
				if(lat.equals(latarr[j]) && lon.equals(lonarr[j])){
					index = j;
					break getindex;
				}else{
					String wd = latarr[j].substring(0, latarr[j].length()-(i+1));
					String jd = lonarr[j].substring(0, lonarr[j].length()-(i+1));
					if(lat.substring(0, lat.length()-(i+1)).equals(wd) && lon.substring(0, lon.length()-(i+1)).equals(jd)){
						index = j;
						break getindex;
					}
				}
			}
		}
		return index;
	}
	
	/**
     * 连接FTP服务
     * @param ip : IP地址
     * @param port : 端口号
     * @param username : 用户名
     * @param password : 密码
     * @return FTPClient
     */
    public static FTPClient ftpConnection(String ip, int port, String username, String password) {
    	FTPClient ftpClient = new FTPClient(); 
    	ftpClient.setControlEncoding("UTF8");
    	try {  
            ftpClient.connect(ip, port);
            ftpClient.login(username, password); 
    		ftpClient.enterLocalPassiveMode();//设置为被动模式
            int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
            if(!FTPReply.isPositiveCompletion(replyCode)) {
            	ftpClient.disconnect();
            	System.out.println("--ftp连接失败--");
                System.exit(1);
            }
            ftpClient.enterLocalPassiveMode();//这句最好加告诉服务器开一个端口
        } catch (SocketException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }   
        
        return ftpClient;  
    }
    
 
    /**
     * 关闭FTP服务
     */
    public static void ftpClose(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                //System.out.println("FTP已断开!");
            } catch (Exception e) {
            	//System.out.println("FTP断开异常。。。");
                e.printStackTrace();
            }
        }
    }
    
    /**
   	 * 取ftp上的文件内容
   	 * @param FTPClient
   	 * @param filename
   	 * @return String
   	 */
	public static String readFile(FTPClient ftpClient, String filename){
	   
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
	   //去除多余的字符
	   String text = builder.toString().replace(";;NNNN", "").replace(";;", ";");
	   String content = text.substring(0, text.length()-1);
	   return content;
	}
	
	 /**
	 * 取ftp上的文件内容(处理经纬度文件专用)
	 * @param FTPClient
	 * @param filename
	 * @return String
	 */
    public static String readLatLonFile(FTPClient ftpClient, String filename){
	   InputStream ins = null;
	   StringBuilder builder = null;
	   try {
		   
		   // 从服务器上读取指定的文件
		   ins = ftpClient.retrieveFileStream(filename);
		   BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
		   String line;
		   builder = new StringBuilder(150);
		   while ((line = reader.readLine()) != null) {
			   builder.append(line.replaceAll("\\p{C}", " "));//将不可见字符替换为空格
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
	   //去除多余的字符
	   String text = builder.toString().replace(";;NNNN", "").replace(";;", ";");
	   String content = text.substring(0, text.length()-1);
	   return content;
    }
    
    public static void main(String[] args) {
    	TestWeather ts = new TestWeather();
    	//测试WRF的网格数据
    	Map<String, Object> wrfMap = ts.getWRFOrCALPUFGriddingDataInfo("WindMap", "118.916633,35.629743", "2018080810", 0, "WRF");
    	System.out.println("WRF的网格数据为："+wrfMap.get("result"));
    	System.out.println("WRF的网格的左下角经纬度为："+wrfMap.get("returnZXLonlat"));
    	System.out.println("WRF的网格的右上角经纬度为："+wrfMap.get("returnYSLonlat"));
    	
    	//测试CALPUF的网格数据
    	Map<String, Object> CALPUFMap = ts.getWRFOrCALPUFGriddingDataInfo("wind", "118.916633,35.629743", "2018080810", 0, "CALPUF");
    	System.out.println("CALPUF的网格数据为："+CALPUFMap.get("result"));
	}
}
