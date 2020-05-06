package wy.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import wy.qingdao_atmosphere.countrysitedata.dao.SiteDataDao;
import wy.qingdao_atmosphere.countrysitedata.domain.CollectParam;

public class ListUtils {
	
	
	
	@Autowired
	static private SiteDataDao sDao;
	
	
	 /**
     * .
     * 分批插入实时监测数据
     * TODO 递归:分割长List为 subNum/段。
     * @param thesisList 论文list(总)
     * @param subNum 每段长度 (最小1)
     * @return
     * @throws Exception
     */
    public static  int recurSub(List<CollectParam> thesisList,int subNum,Integer baseObjid) throws Exception{
        //参数合法性判断:
        if(thesisList.isEmpty()) return 0;
        if(subNum<1) return 0;

        //大于subNum，进入分割
        if(thesisList.size() > subNum) {// && !(thesisList.isEmpty())
            //将前subNum分出来，直接插入到数据库。
            List<CollectParam> toInsert = thesisList.subList(0, subNum);
            //将subNum至最后 (剩余部分) 继续进行递归分割
            List<CollectParam> toRecurSub = thesisList.subList(subNum, thesisList.size());
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("list", toInsert);
			//int i = siteService.insertCollect(cList);
			map.put("objid", baseObjid);
            //将前subNum分出来，直接插入到数据库 && 将subNum至最后 (剩余部分) 继续进行递归分割 。统计数量
            return sDao.insertCollect(map) + recurSub(toRecurSub,subNum,baseObjid);
            
        //少于subNum，直接插入数据库 (递归出口)
        }else {
        	
        	Map<String,Object> map = new HashMap<String,Object>();
            map.put("list", thesisList);
			//int i = siteService.insertCollect(cList);
			map.put("objid", baseObjid);
            //插入到数据库。统计数量
        	return sDao.insertCollect(map);
        }
    }


}
