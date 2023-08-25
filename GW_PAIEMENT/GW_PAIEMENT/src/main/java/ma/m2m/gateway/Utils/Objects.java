package ma.m2m.gateway.Utils;

import org.springframework.beans.BeanUtils;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class Objects {

	public static void copyProperties(Object dest, Object src) {

		BeanUtils.copyProperties(src, dest);
	}

}
