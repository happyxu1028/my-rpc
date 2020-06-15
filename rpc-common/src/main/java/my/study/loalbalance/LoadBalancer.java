package my.study.loalbalance;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import my.study.pojo.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-15 16:16
 */
public class LoadBalancer {


    /**
     * 返回可用机器的IP
     * @return
     */
    public static ServerInfo loadBalance(){
        Map.Entry<ServerInfo, Pair<Boolean, Long>> candidateServer = null;
        List<ServerInfo> candidateServerList = new ArrayList<>();

        for (Map.Entry<ServerInfo, Pair<Boolean, Long>> thisEntry : InvokeStatisticsCenter.statisticsMap.entrySet()) {
            Pair<Boolean, Long> pair = thisEntry.getValue();
            // 不可用,略过
            if(!pair.getKey()){
                continue;
            }

            // 第一次得到可用的服务器
            if(null == candidateServer){
                candidateServer = thisEntry;
                continue;
            }

            // 进行比较
            long offset = pair.getValue() - candidateServer.getValue().getValue();
            if(offset == 0){
                candidateServerList.add(thisEntry.getKey());
                continue;
            }else if (offset < 0 ){
                candidateServer = thisEntry;
            }
        }

        // 无可用机器
        if(candidateServer == null){
            return null;
        }

        // 没有同等响应时间的机器,返回候选者
        if(CollectionUtil.isEmpty(candidateServerList)){
            return candidateServer.getKey();
        }
        Random random  = new Random(candidateServerList.size());

        return candidateServerList.get(random.nextInt());
    }
}
