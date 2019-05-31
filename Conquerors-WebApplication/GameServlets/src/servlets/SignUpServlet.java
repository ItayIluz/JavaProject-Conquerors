package servlets;

import engine.GameData;
import managers.GameManager;
import managers.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@WebServlet(name = "servlets.SignUpServlet", urlPatterns = "/signUp")
public class SignUpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        HashMap<String,Object> responseJSON = new HashMap<>();

        responseJSON.put("inGameTitle", null);

        if(usernameFromSession != null) { // User is already logged in
            responseJSON.put("result", "CONNECTED");
            responseJSON.put("playerUsername", usernameFromSession);

            GameManager gameManager = ServletUtils.getGameManager(getServletContext());
            for(Map.Entry<String, GameData> entry : gameManager.getGames().entrySet()){
                if(entry.getValue().isPlayerInGame(usernameFromSession))
                    responseJSON.put("inGameTitle", entry.getKey());
            }
        }
        else
            responseJSON.put("result", "Not connected.");

        ServletUtils.sendJSONResponse(response, responseJSON);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        String postUsername = ServletUtils.getValueFromPostAction(request, "username").getAsString();
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        HashMap<String,String> responseJSON = new HashMap<>();

        if(usernameFromSession == null){
            synchronized(this){
                  if(userManager.isUserExists(postUsername)){
                    responseJSON.put("result", "User already exists!");
                } else {
                    userManager.getUsers().add(postUsername);
                    request.getSession(true).setAttribute("username", postUsername);
                    responseJSON.put("result", "SUCCESS");
                }
            }
        } else {
            responseJSON.put("result", "SUCCESS");
        }

        ServletUtils.sendJSONResponse(response, responseJSON);
    }

    @Override
    public String getServletInfo() {
        return "Sign up servlet.";
    }
}
