//code by ljq --20180321
package wy.util;


 

import java.text.DecimalFormat;
import java.util.List;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;





public class Calc {
	
	private static   double [][]WQJP;//水质判断二位数组
	private static  String [] cbyzlist;//超标因子列表对应
	
	
	
	//空气
	private static  double [][] AQJPH;//空气质量判断二位数组 
	
	private static  double []  IAQI;//空气质量分指数判断列表
	private static  String []  Plist;//污染物的名称列表
	
	    //计算空气质量 
	    //首要污染物是IAQI一样的,且污染等级大于1级
	    //计算方式四舍五入非奇进偶舍
	    //不按分指数计算
		public static AirBean AirQ(AirBean airparam)
		{
			 //初始化污染物的名称列表
			 if(Plist==null)
			  {
				  Plist=new String[14];
				  Plist[0]="二氧化硫";//"so2";
				  Plist[1]="二氧化氮";//"no2";
				  Plist[2]="颗粒物(PM10)";//"pm10";
				  Plist[3]="一氧化碳";//"co";
				  Plist[4]="臭氧1小时";//"o3";
				  Plist[5]="臭氧8小时";//"o38";
				  Plist[6]="细颗粒物(PM2.5)";//"pm25"; 
				  
				  Plist[7]="二氧化硫24h";//"so2";
				  Plist[8]="二氧化氮24h";//"no2";
				  Plist[9]="颗粒物(PM10)24h";//"pm10";
				  Plist[10]="一氧化碳24h";//"co";
				  Plist[11]="臭氧1小时24h";//"o3";
				  Plist[12]="臭氧8小时24h";//"o38";
				  Plist[13]="细颗粒物(PM2.5)24h";//"pm25";  
			  }
			  
			  //初始化空气质量分指数判断列表
			  if(IAQI==null)
			  {
				  IAQI=new double[8];
				  IAQI[0]=0;
				  IAQI[1]=50;
				  IAQI[2]=100;
				  IAQI[3]=150;
				  IAQI[4]=200;
				  IAQI[5]=300;
				  IAQI[6]=400;
				  IAQI[7]=500;
			  }
			  
			  //初始化二位数组double
			  if(AQJPH==null)//小时
			  {
				  AQJPH=new double[14][8];
			      AQJPH[0][0]= 0  ; AQJPH[0][1]= 150 ; AQJPH[0][2]= 500  ; AQJPH[0][3]= 650  ; AQJPH[0][4]= 800  ;AQJPH[0][5]= 1600  ; AQJPH[0][6]= 2100  ; AQJPH[0][7]= 2620  ;
				  AQJPH[1][0]= 0  ;  AQJPH[1][1]=100 ; AQJPH[1][2]= 200  ; AQJPH[1][3]= 700  ; AQJPH[1][4]= 1200  ;AQJPH[1][5]=2340  ; AQJPH[1][6]= 3090  ; AQJPH[1][7]= 3840  ;
				  AQJPH[2][0]= 0 ;  AQJPH[2][1]=50 ; AQJPH[2][2]= 150  ; AQJPH[2][3]= 250  ; AQJPH[2][4]= 350  ;AQJPH[2][5]=420  ; AQJPH[2][6]= 500  ; AQJPH[2][7]= 600  ;
				  AQJPH[3][0]= 0 ; AQJPH[3][1]= 5  ; AQJPH[3][2]= 10  ; AQJPH[3][3]= 35  ; AQJPH[3][4]= 60  ;AQJPH[3][5]= 90  ; AQJPH[3][6]= 120  ; AQJPH[3][7]= 150  ;
				  AQJPH[4][0]= 0  ;AQJPH[4][1]= 160  ; AQJPH[4][2]= 200  ; AQJPH[4][3]= 300  ; AQJPH[4][4]= 400  ;AQJPH[4][5]= 800  ; AQJPH[4][6]= 1000  ; AQJPH[4][7]= 1200  ;
			  	  AQJPH[5][0]= 0  ; AQJPH[5][1]= 100  ; AQJPH[5][2]= 160  ; AQJPH[5][3]= 215  ; AQJPH[5][4]= 265  ;AQJPH[5][5]= 800  ; AQJPH[5][6]= 1000  ; AQJPH[5][7]= 1200  ;
				  AQJPH[6][0]= 0  ; AQJPH[6][1]= 35  ; AQJPH[6][2]= 75  ; AQJPH[6][3]= 115  ; AQJPH[6][4]= 150  ;AQJPH[6][5]= 250  ; AQJPH[6][6]= 350  ; AQJPH[6][7]= 500  ;
				  AQJPH[7][0]= 0  ; AQJPH[7][1]= 50 ; AQJPH[7][2]= 150  ; AQJPH[7][3]= 475  ; AQJPH[7][4]= 800  ;AQJPH[7][5]= 1600  ; AQJPH[7][6]= 2100  ; AQJPH[7][7]= 2620  ;
				  AQJPH[8][0]= 0  ;  AQJPH[8][1]= 40 ; AQJPH[8][2]= 80  ; AQJPH[8][3]= 180  ; AQJPH[8][4]= 280  ;AQJPH[8][5]=565  ; AQJPH[8][6]= 750  ; AQJPH[8][7]= 940  ;
				  AQJPH[9][0]= 0 ;  AQJPH[9][1]=50 ; AQJPH[9][2]= 150  ; AQJPH[9][3]= 250  ; AQJPH[9][4]= 350  ;AQJPH[9][5]=420  ; AQJPH[9][6]= 500  ; AQJPH[9][7]= 600  ;
				  AQJPH[10][0]= 0 ; AQJPH[10][1]=2;  AQJPH[10][2]= 4  ; AQJPH[10][3]= 14  ; AQJPH[10][4]= 24  ;AQJPH[10][5]=36  ; AQJPH[10][6]= 48  ; AQJPH[10][7]= 60  ;
				  AQJPH[11][0]= 0  ;AQJPH[11][1]= 160  ; AQJPH[11][2]= 200  ; AQJPH[11][3]= 300  ; AQJPH[11][4]= 400  ;AQJPH[11][5]= 800  ; AQJPH[11][6]= 1000  ; AQJPH[11][7]= 1200  ;
				  AQJPH[12][0]= 0  ; AQJPH[12][1]= 100  ; AQJPH[12][2]= 160  ; AQJPH[12][3]= 215  ; AQJPH[12][4]= 265  ;AQJPH[12][5]= 800  ; AQJPH[12][6]= 1000  ; AQJPH[12][7]= 1200  ;
				  AQJPH[13][0]= 0  ; AQJPH[13][1]= 35  ; AQJPH[13][2]= 75  ; AQJPH[13][3]= 115  ; AQJPH[13][4]= 150  ;AQJPH[13][5]= 250  ; AQJPH[13][6]= 350  ; AQJPH[13][7]= 500  ;
				  		
			  } 
			  
			  //计算中间变量
			  //当前计算模式是小时,还是日.
			  //int type=airparam.getType();//0是小时。1是日 
			  double []AQ;//当前参数
			  AQ=new double[14];
			  double []IAQ;//计算的IAQI
			  IAQ=new double[14];
			  
			  
			  
			  AQ[0]=airparam.getSo2();
			  AQ[1]=airparam.getNo2();
			  AQ[2]=airparam.getPm10();
			  AQ[3]=airparam.getCo(); 
			  AQ[4]=airparam.getO3();  
			  AQ[5]=airparam.getO38(); 
			  AQ[6]=airparam.getPm25(); 
			
			  
			  AQ[7]=airparam.getSo2_24h();
			  AQ[8]=airparam.getNo2_24h();
			  AQ[9]=airparam.getPm10_24h();
			  AQ[10]=airparam.getCo_24h(); 
			  AQ[11]=airparam.getO3_24h();   
			  AQ[12]=airparam.getO38_24h(); 
			  AQ[13]=airparam.getPm25_24h(); 
			
			  
		      //输出的内容
			  int AQlevel=1;//污染等级
			  String pp="";//首要污染物
			  int maxaqi=0;//aqi
			   
			  //计算aqi并取整.
		      for(int i=0;i<14;i++)
			  {
		    	  if (AQ[i]<=0)//co是小数
		    	  {
		    		  IAQ[i]=0; 		    		  
		    		  continue;
		    	  }
		    	  
		    	  //判断单个参数所属的区域
		    	  int flag=7;//最大是7 
		    	  
		    	
		    	 for(int ii=0;ii<8;ii++)
			       {
		    			  if (  AQ[i]>AQJPH[i][ii])
			    	        {
			    	        	continue;
			    	        } 
			    	        flag=ii;
			    	        break;
			       } 
			    	  
			      IAQ[i]=(IAQI[flag]-IAQI[flag-1])*(AQ[i]-AQJPH[i][flag-1])/(AQJPH[i][flag]-AQJPH[i][flag-1]) +IAQI[flag-1] ;//可以超过500
			    	 
		    	   
		        //  IAQ[i]=Math.round(IAQ[i]);//国站网站没有证明到奇进偶舍而是四舍五入
			      
			      
			      if(IAQ[i]<10)
			      {
			    	  IAQ[i]=significanceDigit(IAQ[i], 1); //保留1位
			      }else if (IAQ[i]<100)
			      {
			    	  IAQ[i]=significanceDigit(IAQ[i], 2); //保留2位
			      }else if (IAQ[i]<1000)
			      {
			    	  IAQ[i]=significanceDigit(IAQ[i], 3); //保留3位 
			      }else  
			      {
			    	  IAQ[i]=Math.round(IAQ[i]); //太大了，不考虑小数点了
			      }
			     
			      
		          if (maxaqi<IAQ[i])
		    	  {
		    		  maxaqi=(int)IAQ[i];
		    	  }
			 } 
		      
		    //计算污染等级1,2,3,4,5,6
		      if(maxaqi<51)
		      {
		    	  AQlevel=1;
		      }else  if (maxaqi<101)
		      {
		    	  AQlevel=2;
		      }else if (maxaqi<151)
		      {
		    	  AQlevel=3;
		      }else if (maxaqi<201)
		      {
		    	  AQlevel=4;
		      }else if (maxaqi<301)
		      {
		    	  AQlevel=5;
		      }else 
		      {
		    	  AQlevel=6;
		      }
		      
		      //对分指数设值
		      airparam.setIso2(IAQ[0]);
		      airparam.setIno2(IAQ[1]);
		      airparam.setIpm10(IAQ[2]);
		      airparam.setIco(IAQ[3]);  
			  airparam.setIo3(IAQ[4]); 
			  airparam.setIo38(IAQ[5]); 
			  airparam.setIpm25(IAQ[6]);  
			  airparam.setIso2_24h(IAQ[7]);
		      airparam.setIno2_24h(IAQ[8]);
		      airparam.setIpm10_24h(IAQ[9]);
		      airparam.setIco_24h(IAQ[10]);  
			  airparam.setIo3_24h(IAQ[11]); 
			  airparam.setIo38_24h(IAQ[12]); 
			  airparam.setIpm25_24h(IAQ[13]);  
			  
			  
		      //计算首要污染物,考虑并列
		      for(int i=0;i<14;i++)
		      {
		    	  if(AQlevel<2)//污染等级小于2就是没有首要污染物
		    	  {
		    		  break;
		    	  }
		    	  //计算首要污染物
		    	  if (maxaqi==IAQ[i])
		    	  {
		    		  String substr=""; 
		    		  pp=pp+ Plist[i]+substr+" "; 
		    	  } 
		      }
		       
		      airparam.setAqi(maxaqi);
		      airparam.setAqilevel(AQlevel);
		      airparam.setPp(pp.trim());//首要污染物体 
		      return airparam;
		}
	
	 
	
	
	//计算综合指数
	public static double MixIndex(WaterBean waterparam)
	{
	  double gmsyindex=	waterparam.getGaomengsuanyan()/6;
	  double adindex=	waterparam.getAndan();
	  double zlindex=	waterparam.getZonglin()/0.2;
	 
	  double paveindex=(gmsyindex+adindex+zlindex)/3;
	  double pmaxindex=Math.max(Math.max(adindex, zlindex), gmsyindex);
	  
	  double pmixindex=Math.sqrt( ((paveindex*paveindex)+(pmaxindex*pmaxindex))/2);
	   
	  return pmixindex;
	}
	//计算水质返回水质类别[I类，II类，III类，IV类，V类，劣V类]
	 
	
	//浦江要求--断面名称必须提供
	//1、常规地表水：金坑岭、通济桥、仙华水库这三个断面粪大肠菌群参与水质评价，
	//2、其他断面的评价指标没有粪大肠菌群
	//3、所有断面的水质评价指标没有总氮；
	public static String WaterQ(WaterBean waterparam)
	{
		 
		  int []filter=new int[5];//最多去掉5个
		  filter[0]=-1;
		  filter[1]=-1;
		  filter[2]=-1;
		  filter[3]=-1;
		  filter[4]=-1;
		  //所有断面的水质评价指标没有总氮
		  filter[0]=6;
		  //没有粪大肠菌群
		  if(!(waterparam.getDmname()=="金坑岭" || waterparam.getDmname()=="通济桥"||waterparam.getDmname()=="仙华水库"))
		  {
			  filter[1]=21;
		  }
		  
		  //初始化二位数组double
		 // double [][]WQJP;
		  if(WQJP==null)
		  {
			  WQJP=new double[22][5];
			  WQJP[0][0]= 7.5  ; WQJP[0][1]= 6  ; WQJP[0][2]= 5  ; WQJP[0][3]= 3  ; WQJP[0][4]= 2  ;
			  WQJP[1][0]= 2  ; WQJP[1][1]= 4  ; WQJP[1][2]= 6  ; WQJP[1][3]= 10  ; WQJP[1][4]= 15  ;
			  WQJP[2][0]= 15  ; WQJP[2][1]=15  ; WQJP[2][2]= 20  ; WQJP[2][3]= 30  ; WQJP[2][4]= 40  ;
			  WQJP[3][0]= 3  ; WQJP[3][1]=3  ; WQJP[3][2]= 4 ; WQJP[3][3]= 6  ; WQJP[3][4]= 10  ;
			  WQJP[4][0]= 0.15  ; WQJP[4][1]= 0.5  ; WQJP[4][2]= 1.0  ; WQJP[4][3]= 1.5  ; WQJP[4][4]= 2.0  ;
			  WQJP[5][0]= 0.02  ; WQJP[5][1]= 0.1  ; WQJP[5][2]= 0.2  ; WQJP[5][3]= 0.3  ; WQJP[5][4]= 0.4  ;
			  WQJP[6][0]= 0.2  ; WQJP[6][1]= 0.5  ; WQJP[6][2]= 1.0  ; WQJP[6][3]= 1.5  ; WQJP[6][4]= 2.0  ;
			  WQJP[7][0]= 0.01  ; WQJP[7][1]= 1.0  ; WQJP[7][2]= 1.0  ; WQJP[7][3]= 1.0  ; WQJP[7][4]= 1.0  ;
			  WQJP[8][0]= 0.05  ; WQJP[8][1]= 1.0  ; WQJP[8][2]= 1.0  ; WQJP[8][3]= 2.0  ; WQJP[8][4]= 2.0  ;
			  WQJP[9][0]= 1.0  ; WQJP[9][1]= 1.0  ; WQJP[9][2]= 1.0  ; WQJP[9][3]= 1.5  ; WQJP[9][4]= 1.5  ;
			  
			  WQJP[10][0]= 0.01  ; WQJP[10][1]=0.01  ; WQJP[10][2]= 0.01  ; WQJP[10][3]= 0.02  ; WQJP[10][4]=  0.02  ;
			  WQJP[11][0]= 0.05  ; WQJP[11][1]= 0.05  ; WQJP[11][2]=0.05  ; WQJP[11][3]= 0.1  ; WQJP[11][4]= 0.1  ;
			  WQJP[12][0]= 0.00005  ; WQJP[12][1]=0.00005  ; WQJP[12][2]= 0.0001  ; WQJP[12][3]= 0.001  ; WQJP[12][4]= 0.001  ;
			  WQJP[13][0]= 0.001  ; WQJP[13][1]=0.005  ; WQJP[13][2]= 0.005 ; WQJP[13][3]= 0.005  ; WQJP[13][4]= 0.01  ;
			  WQJP[14][0]= 0.01  ; WQJP[14][1]= 0.05  ; WQJP[14][2]= 0.05  ; WQJP[14][3]= 0.05  ; WQJP[14][4]= 0.1  ;
			  WQJP[15][0]= 0.01  ; WQJP[15][1]= 0.01  ; WQJP[15][2]= 0.05  ; WQJP[15][3]= 0.05  ; WQJP[15][4]= 0.1  ;
			  WQJP[16][0]= 0.005  ; WQJP[16][1]= 0.05  ; WQJP[16][2]= 0.2  ; WQJP[16][3]= 0.2  ; WQJP[16][4]= 0.2  ;
			  WQJP[17][0]= 0.002  ; WQJP[17][1]= 0.002  ; WQJP[17][2]=0.005  ; WQJP[17][3]= 0.01  ; WQJP[17][4]= 0.1  ;
			  WQJP[18][0]= 0.05  ; WQJP[18][1]=0.05  ; WQJP[18][2]= 0.05  ; WQJP[18][3]= 0.5  ; WQJP[18][4]= 1.0  ;
			  WQJP[19][0]= 0.2  ; WQJP[19][1]= 0.2  ; WQJP[19][2]= 0.2  ; WQJP[19][3]= 0.3  ; WQJP[19][4]= 0.3  ;
			  
			  WQJP[20][0]= 0.05  ; WQJP[20][1]=0.1  ; WQJP[20][2]= 0.2  ; WQJP[20][3]= 0.5  ; WQJP[20][4]=  1.0  ;
			  WQJP[21][0]= 200  ; WQJP[21][1]= 2000  ; WQJP[21][2]=10000  ; WQJP[21][3]= 20000  ; WQJP[21][4]= 40000  ;
			
		  }
		 
		  
		  //参数对比
		  
		  double []WQ;
		  WQ=new double[22];
		  WQ[0]=waterparam.getRjy();
		  WQ[1]=waterparam.getGaomengsuanyan();
		  WQ[2]=waterparam.getHxxyl();
		  WQ[3]=waterparam.getWrshxyl();
		  WQ[4]=waterparam.getAndan();
		  WQ[5]=waterparam.getZonglin();
		  WQ[6]=waterparam.getZongdan();
		  WQ[7]=waterparam.getTong();
		  WQ[8]=waterparam.getXin();
		  WQ[9]=waterparam.getFhw(); 
		  WQ[10]=waterparam.getXi();
		  WQ[11]=waterparam.getShen();
		  WQ[12]=waterparam.getGong();
		  WQ[13]=waterparam.getGe();
		  WQ[14]=waterparam.getGeliu();
		  WQ[15]=waterparam.getQian();
		  WQ[16]=waterparam.getQhw();
		  WQ[17]=waterparam.getHhf();
		  WQ[18]=waterparam.getSyl();
		  WQ[19]=waterparam.getYlzbmhxj(); 
		  WQ[20]=waterparam.getLhw();
		  WQ[21]=waterparam.getFdcjq();
		  
		  // double [][]WQJP;
		  if(cbyzlist==null)
		  {
		  cbyzlist=new String[22];
		  cbyzlist[0]="Rjy";
		  cbyzlist[1]="Gaomengsuanyan";
		  cbyzlist[2]="Hxxyl";
		  cbyzlist[3]="Wrshxyl";
		  cbyzlist[4]="Andan";
		  cbyzlist[5]="Zonglin";
		  cbyzlist[6]="Zongdan";
		  cbyzlist[7]="Tong";
		  cbyzlist[8]="Xin";
		  cbyzlist[9]="Fhw"; 
		  cbyzlist[10]="Xi";
		  cbyzlist[11]="Shen";
		  cbyzlist[12]="Gong";
		  cbyzlist[13]="Ge";
		  cbyzlist[14]="Geliu";
		  cbyzlist[15]="Qian";
		  cbyzlist[16]="Qhw";
		  cbyzlist[17]="Hhf";
		  cbyzlist[18]="Syl";
		  cbyzlist[19]="Ylzbmhxj"; 
		  cbyzlist[20]="Lhw";
		  cbyzlist[21]="Fdcjq";
		  }
		  
		  
		  
		
	    	//计算max等级1,2,3,4,5
		  int WQlevel=1;
		  int cbyz=0;
	      for(int i=0;i<22;i++)
			{
	    	  //浦江要求去除2个
	    	  int flag=0;
	    	  
	    	  for(int j=0;j<5;j++)
	    	  {
	    		  if (i==filter[j])
	    		  {
	    			  flag=1;
	    		  } 
	    	  }
	    	  if (flag==1)
	    	  {
	    		  continue;
	    	  }
	    	  
	    	  
	    	  //开始判断
	    	   int templevel=1;
	    	  
				if (i==0)//
				{
					if(WQ[i] > 0){ //判断溶解氧有没有监测数据
					   if(WQ[i]>=WQJP[i][0])
					   {
						   templevel=1; 
					   }else if (WQ[i]>=WQJP[i][1])
					   {
						   templevel=2;
					   }else if (WQ[i]>=WQJP[i][2])
					   {
						   templevel=3;
					   }else if (WQ[i]>=WQJP[i][3])
					   {
						   templevel=4;
					   }else if (WQ[i]>=WQJP[i][4])
					   {
						   templevel=5;
					   }else
					   {
						   templevel=6;
					   } 
					}
					   
				}else
				{
					 if(WQ[i]<=WQJP[i][0])
					   {
						   templevel=1; 
					   }else if (WQ[i]<=WQJP[i][1])
					   {
						   templevel=2;
					   }else if (WQ[i]<=WQJP[i][2])
					   {
						   templevel=3;
					   }else if (WQ[i]<=WQJP[i][3])
					   {
						   templevel=4;
					   }else if (WQ[i]<=WQJP[i][4])
					   {
						   templevel=5;
					   }else
					   {
						   templevel=6;
					   } 
				} 
				
				//水质赋值
				if(WQlevel<templevel)
				{
					WQlevel=templevel;
					cbyz=i;
				}
			}
		  
	      String WQLevelstr="";
	      switch (WQlevel)
	      {
	          case 1:WQLevelstr="Ⅰ类";break;
	          case 2:WQLevelstr="Ⅱ类";break;
	          case 3:WQLevelstr="Ⅲ类";break;
	          case 4:WQLevelstr="Ⅳ类";break;
	          case 5:WQLevelstr="Ⅴ类";break; 
	          case 6:WQLevelstr="劣Ⅴ类";break; 
	      }
		
	    waterparam.setZhsz(WQLevelstr);
		waterparam.setCbyz(cbyzlist[cbyz]);
		return WQLevelstr;
	}
	
