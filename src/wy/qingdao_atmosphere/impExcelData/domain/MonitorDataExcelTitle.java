package wy.qingdao_atmosphere.impExcelData.domain;

import javax.persistence.Entity;

/**
 * 用来存放监测数据模板导出的表头的javabean
 * @author User
 *
 */
@Entity
public class MonitorDataExcelTitle {
	private int paramid;	//参数id
	private String paramkey;//表头key
	public int getParamid() {
		return paramid;
	}
	public void setParamid(int paramid) {
		this.paramid = paramid;
	}
	public String getParamkey() {
		return paramkey;
	}
	public void setParamkey(String paramkey) {
		this.paramkey = paramkey;
	}
}
