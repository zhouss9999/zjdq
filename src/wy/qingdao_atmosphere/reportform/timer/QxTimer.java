package wy.qingdao_atmosphere.reportform.timer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import cmcc.iot.onenet.javasdk.api.datapoints.AddDatapointsApi;
import cmcc.iot.onenet.javasdk.model.Data;
import cmcc.iot.onenet.javasdk.model.Datapoints;
import cmcc.iot.onenet.javasdk.response.BasicResponse;
import wy.qingdao_atmosphere.reportform.service.ReportformService;

/**
 * 气象数据同步到OneNet
 * @author KEYI
 *
 */
@Component
public class QxTimer {
	
	@Resource
	private ReportformService reportformService;
	
	//@Scheduled(cron = "0 30 * * * ?")
	public void getSynchroDataHours(){
		Logger.getLogger("").info("-----------气象数据同步到onenet-------------");
		try {
			SimpleDateFormat bformat = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
			SimpleDateFormat eformat = new SimpleDateFormat("yyyy-MM-dd HH:59:59");
			Map<String, Object> cmap = new HashMap<String, Object>();
			cmap.put("begintime", bformat.format(new Date()));
			cmap.put("endtime", eformat.format(new Date()));
			List<Map<String, Object>> sjlist = reportformService.queryqxSendOneNet(cmap);
			for(int i=0;i<sjlist.size();i++){
				Map<String, Object> objMap = sjlist.get(i);
				//设备编号
				String devid = objMap.get("devicenumber").toString();
				//参数时间
				String datetime = objMap.get("collecttime").toString();
				//参数名称
//				String dataname = request.getParameter("datename") == null ? "" : request.getParameter("datename");
				//数据内容
//				String oval = request.getParameter("oval") == null ? "" : request.getParameter("oval");
				String key = objMap.get("rmk1").toString();
				List<Datapoints> list = new ArrayList<Datapoints>();
				List<Data> d1 = new ArrayList<Data>();
				List<Data> d2 = new ArrayList<Data>();
				List<Data> d3 = new ArrayList<Data>();
				List<Data> d4 = new ArrayList<Data>();
				List<Data> d5 = new ArrayList<Data>();
				List<Data> d6 = new ArrayList<Data>();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				d1.add(new Data(format.format(f.parse(datetime)), Double.parseDouble(objMap.get("温度").toString())));
				d2.add(new Data(format.format(f.parse(datetime)), Double.parseDouble(objMap.get("相对湿度").toString())));
				d3.add(new Data(format.format(f.parse(datetime)), Double.parseDouble(objMap.get("风力").toString())));
				d4.add(new Data(format.format(f.parse(datetime)), objMap.get("风向")));
				d5.add(new Data(format.format(f.parse(datetime)), Double.parseDouble(objMap.get("降水量").toString())));
				d6.add(new Data(format.format(f.parse(datetime)), objMap.get("空气质量")));
				list.add(new Datapoints("温度", d1));
				list.add(new Datapoints("相对湿度", d2));
				list.add(new Datapoints("风力", d3));
				list.add(new Datapoints("风向", d4));
				list.add(new Datapoints("降水量", d5));
				list.add(new Datapoints("空气质量", d6));
				Map<String, List<Datapoints>> map = new HashMap<String, List<Datapoints>>();
				map.put("datastreams", list);
				AddDatapointsApi api = new AddDatapointsApi(map, null, null, devid, key);
				BasicResponse<Void> response = api.executeApi();
				System.out.println("气象数据同步至OneNet:"+devid+" errno:"+response.errno+" error:"+response.error);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
