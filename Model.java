package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    int score;
    int maxTile;

    public Model() {
        resetGameTiles();
    }

    private void saveState(Tile[][] tiles){
        Tile[][] savedTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                savedTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(savedTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if (!previousScores.isEmpty() & !previousStates.isEmpty()){
            score = previousScores.pop();
            gameTiles = previousStates.pop();
        }
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private void addTile(){
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size()>0){
            int randomNumber = (int) (emptyTiles.size() * Math.random());
            emptyTiles.get(randomNumber).value = Math.random() < 0.9 ? 2 : 4;
        }

    }

    private List<Tile> getEmptyTiles(){
        List<Tile> emptyTiles = new ArrayList<>();
        for (Tile[] arr : gameTiles) {
            for (Tile  tile : arr) {
                if (tile.isEmpty()) emptyTiles.add(tile);
            }
        }
        return emptyTiles;
    }

    void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        score = 0;
        maxTile = 0;
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles){
        int pos = 0;
        boolean flag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()){
                if (i != pos){
                    tiles[pos] = tiles[i];
                    tiles[i] = new Tile();
                    flag = true;
                }
                pos++;
            }
        }
        return flag;
    }

    private boolean mergeTiles(Tile[] tiles){
        int pos = 0;
        boolean flag = false;
        for (int i = 1; i < FIELD_WIDTH; i++) {
            if (tiles[i].value == tiles[pos].value && tiles[i].value != 0){
                tiles[pos].value += tiles[i].value;
                score += tiles[pos].value;
                if (tiles[pos].value > maxTile){
                    maxTile = tiles[pos].value;
                }
                tiles[i] = new Tile();
                compressTiles(tiles);
                pos++;
                flag = true;
                continue;
            }
            pos++;
        }

        return flag;

    }

    private Tile[][] rotateClockwise(Tile[][] tiles){
        Tile[][] result = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        int line = 0;
        int column =0;
        for (int i = 0 ; i < FIELD_WIDTH; i++) {
            for (int j = FIELD_WIDTH -1; j >= 0; j--) {
                result[line][column] = tiles[j][i];
                column++;
            }
            line++;
            column = 0;
        }
        return result;
    }

    public void left(){
        if (isSaveNeeded){
            saveState(gameTiles);
        }
        boolean flag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])){
                flag = true;
            }
        }
        if (flag){
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public void up(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    public void down(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    boolean canMove(){
        if (getEmptyTiles().size()>0){
            return true;
        }

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                Tile t = gameTiles[i][j];
                if (i < FIELD_WIDTH - 1 && gameTiles[i+1][j].value == t.value
                    || j < FIELD_WIDTH -1 && gameTiles[i][j+1].value == t.value){
                    return true;
                }
            }
        }
        return false;
        
        
    }

    public void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    private boolean hasBoardChanged(){
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != previousStates.peek()[i][j].value){
                    return true;
                }
            }
        }
        return false;
    }

    private MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1,0,move);
        move.move();
        if (hasBoardChanged()){
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4,Collections.reverseOrder());

        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));

        priorityQueue.peek().getMove().move();
    }


}
