package wy.qingdao_atmosphere.datacenter.domain;

import javax.persistence.Entity;
/**
 * 参数管理
 * @author hero
 *
 */
@Entity
public class Param {
	private int paramid;
	private String paramtype;
	private String paramname;
	private String datatype;
	private String dataunit;
	
	public int getParamid() {
		return paramid;
	}
	public void setParamid(int paramid) {
		this.paramid = paramid;
	}
	public String getParamtype() {
		return paramtype;
	}
	public void setParamtype(String paramtype) {
		this.paramtype = paramtype;
	}
	public String getParamname() {
		return paramname;
	}
	public void setParamname(String paramname) {
		this.paramname = paramname;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public String getDataunit() {
		return dataunit;
	}
	public void setDataunit(String dataunit) {
		this.dataunit = dataunit;
	}
	
}
