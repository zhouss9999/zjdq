package wy.qingdao_atmosphere.reportform.service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import wy.qingdao_atmosphere.reportform.dao.ReportformDao;
import wy.util.AirBean;
import wy.util.Calc;

@Service("reportformService")
public class ReportformServiceImpl implements ReportformService{
	@Resource
	private ReportformDao reportformDao;

	public List<Map<String, Object>> getwz(Map<String, Object> map) {
		return reportformDao.getwz(map);
	}

	//查询实时报数据 或 日报
	public List<Map<String, Object>> queryrealData(Map<String, Object> map){
		List<Map<String, Object>> list = reportformDao.queryrealData(map);
		for(Map<String, Object> m:list){
			AirBean airBean = new AirBean(0,null,null,0,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D);
			if(map.get("objtypeid").equals("1,5")){
				airBean.setType(0);
				airBean.setCo(Double.parseDouble(m.get("co").toString()));
				airBean.setSo2(Double.parseDouble(m.get("so2").toString()));
				airBean.setNo2(Double.parseDouble(m.get("no2").toString()));
				airBean.setPm10(Double.parseDouble(m.get("pm10").toString()));
				airBean.setPm25(Double.parseDouble(m.get("pm25").toString()));
				airBean.setO3(Double.parseDouble(m.get("o3").toString()));
				airBean.setO38(Double.parseDouble(m.get("o38").toString()));
				airBean.setPm10_24h(Double.parseDouble(m.get("pm10_24h").toString()));
				airBean.setPm25_24h(Double.parseDouble(m.get("pm25_24h").toString()));
				airBean.setQy(Double.parseDouble(m.get("qy").toString()));
				airBean.setWd(Double.parseDouble(m.get("wd").toString()));
				airBean.setSd(Double.parseDouble(m.get("sd").toString()));
				airBean.setFl(Double.parseDouble(m.get("fl").toString()));
				airBean.setJsl(Double.parseDouble(m.get("jsl").toString()));
				
				airBean = Calc.AirQ(airBean);
				m.put("iso2", airBean.getIso2());
				m.put("ino2", airBean.getIno2());
				m.put("ipm10", airBean.getIpm10());
				m.put("ipm25", airBean.getIpm25());
				m.put("ico", airBean.getIco());
				m.put("io3", airBean.getIo3());
				m.put("io38", airBean.getIo38());
				m.put("ipm10_24h", airBean.getIpm10_24h());
				m.put("ipm25_24h", airBean.getIpm25_24h());
				m.put("qy", airBean.getQy());
				m.put("wd", airBean.getWd());
				m.put("sd", airBean.getSd());
				m.put("fl", airBean.getFl());
				m.put("jsl", airBean.getJsl());
			}else{
				airBean.setType(1);
				airBean.setCo_24h(Double.parseDouble(m.get("co").toString()));
				airBean.setSo2_24h(Double.parseDouble(m.get("so2").toString()));
				airBean.setNo2_24h(Double.parseDouble(m.get("no2").toString()));
				airBean.setPm10_24h(Double.parseDouble(m.get("pm10").toString()));
				airBean.setPm25_24h(Double.parseDouble(m.get("pm25").toString()));
				airBean.setO3_24h(Double.parseDouble(m.get("o3").toString()));
				airBean.setO38_24h(Double.parseDouble(m.get("o38").toString()));
				airBean.setQy(Double.parseDouble(m.get("qy").toString()));
				airBean.setWd(Double.parseDouble(m.get("wd").toString()));
				airBean.setSd(Double.parseDouble(m.get("sd").toString()));
				airBean.setFl(Double.parseDouble(m.get("fl").toString()));
				airBean.setJsl(Double.parseDouble(m.get("jsl").toString()));
				airBean = Calc.AirQ(airBean);
				m.put("iso2", airBean.getIso2_24h());
				m.put("ino2", airBean.getIno2_24h());
				m.put("ipm10", airBean.getIpm10_24h());
				m.put("ipm25", airBean.getIpm25_24h());
				m.put("ico", airBean.getIco_24h());
				m.put("io3", airBean.getIo3_24h());
				m.put("io38", airBean.getIo38_24h());
				m.put("qy", airBean.getQy());
				m.put("wd", airBean.getWd());
				m.put("sd", airBean.getSd());
				m.put("fl", airBean.getFl());
				m.put("jsl", airBean.getJsl());
			}
			
		}
		return list;
	}
	
