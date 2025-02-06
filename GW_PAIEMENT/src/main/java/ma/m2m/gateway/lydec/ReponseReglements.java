
package ma.m2m.gateway.lydec;

public class ReponseReglements {

    protected int numeroTransaction;
    protected String message;
    protected boolean ok;

    public int getNumeroTransaction() {
        return numeroTransaction;
    }

    public void setNumeroTransaction(int value) {
        this.numeroTransaction = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        this.message = value;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean value) {
        this.ok = value;
    }

}
