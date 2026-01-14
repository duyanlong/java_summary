/**
 * @Project: java_summary
 * @Package org.java.learn.summary.suanfa
 * @Description: TODO
 * @author : duyanlong
 * @date Date : 2019年08月27日 17:12
 * @version V1.0
 */
package org.java.learn.summary.suanfa;

/**
 * 1、排序：堆排序、冒泡排序、插入排序、选择排序、快速排序
 * 2、大文件排序：
 *      2.1：归并排序：先将数据分成多个小文件，对小文件内部排序，在取出每个文件中最小值（即第一条），多个文件最小值比较最小的写入排序的大文件，同时从原小文件中删除，后面依次判断读取即可；最终大文件完成排序
 *      2.2：桶排序：预先计算最大最小值，根据最大最小值得到n个桶，将数据写入n个桶中（根据桶区间判断），基于每个桶文件进行排序，最终将多个桶文件数据合并在一起；
 * 3、大文件单机wordcount:将每个词根据hashcode分成若干小文件，基于每个小文件统计wordcount
 * 4、社交数据互为好友分析：如（mid,(friendlist)）(A,(B,C))  (B,(A)) 统计出A、B互为好友的数据；
 *      4.1：将后面的好友列表分隔，然后在拆分成多行，自己关联自己，判断好友和mid如果都不为空则为互加好友。
 *      4.2：同上面的好友列表分隔拆分成多行后，将好友id和mid排序后作为主键，进行聚合如果数量大于1则为好友id和mid是互为好友，对应spark流程：flatmap
 *      .reducebyke.filter
 * 5、点亮中国全国点亮城市个人排名：将点亮城市按照个数和点亮的人建表A，每个用户新点亮一个城市更新表A，同时拿个人在表A中对应城市数人数排名即为在全国的排名；
 * 6、大文件获取topn：维护一个数组长度为n，依次将数据通过插入排序放入数组，插入后删除最后一条，继续同样的操作完成所有，最终结果即为topn
 * 7、路由器数据每天会有多条数据数据内容含有m2(唯一标识)、路由表（格式：A,B,C）、日志产生时间，要获取最新的一条信息然后将m2和路由表生成多条数据生成明细
 *   select m2,user_id,rid from (
 select m2,user_id,tag_rz1,log_datetime,row_number() over(partition by m2,user_id order by log_datetime desc) as rn
 from td_ihk_iha_m_header_h
 where pday=2019101608
 ) taba lateral view explode(split(tag_rz1,',')) tmp as rid
 where taba.rn=1 ;
 * */