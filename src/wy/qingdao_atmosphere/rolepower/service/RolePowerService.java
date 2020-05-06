package wy.qingdao_atmosphere.rolepower.service;

import java.util.List;


import wy.qingdao_atmosphere.rolepower.domain.Department;
import wy.qingdao_atmosphere.rolepower.domain.Menu;
import wy.qingdao_atmosphere.rolepower.domain.Role;
import wy.qingdao_atmosphere.rolepower.domain.RoleMenu;
import wy.qingdao_atmosphere.rolepower.domain.User;
import wy.qingdao_atmosphere.rolepower.domain.UserRole;
import wy.qingdao_atmosphere.rolepower.domain.UserOperateLog;


/**
 * 角色权限模块service层接口类
 * @author User
 *
 */
public interface RolePowerService {
	//登录
	public List<User> loginService(User user);
	
	//用户模块：添加用户
	public int addUserService(User user);
	
	//用户模块：修改用户
	public int updateUserService(User user);
	
	//用户模块：修改密码
	public int updatePasswordByUseridService(User user);
	
	//用户模块:查看所有用户
	public List<User> getAllUserService();
	
	//用户模块：综合查询
	public List<User> SearchUserService(User user);
	
	//用户模块：根据userid查看用户信息
	public List<User> getUserInfoByUseridService(String userid);
	
	//用户模块：删除用户
	public int deleteUserService(User user);
	
	//角色模块：添加一个角色
	public int addRoleService(Role role);
	
	//角色模块：查看所有角色
	public List<Role> getAllRoleService();
	
	//角色模块：根据role_id查看角色信息
	public List<Role> getRoleInfoByRoleidService(String roleid);
	
	//角色模块：删除角色
	public int deleteRoleService(Role role);
	
	//角色模块：修改角色信息
	public int updateRoleService(Role role);
	
	//角色模块：根据角色id查看菜单信息
	public List<Menu> getMenuInfoByRoleidService(String roleid);
	
	//菜单模块：查看所有的菜单
	public List<Menu> getAllMenuService();
	
	//菜单模块：添加菜单
	public int addMenuService(Menu menu);
	
	//菜单模块：修改菜单信息
	public int updateMenuService(Menu menu);
	
	//菜单模块：删除菜单
	public int deleteMenuService(Menu menu);
	
	//登录成功后：根据用户id去查对应的角色的对应的菜单
	public List<Menu> getMenuListByUseridService(String userid);
	
	//给用户赋予角色的时候，把之前的对应关系清空
	public int deleteUserRoleService(String userid);
	
	//给用户添加一个角色
	public int addUserRoleService(UserRole userrole);
	
	//给角色赋予对应的菜单
	public int addRoleMenuService(List<RoleMenu> rolemenulist);
	
	//给角色赋予权限的时候，把之前对应关系清空
	public int deleteRoleMenuService(String roleid);
	
	//部门模块：查看所有的部门信息
	public List<Department> getAllDepartmentService();
	
	//部门考核管理模块:异步查询所有部门名称
	public List<Department> ajaxDeptName();

	//tb_uim_useroperate_log日志管理，查询日志列表
	public List<UserOperateLog> getUserOperateLogListService(String keyword,String logtype,String begintime,String endtime);
}
