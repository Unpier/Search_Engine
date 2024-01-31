package searchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Getter
@Setter

@Table(name = "page", indexes = {@Index(name = "path_list", columnList = "path")})
@NoArgsConstructor
public class Page implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site siteId;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(length = 1677721500, columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "pageId", cascade = CascadeType.ALL)
    private List<searchengine.model.Index> indexList = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page dbPage = (Page) o;
        return code == dbPage.code && Objects.equals(id, dbPage.id) && Objects.equals(siteId, dbPage.siteId) && Objects.equals(path, dbPage.path) && Objects.equals(content, dbPage.content) && Objects.equals(indexList, dbPage.indexList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, path, code, content, indexList);
    }

    @Override
    public String toString() {
        return "DBPage{" +
                "id=" + id +
                ", DBSite=" + siteId +
                ", path='" + path + '\'' +
                ", code=" + code +
                ", content='" + content + '\'' +
                ", indexList=" + indexList +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Site getSiteId() {
        return siteId;
    }

    public void setSiteId(Site siteId) {
        this.siteId = siteId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<searchengine.model.Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<searchengine.model.Index> indexList) {
        this.indexList = indexList;
    }
}
