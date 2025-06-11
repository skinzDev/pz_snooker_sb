package data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TaCScrapper implements Runnable{

    private String result;
    private final String URL = "https://snooker.yolo.blue/docs/terms-and-conditions/en.html#:~:text=You're%20not%20allowed%20to,languages%2C%20or%20make%20derivative%20versions.";


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
    public String getResult() {
        return result;
    }
}
