package my.study;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import my.study.request.RpcRequest;

import java.util.concurrent.Callable;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-05-20 22:41
 */

public class UserClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private Object result;
    private RpcRequest request;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        context = ctx;
    }

    /**
     * 收到服务端数据，唤醒等待线程
     */

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) {
        result = msg;
        notify();
    }

    /**
     * 写出数据，开始等待唤醒
     */
    @Override
    public synchronized Object call() throws InterruptedException {
        context.writeAndFlush(request);
        wait();
        return result;
    }

    /*
     设置参数
     */
    void setRequest(RpcRequest request) {
        this.request = request;
    }



}
