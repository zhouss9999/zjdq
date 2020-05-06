package wy.qingdao_atmosphere.countrysitedata.domain;

public class CpinfoObj {
	private Integer objid;
	
	private String objtypeid;
	
	private String objname;
	
	private String objnum;
	
	private String remark;
	
	private String isused;
	
	

	public Integer getObjid() {
		return objid;
	}

	public void setObjid(Integer objid) {
		this.objid = objid;
	}

	public String getObjtypeid() {
		return objtypeid;
	}

	public void setObjtypeid(String objtypeid) {
		this.objtypeid = objtypeid;
	}

	public String getObjname() {
		return objname;
	}

	public void setObjname(String objname) {
		this.objname = objname;
	}

	public String getObjnum() {
		return objnum;
	}

	public void setObjnum(String objnum) {
		this.objnum = objnum;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIsused() {
		return isused;
	}

	public void setIsused(String isused) {
		this.isused = isused;
	}

	@Override
	public String toString() {
		return "CpinfoObj [objid=" + objid + ", objtypeid=" + objtypeid
				+ ", objname=" + objname + ", objnum=" + objnum + ", remark="
				+ remark + ", isused=" + isused + "]";
	}
	
	

}
