package com.imooc.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@PropertySource("classpath:excludeUrlPath.properties")
@ConfigurationProperties(prefix = "exclude")
public class ExcludeUrlProperties {
    private List<String> Urls;

    // 被限制的黑名单
    private List<String> ipLimitUrls;
}
