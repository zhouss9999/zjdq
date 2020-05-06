package wy.util;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class NumberCastUtil {
	
	
	
	
	
		
		//System.out.println(object2Str2(3.6E-18));
		public static void main(String[] args){
			
			System.out.println(object2Str("3.6E-18"));
	    	  System.out.println(new BigDecimal("6.88e-018"));
	    	  System.out.println("10*log10:"+10*NumberCastUtil.log10(6.88e-018));
	    	  double d = Double.parseDouble("6.88e-018");
	    	  System.out.println(d);
	    	  double e = NumberCastUtil.log10(d);
	    	  System.out.println("str装double:"+10*e);
	    	  
	    	  System.out.println(new BigDecimal("8.68e-017"));
	    	  System.out.println("10*log10:"+10*NumberCastUtil.log10(8.68e-017));
	    	  double h = Double.parseDouble("8.68e-017");
	    	  System.out.println(h);
	    	  double i = NumberCastUtil.log10(h);
	    	  System.out.println("str装double:"+10*i);
	    	  
	    	  
	    	  System.out.println(new BigDecimal("1.10e-015"));
	    	  System.out.println("10*log10:"+10*NumberCastUtil.log10(1.10e-015));
	    	  
	    	  double f = Double.parseDouble("1.10e-015");
	    	  System.out.println(f);
	    	  double g = NumberCastUtil.log10(f);
	    	  System.out.println("str装double:"+10*g);
	    	  
	    	  double z = 10*NumberCastUtil.log10(Double.parseDouble("1.10e-015"));
	    	  System.out.println("z:"+z);
	      }

		
	
	
	
	
	/**
	* 把科学计数法显示出全部数字
	* @param d
	*/
	public static String object2Str(String d) {
		BigDecimal bd = new BigDecimal("3.6E-18");  
		System.out.println(bd.toPlainString());
		return bd.toPlainString();
	}
	
	
	/**
	* 把科学计数法显示出全部数字
	* @param d
	*/
	public static String object2Str2(Object d) {
		if (d == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getInstance();
	    System.out.println(nf.format(d));
        return nf.format(d);
	}
	
	
	/**
	 * 求以10为底的对数
	 * @param value
	 * @return
	 */
	public static  double log10(double value) {
		return Math.log(value) / Math.log(10.0); 
	    }

}
