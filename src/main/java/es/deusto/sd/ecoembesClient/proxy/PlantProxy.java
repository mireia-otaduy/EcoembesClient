package es.deusto.sd.ecoembesClient.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import es.deusto.sd.ecoembesClient.data.Dumpster;
import es.deusto.sd.ecoembesClient.data.Plant;

@Service
public class PlantProxy {

    @Value("${ecoembes.base-url}")
    private String baseUrl; // e.g. http://localhost:8080

    // En el cliente seguimos usando yyyy-MM-dd para introducir la fecha y aquí la convertimos a ddMMyyyy para el backend.
    private final DateTimeFormatter fmtBackend = DateTimeFormatter.ofPattern("ddMMyyyy");
    private final RestTemplate restTemplate;

    public PlantProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 1. GET PLANT CAPACITY BY NAME
    public String getPlantCapacity(String plantName, LocalDate date, String token) {
        String dateStr = date.format(fmtBackend); // ddMMyyyy
        String encodedName = URLEncoder.encode(plantName, StandardCharsets.UTF_8);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);

        String path = String.format(
                "/plants/%s/capacities?Date=%s&token=%s",
                encodedName, dateStr, encodedToken);

        String url = baseUrl + path;
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        int status = resp.getStatusCode().value();
        if (status == 200) {
            return resp.getBody(); // JSON PlantCapacityDTO
        }
        throw new RuntimeException("HTTP error getting plant capacity: " + status);
    }

    // 2. ASSIGN DUMPSTER TO PLANT BY NAME
    public boolean assignDumpsterToPlant(long dumpsterId, String plantName, String token) {
        String encodedName = URLEncoder.encode(plantName, StandardCharsets.UTF_8);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);

        String path = String.format(
                "/dumpsters/%d/assignments?plantName=%s&token=%s",
                dumpsterId, encodedName, encodedToken);

        String url = baseUrl + path;
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        int status = resp.getStatusCode().value();
        if (status == 200) {
            String body = resp.getBody();
            return body != null && body.trim().equalsIgnoreCase("true");
        }
        throw new RuntimeException("HTTP error assigning dumpster: " + status);
    }

    // 3. LISTA DE TODAS LAS PLANTAS
    public List<Plant> getAllPlants(String token) {
        String endpoint = baseUrl + "/plants/retrievals?token=" + token;

        ResponseEntity<String> resp = restTemplate.getForEntity(endpoint, String.class);
        int status = resp.getStatusCode().value();
        if (status == 200) {
            String json = resp.getBody();
            if (json == null) {
                return List.of();
            }
            json = json.trim();
            System.out.println("Received JSON (plants): " + json);

            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
            }

            List<Plant> plants = new ArrayList<>();
            if (json.isBlank()) {
                return plants;
            }

            String[] items = json.split("},");
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
                        default -> {}
                    }
                }
                plants.add(new Plant(id, name, pc));
            }
            return plants;
        } else if (status == 401) {
            throw new SecurityException("Unauthorized: invalid token");
        } else {
            throw new RuntimeException("Server error: " + status);
        }
    }
}
