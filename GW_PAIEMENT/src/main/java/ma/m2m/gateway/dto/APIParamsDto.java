package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-06-13
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class APIParamsDto {

    private long id;

    private String authorization;

    private String reversal;

    private String capture;

    private String status;

    private String refund;

    private String automaticCapture;

    private String reccuring;

    private String merchantID;

    private String version;

    private String product;
}
