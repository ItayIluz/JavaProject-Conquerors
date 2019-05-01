package ui;

import engine.*;
import engine.Unit;

import java.util.*;

public class UserInterface {

    private final int TERRITORY_SIZE = 8; // Must be at least 8

    private boolean newGameConfigLoaded = false;
    private GameData gameData;
    private Scanner scanner = new Scanner(System.in);

    public UserInterface(){}

    public void readAndInitializeGameFromFile(){
        String filePath;

        System.out.print("Enter path for XML configuration file: ");
        filePath  = scanner.nextLine();

        this.gameData = new GameData(filePath);

        if(!gameData.isValidGameConfig())
            printGameConfigErrors();
        else
            showGameStatus(this.gameData.getBoard().getTerritories(), this.gameData.getPlayers(), this.gameData.getCurrentRound());
    }

    private void showGameStatus(Territory[] territories, Player[] players, int currentRound){
        drawGameBoard(territories);
        System.out.println("Game round: " + currentRound + " / " + this.gameData.getTotalRounds());

        for (Player player: players) {
            printPlayerStatus(player, true);
        }
    }

    private void showGameHistory(){
        GameHistory[] gameHistory = this.gameData.getGameHistory();
        for(int i = 0; i <= this.gameData.getCurrentRound(); i++){
            if(gameHistory[i] != null) {
                showGameStatus(gameHistory[i].getTerritoryHistory(), gameHistory[i].getPlayerHistory(), i);
            } else
                break;
        }
    }

    private void startNextRound(){

        this.gameData.incrementCurrentRound();
        // Update players money & army competence
        for(Player player: this.gameData.getPlayers()){
            int playerOldMoney = player.getMoney();
            player.gainProfitsAndUpdateCompetence();
            System.out.println("\n" + player.getNameAndId() + "'s turn.");
            System.out.println(player.getNameAndId() + " had " + playerOldMoney + " Turings.");
            printPlayerStatus(player, false);
            letPlayerChooseAction(player);
        }

        if(this.gameData.isGameOver()){
            System.out.println("\nGame over!");
            ArrayList<Player> winningPlayers = this.gameData.endgameAftermath();
            newGameConfigLoaded = false;

            for(Player player : this.gameData.getPlayers()) {
                System.out.println(player.getNameAndId() + " has territories with total profit of " + player.getTotalProfits() + " Turings.");
            }

            if(winningPlayers.size() > 1)
                System.out.println("It's a tie!\n");
            else {
                System.out.println(winningPlayers.get(0).getNameAndId() + " is the winner!\n");
            }
        } else
            this.gameData.addHistory(this.gameData.getBoard().getTerritories());
    }

    private void letPlayerChooseAction(Player player){
        int userInput;
        boolean actionCompletedSuccessfully = false;

        while(!actionCompletedSuccessfully){
            userInput = 0;
            System.out.println("\n" + player.getNameAndId() + ", please choose your action:");
            System.out.println("1. Do nothing");
            System.out.println("2. Take action on a territory.");

            try {
                userInput = Integer.parseInt(scanner.nextLine());

                switch (userInput) {
                    case 1: {
                        actionCompletedSuccessfully = true;
                        break;
                    }
                    case 2: {
                        actionCompletedSuccessfully = takeActionOnTerritory(player);
                        break;
                    }
                    default: {
                        System.out.println("ERROR! Please enter a number between 1 and 2!");
                        break;
                    }
                }
            } catch (NumberFormatException e){
                System.out.println("ERROR! Please enter a number!");
            }
        }
        printPlayerStatus(player, false);
    }

