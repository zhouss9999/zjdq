package wy.qingdao_atmosphere.countrysitedata.domain;


/**
 * 数据源bean构建器
 */
public class DataSourceBean {
	
	
	private final String beanName;		//注册在spring中bean名字
	private final String driverClassName; 
	private final String url;
	private final String username;
	private final String password;
	private final String validationQuery;
	private final Boolean testOnBorrow;
	
	private final int maxWait = 10000; //若没设置 ，默认-1 将一直请求
	
	private final int removeAbandonedTimeout = 600;
	
	private final boolean logAbandoned  =true;  
	
	private final boolean  breakAfterAcquireFailure =true;  // 在请求失败以后是否中断
	
	private final  int connectionErrorRetryAttempts = 0;  //请求失败尝试次数
	
	
	

	public int getMaxWait() {
		return maxWait;
	}

	public int getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}

	public boolean isLogAbandoned() {
		return logAbandoned;
	}

	public boolean isBreakAfterAcquireFailure() {
		return breakAfterAcquireFailure;
	}

	public int getConnectionErrorRetryAttempts() {
		return connectionErrorRetryAttempts;
	}

	public String getBeanName() {
		return beanName;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public Boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public DataSourceBean(DataSourceBeanBuilder builder) {
		this.beanName = builder.getBeanName();
		this.driverClassName = builder.getDriverClassName();
		this.url = builder.getUrl();
		this.username = builder.getUsername();
		this.password = builder.getPassword();
		this.validationQuery = builder.getValidationQuery();
		this.testOnBorrow = builder.getTestOnBorrow();
	}

	public static class DataSourceBeanBuilder {
		private String beanName;
		private String driverClassName = "org.postgresql.Driver";
		private String url = "jdbc:postgresql://%s:%s/%s";
	    private String databaseIP;
	    private String databasePort;
	    private String databaseName;
		private String username;
		private String password;
		private String validationQuery = "select 1";
		private Boolean testOnBorrow = true;
		public DataSourceBeanBuilder(String beanName, String databaseIP, String databasePort, String databaseName,
				String username, String password) {
			super();
			this.beanName = beanName;
			this.databaseIP = databaseIP;
			this.databasePort = databasePort;
			this.databaseName = databaseName;
			this.username = username;
			this.password = password;
		}
		public DataSourceBeanBuilder() {
			super();
		}
		public DataSourceBeanBuilder driverClassName(String driverClassName) {
			this.driverClassName = driverClassName;
			return this;
		}
		public DataSourceBeanBuilder validationQuery(String validationQuery) {
			this.validationQuery = validationQuery;
			return this;
		}
		public DataSourceBeanBuilder testOnBorrow(Boolean testOnBorrow) {
			this.testOnBorrow = testOnBorrow;
			return this;
		}
		public String getUrl() {
			return String.format(url,this.databaseIP,this.databasePort,this.databaseName);
		}
		public String getBeanName() {
			return beanName;
		}
		public String getDriverClassName() {
			return driverClassName;
		}
		public String getDatabaseIP() {
			return databaseIP;
		}
		public String getDatabasePort() {
			return databasePort;
		}
		public String getDatabaseName() {
			return databaseName;
		}
		public String getUsername() {
			return username;
		}
		public String getPassword() {
			return password;
		}
		public String getValidationQuery() {
			return validationQuery;
		}
		public Boolean getTestOnBorrow() {
			return testOnBorrow;
		}
	}

	@Override
	public String toString() {
		return "DataSourceBean [beanName=" + beanName + ", driverClassName="
				+ driverClassName + ", url=" + url + ", username=" + username
				+ ", password=" + password + ", validationQuery="
				+ validationQuery + ", testOnBorrow=" + testOnBorrow
				+ ", maxWait=" + maxWait + ", removeAbandonedTimeout="
				+ removeAbandonedTimeout + ", logAbandoned=" + logAbandoned
				+ ", breakAfterAcquireFailure=" + breakAfterAcquireFailure
				+ ", connectionErrorRetryAttempts="
				+ connectionErrorRetryAttempts + "]";
	}

	
	
	

}
