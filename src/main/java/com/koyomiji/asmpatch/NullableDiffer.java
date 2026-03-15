package com.koyomiji.asmpatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NullableDiffer<T, U> {
  private final IDiffer<T, U> differ;

  public NullableDiffer(IDiffer<T, U> differ) {
    this.differ = differ;
  }

  public NullablePatch<T, U> diff(T oldValue, T newValue) {
    var oldList = oldValue == null ? List.<T>of() : List.of(oldValue);
    var newList = newValue == null ? List.<T>of() : List.of(newValue);

    int m = oldList.size();
    int n = newList.size();

    int[] delCosts = new int[m];
    for (int i = 0; i < m; i++) {
      delCosts[i] = differ.distance(oldList.get(i), null);
    }

    int[] insCosts = new int[n];
    for (int j = 0; j < n; j++) {
      insCosts[j] = differ.distance(null, newList.get(j));
    }

    var dp = new int[m + 1][n + 1];

    for (int i = 1; i <= m; i++) {
      dp[i][0] = dp[i - 1][0] + delCosts[i - 1];
    }

    for (int j = 1; j <= n; j++) {
      dp[0][j] = dp[0][j - 1] + insCosts[j - 1];
    }

    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        int del = dp[i - 1][j] + delCosts[i - 1];
        int ins = dp[i][j - 1] + insCosts[j - 1];

        if (!differ.canMatch(oldList.get(i - 1), newList.get(j - 1))) {
          dp[i][j] = Math.min(del, ins);
          continue;
        }

        int sub = dp[i - 1][j - 1] + differ.distance(oldList.get(i - 1), newList.get(j - 1));

        dp[i][j] = Math.min(del, Math.min(ins, sub));
      }
    }

    var entries = new ArrayList<NullablePatch.Entry<T, U>>();
    int i = m;
    int j = n;

    while (i > 0 && j > 0) {
      if (dp[i][j] == dp[i - 1][j] + delCosts[i - 1]) {
        entries.add(NullablePatch.Entry.remove());
        i--;
      } else if (dp[i][j] == dp[i][j - 1] + insCosts[j - 1]) {
        entries.add(NullablePatch.Entry.add(newList.get(j - 1)));
        j--;
      } else {
        entries.add(NullablePatch.Entry.match(differ.diff(oldList.get(i - 1), newList.get(j - 1))));
        i--;
        j--;
      }
    }

    while (i > 0) {
      entries.add(NullablePatch.Entry.remove());
      i--;
    }

    while (j > 0) {
      entries.add(NullablePatch.Entry.add(newList.get(j - 1)));
      j--;
    }

    Collections.reverse(entries);

    return new NullablePatch<>(entries);
  }
}