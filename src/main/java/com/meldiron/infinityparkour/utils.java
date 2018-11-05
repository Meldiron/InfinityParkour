package com.meldiron.infinityparkour;

import java.util.*;

public class utils {
    public static <K, V extends Comparable<? super V>> List<Map.Entry<String, Integer>> entriesSortedByValues(Map<String, Integer> map) {

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

        Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });

        return sortedEntries;
    }
}
