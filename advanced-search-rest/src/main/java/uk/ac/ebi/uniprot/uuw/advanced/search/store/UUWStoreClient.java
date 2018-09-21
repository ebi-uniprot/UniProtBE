package uk.ac.ebi.uniprot.uuw.advanced.search.store;

import uk.ac.ebi.uniprot.dataservice.serializer.avro.Converter;
import uk.ac.ebi.uniprot.dataservice.voldemort.VoldemortClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created 21/09/18
 *
 * @author Edd
 */
public abstract class UUWStoreClient<S, A> implements VoldemortClient<S> {
    private final VoldemortClient<A> client;
    private final Converter<S, A> converter;

    UUWStoreClient(VoldemortClient<A> client, Converter<S, A> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public String getStoreName() {
        return client.getStoreName();
    }

    @Override
    public Optional<S> getEntry(String s) {
        return client.getEntry(s).map(converter::fromAvro);
    }

    @Override
    public List<S> getEntries(Iterable<String> iterable) {
        return client.getEntries(iterable).stream().map(converter::fromAvro).collect(Collectors.toList());
    }

    @Override
    public Map<String, S> getEntryMap(Iterable<String> iterable) {
        return client.getEntryMap(iterable).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, avroEntry -> converter.fromAvro(avroEntry.getValue())));
    }

    @Override
    public void saveEntry(S s) {
        client.saveEntry(converter.toAvro(s));
    }
}
