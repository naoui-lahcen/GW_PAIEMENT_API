package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.AnnlTransactionDto;
import org.springframework.ui.Model;

import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;

import javax.servlet.http.HttpSession;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface AutorisationService {
	
	ThreeDSecureResponse preparerAeqThree3DSS(DemandePaiementDto demandeDto,String folder,String file);
	
	ThreeDSecureResponse getRreqFromThree3DSSAfterACS(String decodedCres, String folder, String file);
	
	 String controllerDataRequest(DemandePaiementDto demandeDto);

	 ThreeDSecureResponse autoriser(ThreeDSecureResponse reponse, String folder, String file);
	 
	 ThreeDSecureResponse preparerAeqMobileThree3DSS(DemandePaiementDto demandeDto,String folder,String file);
	 
	 String controlleRisk(DemandePaiementDto demandeDto,String folder,String file);
	 
	 ThreeDSecureResponse preparerProcessOutAeqThree3DSS(DemandePaiementDto demandeDto,String folder,String file);
	 
	 void processPaymentPageData(DemandePaiementDto demandeDto,String folder, String file);
	 
	 void processInfosMerchant(DemandePaiementDto demandeDto, String folder, String file);

	void logMessage(String file, String message);

	String handleSwitchError(Exception e, String file, String orderid, String merchantid, String resp_tlv, DemandePaiementDto dmd, Model model, String page);

	String handleMpiError(String errmpi, String file, String idDemande, String threeDSServerTransID,
						  DemandePaiementDto dmd, Model model, String page);

	String handleMerchantAndInfoCommercantError(String file, String orderid, String merchantid, String websiteid,
												DemandePaiementDto demandeDtoMsg, Model model, String page, boolean isMerchantError);

	String handleCardValidationError(int iCardValid, String cardNumber, String orderid, String merchantid, DemandePaiementDto demandeDto,
									 String file, DemandePaiementDto demandeDtoMsg, Model model, String page);

	String handleSessionTimeout(
			HttpSession session, String file, int timeout, DemandePaiementDto demandeDto,
			DemandePaiementDto demandeDtoMsg, Model model);

	String getFailUrl(String xid);

	AnnlTransactionDto envoieAnnulation(AnnlTransactionDto annlTransactionDto);
}
