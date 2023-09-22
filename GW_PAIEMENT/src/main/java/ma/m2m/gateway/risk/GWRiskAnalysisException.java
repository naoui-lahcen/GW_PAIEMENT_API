package ma.m2m.gateway.risk;


/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21
 */

public class GWRiskAnalysisException extends Exception {

	private static final long serialVersionUID = 1L;

	public GWRiskAnalysisException() {
		super();
	}

	public GWRiskAnalysisException(String message) {
		super(message);
	}

	public GWRiskAnalysisException(Throwable cause) {
		super(cause);
	}
	
	public GWRiskAnalysisException(String message, Throwable cause) {
		super(message, cause);
	}
	

}
