package wy.qingdao_atmosphere.countrysitedata.domain;

public class DbConnOid {
	private Integer connectid;
	
	private Integer objid;
	
	private Integer otherobjid;
	
	private String dsname;
	
	private Integer dsid;

	public Integer getConnectid() {
		return connectid;
	}

	public void setConnectid(Integer connectid) {
		this.connectid = connectid;
	}

	public Integer getObjid() {
		return objid;
	}

	public void setObjid(Integer objid) {
		this.objid = objid;
	}

	public Integer getOtherobjid() {
		return otherobjid;
	}

	public void setOtherobjid(Integer otherobjid) {
		this.otherobjid = otherobjid;
	}

	public String getDsname() {
		return dsname;
	}

	public void setDsname(String dsname) {
		this.dsname = dsname;
	}

	public Integer getDsid() {
		return dsid;
	}

	public void setDsid(Integer dsid) {
		this.dsid = dsid;
	}

	@Override
	public String toString() {
		return "DbConnOid [connectid=" + connectid + ", objid=" + objid
				+ ", otherobjid=" + otherobjid + ", dsname=" + dsname
				+ ", dsid=" + dsid + "]";
	}
	
	

}
