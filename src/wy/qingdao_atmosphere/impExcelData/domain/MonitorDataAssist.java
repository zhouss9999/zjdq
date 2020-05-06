package wy.qingdao_atmosphere.impExcelData.domain;

import javax.persistence.Entity;
/**
 * 用来存放数据库查询出来的监测数据的javabean
 * @author User
 *
 */
@Entity
public class MonitorDataAssist {
	private String objid;			//对象objid
	private String objname;			//对象名称
	private int paramid;			//参数id
	private String paramname;		//参数名称
	private String dataunit;	//参数单位
	private String datavalue;		//监测数据值
	private String collecttime;		//监测时间
	public String getObjid() {
		return objid;
	}
	public void setObjid(String objid) {
		this.objid = objid;
	}
	public String getObjname() {
		return objname;
	}
	public void setObjname(String objname) {
		this.objname = objname;
	}
	public int getParamid() {
		return paramid;
	}
	public void setParamid(int paramid) {
		this.paramid = paramid;
	}
	public String getDatavalue() {
		return datavalue;
	}
	public void setDatavalue(String datavalue) {
		this.datavalue = datavalue;
	}
	public String getCollecttime() {
		return collecttime;
	}
	public void setCollecttime(String collecttime) {
		this.collecttime = collecttime;
	}
	public String getParamname() {
		return paramname;
	}
	public void setParamname(String paramname) {
		this.paramname = paramname;
	}
	public String getDataunit() {
		return dataunit;
	}
	public void setDataunit(String dataunit) {
		this.dataunit = dataunit;
	}
}
