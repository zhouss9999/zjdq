package wy.qingdao_atmosphere.countrysitedata.domain;


/**
 * 服务器信息
 * @author Lenovo
 *
 */
public class WebServer {
	
	private Integer id; 
	
	private Integer objid;
	
	private String ip;
	
	private Integer port;
	
	private String name;
	
	private String url;
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getObjid() {
		return objid;
	}

	public void setObjid(Integer objid) {
		this.objid = objid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	
	
	
	
	

}