	/**
	 * 保留数字有效位数并且进行奇进偶舍运算
	 * @param num	//数值
	 * @param digit //有效位数
	 * @return
	 */
	public static double significanceDigit(double num,int digit){
		BigDecimal b = new BigDecimal(String.valueOf(num));  
		BigDecimal divisor = BigDecimal.ONE;  
		MathContext mc = new MathContext(digit,RoundingMode.HALF_EVEN);
		return b.divide(divisor, mc).doubleValue();
	}
	
	public static void main(String[] args) {
		DecimalFormat df = new DecimalFormat("#0");
		String str = df.format(2);
		System.out.println(Double.parseDouble(str));
		System.out.println(significanceDigit(10, 3));
	}
	
	public static List<WaterBean> waterDataFormat(List<WaterBean> dataList){
		//遍历list对象，对参数数据进行文件标准化
		if(dataList.size() > 0)
		for(WaterBean obj : dataList){
			double gmsyzs = obj.getGaomengsuanyan();
			double hxxyl = obj.getHxxyl();
			double wrshxyl = obj.getWrshxyl();
			double ad = obj.getAndan();
			double zl = obj.getZonglin();
			double zd = obj.getZongdan();
			double xin = obj.getXin();
			double tong = obj.getTong();
			double qian = obj.getQian();
			double ge = obj.getGe();
			double xi = obj.getXi();
			double shen = obj.getShen();
			double gong = obj.getGong();
			double fhw = obj.getFhw();
			double ljg = obj.getGeliu();
			double qhw = obj.getQhw();
			double hff = obj.getHhf();
			double syl = obj.getSyl();
			double hxj = obj.getYlzbmhxj();
			double lhw = obj.getLhw();
			double yls = obj.getYls();
			double dcgj = obj.getFdcjq();
			double rjy = obj.getRjy();
			double ph = obj.getPh();
			//高猛酸盐指数
			if(gmsyzs < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setGaomengsuanyan(Double.parseDouble(df.format(gmsyzs)));
			}else{
				obj.setGaomengsuanyan(significanceDigit(gmsyzs, 3));
			}
			
			//化学需氧量
			if(hxxyl > 0){
				DecimalFormat df = new DecimalFormat("#0");
				obj.setHxxyl(Double.parseDouble(df.format(hxxyl)));
			}
			
			//五日生化需氧量
			if(wrshxyl < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setWrshxyl(Double.parseDouble(df.format(wrshxyl)));
			}else{
				obj.setWrshxyl(significanceDigit(wrshxyl, 3));
			}
			
			//氨氮
			if(ad < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setAndan(Double.parseDouble(df.format(ad)));
			}else{
				obj.setAndan(significanceDigit(ad, 3));
			}
			
			//总磷
			if(zl < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setZonglin(Double.parseDouble(df.format(zl)));
			}else{
				obj.setZonglin(significanceDigit(zl, 3));
			}
			
			//总氮
			if(zd < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setZongdan(Double.parseDouble(df.format(zd)));
			}else{
				obj.setZongdan(significanceDigit(zd, 3));
			}
			
			//锌
			if(xin < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setXin(Double.parseDouble(df.format(xin)));
			}else{
				obj.setXin(significanceDigit(xin, 3));
			}
			
			//铜
			if(tong > 0){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setTong(Double.parseDouble(df.format(tong)));
			}
			
			//铅
			if(qian > 0){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setQian(Double.parseDouble(df.format(qian)));
			}
			
			//镉
			if(ge > 0){
				DecimalFormat df = new DecimalFormat("#0.0000");
				obj.setGe(Double.parseDouble(df.format(ge)));
			}
			
			//硒（mg/L转换μg/L乘以1000）
			if(xi*1000 < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setXi(Double.parseDouble(df.format(xi*1000)));
			}else{
				obj.setXi(significanceDigit(xi*1000, 3));
			}
			
			//砷（mg/L转换μg/L乘以1000）
			if(shen*1000 < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setShen(Double.parseDouble(df.format(shen*1000)));
			}else{
				obj.setShen(significanceDigit(shen*1000, 3));
			}
			
			//汞（mg/L转换μg/L乘以1000）
			if(gong*1000 < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setGong(Double.parseDouble(df.format(gong*1000)));
			}else{
				obj.setGong(significanceDigit(gong*1000, 3));
			}
			
			//氟化物
			if(fhw < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setFhw(Double.parseDouble(df.format(fhw)));
			}else{
				obj.setFhw(significanceDigit(fhw, 3));
			}
			
			//六价铬
			if(ljg < 1.00){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setGeliu(Double.parseDouble(df.format(ljg)));
			}else{
				obj.setGeliu(significanceDigit(ljg, 3));
			}
			
			//氰化物
			if(qhw < 1.00){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setQhw(Double.parseDouble(df.format(qhw)));
			}else{
				obj.setQhw(significanceDigit(qhw, 3));
			}
			
			//挥发酚
			if(hff < 0.100){
				DecimalFormat df = new DecimalFormat("#0.0000");
				obj.setHhf(Double.parseDouble(df.format(hff)));
			}else{
				obj.setHhf(significanceDigit(hff, 3));
			}
			
			//石油类
			if(syl < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setSyl(Double.parseDouble(df.format(syl)));
			}else{
				obj.setSyl(significanceDigit(syl, 3));
			}
			
			//阴离子表面活性剂
			if(hxj < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setYlzbmhxj(Double.parseDouble(df.format(hxj)));
			}else{
				obj.setYlzbmhxj(significanceDigit(hxj, 3));
			}
			
			//硫化物
			if(lhw < 1.00){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setLhw(Double.parseDouble(df.format(lhw)));
			}else{
				obj.setLhw(significanceDigit(lhw, 3));
			}
			
			//叶绿素α
			if(yls > 0){
				DecimalFormat df = new DecimalFormat("#0");
				obj.setYls(Double.parseDouble(df.format(yls)));
			}
			
			//粪大肠杆菌群
			if(dcgj > 0){
				DecimalFormat df = new DecimalFormat("#0");
				obj.setFdcjq(Double.parseDouble(df.format(dcgj)));
			}
			
			//溶解氧
			if(rjy > 0){
				obj.setRjy(significanceDigit(rjy, 3));
			}
			
			//PH值
			if(ph > 0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setPh(Double.parseDouble(df.format(ph)));
			}
		}
		return dataList;
	}
	
