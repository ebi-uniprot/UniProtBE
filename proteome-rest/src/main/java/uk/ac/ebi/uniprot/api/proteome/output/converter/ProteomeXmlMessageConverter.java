package uk.ac.ebi.uniprot.api.proteome.output.converter;

import javax.xml.bind.Marshaller;

import uk.ac.ebi.uniprot.api.rest.output.converter.AbstractXmlMessageConverter;
import uk.ac.ebi.uniprot.domain.proteome.CanonicalProtein;
import uk.ac.ebi.uniprot.domain.proteome.ProteomeEntry;
import uk.ac.ebi.uniprot.xml.proteome.CanonicalProteinConverter;
import uk.ac.ebi.uniprot.xml.proteome.ProteomeConverter;

/**
 *
 * @author jluo
 * @date: 29 Apr 2019
 *
 */

public class ProteomeXmlMessageConverter extends AbstractXmlMessageConverter<Object, Object> {
	private final ProteomeConverter proteomeConverter;
	private final CanonicalProteinConverter converter;
	private final Marshaller marshaller;
	private static final String XML_CONTEXT = "uk.ac.ebi.uniprot.xml.jaxb.proteome";
	private static final String HEADER = "<uniprot xmlns=\"http://uniprot.org/uniprot\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://uniprot.org/uniprot http://www.uniprot.org/support/docs/proteome.xsd\">\n";

	public ProteomeXmlMessageConverter() {
		proteomeConverter = new ProteomeConverter();
		converter = new CanonicalProteinConverter();
		marshaller = createMarshaller(XML_CONTEXT);
	}

	@Override
	protected String getHeader() {
		return HEADER;
	}

	@Override
	protected Object toXml(Object entity) {
		if(entity instanceof ProteomeEntry) {
			return proteomeConverter.toXml((ProteomeEntry) entity);
		}else if(entity instanceof CanonicalProtein) {
			return converter.toXml((CanonicalProtein) entity);
		}
		return null;
	}

	@Override
	protected Marshaller getMarshaller() {
		return marshaller;
	}

}
