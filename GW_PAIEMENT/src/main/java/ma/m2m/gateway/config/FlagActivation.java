package ma.m2m.gateway.config;

public enum FlagActivation {
	
	ACTIVE("Y"),
	NOT_ACTIVE("N");
	
	private String flag;

	FlagActivation(String flag) {
		this.flag = flag;
	}

	public String getFlag() {
		return flag;
	}

}
