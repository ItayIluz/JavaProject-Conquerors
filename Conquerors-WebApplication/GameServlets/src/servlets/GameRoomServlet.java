package servlets;


import engine.GameData;
import managers.GameManager;
import managers.UserManager;
import utils.PostAction;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "servlets.GameRoomServlet", urlPatterns = "/gamesRoom")
public class GameRoomServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, Object> responseJSON = new HashMap<>();
        ArrayList<HashMap<String, Object>> games = new ArrayList<>();

        synchronized (this) {
            GameManager gameManager = ServletUtils.getGameManager(getServletContext());
            UserManager userManager = ServletUtils.getUserManager(getServletContext());

            for (Map.Entry<String, GameData> entry : gameManager.getGames().entrySet()) {
                HashMap<String, Object> currentGame = new HashMap<>();
                String currentGameTitle = entry.getKey();
                GameData currentGameData = entry.getValue();

                currentGame.put("title", currentGameTitle);
                currentGame.put("uploadedBy", currentGameData.getUploadedBy());
                currentGame.put("status", currentGameData.getGameStatus().name());
                currentGame.put("boardSize", currentGameData.getBoard().getRows() + " x " + currentGameData.getBoard().getColumns());
                currentGame.put("playersInGame", currentGameData.getCurrentPlayers() + " / " + currentGameData.getTotalPlayers());
                currentGame.put("units", currentGameData.getGameUnits());
                currentGame.put("board", currentGameData.getBoard());

                games.add(currentGame);
            }
            responseJSON.put("gamesData", games);

            ArrayList<String> players = new ArrayList<>(userManager.getUsers());
            responseJSON.put("playersData", players);
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void logoutUser(HttpServletRequest request, HttpServletResponse response){

        HashMap<String, String> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession != null) {
            userManager.getUsers().remove(usernameFromSession);
            SessionUtils.clearSession(request);
            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    private void userJoinGame(PostAction postAction, HttpServletRequest request, HttpServletResponse response){
        HashMap<String, String> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (usernameFromSession != null) {
            GameData gamedData = gameManager.getGames().get(postAction.getValues().get("gameTitle").getAsString());

            synchronized (this) {
                gamedData.addPlayer(usernameFromSession);
            }

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        PostAction postAction = ServletUtils.createPostActionFromRequest(request);

        if (postAction.getAction().equals("logout"))
            logoutUser(request, response);
        else if(postAction.getAction().equals("joinGame"))
            userJoinGame(postAction, request, response);
    }

    @Override
    public String getServletInfo() {
        return "Game room servlet.";
    }
}