    private boolean takeActionOnTerritory(Player player){
        int userInput;
        Board board = this.gameData.getBoard();
        Territory[] gameTerritories = board.getTerritories();
        Territory selectedTerritory;

        while(true){
            userInput = -1;
            System.out.print("Enter the territory number you would like to take action on (0 to cancel): ");

            try {
                userInput = Integer.parseInt(scanner.nextLine());

                if(userInput == 0)
                    return false;
                else if(userInput > gameTerritories.length)
                    System.out.println("ERROR! Territory number does not exist!");
                else {
                    selectedTerritory = gameTerritories[userInput-1];
                    if(selectedTerritory.getConqueringPlayerId() == player.getId()) { // Player want to take action on his own territory
                        return takeActionOnOwnTerritory(player, selectedTerritory);
                    } else if(board.isTerritoryInPlayerRange(selectedTerritory.getId(), player.getId()) || player.getOwnedTerritories().size() == 0){ // Player has no territories or wants to take action on a territory adjacent to his territory
                        if(selectedTerritory.isConquered()) // Selected territory is an opponent's territory
                            return conquerOpponentTerritory(player, selectedTerritory);
                        else // Selected territory is a neutral territory
                            return addArmiesToTerritory(player, selectedTerritory);
                    } else
                        System.out.println("ERROR! You cannot select this territory!");
                }
            } catch (NumberFormatException e){
                System.out.println("ERROR! Please enter a number!");
            }
        }
    }

    private boolean takeActionOnOwnTerritory(Player player, Territory territory){
        int userInput = 0;

        while(userInput != 1 && userInput != 2){
            userInput = -1;
            System.out.println("What would you like to do at territory number " + territory.getId() + " (0 to cancel)?");
            System.out.println("1. Fix ALL units competence.");
            System.out.println("2. Add units.");
            try {
                userInput = Integer.parseInt(scanner.nextLine());

                if(userInput == 0)
                    return false;
                else if(userInput == 1) { // Fix competence
                    int totalCompetenceFixPrice = 0;

                    ArrayList<Army> territoryArmies = player.getOwnedTerritories().get(territory);
                    for(Army currentArmy : territoryArmies)
                        totalCompetenceFixPrice += currentArmy.calculateCompetenceFixCost();

                    if(player.hasEnoughMoney(totalCompetenceFixPrice)){
                        for(Army currentArmy : territoryArmies)
                            currentArmy.fixCompetence();
                        Army.uniteSameTypeArmies(territoryArmies);
                        player.subtractMoney(totalCompetenceFixPrice);
                        System.out.println("Armies' competence fixed!");
                        return true;
                    } else {
                        System.out.println("You don't have enough Turings!");
                        return false;
                    }
                } else if(userInput == 2) { // Add units
                    return addArmiesToTerritory(player, territory);
                } else
                    System.out.println("ERROR! Please enter a number between 1 and 2.");
            } catch (NumberFormatException e){
                System.out.println("ERROR! Please enter a number!");
            }
        }
        return false;
    }

    private boolean addArmiesToTerritory(Player player, Territory territory){
        ArrayList<Army> totalArmies;

        if(territory.isConquered())
            totalArmies = player.getOwnedTerritories().get(territory);
        else
            totalArmies = new ArrayList<>();

        if(!createArmies(player, territory, totalArmies)){
            return false;
        } else {
            if(!territory.isConquered())
                player.conquerTerritory(territory, totalArmies);

            return true;
        }
    }

    private boolean createArmies(Player player, Territory territory, ArrayList<Army> totalArmies){
        Army currentArmy;
        String userInputMoreArmies;
        boolean done = false;
        boolean atLeastOneArmyCreated = false;

        while(!done) {
            userInputMoreArmies = "";
            currentArmy = createArmyToAddToTerritory(player, territory);

            if(currentArmy != null) {
                atLeastOneArmyCreated = true;
                totalArmies.add(currentArmy);
            }else
                done = true;

            if (!done && atLeastOneArmyCreated){

                while (!userInputMoreArmies.equals("NO") && !userInputMoreArmies.equals("YES")) {
                    System.out.println("Would you like to add more armies (Yes/No)?");
                    userInputMoreArmies = scanner.nextLine().toUpperCase();
                    if (userInputMoreArmies.equals("NO"))
                        done = true;
                }
            }
        }

        if(atLeastOneArmyCreated){
            Army.uniteSameTypeArmies(totalArmies);
            return true;
        } else
            return false;
    }

