package wy.qingdao_atmosphere.countrysitedata.domain;

public class AttachInfoStore {
	
	private String objid;
	
	private String fieldid;
	
	private String fieldvalue;

	public String getObjid() {
		return objid;
	}

	public void setObjid(String objid) {
		this.objid = objid;
	}

	public String getFieldid() {
		return fieldid;
	}

	public void setFieldid(String fieldid) {
		this.fieldid = fieldid;
	}

	public String getFieldvalue() {
		return fieldvalue;
	}

	public void setFieldvalue(String fieldvalue) {
		this.fieldvalue = fieldvalue;
	}

	@Override
	public String toString() {
		return "AttachInfoStore [objid=" + objid + ", fieldid=" + fieldid
				+ ", fieldvalue=" + fieldvalue + "]";
	}
	
	

}
