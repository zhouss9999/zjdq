//code by ljq --20180321
package wy.util; 



import javax.persistence.Entity;

@Entity
public class AirBean {

	int objid;//对象objid
	String jcname;// 监测名称 
	String collecttime;// 监测时间
	int type;//0小时/1日（24小时）
	
	
	Double so2;// 二氧化硫
	Double no2;// 二氧化氮
	Double pm10;// pm10
	Double pm25;// pm2.5
	Double co;// 一氧化碳 
	Double o3;// 臭氧1小时(如果是小时计算,这里是主要计算aqi的参数,如果是日计算这里是每日的o3最大的那个小时平均值,只做参考不参与aqi计算)
	Double o38;//臭氧滑动8小时(如果是小时计算,这里是最近8小时的滑动平均值,不参与aqi计算,如果是日计算这里是每天的最大8小时的滑动平均参与aqi计算)
	
	
	
	
	Double so2_24h;// 二氧化硫24小时平均值
	Double no2_24h;// 二氧化氮24小时平均值
	Double pm10_24h;// pm1024小时平均值
	Double pm25_24h;// pm2.524小时平均值
	Double co_24h;// 一氧化碳  24小时平均值
	Double o3_24h;//臭氧日最大1小时24小时平均值 
	Double o38_24h;//臭氧日最大8小时24小时平均值
	
	
	
	Double iso2;// 二氧化硫分指数
	Double ino2;// 二氧化氮分指数
	Double ipm10;// pm10分指数
	Double ipm25;// pm2.5分指数
	Double ico;// 一氧化碳 分指数
	Double io3;// 臭氧1小时分指数(如果是小时计算,这里是主要计算aqi的参数,如果是日计算这里是每日的o3最大的那个小时平均值,只做参考不参与aqi计算)
	Double io38;//臭氧8小时分指数(如果是小时计算,这里是最近8小时的滑动平均值,不参与aqi计算,如果是日计算这里是每天的最大8小时的滑动平均参与aqi计算)
	
	Double iso2_24h;// 二氧化硫24小时平均值
	Double ino2_24h;// 二氧化氮24小时平均值
	Double ipm10_24h;// pm1024小时平均值
	Double ipm25_24h;// pm2.524小时平均值
	Double ico_24h;// 一氧化碳  24小时平均值
	Double io3_24h;//臭氧日最大1小时24小时平均值 
	Double io38_24h;//臭氧日最大8小时24小时平均值
	
	
	Double wd;
	Double sd;
	Double fl;
	Double jsl;
	Double qy;
	
	int aqi;//空气质量
	String pp;//首要污染物
	int aqilevel;//空气质量等级 1,2,3,4,5,6
	 
	 
	
	public AirBean(){
		
	}
	
	public AirBean(int objid, String jcname, String collecttime,int type,
			Double so2, Double no2, Double pm10, Double pm25,
			Double co, Double o3,  Double o38,
			Double so2_24h, Double no2_24h, Double pm10_24h, Double pm25_24h,
			Double co_24h, Double o3_24h,  Double o38_24h ) {
		super();
		this.objid = objid;
		this.jcname = jcname;
		this.collecttime = collecttime;
		this.so2 = so2;
		this.no2 = no2;
		this.pm10 = pm10;
		this.pm25 = pm25;
		this.co = co;
		this.o3 = o3;
		this.o38=o38;
		this.so2_24h = so2_24h;
		this.no2_24h = no2_24h;
		this.pm10_24h = pm10_24h;
		this.pm25_24h = pm25_24h;
		this.co_24h = co_24h;
		this.o3_24h = o3_24h;
		this.o38_24h= o38_24h;
		this.type = type;
	}

	public Double getO38() {
		return o38;
	}

	public void setO38(Double o38) {
		this.o38 = o38;
	}

	public Double getPm10_24h() {
		return pm10_24h;
	}

	public void setPm10_24h(Double pm10_24h) {
		this.pm10_24h = pm10_24h;
	}

	public Double getPm25_24h() {
		return pm25_24h;
	}

	public void setPm25_24h(Double pm25_24h) {
		this.pm25_24h = pm25_24h;
	}

	public int getObjid() {
		return objid;
	}

	public void setObjid(int objid) {
		this.objid = objid;
	}

	public String getJcname() {
		return jcname;
	}

	public void setJcname(String jcname) {
		this.jcname = jcname;
	}

	public String getCollecttime() {
		return collecttime;
	}

