package com.malicia.mrg.grecco.parametrage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor;

public class TableDb2 extends FunctionTransverse {
	private String nomTableDb2;
	private List<ColTable> champs;
	private Integer nbChamps = 0;
	private Integer nbChampsNonTechnique = 0;
	private List<List<String>> valeurs;
	private List<Integer> Excel2Plat;
	private List<Integer> Plat2Excel;
	private int rowEnTete = -1;
	private int nbwrite;
	private FileWriter fwDB2;
	private BufferedWriter bwDB2;
	private FileWriter fwPlat;
	private BufferedWriter bwPlat;
	private File filePun;
	private String Db2content;
	private int nbwriteDB2;
	static HSSFWorkbook wb;

	public TableDb2(String nomTable, JLabel Staticlabel, JTextArea textArea) {
		super();
		nomTableDb2 = nomTable;
		labelProgression = Staticlabel;
		LtextArea = textArea;
		msg(nomTable + " instantiation"); //$NON-NLS-1$
		openFichierPlat();
		openXls();
		recupereChamps();
	}

	private void recupereChamps() {
		msg(nomTableDb2 + " recupereChamps du fichier definition "); //$NON-NLS-1$

		champs = new ArrayList();
		openFichierDefinitionTable();

		BufferedReader br;
		String buff = ""; //$NON-NLS-1$
		try {

			br = new BufferedReader(new FileReader(filePun));
			String line;
			boolean zoneData = false;
			while ((line = br.readLine()) != null) {
				if (!zoneData) {
					zoneData = (line.contains(" (")); //$NON-NLS-1$
				}
				if (zoneData) {
					zoneData = !(line.contains(" )")); //$NON-NLS-1$
				}
				if (zoneData) {
					buff = buff + line;
					if (buff.contains(",")) { //$NON-NLS-1$
						String[] buffsplit = buff.split(",\n"); //$NON-NLS-1$
						addChamps(buffsplit[0]);
						if (buffsplit.length > 1) {
							buff = buffsplit[1]; // $NON-NLS-1$
						} else {
							buff = ""; //$NON-NLS-1$
						}
					}
				}
			}
			addChamps(buff);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addChamps(String buff) {
		String[] expl = buff.split("([ (),\"])+"); //$NON-NLS-1$
		if (expl.length > 2) {
			ColTable ch = new ColTable();
			ch.nom = expl[1];
			ch.format = expl[2];
			if (isNumeric(expl[3])) {
				ch.len = Integer.valueOf(expl[3]);
			}
			// if (isNumeric(expl[3])) {
			// ch.position = Integer.valueOf(expl[3]);
			// }
			// ch.format = expl[4];
			// if (expl.length > 6) {
			// if (isNumeric(expl[5])) {
			// ch.len = Integer.valueOf(expl[5]);
			// } else {
			// ch.len = Integer.valueOf(expl[6]);
			// }
			// }
			if (isChampsTechnique(ch.nom)) {
				nbChamps++;
				ch.champTechnique = true;
			} else {
				nbChamps++;
				nbChampsNonTechnique++;
				ch.champTechnique = false;
			}
			champs.add(ch);
		}
	}

	private boolean isChampsTechnique(String nom) {
		String champstech = "tscre,tsmaj,iduticre,idutimaj,idpgmcre,idpgmmaj"; //$NON-NLS-1$
		if (champstech.contains(nom.toLowerCase())) {
			return true;
		}
		return false;
	}

	public void mapChampsdeExcel() {
		openFichierOrdreDB2();

		msg(nomTableDb2 + " mapChampsdeExcel dans la table memoire"); //$NON-NLS-1$

		if (wb != null) {
			HSSFSheet sheet = wb.getSheet(nomTableDb2);
			// HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			int rows; // No of rows
			rows = sheet.getPhysicalNumberOfRows();

			int cols = xlsNombreDeColonne(sheet, rows);

			Excel2Plat = createArrayList(cols);
			Plat2Excel = createArrayList(cols);
			rowEnTete = -1;
			DB2debutOrdreDB2();
			for (int r = 0; r < rows; r++) {
				row = sheet.getRow(r);

				if (row != null) {

					if (rowEnTete == -1 && (r == 0 || r == 1)) {
						xlsRechercheEntete(row, cols, r);
					} else {
						if (rowEnTete >= 0) {
							msgProg(nomTableDb2 + " (" + r + ")  rows d fichier excel lu "); //$NON-NLS-1$ //$NON-NLS-2$
							for (int c = 0; c < Excel2Plat.size(); c++) {
								if (Excel2Plat.get(c) > -1) {
									cell = row.getCell(c);
									if (cell != null) {
										xlsPopulateValeur(cell, c);
									}
								}
							}
							ecrireLigne2Plat();
							DB2addOrdreDB2();
							for (int i = 0; i < nbChamps; i++) {
								champs.get(i).valeurduchamp = "";

							}

						}
					}
				}
			}
			DB2ecrireOrdreDB2();
		}
		DB2fermetureOrdreDB2();
	}

	private void xlsPopulateValeur(HSSFCell cell, int c) {
		String ch = null;
		if (cell.getCellType() == 1) {
			ch = cell.getStringCellValue();
		}
		if (cell.getCellType() == 0) {
			if (champs.get(Excel2Plat.get(c)).format == "DECIMAL") { //$NON-NLS-1$
				ch = String.valueOf(cell.getNumericCellValue());
			} else {
				ch = String.valueOf(Integer.valueOf((int) cell.getNumericCellValue()));
			}
		}
		if (ch != null) {
			if (Excel2Plat.get(c) >= 0) {
				champs.get(Excel2Plat.get(c)).valeurduchamp = ch;
			}
		}
	}

	private void xlsRechercheEntete(HSSFRow row, int cols, int r) {
		HSSFCell cell;
		int nbColChamps = 0;
		for (int c = 0; c < cols; c++) {
			cell = row.getCell(c);
			if (cell != null) {
				if (cell.getCellType() == 1) {
					int numChampsPlat = numeroDuChamps(cell.getStringCellValue());
					if (numChampsPlat >= 0) {
						Excel2Plat.add(c, numChampsPlat);
						Plat2Excel.add(numChampsPlat, c);
						nbColChamps++;
						if (nbColChamps == nbChampsNonTechnique) {
							break;
						}
					}
				}
			}
		}
		if (nbColChamps > (nbChampsNonTechnique / 2)) {
			rowEnTete = r;
			if (nbColChamps != nbChampsNonTechnique) {
				msg(":ERR: " + nomTableDb2 + " " + nbColChamps + " champs trouver dans lexcel sur " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ nbChampsNonTechnique + " dans la definition "); //$NON-NLS-1$
			}
		}
	}

	private int xlsNombreDeColonne(HSSFSheet sheet, int rows) {
		HSSFRow row;
		int cols = 0; // No of columns
		int tmp = 0;
		// This trick ensures that we get the data properly even if it
		// doesn't start from first few rows
		for (int i = 0; i < 10 || i < rows; i++) {
			row = sheet.getRow(i);
			if (row != null) {
				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
				if (tmp > cols)
					cols = tmp;
			}
		}
		return cols;
	}

	private void DB2debutOrdreDB2() {
		nbwriteDB2 = 0;
		Db2content = "";
		Db2content = "delete * from " + nomTableDb2 + ";" + Db2content;
	}

	private void DB2addOrdreDB2() {
		boolean openpar = false;
		for (int i = 0; i < nbChamps; i++) {
			if (i == 0) {
				openpar = true;
				Db2content += "Insert into " + nomTableDb2 + " ( " + SQLgetListeCol() + " ) VALUES (";
			} else {
				Db2content += " ,";
			}
			Db2content += "'" + (champs.get(i).format()).trim() + "'";
			// champs.get(i).valeurduchamp = ""; //$NON-NLS-1$
		}
		if (openpar) {
			Db2content += " ) ";
		}

		if (Db2content.length() > 30000) {
			DB2ecrireOrdreDB2();
			Db2content = "";
		}
		nbwriteDB2++;
		msgProg(nomTableDb2 + " (" + nbwriteDB2 + ")  nombre de ligne ecrite dans le fichier db2 "); //$NON-NLS-1$ //$NON-NLS-2$

	}

	private void DB2ecrireOrdreDB2() {
		try {
			bwDB2.write(SQLmissenormecompacter(Db2content) + ";");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // $NON-NLS-1$
		msg(nomTableDb2 + " (" + nbwriteDB2 + ")  nombre de ligne ecrite dans le fichier db2 "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void DB2fermetureOrdreDB2() {
		try {
			bwDB2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String SQLmissenormecompacter(String content) {
		content = content.replaceAll("  ", " ");
		content = WordUtils.wrap(content, 78);
		return content;
	}

	private String SQLgetListeCol() {
		String ret = "";
		for (int i = 0; i < nbChamps; i++) {
			if (ret == "") {
				ret = "";
			} else {
				ret += " , ";
			}
			ret += champs.get(i).nom;
		}
		return ret;
	}

	private void ecrireLigne2Plat() {
		String content = ""; //$NON-NLS-1$
		for (int i = 0; i < nbChamps; i++) {
			content = content + champs.get(i).format();
			// champs.get(i).valeurduchamp = ""; //$NON-NLS-1$
		}
		try {
			bwPlat.write(content + "\r\n"); //$NON-NLS-1$
			nbwrite++;
			msgProg(nomTableDb2 + " (" + nbwrite + ")  nombre de ligne ecrite dans le fichier plat "); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<Integer> createArrayList(int cols) {
		ArrayList ret = new ArrayList();
		for (int i = 0; i < cols; i++) {
			ret.add(-1);
		}
		return ret;
	}

	private int numeroDuChamps(String stringCellValue) {
		for (int i = 0; i < nbChamps; i++) {
			if (stringCellValue.contains(champs.get(i).nom)) {
				return i;
			}
		}
		return -1;
	}

	private void openFichierOrdreDB2() {
		File parentDirectory;
		File fileout;
		try {
			URL url = this.getClass().getResource(Messages.getString("TableDb2.resourceRepertoireFichierSortieSQL")); //$NON-NLS-1$
			parentDirectory = new File(new URI(url.toString()));
			fileout = new File(parentDirectory, nomTableDb2 + Messages.getString("TableDb2.extensionFichierSortieSQL")); //$NON-NLS-1$
			fileout.delete();
			fileout = new File(parentDirectory, nomTableDb2 + Messages.getString("TableDb2.extensionFichierSortieSQL")); //$NON-NLS-1$
			fwDB2 = new FileWriter(fileout.getAbsoluteFile(), true);
			bwDB2 = new BufferedWriter(fwDB2);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void openFichierPlat() {
		File parentDirectory;
		File fileout;
		try {
			URL url = this.getClass().getResource(Messages.getString("TableDb2.resourceRepertoireFichierSortiePlat")); //$NON-NLS-1$
			parentDirectory = new File(new URI(url.toString()));
			fileout = new File(parentDirectory,
					nomTableDb2 + Messages.getString("TableDb2.extensionFichierSortiePlat")); //$NON-NLS-1$
			fileout.delete();
			fileout = new File(parentDirectory,
					nomTableDb2 + Messages.getString("TableDb2.extensionFichierSortiePlat")); //$NON-NLS-1$
			fwPlat = new FileWriter(fileout.getAbsoluteFile(), true);
			bwPlat = new BufferedWriter(fwPlat);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void openFichierDefinitionTable() {
		// champs = champs;
		filePun = new File(getClass()
				.getResource(Messages.getString("TableDb2.resourceRepertoireFichierDefinitionTable") //$NON-NLS-1$
						+ nomTableDb2 + Messages.getString("TableDb2.extensionFichierDefinitionTable")) //$NON-NLS-1$
				.getFile());

	}

	private static HSSFWorkbook openXls() {
		if (wb == null) {
			try {
				File fileCsv = new File(TableDb2.class
						.getResource(Messages.getString("TableDb2.resourceFichierParametrageExcel")).getFile()); //$NON-NLS-1$

				POIFSFileSystem fs;
				fs = new POIFSFileSystem(new FileInputStream(fileCsv));
				wb = new HSSFWorkbook(fs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return wb;
	}
}
