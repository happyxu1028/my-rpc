package my.study;

import my.study.wathcer.ZkWatcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * Hello world!
 */
@SpringBootApplication
public class ConsumeServerBootStrap implements CommandLineRunner {


    @Resource
    private ZkWatcher zkWatcher;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ConsumeServerBootStrap.class);
    }

    @Override
    public void run(String... args) throws Exception {
//        RpcConsumer rpcConsumer = new RpcConsumer();
//        UserService proxy = (UserService) rpcConsumer.createProxy(UserService.class);
//
//        while (true) {
//            Thread.sleep(2000);
//            System.out.println(proxy.login("changling"));
//        }

        zkWatcher.watchProviderListChange();

    }

}
