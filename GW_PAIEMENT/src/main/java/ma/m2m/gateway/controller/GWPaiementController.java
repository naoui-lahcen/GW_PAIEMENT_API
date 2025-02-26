package ma.m2m.gateway.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.dto.ArticleDGIDto;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.Cartes;
import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.FactureLDDto;
import ma.m2m.gateway.dto.MonthDto;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.dto.ResponseDto;
import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.lydec.Impaye;
import ma.m2m.gateway.lydec.Portefeuille;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.service.ArticleDGIService;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.FactureLDService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
import ma.m2m.gateway.utils.Objects;
import ma.m2m.gateway.utils.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Controller
public class GWPaiementController {
	
	private static final Logger logger = LogManager.getLogger(GWPaiementController.class);

	@Value("${key.SECRET}")
	private String secret;

	@Value("${key.USER_TOKEN}")
	private String usernameToken;

	@Value("${key.SWITCH_URL}")
	private String ipSwitch;

	@Value("${key.SWITCH_PORT}")
	private String portSwitch;

	@Value("${key.JWT_TOKEN_VALIDITY}")
	private long jwtTokenValidity;

	@Value("${key.URL_WSDL_LYDEC}")
	private String urlWsdlLydec;

	@Value("${key.LYDEC_PREPROD}")
	private String lydecPreprod;

	@Value("${key.LYDEC_PROD}")
	private String lydecProd;
	
	@Value("${key.DGI_PREPROD}")
	private String dgiPreprod;

	@Value("${key.DGI_PROD}")
	private String dgiProd;
	
	@Value("${key.ENVIRONEMENT}")
	private String environement;
	
	@Value("${key.TIMEOUT}")
	private int timeout;
	
	//@Autowired
	private final DemandePaiementService demandePaiementService;

	//@Autowired
	private final AutorisationService autorisationService;

	//@Autowired
	private final CommercantService commercantService;

	//@Autowired
	private final InfoCommercantService infoCommercantService;

	//@Autowired
	private final HistoAutoGateService histoAutoGateService;

	//@Autowired
	private final CardtokenService cardtokenService;

	//@Autowired
	private final CodeReponseService codeReponseService;

	//@Autowired
	private final FactureLDService factureLDService;

	//@Autowired
	private final ArticleDGIService articleDGIService;

	private LocalDateTime date;
	private String folder;
	private String file;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;
	
	public static final String DF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	DateFormat dateFormat = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
	DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

	// private static final QName serviceName = new QName("http://service.lydec.com", "GererEncaissementService");

	public GWPaiementController(DemandePaiementService demandePaiementService, AutorisationService autorisationService,
			HistoAutoGateService histoAutoGateService, CommercantService commercantService, 
			InfoCommercantService infoCommercantService,
			CardtokenService cardtokenService, CodeReponseService codeReponseService,
			FactureLDService factureLDService, ArticleDGIService articleDGIService) {
		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.demandePaiementService = demandePaiementService;
		this.autorisationService = autorisationService;
		this.histoAutoGateService = histoAutoGateService;
		this.commercantService = commercantService;
		this.infoCommercantService = infoCommercantService;
		this.cardtokenService = cardtokenService;
		this.codeReponseService = codeReponseService;
		this.factureLDService = factureLDService;
		this.articleDGIService = articleDGIService;
	}

	public static Portefeuille[] preparerTabEcritureLydecListe(List<Impaye> facListe) {
		Portefeuille[] tabEcr = new Portefeuille[facListe.size()];
		int i = 0;
		for (Impaye fac : facListe) {
			Portefeuille ecr = new Portefeuille();
			ecr.setFac_Num(fac.getNumeroFacture());
			ecr.setLigne(fac.getNumeroLigne());
			tabEcr[i] = ecr;

			i++;

		}

		return tabEcr;
	}

	//@RequestMapping(path = "/")
	@GetMapping("/")
	@ResponseBody
	public String home() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String filee = "GW_" + randomWithSplittableRandom;
		Util.creatFileTransaction(filee);
		autorisationService.logMessage(filee, "*********** Start home() ************** ");
		logger.info("*********** Start home() ************** ");

		String msg = "Bienvenue dans la plateforme de paiement NAPS !!!";

		autorisationService.logMessage(filee, "*********** End home () ************** ");
		logger.info("*********** End home () ************** ");
        
