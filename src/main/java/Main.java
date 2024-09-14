import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String DOMAIN = "https://sendel.ru";

    public static void main(String[] args) {
        try (FileWriter writer = new FileWriter("sitemap.txt")) {
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(new SiteMapGenerator.LinkProcessor(DOMAIN, DOMAIN, 0, writer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}