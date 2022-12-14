package fhtw.Model;

import javafx.scene.image.Image;
import java.util.*;

/**
 * Construct to create both types of Characters (Getter and Setters shared by both Party and Enemy).
 */
public class Character {
    private String klass;
    private int as_str, as_dex, as_con, as_int, as_wis, as_cha, hp, ac, hd, xp, order, bonus, bonus_ac;
    public static ArrayList<String> class_list = new ArrayList<>(Arrays.asList("Barbarian", "Bard", "Cleric", "Druid", "Fighter", "Monk", "Paladin", "Ranger", "Rogue", "Sorcerer", "Warlock", "Wizard"));
    public static ArrayList<String> class_heal = new ArrayList<>(Arrays.asList( "Bard", "Cleric", "Druid", "Paladin", "Ranger"));
    public Image imgVal;

    /*public enum Classes {
        WARLOCK, CLERIC, DRUID, PALADIN, ROGUE, BARBARIAN, WIZARD, ENEMY
    }*/

    public Character() {
    }

    public Image getImgVal() {
        return imgVal;
    }

    public void setImgVal(Image imgVal) {
        this.imgVal = imgVal;
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public int getAs_str() {
        return as_str;
    }

    public void setAs_str(int as_str) {
        this.as_str = as_str;
    }

    public int getAs_dex() {
        return as_dex;
    }

    public void setAs_dex(int as_dex) {
        this.as_dex = as_dex;
    }

    public int getAs_con() {
        return as_con;
    }

    public void setAs_con(int as_con) {
        this.as_con = as_con;
    }

    public int getAs_int() {
        return as_int;
    }

    public void setAs_int(int as_int) {
        this.as_int = as_int;
    }

    public int getAs_wis() {
        return as_wis;
    }

    public void setAs_wis(int as_wis) {
        this.as_wis = as_wis;
    }

    public int getAs_cha() {
        return as_cha;
    }

    public void setAs_cha(int as_cha) {
        this.as_cha = as_cha;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getHd() {
        return hd;
    }

    public void setHd(int hd) {
        this.hd = hd;
    }

    public int getBonus_ac() {
        return bonus_ac;
    }

    public void setBonus_ac(int bonus_ac) {
        this.bonus_ac = bonus_ac;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }
}
