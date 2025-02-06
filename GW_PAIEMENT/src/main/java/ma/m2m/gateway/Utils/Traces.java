package ma.m2m.gateway.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */


@Component
@Slf4j
public class Traces {
	
	private static final Logger logger = LogManager.getLogger(Traces.class);

	public void creatFileTransaction(String input) {

		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		String path = "D:/GW_LOGS/" + folder;

		File myObj = new File(path);
		if (myObj.mkdir()) {
			logger.info("======> New folder: {}" , myObj.getName());
		}

		File myfile = new File(path + "/" + input + ".trc");
		try {
			if (myfile.createNewFile()) {
				logger.info("======> New file: {}" , myfile.getName());
			}
		} catch (IOException e) {
			logger.error("======> Creation file error." , e);
		}
	}
	
	@SuppressWarnings("unused") // Indique que certains paramètres ne sont pas utilisés
	public void writeInFileTransaction(String folder, String file, String input) {
		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String dateTr = date.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
		String formattedFolder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		try(FileWriter myWriter = new FileWriter("D:/GW_LOGS/" + formattedFolder + "/" + file + ".trc", true)) {
			myWriter.write(dateTr + "   " + input + System.getProperty("line.separator"));
		} catch (IOException e) {
			logger.error("======> An error occurred.", e);
		}
	}
}
