package wy.qingdao_atmosphere.datacenter.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import wy.qingdao_atmosphere.datacenter.domain.Devicetype;
import wy.qingdao_atmosphere.datacenter.domain.Objtype;
import wy.qingdao_atmosphere.datacenter.domain.Param;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfo;
import wy.qingdao_atmosphere.datacenter.domain.Tableinfoadd;
import wy.qingdao_atmosphere.onemap.domain.OmMenu;



/**
 * 参数设备等管理接口实现
 * @author User
 *
 */
@Repository("dataCenterDao")
public class DataCenterDaoImpl extends SqlSessionDaoSupport implements DataCenterDao{
	
	//创建session工厂
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}
	
	//参数管理-查询列表
	public List<Param> getAllparameter(Map<String,Object> map) {
		return this.getSqlSession().selectList("getAllParam",map);
	}

	//参数管理-新增
	public void addparam(Param param) {
		this.getSqlSession().insert("addParam",param);
	}
	//参数管理 修改
	public void updateParam(Param param) {
		this.getSqlSession().update("updateParam",param);
	}
	//参数管理 删除或恢复
	public void delparam(Map<String,Object> map) {
		this.getSqlSession().update("delparamid",map);
	}
	//对象类型管理  查询
	public List<Objtype> queryObjtype(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryObjtype",map);
	}
	//对象类型管理 新增
	public void addObjtype(Objtype objtype) {
		this.getSqlSession().insert("addObjtype",objtype);
	}
	//对象类型管理 删除
	public void delObjtype(Map<String, Object> map) {
		this.getSqlSession().update("delObjtype",map);
	}
	//对象类型管理 修改 
	public void updateObjtype(Objtype objtype) {
		this.getSqlSession().update("updateObjtype",objtype);
	}
	//对象安装设备 查询
	public List<Map<String, Object>> queryObjDevice(Map<String,Object> map){
		return this.getSqlSession().selectList("queryObjInfo",map);
	}
	//对象安装设备 参数关联查询
	public List<Map<String, Object>> queryDeviceInfo(String devicenumber){
		return this.getSqlSession().selectList("queryDeviceInfo",devicenumber);
	}
	//对象安装设备 安装
	public void addObjinfo(List<Map<String, Object>> list){
		this.getSqlSession().insert("addObjinfo",list);
	}
	//对象安装设备 查询是否存在卸载过得数据
	public List<Map<String, Object>> queryObjNotUsed(Map<String, Object> map){
		return this.getSqlSession().selectList("queryObjNotUsed",map);
	}
	
	//对象安装设备 恢复
	public void recoveryObj(String conectids){
		this.getSqlSession().update("recoveryObj",conectids);
	}
		
	//对象安装设备 安装页 设备查询
	public List<Map<String, Object>> querydeviceName(Map<String, Object> map){
		return this.getSqlSession().selectList("querydeviceName",map);
	}
	//对象安装设备 卸载
	public void delObjinfo(Map<String, Object> map){
		this.getSqlSession().update("delObjinfo",map);
	}
	
	//设备类型管理 查询
	public List<Devicetype> queryDevicetypes(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDevicetype",map);
	}
	//设备类型管理 添加
	public void addDevicetype(Devicetype devicetype) {
		this.getSqlSession().insert("addDevicetype",devicetype);
	}
	//设备类型管理 修改
	public void updateDevicetype(Devicetype devicetype) {
		this.getSqlSession().update("updateDevicetype",devicetype);
	}
	//设备类型管理 删除
	public void delDeviceype(Map<String, Object> map) {
		this.getSqlSession().update("delDevicetype",map);
	}
	
	//设备版本添加
	public void fileupload(Map<String,Object> map){
		this.getSqlSession().insert("addDevVersion",map);
	}
	//设备版本查询
	public List<Map<String,Object>> queryDevVersion(Map<String,Object> map){
		return this.getSqlSession().selectList("queryDevVersion",map);
	}
	//设备版本 修改
	public void updateDevVersion(Map<String,Object> map){
		this.getSqlSession().update("updateDevVersion",map);
	}
	//设备版本删除
	public void delDevVersion(String verid){
		this.getSqlSession().delete("delDevVersion",verid);
	}
	
	
	//设备管理 查询
	public List<Map<String, Object>> queryDevice(Map<String, Object> map) {
		return this.getSqlSession().selectList("queryDevice",map);
	}
	//设备管理 列表查询条件  设备类型查询
	public List<Map<String, Object>> queryDevicetypeMap() {
		return this.getSqlSession().selectList("querydevicetypemap");
	}
	//设备管理 列表查询条件  设备名称查询
	public List<Map<String, Object>> queryDeviceNameMap(int devicetypeid) {
		return this.getSqlSession().selectList("queryDeviceNameMap",devicetypeid);
	}
	//设备管理  添加
	public void addDevice(Map<String, Object> map) {
		this.getSqlSession().insert("addDevice",map);
	}
	//设备管理 修改
	public int updateDevice(Map<String, Object> map){
		return this.getSqlSession().update("updateDevice",map);
	}
	//设备管理 删除或恢复
	public void delDevice(Map<String,Object> map){
		this.getSqlSession().update("delDevice",map);
	}
	//设备参数管理  列表查询
	public List<Map<String, Object>> queryDeviceParam(Map<String, Object> map){
		return this.getSqlSession().selectList("queryDevicetypeParam",map);
	}
	//设备参数管理  添加 参数查询
	public List<Map<String, Object>> queryParambydeviceType(Map<String, Object> map){
		return this.getSqlSession().selectList("queryParambyDT", map);
	}
	//设备参数管理  添加
	public void addDeviceTypeParam(Map<String, Object> map){
		this.getSqlSession().insert("addDeviceTypeParam",map);
	}
	//设备参数管理  修改页查询
	public List<Map<String, Object>> queryDP(int devicetypeid){
		return this.getSqlSession().selectList("queryDP",devicetypeid);
	}
	//设备参数管理  删除或恢复
	public void delDeviceTypeParam(Map<String, Object> map){
		this.getSqlSession().update("deldevicetypeparam",map);
	}
	//查询
	public List<Map<String, Object>> queryData(String sql,Map<String, Object> map){
		return this.getSqlSession().selectList(sql,map);
	}
	//对象管理   添加
	public void addObj(Map<String, Object> map){
		this.getSqlSession().insert("addObj",map);
	}
	//获取地理位置信息
	public Map<String, Object> queryobjdt(Map<String, Object> map){
		return this.getSqlSession().selectOne("queryobjdt", map); 
	}
	//修改地理位置
	public void updateobjdt(Map<String, Object> map){
		this.getSqlSession().update("updateobjdt",map);
	}
	//添加地理位置
	public void addobjdt(Map<String, Object> map){
		this.getSqlSession().insert("addobjdt",map);
	}
	//对象管理 查询空间表名
	public Map<String, Object> querySpace(Map<String, Object> map){
		return this.getSqlSession().selectOne("querySpace", map);
	}
	//对象管理 基本信息维护
	public void updateInfostore(List<Map<String, Object>> list){
		this.getSqlSession().update("updateInfostore",list);
	}
	//对象管理 基本信息维护
	public void updateZBInfostore(List<Map<String, Object>> list){
		this.getSqlSession().update("updateZBInfostore",list);
	}
	//对象管理 基本信息维护
	public void insertInfostore(List<Map<String, Object>> list){
		this.getSqlSession().insert("insertInfostore",list);
	}
	//对象管理 基本信息维护
	public void insertZBInfostore(List<Map<String, Object>> list){
		this.getSqlSession().insert("insertZBInfostore",list);
	}
	//对象管理 基本表字段查询
	public List<Map<String, Object>> queryFieldName(String objid){
		return this.getSqlSession().selectList("queryFieldName",objid);
	}
	//对象管理 删除或恢复
	public void delObj(Map<String, Object> map){
		this.getSqlSession().update("delObj",map);
	}
	//保存实时数据
	public void addwzData(Map<String, Object> map){
		this.getSqlSession().insert("addwzData",map);
	}
	//查询数据是否存在
	public List<Map<String, Object>> querywzDatas(Map<String, Object> map){
		return this.getSqlSession().selectList("querywzDatas",map);
	}
	//触发器修改实时数据
	public void updateTriggerData(Map<String, Object> map){
		this.getSqlSession().insert("updateTriggerData",map);
	}
	//查询对象绑定的参数
	public List<Map<String, Object>> getobjParam(String objid){
		return this.getSqlSession().selectList("getobjParam", objid);
	}
	//添加阈值
	public void addThreshold(Map<String, Object> map){
		this.getSqlSession().insert("addThreshold",map);
	}
	//报警阈值查询
	public List<Map<String,Object>> queryThreshold(Map<String,Object> map){
		return this.getSqlSession().selectList("queryThreshold",map);
	}
	//删除报警阈值
	public void delThreshold(String thresholdid){
		this.getSqlSession().delete("delThreshold",thresholdid);
	}
	//修改报警阈值
	public void updateThreshold(Map<String,Object> map){
		this.getSqlSession().delete("updateThreshold",map);
	}
	//对象管理  子表下拉查询
	public List<Map<String,Object>> queryObjZB(String objtypeid){
		return this.getSqlSession().selectList("queryObjZB",objtypeid);
	}
	//报警  新增
	public void addthrinfo(Map<String,Object> map){
		this.getSqlSession().insert("addthrinfo",map);
	}
	//报警  查询
	public List<Map<String,Object>> queryThr(Map<String,Object> map){
		return this.getSqlSession().selectList("queryThr",map);
	}
	
	//站点管理添加
	public void addObj2Obj(Map<String,Object> map){
		this.getSqlSession().insert("addObj2Obj",map);
	}
	//站点管理 删除
	public void delObj2Obj(Map<String,Object> map){
		this.getSqlSession().delete("delObj2Obj",map);
	}
	//查询目录id
	public Map<String,Object> querycontrol(Map<String,Object> map){
		return this.getSqlSession().selectOne("querycontrol",map);
	}
	//目录添加
	public void addcontrol(Map<String,Object> map){
		this.getSqlSession().insert("addcontrol",map);
	}
	//目录删除
	public void delcontrol(Map<String,Object> map){
		this.getSqlSession().delete("delcontrol",map);
	}
	//目录结构查询
	public List<Map<String,Object>> queryMenujg(){
		return this.getSqlSession().selectList("queryMenujg");
	}
	//目录列表
	public List<OmMenu> getMenuList(String dirtypeid) {
		return this.getSqlSession().selectList("getMenuList",dirtypeid);
	}
	//添加目录结构
	public Map<String,Object> addMenujg (Map<String,Object> map){
		return this.getSqlSession().selectOne("addMenujg",map);
	}
	//依据目录等级和结构查询目录
	public List<Map<String,Object>> queryMenuBylevel(Map<String,Object> map){
		return this.getSqlSession().selectList("queryMenuBylevel",map);
	}
	//目录结构删除
	public void delMenujg(Map<String,Object> map){
		this.getSqlSession().update("delMenujg",map);
	}
	//目录名称修改
	public void updateMenuName(Map<String,Object> map){
		this.getSqlSession().update("updateMenuName",map);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//=========================对象类型表管理和字段管理================================
	
	/**
	 * 获取对象类型的信息表列表/查询功能
	 * @param objtypeid 对象类型ID
	 * @param isused 是否删除
	 * @param tableshowname 表名称
	 * @param tabletype 表类型
	 */
	public List<Tableinfoadd> getTableList(Map<String, Object> map){
		return this.getSqlSession().selectList("getTableList", map);
	}
	
	/**
	 * 对象类型表新增
	 * @param table Tableinfoadd对象
	 */
	public int tableinfoadd(Tableinfoadd table){
		return this.getSqlSession().insert("tableinfoadd", table);
	}
	
	/**
	 * 对象类型表修改
	 * @param table Tableinfoadd对象
	 */
	public int tableinfoupdate(Tableinfoadd table){
		return this.getSqlSession().update("tableinfoupdate", table);
	}
	
	/**
	 * 对象类型表批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param tableid 表id
	 */
	public int tableBatchRemoveAndRecover(Map<String, Object> map){
		return this.getSqlSession().update("tableBatchRemoveAndRecover", map);
	}
	
	/**
	 * 查看表的字段信息
	 * @param tableid 表信息id
	 * @param isused 是否删除
	 * @param fieldname 字段名称
	 * @param fieldshowname 字段显示名称
	 */
	public List<Tableinfo> getFieldinfoList(Map<String, Object> map){
		return this.getSqlSession().selectList("getFieldinfoList", map);
	}
	
	/**
	 * 表字段信息新增
	 * @param table Tableinfo对象
	 */
	public int fieldinfoadd(Tableinfo table){
		return this.getSqlSession().insert("fieldinfoadd", table);
	}
	
	/**
	 * 表字段信息修改
	 * @param table Tableinfo对象
	 */
	public int fieldinfoupdate(Tableinfo table){
		return this.getSqlSession().update("fieldinfoupdate", table);
	}
	
	/**
	 * 表字段排序
	 * @param fieldid 字段id
	 * @param orderintable 排序值
	 * @param order 排序（降序/升序）
	 */
	public int fieldorderby(Map<String, Object> map){
		return this.getSqlSession().update("fieldorderby", map);
	}
	
	/**
	 * 字段批量删除/恢复
	 * @param state 状态：0：删除  1：恢复
	 * @param fieldid 字段id
	 */
	public int fieldBatchRemoveAndRecover(Map<String, Object> map){
		return this.getSqlSession().update("fieldBatchRemoveAndRecover", map);
	}

	public int updateDir(Map<String, Object> map2) {
		return this.getSqlSession().update("updateDir", map2);
	}
}
