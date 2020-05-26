package my.study.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import my.study.request.RpcRequest;
import my.study.service.UserService;
import my.study.service.UserServiceImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-05-20 22:15
 */
@Service
public class UserServerHandler extends ChannelInboundHandlerAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Resource
    private UserServiceImpl userService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcRequest rpcRequest = (RpcRequest) msg;
        Object serviceBean = applicationContext.getBean(Class.forName(rpcRequest.getClassName()));
        Method method = serviceBean.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        Object result = method.invoke(serviceBean, rpcRequest.getParameters());

        ctx.writeAndFlush(result);


    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
