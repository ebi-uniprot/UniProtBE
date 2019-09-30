package org.uniprot.api.uniprotkb.view;

import java.util.Comparator;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public final class ViewBy {
    private String id;
    private String label;
    private String link;
    private boolean expand;
    private long count;

    public static Comparator<ViewBy> SORT_BY_LABEL =
            (o1, o2) -> o1.getLabel().compareTo(o2.getLabel());
    public static Comparator<ViewBy> SORT_BY_ID = (o1, o2) -> o1.getId().compareTo(o2.getId());
}
