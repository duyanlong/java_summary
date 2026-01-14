package org.java.learn.summary.suanfa.chianHoop;


/**
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.suanfa.chianHoop
 * @Description:
 * 方法1：循环链表中的数据判断存在依赖的元素移到另外一个表中，
 * 不存在依赖不动，最终循环判断完后如果结果表中和原来的count
 * 一样说明链表成环；前提是在头节点成环的，如果非头节点成环无法识别；使用方法三更合适；
 * 方法2: 循环链表，设定两个步长1、2，每次循环取出对应步长的元素值，
 * 循环n次后如果两个元素相等说明成环，类似于跑步一样操场是圆的，
 * 两个人一个快一个慢，跑n圈后总有相遇的一次；
 * 方法3：循环过程中将顺序的两个元素生成组合key，value为count+1，如果count>1时说明成环，这样非头节点成环也可以判断
 * @date Date : 2019年08月27日 17:29
 */
public class ChianHoopCheck {

    public static void main(String[] args) {
    }
}
