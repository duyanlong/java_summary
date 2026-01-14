## 参考资料：
https://blog.csdn.net/weixin_39734458/article/details/113605991

## 查询大小写敏感处理
```sql
select
       *
        from t_ds_biz_task
        where BINARY name='task_name1'
        and project_code = *****
```
缺点是只能做到查询时大小写敏感，但写入时唯一约束不区分大小写；
如果表中有将该列作为unique key则会报错
        
## 读写大小写敏感设置修改语法:
```sql
ALTER TABLE t_ds_biz_process MODIFY COLUMN name VARBINARY(200) DEFAULT NULL COMMENT 'process definition name';
```
使用该方式则查询和插入时唯一约束检查都可通过