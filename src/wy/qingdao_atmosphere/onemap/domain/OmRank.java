package wy.qingdao_atmosphere.onemap.domain;

import javax.persistence.Entity;

/**
 * 按区域/站点排名辅助JavaBean
 * @author zzb
 *
 */

@Entity
public class OmRank {
	
	private int rank;					//名次
	private int objid;					//对象id
	private String arsiname = "-";		//区域名称/站点名称
	private String issite = "-";		//站点控制级别(国站/微站)
	
	private String collecttime = "";	//数据时间
	private String aqi = "-";			//AQI
	private String pm25 = "-";			//PM2.5
	private String pm10 = "-";			//PM10
	private String co = "-";			//CO
	private String no2 = "-";			//NO2
	private String so2 = "-";			//SO2
	private String o3 = "-";			//O3
	private String o38 = "-";			//O38
	private String voc = "-";			//VOC
	private String quality = "-";		//空气质量
	private String pollutant = "-";		//首要污染物
	
	private String goodrate = "-";		//优良率
	private String goodday = "-";		//优良天数
	private String daysize = "-";		//总天数
	private String alarmday = "-";		//超标天数
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getObjid() {
		return objid;
	}
	public void setObjid(int objid) {
		this.objid = objid;
	}
	public String getArsiname() {
		return arsiname;
	}
	public void setArsiname(String arsiname) {
		this.arsiname = arsiname;
	}
	public String getIssite() {
		return issite;
	}
	public void setIssite(String issite) {
		this.issite = issite;
	}
	public String getCollecttime() {
		return collecttime;
	}
	public void setCollecttime(String collecttime) {
		this.collecttime = collecttime;
	}
	public String getAqi() {
		return aqi;
	}
	public void setAqi(String aqi) {
		this.aqi = aqi;
	}
	public String getPm25() {
		return pm25;
	}
	public void setPm25(String pm25) {
		this.pm25 = pm25;
	}
	public String getPm10() {
		return pm10;
	}
	public void setPm10(String pm10) {
		this.pm10 = pm10;
	}
	public String getCo() {
		return co;
	}
	public void setCo(String co) {
		this.co = co;
	}
	public String getNo2() {
		return no2;
	}
	public void setNo2(String no2) {
		this.no2 = no2;
	}
	public String getSo2() {
		return so2;
	}
	public void setSo2(String so2) {
		this.so2 = so2;
	}
	public String getO3() {
		return o3;
	}
	public void setO3(String o3) {
		this.o3 = o3;
	}
	public String getO38() {
		return o38;
	}
	public void setO38(String o38) {
		this.o38 = o38;
	}
	public String getVoc() {
		return voc;
	}
	public void setVoc(String voc) {
		this.voc = voc;
	}
	public String getQuality() {
		return quality;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	public String getPollutant() {
		return pollutant;
	}
	public void setPollutant(String pollutant) {
		this.pollutant = pollutant;
	}
	public String getGoodrate() {
		return goodrate;
	}
	public void setGoodrate(String goodrate) {
		this.goodrate = goodrate;
	}
	public String getGoodday() {
		return goodday;
	}
	public void setGoodday(String goodday) {
		this.goodday = goodday;
	}
	public String getDaysize() {
		return daysize;
	}
	public void setDaysize(String daysize) {
		this.daysize = daysize;
	}
	public String getAlarmday() {
		return alarmday;
	}
	public void setAlarmday(String alarmday) {
		this.alarmday = alarmday;
	}
	
}
