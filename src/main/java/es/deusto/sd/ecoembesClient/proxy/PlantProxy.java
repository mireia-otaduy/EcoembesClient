package es.deusto.sd.ecoembesClient.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PlantProxy {

    private final String baseUrl; // e.g. "http://localhost:8081"
    // En el cliente seguimos usando yyyy-MM-dd para introducir la fecha
    // y aquí la convertimos a ddMMyyyy para el backend.
    private final DateTimeFormatter fmtBackend = DateTimeFormatter.ofPattern("ddMMyyyy");

    public PlantProxy(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /* ========== 1. GET PLANT CAPACITY BY NAME ========== */

    public String getPlantCapacity(String plantName,
                                   LocalDate date,
                                   String token) throws IOException {

        String dateStr = date.format(fmtBackend); // ddMMyyyy

        // EmployeeController:
        // @GetMapping("/plants/{plantName}/capacities")
        // @RequestParam("Date") LocalDate (ddMMyyyy)
        // @RequestParam("token") String
        String path = String.format(
                "/plants/%s/capacities?Date=%s&token=%s",
                URLEncoder.encode(plantName, StandardCharsets.UTF_8),
                dateStr,
                URLEncoder.encode(token, StandardCharsets.UTF_8)
        );

        URL url = new URL(baseUrl + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int status = con.getResponseCode();
        if (status == 200) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString(); // JSON PlantCapacityDTO
        }
        throw new IOException("HTTP error getting plant capacity: " + status);
    }

    /* ========== 2. ASSIGN DUMPSTER TO PLANT (BY NAME) ========== */

    public boolean assignDumpsterToPlant(long dumpsterId,
                                         String plantName,
                                         String token) throws IOException {

        // EmployeeController:
        // @GetMapping("/dumpsters/{dumpsterID}/assignments")
        // @RequestParam("plantName") String, @RequestParam("token") String
        String path = String.format(
                "/dumpsters/%d/assignments?plantName=%s&token=%s",
                dumpsterId,
                URLEncoder.encode(plantName, StandardCharsets.UTF_8),
                URLEncoder.encode(token, StandardCharsets.UTF_8)
        );

        URL url = new URL(baseUrl + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int status = con.getResponseCode();
        if (status == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String body = br.readLine(); // "true" o "false"
                return body != null && body.trim().equalsIgnoreCase("true");
            }
        }
        throw new IOException("HTTP error assigning dumpster: " + status);
    }
}
