package es.deusto.sd.ecoembesClient.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * HTTP proxy for Dumpster-related operations against the Ecoembes backend.
 * Requires a valid auth token obtained via AuthProxy.login().
 */
public class DumpsterProxy {

	private final String baseUrl; // e.g. "http://localhost:8081"

	public DumpsterProxy(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/* ========== 1. CREATE DUMPSTER ========== */

	public void createDumpster(long dumpsterId,
			int pc,
			String city,
			String address,
			String type,
			String token) throws IOException {

		String path = String.format(
				"/dumpsters?dumpsterID=%d&dumpsterPC=%d&dumpsterCity=%s&dumpsterAddress=%s&dumpsterType=%s&token=%s",
				dumpsterId, pc, city, address, type, token
				);

		URL url = new URL(baseUrl + path);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");

		int status = con.getResponseCode();
		if (status != 204 && status != 200) {
			throw new IOException("HTTP error creating dumpster: " + status);
		}
	}

	/* ========== 2. UPDATE DUMPSTER CAPACITY ========== */

	public String updateDumpster(long id,
			int containers,
			String token) throws IOException {

		String path = String.format(
				"/dumpsters/%d?Containers=%d&Token=%s",
				id, containers, token
				);

		URL url = new URL(baseUrl + path);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("PUT");
		con.setRequestProperty("Accept", "application/json");

		int status = con.getResponseCode();
		if (status == 200) {
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				return sb.toString(); // JSON DumpsterDTO
			}
		}
		throw new IOException("HTTP error updating dumpster: " + status);
	}



	/* ========== 3. USAGE BY DUMPSTER ID ========== */

	public String getUsageById(long id,
			LocalDate from,
			LocalDate to,
			String token) throws IOException {

		var fmt = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
		String fromStr = from.format(fmt);
		String toStr = to.format(fmt);

		String path = String.format(
				"/dumpsters/%d/usages?FromDate=%s&ToDate=%s&token=%s",
				id, fromStr, toStr, token
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
					sb.append(line).append('\n');
				}
			}
			return sb.toString(); // JSON UsageDTO
		}
		throw new IOException("HTTP error querying usage by id: " + status);
	}


	/* ========== 4. STATUS BY POSTAL CODE ========== */

	public String getStatusByPostalCode(int pc,
			LocalDate date,
			String token) throws IOException {

		var fmt = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
		String dateStr = date.format(fmt);

		String path = String.format(
				"/dumpsters/%d/statuses?Date=%s&token=%s",
				pc, dateStr, token
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
					sb.append(line).append('\n');
				}
			}
			return sb.toString(); // JSON StatusDTO
		}
		throw new IOException("HTTP error querying status by postal code: " + status);
	}


}
