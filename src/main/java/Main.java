import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class Main {

    private static void writeSiteMap(Map<String, List<String>> siteMap, BufferedWriter writer, String url, int depth) throws IOException {
        // Записываем текущую страницу с нужным количеством табуляций
        writer.write("\t".repeat(depth) + url + "\n");

        // Получаем дочерние страницы и рекурсивно записываем их
        List<String> children = siteMap.get(url);
        if (children != null) {
            for (String child : children) {
                writeSiteMap(siteMap, writer, child, depth + 1);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        String startUrl = "https://lenta.ru"; // Начальный URL

        // Структура для хранения карты сайта
        Map<String, List<String>> siteMap = new ConcurrentHashMap<>();
        Set<String> visited = new HashSet<>();

        ForkJoinPool pool = new ForkJoinPool();
        SiteMapTask rootTask = new SiteMapTask(startUrl, visited, siteMap, 0);
        pool.invoke(rootTask); // Запускаем главную задачу

        // После завершения обхода — записываем карту сайта в файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sitemap.txt"))) {
            writeSiteMap(siteMap, writer, startUrl, 0);

        }
    }
}