		return msg;
	}

	@SuppressWarnings("all")
	public JSONObject verifieToken(String securtoken24) {
		// TODO: Traces traces = new Traces();
		JSONObject jso = new JSONObject();

		if (!securtoken24.equals("")) {
			JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
			String token = securtoken24;
			try {
				String userFromToken = jwtTokenUtil.getUsernameFromToken(token, secret);
				Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token, secret);
				Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token, secret);

				autorisationService.logMessage(file, "userFromToken generated : " + userFromToken);
				String dateSysStr = dateFormat.format(new Date());
				autorisationService.logMessage(file, "dateSysStr : " + dateSysStr);
				autorisationService.logMessage(file, "dateExpiration : " + dateExpiration);
				String dateExpirationStr = dateFormat.format(dateExpiration);
				autorisationService.logMessage(file, "dateExpirationStr : " + dateExpirationStr);
				String condition = isTokenExpired == false ? "NO" : "YES";
				autorisationService.logMessage(file, "token is expired : " + condition);
				if (condition.equalsIgnoreCase("YES")) {
					autorisationService.logMessage(file, "Error 500 securtoken24 is expired");

					// TODO: Transaction info
					jso.put("statuscode", "17");
					jso.put("status", "Error 500 securtoken24 is expired");

					return jso;
				} else {
					jso.put("statuscode", "00");
				}
			} catch (Exception ex) {
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 securtoken24 " + ex.getMessage());
				logger.info(jso.get("status"));

				return jso;
			}
		}
		return jso;
	}

	@RequestMapping(value = "/napspayment/{numCompte}", method = RequestMethod.GET)
	@ResponseBody
	@SuppressWarnings("all")
	public String getRibByNumCompte(@PathVariable(value = "numCompte") String numCompte) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "RIB_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start getRibByNumCompte ***********");
		logger.info("*********** Start getRibByNumCompte ***********");
		
		String rib = constructRIB(numCompte);
		autorisationService.logMessage(file, "rib : " + rib);
		logger.info("rib : " + rib);
		
		autorisationService.logMessage(file, "*********** End getRibByNumCompte ***********");
		logger.info("*********** End getRibByNumCompte ***********");
		 
		return rib;
	}
	
	//@PostMapping("/napspayment/uploadExcel")
	@RequestMapping(value = "/napspayment/uploadExcel", method = RequestMethod.POST)
	@ResponseBody
	@SuppressWarnings("all")
    public String handleFileUpload(HttpServletResponse response, @RequestParam("file") MultipartFile file) throws FileNotFoundException, IOException {
        if (file.isEmpty()) {
            return "Le fichier est vide";
        }
    	List<String> updatedDataList = new ArrayList<>();
        try(Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            boolean firstRow = true; 
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
                StringBuilder contentBuilder = new StringBuilder();

                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    contentBuilder.append(cell.toString()).append("\t");
                }
                String NUMCOmpte = row.getCell(4).toString().trim();
                String RIB = constructRIB(NUMCOmpte);
                contentBuilder.append(RIB);
                updatedDataList.add(contentBuilder.toString());
            }
        } catch (Exception ex) {        	
        	logger.error("Exception Erreur lors de la lecture du fichier Excel : ", ex);
        }
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Rib_Etudiants_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		try {
		    updatedDataList.removeIf(String::isEmpty);
			GenerateExcel excelExporter = new GenerateExcel(updatedDataList, "xx");
			excelExporter.exportRIB(response);
		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
		return "Fichier Excel traité généré avec succès ";
    }

    private String constructRIB(String numCompte) {
    	String codeNaps = "842";
    	String codeVille = "780";
    	String sepEspace = "    ";
    	String rib1 = codeNaps + codeVille + numCompte;
		BigInteger cle = calculCle(rib1);

		String cleString = cle.toString();
		if(cleString.length() == 1) {
			cleString = "0"+cleString;
		}

        return codeNaps + sepEspace + codeVille
                + sepEspace + numCompte + sepEspace + cleString;
    }
    
	public BigInteger calculCle(String rb) {
		BigInteger cle = BigInteger.ZERO;
		try {
			BigInteger rib = new BigInteger(rb);
			BigInteger v100 = BigInteger.valueOf(100);
			BigInteger v97 = BigInteger.valueOf(97);
			BigInteger rib100 = rib.multiply(v100);
			BigInteger mod = rib100.mod(v97);
			cle = v97.subtract(mod);
		} catch(Exception ex) {
			logger.error("Exception : ", ex);
		}
		return cle;
	}
	
	//@RequestMapping("/napspayment/index")
	@GetMapping("/napspayment/index")
	//@ResponseBody
	@SuppressWarnings("all")
	public String index() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "return to index.html");
		logger.info("return to index.html");
		String fname ="Jose Ignacio FERNANDEZ GO";
		String email = "ignacio.fernandez@jarama.org";
		String data_noncrypt = "id_commande=999917393603091707&nomprenom="+ fname+"&email="+ email+ "&montant=11860.0&frais=&repauto=94&numAuto=395876&numCarte=427305******5200&typecarte=1&numTrans=259904";
		String cle = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnSJF0sXyt2AEe0ZeK20rFJTEUkfuGGBRaj5fnNKNi1rQPvy3sdExybXr61s3zSKbDdVTSV7Uv23qrL6hpOXZGuxQrzmrVXrq9S8tqXGjiLlMntMLtNGupLDTeuSeMmDVxl47mlSTOzEWHdWH6He9bBh8YpYU2mNenueIBsnXzoY1yBf0MpTmi0xJG3qTFNNuJHFAywhH8haogfsM9s71Dj8mF+0Vu6LNQw/a3z0n0jFNz/73cVuAzDPe+1c38aZP+BvFRM1FrXrdc+5NdAC+Mlj9aSX5eZWh1hkxhf0Tyz/SQwNDzYc/AX6tueIh95ElXqtiQwQq69M/QJH2Ej/LQQIDAQAB";
		String plainTxtSignature = "999917393603091707MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnSJF0sXyt2AEe0ZeK20rFJTEUkfuGGBRaj5fnNKNi1rQPvy3sdExybXr61s3zSKbDdVTSV7Uv23qrL6hpOXZGuxQrzmrVXrq9S8tqXGjiLlMntMLtNGupLDTeuSeMmDVxl47mlSTOzEWHdWH6He9bBh8YpYU2mNenueIBsnXzoY1yBf0MpTmi0xJG3qTFNNuJHFAywhH8haogfsM9s71Dj8mF+0Vu6LNQw/a3z0n0jFNz/73cVuAzDPe+1c38aZP+BvFRM1FrXrdc+5NdAC+Mlj9aSX5eZWh1hkxhf0Tyz/SQwNDzYc/AX6tueIh95ElXqtiQwQq69M/QJH2Ej/LQQIDAQAB";
		try {
			if (data_noncrypt.length() > 200) {
				// TODO : First, try reducing the length by adjusting the fname
				if (!fname.isEmpty()) {
					fname = fname.length() > 10 ? fname.substring(0, 10) : fname;
				}

				// TODO : Rebuild the data_noncrypt string with the updated fname
				data_noncrypt = "id_commande=999917393603091707&nomprenom="+ fname+"&email="+ email+ "&montant=11860.0&frais=&repauto=94&numAuto=395876&numCarte=427305******5200&typecarte=1&numTrans=259904";
				System.out.println((data_noncrypt));
				// TODO : If the length is still greater than 200, reduce the length of email
				if (data_noncrypt.length() > 200 && !email.isEmpty()) {
					email = email.length() > 10 ? email.substring(0, 10) : email;
				}

				// TODO : Rebuild again with the updated email
				data_noncrypt = "id_commande=999917393603091707&nomprenom="+ fname+"&email="+ email+ "&montant=11860.0&frais=&repauto=94&numAuto=395876&numCarte=427305******5200&typecarte=1&numTrans=259904";
				System.out.println((data_noncrypt));
			}
			String data_crypt = RSACrypto.encryptByPublicKeyWithMD5Sign(
					data_noncrypt, cle,
					plainTxtSignature, folder, file);
			System.out.println((data_crypt));
		} catch (Exception e) {
			e.printStackTrace();
		}


		return "index";
	}

	@RequestMapping(value = "/napspayment/authorization/token/{token}", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String showPagePayment(@PathVariable(value = "token") String token, Model model, HttpSession session) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAGE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start affichage page ***********");

		autorisationService.logMessage(file, "findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspayment";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				autorisationService.logMessage(file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

				// TODO: get list of years + 10
				int currentYear = Year.now().getValue();
				List<Integer> years = generateYearList(currentYear, currentYear + 10);

				demandeDto.setYears(years);

				// TODO: get list of months
				List<Month> months = Arrays.asList(Month.values());
				List<String> monthNames = convertMonthListToStringList(months);
				List<MonthDto> monthValues = convertStringAGListToFR(monthNames);

				demandeDto.setMonths(monthValues);
				
				autorisationService.processPaymentPageData(demandeDto, page, file);

				Util.formatAmount(demandeDto);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtatDemande().equals("SW_PAYE") || demandeDto.getEtatDemande().equals("PAYE")) {
					autorisationService.logMessage(file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtatDemande().equals("SW_REJET")) {
					autorisationService.logMessage(file, "Transaction rejetée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
				} else {
					autorisationService.processInfosMerchant(demandeDto, folder, file);
				}
			} else {
				autorisationService.logMessage(file, "demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			autorisationService.logMessage(file,
					"showPagePayment 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token + "]"
							+ Util.formatException(e));

			autorisationService.logMessage(file, "showPagePayment 500 exception" + Util.formatException(e));
			logger.error("Exception : " , e);
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}
		
		// TODO: gestion expiration de la session on stoque la date en millisecond
	    session.setAttribute("paymentStartTime", System.currentTimeMillis());
	    autorisationService.logMessage(file, "paymentStartTime : " + System.currentTimeMillis());
	    demandeDto.setTimeoutURL(String.valueOf(System.currentTimeMillis()));
	    
		if (page.equals("napspayment")) {
			if(demandeDto.getEtatDemande().equals("INIT")) {
				demandeDto.setEtatDemande("P_CHRG_OK");
				demandePaiementService.save(demandeDto);
				autorisationService.logMessage(file, "update Demandepaiement status to P_CHRG_OK");
			}
			if (demandeDto.getComid().equals(lydecPreprod) || demandeDto.getComid().equals(lydecProd)) {
				autorisationService.logMessage(file, "Si le commercant est LYDEC : " + demandeDto.getComid());
				List<FactureLDDto> listFactureLD = new ArrayList<>();
				listFactureLD = factureLDService.findFactureByIddemande(demandeDto.getIddemande());
				if (listFactureLD != null && listFactureLD.size() > 0) {
					autorisationService.logMessage(file, "listFactureLD : " + listFactureLD.size());
					demandeDto.setFactures(listFactureLD);
				} else {
					autorisationService.logMessage(file, "listFactureLD vide ");
					demandeDto.setFactures(null);
				}
				model.addAttribute("demandeDto", demandeDto);
				page = "napspaymentlydec";
			}
			if (demandeDto.getComid().equals(dgiPreprod) || demandeDto.getComid().equals(dgiProd)) {
				autorisationService.logMessage(file, "Si le commercant est DGI : " + demandeDto.getComid());
				List<ArticleDGIDto> articles = new ArrayList<>();
				articles = articleDGIService.findArticleByIddemande(demandeDto.getIddemande());
				if (articles != null && articles.size() > 0) {
					autorisationService.logMessage(file, "articles : " + articles.size());
					demandeDto.setArticles(articles);
				} else {
					autorisationService.logMessage(file, "articles vide ");
					demandeDto.setArticles(null);
				}
				model.addAttribute("demandeDto", demandeDto);
				page = "napspaymentdgi";
			}
			
		}

		autorisationService.logMessage(file, "*********** End affichage page ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/authorization/lydec/token/{token}", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String showPagePaymentLydec(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAGE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start affichage page ***********");

		autorisationService.logMessage(file, "findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspaymentlydec";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				autorisationService.logMessage(file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

				// TODO: get list of years + 10
				int currentYear = Year.now().getValue();
				List<Integer> years = generateYearList(currentYear, currentYear + 10);

				demandeDto.setYears(years);

				// TODO: get list of months
				List<Month> months = Arrays.asList(Month.values());
				List<String> monthNames = convertMonthListToStringList(months);
				List<MonthDto> monthValues = convertStringAGListToFR(monthNames);

				demandeDto.setMonths(monthValues);
				// TODO: if cmr don't accept transaction cof demandeDto.getIs_cof() = N don't show
				// TODO: carte
				if (demandeDto.getIsCof() == null || demandeDto.getIsCof().equals("N")) {
					demandeDto.setDemPan("");
					demandeDto.setDemCvv("");
				}
				Util.formatAmount(demandeDto);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtatDemande().equals("SW_PAYE") || demandeDto.getEtatDemande().equals("PAYE")) {
					autorisationService.logMessage(file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtatDemande().equals("SW_REJET")) {
					autorisationService.logMessage(file, "Transaction rejetée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
				} else {
					autorisationService.processInfosMerchant(demandeDto, folder, file);
				}
			} else {
				autorisationService.logMessage(file, "demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			autorisationService.logMessage(file,
					"showPagePaymentLydec 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token
							+ "]" + Util.formatException(e));

			autorisationService.logMessage(file, "showPagePaymentLdec 500 exception" + Util.formatException(e));
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}

		if (page.equals("napspaymentlydec")) {
			demandeDto.setEtatDemande("P_CHRG_OK");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file, "update Demandepaiement status to P_CHRG_OK");

			List<FactureLDDto> listFactureLD = new ArrayList<>();
			listFactureLD = factureLDService.findFactureByIddemande(73);
			if (listFactureLD != null && listFactureLD.size() > 0) {
				demandeDto.setFactures(listFactureLD);
			} else {
				demandeDto.setFactures(null);
			}
			model.addAttribute("demandeDto", demandeDto);
		}

		autorisationService.logMessage(file, "*********** End affichage page ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/authorization/dgi/token/{token}", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String showPagePaymentDGI(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAGE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start affichage page ***********");

		autorisationService.logMessage(file, "findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspaymentdgi";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				logger.info("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/"
						+ demandeDto.getCommande());
				autorisationService.logMessage(file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

				// TODO: get list of years + 10
				int currentYear = Year.now().getValue();
				List<Integer> years = generateYearList(currentYear, currentYear + 10);

				demandeDto.setYears(years);

				// TODO: get list of months
				List<Month> months = Arrays.asList(Month.values());
				List<String> monthNames = convertMonthListToStringList(months);
				List<MonthDto> monthValues = convertStringAGListToFR(monthNames);

				demandeDto.setMonths(monthValues);
				// TODO: if cmr don't accept transaction cof demandeDto.getIsCof() = N don't show
				// TODO: carte
				if (demandeDto.getIsCof() == null || demandeDto.getIsCof().equals("N")) {
					demandeDto.setDemPan("");
					demandeDto.setDemCvv("");
				}

				Util.formatAmount(demandeDto);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtatDemande().equals("SW_PAYE") || demandeDto.getEtatDemande().equals("PAYE")) {
					autorisationService.logMessage(file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtatDemande().equals("SW_REJET")) {
					autorisationService.logMessage(file, "Transaction rejetée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
				} else {
					autorisationService.processInfosMerchant(demandeDto, folder, file);
				}
			} else {
				autorisationService.logMessage(file, "demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			autorisationService.logMessage(file,
					"showPagePaymentDGILdec 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token
							+ "]" + Util.formatException(e));

			autorisationService.logMessage(file, "showPagePaymentDGILdec 500 exception" + Util.formatException(e));
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}

		if (page.equals("napspaymentdgi")) {
			demandeDto.setEtatDemande("P_CHRG_OK");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file, "update Demandepaiement status to P_CHRG_OK");

			List<ArticleDGIDto> articles = new ArrayList<>();
			articles = articleDGIService.findArticleByIddemande(demandeDto.getIddemande());
			if (articles != null && articles.size() > 0) {
				logger.info("articles : " + articles.size());
				demandeDto.setArticles(articles);
			} else {
				demandeDto.setArticles(null);
			}
			model.addAttribute("demandeDto", demandeDto);
		}

		autorisationService.logMessage(file, "*********** End affichage page ************** ");

		return page;
	}
	
	@PostMapping("/payer")
	@SuppressWarnings("all")
	public String payer(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto, HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAYE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start payer () ************** ");
		logger.info("*********** Start payer () ************** ");

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", successURL, failURL,
				transactiontype, idclient;

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		Objects.copyProperties(demandeDto, dto);
		logger.info("Commande : " + demandeDto.getCommande());
		autorisationService.logMessage(file, "Commande : " + dto.getCommande());
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		DemandePaiementDto dmd = new DemandePaiementDto();

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;
		boolean flagNvCarte, flagSaveCarte;

		String page = "chalenge";
	    
		try {
			autorisationService.logMessage(file, "" + demandeDto.toString());
			// TODO: Transaction info
			orderid = demandeDto.getCommande() == null ? "" : demandeDto.getCommande();
			if (demandeDto.getMontant() == null) {
				demandeDto.setMontant(0.00);
			}
			amount = String.valueOf(demandeDto.getMontant());
			capture = "";
			currency = "504";
			recurring = "N";
			promoCode = "";
			transactionid = "";
			transactiontype = "0"; // TODO: 0 payment , P preauto

			// TODO: Merchnat info
			merchantid = demandeDto.getComid() == null ? "" : demandeDto.getComid();
			merchantname = "";
			websiteName = "";
			websiteid = "";
			cardnumber = "";
			expirydate = "";
			callbackUrl = demandeDto.getCallbackURL() == null ? "" : demandeDto.getCallbackURL();
			successURL = demandeDto.getSuccessURL() == null ? "" : demandeDto.getSuccessURL();
			failURL = demandeDto.getFailURL() == null ? "" : demandeDto.getFailURL();

			// TODO: Card info
			// TODO: if transaction not cof
			if (demandeDto.getDemPan() != null && !demandeDto.getDemPan().equals("")) {
				cardnumber = demandeDto.getDemPan();
				Set<String> uniqueCards = new LinkedHashSet<>(Arrays.asList(cardnumber.split(",")));
				cardnumber = String.join(",", uniqueCards);
				demandeDto.setDemPan(cardnumber);
				expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois().substring(0, 2));
			}
			// TODO: if transaction cof
			if (demandeDto.getInfoCarte() != null && !demandeDto.isFlagNvCarte()
					&& (demandeDto.getDemPan() == null || demandeDto.getDemPan().equals(""))) {
				//String infoCard = demandeDto.getInfoCarte().substring(8, demandeDto.getInfoCarte().length());
				String infoCard = demandeDto.getInfoCarte().replaceAll("Cartes\\(|\\)", "");
				Cartes carteFormated = fromString(infoCard);
				demandeDto.setCarte(carteFormated);
				cardnumber = demandeDto.getCarte().getCarte();
				String annee = String.valueOf(demandeDto.getCarte().getYear());
				expirydate = annee.substring(2, 4).concat(demandeDto.getCarte().getMoisValue());
			}
			if (demandeDto.getInfoCarte() != null && demandeDto.getDemPan().equals("")) {
				if(!demandeDto.getAnnee().equals("") && !demandeDto.getMois().equals("")) {
					expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois().substring(0, 2));
				}
			}
			flagNvCarte = demandeDto.isFlagNvCarte();
			flagSaveCarte = demandeDto.isFlagSaveCarte();
			if (cardnumber.contains(",")) {
				cardnumber = cardnumber.replace(",", "");
			}
			// TODO: cardnumber = demandeDto.getDemPan();
			token = "";
			// TODO: expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois());
			holdername = "";
			cvv = demandeDto.getDemCvv() == null ? "" : demandeDto.getDemCvv();

			// TODO: Client info
			fname = demandeDto.getPrenom() == null ? "" : demandeDto.getPrenom();
			lname = demandeDto.getNom() == null ? "" : demandeDto.getNom();
			email = demandeDto.getEmail() == null ? "" : demandeDto.getEmail();
			country = demandeDto.getCountry() == null ? "" : demandeDto.getCountry();
			phone = demandeDto.getTel() == null ? "" : demandeDto.getTel();
			city = demandeDto.getCity() == null ? "" : demandeDto.getCity();
			state = demandeDto.getState() == null ? "" : demandeDto.getState();
			zipcode = demandeDto.getPostcode() == null ? "" : demandeDto.getPostcode();
			address = demandeDto.getAddress() == null ? "" : demandeDto.getAddress();

		} catch (Exception jerr) {
			autorisationService.logMessage(file, "payer 500 malformed json expression" + Util.formatException(jerr));
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
		}

		if (current_merchant == null) {
			return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
		}

		if (current_merchant.getCmrCodactivite() == null) {
			return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
		}

		if (current_merchant.getCmrCodbqe() == null) {
			return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
		}

		InfoCommercantDto current_infoCommercant = null;

		try {
			current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
		} catch (Exception e) {
			return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, websiteid, demandeDtoMsg, model, page, false);
		}

		if (current_infoCommercant == null) {
			return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, websiteid, demandeDtoMsg, model, page, false);
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		page = autorisationService.handleCardValidationError(i_card_valid, cardnumber, orderid, merchantid, file,
				demandeDtoMsg, model, page);
		if ("result".equals(page)) {
			return page;
		}

		int i_card_type = Util.getCardIss(cardnumber);

		try {
			DemandePaiementDto dmdToEdit = demandePaiementService.findByIdDemande(demandeDto.getIddemande());

			dmdToEdit.setDemPan(cardnumber);
			dmdToEdit.setDemCvv(cvv);
			dmdToEdit.setTypeCarte(i_card_type + "");
			dmdToEdit.setTransactiontype(transactiontype);
			int nbr_tv = dmdToEdit.getNbreTenta() + 1;
			dmdToEdit.setNbreTenta(nbr_tv);

			formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			dmdToEdit.setDemDateTime(dateFormat.format(new Date()));
			dmdToEdit.setEtatDemande("START_PAYMENT");

			demandeDto = demandePaiementService.save(dmdToEdit);
			demandeDto.setExpery(expirydate);
			demandeDto.setFlagNvCarte(flagNvCarte);
			demandeDto.setFlagSaveCarte(flagSaveCarte);
			idclient = demandeDto.getIdClient();
			if (idclient == null) {
				idclient = "";
			}

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"payer 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + Util.formatException(err1));
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		page = autorisationService.handleSessionTimeout(session, file, timeout, demandeDto, demandeDtoMsg, model);

		if ("timeout".equals(page)) {
			return page;
		}
		
		if (demandeDto.getEtatDemande().equals("SW_PAYE") || demandeDto.getEtatDemande().equals("PAYE")) {
			demandeDto.setDemCvv("");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file, "Opération déjà effectuée");
			demandeDto.setMsgRefus(
					"La transaction en cours est déjà effectuée, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDto);
			page = "operationEffectue";
			return page;
		}

		// TODO: for test control risk
		// TODO: refactoring code 2024-03-20
		autorisationService.logMessage(file, "Debut controlleRisk");
		try {
			String msg = autorisationService.controlleRisk(demandeDto, folder, file);
			if (!msg.equalsIgnoreCase("OK")) {
				demandeDto.setDemCvv("");
				demandeDto.setEtatDemande("REJET_RISK_CTRL");
				demandePaiementService.save(demandeDto);
				autorisationService.logMessage(file, msg);
				demandeDto = new DemandePaiementDto();
				demandeDtoMsg.setMsgRefus(msg);
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}
		} catch (Exception e) {
			demandeDto.setDemCvv("");
			demandeDto.setEtatDemande("REJET_RISK_CTRL");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file,
					"payer 500 ControlRiskCmr misconfigured in DB or not existing merchantid:[" + demandeDto.getComid()
							+ Util.formatException(e));
			demandeDto = new DemandePaiementDto();
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		autorisationService.logMessage(file, "Fin controlleRisk");
		
		// TODO: saving card if flagSaveCarte true
		if (demandeDto.isFlagSaveCarte()) {
			try {
				List<CardtokenDto> checkCardNumber = cardtokenService.findByIdMerchantClientAndCardNumber(idclient,
						cardnumber);
				
				CardtokenDto cardtokenDto = new CardtokenDto();
				Calendar dateCalendar = Calendar.getInstance();
				Date dateToken = dateCalendar.getTime();

				autorisationService.logMessage(file, "cardtokenDto expirydate input : " + expirydate);
				String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
				// TODO: get year from date
				String xx = anne.substring(0, 2) + expirydate.substring(0, 2);
				String MM = expirydate.substring(2, expirydate.length());
				// TODO: format date to "yyyy-MM-dd"
				String expirydateFormated = xx + "-" + MM + "-" + "01";
				logger.info("cardtokenDto expirydate : " + expirydateFormated);
				autorisationService.logMessage(file,
						"cardtokenDto expirydate formated : " + expirydateFormated);
				Date dateExp;
				dateExp = dateFormatSimple.parse(expirydateFormated);

				if (checkCardNumber.size() == 0) {
					// TODO: insert new cardToken
					String tokencard = Util.generateCardToken(idclient);

					// TODO: test if token not exist in DB
					CardtokenDto checkCardToken = cardtokenService.findByIdMerchantAndToken(idclient, tokencard);

					while (checkCardToken != null) {
						tokencard = Util.generateCardToken(idclient);
						logger.info("checkCardToken exist => generate new tokencard : " + tokencard);
						autorisationService.logMessage(file,
								"checkCardToken exist => generate new tokencard : " + tokencard);
						checkCardToken = cardtokenService.findByIdMerchantAndToken(merchantid, tokencard);
					}
					logger.info("tokencard : " + tokencard);
					autorisationService.logMessage(file, "tokencard : " + tokencard);

					cardtokenDto.setToken(tokencard);
					String tokenid = UUID.randomUUID().toString();
					cardtokenDto.setIdToken(tokenid);
					cardtokenDto.setExprDate(dateExp);
					String dateTokenStr = dateFormat.format(dateToken);
					Date dateTokenFormated = dateFormat.parse(dateTokenStr);
					cardtokenDto.setTokenDate(dateTokenFormated);
					cardtokenDto.setCardNumber(cardnumber);
					cardtokenDto.setIdMerchant(merchantid);
					cardtokenDto.setIdMerchantClient(idclient);
					cardtokenDto.setFirstName(fname);
					cardtokenDto.setLastName(lname);
					cardtokenDto.setHolderName(holdername);
					cardtokenDto.setMcc(merchantid);

					CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

					autorisationService.logMessage(file, "Saving CARDTOKEN OK");
				} else {
					autorisationService.logMessage(file, "Carte deja enregistrée");
					for(CardtokenDto crd : checkCardNumber) {
						if(crd.getExprDate() != null) {
							if(crd.getCardNumber().equals(cardnumber)) {
								if(crd.getExprDate().before(dateToken)) {
									autorisationService.logMessage(file, "Encienne date expiration est expirée : " + dateFormatSimple.format(crd.getExprDate()));
									autorisationService.logMessage(file, "Update par nv date expiration saisie : "+ expirydateFormated);
									crd.setExprDate(dateExp);
									CardtokenDto cardSaved = cardtokenService.save(crd);
									logger.info("Update CARDTOKEN OK");
									autorisationService.logMessage(file, "Update CARDTOKEN OK");
								}
							}				
						}	
					}
				}

			} catch (ParseException e) {
				logger.error("Exception : " , e);
				autorisationService.logMessage(file, "savingcardtoken 500 Error during CARDTOKEN Saving " + Util.formatException(e));
			}
		}
		
		try {

			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			demandeDto.setDemCvv("");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file, "payer 500 Error during  date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		// TODO: appel 3DSSecure ***********************************************************

		/** dans la preprod les tests sans 3DSS on commente l'appel 3DSS et on mj reponseMPI="Y" */
		autorisationService.logMessage(file, "environement : " + environement);
		if(environement.equals("PREPROD")) {
			threeDsecureResponse.setReponseMPI("Y");
		} else {
			threeDsecureResponse = autorisationService.preparerReqThree3DSS(demandeDto, folder, file);
		}

		// TODO: fin 3DSSecure ***********************************************************

		/*
		 * ------------ DEBUT MPI RESPONSE PARAMS ------------
		 */
		String reponseMPI = "";
		String eci = "";
		String cavv = "";
		String threeDSServerTransID = "";
		String xid = "";
		String errmpi = "";
		String idDemande = String.valueOf(demandeDto.getIddemande() == null ? "" : demandeDto.getIddemande());
		String expiry = ""; // TODO: YYMM

		reponseMPI = threeDsecureResponse.getReponseMPI();

		threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();

		eci = threeDsecureResponse.getEci() == null ? "" : threeDsecureResponse.getEci();

		cavv = threeDsecureResponse.getCavv() == null ? "" : threeDsecureResponse.getCavv();

		errmpi = threeDsecureResponse.getErrmpi() == null ? "" : threeDsecureResponse.getErrmpi();

		expiry = threeDsecureResponse.getExpiry() == null ? "" : threeDsecureResponse.getExpiry();

		if (idDemande == null || idDemande.equals("")) {
			demandeDto.setDemCvv("");
			demandeDto.setEtatDemande("MPI_KO");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file, "received idDemande from MPI is Null or Empty");
			autorisationService.logMessage(file,
					"demandePaiement after update MPI_KO idDemande null : " + demandeDto.toString());
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			demandeDto.setDemCvv("");
			demandePaiementService.save(demandeDto);
			autorisationService.logMessage(file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("") || reponseMPI == null) {
			dmd.setDemCvv("");
			dmd.setEtatDemande("MPI_KO");
			demandePaiementService.save(dmd);
			autorisationService.logMessage(file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			autorisationService.logMessage(file, "Response 3DS is null");
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("Y")) {
			// TODO: ********************* Frictionless responseMPI equal Y *********************
			autorisationService.logMessage(file,
					"********************* Cas frictionless responseMPI equal Y *********************");
			if(threeDSServerTransID != null && !threeDSServerTransID.equals("")) {
				dmd.setDemxid(threeDSServerTransID);
				dmd.setIs3ds("N");
				dmd = demandePaiementService.save(dmd);
			}
			
			// TODO: 2024-03-05
			montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, model);

			merchantname = current_merchant.getCmrNom();
			websiteName = "";
			websiteid = dmd.getGalid();
			String url = "", status = "", statuscode = "";

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(merchantname, 19, ' ');

			merchant_city = "MOROCCO        ";

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

			// TODO: ajout cavv (cavv+eci) xid dans la trame
			String champ_cavv = "";
			xid = threeDSServerTransID;
			if (cavv == null || eci == null) {
				champ_cavv = null;
				autorisationService.logMessage(file, "cavv == null || eci == null");
			} else if (cavv != null && eci != null) {
				champ_cavv = cavv + eci;
			} else {
				autorisationService.logMessage(file, "champ_cavv = null");
				champ_cavv = null;
			}

			boolean cvv_present = checkCvvPresence(cvv);
			boolean is_reccuring = isReccuringCheck(recurring);
			boolean is_first_trs = true;

			String first_auth = "";
			long lrec_serie = 0;

			// TODO: controls
			autorisationService.logMessage(file, "Switch processing start ...");

			String tlv = "";
			autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

			if (!cvv_present && !is_reccuring) {
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file,
						"payer 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				demandeDtoMsg.setMsgRefus(
						"Le champ CVV est vide. Veuillez saisir le code de sécurité à trois chiffres situé au dos de votre carte pour continuer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// TODO: not reccuring , normal
			if (cvv_present && !is_reccuring) {
				autorisationService.logMessage(file, "not reccuring , normal cvv_present && !is_reccuring");
				try {

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

				} catch (Exception err4) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"payer 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}

				autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");

			}

			// TODO: reccuring
			if (is_reccuring) {
				autorisationService.logMessage(file, "reccuring");
			}

			autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
//			SwitchTCPClient sw = SwitchTCPClient.getInstance();
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {

				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				autorisationService.logMessage(file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file, "Switch  malfunction cannot connect!!!");

					autorisationService.logMessage(file,
							"payer 500 Error Switch communication s_conn false switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}

				if (s_conn) {
					autorisationService.logMessage(file, "Switch Connected.");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					autorisationService.logMessage(file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (Exception e) {
				switch_ko = 1;
				return autorisationService.handleSwitchError(e, file, orderid, merchantid, resp_tlv, dmd, model, "result");
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				autorisationService.logMessage(file, "payer 500 Error Switch null response" + "switch ip:[" + sw_s
						+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			if (switch_ko == 0 && resp.length() < 3) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				switch_ko = 1;

				autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
				autorisationService.logMessage(file, "payer 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

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
					tag66_resp = tlvp.getTag(Tags.tag66); // TODO: f1
					tag18_resp = tlvp.getTag(Tags.tag18);
					tag19_resp = tlvp.getTag(Tags.tag19); // TODO: f2
					tag23_resp = tlvp.getTag(Tags.tag23);
					tag20_resp = tlvp.getTag(Tags.tag20);
					tag21_resp = tlvp.getTag(Tags.tag21);
					tag22_resp = tlvp.getTag(Tags.tag22);
					tag80_resp = tlvp.getTag(Tags.tag80);
					tag98_resp = tlvp.getTag(Tags.tag98);

				} catch (Exception e) {
					autorisationService.logMessage(file, "Switch  malfunction tlv parsing !!!" + Util.formatException(e));
					switch_ko = 1;
					autorisationService.logMessage(file, "payer 500 Error during tlv Switch response parse"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// TODO: controle switch
				if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
					autorisationService.logMessage(file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					autorisationService.logMessage(file,
							"payer 500" + "Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}
			}
			autorisationService.logMessage(file, "Switch TLV Respnose Processed");

			String tag20_resp_verified = "";
			String tag19_res_verified = "";
			String tag66_resp_verified = "";
			tag20_resp_verified = tag20_resp;
			tag19_res_verified = tag19_resp;
			tag66_resp_verified = tag66_resp;
			String s_status, pan_auto = "";

			// TODO: SWHistoAutoDto swhist = null;

			if (switch_ko == 1) {
				pan_auto = Util.formatagePan(cardnumber);
				autorisationService.logMessage(file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + merchantid);
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;

			autorisationService.logMessage(file, "Insert into Histogate...");

			s_status = "";
			try {
				CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
				autorisationService.logMessage(file, "" + codeReponseDto);
				if (codeReponseDto != null) {
					s_status = codeReponseDto.getRpcLibelle();
				}
			} catch (Exception ee) {
				autorisationService.logMessage(file, "authorization 500 Error codeReponseDto null" + Util.formatException(ee));
			}
			autorisationService.logMessage(file, "get status Switch status : [" + s_status + "]");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);
				
				websiteid = dmd.getGalid();

				// TODO: Ihist_id = hist.getMAX_ID("HISTOAUTO_GATE", "HAT_ID");
				// TODO: Ihist_id = histoAutoGateService.getMAX_ID();
				// TODO: long currentid = Ihist_id.longValue() + 1;
				// TODO: hist.setId(currentid);

				autorisationService.logMessage(file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);
				autorisationService.logMessage(file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

				autorisationService.logMessage(file, "HistoAutoGate data filling start ...");
				
				autorisationService.logMessage(file, "websiteid : " + websiteid);

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
				hist.setHatNautemt(tag19_res_verified); // TODO: f2
				tag19_resp = tag19_res_verified;
				if (tag22_resp != null)
					hist.setHatProcode(tag22_resp.charAt(0));
				else
					hist.setHatProcode('6');
				hist.setHatExpdate(expirydate);
				hist.setHatRepondeur(tag21_resp);
				hist.setHatTypmsg("3");
				hist.setHatRrn(tag66_resp_verified); // TODO: f1
				tag66_resp_verified = tag66_resp;
				hist.setHatEtat('E');
				if (websiteid.equals("")) {
					hist.setHatCodtpe("1");
				} else {
					hist.setHatCodtpe(websiteid);
				}
				hist.setHatMcc(merc_codeactivite);
				hist.setHatNumCommande(orderid);
				hist.setHatNumdem(new Long(numTransaction));

				if (checkCvvPresence(cvv)) {

					hist.setIsCvvVerified("Y");
				} else {

					hist.setIsCvvVerified("N");
				}

				hist.setIs3ds("N");
				hist.setIsAddcard("N");
				hist.setIsWhitelist("N");
				hist.setIsWithsave("N");
				hist.setIsTokenized("N");

				if (recurring.equalsIgnoreCase("Y"))
					hist.setIsCof("Y");
				if (recurring.equalsIgnoreCase("N"))
					hist.setIsCof("N");

				autorisationService.logMessage(file, "HistoAutoGate data filling end ...");

				autorisationService.logMessage(file, "HistoAutoGate Saving ...");

				hist = histoAutoGateService.save(hist);
				
				autorisationService.logMessage(file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

			} catch (Exception e) {
				autorisationService.logMessage(file,
						"payer 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + Util.formatException(e));
				try {
					autorisationService.logMessage(file, "2eme tentative : HistoAutoGate Saving ... ");
					hist = histoAutoGateService.save(hist);
				} catch (Exception ex) {
					autorisationService.logMessage(file,
							"2eme tentative : payer 500 Error during  insert in histoautogate for given orderid:["
									+ orderid + "]" + Util.formatException(ex));
				}
			}

			autorisationService.logMessage(file, "HistoAutoGate OK.");

			if (tag20_resp == null) {
				tag20_resp = "";
			}

			if (tag20_resp.equalsIgnoreCase("00"))

			{
				autorisationService.logMessage(file, "SWITCH RESONSE CODE :[00]");

				try {
					autorisationService.logMessage(file, "update etat demande : SW_PAYE ...");

					dmd.setEtatDemande("SW_PAYE");
					if(dmd.getTransactiontype().equals("0")) {
						dmd.setDemCvv("");
					}	
					dmd = demandePaiementService.save(dmd);
					autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");
				} catch (Exception e) {
					autorisationService.logMessage(file,
							"payer 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:[" + orderid
									+ "]" + Util.formatException(e));
				}

				// TODO: 2023-01-03 confirmation par Callback URL
				String resultcallback = "";
				String callbackURL = dmd.getCallbackURL();
				autorisationService.logMessage(file, "Call Back URL: " + callbackURL);
				if (dmd.getCallbackURL() != null && !dmd.getCallbackURL().equals("")
						&& !dmd.getCallbackURL().equals("NA")) {
					String clesigne = current_infoCommercant.getClePub();

					String montanttrx = String.format("%.2f", dmd.getMontant()).replace(",", ".");
					String token_gen = "";

					autorisationService.logMessage(file,
							"sendPOST(" + callbackURL + "," + clesigne + "," + dmd.getCommande() + ","
									+ tag20_resp + "," + montanttrx + "," + hist.getHatNautemt() + ","
									+ hist.getHatNumdem() + "," + dmd.getTypeCarte() + ")");

					resultcallback = sendPOST(callbackURL, clesigne, dmd.getCommande(), tag20_resp,
							montanttrx, hist.getHatNautemt(), hist.getHatNumdem(), token_gen,
							Util.formatCard(cardnumber), dmd.getTypeCarte(), folder, file);

					autorisationService.logMessage(file,
							"resultcallback :[" + resultcallback + "]");

					boolean repsucces = resultcallback.indexOf("GATESUCCESS") != -1 ? true : false;

					boolean repfailed = resultcallback.indexOf("GATEFAILED") != -1 ? true : false;

					autorisationService.logMessage(file, "repsucces : " + repsucces);
					autorisationService.logMessage(file, "repfailed : " + repfailed);
					if (repsucces) {
						autorisationService.logMessage(file,
								"Reponse recallURL OK => GATESUCCESS");
						dmd.setRecallRep("Y");
						dmd = demandePaiementService.save(dmd);
					} else {
						if (repfailed) {
							autorisationService.logMessage(file,
									"Reponse recallURL KO => GATEFAILED");
							dmd.setRecallRep("N");
							dmd = demandePaiementService.save(dmd);
						} 
						//else {
							if (!dgiPreprod.equals(merchantid) && !dgiProd.equals(merchantid)) {
								autorisationService.logMessage(file, "Annulation auto start ...");

								String repAnnu = annulationAuto(dmd, current_merchant, hist, model, folder,
										file);

								autorisationService.logMessage(file, "Annulation auto end");
								s_status = "";
								try {
									CodeReponseDto codeReponseDto = codeReponseService
											.findByRpcCode(repAnnu);
									logger.info("codeReponseDto annulation : " + codeReponseDto);
									autorisationService.logMessage(file,
											"codeReponseDto annulation : " + codeReponseDto);
									if (codeReponseDto != null) {
										s_status = codeReponseDto.getRpcLibelle();
									}
								} catch (Exception ee) {
									autorisationService.logMessage(file,
											"Annulation auto 500 Error codeReponseDto null" + Util.formatException(ee));
									// TODO: ee.printStackTrace();
								}
								autorisationService.logMessage(file,
										"Switch status annulation : [" + s_status + "]");
								if (repAnnu.equals("00")) {
									dmd.setEtatDemande("SW_ANNUL_AUTO");
									dmd.setDemCvv("");
									demandePaiementService.save(dmd);
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "operationAnnulee";
								} else {
									page = "error";
								}
								
								response.sendRedirect(dmd.getFailURL());

								autorisationService.logMessage(file, "Fin processRequest ()");
								logger.info("Fin processRequest ()");
								return page;

							}
						//}
					}
				}
				// TODO: 2024-02-28 confirmation par Callback URL

			} else {

				autorisationService.logMessage(file, "transaction declined !!! ");
				autorisationService.logMessage(file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {
					autorisationService.logMessage(file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtatDemande("SW_REJET");
					dmd.setDemCvv("");
					dmd = demandePaiementService.save(dmd);
				} catch (Exception e) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"payer 500 Error during  DemandePaiement update SW_REJET for given orderid:[" + orderid
									+ "]" + Util.formatException(e));
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}
				autorisationService.logMessage(file, "update Demandepaiement status to SW_REJET OK.");
				// TODO: 2024-02-27
				try {
					if(hist.getId() == null) {
						// TODO: get histoauto check if exist
						HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
						if(histToAnnulle != null) {
							autorisationService.logMessage(file,
									"transaction declinded ==> update HistoAutoGateDto etat to A ...");
							histToAnnulle.setHatEtat('A');
							histToAnnulle = histoAutoGateService.save(histToAnnulle);
						} else {
							hist.setHatEtat('A');
							hist = histoAutoGateService.save(hist);
						}
					} else {
						hist.setHatEtat('A');
						hist = histoAutoGateService.save(hist);
					}
				} catch (Exception err2) {
					autorisationService.logMessage(file,
							"payer 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
				}
				autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
				// TODO: 2024-02-27
			}

			autorisationService.logMessage(file, "Generating paymentid...");

			String uuid_paymentid, paymentid = "";
			try {
				uuid_paymentid = String.format("%040d",
						new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
				paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"payer 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + Util.formatException(e));
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors de la génération de l'ID de paiement), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			autorisationService.logMessage(file, "Generating paymentid OK");
			autorisationService.logMessage(file, "paymentid :[" + paymentid + "]");

			// TODO: JSONObject jso = new JSONObject();

			autorisationService.logMessage(file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDemPan();
				transactionid = String.valueOf(hist.getHatNumdem());
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"payer 500 Error during authdata preparation orderid:[" + orderid + "]" + Util.formatException(e));
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// TODO: reccurent transaction processing

			// TODO: reccurent insert and update

			try {

				/*
				 * String data_noncrypt = "orderid=" + orderid + "&fname=" + fname + "&lname=" +
				 * lname + "&email=" + email + "&amount=" + amount + "&coderep=" + coderep +
				 * "&authnumber=" + authnumber + "&cardnumber=" + Util.formatCard(cardnumber) +
				 * "&transactionid=" + transactionid + "&paymentid=" + paymentid;
				 */

				String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
						+ "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep + "&numAuto=" + authnumber
						+ "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
						+ "&numTrans=" + transactionid;

				autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
				logger.info("data_noncrypt : " + data_noncrypt);

				if (data_noncrypt.length() > 200) {
					// TODO : First, try reducing the length by adjusting the fname
					if (!fname.isEmpty()) {
						fname = fname.length() > 10 ? fname.substring(0, 10) : fname;
					}

					// TODO : Rebuild the data_noncrypt string with the updated fname
					data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
							+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
							+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
							+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid;

					autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
					// TODO : If the length is still greater than 200, reduce the length of email
					if (data_noncrypt.length() > 200 && !email.isEmpty()) {
						email = email.length() > 10 ? email.substring(0, 10) : email;
					}

					// TODO : Rebuild again with the updated email
					data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
							+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
							+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
							+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid;

					autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
				}

				String plainTxtSignature = orderid + current_infoCommercant.getClePub();

				autorisationService.logMessage(file, "plainTxtSignature : " + plainTxtSignature);
				logger.info("plainTxtSignature : " + plainTxtSignature);

				String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt, current_infoCommercant.getClePub(),
						plainTxtSignature, folder, file);

				autorisationService.logMessage(file, "data encrypt : " + data);
				logger.info("data encrypt : " + data);

				if (coderep.equals("00")) {
					autorisationService.logMessage(file,
							"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					logger.info("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					if (dmd.getSuccessURL() != null) {
						response.sendRedirect(dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
					} else {
						ResponseDto responseDto = new ResponseDto();
						responseDto.setLname(dmd.getNom());
						responseDto.setFname(dmd.getPrenom());
						responseDto.setOrderid(dmd.getCommande());
						responseDto.setAuthnumber(authnumber);
						responseDto.setAmount(dmd.getMontant());
						responseDto.setTransactionid(transactionid);
						responseDto.setMerchantid(dmd.getComid());
						responseDto.setEmail(dmd.getEmail());
						responseDto.setMerchantname(current_infoCommercant.getCmrNom());
						responseDto.setCardnumber(Util.formatCard(cardnumber));
						responseDto.setTransactiontime(dateFormat.format(new Date()));

						model.addAttribute("responseDto", responseDto);

						page = "index";
						autorisationService.logMessage(file, "Fin payer ()");
						logger.info("Fin payer ()");
						return page;
					}
				} else {
					autorisationService.logMessage(file,
							"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
					logger.info("coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());

					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (" + s_status + ")," + " votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
				}
			} catch (Exception jsouterr) {
				autorisationService.logMessage(file,
						"payer 500 Error during jso out processing given authnumber:[" + authnumber + "]" + jsouterr);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// TODO: fin
			// TODO: *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// TODO: ********************* Cas chalenge responseMPI equal C ou D
			// TODO: *********************
			autorisationService.logMessage(file, "****** Cas chalenge responseMPI equal C ou D ******");
			try {
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				if(threeDSServerTransID.equals("") || threeDSServerTransID == null) {
					threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
				}
				dmd.setDemxid(threeDSServerTransID);
				dmd.setEtatDemande("SND_TO_ACS");
				dmd.setIs3ds("Y");
				demandeDto = demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "threeDSServerTransID : " + demandeDto.getDemxid());
				model.addAttribute("demandeDto", demandeDto);
				// TODO: 2024-06-20 old
				/*page = "chalenge";

				autorisationService.logMessage(file, "set demandeDto model creq : " + demandeDto.getCreq());
				autorisationService.logMessage(file, "return page : " + page);*/
				
				// TODO: 2024-06-20
				// TODO: autre façon de faire la soumission automatique de formulaires ACS via le HttpServletResponse.
	
		        String creq = "";
		        String acsUrl = "";
		        String response3DS = threeDsecureResponse.getHtmlCreq();
		        //Pattern pattern = Pattern.compile("action='(.*?)'.*value='(.*?)'");
		        Pattern pattern = Pattern.compile("action='([^']*)'.*?value='([^']*)'");
		        Matcher matcher = pattern.matcher(response3DS);

		        // TODO: Si une correspondance est trouvée
	            if (matcher.find()) {
	                acsUrl = matcher.group(1);
	                creq = matcher.group(2);
	                logger.info("L'URL ACS est : " + acsUrl);
	                logger.info("La valeur de creq est : " + creq);
	                autorisationService.logMessage(file, "L'URL ACS est : " + acsUrl);
	                autorisationService.logMessage(file, "La valeur de creq est : " + creq);

	                String decodedCreq = new String(Base64.decodeBase64(creq.getBytes()));
	                logger.info("La valeur de decodedCreq est : " + decodedCreq);
	                autorisationService.logMessage(file, "La valeur de decodedCreq est : " + decodedCreq);
	                
	                // TODO: URL de feedback après soumission ACS
	                String feedbackUrl = request.getContextPath() + "/acsFeedback";

	                // TODO: Afficher le formulaire HTML dans la réponse
	                response.setContentType("text/html");
	                response.setCharacterEncoding("UTF-8");
	                response.getWriter().println("<html><body>");
	                response.getWriter().println("<form id=\"acsForm\" action=\"" + acsUrl + "\" method=\"post\">");
	                response.getWriter().println("<input type=\"hidden\" name=\"creq\" value=\"" + creq + "\">");
	                response.getWriter().println("</form>");
	                response.getWriter().println("<script>document.getElementById('acsForm').submit();</script>");
	                
	                /* a revoir apres pour la confirmation de l'affichage acs
	                response.getWriter().println("document.getElementById('acsForm').submit();");
	                response.getWriter().println("fetch('" + feedbackUrl + "', { method: 'POST' });");  // TODO: Envoi du feedback
	                response.getWriter().println("</script>");
	                */
	                response.getWriter().println("</body></html>");
	                
	                logger.info("Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");
	                autorisationService.logMessage(file, "Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");
	                
	                return null;  // TODO: Terminer le traitement ici après avoir envoyé le formulaire
	            } else {
	                logger.info("Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
	                autorisationService.logMessage(file, "Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
	                page = "error";  // TODO: Définir la page d'erreur appropriée
	            }
				
			// TODO: 2024-06-20
			} catch (Exception ex) {
				autorisationService.logMessage(file, "Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML " + Util.formatException(ex));
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				page = "result";
				return page;
			}
		} else if (reponseMPI.equals("E")) {
			// TODO: ********************* Cas responseMPI equal E
			// TODO: *********************
			page = autorisationService.handleMpiError(errmpi, file, idDemande, threeDSServerTransID, dmd, model, page);
		} else {
			page = autorisationService.handleMpiError(errmpi, file, idDemande, threeDSServerTransID, dmd, model, page);
		}

		logger.info("return page : " + page);

		autorisationService.logMessage(file, "*********** End payer () ************** ");
		logger.info("*********** End payer () ************** ");

		return page;
	}
	
	@PostMapping("/cancelPayment")
	@SuppressWarnings("all")
	public ResponseEntity<Map<String, String>> cancelPayment(@RequestBody Map<String, String> requestData) {
		String file = "GW_ABONDONNE_PAGE_" + randomWithSplittableRandom;
		autorisationService.logMessage(file, "cancelPayment : Client Clique sur le bouton Annuler");
		String idDemandeStr = requestData.get("iddemande");
		Integer idDemande = null;
		if (idDemandeStr != null) {
			idDemande = Integer.valueOf(idDemandeStr);
		}
		try {
			DemandePaiementDto demandePaiement = demandePaiementService.findByIdDemande(idDemande);
			if (demandePaiement != null) {
				if(demandePaiement.getEtatDemande().equals("P_CHRG_OK") || demandePaiement.getEtatDemande().equals("START_PAYMENT")) {
					demandePaiement.setEtatDemande("P_ABDNEE_CLC_ANNULER");
					demandePaiement = demandePaiementService.save(demandePaiement);
					autorisationService.logMessage(file, "cancelPayment : Modification etat_demande to P_ABDNEE_CLC_ANNULER idDemande/Commande : " + idDemande +"/" + demandePaiement.getCommande());
				}
				return ResponseEntity.ok(Collections.singletonMap("message", "Paiement annulé avec succès"));
			}
		} catch (Exception e) {
			autorisationService.logMessage(file, "cancelPayment : Erreur lors du traitement de l'annulation idDemande : " + idDemande);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Erreur lors du traitement de l'annulation"));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Collections.singletonMap("message", "Paiement annulé avec succès"));
	}
	
	@PostMapping("/acsFeedback")
    @ResponseBody
    @SuppressWarnings("all")
    public String acsFeedback() {
        // TODO: Traiter le feedback reçu du client
        logger.info("Feedback reçu : la page ACS a été affichée et soumise.");
        autorisationService.logMessage(file, "Feedback reçu : la page ACS a été affichée et soumise.");
        return "Feedback reçu";
    }
	
	@PostMapping("/retour")
	@SuppressWarnings("all")
	public String retour(Model model, @ModelAttribute("demandeDto") DemandePaiementDto demandeDto, HttpServletRequest request,
	                     HttpServletResponse response, HttpSession session) throws IOException {
	    randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
	    String file = "GW_TIMEOUT_" + randomWithSplittableRandom;
	    Util.creatFileTransaction(file);
	    autorisationService.logMessage(file, "*********** Start retour () ************** ");
	    logger.info("*********** Start retour () ************** ");
	    
	    String page = "timeout";
	    String msg = "OK";
	    Integer idDemande = null;
	    // TODO: Récupération de l'attribut de session
	    try {
	    	String idDemandeStr = String.valueOf(session.getAttribute("idDemande"));
	        if (idDemandeStr != null) {
	            idDemande = Integer.valueOf(idDemandeStr);
	        }
	        idDemande = (Integer) session.getAttribute("idDemande");
	        autorisationService.logMessage(file, "idDemande par session : " + idDemande);
	        logger.info("idDemande par session : " + idDemande);            
	    } catch (Exception e) {
	        logger.info("Retour getIdDemande par session " + Util.formatException(e));
	        autorisationService.logMessage(file, "retour getIdDemande par session " + Util.formatException(e));
	        msg = "KO";
	    }
	    
	    // TODO: Si l'attribut de session est nul, utilisez l'ID de la demande du DTO
	    if (idDemande == null) {
	        try {
	            idDemande = demandeDto.getIddemande();
	            autorisationService.logMessage(file, "idDemandepar model demandeDto : " + idDemande);
	            logger.info("idDemande par model demandeDto : " + idDemande);    
	        } catch (Exception ex) {
	            logger.info("Retour getIdDemande par demandeDto " + Util.formatException(ex));
	            autorisationService.logMessage(file, "retour getIdDemande par demandeDto " + Util.formatException(ex));
	            msg = "KO";
	        }
	    }
        logger.info("msg : " + msg);
        autorisationService.logMessage(file, "msg : " + msg);
	    // TODO: Traitement de la demande si l'ID de la demande est disponible
	    if (msg.equals("OK") && idDemande != null) {
	        DemandePaiementDto demandePaiement = demandePaiementService.findByIdDemande(idDemande);

	        if (demandePaiement != null) {
	            logger.info("update Demandepaiement status to Timeout");
	            autorisationService.logMessage(file, "update Demandepaiement status to Timeout");
	            demandePaiement.setEtatDemande("TimeOut");
	            demandePaiement.setDemCvv("");
	            demandePaiement = demandePaiementService.save(demandePaiement);
	            String failUrl = demandePaiement.getFailURL();
	            String successUrl = demandePaiement.getSuccessURL();
	            if (failUrl != null && !failUrl.equals("")) {
	                response.sendRedirect(failUrl);
	            } else {
	                response.sendRedirect(successUrl);
	            }
	        } else {
	            logger.info("DemandePaiement not found ");
	            autorisationService.logMessage(file, "DemandePaiement not found ");
	            response.sendRedirect(page);
	        }
	    } else {
	        // TODO: Gérer le cas où idDemande est null après les tentatives
	        logger.info("idDemande is null");
	        autorisationService.logMessage(file, "idDemande is null");
	        response.sendRedirect(page);
	    }

	    autorisationService.logMessage(file, "*********** End retour () ************** ");
	    logger.info("*********** End retour () ************** ");

	    return page;
	}
	
	@RequestMapping(value = "/chalenge", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String chlenge(Model model) {
		// TODO: Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_CHALENGE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		DemandePaiementDto dem = new DemandePaiementDto();
		logger.info("Start chalenge ()");
		autorisationService.logMessage(file, "*********** Start chalenge () ************** ");
		logger.info("*********** Start chalenge () ************** ");

		String htmlCreq = "<form action='https://acs.naps.ma:443/lacs2' method='post' enctype='application/x-www-form-urlencoded'>"
				+ "<input type='hidden' name='creq' value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjQxZDQ0ZTViLTBjOTYtNGVhNC05NjkxLTM1OWVmOGQ5NTdjMyIsCiAgImFjc1RyYW5zSUQiOiAiOTI3NTQyOGEtYzkzYi00ZWUzLTk3NDEtNDA4NzAzNDlmYzM2IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' />"
				+ "</form>";
		dem.setCreq(htmlCreq);
		logger.info("dem htmlCreq : " + dem.getCreq());

		logger.info("dem commande : " + dem.getCommande());
		logger.info("dem montant : " + dem.getMontant());

		model.addAttribute("demandeDto", dem);
		logger.info("return to chalenge.html");

		autorisationService.logMessage(file, "*********** End chalenge () ************** ");
		logger.info("*********** End chalenge () ************** ");

		return "chalenge";
	}

	@RequestMapping(value = "/napspayment/error/token/{token}", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String error(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_ERROR_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start error ************** ");
		logger.info("*********** Start error ************** ");

		String page = "error";

		autorisationService.logMessage(file, "findByTokencommande token : " + token);
		logger.info("findByTokencommande token : " + token);

		DemandePaiementDto current_dem = demandePaiementService.findByTokencommande(token);
		String msgRefus = "Une erreur est survenue, merci de réessayer plus tard";

		if (current_dem != null) {
			autorisationService.logMessage(file, "current_dem is found OK");
			logger.info("current_dem is found OK");
			if (current_dem.getEtatDemande().equals("SW_PAYE") || current_dem.getEtatDemande().equals("PAYE")) {
				msgRefus = "La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer.";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else if (current_dem.getEtatDemande().equals("SW_REJET")) {
				msgRefus = "La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer.";
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
			autorisationService.logMessage(file, "current_dem not found ");
			logger.info("current_dem null ");
			page = "error";
		}

		autorisationService.logMessage(file, "*********** End error ************** ");
		logger.info("*********** End error ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/index2", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String index2() {
		// TODO: Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_INDEX2_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start index2 () ************** ");
		logger.info("*********** Start index2 () ************** ");

		autorisationService.logMessage(file, "return to index2.html");
		logger.info("return to index2.html");

		autorisationService.logMessage(file, "*********** End index2 () ************** ");
		logger.info("*********** End index2 () ************** ");

		return "index2";
	}

	@RequestMapping(value = "/napspayment/newpage", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String newpage(Model model, HttpSession session) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_NEWPAGE_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start newpage () ************** ");
		logger.info("*********** Start newpage () ************** ");
		DemandePaiementDto demandeDto = new DemandePaiementDto();
		demandeDto.setCommande("81375468795252");
		demandeDto.setMontantStr("250.00");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String formattedDate = now.format(formatter);
		System.out.println("<p><strong>Date :</strong> " + formattedDate + "</p>");
		model.addAttribute("demandeDto", demandeDto);
		model.addAttribute("formattedDate", formattedDate);

		autorisationService.logMessage(file, "*********** End newpage () ************** ");
		logger.info("*********** End newpage () ************** ");

		return "newpage";
	}

	@PostMapping("/processpaiement")
	@SuppressWarnings("all")
	public String processpaiement(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto, HttpServletRequest request,
								  HttpServletResponse response, HttpSession session,
								  BindingResult result) throws IOException {
		System.out.println("processpaiement");
		System.out.println("carte : " + dto.getDemPan());
		try {
			if(dto.getDemPan() != null && dto.getDemPan().length() != 16) {
				response.sendRedirect(request.getContextPath() + "/napspayment/newpage");
				session.setAttribute("error", "Le numéro de carte doit contenir exactement 16 chiffres.");
				return null;
			}
		} catch(Exception e) {
			response.sendRedirect(request.getContextPath() + "/napspayment/newpage");
			session.setAttribute("error", "Vérifiez les informations de la carte.");
			return null;
		}

		return "newpage";
	}
	
	@RequestMapping(value = "/napspayment/operationAnnulee", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String operationAnnulee() {
		// TODO: Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_operationAnnulee_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start operationAnnulee () ************** ");
		logger.info("*********** Start operationAnnulee () ************** ");

		autorisationService.logMessage(file, "return to operationAnnulee.html");
		logger.info("return to operationAnnulee.html");

		autorisationService.logMessage(file, "*********** End operationAnnulee () ************** ");
		logger.info("*********** End operationAnnulee () ************** ");

		return "operationAnnulee";
	}
	
	@RequestMapping(value = "/napspayment/operationEffectue", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String operationEffectue() {
		// TODO: Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_operationEffectue_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start operationEffectue () ************** ");
		logger.info("*********** Start operationEffectue () ************** ");

		autorisationService.logMessage(file, "return to operationEffectue.html");
		logger.info("return to operationEffectue.html");

		autorisationService.logMessage(file, "*********** End operationEffectue () ************** ");
		logger.info("*********** End operationEffectue () ************** ");

		return "operationEffectue";
	}

	@RequestMapping(value = "/napspayment/result", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String result() {
		// TODO: Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_RESULT_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);

		autorisationService.logMessage(file, "*********** Start result () ************** ");
		logger.info("*********** Start result () ************** ");

		autorisationService.logMessage(file, "return to result.html");
		logger.info("return to result.html");

		autorisationService.logMessage(file, "*********** End result () ************** ");
		logger.info("*********** Start Fin () ************** ");

		return "result";
	}
	
	@SuppressWarnings("all")
	public String annulationAuto(DemandePaiementDto current_dmd, CommercantDto current_merchant,
			HistoAutoGateDto current_hist, Model model, String folder, String file) {

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		String[] mm;
		String[] m;
		String montanttrame = "";
		String amount = String.valueOf(current_dmd.getMontant());
		String orderid = current_dmd.getCommande();
		String merchantid = current_dmd.getComid();
		String page = "index";
		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";
		} catch (Exception err3) {
			autorisationService.logMessage(file,
					"annulation auto 500 Error during date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err3));

			return "96";
		}

		// TODO: 2024-03-05
		montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, model);

		autorisationService.logMessage(file, "Switch processing start ...");

		String tlv = "";
		autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

		// TODO: controls
		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		String merchantname = current_merchant.getCmrAbrvnom();
		String cardnumber = current_dmd.getDemPan();
		String authnumber = current_hist.getHatNautemt();
		String merchant_name = merchantname;

		String mesg_type = "2";
		String acq_type = "0000";
		String processing_code = "0";
		String reason_code = "H";
		String transaction_condition = "6";
		String transactionnumber = current_hist.getHatNautemt();
		merchant_name = Util.pad_merchant(merchantname, 19, ' ');
		String merchant_city = "MOROCCO        ";

		try {

			String currency = current_hist.getHatDevise();
			String expirydate = current_hist.getHatExpdate();
			String rrn = current_hist.getHatRrn();
			transactionnumber = rrn;
			tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
					.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
					.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame).withField(Tags.tag15, currency)
					.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate).withField(Tags.tag16, date)
					.withField(Tags.tag17, heure).withField(Tags.tag10, merc_codeactivite)
					.withField(Tags.tag8, "0" + merchantid).withField(Tags.tag9, merchantid)
					.withField(Tags.tag66, transactionnumber).withField(Tags.tag11, merchant_name)
					.withField(Tags.tag12, merchant_city).withField(Tags.tag13, "MAR")
					.withField(Tags.tag23, reason_code).withField(Tags.tag90, acqcode).withField(Tags.tag19, authnumber)
					.encode();

		} catch (Exception err4) {
			autorisationService.logMessage(file,
					"annulation auto 500 Error during switch tlv buildu for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));

			return "96";
		}

		autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");

		autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

		autorisationService.logMessage(file, "Switch Connecting ...");

		String resp_tlv = "";
		SwitchTCPClient sw = SwitchTCPClient.getInstance();

		int port = 0;
		String sw_s = "", s_port = "";
		try {

			s_port = portSwitch;
			sw_s = ipSwitch;

			autorisationService.logMessage(file, "Switch IP / Switch PORT : " + sw_s + "/" + s_port);

			port = Integer.parseInt(s_port);

			boolean s_conn = sw.startConnection(sw_s, port);
			autorisationService.logMessage(file, "Switch Connecting ...");

			if (s_conn) {
				autorisationService.logMessage(file, "Switch Connected.");

				resp_tlv = sw.sendMessage(tlv);

				autorisationService.logMessage(file, "Switch TLV Request end.");
				sw.stopConnection();

			} else {
				autorisationService.logMessage(file, "Switch  malfunction !!!");

				return "96";
			}

		} catch (Exception e) {
			autorisationService.logMessage(file, "Switch  malfunction !!!" + Util.formatException(e));
			return "96";
		}

		String resp = resp_tlv;
		if (resp == null) {
			autorisationService.logMessage(file, "Switch  malfunction !!!");
			return "96";
		}

		if (resp.length() < 3) {
			autorisationService.logMessage(file, "Switch  malfunction !!! short response length() < 3 switch");
			return "96";
		}

		autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

		// TODO: resp debug =
		// TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

		TLVParser tlvp = null;

		// TODO: resp debug =
		// TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

		String tag0_resp, tag1_resp, tag3_resp, tag8_resp, tag9_resp, tag14_resp, tag15_resp, tag16_resp, tag17_resp,
				tag66_resp, tag18_resp, tag19_resp, tag23_resp, tag20_resp, tag21_resp, tag22_resp, tag80_resp,
				tag98_resp = "";

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
			tag66_resp = tlvp.getTag(Tags.tag66); // TODO: f1
			tag18_resp = tlvp.getTag(Tags.tag18);
			tag19_resp = tlvp.getTag(Tags.tag19); // TODO: f2
			tag23_resp = tlvp.getTag(Tags.tag23);
			tag20_resp = tlvp.getTag(Tags.tag20);
			tag21_resp = tlvp.getTag(Tags.tag21);
			tag22_resp = tlvp.getTag(Tags.tag22);
			tag80_resp = tlvp.getTag(Tags.tag80);
			tag98_resp = tlvp.getTag(Tags.tag98);

		} catch (Exception e) {
			autorisationService.logMessage(file, "Switch  malfunction !!! Error during tlv Switch response parse switch" + Util.formatException(e));
			return "96";
		}

		// TODO: controle switch
		if (tag1_resp == null) {
			autorisationService.logMessage(file, "Switch  malfunction !!!");
			return "96";
		}

		if (tag1_resp.length() < 3) {
			autorisationService.logMessage(file, "Switch  malfunction !!! response parse tag1_resp length tag  < 3 switch");
			return "96";
		}

		autorisationService.logMessage(file, "Switch TLV Respnose Processed");

		if (tag20_resp == null) {
			return "96";
		}
		if (tag20_resp.length() < 1) {
			return "96";
		}

		if (tag20_resp.equalsIgnoreCase("00"))

		{
			autorisationService.logMessage(file, "Switch CODE REP : [00]");

			autorisationService.logMessage(file, "Transaction reversed.");

			try {
				autorisationService.logMessage(file, "Setting DemandePaiement status A ...");

				current_dmd.setEtatDemande("A");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"annulation auto 500 Error during  demandepaiement update  A for given orderid:[" + orderid
								+ "]" + Util.formatException(e));

				return "96";
			}

			autorisationService.logMessage(file, "Setting DemandePaiement status OK.");

			autorisationService.logMessage(file, "Setting HistoAutoGate status A ...");

			// TODO: 2024-03-15
			try {
				autorisationService.logMessage(file, "HAT_ID : " + current_hist.getId());
				if(current_hist.getId() == null) {
					// TODO: get histoauto check if exist
					HistoAutoGateDto histToAnnulle = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr(orderid, merchantid);
					if(histToAnnulle !=null) {
						autorisationService.logMessage(file,
								"transaction declinded ==> update HistoAutoGateDto etat to A ...");
						histToAnnulle.setHatEtat('A');
						histToAnnulle = histoAutoGateService.save(histToAnnulle);
					} else {
						current_hist.setHatEtat('A');
						current_hist = histoAutoGateService.save(current_hist);
					}
				} else {
					current_hist.setHatEtat('A');
					current_hist = histoAutoGateService.save(current_hist);
				}
			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"annulation auto 500 Error during HistoAutoGate findLastByHatNumCommandeAndHatNumcmr orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
			}
			autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
			// TODO: 2024-03-15

			autorisationService.logMessage(file, "Setting HistoAutoGate status OK.");
		} else {

			autorisationService.logMessage(file, "Transaction annulation auto declined.");
			autorisationService.logMessage(file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		return tag20_resp;
	}

	@SuppressWarnings("all")
	private String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid, 
			Model model) {
		String montanttrame = "";
		String[] mm;
		String[] m;
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		try {
			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			autorisationService.logMessage(file, "montant : [" + amount + "]");

			String montantt = amount + "";

			mm = montantt.split("\\.");
			if (mm[1].length() == 1) {
				montanttrame = amount + "0";
			} else {
				montanttrame = amount + "";
			}

			m = montanttrame.split("\\.");
			if (m[1].equals("0")) {
				montanttrame = montanttrame.replace(".", "0");
			} else
				montanttrame = montanttrame.replace(".", "");
			montanttrame = Util.formatageCHamps(montanttrame, 12);
		} catch (Exception err3) {
			autorisationService.logMessage(file,
					"authorization 500 Error during  amount formatting for given orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err3));
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			String page0 = "result";
			autorisationService.logMessage(file, "Fin processRequest ()");
			logger.info("Fin processRequest ()");
			return page0;
		}
		return montanttrame;
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
			monthNames.add(month.toString());
		}
		return monthNames;
	}

	@SuppressWarnings("all")
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

			monthNames.add(month.toString());
			monthNamesValues.add(exp);

		}
		return monthNamesValues;
	}

	@SuppressWarnings("all")
	public void formatDateExp(String expirationDate, Cartes carte) {
		try {
			LocalDate localDate = LocalDate.parse(expirationDate);
			Month mois = localDate.getMonth();
			Integer year = localDate.getYear();
			carte.setYear(year);
			// TODO: String formattedMonth = mapToFrenchMonth(month);
			String moisStr = String.format("%s", mois);
			List<String> list = new ArrayList<>();
			list.add(moisStr);
			MonthDto month = mapToFrenchMonth(moisStr);
			carte.setMois(month.getMonth());
			carte.setMoisValue(month.getValue());
			
			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();
			// TODO: get year from date
			// TODO: format date to "yyyy-MM-dd"
			String expirydateFormated = carte.getYear() + "-" + carte.getMoisValue() + "-" + "01";
			// TODO: exp
			//String expirydateFormated = "2020" + "-" + "05" + "-" + "01";
			logger.info("cardtokenDto expirydate : " + expirydateFormated);
			autorisationService.logMessage(file,
					"cardtokenDto expirydate formated : " + expirydateFormated);
			Date dateExp = dateFormatSimple.parse(expirydateFormated);
			if(dateExp.before(dateToken)) {
				logger.info("date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
				autorisationService.logMessage(file, "date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
				carte.setMoisValue("xxxx");
				carte.setMois("xxxx");
				carte.setYear(1111);
			}
			if(dateExp.after(dateToken)) {
				logger.info("date exiration est superieur à l adate systeme : " + dateExp + " < " + dateToken);
			}
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}
	}
	
	@SuppressWarnings("all")
	public static String sendPOST(String urlcalback, String clepub, String idcommande, String repauto, String montant,
			String numAuto, Long numTrans, String token_gen, String pan_trame, String typecarte, String folder,
			String file) throws IOException {

		String result = "";
		HttpPost post = new HttpPost(urlcalback);

		String reqenvoi = idcommande + repauto + clepub + montant;
		String signature = Util.hachInMD5(reqenvoi);
		Util.writeInFileTransaction(folder, file,"Signature : " + signature);
		// TODO: add request parameters or form parameters
		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("repauto", repauto));
		urlParameters.add(new BasicNameValuePair("montant", montant));
		urlParameters.add(new BasicNameValuePair("signature", signature));
		urlParameters.add(new BasicNameValuePair("numAuto", numAuto));
		urlParameters.add(new BasicNameValuePair("numTrans", String.valueOf(numTrans)));
		urlParameters.add(new BasicNameValuePair("idcommande", idcommande));
		urlParameters.add(new BasicNameValuePair("token", token_gen));
		urlParameters.add(new BasicNameValuePair("carte", pan_trame));
		urlParameters.add(new BasicNameValuePair("typecarte", typecarte));
		post.setEntity(new UrlEncodedFormEntity(urlParameters));

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				httpClient = (CloseableHttpClient) getAllSSLClient();
			} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
				Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-KeyManagementException] sendPOST " + e1);
			}
			Util.writeInFileTransaction(folder, file,idcommande + " Recall URL tentative 1");
			CloseableHttpResponse response = httpClient.execute(post);

			result = EntityUtils.toString(response.getEntity());

		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file,
					" sendPOST Exception => {} tv 1 :" + ex.getMessage() + "ex : " + Util.formatException(ex));
			result = "ko";
		}
		boolean repsucces = result.indexOf("GATESUCCESS") != -1 ? true : false;
		boolean repfailed = result.indexOf("GATEFAILED") != -1 ? true : false;
		if (!repsucces && !repfailed) {
			try {
				Thread.sleep(10000);
				Util.writeInFileTransaction(folder, file,idcommande + " Recall URL tentative 2");
				// TODO: tentative 2 apès 10 s
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse response = httpClient.execute(post)) {

					result = EntityUtils.toString(response.getEntity());
				}
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file,
						" sendPOST Exception => {} tv 2 :" + ex.getMessage() + "ex : " + Util.formatException(ex));
				result = "ko";
			}
			boolean repsucces2 = result.indexOf("GATESUCCESS") != -1 ? true : false;
			boolean repfailed2 = result.indexOf("GATEFAILED") != -1 ? true : false;
			if (!repsucces2 && !repfailed2) {
				try {
					Thread.sleep(10000);
					// TODO: tentative 3 après 10s
					Util.writeInFileTransaction(folder, file,idcommande + " Recall URL tentative 3");
					try (CloseableHttpClient httpClient = HttpClients.createDefault();
							CloseableHttpResponse response = httpClient.execute(post)) {

						result = EntityUtils.toString(response.getEntity());
					}
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							" sendPOST Exception => {} tv 3 :" + ex.getMessage() + "ex : " + Util.formatException(ex));
					result = "ko";
				}
			}
		}

		return result;
	}
	
	@SuppressWarnings("all")
	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"}) // Suppression intentionnelle : La validation des certificats est désactivée par choix
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"}) // Suppression intentionnelle : La validation des certificats est désactivée par choix
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		// TODO: modified 2024-0-30
		// TODO: SSLContext context = SSLContext.getInstance("SSL");
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, trustAllCerts, null);

		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		builder.setSSLSocketFactory(sslConnectionFactory);

		PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();

		return builder.build();

	}

	@SuppressWarnings("all")
	private MonthDto mapToFrenchMonth(String month) {

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

		return exp;
	}

	@SuppressWarnings("all")
	private String mapToFrenchMonth(Month month) {
		// TODO: Simple mapping from English to French month names.
		switch (month) {
		case JANUARY:
			return "Janvier";
		case FEBRUARY:
			return "Février";
		case MARCH:
			return "Mars";
		case APRIL:
			return "Avril";
		case MAY:
			return "Mai";
		case JUNE:
			return "Juin";
		case JULY:
			return "Juillet";
		case AUGUST:
			return "Août";
		case SEPTEMBER:
			return "Septembre";
		case OCTOBER:
			return "Octobre";
		case NOVEMBER:
			return "Novembre";
		case DECEMBER:
			return "Décembre";
		default:
			return ""; // TODO: Handle unknown month
		}
	}

	private boolean isReccuringCheck(String recurring) {
		return recurring.equalsIgnoreCase("Y");
	}

	private boolean checkCvvPresence(String cvv) {
		return cvv != null && cvv.length() == 3;
	}

	@SuppressWarnings("deprecation")
	private Date getDateWithoutTime(Date d) {

		if (d == null)
			d = new Date();
		Date dNotime = null;
		try {

			dNotime = new Date(d.getYear(), d.getMonth(), d.getDate());

			dNotime.setHours(0);
			dNotime.setMinutes(0);
			dNotime.setSeconds(0);

		} catch (Exception e) {
			return d;
		}
		return dNotime;

	}

	@SuppressWarnings("all")
	public Cartes fromString(String input) {
		Cartes cartes = new Cartes();

		String[] keyValuePairs = input.substring(0, input.length()).split(", ");

		for (String pair : keyValuePairs) {
			String[] keyValue = pair.split("=");

			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();

				switch (key) {
				case "carte":
					cartes.setCarte(value);
					break;
				case "pcidsscarte":
					cartes.setPcidsscarte(value);
					break;
				case "year":
					cartes.setYear(Integer.parseInt(value));
					break;
				case "mois":
					cartes.setMois(value);
					break;
				case "moisValue":
					cartes.setMoisValue(value);
					break;
				// TODO: Handle other properties as needed
				}
			}
		}

		return cartes;
	}

}