    private boolean conquerOpponentTerritory(Player conqueringPlayer, Territory territory){
        ArrayList<Army> attackingArmies = new ArrayList<>();
        ArrayList<Army> defendingArmyOld = new ArrayList<>();

        System.out.println("\nAttempting to conquer territory number " + territory.getId() + " with the \"I'm Feeling Lucky\" strategy.");
        if(!createArmies(conqueringPlayer, territory, attackingArmies)){
            return false;
        } else {
            ArrayList<Army> defendingArmies = this.gameData.getPlayerById(territory.getConqueringPlayerId()).getOwnedTerritories().get(territory);
            Player defendingPlayer = defendingArmies.get(0).getControllingPlayer();
            for (Army army : defendingPlayer.getOwnedTerritories().get(territory))
                defendingArmyOld.add(new Army(army.getAmount(), army.getCompetence(), army.getUnit(), defendingPlayer));

            territory.attackImFeelingLucky(attackingArmies, defendingArmies);

            System.out.println("\nAttack Results:");
            System.out.println(defendingPlayer.getNameAndId() + " had the following army in the territory:");
            for(Army currentArmy : defendingArmyOld)
                System.out.println("\t" + currentArmy.getAmount() + " " + currentArmy.getUnit().getType() + ", in competence " + currentArmy.getCompetence());

            String conqueringPlayerName = conqueringPlayer.getNameAndId();
            if(territory.getConqueringPlayerId() != defendingPlayer.getId()) { // Attacking player won
                System.out.println(conqueringPlayerName + " won the battle!");

                if(territory.getConqueringPlayerId() == conqueringPlayer.getId()){
                    System.out.println("Territory number " + territory.getId() + " is now under " + conqueringPlayerName + "'s control with the following armies:");
                    for(Army currentArmy : conqueringPlayer.getOwnedTerritories().get(territory))
                        System.out.println("\t" + currentArmy.getAmount() + " " + currentArmy.getUnit().getType() + ", in competence " + currentArmy.getCompetence());
                } else {
                    System.out.println("Territory number " + territory.getId() + " remains neutral.");
                }
            } else { // Attacking player lost
                System.out.println(conqueringPlayerName + " lost the battle...");
            }

            return true;
        }
    }

    private Army createArmyToAddToTerritory(Player player, Territory territory){
        Army createdArmy = null;
        String userInput;
        Unit selectedUnit;
        int amountToAdd;
        int territoryThreshold = territory.getArmyThreshold();
        HashMap<String, Unit> gameUnits = this.gameData.getGameUnits();

        while(createdArmy == null){
            System.out.println("Choose the type of unit to add to the territory (Threshold is " + territoryThreshold + ") (Enter \"Cancel\" to cancel):");
            for(Unit unit : gameUnits.values())
                System.out.println(unit.getType() + " - costs " + unit.getPurchasePrice() + " with " + unit.getMaxFirePower() + " firepower.");

            userInput = scanner.nextLine().toUpperCase();

            if(userInput.equals("CANCEL")){
                break;
            }else if(gameUnits.keySet().contains(userInput)) {
                selectedUnit = gameUnits.get(userInput);
                amountToAdd = -1;
                while (amountToAdd == -1) {

                    System.out.print("How many would you like to add (0 to cancel): ");
                    try {
                        amountToAdd = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR! You must enter a number.");
                    }

                    if (amountToAdd > 0) { // Must add more than 0
                        if (!player.hasEnoughMoney(selectedUnit.getPurchasePrice() * amountToAdd)) { // If the player doesn't have enough money
                            System.out.println("You don't have enough Turings!");
                            amountToAdd = -1;
                        } else { // Player has enough money
                            if (territory.getConqueringPlayerId() != player.getId() && (amountToAdd * selectedUnit.getMaxFirePower()) < territoryThreshold) { // Didn't pass threshold AND territory is not owned by player
                                System.out.println("You must add units with at least " + territoryThreshold + " firepower to conquer this territory!");
                                amountToAdd = -1;
                            } else {
                                createdArmy = new Army(amountToAdd, selectedUnit, player);
                                player.subtractMoney(selectedUnit.getPurchasePrice() * amountToAdd);
                            }
                        }
                    }
                }
            } else
                System.out.println("ERROR! Please choose one of the types in the list!");
        }
        return createdArmy;
    }

