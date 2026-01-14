
### 创建 sql 触发器
```sql
DELIMITER //
CREATE TRIGGER trigger_name AFTER INSERT ON A
FOR EACH ROW
BEGIN
  INSERT INTO A_history (id, column1, column2, backup_time)
  VALUES (NEW.id, NEW.column1, NEW.column2, NOW());
END //

CREATE TRIGGER trigger_name AFTER UPDATE ON A
FOR EACH ROW
BEGIN
  INSERT INTO A_history (id, column1, column2, backup_time)
  VALUES (NEW.id, NEW.column1, NEW.column2, NOW());
END //

CREATE TRIGGER trigger_name AFTER DELETE ON A
FOR EACH ROW
BEGIN
  INSERT INTO A_history (id, column1, column2, backup_time)
  VALUES (OLD.id, OLD.column1, OLD.column2, NOW());
END //
DELIMITER ;
```

### 删除 sql触发器
```sql
DROP TRIGGER IF EXISTS trigger_name;
```


### 更改列表结构
ALTER TABLE 语句用于修改已存在的表的结构，其中 ADD COLUMN 子句用于向表中添加新的列。以下是 ALTER TABLE ADD COLUMN 语句的语法：

sql
```sql
ALTER TABLE table_name
ADD COLUMN column_name column_definition
[ FIRST | AFTER existing_column ];
```
让我们逐个解释每个部分的含义：

ALTER TABLE：用于修改表结构的关键字。
table_name：要修改的表的名称。
ADD COLUMN：用于向表中添加新列的子句。
column_name：要添加的新列的名称。
column_definition：新列的定义，包括数据类型和约束等。
FIRST：可选关键字，用于将新列放置在表的最前面。
AFTER existing_column：可选关键字，用于将新列放置在指定现有列的后面。
你可以根据需要选择使用 FIRST 或 AFTER 关键字来指定新列的位置。如果使用 FIRST，新列将被放置在表的最前面。如果使用 AFTER，你需要指定一个现有列的名称，新列将被放置在该列的后面。