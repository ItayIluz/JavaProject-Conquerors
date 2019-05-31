package servlets;

import com.google.gson.*;
import engine.Army;
import engine.GameData;
import engine.Player;
import managers.GameManager;
import utils.PostAction;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet(name = "ConquerorsGameServlet", urlPatterns = "/conquerorsGame")
public class ConquerorsGameServlet extends HttpServlet{

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        Gson gson = new GsonBuilder().create();

        GameData gameData = new GameData(new File("C:\\Users\\ItayI\\Desktop\\ex2-medium.xml"));

        getServletContext().setAttribute("GameData", gameData);

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(gameData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        String getAction = request.getParameter("get");

        if(getAction.equals("gameData"))
            getGameData(request,response);
        else if(getAction.equals("actionsOnTerritories"))
            getActionsOnTerritories(request,response);
    }

    private void getGameData(HttpServletRequest request, HttpServletResponse response){

        HashMap<String, Object> responseJSON = new HashMap<>();

        String gameTitle = request.getParameter("gameTitle");
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());
        String usernameFromSession = SessionUtils.getUsername(request);

        if (gameTitle != null) {
            GameData gameData = gameManager.getGames().get(gameTitle);

            responseJSON.put("gameData", gameData);
            responseJSON.put("isUsersTurn", false);
            responseJSON.put("lastPlayerName", gameData.getLastPlayerName());
            responseJSON.put("lastPlayerAction", gameData.getLastPlayerAction());

            if(gameData.didGameStart()) {

                gameData.checkGamerOver();
                if (gameData.checkGamerOver()) {
                    responseJSON.put("gameResults", gameData.getGameResults());
                } else {
                    Player currentPlayer = gameData.getCurrentPlayer();
                    if(currentPlayer != null){
                        if (usernameFromSession.equals(currentPlayer.getName()))
                            responseJSON.put("isUsersTurn", true);
                    }
                }
            }

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void getActionsOnTerritories(HttpServletRequest request, HttpServletResponse response){

        HashMap<String, Object> responseJSON = new HashMap<>();

        String gameTitle = request.getParameter("gameTitle");
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());
        String usernameFromSession = SessionUtils.getUsername(request);

        if (gameTitle != null) {
            GameData gameData = gameManager.getGames().get(gameTitle);

            responseJSON.put("actionsOnTerritories", gameData.getAllowedActionsOnBoardForPlayer(usernameFromSession));

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void leaveGame(PostAction postAction, HttpServletRequest request, HttpServletResponse response){
        HashMap<String, String> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (usernameFromSession != null) {
            GameData gameData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());
            gameData.removePlayer(usernameFromSession);

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void endPlayerTurn(PostAction postAction, HttpServletRequest request, HttpServletResponse response, boolean didPlayerSurrender){
        HashMap<String, Object> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (usernameFromSession != null) {
            GameData gameData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());

            if(!didPlayerSurrender){
                if(gameData.canCurrentPlayerTakeAction()) {
                    gameData.setLastPlayerName(usernameFromSession);
                    gameData.setLastPlayerAction("skipped his/her turn.");
                }
            }

            if (gameData.setNextPlayer() == null) {
                if (!gameData.checkGamerOver()) {
                    gameData.setNextPlayer();
                    gameData.startNextRound();
                }
            }

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void playerSurrender(PostAction postAction, HttpServletRequest request, HttpServletResponse response){

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        synchronized(this) {
            if (usernameFromSession != null) {
                GameData gameData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());

                gameData.setLastPlayerName(usernameFromSession);
                gameData.setLastPlayerAction("surrendered.");
                gameData.getCurrentPlayer().setSurrendered(true);
                gameData.removePlayer(usernameFromSession);
                endPlayerTurn(postAction, request, response, true);
            }
        }
    }

    private void fixArmiesCompetence(PostAction postAction, HttpServletRequest request, HttpServletResponse response){

        HashMap<String, Object> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (usernameFromSession != null) {
            GameData gameData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());

            gameData.fixTerritoryArmiesCompetence(postAction.getValues().get("territoryId").getAsInt(), postAction.getValues().get("totalCostToFix").getAsInt());

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void addArmiesToTerritory(PostAction postAction, HttpServletResponse response){

        HashMap<String, String> responseJSON = new HashMap<>();
        ArrayList<Army> newArmies;

        GameManager gameManager = ServletUtils.getGameManager(getServletContext());
        GameData gameData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());

        newArmies = getArmiesFromJSON(gameData, postAction);

        gameData.addArmiesToTerritory(postAction.getValues().get("territoryId").getAsInt(), newArmies, postAction.getValues().get("totalNewArmiesCost").getAsInt());

        responseJSON.put("result", "SUCCESS");

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private ArrayList<Army> getArmiesFromJSON(GameData gameData, PostAction postAction){
        ArrayList<Army> armies = new ArrayList<>();

        Player currentPlayer = gameData.getCurrentPlayer();
        JsonArray postedArmies = postAction.getValues().get("armies").getAsJsonArray();

        for (JsonElement element : postedArmies) {
            JsonObject currentArmy = element.getAsJsonObject();
            armies.add(new Army(
                    currentArmy.get("amount").getAsInt(),
                    gameData.getGameUnit(currentArmy.get("unit").getAsJsonObject().get("type").getAsString()),
                    currentPlayer)
            );
        }

        return armies;
    }

    private void calculateAttackResults(PostAction postAction, HttpServletRequest request, HttpServletResponse response){

        HashMap<String, Object> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (usernameFromSession != null) {
            GameData gameData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());

            ArrayList<Army> attackingArmy = getArmiesFromJSON(gameData, postAction);

            HashMap<String, Object> results = gameData.calculateAttackResults(
                    postAction.getValues().get("territoryId").getAsInt(),
                    postAction.getValues().get("attackType").getAsString(),
                    attackingArmy
            );

            ArrayList<Army> winningArmy = (ArrayList<Army>) results.get("winningArmy");

            responseJSON.put("attackingArmy", results.get("attackingArmy"));
            responseJSON.put("defendingArmy", results.get("defendingArmy"));
            responseJSON.put("winningPlayerName", winningArmy.size() > 0 ? winningArmy.get(0).getControllingPlayer().getName() : null);
            responseJSON.put("winningArmy", winningArmy);
            responseJSON.put("attackResultsDescription", results.get("attackResultsDescription"));
            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        PostAction postAction = ServletUtils.createPostActionFromRequest(request);

        if (postAction.getAction().equals("leaveGame"))
            leaveGame(postAction, request, response);
        else if(postAction.getAction().equals("endTurn"))
            endPlayerTurn(postAction, request, response, false);
        else if(postAction.getAction().equals("addArmiesToTerritory"))
            addArmiesToTerritory(postAction, response);
        else if(postAction.getAction().equals("playerSurrender"))
            playerSurrender(postAction, request, response);
        else if(postAction.getAction().equals("fixArmiesCompetence"))
            fixArmiesCompetence(postAction, request, response);
        else if(postAction.getAction().equals("calculateAttackResults"))
            calculateAttackResults(postAction, request, response);
    }

    @Override
    public String getServletInfo() {
        return "Conquerors game servlet.";
    }
}
