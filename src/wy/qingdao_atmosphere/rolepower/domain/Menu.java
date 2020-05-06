package wy.qingdao_atmosphere.rolepower.domain;

import javax.persistence.Entity;

/**
 * 用来存放菜单信息的javabean
 * @author User
 *
 */
@Entity
public class Menu {
	private String module_key;    //菜单主键
	private String module_name;   //菜单名称
	private String higherlevelkey;//父级菜单key
	private String localurl;	  //本地URL
	private String icon_path;	  //图标地址
	private String remoteurl;	  //远程URL
	private int module_sort;	  //菜单等级
	private int delet_flag;	  	  //删除标记
	private String defaultpageurl;//默认页面URL
	private int showindex;		  //排序
	public String getModule_key() {
		return module_key;
	}
	public void setModule_key(String module_key) {
		this.module_key = module_key;
	}
	public String getModule_name() {
		return module_name;
	}
	public void setModule_name(String module_name) {
		this.module_name = module_name;
	}
	public String getLocalurl() {
		return localurl;
	}
	public void setLocalurl(String localurl) {
		this.localurl = localurl;
	}
	public String getIcon_path() {
		return icon_path;
	}
	public void setIcon_path(String icon_path) {
		this.icon_path = icon_path;
	}
	public String getRemoteurl() {
		return remoteurl;
	}
	public void setRemoteurl(String remoteurl) {
		this.remoteurl = remoteurl;
	}
	public int getModule_sort() {
		return module_sort;
	}
	public void setModule_sort(int module_sort) {
		this.module_sort = module_sort;
	}
	public int getDelet_flag() {
		return delet_flag;
	}
	public void setDelet_flag(int delet_flag) {
		this.delet_flag = delet_flag;
	}
	public String getDefaultpageurl() {
		return defaultpageurl;
	}
	public void setDefaultpageurl(String defaultpageurl) {
		this.defaultpageurl = defaultpageurl;
	}
	public String getHigherlevelkey() {
		return higherlevelkey;
	}
	public void setHigherlevelkey(String higherlevelkey) {
		this.higherlevelkey = higherlevelkey;
	}
	public int getShowindex() {
		return showindex;
	}
	public void setShowindex(int showindex) {
		this.showindex = showindex;
	}
}
