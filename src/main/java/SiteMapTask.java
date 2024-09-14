import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SiteMapTask extends RecursiveTask<Void> {
    private String url;
    private Set<String> visited; // Set для отслеживания посещённых страниц
    private Map<String, List<String>> siteMap; // Структура для хранения ссылок
    private int depth;

    // Регулярное выражение для исключения ненужных ссылок
    private static final String EXCLUDE_PATTERN = ".*(\\.(png|jpg|jpeg|gif|bmp|zip|sql|pdf|doc|docx|xls|xlsx|ppt|pptx|exe|tar|gz|rar|7z)|#.*)$";

    public SiteMapTask(String url, Set<String> visited, Map<String, List<String>> siteMap, int depth) {
        this.url = url;
        this.visited = visited;
        this.siteMap = siteMap;
        this.depth = depth;
    }

    @Override
    protected Void compute() {
        // Проверяем, если URL уже был посещён
        if (visited.contains(url)) return null;
        visited.add(url);

        try {
            // Добавляем случайную задержку от 100 до 150 миллисекунд
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 151));

            // Скачиваем страницу с параметрами соединения
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36") // Имитация браузера
                    .timeout(10000) // Таймаут 10 секунд
                    .referrer("http://www.google.com") // Источник перехода
                    .get();

            // Находим все ссылки на странице
            Elements links = doc.select("a[href]");
            List<String> children = new ArrayList<>(); // Список дочерних ссылок для текущей страницы

            List<SiteMapTask> subtasks = new ArrayList<>();

            for (Element link : links) {
                String linkUrl = link.absUrl("href");

                // Проверяем, чтобы ссылка не содержала нежелательных шаблонов (внутренние якоря, медиафайлы и прочие файлы)
                if (!visited.contains(linkUrl) && linkUrl.startsWith(url) && !linkUrl.matches(EXCLUDE_PATTERN)) {
                    children.add(linkUrl); // Добавляем ссылку в дочерние
                    SiteMapTask task = new SiteMapTask(linkUrl, visited, siteMap, depth + 1);
                    subtasks.add(task);
                    task.fork(); // Запускаем подзадачу
                }
            }

            // Добавляем текущую страницу и её дочерние ссылки в карту
            siteMap.put(url, children);

            // Собираем результаты подзадач
            for (SiteMapTask task : subtasks) {
                task.join();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при обработке URL: " + url);
        }

        return null;
    }

    

    // Метод для записи карты сайта в файл с правильной вложенностью
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
}
