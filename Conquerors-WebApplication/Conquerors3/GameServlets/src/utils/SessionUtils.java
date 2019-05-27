package utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtils {

    public static String getUsername (HttpServletRequest request) {
        String sessionUsername = null;
        HttpSession session = request.getSession(false);

        if(session != null)
            sessionUsername = (String) session.getAttribute("username");

        return sessionUsername;
    }

    public static void clearSession (HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
