module memo {
    requires jdk.httpserver;
    requires java.sql;
    requires org.mybatis;
    requires com.oracle.database.jdbc;

    exports app;
    exports app.api;
    exports app.memo;
    exports app.memo.dao;
    exports app.memo.dto;
    exports app.memo.entity;
    exports app.memo.mapper;
    exports app.util;

    opens app.memo.entity to org.mybatis;
}
