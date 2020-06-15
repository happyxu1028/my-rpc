package my.study.service;

import my.study.register.ZKRegister;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-05-20 22:12
 */
@Service
public class UserServiceImpl implements UserService, InitializingBean {


    @Value("${netty.rpc.port}")
    private Integer prcCommunicationPort;

    @Resource
    private ZKRegister register;

    @Override
    public String login(String userName) {
        System.out.println(">>>> 用户:"+userName+"登录成功");
        return "登录成功";
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        register.registerService(UserService.class.getName(), prcCommunicationPort,ZKRegister.PROVIDER_PATH);
    }
}
