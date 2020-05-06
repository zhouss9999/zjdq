package wy.util;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

@SuppressWarnings("all")

@Component
public class DataFormatUtilImpl implements DataFormatUtil {

	//得到APP端上传的数据
	public StringBuffer convertIotoString(HttpServletRequest request) {
		StringBuffer jb = new StringBuffer();
		String line = "";
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				jb.append(line);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.err.println("APP端JSON数据 （"+java.util.Calendar.getInstance().getTime()+"）----------"+jb);
		return jb;
	}
	
	//处理APP端 传入的数据，去掉[] 双引号转单引号
	public JSONObject formatDataToJson(HttpServletRequest request){
		
		String value = convertIotoString(request).toString();
		if(value.indexOf("[")>=0 && value.indexOf("[")==0){
			value = value.substring(1);
		}
		if(value.indexOf("]")>=0 && value.lastIndexOf("]")==value.length()-1){
			value = value.substring(0,value.length()-1);
		}
		if(value.contains("\"")){
			value = value.toString().replaceAll("\"","'");
		}
		if(value==null || value.equals("")){
			value="{}";
		}
		JSONObject jsonObject = new JSONObject();
		JSONObject jsons = jsonObject.fromObject(value);
		return jsons;
	}
	
	//处理APP端 传入的数据，去掉[] 双引号转单引号
		public String convertStr(String value){
			
			if(value.indexOf("[")>=0 && value.indexOf("[")==0){
				value = value.substring(1);
			}
			if(value.indexOf("]")>=0 && value.lastIndexOf("]")==value.length()-1){
				value = value.substring(0,value.length()-1);
			}
			if(value.contains("\"")){
				value = value.toString().replaceAll("\"","'");
			}
			if(value==null || value.equals("")){
				value="{}";
			}
			return value;
		}
		
	
	//分页处理
		public Map<String,Object> dataPaging(HttpServletRequest request){
			Map<String,Object> pageMap = new HashMap<String,Object>();
			String currentPageStr = request.getParameter("currentPage");
			String pageNumsStr = request.getParameter("pageNums");
			String lineNumsStr = request.getParameter("lineNums");
			int currentPage = (currentPageStr ==null || currentPageStr.equals(""))?1:Integer.valueOf(currentPageStr);
			int pageNums = (pageNumsStr==null || pageNumsStr.equals(""))?10:Integer.valueOf(pageNumsStr);
			int lineNums = (lineNumsStr==null || lineNumsStr.equals(""))?1:Integer.valueOf(lineNumsStr);
			pageMap.put("currentPage", currentPage-1);
			pageMap.put("pageNums", pageNums);
			pageMap.put("lineNums", lineNums);
			return pageMap;
		}
}