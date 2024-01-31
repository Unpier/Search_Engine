package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Lemma;

import java.util.Collection;
import java.util.List;


@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    long countBySiteId(Site site_id);
    Iterable<Page> findBySiteId(Site site_id);
    @Query(value = "SELECT * FROM page p JOIN index_table i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas", nativeQuery = true)
    List<Page> findByLemmaList(@Param("lemmas") Collection<Lemma> lemmaListId);
}
