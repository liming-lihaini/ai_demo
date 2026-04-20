package com.filemanager.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * MyBatis + SQLite 配置
 */
@Configuration
@MapperScan("com.filemanager.repository")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        try {
            Files.createDirectories(Paths.get("D:/file-manager-db"));
        } catch (Exception e) {
            throw new RuntimeException("创建数据库目录失败", e);
        }

        String dbPath = "D:/file-manager-db/fileManager.db";
        File dbFile = new File(dbPath);

        if (!dbFile.exists()) {
            try {
                File targetDb = new File("target/classes/db/fileManager.db");
                if (targetDb.exists()) {
                    Files.createDirectories(Paths.get("D:/file-manager-db"));
                    Files.copy(targetDb.toPath(), dbFile.toPath());
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        var config = new com.zaxxer.hikari.HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");

        return new com.zaxxer.hikari.HikariDataSource(config);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQLITE));
        return interceptor;
    }

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false);
        return globalConfig;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setGlobalConfig(globalConfig());
        factoryBean.setPlugins(mybatisPlusInterceptor());

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);

        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }
}