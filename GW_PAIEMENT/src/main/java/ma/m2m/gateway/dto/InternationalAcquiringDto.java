package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InternationalAcquiringDto {
	
	
	private String numCommercant;
	
	private String isIntAcquiringActive;

}
