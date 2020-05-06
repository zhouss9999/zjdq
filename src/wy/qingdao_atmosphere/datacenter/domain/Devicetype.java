package wy.qingdao_atmosphere.datacenter.domain;

import javax.persistence.Entity;

@Entity
public class Devicetype {
	private int devicetypeid;//主键
	private String devicetypename;//设备类型名称
	private String pinpai;//品牌
	private String xinghao;//型号
	private String rmk1;//备注
	private String rmk2;
	private String rmk3;
	private int isused;//状态
	public int getDevicetypeid() {
		return devicetypeid;
	}
	public void setDevicetypeid(int devicetypeid) {
		this.devicetypeid = devicetypeid;
	}
	public String getDevicetypename() {
		return devicetypename;
	}
	public void setDevicetypename(String devicetypename) {
		this.devicetypename = devicetypename;
	}
	public String getPinpai() {
		return pinpai;
	}
	public void setPinpai(String pinpai) {
		this.pinpai = pinpai;
	}
	public String getXinghao() {
		return xinghao;
	}
	public void setXinghao(String xinghao) {
		this.xinghao = xinghao;
	}
	public String getRmk1() {
		return rmk1;
	}
	public void setRmk1(String rmk1) {
		this.rmk1 = rmk1;
	}
	public String getRmk2() {
		return rmk2;
	}
	public void setRmk2(String rmk2) {
		this.rmk2 = rmk2;
	}
	public String getRmk3() {
		return rmk3;
	}
	public void setRmk3(String rmk3) {
		this.rmk3 = rmk3;
	}
	public int getIsused() {
		return isused;
	}
	public void setIsused(int isused) {
		this.isused = isused;
	}
	
}
