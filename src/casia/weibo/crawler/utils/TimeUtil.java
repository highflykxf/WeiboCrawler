package casia.weibo.crawler.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 时间操作工具�?
 * 
 * @author mingyuan.jiang@ia.ac.cn
 * 
 */
public class TimeUtil {
	/**
	 * 休眠
	 * 
	 * @param seconds
	 */
	public static final void sleep(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回传入的时间加1天对应的日期
	 * 
	 * @param time
	 * @return 传入的时间加1天对应的日期
	 */
	public static final Date datePlusOneDay(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		calendar.add(Calendar.DATE, 1);
		return calendar.getTime();
	}

	/**
	 * 返回传入的时间减1天对应的日期
	 * 
	 * @param time
	 * @return 传入的时间减1天对应的日期
	 */
	public static final Date dateMinusOneDay(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
	}

	/**
	 * 判断时间是否比今天大
	 * 
	 * @param startTime
	 *            时间的long格式
	 * @return
	 */
	public static final boolean isDateGreaterThanToday(long date) {
		Calendar instance = Calendar.getInstance();
		instance.set(Calendar.HOUR_OF_DAY, 0);
		instance.set(Calendar.MINUTE, 0);
		instance.set(Calendar.SECOND, 0);
		instance.set(Calendar.MILLISECOND, 0);
		instance.add(Calendar.DATE, 1);
		// System.out.println(new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getTime()) +
		// "\t" + instance.getTime().getTime());
		return date >= instance.getTime().getTime();
	}

	public static final long changeTimeToTheBeginingOfTheDay(long date) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(new Date(date));
		instance.set(Calendar.HOUR_OF_DAY, 0);
		instance.set(Calendar.MINUTE, 0);
		instance.set(Calendar.SECOND, 0);
		instance.set(Calendar.MILLISECOND, 0);
		return instance.getTimeInMillis();
	}

	public static final void sleepRandomSeconds(int seconds) {
		int time = (int) (Math.random() * 5);
		System.out.println(time);
		try {
			TimeUnit.SECONDS.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ParseException {
		sleepRandomSeconds(5);
	}

}
