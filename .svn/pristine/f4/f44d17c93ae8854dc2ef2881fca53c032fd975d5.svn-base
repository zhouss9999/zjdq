package wy.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cmcc.iot.onenet.javasdk.api.datapoints.GetDatapointsListApi;
import cmcc.iot.onenet.javasdk.response.BasicResponse;
import cmcc.iot.onenet.javasdk.response.datapoints.DatapointsList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 从OneNet获取监测实时数据
 * @author zzb
 * @date 2018年8月8日14:30:46
 */
public class DatapointsApiThreadUtil {
	
	/**
	 * 数据点查询(单个站点所有要查询参数的监测值)
	 * 
	 * @param datastreamIds:查询的数据流，多个数据流之间用逗号分隔（可选）,String
	 * @param devid:设备ID,String
	 * @param key:masterkey 或者 设备apikey
	 * @param startTime:提取数据点的开始时间（可选）,String;格式要求：yyyy-MM-dd HH:mm:ss
	 * @param endTime:提取数据点的结束时间（可选）,String;格式要求：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param cursor:指定本次请求继续从cursor位置开始提取数据（可选）,String
	 * @param sort:值为DESC|ASC时间排序方式，DESC:倒序，ASC升序，默认升序,String
	 * @param dataMap:存放数据的map,用于存放数据返回
	 * 
	 * @return dataMap {参数名称：监测数据列表(时间,值)}
	 * @throws InterruptedException 
	 */
	
