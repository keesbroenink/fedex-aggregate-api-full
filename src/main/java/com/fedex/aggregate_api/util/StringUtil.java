package com.fedex.aggregate_api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {
    /**
     * When the input is null we return an empty list. Extra functionality: remove duplicates
     * @param element
     * @return
     */
    public static List<String> commaSeparatedtoList(String element) {
        return element == null ?
                Collections.emptyList() :
                Arrays.stream(element.split(",")).map(s->s.trim()).distinct().collect(Collectors.toList());
    }
    public static String listToCommaSeparated(List<String> elements) {
        return elements.stream()
                .collect(Collectors.joining(","));
    }
}
