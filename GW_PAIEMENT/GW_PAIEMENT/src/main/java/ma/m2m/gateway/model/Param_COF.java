package ma.m2m.gateway.model;

import java.io.Serializable;

public class Param_COF  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	
	private Integer id;
	private String idMerchant;
	private String is_cof;
	private String is_3ds_normal;
	private String is_3ds_cof;
	private String is_cvv_normal;
	private String is_cvv_cof;
	private String multi_select;
	private int prf_token;
	
	
	public Param_COF() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getIdMerchant() {
		return idMerchant;
	}


	public void setIdMerchant(String idMerchant) {
		this.idMerchant = idMerchant;
	}


	public String getIs_cof() {
		return is_cof;
	}


	public void setIs_cof(String is_cof) {
		this.is_cof = is_cof;
	}


	public String getIs_3ds_normal() {
		return is_3ds_normal;
	}


	public void setIs_3ds_normal(String is_3ds_normal) {
		this.is_3ds_normal = is_3ds_normal;
	}


	public String getIs_3ds_cof() {
		return is_3ds_cof;
	}


	public void setIs_3ds_cof(String is_3ds_cof) {
		this.is_3ds_cof = is_3ds_cof;
	}


	public String getIs_cvv_normal() {
		return is_cvv_normal;
	}


	public void setIs_cvv_normal(String is_cvv_normal) {
		this.is_cvv_normal = is_cvv_normal;
	}


	public String getIs_cvv_cof() {
		return is_cvv_cof;
	}


	public void setIs_cvv_cof(String is_cvv_cof) {
		this.is_cvv_cof = is_cvv_cof;
	}


	public String getMulti_select() {
		return multi_select;
	}


	public void setMulti_select(String multi_select) {
		this.multi_select = multi_select;
	}


	public int getPrf_token() {
		return prf_token;
	}


	public void setPrf_token(int prf_token) {
		this.prf_token = prf_token;
	}


	public Param_COF(Integer id, String idMerchant, String is_cof, String is_3ds_normal, String is_3ds_cof,
			String is_cvv_normal, String is_cvv_cof, String multi_select, int prf_token) {
		super();
		this.id = id;
		this.idMerchant = idMerchant;
		this.is_cof = is_cof;
		this.is_3ds_normal = is_3ds_normal;
		this.is_3ds_cof = is_3ds_cof;
		this.is_cvv_normal = is_cvv_normal;
		this.is_cvv_cof = is_cvv_cof;
		this.multi_select = multi_select;
		this.prf_token = prf_token;
	}


	@Override
	public String toString() {
		return "Param_COF [id=" + id + ", idMerchant=" + idMerchant + ", is_cof=" + is_cof + ", is_3ds_normal="
				+ is_3ds_normal + ", is_3ds_cof=" + is_3ds_cof + ", is_cvv_normal=" + is_cvv_normal + ", is_cvv_cof="
				+ is_cvv_cof + ", multi_select=" + multi_select + ", prf_token=" + prf_token + "]";
	}



	
	
	
	

	

}
