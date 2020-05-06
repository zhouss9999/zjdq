package wy.qingdao_atmosphere.countrysitedata.domain;
/**
 * 空间表
 * @author Lenovo
 *
 */
public class SpaceTable {
	private Integer  objid;
	
	private Integer id;
	
	private String shape;
	
	private String tablename;

	public Integer getObjid() {
		return objid;
	}

	public void setObjid(Integer objid) {
		this.objid = objid;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	
	
	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	@Override
	public String toString() {
		return "SpaceTable [objid=" + objid + ", id=" + id + ", shape=" + shape
				+ "]";
	}
	
    

}
