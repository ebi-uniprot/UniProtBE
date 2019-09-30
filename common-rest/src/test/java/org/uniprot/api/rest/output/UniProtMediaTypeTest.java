package org.uniprot.api.rest.output;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.uniprot.api.rest.output.UniProtMediaType.*;

import org.junit.jupiter.api.Test;

/**
 * Ensure file extensions associated with media types are as expected, so that downloads produce
 * expected results.
 *
 * <p>Created 22/10/18
 *
 * @author Edd
 */
class UniProtMediaTypeTest {
    @Test
    void flatfileFileExtensionIsTxt() {
        assertThat(UniProtMediaType.getFileExtension(FF_MEDIA_TYPE), is("txt"));
    }

    @Test
    void excelFileExtensionIsTxt() {
        assertThat(UniProtMediaType.getFileExtension(XLS_MEDIA_TYPE), is("xlsx"));
    }

    @Test
    void listFileExtensionIsTxt() {
        assertThat(UniProtMediaType.getFileExtension(LIST_MEDIA_TYPE), is("list"));
    }

    @Test
    void tsvFileExtensionIsTxt() {
        assertThat(UniProtMediaType.getFileExtension(TSV_MEDIA_TYPE), is("tsv"));
    }

    @Test
    void fastaFileExtensionIsTxt() {
        assertThat(UniProtMediaType.getFileExtension(FASTA_MEDIA_TYPE), is("fasta"));
    }

    @Test
    void gffFileExtensionIsTxt() {
        assertThat(UniProtMediaType.getFileExtension(GFF_MEDIA_TYPE), is("gff"));
    }
}
