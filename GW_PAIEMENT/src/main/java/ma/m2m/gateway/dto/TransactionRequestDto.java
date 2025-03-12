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
public class TransactionRequestDto {

    private String orderid;

    private String amount;

    private String transactionid;

    private String paymentid;

    private String authnumber;

    private String merchantid;

    private String merchantname;

    private String websiteName;

    private String websiteid;

    private String callbackurl;

    private String cardnumber;

    private String token;

    private String expirydate;

    private String cvv;

    private String fname;

    private String lname;

    private String email;

    private String securtoken24;

    private String mac_value;

}
