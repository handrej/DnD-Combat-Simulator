package fhtw.Model;

public class HighscoreEntry {

    private int score;
    private String name;

    public HighscoreEntry(String name, int score) {
        this.score = score;
        this.name = name;
    }

    public Integer getScore() {
        return this.score;
    }

    public String getName() {
        return name;
    }
}