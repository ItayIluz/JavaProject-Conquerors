package engine;

import engine.xmlclasses.Game;
import engine.xmlclasses.GameDescriptor;
import engine.xmlclasses.Teritory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class GameData implements Serializable {

    public enum GameType {
        BASIC, MULTIPLAYER, DYNAMIC_MULTIPLAYER
    }

    private static final long serialVersionUID = 4L;
    private GameType gameType;
    private int totalRounds;
    private int currentRound;
    private int initialFunds;
    private boolean gameStarted = false;
    private Board gameBoard;
    private HashMap<String, Unit> gameUnits;
    private Player[] players;
    private GameHistory[] gameHistory;

    private boolean isValidGameConfig = true;
    private Stack<String> gameErrorMessages = new Stack<>();

    public GameData(){};

    public GameData(GameType gameType, int totalRounds, int initialFunds, Board gameBoard, HashMap<String, Unit> gameUnits, Player[] players){
        this.gameType = gameType;
        this.totalRounds = totalRounds;
        this.initialFunds = initialFunds;
        this.gameBoard = gameBoard;
        this.gameUnits = gameUnits;
        this.players = players;
        this.gameHistory = new GameHistory[totalRounds];
    }

    public GameData(String xmlPath){

        File xmlFile = new File(xmlPath);

        if(xmlFile != null && !xmlFile.exists()) {
            this.setGameInvalid("The file you specified does not exist.");
        } else if(xmlFile.isFile() && !xmlFile.getName().substring(xmlFile.getName().lastIndexOf(".")+1).equals("xml")) {
            this.setGameInvalid("The file you specified is not an XML file.");
        } else {
            try {
                JAXBContext jc = JAXBContext.newInstance("engine.xmlclasses");
                Unmarshaller u = jc.createUnmarshaller();
                GameDescriptor gameDescriptor = (GameDescriptor) u.unmarshal(xmlFile);
                Game schemaGame = gameDescriptor.getGame();

                // GameType Tag
                String gameTypeNodeText = gameDescriptor.getGameType();

                this.gameType = GameType.valueOf(gameTypeNodeText.toUpperCase());

                switch (this.gameType) {
                    case BASIC: {
                        this.players = new Player[2];
                        this.players[0] = new Player(1, "Player1");
                        this.players[1] = new Player(2, "Player2");
                        break;
                    }
                    case MULTIPLAYER: {
                        // TODO Parse player XML
                        break;
                    }
                    case DYNAMIC_MULTIPLAYER: {
                        // TODO Parse player XML
                        break;
                    }
                    default: {
                        this.setGameInvalid("Unknown GameType in XML file.");
                        break;
                    }
                }

                // GameData Tag
                this.totalRounds = schemaGame.getTotalCycles().intValue();
                this.gameHistory = new GameHistory[totalRounds];

                // Board & Territories
                ArrayList<Territory> predefinedTerritories = new ArrayList<>();
                int gameBoardRows = schemaGame.getBoard().getRows().intValue();
                int gameBoardColumns = schemaGame.getBoard().getColumns().intValue();

                // territory default values
                int territoryDefaultProfit = schemaGame.getTerritories().getDefaultProfit() != null ? schemaGame.getTerritories().getDefaultProfit().intValue() : -1;
                int territoryDefaultArmyThreshold = schemaGame.getTerritories().getDefaultArmyThreshold() != null ? schemaGame.getTerritories().getDefaultArmyThreshold().intValue() : -1;

                if (gameBoardRows < 2 || gameBoardRows > 30)
                    this.setGameInvalid("The number of board rows is not between 2 and 30.");

                if (gameBoardColumns < 3 || gameBoardColumns > 30)
                    this.setGameInvalid("The number of board columns is not between 3 and 30.");

                // Defined Territories
                List<Teritory> schemaTerritories = schemaGame.getTerritories().getTeritory();

                Set<Integer> validateTerritoryIds = new HashSet<>();
                int territoryCounter = 0;

                for (int i = 0; i < schemaTerritories.size(); i++) {
                    Teritory currentTerritory = schemaTerritories.get(i);

                    territoryCounter++;

                    int territoryId = currentTerritory.getId().intValue();

                    if (validateTerritoryIds.contains(territoryId)) {
                        this.setGameInvalid("Settings for territory with id " + territoryId + " is defined more than once.");
                    } else if (territoryId > (gameBoardRows * gameBoardColumns)) {
                        this.setGameInvalid("Territory with id " + territoryId + " is beyond the board bounds.");
                    } else {
                        validateTerritoryIds.add(territoryId);

                        int territoryProfit = currentTerritory.getProfit().intValue();
                        int territoryArmyThreshold = currentTerritory.getArmyThreshold().intValue();

                        if (territoryProfit <= 0)
                            this.setGameInvalid("The value for profit of territory with id " + territoryId + " is not a whole positive number.");

                        if (territoryArmyThreshold <= 0)
                            this.setGameInvalid("The value for army threshold of territory with id " + territoryId + " is not a whole positive number.");

                        predefinedTerritories.add(new Territory(territoryId, territoryProfit, territoryArmyThreshold));
                    }
                }

                if ((territoryDefaultProfit == -1 || territoryDefaultArmyThreshold == -1) && territoryCounter != gameBoardRows * gameBoardColumns)
                    this.setGameInvalid("Territory default values are not set and not all territories are explicitly defined.");
                else
                    this.gameBoard = new Board(gameBoardRows, gameBoardColumns, territoryDefaultProfit, territoryDefaultArmyThreshold, predefinedTerritories);

                // InitialFunds
                this.initialFunds = schemaGame.getInitialFunds().intValue();

                // Army Units
                HashMap<String, Unit> armyUnits = new HashMap<>();
                Set<String> validateUnitTypes = new HashSet<>();
                List<engine.xmlclasses.Unit> schemaUnits = schemaGame.getArmy().getUnit();

                for (int i = 0; i < schemaUnits.size(); i++) {
                    engine.xmlclasses.Unit currentUnit = schemaUnits.get(i);

                    String unitType = currentUnit.getType();

                    if (validateUnitTypes.contains(unitType)) {
                        this.setGameInvalid("Unit type " + unitType + " is defined more than once.");
                    } else {
                        validateUnitTypes.add(unitType);

                        int unitRank = currentUnit.getRank();

                        int unitPurchase = currentUnit.getPurchase().intValue();
                        int unitMaxFirePower = currentUnit.getMaxFirePower().intValue();
                        int unitCompetenceReduction = currentUnit.getCompetenceReduction().intValue();

                        if (unitPurchase <= 0)
                            this.setGameInvalid("The value of purchase for unit type " + unitType + " is not a whole positive number.");

                        if (unitMaxFirePower <= 0)
                            this.setGameInvalid("The value of max fire power for unit type " + unitType + " is not a whole positive number.");

                        if (unitCompetenceReduction <= 0)
                            this.setGameInvalid("The value of competence reduction for unit type " + unitType + " is not a whole positive number.");

                        armyUnits.put(unitType.toUpperCase(), new Unit(unitType, unitRank, unitPurchase, unitMaxFirePower, unitCompetenceReduction));
                    }
                }

                this.gameUnits = armyUnits;
                for (Player player : players)
                    player.setMoney(initialFunds);

            } catch (JAXBException e) {
                this.setGameInvalid(e.getMessage());
            }
        }
    }

    private void setGameInvalid(String errorMessage){
        this.isValidGameConfig = false;
        this.gameErrorMessages.push(errorMessage);
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public Board getBoard() {
        return gameBoard;
    }

    public boolean isValidGameConfig() {
        return isValidGameConfig;
    }

    public boolean isGameOver(){
        return currentRound == totalRounds;
    }

    public Stack<String> getGameErrorMessages(){
        return gameErrorMessages;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void incrementCurrentRound() {
        this.currentRound++;
    }

    public Player[] getPlayers() {
        return players;
    }

    public GameHistory[] getGameHistory() {
        return gameHistory;
    }

    public void addHistory(Territory[] currentTerritories){
        Player[] playersHistory = new Player[players.length];
        Territory[] territoryHistory = new Territory[currentTerritories.length];

        cloneTerritoriesAndPlayers(territoryHistory, playersHistory, currentTerritories, players);

        gameHistory[currentRound] = new GameHistory(territoryHistory, playersHistory);
    }

    public Unit getGameUnit(String type) {
        return gameUnits.get(type);
    }

    public HashMap<String, Unit> getGameUnits(){
        return gameUnits;
    }

    public void startGame(boolean newGameConfigLoaded){
        if(!gameStarted) {
                gameStarted = true;
                currentRound = 0;
                gameHistory = new GameHistory[totalRounds];
                addHistory(gameBoard.getTerritories());

            if(!newGameConfigLoaded) { // Also create a new game board and new players
                gameBoard = new Board(gameBoard);
                players = Player.reconstructPlayers(players, initialFunds);
            }
        }
    }

    public ArrayList<Player> endgameAftermath(){
        int maxTotalTerritoryProfit = 0;
        ArrayList<Player> winningPlayers = new ArrayList<>(); // To check for a tie;

        for(Player player : players){
            int currentPlayerMaxTerritoryValue = player.getTotalProfits();

            if(currentPlayerMaxTerritoryValue == maxTotalTerritoryProfit){
                winningPlayers.add(player);
            } else if (currentPlayerMaxTerritoryValue > maxTotalTerritoryProfit){
                winningPlayers.clear();
                winningPlayers.add(player);
                maxTotalTerritoryProfit = currentPlayerMaxTerritoryValue;
            }
        }
        gameStarted = false;
        return winningPlayers;
    }

    public boolean didGameStart(){ return gameStarted;}

    public String saveGame(String filePath) {
        String errorMessage = null;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath + ".dat");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            errorMessage = "File not found!";
        } catch (IOException e) {
            errorMessage = e.getMessage();
        }

        return errorMessage;
    }

    public String loadGame(String filePath){
        String errorMessage = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
            GameData loadedGameData = (GameData) objectInputStream.readObject();

            gameType = loadedGameData.gameType;
            totalRounds = loadedGameData.totalRounds;
            currentRound = loadedGameData.currentRound;
            initialFunds = loadedGameData.initialFunds;
            gameStarted = loadedGameData.gameStarted;
            gameBoard = loadedGameData.gameBoard;
            gameUnits = loadedGameData.gameUnits;
            gameHistory = loadedGameData.gameHistory;
            players = loadedGameData.players;
            isValidGameConfig = loadedGameData.isValidGameConfig;
            gameErrorMessages = loadedGameData.gameErrorMessages;

            objectInputStream.close();
        } catch (FileNotFoundException e) {
            errorMessage = "File not found!";
        } catch (IOException e) {
            errorMessage = e.getMessage();
        } catch (ClassNotFoundException e) {
            errorMessage = e.getMessage();
        }

        return errorMessage;
    }

    public Player getPlayerById(int playerId){
        for(int i = 0; i < players.length; i++){
            if(players[i].getId() == playerId)
                return players[i];
        }
        return null;
    }

    public boolean undoRound(){

        if(currentRound > 0) {
            Territory[] lastRoundTerritories = gameHistory[currentRound - 1].getTerritoryHistory();
            Player[] lastRoundPlayers = gameHistory[currentRound - 1].getPlayerHistory();
            Territory[] newTerritories = new Territory[lastRoundTerritories.length];
            Player[] newPlayers = new Player[lastRoundPlayers.length];

            cloneTerritoriesAndPlayers(newTerritories, newPlayers, lastRoundTerritories, lastRoundPlayers);

            gameBoard.setTerritories(newTerritories);
            players = newPlayers;

            gameHistory[currentRound] = null;
            currentRound--;
            return true;
        } else
            return false;
    }

    private void cloneTerritoriesAndPlayers(Territory[] clonedTerritories, Player[] clonedPlayers, Territory[] territoriesToClone, Player[] playersToClone) {
        HashMap<Territory, Territory> temp = new HashMap<>();

        for (int i = 0; i < territoriesToClone.length; i++) {
            Territory toClone = territoriesToClone[i];
            clonedTerritories[i] = new Territory(toClone.getId(), toClone.getProfit(), toClone.getArmyThreshold());
            if (toClone.isConquered()) {
                clonedTerritories[i].setConqueredByPlayer(toClone.getConqueringPlayerId());
                temp.put(clonedTerritories[i], toClone);
            }
        }

        for (int i = 0; i < playersToClone.length; i++) {
            Player toClone = playersToClone[i];
            int currentPlayerId = toClone.getId();
            clonedPlayers[i] = new Player(currentPlayerId, toClone.getName(), toClone.getMoney());

            HashMap<Territory, ArrayList<Army>> currentPlayerOwnedTerritories = new HashMap<>();

            if (playersToClone[i].getOwnedTerritories().size() > 0) {
                for (Territory territory : clonedTerritories) {
                    if (territory.isConquered() && territory.getConqueringPlayerId() == currentPlayerId) {

                        ArrayList<Army> territoryArmy = new ArrayList<>();

                        for (Army army : playersToClone[i].getOwnedTerritories().get(temp.get(territory)))
                            territoryArmy.add(new Army(army.getAmount(), army.getCompetence(), army.getUnit(), clonedPlayers[i]));

                        currentPlayerOwnedTerritories.put(territory, territoryArmy);
                    }
                }
            }
            clonedPlayers[i].setOwnedTerritories(currentPlayerOwnedTerritories);
        }
    }
}
