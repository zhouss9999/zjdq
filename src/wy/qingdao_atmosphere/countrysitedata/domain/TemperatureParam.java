package wy.qingdao_atmosphere.countrysitedata.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public class TemperatureParam {
	
	/*%% 输入参数 Hz，廓线高度点，1*N数组
	%% 输入参数 Vh，廓线高度点上的水平风速（m/s），风廓线提供，1*N数组
	%% 输入参数 Vz，廓线高度点上的垂直风速（m/s），方向向上为正，向下为负，风廓线提供，1*N数组
	%% 输入参数 t，廓线高度点上的温度（℃）,辐射计提供，1*N数组
	%% 输入参数 RH，廓线高度点上的相对湿度，辐射计提供，1*N数组
	%% 输入参数 N，廓线高度点个数
	%% 输入参数 P0，地面压强hPa
	%% 输入参数 t0，地面温度（℃）*/
	 private String collecttime;
	 private BigDecimal fai ; //当地纬度
     private BigDecimal t0 ;  //地面温度
     private BigDecimal[] Hz; //各层高度
     private BigDecimal[] V; //水平风向
     private BigDecimal[] D; //水平风速
     private BigDecimal[] Vz; //垂直风速
     private BigDecimal[] RH; //相对湿度廓线
     private BigDecimal[] t; //温度廓线
     private BigDecimal P0 ; //地面压强hPa
     private int highsize ; //高度层数
     
     
     public int getHighsize() {
		return highsize;
	}
	public void setHighsize(int highsize) {
		this.highsize = highsize;
	}
	public BigDecimal[] getVz() {
		return Vz;
	}
	public void setVz(BigDecimal[] vz) {
		Vz = vz;
	}
	public BigDecimal[] getRH() {
		return RH;
	}
	public void setRH(BigDecimal[] rH) {
		RH = rH;
	}
	public BigDecimal[] getT() {
		return t;
	}
	public void setT(BigDecimal[] t) {
		this.t = t;
	}
	public BigDecimal getP0() {
		return P0;
	}
	public void setP0(BigDecimal p0) {
		P0 = p0;
	}
	public String getCollecttime() {
 		return collecttime;
 	}
 	public void setCollecttime(String collecttime) {
 		this.collecttime = collecttime;
 	}
	public BigDecimal getFai() {
		return fai;
	}
	public void setFai(BigDecimal fai) {
		this.fai = fai;
	}
	public BigDecimal getT0() {
		return t0;
	}
	public void setT0(BigDecimal t0) {
		this.t0 = t0;
	}
	public BigDecimal[] getHz() {
		return Hz;
	}
	public void setHz(BigDecimal[] hz) {
		Hz = hz;
	}
	public BigDecimal[] getV() {
		return V;
	}
	public void setV(BigDecimal[] v) {
		V = v;
	}
	public BigDecimal[] getD() {
		return D;
	}
	public void setD(BigDecimal[] d) {
		D = d;
	}
	@Override
	public String toString() {
		return "TemperatureParam [collecttime=" + collecttime + ", fai=" + fai
				+ ", t0=" + t0 + ", Hz=" + Arrays.toString(Hz) + ", V="
				+ Arrays.toString(V) + ", D=" + Arrays.toString(D) + ", Vz="
				+ Arrays.toString(Vz) + ", RH=" + Arrays.toString(RH) + ", t="
				+ Arrays.toString(t) + ", P0=" + P0 + "]";
	}
	
}
