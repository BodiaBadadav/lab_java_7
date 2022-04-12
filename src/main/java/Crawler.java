import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

class Crawler<LINK_REGEX> {
    /** Объект для храниения связки "ссылка + глубина" */
    private HashMap<String, URLDepthPair> links = new HashMap<>();

    /** Список для хранения найденных ссылок */
    private LinkedList<URLDepthPair> pool = new LinkedList<>();

    /** начальное значение глубины */
    private int depth = 0;

    public Crawler(String url, int dep) {
        depth = dep;
        pool.add(new URLDepthPair(url, 0));
    }

    /** Метод для выведения результов работы программы */
    public void run() {
        while (pool.size() > 0)
            parseLink(pool.pop());

        for (URLDepthPair link : links.values())
            System.out.println(link);

        System.out.println();
        System.out.printf("Found %d URLS\n", links.size());
    }

    /** Регулярное выражение
         + - идентификатор 1 и более раз
         ? - идентификатор 0 или 1
         [] - любой из символов внутри скобок
         ^ - начало строки
         $ - конец строки
         () - группа
         * - идентификатор 0 или более раз
         \\1 - проверка совпадения с первой группой
         \\s - пробел
     Вывражение рассматривает подстроку в строке, и разбивает ее на группы. */
    public static Pattern LINK_REGEX = Pattern.compile(
            "<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1"
    );

    /** Метод, который анализирует найденные ссылки */
    private void parseLink(URLDepthPair link) {

        /** если ссылка уже была рассмотрена, то не рассматриваем эту ссылку */
        if (links.containsKey(link.getURL())) {
            URLDepthPair knownLink = links.get(link.getURL());
            knownLink.incrementVisited();
            return;
        }
        /** Если предыдущее условие не сработало, то начинаем работу над данной ссылкой */
        links.put(link.getURL(), link);

        /** Если глубина данной ссылки больше установленной, то не рассматриваем эту ссылку */
        if (link.getDepth() >= depth)
            return;

        /** Создаем подключение */
        try {
            URL url = new URL(link.getURL());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            /** сканируем ссылку */
            Scanner s = new Scanner(con.getInputStream());
            while (s.findWithinHorizon(LINK_REGEX, 0) != null && pool.size() < 10) {  // ограничение на количество ссылок

                /** Выделяет в подстроке ту группу, в которой содержится ссылка (URL) */
                String newURL = s.match().group(2);

                /** если ссылка локальная, то делаем ее глобальной (дописывем ее) */
                if (newURL.startsWith("/"))
                    newURL = link.getURL() + newURL;

                /** если ссылка глобальная, то работаем с ней */
                else if (!newURL.startsWith("http"))
                    continue;
                URLDepthPair newLink = new URLDepthPair(newURL, link.getDepth() + 1);
                pool.add(newLink);
            }
        } catch (Exception e) {}
    }

    /** Если аргументы введены неправильно, то выодим сообщение: */
    public static void showHelp() {
        System.out.println("usage: java Crawler <URL> <depth>");
        System.exit(1);
    }

    public static void main(String[] args){
        Scanner scan=new Scanner(System.in);
        String[] argg = new String[2];
        System.out.println("Enter URL: ");
        argg[0]=scan.nextLine();
        System.out.println("Enter depth: ");
        argg[1]=scan.nextLine();
        if (argg.length !=2) showHelp();
        int depth = 0;
        String url=argg[0];
        try {
            depth = Integer.parseInt(argg[1]);
        } catch (Exception e) {
            showHelp();
        }
        Crawler crawler = new Crawler(url, depth);
        crawler.run();
    }
}