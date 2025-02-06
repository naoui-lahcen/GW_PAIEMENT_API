package ma.m2m.gateway.switching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public final class SwitchTCPClient {

	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	private static SwitchTCPClient INSTANCE;

	private SwitchTCPClient() {
	}

	public static SwitchTCPClient getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SwitchTCPClient();
		}

		return INSTANCE;
	}

	public boolean startConnection(String ip, int port) throws IOException, UnknownHostException {
		clientSocket = new Socket(ip, port);

		if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed()) {
			clientSocket.setSoTimeout(/* 15000 */90000); // TODO: fixpack090922
			// TODO: return clientSocket.isConnected();
			return true;
		}
		else
			return false;

	}

	public String sendMessage(String msg) throws IOException {

		out = new PrintWriter(clientSocket.getOutputStream());
		out.print(msg);
		out.flush();
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String resp = (char) in.read() + "";
		while (in.ready()) {
			resp += (char) in.read();
		}

		return resp;
	}

	public void stopConnection() throws IOException {
		in.close();
		out.close();
		clientSocket.close();
	}

}
