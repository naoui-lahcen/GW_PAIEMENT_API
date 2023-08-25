package ma.m2m.gateway.tlv;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

/**
 * Class responsible for parsing an encoded-text in TLV (Tag-Length-Value) format
 */
public class TLVParser {
	
	private static final Logger logger = LogManager.getLogger(TLVParser.class);

	/**
	 * The minimum TLV size allowed
	 */
	private static final int MIN_TLV_SIZE = 7;
	
	/**
	 * Holds the original TLV encoded-text
	 */
	private final String tlv;
	/**
	 * Holds all tags keeping their insertion order
	 */
	private final Map<String, String> tlvData = new LinkedHashMap<>();

	
	/**
	 * Creates a parser for the specified <code>tlv</code>
	 * 
	 * @param tlv the TLV encoded-text that will be parsed
	 */
	public TLVParser(String tlv) {
		this.tlv = tlv;
		parses(tlv);
	}

	/**
	 * Parses TLV encoded-text into a Map
	 */
	private void parses(String tlv) {
		
		if (tlv == null) {
			logger.error("Invalid TLV data: can not be null");
			return;
		}
		
		if (tlv.length() < MIN_TLV_SIZE) {
			logger.error("Invalid TLV data: must be at least 7 characters long (tlv='" + tlv + "')");
			return;
		}
		
		try {
			int i = 0;
			while (true) {
				String tag = tlv.substring(i, i+3);
				int len = Integer.parseInt(tlv.substring(i+3, i+6));
				String value = tlv.substring(i+6, i+6+len);

				tlvData.put(tag, value);

				i = i+6+len;
				if (i >= tlv.length())
					break;
			}
		} catch (Exception e) {
			logger.error("Unexpected error while parsing TLV data: tlv='" + tlv + "'");
		}
	}
	
	/**
	 * Finds the tag value by the specified <code>tagName</code>
	 * 
	 * @param tagName the tag name
	 * @return the tag value or <code>null</code> if not found
	 */
	public String getTag(String tagName) {
		return tlvData.get(tagName);
	}
	
	/**
	 * Finds the tag value by the specified <code>tagName</code>. In case the tag is
	 * not found, returns the <code>defaultValue</code>
	 * 
	 * @param tagName      the tag name
	 * @param defaultValue the default value that will used in case a tag is not
	 *                     found
	 * @return the tag value or <code>defaultValue</code> if not found
	 */
	public String getTag(String tagName, String defaultValue) {
		String tagValue = getTag(tagName);
		if (tagValue == null) {
			return defaultValue;
		}
		return tagValue;
	}
	
	/**
	 * Returns the total of tags found
	 */
	public int getTotalOfTags() {
		return tlvData.size();
	}

	@Override
	public String toString() {
		return "TLVParser [tlv=" + tlv 
					  + ", totalOfTags=" + getTotalOfTags()
					  + ", tlvData=" + tlvData 
					  + "]";
	}

}
