package com.gynt.webscrape.testing;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import com.gynt.webscrape.ParseException;
import com.gynt.webscrape.Parser;

public class TestAll {

	public static void main(String[] args) throws IOException {
		File[] testfiles = new File("resources").listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".xml");
			}
		});
		for (File testfile : testfiles) {
			System.out.println("Testing: " + testfile.getName());
			String data = new String(Files.readAllBytes(testfile.toPath()), StandardCharsets.UTF_8);
			Parser p = Parser.getInstance(data);
			File[] htmlfile = new File("resources").listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					return pathname.isFile() && pathname.getName().equals(testfile.getName().split("[.]")[0] + ".html");
				}

			});
			if (htmlfile.length != 1)
				continue;
			File file = htmlfile[0];
			String html = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			try {
				for (Map<String, Object> object : p.parse(html, "")) {
					System.out.println("");
					for (Entry<String, Object> entry : object.entrySet()) {
						System.out.println(entry.getKey() + ":" + entry.getValue());
					}
					System.out.println("");
				}
			} catch (ParseException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
