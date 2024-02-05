package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.StatisticsSearch;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.morphology.Morphology;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.CleanHtmlCode;

import java.util.*;
import java.util.stream.Collectors;
/**
 * Реализация сервиса поиска.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final Morphology morphology;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexSearchRepository;
    private final SiteRepository siteRepository;
    /**
     * Выполняет поиск по всем сайтам.
     *
     * @param searchText текст для поиска
     * @param offset     смещение для пагинации
     * @param limit      максимальное количество результатов
     * @return список статистических данных о результатах поиска
     */
    @Override
    public List<StatisticsSearch> allSiteSearch(String searchText, int offset, int limit) {
        log.info("Получение результатов поиска \"" + searchText + "\"");
        List<Site> siteList = siteRepository.findAll();
        List<StatisticsSearch> result = new ArrayList<>();
        List<Lemma> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        for (Site site : siteList) {
            foundLemmaList.addAll(getLemmaListFromSite(textLemmaList, site));
        }
        List<StatisticsSearch> searchData = null;
        for (Lemma l : foundLemmaList) {
            if (l.getLemma().equals(searchText)) {
                searchData = new ArrayList<>(getSearchDtoList(foundLemmaList, textLemmaList, offset, limit));
                searchData.sort((o1, o2) -> Float.compare(o2.getRelevance(), o1.getRelevance()));
                if (searchData.size() > limit) {
                    for (int i = offset; i < limit; i++) {
                        result.add(searchData.get(i));
                    }
                    return result;
                }
            } else {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        log.info("Поиск завершен. Получены результаты.");
        return searchData;
    }
    /**
     * Выполняет поиск на указанном сайте.
     *
     * @param searchText текст для поиска
     * @param url        URL сайта для поиска
     * @param offset     смещение для пагинации
     * @param limit      максимальное количество результатов
     * @return список статистических данных о результатах поиска
     */
    @Override
    public List<StatisticsSearch> siteSearch(String searchText, String url, int offset, int limit) {
        log.info("Поиск \"" + searchText + "\" в - " + url);
        Site site = siteRepository.findByUrl(url);
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        List<Lemma> foundLemmaList = getLemmaListFromSite(textLemmaList, site);
        log.info("Поиск завершен. Получены результаты.");
        return getSearchDtoList(foundLemmaList, textLemmaList, offset, limit);
    }
    /**
     * Извлекает леммы из текста для поиска.
     *
     * @param searchText текст для поиска
     * @return список лемм
     */
    private List<String> getLemmaFromSearchText(String searchText) {
        String[] words = searchText.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        for (String lemma : words) {
            List<String> list = morphology.getLemma(lemma);
            lemmaList.addAll(list);
        }
        return lemmaList;
    }
    /**
     * Получает список лемм из указанного сайта.
     *
     * @param lemmas список лемм
     * @param site   сайт для поиска
     * @return список лемм
     */
    private List<Lemma> getLemmaListFromSite(List<String> lemmas, Site site) {
        lemmaRepository.flush();
        return lemmaRepository.findLemmaListBySite(lemmas, site)
                .stream()
                .sorted(Comparator.comparingInt(Lemma::getFrequency))
                .collect(Collectors.toList());
    }
    /**
     * Получает список статистических данных о результатах поиска на основе лемм и текста запроса.
     *
     * @param lemmaList     список лемм
     * @param textLemmaList список лемм текста запроса
     * @param offset        смещение для пагинации
     * @param limit         максимальное количество результатов
     * @return список статистических данных о результатах поиска
     */
    private List<StatisticsSearch> getSearchDtoList(List<Lemma> lemmaList, List<String> textLemmaList, int offset, int limit) {
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {
            List<Page> foundPageList = pageRepository.findByLemmaList(lemmaList);
            indexSearchRepository.flush();
            List<Index> foundIndexList = indexSearchRepository.findByPagesAndLemmas(lemmaList, foundPageList);
            Hashtable<Page, Float> sortedPageByAbsRelevance = getPageAbsRelevance(foundPageList, foundIndexList);
            return getSearchData(sortedPageByAbsRelevance, textLemmaList, offset, limit);
        } else {
            return new ArrayList<>();
        }
    }
    /**
     * Получает статистические данные о результатах поиска.
     *
     * @param pageList      список страниц
     * @param textLemmaList список лемм текста запроса
     * @return список статистических данных о результатах поиска
     */
    private List<StatisticsSearch> getSearchData(Hashtable<Page, Float> pageList, List<String> textLemmaList, int offset, int limit) {
        List<StatisticsSearch> result = new ArrayList<>();

        for (Page page : pageList.keySet()) {
            String uri = page.getPath();
            String content = page.getContent();
            Site pageSite = page.getSiteId();
            String site = pageSite.getUrl();
            String siteName = pageSite.getName();
            Float absRelevance = pageList.get(page);

            StringBuilder clearContent = new StringBuilder();
            String title = CleanHtmlCode.clear(content, "title");
            String body = CleanHtmlCode.clear(content, "body");
            clearContent.append(title).append(" ").append(body);
            String snippet = getSnippet(clearContent.toString(), textLemmaList);

            result.add(new StatisticsSearch(site, siteName, uri, title, snippet, absRelevance));
        }
        return result;
    }
    /**
     * Создает сниппет для результата поиска.
     *
     * @param content    содержимое страницы
     * @param lemmaList  список лемм
     * @return сниппет результата поиска
     */
    private String getSnippet(String content, List<String> lemmaList) {
        List<Integer> lemmaIndex = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        for (String lemma : lemmaList) {
            lemmaIndex.addAll(morphology.findLemmaIndexInText(content, lemma));
        }
        Collections.sort(lemmaIndex);
        List<String> wordsList = getWordsFromContent(content, lemmaIndex);
        for (int i = 0; i < wordsList.size(); i++) {
            result.append(wordsList.get(i)).append("... ");
            if (i > 3) {
                break;
            }
        }
        return result.toString();
    }
    /**
     * Получает список слов из содержимого страницы на основе индексов лемм.
     *
     * @param content    содержимое страницы
     * @param lemmaIndex индексы лемм
     * @return список слов
     */
    private List<String> getWordsFromContent(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lemmaIndex.size(); i++) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int nextPoint = i + 1;
            while (nextPoint < lemmaIndex.size() && lemmaIndex.get(nextPoint) - end > 0 && lemmaIndex.get(nextPoint) - end < 5) {
                end = content.indexOf(" ", lemmaIndex.get(nextPoint));
                nextPoint += 1;
            }
            i = nextPoint - 1;
            String text = getWordsFromIndex(start, end, content);
            result.add(text);
        }
        result.sort(Comparator.comparingInt(String::length).reversed());
        return result;
    }
    /**
     * Получает слово из содержимого страницы по индексам начала и конца.
     *
     * @param start  индекс начала слова
     * @param end    индекс конца слова
     * @param content содержимое страницы
     * @return слово с выделенной частью
     */
    private String getWordsFromIndex(int start, int end, String content) {
        String word = content.substring(start, end);
        int prevPoint;
        int lastPoint;
        if (content.lastIndexOf(" ", start) != -1) {
            prevPoint = content.lastIndexOf(" ", start);
        } else prevPoint = start;
        if (content.indexOf(" ", end + 30) != -1) {
            lastPoint = content.indexOf(" ", end + 30);
        } else lastPoint = content.indexOf(" ", end);
        String text = content.substring(prevPoint, lastPoint);
        try {
            text = text.replaceAll(word, "<b>" + word + "</b>");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return text;
    }
    /**
     * Вычисляет абсолютную релевантность страницы на основе списка страниц и индексов.
     *
     * @param pageList  список страниц
     * @param indexList список индексов
     * @return хэш-таблица с абсолютной релевантностью страниц
     */
    private Hashtable<Page, Float> getPageAbsRelevance(List<Page> pageList, List<Index> indexList) {
        HashMap<Page, Float> pageWithRelevance = new HashMap<>();
        for (Page page : pageList) {
            float relevant = 0;
            for (Index index : indexList) {
                if (index.getPageId() == page) {
                    relevant += index.getRank();
                }
            }
            pageWithRelevance.put(page, relevant);
        }
        HashMap<Page, Float> pageWithAbsRelevance = new HashMap<>();
        for (Page page : pageWithRelevance.keySet()) {
            float absRelevant = pageWithRelevance.get(page) / Collections.max(pageWithRelevance.values());
            pageWithAbsRelevance.put(page, absRelevant);
        }
        return pageWithAbsRelevance.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, Hashtable::new));
    }
}