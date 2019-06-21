package uk.ac.ebi.uniprot.api.proteome.output.converter;

import uk.ac.ebi.uniprot.api.configure.proteome.ProteomeResultFields;
import uk.ac.ebi.uniprot.api.configure.uniprot.domain.Field;
import uk.ac.ebi.uniprot.api.rest.output.context.MessageConverterContext;
import uk.ac.ebi.uniprot.api.rest.output.converter.AbstractTsvMessagerConverter;
import uk.ac.ebi.uniprot.domain.proteome.ProteomeEntry;
import uk.ac.ebi.uniprot.parser.tsv.proteome.ProteomeEntryMap;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author jluo
 * @date: 29 Apr 2019
 *
 */

public class ProteomeTsvMessageConverter extends AbstractTsvMessagerConverter<ProteomeEntry> {
	private ThreadLocal<List<String>> tlFields = new ThreadLocal<>();

	public ProteomeTsvMessageConverter() {
		super(ProteomeEntry.class);
	}

	@Override
	protected void initBefore(MessageConverterContext<ProteomeEntry> context) {
		tlFields.set(ProteomeFieldsParser.parse(context.getFields()));

	}

	@Override
	protected List<String> getHeader() {
		List<String> fields = tlFields.get();
		return fields.stream().map(this::getFieldDisplayName).collect(Collectors.toList());

	}

	@Override
	protected List<String> entry2TsvStrings(ProteomeEntry entity) {
		return new ProteomeEntryMap(entity, tlFields.get()).getData();
	}

	private String getFieldDisplayName(String field) {
		Optional<Field> opField = ProteomeResultFields.INSTANCE.getField(field);
		if (opField.isPresent())
			return opField.get().getLabel();
		else
			return field;
	}

}
