package com.gec.queryterminal.library;

/**
 * @author GEC-LAB
 * @version 1.0
 */
public class BuzzerDev {
	private static BuzzerDev mBuzzerDev = null;

	/**
	 * 静态加载动态库
	 */
	static {
		System.loadLibrary("buzzer_dev");
	}

	/**
	 * 默认构造器
	 */
	private BuzzerDev() {
	}

	/**
	 * 获取单例
	 * 
	 * @return mBuzzerDev
	 */
	public static BuzzerDev getInstance() {
		if (mBuzzerDev == null)
			mBuzzerDev = new BuzzerDev();
		return mBuzzerDev;
	}

	/**
	 * 打开蜂鸣器驱动
	 */
	public native void openBuzzerDev();

	/**
	 * 操作蜂鸣器
	 * 
	 * @param op
	 *            "0"：停止鸣叫 "1"：开始鸣叫
	 * @param freq
	 *            频率
	 */
	public native void opBuzzerDev(int op, long freq);

	/**
	 * 关闭蜂鸣器驱动
	 */
	public native void closeBuzzerDev();
}
