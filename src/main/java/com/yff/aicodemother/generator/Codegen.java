package com.yff.aicodemother.generator;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yff
 * @date 2026-02-02 15:15:05
 */
public class Codegen {

    public static void main(String[] args) {
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/ai_code_mother?characterEncoding=utf-8");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle1();
        //GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle1() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.setBasePackage("com.test");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable("user")
                .setLogicDeleteColumn("isDelete"); // 设置逻辑删除字段，默认是 deleted

        //设置生成 entity 并启用 Lombok
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        //设置项目的JDK版本，项目的JDK为14及以上时建议设置该项，小于14则可以不设置
        globalConfig.setEntityJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        //设置生成service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        //设置生成controller
        globalConfig.enableController();

        //可以单独配置某个列
//        ColumnConfig columnConfig = new ColumnConfig();
//        columnConfig.setColumnName("tenant_id");
//        columnConfig.setLarge(true);
//        columnConfig.setVersion(true);
//        globalConfig.setColumnConfig("tb_account", columnConfig);

        //设置生成时间和字符串为空，避免多余的代码改动
        globalConfig.getJavadocConfig()
                .setAuthor("yff")
                .setSince(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return globalConfig;
    }




}
