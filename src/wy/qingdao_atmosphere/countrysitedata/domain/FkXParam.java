package wy.qingdao_atmosphere.countrysitedata.domain;

public class FkXParam {
	private String height; //高度
	private String spfx;  //水平风向
	private String spfs;  //水平风速
	private String czfs;  //垂直风速
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getSpfx() {
		return spfx;
	}
	public void setSpfx(String spfx) {
		this.spfx = spfx;
	}
	public String getSpfs() {
		return spfs;
	}
	public void setSpfs(String spfs) {
		this.spfs = spfs;
	}
	public String getCzfs() {
		return czfs;
	}
	public void setCzfs(String czfs) {
		this.czfs = czfs;
	}
	@Override
	public String toString() {
		return height + " " + spfx + " "
				+ spfs + " " + czfs ;
	}
	
	
	

}
