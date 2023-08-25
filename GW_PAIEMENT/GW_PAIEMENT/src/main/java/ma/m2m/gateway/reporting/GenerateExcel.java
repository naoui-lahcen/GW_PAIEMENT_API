package ma.m2m.gateway.reporting;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class GenerateExcel {
	
	@Value("${key.LINK_FILE_EXCEL}")
	private String chemin_fichier;
	
	public void generateExcel() {
		 Workbook workbook = new XSSFWorkbook(); // Créer un nouveau classeur Excel

	        Sheet sheet = workbook.createSheet("Feuille1"); // Créer une feuille de calcul

	        // Créer des données de test
	        String[][] data = {
	                {"Nom", "Âge", "Ville"},
	                {"John Doe", "30", "New York"},
	                {"Jane Smith", "25", "London"},
	                {"Bob Johnson", "35", "Paris"}
	        };

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
	        //String chemin = chemin_fichier+"fichier.xlsx";
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

}
