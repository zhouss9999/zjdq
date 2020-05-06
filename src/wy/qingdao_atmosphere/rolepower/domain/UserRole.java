package wy.qingdao_atmosphere.rolepower.domain;

import javax.persistence.Entity;

/**
 * 用来存放用户对应角色的信息的javabean
 * @author User
 *
 */
@Entity
public class UserRole {
	private String id; //主键id
	private String username; //用户userid
	private String role_id;  //角色id
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRole_id() {
		return role_id;
	}
	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}
}
