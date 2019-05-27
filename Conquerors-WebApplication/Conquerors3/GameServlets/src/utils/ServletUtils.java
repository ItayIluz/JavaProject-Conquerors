package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engine.xmlclasses.Game;
import managers.GameManager;
import managers.UserManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ServletUtils {

    private static final Object userManagerLock = new Object();
    private static final Object gameManagerLock = new Object();

    public static PostAction createPostActionFromRequest(HttpServletRequest request){
        PostAction postAction = null;
        try {
            postAction = new Gson().fromJson(request.getReader(), PostAction.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return postAction;
    }

    public static String getValueFromPostAction(HttpServletRequest request, String key){
        PostAction postAction = createPostActionFromRequest(request);

        if(postAction != null){
            if(postAction.getValues().containsKey(key)){
                return postAction.getValues().get(key);
            }
        }

        return null;
    }

    public static void sendJSONReponse(HttpServletResponse response, HashMap jsonToSend){
        response.setContentType("application/json");
        Gson gson = new GsonBuilder().create();

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonToSend));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UserManager getUserManager(ServletContext servletContext) {

        synchronized (userManagerLock) {
            if (servletContext.getAttribute("userManager") == null) {
                servletContext.setAttribute("userManager", new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute("userManager");
    }

    public static GameManager getGameManager(ServletContext servletContext) {
        synchronized (gameManagerLock) {
            if (servletContext.getAttribute("gameManager") == null) {
                servletContext.setAttribute("gameManager", new GameManager());
            }
        }
        return (GameManager) servletContext.getAttribute("gameManager");
    }
}
