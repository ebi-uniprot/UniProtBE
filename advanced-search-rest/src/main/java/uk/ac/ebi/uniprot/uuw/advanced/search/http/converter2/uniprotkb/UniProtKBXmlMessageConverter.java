package uk.ac.ebi.uniprot.uuw.advanced.search.http.converter2.uniprotkb;

import com.sun.xml.bind.marshaller.DataWriter;
import org.springframework.http.MediaType;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.xml.jaxb.uniprot.Entry;
import uk.ac.ebi.uniprot.dataservice.restful.entry.EntryXmlConverter;
import uk.ac.ebi.uniprot.dataservice.restful.entry.domain.EntryXmlConverterImpl;
import uk.ac.ebi.uniprot.uuw.advanced.search.http.context.MessageConverterContext;
import uk.ac.ebi.uniprot.uuw.advanced.search.http.converter2.AbstractUUWHttpMessageConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;

public class UniProtKBXmlMessageConverter extends AbstractUUWHttpMessageConverter<MessageConverterContext, UniProtEntry> {
    private final EntryXmlConverter converter;
    private final Marshaller marshaller;
    private static final String XML_CONTEXT = "uk.ac.ebi.kraken.xml.jaxb.uniprot";
    private static final String HEADER = "<uniprot xmlns=\"http://uniprot.org/uniprot\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://uniprot.org/uniprot http://www.uniprot.org/support/docs/uniprot.xsd\">\n";
    private static final String FOOTER = "<copyright>\n" +
                "Copyrighted by the UniProt Consortium, see https://www.uniprot.org/terms Distributed under the Creative Commons Attribution (CC BY 4.0) License\n" +
                "</copyright>\n" +
                "</uniprot>";

    public UniProtKBXmlMessageConverter() {
        super(MediaType.APPLICATION_XML);
        converter = new EntryXmlConverterImpl();
        marshaller = createMarshaller(XML_CONTEXT);
    }

    @Override
    protected void writeEntity(UniProtEntry entity, OutputStream outputStream) throws IOException {
        outputStream.write(getXmlString(entity).getBytes());
    }

    @Override
    protected void after(MessageConverterContext context, OutputStream outputStream) throws IOException {
        outputStream.write(FOOTER.getBytes());
    }

    @Override
    protected void before(MessageConverterContext context, OutputStream outputStream) throws IOException {
        outputStream.write(HEADER.getBytes());
    }

    String getXmlString(UniProtEntry uniProtEntry) {
        try {
            Entry entry = converter.convert(uniProtEntry);
            StringWriter xmlString = new StringWriter();
            Writer out = new BufferedWriter(xmlString);
            DataWriter writer = new DataWriter(out, "UTF-8");
            writer.setIndentStep("  ");
            marshaller.marshal(entry, writer);
            writer.characters("\n");
            writer.flush();
            return xmlString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Marshaller createMarshaller(String context) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(context);
            Marshaller contextMarshaller = jaxbContext.createMarshaller();
            contextMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            contextMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            return contextMarshaller;
        } catch (Exception e) {
            throw new RuntimeException("JAXB initialisation failed", e);
        }
    }
}