	public static List<WaterBean> DbswaterDataFormat(List<WaterBean> dataList){
		//遍历list对象，对参数数据进行文件标准化
		if(dataList.size() > 0)
		for(WaterBean obj : dataList){
			double gmsyzs = obj.getGaomengsuanyan();
			double hxxyl = obj.getHxxyl();
			double wrshxyl = obj.getWrshxyl();
			double ad = obj.getAndan();
			double zl = obj.getZonglin();
			double zd = obj.getZongdan();
			double xin = obj.getXin();
			double tong = obj.getTong();
			double qian = obj.getQian();
			double ge = obj.getGe();
			double xi = obj.getXi();
			double shen = obj.getShen();
			double gong = obj.getGong();
			double fhw = obj.getFhw();
			double ljg = obj.getGeliu();
			double qhw = obj.getQhw();
			double hff = obj.getHhf();
			double syl = obj.getSyl();
			double hxj = obj.getYlzbmhxj();
			double lhw = obj.getLhw();
			double yls = obj.getYls();
			double dcgj = obj.getFdcjq();
			double rjy = obj.getRjy();
			double ph = obj.getPh();
			//高猛酸盐指数
			if(gmsyzs > 0){
				obj.setGaomengsuanyan(significanceDigit(gmsyzs, 2));
			}
			
			//化学需氧量
			if(hxxyl > 0){
				DecimalFormat df = new DecimalFormat("#0");
				obj.setHxxyl(Double.parseDouble(df.format(hxxyl)));
			}
			
			//五日生化需氧量
			if(wrshxyl < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setWrshxyl(Double.parseDouble(df.format(wrshxyl)));
			}else{
				obj.setWrshxyl(significanceDigit(wrshxyl, 3));
			}
			
			//氨氮
			if(ad > 0){
				obj.setAndan(significanceDigit(ad, 3));
			}
			
			//总磷
			if(zl > 0){
				obj.setZonglin(significanceDigit(zl, 3));
			}
			
			//总氮
			if(zd < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setZongdan(Double.parseDouble(df.format(zd)));
			}else{
				obj.setZongdan(significanceDigit(zd, 3));
			}
			
			//锌
			if(xin < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setXin(Double.parseDouble(df.format(xin)));
			}else{
				obj.setXin(significanceDigit(xin, 3));
			}
			
			//铜
			if(tong > 0){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setTong(Double.parseDouble(df.format(tong)));
			}
			
			//铅
			if(qian > 0){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setQian(Double.parseDouble(df.format(qian)));
			}
			
			//镉
			if(ge > 0){
				DecimalFormat df = new DecimalFormat("#0.0000");
				obj.setGe(Double.parseDouble(df.format(ge)));
			}
			
			//硒（mg/L转换μg/L乘以1000）
			if(xi*1000 < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setXi(Double.parseDouble(df.format(xi*1000)));
			}else{
				obj.setXi(significanceDigit(xi*1000, 3));
			}
			
			//砷（mg/L转换μg/L乘以1000）
			if(shen*1000 < 100){
				DecimalFormat df = new DecimalFormat("#0.0");
				obj.setShen(Double.parseDouble(df.format(shen*1000)));
			}else{
				obj.setShen(significanceDigit(shen*1000, 3));
			}
			
			//汞（mg/L转换μg/L乘以1000）
			if(gong*1000 < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setGong(Double.parseDouble(df.format(gong*1000)));
			}else{
				obj.setGong(significanceDigit(gong*1000, 3));
			}
			
			//氟化物
			if(fhw < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setFhw(Double.parseDouble(df.format(fhw)));
			}else{
				obj.setFhw(significanceDigit(fhw, 3));
			}
			
			//六价铬
			if(ljg < 1.00){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setGeliu(Double.parseDouble(df.format(ljg)));
			}else{
				obj.setGeliu(significanceDigit(ljg, 3));
			}
			
			//氰化物
			if(qhw < 1.00){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setQhw(Double.parseDouble(df.format(qhw)));
			}else{
				obj.setQhw(significanceDigit(qhw, 3));
			}
			
			//挥发酚
			if(hff < 0.100){
				DecimalFormat df = new DecimalFormat("#0.0000");
				obj.setHhf(Double.parseDouble(df.format(hff)));
			}else{
				obj.setHhf(significanceDigit(hff, 3));
			}
			
			//石油类
			if(syl < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setSyl(Double.parseDouble(df.format(syl)));
			}else{
				obj.setSyl(significanceDigit(syl, 3));
			}
			
			//阴离子表面活性剂
			if(hxj < 10.0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setYlzbmhxj(Double.parseDouble(df.format(hxj)));
			}else{
				obj.setYlzbmhxj(significanceDigit(hxj, 3));
			}
			
			//硫化物
			if(lhw < 1.00){
				DecimalFormat df = new DecimalFormat("#0.000");
				obj.setLhw(Double.parseDouble(df.format(lhw)));
			}else{
				obj.setLhw(significanceDigit(lhw, 3));
			}
			
			//叶绿素α
			if(yls > 0){
				DecimalFormat df = new DecimalFormat("#0");
				obj.setYls(Double.parseDouble(df.format(yls)));
			}
			
			//粪大肠杆菌群
			if(dcgj > 0){
				DecimalFormat df = new DecimalFormat("#0");
				obj.setFdcjq(Double.parseDouble(df.format(dcgj)));
			}
			
			//溶解氧
			if(rjy > 0){
				obj.setRjy(significanceDigit(rjy, 3));
			}
			
			//PH值
			if(ph > 0){
				DecimalFormat df = new DecimalFormat("#0.00");
				obj.setPh(Double.parseDouble(df.format(ph)));
			}
		}
		return dataList;
	}
}
