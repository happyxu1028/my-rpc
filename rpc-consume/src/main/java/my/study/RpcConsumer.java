package my.study;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import my.study.loalbalance.ApiInvokeInfo;
import my.study.loalbalance.LoadBalancer;
import my.study.pojo.ServerInfo;
import my.study.request.RpcRequest;
import my.study.serialize.JSONSerializer;
import my.study.serialize.RpcEncoder;
import my.study.table.ServiceRegistryTable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-05-20 22:49
 */
@Component
public class RpcConsumer {

    @Resource
    private ServiceRegistryTable serviceRegistryTable;


    /**
     * 每个服务一个线程池,避免某个服务因异常原因阻塞时,线程池被塞满不可用
     */
    private Map<Class,ExecutorService> executorMap = new HashMap<>();
//    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 每个api提供者对应一个ClientHandler
     */
    private static Map<ServerInfo,ClientHandler> serverClientHandlerMap = new HashMap<>();

    /**
     * 创建代理对象
     * @param serviceClassName
     * @return
     */
    public Object  createProxy(final Class<?> serviceClassName){

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{serviceClassName},new InvocationHandler(){

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                List<ServerInfo> serviceProviders = serviceRegistryTable.getServiceProviders(serviceClassName.getName());
                if(CollectionUtils.isEmpty(serviceProviders)){
                    String info = String.format(">>>> api:%s have no provider",serviceClassName.getName());
                    System.out.println(info);
                    throw new RuntimeException(info);
                }

                ServerInfo candidatePdfServer = null;
                for (ServerInfo thisProvider : serviceProviders) {
                    candidatePdfServer = thisProvider;
                    break;
                }

                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setClassName(serviceClassName.getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);

                ClientHandler clientHandler = getClient(candidatePdfServer);
                // 设置参数
                clientHandler.setRequest(request);
                // 设置调用信息
                clientHandler.setInvokeInfo(buildInvokeInfo(serviceClassName));

                ExecutorService executorService = executorMap.get(serviceClassName);
                if(null == executorService){
                    executorService = Executors.newFixedThreadPool(2);
                }
                return executorService.submit(clientHandler).get();
            }


        });
    }

    /**
     * 创建调用信息
     * @param serviceClassName
     * @return
     */
    private ApiInvokeInfo buildInvokeInfo(Class<?> serviceClassName) {
        ApiInvokeInfo invokeInfo = new ApiInvokeInfo();
        invokeInfo.setServiceName(serviceClassName.getName());
        invokeInfo.setTargetServer(LoadBalancer.loadBalance());
        return invokeInfo;
    }

    private static ClientHandler getClient(ServerInfo candidatePdfServer) throws InterruptedException {
        ClientHandler clientHandler = serverClientHandlerMap.get(candidatePdfServer);

        // 初始化netty客户端
        if(clientHandler != null){
            return clientHandler;
        }

        clientHandler = new ClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        final ClientHandler finalClientHandler = clientHandler;
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RpcEncoder(RpcRequest.class,new JSONSerializer()));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(finalClientHandler);

                    }
                });

        bootstrap.connect(candidatePdfServer.getIp(),candidatePdfServer.getPort()).sync();
        return clientHandler;
    }
}
