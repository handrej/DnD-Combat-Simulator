package fhtw.API;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Classes {

    private int count;
    private ArrayList<Map<String, String>> results;

    public ArrayList<Map<String, String>> getResults() {
        return results;
    }

    public void setResults(ArrayList<Map<String, String>> results) {
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}