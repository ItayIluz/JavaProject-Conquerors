package engine;

import engine.xmlclasses.Game;
import engine.xmlclasses.GameDescriptor;
import engine.xmlclasses.Teritory;
import javafx.scene.paint.Color;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class GameData implements Serializable {

    public enum GameType {
        BASIC, MULTIPLAYER, DYNAMIC_MULTIPLAYER
    }

    private transient HashMap<String, Color> playerColors = new HashMap<>();

    private static final long serialVersionUID = 4L;
    private GameType gameType;
    private int totalRounds;
    private int currentRound;
    private int initialFunds;
    private boolean gameStarted = false;
    private boolean gameStartedFirstTime = false;
    private Board gameBoard;
    private HashMap<String, Unit> gameUnits;
    private Player[] players;
    private GameHistory[] gameHistory;
    private int currentPlayerIndex = -1;

    private boolean isValidGameConfig = true;
    private Stack<String> gameErrorMessages = new Stack<>();

    public GameData(){
        setPlayerColors();
    }

    public GameData(GameType gameType, int totalRounds, int initialFunds, Board gameBoard, HashMap<String, Unit> gameUnits, Player[] players){
        setPlayerColors();
        this.gameType = gameType;
        this.totalRounds = totalRounds;
        this.initialFunds = initialFunds;
        this.gameBoard = gameBoard;
        this.gameUnits = gameUnits;
        this.players = players;
        this.gameHistory = new GameHistory[totalRounds];
    }

    public GameData(File xmlFile){
        setPlayerColors();
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
                        List<engine.xmlclasses.Player> gameDescriptorPlayers = gameDescriptor.getPlayers().getPlayer();
                        int numOfPlayers = gameDescriptorPlayers.size();
                        Set<Integer> validatePlayerIds = new HashSet<>();

                        // Check number of players
                        if(numOfPlayers < 2 || numOfPlayers > 4)
                            this.setGameInvalid("The number of players must be 2-4.");
                        else {
                            this.players = new Player[numOfPlayers];
                            for(int i = 0; i < numOfPlayers; i++){
                                engine.xmlclasses.Player currentPlayer = gameDescriptorPlayers.get(i);
                                int currentPlayerId = currentPlayer.getId().intValue();
                                this.players[i] = new Player(currentPlayerId, currentPlayer.getName());

                                if(validatePlayerIds.contains(currentPlayerId))
                                    this.setGameInvalid("Each player must have a unique ID.");
                                else
                                    validatePlayerIds.add(currentPlayerId);
                            }
                        }

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

                if(players != null){
                    int playerIndex = 0;
                    for(Map.Entry<String, Color> entry : playerColors.entrySet()){
                        if(playerIndex < players.length) {
                            players[playerIndex].setColor(entry.getValue(), entry.getKey());
                            playerIndex++;
                        } else
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

                for (Teritory currentTerritory : schemaTerritories) {
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
                Set<Integer> validateUnitRanks = new HashSet<>();
                List<engine.xmlclasses.Unit> schemaUnits = schemaGame.getArmy().getUnit();

                for (engine.xmlclasses.Unit currentUnit : schemaUnits) {
                    String unitType = currentUnit.getType();
                    int unitRank = currentUnit.getRank();

                    if (validateUnitTypes.contains(unitType)) {
                        this.setGameInvalid("Unit type " + unitType + " is defined more than once.");
                    } else if (validateUnitRanks.contains(unitRank)) {
                        this.setGameInvalid("Each unit must have a unique rank.");
                    } else {
                        validateUnitTypes.add(unitType);
                        validateUnitRanks.add(unitRank);

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


                /*List<Unit> sortedByRank = armyUnits.entrySet().stream()
                        .sorted(Comparator.comparingInt(a -> a.getValue().getRank()))
                        .map(e -> new Unit(e.getValue()))
                        .collect(Collectors.toList());*/
                // Validate unit ranks are sequential
                if(!validateUnitRanks.isEmpty()){
                    int maxId = validateUnitRanks.stream().mapToInt(v -> v).max().getAsInt();

                    if(maxId != validateUnitRanks.size())
                        this.setGameInvalid("All unit ranks must be sequential.");
                }


                this.gameUnits = armyUnits;
                if (players != null) {
                    for (Player player : players)
                        player.setMoney(initialFunds);
                }

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

    public int incrementCurrentRound() {
        currentRound++;
        return currentRound;
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
        HashMap<String,Integer> totalUnitsOnBoardHistory = new HashMap<>();

        cloneTerritoriesAndPlayers(territoryHistory, playersHistory, currentTerritories, players);
        for(Unit unit : gameUnits.values())
            totalUnitsOnBoardHistory.put(unit.getType(), unit.getTotalOnBoard());

        gameHistory[currentRound] = new GameHistory(territoryHistory, playersHistory, totalUnitsOnBoardHistory);
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

            if(!newGameConfigLoaded) { // If using the save game config, also create a new game board and new players
                gameBoard = new Board(gameBoard);
                players = Player.reconstructPlayers(players, initialFunds);
            }
        }
    }

    public HashSet<Player> endgameAftermath(){
        int maxTotalTerritoryProfit = 0;
        HashSet<Player> winningPlayers = new HashSet<>(); // To check for a tie;

        for(Player player : players){
            if(!player.isSurrendered()) {
                int currentPlayerMaxTerritoryValue = player.getTerritoriesTotalProfit();

                if (currentPlayerMaxTerritoryValue == maxTotalTerritoryProfit) {
                    winningPlayers.add(player);
                } else if (currentPlayerMaxTerritoryValue > maxTotalTerritoryProfit) {
                    winningPlayers.clear();
                    winningPlayers.add(player);
                    maxTotalTerritoryProfit = currentPlayerMaxTerritoryValue;
                }
            }
        }
        currentPlayerIndex = -1;
        gameStarted = false;
        return winningPlayers;
    }

    public boolean didGameStart(){ return gameStarted;}

    public boolean didGameStartFirstTime(){ return gameStartedFirstTime;}

    public void setGameStartedFirstTime(boolean gameStartedFirstTime) { this.gameStartedFirstTime = gameStartedFirstTime; }

    public String saveGame(File fileToSave) {
        String errorMessage = null;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            errorMessage = "File not found!";
        } catch (IOException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        }

        return errorMessage;
    }

    public String loadGame(File fileToLoad){
        String errorMessage = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(fileToLoad);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
            GameData loadedGameData = (GameData) objectInputStream.readObject();

            gameType = loadedGameData.gameType;
            totalRounds = loadedGameData.totalRounds;
            currentRound = loadedGameData.currentRound;
            initialFunds = loadedGameData.initialFunds;
            gameStarted = loadedGameData.gameStarted;
            gameStartedFirstTime = loadedGameData.gameStartedFirstTime;
            gameBoard = loadedGameData.gameBoard;
            gameUnits = loadedGameData.gameUnits;
            gameHistory = loadedGameData.gameHistory;
            players = loadedGameData.players;
            isValidGameConfig = loadedGameData.isValidGameConfig;
            gameErrorMessages = loadedGameData.gameErrorMessages;

            objectInputStream.close();
        } catch (FileNotFoundException e) {
            errorMessage = "File not found!";
        } catch (EOFException e) {
            errorMessage = "File is invalid or corrupted.";
        } catch (IOException | ClassNotFoundException e) {
            errorMessage = e.getMessage();
        }

        return errorMessage;
    }

    public Player getPlayerById(int playerId){
        for (Player player : players) {
            if (player.getId() == playerId)
                return player;
        }
        return null;
    }

    public Player setNextPlayer(){
        currentPlayerIndex++;
        if(currentPlayerIndex == players.length)
            currentPlayerIndex = -1;

        if(currentPlayerIndex == -1)
            return null;
        else
            return players[currentPlayerIndex];
    }

    public void undoRound(){

        if(currentRound > 0) {
            Territory[] lastRoundTerritories = gameHistory[currentRound-1].getTerritoryHistory();
            Player[] lastRoundPlayers = gameHistory[currentRound-1].getPlayerHistory();
            HashMap<String,Integer> totalUnitsOnBoardHistory = gameHistory[currentRound-1].getTotalUnitsOnBoardHistory();

            Territory[] newTerritories = new Territory[lastRoundTerritories.length];
            Player[] newPlayers = new Player[lastRoundPlayers.length];

            cloneTerritoriesAndPlayers(newTerritories, newPlayers, lastRoundTerritories, lastRoundPlayers);

            gameBoard.setTerritories(newTerritories);
            players = newPlayers;
            for(Unit unit : gameUnits.values())
                unit.setTotalOnBoard(totalUnitsOnBoardHistory.get(unit.getType()));

            gameHistory[currentRound] = null;
            currentRound--;
        }
    }

    public void setReplayData(int turn){
        HashMap<String,Integer> totalUnitsOnBoardHistory = gameHistory[turn].getTotalUnitsOnBoardHistory();

        gameBoard.setTerritories(gameHistory[turn].getTerritoryHistory());
        players = gameHistory[turn].getPlayerHistory();
        for(Unit unit : gameUnits.values())
            unit.setTotalOnBoard(totalUnitsOnBoardHistory.get(unit.getType()));
    }

    public String capitalizeString(String string){
        return (string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
    }

    private void cloneTerritoriesAndPlayers(Territory[] clonedTerritories, Player[] clonedPlayers, Territory[] territoriesToClone, Player[] playersToClone) {
        HashMap<Player, Player> temp = new HashMap<>();

        // Clone Players
        for (int i = 0; i < playersToClone.length; i++) {
            Player toClone = playersToClone[i];
            int currentPlayerId = toClone.getId();
            clonedPlayers[i] = new Player(currentPlayerId, toClone.getName(), toClone.getMoney(), toClone.getColor(), toClone.getColorName());
            clonedPlayers[i].setSurrendered(toClone.isSurrendered());
            temp.put(toClone, clonedPlayers[i]);
        }

        // Clone Territories & Armies
        for (int i = 0; i < territoriesToClone.length; i++) {
            Territory toClone = territoriesToClone[i];
            clonedTerritories[i] = new Territory(toClone.getId(), toClone.getProfit(), toClone.getArmyThreshold());
            if (toClone.isConquered()) {
                Player controllingPlayer = temp.get(toClone.getConqueringPlayer());
                ArrayList<Army> territoryArmy = Army.cloneArmies(toClone.getArmies(), controllingPlayer);

                clonedTerritories[i].setConqueredByPlayer(controllingPlayer, territoryArmy);
            }
        }

        for (int i = 0; i < playersToClone.length; i++) {
            if (playersToClone[i].getOwnedTerritories().size() > 0) {
                for (Territory territory : clonedTerritories) {
                    if (territory.isConquered() && territory.getConqueringPlayer() == clonedPlayers[i])
                        clonedPlayers[i].addTerritory(territory);
                }
            }
        }
    }

    private void setPlayerColors(){
        playerColors.put("Red", Color.TOMATO);
        playerColors.put("Blue", Color.LIGHTBLUE);
        playerColors.put("Yellow", Color.GOLD);
        playerColors.put("Green", Color.LIGHTGREEN);
    }
}
