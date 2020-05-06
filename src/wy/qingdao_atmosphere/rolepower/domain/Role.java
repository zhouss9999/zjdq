package wy.qingdao_atmosphere.rolepower.domain;

import javax.persistence.Entity;

/**
 * 用来存放角色信息的javabean
 * @author User
 *
 */
@Entity
public class Role {
	private String id;	//角色id
	private String role_name; //角色名称
	private String memo;	//备注
	private int order_num;	//排序id
	private int islock;		//是否显示id
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRole_name() {
		return role_name;
	}
	public void setRole_name(String role_name) {
		this.role_name = role_name;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public int getOrder_num() {
		return order_num;
	}
	public void setOrder_num(int order_num) {
		this.order_num = order_num;
	}
	public int getIslock() {
		return islock;
	}
	public void setIslock(int islock) {
		this.islock = islock;
	}
}