	//月报计算
	public List<Map<String, Object>> queryMonthlyReport(List<Map<String, Object>> list) throws Exception{
		List<Map<String,Object>> dylist = new ArrayList<Map<String,Object>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-01");
		List<Map<String, Object>> resultlist = getmothReport(list, sdf.parse(sdf.format(new Date())), dylist,0);
		for(int i=0;i<resultlist.size();i++){
			Map<String, Object> map = resultlist.get(i);
			if(resultlist.size()==1||i==resultlist.size()-1){
				map.put("huanbi", "");
			}else{
				//与上月环比计算
				double huanbi = ((Double)map.get("yl")-(Double)(resultlist.get(i+1).get("yl")))/(Double)(resultlist.get(i+1).get("yl"));
				DecimalFormat df = new DecimalFormat("#0.00");
				map.put("huanbi", df.format(huanbi*100));
			}
		}
		return resultlist;
	}
	
	//月报遍历计算  Type=0 月报   Type=1年报
	public List<Map<String,Object>> getmothReport(List<Map<String,Object>> list,Date date,List<Map<String,Object>> dylist,int Type) throws Exception{
		if(list.size()==0){
			return dylist;
		}else{
			Double so2 = 0D;// 二氧化硫
			int so2Day = 0;
			Double no2 = 0D;// 二氧化氮
			int no2Day = 0;
			Double pm10 = 0D;// pm10
			int pm10Day = 0;
			Double pm25 = 0D;// pm2.5
			int pm25Day = 0;
			Double co = 0D;// 一氧化碳 
			int coDay = 0;
			Double o3 = 0D;// 臭氧1小时 
			int o3Day = 0;
			int yl = 0;
			int count = 0;
			
			Double wd = 0D;//温度
			Double sd = 0D;//湿度
			Double qy = 0D;//气压
			Double jsl = 0D;//降雨量
			Double fl = 0D;//风力

			
			String objname = "";
			for(int i=list.size()-1;i>=0;i--){
				Map<String,Object> map = list.get(i);
				SimpleDateFormat fom = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				System.out.println("传入时间"+fom.format(date));
//				System.out.println("查询时间"+map.get("collecttime").toString());
				int collecttime = (int)((fom.parse(map.get("collecttime").toString()).getTime())/1000);
				if(collecttime>=(date.getTime()/1000)){
//					System.out.println("so2:"+map.get("so2"));
					objname = map.get("objname").toString();
					so2+=Double.parseDouble(map.get("so2").toString());
					no2+=Double.parseDouble(map.get("no2").toString());
					pm10+=Double.parseDouble(map.get("pm10").toString());
					pm25+=Double.parseDouble(map.get("pm25").toString());
					co+=Double.parseDouble(map.get("co").toString());
					o3+=Double.parseDouble(map.get("o3").toString());
					
					wd+=Double.parseDouble(map.get("wd").toString());
					sd+=Double.parseDouble(map.get("sd").toString());
					fl+=Double.parseDouble(map.get("fl").toString());
					qy+=Double.parseDouble(map.get("qy").toString());
					jsl+=Double.parseDouble(map.get("jsl").toString());
					String pp = map.get("primary_pollutant").toString();
					if(pp.contains("二氧化硫")){
						so2Day++;
					}
					if(pp.contains("二氧化氮")){
						no2Day++;
					}
					if(pp.contains("颗粒物(PM10)")){
						pm10Day++;
					}
					if(pp.contains("一氧化碳")){
						coDay++;
					}
					if(pp.contains("臭氧")){
						o3Day++;
					}
					if(pp.contains("细颗粒物(PM2.5)")){
						pm25Day++;
					}
					if(map.get("quality").toString().equals("1")||map.get("quality").toString().equals("2")){
						yl++;
					}
					count++;
					list.remove(i);//删除统计过得数据
				}
			}
			Map<String, Object> resultMonthMap = new HashMap<String, Object>();
			if(count!=0){
				if(Type==0){
					SimpleDateFormat fom2 = new SimpleDateFormat("yyyy-MM");
					resultMonthMap.put("date", fom2.format(date));
				}else{
					SimpleDateFormat fom2 = new SimpleDateFormat("yyyy");
					resultMonthMap.put("date", fom2.format(date));
				}
				resultMonthMap.put("objname", objname);
				resultMonthMap.put("so2", so2/count);
				resultMonthMap.put("so2Day", so2Day);
				resultMonthMap.put("no2", no2/count);
				resultMonthMap.put("no2Day", no2Day);
				resultMonthMap.put("pm10", pm10/count);
				resultMonthMap.put("pm10Day", pm10Day);
				resultMonthMap.put("pm25", pm25/count);
				resultMonthMap.put("pm25Day", pm25Day);
				resultMonthMap.put("co", co/count);
				resultMonthMap.put("coDay", coDay);
				resultMonthMap.put("o3", o3/count);
				resultMonthMap.put("o3Day", o3Day);
				resultMonthMap.put("yl", ((double)yl/count)*100);//优良率 单位%
				resultMonthMap.put("wd", wd/count);
				resultMonthMap.put("sd", sd/count);
				resultMonthMap.put("fl", fl/count);
				resultMonthMap.put("qy", qy/count);
				resultMonthMap.put("jsl", jsl/count);
				dylist.add(resultMonthMap);
			}
			if(Type==0){//月报
				return getmothReport(list, getLastMonths(date), dylist,Type);
			}else{//年报
				return getmothReport(list, getLastYears(date), dylist,Type);
			}
			
		}
	}
	
