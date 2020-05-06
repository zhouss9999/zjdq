package wy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import sun.misc.BASE64Decoder;

@Component
public class FileUtilImpl implements IFileUtil {
	
	public String fileUploadBefore(HttpServletRequest request,String DataSource) {
		return fileUpload(request,DataSource);
	}
	
	public String fileUpload(HttpServletRequest request,String DataSource) {
		String imgUrl = "";
		try {
			// 转型为MultipartHttpRequest：
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			// 获得文件：
			MultipartFile file = multipartRequest.getFile("file");
			// 获得文件名：
			//文件名：年月日时分秒毫秒+随机数+文件上传的名称
			Double filenameDouble = Math.random();
			Calendar datenow = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String filenameStr = sdf.format(datenow.getTime());
			String filename = filenameStr+String.valueOf(filenameDouble)+"_"+file.getOriginalFilename();
			String filePath = "";
			if(DataSource.equals(""))
				filePath = request.getSession().getServletContext()
					.getRealPath("/")
					+ "upload\\" + filename;
			else
				filePath = request.getSession().getServletContext()
				.getRealPath("/")
				+ "upload\\"+DataSource+"\\" + filename;
			
			File source = new File(filePath.toString());
			file.transferTo(source);
			//转换成前段所需要的 图片路径
			imgUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/upload/"+DataSource+"/"+filename;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imgUrl;
	}
	
	public String attachUpload(HttpServletRequest request,String DataSource) {
		String imgUrl = "";
		String fileName = "";
		try {
			// 转型为MultipartHttpRequest：
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			// 获得文件：
			MultipartFile file = multipartRequest.getFile("file");
			// 获得文件名：
			//文件名：年月日时分秒毫秒+随机数+文件上传的名称
			Double filenameDouble = Math.random();
			Calendar datenow = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String filenameStr = sdf.format(datenow.getTime());
			String filename = filenameStr+String.valueOf(filenameDouble)+"_"+file.getOriginalFilename();
			fileName=file.getOriginalFilename();
			String filePath = "";
			if(DataSource.equals(""))
				filePath = request.getSession().getServletContext()
					.getRealPath("/")
					+ "upload\\" + filename;
			else
				filePath = request.getSession().getServletContext()
				.getRealPath("/")
				+ "upload\\"+DataSource+"\\" + filename;
			
			File source = new File(filePath.toString());
			file.transferTo(source);
			//转换成前段所需要的 图片路径
			imgUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/upload/"+DataSource+"/"+filename;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName+"&"+imgUrl;
	}

	public String fileDownload(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	//手机端通过Base64格式上传文件
    public String phoneAttachUpload(HttpServletRequest request,String DataSource) {
		String imgUrl = "";
		String filename = "";
		try {
			String imgStr =request.getParameter("imageData");
            BASE64Decoder decoder = new BASE64Decoder();  
            // Base64解码  
            byte[] imageByte = decoder.decodeBuffer(imgStr);  
            for (int i = 0; i < imageByte.length; ++i) {  
                if (imageByte[i] < 0) {// 调整异常数据  
                	imageByte[i] += 256;  
                }  
            }
			// 获得文件名：
			//文件名：年月日时分秒毫秒+随机数+文件上传的名称
			Double filenameDouble = Math.random();
			Calendar datenow = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String filenameStr = sdf.format(datenow.getTime());
			filename = filenameStr+String.valueOf(filenameDouble)+".jpg";
			String filePath = "";
			if(DataSource.equals(""))
				filePath = request.getSession().getServletContext()
					.getRealPath("/")
					+ "upload\\" + filename;
			else
				filePath = request.getSession().getServletContext()
				.getRealPath("/")
				+ "upload\\"+DataSource+"\\" + filename;
			
             try {
                 // 生成文件         
                 File imageFile = new File(filePath.toString());
                 imageFile.createNewFile();
                    if(!imageFile.exists()){
                        imageFile.createNewFile();
                     }
                     OutputStream imageStream = new FileOutputStream(imageFile);
                     imageStream.write(imageByte);
                     imageStream.flush();
                     imageStream.close();                    
             } catch (Exception e) {         
                 e.printStackTrace();
             }
			//转换成前段所需要的 图片路径
			imgUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/upload/"+DataSource+"/"+filename;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filename+"&"+imgUrl;
	}

}
