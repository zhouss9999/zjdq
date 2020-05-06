package wy.qingdao_atmosphere.devicecontrol.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface DevService {
    /**
     * 操作设备电源  打开（1）或关闭（0）
     * @param request
     * @return
     */
	Map<String, Object> updatePower(HttpServletRequest request);

	
	/**
	 * 查询设备电源状态
	 * @param request
	 * @return
	 */
	Map<String, Object> queryPower(HttpServletRequest request);

    /**
     * 鼓风机/接收机风扇控制
     * @param request
     * @return
     */
	Map<String, Object> queryFans(HttpServletRequest request);

    
	/**
	 * 伺服角度查询
	 * @param request
	 * @return
	 */
	Map<String, Object> queryServo(HttpServletRequest request);


	/**
	 * 查询当前设备工作状态
	 * @param request
	 * @return
	 */
	Map<String, Object> queryWorkMode(HttpServletRequest request);

    /**
     * 获取日志文件
     * @param request
     * @return
     */
	Map<String, Object> queryLogs(HttpServletRequest request);

	/**
	 * 鼓风机/接收机风扇控制
	 * @param request
	 * @return
	 */
	Map<String, Object> updateFans(HttpServletRequest request);

    
	/**
	 * 伺服控制
	 * @param request
	 * @return
	 */
	Map<String, Object> updateServo(HttpServletRequest request);

	/**
	 * 定向观测
	 * @param request
	 * @return
	 */
	Map<String, Object> updateDirectObs(HttpServletRequest request);

	/**
	 * 扫描观测
	 * @param request
	 * @return
	 */
	Map<String, Object> scanObs(HttpServletRequest request);

	/**
	 * 定标
	 * @param request
	 * @return
	 */
	Map<String, Object> calibrate(HttpServletRequest request);

    /**
     * 远程升级
     * @param request
     * @return
     */
	Map<String, Object> update(HttpServletRequest request);

}
