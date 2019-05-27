package servlets;

import engine.GameData;
import managers.GameManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@WebServlet(name = "FileUploadServlet", urlPatterns = "/uploadFile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean isGameTitleValid = true;
        HashMap<String, Object> responseJSON = new HashMap<>();
        ArrayList<String> errorMessageDetails = new ArrayList<>();

        Part filePart = request.getPart("uploadedFile");
       /* String filePath = getServletContext().getAttribute("javax.servlet.context.tempdir").toString();
        Object filePath2 = getServletContext().getAttribute("javax.servlet.context.tempdir");*/
        String fileName = getFileName(filePart);

        if(fileName != null){
            File uploadedFile = new File(fileName);
            GameData newGameData = new GameData(uploadedFile);

            String newGameTitle = newGameData.getGameTitle();

            if(newGameTitle != null) {
                synchronized (this) { // Check for unique game title
                    GameManager gameManager = ServletUtils.getGameManager(getServletContext());
                    if (gameManager.isGameExists(newGameData.getGameTitle())) {
                        isGameTitleValid = false;
                        responseJSON.put("result", "ERROR");
                        errorMessageDetails.add("A game already exists with that title!");
                        responseJSON.put("errors", errorMessageDetails);
                    }
                }
            }

            if(!newGameData.isValidGameConfig()) { // File is invalid...
                responseJSON.put("result", "ERROR");
                errorMessageDetails.addAll(newGameData.getGameErrorMessages());
                responseJSON.put("errors", errorMessageDetails);
            } else {
                if(isGameTitleValid) {
                    responseJSON.put("result", "SUCCESS");
                    synchronized (this) {
                        GameManager gameManager = ServletUtils.getGameManager(getServletContext());
                        gameManager.getGames().put(newGameTitle, newGameData);
                        newGameData.setUploadedBy(SessionUtils.getUsername(request));
                    }
                }
            }
       } else { // Error in uploaded file itself
            responseJSON.put("result", "ERROR");
            errorMessageDetails.add("Invalid file.");
            responseJSON.put("errors", errorMessageDetails);
        }

        ServletUtils.sendJSONReponse(response, responseJSON);
    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public String getServletInfo() {
        return "Manages file upload.";
    }
}
