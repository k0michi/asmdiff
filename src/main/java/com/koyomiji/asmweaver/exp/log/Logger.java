package com.koyomiji.asmweaver.exp.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public class Logger {
  private static volatile Logger instance = null;
  private static final Object instanceLock = new Object();
  private BufferedWriter writer = null;

  private Logger() {
    try {
      var logFilePath = getLogFilePath();
      var logDir = logFilePath.getParent();
      Files.createDirectories(logDir);
      writer = new BufferedWriter(new FileWriter(logFilePath.toFile(), true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Logger getInstance() {
    if (instance == null) {
      synchronized (instanceLock) {
        if (instance == null) {
          instance = new Logger();
        }
      }
    }
    return instance;
  }

  private Path getLogFilePath() {
    var now = OffsetDateTime.now();

    // Return the next available log file path
    // "logs/YYYYMMDD_HHMMSS_N.log"

    var logDir = "logs";
    var datePart = String.format("%04d%02d%02d_%02d%02d%02d", now.getYear(),
            now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond());
    var index = 1;

    while (true) {
      var fileName = String.format("%s_%d.log", datePart, index);
      var logFilePath = Path.of(logDir, fileName);

      if (!logFilePath.toFile().exists()) {
        return logFilePath;
      }

      index++;
    }
  }

  private void write(String message) {
    synchronized (this) {
      try {
        writer.write(message);
        writer.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    System.out.print(message);
  }

  public static void info(String message) {
    getInstance().write(String.format("[%s] [%s] [INFO] %s%n", OffsetDateTime.now(),
            Thread.currentThread().getName(), message));
  }

  public static void infof(String message, Object... args) {
    getInstance().write(String.format("[%s] [%s] [INFO] %s%n", OffsetDateTime.now(),
            Thread.currentThread().getName(), String.format(message, args)));
  }

  public static void error(String message) {
    getInstance().write(String.format("[%s] [%s] [ERROR] %s%n", OffsetDateTime.now(),
            Thread.currentThread().getName(), message));
  }

  public static void errorf(String message, Object... args) {
    getInstance().write(String.format("[%s] [%s] [ERROR] %s%n", OffsetDateTime.now(),
            Thread.currentThread().getName(), String.format(message, args)));
  }



//  public void log(String message) {
//    var now = OffsetDateTime.now();
//    write(String.format("[%s] [%s] %s%n", now,
//            Thread.currentThread().getName(), message));
//  }
//
//  public void log(Object... args) {
//    var sb = new StringBuilder();
//
//    for (var arg : args) {
//      sb.append(arg);
//      sb.append(" ");
//    }
//
//    log(sb.toString());
//  }

//  public void debug(String message) {
//    var now = OffsetDateTime.now();
//    write(String.format("[%s] [%s] [DEBUG] %s%n", now.toString(), Thread.currentThread().getName(), message));
//  }
//
//  public void debug(Object... args) {
//    var sb = new StringBuilder();
//
//    for (var arg : args) {
//      sb.append(arg);
//      sb.append(" ");
//    }
//
//    debug(sb.toString());
//  }
//
//  public void important(String message) {
//    var now = OffsetDateTime.now();
//    write(String.format("[%s] [%s] [IMPORTANT] %s%n", now.toString(), Thread.currentThread().getName(), message));
//  }
//
//  public void important(Object... args) {
//    var sb = new StringBuilder();
//
//    for (var arg : args) {
//      sb.append(arg);
//      sb.append(" ");
//    }
//
//    important(sb.toString());
//  }
//
//  public void warn(String message) {
//    var now = OffsetDateTime.now();
//    write(String.format("[%s] [%s] [WARN] %s%n", now.toString(), Thread.currentThread().getName(), message));
//  }
//
//  public void warn(Object... args) {
//    var sb = new StringBuilder();
//    for (var arg : args) {
//      sb.append(arg);
//      sb.append(" ");
//    }
//    warn(sb.toString());
//  }
//
//  public void error(String message) {
//    var now = OffsetDateTime.now();
//    write(String.format("[%s] [%s] [ERROR] %s%n", now.toString(), Thread.currentThread().getName(), message));
//  }
//
//  public void error(Object... args) {
//    var sb = new StringBuilder();
//    for (var arg : args) {
//      sb.append(arg);
//      sb.append(" ");
//    }
//    error(sb.toString());
//  }
}
