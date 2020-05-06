package wy.qingdao_atmosphere.rolepower.domain;

import javax.persistence.Entity;
/**
 * 用户信息
 * @author User
 *
 */
@Entity
public class User {
	private String userid;   //账号
	private String pwd;		 //密码
	private String menmo;	 //备注
	private String add_time;
	private int islock;		 //是否显示
	private String system_key;
	private String ca;
	private String orgid;	 //部门key
	private String username; //姓名
	private String email;	 //邮箱
	private String phone;	 //移动电话
	private int isportaluser;
	private int fcountycode;
	private String sex;		 //性别
	private String birth;	 //生日
	private String cerno;
	private String fax;
	private int showindex;	 //排序
	private String duty;
	private String telephone;//办公室电话
	private String family_address;//家庭住址
	private String company_address;//公司地址
	private String isadd;
	private String iscomm;
	private String theme_id;
	private String mc;		 //部门名称
	private String role_id;  //所属角色id
	private String role_name;//所属角色
	private String usernum;  //用户编号
	private String headimage_path; //头像地址
	private String job;  //职务
	private String cornet;  //短号
	private String zfznum;  //执法证编号
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getMenmo() {
		return menmo;
	}
	public void setMenmo(String menmo) {
		this.menmo = menmo;
	}
	public String getAdd_time() {
		return add_time;
	}
	public void setAdd_time(String add_time) {
		this.add_time = add_time;
	}
	public int getIslock() {
		return islock;
	}
	public void setIslock(int islock) {
		this.islock = islock;
	}
	public String getSystem_key() {
		return system_key;
	}
	public void setSystem_key(String system_key) {
		this.system_key = system_key;
	}
	public String getCa() {
		return ca;
	}
	public void setCa(String ca) {
		this.ca = ca;
	}
	public String getOrgid() {
		return orgid;
	}
	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getIsportaluser() {
		return isportaluser;
	}
	public void setIsportaluser(int isportaluser) {
		this.isportaluser = isportaluser;
	}
	public int getFcountycode() {
		return fcountycode;
	}
	public void setFcountycode(int fcountycode) {
		this.fcountycode = fcountycode;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getBirth() {
		return birth;
	}
	public void setBirth(String birth) {
		this.birth = birth;
	}
	public String getCerno() {
		return cerno;
	}
	public void setCerno(String cerno) {
		this.cerno = cerno;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public int getShowindex() {
		return showindex;
	}
	public void setShowindex(int showindex) {
		this.showindex = showindex;
	}
	public String getDuty() {
		return duty;
	}
	public void setDuty(String duty) {
		this.duty = duty;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getFamily_address() {
		return family_address;
	}
	public void setFamily_address(String family_address) {
		this.family_address = family_address;
	}
	public String getCompany_address() {
		return company_address;
	}
	public void setCompany_address(String company_address) {
		this.company_address = company_address;
	}
	public String getIsadd() {
		return isadd;
	}
	public void setIsadd(String isadd) {
		this.isadd = isadd;
	}
	public String getIscomm() {
		return iscomm;
	}
	public void setIscomm(String iscomm) {
		this.iscomm = iscomm;
	}
	public String getTheme_id() {
		return theme_id;
	}
	public void setTheme_id(String theme_id) {
		this.theme_id = theme_id;
	}
	public String getMc() {
		return mc;
	}
	public void setMc(String mc) {
		this.mc = mc;
	}
	public String getRole_name() {
		return role_name;
	}
	public void setRole_name(String role_name) {
		this.role_name = role_name;
	}
	public String getUsernum() {
		return usernum;
	}
	public void setUsernum(String usernum) {
		this.usernum = usernum;
	}
	public String getHeadimage_path() {
		return headimage_path;
	}
	public void setHeadimage_path(String headimage_path) {
		this.headimage_path = headimage_path;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getCornet() {
		return cornet;
	}
	public void setCornet(String cornet) {
		this.cornet = cornet;
	}
	public String getZfznum() {
		return zfznum;
	}
	public void setZfznum(String zfznum) {
		this.zfznum = zfznum;
	}
	public String getRole_id() {
		return role_id;
	}
	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}
}