    private void printPlayerStatus(Player player, boolean hideArmyDetails){
        HashMap<Territory, ArrayList<Army>> playerTerritories = player.getOwnedTerritories();
        String playerName = player.getNameAndId();
        System.out.println(playerName + " now has " + player.getMoney() + " Turings.");

        if(playerTerritories.size() > 0){
            System.out.println(playerName + " has " + playerTerritories.keySet().size() + " territories:");

            for(Map.Entry<Territory, ArrayList<Army>> entry : playerTerritories.entrySet()){
                Territory currentTerritory = entry.getKey();
                ArrayList<Army> armies = entry.getValue();

                System.out.print("\tTerritory number " + currentTerritory.getId());

                if(!hideArmyDetails) {
                    System.out.println(" with army threshold " + currentTerritory.getArmyThreshold() +":");
                    int competenceFixCost = 0;
                    for (Army currentArmy : armies) {
                        System.out.println("\t\t" + currentArmy.getAmount() + " " + currentArmy.getUnit().getType() + ", in competence " + currentArmy.getCompetence());

                        competenceFixCost += currentArmy.calculateCompetenceFixCost();
                    }
                    System.out.println("\tTotal cost for fixing army competence: " + competenceFixCost);
                } else {
                    System.out.println(".");
                }
            }
        } else {
            System.out.println(playerName + " has no territories.");
        }
    }

    public void printMenuAndGetUserInput(){

        int userInput;
        String fileNotLoadedErrorMessage = "ERROR! A valid game configuration file must first be loaded!";
        String gameAlreadyStartedErrorMessage = "ERROR! The game has already started!";
        String gameNotStartedErrorMessage = "ERROR! You first need to start a new game!";
        boolean quit = false;

        while(!quit){
            printMenu();
            userInput = 0;
            try {
                userInput = Integer.parseInt(scanner.nextLine());

                switch (userInput) {
                    case 1: { //  Read game configuration file
                        if (this.gameData == null || !this.gameData.didGameStart()) {
                            readAndInitializeGameFromFile();
                            newGameConfigLoaded = true;
                        } else
                            System.out.println(gameAlreadyStartedErrorMessage);
                        break;
                    }
                    case 2: { // Start a new game
                        if (this.gameData == null || !gameData.isValidGameConfig())
                            System.out.println(fileNotLoadedErrorMessage);
                        else if (this.gameData.didGameStart())
                            System.out.println(gameAlreadyStartedErrorMessage);
                        else {
                            this.gameData.startGame(newGameConfigLoaded);
                            System.out.println("Game started!");
                        }
                        break;
                    }
                    case 3: { // Show game status
                        if (this.gameData == null || !gameData.isValidGameConfig())
                            System.out.println(fileNotLoadedErrorMessage);
                        else
                            showGameStatus(this.gameData.getBoard().getTerritories(), this.gameData.getPlayers(), this.gameData.getCurrentRound());
                        break;
                    }
                    case 4: { // Start next game round
                        if (this.gameData == null || !this.gameData.didGameStart())
                            System.out.println(gameNotStartedErrorMessage);
                        else
                            startNextRound();
                        break;
                    }
                    case 5: { // Show current game history
                        if (this.gameData == null || !this.gameData.didGameStart())
                            System.out.println(gameNotStartedErrorMessage);
                        else
                            showGameHistory();
                        break;
                    }
                    case 6: { // Undo last round
                        if (this.gameData == null || !this.gameData.didGameStart())
                            System.out.println(gameNotStartedErrorMessage);
                        else {
                            if (this.gameData.undoRound()) {
                                System.out.println("Undo successful!");
                                showGameStatus(this.gameData.getBoard().getTerritories(), this.gameData.getPlayers(), this.gameData.getCurrentRound());
                            } else
                                System.out.println("Undo failed! Current round is the first round!");
                        }
                        break;
                    }
                    case 7: { // Save game
                        if (this.gameData == null || !this.gameData.didGameStart())
                            System.out.println(gameNotStartedErrorMessage);
                        else {
                            System.out.print("Enter the full path and name of the file to save the game: ");
                            String saveGameError = this.gameData.saveGame(scanner.nextLine());
                            if (saveGameError != null)
                                System.out.println("ERROR! " + saveGameError);
                            else
                                System.out.println("Game saved successfully!");
                        }
                        break;
                    }
                    case 8: { // Load game
                        System.out.print("Enter the full path of the file to load (.dat file): ");

                        if (this.gameData == null || !gameData.isValidGameConfig())
                            this.gameData = new GameData();

                        String loadGameError = this.gameData.loadGame(scanner.nextLine());

                        if (loadGameError != null)
                            System.out.println("ERROR! " + loadGameError);
                        else
                            System.out.println("Game loaded successfully!");

                        break;
                    }
                    case 9: { // Quit
                        quit = true;
                        break;
                    }
                    default: { // Input Error
                        System.out.println("ERROR! Please enter a number between 1 and 6!");
                        break;
                    }
                }
            } catch (NumberFormatException e){
                System.out.println("ERROR! You must enter a number.");
            }
        }
    }

