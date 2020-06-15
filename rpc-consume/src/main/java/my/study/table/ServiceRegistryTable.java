package my.study.table;

import lombok.Getter;
import my.study.loalbalance.InvokeStatisticsCenter;
import my.study.pojo.ApiProvidersInfo;
import my.study.pojo.ServerInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 服务注册表
 * @Author: 长灵
 * @Date: 2020-06-12 17:23
 */
@Component
public class ServiceRegistryTable {

    @Getter
    public static ConcurrentHashMap<String, List<ServerInfo>> serviceTable = new ConcurrentHashMap<>();


    /**
     * 刷新服务提供者
     * @param apiProvidersInfo
     */
    public  void refreshProvider(ApiProvidersInfo apiProvidersInfo,Boolean isFirstInit){
//        List<ServerInfo> existProviderSet= serviceTable.get(apiProvidersInfo.getServiceName());
//        if(null == existProviderSet || existProviderSet.size() >= 0){
//            existProviderSet = new ArrayList<>();
//        }


        List<ServerInfo> providerInfoList = apiProvidersInfo.getProviderInfoList();
        if(CollectionUtils.isEmpty(providerInfoList)){
            providerInfoList = new ArrayList<>();
        }



        // 写入到服务注册表
        serviceTable.put(apiProvidersInfo.getServiceName(),providerInfoList);

        // 非第一次初始化,到这里结束
//        if(!isFirstInit){
//            return;
//        }

        // 初始化到服务注册
        InvokeStatisticsCenter.initServer(providerInfoList);


    }



    public List<ServerInfo> getServiceProviders(String serviceName){
        return serviceTable.get(serviceName);
    }


}

