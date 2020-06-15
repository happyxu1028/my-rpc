package my.study.controller;

import my.study.RpcConsumer;
import my.study.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-13 15:26
 */
@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private RpcConsumer rpcConsumer;

    @GetMapping("/login/{name}")
    public Object login(@PathVariable("name")String name) {
        try {
            UserService proxy = (UserService) rpcConsumer.createProxy(UserService.class);
            return proxy.login(name);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
