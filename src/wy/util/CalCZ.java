package wy.util;
/**
 * 插值算法
 * @author Lenovo
 *
 */
public class CalCZ {
	
	
	/**
	 * 
	 * @param lastHt 上一个y值（比目标值小）
	 * @param nextHt 下一个y值（比目标值大）
	 * @param flagHt 目标值 
	 * @param lastvl 上一个x值 
	 * @param nextvl 下一个x值
	 * @return
	 */
	public static  float calCZ(String lastHt,String nextHt,String flagHt,String lastvl,String nextvl){
		float lastH = Float.valueOf(lastHt); //lastH  上一个值（比目标值小）200.60
		float nextH = Float.valueOf(nextHt); //nextH  下一个值（比目标值大） 300.40
		float flagH = Float.valueOf(flagHt); //flagH  目标值  240.50
		float  lastv =Float.valueOf(lastvl);    //lastv  上一个值  5.0
		float  nextv = Float.valueOf(nextvl);   //nextv  下一个值 4.0
		float fv = (nextH-lastH)/(lastv-nextv); 
		//System.out.println("fv:"+fv);
		float sv = (flagH-lastH)/fv;
		//System.out.println("sv:"+sv);
		float result = nextv+sv;
		//System.out.println("result:"+result);
		return result;
	}

}
