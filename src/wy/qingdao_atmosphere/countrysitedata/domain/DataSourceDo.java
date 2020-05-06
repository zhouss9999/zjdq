package wy.qingdao_atmosphere.countrysitedata.domain;

public class DataSourceDo {
	
	private Integer id;
	
	private String dataSourceName; //数据源别名v
	
	private String  databaseIp;   //ip
	
	private String  databasePort;     //端口
	
	private String databaseName;   //数据库名
	
	private String databaseUsername;  //用户名
	
	private String password; //密码
	
    private String databaseType;   //数据库类型   1-辐射计数据库   2-风廓线数据库
	
	private int isused;      //是否使用
	
	
	
	
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public int getIsused() {
		return isused;
	}

	public void setIsused(int isused) {
		this.isused = isused;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getDatabaseIp() {
		return databaseIp;
	}

	public void setDatabaseIp(String databaseIp) {
		this.databaseIp = databaseIp;
	}

	public String getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DataSourceDo() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "DataSourceDo [id=" + id + ", dataSourceName=" + dataSourceName
				+ ", databaseIp=" + databaseIp + ", databasePort="
				+ databasePort + ", databaseName=" + databaseName
				+ ", databaseUsername=" + databaseUsername + ", password="
				+ password + ", databaseType=" + databaseType + ", isused="
				+ isused + "]";
	}

	

	
	
	
	
	

}
