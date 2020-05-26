package my.study.service;

import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-05-20 22:12
 */
@Service
public class UserServiceImpl implements UserService {


    @Override
    public String login(String userName) {
        System.out.println(">>>> 用户:"+userName+"登录成功");
        return "登录成功";
    }
}
