//code by ljq --20180321
package wy.util;



import javax.persistence.Entity;

@Entity
public class WaterBean {

	int objid;//对象objid
	String dmname;// 断面名称 
	String collecttime;// 监测时间
	
	Double gaomengsuanyan;// 高锰酸盐mg/l
	Double andan;// 氨氮
	Double zonglin;// 总磷
	Double ph;// ph值
	Double hxxyl;// 化学需氧量
	
	String zhsz;// 综合水质
	
	String cbyz;//超标因子
	
	 
	Double wrshxyl;//五日生化需氧量(BOD)
	Double rjy;//溶解氧
	Double zongdan;//总氮
	Double tong;//铜
	Double xin;//锌
	Double fhw;//氟化物
	Double xi;//硒
	Double shen; //砷
	Double gong;//汞
	Double ge;//镉
	Double geliu;//铬(六价)
	Double qian;//铅 
	Double qhw;//氰化物
	Double hhf;//挥发酚
	Double syl;//石油类
	Double ylzbmhxj;//阴离子表面活性剂
	Double lhw;//硫化物
	Double fdcjq;//粪大肠菌群
	 
	Double yls;//叶绿素
	Double swdx;//生物毒性
	Double lz;//绿藻
	Double llz;//蓝绿藻
	
	public WaterBean(){
		
	}
	
	public WaterBean(int objid, String dmname, String collecttime,
			Double gaomengsuanyan, Double andan, Double zonglin, Double ph,
			Double hxxyl, String zhsz, String cbyz, Double wrshxyl, Double rjy,
			Double zongdan, Double tong, Double xin, Double fhw, Double xi,
			Double shen, Double gong, Double ge, Double geliu, Double qian,
			Double qhw, Double hhf, Double syl, Double ylzbmhxj, Double lhw,
			Double fdcjq, Double yls, Double swdx, Double lz, Double llz) {
		super();
		this.objid = objid;
		this.dmname = dmname;
		this.collecttime = collecttime;
		this.gaomengsuanyan = gaomengsuanyan;
		this.andan = andan;
		this.zonglin = zonglin;
		this.ph = ph;
		this.hxxyl = hxxyl;
		this.zhsz = zhsz;
		this.cbyz = cbyz;
		this.wrshxyl = wrshxyl;
		this.rjy = rjy;
		this.zongdan = zongdan;
		this.tong = tong;
		this.xin = xin;
		this.fhw = fhw;
		this.xi = xi;
		this.shen = shen;
		this.gong = gong;
		this.ge = ge;
		this.geliu = geliu;
		this.qian = qian;
		this.qhw = qhw;
		this.hhf = hhf;
		this.syl = syl;
		this.ylzbmhxj = ylzbmhxj;
		this.lhw = lhw;
		this.fdcjq = fdcjq;
		this.yls = yls;
		this.swdx = swdx;
		this.lz = lz;
		this.llz = llz;
	}




	public int getObjid() {
		return objid;
	}

	public void setObjid(int objid) {
		this.objid = objid;
	}

	 

	public String getCbyz() {
		return cbyz;
	}

	public void setCbyz(String cbyz) {
		this.cbyz = cbyz;
	}

	public String getDmname() {
		return dmname;
	}

	public void setDmname(String dmname) {
		this.dmname = dmname;
	}

	public String getCollecttime() {
		return collecttime;
	}

	public void setCollecttime(String collecttime) {
		this.collecttime = collecttime;
	}

	

	public Double getGaomengsuanyan() {
		return gaomengsuanyan;
	}

	public void setGaomengsuanyan(Double gaomengsuanyan) {
		this.gaomengsuanyan = gaomengsuanyan;
	}

	public Double getAndan() {
		return andan;
	}

	public void setAndan(Double andan) {
		this.andan = andan;
	}

	public Double getZonglin() {
		return zonglin;
	}

	public void setZonglin(Double zonglin) {
		this.zonglin = zonglin;
	}

	public Double getPh() {
		return ph;
	}

	public void setPh(Double ph) {
		this.ph = ph;
	}

	public Double getHxxyl() {
		return hxxyl;
	}

	public void setHxxyl(Double hxxyl) {
		this.hxxyl = hxxyl;
	}

	public String getZhsz() {
		return zhsz;
	}

	public void setZhsz(String zhsz) {
		this.zhsz = zhsz;
	}

	public Double getWrshxyl() {
		return wrshxyl;
	}

	public void setWrshxyl(Double wrshxyl) {
		this.wrshxyl = wrshxyl;
	}

	public Double getRjy() {
		return rjy;
	}

	public void setRjy(Double rjy) {
		this.rjy = rjy;
	}

	public Double getZongdan() {
		return zongdan;
	}

	public void setZongdan(Double zongdan) {
		this.zongdan = zongdan;
	}

	public Double getTong() {
		return tong;
	}

	public void setTong(Double tong) {
		this.tong = tong;
	}

	public Double getXin() {
		return xin;
	}

	public void setXin(Double xin) {
		this.xin = xin;
	}

	public Double getFhw() {
		return fhw;
	}

	public void setFhw(Double fhw) {
		this.fhw = fhw;
	}

	public Double getXi() {
		return xi;
	}

	public void setXi(Double xi) {
		this.xi = xi;
	}

	public Double getShen() {
		return shen;
	}

	public void setShen(Double shen) {
		this.shen = shen;
	}

	public Double getGong() {
		return gong;
	}

	public void setGong(Double gong) {
		this.gong = gong;
	}

	public Double getGe() {
		return ge;
	}

	public void setGe(Double ge) {
		this.ge = ge;
	}

	public Double getGeliu() {
		return geliu;
	}

	public void setGeliu(Double geliu) {
		this.geliu = geliu;
	}

	public Double getQian() {
		return qian;
	}

	public void setQian(Double qian) {
		this.qian = qian;
	}

	public Double getQhw() {
		return qhw;
	}

	public void setQhw(Double qhw) {
		this.qhw = qhw;
	}

	public Double getHhf() {
		return hhf;
	}

	public void setHhf(Double hhf) {
		this.hhf = hhf;
	}

	public Double getSyl() {
		return syl;
	}

	public void setSyl(Double syl) {
		this.syl = syl;
	}

	public Double getYlzbmhxj() {
		return ylzbmhxj;
	}

	public void setYlzbmhxj(Double ylzbmhxj) {
		this.ylzbmhxj = ylzbmhxj;
	}

	public Double getLhw() {
		return lhw;
	}

	public void setLhw(Double lhw) {
		this.lhw = lhw;
	}

	public Double getFdcjq() {
		return fdcjq;
	}

	public void setFdcjq(Double fdcjq) {
		this.fdcjq = fdcjq;
	}

	public Double getYls() {
		return yls;
	}

	public void setYls(Double yls) {
		this.yls = yls;
	}

	public Double getSwdx() {
		return swdx;
	}

	public void setSwdx(Double swdx) {
		this.swdx = swdx;
	}

	public Double getLz() {
		return lz;
	}

	public void setLz(Double lz) {
		this.lz = lz;
	}

	public Double getLlz() {
		return llz;
	}

	public void setLlz(Double llz) {
		this.llz = llz;
	}
	
	

}

