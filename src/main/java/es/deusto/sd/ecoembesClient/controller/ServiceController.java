package es.deusto.sd.ecoembesClient.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import es.deusto.sd.ecoembesClient.data.Dumpster;
import es.deusto.sd.ecoembesClient.data.Plant;
import es.deusto.sd.ecoembesClient.proxy.AuthProxy;
import es.deusto.sd.ecoembesClient.proxy.DumpsterProxy;
import es.deusto.sd.ecoembesClient.proxy.PlantProxy;

public class ServiceController {
	private AuthProxy authProxy;
	private DumpsterProxy dumpsterProxy;
	private PlantProxy plantProxy;
	private String token;

    public ServiceController(String baseUrl) {
        this.authProxy = new AuthProxy(baseUrl);
        this.dumpsterProxy = new DumpsterProxy(baseUrl);
        this.plantProxy = new PlantProxy(baseUrl);
    }
    
    public String login(String email, String password) throws IOException {
    	token = authProxy.login(email, password);
		return token;
	}
    
	public boolean logout(String token) throws IOException {
		return authProxy.logout(token);
	}
	
	public void createDumpster(long dumpsterId,
			int pc,
			String city,
			String address,
			String type,
			String token) throws IOException {
		dumpsterProxy.createDumpster(dumpsterId, pc, city, address, type, token);
	}
	
	public String updateDumpster(long id,
			int containers,
			String token) throws IOException {
		return dumpsterProxy.updateDumpster(id, containers, token);
	}
	public String getUsageById(long id,
			LocalDate from,
			LocalDate to,
			String token) throws IOException {
		return dumpsterProxy.getUsageById(id, from, to, token);
	}
	
	public String getStatusByPostalCode(int pc,
			LocalDate date,
			String token) throws IOException {
		return dumpsterProxy.getStatusByPostalCode(pc, date, token);
	}
	
	public String getPlantCapacity(String plantName,
            LocalDate date,
            String token) throws IOException {
			return plantProxy.getPlantCapacity(plantName, date, token);
	}
	
	public boolean assignDumpsterToPlant(long dumpsterId,
            String plantName,
            String token) throws IOException {
			return plantProxy.assignDumpsterToPlant(dumpsterId, plantName, token);
	}
	
	public List<Dumpster> getAllDumpsters(String token) throws IOException {
		return dumpsterProxy.getAllDumpsters(token);
	}
	public List<Plant> getAllPlants(String token) throws IOException {
		return plantProxy.getAllPlants(token);
	}
	
	public AuthProxy getAuthProxy() {
		return authProxy;
	}
	
	
}
