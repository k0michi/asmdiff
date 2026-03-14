package com.koyomiji.asmpatch;

import java.util.List;

public class ListHelper {
  public static <T> List<T> orEmpty(List<T> list) {
    return list != null ? list : List.of();
  }
}
