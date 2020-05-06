package wy.qingdao_atmosphere.reportform.timer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import wy.qingdao_atmosphere.datacenter.service.DataCenterService;
import wy.qingdao_atmosphere.reportform.service.ReportformService;
import wy.util.AirBean;
import wy.util.Calc;
import wy.util.datapersistence.Dao.BaseaddDao;

/**
 * 定时获取微站大气监测小时数据
 * @author KEYI
 *
 */
@Component
public class ReportformTimer {
	@Resource
	private BaseaddDao baseaddDao;
	
	@Resource
	private ReportformService reportformService;
	
	@Resource
	private DataCenterService dataCenterService;
	
	//定时任务取数据，每小时一次
	//@Scheduled(cron = "0 0 * * * ?")
	public void getSynchroDataHours(){
		Logger.getLogger("").info("-----------微站同步小时数据-------------");
		try {
			getSynchroData("1", 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			baseaddDao.addUserOperateLog("异常操作","微站保存小时数据", "", e.getMessage(), "false", null, null);
		}
	}
	
	//@Scheduled(cron = "0 0 0 * * ?")
	public void getSynchroData24Hours(){
		try {
			getSynchroData("2", 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			baseaddDao.addUserOperateLog("异常操作","微站保存日数据", "", e.getMessage(), "false", null, null);
		}
	}
	
	/**
	 * 
	 * @param objtypeid     1是站点(监测实时数据) 2是站点监测日数据
	 * @param type			0是小时   1是日数据
	 * @throws Exception
	 */
	public void  getSynchroData(String objtypeid,int type) throws Exception{
		//查询需要同步数据的站点
		Map<String, Object> querymap = new HashMap<String, Object>();
		querymap.put("objtypeid", objtypeid);
		List<Map<String, Object>> list = reportformService.getwz(querymap);
		for(int i=0;i<list.size();i++){
			Map<String, Object> quymap = list.get(i);
			System.out.println(quymap.get("devicename")+"   "+quymap.get("devicenumber"));
			String key = quymap.get("rmk1").toString();
			String devid = quymap.get("devicenumber").toString();
			//需要查询的数据
//			String datastreamIds = "so2,no2,pm10,pm25,co,o3,o38,AQI";
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
			if(type==1){
				format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			}
			AirBean airBean = new AirBean(0,null,null,0,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D);
			airBean.setObjid(Integer.parseInt(quymap.get("objid").toString()));//对象id
			airBean.setType(type);//0是小时   1是日数据
			airBean.setCollecttime(format.format(new Date()));
			airBean = dataCenterService.getAirBean(airBean,devid,key);//获取平均值
//			airBean.setPm25(57D);
//			airBean.setPm10(67D);
//			airBean.setCo(1.0D);
//			airBean.setNo2(90D);
//			airBean.setO3(111D);
//			airBean.setO38(118D);
//			airBean.setSo2(17D);
//			airBean.setPm10_24h(88.0);
//			airBean.setPm25_24h(49.0);
			if(airBean!=null){
				DecimalFormat df = new DecimalFormat("#0.0");
				//df.setRoundingMode(RoundingMode.HALF_UP);四舍五入
				airBean = Calc.AirQ(airBean);//计算AQI,污染物,空气质量等级
				Map<String, Object> map = new HashMap<String, Object>();
	        	map.put("devicenumber", devid);
	        	map.put("paramname", "AQI");
	        	map.put("datavalue", airBean.getAqi());
	    		map.put("collecttime", airBean.getCollecttime());
	    		map.put("objid", airBean.getObjid());
	    		dataCenterService.addwzData(map);
	    		
	    		if(type==0){
	        		//实时数据  报警阈值监测
	        		dataCenterService.isThreshold(airBean);
	        		map.put("paramname", "pm10_24h");
		        	map.put("datavalue", df.format(airBean.getPm10_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "pm25_24h");
		        	map.put("datavalue", df.format(airBean.getPm25_24h()));
		        	dataCenterService.addwzData(map);
		        	
		        	map.put("paramname", "so2");
		        	map.put("datavalue", df.format(airBean.getSo2()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "no2");
		        	map.put("datavalue", df.format(airBean.getNo2()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "pm10");
		        	map.put("datavalue", df.format(airBean.getPm10()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "pm25");
		        	map.put("datavalue", df.format(airBean.getPm25()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "co");
		        	map.put("datavalue", df.format(airBean.getCo()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "o3");
		        	map.put("datavalue", df.format(airBean.getO3()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "O38");
		        	map.put("datavalue", df.format(airBean.getO38()));
		        	dataCenterService.addwzData(map);
	        	}else{
	        		map.put("paramname", "so2");
		        	map.put("datavalue", df.format(airBean.getSo2_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "no2");
		        	map.put("datavalue", df.format(airBean.getNo2_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "pm10");
		        	map.put("datavalue", df.format(airBean.getPm10_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "pm25");
		        	map.put("datavalue", df.format(airBean.getPm25_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "co");
		        	map.put("datavalue", df.format(airBean.getCo_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "o3");
		        	map.put("datavalue", df.format(airBean.getO3_24h()));
		        	dataCenterService.addwzData(map);
		        	map.put("paramname", "o38");
		        	map.put("datavalue", df.format(airBean.getO38_24h()));
		        	dataCenterService.addwzData(map);
	        	}
	    		
	        	
	        	map.put("paramname", "quality");
	        	map.put("datavalue", airBean.getAqilevel());
	        	dataCenterService.addwzData(map);
	        	map.put("paramname", "primary_pollutant");
	        	map.put("datavalue", airBean.getPp());
	        	dataCenterService.addwzData(map);
			}
		}
	}
	
	//每小时
	//@Scheduled(cron = "0 0 * * * ?")
	public void getOneNetData() throws Exception{
		//需要查询的数据
		String[] Datas = {"fx","fs","wd","sd","dqyl","jyl_h"};
		Map<String, Object> querymap = new HashMap<String, Object>();
		querymap.put("objtypeid", 1);
		List<Map<String, Object>> list = reportformService.getwz(querymap);
		DecimalFormat df = new DecimalFormat("#0.0000");
		for(int i=0;i<list.size();i++){
			Map<String, Object> quymap = list.get(i);
			System.out.println(quymap.get("devicename")+"   "+quymap.get("devicenumber"));
			String key = quymap.get("rmk1").toString();
			String devid = quymap.get("devicenumber").toString();
			SimpleDateFormat formatN = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
			String collecttime = formatN.format(new Date());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat OneNETformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String end = OneNETformat.format(format.parse(collecttime));
			//一小时
			String start = OneNETformat.format(format.parse(format.format(((format.parse(collecttime)).getTime()-3600000))));
			for(String datax:Datas){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rmk1", key);
				map.put("devicenumber", devid);
				map.put("start", start);
				map.put("end", end);
				map.put("datastreamids", datax);
				Double d = dataCenterService.getAvg(map);//oneNet取平均值
				if(d!=null){
					Map<String, Object> cmap = new HashMap<String, Object>();
					cmap.put("devicenumber", devid);
					cmap.put("paramname", datax);
		        	cmap.put("datavalue", df.format(d));
		        	cmap.put("collecttime", collecttime);
		        	cmap.put("objid", quymap.get("objid").toString());
		    		dataCenterService.addwzData(cmap);//存数据
				}
			}
		}
	}
}