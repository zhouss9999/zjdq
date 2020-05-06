package wy.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ConvertCoordinate {

	// 传入 经纬度的度分秒坐标 如：经度 121 12 12.1 维度 31 11 11.1
	public double getDecimal(double d, double f, double m) {
		double sec = m / 3600;
		double minutes = f / 60;
		double convertResult = d + minutes + sec;
		return convertResult;
	}

	// double Lngd,double Lngf,double Lngm经度 度分秒
	// double latd,double latf,double latm维度 度分秒
	public String getCoordinate(double Lngd, double Lngf, double Lngm, double latd,
			double latf, double latm) {
		double sec = Lngd / 3600;
		double minutes = Lngf / 60;
		double convertLng = Lngm + minutes + sec;

		double sec1 = latm / 3600;
		double minutes1 = latf / 60;
		double convertLat = latd + minutes1 + sec1;

		return convertLng + " " + convertLat;
	}
	
	// datasStr 经度维度 度分秒
	public Map<String, String> getCoordinatetwo(String datasStr){
		Map<String, String> coordinateMap = new HashMap<String, String>();
		if(datasStr.contains("----")){
			 String [] datasArray=datasStr.split("----");
			 String[] lngArray = new String[3];
			 String[] latArray = new String[3];
			 String objid = datasArray[0];
			 String lng = datasArray[1];
			 String lat = datasArray[2];
			 if(lng.contains(",")){
				 lngArray = lng.split(",");
			 }
			 if(lat.contains(",")){
				 latArray = lng.split(",");
			 }
			 double Lngd=Double.valueOf(lngArray[0].toString());
			 double Lngf=Double.valueOf(lngArray[1].toString());
			 double Lngm=Double.valueOf(lngArray[2].toString());
			 double latd=Double.valueOf(latArray[0].toString());
			 double latf=Double.valueOf(latArray[1].toString());
			 double latm=Double.valueOf(latArray[2].toString());
			 double sec = Lngd/3600;
			 double minutes = Lngf/60;
			 double convertLng = Lngm+minutes+sec;
			 double sec1 = latm/3600;
			 double minutes1 = latf/60;
			 double convertLat = latd+minutes1+sec1;
			 coordinateMap.put("objid", objid);
			 coordinateMap.put("coordinate", convertLng+" "+convertLat);
		}	
		return coordinateMap;
	}

	public String getCoordinate() {

		return "";
	}

	public static void main(String[] args) {
		double d = 121;
		double f = 28;
		double m = 36;

		double sec = m / 3600;
		double minutes = f / 60;
		double convertResult = d + minutes + sec;

		System.out.println("convertResult ------ " + convertResult);
	}

}
