package fhtw.Controller;

import fhtw.Model.*;
import fhtw.Model.Character;
import fhtw.Model.Dungeon;
import fhtw.View.ViewManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.*;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.*;

public class GameController {
    private boolean endgame = false;
    //private final Client client;
    private final PrintWriter output;
    private final String username;
    private final Socket client;
    private ArrayList<Character> party;
    private ArrayList<ArrayList<ArrayList<Character>>> dungeon;

    private TreeMap<Integer, Character> init_room = new TreeMap<>();
    private TreeMap<Integer, Character> init_turn = new TreeMap<>();
    private ArrayList<ImageView> battlefield = new ArrayList<>();
    private ArrayList<Text> battlefield_data = new ArrayList<>();
    private ArrayList<ImageView> partymember = new ArrayList<>();
    private ArrayList<Text> partymember_data = new ArrayList<>();

    private int score;
    private int stage = 0, rooms = 0, round = 0;
    private Character target;
    private Character wound;
    private Character turn;

    public GameController(Client client) {
        this.client = client.getClient();
        this.username = client.getUsername();
        this.output = client.getOutput();
    }

    @FXML
    private Button roll_party;

    @FXML
    private Button btn_logout;

    @FXML
    private Button action_attack;

    @FXML
    private Button action_defend;

    @FXML
    private Button action_potion;

    @FXML
    private Button start_game;

    @FXML
    private ImageView party_00;

    @FXML
    private ImageView party_01;

    @FXML
    private ImageView party_02;

    @FXML
    private ImageView party_03;

    @FXML
    private Text party_data_00;

    @FXML
    private Text party_data_01;

    @FXML
    private Text party_data_02;

    @FXML
    private Text party_data_03;

    @FXML
    private ImageView enemy_00;

    @FXML
    private ImageView enemy_01;

    @FXML
    private ImageView enemy_02;

    @FXML
    private ImageView enemy_03;

    @FXML
    private Text enemy_data_00;

    @FXML
    private Text enemy_data_01;

    @FXML
    private Text enemy_data_02;

    @FXML
    private Text enemy_data_03;

    @FXML
    private TextArea txt_dungeon;

    @FXML
    private TextField txt_input;

    @FXML
    private Text txt_status_dungeon;

    @FXML
    private Text txt_status_chatroom;

    @FXML
    private Button btn_sendButton;

    @FXML
    void roll_party(ActionEvent event) throws IOException {
        this.party = Dungeon.createParty();
        for (Character member : this.party) {
            int hp_pool = member.getHd() + Dungeon.as_mod(member.getAs_con());
            partymember_data.get(this.party.indexOf(member)).setText(member.getKlass() + " - HP: " + member.getHp() + " / " + hp_pool);
            partymember.get(this.party.indexOf(member)).setVisible(member.getHp() > 0);
        }
        for (Character member : this.party) {
            partymember.get(this.party.indexOf(member)).setImage(member.imgVal);
        }
        party_01.setVisible(true);
        party_02.setVisible(true);
        party_02.setVisible(true);
        party_03.setVisible(true);
        party_00.setVisible(true);
    }

    @FXML
    void sendChat(ActionEvent event) {
        if (txt_input.getText().charAt(0) == '@'){
            output.println(txt_input.getText());
        }else{
            output.println("!" + username + " " + txt_input.getText());
        }
        txt_input.setText("");
    }

    @FXML
    void choose_attack(MouseEvent event) {
        String obj_id = ((ImageView) event.getSource()).getId();
        target = encounter().get(Integer.parseInt((obj_id.substring(obj_id.length() - 1))));
        for (Character member : this.party) {
            partymember.get(this.party.indexOf(member)).setOpacity(0.4);
        }
        for (Character monster : encounter()) {
            battlefield.get(encounter().indexOf(monster)).setOpacity(0.4);
        }
        if (Character.class_list.contains(turn.getKlass())) {
            partymember.get(this.party.indexOf(turn)).setOpacity(1);
        }
        battlefield.get(encounter().indexOf(target)).setOpacity(1);
    }

