package wy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

public class SendHttpRequest {
	/**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */   
    public static String sendGet(String url, String param) {
    	StringBuffer buffer = new StringBuffer();
    	String result = "";
		try {
			String requestUrl = "";
        	if ("".equals(param) || param == null) {
        		requestUrl = url;
			} else {
				requestUrl = url + "?" + param;
			}
			//建立网络连接
			URL urlobj = new URL(requestUrl);
			//获取连接对象
			URLConnection connection = urlobj.openConnection();
			// 设置允许输出
			connection.setDoOutput(true);
			
			// 设置通用的请求属性
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			
			//直接连接
			connection.connect();
			//获取请求结果
			InputStreamReader reader = new InputStreamReader(connection.getInputStream(),"UTF-8");
			//建立文件缓冲流
			BufferedReader br = new BufferedReader(reader);
			//建立临时文件
			String temp =null;
			while((temp = br.readLine()) != null){
				buffer.append(temp);
			}
			br.close();
			reader.close();
			result=new String(buffer.toString().getBytes());
		} catch (Exception e) {
			Logger.getLogger("").info("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            Logger.getLogger("").info("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }  
    
}
