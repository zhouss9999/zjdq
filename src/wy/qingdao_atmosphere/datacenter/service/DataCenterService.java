package wy.qingdao_atmosphere.datacenter.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import wy.qingdao_atmosphere.datacenter.domain.Devicetype;
import wy.qingdao_atmosphere.datacenter.domain.Objtype;
import wy.qingdao_atmosphere.datacenter.domain.Param;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfo;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfoadd;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;
import wy.util.AirBean;

/**
 * 参数，设备管理
 * @author hero
 *
 */
public interface DataCenterService {

	//ky:参数管理
	public List<Param> getAllparameter(Map<String,Object> map);
	//参数管理 新增
	public void addparam(Param param);
	//参数管理 修改
	public void updateParam(Param param);
	//参数管理 删除或恢复
	public void delparam(Map<String,Object> map);
	//===========================================对象类型
	//对象类型 查询
	public List<Objtype> queryObjtype(Map<String,Object> map);
	//对象类型 增加
	public void addObjtype(Objtype objtype);
	//对象类型 删除或恢复
	public void delObjtype(Map<String,Object> map);
	//对象类型 修改
	public void updateObjtype(Objtype objtype);
	//对象安装设备 查询
	public List<Map<String, Object>> queryObjDevice(Map<String,Object> map);
	//对象安装设备  添加
	public void addObjinfo(Map<String,Object> map);
	//对象安装设备 安装页 设备查询
	public List<Map<String, Object>> querydeviceName(Map<String,Object> map);
	//对象安装设备 卸载
	public void delObjinfo(Map<String, Object> map);
	//获取地理位置信息
	public Map<String, Object> queryobjdt(Map<String, Object> map);
	//修改地理位置
	public void updateobjdt(Map<String, Object> map);
	
	//============================================设备管理
	//设备类型管理  查询
	public List<Devicetype> queryDevicetypes(Map<String, Object> map);
	//设备类型管理  添加
	public void addDevicetype(Devicetype devicetype);
	//设备类型管理 修改
	public void updateDevicetype(Devicetype devicetype);
	//设备类型管理 删除或恢复
	public void delDeviceype(Map<String,Object> map);
	
	//设备版本添加
	public void fileupload(HttpServletRequest request,HttpServletResponse response) throws Exception;
	//设备版本查询
	public List<Map<String,Object>> queryDevVersion(Map<String,Object> map);
	//设备版本删除
	public void delDevVersion(String verid);
	//设备管理 查询
	public List<Map<String, Object>> queryDevice(Map<String,Object> map);
	//设备管理 列表查询条件  设备类型查询
	public List<Map<String, Object>> queryDevicetypeMap();
	//设备管理 列表查询条件  设备名称查询
	public List<Map<String, Object>> queryDeviceNameMap(int devicetypeid);
	//设备管理 添加
	public void addDevice(Map<String,Object> map);
	//设备管理 修改
	public int updateDevice(Map<String,Object> map);
	//设备管理 删除或恢复
	public void delDevice(Map<String,Object> map);
	//设备参数管理  列表查询
	public List<Map<String, Object>> queryDeviceParam(Map<String, Object> map);
	//设备参数管理  添加 参数查询
	public List<Map<String, Object>> queryParambydeviceType(Map<String, Object> map);
	//设备参数管理  添加
	public void addDeviceTypeParam(Map<String, Object> map);
	//设备参数管理  修改页查询
	public List<Map<String, Object>> queryDP(int devicetypeid);
	//设备参数管理  删除或恢复
	public void delDeviceTypeParam(Map<String, Object> map);
	//设备参数管理  修改
	public void updateDP(List<Map<String, Object>> list,String devicetypeid);
	//对象管理   对象列表查询
	public List<Map<String, Object>> queryObj(Map<String, Object> map);
	//对象管理  对象基本信息查询
	public List<Map<String,Object>> queryInformation(Map<String, Object> map);
	//对象管理  对象基本信息查询
	public List<Map<String,Object>> queryInformation2(Map<String, Object> map);
	//对象管理   添加
	public void addObj(Map<String, Object> map);
	//对象管理 基本信息维护
	public void updateInfostore(Map<String, Object> map);
	//对象管理 基本信息维护
	public void updateZBInfostore(Map<String, Object> map);
	//对象管理 基本信息维护
	public void insertInfostore(Map<String, Object> map);
	//对象管理 子表信息维护
	public void insertZBInfostore(Map<String, Object> map);
	//对象管理 基本表字段查询
	public List<Map<String, Object>> queryFieldName(String objid);
	//对象管理 删除或恢复
	public void delObj(Map<String, Object> map);
	//查询对应字段信息
	public List<Map<String,Object>> queryFieldshowname(Map<String, Object> map);
	//对象管理  子表下拉查询
	public List<Map<String,Object>> queryObjZB(String objtypeid);
	//对象管理  子表信息查询
	public List<Map<String,Object>> queryZBInformation(Map<String,Object> map);
	//对象管理 查询对象子表字段
	public List<Map<String, Object>> queryZBFieldName(Map<String,Object> map);
	
