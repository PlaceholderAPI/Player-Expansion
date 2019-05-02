package com.extendedclip.papi.expansion.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

  public static String getPing(Player p) {
    try {
      Method getHandleMethod = p.getClass().getDeclaredMethod("getHandle", new Class[0]);
      Object nmsplayer = getHandleMethod.invoke(p, new Object[0]);
      Field pingField = nmsplayer.getClass().getDeclaredField("ping");
      return String.valueOf(pingField.getInt(nmsplayer));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0";
  }

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
    long mins = (long) Math.floor(ticks / ticksPerMinute);
    if (hours >= 24) {
      hours = hours - 24;
    }
    return (hours < 10 ? "0" + hours : hours) + ":" + (mins < 10 ? "0" + mins : mins);
  }

  public static String getCardinalDirection(Player player) {
    double rotation = player.getLocation().getYaw() - 180.0F;
    if (rotation < 0.0D) {
      rotation += 360.0D;
    }
    if ((0.0D <= rotation) && (rotation < 22.5D)) {
      return "N";
    }
    if ((22.5D <= rotation) && (rotation < 67.5D)) {
      return "NE";
    }
    if ((67.5D <= rotation) && (rotation < 112.5D)) {
      return "E";
    }
    if ((112.5D <= rotation) && (rotation < 157.5D)) {
      return "SE";
    }
    if ((157.5D <= rotation) && (rotation < 202.5D)) {
      return "S";
    }
    if ((202.5D <= rotation) && (rotation < 247.5D)) {
      return "SW";
    }
    if ((247.5D <= rotation) && (rotation < 292.5D)) {
      return "W";
    }
    if ((292.5D <= rotation) && (rotation < 337.5D)) {
      return "NW";
    }
    if ((337.5D <= rotation) && (rotation < 360.0D)) {
      return "N";
    }
    return null;
  }

  public static ItemStack itemInHand(Player p) {
    try {
      return p.getInventory().getItemInMainHand();
    } catch (NoSuchMethodError e) {
      return p.getInventory().getItemInHand();
    }
  }

}
