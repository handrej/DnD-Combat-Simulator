package fhtw.API;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassesData {

    private int hit_die;
    private ArrayList<Map<String, String>> saving_throws;

    public int getHit_die() {
        return hit_die;
    }

    public void setHit_die(int hit_die) {
        this.hit_die = hit_die;
    }

    public ArrayList<Map<String, String>> getSaving_throws() {
        return saving_throws;
    }

    public void setSaving_throws(ArrayList<Map<String, String>> saving_throws) {
        this.saving_throws = saving_throws;
    }
}