package my.sutdy;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-14 13:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DataSourceProperties {

    private String url;

    private String username;

    private String password;


    public static void main(String[] args) {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:mysql://127.0.0.1:3306/bank");
        properties.setUsername("root");
        properties.setPassword("root");
        System.out.println(JSON.toJSONString(properties));
    }
}
