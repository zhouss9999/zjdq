package wy.qingdao_atmosphere.rolepower.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import wy.qingdao_atmosphere.rolepower.domain.Department;
import wy.qingdao_atmosphere.rolepower.domain.Menu;
import wy.qingdao_atmosphere.rolepower.domain.Role;
import wy.qingdao_atmosphere.rolepower.domain.RoleMenu;
import wy.qingdao_atmosphere.rolepower.domain.User;
import wy.qingdao_atmosphere.rolepower.domain.UserRole;
import wy.qingdao_atmosphere.rolepower.domain.UserOperateLog;


/**
 * 角色权限模块dao层接口实现类
 * @author User
 *
 */
@Repository("rolepowerDao")
public class RolePowerDaoImpl extends SqlSessionDaoSupport implements RolePowerDao{
	//创建session工厂
	@Resource
	public void setSuperSessionFactory(SqlSessionFactory sessionFactory) {
		this.setSqlSessionFactory(sessionFactory);
	}
	
	//登录
	public List<User> login(User user) {
		return this.getSqlSession().selectList("login",user);
	}
	
	//用户模块：添加用户
	public int addUser(User user) {
		return this.getSqlSession().insert("addUser",user);
	}
	
	//用户模块：修改用户
	public int updateUser(User user) {
		return this.getSqlSession().update("updateUser", user);
	}
	
	//用户模块：修改密码
	public int updatePasswordByUserid(User user) {
		return this.getSqlSession().update("updatePasswordByUserid",user);
	}
	
	//用户模块:查看所有用户
	public List<User> getAllUser() {
		return this.getSqlSession().selectList("getAllUser");
	}
	
	//用户模块：综合查询
	public java.util.List<User> SearchUser(User user) {
		return this.getSqlSession().selectList("SearchUser",user);
	}
	
	//用户模块：根据userid查看用户信息
	public List<User> getUserInfoByUserid(String userid) {
		return this.getSqlSession().selectList("getUserInfoByUserid",userid);
	}
	
	//用户模块：删除用户
	public int deleteUser(User user) {
		return this.getSqlSession().delete("deleteUser",user);
	}
	
	//角色模块：添加一个角色
	public int addRole(Role role) {
		return this.getSqlSession().insert("addRole", role);
	}
	
	//角色模块：查看所有角色
	public List<Role> getAllRole() {
		return this.getSqlSession().selectList("getAllRole");
	}
	
	//角色模块：根据role_id查看角色信息
	public java.util.List<Role> getRoleInfoByRoleid(String roleid) {
		return this.getSqlSession().selectList("getRoleInfoByRoleid",roleid);
	}
	
	//角色模块：删除角色
	public int deleteRole(Role role) {
		return this.getSqlSession().update("deleteRole",role);
	}
	
	//角色模块：修改角色信息
	public int updateRole(Role role) {
		return this.getSqlSession().update("updateRole", role);
	}
	
	//角色模块：根据角色id查看菜单信息
	public java.util.List<Menu> getMenuInfoByRoleid(String roleid) {
		return this.getSqlSession().selectList("getMenuInfoByRoleid",roleid);
	}
	
	//菜单模块：查看所有的菜单
	public List<Menu> getAllMenu() {
		return this.getSqlSession().selectList("getAllMenu");
	}
	
	//菜单模块：添加菜单
	public int addMenu(Menu menu) {
		return this.getSqlSession().insert("addMenu",menu);
	}
	
	//菜单模块：修改菜单信息
	public int updateMenu(Menu menu) {
		return this.getSqlSession().update("updateMenu",menu);
	}
	
	//菜单模块：删除菜单
	public int deleteMenu(Menu menu) {
		return this.getSqlSession().update("deleteMenu",menu);
	}
	
	//登录成功后：根据用户id去查对应的角色的对应的菜单
	public List<Menu> getMenuListByUserid(String userid) {
		return this.getSqlSession().selectList("getMenuListByUserid",userid);
	}
	
	//给用户赋予角色的时候，把之前的对应关系清空
	public int deleteUserRole(String userid) {
		return this.getSqlSession().delete("deleteUserRole",userid);
	}
	
	//给用户添加一个角色
	public int addUserRole(UserRole userrole) {
		return this.getSqlSession().insert("addUserRole",userrole);
	}
	
	//给角色赋予对应的菜单
	public int addRoleMenu(List<RoleMenu> rolemenulist) {
		return this.getSqlSession().insert("addRoleMenu",rolemenulist);
	}
	
	//给角色赋予权限的时候，把之前对应关系清空
	public int deleteRoleMenu(String roleid) {
		return this.getSqlSession().delete("deleteRoleMenu",roleid);
	}
	
	//部门模块：查看所有的部门信息
	public List<Department> getAllDepartment() {
		return this.getSqlSession().selectList("getAllDepartment");
	}
	
	//部门考核管理模块：异步查询所有部门名称
	public List<Department> ajaxDeptName() {
		return this.getSqlSession().selectList("ajaxDeptName");
	}
	
	//tb_uim_useroperate_log日志管理，查询日志列表
	public List<UserOperateLog> getUserOperateLogList(Map<String, Object> map){
		return this.getSqlSession().selectList("getUserOperateLogList",map);
	}
}
