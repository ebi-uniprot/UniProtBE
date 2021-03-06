package org.uniprot.api.idmapping.output.converter.uniprotkb;

import java.io.IOException;
import java.io.OutputStream;

import org.uniprot.api.idmapping.model.UniProtKBEntryPair;
import org.uniprot.api.rest.output.UniProtMediaType;
import org.uniprot.api.rest.output.converter.AbstractEntityHttpMessageConverter;
import org.uniprot.core.parser.gff.uniprot.UniProtGffParser;

/**
 * @author sahmad
 * @created 03/03/2021
 */
public class UniProtKBEntryPairGFFMessageConverter
        extends AbstractEntityHttpMessageConverter<UniProtKBEntryPair> {
    public UniProtKBEntryPairGFFMessageConverter() {
        super(UniProtMediaType.GFF_MEDIA_TYPE, UniProtKBEntryPair.class);
    }

    @Override
    protected void writeEntity(UniProtKBEntryPair entryPair, OutputStream outputStream)
            throws IOException {
        outputStream.write((UniProtGffParser.convert(entryPair.getTo()) + "\n").getBytes());
    }
}
