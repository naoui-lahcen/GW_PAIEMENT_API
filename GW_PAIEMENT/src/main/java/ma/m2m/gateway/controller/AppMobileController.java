package ma.m2m.gateway.controller;

import java.io.IOException;
import java.math.BigInteger;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.m2m.gateway.dto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.GalerieService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.CRes;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
import ma.m2m.gateway.utils.Objects;
import ma.m2m.gateway.utils.Util;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2023-11-03
 */

@Controller
public class AppMobileController {

    private static final Logger logger = LogManager.getLogger(AppMobileController.class);

    private LocalDateTime dateF;
    private String folder;
    private String file;
    private SplittableRandom splittableRandom = new SplittableRandom();
    long randomWithSplittableRandom;

    private Gson gson;

    @Value("${key.LIEN_3DSS_V}")
    private String urlThreeDSS;

    @Value("${key.LINK_CCB}")
    private String linkCcb;

    @Value("${key.SWITCH_URL}")
    private String ipSwitch;

    @Value("${key.SWITCH_PORT}")
    private String portSwitch;

    @Value("${key.SECRET}")
    private String secret;

    @Value("${key.USER_TOKEN}")
    private String usernameToken;

    @Value("${key.JWT_TOKEN_VALIDITY}")
    private long jwtTokenValidity;

    @Value("${key.ENVIRONEMENT}")
    private String environement;

    @Value("${key.FRAIS_CCB}")
    private String fraisCCB;

    @Value("${key.TIMEOUT}")
    private int timeout;

    //@Autowired
    private final AutorisationService autorisationService;

    //@Autowired
    private final DemandePaiementService demandePaiementService;

    //@Autowired
    private final HistoAutoGateService histoAutoGateService;

    //@Autowired
    private final CommercantService commercantService;

    //@Autowired
    private final InfoCommercantService infoCommercantService;

    //@Autowired
    private final CardtokenService cardtokenService;

    //@Autowired
    private final GalerieService galerieService;

    //@Autowired
    private final CodeReponseService codeReponseService;

    public static final String DF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

    DateFormat dateFormat = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
    DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

    public AppMobileController(DemandePaiementService demandePaiementService, AutorisationService autorisationService,
                               HistoAutoGateService histoAutoGateService, CommercantService commercantService,
                               InfoCommercantService infoCommercantService, GalerieService galerieService,
                               CardtokenService cardtokenService, CodeReponseService codeReponseService) {
        randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
        dateF = LocalDateTime.now(ZoneId.systemDefault());
        folder = dateF.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        this.gson = new GsonBuilder().serializeNulls().create();
        this.demandePaiementService = demandePaiementService;
        this.autorisationService = autorisationService;
        this.histoAutoGateService = histoAutoGateService;
        this.commercantService = commercantService;
        this.infoCommercantService = infoCommercantService;
        this.galerieService = galerieService;
        this.cardtokenService = cardtokenService;
        this.codeReponseService = codeReponseService;
    }

