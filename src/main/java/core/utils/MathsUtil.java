package core.utils;

public class MathsUtil {

	public static boolean isLong(String string) {
		try {
			Long.parseLong(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static long getLong(String string) {
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static Long parseLong(String string) {
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
