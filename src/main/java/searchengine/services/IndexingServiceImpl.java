package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.parser.IndexParser;
import searchengine.parser.LemmaParser;
import searchengine.parser.SiteIndexingTask;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Реализация сервиса индексации.
 */
@Service
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final IndexParser indexParser;
    private final LemmaParser lemmaParser;
    private ExecutorService executorService;

    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, SitesList sitesList, IndexParser indexParser, LemmaParser lemmaParser) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.sitesList = sitesList;
        this.indexRepository = indexRepository;
        this.indexParser = indexParser;
        this.lemmaParser = lemmaParser;
    }
    /**
     * Индексирует указанный URL.
     *
     * @param url URL для индексации
     * @return true, если индексация начата; в противном случае - false
     */
    @Override
    public boolean urlIndexing(String url) {
        if (urlCheck(url)) {
            log.info("Начало переиндексации сайта - " + url);
            executorService = Executors.newFixedThreadPool(10);
            executorService.submit(new SiteIndexingTask(pageRepository, siteRepository, lemmaRepository, indexRepository, lemmaParser, indexParser, url, sitesList));
            executorService.shutdown();
            return true;
        } else {
            return false;
        }
    }
    /**
     * Запускает индексацию всех сайтов.
     *
     * @return true, если индексация начата; в противном случае - false
     */
    @Override
    public boolean startIndexing() {
        if (isIndexingActive()) {
            log.debug("Индексация уже запущена");
            return false;
        } else {
            List<searchengine.config.Site> siteList = sitesList.getSites();
            executorService = Executors.newFixedThreadPool(10);
            for (searchengine.config.Site site : siteList) {
                String url = site.getUrl();
                Site dbSite = new Site();
                dbSite.setName(site.getName());
                log.info("Парсинг сайта: " + site.getName());
                executorService.submit(new SiteIndexingTask(pageRepository, siteRepository, lemmaRepository, indexRepository, lemmaParser, indexParser, url, sitesList));
            }
            executorService.shutdown();
        }
        return true;
    }
    /**
     * Останавливает индексацию.
     *
     * @return true, если индексация остановлена; в противном случае - false
     */
    @Override
    public boolean stopIndexing() {
        if (isIndexingActive()) {
            log.info("Индексация остановлена");
            executorService.shutdownNow();
            return true;
        } else {
            log.info("Индексация не была остановлена, так как не была запущена");
            return false;
        }
    }
    /**
     * Проверяет, активна ли индексация.
     *
     * @return true, если индексация активна; в противном случае - false
     */
    private boolean isIndexingActive() {
        siteRepository.flush();
        Iterable<Site> siteList = siteRepository.findAll();
        for (Site site : siteList) {
            if (site.getStatus() == Status.INDEXING) {
                return true;
            }
        }
        return false;
    }
    /**
     * Проверяет, существует ли указанный URL в списке сайтов.
     *
     * @param url URL для проверки
     * @return true, если URL существует в списке; в противном случае - false
     */
    private boolean urlCheck(String url) {
        List<searchengine.config.Site> urlList = sitesList.getSites();
        for (searchengine.config.Site site : urlList) {
            if (site.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }
}