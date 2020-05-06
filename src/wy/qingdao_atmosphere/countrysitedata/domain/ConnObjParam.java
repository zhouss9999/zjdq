package wy.qingdao_atmosphere.countrysitedata.domain;

public class ConnObjParam {
	
	private String objid;
	
	private String paramid;
	
	private String dataguid;
	
	private String isused;
	
	private String paramnumber;
	
	private String devicenumber;

	public String getObjid() {
		return objid;
	}

	public void setObjid(String objid) {
		this.objid = objid;
	}

	public String getParamid() {
		return paramid;
	}

	public void setParamid(String paramid) {
		this.paramid = paramid;
	}

	public String getDataguid() {
		return dataguid;
	}

	public void setDataguid(String dataguid) {
		this.dataguid = dataguid;
	}

	public String getIsused() {
		return isused;
	}

	public void setIsused(String isused) {
		this.isused = isused;
	}

	public String getParamnumber() {
		return paramnumber;
	}

	public void setParamnumber(String paramnumber) {
		this.paramnumber = paramnumber;
	}

	public String getDevicenumber() {
		return devicenumber;
	}

	public void setDevicenumber(String devicenumber) {
		this.devicenumber = devicenumber;
	}

	@Override
	public String toString() {
		return "ConnObjParam [objid=" + objid + ", paramid=" + paramid
				+ ", dataguid=" + dataguid + ", isused=" + isused
				+ ", paramnumber=" + paramnumber + ", devicenumber="
				+ devicenumber + "]";
	}
	
	

}
