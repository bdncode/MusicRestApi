package org.mypackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class MusicLibraryApp extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI().toLowerCase();
        JsonArray queryResult = new JsonArray();
        if (uri.matches(".*/artists")) {
            queryResult = getArtists(resp);
        } else if (uri.matches(".*/albums")) {
            queryResult = getAlbums(resp);
        } else if (uri.matches(".*/albumsfrom")) {
            queryResult = getAlbumsFromYear(resp, req.getParameter("year"));
        } else {
            resp.setStatus(404);
        }
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
        resp.setHeader("Accept", "*/*");
        resp.setHeader("Content-Type", "application/json");
        resp.getOutputStream().print(queryResult.toString());
    }

    private JsonArray getArtists(HttpServletResponse resp) {
        Connection connection = UtilsFactory.getConnectionProps();
        JsonArray artists = new JsonArray ();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM artists order by ArtistName;");
            while (resultSet.next()) {
                JsonObject artist = new JsonObject ();
                artist.addProperty("artistName",resultSet.getString("ArtistName"));
                artist.addProperty("genre",resultSet.getString("Genre"));
                artists.add(artist);
                resp.setStatus(200);
            }
        } catch (SQLException e) {
            resp.setStatus(500);
        }
        return artists;
    }

    private JsonArray getAlbums(HttpServletResponse resp) {
        Connection connection = UtilsFactory.getConnectionProps();
        JsonArray albums = new JsonArray ();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM albums order by AlbumName;");
            while (resultSet.next()) {
                JsonObject album = new JsonObject ();
                album.addProperty("albumName",resultSet.getString("AlbumName"));
                album.addProperty("releaseYear",resultSet.getString("ReleaseYear"));
                albums.add(album);
                resp.setStatus(200);
            }
        } catch (SQLException e) {
            resp.setStatus(500);
        }
        return albums;
    }

    private JsonArray getAlbumsFromYear(HttpServletResponse resp, String year) {
        Connection connection = UtilsFactory.getConnectionProps();
        JsonArray albums = new JsonArray ();
        try {
            PreparedStatement preparedStatementArtist = connection.prepareStatement(
            "SELECT ArtistName, AlbumName FROM artists INNER JOIN albums" +
            " ON albums.Artist = artists.Id WHERE ReleaseYear = ? ORDER BY ArtistName;");
            preparedStatementArtist.setString(1, year);
            ResultSet resultSet = preparedStatementArtist.executeQuery();
            while (resultSet.next()) {
                JsonObject album = new JsonObject ();
                album.addProperty("artistName",resultSet.getString("ArtistName"));
                album.addProperty("albumName",resultSet.getString("AlbumName"));
                albums.add(album);
                resp.setStatus(200);
            }
        } catch (SQLException e) {
            resp.setStatus(500);
        }
        return albums;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI().toLowerCase();
        JsonObject body = UtilsFactory.getBody(req);
        if (uri.matches(".*/artist")) {
            resp = postArtist(body, resp);
        } else if(uri.matches(".*/album")) {
            resp = postAlbum(body, resp);
        } else {
            resp.setStatus(404);
        }
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
        resp.setHeader("Accept", "*/*");
        resp.setHeader("Content-Type", "application/json");
        if (resp.getStatus() == 201) {
            resp.getOutputStream().print("OK");
        }
    }

    protected HttpServletResponse postArtist(JsonObject body, HttpServletResponse resp) {
        Connection connection = UtilsFactory.getConnectionProps();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `artists` (`ArtistName`, `Genre`) VALUES (?, ?);");
            preparedStatement.setString(1, body.get("artistName").getAsString());
            preparedStatement.setString(2, body.get("genre").getAsString());
            preparedStatement.execute();
            resp.setStatus(201);
        } catch (SQLException e) {
            resp.setStatus(500);
        }
        return resp;
    }

    protected HttpServletResponse postAlbum(JsonObject body, HttpServletResponse resp) {
        Connection connection = UtilsFactory.getConnectionProps();
        try {

            PreparedStatement preparedStatementArtist = connection.prepareStatement("SELECT Id FROM artists WHERE artistName = (?);");
            preparedStatementArtist.setString(1, body.get("Artist").getAsString());
            ResultSet resultSet = preparedStatementArtist.executeQuery();
            int i = 0;
            if (resultSet.next()) {
                PreparedStatement preparedStatementAlbum = connection.prepareStatement("INSERT INTO albums (Artist, AlbumName, ReleaseYear, StoredInDb) VALUES (?, ?, ?, now());");
                preparedStatementAlbum.setInt(1, resultSet.getInt("Id"));
                preparedStatementAlbum.setString(2, body.get("AlbumName").getAsString());
                preparedStatementAlbum.setString(3, body.get("ReleaseYear").getAsString());
                preparedStatementAlbum.execute();
                resp.setStatus(201);
            }
        } catch (SQLException e) {
            resp.setStatus(500);
        }
        return resp;
    }

    protected void doDelete (HttpServletRequest req, HttpServletResponse resp) {
        Connection connection = UtilsFactory.getConnectionProps();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM artists WHERE Id = ?;");
            preparedStatement.setInt(1, Integer.parseInt(req.getParameter("id")));
            preparedStatement.executeUpdate();
            resp.setStatus(200);
        } catch (SQLException e) {
            resp.setStatus(500);
        }
    }
}
