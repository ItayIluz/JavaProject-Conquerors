package servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engine.GameData;
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
import java.util.HashMap;

@WebServlet(name = "ConquerorsGameServlet", urlPatterns = "/conquerorsGame")
public class ConquerorsGameServlet extends HttpServlet{

    private void addGame(String[] params){
       /* response.setContentType("application/json");
        Gson gson = new GsonBuilder().create();

        JSONO*/
    }

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
        HashMap<String, Object> responseJSON = new HashMap<>();

        String gameTitle = request.getParameter("gameTitle");
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (gameTitle != null) {
            GameData gamedData = gameManager.getGames().get(gameTitle);

            responseJSON.put("gameData", gamedData);
            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONReponse(response, responseJSON);
    }

    private void leaveGame(PostAction postAction, HttpServletRequest request, HttpServletResponse response){
        HashMap<String, String> responseJSON = new HashMap<>();

        String usernameFromSession = SessionUtils.getUsername(request);
        GameManager gameManager = ServletUtils.getGameManager(getServletContext());

        if (usernameFromSession != null) {
            GameData gamedData = gameManager.getGames().get(postAction.getValues().get("gameTitle"));
            gamedData.removePlayer(usernameFromSession);

            responseJSON.put("result", "SUCCESS");
        } else {
            responseJSON.put("result", "ERROR");
        }

        ServletUtils.sendJSONReponse(response, responseJSON);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        PostAction postAction = ServletUtils.createPostActionFromRequest(request);

        if (postAction.getAction().equals("leaveGame"))
            leaveGame(postAction, request, response);
        /*else if(postAction.getAction().equals("joinGame"))
            userJoinGame(postAction, request, response);*/
    }

    @Override
    public String getServletInfo() {
        return "Conquerors game servlet.";
    }
}
