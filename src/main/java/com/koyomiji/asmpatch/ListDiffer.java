package com.koyomiji.asmpatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListDiffer<T, U, V> {
  private final IDiffer<T, U> differ;

  public ListDiffer(IDiffer<T, U> differ) {
    this.differ = differ;
  }

  public ListPatch<T, U, V> diff(List<T> listA, List<T> listB) {
    int m = listA.size();
    int n = listB.size();

    int[] delCosts = new int[m];
    for (int i = 0; i < m; i++) {
      delCosts[i] = differ.distance(listA.get(i), null);
    }

    int[] insCosts = new int[n];
    for (int j = 0; j < n; j++) {
      insCosts[j] = differ.distance(null, listB.get(j));
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

        if (!differ.canMatch(listA.get(i - 1), listB.get(j - 1))) {
          dp[i][j] = Math.min(del, ins);
          continue;
        }

        int sub = dp[i - 1][j - 1] + differ.distance(listA.get(i - 1), listB.get(j - 1));

        dp[i][j] = Math.min(del, Math.min(ins, sub));
      }
    }

    var entries = new ArrayList<ListPatch.Entry<T, U, V>>();
    int i = m;
    int j = n;

    while (i > 0 && j > 0) {
      if (dp[i][j] == dp[i - 1][j] + delCosts[i - 1]) {
        entries.add(ListPatch.Entry.remove());
        i--;
      } else if (dp[i][j] == dp[i][j - 1] + insCosts[j - 1]) {
        entries.add(ListPatch.Entry.add(listB.get(j - 1)));
        j--;
      } else {
        var patch = differ.diff(listA.get(i - 1), listB.get(j - 1));
        entries.add(ListPatch.Entry.match(patch));
        i--;
        j--;
      }
    }

    while (i > 0) {
      entries.add(ListPatch.Entry.remove());
      i--;
    }

    while (j > 0) {
      entries.add(ListPatch.Entry.add(listB.get(j - 1)));
      j--;
    }

    Collections.reverse(entries);

    return new ListPatch<>(entries);
  }

  public int distance(List<T> listA, List<T> listB) {
    int m = listA.size();
    int n = listB.size();

    int[] delCosts = new int[m];
    for (int i = 0; i < m; i++) {
      delCosts[i] = differ.distance(listA.get(i), null);
    }

    int[] insCosts = new int[n];
    for (int j = 0; j < n; j++) {
      insCosts[j] = differ.distance(null, listB.get(j));
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

        if (!differ.canMatch(listA.get(i - 1), listB.get(j - 1))) {
          dp[i][j] = Math.min(del, ins);
          continue;
        }

        int sub = dp[i - 1][j - 1] + differ.distance(listA.get(i - 1), listB.get(j - 1));

        dp[i][j] = Math.min(del, Math.min(ins, sub));
      }
    }

    return dp[m][n];
  }
}
