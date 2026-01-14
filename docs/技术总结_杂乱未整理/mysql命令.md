
* 创建用户      
create user calc@'%' identified by '****'
* 为用户赋权     
grant all privileges on calc_db_dev.* to calc@'%';
* 查询有哪些用户       
select user,host from mysql.user;
* 删除无用账号
drop user calc@'%';
* 查询表大小     
select table_schema as '数据库',
table_name as '表名',
table_rows as '记录数',truncate(data_length/1024/1024, 2) as '数据容量(MB)',truncate(index_length/1024
/1024, 2) as '索引容量(MB)'from information_schema.tables order by data_length desc, index_length desc;

* 查询库大小
select table_schema as '数据库',sum(table_rows) as '记录数',sum(truncate(data_length/1024/1024, 2)) as
 '数据容量(MB)',sum(truncate(index_length/1024/1024, 2)) as '索引容量(MB)'from information_schema.tables
  group by table_schema order by sum(data_length) desc, sum(index_length) desc;
  
* 修改用户密码
使用root登录mysql后执行下面命令
set password for 用户名@'服务器'=password('新密码');