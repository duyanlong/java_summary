基于星云 cos 建表，跨集群部署目前有问题，待添加配置验证 ，参考文档https://cloud.tencent.com/document/product/436/6884
```sql
use HDP_FIN_STG;
CREATE EXTERNAL  TABLE IF NOT EXISTS `stg_fin_cas_great_market_base_1df` (
`ftime` bigint  COMMENT '月份',
`day` bigint  COMMENT '日期',
`platform` bigint  COMMENT '平台',
`contract_no` string  COMMENT '合同号',
`fpay_type` bigint  COMMENT '付费类型',
`fcp_id` string  COMMENT '版权方ID',
`fcp_name` string  COMMENT '版权方名',
`contract_start_date` string  COMMENT '合同开始时间',
`contract_end_date` string  COMMENT '合同结束时间',
`vander_id` string  COMMENT '他方主体ID',
`play_p0` bigint  COMMENT '免费用户播放',
`play_p8` bigint  COMMENT '8元用户播放',
`play_p10` bigint  COMMENT '10元用户播放',
`play_p12` bigint  COMMENT '12元用户播放',
`play_p15` bigint  COMMENT '15元用户播放',
`play_ps` bigint  COMMENT '单次购买用户播放',
`play_bendi_p0` bigint  COMMENT '免费用户本地播放',
`play_bendi_p8` bigint  COMMENT '8元用户本地播放',
`play_bendi_p10` bigint  COMMENT '10元用户本地播放',
`play_bendi_p12` bigint  COMMENT '12元用户本地播放',
`play_bendi_p15` bigint  COMMENT '15元用户本地播放',
`play_bendi_ps` bigint  COMMENT '单次购买用户本地播放',
`down_d0` bigint  COMMENT '免费用户下载',
`down_d8` bigint  COMMENT '8元用户下载',
`down_d10` bigint  COMMENT '10元用户下载',
`down_d12` bigint  COMMENT '12元用户下载',
`down_d15` bigint  COMMENT '15元用户下载',
`down_ds` bigint  COMMENT '单次购买用户下载',
`ffb_paysum` bigint  COMMENT '付费包驱动金额',
`vip_paysum` bigint  COMMENT '包月驱动金额',
`buy1_paysum` bigint  COMMENT '单曲购买金额',
`yuebi_paysum` bigint  COMMENT '乐币打榜金额',
`digital_song_ios_sale_cnt` bigint  COMMENT 'ios数字单曲销量',
`digital_song_sale_cnt` bigint  COMMENT '非ios数字单曲销量',
`digital_song_ios_sale_amount` bigint  COMMENT 'ios数字单曲销售金额',
`digital_song_sale_amount` bigint  COMMENT '非ios数字单曲销售金额',
`single_ios_sale_cnt` bigint  COMMENT 'ios销售量',
`single_ios_sale_amount` bigint  COMMENT 'ios销售额',
`single_sale_cnt` bigint  COMMENT '非ios销售量',
`single_sale_amount` bigint  COMMENT '非ios销售额',
`type` bigint  COMMENT '类型，1全量，2全量有版权，3独家全量，独家全量有版权'
) COMMENT 'CAS大盘报表基础数据1'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
stored as parquet
LOCATION 'cosn://tme-di-tdw2cos-1258344705/tme_di/fin/tme_fin_it/dws_cost_great_word_market_base_merge_contract_df/202305'
tblproperties ("parquet.compress"="SNAPPY");
```
spark 读写 cos
https://cloud.tencent.com/document/product/436/6884

基于中心 cos 建表 ，添加 hive 中 url 配置给 hdp-fin 修改后实现
enter image description here

