package us.crast.mondochest.util;

import java.util.ArrayList;
import java.util.List;

public class DecodeResults<T> {
    public List<T> validValues = new ArrayList<T>();
    public List<Object> failedValues = new ArrayList<Object>();
    
    public boolean hasFailures() {
        return !failedValues.isEmpty();
    }
}
