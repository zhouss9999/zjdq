package wy.qingdao_atmosphere.mobile.domain;
/**
 * @Description 天气预报数据javabean
 * @author Octocat
 * @version V1.0
 * @date 2018-10-10
 * 
 */
public class WeatherForecastData {
	private int objid;			// 对象id
	private String objname;		// 对象名称
	private String wendu;		// 温度
	private String fengxiang;	// 风向
	private String fengli;		// 风力
	private String collecttime;	// 监测时间
	public int getObjid() {
		return objid;
	}
	public void setObjid(int objid) {
		this.objid = objid;
	}
	public String getObjname() {
		return objname;
	}
	public void setObjname(String objname) {
		this.objname = objname;
	}
	public String getWendu() {
		return wendu;
	}
	public void setWendu(String wendu) {
		this.wendu = wendu;
	}
	public String getFengxiang() {
		return fengxiang;
	}
	public void setFengxiang(String fengxiang) {
		this.fengxiang = fengxiang;
	}
	public String getFengli() {
		return fengli;
	}
	public void setFengli(String fengli) {
		this.fengli = fengli;
	}
	public String getCollecttime() {
		return collecttime;
	}
	public void setCollecttime(String collecttime) {
		this.collecttime = collecttime;
	}
}
