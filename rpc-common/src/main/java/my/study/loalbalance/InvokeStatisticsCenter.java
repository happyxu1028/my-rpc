package my.study.loalbalance;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import my.study.pojo.ServerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:服务调用统计
 * @Author: 长灵
 * @Date: 2020-06-14 15:35
 */
public class InvokeStatisticsCenter {

    /**
     * 装载调用信息的容器
     * key: 机器IP
     * value.left:  是否可用
     * value.right: 更新时间
     */
    public static Map<ServerInfo, Pair<Boolean, Long>> statisticsMap = new HashMap<>();

    private static List<ServerInfo> previousServerList = null;

    /**
     * 服务统计
     *
     * @param apiInvokeInfo
     */
    public static synchronized void statistics(ApiInvokeInfo apiInvokeInfo) {
        ServerInfo targetServer = apiInvokeInfo.getTargetServer();
        Pair<Boolean, Long> serverInfo = null;
        long handTime = apiInvokeInfo.getInvokeResponseTime().getTime() - apiInvokeInfo.getInvokeRequestTime().getTime();
        DateTime limitTime = DateUtil.offsetMinute(apiInvokeInfo.getInvokeRequestTime(), 5);
        if (handTime <= 1000L*5L) {
            serverInfo = new Pair<>(true, handTime);
        } else {
            serverInfo = new Pair<>(false, handTime);
        }
        statisticsMap.put(targetServer, serverInfo);
        System.out.println(">>>> 当前服务调用统计: "+ JSON.toJSONString(statisticsMap)+"\r\n");
    }


    /**
     * 移除下线机器
     *
     * @param serverInfo
     */
    public void removeOfflineServer(ServerInfo serverInfo) {
        statisticsMap.remove(serverInfo);
    }


    /**
     * 初始化
     *
     * @param providerInfoList
     */
    public static void initServer(List<ServerInfo> providerInfoList) {

        if (null == previousServerList) {
            // 初始化到服务注册
            for (ServerInfo providerInfo : providerInfoList) {
                System.out.println(">>>> 负载均衡 | 初始化候选服务器: "+providerInfo.getIp()+":"+providerInfo.getPort());
                statisticsMap.put(providerInfo, new Pair<Boolean, Long>(true, 0L));
            }

        } else {

            // 下线机器 (上一个版本的数据 - 现在版本的数据)
            List<ServerInfo> offlineServer = previousServerList.stream().filter(item -> !providerInfoList.contains(item)).collect(Collectors.toList());
            if (!CollectionUtil.isEmpty(offlineServer)) {

                offlineServer.forEach(t -> {
                    System.out.println(">>>> 负载均衡 | 移除候选服务器: "+t.getIp()+":"+t.getPort());
                    statisticsMap.remove(t);

                });
            }

            // 上线机器
            List<ServerInfo> newOnlineServer = providerInfoList.stream().filter(item -> !previousServerList.contains(item)).collect(Collectors.toList());
            if (!CollectionUtil.isEmpty(newOnlineServer)) {
                newOnlineServer.forEach(t -> {
                    System.out.println(">>>> 负载均衡 | 添加候选服务器: "+t.getIp()+":"+t.getPort());
                    statisticsMap.put(t, new Pair<>(true, 0L));
                });
            }
        }
        previousServerList = providerInfoList;
        System.out.println(">>>> 当前候选服务器信息: "+ JSON.toJSONString(statisticsMap)+"\r\n");
    }

}
