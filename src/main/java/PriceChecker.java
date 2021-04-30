import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.StringTokenizer;

public class PriceChecker {
    /**
     * Takes a string (url)
     * @return price of provided amazon web url
     */
    public float getPrice(String url){
        try {
            Document document = Jsoup.connect(url).userAgent("Mozilla/87.0").get();
            Elements pricestr = document.select("#priceblock_ourprice");
            StringTokenizer st = new StringTokenizer(pricestr.html(), "&");
            String temp = st.nextToken();
            temp = temp.replace(',', '.');
            System.out.println(temp+" is the price found of url: "+url);
            return Float.parseFloat(temp);
        } catch (IOException e) {
            System.err.println("Error loading web url: "+url);
            return -1;
        }
    }
}
