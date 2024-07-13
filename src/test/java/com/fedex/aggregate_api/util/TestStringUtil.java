package com.fedex.aggregate_api.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fedex.aggregate_api.util.StringUtil.commaSeparatedtoList;
import static com.fedex.aggregate_api.util.StringUtil.listToCommaSeparated;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStringUtil {
    @Test
    void testCommaSeparatedtoList() {
        assertEquals( emptyList(), commaSeparatedtoList(null));
        assertEquals( List.of("aap"), commaSeparatedtoList("aap"));
        assertEquals( List.of("aap"), commaSeparatedtoList("aap  ,  aap"));
        assertEquals( List.of("aap","noot"), commaSeparatedtoList("aap,noot"));
        assertEquals( List.of("aap","noot"), commaSeparatedtoList("aap, noot,aap"));
    }

    @Test
    void testListToCommaSeparated() {
        assertEquals( listToCommaSeparated(List.of("aap","noot")), "aap,noot");
        assertEquals( listToCommaSeparated(List.of("aap")), "aap");
        assertEquals( listToCommaSeparated(emptyList()), "");
    }
}
