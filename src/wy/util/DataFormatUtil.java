package wy.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

public interface DataFormatUtil {
	// json数据转javabean
	public JSONObject formatDataToJson(HttpServletRequest request);

	// 分页数据处理
	public Map<String, Object> dataPaging(HttpServletRequest request);
	
	// 字符串转换
	public String convertStr(String value);
}
