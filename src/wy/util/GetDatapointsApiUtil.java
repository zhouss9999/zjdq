package wy.util;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cmcc.iot.onenet.javasdk.api.datapoints.GetDatapointsListApi;
import cmcc.iot.onenet.javasdk.response.BasicResponse;
import cmcc.iot.onenet.javasdk.response.datapoints.DatapointsList;
/**
 * 从OneNet获取监测实时数据
 * @author zzb
 * @date 2018年8月8日14:30:46
 */
public class GetDatapointsApiUtil {
	
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
	 */
	public static Map<String, Object> getDatapointsApi(String datastreamIds,String devid,String key,String startTime, String endTime,
			String cursor, String sort, Map<String, Object> dataMap) {
		/**
		 * 数据点查询
		 * @param datastreamIds:查询的数据流，多个数据流之间用逗号分隔（可选）,String
		 * @param startTime:提取数据点的开始时间（可选）,String
		 * @param endTime:提取数据点的结束时间（可选）,String
		 * @param devid:设备ID,String
		 * 
		 * @param duration:查询时间区间（可选，单位为秒）,Integer
		 *  start+duration：按时间顺序返回从start开始一段时间内的数据点
		 *  end+duration：按时间倒序返回从end回溯一段时间内的数据点
		 * 
		 * @param limit:限定本次请求最多返回的数据点数，0<n<=6000（可选，默认1440）,Integer
		 * @param cursor:指定本次请求继续从cursor位置开始提取数据（可选）,String
		 * @param interval:通过采样方式返回数据点，interval值指定采样的时间间隔（可选）,Integer
		 * @param metd:指定在返回数据点时，同时返回统计结果，可能的值为（可选）,String
		 * @param first:返回结果中最值的时间点。1-最早时间，0-最近时间，默认为1（可选）,Integer
		 * @param sort:值为DESC|ASC时间排序方式，DESC:倒序，ASC升序，默认升序,String
		 * @param key:masterkey 或者 设备apikey
		 */
		
		startTime = (startTime == null || "".equals(startTime)) ? "1970-01-01T00:00:00" : startTime.replace(" ", "T");
		endTime = (endTime == null || "".equals(endTime)) ? endTime : endTime.replace(" ", "T");
		
		GetDatapointsListApi api = new GetDatapointsListApi(datastreamIds, startTime, endTime,
				devid, null, 6000, cursor, null, null, null, sort, key);
		BasicResponse<DatapointsList> response = api.executeApi();
		
		JSONObject jsonObj = JSONObject.fromObject(response.getJson()).getJSONObject("data");
		
		if(!jsonObj.isNullObject() && jsonObj.getInt("count")>0){
			JSONArray jsonArr = JSONArray.fromObject(jsonObj.get("datastreams")); 
			
			for (int i = 0, size = jsonArr.size(); i < size; i++) {
				JSONObject json = jsonArr.getJSONObject(i);
				
				String mapkey = json.get("id").toString();
				JSONArray dataJson = json.getJSONArray("datapoints");
				
				if (dataMap.containsKey(mapkey)) {
					((JSONArray)dataMap.get(mapkey)).addAll(dataJson);
					dataMap.put(json.get("id").toString(), dataMap.get(mapkey));
				} else {
					dataMap.put(json.get("id").toString(), dataJson);
				}
			}
			if(jsonObj.has("cursor")){
				cursor = jsonObj.getString("cursor")==null?"":jsonObj.getString("cursor");
				return getDatapointsApi(datastreamIds,devid,key,startTime,endTime,cursor, sort,dataMap);
			}else{
				return dataMap;
			}
		} else {
			return dataMap;
		}
	}
	
}
