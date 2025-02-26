package xyz.zzj.interviewBank_zzj.config;

import lombok.Data;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsPackage: xyz.zzj.interviewBank_zzj.config
 * @ClassName: RedissonConfig
 * @Author: zeng
 * @CreateTime: 2025/2/3 21:37
 * @Description: redisson配置类
 * @Version: 1.0
 */

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;
    private Integer port;
    private String password;
    private Integer database;


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 单机模式
        config.useSingleServer()
                // 设置redis地址
                .setAddress("redis://" + host + ":" + port)
                // 设置数据库
                .setDatabase(database)
                // 设置密码
                .setPassword(password);
        return Redisson.create(config);
    }

}
