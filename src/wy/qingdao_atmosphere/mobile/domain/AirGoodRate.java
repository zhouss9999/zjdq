package wy.qingdao_atmosphere.mobile.domain;

import javax.persistence.Entity;

/**
 * 优良率辅助javaBean
 * @author zzb
 *
 */

@Entity
public class AirGoodRate {
	
	private int objid;			//对象objid
	private String sitenumber;	//站点编号
	private String sitename;	//站点名称
	private String lon;			//地理位置-经度
	private String lat;			//地理位置-纬度
	private String issite;		//是否国站(国站/微站)
	private String goodrate;	//优良率
	
	public AirGoodRate(){}
	
	public AirGoodRate(int objid, String sitenumber, String sitename, String issite, String goodrate) {
		super();
		this.objid = objid;
		this.sitenumber = sitenumber;
		this.sitename = sitename;
		this.issite = issite;
		this.goodrate = goodrate;
	}

	public int getObjid() {
		return objid;
	}
	public void setObjid(int objid) {
		this.objid = objid;
	}
	public String getSitenumber() {
		return sitenumber;
	}
	public void setSitenumber(String sitenumber) {
		this.sitenumber = sitenumber;
	}
	public String getSitename() {
		return sitename;
	}
	public void setSitename(String sitename) {
		this.sitename = sitename;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getIssite() {
		return issite;
	}
	public void setIssite(String issite) {
		this.issite = issite;
	}
	public String getGoodrate() {
		return goodrate;
	}
	public void setGoodrate(String goodrate) {
		this.goodrate = goodrate;
	}
	
}