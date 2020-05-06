package wy.qingdao_atmosphere.datacenter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import cmcc.iot.onenet.javasdk.api.device.GetDeviceApi;
import cmcc.iot.onenet.javasdk.api.device.GetDevicesStatus;
import cmcc.iot.onenet.javasdk.response.BasicResponse;
import cmcc.iot.onenet.javasdk.response.device.DeviceResponse;
import cmcc.iot.onenet.javasdk.response.device.DevicesStatusList;


import wy.qingdao_atmosphere.datacenter.websocket.DataWebSocket;
/**
 * 此定时器用来轮询判断设备状态，想用户发送最新实时数据
 * 每30秒执行一次，当websocket接入时开始查询数据库
 * @author hero
 *
 */
//@Component
public class DataTimedtaskService {
	
	@Autowired
	private DataCenterService dataCenterService;
	
	public static void main(String[] args) {
		String id = "1674527";
		String key = "tvjS=OlXZpuQHCPL=iINh=2X4BI=";
		/**
		 * 精确查询单个设备
		 * 参数顺序与构造函数顺序一致
		 * @param devid:设备名，String
		 * @param key:masterkey 或者 设备apikey,String
		 */
		GetDeviceApi api = new GetDeviceApi(id, key);
		BasicResponse<DeviceResponse> response = api.executeApi();
		System.out.println("errno:"+response.errno+" error:"+response.error);
		System.out.println(response.getJson());
	}
	
	//@Scheduled(cron = "0/15 * * * * ?")
	public void timeTask(){
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
//		System.out.println(format.format(new Date()));
		DataWebSocket dataWebSocket = new DataWebSocket();
		//获取查看人数
		int OnlineCount = dataWebSocket.getOnlineSum();
		if(OnlineCount>0){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("isused", 1);
			List<Map<String, Object>> list = dataCenterService.queryDevice(map);
			for(Map<String, Object> device:list){
				//查询微站设备在线状态
				String key = device.get("rmk1")==null?"":device.get("rmk1").toString();
		        String devIds= device.get("devicenumber")==null?"":device.get("devicenumber").toString();
		        if(key.equals("YA2pIfTi0aVAn=nQc9Hc2Ywg8BM=")){
		        	GetDevicesStatus api = new GetDevicesStatus(devIds,key);
			        BasicResponse<DevicesStatusList> response = api.executeApi();
//			        System.out.println("errno:"+response.errno+" error:"+response.error);
			        if(response.error.equals("succ")){
			        	boolean online = response.data.getDevices().get(0).getIsonline();
				        device.put("online", online);
					}else{
						device.put("online", false);
					}
		        }
			}
			List<Map<String, Object>> listinstence = DataInstance.getInstance().getlist();
			if(listinstence.size()==0){
				DataInstance.getInstance().addlist(list);
			}else{
				if(!isListEqual(list,listinstence)){
					DataInstance.getInstance().addlist(list);
					//发送websocket
					Map<String, Object> resultMap = new HashMap<String, Object>();
					resultMap.put("socketName", "queryDevice");
					resultMap.put("state", true);
					dataWebSocket.sendMsg(JSONArray.fromObject(resultMap).toString());
				}
			}
		}
		
	}
	
	//判断两个list的数据是否相等
	@SuppressWarnings("rawtypes")
	public boolean isListEqual(List l0, List l1){
        if (l0 == l1)
            return true;
        if (l0 == null && l1 == null)
            return true;
        if (l0 == null || l1 == null)
            return false;
        if (l0.size() != l1.size())
            return false;
        for (Object o : l0) {
            if (!l1.contains(o))
                return false;
        }
        for (Object o : l1) {
            if (!l0.contains(o))
                return false;
        }
        return true;
    }
	
}
