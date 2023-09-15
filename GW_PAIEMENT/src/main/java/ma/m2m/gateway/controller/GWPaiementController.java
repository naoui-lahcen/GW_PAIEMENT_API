package ma.m2m.gateway.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SplittableRandom;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;

import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.MonthDto;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.dto.UserDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.GalerieService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.service.TelecollecteService;
import ma.m2m.gateway.service.TransactionService;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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

	@Value("${key.LINK_INDEX}")
	private String link_index;

	@Value("${key.SECRET}")
	private String secret;

	@Value("${key.USER_TOKEN}")
	private String usernameToken;

	@Value("${key.SWITCH_URL}")
	private String ipSwitch;

	@Value("${key.SWITCH_PORT}")
	private String portSwitch;

	@Autowired
	CommercantService commercantService;

	@Autowired
	private InfoCommercantService infoCommercantService;

	@Autowired
	GalerieService galerieService;

	@Autowired
	HistoAutoGateService histoAutoGateService;

	@Autowired
	TransactionService transactionService;

	@Autowired
	TelecollecteService telecollecteService;

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
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start home() ************** ");
		System.out.println("*********** Start home() ************** ");

		String msg = "Bienvenue dans la plateforme de paiement NAPS !!!";

		System.out.println("*********** Fin home() ************** ");

		return msg;
	}

	@RequestMapping(path = "/napspayment/generatetoken")
	@ResponseBody
	public ResponseEntity<String> generateToken() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start generateToken() ************** ");
		System.out.println("*********** Start generateToken() ************** ");

		// pour tester la generation du tocken
		JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
		String msg = "";
		try {
			String token = jwtTokenUtil.generateToken(usernameToken, secret);
			String userFromToken = jwtTokenUtil.getUsernameFromToken(token);
			Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token);
			Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token);

			System.out.println("token generated : " + token);
			traces.writeInFileTransaction(folder, file, "userFromToken generated : " + userFromToken);
			System.out.println("userFromToken generated : " + userFromToken);
			String dateSysStr = dateFormat.format(new Date());
			System.out.println("dateSysStr : " + dateSysStr);
			traces.writeInFileTransaction(folder, file, "dateSysStr : " + dateSysStr);
			System.out.println("dateExpiration : " + dateExpiration);
			traces.writeInFileTransaction(folder, file, "dateExpiration : " + dateExpiration);
			String dateExpirationStr = dateFormat.format(dateExpiration);
			System.out.println("dateExpirationStr : " + dateExpirationStr);
			traces.writeInFileTransaction(folder, file, "dateExpirationStr : " + dateExpirationStr);
			String condition = isTokenExpired == false ? "Non" : "OUI";
			System.out.println("token is expired : " + condition);
			traces.writeInFileTransaction(folder, file, "token is expired : " + condition);
			msg = "le token est généré avec succès";
		} catch (Exception ex) {
			msg = "echec lors de la génération du token";
		}

		// fin
		System.out.println("*********** Fin generateToken() ************** ");

		return ResponseEntity.ok().body(msg);
	}

	@RequestMapping(path = "/napspayment/generateexcel")
	@ResponseBody
	public String generateExcel() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start generateExcel() ************** ");
		System.out.println("*********** Start generateExcel() ************** ");

		String msg = "";
		// pour tester la generation du fichier
		GenerateExcel excel = new GenerateExcel();
		try {
			excel.generateExpExcel();
			msg = "le fichier excel est généré avec succès";
		} catch (Exception ex) {
			msg = "echec lors de la génération du fichier excel";
		}

		// fin

		System.out.println("*********** Fin generateExcel() ************** ");

		return msg;
	}

	@RequestMapping(value = "/napspayment/histo/exportexcel/{merchantid}", method = RequestMethod.GET)
	public void exportToExcel(HttpServletResponse response,@PathVariable(value = "merchantid") String merchantid) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start exportToExcel ***********");
		System.out.println("*********** Start exportToExcel ***********");

		traces.writeInFileTransaction(folder, file, "findByHatNumcmr merchantid : " + merchantid);
		System.out.println("findByHatNumcmr merchantid : " + merchantid);
		
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=HistoriqueTrs_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		try {
			
			//List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findAll();
			List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findByHatNumcmr(merchantid);
			
			GenerateExcel excelExporter = new GenerateExcel(listHistoGate);
			
			excelExporter.export(response);
			
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file, "exportToExcel 500 merchantid:[" + merchantid + "]");
			traces.writeInFileTransaction(folder, file, "exportToExcel 500 exception" + e);
			e.printStackTrace();
		}
		
		traces.writeInFileTransaction(folder, file, "*********** Fin exportToExcel ***********");
		System.out.println("*********** Fin exportToExcel ***********");
	}

	@RequestMapping("/napspayment/index")
	public String index() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "return to index.html");
		System.out.println("return to index.html");

		return "index";
	}

	@RequestMapping(value = "/napspayment/authorization/token/{token}", method = RequestMethod.GET)
	public String showPagePayment(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_PAGE_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start affichage page ***********");
		System.out.println("*********** Start affichage page ***********");

		traces.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = null;
		CommercantDto merchant = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspayment";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				System.out.println("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/" + demandeDto.getCommande());
				traces.writeInFileTransaction(folder, file,
						"DemandePaiement is found iddemande/Commande : " + demandeDto.getIddemande() + "/" + demandeDto.getCommande());

				// get list of years + 10
				int currentYear = Year.now().getValue();
				List<Integer> years = generateYearList(currentYear, currentYear + 10);

				demandeDto.setYears(years);

				// get list of months
				List<Month> months = Arrays.asList(Month.values());
				List<String> monthNames = convertMonthListToStringList(months);
				List<MonthDto> monthValues = convertStringAGListToFR(monthNames);

				demandeDto.setMonths(monthValues);
				// if cmr don't accept transaction cof demandeDto.getIs_cof() = N don't show
				// carte
				if (demandeDto.getIs_cof() == null || demandeDto.getIs_cof().equals("N")) {
					demandeDto.setDem_pan("");
					demandeDto.setDem_cvv("");
				}
				// if cmr accept transaction cof demandeDto.getIs_cof() = Y show your carte
				// saved
				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
					traces.writeInFileTransaction(folder, file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtat_demande().equals("SW_REJET")) {
					traces.writeInFileTransaction(folder, file, "Transaction rejetée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
				} else {
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						merchant = commercantService.findByCmrNumcmr(merchantid);
						if (merchant != null) {
							demandeDto.setCommercantDto(merchant);
						}
					} catch (Exception e) {
						traces.writeInFileTransaction(folder, file,
								"showPagePayment 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Merchant misconfigured in DB or not existing");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
					GalerieDto galerie = null;
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						galerie = galerieService.findByCodeCmr(merchantid);
						if (galerie != null) {
							demandeDto.setGalerieDto(galerie);
						}
					} catch (Exception e) {
						traces.writeInFileTransaction(folder, file,
								"showPagePayment 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Galerie misconfigured in DB or not existing");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
				}
			} else {
				traces.writeInFileTransaction(folder, file, "demandeDto not found token : " + token);
				System.out.println("demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("DEMANDE_PAIEMENT not fount in DB");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"showPagePayment 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token + "]"
							+ e);

			traces.writeInFileTransaction(folder, file, "showPagePayment 500 exception" + e);
			e.printStackTrace();
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("DEMANDE_PAIEMENT misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin affichage page ************** ");
		System.out.println("*********** Fin affichage page ************** ");

		return page;
	}

	@RequestMapping(path = "/napspayment/linkpayment1", produces = "application/json; charset=UTF-8")
	public ResponseEntity<responseDto> getLink1(@RequestBody DemandePaiementDto demandeDto) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;

		System.out.println("*********** Start getLink ************** ");
		System.out.println("demandeDto commerçant recupérée : " + demandeDto.getComid());
		System.out.println("demandeDto Commande recupérée : " + demandeDto.getCommande());
		System.out.println("DemandeDto montant recupérée : " + demandeDto.getMontant());

		String urlRetour = "";
		String result = "";
		responseDto response = new responseDto();

		// pour faciliter le test : result = ""
		// String result = autorisationService.controllerDataRequest(demandeDto);
		String tokencommande = "HLNDI25454205VRZR2104202";
		demandeDto.setTokencommande(tokencommande);

		if (result.equals("")) {

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

			urlRetour = link_success + demandeDto.getTokencommande();

			response.setErrorNb("000");
			response.setMsgRetour("Valide");
			response.setUrl(urlRetour);
			traces.writeInFileTransaction(folder, file, "Link response success : " + urlRetour);
			System.out.println("Link response Success: " + response.toString());

		} else {
			urlRetour = link_fail + demandeDto.getTokencommande();

			traces.writeInFileTransaction(folder, file, "Manque d'information dans la demande : ");
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
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_PAYE_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start payer ()");
		System.out.println("Start payer ()");
		System.out.println("demandeDto commande : " + demandeDto.getCommande());
		System.out.println("demandeDto montant : " + demandeDto.getMontant());

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", successURL, failURL,
				transactiontype;

		DemandePaiementDto dmd = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		String page = "chalenge";
		try {
			// Transaction info
			orderid = demandeDto.getCommande();
			amount = String.valueOf(demandeDto.getMontant());
			capture = "";
			currency = "504";
			recurring = "N";
			promoCode = "";
			transactionid = "";
			transactiontype = "0"; // 0 payment , P preauto

			// Merchnat info
			merchantid = demandeDto.getComid();
			merchantname = "";
			websiteName = "";
			websiteid = "";
			callbackUrl = demandeDto.getCallbackURL();
			successURL = demandeDto.getSuccessURL();
			failURL = demandeDto.getFailURL();

			// Card info
			cardnumber = demandeDto.getDem_pan();
			token = "";
			expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois());
			holdername = "";
			cvv = demandeDto.getDem_cvv();

			// Client info
			fname = demandeDto.getPrenom();
			lname = demandeDto.getNom();
			email = demandeDto.getEmail();
			country = demandeDto.getCountry();
			phone = demandeDto.getTel();
			city = demandeDto.getCity();
			state = demandeDto.getState();
			zipcode = demandeDto.getPostcode();
			address = demandeDto.getAddress();

		} catch (Exception jerr) {
			traces.writeInFileTransaction(folder, file, "payer 500 malformed json expression" + jerr);
			demandeDto.setMsgRefus("malformed json expression");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]" + e);
			demandeDto.setMsgRefus("Merchant misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (current_merchant == null) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");
			demandeDto.setMsgRefus("Merchant misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodactivite() == null) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");
			demandeDto.setMsgRefus("Merchant misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodbqe() == null) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");
			demandeDto.setMsgRefus("Merchant misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		InfoCommercantDto current_infoCommercant = null;

		try {
			current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 InfoCommercant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);

			// response.sendRedirect("GW-AUTO-INVALIDE-DEM");
			demandeDto.setMsgRefus("InfoCommercant misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (current_infoCommercant == null) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 InfoCommercantDto misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");

			// response.sendRedirect("GW-AUTO-INVALIDE-DEM");
			demandeDto.setMsgRefus("InfoCommercantDto misconfigured in DB or not existing");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			traces.writeInFileTransaction(folder, file, "payer 500 Card number length is incorrect orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			demandeDto.setMsgRefus("Card number length is incorrect");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (i_card_valid == 2) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Card number  is not valid incorrect luhn check orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDto.setMsgRefus("Card number  is not valid");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		int i_card_type = Util.getCardIss(cardnumber);

		try {
			DemandePaiementDto dmdToEdit = demandePaiementService.findByIdDemande(demandeDto.getIddemande());

			dmdToEdit.setDem_pan(cardnumber);
			dmdToEdit.setDem_cvv(cvv);
			dmdToEdit.setType_carte(i_card_type + "");
			dmdToEdit.setDateexpnaps(expirydate);
			dmdToEdit.setTransactiontype(transactiontype);
			dmdToEdit.setEtat_demande("INIT");

			formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			dmdToEdit.setDem_date_time(dateFormat.format(new Date()));

			demandeDto = demandePaiementService.save(dmdToEdit);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]"
							+ err1);
			demandeDto.setMsgRefus("Error during DEMANDE_PAIEMENT insertion");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		try {

			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			traces.writeInFileTransaction(folder, file,
					"payer 500 Error during  date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err2);
			demandeDto.setMsgRefus("Error during  date formatting");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		JSONObject jso = new JSONObject();

		// appel 3DSSecure ***********************************************************

		ThreeDSecureResponse threeDsecureResponse = autorisationService.preparerReqThree3DSS(demandeDto, folder, file);
		// fin 3DSSecure ***********************************************************

		/*
		 * ------------ DEBUT MPI RESPONSE PARAMS ------------
		 */
		String reponseMPI = "";
		String natbin = "";
		String eci = "";
		String cavv = "";
		String threeDSServerTransID = "";
		String xid = "";
		String errmpi = "";
		String idDemande = "";
		String expiry = ""; // YYMM
		String trameRepMPI = "";
		String idCommercant = "";
		String merchantName = "";
		String montantStr = "";

		String expirationCarte = "";
		String merchantEmail = "";
		String id_client = "";
		String idCommande = "";

		String paResSyntaxOK = "";
		String paResVerified = "";

		/*
		 * ------------ DEBUT COF INSTANCES ------------
		 */
		// TokenProcessor tk = new TokenProcessor();
		// Param_COF current_pcof = null;
		/*
		 * ------------ END COF INSTANCES ------------
		 */
		String link = "";
		String ref = "";
		String idService = "";
		String IdTxMTC = "";
		String statut = "";
		String retourWSKey5 = "";
		String Sec;
		String idTxSysPmt = "";
		String dateTX = "";
		int idsysPmt;
		String numTrans = "";

		// long numTransaction;

		if (threeDsecureResponse.getReponseMPI() != null) {
			reponseMPI = threeDsecureResponse.getReponseMPI();
		}
		if (threeDsecureResponse.getIdDemande() != null) {
			idDemande = threeDsecureResponse.getIdDemande();
		}
		if (threeDsecureResponse.getThreeDSServerTransID() != null) {
			threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
		}
		if (threeDsecureResponse.getEci() != null) {
			eci = threeDsecureResponse.getEci();
		} else {
			eci = "";
		}
		if (threeDsecureResponse.getCavv() != null) {
			cavv = threeDsecureResponse.getCavv();
		} else {
			cavv = "";
		}
		if (threeDsecureResponse.getErrmpi() != null) {
			errmpi = threeDsecureResponse.getErrmpi();
		} else {
			errmpi = "";
		}
		if (threeDsecureResponse.getExpiry() != null) {
			expiry = threeDsecureResponse.getExpiry();
		} else {
			expiry = "";
		}

		if (idDemande == null || idDemande.equals("")) {
			traces.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
			demandeDto.setEtat_demande("MPI_KO");
			demandePaiementService.save(demandeDto);
			traces.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO idDemande null : " + demandeDto.toString());
			demandeDto.setMsgRefus(
					"La transaction en cours n’a pas abouti (MPI_KO), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			traces.writeInFileTransaction(folder, file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			demandeDto.setMsgRefus(
					"La transaction en cours n’a pas abouti (demandePaiement not found), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("") || reponseMPI == null) {
			dmd.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmd);
			traces.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			traces.writeInFileTransaction(folder, file, "Response 3DS is null");
			demandeDto.setMsgRefus(
					"La transaction en cours n’a pas abouti (MPI_KO reponseMPI null), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("Y")) {
			// ********************* Frictionless responseMPI equal Y *********************
			traces.writeInFileTransaction(folder, file,
					"********************* Cas frictionless responseMPI equal Y *********************");

			dmd.setDem_xid(threeDSServerTransID);
			demandePaiementService.save(dmd);

//			try {
//				mm = new String[2];
//				montanttrame = "";
//
//				mm = amount.split("\\.");
//				if (mm[0].length() == 1) {
//					montanttrame = amount + "0";
//				} else {
//					montanttrame = amount + "";
//				}
//
//				m = new String[2];
//				m = montanttrame.split("\\.");
//				if (m[0].equals("0")) {
//					montanttrame = montanttrame.replace(".", "0");
//				} else
//					montanttrame = montanttrame.replace(".", "");
//				montanttrame = Util.formatageCHamps(montanttrame, 12);
//
//			} catch (Exception err3) {
//				traces.writeInFileTransaction(folder, file,
//						"payer 500 Error during  amount formatting for given orderid:[" + orderid
//								+ "] and merchantid:[" + merchantid + "]" + err3);
//				demandeDto.setMsgRefus("Error during  amount formatting");
//				model.addAttribute("demandeDto", demandeDto);
//				page = "result";
//				return page;
//			}
			
			try {
				montanttrame = "";

				mm = new String[2];
				
				System.out.println("montant v0 : " + amount);
				traces.writeInFileTransaction(folder, file, "montant v0 : " + amount);
				
				if(amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				if(!amount.contains(".") && !amount.contains(",")) {
					amount = amount +"."+"00";
				}
				System.out.println("montant v1 : " + amount);
				traces.writeInFileTransaction(folder, file, "montant v1 : " + amount);
				
				String montantt = amount + "";

				mm = montantt.split("\\.");
				if (mm[1].length() == 1) {
					montanttrame = amount + "0";
				} else {
					montanttrame = amount + "";
				}

				m = new String[2];
				m = montanttrame.split("\\.");
				if (m[1].equals("0")) {
					montanttrame = montanttrame.replace(".", "0");
				} else
					montanttrame = montanttrame.replace(".", "");
				montanttrame = Util.formatageCHamps(montanttrame, 12);
				System.out.println("montanttrame : " + montanttrame);
				traces.writeInFileTransaction(folder, file, "montanttrame : " + montanttrame);
			} catch (Exception err3) {
				traces.writeInFileTransaction(folder, file,
						"payer 500 Error during  amount formatting for given orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err3);
				demandeDto.setMsgRefus("Error during  amount formatting");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			merchantname = current_merchant.getCmrNom();
			websiteName = "";
			websiteid = "";
			String url = "", status = "", statuscode = "";

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(merchantname, 19, ' ');
			traces.writeInFileTransaction(folder, file, "merchant_name : [" + merchant_name + "]");

			merchant_city = "MOROCCO        ";
			traces.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

			acq_type = "0000";
			reason_code = "H";
			transaction_condition = "6";
			mesg_type = "0";

			processing_code = "";
			if (transactiontype.equals("0")) {
				processing_code = "0";
			} else if (transactiontype.equals("P")) {
				processing_code = "P";
			} else {
				processing_code = "0";
			}

			// ajout cavv (cavv+eci) xid dans la trame
			String champ_cavv = "";
			xid = threeDSServerTransID;
			if (cavv == null || eci == null) {
				champ_cavv = null;
				traces.writeInFileTransaction(folder, file, "cavv == null || eci == null");
			} else if (cavv != null && eci != null) {
				champ_cavv = cavv + eci;
				traces.writeInFileTransaction(folder, file, "cavv != null && eci != null");
				traces.writeInFileTransaction(folder, file, "champ_cavv : [" + champ_cavv + "]");
			} else {
				traces.writeInFileTransaction(folder, file, "champ_cavv = null");
				champ_cavv = null;
			}

			boolean cvv_present = check_cvv_presence(cvv);
			boolean is_reccuring = is_reccuring_check(recurring);
			boolean is_first_trs = true;

			String first_auth = "";
			long lrec_serie = 0;

			// controls
			traces.writeInFileTransaction(folder, file, "Switch processing start ...");

			String tlv = "";
			traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

			if (!cvv_present && !is_reccuring) {
				traces.writeInFileTransaction(folder, file,
						"payer 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (cvv must be present in normal transaction), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			// not reccuring , normal
			if (cvv_present && !is_reccuring) {
				traces.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
				try {

					/*
					 * old old sans cavv et xid
					 * tlv = new TLVEncoder().withField(Tags.tag0,
					 * mesg_type).withField(Tags.tag1, cardnumber) .withField(Tags.tag3,
					 * processing_code).withField(Tags.tag22, transaction_condition)
					 * .withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
					 * .withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
					 * .withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
					 * .withField(Tags.tag16, date).withField(Tags.tag17, heure)
					 * .withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" +
					 * merchantid) .withField(Tags.tag9, merchantid).withField(Tags.tag66,
					 * rrn).withField(Tags.tag67, cvv) .withField(Tags.tag11,
					 * merchant_name).withField(Tags.tag12, merchant_city) .withField(Tags.tag90,
					 * acqcode).encode();
					 */

					tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
							.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
							.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
							.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
							.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
							.withField(Tags.tag16, date).withField(Tags.tag17, heure)
							.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
							.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
							.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
							.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
							.withField(Tags.tag168, xid).encode();

					traces.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
					traces.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
					traces.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
					traces.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
					traces.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
					traces.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
					traces.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
					traces.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
					traces.writeInFileTransaction(folder, file, "tag18_request : [761454]");
					traces.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
					traces.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
					traces.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
					traces.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
					traces.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
					traces.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
					traces.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
					traces.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
					traces.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
					traces.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
					traces.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
					traces.writeInFileTransaction(folder, file, "tag167_request : [" + champ_cavv + "]");
					traces.writeInFileTransaction(folder, file, "tag168_request : [" + xid + "]");

				} catch (Exception err4) {
					traces.writeInFileTransaction(folder, file,
							"payer 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err4);
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Error during switch tlv buildup), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
					return page;
				}

				traces.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

				try {

					String tlv2 = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
							.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
							.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
							.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
							.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
							.withField(Tags.tag16, "****").withField(Tags.tag17, heure)
							.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
							.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, "***")
							.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
							.withField(Tags.tag90, acqcode).encode();

					traces.writeInFileTransaction(folder, file, "tlv2 : " + tlv2);

				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file, "Switch TLV Request ecncoding error " + e);
					e.printStackTrace();
				}
			}

			// reccuring
			if (is_reccuring) {
				traces.writeInFileTransaction(folder, file, "reccuring");
			}

			traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
//			SwitchTCPClient sw = SwitchTCPClient.getInstance();
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {

				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				traces.writeInFileTransaction(folder, file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					return "payer 500 Error Switch communication s_conn false" + "switch ip:[" + sw_s
							+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]";
				}

				if (s_conn) {
					traces.writeInFileTransaction(folder, file, "Switch Connected.");
					traces.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					traces.writeInFileTransaction(folder, file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				page = "result";
				return page;

			} catch (java.net.ConnectException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Switch  malfunction ConnectException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			catch (SocketTimeoutException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				traces.writeInFileTransaction(folder, file,
						"payer 500 Error Switch communication SocketTimeoutException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Error Switch communication SocketTimeoutException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			catch (IOException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				traces.writeInFileTransaction(folder, file, "payer 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Error Switch communication IOException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			catch (Exception e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Switch  malfunction Exception), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				traces.writeInFileTransaction(folder, file, "payer 500 Error Switch null response" + "switch ip:["
						+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Switch  malfunction resp null), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				traces.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				traces.writeInFileTransaction(folder, file, "payer 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
			}

			traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			traces.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

			TLVParser tlvp = null;

			String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null, tag9_resp = null,
					tag14_resp = null, tag15_resp = null, tag16_resp = null, tag17_resp = null, tag66_resp = null,
					tag18_resp = null, tag19_resp = null, tag23_resp = null, tag20_resp = null, tag21_resp = null,
					tag22_resp = null, tag80_resp = null, tag98_resp = null;

			if (switch_ko == 0) {
				try {
					tlvp = new TLVParser(resp);

					tag0_resp = tlvp.getTag(Tags.tag0);
					tag1_resp = tlvp.getTag(Tags.tag1);
					tag3_resp = tlvp.getTag(Tags.tag3);
					tag8_resp = tlvp.getTag(Tags.tag8);
					tag9_resp = tlvp.getTag(Tags.tag9);
					tag14_resp = tlvp.getTag(Tags.tag14);
					tag15_resp = tlvp.getTag(Tags.tag15);
					tag16_resp = tlvp.getTag(Tags.tag16);
					tag17_resp = tlvp.getTag(Tags.tag17);
					tag66_resp = tlvp.getTag(Tags.tag66); // f1
					tag18_resp = tlvp.getTag(Tags.tag18);
					tag19_resp = tlvp.getTag(Tags.tag19); // f2
					tag23_resp = tlvp.getTag(Tags.tag23);
					tag20_resp = tlvp.getTag(Tags.tag20);
					tag21_resp = tlvp.getTag(Tags.tag21);
					tag22_resp = tlvp.getTag(Tags.tag22);
					tag80_resp = tlvp.getTag(Tags.tag80);
					tag98_resp = tlvp.getTag(Tags.tag98);

				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file, "payer 500 Error during tlv Switch response parse"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// controle switch
				if (tag1_resp == null) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"payer 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				if (tag1_resp != null && tag1_resp.length() < 3) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"payer 500" + "Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag20_resp == null) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"payer 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}
			}
			traces.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
			traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			traces.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

			String tag20_resp_verified = "";
			String tag19_res_verified = "";
			String tag66_resp_verified = "";
			tag20_resp_verified = tag20_resp;
			tag19_res_verified = tag19_resp;
			tag66_resp_verified = tag66_resp;
			String s_status, pan_auto = "";

			// SWHistoAutoDto swhist = null;

			if (switch_ko == 1) {
				pan_auto = Util.formatagePan(cardnumber);
				traces.writeInFileTransaction(folder, file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + merchantid);
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;

			traces.writeInFileTransaction(folder, file, "Insert into Histogate...");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

				traces.writeInFileTransaction(folder, file, "get status ...");

				// s_status = histservice.getLib("RPC_LIBELLE", "CODEREPONSE", "RPC_CODE='" +
				// tag20_resp + "'");
				// if (s_status == null)
				s_status = "";
				traces.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

				traces.writeInFileTransaction(folder, file, "get max id ...");

				// Ihist_id = hist.getMAX_ID("HISTOAUTO_GATE", "HAT_ID");
				// Ihist_id = histoAutoGateService.getMAX_ID();
				// long currentid = Ihist_id.longValue() + 1;
				// hist.setId(currentid);

				traces.writeInFileTransaction(folder, file, "max id : [" + Ihist_id + "]");

				traces.writeInFileTransaction(folder, file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);
				traces.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

				traces.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");

				Date current_date_1 = getDateWithoutTime(curren_date_hist);
				hist.setHatDatdem(current_date_1);

				hist.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
				hist.setHatMontant(Double.parseDouble(amount));
				hist.setHatNumcmr(merchantid);
				hist.setHatCoderep(tag20_resp_verified);
				tag20_resp = tag20_resp_verified;
				hist.setHatDevise(currency);
				hist.setHatBqcmr(acqcode);
				hist.setHatPorteur(pan_auto);
				hist.setHatMtfref1(s_status);
				hist.setHatNomdeandeur(websiteid);
				hist.setHatNautemt(tag19_res_verified); // f2
				tag19_resp = tag19_res_verified;
				if (tag22_resp != null)
					hist.setHatProcode(tag22_resp.charAt(0));
				else
					hist.setHatProcode('6');
				hist.setHatExpdate(expirydate);
				hist.setHatRepondeur(tag21_resp);
				hist.setHatTypmsg("3");
				hist.setHatRrn(tag66_resp_verified); // f1
				tag66_resp_verified = tag66_resp;
				hist.setHatEtat('E');
				hist.setHatCodtpe(websiteid);
				hist.setHatMcc(merc_codeactivite);
				hist.setHatNumCommande(orderid);
				hist.setHatNumdem(new Long(numTransaction));

				if (check_cvv_presence(cvv)) {

					hist.setIs_cvv_verified("Y");
				} else {

					hist.setIs_cvv_verified("N");
				}

				hist.setIs_3ds("N");
				hist.setIs_addcard("N");
				// if (card_destination == 1)
				// hist.setIs_national("Y");
				// else
				// hist.setIs_national("N");
				hist.setIs_whitelist("N");
				hist.setIs_withsave("N");
				hist.setIs_tokenized("N");

				if (recurring.equalsIgnoreCase("Y"))
					hist.setIs_cof("Y");
				if (recurring.equalsIgnoreCase("N"))
					hist.setIs_cof("N");

				traces.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

				traces.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

				histoAutoGateService.save(hist);

			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file, "Error during  insert in histoautogate for given orderid");
				traces.writeInFileTransaction(folder, file,
						"payer 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + e);
			}

			traces.writeInFileTransaction(folder, file, "HistoAutoGate OK.");

			if (tag20_resp == null) {
				tag20_resp = "";
			}

			if (tag20_resp.equalsIgnoreCase("00"))

			{
				traces.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[00]");

				try {
					traces.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE ...");

					dmd.setEtat_demande("SW_PAYE");
					demandePaiementService.save(dmd);

				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file,
							"payer 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:[" + orderid
									+ "]" + e);
				}

				traces.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE OK");

				String capture_status = "N";
				int exp_flag = 0;

				if (capture.equalsIgnoreCase("Y")) {

					Date current_date = null;
					current_date = new Date();
					traces.writeInFileTransaction(folder, file, "Automatic capture start...");

					traces.writeInFileTransaction(folder, file, "Getting authnumber");

					String authnumber = hist.getHatNautemt();
					traces.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

					traces.writeInFileTransaction(folder, file, "Getting authnumber");
					TransactionDto trs_check = null;

					try {
						trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
					} catch (Exception ee) {

						traces.writeInFileTransaction(folder, file,
								"trs_check trs_check exception e : [" + ee.toString() + "]");
					}

					if (trs_check != null) {
						// do nothing
						traces.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
					} else {
						traces.writeInFileTransaction(folder, file, "inserting into telec start ...");
						try {
							// insert into telec

							TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

							long lidtelc = 0;

							if (n_tlc == null) {
								Integer idtelc = null;

								TelecollecteDto tlc = null;

								// insert into telec
								idtelc = telecollecteService.getMAX_ID();
								lidtelc = idtelc.longValue() + 1;
								tlc = new TelecollecteDto();
								tlc.setTlc_numtlcolcte(lidtelc);

								tlc.setTlc_numtpe(hist.getHatCodtpe());

								tlc.setTlc_datcrtfich(current_date);
								tlc.setTlc_nbrtrans(new Double(1));
								tlc.setTlc_gest("N");

								tlc.setTlc_datremise(current_date);
								tlc.setTlc_numremise(new Double(lidtelc));
								// tlc.setTlc_numfich(new Double(0));
								String tmpattern = "HH:mm";
								SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
								String stm = sftm.format(current_date);
								tlc.setTlc_heuremise(stm);

								tlc.setTlc_codbq(acqcode);
								tlc.setTlc_numcmr(merchantid);
								tlc.setTlc_numtpe(websiteid);
								telecollecteService.save(tlc);

							} else {
								traces.writeInFileTransaction(folder, file, "n_tlc !null ");

								lidtelc = n_tlc.getTlc_numtlcolcte();
								double nbr_trs = n_tlc.getTlc_nbrtrans();

								nbr_trs = nbr_trs + 1;

								n_tlc.setTlc_nbrtrans(nbr_trs);

								telecollecteService.save(n_tlc);
							}

							// insert into transaction
							TransactionDto trs = new TransactionDto();
							trs.setTrs_numcmr(merchantid);
							trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));

							String frmt_cardnumber = Util.formatagePan(cardnumber);
							trs.setTrs_codporteur(frmt_cardnumber);

							double dmnt = 0;

							dmnt = Double.parseDouble(amount);

							trs.setTrs_montant(dmnt);
							// trs.setTrs_dattrans(new Date());

							current_date = new Date();
							Date current_date_1 = getDateWithoutTime(current_date);
							trs.setTrs_dattrans(current_date_1);

							trs.setTrs_numaut(authnumber);
							trs.setTrs_etat("N");
							trs.setTrs_devise(hist.getHatDevise());
							trs.setTrs_certif("N");
							Integer idtrs = transactionService.getMAX_ID();
							long lidtrs = idtrs.longValue() + 1;
							trs.setTrs_id(lidtrs);
							trs.setTrs_commande(orderid);
							trs.setTrs_procod("0");
							trs.setTrs_groupe(websiteid);
							transactionService.save(trs);

							hist.setHatEtat('T');
							hist.setHatdatetlc(current_date);
							hist.setOperateurtlc("mxplusapi");
							histoAutoGateService.save(hist);

							capture_id = String.format("%040d",
									new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
							Date dt = new Date();
							String dtpattern = "yyyy-MM-dd";
							SimpleDateFormat sfdt = new SimpleDateFormat(dtpattern);
							String sdt = sfdt.format(dt);
							String tmpattern = "HH:mm:ss";
							SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
							String stm = sftm.format(dt);
							traces.writeInFileTransaction(folder, file, "inserting into telec ok");
							capture_status = "Y";

						} catch (Exception e) {
							exp_flag = 1;
							traces.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing " + e);
						}
					}
					if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
						capture_status.equalsIgnoreCase("N");

					traces.writeInFileTransaction(folder, file, "Automatic capture end.");
				}

			} else {

				traces.writeInFileTransaction(folder, file, "transaction declined !!! ");
				traces.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {
					traces.writeInFileTransaction(folder, file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtat_demande("SW_REJET");
					demandePaiementService.save(dmd);
					
				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file,
							"payer 500 Error during  DemandePaiement update RE for given orderid:[" + orderid + "]"
									+ e);
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Error during  DemandePaiement update SW_REJET), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
					return page;
				}
				traces.writeInFileTransaction(folder, file, "update Demandepaiement status to RE OK.");
			}

			traces.writeInFileTransaction(folder, file, "Generating paymentid...");

			String uuid_paymentid, paymentid = "";
			try {
				uuid_paymentid = String.format("%040d",
						new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
				paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file,
						"payer 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + e);
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Error during  paymentid generation), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			traces.writeInFileTransaction(folder, file, "Generating paymentid OK");
			traces.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

			// JSONObject jso = new JSONObject();

			traces.writeInFileTransaction(folder, file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDem_pan();
			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file,
						"payer 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Error during authdata preparation), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			// reccurent transaction processing

			// reccurent insert and update

			try {

				String data_noncrypt = "orderid=" + orderid + "&fname=" + fname + "&lname=" + lname + "&email=" + email
						+ "&amount=" + amount + "&coderep=" + coderep + "&authnumber=" + authnumber + "&cardnumber="
						+ Util.formatCard(cardnumber) + "&transactionid=" + transactionid + "&paymentid=" + paymentid;

				traces.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
				System.out.println("data_noncrypt : " + data_noncrypt);

				String plainTxtSignature = orderid + current_infoCommercant.getClePub();

				traces.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
				System.out.println("plainTxtSignature : " + plainTxtSignature);

				String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt, current_infoCommercant.getClePub(),
						plainTxtSignature, folder, file);

				traces.writeInFileTransaction(folder, file, "data encrypt : " + data);
				System.out.println("data encrypt : " + data);

				if (coderep.equals("00")) {
					traces.writeInFileTransaction(folder, file,
							"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					// response.sendRedirect(dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" +
					// merchantid);
					page = "index";
					return page;
				} else {
					traces.writeInFileTransaction(folder, file,
							"coderep !=00 => Redirect to failURL : " + dmd.getFailURL());
					System.out.println("coderep !=00 => Redirect to failURL : " + dmd.getFailURL());
					// response.sendRedirect(dmd.getFailURL() + "?data=" + data + "==&codecmr=" +
					// merchantid);
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Error during response Switch coderep !=00), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
					return page;
				}
			} catch (Exception jsouterr) {
				traces.writeInFileTransaction(folder, file,
						"payer 500 Error during jso out processing given authnumber:[" + authnumber + "]" + jsouterr);
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Error during jso out processing), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}

			// fin
			// *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// ********************* Cas chalenge responseMPI equal C ou D
			// *********************
			traces.writeInFileTransaction(folder, file, "****** Cas chalenge responseMPI equal C ou D ******");
			try {

				// insertion htmlCreq dans la demandePaiement
				// dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				//dmd.setCreq(
				//		"<form  action='https://acs2.sgmaroc.com:443/lacs2' method='post' enctype='application/x-www-form-urlencoded'><input type='hidden' name='creq' value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjBlYmU1ODEwLTlhMDMtNGYzZi05MDgzLTJlZWNhNjhiMjY2YSIsCiAgImFjc1RyYW5zSUQiOiAiMmM5MjAxNDgtNjhiOC00ZjA0LWJhODQtY2RiYTFlOTM5MDM3IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' /></form>");
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				dmd.setDem_xid(threeDSServerTransID);
				demandeDto = demandePaiementService.save(dmd);
				demandeDto = dmd;
				model.addAttribute("demandeDto", demandeDto);
				page = "chalenge";
				
				traces.writeInFileTransaction(folder, file, "set demandeDto model creq : " + demandeDto.getCreq());
				traces.writeInFileTransaction(folder, file, "return page : " + page);
				
				return page;
			} catch (Exception ex) {
				traces.writeInFileTransaction(folder, file, "payer 500 Error during jso out processing " + ex);
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (Error during jso out processing), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}
		} else if (reponseMPI.equals("E")) {
			// ********************* Cas responseMPI equal E
			// *********************
			traces.writeInFileTransaction(folder, file, "****** Cas responseMPI equal E ******");
			traces.writeInFileTransaction(folder, file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
			dmd.setEtat_demande("MPI_DS_ERR");
			demandePaiementService.save(dmd);
			demandeDto.setMsgRefus(
					"La transaction en cours n’a pas abouti (Error 3DSS), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
			return page;
		} else {
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				traces.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("MPI_CMR_INEX");
				demandePaiementService.save(dmd);
				// return "COMMERCANT NON PARAMETRE";
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			case "BIN NON PARAMETRE":
				traces.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				// return " BIN NON PARAMETREE";
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			case "DIRECTORY SERVER":
				traces.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				// return "MPI_DS_ERR";
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (MPI_DS_ERR), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			case "CARTE ERRONEE":
				traces.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				// return "CARTE ERRONEE";
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			case "CARTE NON ENROLEE":
				traces.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				// return "CARTE NON ENROLLE";
				demandeDto.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
				return page;
			}
		}

		System.out.println("demandeDto htmlCreq : " + demandeDto.getCreq());
		System.out.println("return page : " + page);

		return page;
	}

	@RequestMapping(value = "/chalenge", method = RequestMethod.GET)
	public String chlenge(Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
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
		System.out.println("return to chalenge.html");

		return "chalenge";
	}

	@RequestMapping(value = "/napspayment/error/token/{token}", method = RequestMethod.GET)
	public String error(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start error ************** ");
		System.out.println("*********** Start error ************** ");

		String page = "error";

		traces.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto current_dem = demandePaiementService.findByTokencommande(token);
		String msgRefus = "Une erreur est survenue, merci de réessayer plus tard";

		if (current_dem != null) {
			traces.writeInFileTransaction(folder, file, "current_dem is exist OK");
			System.out.println("current_dem is exist OK");
			if (current_dem.getEtat_demande().equals("SW_PAYE") || current_dem.getEtat_demande().equals("PAYE")) {
				msgRefus = "La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else if (current_dem.getEtat_demande().equals("SW_REJET")) {
				msgRefus = "La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer .";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else {
				msgRefus = "La transaction en cours n’a pas abouti (Problème authentification 3DSecure), votre compte ne sera pas débité, merci de contacter votre banque .";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			}
		} else {
			DemandePaiementDto demande = new DemandePaiementDto();
			msgRefus = "Votre commande est introuvable ";
			demande.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demande);
			traces.writeInFileTransaction(folder, file, "current_dem not found ");
			System.out.println("current_dem null ");
			page = "error";
		}
		
		traces.writeInFileTransaction(folder, file, "*********** Fin error ************** ");
		System.out.println("*********** Fin error ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/index2", method = RequestMethod.GET)
	public String index2() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "return to index2.html");
		System.out.println("return to index2.html");

		return "index2";
	}

	@RequestMapping(value = "/napspayment/result", method = RequestMethod.GET)
	public String result() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		traces.creatFileTransaction(file);
		System.out.println("return to result.html");

		return "result";
	}

	@PostMapping(path = "/napspayment/linkpayment-xml", consumes = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public String userInformation(@RequestBody UserDto user) {

		System.out.println(user.getName() + " " + user.getEmail());

		return "User information saved successfully ::.";
	}
	
	private List<Integer> generateYearList(int startYear, int endYear) {
		List<Integer> years = new ArrayList<>();
		for (int year = startYear; year <= endYear; year++) {
			years.add(year);
		}
		return years;
	}

	private List<String> convertMonthListToStringList(List<Month> monthList) {
		List<String> monthNames = new ArrayList<>();
		for (Month month : monthList) {
			monthNames.add(month.toString()); // Convert Month to its string representation
		}
		return monthNames;
	}

	private List<MonthDto> convertStringAGListToFR(List<String> monthList) {
		List<String> monthNames = new ArrayList<>();
		List<MonthDto> monthNamesValues = new ArrayList<>();

		for (String month : monthList) {
			MonthDto exp = new MonthDto();
			if (month.equals("JANUARY")) {
				month = "Janvier";
				exp.setMonth(month);
				exp.setValue("01");
			} else if (month.toString().equals("FEBRUARY")) {
				month = "Février";
				exp.setMonth(month);
				exp.setValue("02");
			} else if (month.toString().equals("MARCH")) {
				month = "Mars";
				exp.setMonth(month);
				exp.setValue("03");
			} else if (month.toString().equals("APRIL")) {
				month = "Avril";
				exp.setMonth(month);
				exp.setValue("04");
			} else if (month.toString().equals("MAY")) {
				month = "Mai";
				exp.setMonth(month);
				exp.setValue("05");
			} else if (month.toString().equals("JUNE")) {
				month = "Juin";
				exp.setMonth(month);
				exp.setValue("06");
			} else if (month.toString().equals("JULY")) {
				month = "Juillet";
				exp.setMonth(month);
				exp.setValue("07");
			} else if (month.toString().equals("AUGUST")) {
				month = "Aout";
				exp.setMonth(month);
				exp.setValue("08");
			} else if (month.toString().equals("SEPTEMBER")) {
				month = "Septembre";
				exp.setMonth(month);
				exp.setValue("09");
			} else if (month.toString().equals("OCTOBER")) {
				month = "Octobre";
				exp.setMonth(month);
				exp.setValue("10");
			} else if (month.toString().equals("NOVEMBER")) {
				month = "Novembre";
				exp.setMonth(month);
				exp.setValue("11");
			} else if (month.toString().equals("DECEMBER")) {
				month = "Décembre";
				exp.setMonth(month);
				exp.setValue("12");
			}
			// Convert Month to its string representation
			monthNames.add(month.toString());
			monthNamesValues.add(exp);

		}
		return monthNamesValues;
	}

	private boolean is_reccuring_check(String recurring) {
		if (recurring.equalsIgnoreCase("Y"))
			return true;
		else
			return false;
	}

	private boolean check_reccuring_flag(String recurring) {
		if (recurring == null)
			return false;
		if (recurring.length() < 1)
			return false;
		if (recurring.length() > 1)
			return false;
		if (recurring.equalsIgnoreCase("Y") || recurring.equalsIgnoreCase("N"))
			return true;
		return false;
	}

	private boolean check_cvv_presence(String cvv) {
		if (cvv == null)
			return false;
		if (cvv.length() < 3)
			return false;
		if (cvv.length() > 3)
			return false;
		return true;
	}

	@SuppressWarnings("deprecation")
	private Date getDateWithoutTime(Date d) {

		if (d == null)
			d = new Date();
		Date d_notime = null;
		try {

			d_notime = new Date(d.getYear(), d.getMonth(), d.getDate());

			d_notime.setHours(0);
			d_notime.setMinutes(0);
			d_notime.setSeconds(0);

		} catch (Exception e) {
			return d; // leave it as it is if not null
		}
		return d_notime;

	}


}
