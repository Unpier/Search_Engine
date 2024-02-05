package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Реализация сервиса статистики.
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    /**
     * Получает общую статистику.
     *
     * @return объект общей статистики
     */
    private TotalStatistics getTotal() {
        long sites = siteRepository.count();
        long pages = pageRepository.count();
        long lemmas = lemmaRepository.count();
        return new TotalStatistics(sites, pages, lemmas, true);
    }
    /**
     * Получает детализированную статистику для указанного сайта.
     *
     * @param site сайт для получения статистики
     * @return объект детализированной статистики для указанного сайта
     */
    private DetailedStatisticsItem getDetailed(Site site) {
        String url = site.getUrl();
        String name = site.getName();
        Status status = site.getStatus();
        LocalDateTime statusTime = site.getStatusTime();
        String error = site.getLastError();
        long pages = pageRepository.countBySiteId(site);
        long lemmas = lemmaRepository.countBySitePageId(site);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }
    /**
     * Получает список детализированных статистических данных для всех сайтов.
     *
     * @return список объектов детализированной статистики для всех сайтов
     */
    private List<DetailedStatisticsItem> getDetailedList() {
        List<Site> siteList = siteRepository.findAll();
        List<DetailedStatisticsItem> result = new ArrayList<>();
        for (Site site : siteList) {
            DetailedStatisticsItem item = getDetailed(site);
            result.add(item);
        }
        return result;
    }

    /**
     * Получает статистические данные.
     *
     * @return объект ответа со статистическими данными
     */
    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = getTotal();
        List<DetailedStatisticsItem> list = getDetailedList();
        return new StatisticsResponse(true, new StatisticsData(total, list));
    }
}
