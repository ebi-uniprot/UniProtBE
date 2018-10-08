package uk.ac.ebi.uniprot.uuw.advanced.search.mockers;

import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.model.factories.DefaultUniProtFactory;
import uk.ac.ebi.kraken.model.uniprot.UniProtEntryImpl;
import uk.ac.ebi.kraken.parser.UniProtParser;
import uk.ac.ebi.kraken.parser.UniProtParserException;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 19/09/18
 *
 * @author Edd
 */
public class UniProtEntryMocker {
    public enum Type {
        SP("Q8DIA7.dat"), SP_COMPLEX("Q8DIA7.dat"), TR("Q8DIA7.dat"), TR_COMPLEX("Q8DIA7.dat");

        private final String fileName;

        Type(String fileName) {
            this.fileName = fileName;
        }
    }

    private static Map<Type, UniProtEntry> entryMap = new HashMap<>();

    static {
        for (Type type : Type.values()) {
            InputStream is = UniProtEntryMocker.class.getResourceAsStream("/entry/" + type.fileName);
            try {
                UniProtEntry entry = UniProtParser.parse(is, DefaultUniProtFactory.getInstance());
                entryMap.put(type, entry);
            } catch (UniProtParserException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static UniProtEntry create(String accession) {
        UniProtEntryImpl uniProtEntry = new UniProtEntryImpl(entryMap.get(Type.SP));
        uniProtEntry.getPrimaryUniProtAccession().setValue(accession);
        return uniProtEntry;
    }

    public static UniProtEntry create(Type type) {
        return new UniProtEntryImpl(entryMap.get(type));
    }

    public static Collection<UniProtEntry> createEntries() {
        return entryMap.values();
    }
}