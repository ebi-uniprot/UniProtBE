package uk.ac.ebi.uniprot.uuw.advanced.search.model.download;

import uk.ac.ebi.uniprot.dataservice.restful.entry.domain.model.Organism;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadableOrganism implements Downloadable {
	public static final List<String> FIELDS = Arrays.asList( "organism", "organism_id", "tax_id" );
	private final Organism organism;

	public DownloadableOrganism(Organism organism) {
		this.organism = organism;
	}

	@Override
	public Map<String, String> attributeValues() {
		Map<String, String> map = new HashMap<>();
		map.put(FIELDS.get(0), DownloadableUtil.convertOrganism(organism));
		map.put(FIELDS.get(1), "" + organism.getTaxonomy());
		map.put(FIELDS.get(2), "" + organism.getTaxonomy());

		return map;
	}

	
	public static boolean contains(List<String> fields) {
		return fields.stream().anyMatch(FIELDS::contains);
	}
}
