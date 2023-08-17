package ma.m2m.gateway.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SplittableRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.dto.UserDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.GalerieService;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class GWPaiementController {

	@Autowired
	private DemandePaiementService demandePaiementService;
	
	@Autowired
	AutorisationService autorisationService;
	
	@Value("${key.LINK_SUCCESS}")
	private String link_success;
	
	@Value("${key.LINK_FAIL}")
	private String link_fail;
	
    @Value("${key.SECRET}")
    private String secret;
    
    @Value("${key.USER_TOKEN}")
    private String usernameToken;
    
	@Autowired
	CommercantService commercantService;
	
	@Autowired
	GalerieService galerieService;
    
	private Traces traces = new Traces();
	private LocalDateTime date;
	private String folder;
	private String file;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  

	public GWPaiementController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// date of folder logs
		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
	}

	@RequestMapping(path = "/")
	@ResponseBody
	public String home() {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start home() ************** ");
		System.out.println("*********** Start home() ************** ");
		
		String msg="Bienvenue dans la plateforme NAPS de paiement !!!";

		System.out.println("*********** Fin home() ************** ");

		return msg;
	}
	
	@RequestMapping(path = "/napspayment/generatetoken")
	@ResponseBody
	public ResponseEntity<String> generateToken() {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start generateToken() ************** ");
		System.out.println("*********** Start generateToken() ************** ");

		// pour tester la generation du tocken
		JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
		String msg="";
		try {
			String token = jwtTokenUtil.generateToken(usernameToken, secret);
			String userFromToken = jwtTokenUtil.getUsernameFromToken(token);
			Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token);
			Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token);
			
			System.out.println("token generated : " + token);
			traces.writeInFileTransaction(folder, file, "userFromToken generated : " + userFromToken);
			System.out.println("userFromToken generated : " + userFromToken);
			traces.writeInFileTransaction(folder, file, "userFromToken generated : " + userFromToken);
			String dateSysStr = dateFormat.format(new Date());
			System.out.println("dateSysStr : " + dateSysStr);
			traces.writeInFileTransaction(folder, file, "dateSysStr : " + dateSysStr);
			System.out.println("dateExpiration : " + dateExpiration);
			traces.writeInFileTransaction(folder, file, "dateExpiration : " + dateExpiration);
			String dateExpirationStr = dateFormat.format(dateExpiration);
			System.out.println("dateExpirationStr : " + dateExpirationStr);
			traces.writeInFileTransaction(folder, file, "dateExpirationStr : " + dateExpirationStr);
			String condition = isTokenExpired == false ? "Non" : "OUI" ;
			System.out.println("token is expired : " + condition );
			traces.writeInFileTransaction(folder, file, "token is expired : " + condition );
			msg = "le token est généré avec succès";
		} catch(Exception ex) {
			msg = "echec lors de la génération du token";
		}

		// fin
		System.out.println("*********** Fin generateToken() ************** ");
		
		 return ResponseEntity.ok().body(msg);
	}
	
	@RequestMapping(path = "/napspayment/generateexcel")
	@ResponseBody
	public String generateExcel() {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start generateExcel() ************** ");
		System.out.println("*********** Start generateExcel() ************** ");

		String msg = "";
		// pour tester la generation du fichier 
		GenerateExcel excel = new GenerateExcel();
		try {
			excel.generateExcel();
			msg = "le fichier excel est généré avec succès";
		} catch(Exception ex) {
			msg = "echec lors de la génération du fichier excel";
		}
		
		// fin
		
		System.out.println("*********** Fin generateExcel() ************** ");
		
		return "OK";
	}
	
	@RequestMapping("/napspayment/index")
	public String index() {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start index ()");
		traces.writeInFileTransaction(folder, file, "*********** returns to index.html ************** ");
		System.out.println("*********** returns to index.html ************** ");

		return "index";
	}
	
	@RequestMapping(value = "/napspayment/authorization/token/{token}", method = RequestMethod.GET)
	public String showPagePayment(@PathVariable(value = "token") String token, Model model) {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start showPagePayment ************** ");
		System.out.println("*********** Start showPagePayment ************** ");
		
		traces.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);		
		
		DemandePaiementDto demandeDto = demandePaiementService.findByTokencommande(token);
				
		if(demandeDto == null) {
			traces.writeInFileTransaction(folder, file, "demandeDto null token : " + token);
			System.out.println("demandeDto null token : " + token);		
		}
		
		CommercantDto merchant = null;
		String merchantid = "";
		String orderid = "";
		try {
			merchantid =demandeDto.getComid();
			orderid = demandeDto.getCommande();
			merchant = commercantService.findByCmrCode(merchantid);
			if(merchant !=null) {
				demandeDto.setCommercantDto(merchant);
			}
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid );

			return "authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]";
		}
		GalerieDto galerie = null;
		try {
			merchantid =demandeDto.getComid();
			orderid = demandeDto.getCommande();
			galerie = galerieService.findByCodeCmr(merchantid);
			if(galerie !=null) {
				demandeDto.setGalerieDto(galerie);
			}
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid );

			return "authorization 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]";
		}

		
		model.addAttribute("demandeDto", demandeDto);
		System.out.println("findByTokencommande Iddemande recupérée : " + demandeDto.getIddemande());
		traces.writeInFileTransaction(folder, file, "findByTokencommande Iddemande recupérée : " + demandeDto.getIddemande());
		
		traces.writeInFileTransaction(folder, file, "*********** Fin showPagePayment ************** ");
		System.out.println("*********** Fin showPagePayment ************** ");

		return "napspayment";
	}
	
	@RequestMapping(path = "/napspayment/linkpayment", produces = "application/json; charset=UTF-8")
	public ResponseEntity<responseDto> getLink(@RequestBody DemandePaiementDto demandeDto) {

		System.out.println("*********** Start getLink ************** ");
		System.out.println("demandeDto commerçant recupérée : " + demandeDto.getComid());
		System.out.println("demandeDto Commande recupérée : " + demandeDto.getCommande());
		System.out.println("DemandeDto montant recupérée : " + demandeDto.getMontant());
				
		String urlRetour ="";
		String result="";
		responseDto response = new responseDto();
		
		// pour faciliter le test : result = ""
		//String result = autorisationService.controllerDataRequest(demandeDto);
		String tokencommande = "HLNDI25454205VRZR2104202";
		demandeDto.setTokencommande(tokencommande);

		if(result.equals("")) {
			
//			DemandePaiementDto demandeSaved = demandePaiementService.save(demandeDto);
//			
//			traces.writeInFileTransaction(folder, file, "*********** demandeSaved apres save ************** ");
//			System.out.println("*********** demandeSaved apres save ************** ");
//			
//			traces.writeInFileTransaction(folder, file, "demandeSaved apres save idDemande : " + demandeSaved.getIddemande());
//			System.out.println(" demandeSaved apres save idDemande : " + demandeSaved.getIddemande());
//			
//			Objects.copyProperties(demandeDto, demandeSaved);
			
//			String tokencommande = Util.genTokenCom(demandeDto.getCommande(), demandeDto.getComid());
//			demandeDto.setTokencommande(tokencommande);
			
			urlRetour = link_success+demandeDto.getTokencommande();
			
			response.setErrorNb("000");
			response.setMsgRetour("Valide");
			response.setUrl(urlRetour);
			traces.writeInFileTransaction(folder, file, "Link response success : " + urlRetour);
			System.out.println("Link response Success: " + response.toString());
			
		} else {
			urlRetour = link_fail+demandeDto.getTokencommande();

			traces.writeInFileTransaction(folder, file, "Manque d'information dans la demande : " );
			traces.writeInFileTransaction(folder, file, "message : " + result);
			System.out.println("Manque d'information dans la demande : ");
			
			response.setErrorNb("900");
			response.setMsgRetour("Erreur");
			response.setUrl(urlRetour);
			traces.writeInFileTransaction(folder, file, "Link response error : " + urlRetour);
			System.out.println("Link response error : " + response.toString());
		}
		
		// Return the link in the response
		// Map<String, String> response = new HashMap();
		// response.put("url", urlRetour);
		
		System.out.println("*********** Fin getLink ************** ");
		  
		  return ResponseEntity.ok().body(response);
	}
	

	@PostMapping("/payer")
	public String payer(Model model, @ModelAttribute("demandeDto") DemandePaiementDto demandeDto) {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start payer ()");
		System.out.println("Start payer ()");
		System.out.println("demandeDto commande : " + demandeDto.getCommande());
		System.out.println("demandeDto montant : " + demandeDto.getMontant());
				
		//ThreeDSecureResponse result = autorisationService.payer(demandeDto, folder, file);
		
		String htmlCreq = "<form action='https://acs.naps.ma:443/lacs2' method='post' enctype='application/x-www-form-urlencoded'>"
				+ "<input type='hidden' name='creq' value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjQxZDQ0ZTViLTBjOTYtNGVhNC05NjkxLTM1OWVmOGQ5NTdjMyIsCiAgImFjc1RyYW5zSUQiOiAiOTI3NTQyOGEtYzkzYi00ZWUzLTk3NDEtNDA4NzAzNDlmYzM2IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' />"
				+ "</form>";
		demandeDto.setCreq(htmlCreq);
		System.out.println("demandeDto htmlCreq : " + demandeDto.getCreq());

		
		return "chalenge";
	}
	
	@RequestMapping(value = "/chalenge", method = RequestMethod.GET)
	public String chlenge(Model model) {
		DemandePaiementDto dem = new DemandePaiementDto();
		System.out.println("Start chalenge ()");
		
		String htmlCreq = "<form action='https://acs.naps.ma:443/lacs2' method='post' enctype='application/x-www-form-urlencoded'>"
				+ "<input type='hidden' name='creq' value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjQxZDQ0ZTViLTBjOTYtNGVhNC05NjkxLTM1OWVmOGQ5NTdjMyIsCiAgImFjc1RyYW5zSUQiOiAiOTI3NTQyOGEtYzkzYi00ZWUzLTk3NDEtNDA4NzAzNDlmYzM2IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' />"
				+ "</form>";
		dem.setCreq(htmlCreq);
		System.out.println("dem htmlCreq : " + dem.getCreq());
				
		System.out.println("dem commande : " + dem.getCommande());
		System.out.println("dem montant : " + dem.getMontant());
		
		model.addAttribute("demandeDto", dem);
		System.out.println("*********** returns to chalenge.html ************** ");

		return "chalenge";
	}
	
	
	@RequestMapping(value = "/napspayment/error/token/{token}", method = RequestMethod.GET)
	public String error(@PathVariable(value = "token") String token, Model model) {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start error ************** ");
		System.out.println("*********** Start error ************** ");
		
		traces.writeInFileTransaction(folder, file, "*********** Fin error ************** ");
		System.out.println("*********** Fin error ************** ");

		return "error";
	}
	
	@PostMapping(value = "/saveDemande")
	public String saveDemande(Model model, @RequestBody DemandePaiementDto demandeDto) {
		System.out.println("demandeDto commande : " + demandeDto.getCommande());
		System.out.println("demandeDto montant : " + demandeDto.getMontant());
		model.addAttribute("demandeDto", demandeDto);
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start saveDemande ()");
		traces.writeInFileTransaction(folder, file, "*********** returns to napspayment.html ************** ");
		System.out.println("*********** returns to napspayment.html ************** ");
		
		//String result = autorisationService.controllerDataRequestRequest(demandeDto);
		String result="";
		
		// pour teste la fonction findByDem_xid
		DemandePaiementDto demandeP = new DemandePaiementDto();
		demandeDto.setDem_xid("a1adb46d-f916-4895-9f02-20425478697f");
		traces.writeInFileTransaction(folder, file, "findByDem_xid xid : " + demandeDto.getDem_xid());
		System.out.println("findByDem_xid xid : " + demandeDto.getDem_xid());
		demandeP = demandePaiementService.findByDem_xid(demandeDto.getDem_xid());
		System.out.println("findByDem_xid apres return xid : " + demandeP.getDem_xid());
		
		if(result.equals("")) {
			
			String tokencommande = Util.genTokenCom(demandeDto.getCommande(), demandeDto.getComid());
			demandeDto.setTokencommande(tokencommande);
//			DemandePaiementDto demandeSaved = demandePaiementService.save(demandeDto);
//			
//			traces.writeInFileTransaction(folder, file, "*********** demandeSaved apres save ************** ");
//			System.out.println("*********** demandeSaved apres save ************** ");
//			
//			traces.writeInFileTransaction(folder, file, "demandeSaved apres save idDemande : " + demandeSaved.getIddemande());
//			System.out.println("demandeSaved apres save idDemande : " + demandeSaved.getIddemande());
//			
//			Objects.copyProperties(demandeDto, demandeSaved);
			
		} else {
			traces.writeInFileTransaction(folder, file, "Manque d'information dans la demande : " );
			traces.writeInFileTransaction(folder, file, "message : " + result);
			System.out.println("Manque d'information dans la demande : ");
		}

		return "result";
	}
	
	@PostMapping("/infoDemande")
	public String infoDemande(@ModelAttribute("infoDemande") DemandePaiementDto demandeDto) {
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start payer ()");
		System.out.println("Start payer ()");
		
		ThreeDSecureResponse result = autorisationService.callThree3DSS(demandeDto, folder, file);
		
		return "info-demande";
	}
	
	@RequestMapping(value = "/napspayment/index2", method = RequestMethod.GET)
	public String index2() {
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** returns to index2.html ************** ");
		System.out.println("*********** returns to index2.html ************** ");

		return "index2";
	}
	
	@RequestMapping(value = "/napspayment/result", method = RequestMethod.GET)
	public String result() {
		traces.creatFileTransaction(file);
		System.out.println("*********** returns to result.html ************** ");
		System.out.println("*********** returns to resut.html ************** ");

		return "result";
	}

	@RequestMapping(path = "/napspayment/api", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public UserDto getUser(@RequestBody UserDto userDto) {

		System.out.println("*********** getUser ************** ");
		System.out.println("userDto name json : " + userDto.getName());
		System.out.println("userDto email json : " + userDto.getEmail());

		UserDto newuser = new UserDto();

		newuser.setEmail("lucas@gmail.com");
		newuser.setName("lucas");

		System.out.println("*********** getUser ************** ");
		System.out.println("newuser name : " + newuser.getName());
		System.out.println("newuser email : " + newuser.getEmail());

		userDto.setEmail(newuser.getEmail());
		userDto.setName(newuser.getName());

		System.out.println("*********** getUser ************** ");
		System.out.println("name userDto  apres remplacement : " + userDto.getName());
		System.out.println("email userDto apres remplacement : " + userDto.getEmail());

		return userDto;
	}
	
	
// 
//    @GetMapping("/napspayment/showFormForUpdate/{id}")
//    public String updateForm(@PathVariable(value = "id") long id, Model model) {
//        DemandePaiement DemandePaiement = demandePaiementService.findById(id);
//        model.addAttribute("demandePaiement", DemandePaiement);
//        return "index";
//    }
// 
//    @GetMapping("/napspayment/deleteDemandePaiement/{id}")
//    public String deleteThroughId(@PathVariable(value = "id") long id) {
//        demandePaiementService.deleteViaId(id);
//        return "redirect:/";
// 
//    }

}