    @FXML
    void action_attack(ActionEvent event) {
        action_attack.setVisible(false);
        action_defend.setVisible(false);
        action_potion.setVisible(false);
        if (Character.class_list.contains(turn.getKlass())) {
            if (target == null) {
                randomizeTarget(encounter());
            }
            if (target.getHp() <= 0) {
                randomizeTarget(encounter());
            }
            if (Character.class_list.contains(target.getKlass())){
                randomizeTarget(encounter());
            }
        } else {
            randomizeTarget(this.party);
        }
        int attackRoll = diceRoller(20) + Dungeon.as_mod(turn.getAs_str()) + turn.getBonus();
        int armorscore = target.getAc() + target.getBonus_ac();
        updateText("Attack Roll on " + target.getKlass() + " : " + attackRoll + "(" + armorscore + ")");
        if (attackRoll >= armorscore) {
            int damageRoll = diceRoller(20);
            updateText("You rolled " + damageRoll + "dmg!");
            target.setHp(target.getHp() - damageRoll);
            if (target.getHp() <= 0) {
                target.setHp(0);
                updateText(target.getKlass() + " is defeated.");
            }
        } else {
            updateText("You missed!?");
        }
        game_loop();
    }

    @FXML
    void action_defend(ActionEvent event) {
        action_attack.setVisible(false);
        action_defend.setVisible(false);
        action_potion.setVisible(false);
        turn.setBonus_ac(diceRoller(6));
        game_loop();
    }

    @FXML
    void choose_heal(MouseEvent event) {
        // string.substring(string.length() - 1));
        String obj_id = ((ImageView) event.getSource()).getId();
        if (Character.class_heal.contains(turn.getKlass())){
            wound = this.party.get(Integer.parseInt((obj_id.substring(obj_id.length() - 1))));
            for (Character monster : encounter()) {
                battlefield.get(encounter().indexOf(monster)).setOpacity(0.4);
            }
            for (Character member : this.party) {
                partymember.get(this.party.indexOf(member)).setOpacity(0.4);
            }
            if (Character.class_list.contains(turn.getKlass())) {
                partymember.get(this.party.indexOf(turn)).setOpacity(1);
            }
            partymember.get(this.party.indexOf(wound)).setOpacity(1);
        }
    }

    @FXML
    void action_potion(ActionEvent event) {
        action_attack.setVisible(false);
        action_defend.setVisible(false);
        action_potion.setVisible(false);
        int spellRoll = diceRoller(8) + Dungeon.as_mod(turn.getAs_wis());
        updateText("Healing Roll " + spellRoll);
        if (wound == null) {
            wound = this.party.get((new Random().nextInt(this.party.size())));
        }
        int hp_plus = (wound.getHp() + spellRoll);
        int hp_pool = wound.getHd() + Dungeon.as_mod(wound.getAs_con());
        wound.setHp(hp_plus <= hp_pool ? hp_plus : hp_pool);
        updateText(wound.getKlass() + " was healed to " + hp_plus + " (" + hp_pool + ")");
        game_loop();
    }

