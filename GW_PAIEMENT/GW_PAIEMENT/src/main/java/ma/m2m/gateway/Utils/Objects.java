package ma.m2m.gateway.Utils;

import org.springframework.beans.BeanUtils;

public class Objects {

	public static void copyProperties(Object dest, Object src) {

		BeanUtils.copyProperties(src, dest);
	}

}
