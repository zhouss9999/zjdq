package wy.qingdao_atmosphere.weatheranalyze.service;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import wy.qingdao_atmosphere.weatheranalyze.dao.WeatherAnalyzeDao;
import wy.util.FtpUtil;
/**
 * @author xsq
 * @description 气象分析Service层接口实现类
 * 
 */
@Service("weatherAnalyzeService")
public class WeatherAnalyzeServiceImpl implements WeatherAnalyzeService {
	
	private WeatherAnalyzeDao weatherAnalyzeDao;

	public WeatherAnalyzeDao getWeatherAnalyzeDao() {
		return weatherAnalyzeDao;
	}
	@Resource
	public void setWeatherAnalyzeDao(WeatherAnalyzeDao weatherAnalyzeDao) {
		this.weatherAnalyzeDao = weatherAnalyzeDao;
	}
	
	//FTP服务器IP地址
	private final static String ip = "124.64.196.19";
	//端口号
	private final static int port = 21;
	//用户名
	private final static String username = "root";
	//密码
	private final static String password = "wcgbs@2017";
	//WRF模式总目录
	private final static String WRFURL = "/home/model/atmosphere/WRF_MODEL/GFS_MODEL/MODEL1/DRAW1/";
	//CALPUF模式总目录
	private final static String CALPUFURL = "/home/model/atmosphere/CALPUFF_MODEL/MODEL2/CASE/CASE1/RESULT/";
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度获取网格预测图片
	 * @param ftpClient ftp连接客户端
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)、wind-vec-black(风向箭头图-黑色)、wind-vec-white(风向箭头图-白色)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param forecastDate 日期
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return List<String>
	 */
	public List<String> getWeatherImgUrl(FTPClient ftpClient, String weathertype, String forecastDate, double height, String patternType){
		List<String> resultlist = new ArrayList<String>();
		if(!"".equals(weathertype) && !"".equals(forecastDate) && !"".equals(patternType)){
			//ftp目录串
			String ftpUrl = "";
			if(patternType.equals("WRF")){
				if(weathertype.equals("WindMap") || weathertype.equals("TemperatureMap")){
					ftpUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/";
				}else if(weathertype.equals("RainMap") || weathertype.equals("PressureMap") 
						|| weathertype.equals("RHMap") || weathertype.equals("wind-vec-black") 
						|| weathertype.equals("wind-vec-white")){
					ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/";
				}
			}else if(patternType.equals("CALPUF")){
				//CALPUF格式日期目录处理
				//String calpufdate = forecastDate.substring(0, 4)+"_M"+forecastDate.substring(4, 6)+"_D"+forecastDate.substring(6, 8)+"_0700/";
				if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
					ftpUrl = CALPUFURL+"conc/"+weathertype+"/"+forecastDate+"/";
				}else if(weathertype.equals("temp") || weathertype.equals("wind")){
					ftpUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/";
				}
			}
			try {
				String[] str = ftpClient.listNames(ftpUrl);
				for(int i = 0; i < str.length; i++){
					if(str[i].contains(".png")){
						//tomcat文件服务器配置在/home目录下，因此目录串截取到/home之后的地址串
						String fileurl = "http://"+ip+":8666/"+str[i].substring(23);
						resultlist.add(fileurl);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultlist;
	}
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度获取实时数据
	 * @param ftpClient ftp连接客户端
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param forecastDate 日期
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWeatherActualData(FTPClient ftpClient, String weathertype, String forecastDate, double height, String patternType){
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if(!"".equals(weathertype) && !"".equals(forecastDate)){
			try {
				//ftp目录串
				String ftpUrl = "";
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap")){
						ftpUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/avewsp.txt";
					}else if(weathertype.equals("RainMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/averain.txt";
					}else if(weathertype.equals("PressureMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/avepre.txt";
					}else if(weathertype.equals("TemperatureMap")){
						ftpUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/avetemp.txt";
					}else if(weathertype.equals("RHMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/averh.txt";
					}
				}else if(patternType.equals("CALPUF")){
					//CALPUF格式日期目录处理
					//String calpufdate = forecastDate.substring(0, 4)+"_M"+forecastDate.substring(4, 6)+"_D"+forecastDate.substring(6, 8)+"_0700/";
					if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
						ftpUrl = CALPUFURL+"conc/"+weathertype+"/"+forecastDate+"/aveconc.txt";
					}else if(weathertype.equals("mix")){
						ftpUrl = CALPUFURL+weathertype+"/"+forecastDate+"/avemix.txt";
					}else if(weathertype.equals("pgt")){
						ftpUrl = CALPUFURL+weathertype+"/"+forecastDate+"/avepgt.txt";
					}else if(weathertype.equals("temp")){
						ftpUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/avetemp.txt";
					}else if(weathertype.equals("wind")){
						ftpUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/avewsp.txt";
					}
				}
				//读取文件返回文本内容
				String data = FtpUtil.readFile(ftpClient, ftpUrl);
				//按分号分隔成一组组的数据
				String[] groupData = data.split(";");
				for(int i = 0; i < groupData.length; i++){
					//WRF和CALPUF模式做不同的处理
					if(patternType.equals("WRF")){
						//对一组数据进行空格分隔
						String[] oneData = groupData[i].trim().split(" ");
						resultMap.put(oneData[0], oneData[1]);
					}else if(patternType.equals("CALPUF")){
						//对一组数据进行空格分隔
						String[] oneData = groupData[i].trim().split(" ");
						resultMap.put(oneData[0], oneData[1]);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
	
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度获取日数据
	 * @param ftpClient ftp连接客户端
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param forecastDate 日期
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWeatherDayData(FTPClient ftpClient, String weathertype, String forecastDate, double height, String patternType){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(!"".equals(weathertype) && !"".equals(forecastDate)){
			try {
				//ftp目录串
				String ftpUrl = "";
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap")){
						ftpUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/day_wsp.txt";
					}else if(weathertype.equals("RainMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/day_rain.txt";
					}else if(weathertype.equals("PressureMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/day_pre.txt";
					}else if(weathertype.equals("TemperatureMap")){
						ftpUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/day_temp.txt";
					}else if(weathertype.equals("RHMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/day_rh.txt";
					}
				}else if(patternType.equals("CALPUF")){
					//CALPUF格式日期目录处理
					//String calpufdate = forecastDate.substring(0, 4)+"_M"+forecastDate.substring(4, 6)+"_D"+forecastDate.substring(6, 8)+"_0700/";
					if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2") || weathertype.equals("CO") || weathertype.equals("O3")){
						ftpUrl = CALPUFURL+"conc/"+weathertype+"/"+forecastDate+"/day_conc.txt";
					}else if(weathertype.equals("mix")){
						ftpUrl = CALPUFURL+weathertype+"/"+forecastDate+"/day_mix.txt";
					}else if(weathertype.equals("pgt")){
						ftpUrl = CALPUFURL+weathertype+"/"+forecastDate+"/day_pgt.txt";
					}else if(weathertype.equals("temp")){
						ftpUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/day_temp.txt";
					}else if(weathertype.equals("wind")){
						ftpUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/day_wsp.txt";
					}
				}
				//读取文件返回文本内容
				String data = FtpUtil.readFile(ftpClient, ftpUrl);
				//对一组数据进行双空格替换为单空格，然后按单空格分隔
				String[] oneData = data.trim().replaceAll("  ", " ").split(" ",2);
				resultMap.put(oneData[0], oneData[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间获取网格图片xy坐标信息
	 * @param ftpClient ftp连接客户端
	 * @param weathertype参数：WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)
	 * @param forecastDate 日期
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWeatherImgXYInfo(FTPClient ftpClient, String weathertype, String forecastDate, String patternType){
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if(!"".equals(weathertype) && !"".equals(forecastDate)){
			try {
				String ftpUrl = "";
				//ftp目录串
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap") || weathertype.equals("TemperatureMap")){
						ftpUrl = WRFURL+weathertype+"/H0/"+forecastDate+"/xy.txt";
					}else if(weathertype.equals("RainMap") || weathertype.equals("PressureMap") || weathertype.equals("RHMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/xy.txt";
					}
				}else if(patternType.equals("CALPUF")){
					if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
						ftpUrl = CALPUFURL+"conc/xy.txt";
					}else if(weathertype.equals("temp") || weathertype.equals("wind")){
						ftpUrl = CALPUFURL+weathertype+"/xy.txt";
					}
				}
				//读取文件返回文本内容
				String data = FtpUtil.readFile(ftpClient, ftpUrl);
				//按分号分隔成一组组的数据
				String[] groupData = data.split(";");
				for(int i = 0; i < groupData.length; i++){
					//对一组数据进行空格消除，然后根据=号分隔
					String[] oneData = groupData[i].trim().replaceAll(" ", "").split("=");
					resultMap.put(oneData[0], oneData[1]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度获取风场UV数据信息
	 * @param ftpClient ftp连接客户端
	 * @param weathertype 参数：WRF模式：[WindMap(风场)],CALPUF模式：[wind(风场)]
	 * @param forecastTime 日期时间
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWRFOrCALPUFWindUVDataInfo(String weathertype, String forecastTime, double height, String patternType){
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		String Ufilename = "",Vfilename = "";
		if(!"".equals(weathertype) && !"".equals(forecastTime) && !"".equals(patternType)){
			try {
				//ftp连接
				FTPClient ftpClient = FtpUtil.ftpConnection(ip, port, username, password);
				//ftp目录串
				String ftpUrl = "";
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap")){
						ftpUrl = WRFURL+weathertype+"/H0/"+forecastTime.substring(0,forecastTime.length()-2)+"/";
					}
				}else if(patternType.equals("CALPUF")){
					if(weathertype.equals("wind")){
						ftpUrl = CALPUFURL+weathertype+"/h0/"+forecastTime.substring(0,forecastTime.length()-2)+"/";
					}
				}
				//获取目录下所有文件完整名称
				String[] str = ftpClient.listNames(ftpUrl);
				for(int i = 0; i < str.length; i++){
					String filename = str[i].substring(str[i].lastIndexOf("/")+1, str[i].length());
					if(patternType.equals("WRF")){
						//取风场特定时间的U数据文件名
						if((forecastTime+".u").equals(filename)){
							Ufilename = str[i];
						}
						//取风场特定时间V数据文件名
						if((forecastTime+".v").equals(filename)){
							Vfilename = str[i];
						}
					}else if(patternType.equals("CALPUF")){
						//取风场特定时间的U数据文件名
						if((forecastTime+".u").equals(filename)){
							Ufilename = str[i];
						}
						//取风场特定时间V数据文件名
						if((forecastTime+".v").equals(filename)){
							Vfilename = str[i];
						}
					}
				}
				if(!"".equals(Ufilename) && !"".equals(Vfilename)){
					//读取U文件返回文本内容
					String UData = FtpUtil.readFile(ftpClient, Ufilename);
					//读取V文件返回文本内容
					String VData = FtpUtil.readFile(ftpClient, Vfilename);
					//去掉UData文本的空格，然后按照;号拆分数据
					String[] UList = UData.replace(" ", "").split(";");
					//去掉VData文本的空格，然后按照;号拆分数据
					String[] VList = VData.replace(" ", "").split(";");
					resultMap.put("UData", UList);
					resultMap.put("VData", VList);
				}
				//关闭ftp连接
				FtpUtil.ftpClose(ftpClient);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
	
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
				FTPClient ftpClient = FtpUtil.ftpConnection(ip, port, username, password);
				//ftp网格数据路径
				String ftpGridDataUrl = "";
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap") || weathertype.equals("TemperatureMap")){
						ftpGridDataUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastTime.substring(0, 8)+"/"+forecastTime+".txt";
					}else if(weathertype.equals("PressureMap") || weathertype.equals("RHMap")){
						ftpGridDataUrl = WRFURL+weathertype+"/"+forecastTime.substring(0, 8)+"/"+forecastTime+".txt";
					}else if(weathertype.equals("RainMap")){
						//传入规定格式时间，相加一个小时的算法
						SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHH"); 
						Date date = formatDate.parse(forecastTime); 
						//date的毫秒数 
						Long nowValue = date.getTime();
						//date加一个小时的毫秒数 
						Long afterHour = nowValue+60*60*1000;
						Date afterHourDate = new Date(afterHour);
						String rainfilename = forecastTime+"T"+formatDate.format(afterHourDate)+".txt";
						ftpGridDataUrl = WRFURL+weathertype+"/"+forecastTime.substring(0, 8)+"/"+rainfilename;
					}
				}else if(patternType.equals("CALPUF")){
					if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
						ftpGridDataUrl = CALPUFURL+"conc/"+weathertype+"/"+forecastTime.substring(0, 8)+"/"+forecastTime+".txt";
					}else if(weathertype.equals("temp") || weathertype.equals("wind")){
						ftpGridDataUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastTime.substring(0, 8)+"/"+forecastTime;
					}
				}
				if(patternType.equals("WRF")){
					//根据经纬度查找WRF模式下最近的网格的下标
					Map<String, Object> indexMap = getIndexByWRFGridLonlat(lonlat);
					if(weathertype.equals("WindMap")){
						//网格下标
						int index  = Integer.parseInt(indexMap.get("index").toString());
						if(index > -1){
							//读取风向网格文件数据
							String gridfileconWDR = FtpUtil.readFile(ftpClient, ftpGridDataUrl.substring(0, ftpGridDataUrl.length() - 4)+".wdr");
							//读取风速网格文件数据
							String gridfileconWSP = FtpUtil.readFile(ftpClient, ftpGridDataUrl.substring(0, ftpGridDataUrl.length() - 4)+".wsp");
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
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", indexMap.get("returnYSLonlat").toString());
						}else{
							resultMap.put("result", "无");
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							resultMap.put("returnYSLonlat", indexMap.get("returnZXLonlat").toString());
						}
					}else{
						//读取网格文件数据
						String gridfilecon = FtpUtil.readFile(ftpClient, ftpGridDataUrl);
						//去掉数据空格按分号分隔每组数据
						String[] gridData = gridfilecon.replace(" ", "").split(";");
						//网格下标
						int index  = Integer.parseInt(indexMap.get("index").toString());
						if(index > -1){
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							resultMap.put("result", gridData[index - 1]);
							//网格左下角经纬度
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", indexMap.get("returnYSLonlat").toString());
						}else{
							resultMap.put("result", "无");
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							resultMap.put("returnYSLonlat", indexMap.get("returnZXLonlat").toString());
						}
					}
				}else if(patternType.equals("CALPUF")){		
					//根据经纬度查找CALPUF模式下最近的网格的下标
					Map<String, Object> indexMap = getIndexByCALPUFGridLonlat(lonlat);
					int index = Integer.parseInt(indexMap.get("index").toString());
					if(index > -1){
						if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
							//读取网格文件数据
							String gridfilecon = FtpUtil.readFile(ftpClient, ftpGridDataUrl);
							//将所有的空格替换为1个空格按分号分隔每组数据
							String[] gridData = gridfilecon.replaceAll(" +", "").split(";");
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							resultMap.put("result", gridData[index -1]);
							//网格左下角经纬度
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", indexMap.get("returnYSLonlat").toString());
						}else if(weathertype.equals("temp")){
							//读取网格文件数据
							String gridfilecon = FtpUtil.readFile(ftpClient, ftpGridDataUrl+".txt");
							//将所有的空格替换为1个空格按分号分隔每组数据
							String[] gridData = gridfilecon.replaceAll(" +", "").split(";");
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							resultMap.put("result", gridData[index -1]);
							//网格左下角经纬度
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", indexMap.get("returnYSLonlat").toString());
						}else if(weathertype.equals("wind")){
							//读取风向网格文件数据
							String gridfileconWDR = FtpUtil.readFile(ftpClient, ftpGridDataUrl+".wdir");
							//读取风速网格文件数据
							String gridfileconWSP = FtpUtil.readFile(ftpClient, ftpGridDataUrl+".wseep");
							String[] gridDataWDR = gridfileconWDR.replaceAll(" +", "").split(";");
							String[] gridDataWSP = gridfileconWSP.replaceAll(" +", "").split(";");
							//风向度数
							//解析的数据数组从下标0开始，所以网格下标取数据则减1
							double wdrdata = Double.parseDouble(gridDataWDR[index -1]);
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
							map.put("风速", gridDataWSP[index -1]);
							resultMap.put("result", map);
							//网格左下角经纬度
							resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
							//网格右上角经纬度
							resultMap.put("returnYSLonlat", indexMap.get("returnYSLonlat").toString());
						}
					}else{
						resultMap.put("result", "无");
						//网格左下角经纬度
						resultMap.put("returnZXLonlat", indexMap.get("returnZXLonlat").toString());
						//网格右上角经纬度
						resultMap.put("returnYSLonlat", indexMap.get("returnYSLonlat").toString());
					}
				}
				FtpUtil.ftpClose(ftpClient);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("result", "无");
				//网格左下角经纬度
				resultMap.put("returnZXLonlat", getIndexByCALPUFGridLonlat(lonlat).get("returnZXLonlat").toString());
				//网格右上角经纬度
				resultMap.put("returnYSLonlat", getIndexByCALPUFGridLonlat(lonlat).get("returnYSLonlat").toString());
			}
		}
		return resultMap;
	}
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度和经纬度获取24小时网格数据信息
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param lonlat 经纬度
	 * @param forecastDate 时间
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWRFOrCALPUF24HoursGriddingDataInfo(String weathertype, String lonlat, String forecastDate, double height, String patternType){
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if(!"".equals(weathertype) && !"".equals(lonlat) && !"".equals(forecastDate) && !"".equals(patternType)){
			try {
				//ftp连接
				FTPClient ftpClient = FtpUtil.ftpConnection(ip, port, username, password);
				//ftp目录串
				String ftpUrl = "";
				if(patternType.equals("WRF")){
					if(weathertype.equals("WindMap") || weathertype.equals("TemperatureMap")){
						ftpUrl = WRFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/";
					}else if(weathertype.equals("RainMap") || weathertype.equals("PressureMap") || weathertype.equals("RHMap")){
						ftpUrl = WRFURL+weathertype+"/"+forecastDate+"/";
					}
				}else if(patternType.equals("CALPUF")){
					if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
						ftpUrl = CALPUFURL+"conc/"+weathertype+"/"+forecastDate+"/";
					}else if(weathertype.equals("temp") || weathertype.equals("wind")){
						ftpUrl = CALPUFURL+weathertype+"/"+getHeightFolder(height, patternType)+forecastDate+"/";
					}
				}
				//遍历目录下所有的文件名
				String[] listNames = ftpClient.listNames(ftpUrl);
				if(patternType.equals("WRF")){
					//根据经纬度查找WRF模式下最近的网格的下标
					Map<String, Object> indexMap = getIndexByWRFGridLonlat(lonlat);
					if(weathertype.equals("WindMap")){
						//网格下标
						int index  = Integer.parseInt(indexMap.get("index").toString());
						if(index > -1){
							//WDR文件url列表
							List<String> WDRFileUrlList = new ArrayList<String>();
							//WSP文件url列表
							List<String> WSPFileUrlList = new ArrayList<String>();
							for(int i = 0; i < listNames.length; i++){
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".wdr")){
									WDRFileUrlList.add(listNames[i]);
								}
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".wsp")){
									WSPFileUrlList.add(listNames[i]);
								}
							}
							//两个文件的list的size一样，并且文件名日期顺序一样，循环读文件取数据
							for(int i = 0; i < WDRFileUrlList.size(); i++){
								//读取风向网格文件数据
								String gridfileconWDR = FtpUtil.readFile(ftpClient, WDRFileUrlList.get(i));
								//读取风速网格文件数据
								String gridfileconWSP = FtpUtil.readFile(ftpClient, WSPFileUrlList.get(i));
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
								resultMap.put(WDRFileUrlList.get(i).substring(WDRFileUrlList.get(i).lastIndexOf("/") + 1, WDRFileUrlList.get(i).lastIndexOf(".")), map);
							}
						}else{
							resultMap.put("result", "无");
						}
					}else{
						//根据经纬度查找WRF模式下最近的网格的下标
						int index  = Integer.parseInt(indexMap.get("index").toString());
						if(index > -1){
							for(int i = 0; i < listNames.length; i++){
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(forecastDate) && listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".txt")){
									//读取网格文件数据
									String gridfilecon = FtpUtil.readFile(ftpClient, listNames[i]);
									//去掉数据空格按分号分隔每组数据
									String[] gridData = gridfilecon.replace(" ", "").split(";");
									//解析的数据数组从下标0开始，所以网格下标取数据则减1
									resultMap.put(listNames[i].substring(listNames[i].lastIndexOf("/") + 1, listNames[i].lastIndexOf(".")), gridData[index - 1]);
								}
							}
						}else{
							resultMap.put("result", "无");
						}
					}
				}else if(patternType.equals("CALPUF")){
					//根据经纬度查找CALPUF模式下最近的网格的下标
					int index = Integer.parseInt(getIndexByCALPUFGridLonlat(lonlat).get("index").toString());
					if(index > -1){
						if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
							for(int i = 0; i < listNames.length; i++){
								//if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains("_M") && listNames[i].contains(".txt")){
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(forecastDate) && listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".txt")){
									//截取文件名的日期作为key
									String date = listNames[i].substring(listNames[i].lastIndexOf("/") + 1);
									//String keyDate = date.substring(0, 4)+date.substring(6, 8)+date.substring(10, 12)+date.substring(13, 15);
									String keyDate = date.substring(0, date.lastIndexOf("."));
									//读取网格文件数据
									String gridfilecon = FtpUtil.readFile(ftpClient, listNames[i]);
									//将所有的空格替换为1个空格按分号分隔每组数据
									String[] gridData = gridfilecon.replaceAll(" +", "").split(";");
									//解析的数据数组从下标0开始，所以网格下标取数据则减1
									resultMap.put(keyDate, gridData[index - 1]);
								}
							}
						}else if(weathertype.equals("temp")){
							for(int i = 0; i < listNames.length; i++){
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(forecastDate) && listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".txt")){
									//截取文件名的日期作为key
									String date = listNames[i].substring(listNames[i].lastIndexOf("/") + 1);
									//String keyDate = date.substring(0, 4)+date.substring(6, 8)+date.substring(10, 12)+date.substring(13, 15);
									String keyDate = date.substring(0, date.lastIndexOf("."));
									//读取网格文件数据
									String gridfilecon = FtpUtil.readFile(ftpClient, listNames[i]);
									//将所有的空格替换为1个空格按分号分隔每组数据
									String[] gridData = gridfilecon.replaceAll(" +", "").split(";");
									//解析的数据数组从下标0开始，所以网格下标取数据则减1
									resultMap.put(keyDate, gridData[index - 1]);
								}
							}
						}else if(weathertype.equals("wind")){
							//WDR文件url列表
							List<String> WDRFileUrlList = new ArrayList<String>();
							//WSP文件url列表
							List<String> WSPFileUrlList = new ArrayList<String>();
							for(int i = 0; i < listNames.length; i++){
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".wdir")){
									WDRFileUrlList.add(listNames[i]);
								}
								if(listNames[i].substring(listNames[i].lastIndexOf("/") + 1).contains(".wseep")){
									WSPFileUrlList.add(listNames[i]);
								}
							}
							//两个文件的list的size一样，并且文件名日期顺序一样，循环读文件取数据
							for(int i = 0; i < WDRFileUrlList.size(); i++){
								//截取文件名的日期作为key
								String date = WDRFileUrlList.get(i).substring(WDRFileUrlList.get(i).lastIndexOf("/") + 1);
								//String keyDate = date.substring(0, 4)+date.substring(6, 8)+date.substring(10, 12)+date.substring(13, 15);
								String keyDate = date.substring(0, date.lastIndexOf("."));
								//读取风向网格文件数据
								String gridfileconWDR = FtpUtil.readFile(ftpClient, WDRFileUrlList.get(i));
								//读取风速网格文件数据
								String gridfileconWSP = FtpUtil.readFile(ftpClient, WSPFileUrlList.get(i));
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
								resultMap.put(keyDate, map);
							}
						}
					}else{
						resultMap.put("result", "无");
					}
				}
				FtpUtil.ftpClose(ftpClient);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("result", "无");
			}
		}
		return resultMap;
	}
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度获取数据信息
	 * @param ftpClient ftp连接客户端
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param forecastDate 日期
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @return Map
	 */
	public Map<String, Object> getWRFOrCALPUFWeatherData(String weathertype, String forecastDate, double height, String patternType){
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			FTPClient ftpClient = FtpUtil.ftpConnection(ip, port, username, password);
			//根据气场类型和时间获取网格预测图片
			List<String> imgUrl = getWeatherImgUrl(ftpClient, weathertype, forecastDate, height, patternType);
			//根据气场类型和时间获取实时数据
			Map<String, Object> actualData = getWeatherActualData(ftpClient, weathertype, forecastDate, height, patternType);
			//根据气场类型和时间获取日数据
			Map<String, Object> dayData = getWeatherDayData(ftpClient, weathertype, forecastDate, height, patternType);
			//根据气场类型和时间获取网格图片xy坐标信息
			Map<String, Object> imgXYInfo = getWeatherImgXYInfo(ftpClient, weathertype, forecastDate, patternType);
			resultMap.put("imgUrl", imgUrl);
			resultMap.put("actualData", actualData);
			resultMap.put("dayData", dayData);
			resultMap.put("imgXYInfo", imgXYInfo);
			FtpUtil.ftpClose(ftpClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}
	
	/**
	 * @description 根据WRF/CALPUF模式的气场类型和时间以及高度获取网格数据文件下载
	 * @param weathertype 参数：WRF模式：[WindMap(风场)、RainMap(降雨)、PressureMap(气压)、TemperatureMap(温度)、RHMap(相对湿度)],CALPUF格式：[NOX、PM2.5、PM10、SO2、mix(混合层高度)、pgt(大气稳定度)、temp(温度)、wind(风场)]
	 * @param forecastTime 时间
	 * @param height 高度
	 * @param patternType 模式类型:WRF/CALPUF
	 * @param WDROrWSP 风速或风向
	 * @return Map
	 */
	public void getWRFOrCALPUFGriddingDataFileDownload(String weathertype, String forecastTime, double height, String patternType, String WDROrWSP, HttpServletResponse response) throws Exception{
		if(!"".equals(weathertype) && !"".equals(forecastTime) && !"".equals(patternType)){
			String urlHeader = "http://124.64.196.19:8666";
			//ftp网格数据路径
			String ftpGridDataUrl = "";
			if(patternType.equals("WRF")){
				if(weathertype.equals("WindMap") || weathertype.equals("TemperatureMap")){
					ftpGridDataUrl = urlHeader+"/WRF_MODEL/GFS_MODEL/MODEL1/DRAW1/"+weathertype+"/"+getHeightFolder(height, patternType)+forecastTime.substring(0, 8)+"/"+forecastTime+".txt";
				}else if(weathertype.equals("PressureMap") || weathertype.equals("RHMap")){
					ftpGridDataUrl = urlHeader+"/WRF_MODEL/GFS_MODEL/MODEL1/DRAW1/"+weathertype+"/"+forecastTime.substring(0, 8)+"/"+forecastTime+".txt";
				}else if(weathertype.equals("RainMap")){
					//传入规定格式时间，相加一个小时的算法
					SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHH"); 
					Date date = formatDate.parse(forecastTime); 
					//date的毫秒数 
					Long nowValue = date.getTime();
					//date加一个小时的毫秒数 
					Long afterHour = nowValue+60*60*1000;
					Date afterHourDate = new Date(afterHour);
					String rainfilename = forecastTime+"T"+formatDate.format(afterHourDate)+".txt";
					ftpGridDataUrl = urlHeader+"/WRF_MODEL/GFS_MODEL/MODEL1/DRAW1/"+weathertype+"/"+forecastTime.substring(0, 8)+"/"+rainfilename;
				}
			}else if(patternType.equals("CALPUF")){
				if(weathertype.equals("NOX") || weathertype.equals("PM2.5") || weathertype.equals("PM10") || weathertype.equals("SO2")){
					ftpGridDataUrl = urlHeader+"/CALPUFF_MODEL/MODEL2/CASE/CASE1/RESULT/conc/"+weathertype+"/"+forecastTime.substring(0, 8)+"/"+forecastTime+".txt";
				}else if(weathertype.equals("temp") || weathertype.equals("wind")){
					ftpGridDataUrl = urlHeader+"/CALPUFF_MODEL/MODEL2/CASE/CASE1/RESULT/"+weathertype+"/"+getHeightFolder(height, patternType)+forecastTime.substring(0, 8)+"/"+forecastTime;
				}
			}
			if(patternType.equals("WRF") && weathertype.equals("WindMap") && WDROrWSP.equals("风向")){
				ftpGridDataUrl = ftpGridDataUrl.substring(0, ftpGridDataUrl.length()-3)+"wdr";
			}else if(patternType.equals("WRF") && weathertype.equals("WindMap") && WDROrWSP.equals("风速")){
				ftpGridDataUrl = ftpGridDataUrl.substring(0, ftpGridDataUrl.length()-3)+"wsp";
			}else if(patternType.equals("CALPUF") && weathertype.equals("wind") && WDROrWSP.equals("风向")){
				ftpGridDataUrl = ftpGridDataUrl+".wdir";
			}else if(patternType.equals("CALPUF") && weathertype.equals("wind") && WDROrWSP.equals("风速")){
				ftpGridDataUrl = ftpGridDataUrl+".wseep";
			}else if(patternType.equals("CALPUF") && weathertype.equals("temp")){
				ftpGridDataUrl = ftpGridDataUrl+".txt";
			}
			String filename = patternType+"模式_"+weathertype+"_"+ftpGridDataUrl.substring(ftpGridDataUrl.lastIndexOf("/")+1);
	        if (null == ftpGridDataUrl || ftpGridDataUrl.length() == 0) {
	            throw new RuntimeException("remoteFileUrl is invalid!");
	        }
	        URL url = new URL(ftpGridDataUrl);
	        BufferedInputStream in = null;
	        in = new BufferedInputStream(url.openStream());
	        response.reset();
	        response.setContentType("application/octet-stream");
	        response.setHeader("Content-Disposition","attachment;filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"");

	        // 将网络输入流转换为输出流
	        int i;
	        while ((i = in.read()) != -1) {
	            response.getOutputStream().write(i);
	        }
	        in.close();
	        response.getOutputStream().close();
		}
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
	 * 根据经纬度查找CALPUF模式下最近的网格的下标
	 * @param lonlat 经纬度
	 * @return index 返回第多少个格子 returnZXLonlat 方格左下角经纬度  returnYSLonlat 方格右上角经纬度
	 */
	public static Map<String, Object> getIndexByCALPUFGridLonlat(String lonlat){
		Map<String, Object> map = new HashMap<String, Object>();
		String lat = lonlat.split(",")[1]; //纬度
		String lon = lonlat.split(",")[0]; //经度
		//给出大网格左下角经纬度，右上角经纬度，造出94行92列的小网格经纬度数据
		double[][] lonarr = new double[94][92]; //初始化经度二维数组
		double[][] latarr = new double[94][92]; //初始化纬度二维数组
		double zxlon = 118.890770; //左下角经度
		double zxlat = 35.441444;  //左下角纬度
		double yslon = 121.118484; //右上角经度
		double yslat = 37.275726;  //右上角纬度
		double jdc = (yslon - zxlon)/91; //经度差:右上角经度减去左下角经度除以网格的列数-1(列数减一的处理是为了递增的时候原始的经度第一次不用做递增，存储在下标0)
		double wdc = (yslat - zxlat)/93; //纬度差:右上角纬度减去左下角纬度除以网格的行数-1(行数减一的处理是为了递增的时候原始的纬度第一次不用做递增，存储在下标0)
		//循环递增数据，一行内的纬度一致，经度递增，不同行的纬度递增
		for(int i = 0; i < 94; i++){
			double x = zxlon;
			double y = i > 0 ? zxlat += wdc : zxlat;
			for(int j = 0; j < 92; j++){
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
			index = (y + 1) * 92 - (92 - (x + 1));
		}
		map.put("returnZXLonlat", returnZXLonlat);
		map.put("returnYSLonlat", returnYSLonlat);
		map.put("index", index);
		return map;
	}
	
	public static void main(String[] args) throws Exception {
		//FTPClient ftpClient = FtpUtil.ftpConnection("124.64.196.19", 21, "root", "wcgbs@2017");
		//String weathertype = "WindMap";
		//String forecastDate = "20190109";
		
		//WeatherAnalyzeServiceImpl imp = new WeatherAnalyzeServiceImpl();
		//imp.getWeatherImgUrl(ftpClient,weathertype,forecastDate,0,"WRF");
		//imp.getWeatherActualData(ftpClient, weathertype, forecastDate, 0, "WRF");
		//imp.getWeatherDayData(ftpClient, weathertype, forecastDate, 0, "CALPUF");
		//imp.getWeatherImgXYInfo(ftpClient, weathertype, forecastDate, "WRF");
		//imp.getWRFOrCALPUFWindUVDataInfo(weathertype, forecastDate,  0, "CALPUF");
		//imp.getWRFOrCALPUFGriddingDataInfo(weathertype, "118.916633,35.629743", forecastDate, 0, "WRF");
		//imp.getWRFOrCALPUF24HoursGriddingDataInfo(weathertype, "118.916633,35.629743", forecastDate, 0, "WRF");
		
		//String lat = "35.629743";
		//String lon = "118.916633";
		//imp.getCalmetGriddingDataInfo(ftpClient, "Temp", "120.152032,36.510641", "");
		//String sdfdate = new SimpleDateFormat("yyyyMMdd").parse(forecastDate);

		/*//传入规定格式时间，相加一个小时的算法
		try {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHH"); 
			//输入参数 
			String dateStr="2018083123";
			Date date =formatDate.parse(dateStr); 
			//date的毫秒数 
			Long nowValue= date.getTime();
			//date加一个小时的毫秒数 
			Long afterHour=nowValue+60*60*1000;
			Date afterHourDate=new Date(afterHour);
			System.out.println(formatDate.format(afterHourDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		//getIndexByWRFGridLonlat("120.152032,36.510641");
		//getIndexByCALPUFGridLambert("117.939345,36.904019");
		
		//FtpUtil.ftpClose(ftpClient);
	}
}
