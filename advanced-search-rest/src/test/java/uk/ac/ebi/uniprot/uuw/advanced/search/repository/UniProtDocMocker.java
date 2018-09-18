package uk.ac.ebi.uniprot.uuw.advanced.search.repository;

import uk.ac.ebi.uniprot.dataservice.document.uniprot.UniProtDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * A class used for mocking {@link UniProtDocument} instances. Used in tests and for simplifying our interaction with
 * Solr.
 *
 * Created 14/09/18
 *
 * @author Edd
 */
public class UniProtDocMocker {
    public static UniProtDocument createDoc(String accession) {
        UniProtDocument document = new UniProtDocument();
        document.accession = accession;
        document.proteinNames = "Phosphoribosylformylglycinamidine synthase subunit PurQ";
        document.avro_binary = new byte[0];
        document.active = true;
        return document;
    }

    public static List<UniProtDocument> createDocs(int count) {
        List<UniProtDocument> docs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            docs.add(createDoc(createAcc(i)));
        }
        return docs;
    }

    private static String createAcc(int id) {
        return String.format("P%05d", id);
    }
}