	//获取前一年的时间
	public Date getLastYears(Date time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-01-01");
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		c.add(Calendar.YEAR, -1);
		Date m = c.getTime();
//			System.out.println(sdf.format(m));
		String t = sdf.format(m);
		try {
			time = sdf.parse(t);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}
	//获取前一月的时间
	public Date getLastMonths(Date time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-01");
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		c.add(Calendar.MONTH, -1);
		Date m = c.getTime();
//		System.out.println(sdf.format(m));
		String t = sdf.format(m);
		try {
			time = sdf.parse(t);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}
	
	//年报计算
	public List<Map<String, Object>> queryYearReport(List<Map<String, Object>> list) throws Exception{
		List<Map<String,Object>> dylist = new ArrayList<Map<String,Object>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-01-01");
		List<Map<String, Object>> resultlist = getmothReport(list, sdf.parse(sdf.format(new Date())), dylist,1);
		for(int i=0;i<resultlist.size();i++){
			Map<String, Object> map = resultlist.get(i);
			if(resultlist.size()==1||i==resultlist.size()-1){
				map.put("tongbi", "");
			}else{
				//与去年同比计算
				double huanbi = ((Double)map.get("yl")-(Double)(resultlist.get(i+1).get("yl")))/(Double)(resultlist.get(i+1).get("yl"));
				DecimalFormat df = new DecimalFormat("#0.00");
				map.put("tongbi", df.format(huanbi*100));
			}
		}
		return resultlist;
	}
	
	
	//查询探空曲线站点
	public List<Map<String, Object>> querytkqxobj(){
		return reportformDao.querytkqxobj();
	}
	
	//查询探空曲线
	public List<Map<String,Object>> queryTkqx(Map<String, Object> map){
		return reportformDao.queryTkqx(map);
	}
	
	//修改探空曲线
	public void updateTkqx(Map<String, Object> map){
		reportformDao.updateTkqx(map);
	}
	
	//AQI日历
	public List<Map<String,Object>> queryAqiCalendar(Map<String, Object> map){
		return reportformDao.queryAqiCalendar(map);
	}
	
	//计算优良率
	public Double queryGoodrate(Map<String,Object> map){
		return reportformDao.queryGoodrate(map);
	}
	
	//实时监测数据
	public List<Map<String,Object>> queryRealTimeData(Map<String,Object> map){
		return reportformDao.queryRealTimeData(map);
	}
	
	//空气质量优良率
	public List<Map<String, Object>> queryGoodratetwo(Map<String,Object> map){
		return reportformDao.queryGoodratetwo(map);
	}
	
	//气象分析
	public Map<String, Object> queryqxfx(Map<String,Object> map){
		return reportformDao.queryqxfx(map);
	}
	//气象数据同步至oneNet
	public List<Map<String, Object>> queryqxSendOneNet(Map<String,Object> map){
		return reportformDao.queryqxSendOneNet(map);
	}
}
