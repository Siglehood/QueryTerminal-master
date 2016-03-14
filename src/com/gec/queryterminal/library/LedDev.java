package com.gec.queryterminal.library;

/**
 * @author GEC-LAB
 * @version 1.0
 */
public class LedDev {
	private static LedDev mLedDev = null;

	/**
	 * 静态加载动态库
	 */
	static {
		System.loadLibrary("led_dev");
	}

	/**
	 * 默认构造器
	 */
	private LedDev() {
	}

	/**
	 * 获取单例
	 * 
	 * @return mLedDev
	 */
	public static LedDev getInstance() {
		if (mLedDev == null)
			mLedDev = new LedDev();
		return mLedDev;
	}

	/**
	 * 打开LED驱动
	 */
	public native void openLedDev();

	/**
	 * 操作LED驱动
	 * 
	 * @param pos
	 *            LED的位置
	 * @param op
	 *            "on"：亮灯 "off"：灭灯
	 */
	public native void opLedDev(int pos, String op);

	/**
	 * 关闭LED驱动
	 */
	public native void closeLedDev();
}
