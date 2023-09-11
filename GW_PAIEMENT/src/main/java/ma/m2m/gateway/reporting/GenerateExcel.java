package ma.m2m.gateway.reporting;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;

import ma.m2m.gateway.dto.HistoAutoGateDto;

import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class GenerateExcel {

	@Value("${key.LINK_FILE_EXCEL}")
	private String chemin_fichier;

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private List<HistoAutoGateDto> listHistoAuto;

	public GenerateExcel() {
		super();
	}

	public GenerateExcel(List<HistoAutoGateDto> listHistoAuto) {
		this.listHistoAuto = listHistoAuto;
		workbook = new XSSFWorkbook();
	}

	public void generateExpExcel() {
		Workbook workbook = new XSSFWorkbook(); // Créer un nouveau classeur Excel

		Sheet sheet = workbook.createSheet("Feuille1"); // Créer une feuille de calcul

		// Créer des données de test
		String[][] data = { { "Nom", "Âge", "Ville" }, { "John Doe", "30", "New York" },
				{ "Jane Smith", "25", "London" }, { "Bob Johnson", "35", "Paris" } };

		// Remplir les données dans la feuille de calcul
		int rowNum = 0;
		for (String[] rowData : data) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (String cellData : rowData) {
				Cell cell = row.createCell(colNum++);
				cell.setCellValue(cellData);
			}
		}
		// String chemin = chemin_fichier+"fichier.xlsx";
		try (FileOutputStream file = new FileOutputStream("D:/fichier.xlsx")) {
			workbook.write(file); // Écrire les données dans le fichier Excel
			System.out.println("Fichier Excel créé avec succès !");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				workbook.close(); // Fermer le classeur Excel
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeHeaderLine() {

		sheet = workbook.createSheet("HistoriqueTRs");

		Row row = sheet.createRow(0);

		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		style.setFont(font);

		createCell(row, 0, "ID", style);
		createCell(row, 1, "Commande", style);
		createCell(row, 2, "Montant", style);
		createCell(row, 3, "N° autorisation", style);
		createCell(row, 4, "Code réponse", style);

	}

	private void createCell(Row row, int columnCount, Object value, CellStyle style) {
		sheet.autoSizeColumn(columnCount);
		Cell cell = row.createCell(columnCount);
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else {
			cell.setCellValue((String) value);
		}
		cell.setCellStyle(style);
	}

	private void writeDataLines() {
		int rowCount = 1;

		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		style.setFont(font);

		for (HistoAutoGateDto histo : listHistoAuto) {
			Row row = sheet.createRow(rowCount++);
			int columnCount = 0;

			createCell(row, columnCount++, histo.getId(), style);
			createCell(row, columnCount++, histo.getHatNumCommande(), style);
			createCell(row, columnCount++, histo.getHatMontant().toString(), style);
			createCell(row, columnCount++, histo.getHatNautemt(), style);
			createCell(row, columnCount++, histo.getHatCoderep(), style);

		}
	}

	public void export(HttpServletResponse response) throws IOException {
		writeHeaderLine();
		writeDataLines();

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();

		outputStream.close();

	}

}
