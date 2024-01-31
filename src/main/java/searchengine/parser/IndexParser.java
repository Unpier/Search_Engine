package searchengine.parser;


import searchengine.dto.statistics.StatisticsIndex;
import searchengine.model.Site;

import java.util.List;

public interface IndexParser {
    void run(Site dbSite);
    List<StatisticsIndex> getIndexList();
}