```sql
-- 建库
create database HDP_FIN_ODS location 'cosn://fin-dw-test-1253428821/hive/ods';
create database HDP_FIN_STG location 'cosn://fin-dw-test-1253428821/hive/stg';
-- 建表
CREATE TABLE `hdp_fin_tmp.tmp_fin_gl_balance_3_cos_11`(
`query_date` date COMMENT '查询维表日期',
`ledger_id` varchar(5000) COMMENT '账套ID',
`ledger_name` varchar(5000) COMMENT '账套名',
`ccid` varchar(5000) COMMENT '账户组合ID',
`is_summary_flag` varchar(5000) COMMENT '是否汇总',
`account_type` varchar(5000) COMMENT '科目类型',
`company_code` varchar(5000) COMMENT '公司code',
`company_name` varchar(5000) COMMENT '公司名称',
`costcenter_code` varchar(5000) COMMENT '成本中心code',
`costcenter_name` varchar(5000) COMMENT '成本中心名称',
`account_code` varchar(5000) COMMENT '科目code',
`account_name` varchar(5000) COMMENT '科目名称',
`subacc_code` varchar(5000) COMMENT '明细科目code',
`subacc_name` varchar(5000) COMMENT '明细科目名称',
`project_code` varchar(5000) COMMENT '项目段code',
`project_name` varchar(5000) COMMENT '项目名称',
`product_code` varchar(5000) COMMENT '产品段code',
`product_name` varchar(5000) COMMENT '产品段名称',
`channel_code` varchar(5000) COMMENT '渠道段code',
`channel_name` varchar(5000) COMMENT '渠道段名称',
`ccid_code` varchar(5000) COMMENT 'ccid组合code',
`ccid_name` varchar(5000) COMMENT 'ccid组合名称',
`period_name` varchar(5000) COMMENT '会计期',
`period_end_date` date COMMENT '期间结束日期',
`base_currency_code` varchar(5000) COMMENT '账套本位币',
`currency_code` varchar(5000) COMMENT '币种',
`actual_flag` varchar(5000) COMMENT '余额类型',
`translated_flag` varchar(5000) COMMENT '折算标志',
`period_year` varchar(5000) COMMENT '期间年度',
`period_no` varchar(5000) COMMENT '期间序号',
`begin_year_period` varchar(5000) COMMENT '年初会计期',
`begin_quarter_period` varchar(5000) COMMENT '季初会计期',
`ptd_net_dr_amt` double COMMENT '本期原币发生借方',
`ptd_net_cr_amt` double COMMENT '本期原币发生贷方',
`ptd_net_amt` double COMMENT '本期原币发生额',
`ptd_begin_dr_amt` double COMMENT '本期原币期初借方',
`ptd_begin_cr_amt` double COMMENT '本期原币期初贷方',
`ptd_begin_amt` double COMMENT '本期原币期初余额',
`end_balance_amt` double COMMENT '期末原币余额',
`ytd_begin_dr_amt` double COMMENT '年原币期初借方',
`ytd_begin_cr_amt` double COMMENT '年原币期初贷方',
`ytd_begin_amt` double COMMENT '年原币期初余额',
`ytd_net_dr_amt` double COMMENT '年原币发生借方',
`ytd_net_cr_amt` double COMMENT '年原币发生贷方',
`ytd_net_amt` double COMMENT '年原币发生额',
`qtd_begin_dr_amt` double COMMENT '季度原币期初借方',
`qtd_begin_cr_amt` double COMMENT '季度原币期初贷方',
`qtd_begin_amt` double COMMENT '季度原币期初余额',
`qtd_net_dr_amt` double COMMENT '季度原币发生借方',
`qtd_net_cr_amt` double COMMENT '季度原币发生贷方',
`qtd_net_amt` double COMMENT '季度原币发生额',
`beq_ptd_net_dr_amt` double COMMENT '本期本位币发生借方',
`beq_ptd_net_cr_amt` double COMMENT '本期本位币发生贷方',
`beq_ptd_net_amt` double COMMENT '本期本位币发生额',
`beq_ptd_begin_dr_amt` double COMMENT '本期本位币期初借方',
`beq_ptd_begin_cr_amt` double COMMENT '本期本位币期初贷方',
`beq_ptd_begin_amt` double COMMENT '本期本位币期初余额',
`beq_ptd_balance_amt` double COMMENT '期末本位币余额',
`beq_ytd_begin_dr_amt` double COMMENT '年本位币期初借方',
`beq_ytd_begin_cr_amt` double COMMENT '年本位币期初贷方',
`beq_ytd_begin_amt` double COMMENT '年本位币期初余额',
`beq_ytd_net_dr_amt` double COMMENT '年本位币发生借方',
`beq_ytd_net_cr_amt` double COMMENT '年本位币发生贷方',
`beq_ytd_net_amt` double COMMENT '年本位币发生额',
`beq_qtd_begin_dr_amt` double COMMENT '季度本位币期初借方',
`beq_qtd_begin_cr_amt` double COMMENT '季度本位币期初贷方',
`beq_qtd_begin_amt` double COMMENT '季度本位币期初余额',
`beq_qtd_net_dr_amt` double COMMENT '季度本位币发生借方',
`beq_qtd_net_cr_amt` double COMMENT '季度本位币发生贷方',
`beq_qtd_net_amt` double COMMENT '季度本位币发生额',
`data_source` varchar(5000) COMMENT '数据来源',
`etl_date` string COMMENT 'ETL时间')
COMMENT '总账余额表计算临时表3'
ROW FORMAT SERDE
'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
WITH SERDEPROPERTIES (
'field.delim'='\t',
'serialization.format'='\t')
STORED AS INPUTFORMAT
'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'
LOCATION
'cosn://fin-dw-test-1253428821/hive/tmp_fin_gl_balance_3_cos_11'
TBLPROPERTIES (
'orc.compress'='SNAPPY',
'transient_lastDdlTime'='1690718084');
```

/usr/local/service/spark/conf/spark-default.conf
```text
# cos
spark.hadoop.fs.cosn.bucket.region=ap-guangzhou
spark.hadoop.fs.cos.userinfo.region=gz
spark.hadoop.fs.cosn.credentials.provider=org.apache.hadoop.fs.auth.SimpleCredentialProvider
spark.hadoop.fs.cosn.bucket.tme-di-tdw2cos-12***.userinfo.secretId=AKID2*****vvg
spark.hadoop.fs.cosn.bucket.tme-di-tdw2cos-12***.userinfo.secretKey=Zty****RYP2
spark.hadoop.fs.cosn.bucket.fin-dw-test-12**.userinfo.secretId=AK*****lrmY
spark.hadoop.fs.cosn.bucket.fin-dw-test-12**.userinfo.secretKey=lEX*****wFM
```

https://cloud.tencent.com/document/product/589/12303