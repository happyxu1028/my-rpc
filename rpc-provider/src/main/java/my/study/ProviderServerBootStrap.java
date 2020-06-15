package my.study;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import my.study.handler.UserServerHandler;
import my.study.request.RpcRequest;
import my.study.serialize.JSONSerializer;
import my.study.serialize.RpcDecoder;
import my.study.serialize.RpcEncoder;
import my.study.util.IpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * Hello world!
 */
@SpringBootApplication
public class ProviderServerBootStrap implements CommandLineRunner {


    @Value("${netty.rpc.port}")
    private Integer prcCommunicationPort;

    @Resource
    private UserServerHandler userServerHandler;


    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ProviderServerBootStrap.class);
    }


    @Override
    public void run(String... args) throws Exception {
        startServer();
    }

    private void startServer() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(userServerHandler);

                    }
                });
        serverBootstrap.bind(IpUtil.getIp(), prcCommunicationPort).sync();
    }
}
