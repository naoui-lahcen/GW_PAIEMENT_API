package ma.m2m.gateway.tlv;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.leftPad;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import hirondelle.date4j.DateTime;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

/**
 * Class responsible for encoding many data fields into TLV format
 */
public class TLVEncoder {
	
	/**
	 * Holds all fields keeping the their insertion order
	 */
	private final Map<String, String> fields = new LinkedHashMap<>();

	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>String</code>
	 * 
	 * @param tagName the tag name
	 * @param value   the string value
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, String value) {
		addField(tagName, value);
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>Integer</code>
	 * 
	 * @param tagName the tag name
	 * @param value   the integer value
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, Integer value) {
		addField(tagName, value);
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>Integer</code> and left pad the value with zeros
	 * 
	 * @param tagName     the tag name
	 * @param value       the integer value
	 * @param leftPadding the size to pad to
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, Integer value, int leftPadding) {
		addField(tagName, leftPaddingWithZeros(value, leftPadding));
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>Long</code>
	 * 
	 * @param tagName the tag name
	 * @param value   the long value
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, Long value) {
		addField(tagName, value);
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>Long</code> and left pad the value with zeros
	 * 
	 * @param tagName     the tag name
	 * @param value       the long value
	 * @param leftPadding the size to pad to
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, Long value, int leftPadding) {
		addField(tagName, leftPaddingWithZeros(value, leftPadding));
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>BigDecimal</code>. All non-numeric characters are removed, like '.' or
	 * ',' for example
	 * 
	 * @param tagName the tag name
	 * @param value   the bigdecimal value
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, BigDecimal value) {
		addField(tagName, onlyNumbers(value));
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>BigDecimal</code> and left pad the value with zeros. All non numeric
	 * characters are removed, like '.' or ',' for example
	 * 
	 * @param tagName     the tag name
	 * @param value       the bigdecimal value
	 * @param leftPadding the size to pad to
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, BigDecimal value, int leftPadding) {
		
		String formatted = onlyNumbers(value);
		formatted = leftPaddingWithZeros(formatted, leftPadding);
		
		addField(tagName, formatted);
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>DateTime</code> and formats the value with the pattern 'MMDDhhmmss'
	 * 
	 * @param tagName the tag name
	 * @param value   the datatime value
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, DateTime value) {
		withField(tagName, value, "MMDDhhmmss");
		return this;
	}
	
	/**
	 * Adds a <code>tagName</code> with a <code>value</code> of type
	 * <code>DateTime</code> and formats the value with the specified
	 * <code>pattern</code>
	 * 
	 * @param tagName the tag name
	 * @param value   the datatime value
	 * @return returns this same instance
	 */
	public TLVEncoder withField(String tagName, DateTime value, String pattern) {
		String formatted = "";
		if (value != null) {
			formatted = value.format(pattern);
		}
		addField(tagName, formatted);
		return this;
	}
	
	/**
	 * Encodes all fields into a TLV text
	 */
	public String encode() {
		return join(fields.values(), "");
	}
	
	@Override
	public String toString() {
		return "TLVEncoder [fields=" + fields + "]";
	}
	
	/**
	 * Encodes <code>tagName</code> and its <code>value</code> into a TLV text
	 */
	private void addField(String tagName, Object value) {
		
		if (shouldIgnore(value)) return;
		
		validate(tagName, value);
		
		String strValue = value.toString();
		Integer length = strValue.length();
		String strLength = leftPad(length.toString(), 3, "0");
		
		String tlv = "{tag}{length}{value}"
				.replace("{tag}"   , tagName)
				.replace("{length}", strLength)
				.replace("{value}" , strValue);
		
		fields.put(tagName, tlv);
	}
	
	/**
	 * Verifies whether <code>tagValue</code> must be ignored or not
	 */
	private boolean shouldIgnore(Object tagValue) {
		if (tagValue == null) return true;
		if (isBlank(tagValue.toString())) return true;
		return false;
	}

	/**
	 * Validates <code>tagName</code> and <code>value</code> content
	 */
	private void validate(String tagName, Object value) {
		
		if (isBlank(tagName))
			throw new IllegalArgumentException("Tag name must not be null or empty");
		
		if (tagName.trim().length() != 3)
			throw new IllegalArgumentException("Tag name must be 3 characters long: " + tagName);
		
		if (nvl(value).length() > 999)
			throw new IllegalArgumentException("Tag value must be at most 999 characters long");
	}
	
	private String nvl(Object value) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}
	
	private String leftPaddingWithZeros(Object value, int size) {
		String v = nvl(value);
		if (isBlank(v)) {
			return null;
		}
		return leftPad(v, size, "0");
	}
	
	private String onlyNumbers(BigDecimal value) {
		String v = nvl(value);
		return v.replaceAll("[^0-9]", "");
	}

}
