package wy.qingdao_atmosphere.datacenter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 此单例存放设备列表信息，用来判断设备状态是否有变化
 * @author hero
 *
 */
public class DataInstance {
	
	private DataInstance(){	}
	
	private static DataInstance instance = null;
	
	public static DataInstance getInstance(){
		if(instance==null){
			synchronized(DataInstance.class) {
                if (instance == null) {
                    instance = new DataInstance();
                }
            }
		}
		return instance;
	}
	
	private List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
	
	public void addlist(List<Map<String, Object>> datalist){
		list = datalist;
	}
	
	public List<Map<String, Object>> getlist(){
		return list;
	}
	
	
}