    private void printMenu(){

        final String[] menuOptions = {
                "Load game XML configuration file.",
                "Start a new game.",
                "Show game status.",
                "Start next game round.",
                "Show current game history.",
                "Undo last round.",
                "Save game.",
                "Load game.",
                "Quit.",
        };

        System.out.println("\nChoose a command (enter a number between 1 and 6):");

        for(int i = 0; i < menuOptions.length; i++)
            System.out.println((i+1) + ". " + menuOptions[i]);
    }

    private void printGameConfigErrors(){
        Stack<String> gameMessages = this.gameData.getGameErrorMessages();

        System.out.println("The following errors occurred during XML parsing of the game configuration:");
        for(int i = 0; i < gameMessages.size(); i++)
            System.out.println(gameMessages.pop());
    }

    private void drawGameBoard(Territory[] boardTerritories) {

        int boardRows = this.gameData.getBoard().getRows();
        int boardColumns = this.gameData.getBoard().getColumns();
        String border = "";
        String leftAlignFormat = "%-1c%-" + (TERRITORY_SIZE * 3) + "s%-1c";

        for (int i = 0; i < (TERRITORY_SIZE * 3); i++)
            border += "-";

        for (int row = 0; row < boardRows; row++) {
            for (int territoryRow = 0; territoryRow < TERRITORY_SIZE - 1; territoryRow++) {
                for (int column = 0; column < boardColumns; column++) {

                    int territoryIndex = (row * boardColumns) + column;
                    Territory territory = boardTerritories[territoryIndex];
                    if (territoryRow == 0) {
                        System.out.format(leftAlignFormat, '/', border, '\\');
                    } else if (territoryRow == TERRITORY_SIZE - 2) {
                        System.out.format(leftAlignFormat, '\\', border, '/');
                    } else {
                        if (territoryRow == 1)
                            System.out.format(leftAlignFormat, '|', " Territory Number: " + territory.getId(), '|');
                        else if (territoryRow == TERRITORY_SIZE - 5)
                            System.out.format(leftAlignFormat, '|', " Profit: " + territory.getProfit(), '|');
                        else  if (territoryRow == TERRITORY_SIZE - 4)
                            System.out.format(leftAlignFormat, '|', " Army Threshold: " + territory.getArmyThreshold(), '|');
                        else if (territoryRow == TERRITORY_SIZE - 3)
                            System.out.format(leftAlignFormat, '|', " " + (territory.isConquered() ? "Conquerer: " + this.gameData.getPlayerById(territory.getConqueringPlayerId()).getNameAndId() : "Neutral"), '|');
                        else
                            System.out.format(leftAlignFormat, '|', "", '|');
                    }
                }
                System.out.print("\n");
            }
        }
    }
}
