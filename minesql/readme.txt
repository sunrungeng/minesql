1 登录及注册
    1.1 登录:
        minesql -u username -p password
    1.2 注册:(只有角色为manage的才能实现功能)
        create user username password;
2 操作说明:
    \h
    help
3 用户管理:
    3.1 授予权限:
        grant privilege to username;
    3.2 收回权限:
        revoke privilege from username;
    3.3 设置管理员:
        set user username manager;
4 数据库管理:
    4.1 创建数据库:
        create database databasename;
    4.2 删除数据库:
        drop database databasename;
    4.3 查看所有数据库名:
        show database;
    4.4 查看所有数据表、视图、索引的信息:
        help database;
5 关系表管理:
    5.1 创建:
        create table tablename(colunmnname columntype(datalength) null[not null] [primary key] ,,);
    5.2 删除:
        drop table tablename;
    5.3 增:
        insert [into] tablename[(columnname1,columnname2,)] values(value1,value2);
    5.4 删:
        delete form tablename [where a = b];
    5.5 改:
        update tablename set a = 1 [where c = d];
    5.6 查:
        select [*],[columnname1,columnname2] from tablename [where a = b];
    5.7 查看表结构
        help table tablename;
6 退出
    exit
    d