package wy.qingdao_atmosphere.countrysitedata.domain;

import javax.persistence.Entity;
/**
 * 参数辅助JavaBean
 * @author zzb
 *
 */
@Entity
public class ParamAssis {
	
	private long objid;//对象id
	
	private String paramid;//参数id
	
	private String paramname;//参数名称
	
	private String dataguid;//dataguid
	
	private String devicenumber;//设备编号
	
	private String devicename;//设备名称
	
	private String rmk1;//设备的key

	public long getObjid() {
		return objid;
	}

	public void setObjid(long objid) {
		this.objid = objid;
	}

	public String getParamid() {
		return paramid;
	}

	public void setParamid(String paramid) {
		this.paramid = paramid;
	}

	public String getParamname() {
		return paramname;
	}

	public void setParamname(String paramname) {
		this.paramname = paramname;
	}

	public String getDataguid() {
		return dataguid;
	}

	public void setDataguid(String dataguid) {
		this.dataguid = dataguid;
	}

	public String getDevicenumber() {
		return devicenumber;
	}

	public void setDevicenumber(String devicenumber) {
		this.devicenumber = devicenumber;
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public String getRmk1() {
		return rmk1;
	}

	public void setRmk1(String rmk1) {
		this.rmk1 = rmk1;
	}


}
