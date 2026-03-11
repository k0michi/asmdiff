package com.koyomiji.asmpatch;

import java.util.ArrayList;
import java.util.List;

public class ListDiffer<T, U extends IPatch<T>, V> {
  private final IDiffer<T, U> differ;
  private final IKeyProvider<T, V> keyProvider;

  public ListDiffer(IDiffer<T, U> differ, IKeyProvider<T, V> keyProvider) {
    this.differ = differ;
    this.keyProvider = keyProvider;
  }

  public ListPatch<T, U, V> diff(List<T> listA, List<T> listB) {
    var dp = new int[listA.size() + 1][listB.size() + 1];

    for (int i = 0; i <= listA.size(); i++) {
      dp[i][0] = i;
    }

    for (int j = 0; j <= listB.size(); j++) {
      dp[0][j] = j;
    }

    var entries = new ArrayList<ListPatch.Entry<T, U, V>>();

    for (int i = 1; i <= listA.size(); i++) {
      for (int j = 1; j <= listB.size(); j++) {
        if (keyProvider.compareValues(listA.get(i - 1), listB.get(j - 1))) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1);
        }
      }
    }

    int i = listA.size();
    int j = listB.size();

    while (i > 0 && j > 0) {
      if (differ.canDiff(listA.get(i - 1), listB.get(j - 1))) {
        entries.add(ListPatch.Entry.match(keyProvider.getKey(listA.get(i - 1)), differ.diff(listA.get(i - 1), listB.get(j - 1))));
        i--;
        j--;
      } else if (dp[i][j] == dp[i - 1][j] + 1) {
        entries.add(ListPatch.Entry.remove(keyProvider.getKey(listA.get(i - 1))));
        i--;
      } else if (dp[i][j] == dp[i][j - 1] + 1) {
        entries.add(ListPatch.Entry.add(listB.get(j - 1)));
        j--;
      }
    }

    while (i > 0) {
      entries.add(ListPatch.Entry.remove(keyProvider.getKey(listA.get(i - 1))));
      i--;
    }

    while (j > 0) {
      entries.add(ListPatch.Entry.add(listB.get(j - 1)));
      j--;
    }

    return new ListPatch<>(entries);
  }
}
