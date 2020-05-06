package wy.qingdao_atmosphere.countanalysis.domain;

import javax.persistence.Entity;

/**
 * 区域报警统计
 */

@Entity
public class AreaAlarmCount {
	
	private String area;//区域名称
	private int gyjc;	//工业监测报警次数
	private int gdjc;	//工地监测报警次数
	private int dljt;	//道路交通报警次数
	private int smjc;	//散煤监测报警次数
	private int csjc;	//传输监测报警次数
	private int jcwl;	//基础网络报警次数
	private int vocs;	//VOCs报警次数
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public int getGyjc() {
		return gyjc;
	}
	public void setGyjc(int gyjc) {
		this.gyjc = gyjc;
	}
	public int getGdjc() {
		return gdjc;
	}
	public void setGdjc(int gdjc) {
		this.gdjc = gdjc;
	}
	public int getDljt() {
		return dljt;
	}
	public void setDljt(int dljt) {
		this.dljt = dljt;
	}
	public int getSmjc() {
		return smjc;
	}
	public void setSmjc(int smjc) {
		this.smjc = smjc;
	}
	public int getCsjc() {
		return csjc;
	}
	public void setCsjc(int csjc) {
		this.csjc = csjc;
	}
	public int getJcwl() {
		return jcwl;
	}
	public void setJcwl(int jcwl) {
		this.jcwl = jcwl;
	}
	public int getVocs() {
		return vocs;
	}
	public void setVocs(int vocs) {
		this.vocs = vocs;
	}
	
}
