package org.java.learn.summary.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageStrategy {

    public StorageStrategy() {

    }

    public StorageStrategy(Map<String, String> connInfo) {
    }

    public void runUpdate(String sql) {

    }

    public List<Map<String, Object>> runSelect(String sql, List<String> column) {
        return new ArrayList<Map<String, Object>>();
    }
}
