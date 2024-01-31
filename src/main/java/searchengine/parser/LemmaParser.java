package searchengine.parser;

import org.springframework.stereotype.Component;
import searchengine.dto.statistics.StatisticsLemma;
import searchengine.model.Site;

import java.util.List;
@Component
public interface LemmaParser {
    void run(Site site);
    List<StatisticsLemma> getLemmaDtoList();
}
