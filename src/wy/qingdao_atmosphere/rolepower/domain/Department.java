package wy.qingdao_atmosphere.rolepower.domain;

import javax.persistence.Entity;

/**
 * 用来存放部门信息的javabean
 * @author User
 *
 */
@Entity
public class Department {
	private String id; //主键
	private String mc; //部门名称
	private String flag;
	private int fcountycode;
	private String parent_id;//父级部门id
	private String add_time;
	private int valid;
	private String memo; //备注
	private String principal;
	private String dutytel;
	private String fax;
	private String postcode;
	private int showindex; //排序
	private String addtime;
	private String icon_path;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMc() {
		return mc;
	}
	public void setMc(String mc) {
		this.mc = mc;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public int getFcountycode() {
		return fcountycode;
	}
	public void setFcountycode(int fcountycode) {
		this.fcountycode = fcountycode;
	}
	public String getParent_id() {
		return parent_id;
	}
	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}
	public String getAdd_time() {
		return add_time;
	}
	public void setAdd_time(String add_time) {
		this.add_time = add_time;
	}
	public int getValid() {
		return valid;
	}
	public void setValid(int valid) {
		this.valid = valid;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getPrincipal() {
		return principal;
	}
	public void setPrincipal(String principal) {
		this.principal = principal;
	}
	public String getDutytel() {
		return dutytel;
	}
	public void setDutytel(String dutytel) {
		this.dutytel = dutytel;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public int getShowindex() {
		return showindex;
	}
	public void setShowindex(int showindex) {
		this.showindex = showindex;
	}
	public String getAddtime() {
		return addtime;
	}
	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}
	public String getIcon_path() {
		return icon_path;
	}
	public void setIcon_path(String icon_path) {
		this.icon_path = icon_path;
	}
}
