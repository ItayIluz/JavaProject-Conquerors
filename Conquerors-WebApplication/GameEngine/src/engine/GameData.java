package engine;

import engine.xmlclasses.Game;
import engine.xmlclasses.GameDescriptor;
import engine.xmlclasses.Teritory;
import javafx.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class GameData implements Serializable {

    public enum GameType {
        Basic, MultiPlayer, DynamicMultiPlayer
    }

    public enum DynamicGameStatus {
        InSession, Pending, Ended
    }

    private enum ActionOnTerritory{
        View, Conquer, Attack, Maintain
    }

    private transient Pair<String, String>[] playerColors = new Pair[4];

    private static final long serialVersionUID = 4L;
    private String gameTitle = "Conquerors Game";
    private DynamicGameStatus gameStatus = DynamicGameStatus.InSession;
    private String uploadedBy = "";
    private int currentPlayers = 0;
    private int totalPlayers;
    private GameType gameType;
    private int totalRounds;
    private int currentRound;
    private int initialFunds;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean gameStartedFirstTime = false;
    private Board gameBoard;
    private HashMap<String, Unit> gameUnits;
    private ArrayList<Player> players;
    private ArrayList<HashMap<String, Object>> gameResults;
    private GameHistory[] gameHistory;
    private int currentPlayerIndex = -1;
    private boolean canCurrentPlayerTakeAction = true;
    private String lastPlayerName;
    private String lastPlayerAction;

    private boolean isValidGameConfig = true;
    private Stack<String> gameErrorMessages = new Stack<>();

    public GameData(){
        setPlayerColors();
    }

    public GameData(GameType gameType, int totalRounds, int initialFunds, Board gameBoard, HashMap<String, Unit> gameUnits, ArrayList<Player> players){
        setPlayerColors();
        this.gameType = gameType;
        this.totalRounds = totalRounds;
        this.initialFunds = initialFunds;
        this.gameBoard = gameBoard;
        this.gameUnits = gameUnits;
        this.players = players;
        this.gameHistory = new GameHistory[totalRounds];
    }

    public GameData(InputStream inputStream){
        try {
            JAXBContext jc = JAXBContext.newInstance("engine.xmlclasses");
            Unmarshaller u = jc.createUnmarshaller();
            GameDescriptor gameDescriptor = (GameDescriptor) u.unmarshal(inputStream);
            initializeFromDescriptor(gameDescriptor);
        } catch (JAXBException e) {
            this.setGameInvalid(e.getMessage());
        }
    }

    public GameData(File xmlFile){
        if(xmlFile != null && !xmlFile.exists()) {
            this.setGameInvalid("The file you specified does not exist.");
        } else if(xmlFile.isFile() && !xmlFile.getName().substring(xmlFile.getName().lastIndexOf(".")+1).equals("xml")) {
            this.setGameInvalid("The file you specified is not an XML file.");
        } else {
            try {
                JAXBContext jc = JAXBContext.newInstance("engine.xmlclasses");
                Unmarshaller u = jc.createUnmarshaller();
                GameDescriptor gameDescriptor = (GameDescriptor) u.unmarshal(xmlFile);
                initializeFromDescriptor(gameDescriptor);
            } catch (JAXBException e) {
                this.setGameInvalid(e.getMessage());
            }
        }
    }

    private void initializeFromDescriptor(GameDescriptor gameDescriptor){
        setPlayerColors();
        Game schemaGame = gameDescriptor.getGame();

        // GameType Tag
        String gameTypeNodeText = gameDescriptor.getGameType();

        this.gameType = GameType.valueOf(gameTypeNodeText);

        switch (this.gameType) {
            case Basic: {
                this.players = new ArrayList<>(2);
                this.players.add(new Player(1, "Player1"));
                this.players.add(new Player(2, "Player2"));
                this.currentPlayers = 2;
                this.totalPlayers = 2;
                break;
            }
            case MultiPlayer: {
                List<engine.xmlclasses.Player> gameDescriptorPlayers = gameDescriptor.getPlayers().getPlayer();
                int numOfPlayers = gameDescriptorPlayers.size();
                Set<Integer> validatePlayerIds = new HashSet<>();

                // Check number of players
                if(numOfPlayers < 2 || numOfPlayers > 4)
                    this.setGameInvalid("The number of players must be 2-4.");
                else {
                    this.players = new ArrayList<>(numOfPlayers);
                    this.currentPlayers = numOfPlayers;
                    this.totalPlayers = numOfPlayers;
                    for(int i = 0; i < numOfPlayers; i++){
                        engine.xmlclasses.Player currentPlayer = gameDescriptorPlayers.get(i);
                        int currentPlayerId = currentPlayer.getId().intValue();
                        this.players.add(i, new Player(currentPlayerId, currentPlayer.getName()));

                        if(validatePlayerIds.contains(currentPlayerId))
                            this.setGameInvalid("Each player must have a unique ID.");
                        else
                            validatePlayerIds.add(currentPlayerId);
                    }
                }

                break;
            }
            case DynamicMultiPlayer: {
                int numOfTotalPlayers = gameDescriptor.getDynamicPlayers().getTotalPlayers().intValue();

                // Check number of players
                if(numOfTotalPlayers < 2 || numOfTotalPlayers > 4)
                    this.setGameInvalid("The number of players must be 2-4.");
                else {
                    this.totalPlayers = gameDescriptor.getDynamicPlayers().getTotalPlayers().intValue();
                    this.players = new ArrayList<>(this.totalPlayers);
                }

                String gameTitle = gameDescriptor.getDynamicPlayers().getGameTitle();

                if(gameTitle == null)
                    this.setGameInvalid("The game must have a game title.");
                else
                    this.gameTitle = gameTitle;

                this.gameStatus = DynamicGameStatus.Pending;

                break;
            }
            default: {
                this.setGameInvalid("Unknown GameType in XML file.");
                break;
            }
        }

        if(players != null){
            for(int i = 0; i < players.size(); i++) {
                if(players.get(i) != null)
                    players.get(i).setColor(playerColors[i].getValue(), playerColors[i].getKey());
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

                armyUnits.put(unitType, new Unit(unitType, unitRank, unitPurchase, unitMaxFirePower, unitCompetenceReduction));
            }
        }

        // Validate unit ranks are sequential
        if(!validateUnitRanks.isEmpty()){
            int maxId = validateUnitRanks.stream().mapToInt(v -> v).max().getAsInt();

            if(maxId != validateUnitRanks.size())
                this.setGameInvalid("All unit ranks must be sequential.");
        }

        this.gameUnits = armyUnits;
        if (players != null) {
            for(int i = 0; i < players.size(); i++) {
                if (players.get(i) != null)
                    players.get(i).setMoney(initialFunds);
            }
        }
    }

    private void setGameInvalid(String errorMessage){
        this.isValidGameConfig = false;
        this.gameErrorMessages.push(errorMessage);
    }

    public void addPlayer(String playerName){
        Player newPlayer = new Player((currentPlayers+1), playerName, initialFunds, playerColors[currentPlayers].getValue(), playerColors[currentPlayers].getKey());
        players.add(currentPlayers, newPlayer);
        currentPlayers++;

        if(currentPlayers == totalPlayers) {
            startGame(false);
            setNextPlayer();
            gameStatus = DynamicGameStatus.InSession;
        }
    }

    public void removePlayer(String playerName){
        for(int i = 0; i < players.size(); i++){
            if(players.get(i) != null && players.get(i).getName().equals(playerName)) {
                players.remove(i);
                currentPlayers--;

                if(currentPlayerIndex >= players.size())
                    currentPlayerIndex = -1;

                break;
            }
        }

        if(currentPlayers == 0 && gameStatus == DynamicGameStatus.Ended){
            gameBoard = new Board(gameBoard);
            gameStatus = DynamicGameStatus.Pending;
            gameStarted = false;
            gameOver = false;
        }
    }

    public boolean isPlayerInGame(String playerName){
        for (Player player : players) {
            if (player != null && player.getName().equals(playerName) && !player.isSurrendered())
                return true;
        }
        return false;
    }

    public DynamicGameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(DynamicGameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getGameTitle(){
        return this.gameTitle;
    }

    public int getTotalPlayers(){
        return this.totalPlayers;
    }

    public String getUploadedBy(){
        return this.uploadedBy;
    }

    public void setUploadedBy(String uploadedBy){
        this.uploadedBy = uploadedBy;
    }

    public void incrementCurrentPlayers(){
        this.currentPlayers++;
    }

    public int getCurrentPlayers(){
        return this.currentPlayers;
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
        return gameOver;
    }

    public boolean checkGamerOver(){

        if(currentRound == totalRounds)
            gameOver = true;
        else if(currentPlayers == 1) // All players surrendered
            gameOver = true;

        if(gameOver) {
            gameStatus = DynamicGameStatus.Ended;
            calculateGameResults();
        }

        return gameOver;
    }

    public String getLastPlayerAction() {
        return lastPlayerAction;
    }

    public void setLastPlayerAction(String lastPlayerAction){
        this.lastPlayerAction = lastPlayerAction;
    }

    public String getLastPlayerName() {
        return lastPlayerName;
    }

    public void setLastPlayerName(String lastPlayerName){
        this.lastPlayerName = lastPlayerName;
    }

    public Stack<String> getGameErrorMessages(){
        return gameErrorMessages;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int startNextRound() {
        for(Player player : players){
            if(!player.isSurrendered())
                player.gainProfitsAndUpdateArmyCompetence();
        }
        currentRound++;
        return currentRound;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public GameHistory[] getGameHistory() {
        return gameHistory;
    }

    public void addHistory(Territory[] currentTerritories){
        ArrayList<Player> playersHistory = new ArrayList<>(players.size());
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
                gameOver = false;
                currentRound = 0;
                gameHistory = new GameHistory[totalRounds];
                lastPlayerName = null;
                lastPlayerAction = null;

                if(gameType != GameType.DynamicMultiPlayer)
                    addHistory(gameBoard.getTerritories());

            if(!newGameConfigLoaded) { // If using the save game config, also create a new game board and new players
                gameBoard = new Board(gameBoard);
                players = Player.reconstructPlayers(players, initialFunds);
            }
        }
    }

    public void calculateGameResults(){
        ArrayList<HashMap<String, Object>> results = new ArrayList<>();
        int maxTotalTerritoryProfit = 0;

        for(Player player : players){
            HashMap<String, Object> currentPlayerData = new HashMap<>();

            currentPlayerData.put("player", player);
            currentPlayerData.put("numOfTerritories", player.getOwnedTerritories().size());

            if(!player.isSurrendered()) {
                int currentPlayerMaxTerritoryValue = player.getTerritoriesTotalProfit();
                currentPlayerData.put("totalTerritoriesProfit", player.getTerritoriesTotalProfit());

                if (currentPlayerMaxTerritoryValue > maxTotalTerritoryProfit)
                    maxTotalTerritoryProfit = currentPlayerMaxTerritoryValue;

            } else {
                currentPlayerData.put("totalTerritoriesProfit", 0);
            }

            results.add(currentPlayerData);
        }

        for (HashMap<String, Object> currentPlayerData : results) {
            if ((int) currentPlayerData.get("totalTerritoriesProfit") == maxTotalTerritoryProfit)
                currentPlayerData.put("isWinner", true);
            else
                currentPlayerData.put("isWinner", false);
        }

        currentPlayerIndex = -1;
        gameResults = results;
    }

    public ArrayList<HashMap<String, Object>> getGameResults() {
        return gameResults;
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

    public Player getPlayerByName(String playerName){
        for (Player player : players) {
            if (player.getName().equals(playerName))
                return player;
        }
        return null;
    }

    public Player getCurrentPlayer(){
        if(currentPlayerIndex > -1 && currentPlayerIndex < players.size())
            return players.get(currentPlayerIndex);
        else
            return null;
    }

    public ActionOnTerritory[] getAllowedActionsOnBoardForPlayer(String playerName){
        ActionOnTerritory[] actionsArray = new ActionOnTerritory[gameBoard.getRows()*gameBoard.getColumns()];

        Player playerToCheck = getPlayerByName(playerName);
        Player currentPlayersTurn = getCurrentPlayer();
        Territory[] boardTerritories = gameBoard.getTerritories();
        int numberOfPlayerOwnedTerritories = playerToCheck.getOwnedTerritories().size();

        for(int i = 0; i < boardTerritories.length; i++) {
            Territory currentTerritory = boardTerritories[i];

            if(currentTerritory.getConqueringPlayer() == playerToCheck && currentPlayersTurn == playerToCheck){
                actionsArray[i] = ActionOnTerritory.Maintain;
            } else if(gameBoard.isTerritoryInPlayerRange(i, playerToCheck) || numberOfPlayerOwnedTerritories == 0){
                if(boardTerritories[i].isConquered()) { // Selected territory is an opponent's territory
                    actionsArray[i] = ActionOnTerritory.Attack;
                } else {
                    actionsArray[i] = ActionOnTerritory.Conquer;
                }
            } else {
                actionsArray[i] = null;
            }
        }

        return actionsArray;
    }

    public Player setNextPlayer(){
        currentPlayerIndex++;
        if(currentPlayerIndex == players.size())
            currentPlayerIndex = -1;

        if(currentPlayerIndex == -1)
            return null;
        else {
            Player currentPlayer = getCurrentPlayer();
            canCurrentPlayerTakeAction = true;
            return currentPlayer;
        }
    }

    public void setCanCurrentPlayerTakeAction(boolean can){
        this.canCurrentPlayerTakeAction = can;
    }

    public String capitalizeString(String string){
        return (string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
    }

    private void cloneTerritoriesAndPlayers(Territory[] clonedTerritories, ArrayList<Player> clonedPlayers, Territory[] territoriesToClone, ArrayList<Player> playersToClone) {
        HashMap<Player, Player> temp = new HashMap<>();

        // Clone Players
        for (int i = 0; i < playersToClone.size(); i++) {
            Player toClone = playersToClone.get(i);
            int currentPlayerId = toClone.getId();
            clonedPlayers.add(i, new Player(currentPlayerId, toClone.getName(), toClone.getMoney(), toClone.getColor(), toClone.getColorName()));
            clonedPlayers.get(i).setSurrendered(toClone.isSurrendered());
            temp.put(toClone, clonedPlayers.get(i));
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

        for (int i = 0; i < playersToClone.size(); i++) {
            if (playersToClone.get(i).getOwnedTerritories().size() > 0) {
                for (Territory territory : clonedTerritories) {
                    if (territory.isConquered() && territory.getConqueringPlayer() == clonedPlayers.get(i))
                        clonedPlayers.get(i).addTerritory(territory);
                }
            }
        }
    }

    public void addArmiesToTerritory(int territoryId, ArrayList<Army> newArmies, int totalNewArmiesCost){

        Player currentPlayer = getCurrentPlayer();
        Territory territoryToAddTo = gameBoard.getTerritories()[territoryId-1];
        ArrayList<Army> currentTerritoryArmies = territoryToAddTo.getArmies();

        for(Army currentArmy : newArmies) {
            currentArmy.setInTerritory(territoryToAddTo);
            currentTerritoryArmies.add(currentArmy);
        }

        Army.uniteSameTypeArmies(currentTerritoryArmies);

        lastPlayerName = currentPlayer.getName();
        lastPlayerAction = (territoryToAddTo.isConquered() ? "maintained " : "conquered ") + "territory #" + territoryId + ".";

        territoryToAddTo.setConqueredByPlayer(currentPlayer, currentTerritoryArmies);
        currentPlayer.subtractMoney(totalNewArmiesCost);

        canCurrentPlayerTakeAction = false;
    }

    public void fixTerritoryArmiesCompetence(int territoryId, int totalCostToFixCompetence){

        Player currentPlayer = getCurrentPlayer();
        Territory territoryToAddTo = gameBoard.getTerritories()[territoryId-1];
        ArrayList<Army> currentTerritoryArmies = territoryToAddTo.getArmies();

        for (Army currentArmy : currentTerritoryArmies)
            currentArmy.fixCompetence();

        Army.uniteSameTypeArmies(currentTerritoryArmies);
        currentPlayer.subtractMoney(totalCostToFixCompetence);
        lastPlayerName = currentPlayer.getName();
        lastPlayerAction = "maintained territory #" + territoryId + ".";
        canCurrentPlayerTakeAction = false;
    }

    public HashMap<String, Object> calculateAttackResults(int territoryId, String attackType, ArrayList<Army> attackingArmy){

        HashMap<String, Object> results = new HashMap<>();
        Territory attackedTerritory = getBoard().getTerritories()[territoryId - 1];
        Player attackingPlayer = getCurrentPlayer();
        Player defendingPlayer = attackedTerritory.getConqueringPlayer();

        Army.uniteSameTypeArmies(attackingArmy);
        ArrayList<Army> copyOfAttackingArmy = Army.cloneArmies(attackingArmy, attackingPlayer);
        ArrayList<Army> copyOfDefendingArmy = Army.cloneArmies(attackedTerritory.getArmies(), defendingPlayer);

        ArrayList<Army> copyOfWinningArmy;

        if(attackType.equals("Deterministic"))
            copyOfWinningArmy = attackedTerritory.attackDeterministic(attackingArmy, attackedTerritory.getArmies());
        else  //attackType == I'm Feeling Lucky
            copyOfWinningArmy = attackedTerritory.attackImFeelingLucky(attackingArmy, attackedTerritory.getArmies());

        StringBuilder attackResultsDescription = new StringBuilder();
        if(copyOfWinningArmy.size() > 0){
            if(attackedTerritory.getConqueringPlayer() != defendingPlayer) { // Attacking player won
                attackResultsDescription.append(attackingPlayer.getName()).append(" won the battle!\n");

                if(attackedTerritory.getConqueringPlayer() == attackingPlayer)
                    attackResultsDescription.append("Territory # ").append(attackedTerritory.getId()).append(" is now under ").append(attackingPlayer.getName()).append("'s control.");
                else
                    attackResultsDescription.append("Territory # ").append(attackedTerritory.getId()).append(" remains neutral.");

            } else {
                attackResultsDescription.append(attackingPlayer.getName()).append(" lost the battle...\n");
            }
        } else { // It's a tie
            attackResultsDescription.append("Both armies lost!");
        }

        lastPlayerName = attackingPlayer.getName();
        lastPlayerAction = "attacked territory #" + territoryId + " and " + (attackedTerritory.getConqueringPlayer() != defendingPlayer ? "won!" : "lost.");
        canCurrentPlayerTakeAction = false;

        results.put("attackingArmy", copyOfAttackingArmy);
        results.put("defendingArmy", copyOfDefendingArmy);
        results.put("winningArmy", copyOfWinningArmy);
        results.put("attackResultsDescription", attackResultsDescription.toString());

        return results;
    }

    public boolean canCurrentPlayerTakeAction() {
        return canCurrentPlayerTakeAction;
    }

    private void setPlayerColors(){
        playerColors[0] = new Pair<>("Red", "tomato");
        playerColors[1] = new Pair<>("Blue", "lightblue");
        playerColors[2] = new Pair<>("Yellow", "gold");
        playerColors[3] = new Pair<>("Green", "lightgreen");
    }
}
