package es.deusto.sd.ecoembesClient.proxy;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AuthProxy {

    private final String baseUrl; // ej: "http://localhost:8081"

    public AuthProxy(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String login(String email, String password) throws IOException {
        URL url = new URL(baseUrl + "/auth/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String jsonBody = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }";

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = con.getResponseCode();
        if (status == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                return br.readLine(); // token devuelto por el backend (String plano)
            }
        } else if (status == 401) {
            return null; // credenciales incorrectas
        }
        throw new IOException("Error HTTP en login: " + status);
    }

    public boolean logout(String token) throws IOException {
        URL url = new URL(baseUrl + "/auth/logout");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String body = "\"" + token + "\"";

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = con.getResponseCode();
        if (status == 204) {
            return true;            // logout OK
        }
        if (status == 401) {
            // Token already invalid/expired -> for the client it's also "OK"
            return false;
        }
        throw new IOException("HTTP error in logout: " + status);
    }

}

