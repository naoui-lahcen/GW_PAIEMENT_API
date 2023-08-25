package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface AutorisationService {
	
	ThreeDSecureResponse preparerReqThree3DSS(DemandePaiementDto demandeDto,String folder,String file);
	
	ThreeDSecureResponse callThree3DSSAfterACS(String decodedCres, String folder, String file);
	
	 String controllerDataRequest(DemandePaiementDto demandeDto);

	 ThreeDSecureResponse autoriser(ThreeDSecureResponse reponse, String folder, String file);
}
