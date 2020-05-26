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
import io.netty.handler.codec.string.StringEncoder;
import my.study.request.RpcRequest;
import my.study.serialize.JSONSerializer;
import my.study.serialize.RpcEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-05-20 22:49
 */
public class RpcConsumer {

    /**
     * 创建线程池对象
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static UserClientHandler userClientHandler;

    /**
     * 创建代理对象
     * @param serviceClassName
     * @return
     */
    public Object  createProxy(final Class<?> serviceClassName){

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{serviceClassName},new InvocationHandler(){

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                // 初始化netty客户端
                if(userClientHandler == null){
                    initClient();
                }

                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setClassName(serviceClassName.getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);


                // 设置参数
                userClientHandler.setRequest(request);

                return executor.submit(userClientHandler).get();
            }


        });
    }

    private static void initClient() throws InterruptedException {
        userClientHandler = new UserClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RpcEncoder(RpcRequest.class,new JSONSerializer()));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(userClientHandler);

                    }
                });

        bootstrap.connect("127.0.0.1",8088).sync();
    }
}
