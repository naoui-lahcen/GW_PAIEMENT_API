package ma.m2m.gateway.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-01-10
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkRequestDto {

    private String capture;
    private String merchantid;
    private String orderid;
    private String websiteid;
    private String currency;
    private String promocode;
    private String merchantname;
    private String websitename;
    private String amount;
    private String transactionid;
    private String transactiontype;
    private String fname;
    private String lname;
    private String email;
    private String id_client;
    private String token;
    private String cartenaps;
    private String dateexpnaps;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String state;
    private String zipcode;
    private String successURL;
    private String failURL;
    private String callbackurl;
    private String recurring;
    private String cardnumber;
    private String expirydate;
    private String holdername;
    private String cvv;
    private String auth3ds;
    private String securtoken24;
    private String mac_value;

}
