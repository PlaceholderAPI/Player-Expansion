package com.extendedclip.papi.expansion.player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
/*
*
* Player-Expansion
* Copyright (C) 2018 Ryan McCarthy
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*
*/
public class PlayerUtil {

	public static final int ticksAtMidnight = 18000;
	public static final int ticksPerDay = 24000;
	public static final int ticksPerHour = 1000;
	public static final double ticksPerMinute = 1000d / 60d;
	public static final double ticksPerSecond = 1000d / 60d / 60d;
	private static final SimpleDateFormat twentyFour = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	private static final SimpleDateFormat twelve = new SimpleDateFormat("h:mm aa", Locale.ENGLISH);
	
	public static String format12(long ticks) {
		try {
			return twelve.format(twentyFour.parse(ticksToTime(ticks)));
		} catch (ParseException e) {
			return ticksToTime(ticks);
		}
	}
	
	public static String format24(long ticks) {
		return ticksToTime(ticks);
	}
	
	private static String ticksToTime(long ticks) {
		ticks = ticks - ticksAtMidnight + ticksPerDay;
		long hours = ticks / ticksPerHour;
		ticks -= hours * ticksPerHour;
		long mins = (long)Math.floor(ticks / ticksPerMinute);
		if (hours >= 24) {
			hours = hours - 24;
		}
		return (hours < 10 ? "0" + hours : hours) + ":" + (mins < 10 ? "0" + mins : mins);
	}	
}