	//保存实时数据
	public void addwzData(Map<String, Object> map);
	//触发器修改实时数据
	public void updateTriggerData(Map<String, Object> map);
	//分包
	public List<byte[]> byteList(String name) throws Exception;
	//实时数据查询
	public List<Map<String, Object>> queryActualData(Map<String, Object> map);
	//获取指定时间OneNET某参数的平均值
	public Double getAvg(Map<String, Object> map);
	//计算aqi
	public AirBean getAirBean(AirBean airBean,String devicenumber,String rmk1)throws Exception;
	
	//==================站点阈值
	//查询对象绑定的参数
	public List<Map<String, Object>> getobjParam(String objid);
	//添加阈值
	public void addThreshold(String objid,JSONArray json);
	//报警阈值查询
	public List<Map<String,Object>> queryThreshold(Map<String,Object> map);
	//删除报警阈值
	public void delThreshold(String thresholdid);
	//修改报警阈值
	public void updateThreshold(Map<String,Object> map);
	//报警判断
	public void isThreshold(AirBean airBean);
	//报警  查询
	public List<Map<String,Object>> queryThr(Map<String,Object> map);
	
	//站点管理添加
	public void addObj2Obj(Map<String,Object> map);
	//站点管理查询
	public List<Map<String, Object>> queryObj2Obj(Map<String,Object> map);
	//站点管理 删除
	public void delObj2Obj(Map<String,Object> map);
	//查询目录id
	public Map<String,Object> querycontrol(Map<String,Object> map);
	//目录添加
	public void addcontrol(Map<String,Object> map);
	//目录删除
	public void delcontrol(Map<String,Object> map);
	//目录结构查询
	public List<Map<String,Object>> queryMenujg();
	//获取指定结构目录列表
	public List<OmMenu> getMenuList(String dirtypeid);
	//添加目录结构
	public void addMenujg (Map<String,Object> map);
	//依据目录等级和结构查询目录
	public List<Map<String,Object>> queryMenuBylevel(Map<String,Object> map);
	//目录结构删除
	public void delMenujg(Map<String,Object> map);
	//目录名称修改
	public void updateMenuName(Map<String,Object> map);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//=========================对象类型表管理和字段管理================================

	/**
	 * 获取对象类型的信息表列表
	 * @param objtypeid 对象类型ID
	 * @param isused 是否删除
	 * @param tableshowname 表名称
	 * @param tabletype 表类型
	 */
	public List<Tableinfoadd> getTableListService(Map<String, Object> map);
	
	/**
	 * 对象类型表新增
	 * @param table Tableinfoadd对象
	 */
	public int tableinfoaddService(Tableinfoadd table);
	
	/**
	 * 对象类型表修改
	 * @param table Tableinfoadd对象
	 */
	public int tableinfoupdateService(Tableinfoadd table);
	
	/**
	 * 对象类型表批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param tableid 表id
	 */
	public int tableBatchRemoveAndRecoverService(Map<String, Object> map);
	
	/**
	 * 查看表的字段信息
	 * @param tableid 表信息id
	 * @param isused 是否删除
	 * @param fieldname 字段名称
	 * @param fieldshowname 字段显示名称
	 */
	public List<Tableinfo> getFieldinfoListService(Map<String, Object> map);
	
	/**
	 * 表字段信息新增
	 * @param table Tableinfo对象
	 */
	public int fieldinfoaddService(Tableinfo table);
	
	/**
	 * 表字段信息修改
	 * @param table Tableinfo对象
	 */
	public int fieldinfoupdateService(Tableinfo table);
	
	/**
	 * 表字段排序
	 * @param fieldid 字段id
	 * @param orderintable 排序值
	 * @param order 排序（降序/升序）
	 */
	public int fieldorderbyService(Map<String, Object> map);
	
	/**
	 * 字段批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param fieldid 字段id
	 */
	public int fieldBatchRemoveAndRecoverService(Map<String, Object> map);
	
	public int updateDir(Map<String, Object> map2);
}