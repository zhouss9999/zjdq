package wy.qingdao_atmosphere.rolepower.domain;

import javax.persistence.Entity;

/**
 * 用来存放数据库查询的用户操作日志信息的javabean
 * @author User
 *
 */
@Entity
public class UserOperateLog {
	private String logtype;		//日志类型
	private String logcontent;	//日志内容
	private String devicetype;	//设备类型
	private String ip;			//ip地址ַ
	private String username;	//操作人
	private String createtime;	//操作时间
	public String getLogtype() {
		return logtype;
	}
	public void setLogtype(String logtype) {
		this.logtype = logtype;
	}
	public String getLogcontent() {
		return logcontent;
	}
	public void setLogcontent(String logcontent) {
		this.logcontent = logcontent;
	}
	public String getDevicetype() {
		return devicetype;
	}
	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
}
