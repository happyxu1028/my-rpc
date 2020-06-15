package my.study.loalbalance;

import lombok.Data;
import lombok.EqualsAndHashCode;
import my.study.pojo.ServerInfo;

import java.util.Date;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-14 15:39
 */
@Data
@EqualsAndHashCode
public class ApiInvokeInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 目标服务器
     */
    private ServerInfo targetServer;

    /**
     * 请求时间
     */
    private Date invokeRequestTime;

    /**
     * 响应时间
     */
    private Date invokeResponseTime;


    public ApiInvokeInfo() {
    }

    public ApiInvokeInfo(String serviceName, ServerInfo targetServer, Date invokeRequestTime) {
        this.serviceName = serviceName;
        this.targetServer = targetServer;
        this.invokeRequestTime = invokeRequestTime;
    }
}
