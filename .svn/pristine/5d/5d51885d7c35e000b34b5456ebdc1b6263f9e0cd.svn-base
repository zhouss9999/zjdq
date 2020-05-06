package wy.qingdao_atmosphere.onemap.domain;

import java.util.List;

import javax.persistence.Entity;

/**
 * @author 五易科技
 * @description 一张图-菜单-辅助JavaBean
 */

@Entity
public class OmMenu {
	
	private int id;					//目录id
	private String dirname;			//目录名称
	private String dirlevel;		//目录级别
	private String parentid;		//父级目录id
	private List<OmMenu> childList;	//子级目录列表
	
	public OmMenu(){}

	public OmMenu(int id, String dirname, String dirlevel,
			String parentid, List<OmMenu> childList) {
		super();
		this.id = id;
		this.dirname = dirname;
		this.dirlevel = dirlevel;
		this.parentid = parentid;
		this.childList = childList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDirname() {
		return dirname;
	}

	public void setDirname(String dirname) {
		this.dirname = dirname;
	}

	public String getDirlevel() {
		return dirlevel;
	}

	public void setDirlevel(String dirlevel) {
		this.dirlevel = dirlevel;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public List<OmMenu> getChildList() {
		return childList;
	}

	public void setChildList(List<OmMenu> childList) {
		this.childList = childList;
	}
	
}
