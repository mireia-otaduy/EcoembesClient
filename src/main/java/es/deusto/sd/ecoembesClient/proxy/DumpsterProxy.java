package es.deusto.sd.ecoembesClient.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import es.deusto.sd.ecoembesClient.data.Dumpster;

/**
 * HTTP proxy for Dumpster-related operations against the Ecoembes backend.
 * Requires a valid auth token obtained via AuthProxy.login.
 */
@Service
public class DumpsterProxy {

    @Value("${ecoembes.base-url}")
    private String baseUrl; // e.g. http://localhost:8080

    private final RestTemplate restTemplate;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("ddMMyyyy");

    public DumpsterProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 1. CREATE DUMPSTER
    public void createDumpster(long dumpsterId, int pc, String city, String address, String type, String token) {
        String path = String.format(
                "/dumpsters?dumpsterID=%d&dumpsterPC=%d&dumpsterCity=%s&dumpsterAddress=%s&dumpsterType=%s&token=%s",
                dumpsterId, pc, city, address, type, token);
        String url = baseUrl + path;

        ResponseEntity<Void> resp = restTemplate.postForEntity(url, null, Void.class);
        int status = resp.getStatusCode().value();
        if (status != 204 && status != 200) {
            throw new RuntimeException("HTTP error creating dumpster: " + status);
        }
    }

    // 2. UPDATE DUMPSTER CAPACITY
    public String updateDumpster(long id, int containers, String token) {
        String path = String.format("/dumpsters/%d?Containers=%d&Token=%s", id, containers, token);
        String url = baseUrl + path;

        ResponseEntity<String> resp = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.PUT,
                null,
                String.class
        );
        int status = resp.getStatusCode().value();
        if (status == 200) { // JSON DumpsterDTO
            return resp.getBody();
        }
        throw new RuntimeException("HTTP error updating dumpster: " + status);
    }

    // 3. USAGE BY DUMPSTER ID
    public String getUsageById(long id, LocalDate from, LocalDate to, String token) {
        String fromStr = from.format(fmt);
        String toStr = to.format(fmt);
        String path = String.format(
                "/dumpsters/%d/usages?FromDate=%s&ToDate=%s&token=%s",
                id, fromStr, toStr, token);
        String url = baseUrl + path;

        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        int status = resp.getStatusCode().value();
        if (status == 200) { // JSON UsageDTO (texto completo, sin los que añadas antes)
            return resp.getBody();
        }
        throw new RuntimeException("HTTP error querying usage by id: " + status);
    }

    // 4. STATUS BY POSTAL CODE
    public String getStatusByPostalCode(int pc, LocalDate date, String token) {
        String dateStr = date.format(fmt);
        String path = String.format(
                "/dumpsters/%d/statuses?Date=%s&token=%s",
                pc, dateStr, token);
        String url = baseUrl + path;

        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        int status = resp.getStatusCode().value();
        if (status == 200) { // JSON StatusDTO
            return resp.getBody();
        }
        throw new RuntimeException("HTTP error querying status by postal code: " + status);
    }

    // 5. LISTA DE TODOS LOS DUMPSTERS
    public List<Dumpster> getAllDumpsters(String token) {
        String endpoint = baseUrl + "/dumpsters/retrievals?token=" + token;

        ResponseEntity<String> resp = restTemplate.getForEntity(endpoint, String.class);
        int status = resp.getStatusCode().value();
        if (status == 200) {
            String json = resp.getBody();
            if (json == null) {
                return List.of();
            }
            json = json.trim();
            System.out.println("Received JSON (dumpsters): " + json);

            // Remove surrounding [ ] from JSON array
            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
            }

            List<Dumpster> dumpsters = new ArrayList<>();
            if (json.isBlank()) {
                return dumpsters;
            }

            // Very simple manual parse, assumes no nested objects and flat fields
            String[] items = json.split("},");
            for (String item : items) {
                item = item.replace("{", "").replace("}", "").trim();

                long id = 0;
                int containers = 0;
                String level = "";
                int pc = 0;

                String[] fields = item.split(",");
                for (String field : fields) {
                    String[] kv = field.split(":", 2);
                    if (kv.length != 2) continue;
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    switch (key) {
                        case "id" -> id = Long.parseLong(value);
                        case "containers" -> containers = Integer.parseInt(value);
                        case "level" -> level = value;
                        case "pc" -> pc = Integer.parseInt(value);
                        default -> {}
                    }
                }
                dumpsters.add(new Dumpster(id, containers, level, pc));
            }
            return dumpsters;
        } else if (status == 401) {
            throw new SecurityException("Unauthorized: invalid token");
        } else {
            throw new RuntimeException("Server error: " + status);
        }
    }
}
