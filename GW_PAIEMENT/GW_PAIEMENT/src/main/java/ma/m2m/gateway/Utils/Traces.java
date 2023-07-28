package ma.m2m.gateway.Utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The Class Traces generate traces
 * 
 * @version 1.0 2023-04-21
 * @author lnaoui
 */

@Component
public class Traces {

	// fonction de creation dossier de jour et le fichier trace par transaction
	public void creatFileTransaction(String input) {

		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

		String path = "D:/GW_LOGS/" + folder;

		File myObj = new File(path);
		if (myObj.mkdir()) {
			//System.out.println("======> New folder: " + myObj.getName());
		} else {
			//System.out.println("======> Folder already exists.");
		}

		File myfile = new File(path + "/" + input + ".trc");
		try {
			if (myfile.createNewFile()) {
				//System.out.println("======> New file: " + myfile.getName());
			} else {
				//System.out.println("======> File already exists.");
			}
		} catch (IOException e) {
			System.out.println("======> Creation file error. " + e);
		}
	}

	// fonction d'ecrire dans le fichier trace
	public void writeInFileTransaction(String folder, String file, String input) {
		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String dateTr = date.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
		try {

			FileWriter myWriter = new FileWriter("D:/GW_LOGS/" + folder + "/" + file + ".trc", true);

			myWriter.write(dateTr + "   " + input + System.getProperty("line.separator"));

			myWriter.close();
		} catch (IOException e) {
			System.out.println("======> An error occurred. ");
			e.printStackTrace();
		}
	}
}
