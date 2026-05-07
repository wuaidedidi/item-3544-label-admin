package com.label.admin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DbType dbType = detectDbType(datasourceUrl);
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(dbType);
        paginationInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    private DbType detectDbType(String url) {
        if (url == null || url.isEmpty()) {
            return DbType.MYSQL;
        }
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains(":mysql:")) {
            return DbType.MYSQL;
        } else if (lowerUrl.contains(":postgresql:")) {
            return DbType.POSTGRE_SQL;
        } else if (lowerUrl.contains(":oracle:")) {
            return DbType.ORACLE;
        } else if (lowerUrl.contains(":sqlserver:") || lowerUrl.contains(":microsoft:")) {
            return DbType.SQL_SERVER;
        } else if (lowerUrl.contains(":mariadb:")) {
            return DbType.MARIADB;
        } else if (lowerUrl.contains(":dm:")) {
            return DbType.DM;
        } else if (lowerUrl.contains(":kingbase:") || lowerUrl.contains(":kingbase8:")) {
            return DbType.KINGBASE_ES;
        }
        return DbType.MYSQL;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
