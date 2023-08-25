package ma.m2m.gateway.switching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public final class SwitchTCPClientV2 {

	private final Socket clientSocket;
	private final Object lock = new Object();

	public SwitchTCPClientV2(String host, int port) throws IOException {
		clientSocket = new Socket(host, port);
		if (clientSocket != null)
			clientSocket.setSoTimeout(90000);
	}

	public boolean isConnected() throws SocketException {
		if (clientSocket != null)
			return this.clientSocket.isConnected();
		else
			return false;
	}

	public String sendMessage(String trameTLV) throws IOException {
		PrintWriter out;
		BufferedReader in;
		String response = "";

		out = new PrintWriter(clientSocket.getOutputStream());
		out.print(trameTLV);
		out.flush();

		synchronized (lock) {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			response = (char) in.read() + "";
			try {
				while (in.ready())
					response += (char) in.read();
			} finally {
				in.close();
				out.close();
				shutdown();
			}
		}

		return response;
	}

	public void shutdown() throws IOException {
		clientSocket.close();
	}

}
