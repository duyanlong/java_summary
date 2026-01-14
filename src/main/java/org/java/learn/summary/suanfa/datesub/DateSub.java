package org.java.learn.summary.suanfa.datesub;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: ditto
 * @Package com.qihoo.dc.ditto.module.flink.elements.extract
 * @Description: 计算两个日期间的日期差
 * @date Date : 2019年08月26日 15:13
 */
public class DateSub {

    static DateFormat dfDay = new SimpleDateFormat("D");
    static DateFormat dfYear = new SimpleDateFormat("yyyy");
    static DateFormat dfymd = new SimpleDateFormat("yyyy-MM-dd");

    public static Long compute1(Date sdate, Date edate) {

        Calendar scalendar = Calendar.getInstance();
        scalendar.setTime(sdate);
        Calendar ecalendar = Calendar.getInstance();
        ecalendar.setTime(edate);

        // 结束日期大于开始日期返回0
        if (scalendar.after(ecalendar)) {
            return 0L;
        }

        // 获取时间戳
        long startTime = scalendar.getTimeInMillis();
        long endTime = ecalendar.getTimeInMillis();

        // 直接使用时间戳计算
        return (endTime - startTime) / 1000 / 60 / 60 / 24;
    }

    public static Long compute2(Date sdate, Date edate) {

        Long dayDiff = Long.valueOf(dfDay.format(edate)) - Long.valueOf(dfDay.format(sdate));
        Long eyear = Long.valueOf(dfYear.format(edate));
        Long syear = Long.valueOf(dfYear.format(sdate));
        Long yearDiff = eyear - syear;

        // 日期不合法
        if (yearDiff < 0L || (yearDiff == 0L && dayDiff < 0L)) {
            return 0L;
        }

        if (yearDiff == 0L) {
            return dayDiff;
        }

        // 闰年情况- 忘记原因了
        if (syear % 4 < yearDiff || eyear % 4 < yearDiff) {
            return dayDiff + (yearDiff * 365) + (yearDiff / 4) + 1;
        }

        return dayDiff + (yearDiff * 365) + (yearDiff / 4);
    }

    public static void main(String[] args) throws ParseException {
        Date sdate = dfymd.parse("2019-07-11");
        Date edate = dfymd.parse("2019-09-11");
        System.out.println("第一种方法 = [" + compute1(sdate, edate) + "]");
        System.out.println("第二种方法 = [" + compute2(sdate, edate) + "]");
    }


}
