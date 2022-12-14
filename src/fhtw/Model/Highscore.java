package fhtw.Model;

import java.util.*;
import java.io.*;
/**
 * GRADING NOTICE:
 * Shamelessly taken from https://github.com/Overv/BoggleSaga/blob/master/src/Model/Highscore.java
 * Feels in a way wrong to comment someone else code. Basically has everything to manage a highscore file to store Highscore values.
 * Might aswell be omitted or excluded from grading but it's here because it has solid File Handling for reference. Lots of Buffered, File Handling, Error Catching.
 * Really basic usage in project usage when initializing a New Game for loading and writing Highscores.
 * */
public class Highscore {

    private String pathToHighscoreFile = "src\\fhtw\\Resources\\highscores.txt";
    private ArrayList<HighscoreEntry> highscores;

    private static Highscore instance;

    public static Highscore getInstance() {
        if (instance == null) {
            instance = new Highscore();
        }

        return instance;
    }

    private Highscore() {
        highscores = new ArrayList<HighscoreEntry>();
        fillHighscoreList();
    }

    // Returns all the highscores sorted on descending order
    public ArrayList<HighscoreEntry> getHighscores() {
        return this.highscores;
    }

    public void fillHighscoreList() {
        File f = new File(pathToHighscoreFile);
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                //System.err.println("Could not create highscore file.");
            }
        }
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(f));
            while ((sCurrentLine = br.readLine()) != null) {
                String[] nameAndScore = sCurrentLine.split("__");
                try {
                    this.addHighscore(nameAndScore[0], Integer.parseInt(nameAndScore[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("File format is corrupted.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("Filled highscore list from: " + pathToHighscoreFile);
    }

    public void addHighscore(String name, int score) {
        highscores.add(new HighscoreEntry(name, score));
        this.saveHighscores();
        //sortHighscores();
    }

    public void saveHighscores() {
        File f = new File(pathToHighscoreFile);
        try {
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(this.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("Could not write to file " + pathToHighscoreFile);
        }
    }

    public String toString() {
        String res = "";
        for(HighscoreEntry entry : this.highscores) {
            res += entry.getName() + "__" + entry.getScore() + "\n";
        }
        return res;
    }

    /*public void sortHighscores() {
        Collections.sort(this.highscores, new ascendingComparator());
    }

    public class ascendingComparator implements Comparator<HighscoreEntry> {
        @Override
        public int compare(HighscoreEntry o1, HighscoreEntry o2) {
            return o2.getScore().compareTo(o1.getScore());
        }
    }*/
}

