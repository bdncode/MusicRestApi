package org.mypackage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class UtilsFactory {
    private static Logger logger = Logger.getLogger(UtilsFactory.class.getName());

    protected static Connection getConnectionProps(){
        Connection ret = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            File dbPropsFile = new File("\\config.properties");
            FileReader fileReader = new FileReader(dbPropsFile);
            props.load(fileReader);
            String dbConnUrl = props.getProperty("db.url");
            String dbUserName = props.getProperty("db.username");
            String dbPassword = props.getProperty("db.password");
            ret = DriverManager.getConnection(dbConnUrl, dbUserName, dbPassword);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            return ret;
        }
    }

    static JsonObject getBody(HttpServletRequest req){
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = req.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (IOException ex) {
            logger.warning(ex.getLocalizedMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    logger.warning(ex.getLocalizedMessage());
                }
            }
        }


        return new JsonParser().parse(stringBuilder.toString()).getAsJsonObject();
    }
}
