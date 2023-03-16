select
'unique_checkout' as type
,'${columnName}' as table_name
,'职级关系明细汇总表' as table_desc
,count(1) as total_num
,count(distinct emplid,empl_rcd,query_effdt,report_line_emplid ,report_line_empl_rcd) as unique_num
,count(1)-count(distinct emplid,empl_rcd,query_effdt,report_line_emplid ,report_line_empl_rcd) as divergent_num
,'hdp_hr_dws' as db_name
,'${ds}' as ds
from ${databaseName}.${tableName}