	public static Map<String, Object> getDatapointsApi(String datastreamIds,String devid,String key,String startTime, String endTime,
			String cursor, String sort, Map<String, Object> resultMap) {
		
		
		startTime = (startTime == null || "".equals(startTime)) ? "1970-01-01 00:00:00" : startTime;
		endTime = (endTime == null || "".equals(endTime)) ? dateToString("yyyy-MM-dd HH:mm:ss",new Date()) : endTime;
		
		//给起始时间分段
		List<String> timeList = getIntervalTimeList(startTime, endTime, 6);
		
		/*创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
		这里的线程池是无限大的，当一个线程完成任务之后，这个线程可以接下来完成将要分配的任务，而不是创建一个新的线程*/
		ExecutorService pool = Executors.newCachedThreadPool();
		
		//创建多个返回值任务
		List<Future<Map<Integer, Map<String, Object>>>> futureList = new ArrayList<Future<Map<Integer,Map<String,Object>>>>();
		//时间段数组
		int[] timekey =new int[timeList.size() - 1];
		try {
			//为实现体对象分配内存
			for (int i = 0, size = timeList.size() - 1; i < size; i++) {
				timekey[i] = i;
				Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
				Callable<Map<Integer, Map<String, Object>>> dataThread = new DataThread(datastreamIds, devid, key, timeList.get(i).replace(" ", "T"), timeList.get(i+1).replace(" ", "T"), cursor, sort, dataMap, i);  
				Future<Map<Integer, Map<String, Object>>> future = pool.submit(dataThread);
				futureList.add(future);
			}
			//关闭线程池
			pool.shutdown();
			while (!pool.awaitTermination(1, TimeUnit.HOURS)) {
				Logger.getLogger("").info("-----------线程池中的子线程未结束---");
			}
			Logger.getLogger("").info("-----------线程池中的子线程全部结束,执行主线程---");
			
			//list转map
			Map<Integer, Map<String, Object>> futureMap = new LinkedHashMap<Integer, Map<String,Object>>();
			for (int i = 0; i < futureList.size(); i++) {
				Map<Integer, Map<String, Object>> fm = futureList.get(i).get();
				futureMap.putAll(fm);
			}
			
			String[] mapkey = datastreamIds.split(",");
			
			if ("DESC".equals(sort)) {//倒叙排序的情况
				for (int i = timekey.length - 1; i >= 0; i--) {
					if (futureMap.containsKey(i)) {
						Map<String, Object> dm = futureMap.get(i);
						for (int j = 0; j < mapkey.length; j++) {
							if (dm.containsKey(mapkey[j])) {
								JSONArray dataJson = (JSONArray)dm.get(mapkey[j]);
								if (resultMap.containsKey(mapkey[j])) {
									((JSONArray)resultMap.get(mapkey[j])).addAll(dataJson);
									resultMap.put(mapkey[j], resultMap.get(mapkey[j]));
								} else {
									resultMap.put(mapkey[j], dataJson);
								}
							}
						}
					}	
				}
			} else {//正序排序的情况
				for (int i = 0; i < timekey.length; i++) {
					if (futureMap.containsKey(i)) {
						Map<String, Object> dm = futureMap.get(i);
						for (int j = 0; j < mapkey.length; j++) {
							if (dm.containsKey(mapkey[j])) {
								JSONArray dataJson = (JSONArray)dm.get(mapkey[j]);
								if (resultMap.containsKey(mapkey[j])) {
									((JSONArray)resultMap.get(mapkey[j])).addAll(dataJson);
									resultMap.put(mapkey[j], resultMap.get(mapkey[j]));
								} else {
									resultMap.put(mapkey[j], dataJson);
								}
							}
						}
					}	
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	
	/**
     * 获取固定间隔时刻集合
     * @param start 开始时间
     * @param end 结束时间
     * @param interval 时间间隔(单位：小时)
     * @return
     */
    public static List<String> getIntervalTimeList(String start,String end,int interval){
    	List<String> list = new ArrayList<String>();
    	try{
	        Date startDate = stringToDate("yyyy-MM-dd HH:mm:ss",start);
	        Date endDate = stringToDate("yyyy-MM-dd HH:mm:ss",end);
	        while(startDate.getTime()<=endDate.getTime()){
	           list.add(dateToString("yyyy-MM-dd HH:mm:ss",startDate));
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTime(startDate);
	            calendar.add(Calendar.HOUR,interval);
	            if(calendar.getTime().getTime()>endDate.getTime()){
	                if(!startDate.equals(endDate)){
	                   list.add(dateToString("yyyy-MM-dd HH:mm:ss",endDate));
	                }
	                startDate = calendar.getTime();
	            }else{
	                startDate = calendar.getTime();
	            }
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return list;
    }
    
    /**字符串时间转日期*/
	public static Date stringToDate(String timeFormat, String timeString) throws Exception {
		
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(timeFormat);
		localSimpleDateFormat.setLenient(false);
		try {
			return localSimpleDateFormat.parse(timeString);
		} catch (ParseException e) {
			throw new Exception("解析日期字符串时出错！");
		}
		
	}
 
	/**日期转字符串时间*/
	public static String dateToString(String timeFormat, Date date) {
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(timeFormat);
		
		localSimpleDateFormat.setLenient(false);
		return localSimpleDateFormat.format(date);
	}
}

class DataThread  implements Callable<Map<Integer, Map<String, Object>>>{
	private String datastreamIds;         
	private String devid;                 
	private String key;                   
	private String startTime;             
	private String endTime;               
	
	private String cursor;                
	private String sort;                  
	private Map<String, Object> dataMap; 
	private int mapkey; 
	
	public DataThread(){}
	public DataThread(String datastreamIds, String devid, String key,
			String startTime, String endTime, String cursor, String sort,
			Map<String, Object> dataMap, int mapkey) {
		super();
		this.datastreamIds = datastreamIds;
		this.devid = devid;
		this.key = key;
		this.startTime = startTime;
		this.endTime = endTime;
		this.cursor = cursor;
		this.sort = sort;
		this.dataMap = dataMap;
		this.mapkey = mapkey;
	}

	public String getDatastreamIds() {
		return datastreamIds;
	}
	public void setDatastreamIds(String datastreamIds) {
		this.datastreamIds = datastreamIds;
	}
	public String getDevid() {
		return devid;
	}
	public void setDevid(String devid) {
		this.devid = devid;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getCursor() {
		return cursor;
	}
	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public Map<String, Object> getDataMap() {
		return dataMap;
	}
	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}
	
	public Map<Integer, Map<String, Object>> call() throws Exception {
		
		Map<String, Object> map = datapointsApi(datastreamIds, devid, key, startTime, endTime, cursor, sort, dataMap);
		
		Map<Integer, Map<String, Object>> futureMap = new LinkedHashMap<Integer, Map<String, Object>>();
		futureMap.put(mapkey, map);
		
		return futureMap;
	}
	
	
	public static Map<String, Object> datapointsApi(String datastreams,String devcenumber,String apikey,String begintime, String endtime,
			String cursors, String sortbytime, Map<String, Object> dataStreamMap) {
		/**
		 * 数据点查询
		 * @param datastreams:查询的数据流，多个数据流之间用逗号分隔（可选）,String
		 * @param begintime:提取数据点的开始时间（可选）,String
		 * @param endtime:提取数据点的结束时间（可选）,String
		 * @param devcenumber:设备ID,String
		 * 
		 * @param duration:查询时间区间（可选，单位为秒）,Integer
		 *  start+duration：按时间顺序返回从start开始一段时间内的数据点
		 *  end+duration：按时间倒序返回从end回溯一段时间内的数据点
		 * 
		 * @param limit:限定本次请求最多返回的数据点数，0<n<=6000（可选，默认1440）,Integer
		 * @param cursors:指定本次请求继续从cursor位置开始提取数据（可选）,String
		 * @param interval:通过采样方式返回数据点，interval值指定采样的时间间隔（可选）,Integer
		 * @param metd:指定在返回数据点时，同时返回统计结果，可能的值为（可选）,String
		 * @param first:返回结果中最值的时间点。1-最早时间，0-最近时间，默认为1（可选）,Integer
		 * @param sortbytime:值为DESC|ASC时间排序方式，DESC:倒序，ASC升序，默认升序,String
		 * @param apikey:masterkey 或者 设备apikey
		 */
		GetDatapointsListApi api = new GetDatapointsListApi(datastreams, begintime, endtime,
				devcenumber, null, 6000, cursors, null, null, null, sortbytime, apikey);
		BasicResponse<DatapointsList> response = api.executeApi();
		
		JSONObject jsonObj = JSONObject.fromObject(response.getJson()).getJSONObject("data");
		
		if(!jsonObj.isNullObject() && jsonObj.getInt("count")>0){
			JSONArray jsonArr = JSONArray.fromObject(jsonObj.get("datastreams")); 
			
			for (int i = 0, size = jsonArr.size(); i < size; i++) {
				JSONObject json = jsonArr.getJSONObject(i);
				
				String mapkey = json.get("id").toString();
				JSONArray dataJson = json.getJSONArray("datapoints");
				
				if (dataStreamMap.containsKey(mapkey)) {
					((JSONArray)dataStreamMap.get(mapkey)).addAll(dataJson);
					dataStreamMap.put(json.get("id").toString(), dataStreamMap.get(mapkey));
				} else {
					dataStreamMap.put(json.get("id").toString(), dataJson);
				}
			}
			if(jsonObj.has("cursor")){
				cursors = jsonObj.getString("cursor")==null?"":jsonObj.getString("cursor");
				return datapointsApi(datastreams,devcenumber,apikey,begintime,endtime,cursors, sortbytime,dataStreamMap);
			}else{
				return dataStreamMap;
			}
		} else {
			return dataStreamMap;
		}
	}
	
}  

