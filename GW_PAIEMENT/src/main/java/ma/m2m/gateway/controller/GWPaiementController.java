package ma.m2m.gateway.controller;

import static ma.m2m.gateway.Utils.StringUtils.isNullOrEmpty;
import static ma.m2m.gateway.config.FlagActivation.ACTIVE;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.XXHash32;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.dto.ArticleDGIDto;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.Cartes;
import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.dto.FactureLDDto;
import ma.m2m.gateway.dto.MonthDto;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.dto.UserDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.lydec.DemandesReglements;
import ma.m2m.gateway.lydec.GererEncaissement;
import ma.m2m.gateway.lydec.GererEncaissementService;
import ma.m2m.gateway.lydec.Impaye;
import ma.m2m.gateway.lydec.MoyenPayement;
import ma.m2m.gateway.lydec.Portefeuille;
import ma.m2m.gateway.lydec.ReponseReglements;
import ma.m2m.gateway.lydec.Transaction;
import ma.m2m.gateway.model.FactureLD;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.risk.GWRiskAnalysis;
import ma.m2m.gateway.service.ArticleDGIService;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.ControlRiskCmrService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.EmetteurService;
import ma.m2m.gateway.service.FactureLDService;
import ma.m2m.gateway.service.GalerieService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.service.TelecollecteService;
import ma.m2m.gateway.service.TransactionService;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Value("${key.JWT_TOKEN_VALIDITY}")
	private long jwt_token_validity;

	@Value("${key.URL_WSDL_LYDEC}")
	private String URL_WSDL_LYDEC;

	@Value("${key.LYDEC_PREPROD}")
	private String LYDEC_PREPROD;

	@Value("${key.LYDEC_PROD}")
	private String LYDEC_PROD;
	
	@Value("${key.DGI_PREPROD}")
	private String DGI_PREPROD;

	@Value("${key.DGI_PROD}")
	private String DGI_PROD;
	
	@Value("${key.ENVIRONEMENT}")
	private String environement;
	
	@Value("${key.TIMEOUT}")
	private int timeout;

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

	@Autowired
	CardtokenService cardtokenService;

	@Autowired
	private ControlRiskCmrService controlRiskCmrService;

	@Autowired
	private EmetteurService emetteurService;

	@Autowired
	CodeReponseService codeReponseService;

	@Autowired
	FactureLDService factureLDService;

	@Autowired
	ArticleDGIService articleDGIService;

	private LocalDateTime date;
	private String folder;
	private String file;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd");

	private static final QName SERVICE_NAME = new QName("http://service.lydec.com", "GererEncaissementService");

	public GWPaiementController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "GW_" + randomWithSplittableRandom;
		// date of folder logs
		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
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

	@RequestMapping(path = "/connectLydec")
	@ResponseBody
	public String connectLydec() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start connectLydec() ************** ");
		System.out.println("*********** Start connectLydec() ************** ");

		String msg = "Test connectLydec !!! ";

		try {
			// URL wsdlURL = GererEncaissementService.WSDL_LOCATION;
			URL wsdlURL = new URL(URL_WSDL_LYDEC);
			Util.writeInFileTransaction(folder, file, "wsdlURL : " + wsdlURL);

			GererEncaissementService ss = new GererEncaissementService(wsdlURL, SERVICE_NAME);
			GererEncaissement port = ss.getGererEncaissement();

			ReponseReglements reponseReglement = null;
			DemandesReglements demReglement = new DemandesReglements();
			demReglement.setAgc_Cod((short) 840);
			MoyenPayement[] listeMoyensPayement = new MoyenPayement[1];
			MoyenPayement ecr = new MoyenPayement();
			List<Impaye> factListImpayes = new ArrayList<Impaye>();
			BigDecimal montant = new BigDecimal(0);
			BigDecimal montantTimbre = new BigDecimal(0);
			BigDecimal montantTotalSansTimbre = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
			java.util.Calendar date_pai = Calendar.getInstance();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ecr.setType_Moy_Pai("C");
			ecr.setBanq_Cod("NPS");

			ecr.setDate_Pai(date_pai);
			ecr.setMontant(montantTimbre.add(new BigDecimal(100).setScale(2, BigDecimal.ROUND_HALF_UP)));
			ecr.setMoyen_Pai("974772");
			listeMoyensPayement[0] = ecr;
			Transaction transaction = new Transaction();
			transaction.setAgc_Cod((short) 840);
			transaction.setDate_Trans(listeMoyensPayement[0].getDate_Pai());
			transaction.setDate_Val(new Date());
			transaction.setEtat_Trans("R");
			transaction.setType_Trans("RX");

			List<FactureLDDto> listFactureLD = new ArrayList<>();
			Util.writeInFileTransaction(folder, file, "findFactureByIddemande : " + 196884);
			listFactureLD = factureLDService.findFactureByIddemande(197267);
			Util.writeInFileTransaction(folder, file,
					"preparerReglementLydec listFactureLD.size  : " + listFactureLD.size());
			for (FactureLDDto facLD : listFactureLD) {
				// log.info("preparerReglementLydec facLD : " + facLD.toString());
				Impaye imp = new Impaye();
				imp.setNumeroFacture(Integer.valueOf(facLD.getNumfacture()));
				if (facLD.getNumligne() != null) {
					imp.setNumeroLigne(Integer.valueOf(facLD.getNumligne()));
				} else {
					imp.setNumeroLigne(0);
				}
				imp.setCodeFourniture(facLD.getFourniture());
				imp.setNumeroPolice(facLD.getNumPolice());
				imp.setProduit(Integer.valueOf(facLD.getProduit()));
				imp.setMontantTTC(new BigDecimal(facLD.getMontantTtc()).setScale(2, BigDecimal.ROUND_HALF_UP));
				imp.setMontantTimbre(new BigDecimal(facLD.getMontantTbr()).setScale(2, BigDecimal.ROUND_HALF_UP));
				imp.setMontantTVA(new BigDecimal(facLD.getMontantTva()).setScale(2, BigDecimal.ROUND_HALF_UP));
				montant = montant.add(new BigDecimal(facLD.getMontantTtc()).setScale(2, BigDecimal.ROUND_HALF_UP));
				montantTimbre = montantTimbre
						.add(new BigDecimal(facLD.getMontantTbr()).setScale(2, BigDecimal.ROUND_HALF_UP));
				Util.writeInFileTransaction(folder, file, "preparerReglementLydec imp  : " + imp.toString());
				factListImpayes.add(imp);
			}
			Portefeuille[] listePortefeuilles = preparerTabEcritureLydecListe(factListImpayes);

			demReglement.setTransaction(transaction);
			demReglement.setListeMoyensPayement(listeMoyensPayement);
			demReglement.setListePortefeuilles(listePortefeuilles);
			Util.writeInFileTransaction(folder, file,
					"preparerReglementLydec demReglement : " + demReglement.toString());
			reponseReglement = port.ecrireReglements(demReglement);

			if (reponseReglement != null) {
				System.out.println("reponseReglement isOk/message : " + reponseReglement.isOk() + "/"
						+ reponseReglement.getMessage());
				Util.writeInFileTransaction(folder, file,
						"isOk/message : " + reponseReglement.isOk() + "/" + reponseReglement.getMessage());
				msg = msg + "reponseReglement isOk/message : " + reponseReglement.isOk() + "/"
						+ reponseReglement.getMessage();
			} else {
				System.out.println("reponseReglement : " + null);
				Util.writeInFileTransaction(folder, file, "reponseReglement : " + null);
				msg = "Test connectLydec !!! failed : reponseReglement null";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Util.writeInFileTransaction(folder, file, "Exception : " + ex);
			msg = "Test connectLydec !!! failed";
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin connectLydec () ************** ");
		System.out.println("*********** Fin connectLydec () ************** ");

		return msg;
	}

	@RequestMapping(path = "/")
	@ResponseBody
	public String home() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start home() ************** ");
		System.out.println("*********** Start home() ************** ");

		String msg = "Bienvenue dans la plateforme de paiement NAPS !!!";

		Util.writeInFileTransaction(folder, file, "*********** Fin home () ************** ");
		System.out.println("*********** Fin home () ************** ");
        
		return msg;
	}
	
	@RequestMapping(path = "/apis")
	@ResponseBody
	public String apis() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start apis() ************** ");
		System.out.println("*********** Start apis() ************** ");

		String msg = "Bienvenue dans la plateforme de paiement NAPS !!!";

		Util.writeInFileTransaction(folder, file, "*********** Fin apis () ************** ");
		System.out.println("*********** Fin apis () ************** ");

		String cvvNumeric = "561"; // CVV numérique
		String cvvAlphabetic = "FGB"; // CVV alphabetique
        String cvv_convert_alpha = Util.convertCVVNumericToAlphabetic(cvvNumeric);
        String cvv_convert_numeric = Util.convertCVVAlphabeticToNumeric(cvvAlphabetic);
        System.out.println("CVV numeric (561) converti alphabetic : " + cvv_convert_alpha); 
        System.out.println("CVV alphabetic (FGB) converti numeric : " + cvv_convert_numeric);
        Util.writeInFileTransaction(folder, file, "CVV numeric (561) converti alphabetic : " + cvv_convert_alpha); 
        Util.writeInFileTransaction(folder, file, "CVV alphabetic (FGB) converti numeric : " + cvv_convert_numeric);
        if(cvvNumeric.equals(cvv_convert_numeric)) {
        	System.out.println("CVV numeric converti alphabetic OK [" + cvvNumeric +"=" +cvv_convert_numeric+"]"); 
        	Util.writeInFileTransaction(folder, file, "CVV numeric converti alphabetic OK [" + cvvNumeric +"=" +cvv_convert_numeric+"]");
        }
        if(cvvAlphabetic.equals(cvv_convert_alpha)) {
        	System.out.println("CVV alphabetic converti numeric OK [" + cvvAlphabetic +"=" +cvv_convert_alpha+"]"); 
        	Util.writeInFileTransaction(folder, file, "CVV alphabetic converti numeric OK [" + cvvAlphabetic +"=" +cvv_convert_alpha+"]"); 
        }
        //TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N("1180092");
        //System.out.println("n_tlc [" + n_tlc +"]"); 
        //Util.writeInFileTransaction(folder, file, "n_tlc [" + n_tlc +"]"); 
        
        /*try {
            HistoAutoGateDto histToAnnulle = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr("120uytmps542256", "123456");
            System.out.println("histToAnnulle [" + histToAnnulle +"]"); 
        } catch(Exception ex) {
        	System.out.println("ex [" + ex +"]"); 
        }*/
        
        /*try {       	
        	HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep("120uytmps542256","123456", "123456", "00");
        	System.out.println("histToAnnulle [" + histToAnnulle +"]"); 
		} catch(Exception ex) {
		    System.out.println("ex [" + ex +"]"); 
		}
        
        long idtelc = telecollecteService.getMAX_ID("123456");
        System.out.println("idtelc [" + idtelc +"]");*/ 
        
        /*HistoAutoGateDto hist = new HistoAutoGateDto();
        hist.setHatNumCommande("120uytmps542257");
        hist = histoAutoGateService.save(hist);
        System.out.println("hist [" + hist +"]");*/
        
		return msg;
	}
	
	@GetMapping("/redirect-to-acs")
    public void redirectToAcs(HttpServletResponse response) throws IOException {
        // La réponse 3DS fournie dans votre exemple
        String response3DS = "<form action='https://acs2.sgmaroc.com:443/lacs2' method='post' " +
                             "enctype='application/x-www-form-urlencoded'><input type='hidden' name='creq' " +
                             "value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjBlYmU1ODEwLTlhMDMtNGYzZi05MDgzLTJlZWNhNjhiMjY2YSIsCiAgImFjc1RyYW5zSUQiOiAiMmM5MjAxNDgtNjhiOC00ZjA0LWJhODQtY2RiYTFlOTM5MDM3IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' /></form>";

        String creq = "";
        String acsUrl = "";
        Pattern pattern = Pattern.compile("action='(.*?)'.*value='(.*?)'");
        Matcher matcher = pattern.matcher(response3DS);

        // Si une correspondance est trouvée
        if (matcher.find()) {
            acsUrl = matcher.group(1);
            creq = matcher.group(2);
            System.out.println("L'URL ACS est : " + acsUrl);
            System.out.println("La valeur de creq est : " + creq);
        } else {
            System.out.println("Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
        }
        // Afficher le formulaire HTML dans la réponse
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<form id=\"acsForm\" action=\"" + acsUrl + "\" method=\"post\">");
        response.getWriter().println("<input type=\"hidden\" name=\"creq\" value=\"" + creq + "\">");
        response.getWriter().println("</form>");
        response.getWriter().println("<script>document.getElementById('acsForm').submit();</script>");
        response.getWriter().println("</body></html>");
    }
	
	@PostMapping("/redirectACS")
    public String redirectToAcsV1(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto,
    		HttpServletResponse response) throws IOException {
        // La réponse 3DS fournie dans votre exemple
        String response3DS = "<form action='https://acs2.sgmaroc.com:443/lacs2' method='post' " +
                             "enctype='application/x-www-form-urlencoded'><input type='hidden' name='creq' " +
                             "value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjBlYmU1ODEwLTlhMDMtNGYzZi05MDgzLTJlZWNhNjhiMjY2YSIsCiAgImFjc1RyYW5zSUQiOiAiMmM5MjAxNDgtNjhiOC00ZjA0LWJhODQtY2RiYTFlOTM5MDM3IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' /></form>";
        
        String res3DS="<form  action='https://acsprod.cihbank.ma:443/acsauthentication/auth/customerChallenge.jsf'"
        		+ "method='post' enctype='application/x-www-form-urlencoded'><input type='hidden' name='creq' "
        		+ "value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogImU0NmQ4YTcwLTgwYTYtNDEyOC1hOTRlLWIyYTFmZTliODI5NCIsCiAgImFjc1RyYW5zSUQiOiAiNWMwOGRlNGUtMzljYy00MmFkLWE1NDEtZWQ3NjFkZDdiZjU3IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' /></form>";
        
        String creq = "";
        String acsUrl = "";
        Pattern pattern = Pattern.compile("action='(.*?)'.*value='(.*?)'");
        Matcher matcher = pattern.matcher(response3DS);

        // Si une correspondance est trouvée
        if (matcher.find()) {
            acsUrl = matcher.group(1);
            creq = matcher.group(2);
            System.out.println("L'URL ACS est : " + acsUrl);
            System.out.println("La valeur de creq est : " + creq);
        } else {
            System.out.println("Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
            response.sendRedirect("");
        }
        // Afficher le formulaire HTML dans la réponse
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<form id=\"acsForm\" action=\"" + acsUrl + "\" method=\"post\">");
        response.getWriter().println("<input type=\"hidden\" name=\"creq\" value=\"" + creq + "\">");
        response.getWriter().println("</form>");
        response.getWriter().println("<script>document.getElementById('acsForm').submit();</script>");
        response.getWriter().println("</body></html>");
        return "chalenge";
    }

	@RequestMapping(path = "/napspayment/generatetoken")
	@ResponseBody
	public ResponseEntity<String> generateToken() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start generateToken() ************** ");
		System.out.println("*********** Start generateToken() ************** ");

		// pour tester la generation du tocken
		JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
		String msg = "";
		JSONObject jso = new JSONObject();
		try {
			// test par jwt_token_validity configuree
			String token = jwtTokenUtil.generateToken(usernameToken, secret, jwt_token_validity);

			// verification expiration token
			jso = verifieToken(token);

			if (jso != null && !jso.get("statuscode").equals("00")) {
				Util.writeInFileTransaction(folder, file, "jsoVerified : " + jso.toString());
				System.out.println("jsoVerified : " + jso.toString());
				msg = "echec lors de la génération du token";
				Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");
				System.out.println("*********** Fin generateToken() ************** ");
			} else {
				msg = "the token successfully generated";
			}

		} catch (Exception ex) {
			msg = "the token generation failed";
		}

		// fin
		Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");
		System.out.println("*********** Fin generateToken() ************** ");

		return ResponseEntity.ok().body(msg);
	}

	public JSONObject verifieToken(String securtoken24) {
		// Traces traces = new Traces();
		JSONObject jso = new JSONObject();

		if (!securtoken24.equals("")) {
			JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
			String token = securtoken24;
			try {
				String userFromToken = jwtTokenUtil.getUsernameFromToken(token, secret);
				Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token, secret);
				Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token, secret);

				Util.writeInFileTransaction(folder, file, "userFromToken generated : " + userFromToken);
				String dateSysStr = dateFormat.format(new Date());
				Util.writeInFileTransaction(folder, file, "dateSysStr : " + dateSysStr);
				Util.writeInFileTransaction(folder, file, "dateExpiration : " + dateExpiration);
				String dateExpirationStr = dateFormat.format(dateExpiration);
				Util.writeInFileTransaction(folder, file, "dateExpirationStr : " + dateExpirationStr);
				String condition = isTokenExpired == false ? "NO" : "YES";
				Util.writeInFileTransaction(folder, file, "token is expired : " + condition);
				if (condition.equalsIgnoreCase("YES")) {
					Util.writeInFileTransaction(folder, file, "Error 500 securtoken24 is expired");

					// Transaction info
					jso.put("statuscode", "17");
					jso.put("status", "Error 500 securtoken24 is expired");

					return jso;
				} else {
					jso.put("statuscode", "00");
				}
			} catch (Exception ex) {
				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 securtoken24 " + ex.getMessage());
				System.out.println(jso.get("status"));

				return jso;
			}
		}
		return jso;
	}

	@RequestMapping(path = "/napspayment/generateexcel")
	@ResponseBody
	public String generateExcel() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start generateExcel() ************** ");
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
	public void exportToExcel(HttpServletResponse response, @PathVariable(value = "merchantid") String merchantid)
			throws IOException {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start exportToExcel ***********");
		System.out.println("*********** Start exportToExcel ***********");

		Util.writeInFileTransaction(folder, file, "findByHatNumcmr merchantid : " + merchantid);
		System.out.println("findByHatNumcmr merchantid : " + merchantid);

		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=HistoriqueTrs_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		try {

			// List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findAll();
			List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findByHatNumcmr(merchantid);

			GenerateExcel excelExporter = new GenerateExcel(listHistoGate);

			excelExporter.export(response);

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "exportToExcel 500 merchantid:[" + merchantid + "]");
			Util.writeInFileTransaction(folder, file, "exportToExcel 500 exception" + e);
			e.printStackTrace();
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin exportToExcel ***********");
		System.out.println("*********** Fin exportToExcel ***********");
	}
	
	@RequestMapping(value = "/napspayment/{numCompte}", method = RequestMethod.GET)
	@ResponseBody
	public String getRibByNumCompte(@PathVariable(value = "numCompte") String numCompte) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "RIB_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start getRibByNumCompte ***********");
		System.out.println("*********** Start getRibByNumCompte ***********");
		
		String rib = constructRIB(numCompte);
		Util.writeInFileTransaction(folder, file, "rib : " + rib);
		System.out.println("rib : " + rib);
		
		Util.writeInFileTransaction(folder, file, "*********** Fin getRibByNumCompte ***********");
		System.out.println("*********** Fin getRibByNumCompte ***********");
		 
		return rib;
	}
	
	//@PostMapping("/napspayment/uploadExcel")
	//@ResponseBody
	@RequestMapping(value = "/napspayment/uploadExcel", method = RequestMethod.POST)
	@ResponseBody
    public String handleFileUpload(HttpServletResponse response, @RequestParam("file") MultipartFile file) throws FileNotFoundException, IOException {
        if (file.isEmpty()) {
            return "Le fichier est vide";
        }
    	List<String> updatedDataList = new ArrayList<>();
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
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
        	System.out.println("Exception Erreur lors de la lecture du fichier Excel : ");
            //ex.getMessage();
        }    
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Rib_Etudiants_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		try {
			 // Filtrer les lignes vides de updatedDataList
		    updatedDataList.removeIf(String::isEmpty);
			GenerateExcel excelExporter = new GenerateExcel(updatedDataList, "xx");
			excelExporter.exportRIB(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Fichier Excel traité généré avec succès ";
    }
	
	// Méthode pour construire le RIB à partir du NUMCOmpte
    private String constructRIB(String NUMCOmpte) {
    	String CODE_NAPS = "842";
    	String CODE_VILLE = "780";
    	String SEPARATEUR_ESPACE = "    ";
    	String rib1 = CODE_NAPS + CODE_VILLE + NUMCOmpte;
		BigInteger cle = calculCle(rib1);

		String cleStr = cle.toString();
		if(cleStr.length() == 1) {
			cleStr = "0"+cleStr;
		}
		String rib = CODE_NAPS + SEPARATEUR_ESPACE + CODE_VILLE
				+ SEPARATEUR_ESPACE + NUMCOmpte + SEPARATEUR_ESPACE + cleStr;
		
        return rib;
    }
    
	public BigInteger calculCle(String RIB) {
		BigInteger cle = new BigInteger("0");
		try {
			BigInteger rib = new BigInteger(RIB);
			BigInteger v100 = BigInteger.valueOf(100);
			BigInteger v97 = BigInteger.valueOf(97);
			BigInteger rib100 = rib.multiply(v100);
			BigInteger mod = rib100.mod(v97);
			cle = v97.subtract(mod);
		} catch(Exception ex) {
			//ex.printStackTrace();
		}
		return cle;
	}
	
	@RequestMapping("/napspayment/index")
	public String index() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "return to index.html");
		System.out.println("return to index.html");

		return "index";
	}

	@RequestMapping(value = "/napspayment/authorization/token/{token}", method = RequestMethod.GET)
	public String showPagePayment(@PathVariable(value = "token") String token, Model model, HttpSession session) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAGE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start affichage page ***********");
		System.out.println("*********** Start affichage page ***********");

		Util.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspayment";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				System.out.println("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/"
						+ demandeDto.getCommande());
				Util.writeInFileTransaction(folder, file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

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
				// get cardnumber by idclient
				if(demandeDto.getToken() != null) {
					if(demandeDto.getToken().equals("") || demandeDto.getToken().equals(" ")) {
						demandeDto.setToken(null);
					}
				}
				if(demandeDto.getId_client() != null) {
					if(demandeDto.getId_client().equals("") || demandeDto.getId_client().equals(" ")) {
						demandeDto.setId_client(null);
					}
				}
				String idclient = demandeDto.getId_client();
				if (idclient == null) {
					idclient = "";
				}
				merchantid = demandeDto.getComid();
				// merchantid = "";
				String cardnumber = "";
				List<Cartes> cartes = new ArrayList<>();
				if (!idclient.equals("") && idclient != null && !idclient.equals("null")) {
					System.out.println("idclient/merchantid : " + idclient + "/" + merchantid);
					try {
						List<CardtokenDto> cards = new ArrayList<>();
						cards = cardtokenService.findByIdMerchantAndIdMerchantClient(merchantid, idclient);
						if (cards != null && cards.size() > 0) {
							for (CardtokenDto card : cards) {
								if (card.getCardNumber() != null) {
									Cartes carte = new Cartes();
									cardnumber = card.getCardNumber();
									carte.setCarte(cardnumber);
									carte.setPcidsscarte(Util.formatCard(cardnumber));
									String dateExStr = dateFormatSimple.format(card.getExprDate());
									formatDateExp(dateExStr, carte);
									cartes.add(carte);
								}
							}
							System.out.println("Cartes : " + cartes.toString());
							demandeDto.setCartes(cartes);
						} else {
							demandeDto.setCartes(null);
						}
					} catch (Exception ex) {
						Util.writeInFileTransaction(folder, file, "showPagePayment 500 idclient not found" + ex);
					}
				} else {
					demandeDto.setCartes(null);
				}
				// Créez un objet DecimalFormat avec le modèle "0.00"
				DecimalFormat df = new DecimalFormat("0.00");

				// Formatez le nombre en une chaîne avec deux chiffres après la virgule
				Double mont = demandeDto.getMontant();
				String mtFormate = df.format(mont);
				if (mtFormate.contains(",")) {
					mtFormate = mtFormate.replace(",", ".");
				}

				demandeDto.setMontantStr(mtFormate);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
					Util.writeInFileTransaction(folder, file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtat_demande().equals("SW_REJET")) {
					Util.writeInFileTransaction(folder, file, "Transaction rejetée");
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
						Util.writeInFileTransaction(folder, file,
								"showPagePayment 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						galerie = galerieService.findByCodeCmr(merchantid);
						if (galerie != null) {
							demandeDto.setGalerieDto(galerie);
						}
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"showPagePayment 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Galerie mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
				}
			} else {
				Util.writeInFileTransaction(folder, file, "demandeDto not found token : " + token);
				System.out.println("demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPagePayment 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token + "]"
							+ e);

			Util.writeInFileTransaction(folder, file, "showPagePayment 500 exception" + e);
			e.printStackTrace();
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}
		
		// gestion expiration de la session on stoque la date en millisecond
	    session.setAttribute("paymentStartTime", System.currentTimeMillis());
	    Util.writeInFileTransaction(folder, file, "paymentStartTime : " + System.currentTimeMillis());
	    demandeDto.setTimeoutURL(String.valueOf(System.currentTimeMillis()));
	    
		if (page.equals("napspayment")) {
			if(demandeDto.getEtat_demande().equals("INIT")) {
				demandeDto.setEtat_demande("P_CHRG_OK");
				demandePaiementService.save(demandeDto);
				System.out.println("update Demandepaiement status to P_CHRG_OK");
				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to P_CHRG_OK");
			}
			if (demandeDto.getComid().equals(LYDEC_PREPROD) || demandeDto.getComid().equals(LYDEC_PROD)) {
				System.out.println("Si le commercant est LYDEC : " + demandeDto.getComid());
				Util.writeInFileTransaction(folder, file, "Si le commercant est LYDEC : " + demandeDto.getComid());
				List<FactureLDDto> listFactureLD = new ArrayList<>();
				listFactureLD = factureLDService.findFactureByIddemande(demandeDto.getIddemande());
				if (listFactureLD != null && listFactureLD.size() > 0) {
					System.out.println("listFactureLD : " + listFactureLD.size());
					Util.writeInFileTransaction(folder, file, "listFactureLD : " + listFactureLD.size());
					demandeDto.setFactures(listFactureLD);
				} else {
					System.out.println("listFactureLD vide ");
					Util.writeInFileTransaction(folder, file, "listFactureLD vide ");
					demandeDto.setFactures(null);
				}
				model.addAttribute("demandeDto", demandeDto);
				page = "napspaymentlydec";
			}
			if (demandeDto.getComid().equals(DGI_PREPROD) || demandeDto.getComid().equals(DGI_PROD)) {
				System.out.println("Si le commercant est DGI : " + demandeDto.getComid());
				Util.writeInFileTransaction(folder, file, "Si le commercant est DGI : " + demandeDto.getComid());
				List<ArticleDGIDto> articles = new ArrayList<>();
				articles = articleDGIService.findArticleByIddemande(demandeDto.getIddemande());
				if (articles != null && articles.size() > 0) {
					System.out.println("articles : " + articles.size());
					Util.writeInFileTransaction(folder, file, "articles : " + articles.size());
					demandeDto.setArticles(articles);
				} else {
					System.out.println("articles vide ");
					Util.writeInFileTransaction(folder, file, "articles vide ");
					demandeDto.setArticles(null);
				}
				model.addAttribute("demandeDto", demandeDto);
				page = "napspaymentdgi";
			}
			
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin affichage page ************** ");
		System.out.println("*********** Fin affichage page ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/authorization/lydec/token/{token}", method = RequestMethod.GET)
	public String showPagePaymentLydec(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAGE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start affichage page ***********");
		System.out.println("*********** Start affichage page ***********");

		Util.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspaymentlydec";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				System.out.println("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/"
						+ demandeDto.getCommande());
				Util.writeInFileTransaction(folder, file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

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
				// Créez un objet DecimalFormat avec le modèle "0.00"
				DecimalFormat df = new DecimalFormat("0.00");

				// Formatez le nombre en une chaîne avec deux chiffres après la virgule
				Double mont = demandeDto.getMontant();
				String mtFormate = df.format(mont);
				if (mtFormate.contains(",")) {
					mtFormate = mtFormate.replace(",", ".");
				}

				demandeDto.setMontantStr(mtFormate);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
					Util.writeInFileTransaction(folder, file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtat_demande().equals("SW_REJET")) {
					Util.writeInFileTransaction(folder, file, "Transaction rejetée");
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
						Util.writeInFileTransaction(folder, file,
								"showPagePaymentLydec 500 Merchant misconfigured in DB or not existing orderid:["
										+ orderid + "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						galerie = galerieService.findByCodeCmr(merchantid);
						if (galerie != null) {
							demandeDto.setGalerieDto(galerie);
						}
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"showPagePaymentLydec 500 Galerie misconfigured in DB or not existing orderid:["
										+ orderid + "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Galerie mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
				}
			} else {
				Util.writeInFileTransaction(folder, file, "demandeDto not found token : " + token);
				System.out.println("demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPagePaymentLydec 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token
							+ "]" + e);

			Util.writeInFileTransaction(folder, file, "showPagePaymentLdec 500 exception" + e);
			e.printStackTrace();
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}

		if (page.equals("napspaymentlydec")) {
			demandeDto.setEtat_demande("P_CHRG_OK");
			demandePaiementService.save(demandeDto);
			System.out.println("update Demandepaiement status to P_CHRG_OK");
			Util.writeInFileTransaction(folder, file, "update Demandepaiement status to P_CHRG_OK");

			List<FactureLDDto> listFactureLD = new ArrayList<>();
			listFactureLD = factureLDService.findFactureByIddemande(73);
			if (listFactureLD != null && listFactureLD.size() > 0) {
				System.out.println("listFactureLD : " + listFactureLD.size());
				demandeDto.setFactures(listFactureLD);
			} else {
				demandeDto.setFactures(null);
			}
			model.addAttribute("demandeDto", demandeDto);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin affichage page ************** ");
		System.out.println("*********** Fin affichage page ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/authorization/dgi/token/{token}", method = RequestMethod.GET)
	public String showPagePaymentDGI(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAGE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start affichage page ***********");
		System.out.println("*********** Start affichage page ***********");

		Util.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "napspaymentdgi";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				System.out.println("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/"
						+ demandeDto.getCommande());
				Util.writeInFileTransaction(folder, file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

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
				// Créez un objet DecimalFormat avec le modèle "0.00"
				DecimalFormat df = new DecimalFormat("0.00");

				// Formatez le nombre en une chaîne avec deux chiffres après la virgule
				Double mont = demandeDto.getMontant();
				String mtFormate = df.format(mont);
				if (mtFormate.contains(",")) {
					mtFormate = mtFormate.replace(",", ".");
				}

				demandeDto.setMontantStr(mtFormate);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
					Util.writeInFileTransaction(folder, file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtat_demande().equals("SW_REJET")) {
					Util.writeInFileTransaction(folder, file, "Transaction rejetée");
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
						Util.writeInFileTransaction(folder, file,
								"showPagePaymentDGI 500 Merchant misconfigured in DB or not existing orderid:["
										+ orderid + "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						galerie = galerieService.findByCodeCmr(merchantid);
						if (galerie != null) {
							demandeDto.setGalerieDto(galerie);
						}
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"showPagePaymentDGI 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("Galerie mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
				}
			} else {
				Util.writeInFileTransaction(folder, file, "demandeDto not found token : " + token);
				System.out.println("demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPagePaymentDGILdec 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token
							+ "]" + e);

			Util.writeInFileTransaction(folder, file, "showPagePaymentDGILdec 500 exception" + e);
			e.printStackTrace();
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}

		if (page.equals("napspaymentdgi")) {
			demandeDto.setEtat_demande("P_CHRG_OK");
			demandePaiementService.save(demandeDto);
			System.out.println("update Demandepaiement status to P_CHRG_OK");
			Util.writeInFileTransaction(folder, file, "update Demandepaiement status to P_CHRG_OK");

			List<ArticleDGIDto> articles = new ArrayList<>();
			articles = articleDGIService.findArticleByIddemande(demandeDto.getIddemande());
			if (articles != null && articles.size() > 0) {
				System.out.println("articles : " + articles.size());
				demandeDto.setArticles(articles);
			} else {
				demandeDto.setArticles(null);
			}
			model.addAttribute("demandeDto", demandeDto);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin affichage page ************** ");
		System.out.println("*********** Fin affichage page ************** ");

		return page;
	}

	@PostMapping("/payer")
	public String payer(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto, HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_PAYE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start payer () ************** ");
		System.out.println("*********** Start payer () ************** ");

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", successURL, failURL,
				transactiontype, idclient;

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		Objects.copyProperties(demandeDto, dto);
		System.out.println("Commande : " + demandeDto.getCommande());
		Util.writeInFileTransaction(folder, file, "Commande : " + dto.getCommande());
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
			// Transaction info
			orderid = demandeDto.getCommande();
			if (demandeDto.getMontant() == null) {
				demandeDto.setMontant(0.00);
			}
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
			cardnumber = "";
			expirydate = "";
			callbackUrl = demandeDto.getCallbackURL();
			successURL = demandeDto.getSuccessURL();
			failURL = demandeDto.getFailURL();

			// Card info
			// if transaction not cof
			if (demandeDto.getDem_pan() != null && !demandeDto.getDem_pan().equals("")) {
				cardnumber = demandeDto.getDem_pan();
				expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois().substring(0, 2));
			}
			// if transaction cof
			if (demandeDto.getInfoCarte() != null && !demandeDto.isFlagNvCarte()
					&& (demandeDto.getDem_pan() == null || demandeDto.getDem_pan().equals(""))) {
				String infoCard = demandeDto.getInfoCarte().substring(8, demandeDto.getInfoCarte().length());
				Cartes carteFormated = fromString(infoCard);
				demandeDto.setCarte(carteFormated);
				cardnumber = demandeDto.getCarte().getCarte();
				String annee = String.valueOf(demandeDto.getCarte().getYear());
				expirydate = annee.substring(2, 4).concat(demandeDto.getCarte().getMoisValue());
			}
			if (demandeDto.getDem_pan().equals("") && demandeDto.getInfoCarte() != null) {
				if(!demandeDto.getAnnee().equals("") && !demandeDto.getMois().equals("")) {
					expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois().substring(0, 2));
				}
			}
			flagNvCarte = demandeDto.isFlagNvCarte();
			flagSaveCarte = demandeDto.isFlagSaveCarte();
			if (cardnumber.contains(",")) {
				cardnumber = cardnumber.replace(",", "");
			}
			// cardnumber = demandeDto.getDem_pan();
			token = "";
			// expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois());
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
			Util.writeInFileTransaction(folder, file, "payer 500 malformed json expression" + jerr);
			demandeDtoMsg.setMsgRefus("données mal formées");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "payer 500 Merchant misconfigured in DB or not existing orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + e);
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file, "payer 500 Merchant misconfigured in DB or not existing orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file, "payer 500 Merchant misconfigured in DB or not existing orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file, "payer 500 Merchant misconfigured in DB or not existing orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		InfoCommercantDto current_infoCommercant = null;

		try {
			current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"payer 500 InfoCommercant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);

			demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_infoCommercant == null) {
			Util.writeInFileTransaction(folder, file,
					"payer 500 InfoCommercantDto misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");

			demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			Util.writeInFileTransaction(folder, file, "payer 500 Card number length is incorrect orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("La longueur du numéro de la carte est incorrecte");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (i_card_valid == 2) {
			Util.writeInFileTransaction(folder, file,
					"payer 500 Card number  is not valid incorrect luhn check orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Le numéro de la carte est invalide");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		int i_card_type = Util.getCardIss(cardnumber);

		try {
			DemandePaiementDto dmdToEdit = demandePaiementService.findByIdDemande(demandeDto.getIddemande());

			dmdToEdit.setDem_pan(cardnumber);
			dmdToEdit.setDem_cvv(cvv);
			dmdToEdit.setType_carte(i_card_type + "");
			dmdToEdit.setTransactiontype(transactiontype);
			int nbr_tv = dmdToEdit.getNbreTenta() + 1;
			dmdToEdit.setNbreTenta(nbr_tv);

			formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			dmdToEdit.setDem_date_time(dateFormat.format(new Date()));

			demandeDto = demandePaiementService.save(dmdToEdit);
			demandeDto.setExpery(expirydate);
			demandeDto.setFlagNvCarte(flagNvCarte);
			demandeDto.setFlagSaveCarte(flagSaveCarte);
			idclient = demandeDto.getId_client();
			if (idclient == null) {
				idclient = "";
			}

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"payer 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + err1);
			demandeDtoMsg.setMsgRefus("Erreur lors de l'insertion DEMANDE_PAIEMENT");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		// 2024-06-03
		// gestion expiration de la session on recupere la date en millisecond
		Long paymentStartTime = (Long) session.getAttribute("paymentStartTime");
		Util.writeInFileTransaction(folder, file, "paymentStartTime : " + paymentStartTime);

	    if (paymentStartTime != null) {
	        long currentTime = System.currentTimeMillis();
	        long elapsedTime = currentTime - paymentStartTime;
	        Util.writeInFileTransaction(folder, file, "currentTime : " + currentTime);
	        Util.writeInFileTransaction(folder, file, "elapsedTime : " + elapsedTime);
	        // Check if more than 5 minutes (300000 milliseconds) have passed
	        int timeoutF = timeout;
	        if (elapsedTime > timeoutF) {
				Util.writeInFileTransaction(folder, file, "Page expirée Time > 5min");
				demandeDtoMsg.setMsgRefus("Votre session de paiement a expiré. Veuillez réessayer.");
				session.setAttribute("idDemande", demandeDto.getIddemande());				
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "timeout";
				
				Util.writeInFileTransaction(folder, file, "*********** Fin payer () ************** ");
				System.out.println("*********** Fin payer () ************** ");
				
				return page;
	        }
	    }
	 // 2024-06-03
		
		if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
			demandeDto.setDem_cvv("");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file, "Opération déjà effectuée");
			demandeDto.setMsgRefus(
					"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDto);
			page = "operationEffectue";
			return page;
		}

		// for test control risk
		// refactoring code 2024-03-20
		Util.writeInFileTransaction(folder, file, "Debut controlleRisk");
		try {
			String msg = autorisationService.controlleRisk(demandeDto, folder, file);
			if (!msg.equalsIgnoreCase("OK")) {
				demandeDto.setDem_cvv("");
				demandeDto.setEtat_demande("REJET_RISK_CTRL");
				demandePaiementService.save(demandeDto);
				Util.writeInFileTransaction(folder, file, "payer 500 Error " + msg);
				demandeDto = new DemandePaiementDto();
				demandeDtoMsg.setMsgRefus(msg);
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}
		} catch (Exception e) {
			demandeDto.setDem_cvv("");
			demandeDto.setEtat_demande("REJET_RISK_CTRL");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"payer 500 ControlRiskCmr misconfigured in DB or not existing merchantid:[" + demandeDto.getComid()
							+ e);
			demandeDto = new DemandePaiementDto();
			demandeDtoMsg.setMsgRefus("Error 500 Opération rejetée: Contrôle risque");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		Util.writeInFileTransaction(folder, file, "Fin controlleRisk");
		
		// saving card if flagSaveCarte true
		if (demandeDto.isFlagSaveCarte()) {
			try {
				List<CardtokenDto> checkCardNumber = cardtokenService.findByIdMerchantClientAndCardNumber(idclient,
						cardnumber);
				
				CardtokenDto cardtokenDto = new CardtokenDto();
				Calendar dateCalendar = Calendar.getInstance();
				Date dateToken = dateCalendar.getTime();

				Util.writeInFileTransaction(folder, file, "cardtokenDto expirydate input : " + expirydate);
				String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
				// get year from date
				String xx = anne.substring(0, 2) + expirydate.substring(0, 2);
				String MM = expirydate.substring(2, expirydate.length());
				// format date to "yyyy-MM-dd"
				String expirydateFormated = xx + "-" + MM + "-" + "01";
				System.out.println("cardtokenDto expirydate : " + expirydateFormated);
				Util.writeInFileTransaction(folder, file,
						"cardtokenDto expirydate formated : " + expirydateFormated);
				Date dateExp;
				dateExp = dateFormatSimple.parse(expirydateFormated);

				if (checkCardNumber.size() == 0) {
					// insert new cardToken
					String tokencard = Util.generateCardToken(idclient);

					// test if token not exist in DB
					CardtokenDto checkCardToken = cardtokenService.findByIdMerchantAndToken(idclient, tokencard);

					while (checkCardToken != null) {
						tokencard = Util.generateCardToken(idclient);
						System.out.println("checkCardToken exist => generate new tokencard : " + tokencard);
						Util.writeInFileTransaction(folder, file,
								"checkCardToken exist => generate new tokencard : " + tokencard);
						checkCardToken = cardtokenService.findByIdMerchantAndToken(merchantid, tokencard);
					}
					System.out.println("tokencard : " + tokencard);
					Util.writeInFileTransaction(folder, file, "tokencard : " + tokencard);

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
					cardtokenDto.setFirst_name(fname);
					cardtokenDto.setLast_name(lname);
					cardtokenDto.setHolderName(holdername);
					cardtokenDto.setMcc(merchantid);

					CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

					Util.writeInFileTransaction(folder, file, "Saving CARDTOKEN OK");
				} else {
					Util.writeInFileTransaction(folder, file, "Carte deja enregistrée");
					for(CardtokenDto crd : checkCardNumber) {
						if(crd.getExprDate() != null) {
							if(crd.getCardNumber().equals(cardnumber)) {
								if(crd.getExprDate().before(dateToken)) {
									Util.writeInFileTransaction(folder, file, "Encienne date expiration est expirée : " + dateFormatSimple.format(crd.getExprDate()));
									Util.writeInFileTransaction(folder, file, "Update par nv date expiration saisie : "+ expirydateFormated);
									crd.setExprDate(dateExp);
									CardtokenDto cardSaved = cardtokenService.save(crd);
									System.out.println("Update CARDTOKEN OK");
									Util.writeInFileTransaction(folder, file, "Update CARDTOKEN OK");
								}
							}				
						}	
					}
				}

			} catch (ParseException e) {
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "savingcardtoken 500 Error during CARDTOKEN Saving " + e);
			}
		}
		
		try {

			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			demandeDto.setDem_cvv("");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file, "payer 500 Error during  date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err2);
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage de la date");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		// appel 3DSSecure ***********************************************************

		/** dans la preprod les tests sans 3DSS on commente l'appel 3DSS et on mj reponseMPI="Y" */
		Util.writeInFileTransaction(folder, file, "environement : " + environement);
		if(environement.equals("PREPROD")) {
			//threeDsecureResponse = autorisationService.preparerReqThree3DSS(demandeDto, folder, file);
		
			threeDsecureResponse.setReponseMPI("Y");
		} else {
			threeDsecureResponse = autorisationService.preparerReqThree3DSS(demandeDto, folder, file);
		}

		// fin 3DSSecure ***********************************************************

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
		String expiry = ""; // YYMM

		if (threeDsecureResponse.getReponseMPI() != null) {
			reponseMPI = threeDsecureResponse.getReponseMPI();
		}
		/*if (threeDsecureResponse.getIdDemande() != null) {
			idDemande = threeDsecureResponse.getIdDemande();
		}*/
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
			demandeDto.setDem_cvv("");
			demandeDto.setEtat_demande("MPI_KO");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO idDemande null : " + demandeDto.toString());
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti (MPI_KO), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			demandeDto.setDem_cvv("");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti (DemandePaiement introuvable), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("") || reponseMPI == null) {
			dmd.setDem_cvv("");
			dmd.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmd);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			Util.writeInFileTransaction(folder, file, "Response 3DS is null");
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti (MPI_KO reponseMPI null), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("Y")) {
			// ********************* Frictionless responseMPI equal Y *********************
			Util.writeInFileTransaction(folder, file,
					"********************* Cas frictionless responseMPI equal Y *********************");
			if(!threeDSServerTransID.equals("")) {
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
			}
			
			// 2024-03-05
			montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, page, model);

			merchantname = current_merchant.getCmrNom();
			websiteName = "";
			websiteid = dmd.getGalid();
			String url = "", status = "", statuscode = "";

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(merchantname, 19, ' ');
			Util.writeInFileTransaction(folder, file, "merchant_name : [" + merchant_name + "]");

			merchant_city = "MOROCCO        ";
			Util.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

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
				Util.writeInFileTransaction(folder, file, "cavv == null || eci == null");
			} else if (cavv != null && eci != null) {
				champ_cavv = cavv + eci;
				Util.writeInFileTransaction(folder, file, "cavv != null && eci != null");
				Util.writeInFileTransaction(folder, file, "champ_cavv : [" + champ_cavv + "]");
			} else {
				Util.writeInFileTransaction(folder, file, "champ_cavv = null");
				champ_cavv = null;
			}

			boolean cvv_present = check_cvv_presence(cvv);
			boolean is_reccuring = is_reccuring_check(recurring);
			boolean is_first_trs = true;

			String first_auth = "";
			long lrec_serie = 0;

			// controls
			Util.writeInFileTransaction(folder, file, "Switch processing start ...");

			String tlv = "";
			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

			if (!cvv_present && !is_reccuring) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file,
						"payer 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (cvv doit être présent dans la transaction normale), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// not reccuring , normal
			if (cvv_present && !is_reccuring) {
				Util.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
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

					Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
					Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
					Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
					Util.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
					Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
					Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
					Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
					Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
					Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
					Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
					Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
					Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
					Util.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
					Util.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
					Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
					Util.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
					Util.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
					Util.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
					Util.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
					Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
					Util.writeInFileTransaction(folder, file, "tag167_request : [" + champ_cavv + "]");
					Util.writeInFileTransaction(folder, file, "tag168_request : [" + xid + "]");

				} catch (Exception err4) {
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file,
							"payer 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err4);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la création du switch tlv), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}

				Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

			}

			// reccuring
			if (is_reccuring) {
				Util.writeInFileTransaction(folder, file, "reccuring");
			}

			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
//			SwitchTCPClient sw = SwitchTCPClient.getInstance();
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {

				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				Util.writeInFileTransaction(folder, file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					Util.writeInFileTransaction(folder, file,
							"payer 500 Error Switch communication s_conn false switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}

				if (s_conn) {
					Util.writeInFileTransaction(folder, file, "Switch Connected.");
					Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;

			} catch (java.net.ConnectException e) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Un dysfonctionnement du switch), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (SocketTimeoutException e) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "payer 500 Error Switch communication SocketTimeoutException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur de communication du switch SocketTimeoutException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (IOException e) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "payer 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur de communication du switch IOException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (Exception e) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch Exception), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				Util.writeInFileTransaction(folder, file, "payer 500 Error Switch null response" + "switch ip:[" + sw_s
						+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp null), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			if (switch_ko == 0 && resp.length() < 3) {
				dmd.setDem_cvv("");
				dmd.setEtat_demande("SW_KO");
				demandePaiementService.save(dmd);
				switch_ko = 1;

				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				Util.writeInFileTransaction(folder, file, "payer 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp < 3 !!!), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

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
					Util.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file, "payer 500 Error during tlv Switch response parse"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// controle switch
				if (tag1_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"payer 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				if (tag1_resp != null && tag1_resp.length() < 3) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"payer 500" + "Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag20_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"payer 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}
			}
			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			Util.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

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
				Util.writeInFileTransaction(folder, file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + merchantid);
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;

			Util.writeInFileTransaction(folder, file, "Insert into Histogate...");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

				Util.writeInFileTransaction(folder, file, "get status ...");

				s_status = "";
				try {
					CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
					System.out.println("codeReponseDto : " + codeReponseDto);
					Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
					if (codeReponseDto != null) {
						s_status = codeReponseDto.getRpcLibelle();
					}
				} catch (Exception ee) {
					Util.writeInFileTransaction(folder, file, "authorization 500 Error codeReponseDto null");
					ee.printStackTrace();
				}
				
				websiteid = dmd.getGalid();

				Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

				Util.writeInFileTransaction(folder, file, "get max id ...");

				// Ihist_id = hist.getMAX_ID("HISTOAUTO_GATE", "HAT_ID");
				// Ihist_id = histoAutoGateService.getMAX_ID();
				// long currentid = Ihist_id.longValue() + 1;
				// hist.setId(currentid);

				Util.writeInFileTransaction(folder, file, "max id : [" + Ihist_id + "]");

				Util.writeInFileTransaction(folder, file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);
				Util.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");
				
				Util.writeInFileTransaction(folder, file, "websiteid : " + websiteid);

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
				if (websiteid.equals("")) {
					hist.setHatCodtpe("1");
				} else {
					hist.setHatCodtpe(websiteid);
				}
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

				Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

				hist = histoAutoGateService.save(hist);
				
				Util.writeInFileTransaction(folder, file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"payer 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + e);
				try {
					Util.writeInFileTransaction(folder, file, "2eme tentative : HistoAutoGate Saving ... ");
					hist = histoAutoGateService.save(hist);
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							"2eme tentative : payer 500 Error during  insert in histoautogate for given orderid:["
									+ orderid + "]" + ex);
				}
			}

			Util.writeInFileTransaction(folder, file, "HistoAutoGate OK.");

			if (tag20_resp == null) {
				tag20_resp = "";
			}

			if (tag20_resp.equalsIgnoreCase("00"))

			{
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[00]");

				try {
					Util.writeInFileTransaction(folder, file, "update etat demande : SW_PAYE ...");

					dmd.setEtat_demande("SW_PAYE");
					if(dmd.getTransactiontype().equals("0")) {
						dmd.setDem_cvv("");
					}	
					demandePaiementService.save(dmd);

				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"payer 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:[" + orderid
									+ "]" + e);
				}

				Util.writeInFileTransaction(folder, file, "update etat demande : SW_PAYE OK");

				String capture_status = "N";
				int exp_flag = 0;

				if (capture.equalsIgnoreCase("Y")) {
					// 2024-05-17
					HistoAutoGateDto histToCapture= null;
					try {
						if(hist.getId() == null) {
							// get histoauto check if exist
							histToCapture = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr(orderid, merchantid);
							if(histToCapture == null) {
								histToCapture = hist;
							}
						} else {
							histToCapture = hist;
						}
					} catch (Exception err2) {
						Util.writeInFileTransaction(folder, file,
								"payer 500 Error during HistoAutoGate findLastByHatNumCommandeAndHatNumcmr orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + err2);
					}
					// 2024-05-17

					Date current_date = null;
					current_date = new Date();
					Util.writeInFileTransaction(folder, file, "Automatic capture start...");

					Util.writeInFileTransaction(folder, file, "Getting authnumber");

					String authnumber = histToCapture.getHatNautemt();
					Util.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

					Util.writeInFileTransaction(folder, file, "Getting authnumber");
					TransactionDto trs_check = null;

					try {
						trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
					} catch (Exception ee) {

						Util.writeInFileTransaction(folder, file,
								"trs_check trs_check exception e : [" + ee.toString() + "]");
					}

					if (trs_check != null) {
						// do nothing
						Util.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
					} else {
						Util.writeInFileTransaction(folder, file, "inserting into telec start ...");
						try {
							// insert into telec

							TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

							long lidtelc = 0;

							if (n_tlc == null) {
								Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
								Integer idtelc = null;

								TelecollecteDto tlc = null;

								// insert into telec
								idtelc = telecollecteService.getMAX_ID(merchantid);
								Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);
								
								if (idtelc != null) {
									lidtelc = idtelc.longValue() + 1;
								} else {
									lidtelc = 1;
								}
								tlc = new TelecollecteDto();
								tlc.setTlc_numtlcolcte(lidtelc);

								tlc.setTlc_numtpe(histToCapture.getHatCodtpe());

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
								Util.writeInFileTransaction(folder, file, "n_tlc !=null ");

								lidtelc = n_tlc.getTlc_numtlcolcte();
								double nbr_trs = n_tlc.getTlc_nbrtrans();

								nbr_trs = nbr_trs + 1;

								n_tlc.setTlc_nbrtrans(nbr_trs);

								telecollecteService.save(n_tlc);
							}

							// insert into transaction
							TransactionDto trs = new TransactionDto();
							trs.setTrsnumcmr(merchantid);
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

							trs.setTrsnumaut(authnumber);
							trs.setTrs_etat("N");
							trs.setTrs_devise(histToCapture.getHatDevise());
							trs.setTrs_certif("N");
							Integer idtrs = transactionService.getMAX_ID();
							long lidtrs = idtrs.longValue() + 1;
							trs.setTrs_id(lidtrs);
							trs.setTrs_commande(orderid);
							trs.setTrs_procod("0");
							trs.setTrs_groupe(websiteid);
							trs.setTrs_codtpe(0.0);
							trs.setTrs_numbloc(0.0);
							trs.setTrs_numfact(0.0);
							transactionService.save(trs);

							histToCapture.setHatEtat('T');
							histToCapture.setHatdatetlc(current_date);
							histToCapture.setOperateurtlc("mxplusapi");
							histoAutoGateService.save(histToCapture);

							capture_id = String.format("%040d",
									new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
							Date dt = new Date();
							String dtpattern = "yyyy-MM-dd";
							SimpleDateFormat sfdt = new SimpleDateFormat(dtpattern);
							String sdt = sfdt.format(dt);
							String tmpattern = "HH:mm:ss";
							SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
							String stm = sftm.format(dt);
							Util.writeInFileTransaction(folder, file, "inserting into telec ok");
							capture_status = "Y";

						} catch (Exception e) {
							exp_flag = 1;
							Util.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing " + e);
						}
					}
					if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
						capture_status.equalsIgnoreCase("N");

					Util.writeInFileTransaction(folder, file, "Automatic capture end.");
				}
				
				// 2023-01-03 confirmation par Callback URL
				String resultcallback = "";
				String callbackURL = dmd.getCallbackURL();
				Util.writeInFileTransaction(folder, file, "Call Back URL: " + callbackURL);
				if (dmd.getCallbackURL() != null && !dmd.getCallbackURL().equals("")
						&& !dmd.getCallbackURL().equals("NA")) {
					String clesigne = current_infoCommercant.getClePub();

					String montanttrx = String.format("%.2f", dmd.getMontant()).replaceAll(",", ".");
					String token_gen = "";

					Util.writeInFileTransaction(folder, file,
							"sendPOST(" + callbackURL + "," + clesigne + "," + dmd.getCommande() + ","
									+ tag20_resp + "," + montanttrx + "," + hist.getHatNautemt() + ","
									+ hist.getHatNumdem() + "," + dmd.getType_carte() + ")");

					resultcallback = sendPOST(callbackURL, clesigne, dmd.getCommande(), tag20_resp,
							montanttrx, hist.getHatNautemt(), hist.getHatNumdem(), token_gen,
							Util.formatCard(cardnumber), dmd.getType_carte(), folder, file);

					Util.writeInFileTransaction(folder, file,
							"resultcallback :[" + resultcallback + "]");

					boolean repsucces = resultcallback.indexOf("GATESUCCESS") != -1 ? true : false;

					boolean repfailed = resultcallback.indexOf("GATEFAILED") != -1 ? true : false;

					Util.writeInFileTransaction(folder, file, "repsucces : " + repsucces);
					Util.writeInFileTransaction(folder, file, "repfailed : " + repfailed);
					if (repsucces) {
						Util.writeInFileTransaction(folder, file,
								"Reponse recallURL OK => GATESUCCESS");
						dmd.setRecallRep("Y");
						demandePaiementService.save(dmd);
					} else {
						if (repfailed) {
							Util.writeInFileTransaction(folder, file,
									"Reponse recallURL KO => GATEFAILED");
							dmd.setRecallRep("N");
							demandePaiementService.save(dmd);
						} 
						//else {
							if (!DGI_PREPROD.equals(merchantid) && !DGI_PROD.equals(merchantid)) {
								Util.writeInFileTransaction(folder, file, "Annulation auto start ...");

								String repAnnu = AnnulationAuto(dmd, current_merchant, hist, model, folder,
										file);

								Util.writeInFileTransaction(folder, file, "Annulation auto end");
								s_status = "";
								try {
									CodeReponseDto codeReponseDto = codeReponseService
											.findByRpcCode(repAnnu);
									System.out.println("codeReponseDto annulation : " + codeReponseDto);
									Util.writeInFileTransaction(folder, file,
											"codeReponseDto annulation : " + codeReponseDto);
									if (codeReponseDto != null) {
										s_status = codeReponseDto.getRpcLibelle();
									}
								} catch (Exception ee) {
									Util.writeInFileTransaction(folder, file,
											"Annulation auto 500 Error codeReponseDto null");
									ee.printStackTrace();
								}
								Util.writeInFileTransaction(folder, file,
										"Switch status annulation : [" + s_status + "]");
								if (repAnnu.equals("00")) {
									dmd.setEtat_demande("SW_ANNUL_AUTO");
									dmd.setDem_cvv("");
									demandePaiementService.save(dmd);
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti (Web service CallBack Hors service), votre compte ne sera pas débité, merci de réessayer .");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "operationAnnulee";
								} else {
									page = "error";
								}

								Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
								System.out.println("Fin processRequest ()");
								return page;

							}
						//}
					}
				}
				// 2024-02-28 confirmation par Callback URL

			} else {

				Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {
					Util.writeInFileTransaction(folder, file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtat_demande("SW_REJET");
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					// old
					//hist.setHatEtat('A');
					//histoAutoGateService.save(hist);
				} catch (Exception e) {
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file,
							"payer 500 Error during  DemandePaiement update SW_REJET for given orderid:[" + orderid
									+ "]" + e);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}
				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to SW_REJET OK.");
				// 2024-02-27
				try {
					if(hist.getId() == null) {
						// get histoauto check if exist
						HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
						if(histToAnnulle != null) {
							Util.writeInFileTransaction(folder, file,
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
					Util.writeInFileTransaction(folder, file,
							"payer 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err2);
				}
				Util.writeInFileTransaction(folder, file, "update HistoAutoGateDto etat to A OK.");
				// 2024-02-27
			}

			Util.writeInFileTransaction(folder, file, "Generating paymentid...");

			String uuid_paymentid, paymentid = "";
			try {
				uuid_paymentid = String.format("%040d",
						new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
				paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"payer 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + e);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors de la génération de l'ID de paiement), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			Util.writeInFileTransaction(folder, file, "Generating paymentid OK");
			Util.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

			// JSONObject jso = new JSONObject();

			Util.writeInFileTransaction(folder, file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDem_pan();
				transactionid = String.valueOf(hist.getHatNumdem());
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"payer 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// reccurent transaction processing

			// reccurent insert and update

			try {

				/*
				 * String data_noncrypt = "orderid=" + orderid + "&fname=" + fname + "&lname=" +
				 * lname + "&email=" + email + "&amount=" + amount + "&coderep=" + coderep +
				 * "&authnumber=" + authnumber + "&cardnumber=" + Util.formatCard(cardnumber) +
				 * "&transactionid=" + transactionid + "&paymentid=" + paymentid;
				 */

				String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
						+ "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep + "&numAuto=" + authnumber
						+ "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getType_carte()
						+ "&numTrans=" + transactionid;

				Util.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
				System.out.println("data_noncrypt : " + data_noncrypt);

				String plainTxtSignature = orderid + current_infoCommercant.getClePub();

				Util.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
				System.out.println("plainTxtSignature : " + plainTxtSignature);

				String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt, current_infoCommercant.getClePub(),
						plainTxtSignature, folder, file);

				Util.writeInFileTransaction(folder, file, "data encrypt : " + data);
				System.out.println("data encrypt : " + data);

				if (coderep.equals("00")) {
					Util.writeInFileTransaction(folder, file,
							"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					if (dmd.getSuccessURL() != null) {
						response.sendRedirect(dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
					} else {
						responseDto responseDto = new responseDto();
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
						Util.writeInFileTransaction(folder, file, "Fin payer ()");
						System.out.println("Fin payer ()");
						return page;
					}
				} else {
					Util.writeInFileTransaction(folder, file,
							"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
					System.out.println("coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
					String libelle = "";
					try {
						CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(coderep);
						System.out.println("codeReponseDto : " + codeReponseDto);
						Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
						if (codeReponseDto != null) {
							libelle = codeReponseDto.getRpcLibelle();
						}
					} catch (Exception ee) {
						Util.writeInFileTransaction(folder, file, "payer 500 Error codeReponseDto null");
						ee.printStackTrace();
					}
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Coderep " + coderep
									+ ":" + libelle + ")," + " votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
				}
			} catch (Exception jsouterr) {
				Util.writeInFileTransaction(folder, file,
						"payer 500 Error during jso out processing given authnumber:[" + authnumber + "]" + jsouterr);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// fin
			// *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// ********************* Cas chalenge responseMPI equal C ou D
			// *********************
			Util.writeInFileTransaction(folder, file, "****** Cas chalenge responseMPI equal C ou D ******");
			try {

				// insertion htmlCreq dans la demandePaiement
				// dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				// dmd.setCreq(
				// "<form action='https://acs2.sgmaroc.com:443/lacs2' method='post'
				// enctype='application/x-www-form-urlencoded'><input type='hidden' name='creq'
				// value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjBlYmU1ODEwLTlhMDMtNGYzZi05MDgzLTJlZWNhNjhiMjY2YSIsCiAgImFjc1RyYW5zSUQiOiAiMmM5MjAxNDgtNjhiOC00ZjA0LWJhODQtY2RiYTFlOTM5MDM3IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0='
				// /></form>");
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("SND_TO_ACS");
				demandeDto = demandePaiementService.save(dmd);
				model.addAttribute("demandeDto", demandeDto);
				page = "chalenge";

				Util.writeInFileTransaction(folder, file, "set demandeDto model creq : " + demandeDto.getCreq());
				Util.writeInFileTransaction(folder, file, "return page : " + page);

				// return page;
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file, "payer 500 Error during jso out processing " + ex);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}
		} else if (reponseMPI.equals("E")) {
			// ********************* Cas responseMPI equal E
			// *********************
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("MPI_CMR_INEX");
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "BIN NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "DIRECTORY SERVER":
				Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (MPI_DS_ERR), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "CARTE ERRONEE":
				Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "CARTE NON ENROLEE":
				Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "ERROR REPONSE ACS":
				Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_RS_ACS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (ERROR REPONSE ACS), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "Error 3DSS":
				Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_3DSS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (ERROR 3DSS), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin process ()");
				System.out.println("Fin process ()");
				return page;
			}
		} else {
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("MPI_CMR_INEX");
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "BIN NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "DIRECTORY SERVER":
				Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (MPI_DS_ERR), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "CARTE ERRONEE":
				Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "CARTE NON ENROLEE":
				Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "ERROR REPONSE ACS":
				Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_RS_ACS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (ERROR REPONSE ACS), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
				System.out.println("Fin processRequest ()");
				return page;
			case "Error 3DSS":
				Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_3DSS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (ERROR 3DSS), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin process ()");
				System.out.println("Fin process ()");
				return page;
			}
		}

		System.out.println("demandeDto htmlCreq : " + demandeDto.getCreq());
		System.out.println("return page : " + page);

		Util.writeInFileTransaction(folder, file, "*********** Fin payer () ************** ");
		System.out.println("*********** Fin payer () ************** ");

		return page;
	}
	
	@PostMapping("/retour")
	public String retour(Model model, @ModelAttribute("demandeDto") DemandePaiementDto demandeDto, HttpServletRequest request,
	                     HttpServletResponse response, HttpSession session) throws IOException {
	    randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
	    String file = "GW_retour_" + randomWithSplittableRandom;
	    // create file log
	    Util.creatFileTransaction(file);
	    Util.writeInFileTransaction(folder, file, "*********** Start retour () ************** ");
	    System.out.println("*********** Start retour () ************** ");
	    
	    String page = "timeout";
	    String msg = "OK";
	    Integer idDemande = null;
	    // Récupération de l'attribut de session
	    try {
	    	String idDemandeStr = String.valueOf(session.getAttribute("idDemande"));
	        if (idDemandeStr != null) {
	            idDemande = Integer.valueOf(idDemandeStr);
	        }
	        idDemande = (Integer) session.getAttribute("idDemande");
	        Util.writeInFileTransaction(folder, file, "idDemande par session : " + idDemande);
	        System.out.println("idDemande par session : " + idDemande);            
	    } catch (Exception e) {
	        System.out.println("Retour getIdDemande par session " + e);
	        Util.writeInFileTransaction(folder, file, "retour getIdDemande par session " + e);
	        msg = "KO";
	    }
	    
	    // Si l'attribut de session est nul, utilisez l'ID de la demande du DTO
	    if (idDemande == null) {
	        try {
	            idDemande = demandeDto.getIddemande();
	            Util.writeInFileTransaction(folder, file, "idDemandepar model demandeDto : " + idDemande);
	            System.out.println("idDemande par model demandeDto : " + idDemande);    
	        } catch (Exception ex) {
	            System.out.println("Retour getIdDemande par demandeDto " + ex);
	            Util.writeInFileTransaction(folder, file, "retour getIdDemande par demandeDto " + ex);
	            msg = "KO";
	        }
	    }
        System.out.println("msg : " + msg);
        Util.writeInFileTransaction(folder, file, "msg : " + msg);
	    // Traitement de la demande si l'ID de la demande est disponible
	    if (msg.equals("OK") && idDemande != null) {
	        DemandePaiementDto demandePaiement = demandePaiementService.findByIdDemande(idDemande);

	        if (demandePaiement != null) {
	            System.out.println("update Demandepaiement status to Timeout");
	            Util.writeInFileTransaction(folder, file, "update Demandepaiement status to Timeout");
	            demandePaiement.setEtat_demande("TimeOut");
	            demandePaiement.setDem_cvv("");
	            demandePaiement = demandePaiementService.save(demandePaiement);
	            String failUrl = demandePaiement.getFailURL();
	            String successUrl = demandePaiement.getSuccessURL();
	            if (failUrl != null && !failUrl.equals("")) {
	                response.sendRedirect(failUrl);
	            } else {
	                response.sendRedirect(successUrl);
	            }
	        } else {
	            System.out.println("DemandePaiement not found ");
	            Util.writeInFileTransaction(folder, file, "DemandePaiement not found ");
	            response.sendRedirect(page);
	        }
	    } else {
	        // Gérer le cas où idDemande est null après les tentatives
	        System.out.println("idDemande is null");
	        Util.writeInFileTransaction(folder, file, "idDemande is null");
	        response.sendRedirect(page);
	    }

	    Util.writeInFileTransaction(folder, file, "*********** Fin retour () ************** ");
	    System.out.println("*********** Fin retour () ************** ");

	    return page;
	}

	
//	@PostMapping("/retour")
//	public String retour(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto, HttpServletRequest request,
//			HttpServletResponse response, HttpSession session) throws IOException {
//		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
//		String file = "GW_retour_" + randomWithSplittableRandom;
//		// create file log
//		Util.creatFileTransaction(file);
//		Util.writeInFileTransaction(folder, file, "*********** Start retour () ************** ");
//		System.out.println("*********** Start retour () ************** ");
//		
//		String page = "timeout";
//		String msg ="OK";
//		Integer idDemande = null;
//		try {
//			idDemande = (Integer) session.getAttribute("idDemande");			
//			Util.writeInFileTransaction(folder, file, "idDemande session : " + idDemande);
//			System.out.println("idDemande session : " + idDemande);			
//		} catch(Exception e) {
//			System.out.println("Retour session " + e);
//			Util.writeInFileTransaction(folder, file, "retour session " + e);
//			msg = "KO";
//		}
//		try {
//			idDemande = dto.getIddemande();
//			Util.writeInFileTransaction(folder, file, "idDemande dto : " + idDemande);
//			System.out.println("idDemande dto : " + idDemande);	
//		} catch(Exception ex) {
//			System.out.println("Retour dto " + ex);
//			Util.writeInFileTransaction(folder, file, "retour dto " + ex);
//			msg = "KO";
//		}
//		if(msg.equals("OK")) {
//			DemandePaiementDto demandePaiement = demandePaiementService.findByIdDemande(idDemande);
//
//			if(demandePaiement != null) {
//				System.out.println("update Demandepaiement status to Timeout");
//				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to Timeout");
//				demandePaiement.setEtat_demande("TimeOut");
//				demandePaiement = demandePaiementService.save(demandePaiement);
//				String failUrl = demandePaiement.getFailURL();
//				String successUrl = demandePaiement.getSuccessURL();
//				if(failUrl != null && !failUrl.equals("")) {
//					response.sendRedirect(failUrl);
//				} else {
//					response.sendRedirect(successUrl);
//				}
//			} else {
//				System.out.println("DemandePaiement not found ");
//				Util.writeInFileTransaction(folder, file, "DemandePaiement not found ");
//				response.sendRedirect(page);
//			}
//		}
//
//		Util.writeInFileTransaction(folder, file, "*********** Fin retour () ************** ");
//		System.out.println("*********** Fin retour () ************** ");
//
//		return page;
//	}
	
	@RequestMapping(value = "/chalenge", method = RequestMethod.GET)
	public String chlenge(Model model) {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_CHALENGE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		DemandePaiementDto dem = new DemandePaiementDto();
		System.out.println("Start chalenge ()");
		Util.writeInFileTransaction(folder, file, "*********** Start chalenge () ************** ");
		System.out.println("*********** Start chalenge () ************** ");

		String htmlCreq = "<form action='https://acs.naps.ma:443/lacs2' method='post' enctype='application/x-www-form-urlencoded'>"
				+ "<input type='hidden' name='creq' value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjQxZDQ0ZTViLTBjOTYtNGVhNC05NjkxLTM1OWVmOGQ5NTdjMyIsCiAgImFjc1RyYW5zSUQiOiAiOTI3NTQyOGEtYzkzYi00ZWUzLTk3NDEtNDA4NzAzNDlmYzM2IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' />"
				+ "</form>";
		dem.setCreq(htmlCreq);
		System.out.println("dem htmlCreq : " + dem.getCreq());

		System.out.println("dem commande : " + dem.getCommande());
		System.out.println("dem montant : " + dem.getMontant());

		model.addAttribute("demandeDto", dem);
		System.out.println("return to chalenge.html");

		Util.writeInFileTransaction(folder, file, "*********** Fin chalenge () ************** ");
		System.out.println("*********** Fin chalenge () ************** ");

		return "chalenge";
	}

	@RequestMapping(value = "/napspayment/error/token/{token}", method = RequestMethod.GET)
	public String error(@PathVariable(value = "token") String token, Model model) {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_ERROR_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start error ************** ");
		System.out.println("*********** Start error ************** ");

		String page = "error";

		Util.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto current_dem = demandePaiementService.findByTokencommande(token);
		String msgRefus = "Une erreur est survenue, merci de réessayer plus tard";

		if (current_dem != null) {
			Util.writeInFileTransaction(folder, file, "current_dem is found OK");
			System.out.println("current_dem is found OK");
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
			Util.writeInFileTransaction(folder, file, "current_dem not found ");
			System.out.println("current_dem null ");
			page = "error";
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin error ************** ");
		System.out.println("*********** Fin error ************** ");

		return page;
	}

	@RequestMapping(value = "/napspayment/index2", method = RequestMethod.GET)
	public String index2() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_INDEX2_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start index2 () ************** ");
		System.out.println("*********** Start index2 () ************** ");

		Util.writeInFileTransaction(folder, file, "return to index2.html");
		System.out.println("return to index2.html");

		Util.writeInFileTransaction(folder, file, "*********** Fin index2 () ************** ");
		System.out.println("*********** Fin index2 () ************** ");

		return "index2";
	}

	@RequestMapping(value = "/napspayment/result", method = RequestMethod.GET)
	public String result() {
		// Traces traces = new Traces();
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "GW_RESULT_" + randomWithSplittableRandom;
		Util.creatFileTransaction(file);

		Util.writeInFileTransaction(folder, file, "*********** Start result () ************** ");
		System.out.println("*********** Start result () ************** ");

		Util.writeInFileTransaction(folder, file, "return to result.html");
		System.out.println("return to result.html");

		Util.writeInFileTransaction(folder, file, "*********** Fin result () ************** ");
		System.out.println("*********** Start Fin () ************** ");

		return "result";
	}

	@PostMapping(path = "/napspayment/linkpayment-xml", consumes = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public String userInformation(@RequestBody UserDto user) {

		System.out.println(user.getName() + " " + user.getEmail());

		return "User information saved successfully ::.";
	}
	
	public String AnnulationAuto(DemandePaiementDto current_dmd, CommercantDto current_merchant,
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
			Util.writeInFileTransaction(folder, file,
					"annulation auto 500 Error during date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err3);

			return "annulation auto 500 Error during date formatting for given orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]" + err3;
		}

		// 2024-03-05
		montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, page, model);

		Util.writeInFileTransaction(folder, file, "Switch processing start ...");

		String tlv = "";
		Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

		// controls
		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		String merchantname = current_merchant.getCmrAbrvnom();
		String cardnumber = current_dmd.getDem_pan();
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

			Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
			Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
			Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
			Util.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
			Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
			Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
			Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
			Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
			Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
			Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
			Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
			Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
			Util.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
			Util.writeInFileTransaction(folder, file, "tag8_request : [0+" + merchantid + "]");
			Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
			Util.writeInFileTransaction(folder, file, "tag66_request : [" + transactionnumber + "]");
			Util.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
			Util.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
			Util.writeInFileTransaction(folder, file, "tag13_request : [MAR]");
			Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
			Util.writeInFileTransaction(folder, file, "tag19_request : [" + authnumber + "]");

		} catch (Exception err4) {
			Util.writeInFileTransaction(folder, file,
					"annulation auto 500 Error during switch tlv buildu for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err4);

			return "annulation auto 500 Error during switch tlv buildu";
		}

		Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

		Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

		Util.writeInFileTransaction(folder, file, "Switch Connecting ...");

		String resp_tlv = "";
		SwitchTCPClient sw = SwitchTCPClient.getInstance();

		int port = 0;
		String sw_s = "", s_port = "";
		try {

			s_port = portSwitch;
			sw_s = ipSwitch;

			Util.writeInFileTransaction(folder, file, "Switch IP / Switch PORT : " + sw_s + "/" + s_port);

			port = Integer.parseInt(s_port);

			boolean s_conn = sw.startConnection(sw_s, port);
			Util.writeInFileTransaction(folder, file, "Switch Connecting ...");

			if (s_conn) {
				Util.writeInFileTransaction(folder, file, "Switch Connected.");
				Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

				resp_tlv = sw.sendMessage(tlv);

				Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
				sw.stopConnection();

			} else {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

				return "annulation auto 500 Error Switch communication s_conn false";
			}

		} catch (SocketTimeoutException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch communication SocketTimeoutException";
		} catch (UnknownHostException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch communication UnknownHostException";
		}

		catch (IOException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);
			return "annulation auto 500 Error Switch communication IOException";
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);
			return "annulation auto 500 Error Switch communication General Exception switch";
		}

		String resp = resp_tlv;
		if (resp == null) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch null response switch";
		}

		if (resp.length() < 3) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch short response length() < 3 switch";
		}

		Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

		Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

		// resp debug =
		// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

		TLVParser tlvp = null;

		// resp debug =
		// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

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
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);
			return "annulation auto 500 Error during tlv Switch response parse switch";
		}

		// controle switch
		if (tag1_resp == null) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error during tlv Switch response parse tag1_resp tag null";
		}

		if (tag1_resp.length() < 3) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error during tlv Switch response parse tag1_resp length tag  < 3 switch";
		}

		Util.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
		Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

		Util.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

		if (tag20_resp == null) {
			return "annulation auto 500 Switch malfunction response code not present";
		}
		if (tag20_resp.length() < 1) {
			return "annulation auto 500 Switch malfunction response code length incorrect";
		}

		if (tag20_resp.equalsIgnoreCase("00"))

		{
			Util.writeInFileTransaction(folder, file, "Switch CODE REP : [00]");

			Util.writeInFileTransaction(folder, file, "Transaction reversed.");

			try {
				Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status A ...");

				current_dmd.setEtat_demande("A");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"annulation auto 500 Error during  demandepaiement update  A for given orderid:[" + orderid
								+ "]" + e);

				return "annulation auto 500 Error during  demandepaiement update A";
			}

			Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status OK.");

			Util.writeInFileTransaction(folder, file, "Setting HistoAutoGate status A ...");

			// 2024-03-15
			try {
				if(current_hist.getId() == null) {
					// get histoauto check if exist
					HistoAutoGateDto histToAnnulle = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr(orderid, merchantid);
					if(histToAnnulle !=null) {
						Util.writeInFileTransaction(folder, file,
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
				Util.writeInFileTransaction(folder, file,
						"annulation auto 500 Error during HistoAutoGate findLastByHatNumCommandeAndHatNumcmr orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err2);
			}
			Util.writeInFileTransaction(folder, file, "update HistoAutoGateDto etat to A OK.");
			// 2024-03-15

			Util.writeInFileTransaction(folder, file, "Setting HistoAutoGate status OK.");
		} else {

			Util.writeInFileTransaction(folder, file, "Transaction annulation auto declined.");
			Util.writeInFileTransaction(folder, file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		return tag20_resp;
	}

	private String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid, 
			String page, Model model) {
		String montanttrame;
		String[] mm;
		String[] m;
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		try {
			montanttrame = "";

			mm = new String[2];

			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			System.out.println("montant : [" + amount + "]");
			Util.writeInFileTransaction(folder, file, "montant : [" + amount + "]");

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
			System.out.println("montanttrame : [" + montanttrame + "]");
			Util.writeInFileTransaction(folder, file, "montanttrame : [" + montanttrame + "]");
		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error during  amount formatting for given orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err3);
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
			System.out.println("Fin processRequest ()");
			return page;
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

	public void formatDateExp(String expirationDate, Cartes carte) {
		try {
			LocalDate localDate = LocalDate.parse(expirationDate);
			Month mois = localDate.getMonth();
			Integer year = localDate.getYear();
			carte.setYear(year);
			// String formattedMonth = mapToFrenchMonth(month);
			String moisStr = String.format("%s", mois);
			List<String> list = new ArrayList<>();
			list.add(moisStr);
			MonthDto month = mapToFrenchMonth(moisStr);
			carte.setMois(month.getMonth());
			carte.setMoisValue(month.getValue());
			
			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();
			// get year from date
			// format date to "yyyy-MM-dd"
			String expirydateFormated = carte.getYear() + "-" + carte.getMoisValue() + "-" + "01";
			// exp
			//String expirydateFormated = "2020" + "-" + "05" + "-" + "01";
			System.out.println("cardtokenDto expirydate : " + expirydateFormated);
			Util.writeInFileTransaction(folder, file,
					"cardtokenDto expirydate formated : " + expirydateFormated);
			Date dateExp = dateFormatSimple.parse(expirydateFormated);
			if(dateExp.before(dateToken)) {
				System.out.println("date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
				Util.writeInFileTransaction(folder, file, "date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
				carte.setMoisValue("xxxx");
				carte.setMois("xxxx");
				carte.setYear(1111);
			}
			if(dateExp.after(dateToken)) {
				System.out.println("date exiration est superieur à l adate systeme : " + dateExp + " < " + dateToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String sendPOST(String urlcalback, String clepub, String idcommande, String repauto, String montant,
			String numAuto, Long numTrans, String token_gen, String pan_trame, String typecarte, String folder,
			String file) throws IOException {

		String result = "";
		HttpPost post = new HttpPost(urlcalback);

		String reqenvoi = idcommande + repauto + clepub + montant;
		String signature = Util.hachInMD5(reqenvoi);
		Util.writeInFileTransaction(folder, file, "Signature : " + signature);
		// add request parameters or form parameters
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
			Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 1");
			CloseableHttpResponse response = httpClient.execute(post);

			result = EntityUtils.toString(response.getEntity());

		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file,
					" sendPOST Exception => {} tv 1 :" + ex.getMessage() + "ex : " + ex);
			result = "ko";
		}
		boolean repsucces = result.indexOf("GATESUCCESS") != -1 ? true : false;
		boolean repfailed = result.indexOf("GATEFAILED") != -1 ? true : false;
		if (!repsucces && !repfailed) {
			try {
				Thread.sleep(10000);
				Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 2");
				// tentative 2 apès 10 s
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse response = httpClient.execute(post)) {

					result = EntityUtils.toString(response.getEntity());
				}
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file,
						" sendPOST Exception => {} tv 2 :" + ex.getMessage() + "ex : " + ex);
				result = "ko";
			}
			boolean repsucces2 = result.indexOf("GATESUCCESS") != -1 ? true : false;
			boolean repfailed2 = result.indexOf("GATEFAILED") != -1 ? true : false;
			if (!repsucces2 && !repfailed2) {
				try {
					Thread.sleep(10000);
					// tentative 3 après 10s
					Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 3");
					try (CloseableHttpClient httpClient = HttpClients.createDefault();
							CloseableHttpResponse response = httpClient.execute(post)) {

						result = EntityUtils.toString(response.getEntity());
					}
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							" sendPOST Exception => {} tv 3 :" + ex.getMessage() + "ex : " + ex);
					result = "ko";
				}
			}
		}

		return result;
	}
	
	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		// modified 2024-0-30
		// SSLContext context = SSLContext.getInstance("SSL");
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, trustAllCerts, null);

		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		builder.setSSLSocketFactory(sslConnectionFactory);

		PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();

		return builder.build();

	}

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

	private String mapToFrenchMonth(Month month) {
		// Simple mapping from English to French month names.
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
			return ""; // Handle unknown month
		}
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

	// Static factory method to create a Cartes object from a string
	public Cartes fromString(String input) {
		Cartes cartes = new Cartes();

		// Remove square brackets and split the input string
		String[] keyValuePairs = input.substring(0, input.length() - 1).split(", ");

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
				// Handle other properties as needed
				}
			}
		}

		return cartes;
	}

}
