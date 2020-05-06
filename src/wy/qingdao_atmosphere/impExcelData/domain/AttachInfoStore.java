package wy.qingdao_atmosphere.impExcelData.domain;

import javax.persistence.Entity;

@Entity
public class AttachInfoStore {
	
	int infoid;
	int objid;
	int fieldid;
	String fieldvalue;
	
	public AttachInfoStore(){
		
	}
	
	public AttachInfoStore(int infoid, int objid, int fieldid,
			String fieldvalue) {
		super();
		this.infoid = infoid;
		this.objid = objid;
		this.fieldid = fieldid;
		this.fieldvalue = fieldvalue;
	}

	public int getInfoid() {
		return infoid;
	}

	public void setInfoid(int infoid) {
		this.infoid = infoid;
	}

	public int getObjid() {
		return objid;
	}

	public void setObjid(int objid) {
		this.objid = objid;
	}

	public int getFieldid() {
		return fieldid;
	}

	public void setFieldid(int fieldid) {
		this.fieldid = fieldid;
	}

	public String getFieldvalue() {
		return fieldvalue;
	}

	public void setFieldvalue(String fieldvalue) {
		this.fieldvalue = fieldvalue;
	}
	
	
}
