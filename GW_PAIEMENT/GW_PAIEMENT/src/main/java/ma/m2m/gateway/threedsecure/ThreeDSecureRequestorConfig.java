package ma.m2m.gateway.threedsecure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreeDSecureRequestorConfig {
	
	 public static final Gson GSON = new GsonBuilder().serializeNulls().create();
}
