package es.deusto.sd.ecoembesClient.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class AuthProxy {

    @Value("${ecoembes.base-url}")
    private String baseUrl; // ej: "http://localhost:8081"

    public AuthProxy() {
        // constructor vacío para Spring
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
                // token devuelto por el backend (String plano)
                return br.readLine();
            }
        } else if (status == 401) {
            // credenciales incorrectas
            return null;
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
            // logout OK
            return true;
        }
        if (status == 401) {
            // Token ya inválido/expirado -> para el cliente también es "OK"
            return false;
        }
        throw new IOException("HTTP error in logout: " + status);
    }
}
