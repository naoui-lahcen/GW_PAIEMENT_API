
package ma.m2m.gateway.lydec;

import java.util.Arrays;

public class Impayes {

    private Impaye[] listeImpayes;

    private Client client;

    private String message;

    private boolean ok;

	public Impaye[] getListeImpayes() {
		return listeImpayes;
	}

	public void setListeImpayes(Impaye[] listeImpayes) {
		this.listeImpayes = listeImpayes;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public Impayes(Impaye[] listeImpayes, Client client, String message, boolean ok) {
		super();
		this.listeImpayes = listeImpayes;
		this.client = client;
		this.message = message;
		this.ok = ok;
	}

	public Impayes() {
		super();
	}

	@Override
	public String toString() {
		return "Impayes [listeImpayes=" + Arrays.toString(listeImpayes) + ", client=" + client + ", message=" + message
				+ ", ok=" + ok + "]";
	}

    

}
