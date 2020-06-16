package my.study.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ServerInfo {

    /**
     * 提供服务的机器IP
     */
    private String ip;

    /**
     * 提供服务的机器端口
     */
    private Integer port;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return Objects.equals(ip, that.ip) &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
