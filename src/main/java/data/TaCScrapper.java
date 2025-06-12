package data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Terms and Conditions Scraper
 * <p>
 * This class implements the Runnable interface to perform web scraping in a background thread.
 * It connects to a specific URL, parses the HTML content using Jsoup, and extracts the text
 * from all paragraph elements to retrieve the Terms and Conditions.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class TaCScrapper implements Runnable {

    private String result;
    private static final String URL = "https://snooker.yolo.blue/docs/terms-and-conditions/en.html#:~:text=You're%20not%20allowed%20to,languages%2C%20or%20make%20derivative%20versions.";

    /**
     * Executed when the thread starts. Connects to the URL, fetches the content,
     * parses the paragraph tags, and stores the concatenated text in the result field.
     * In case of an error, an error message is stored in the result field.
     */
    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements terms = doc.body().getElementsByTag("p");
            StringBuilder sb = new StringBuilder();
            for (Element term : terms) {
                sb.append(term.text()).append("\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            result = "Error fetching Terms and Conditions: " + e.getMessage();
        }
    }

    /**
     * Gets the result of the scraping operation.
     * This will be the text of the Terms and Conditions or an error message.
     *
     * @return The scraped text or an error string.
     */
    public String getResult() {
        return result;
    }
}
