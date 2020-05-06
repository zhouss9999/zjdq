package wy.qingdao_atmosphere.countrysitedata.domain;

public class FsjFtpParam {
	
	private String id;
	
	private String objid;
	
	private String ip;
	
	private String port;
	
	private String username ;
	
	private String password ;
	
	private String sitenumber ;
	
	private String filepath;
	
	private String objtypeid;
	
	private String isused;
	
	
	
	

	public String getObjtypeid() {
		return objtypeid;
	}

	public void setObjtypeid(String objtypeid) {
		this.objtypeid = objtypeid;
	}

	public String getIsused() {
		return isused;
	}

	public void setIsused(String isused) {
		this.isused = isused;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObjid() {
		return objid;
	}

	public void setObjid(String objid) {
		this.objid = objid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSitenumber() {
		return sitenumber;
	}

	public void setSitenumber(String sitenumber) {
		this.sitenumber = sitenumber;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public FsjFtpParam() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "FsjFtpParam [id=" + id + ", objid=" + objid + ", ip=" + ip
				+ ", port=" + port + ", username=" + username + ", password="
				+ password + ", sitenumber=" + sitenumber + ", filepath="
				+ filepath + "]";
	}
	
	
	

}
