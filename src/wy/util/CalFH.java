package wy.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CalFH {
    
	/*%% 计算水汽通量 
	%% 输入参数 Hz，廓线高度点，1*N数组
	%% 输入参数 Vh，廓线高度点上的水平风速（m/s），风廓线提供，1*N数组
	%% 输入参数 Vz，廓线高度点上的垂直风速（m/s），方向向上为正，向下为负，风廓线提供，1*N数组
	%% 输入参数 t，廓线高度点上的温度（℃）,辐射计提供，1*N数组
	%% 输入参数 RH，廓线高度点上的相对湿度，辐射计提供，1*N数组
	%% 输入参数 N，廓线高度点个数
	%% 输入参数 P0，地面压强hPa
	%% 输入参数 t0，地面温度（℃）

	%% 输出参数 Pb，廓线高度上压强点，为1*N数组
	%% 输出参数 FH，廓线高度点上的水平水汽通量，为1*N数组( g.cm-1.hPa-1.s-1)
	%% 输出参数 FZ，廓线高度点上的垂直水汽通量，为1*N数组( g.cm-2.s-1)
	%% 水汽通量绘制时，纵坐标为压强Pb（HPa）*/
	
    public static void main(String[] args) {
//高度
        BigDecimal[] Hz = {
                new BigDecimal("00120"),
                new BigDecimal("00240"),
                new BigDecimal("00360"),
                new BigDecimal("00480"),
                new BigDecimal("00600"),
                new BigDecimal("00720"),
                new BigDecimal("00840"),
                new BigDecimal("00960"),
                new BigDecimal("01080"),
                new BigDecimal("01200"),
                new BigDecimal("01320"),
                new BigDecimal("01440"),
                new BigDecimal("01560"),
                new BigDecimal("01680"),
                new BigDecimal("01800"),
                new BigDecimal("01920"),
                new BigDecimal("02160"),
                new BigDecimal("02400"),
                new BigDecimal("02640"),
                new BigDecimal("02880"),
                new BigDecimal("03120"),
                new BigDecimal("03360"),
                new BigDecimal("03600"),
                new BigDecimal("03840"),
                new BigDecimal("04080"),
                new BigDecimal("04320"),
                new BigDecimal("04560"),
                new BigDecimal("04800"),
                new BigDecimal("05040"),
                new BigDecimal("05280"),
                new BigDecimal("05520"),
                new BigDecimal("05760"),
                new BigDecimal("06000")};
        //水平风速
        BigDecimal[] d = {
                new BigDecimal("001.5"),
                new BigDecimal("001.9"),
                new BigDecimal("002.5"),
                new BigDecimal("003.0"),
                new BigDecimal("003.8"),
                new BigDecimal("004.9"),
                new BigDecimal("005.9"),
                new BigDecimal("006.5"),
                new BigDecimal("006.2"),
                new BigDecimal("005.8"),
                new BigDecimal("005.6"),
                new BigDecimal("005.7"),
                new BigDecimal("006.4"),
                new BigDecimal("006.7"),
                new BigDecimal("007.2"),
                new BigDecimal("006.1"),
                new BigDecimal("007.3"),
                new BigDecimal("007.7"),
                new BigDecimal("007.9"),
                new BigDecimal("008.1"),
                new BigDecimal("008.2"),
                new BigDecimal("008.3"),
                new BigDecimal("008.4"),
                new BigDecimal("008.7"),
                new BigDecimal("008.9"),
                new BigDecimal("009.3"),
                new BigDecimal("009.6"),
                new BigDecimal("009.2"),
                new BigDecimal("009.0"),
                new BigDecimal("009.9"),
                new BigDecimal("006.1"),
                new BigDecimal("006.5"),
                new BigDecimal("007.5")};
        //垂直风速
        BigDecimal[] Vz = {
                new BigDecimal("0012.5"),
                new BigDecimal("0000.1"),
                new BigDecimal("0000.1"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.4"),
                new BigDecimal("0000.5"),
                new BigDecimal("0000.5"),
                new BigDecimal("0000.4"),
                new BigDecimal("0000.4"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.0"),
                new BigDecimal("0000.1"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.0"),
                new BigDecimal("0000.0"),
                new BigDecimal("0000.0"),
                new BigDecimal("0000.0"),
                new BigDecimal("-000.0"),
                new BigDecimal("-000.1"),
                new BigDecimal("-000.1"),
                new BigDecimal("-000.0"),
                new BigDecimal("-000.0"),
                new BigDecimal("-000.0"),
                new BigDecimal("-000.0"),
                new BigDecimal("0000.0"),
                new BigDecimal("0000.0"),
                new BigDecimal("0000.1"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.2"),
                new BigDecimal("0000.1")};
        int N = Hz.length;
        //P0=1025; %地面压强 hPa
        BigDecimal P0 = new BigDecimal(1025);
        //t0=23.0; %地面温度 ℃
        BigDecimal t0 = new BigDecimal(23.0);
        //t=t0-6.5*Hz/1000; %温度廓线，假定
        BigDecimal[] t = new BigDecimal[Hz.length];
        for(int i=0;i<Hz.length;i++){
            t[i] = t0.subtract(new BigDecimal(6.5).multiply(Hz[i]).divide(new BigDecimal(1000)));
        }
        BigDecimal[] RH = new BigDecimal[Hz.length];
        for(int i=0;i<Hz.length;i++){
            RH[i] = new BigDecimal(65).subtract(new BigDecimal(0.75).multiply(Hz[i]).divide(new BigDecimal(100)));
        }
//        for(BigDecimal a:RH){
//            System.out.println(a);
//        }
        Map<String,BigDecimal[]> map = calFH(Hz,d,Vz,t,RH,N,P0,t0);
        for(BigDecimal i:map.get("Pb")){
            System.out.println(i);
        }
        for(BigDecimal i:map.get("FH")){
            System.out.println(i);
        }
        System.out.println("-------------------------------");
        for(BigDecimal i:map.get("FZ")){
            System.out.println(i);
        }
    }
    /**
     * 
     * @param Hz  廓线高度点，1*N数组
     * @param Vh  廓线高度点上的水平风速（m/s），风廓线提供，1*N数组
     * @param Vz  廓线高度点上的垂直风速（m/s），方向向上为正，向下为负，风廓线提供，1*N数组
     * @param t   廓线高度点上的温度（℃）,辐射计提供，1*N数组
     * @param RH  线高度点上的相对湿度，辐射计提供，1*N数组
     * @param N   廓线高度点个数	
     * @param P0  地面压强hPa
     * @param t0  地面温度（℃）
     * @return
     */
    public static Map<String,BigDecimal[]> calFH(BigDecimal[] Hz,BigDecimal[] Vh,BigDecimal[] Vz,BigDecimal[] t,BigDecimal[] RH,int N,BigDecimal P0,BigDecimal t0){
        BigDecimal[] Pb = new BigDecimal[N];
        BigDecimal[] FH = new BigDecimal[N];
        BigDecimal[] FZ = new BigDecimal[N];

        //a=0.1903;
        BigDecimal a = new BigDecimal(0.1903);
        //delta=0.0065;
        BigDecimal delta = new BigDecimal(0.0065);
        //T0=t0+273.15;
        BigDecimal TO = t0.add(new BigDecimal(273.15));
        //g=9.8;
        BigDecimal g = new BigDecimal(9.8);
        //rou=1.293;
        BigDecimal rou = new BigDecimal(1.293);

        for(int n=0;n<N;n++){
            int r = t[n].compareTo(new BigDecimal(0));
            BigDecimal es;
            if(r>=0){
                //es=6.112*exp((17.502.* t(n))/(240.97+ t(n)));
                es = new BigDecimal(6.112).multiply(new BigDecimal(Math.exp(new BigDecimal(17.502).multiply(t[n]).divide(new BigDecimal(240.97).add(t[n]),16,BigDecimal.ROUND_HALF_EVEN).doubleValue())));
            }else{
                //es=6.112*exp((22.452.* t(n))/(272.55+ t(n)));
                es = new BigDecimal(6.112).multiply(new BigDecimal(Math.exp(new BigDecimal(22.452).multiply(t[n]).divide(new BigDecimal(272.55).add(t[n]),16,BigDecimal.ROUND_HALF_EVEN).doubleValue())));
            }
//            System.out.println(es.setScale(4, BigDecimal.ROUND_HALF_EVEN));
            //e=es*RH(n)/100;
            BigDecimal e = es.multiply(RH[n]).divide(new BigDecimal(100),16,BigDecimal.ROUND_HALF_EVEN);
//            System.out.println(e.setScale(4, BigDecimal.ROUND_HALF_EVEN));
            //LnP=log(P0)+1/a*log(1-Hz(n)*delta/T0);
            BigDecimal LnP = new BigDecimal(Math.log(P0.doubleValue())).add(new BigDecimal(1).divide(a,16,BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(Math.log((new BigDecimal(1).subtract(Hz[n].multiply(delta).divide(TO,16,BigDecimal.ROUND_HALF_EVEN))).doubleValue()))));
            //1-Hz(n)*delta/T0
//            System.out.println(LnP.setScale(4, BigDecimal.ROUND_HALF_EVEN));
            //Pb(n)=exp(LnP);
            Pb[n] = new BigDecimal(Math.exp(LnP.doubleValue())).setScale(16, BigDecimal.ROUND_HALF_EVEN);
            //q=0.622*e/(Pb(n)-e);
            BigDecimal q = new BigDecimal(0.622).multiply(e).divide(Pb[n].subtract(e),16,BigDecimal.ROUND_HALF_EVEN);
            //q=q*1000;%转换单位
            q = q.multiply(new BigDecimal(1000));
//            System.out.println(q.setScale(4, BigDecimal.ROUND_HALF_EVEN));
            // FH(n)=Vh(n)*q/g; %水平水汽通量
            FH[n] = Vh[n].multiply(q).divide(g,16,BigDecimal.ROUND_HALF_EVEN).setScale(4, BigDecimal.ROUND_HALF_EVEN);
            // FZ(n)=rou*Vz(n)*q*1e-4; %垂直水汽通量
            FZ[n] = rou.multiply(Vz[n]).multiply(q).multiply(new BigDecimal(1e-4)).setScale(9, BigDecimal.ROUND_HALF_EVEN);
        }
        Map<String,BigDecimal[]> map = new HashMap<String,BigDecimal[]>();
        map.put("Pb",Pb);
        map.put("FH",FH);
        map.put("FZ",FZ);
        return map;
    }

}
