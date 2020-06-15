package my.study.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerInfo {

    /**
     * 提供服务的机器IP
     */
    private String ip;

    /**
     * 提供服务的机器端口
     */
    private Integer port;


}
