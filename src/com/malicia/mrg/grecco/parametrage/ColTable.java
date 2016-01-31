package com.malicia.mrg.grecco.parametrage;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

	public class ColTable extends FunctionTransverse{
		String nom;
		int len;
		Boolean champTechnique;
		int position;
		String format;
		String valeurduchamp;
		private String nomTableDb2;

		public String format() {
			String ret;
			if (champTechnique) {
				switch (nom) {
				case "TSCRE": //$NON-NLS-1$
				case "TSMAJ": //$NON-NLS-1$
					len = 26;
					java.sql.Timestamp creationdate = new java.sql.Timestamp(new Date().getTime());
					ret = (creationdate.toString());
					ret = StringUtils.rightPad(ret, len, "0"); //$NON-NLS-1$
					ret = ret.replaceAll(" ", "-");
					break;
				case "IDUTICRE": //$NON-NLS-1$
				case "IDUTIMAJ": //$NON-NLS-1$
					ret = "ADMIN"; //$NON-NLS-1$
					break;
				case "IDPGMCRE": //$NON-NLS-1$
				case "IDPGMMAJ": //$NON-NLS-1$
					ret = "IMPORTXLS"; //$NON-NLS-1$
					break;
				default:
					ret = ":err:" + nom; //$NON-NLS-1$
					msg(":ERR: " + nomTableDb2 + " champTechnique '" + nom + "' non formatable"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					break;
				}
			} else {
				switch (format) {
				case "CHAR": //$NON-NLS-1$
					ret = string2string(valeurduchamp);
					break;
				case "SMALLINT": //$NON-NLS-1$
					ret = smallint2tostring(valeurduchamp);
					break;
				case "VARCHAR": //$NON-NLS-1$
					ret = string2string(valeurduchamp);
					len = 255;
					break;
				case "DECIMAL": //$NON-NLS-1$
					ret = tointerger(valeurduchamp);
					break;
				case "DATE": //$NON-NLS-1$
					ret = todate(valeurduchamp);
					break;
				default:
					ret = ":err:" + format; //$NON-NLS-1$
					msg(":ERR: " + nomTableDb2 + " type de champ '" + format + "' non formatable"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					break;
				}
			}
			if (len <= ret.length()) {
				ret = ret.substring(0, len);
			}
			ret = (StringUtils.leftPad(ret, len, "")); //$NON-NLS-1$
			ret.replaceAll("[']", " ");
			return ret;
		}

		private String todate(String valeurduchamp2) {
			String ret = string2string(valeurduchamp2);
			return ret;
		}

		private String string2string(String valeurduchamp2) {
			if (valeurduchamp2 != null) {
				return valeurduchamp2;
			}
			return ""; //$NON-NLS-1$
		}

		private String tointerger(String valeurduchamp2) {
			if (valeurduchamp2 != null) {
				if (isNumeric(valeurduchamp2)) {
					return valeurduchamp2;
				}
			}
			return "0"; //$NON-NLS-1$

			// return null;
		}

		private String smallint2tostring(String valeurduchamp2) {
			// String ret = Integer.toHexString(retint);
			// String ret = Integer.toBinaryString(retint);
			// String hex = Integer.toString(retint, 16);
			String ret = tointerger(String.valueOf(valeurduchamp2));
			return ret;
		}


	}


