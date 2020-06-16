package my.sutdy;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.I0Itec.zkclient.IZkConnection;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-14 13:34
 */
@Configuration
public class DataSourceConfig {


    @Bean
    public DataSource dataSource() {
        String dbConfigPath = "/db-config";

        ZkClient configPath = new ZkClient("127.0.0.1:2181", Integer.MAX_VALUE, Integer.MAX_VALUE,stringZkSerializer);

        return new DynamicDataSource(configPath, dbConfigPath);
    }

    public class DynamicDataSource extends DelegatingDataSource {

        private ZkClient zkClient;
        private String configPath;

        public DynamicDataSource(ZkClient zkClient, String configPath) {
            this.zkClient = zkClient;
            this.configPath = configPath;
        }

        @Override
        public void afterPropertiesSet() {

            //   /db-config

            // 监听连接配置变化
            zkClient.subscribeDataChanges(configPath, new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    refreshDataSource(data);
                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    refreshDataSource(null);
                }
            });

            // 初始获取配置
            Object data = zkClient.readData(configPath);
            refreshDataSource(data);
        }

        private void refreshDataSource(Object data) {

            System.out.println(">>>> 开始刷新datasource，data:" + data);
            DataSourceProperties dataSourceProperties = JSON.parseObject((String) data, DataSourceProperties.class);
            DataSource    newDataSource = createHikariDataSource(dataSourceProperties);


            if (newDataSource != null) {

                // 释放原来的DataSource
                DataSource dataSource = getTargetDataSource();
                try {
                    if(null != dataSource){
                        System.out.println(String.format(">>>> 旧数据库连接池:%s 已关闭",dataSource.toString()));
                        closeQuality(dataSource);
                    }
                } catch (RuntimeException e) {
                    closeQuality(newDataSource); // 出现异常，要释放新创建的dataSource，防止连接泄露
                    throw e;
                }

                // 使用新的数据源
                setTargetDataSource(newDataSource);
                System.out.println(String.format(">>>> 新数据库连接池:%s 已生成, config:%s",newDataSource.toString(),dataSourceProperties));
            }

            obtainTargetDataSource();
        }

        private Map<String, String> parse(String data) throws IOException {
            return new ObjectMapper().readValue(data, HashMap.class);
        }

        private void closeQuality(DataSource dataSource) {
            if (dataSource != null && dataSource instanceof Closeable) {
                try {
                    ((Closeable) dataSource).close();
                } catch (IOException e) {
                    throw new RuntimeException("释放数据源失败", e);
                }
            }
        }

    }

    public DataSource createHikariDataSource(DataSourceProperties dataSourceProperties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
        hikariConfig.setUsername(dataSourceProperties.getUsername());
        hikariConfig.setPassword(dataSourceProperties.getPassword());

        return new HikariDataSource(hikariConfig);
    }

    private static final ZkSerializer stringZkSerializer = new ZkSerializer() {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] serialize(Object data) throws ZkMarshallingError {
            try {
                return objectMapper.writeValueAsString(data).getBytes("UTF-8");
            } catch (Exception e) {
                throw new ZkMarshallingError(e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws ZkMarshallingError {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new ZkMarshallingError(e);
            }
        }
    };
}