	public void setCollecttime(String collecttime) {
		this.collecttime = collecttime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Double getSo2() {
		return so2;
	}

	public void setSo2(Double so2) {
		this.so2 = so2;
	}

	public Double getNo2() {
		return no2;
	}

	public void setNo2(Double no2) {
		this.no2 = no2;
	}

	public Double getPm10() {
		return pm10;
	}

	public void setPm10(Double pm10) {
		this.pm10 = pm10;
	}

	public Double getPm25() {
		return pm25;
	}

	public void setPm25(Double pm25) {
		this.pm25 = pm25;
	}

	public Double getCo() {
		return co;
	}

	public void setCo(Double co) {
		this.co = co;
	}

	public Double getO3() {
		return o3;
	}

	public void setO3(Double o3) {
		this.o3 = o3;
	}

	public int getAqi() {
		return aqi;
	}

	public void setAqi(int aqi) {
		this.aqi = aqi;
	}

	public String getPp() {
		return pp;
	}

	public void setPp(String pp) {
		this.pp = pp;
	}

	public int getAqilevel() {
		return aqilevel;
	}

	public void setAqilevel(int aqilevel) {
		this.aqilevel = aqilevel;
	}

	public Double getIso2() {
		return iso2;
	}

	public void setIso2(Double iso2) {
		this.iso2 = iso2;
	}

	public Double getIno2() {
		return ino2;
	}

	public void setIno2(Double ino2) {
		this.ino2 = ino2;
	}

	public Double getIpm10() {
		return ipm10;
	}

	public void setIpm10(Double ipm10) {
		this.ipm10 = ipm10;
	}

	public Double getIpm25() {
		return ipm25;
	}

	public void setIpm25(Double ipm25) {
		this.ipm25 = ipm25;
	}

	public Double getIco() {
		return ico;
	}

	public void setIco(Double ico) {
		this.ico = ico;
	}

	public Double getIo3() {
		return io3;
	}

	public void setIo3(Double io3) {
		this.io3 = io3;
	}

	public Double getIo38() {
		return io38;
	}

	public void setIo38(Double io38) {
		this.io38 = io38;
	}

	public Double getSo2_24h() {
		return so2_24h;
	}

	public void setSo2_24h(Double so2_24h) {
		this.so2_24h = so2_24h;
	}

	public Double getNo2_24h() {
		return no2_24h;
	}

	public void setNo2_24h(Double no2_24h) {
		this.no2_24h = no2_24h;
	}

	public Double getCo_24h() {
		return co_24h;
	}

	public void setCo_24h(Double co_24h) {
		this.co_24h = co_24h;
	}

	public Double getO3_24h() {
		return o3_24h;
	}

	public void setO3_24h(Double o3_24h) {
		this.o3_24h = o3_24h;
	}

	public Double getO38_24h() {
		return o38_24h;
	}

	public void setO38_24h(Double o38_24h) {
		this.o38_24h = o38_24h;
	}

	public Double getIso2_24h() {
		return iso2_24h;
	}

	public void setIso2_24h(Double iso2_24h) {
		this.iso2_24h = iso2_24h;
	}

	public Double getIno2_24h() {
		return ino2_24h;
	}

	public void setIno2_24h(Double ino2_24h) {
		this.ino2_24h = ino2_24h;
	}

	public Double getIpm10_24h() {
		return ipm10_24h;
	}

	public void setIpm10_24h(Double ipm10_24h) {
		this.ipm10_24h = ipm10_24h;
	}

	public Double getIpm25_24h() {
		return ipm25_24h;
	}

	public void setIpm25_24h(Double ipm25_24h) {
		this.ipm25_24h = ipm25_24h;
	}

	public Double getIco_24h() {
		return ico_24h;
	}

	public void setIco_24h(Double ico_24h) {
		this.ico_24h = ico_24h;
	}

	public Double getIo3_24h() {
		return io3_24h;
	}

	public void setIo3_24h(Double io3_24h) {
		this.io3_24h = io3_24h;
	}

	public Double getIo38_24h() {
		return io38_24h;
	}

	public void setIo38_24h(Double io38_24h) {
		this.io38_24h = io38_24h;
	}

	public Double getWd() {
		return wd;
	}

	public void setWd(Double wd) {
		this.wd = wd;
	}

	public Double getSd() {
		return sd;
	}

	public void setSd(Double sd) {
		this.sd = sd;
	}

	public Double getFl() {
		return fl;
	}

	public void setFl(Double fl) {
		this.fl = fl;
	}

	public Double getJsl() {
		return jsl;
	}

	public void setJsl(Double jsl) {
		this.jsl = jsl;
	}

	public Double getQy() {
		return qy;
	}

	public void setQy(Double qy) {
		this.qy = qy;
	}

 
}

