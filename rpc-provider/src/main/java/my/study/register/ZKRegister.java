package my.study.register;

import my.study.util.IpUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-12 14:45
 */
@Component
public class ZKRegister {

    public static final String PROVIDER_PATH = "/provider";

    public static final String CONSUME_PATH = "/consume";


    static CuratorFramework zkClient = null;

    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        zkClient = CuratorFrameworkFactory.builder()
                .namespace("my_rpc")
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(100000)
                .connectionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
    }

    public void registerService(String serviceInterface,Integer prcCommunicationPort,String actionPath) {

        try {

            String providerDirPath = "/"+serviceInterface  + actionPath;
            Stat stat = zkClient.checkExists().forPath(providerDirPath);
            if(null == stat){
                // 创建永久的/my_rpc/provider
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(providerDirPath);
            }


            // 创建临时的提供服务的节点
            String providerPath = providerDirPath+"/"+ IpUtil.getIp() +":"+prcCommunicationPort;
//            String providerPath = providerDirPath+"/"+ "127.0.0.1" +":"+prcCommunicationPort;
            String providerPathResult = zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(providerPath,serviceInterface.getBytes());
            System.out.println(">>>> 已创建临时服务提供节点: "+providerPathResult);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
