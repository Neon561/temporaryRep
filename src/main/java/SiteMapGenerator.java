import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class SiteMapGenerator {

    public static final Set<String> visitedUrls = new HashSet<>();
    public static final long PAUSE_MS = 150;
    public static final Set<String> FILE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "zip", "rar", "7z", "tar", "gz", "mp4", "mp3", "avi", "mkv", "sql");

    public static class LinkProcessor extends RecursiveTask<Void> {
        private final String url;
        private final String domain;
        private final int depth;
        private final FileWriter writer;

        public LinkProcessor(String url, String domain, int depth, FileWriter writer) {
            this.url = url;
            this.domain = domain;
            this.depth = depth;
            this.writer = writer;
        }

        @Override
        protected Void compute() {
            synchronized (visitedUrls) {
                if (visitedUrls.contains(url)) {
                    return null;
                }
                visitedUrls.add(url);
            }

            if (shouldIgnoreUrl(url)) {
                return null;
            }

            try {
                Thread.sleep(PAUSE_MS); // Pause to avoid server block

                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");
                Set<LinkProcessor> subtasks = new HashSet<>();

                synchronized (writer) {
                    writer.write(" ".repeat(depth * 4) + url + "\n");
                    writer.flush(); // Ensure the data is written to the file
                }

                for (Element link : links) {
                    String absUrl = link.absUrl("href");
                    if (absUrl.startsWith(domain) && !absUrl.contains("#") && !shouldIgnoreUrl(absUrl)) {
                        LinkProcessor task = new LinkProcessor(absUrl, domain, depth + 1, writer);
                        task.fork();
                        subtasks.add(task);
                    }
                }

                for (LinkProcessor task : subtasks) {
                    task.join();
                }

            } catch (IOException e) {
                System.err.println("IO error occurred for URL: " + url + " - " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted: " + e.getMessage());
            }
            return null;
        }

        private boolean shouldIgnoreUrl(String url) {
            String lowerCaseUrl = url.toLowerCase();
            int lastDotIndex = lowerCaseUrl.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String extension = lowerCaseUrl.substring(lastDotIndex + 1);
                if (FILE_EXTENSIONS.contains(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}
