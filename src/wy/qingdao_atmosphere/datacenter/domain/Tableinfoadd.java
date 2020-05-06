package wy.qingdao_atmosphere.datacenter.domain;

import javax.persistence.Entity;

/**
 * 对象类型表管理
 * @author User
 *
 */
@Entity
public class Tableinfoadd {
	  private int tableid;			//表主键ID
	  private int objtypeid;		//对象类型ID
	  private String tableshowname; //表名称
	  private int isused;			//是否删除
	  private String tabletype;		//表类型
	public int getTableid() {
		return tableid;
	}
	public void setTableid(int tableid) {
		this.tableid = tableid;
	}
	public int getObjtypeid() {
		return objtypeid;
	}
	public void setObjtypeid(int objtypeid) {
		this.objtypeid = objtypeid;
	}
	public String getTableshowname() {
		return tableshowname;
	}
	public void setTableshowname(String tableshowname) {
		this.tableshowname = tableshowname;
	}
	public int getIsused() {
		return isused;
	}
	public void setIsused(int isused) {
		this.isused = isused;
	}
	public String getTabletype() {
		return tabletype;
	}
	public void setTabletype(String tabletype) {
		this.tabletype = tabletype;
	}
}
