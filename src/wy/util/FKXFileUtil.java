package wy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.domain.SiteData;
import wy.qingdao_atmosphere.countrysitedata.service.WindProfileService;

@Component
public class FKXFileUtil {

	@Autowired
	private  WindProfileService wpfService;
	@Autowired
	private  SiteDataDao dataDao;

	/**
	 * 取ftp上的风廓线文件列表
	 * 
	 * @param FTPClient
	 * @param hour
	 *            取几小时的数据
	 * @return fileMap
	 */
	public  Map<String, List<SiteData>> getFileList(File dir, int hour,
			String dbMaxTime) {

		// 获取两小时之内的数据
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)
				- hour);// 取几小时的数据
		String time = sdf.format(calendar.getTime());

		// 创建map，用于返回
		Map<String, List<SiteData>> fileMap = new LinkedHashMap<String, List<SiteData>>();

		// 获得指定目录下所有文件名
		File[] listFiles = dir.listFiles();

		List<String> robsList = new ArrayList<String>();// 实时产品采样数据
		List<String> radList = new ArrayList<String>();// 径向数据

		for (File f : listFiles) {
			if (f.isFile() && f.length() > 0) {
				System.out.println("file is file ");
				if (f.getName().contains("ROBS")) {// 每小时产品采样数据
					System.out.println("file is contais ROBS ");
					if(f.getName().split("_")[4].compareTo(time)>0){
						System.out.println(f.getName()+".filetime is >"+time);
						robsList.add(f.getName());
					}
				} else if (f.getName().endsWith("RAD.TXT")) {// 每小时产品采样数据
					System.out.println("file is contais ROBS ");
					radList.add(f.getName());
				}
			}

		}
        //解析robs数据文件
		fileMap.put("robs",
				readFile(dir, getFileListByTime(robsList, dbMaxTime)));
		
		//此处应采用专门的rad径向文件解析方法解析，暂时还没写
		/*fileMap.put("rad",
				readFile(dir, getFileListByTime(radList, dbMaxTime)));*/
     
		return fileMap;
	}

	/**
	 * 获取大于数据库时间的数据
	 * 
	 * @param goupList
	 *            分组后的文件名列表
	 * @param dbMaxTime
	 *            数据库最大时间
	 * @return fileLists
	 */
	public static List<String> getFileListByTime(List<String> goupList,
			String dbMaxTime) {
		List<String> groupFileList = new ArrayList<String>();
		for (String filename : goupList) {
			if ((dbMaxTime != null) && (!"".equals(dbMaxTime))) {
				if (filename.split("_")[4].compareTo(dbMaxTime) > 0) {
					groupFileList.add(filename);
				}
			} else {
				groupFileList.add(filename);
			}

		}

		return groupFileList;
	}

	/**
	 * 取ftp上的文件内容
	 * 
	 * @param FTPClient
	 * @param fileLists
	 * @return list
	 */
	public  List<SiteData> readFile(File dir,
			List<String> filelist) {
        String filepath = dir.getAbsolutePath();   //文件路劲，试验看是否可行
        System.out.println("dir.getAbsolutePath-文件路径:"+filepath);
		//List<String> list = new ArrayList<String>();
		String dirpath = "C:\\Users\\Lenovo\\Desktop\\风廓线\\20180803\\WIND"; //文件路劲，先写死
		
		List<SiteData> listdata = new ArrayList<SiteData>();// 结果集对象集合，要插入到数据库中的
		if (filelist.size() > 0) {
			for (String filename : filelist) {
				//新加的动态的dataguid添加数据，还没测
				String qzbh = filename.split("_")[3]; //区站编号
				Map<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("qzbh",qzbh);
				Map<String,Object> map = dataDao.queryByStaNum(paramMap);  //根据区站编号查风廓线dataguid相关参数
				String dataguid = map.get("objtypeid").toString()+"_"+map.get("objid").toString()+"_"+map.get("devicenumber").toString()+"_";
				System.out.println("dataguid:"+dataguid);
				//---------------&&&&&&&以上是新改的
				
				BufferedReader in = null; // 如果maxtime==null，说明数据库第一次读或者没数据，全部读取并插入
				System.out.println("filetime>maxtime,进来方法了");
				File file = new File(dirpath + "\\" + filename);
				System.out.println("file-文件路径:"+file.getAbsolutePath());
				// BufferedWriter out = null;
				try {
					// 加入编码字符集
					in = new BufferedReader(new InputStreamReader(
							new FileInputStream(file), "gbk"));

					// 加入编码字符集
					// out = new BufferedWriter( new OutputStreamWriter(new
					// FileOutputStream(secondFile), "gbk"));

					String line = "";
					StringBuilder spfx = new StringBuilder(); // 用来接收水平方向所对应的的各个高度的值
					StringBuilder spfs = new StringBuilder(); // 用来接收水平风速所对应的的各个高度的值
					StringBuilder czfs = new StringBuilder(); // 用来接收垂直风速所对应的的各个高度的值
					StringBuilder spfxkxd = new StringBuilder(); // 用来接收水平方向可信度所对应的的各个高度的值
					StringBuilder czfxkxd = new StringBuilder(); // 用来接收垂直方向可信度所对应的的各个高度的值
					StringBuilder cn2 = new StringBuilder(); // 用来接收垂直方向Cn2所对应的的各个高度的值

					spfx.append("{");
					spfs.append("{");
					czfs.append("{");
					spfxkxd.append("{");
					czfxkxd.append("{");
					cn2.append("{");
					int num = 0;
					String collecttime = "";// 观测时间
					while ((line = in.readLine()) != null) {

						if (num == 1) { // 第二行，测站基本参数
							String[] arrs = line.replace(" ", ",").split(",");
							collecttime = arrs[arrs.length - 1]; // 获取观测时间

						}
						num++;
						if (num > 3) {
							if (!"NNNN".equals(line)) { // 不拼接最后的结束标志NNNN,产品数据实体部分开始
								String[] arrs = line.replace(" ", ",").split(
										",");
								for (int i = 0; i < arrs.length; i++) {

									if (i == 0) {// 采样高度
										spfx.append('"' + "spfx_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										spfs.append('"' + "spfs_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										czfs.append('"' + "czfs_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										spfxkxd.append('"' + "spfxkxd_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										czfxkxd.append('"' + "czfxkxd_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
										cn2.append('"' + "cn2_"
												+ Integer.parseInt(arrs[i])
												+ '"'); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成int值，去掉高位上的零
									} else if (i == 1) {
										spfx.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i]))+ '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 2) {
										spfs.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 3) {
										czfs.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 4) {
										spfxkxd.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} else if (i == 5) {
										czfxkxd.append(":"
												+ '"'
												+ (arrs[i].contains("/") ? "/"
														: Float.parseFloat(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									} /*else if (i == 6) {
										cn2.append(":"
												+ '"'
												+ (arrs[i]
														.contains("/") ? "/"
														: NumberCastUtil.object2Str(arrs[i])) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									}*/
									else if (i == 6) {
										cn2.append(":"
												+ '"'
												+ (arrs[i]
														.contains("/") ? "/"
														: 10*NumberCastUtil.log10(Double.parseDouble(arrs[i]))) + '"' + ","); // 在值前后加“”双引号，应该是方便到时查询的时候转json,转化成float值，去掉高位上的零
									}
								}
							}

						}
						System.out.println(line.replace(" ", ","));

						// out.write(line+"\r\n");
					}
					// spfx.append("}"); //加上右半结束框

					String spfxStr = spfx.toString().substring(0,
							spfx.toString().length() - 1)
							+ "}"; // 水平方向 paramid :168
					wpfService.addSiteData(spfxStr, collecttime, dataguid+"168",
							listdata); // 封装为结果对象并添加到结果集集合中

					String spfsStr = spfs.toString().substring(0,
							spfs.toString().length() - 1)
							+ "}"; // 水平风速 paramid :169
					wpfService.addSiteData(spfsStr, collecttime, dataguid+"169",
							listdata); // 封装为结果对象并添加到结果集集合中

					String czfsStr = czfs.toString().substring(0,
							czfs.toString().length() - 1)
							+ "}"; // 垂直风速 paramid :170
					wpfService.addSiteData(czfsStr, collecttime, dataguid+"170",
							listdata); // 封装为结果对象并添加到结果集集合中

					String spfxkxdStr = spfxkxd.toString().substring(0,
							spfxkxd.toString().length() - 1)
							+ "}"; // 水平方向可信度 paramid :171
					wpfService.addSiteData(spfxkxdStr, collecttime, dataguid+"171",
							listdata); // 封装为结果对象并添加到结果集集合中

					String czfxkxdStr = czfxkxd.toString().substring(0,
							czfxkxd.toString().length() - 1)
							+ "}"; // 垂直方向可信度 paramid :172
					wpfService.addSiteData(czfxkxdStr, collecttime, dataguid+"172",
							listdata); // 封装为结果对象并添加到结果集集合中

					String cn2Str = cn2.toString().substring(0,
							cn2.toString().length() - 1)
							+ "}"; // 垂直方向Cn2 paramid :173
					wpfService
							.addSiteData(cn2Str, collecttime, dataguid+"173", listdata); // 封装为结果对象并添加到结果集集合中

					// 将检测到的ROBS实时采样数据插入到数据库中
					System.out.println("listdata.size:" + listdata.size());
					//dataDao.addCollectDataTwo(listdata);

					System.out.println("spfx:" + spfxStr);
					System.out.println("spfsStr:" + spfsStr);
					System.out.println("czfsStr:" + czfsStr);
					System.out.println("spfxkxdStr:" + spfxkxdStr);
					System.out.println("czfxkxdStr:" + czfxkxdStr);
					System.out.println("cn2Str:" + cn2Str);
				} catch (FileNotFoundException e) {
					System.out.println("file is not fond");
				} catch (IOException e) {
					System.out.println("Read or write Exceptioned");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					wpfService.closed(in);
					// closed(out);
				}
			}
			
		}
		return listdata;
	}
}
