package es.deusto.sd.ecoembesClient.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembesClient.data.Dumpster;
import es.deusto.sd.ecoembesClient.data.Plant;
import es.deusto.sd.ecoembesClient.proxy.AuthProxy;
import es.deusto.sd.ecoembesClient.proxy.DumpsterProxy;
import es.deusto.sd.ecoembesClient.proxy.PlantProxy;

@Service
public class ServiceController {

    private final AuthProxy authProxy;
    private final DumpsterProxy dumpsterProxy;
    private final PlantProxy plantProxy;

    public ServiceController(AuthProxy authProxy,
                             DumpsterProxy dumpsterProxy,
                             PlantProxy plantProxy) {
        this.authProxy = authProxy;
        this.dumpsterProxy = dumpsterProxy;
        this.plantProxy = plantProxy;
    }

    // LOGIN / LOGOUT

    public String login(String email, String password) throws Exception {
        return authProxy.login(email, password);
    }

    public boolean logout(String token) throws Exception {
        return authProxy.logout(token);
    }

    // DUMPSTERS

    public void createDumpster(long dumpsterId,
                               int pc,
                               String city,
                               String address,
                               String type,
                               String token) throws Exception {
        dumpsterProxy.createDumpster(dumpsterId, pc, city, address, type, token);
    }

    public String updateDumpster(long id,
                                 int containers,
                                 String token) throws Exception {
        return dumpsterProxy.updateDumpster(id, containers, token);
    }

    public String getUsageById(long id,
                               LocalDate from,
                               LocalDate to,
                               String token) throws Exception {
        return dumpsterProxy.getUsageById(id, from, to, token);
    }

    public String getStatusByPostalCode(int pc,
                                        LocalDate date,
                                        String token) throws Exception {
        return dumpsterProxy.getStatusByPostalCode(pc, date, token);
    }

    public List<Dumpster> getAllDumpsters(String token) throws Exception {
        return dumpsterProxy.getAllDumpsters(token);
    }

    // PLANTS

    public String getPlantCapacity(String plantName,
                                   LocalDate date,
                                   String token) throws Exception {
        return plantProxy.getPlantCapacity(plantName, date, token);
    }

    public boolean assignDumpsterToPlant(long dumpsterId,
                                         String plantName,
                                         String token) throws Exception {
        return plantProxy.assignDumpsterToPlant(dumpsterId, plantName, token);
    }

    public List<Plant> getAllPlants(String token) throws Exception {
        return plantProxy.getAllPlants(token);
    }

    public AuthProxy getAuthProxy() {
        return authProxy;
    }
}