    @PostMapping("/napspayment/ccb/acs")
    @SuppressWarnings("all")
    public String processRequestMobile(HttpServletRequest request, HttpServletResponse response, Model model, HttpSession session)
            throws IOException {
        randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
        String file = "MB_R_ACS_" + randomWithSplittableRandom;
        // TODO: create file log
        Util.creatFileTransaction(file);
        autorisationService.logMessage(file, "Start processRequestMobile ()");
        logger.info("Start processRequestMobile ()");
        CRes cleanCres = new CRes();
        String msgRefus = "";
        DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
        String page = "index";
        try {
            String encodedCres = request.getParameter("cres");
            logger.info("ACSController RETOUR ACS =====> encodedCres : " + encodedCres);
            autorisationService.logMessage(file, "ACSController RETOUR ACS =====> encodedCres : " + encodedCres);

            String decodedCres = "";

            decodedCres = new String(Base64.decodeBase64(encodedCres.getBytes()));
            if (decodedCres.indexOf("}") != -1) {
                decodedCres = decodedCres.substring(0, decodedCres.indexOf("}") + 1);
            }
            autorisationService.logMessage(file, "ACSController RETOUR ACS =====> decodedCres : " + decodedCres);
            logger.info("ACSController RETOUR ACS =====> decodedCres : " + decodedCres);

            cleanCres = gson.fromJson(decodedCres, CRes.class);
            autorisationService.logMessage(file, "ACSController RETOUR ACS =====> cleanCres : " + cleanCres);

            autorisationService.logMessage(file, "transStatus/threeDSServerTransID : " + cleanCres.getTransStatus()
                    + "/" + cleanCres.getThreeDSServerTransID());

            // TODO: just for test
            // TODO: cleanCres.setTransStatus("N");

            if (cleanCres.getTransStatus().equals("Y") || cleanCres.getTransStatus().equals("N")) {
                logger.info("ACSController RETOUR ACS =====> callThree3DSSAfterACS ");
                autorisationService.logMessage(file, "ACSController RETOUR ACS =====> callThree3DSSAfterACS ");

                ThreeDSecureResponse threeDsecureResponse = autorisationService.callThree3DSSAfterACS(decodedCres,
                        folder, file);

                DemandePaiementDto dmd = new DemandePaiementDto();
                JSONObject jso = new JSONObject();
                String[] mm;
                String[] m;
                SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;

                /*
                 * ------------ DEBUT MPI RESPONSE PARAMS ------------
                 */
                String reponseMPI = "";
                String eci = "";
                String cavv = "";
                String threeDSServerTransID = "";
                String xid = "";
                String errmpi = "";
                String idDemande = "";
                String expiry = ""; // TODO: YYMM
                String processing_code = "";
                String acq_type = "";
                String merchant_city = "";
                String reason_code = "";
                String transaction_condition = "";
                String mesg_type = "";
                String currency = "504";

                String capture = "";
                String orderid = "";
                String recurring = "";
                String amount = "";
                String promoCode = "";
                String transactionid = "";
                String capture_id = "";
                String merchantid = "";
                String merchantname = "";
                String websiteName = "";
                String websiteid = "";
                String callbackUrl = "";
                String cardnumber = "";
                String token = "";
                String expirydate = "";
                String holdername = "";
                String cvv = "";
                String fname = "";
                String lname = "";
                String email = "";
                String country = "";
                String phone = "";
                String city = "";
                String state = "";
                String zipcode = "";
                String address = "";
                String merc_codeactivite = "";
                String acqcode = "";
                String merchant_name = "";
                String transactiondate = "";
                String transactiontime = "";
                String date = "";
                String rrn = "";
                String heure = "";
                String montanttrame = "";
                String montantRechgtrame = "", cartenaps = "", dateExnaps = "";
                String num_trs = "";
                String successURL = "";
                String failURL;

                if (threeDsecureResponse != null && threeDsecureResponse.getEci() != null) {
                    if (threeDsecureResponse.getEci().equals("05") || threeDsecureResponse.getEci().equals("02")
                            || threeDsecureResponse.getEci().equals("06")
                            || threeDsecureResponse.getEci().equals("01")) {

                        autorisationService.logMessage(file,
                                "if(eci=05) || eci=02 || eci=06 || eci=01) : continue le processus");

                        idDemande = threeDsecureResponse.getIdDemande();

                        reponseMPI = threeDsecureResponse.getReponseMPI();

                        threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();

                        eci = threeDsecureResponse.getEci() == null ? "" : threeDsecureResponse.getEci();

                        cavv = threeDsecureResponse.getCavv() == null ? "" : threeDsecureResponse.getCavv();

                        errmpi = threeDsecureResponse.getErrmpi() == null ? "" : threeDsecureResponse.getErrmpi();

                        expiry = threeDsecureResponse.getExpiry() == null ? "" : threeDsecureResponse.getExpiry();

                        if (idDemande == null || idDemande.equals("")) {
                            autorisationService.logMessage(file, "received idDemande from MPI is Null or Empty");
                            autorisationService.logMessage(file,
                                    "demandePaiement after update MPI_KO idDemande null");
                            demandeDtoMsg.setMsgRefus(
                                    "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                            model.addAttribute("demandeDto", demandeDtoMsg);
                            page = "result";
                            autorisationService.logMessage(file, "Fin processRequestMobile ()");
                            logger.info("Fin processRequestMobile ()");
                            return page;
                        }

                        dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

                        if (dmd == null) {
                            autorisationService.logMessage(file,
                                    "demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
                                            + idDemande);
                            demandeDtoMsg.setMsgRefus(
                                    "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                            model.addAttribute("demandeDto", demandeDtoMsg);
                            page = "result";
                            autorisationService.logMessage(file, "Fin processRequestMobile ()");
                            logger.info("Fin processRequestMobile ()");
                            return page;
                        }

                        page = autorisationService.handleSessionTimeout(session, file, timeout, dmd, demandeDtoMsg, model);

                        if ("timeout".equals(page)) {
                            return page;
                        }

                        // TODO: Merchnat info
                        merchantid = dmd.getComid();
                        websiteid = dmd.getGalid();

                        String timeStamp = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS).format(new Date());

                        autorisationService.logMessage(file, "authorization_" + orderid + timeStamp);

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

                        // TODO: Info
                        merc_codeactivite = current_merchant.getCmrCodactivite();
                        acqcode = current_merchant.getCmrCodbqe();

                        orderid = dmd.getCommande();
                        recurring = "";
                        amount = String.valueOf(dmd.getMontant());
                        promoCode = "";
                        transactionid = "";

                        // TODO: Merchnat info
                        merchantid = dmd.getComid();
                        merchantname = current_merchant.getCmrNom();
                        merchant_name = Util.pad_merchant(merchantname, 19, ' ');
                        websiteName = "";
                        callbackUrl = dmd.getCallbackURL();
                        successURL = dmd.getSuccessURL();
                        failURL = dmd.getFailURL();

                        // TODO: Card info
                        cardnumber = dmd.getDemPan();
                        token = dmd.getToken();
                        expirydate = expiry;
                        holdername = "";
                        cvv = dmd.getDemCvv();
                        cartenaps = dmd.getCartenaps();
                        dateExnaps = dmd.getDateexpnaps();

                        // TODO: Client info
                        fname = dmd.getNom();
                        lname = dmd.getPrenom();
                        email = dmd.getEmail();
                        country = dmd.getCountry();
                        phone = dmd.getTel();
                        city = dmd.getCity();
                        state = dmd.getState();
                        zipcode = dmd.getPostcode();
                        address = dmd.getAddress();

                        try {
                            formatheure = new SimpleDateFormat("HHmmss");
                            formatdate = new SimpleDateFormat("ddMMyy");
                            date = formatdate.format(new Date());
                            heure = formatheure.format(new Date());
                            rrn = Util.getGeneratedRRN();
                        } catch (Exception err2) {
                            dmd.setDemCvv("");
                            demandePaiementService.save(dmd);
                            autorisationService.logMessage(file,
                                    "authorization 500 Error during  date formatting for given orderid:[" + orderid
                                            + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
                            demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                            model.addAttribute("demandeDto", demandeDtoMsg);
                            page = "result";
                            autorisationService.logMessage(file, "Fin processRequestMobile ()");
                            logger.info("Fin processRequestMobile ()");
                            return page;
                        }

                        if (reponseMPI.equals("") || reponseMPI == null) {
                            dmd.setEtatDemande("MPI_KO");
                            dmd.setDemCvv("");
                            demandePaiementService.save(dmd);
                            autorisationService.logMessage(file,
                                    "demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
                            autorisationService.logMessage(file, "Response 3DS is null");
                            demandeDtoMsg.setMsgRefus(
                                    "La transaction en cours n’a pas abouti (MPI_KO reponseMPI null), votre compte ne sera pas débité, merci de réessayer.");
                            model.addAttribute("demandeDto", demandeDtoMsg);
                            page = "result";
                            autorisationService.logMessage(file, "Fin processRequestMobile ()");
                            logger.info("Fin processRequestMobile ()");
                            return page;
                        }

                        if (reponseMPI.equals("Y")) {
                            // TODO: ********************* Frictionless responseMPI equal Y *********************
                            autorisationService.logMessage(file,
                                    "********************* responseMPI equal Y *********************");

                            if (threeDSServerTransID != null && !threeDSServerTransID.equals("")) {
                                dmd.setDemxid(threeDSServerTransID);
                                // TODO: stackage de eci dans le chmp date_sendMPI vu que ce chmp nest pas utilisé
                                dmd.setDateSendMPI(eci);
                                // TODO: stackage de cavv dans le chmp date_SendSWT vu que ce chmp nest pas utilisé
                                dmd.setDateSendSWT(cavv);
                                dmd = demandePaiementService.save(dmd);
                            }

                            // TODO: 2024-03-05
                            montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, dmd, model);

                            // TODO: 2024-03-05
                            montantRechgtrame = formatMontantRechargeTrame(folder, file, amount, orderid, merchantid, dmd, page, model);

                            boolean cvv_present = checkCvvPresence(cvv);
                            boolean is_reccuring = isReccuringCheck(recurring);
                            boolean is_first_trs = true;

                            String first_auth = "";
                            long lrec_serie = 0;

                            merchant_city = "MOROCCO        ";
                            autorisationService.logMessage(file, "merchant_city : [" + merchant_city + "]");

                            acq_type = "0000";
                            processing_code = dmd.getTransactiontype();
                            reason_code = "H";
                            transaction_condition = "6";
                            mesg_type = "0";

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

                            // TODO: controls
                            autorisationService.logMessage(file, "Switch processing start ...");

                            String tlv = "";
                            autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

                            if (!cvv_present && !is_reccuring) {
                                dmd.setDemCvv("");
                                demandePaiementService.save(dmd);
                                autorisationService.logMessage(file,
                                        "authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");
                                demandeDtoMsg.setMsgRefus(
                                        "La transaction en cours n’a pas abouti (cvv doit être présent dans la transaction normale), votre compte ne sera pas débité, merci de réessayer.");
                                model.addAttribute("demandeDto", demandeDtoMsg);
                                page = "result";
                                autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                logger.info("Fin processRequestMobile ()");
                                return page;
                            }

                            // TODO: not reccuring , normal
                            if (cvv_present && !is_reccuring) {
                                autorisationService.logMessage(file,
                                        "not reccuring , normal cvv_present && !is_reccuring");
                                try {
                                    // TODO: tag 046 tlv info carte naps
                                    String tlvCCB = new TLVEncoder().withField(Tags.tag1, cartenaps)
                                            .withField(Tags.tag14, montantRechgtrame).withField(Tags.tag42, dateExnaps)
                                            .encode();
                                    // TODO: tlv total ccb
                                    tlv = new TLVEncoder().withField(Tags.tag0, mesg_type)
                                            .withField(Tags.tag1, cardnumber).withField(Tags.tag3, processing_code)
                                            .withField(Tags.tag22, transaction_condition)
                                            .withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
                                            .withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
                                            .withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
                                            .withField(Tags.tag16, date).withField(Tags.tag17, heure)
                                            .withField(Tags.tag10, merc_codeactivite)
                                            .withField(Tags.tag8, "0" + merchantid).withField(Tags.tag9, merchantid)
                                            .withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
                                            .withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
                                            .withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
                                            .withField(Tags.tag168, xid).withField(Tags.tag46, tlvCCB).encode();

                                } catch (Exception err4) {
                                    dmd.setDemCvv("");
                                    demandePaiementService.save(dmd);
                                    autorisationService.logMessage(file,
                                            "authorization 500 Error during switch tlv buildup for given orderid:["
                                                    + orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
                                    demandeDtoMsg.setMsgRefus(
                                            "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                                    model.addAttribute("demandeDto", demandeDtoMsg);
                                    page = "result";
                                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                    logger.info("Fin processRequestMobile ()");
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
//							SwitchTCPClient sw = SwitchTCPClient.getInstance();
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
                                            "authorization 500 Error Switch communication s_conn false switch ip:["
                                                    + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
                                                    + "]");
                                    demandeDtoMsg
                                            .setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                                    model.addAttribute("demandeDto", demandeDtoMsg);
                                    page = "result";
                                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                    logger.info("Fin processRequestMobile ()");
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

                            // TODO: resp debug
                            // TODO: resp =
                            // TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

                            if (switch_ko == 0 && resp == null) {
                                dmd.setDemCvv("");
                                dmd.setEtatDemande("SW_KO");
                                demandePaiementService.save(dmd);
                                autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
                                switch_ko = 1;
                                autorisationService.logMessage(file,
                                        "authorization 500 Error Switch null response" + "switch ip:[" + sw_s
                                                + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
                                demandeDtoMsg.setMsgRefus(
                                        "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                                model.addAttribute("demandeDto", demandeDtoMsg);
                                page = "result";
                                autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                logger.info("Fin processRequestMobile ()");
                                return page;
                            }

                            if (switch_ko == 0 && resp.length() < 3) {
                                dmd.setDemCvv("");
                                dmd.setEtatDemande("SW_KO");
                                demandePaiementService.save(dmd);
                                switch_ko = 1;

                                autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
                                autorisationService.logMessage(file,
                                        "authorization 500 Error Switch short response length() < 3 " + "switch ip:["
                                                + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
                                                + "]");
                                demandeDtoMsg.setMsgRefus(
                                        "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                                model.addAttribute("demandeDto", demandeDtoMsg);
                                page = "result";
                                autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                logger.info("Fin processRequestMobile ()");
                                return page;
                            }

                            autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

                            TLVParser tlvp = null;

                            String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null,
                                    tag9_resp = null, tag14_resp = null, tag15_resp = null, tag16_resp = null,
                                    tag17_resp = null, tag66_resp = null, tag18_resp = null, tag19_resp = null,
                                    tag23_resp = null, tag20_resp = null, tag21_resp = null, tag22_resp = null,
                                    tag80_resp = null, tag98_resp = null;

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
                                    dmd.setDemCvv("");
                                    dmd.setEtatDemande("SW_KO");
                                    demandePaiementService.save(dmd);
                                    autorisationService.logMessage(file,
                                            "Switch  malfunction tlv parsing !!!" + Util.formatException(e));
                                    switch_ko = 1;
                                    autorisationService.logMessage(file,
                                            "authorization 500 Error during tlv Switch response parse" + "switch ip:["
                                                    + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
                                                    + "]");
                                    demandeDtoMsg.setMsgRefus(
                                            "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                                    model.addAttribute("demandeDto", demandeDtoMsg);
                                    page = "result";
                                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                    logger.info("Fin processRequestMobile ()");
                                    return page;
                                }

                                // TODO: TODO: controle switch
                                if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
                                    autorisationService.logMessage(file,
                                            "Switch  malfunction !!! tag1_resp == null");
                                    switch_ko = 1;
                                    autorisationService.logMessage(file,
                                            "authorization 500"
                                                    + "Error during tlv Switch response parse tag1_resp length tag  < 3"
                                                    + "switch ip:[" + sw_s + "] and switch port:[" + port
                                                    + "] resp_tlv : [" + resp_tlv + "]");
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

                            try {
                                // TODO: TODO: calcule du montant avec les frais
                                amount = calculMontantTotalOperation(dmd);
                            } catch (Exception ex) {
                                autorisationService.logMessage(file, "calcule du montant avec les frais : " + Util.formatException(ex));
                            }

                            if (switch_ko == 1) {
                                pan_auto = Util.formatagePan(cardnumber);
                                autorisationService.logMessage(file,
                                        "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : " + pan_auto + "/" + rrn
                                                + "/" + amount + "/" + date + "/" + merchantid);
                            }
                            HistoAutoGateDto hist = null;
                            Integer Ihist_id = null;

                            autorisationService.logMessage(file, "Insert into Histogate...");

                            s_status = "";
                            try {
                                CodeReponseDto codeReponseDto = codeReponseService
                                        .findByRpcCode(tag20_resp_verified);
                                autorisationService.logMessage(file, "" + codeReponseDto);
                                if (codeReponseDto != null) {
                                    s_status = codeReponseDto.getRpcLibelle();
                                }
                            } catch (Exception ee) {
                                autorisationService.logMessage(file,
                                        "authorization 500 Error codeReponseDto null" + Util.formatException(ee));
                            }
                            autorisationService.logMessage(file,"get status Switch status : [" + s_status + "]");

                            try {

                                hist = new HistoAutoGateDto();
                                Date curren_date_hist = new Date();
                                int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

                                websiteid = dmd.getGalid();

                                autorisationService.logMessage(file, "formatting pan...");

                                pan_auto = Util.formatagePan(cardnumber);
                                autorisationService.logMessage(file,
                                        "formatting pan Ok pan_auto :[" + pan_auto + "]");

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
                                if (websiteid.equals("")) {
                                    websiteid = "0066";
                                }
                                hist.setHatNomdeandeur(websiteid);
                                hist.setHatNautemt(tag19_res_verified); // TODO: TODO: f2
                                tag19_resp = tag19_res_verified;
                                if (tag22_resp != null)
                                    hist.setHatProcode(tag22_resp.charAt(0));
                                else
                                    hist.setHatProcode('6');
                                hist.setHatExpdate(expirydate);
                                hist.setHatRepondeur(tag21_resp);
                                hist.setHatTypmsg("3");
                                hist.setHatRrn(tag66_resp_verified); // TODO: TODO: f1
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
                                        "authorization 500 Error during  insert in histoautogate for given orderid:["
                                                + orderid + "]" + Util.formatException(e));
                                try {
                                    autorisationService.logMessage(file,
                                            "2eme tentative : HistoAutoGate Saving ... ");
                                    hist = histoAutoGateService.save(hist);
                                } catch (Exception ex) {
                                    autorisationService.logMessage(file,
                                            "2eme tentative : authorization 500 Error during  insert in histoautogate for given orderid:["
                                                    + orderid + "]" + Util.formatException(ex));
                                }
                            }

                            autorisationService.logMessage(file, "HistoAutoGate OK.");

                            if (tag20_resp == null) {
                                tag20_resp = "";
                            }

                            if (tag20_resp.equalsIgnoreCase("00")) {
                                autorisationService.logMessage(file, "SWITCH RESONSE CODE :[00]");

                                try {
                                    autorisationService.logMessage(file, "update etat demande : SW_PAYE ...");

                                    dmd.setEtatDemande("SW_PAYE");
                                    dmd.setDemCvv("");
                                    dmd = demandePaiementService.save(dmd);
                                    autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");
                                } catch (Exception e) {
                                    autorisationService.logMessage(file,
                                            "authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
                                                    + orderid + "]" + Util.formatException(e));
                                }

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
                                            "authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
                                                    + orderid + "]" + Util.formatException(e));
                                    demandeDtoMsg.setMsgRefus(
                                            "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                                    model.addAttribute("demandeDto", demandeDtoMsg);
                                    page = "result";
                                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                    logger.info("Fin processRequestMobile ()");
                                    return page;
                                }
                                autorisationService.logMessage(file,
                                        "update Demandepaiement status to SW_REJET OK.");
                                // TODO: 2024-02-27
                                try {
                                    if (hist.getId() == null) {
                                        // TODO: get histoauto check if exist
                                        HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
                                        if (histToAnnulle != null) {
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
                                            "authorization 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
                                                    + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
                                }
                                autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
                                // TODO: 2024-02-27
                            }

                            // TODO: JSONObject jso = new JSONObject();

                            autorisationService.logMessage(file, "Preparing autorization api response");

                            String authnumber = "";
                            String coderep = "";
                            String motif = "";
                            String merchnatidauth = "";
                            String dtdem = "";
                            String frais = "";
                            String montantSansFrais = "";
                            String data = "";
                            try {
                                authnumber = hist.getHatNautemt();
                                coderep = hist.getHatCoderep();
                                motif = hist.getHatMtfref1();
                                merchnatidauth = hist.getHatNumcmr();
                                dtdem = dmd.getDemPan();
                                transactionid = String.valueOf(hist.getHatNumdem());
                                montantSansFrais = String.valueOf(dmd.getMontant());
                                frais = String.valueOf(dmd.getFrais());
                                autorisationService.logMessage(file, "frais :[" + frais + "]");
                            } catch (Exception e) {
                                autorisationService.logMessage(file,
                                        "authorization 500 Error during authdata preparation orderid:[" + orderid + "]"
                                                + Util.formatException(e));
                            }

                            try {
                                String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
                                        + email + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto="
                                        + coderep + "&numAuto=" + authnumber + "&numCarte="
                                        + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
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
                                            + email + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto="
                                            + coderep + "&numAuto=" + authnumber + "&numCarte="
                                            + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
                                            + "&numTrans=" + transactionid;

                                    autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
                                    // TODO : If the length is still greater than 200, reduce the length of email
                                    if (data_noncrypt.length() > 200 && !email.isEmpty()) {
                                        email = email.length() > 10 ? email.substring(0, 10) : email;
                                    }

                                    // TODO : Rebuild again with the updated email
                                    data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
                                            + email + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto="
                                            + coderep + "&numAuto=" + authnumber + "&numCarte="
                                            + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
                                            + "&numTrans=" + transactionid;

                                    autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
                                }

                                String plainTxtSignature = orderid + current_infoCommercant.getClePub();

                                autorisationService.logMessage(file, "plainTxtSignature : " + plainTxtSignature);
                                logger.info("plainTxtSignature : " + plainTxtSignature);

                                data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt,
                                        current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

                                autorisationService.logMessage(file, "data encrypt : " + data);
                                logger.info("data encrypt : " + data);

                            } catch (Exception jsouterr) {
                                autorisationService.logMessage(file,
                                        "authorization 500 Error during jso out processing given authnumber:["
                                                + authnumber + "]" + jsouterr);
                                autorisationService.logMessage(file,
                                        "Erreur lors du traitement de sortie, transaction abouti redirection to SuccessUrl");
                            }
                            if (coderep.equals("00")) {
                                autorisationService.logMessage(file,
                                        "coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
                                autorisationService.logMessage(file,"?data=" + data + "==&codecmr=" + merchantid);
                                if (dmd.getSuccessURL() != null) {
                                    response.sendRedirect(
                                            dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
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
                                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                    logger.info("Fin processRequestMobile ()");
                                    return page;
                                }
                            } else {
                                autorisationService.logMessage(file,
                                        "coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
                                demandeDtoMsg.setMsgRefus(
                                        "La transaction en cours n’a pas abouti (" + s_status + "),"
                                                + " votre compte ne sera pas débité, merci de réessayer.");
                                model.addAttribute("demandeDto", demandeDtoMsg);
                                page = "result";
                                response.sendRedirect(dmd.getFailURL());
                                autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                logger.info("Fin processRequestMobile ()");
                                return null;
                                //return page;
                            }
                        } else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
                            try {
                                autorisationService.logMessage(file,
                                        "2eme chalenge apres auth acs => Redirect to failURL : " + dmd.getFailURL());
                                response.sendRedirect(dmd.getFailURL());

                                autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                logger.info("Fin processRequestMobile ()");

                                return null;
                            } catch (Exception ex) {
                                autorisationService.logMessage(file,
                                        "authorization 500 Error during jso out processing " + Util.formatException(ex));
                                demandeDtoMsg.setMsgRefus(
                                        "La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer.");
                                model.addAttribute("demandeDto", demandeDtoMsg);
                                page = "result";
                                autorisationService.logMessage(file, "Fin processRequestMobile ()");
                                logger.info("Fin processRequestMobile ()");
                                return page;
                            }
                        } else if (reponseMPI.equals("E")) {
                            // TODO: ********************* Cas responseMPI equal E
                            // TODO: *********************
                            autorisationService.logMessage(file, "****** Cas responseMPI equal E ******");
                            autorisationService.logMessage(file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
                            page = autorisationService.handleMpiError(errmpi, file, idDemande, threeDSServerTransID, dmd, model, page);

                        } else {
                            page = autorisationService.handleMpiError(errmpi, file, idDemande, threeDSServerTransID, dmd, model, page);
                        }
                    } else {
                        idDemande = threeDsecureResponse.getIdDemande() == null ? "" : threeDsecureResponse.getIdDemande();
                        dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));
                        dmd.setEtatDemande("AUTH_ACS_FAILED");
                        dmd.setDemxid(threeDSServerTransID);
                        // TODO: stackage de eci dans le chmp date_sendMPI vu que ce chmp nest pas utilisé
                        dmd.setDateSendMPI(eci);
                        // TODO: stackage de cavv dans le chmp date_SendSWT vu que ce chmp nest pas utilisé
                        dmd.setDateSendSWT(cavv);
                        dmd.setDemCvv("");
                        demandePaiementService.save(dmd);
                        autorisationService.logMessage(file,
                                "if(eci!=05) || eci!=02|| eci!=06 || eci!=01) : arret du processus ");
                        demandeDtoMsg.setMsgRefus(
                                "La transaction en cours n’a pas abouti (Authentification failed), votre compte ne sera pas débité, merci de réessayer.");
                        model.addAttribute("demandeDto", demandeDtoMsg);
                        page = "result";
                        autorisationService.logMessage(file, "Fin processRequestMobile ()");
                        logger.info("Fin processRequestMobile ()");
                        return page;
                    }
                } else {
                    autorisationService.logMessage(file, "threeDsecureResponse null");
                    demandeDtoMsg.setMsgRefus(
                            "La transaction en cours n’a pas abouti (Authentification failed), votre compte ne sera pas débité, merci de réessayer.");
                    model.addAttribute("demandeDto", demandeDtoMsg);
                    page = "result";
                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                    logger.info("Fin processRequestMobile ()");
                    return page;
                }
            } else {
                autorisationService.logMessage(file,
                        "ACSController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
                logger.info(
                        "ACSController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
                DemandePaiementDto demandeP = new DemandePaiementDto();
                autorisationService.logMessage(file,
                        "ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());
                logger.info(
                        "ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());

                demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());

                if (demandeP != null) {

                    demandeP.setEtatDemande("RETOUR_ACS_NON_AUTH");
                    demandePaiementService.save(demandeP);

                    msgRefus = "";

                    autorisationService.logMessage(file,
                            "TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());
                    logger.info(
                            "TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());

                    msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
                            + "), votre compte ne sera pas débité, merci de réessayer.";

                    demandeDtoMsg.setMsgRefus(msgRefus);
                    model.addAttribute("demandeDto", demandeDtoMsg);
                    page = "result";
                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                    logger.info("Fin processRequestMobile ()");
                    return page;
                } else {
                    msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
                            + "), votre compte ne sera pas débité, merci de réessayer.";
                    demandeDtoMsg.setMsgRefus(msgRefus);
                    model.addAttribute("demandeDto", demandeDtoMsg);
                    page = "result";
                    autorisationService.logMessage(file, "Fin processRequestMobile ()");
                    logger.info("Fin processRequestMobile ()");
                    return page;
                }
            }
        } catch (Exception ex) {
            autorisationService.logMessage(file, "ACSController RETOUR ACS =====> Exception " + Util.formatException(ex));
            logger.info("ACSController RETOUR ACS =====> Exception " + Util.formatException(ex));
            msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
                    + "), votre compte ne sera pas débité, merci de réessayer.";
            demandeDtoMsg.setMsgRefus(msgRefus);
            model.addAttribute("demandeDto", demandeDtoMsg);
            page = "result";
            autorisationService.logMessage(file, "Fin processRequestMobile ()");
            logger.info("Fin processRequestMobile ()");
            return page;
        }
        autorisationService.logMessage(file, "Fin processRequestMobile ()");
        logger.info("Fin processRequestMobile ()");

        return page;
    }

    @PostMapping(value = "/napspayment/linkCCB", consumes = "application/json", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("all")
    public String getLinkCCB(@RequestHeader MultiValueMap<String, String> header, @RequestBody String linkP,
                             HttpServletResponse response) {
        randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
        String file = "MB_LINK_CCB_" + randomWithSplittableRandom;
        // TODO: create file log
        Util.creatFileTransaction(file);
        autorisationService.logMessage(file, "*********** Start getLinkCCB() ************** ");
        logger.info("*********** Start getLinkCCB() ************** ");

        autorisationService.logMessage(file, "getLinkCCB api call start ...");
        autorisationService.logMessage(file, "getLinkCCB : [" + linkP + "]");

        DemandePaiementDto dmd = null;
        DemandePaiementDto dmdSaved = null;
        SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
        Date trsdate = null;
        Integer Idmd_id = null;

        LinkRequestDto linkRequestDto;

        try {
            linkRequestDto = new ObjectMapper().readValue(linkP, LinkRequestDto.class);
        } catch (JsonProcessingException e) {
            autorisationService.logMessage(file, "getLinkCCB 500 malformed json expression " + linkP + Util.formatException(e));
            return Util.getMsgError(folder, file, null, "getLinkCCB 500 malformed json expression", null);
        }

        String amount, transactiondate, transactiontime, idDemande, lname ="", fname ="", websiteid ="", id_client = "", token ="";

        String url = "", status = "", statuscode = "";
        JSONObject jso = new JSONObject();

        try {
            Double montant = 0.00;
            amount = Util.sanitizeAmount(linkRequestDto.getAmount());
            montant = Double.valueOf(amount);
            if (montant < 5) {
                url = "";
                statuscode = "17";
                status = "The amount must be greater than or equal to 5dh";

                jso.put("statuscode", statuscode);
                jso.put("status", status);
                jso.put("orderid", linkRequestDto.getOrderid());
                jso.put("amount", amount);
                jso.put("url", url);
                return jso.toString();
            }
        } catch (Exception e) {
            autorisationService.logMessage(file, "The amount must be greater than or equal to 5dh" + Util.formatException(e));
            return Util.getMsgError(folder, file, linkRequestDto, "The amount must be greater than or equal to 5dh" + e.getMessage(),
                    null);
        }

        CommercantDto current_merchant = null;
        try {
            current_merchant = commercantService.findByCmrNumcmr(linkRequestDto.getMerchantid());
        } catch (Exception e) {
            autorisationService.logMessage(file,
                    "authorization 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(e));

            return Util.getMsgError(folder, file, linkRequestDto,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
        }

        if (current_merchant == null) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

            return Util.getMsgError(folder, file, linkRequestDto,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
        }

        if (current_merchant.getCmrCodactivite() == null) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

            return Util.getMsgError(folder, file, linkRequestDto,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
        }

        if (current_merchant.getCmrCodbqe() == null) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

            return Util.getMsgError(folder, file, linkRequestDto,
                    "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
        }

        GalerieDto galerie = null;

        try {
            galerie = galerieService.findByCodeCmr(linkRequestDto.getMerchantid());
        } catch (Exception e) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

            return Util.getMsgError(folder, file, linkRequestDto,
                    "getLinkCCB 500 Galerie misconfigured in DB or not existing", "15");
        }

        if (galerie == null) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

            return Util.getMsgError(folder, file, linkRequestDto,
                    "getLinkCCB 500 Galerie misconfigured in DB or not existing", "15");
        }
        if (!linkRequestDto.getWebsiteid().equals(galerie.getCodeGal())) {
            websiteid = galerie.getCodeGal();
        } else {
            websiteid = linkRequestDto.getWebsiteid();
        }

        DemandePaiementDto check_dmd = null;

        try {
            check_dmd = demandePaiementService.findByCommandeAndComid(linkRequestDto.getOrderid(), linkRequestDto.getMerchantid());

        } catch (Exception err1) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err1));

            return Util.getMsgError(folder, file, linkRequestDto, "getLinkCCB 500 Error during PaiementRequest", null);
        }
        if (check_dmd != null && check_dmd.getEtatDemande().equals("SW_PAYE")) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:[" + linkRequestDto.getOrderid()
                            + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

            return Util.getMsgError(folder, file, linkRequestDto, "getLinkCCB 500 Error Already exist in PaiementRequest",
                    "16");
        }

        try {
            String tokencommande = "";
            if (check_dmd != null) {
                // TODO: generer token
                tokencommande = Util.genTokenCom(check_dmd.getCommande(), check_dmd.getComid());
                url = "";
                statuscode = "17";
                status = "PaiementRequest Already exist orderid:[" + linkRequestDto.getOrderid() + "]";
                idDemande = String.valueOf(check_dmd.getIddemande());
            } else {
                dmd = new DemandePaiementDto();

                dmd.setComid(linkRequestDto.getMerchantid());
                dmd.setCommande(linkRequestDto.getOrderid());
                dmd.setCartenaps(linkRequestDto.getCartenaps());
                dmd.setDateexpnaps(linkRequestDto.getDateexpnaps());
                dmd.setGalid(websiteid);
                dmd.setSuccessURL(linkRequestDto.getSuccessURL());
                dmd.setFailURL(linkRequestDto.getFailURL());
                dmd.setCallbackURL(linkRequestDto.getCallbackurl());
                amount = Util.sanitizeAmount(amount);
                dmd.setMontant(Double.parseDouble(amount));
                // TODO: calcule des frais de recharge
                String frais = fraisCCB;
                if (frais.equals("") || frais == null) {
                    frais = "0.00";
                }
                autorisationService.logMessage(file, "fraisCCB : " + frais + "%");
                Double fraisD = Double.valueOf(frais);

                Double montantrecharge = ((Double.parseDouble(amount) * fraisD) / 100);
                //Double montantrecharge = (0 + (Double.parseDouble(amount) * 0.65) / 100);
                String fraistr = String.format("%.2f", montantrecharge).replace(",", ".");
                autorisationService.logMessage(file, "FraisMontantRecharge : " + fraistr);

                dmd.setFrais(Double.parseDouble(fraistr));
                lname = linkRequestDto.getLname();
                if (lname.length() > 25) {
                    lname = linkRequestDto.getLname().substring(0, 25);
                }
                dmd.setNom(lname);
                fname = linkRequestDto.getFname();
                if (fname.length() > 20) {
                    fname = fname.substring(0, 20);
                }
                dmd.setPrenom(fname);
                dmd.setEmail(linkRequestDto.getEmail());
                dmd.setTel(linkRequestDto.getPhone());
                dmd.setAddress(linkRequestDto.getAddress());
                dmd.setCity(linkRequestDto.getCity());
                dmd.setCountry(linkRequestDto.getCountry());
                dmd.setState(linkRequestDto.getState());
                dmd.setPostcode(linkRequestDto.getZipcode());
                dmd.setLangue("E");
                dmd.setEtatDemande("INIT");

                formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
                formatter_2 = new SimpleDateFormat("HH:mm:ss");
                trsdate = new Date();
                transactiondate = formatter_1.format(trsdate);
                transactiontime = formatter_2.format(trsdate);
                dmd.setDemDateTime(dateFormat.format(new Date()));

                id_client = linkRequestDto.getId_client() == null ? "" : linkRequestDto.getId_client();
                token = linkRequestDto.getToken() == null ? "" : linkRequestDto.getToken();

                dmd.setIdClient(id_client);
                dmd.setToken(token);

                if (!id_client.equalsIgnoreCase("") || !token.equalsIgnoreCase("")) {
                    dmd.setIsCof("Y");
                } else {
                    dmd.setIsCof("N");
                }
                dmd.setIsAddcard("N");
                dmd.setIsTokenized("N");
                dmd.setIsWhitelist("N");
                dmd.setIsWithsave("N");

                // TODO: generer token
                tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
                dmd.setTokencommande(tokencommande);

                dmdSaved = demandePaiementService.save(dmd);

                url = linkCcb + dmdSaved.getTokencommande();
                statuscode = "00";
                status = "OK";
                idDemande = String.valueOf(dmdSaved.getIddemande());
            }

        } catch (Exception err1) {
            url = "";
            statuscode = "";
            status = "KO";
            idDemande = "";
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + linkRequestDto.getOrderid() + "]"
                            + Util.formatException(err1));

            return Util.getMsgError(folder, file, linkRequestDto, "getLinkCCB 500 Error during DEMANDE_PAIEMENT insertion",
                    null);
        }

        try {
            // TODO: Transaction info
            jso.put("statuscode", statuscode);
            jso.put("status", status);
            jso.put("orderid", linkRequestDto.getOrderid());
            jso.put("amount", amount);
            jso.put("idDemande", idDemande);
            jso.put("url", url);

            // TODO: Merchant info
            jso.put("merchantid", linkRequestDto.getMerchantid());

            autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
            logger.info("json res : [" + jso.toString() + "]");

        } catch (Exception err8) {
            autorisationService.logMessage(file,
                    "getLinkCCB 500 Error during jso out processing given orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(err8));

            return Util.getMsgError(folder, file, linkRequestDto, "getLinkCCB 500 Error during jso out processing", null);
        }

        autorisationService.logMessage(file, "*********** End getLinkCCB() ************** ");
        logger.info("*********** End getLinkCCB() ************** ");

        return jso.toString();

    }

    @RequestMapping(value = "/napspayment/authorization/ccb/token/{token}", method = RequestMethod.GET)
    @SuppressWarnings("all")
    public String showPageRchg(@PathVariable(value = "token") String token, Model model, HttpSession session) {
        randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
        String file = "MB_PAGE_CCB_" + randomWithSplittableRandom;
        // TODO: create file log
        Util.creatFileTransaction(file);
        autorisationService.logMessage(file, "*********** Start affichage page ccb ***********");

        autorisationService.logMessage(file, "findByTokencommande token : " + token);

        DemandePaiementDto demandeDto = new DemandePaiementDto();
        CommercantDto merchant = null;
        GalerieDto galerie = null;
        String merchantid = "";
        String orderid = "";

        String page = "erecharge";

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
                            "La transaction en cours est déjà effectuée, votre compte ne sera pas débité.");
                    model.addAttribute("demandeDto", demandeDto);
                    page = "operationEffectue";
                } else if (demandeDto.getEtatDemande().equals("SW_REJET")) {
                    autorisationService.logMessage(file, "Transaction rejetée");
                    demandeDto.setMsgRefus(
                            "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
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
                    "showPageRchg 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token + "]" + Util.formatException(e));

            autorisationService.logMessage(file, "showPageRchg 500 exception" + Util.formatException(e));
            logger.error("Exception : " , e);
            demandeDto = new DemandePaiementDto();
            demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
            model.addAttribute("demandeDto", demandeDto);
            page = "result";
        }

        // TODO: TODO: gestion expiration de la session on stoque la date en millisecond
        session.setAttribute("paymentStartTime", System.currentTimeMillis());
        autorisationService.logMessage(file, "paymentStartTime : " + System.currentTimeMillis());
        demandeDto.setTimeoutURL(String.valueOf(System.currentTimeMillis()));

        if (page.equals("erecharge")) {
            if (demandeDto.getEtatDemande().equals("INIT")) {
                demandeDto.setEtatDemande("P_CHRG_OK");
                demandePaiementService.save(demandeDto);
                autorisationService.logMessage(file, "update Demandepaiement status to P_CHRG_OK");
            }
        }

        autorisationService.logMessage(file, "*********** End affichage page ccb ************** ");

        return page;
    }

    @PostMapping("/recharger")
    @SuppressWarnings("all")
    public String recharger(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto,
                            HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
        String file = "MB_RECHARGER_" + randomWithSplittableRandom;
        // TODO: create file log
        Util.creatFileTransaction(file);
        autorisationService.logMessage(file, "*********** Start recharger () ************** ");

        String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
                merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
                fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
                acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
                transactiondate, transactiontime, date, rrn, heure, montanttrame, montantRechgtrame, cartenaps,
                dateExnaps, num_trs = "", successURL, failURL, transactiontype, idclient;

        DemandePaiementDto demandeDto = new DemandePaiementDto();
        Objects.copyProperties(demandeDto, dto);
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
                if (!demandeDto.getAnnee().equals("") && !demandeDto.getMois().equals("")) {
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
            // TODO: expirydate = demandeDto.getAnnee().substring(2,
            // TODO: 4).concat(demandeDto.getMois());
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
            autorisationService.logMessage(file, "recharger 500 malformed json expression" + Util.formatException(jerr));
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
            // TODO: dmdToEdit.setDateexpnaps(expirydate);
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
                    "recharger 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + Util.formatException(err1));
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
                    "recharger 500 ControlRiskCmr misconfigured in DB or not existing merchantid:["
                            + demandeDto.getComid() + Util.formatException(e));
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
                autorisationService.logMessage(file, "cardtokenDto expirydate formated : " + expirydateFormated);
                Date dateExp;
                dateExp = dateFormatSimple.parse(expirydateFormated);

                if (checkCardNumber.size() == 0) {
                    // TODO: insert new cardToken
                    String tokencard = Util.generateCardToken(idclient);

                    // TODO: test if token not exist in DB
                    CardtokenDto checkCardToken = cardtokenService.findByIdMerchantAndToken(idclient, tokencard);

                    while (checkCardToken != null) {
                        tokencard = Util.generateCardToken(idclient);
                        autorisationService.logMessage(file,
                                "checkCardToken exist => generate new tokencard : " + tokencard);
                        checkCardToken = cardtokenService.findByIdMerchantAndToken(merchantid, tokencard);
                    }
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
                    for (CardtokenDto crd : checkCardNumber) {
                        if (crd.getExprDate() != null) {
                            if (crd.getCardNumber().equals(cardnumber)) {
                                if (crd.getExprDate().before(dateToken)) {
                                    autorisationService.logMessage(file, "Encienne date expiration est expirée : "
                                            + dateFormatSimple.format(crd.getExprDate()));
                                    autorisationService.logMessage(file,
                                            "Update par nv date expiration saisie : " + expirydateFormated);
                                    crd.setExprDate(dateExp);
                                    CardtokenDto cardSaved = cardtokenService.save(crd);
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
            autorisationService.logMessage(file, "recharger 500 Error during  date formatting for given orderid:["
                    + orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
            demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
            model.addAttribute("demandeDto", demandeDtoMsg);
            page = "result";
            return page;
        }

        ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

        // TODO: appel 3DSSecure ***********************************************************

        /**
         * dans la preprod les tests sans 3DSS on commente l'appel 3DSS et on mj
         * reponseMPI="Y"
         */
        autorisationService.logMessage(file, "environement : " + environement);
        if (environement.equals("PREPROD")) {
            threeDsecureResponse.setReponseMPI("Y");
        } else {
            threeDsecureResponse = autorisationService.preparerReqMobileThree3DSS(demandeDto, folder, file);
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
            autorisationService.logMessage(file, "received idDemande from MPI is Null or Empty");
            demandeDto.setDemCvv("");
            demandeDto.setEtatDemande("MPI_KO");
            demandePaiementService.save(demandeDto);
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
            if (threeDSServerTransID != null && !threeDSServerTransID.equals("")) {
                dmd.setDemxid(threeDSServerTransID);
                dmd.setIs3ds("N");
                dmd = demandePaiementService.save(dmd);
            }
            cartenaps = dmd.getCartenaps();
            dateExnaps = dmd.getDateexpnaps();

            // TODO: 2024-03-05
            montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, dmd, model);

            // TODO: 2024-03-05
            montantRechgtrame = formatMontantRechargeTrame(folder, file, amount, orderid, merchantid, dmd, page, model);

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
                        "recharger 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

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
                    // TODO: tag 046 tlv info carte naps
                    String tlvCCB = new TLVEncoder().withField(Tags.tag1, cartenaps)
                            .withField(Tags.tag14, montantRechgtrame).withField(Tags.tag42, dateExnaps).encode();
                    // TODO: tlv total ccb
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
                            .withField(Tags.tag168, xid).withField(Tags.tag46, tlvCCB).encode();

                } catch (Exception err4) {
                    dmd.setDemCvv("");
                    demandePaiementService.save(dmd);
                    autorisationService.logMessage(file,
                            "recharger 500 Error during switch tlv buildup for given orderid:[" + orderid
                                    + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
                    demandeDtoMsg.setMsgRefus(
                            "La transaction en cours n’a pas abouti (Erreur lors de la création du switch tlv), votre compte ne sera pas débité, merci de réessayer.");
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
                            "recharger 500 Error Switch communication s_conn false switch ip:[" + sw_s
                                    + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
                    demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
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
                demandePaiementService.save(dmd);
                autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
                switch_ko = 1;
                autorisationService.logMessage(file, "recharger 500 Error Switch null response" + "switch ip:["
                        + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
                demandeDtoMsg.setMsgRefus(
                        "La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp null), votre compte ne sera pas débité, merci de réessayer.");
                model.addAttribute("demandeDto", demandeDtoMsg);
                page = "result";
                return page;
            }

            if (switch_ko == 0 && resp.length() < 3) {
                dmd.setDemCvv("");
                demandePaiementService.save(dmd);
                switch_ko = 1;

                autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
                autorisationService.logMessage(file, "recharger 500 Error Switch short response length() < 3 "
                        + "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
                demandeDtoMsg.setMsgRefus(
                        "La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp < 3 !!!), votre compte ne sera pas débité, merci de réessayer.");
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
                    autorisationService.logMessage(file, "recharger 500 Error during tlv Switch response parse"
                            + "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
                }

                // TODO: controle switch
                if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
                    autorisationService.logMessage(file, "Switch  malfunction !!! tag1_resp == null");
                    switch_ko = 1;
                    autorisationService.logMessage(file,
                            "recharger 500" + "Error during tlv Switch response parse tag1_resp length tag  < 3"
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

            try {
                // TODO: calcule du montant avec les frais
                amount = calculMontantTotalOperation(dmd);
            } catch (Exception ex) {
                autorisationService.logMessage(file, "calcule du montant avec les frais : " + Util.formatException(ex));
            }

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
                autorisationService.logMessage(file, "recharger 500 Error codeReponseDto null" + Util.formatException(ee));
            }
            autorisationService.logMessage(file, "get status Switch status : [" + s_status + "]");

            try {

                hist = new HistoAutoGateDto();
                Date curren_date_hist = new Date();
                int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

                websiteid = dmd.getGalid();

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
                if (websiteid.equals("")) {
                    websiteid = "0066";
                }
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
                        "recharger 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + Util.formatException(e));
                try {
                    autorisationService.logMessage(file, "2eme tentative : HistoAutoGate Saving ... ");
                    hist = histoAutoGateService.save(hist);
                } catch (Exception ex) {
                    autorisationService.logMessage(file,
                            "2eme tentative : recharger 500 Error during  insert in histoautogate for given orderid:["
                                    + orderid + "]" + Util.formatException(ex));
                }
            }

            autorisationService.logMessage(file, "HistoAutoGate OK.");

            if (tag20_resp == null) {
                tag20_resp = "";
            }

            if (tag20_resp.equalsIgnoreCase("00")) {
                autorisationService.logMessage(file, "SWITCH RESONSE CODE :[00]");

                try {
                    autorisationService.logMessage(file, "update etat demande : SW_PAYE ...");

                    dmd.setEtatDemande("SW_PAYE");
                    dmd.setDemCvv("");
                    dmd = demandePaiementService.save(dmd);
                    autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");
                } catch (Exception e) {
                    autorisationService.logMessage(file,
                            "recharger 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
                                    + orderid + "]" + Util.formatException(e));
                }

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
                            "recharger 500 Error during  DemandePaiement update SW_REJET for given orderid:[" + orderid
                                    + "]" + Util.formatException(e));
                    demandeDtoMsg.setMsgRefus(
                            "La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
                    model.addAttribute("demandeDto", demandeDtoMsg);
                    page = "result";
                    return page;
                }
                autorisationService.logMessage(file, "update Demandepaiement status to SW_REJET OK.");
                // TODO: 2024-02-27
                try {
                    if (hist.getId() == null) {
                        // TODO: get histoauto check if exist
                        HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
                        if (histToAnnulle != null) {
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
                            "recharger 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
                                    + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
                }
                autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
                // TODO: 2024-02-27
            }

            // TODO: JSONObject jso = new JSONObject();

            autorisationService.logMessage(file, "Preparing autorization api response");

            String authnumber = "", coderep = "", motif, merchnatidauth, dtdem = "", frais = "", montantSansFrais = "", data = "";

            try {
                authnumber = hist.getHatNautemt();
                coderep = hist.getHatCoderep();
                motif = hist.getHatMtfref1();
                merchnatidauth = hist.getHatNumcmr();
                dtdem = dmd.getDemPan();
                transactionid = String.valueOf(hist.getHatNumdem());
                montantSansFrais = String.valueOf(dmd.getMontant());
                frais = String.valueOf(dmd.getFrais());
                autorisationService.logMessage(file, "frais :[" + frais + "]");
            } catch (Exception e) {
                autorisationService.logMessage(file,
                        "recharger 500 Error during authdata preparation orderid:[" + orderid + "]" + Util.formatException(e));
            }

            // TODO: reccurent transaction processing

            // TODO: reccurent insert and update

            try {
                String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
                        + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto=" + coderep + "&numAuto="
                        + authnumber + "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
                        + "&numTrans=" + transactionid;

                autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);

                if (data_noncrypt.length() > 200) {
                    // TODO : First, try reducing the length by adjusting the fname
                    if (!fname.isEmpty()) {
                        fname = fname.length() > 10 ? fname.substring(0, 10) : fname;
                    }

                    // TODO : Rebuild the data_noncrypt string with the updated fname
                    data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
                            + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto=" + coderep + "&numAuto="
                            + authnumber + "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
                            + "&numTrans=" + transactionid;

                    autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
                    // TODO : If the length is still greater than 200, reduce the length of email
                    if (data_noncrypt.length() > 200 && !email.isEmpty()) {
                        email = email.length() > 10 ? email.substring(0, 10) : email;
                    }

                    // TODO : Rebuild again with the updated email
                    data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
                            + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto=" + coderep + "&numAuto="
                            + authnumber + "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getTypeCarte()
                            + "&numTrans=" + transactionid;

                    autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
                }

                String plainTxtSignature = orderid + current_infoCommercant.getClePub();

                autorisationService.logMessage(file, "plainTxtSignature : " + plainTxtSignature);

                data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt, current_infoCommercant.getClePub(),
                        plainTxtSignature, folder, file);

                autorisationService.logMessage(file, "data encrypt : " + data);

            } catch (Exception jsouterr) {
                autorisationService.logMessage(file,
                        "recharger 500 Error during jso out processing given authnumber:[" + authnumber + "]"
                                + jsouterr);
                autorisationService.logMessage(file,
                        "Erreur lors du traitement de sortie, transaction abouti redirection to SuccessUrl");
            }

            if (coderep.equals("00")) {
                autorisationService.logMessage(file,
                        "coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
                autorisationService.logMessage(file,"?data=" + data + "==&codecmr=" + merchantid);
                if (dmd.getSuccessURL() != null) {
                    response.sendRedirect(dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
                    autorisationService.logMessage(file, "Fin recharger ()");
                    return  null;
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
                    autorisationService.logMessage(file, "Fin recharger ()");
                    return page;
                }
            } else {
                autorisationService.logMessage(file,
                        "coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());

                demandeDtoMsg.setMsgRefus(
                        "La transaction en cours n’a pas abouti (" + s_status + ")," + " votre compte ne sera pas débité, merci de réessayer.");
                model.addAttribute("demandeDto", demandeDtoMsg);
                page = "result";
                response.sendRedirect(dmd.getFailURL());
                autorisationService.logMessage(file, "Fin recharger ()");
                return  null;
            }

            // TODO: fin
            // TODO: *******************************************************************************************************************
        } else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
            // TODO: ********************* Cas chalenge responseMPI equal C ou D
            // TODO: *********************
            autorisationService.logMessage(file, "****** Cas chalenge responseMPI equal C ou D ******");
            try {
                dmd.setCreq(threeDsecureResponse.getHtmlCreq());
                if (threeDSServerTransID.equals("") || threeDSServerTransID == null) {
                    threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
                }
                dmd.setDemxid(threeDSServerTransID);
                dmd.setEtatDemande("SND_TO_ACS");
                dmd.setIs3ds("Y");
                demandeDto = demandePaiementService.save(dmd);
                autorisationService.logMessage(file, "threeDSServerTransID : " + demandeDto.getDemxid());
                model.addAttribute("demandeDto", demandeDto);
                // TODO: 2024-06-27 old
			/*page = "chalenge";

			autorisationService.logMessage(file, "set demandeDto model creq : " + demandeDto.getCreq());
			autorisationService.logMessage(file, "return page : " + page);*/

                // TODO: 2024-06-27
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
                    autorisationService.logMessage(file, "L'URL ACS est : " + acsUrl);
                    autorisationService.logMessage(file, "La valeur de creq est : " + creq);

                    String decodedCreq = new String(Base64.decodeBase64(creq.getBytes()));
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

                    autorisationService.logMessage(file, "Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");

                    return null;  // TODO: Terminer le traitement ici après avoir envoyé le formulaire
                } else {
                    autorisationService.logMessage(file, "Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
                    page = "error";  // TODO: Définir la page d'erreur appropriée
                }

                // TODO: 2024-06-27
            } catch (Exception ex) {
                autorisationService.logMessage(file, "Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML " + Util.formatException(ex));
                demandeDtoMsg.setMsgRefus(
                        "La transaction en cours n’a pas abouti (Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML), votre compte ne sera pas débité, merci de réessayer.");
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

        logger.info("demandeDto htmlCreq : " + demandeDto.getCreq());
        logger.info("return page : " + page);

        autorisationService.logMessage(file, "*********** End recharger () ************** ");
        logger.info("*********** End recharger () ************** ");

        return page;
    }

    public String calculMontantTotalOperation(DemandePaiementDto dto) {
        if (dto.getMontant() == null) {
            dto.setMontant(0.00);
        }
        if (dto.getFrais() == null) {
            dto.setFrais(0.00);
        }
        double mnttotalopp = dto.getMontant() + dto.getFrais();
        return String.format("%.2f", mnttotalopp).replace(",", ".");
    }

    public String calculMontantSansOperation(DemandePaiementDto dto) {
        if (dto.getMontant() == null) {
            dto.setMontant(0.00);
        }
        return String.format("%.2f", dto.getMontant()).replace(",", ".");
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
            return d; // TODO: leave it as it is if not null
        }
        return dNotime;

    }

    @SuppressWarnings("all")
    private String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid,
                                      DemandePaiementDto dmd, Model model) {
        String montanttrame = "";
        String[] mm;
        String[] m;
        DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
        try {
            amount = calculMontantTotalOperation(dmd);

            if (amount.contains(",")) {
                amount = amount.replace(",", ".");
            }
            if (!amount.contains(".") && !amount.contains(",")) {
                amount = amount + "." + "00";
            }
            //logger.info("montant recharge avec frais : [" + amount + "]");
            autorisationService.logMessage(file,
                    "montant recharge avec frais : [" + amount + "]");

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
            autorisationService.logMessage(file, "Fin processRequestMobile ()");
            logger.info("Fin processRequestMobile ()");
            return page0;
        }
        return montanttrame;
    }

    @SuppressWarnings("all")
    private String formatMontantRechargeTrame(String folder, String file, String amount, String orderid, String merchantid,
                                              DemandePaiementDto dmd, String page, Model model) {
        String montantRechgtrame;
        String[] mm;
        String[] m;
        DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
        try {
            montantRechgtrame = "";

            String amount1 = calculMontantSansOperation(dmd);

            if (amount1.contains(",")) {
                amount1 = amount1.replace(",", ".");
            }
            if (!amount1.contains(".") && !amount1.contains(",")) {
                amount1 = amount1 + "." + "00";
            }
            //logger.info("montant recharge sans frais : [" + amount1 + "]");
            autorisationService.logMessage(file,
                    "montant recharge sans frais : [" + amount1 + "]");

            String montantt = amount1 + "";

            mm = montantt.split("\\.");
            if (mm[1].length() == 1) {
                montantRechgtrame = amount1 + "0";
            } else {
                montantRechgtrame = amount1 + "";
            }

            m = montantRechgtrame.split("\\.");
            if (m[1].equals("0")) {
                montantRechgtrame = montantRechgtrame.replace(".", "0");
            } else
                montantRechgtrame = montantRechgtrame.replace(".", "");
            montantRechgtrame = Util.formatageCHamps(montantRechgtrame, 12);
            //logger.info("montantRechgtrame sans frais: [" + montantRechgtrame + "]");
            autorisationService.logMessage(file,
                    "montantRechgtrame sans frais : [" + montantRechgtrame + "]");
        } catch (Exception err3) {
            autorisationService.logMessage(file,
                    "recharger 500 Error during  amount formatting for given orderid:[" + orderid
                            + "] and merchantid:[" + merchantid + "]" + Util.formatException(err3));
            demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
            model.addAttribute("demandeDto", demandeDtoMsg);
            page = "result";
            return page;
        }
        return montantRechgtrame;
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
            monthNames.add(month.toString()); // TODO: Convert Month to its string representation
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
            // TODO: Convert Month to its string representation
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
            // TODO: String expirydateFormated = "2020" + "-" + "05" + "-" + "01";
            autorisationService.logMessage(file, "cardtokenDto expirydate formated : " + expirydateFormated);
            Date dateExp = dateFormatSimple.parse(expirydateFormated);
            if (dateExp.before(dateToken)) {
                logger.info("date exiration est inferieur à la date systeme : " + dateExp + " < " + dateToken);
                autorisationService.logMessage(file,
                        "date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
                carte.setMoisValue("xxxx");
                carte.setMois("xxxx");
                carte.setYear(1111);
            }
        } catch (Exception e) {
            logger.error("Exception : " , e);
        }
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
}
