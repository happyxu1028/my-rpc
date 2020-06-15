package my.study.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-13 08:46
 */
@Data
@AllArgsConstructor
public class ApiProvidersInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 提供服务的机器列表
     */
    private List<ServerInfo> providerInfoList;


}
