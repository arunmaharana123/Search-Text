package searchtext.searchtext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * @author Arun Maharana
 *
 */
public class SearchText {
	public static String PDF_FILE_PATH = "E:\\PDF\\10\\";
	public static String SEARCH_TEXT_EXACT_FILE_PATH = "E:\\PDF\\10\\search\\exact names.txt";
	public static String SEARCH_TEXT_PARTIAL_FILE_PATH = "E:\\PDF\\10\\search\\partial names.txt";

	public static void main(String[] args) {

		final List<String> result = new ArrayList<String>();

		try (Stream<String> lines = Files.lines(Paths.get(SEARCH_TEXT_PARTIAL_FILE_PATH))) {
			result.addAll(lines.collect(Collectors.toList()));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try (Stream<Path> paths = Files.walk(Paths.get(PDF_FILE_PATH))) {
			paths.filter(Files::isRegularFile).filter(n -> n.toString().endsWith(".pdf")).forEach(k -> {
				try {
					System.out.println("File Name: " + k.toString());
					PdfReader reader = new PdfReader(k.toString());
					int number_of_pages = reader.getNumberOfPages();
					System.out.println("Total Number of Pages: " + number_of_pages);
					String text = PdfTextExtractor.getTextFromPage(reader, 1);
					String deal_name = getExactSearch(result, text, false, true);
					if (deal_name != null && !deal_name.trim().isEmpty()) {
						System.out.println("Exact Match: " + deal_name);
					} else {
						String partial_matches = getPartialSearch(result, text, false, true);
						if (!partial_matches.isEmpty()) {
							System.out.println("Partial Matches: " + partial_matches);
						} else {
							System.err.println("No more matches...");
						}
					}

					System.out.println("--------------------------------------------------------------------");
					reader.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private static String getPartialSearch(List<String> textsToSearch, String paragraph, boolean case_sensitive,
			boolean remove_symbols) {
		Map<String, Integer> partial_matches = new LinkedHashMap<>();

		if (remove_symbols) {
			paragraph = paragraph.replaceAll("[^a-zA-Z0-9]", " ");
			paragraph = paragraph.replaceAll("[ ]{2,}", " ");
		}

		String[] paragraph_lines = paragraph.split("\n");
		for (String line : paragraph_lines) {
			for (String textToSearch : textsToSearch) {
				int count = 0;
				textToSearch = textToSearch.replaceAll("[^a-zA-Z0-9]", " ");
				textToSearch = textToSearch.replaceAll("[ ]{2,}", " ");
				String[] search_words = textToSearch.trim().split(" ");
				for (String word : search_words) {
					if (line.toUpperCase().contains(word.toUpperCase())) {
						count++;
						partial_matches.put(textToSearch, count);
//						System.out.println(count + "-" + line + "#" + textToSearch);
					}
				}
			}
		}
		List<Entry<String, Integer>> list = new ArrayList<>(partial_matches.entrySet());

		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		if (list.size() > 0) {
			return list.get(0).getKey();
		}
		return null;
	}

	private static String getExactSearch(List<String> textsToSearch, String paragraph, boolean case_sensitive,
			boolean remove_symbols) {

		final StringBuilder exact_value = new StringBuilder();

		for (String search : textsToSearch) {
			String term = search;
			if (remove_symbols) {
				term = term.replaceAll("[^a-zA-Z0-9]", "");
				paragraph = paragraph.replaceAll("[^a-zA-Z0-9]", "");
			}
			if (!case_sensitive) {
				term = term.toUpperCase();
				paragraph = paragraph.toUpperCase();
			}
			if (paragraph.contains(term)) {
				exact_value.append(search);
				break;
			}
		}
		return exact_value.toString();
	}
}
