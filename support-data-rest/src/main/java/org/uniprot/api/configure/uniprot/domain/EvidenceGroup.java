package org.uniprot.api.configure.uniprot.domain;

import java.util.List;

public interface EvidenceGroup {
	String getGroupName();
	List<EvidenceItem> getItems();
}