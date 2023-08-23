package com.zwechat.usbserialtest;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Logger {
	private final String tagName = "UsbMessage";
	private final static boolean ENABLE_TIME = false;
	private final static int logLevel = Log.VERBOSE;

	private static Logger inst;
	private final SimpleDateFormat simpleDateFormat;

	private Logger() {
		simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS", Locale.getDefault());
	}

	public static synchronized Logger getLogger(Class<?> key) {
		if (inst == null) {
			inst = new Logger();
		}
		return inst;
	}

	public static synchronized Logger getLogger() {
		return getLogger(null);
	}

	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}

			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}

			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}

			return "[" + st.getFileName() + ":" + st.getLineNumber() + "]";
		}

		return null;
	}

	private String createMessage(String msg) {
		String functionName = getFunctionName();
		StringBuilder sb = new StringBuilder();
		if (ENABLE_TIME) {
			sb.append(simpleDateFormat.format(new Date()));
			sb.append(" - ");
		}
		if (functionName != null) {
			sb.append(functionName);
			sb.append(" - ");
			sb.append(Thread.currentThread().getId());
		}

		if (msg != null && msg.length() > 0) {
			sb.append(" - ");
			sb.append(msg);
		}

		return sb.toString();
	}

	/**
	 * log.i
	 */
	public void i(String format, Object... args) {
		if (logLevel <= Log.INFO) {
			String message = createMessage(getInputString(format, args));
			Log.i(tagName, message);
		}
	}



	/**
	 * log.d
	 */
	public void d(String format, Object... args) {
		if (logLevel <= Log.DEBUG) {
			String message = createMessage(getInputString(format, args));
			Log.d(tagName, message);
		}
	}



	/**
	 * log.e
	 */
	public void e(String format, Object... args) {
		if (logLevel <= Log.ERROR) {
			String message = createMessage(getInputString(format, args));
			Log.e(tagName, message);
		}
	}

	public void e(Throwable t) {
		e(null, t);
	}

	public void e(String msg, Throwable t) {
		if (logLevel <= Log.ERROR) {
			String message = createMessage(msg);
			Log.e(tagName, message, t);
		}
	}

	private String getInputString(String format, Object... args) {
		if (format == null) {
			return null;
		}
		return String.format(format, args);
	}

	/**
	 * log.d
	 */
	public void w(String format, Object... args) {
		if (logLevel <= Log.WARN) {
			String message = createMessage(getInputString(format, args));
			Log.w(tagName, message);
		}
	}
}
