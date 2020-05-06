package wy.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CalDJR {

	/*%% 温度平流计算函数，该函数一次计算一个时刻采集的风廓线数据，
	%% 得到该时刻高度层上的各点的温度平流
	%% 输入参数  Hz，风廓线的垂直高度（单位m），为1*N维数组，N即该廓线高度分层数
	%% 输入参数V，各高度点上的水平风速（m/s），，为1*N维数组
	%% 输入参数d，各高度点上的水平风速的方向（单位°），，为1*N维数组
	%% 输入参数N, 该廓线的高度点数
	%% 输入参数t0，站点地面温度（℃）
	%% 输入参数fail，当地纬度(°)
	%% 输出参数 Ht，计算得到的温度平流的各点高度（m），为1*NT数组
	%% 输出参数DT，计算得到的各高度点的温度平流值，为1*NT数组*/

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
        //水平风向
        BigDecimal[] V = {
                new BigDecimal("076.8"),
                new BigDecimal("092.0"),
                new BigDecimal("085.4"),
                new BigDecimal("095.1"),
                new BigDecimal("105.3"),
                new BigDecimal("111.6"),
                new BigDecimal("114.7"),
                new BigDecimal("116.6"),
                new BigDecimal("114.7"),
                new BigDecimal("109.9"),
                new BigDecimal("106.0"),
                new BigDecimal("103.7"),
                new BigDecimal("099.0"),
                new BigDecimal("094.1"),
                new BigDecimal("089.1"),
                new BigDecimal("091.7"),
                new BigDecimal("090.8"),
                new BigDecimal("092.8"),
                new BigDecimal("095.3"),
                new BigDecimal("095.5"),
                new BigDecimal("093.0"),
                new BigDecimal("089.1"),
                new BigDecimal("087.1"),
                new BigDecimal("086.0"),
                new BigDecimal("085.8"),
                new BigDecimal("084.7"),
                new BigDecimal("084.8"),
                new BigDecimal("086.7"),
                new BigDecimal("090.8"),
                new BigDecimal("090.9"),
                new BigDecimal("111.0"),
                new BigDecimal("114.9"),
                new BigDecimal("119.2")};
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
        int N = Hz.length;
        BigDecimal fai = new BigDecimal("36.12");
        BigDecimal t0 = new BigDecimal("23.0");

        Map<String,BigDecimal[]> map = calTempFlue(Hz,d,V,N,fai,t0);
        BigDecimal[] Dt = map.get("Dt");
        for(BigDecimal i:map.get("Dt")){
            System.out.println(i);
        }
        for(BigDecimal i:map.get("Ht")){
            System.out.println(i);
        }
    }
     /**
      * 
      * @param Hz  风廓线的垂直高度 为1*N维数组，N即该廓线高度分层数
      * @param V  各高度点上的水平风速（m/s） 为1*N维数组
      * @param d  各高度点上的水平风速的方向（单位°），，为1*N维数组
      * @param N  该廓线的高度点数
      * @param fai 当地纬度(°)
      * @param t0  站点地面温度（℃）
      * @return
      */
    public static Map<String,BigDecimal[]> calTempFlue(BigDecimal[] Hz ,BigDecimal[] V,BigDecimal[] d,int N,BigDecimal fai,BigDecimal t0){
        //温度平流分层数
        int NT = N-1;
        BigDecimal[] Ht = new BigDecimal[NT];
        BigDecimal[] Dt = new BigDecimal[NT];

        BigDecimal w = new BigDecimal("7.292e-5");
        BigDecimal Rd = new BigDecimal("287.05");
        //w=7.292e-5;
        //Rd=287.05;
        //f=2*w*sin(fai*pi/180);
        BigDecimal f;
        BigDecimal f2 = new BigDecimal("2");
        BigDecimal sinval = (fai.multiply(new BigDecimal(Math.PI)).divide(new BigDecimal(180),4,BigDecimal.ROUND_HALF_EVEN));
        f = w.multiply(f2).multiply(new BigDecimal(Math.sin(sinval.doubleValue())).setScale(4, BigDecimal.ROUND_HALF_EVEN));
        f = f.setScale(8, BigDecimal.ROUND_HALF_EVEN);
        //c=f/Rd;
        BigDecimal c = f.divide(Rd,11,BigDecimal.ROUND_HALF_EVEN);
        //a=0.1903;
        BigDecimal a = new BigDecimal("0.1903");
        //delta=0.0065;
        BigDecimal delta = new BigDecimal("0.0065");
        //T0=t0+273.15;
        BigDecimal TO = t0.add(new BigDecimal("273.15"));
        for(int nt=0;nt<NT;nt++){
            BigDecimal V1 = V[nt];
            BigDecimal V2 = V[nt+1];
            BigDecimal d1 = d[nt];
            BigDecimal d2 = d[nt+1];
            //x1=log(1-Hz(nt)*delta/T0);
            double aa = Math.log((new BigDecimal("1").subtract(Hz[nt].multiply(delta).divide(TO,16,BigDecimal.ROUND_HALF_EVEN))).doubleValue());
            BigDecimal x1 = new BigDecimal(aa).setScale(16, BigDecimal.ROUND_HALF_EVEN);
            //x2=log(1-Hz(nt+1)*delta/T0);
            double bb = Math.log((new BigDecimal("1").subtract(Hz[nt+1].multiply(delta).divide(TO,16,BigDecimal.ROUND_HALF_EVEN))).doubleValue());
            BigDecimal x2 = new BigDecimal(bb).setScale(16, BigDecimal.ROUND_HALF_EVEN);
            //DlnP=1/a*(x1-x2);
            BigDecimal a_1 = new BigDecimal("1").divide(a,16,BigDecimal.ROUND_HALF_EVEN).setScale(16, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal DlnP = a_1.multiply(x1.subtract(x2));
//            System.out.println(x1.subtract(x2).setScale(16, BigDecimal.ROUND_HALF_UP));
            DlnP = DlnP.setScale(16, BigDecimal.ROUND_HALF_EVEN);
            //DT(nt)=c/DlnP*V1*V2*sin(d2-d1);
            Dt[nt] = c.divide(DlnP,16,BigDecimal.ROUND_HALF_EVEN).multiply(V1).multiply(V2).multiply(new BigDecimal(Math.sin(d2.subtract(d1).doubleValue())).setScale(16, BigDecimal.ROUND_HALF_EVEN));
            //Dt[nt] = Dt[nt].setScale(7, BigDecimal.ROUND_HALF_EVEN);(原版)
            //以下由于需求乘了个1000试试
            Dt[nt] = Dt[nt].setScale(7, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(1000));
            //Ht(nt)=0.5*(Hz(nt)+Hz(nt+1));
            Ht[nt] = (new BigDecimal("0.5").multiply(Hz[nt].add(Hz[nt+1])));
            Ht[nt] = Ht[nt].setScale(0, BigDecimal.ROUND_HALF_EVEN);
        }
        Map<String,BigDecimal[]> map = new HashMap<String,BigDecimal[]>();
        map.put("Dt",Dt);
        map.put("Ht",Ht);
        return map;
    }



}
