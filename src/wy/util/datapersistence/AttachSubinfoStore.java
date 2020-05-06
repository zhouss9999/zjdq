package wy.util.datapersistence;

public class AttachSubinfoStore {
	int subinfoid;
	int objid;
	long fieldid;
	String fieldname;
	String fieldvalue;
	String valueid;
	
	public AttachSubinfoStore(){
		
	}
	
	public AttachSubinfoStore(int subinfoid, int objid, long fieldid,
			String fieldname,String fieldvalue, String valueid) {
		super();
		this.subinfoid = subinfoid;
		this.objid = objid;
		this.fieldid = fieldid;
		this.fieldname = fieldname;
		this.fieldvalue = fieldvalue;
		this.valueid = valueid;
	}

	public int getSubinfoid() {
		return subinfoid;
	}

	public void setSubinfoid(int subinfoid) {
		this.subinfoid = subinfoid;
	}

	public int getObjid() {
		return objid;
	}

	public void setObjid(int objid) {
		this.objid = objid;
	}
	
	

	public String getFieldname() {
		return fieldname;
	}

	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}

	public long getFieldid() {
		return fieldid;
	}

	public void setFieldid(long fieldid) {
		this.fieldid = fieldid;
	}

	public String getFieldvalue() {
		return fieldvalue;
	}

	public void setFieldvalue(String fieldvalue) {
		this.fieldvalue = fieldvalue;
	}

	public String getValueid() {
		return valueid;
	}

	public void setValueid(String valueid) {
		this.valueid = valueid;
	}
	
	
}
