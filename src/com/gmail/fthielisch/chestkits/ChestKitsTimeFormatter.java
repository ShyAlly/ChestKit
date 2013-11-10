package com.gmail.fthielisch.chestkits;

public class ChestKitsTimeFormatter {

	public static String formatTime(int seconds) {
		StringBuilder sb = new StringBuilder();
		
		int minutes = seconds / 60;
		seconds -= minutes * 60;
		
		int hours = minutes / 60;
		minutes -= hours * 60;
		
		if (hours > 0) {
			sb.append(hours);
			sb.append(" hour");
			if (hours > 1) {
				sb.append('s');
			}
		}
		if (minutes > 0) {
			if (hours > 0) {
				sb.append(", ");
			}
			sb.append(minutes);
			sb.append(" minute");
			if (minutes > 1) {
				sb.append('s');
			}
		}
		if (seconds > 0) {
			if (minutes > 0 || hours > 0) {
				sb.append(", ");
				if (minutes > 0 && hours > 0) {
					sb.append("and ");
				}
			}
			sb.append(seconds);
			sb.append(" second");
			if (seconds > 0) {
				sb.append('s');
			}
		}
		
		return sb.toString();		
	}
}