    @FXML
    void start_game(ActionEvent event) throws IOException {
        this.endgame = false;
        score = 0;
        start_game.setVisible(false);
        btn_logout.setVisible(false);
        roll_party.setVisible(false);
        if (this.party == null){
            this.party = Dungeon.createParty();
        }
        for (Character member : this.party) {
            partymember.get(this.party.indexOf(member)).setImage(member.imgVal);
        }
        this.dungeon = Dungeon.createDungeon(8,4);
        for (Character member : this.party) {
            partymember.get(this.party.indexOf(member)).setImage(member.imgVal);
        }
        //show table
        enemy_01.setVisible(true);
        enemy_02.setVisible(true);
        enemy_03.setVisible(true);
        enemy_00.setVisible(true);
        party_01.setVisible(true);
        party_02.setVisible(true);
        party_02.setVisible(true);
        party_03.setVisible(true);
        party_00.setVisible(true);
        //..sleep?
        party_00.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_heal(clonk);
            clonk.consume();
        });
        party_01.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_heal(clonk);
            clonk.consume();
        });
        party_02.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_heal(clonk);
            clonk.consume();
        });
        party_03.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_heal(clonk);
            clonk.consume();
        });

        enemy_00.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_attack(clonk);
            clonk.consume();
        });
        enemy_01.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_attack(clonk);
            clonk.consume();
        });
        enemy_02.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_attack(clonk);
            clonk.consume();
        });
        enemy_03.addEventHandler(MouseEvent.MOUSE_CLICKED, clonk -> {
            choose_attack(clonk);
            clonk.consume();
        });
        updateText("Your descend into the Dungeon begins.");
        output.println("!" + username + " started his descend");
        //start game
        game_loop();
    }

    /**
     * Progress checks room status after every move made and increases counters if necessary
     */
    private void progress(){
        int party_hp = 0, enemy_hp = 0;
        for (Character member : this.party) {
            party_hp += member.getHp();
        }
        for (Character monster : encounter()) {
            enemy_hp += monster.getHp();
        }
        if (party_hp <= 0) {
            output.println("!" + username + " adventure ended.");
            updateText("Your party perished! Game over. End score " + score);
            Highscore.getInstance().addHighscore(username, score);
            this.endgame = true;
        }
        if (enemy_hp <= 0) {
            txt_dungeon.setText("");
            int day_xp = 0;
            for (Character e : encounter()) {
                day_xp += e.getXp();
            }
            score += day_xp;
            if (this.dungeon.size() == (this.stage+1) && this.dungeon.get((this.dungeon.size()-1)).size() == (this.rooms+1)){
                output.println("!" + username + " cleared the Dungeon!!");
                updateText("Congratulations! You cleared the Dungeon. Total score " + score);
                Highscore.getInstance().addHighscore(username, score);
                this.endgame = true;
            }
            if (this.endgame == false){
                this.rooms++;
                if (this.dungeon.get(this.stage).size() <= this.rooms) {
                    this.stage++;
                    this.rooms = 0;
                } else if ((this.stage + 1) % 4 == 0) {
                    this.stage++;
                    this.rooms = 0;
                    output.println("!" + username + " just cleared a boos room!");
                }
                if (this.rooms % 2 == 0){
                    updateText("You recover some HP while resting.");
                    for (Character p : this.party) {
                        int hp_plus = (p.getHp() + diceRoller(p.getHd()));
                        int hp_pool = p.getHd() + Dungeon.as_mod(p.getAs_con());
                        p.setHp(hp_plus <= hp_pool ? hp_plus : hp_pool);
                    }
                }
                updateText("Stage: " + (this.stage+1) + " Room: " + (this.rooms+1));
                updateText("================");
                init_room.clear();
                for (Character p : this.party) {
                    p.setBonus_ac(0);
                    p.setXp(p.getXp() + day_xp);
                    //p.setHp(turn.getHd() + Dungeon.as_mod(turn.getAs_con()));
                }
            }
        }
        for (Text bf_data : battlefield_data){
            bf_data.setText("");
        }
        for (Text pm_data : partymember_data){
            pm_data.setText("");
        }
        for (ImageView monster : battlefield) {
            monster.setImage(null);
        }
    }

    /**
     * Main loop setting ui data and turn order for actions
     */
    public void game_loop() {
        int initiative;
        //Check party status after encounter
        progress();
        if (this.endgame == false) {
            for (Character member : this.party) {
                partymember.get(this.party.indexOf(member)).setOpacity(1);
            }
            for (Character monster : encounter()) {
                battlefield.get(encounter().indexOf(monster)).setOpacity(1);
            }
            for (Character member : this.party) {
                int hp_pool = member.getHd() + Dungeon.as_mod(member.getAs_con());
                partymember_data.get(this.party.indexOf(member)).setText(member.getKlass() + " - HP: " + member.getHp() + " / " + hp_pool);
                partymember.get(this.party.indexOf(member)).setVisible(member.getHp() > 0);
            }
            for (Character monster : encounter()) {
                battlefield_data.get(encounter().indexOf(monster)).setText(monster.getKlass() + " - HP: " + monster.getHp());
                battlefield.get(encounter().indexOf(monster)).setVisible(monster.getHp() > 0);
                battlefield.get(encounter().indexOf(monster)).setImage(monster.getImgVal());
            }
            updateText("~~~ Round: " + ++this.round + " ~~~");
            txt_status_dungeon.setText("Stage: " + (this.stage + 1) + " Room: " + (this.rooms + 1) + " Round: " + (this.round + 1));
            /*
             * ENCOUNTER
             * */
            init_room.values().removeIf(character -> character.getHp() <= 0);
            init_turn.values().removeIf(character -> character.getHp() <= 0);

            ArrayList<Character> members = new ArrayList<>(this.party);
            members.addAll(encounter());
            //init_room order stays same in rounds
            /*
             * Setup order to stay same in rounds
             * */
            if (init_room.size() == 0) {
                for (Character member : members) {
                    if (member.getHp() > 0) {
                        do {
                            initiative = diceRoller(20) + Dungeon.as_mod(member.getAs_dex());
                            member.setOrder(initiative);
                            if (!init_room.containsKey(initiative)) {
                                init_room.put(initiative, member);
                            }
                        } while (!init_room.containsValue(member));
                    }
                }
            }
            if (init_turn.size() == 0) {
                //init_turn = (TreeMap)init_room.clone();
                init_turn = new TreeMap<>(init_room);
            }
            turn = init_turn.get(init_turn.lastKey());
            //your reign is already over
            init_turn.remove(init_turn.lastKey());

            /*
             * Choose action
             * */
            updateText("Turn " + turn.getKlass() + " HP:(" + turn.getHp() + ")");
            if (Character.class_list.contains(turn.getKlass())) {
                for (Character member : this.party) {
                    partymember.get(this.party.indexOf(member)).setOpacity(0.4);
                }
                //oob on new game
                partymember.get(this.party.indexOf(turn)).setOpacity(1);
                action_attack.setVisible(true);
                action_defend.setVisible(true);
                if (Character.class_heal.contains(turn.getKlass())) {
                    for (Character member : this.party) {
                        partymember.get(this.party.indexOf(member)).setOpacity(1);
                    }
                    action_potion.setVisible(true);
                }
            } else {
                action_attack.fire();
            }
        }else{
            action_attack.setVisible(false);
            action_defend.setVisible(false);
            action_potion.setVisible(false);
            start_game.setVisible(true);
            btn_logout.setVisible(true);
            roll_party.setVisible(true);
            enemy_01.setVisible(false);
            enemy_02.setVisible(false);
            enemy_03.setVisible(false);
            enemy_00.setVisible(false);
            party_01.setVisible(false);
            party_02.setVisible(false);
            party_02.setVisible(false);
            party_03.setVisible(false);
            party_00.setVisible(false);
            this.init_room = new TreeMap<>();
            this.init_turn = new TreeMap<>();
            this.wound = null;
            this.turn = null;
            this.target = null;
            this.stage = 0;
            this.round = 0;
            this.rooms = 0;
            this.party = null;
            this.dungeon = null;
        }
    }

    @FXML
    public void initialize() throws IOException {
        /*
        * High intensity operation ?
        * */
        ArrayList<HighscoreEntry> leaderboard = Highscore.getInstance().getHighscores();
        leaderboard.sort((o1, o2) -> o2.getScore().compareTo(o1.getScore()));

        updateText("(Local) Highscores\n=============");
        int max = 0;
        for (HighscoreEntry highscores : leaderboard){
            if (max == 4) break;
            updateText("(" + (leaderboard.indexOf(highscores)+1) + ") " + highscores.getName() + " " +highscores.getScore() );
            max++;
        }
        this.party = Dungeon.createParty();
        //==========================
        start_game.setVisible(true);
        battlefield.add(enemy_00);
        battlefield.add(enemy_01);
        battlefield.add(enemy_02);
        battlefield.add(enemy_03);
        battlefield_data.add(enemy_data_00);
        battlefield_data.add(enemy_data_01);
        battlefield_data.add(enemy_data_02);
        battlefield_data.add(enemy_data_03);
        partymember.add(party_00);
        partymember.add(party_01);
        partymember.add(party_02);
        partymember.add(party_03);
        partymember_data.add(party_data_00);
        partymember_data.add(party_data_01);
        partymember_data.add(party_data_02);
        partymember_data.add(party_data_03);
        action_attack.setVisible(false);
        action_defend.setVisible(false);
        action_potion.setVisible(false);

    }

    /*
    * Helper Functions
    * */

    /**
     * Listener incoming messages
     * @param data messageProperty new value
     */
    public void updateText(String data) {
        txt_dungeon.appendText(data + "\n");
    }

    /**
     * Lister incoming chatroom data
     * @param data chatroomProperty new value
     */
    public void updateRoom(String data) {
        txt_status_chatroom.setText("Online: " + data);
    }

    /**
     * Randomize targets for enemies and on player turn if none are chosen, cant be chosen due to health pool, or part of own party
     * @param pool Character ArrayList to choose from
     */
    private void randomizeTarget(ArrayList<Character> pool) {
        ArrayList<Integer> targets = new ArrayList<>();
        for (Character p : pool) {
            if (p.getHp() > 0) {
                targets.add(pool.indexOf(p));
            }
        }
        target = pool.get(targets.get(new Random().nextInt(targets.size())));
    }

    /**
     * Shorthandle to current encounter
     * @return ArrayList of Enemies in current room
     */
    ArrayList<Character> encounter() {
        return this.dungeon.get(this.stage).get(this.rooms);
    }

    /**
     * Gamba
     * @param sides Amount of sides for the dice
     * @return Pseudo random number in the range of the dice
     */
    private static int diceRoller(int sides) {
        Random rng = new Random();
        return rng.nextInt(sides) + 1;
    }

    /**
     * Logout Button Action Handler
     * @param loginManager Scene object reference
     */
    public void initLogout(ViewManager loginManager) {
        btn_logout.setOnAction(event -> {
            loginManager.logout();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
