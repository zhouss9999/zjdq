package wy.qingdao_atmosphere.rolepower.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import wy.qingdao_atmosphere.rolepower.dao.RolePowerDao;
import wy.qingdao_atmosphere.rolepower.domain.Department;
import wy.qingdao_atmosphere.rolepower.domain.Menu;
import wy.qingdao_atmosphere.rolepower.domain.Role;
import wy.qingdao_atmosphere.rolepower.domain.RoleMenu;
import wy.qingdao_atmosphere.rolepower.domain.User;
import wy.qingdao_atmosphere.rolepower.domain.UserOperateLog;
import wy.qingdao_atmosphere.rolepower.domain.UserRole;


/**
 * 角色权限模块service层接口实现类
 * @author User
 *
 */
@Service("rolepowerService")
public class RolePowerServiceImpl implements RolePowerService {
	private RolePowerDao rolepowerDao;

	public RolePowerDao getRolepowerDao() {
		return rolepowerDao;
	}
	@Resource
	public void setRolepowerDao(RolePowerDao rolepowerDao) {
		this.rolepowerDao = rolepowerDao;
	}
	
	//登录
	public List<User> loginService(User user) {
		return rolepowerDao.login(user);
	}
	
	//用户模块：添加用户
	public int addUserService(User user) {
		return rolepowerDao.addUser(user);
	}
	
	//用户模块：修改用户
	public int updateUserService(User user) {
		return rolepowerDao.updateUser(user);
	}
	
	//用户模块：修改密码
	public int updatePasswordByUseridService(User user) {
		return rolepowerDao.updatePasswordByUserid(user);
	}
	
	//用户模块:查看所有用户
	public List<User> getAllUserService() {
		return rolepowerDao.getAllUser();
	}
	
	//用户模块：综合查询
	public java.util.List<User> SearchUserService(User user) {
		return rolepowerDao.SearchUser(user);
	}
	
	//用户模块：根据userid查看用户信息
	public List<User> getUserInfoByUseridService(String userid){
		return rolepowerDao.getUserInfoByUserid(userid);
	}
	
	//用户模块：删除用户
	public int deleteUserService(User user) {
		return rolepowerDao.deleteUser(user);
	}
	
	//角色模块：添加一个角色
	public int addRoleService(Role role) {
		return rolepowerDao.addRole(role);
	}
	
	//角色模块：查看所有角色
	public List<Role> getAllRoleService(){
		return rolepowerDao.getAllRole();
	}
	
	//角色模块：根据role_id查看角色信息
	public java.util.List<Role> getRoleInfoByRoleidService(String roleid) {
		return rolepowerDao.getRoleInfoByRoleid(roleid);
	}
	
	//角色模块：删除角色
	public int deleteRoleService(Role role){
		return rolepowerDao.deleteRole(role);
	}
	
	//角色模块：修改角色信息
	public int updateRoleService(Role role){
		return rolepowerDao.updateRole(role);
	}
	
	//角色模块：根据角色id查看菜单信息
	public java.util.List<Menu> getMenuInfoByRoleidService(String roleid) {
		return rolepowerDao.getMenuInfoByRoleid(roleid);
	}
	
	//菜单模块：查看所有的菜单
	public List<Menu> getAllMenuService(){
		return rolepowerDao.getAllMenu();
	}
	
	//菜单模块：添加菜单
	public int addMenuService(Menu menu){
		return rolepowerDao.addMenu(menu);
	}
	
	//菜单模块：修改菜单信息
	public int updateMenuService(Menu menu){
		return rolepowerDao.updateMenu(menu);
	}
	
	//菜单模块：删除菜单
	public int deleteMenuService(Menu menu){
		return rolepowerDao.deleteMenu(menu);
	}
	
	//登录成功后：根据用户id去查对应的角色的对应的菜单
	public List<Menu> getMenuListByUseridService(String userid) {
		return rolepowerDao.getMenuListByUserid(userid);
	}
	
	//给用户赋予角色的时候，把之前的对应关系清空
	public int deleteUserRoleService(String userid) {
		return rolepowerDao.deleteUserRole(userid);
	}
	
	//给用户添加一个角色
	public int addUserRoleService(UserRole userrole){
		return rolepowerDao.addUserRole(userrole);
	}
	
	//给角色赋予对应的菜单
	public int addRoleMenuService(List<RoleMenu> rolemenulist){
		return rolepowerDao.addRoleMenu(rolemenulist);
	}
	
	//给角色赋予权限的时候，把之前对应关系清空
	public int deleteRoleMenuService(String roleid) {
		return rolepowerDao.deleteRoleMenu(roleid);
	}
	
	//部门模块：查看所有的部门信息
	public List<Department> getAllDepartmentService() {
		return rolepowerDao.getAllDepartment();
	}
	
	//部门考核管理模块:异步查询所有部门名称
	public List<Department> ajaxDeptName() {
		return rolepowerDao.ajaxDeptName();
	}
	
	//tb_uim_useroperate_log日志管理，查询日志列表
	public List<UserOperateLog> getUserOperateLogListService(String keyword,String logtype,String begintime,String endtime){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("keyword", keyword);
		map.put("logtype", logtype);
		map.put("begintime", begintime);
		map.put("endtime", endtime);
		return rolepowerDao.getUserOperateLogList(map);
	}
}
