package uk.ac.ebi.uniprot.uniprotkb.output.converter;

import uk.ac.ebi.uniprot.domain.uniprot.UniProtEntry;
import uk.ac.ebi.uniprot.parser.fasta.uniprot.UniprotFastaParser;
import uk.ac.ebi.uniprot.rest.output.UniProtMediaType;
import uk.ac.ebi.uniprot.rest.output.converter.AbstractEntityHttpMessageConverter;

import java.io.IOException;
import java.io.OutputStream;

public class UniProtKBFastaMessageConverter extends AbstractEntityHttpMessageConverter<UniProtEntry> {
    public UniProtKBFastaMessageConverter() {
        super(UniProtMediaType.FASTA_MEDIA_TYPE);
    }

    @Override
    protected void writeEntity(UniProtEntry entity, OutputStream outputStream) throws IOException {
        outputStream.write((UniprotFastaParser.create(entity).toString() + "\n").getBytes());
    }
}