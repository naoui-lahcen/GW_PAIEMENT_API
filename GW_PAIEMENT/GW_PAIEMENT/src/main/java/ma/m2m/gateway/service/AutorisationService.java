package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;

public interface AutorisationService {
	
	ThreeDSecureResponse callThree3DSS(DemandePaiementDto demandeDto,String folder,String file);
	
	ThreeDSecureResponse callThree3DSSAfterACS(String decodedCres, String folder, String file);
	
	 String controllerDataRequest(DemandePaiementDto demandeDto);

	 ThreeDSecureResponse autoriser(ThreeDSecureResponse reponse, String folder, String file);
}