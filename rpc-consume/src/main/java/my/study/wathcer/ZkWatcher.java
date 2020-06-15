package my.study.wathcer;

import my.study.loalbalance.InvokeStatisticsCenter;
import my.study.pojo.ApiProvidersInfo;
import my.study.pojo.ServerInfo;
import my.study.table.ServiceRegistryTable;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-12 17:19
 */
@Component
public class ZkWatcher {

    @Resource
    private ServiceRegistryTable serviceRegistryTable;

    public static final String BASE_PATH = "/my_rpc";

    public static final String CONSUME_DIR_PATH = "/my_rpc/consume/";


    static ZkClient zkClient = new ZkClient("127.0.0.1:2181");


    public synchronized void watchProviderListChange() {

        // 初始化
        initProviderList();


        // 监听
        List<String> children = zkClient.getChildren(BASE_PATH);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        for (String thisChild : children) {
            zkClient.subscribeChildChanges(BASE_PATH + "/" + thisChild + "/provider", new IZkChildListener() {
                /**
                 *
                 * @param parentPath  /my_rpc/my.study.service.UserService/provider
                 * @param currentChilds
                 * @throws Exception
                 */
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    System.out.println(">>>> 监听到节点"+parentPath+"下的子节点列表变更:"+currentChilds);
                    ApiProvidersInfo apiProvidersInfo = parse(parentPath, currentChilds);
                    serviceRegistryTable.refreshProvider(apiProvidersInfo,false);
                }
            });
        }
    }

    /**
     * 初始化服务器
     */
    public void initProviderList() {
        List<String> serviceChildList = zkClient.getChildren(BASE_PATH);
        if (CollectionUtils.isEmpty(serviceChildList)) {
            return;
        }


        for (String serviceChild : serviceChildList) {
            String parentPath = BASE_PATH + "/"+serviceChild + "/provider";
            List<String> providerChildList = zkClient.getChildren(parentPath);
            serviceRegistryTable.refreshProvider(parse(parentPath, providerChildList),true);
        }


    }


    /**
     * 解析
     * @param providerBasePath
     * @param currentChilds
     * @return
     */
    public ApiProvidersInfo parse(String providerBasePath, List<String> currentChilds) {
        String[] split = providerBasePath.split("/");
        String serviceName = split[2];


        if (CollectionUtils.isEmpty(currentChilds)) {
            return new ApiProvidersInfo(serviceName, null);
        }

        ArrayList<ServerInfo> list = new ArrayList();
        for (String currentChild : currentChilds) {
            String[] urlAndPort = currentChild.split(":");
            ServerInfo pdf = new ServerInfo(urlAndPort[0], Integer.parseInt(urlAndPort[1]));
            list.add(pdf);
        }
        return new ApiProvidersInfo(serviceName, list);
    }


}
