package ma.m2m.gateway.threedsecure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.experimental.UtilityClass;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@UtilityClass
public class ThreeDSecureRequestorConfig {
	
	 public static final Gson GSON = new GsonBuilder().serializeNulls().create();
}
