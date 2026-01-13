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
import java.util.ArrayList;
import java.util.List;

import es.deusto.sd.ecoembesClient.data.Dumpster;
import es.deusto.sd.ecoembesClient.data.Plant;

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
    
    public List<Plant> getAllPlants(String token) throws IOException {

	    String endpoint = baseUrl + "/plants/retrievals?token=" + token;
	    URL url = new URL(endpoint);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("Accept", "application/json");
	    conn.setDoOutput(true);

	    int responseCode = conn.getResponseCode();

	    if (responseCode == HttpURLConnection.HTTP_OK) {
	        // Read the response into a String
	        StringBuilder sb = new StringBuilder();
	        try (BufferedReader br = new BufferedReader(
	                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	        }

	        String json = sb.toString().trim();
	        System.out.println("Received JSON: " + json);
	        // Remove surrounding [ ] from JSON array
	        if (json.startsWith("[") && json.endsWith("]")) {
	            json = json.substring(1, json.length() - 1);
	        }

	        List<Plant> plants = new ArrayList<>();

	        // Split each object (assumes no nested } inside objects)
	        String[] items = json.split("\\},\\s*\\{");

	        for (String item : items) {
	            item = item.replace("{", "").replace("}", "").trim();

	            long id = 0;
	            String name = "";
	            int pc = 0;
	            
	            String[] fields = item.split(",");
	            for (String field : fields) {
	                String[] kv = field.split(":", 2);
	                if (kv.length != 2) continue;

	                String key = kv[0].trim().replace("\"", "");
	                String value = kv[1].trim().replace("\"", "");
	                
	                switch (key) {
	                    case "id" -> id = Long.parseLong(value);
	                    case "name" -> name = value;
	                    case "pc" -> pc = Integer.parseInt(value);
	                    
	                }
	            }

	            // Construct the immutable record
	            plants.add(new Plant(id, name,pc));
	        }

	        return plants;

	    } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
	        throw new SecurityException("Unauthorized: invalid token");
	    } else {
	        throw new IOException("Server error: " + responseCode);
	    }
	}
}
