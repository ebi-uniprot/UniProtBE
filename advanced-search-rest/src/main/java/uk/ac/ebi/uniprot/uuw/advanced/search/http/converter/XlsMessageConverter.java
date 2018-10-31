package uk.ac.ebi.uniprot.uuw.advanced.search.http.converter;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.uniprot.configure.uniprot.domain.Field;
import uk.ac.ebi.uniprot.configure.uniprot.domain.impl.UniProtResultFields;
import uk.ac.ebi.uniprot.dataservice.restful.entry.domain.converter.EntryConverter;
import uk.ac.ebi.uniprot.dataservice.restful.entry.domain.model.UPEntry;
import uk.ac.ebi.uniprot.uuw.advanced.search.http.context.MessageConverterContext;
import uk.ac.ebi.uniprot.uuw.advanced.search.http.context.UniProtMediaType;
import uk.ac.ebi.uniprot.uuw.advanced.search.model.download.DownloadableEntry;
import uk.ac.ebi.uniprot.uuw.advanced.search.model.response.filter.EntryFilters;
import uk.ac.ebi.uniprot.uuw.advanced.search.model.response.filter.FieldsParser;

public class XlsMessageConverter extends AbstractUUWHttpMessageConverter<MessageConverterContext> {
	private static final Logger LOGGER = getLogger(FlatFileMessageConverter.class);
	private static final int FLUSH_INTERVAL = 5000;
	private final Function<UniProtEntry, UPEntry> entryConverter = new EntryConverter();

	public XlsMessageConverter() {
		super(UniProtMediaType.XLS_MEDIA_TYPE);
	}

	@Override
	protected boolean supports(Class<?> aClass) {
		return MessageConverterContext.class.isAssignableFrom(aClass);
	}

	@Override
	protected MessageConverterContext readInternal(Class<? extends MessageConverterContext> aClass,
			HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void write(MessageConverterContext messageConfig, OutputStream outputStream, Instant start,
			AtomicInteger counter) throws IOException {
		// fields requested
		Map<String, List<String>> filters = FieldsParser.parseForFilters(messageConfig.getFields());
		List<String> fields = FieldsParser.parse(messageConfig.getFields());
		// entries
		Stream<Collection<UniProtEntry>> entriesStream = null;//(Stream<Collection<UniProtEntry>>) messageConfig.getEntities();
		SXSSFWorkbook wb = new SXSSFWorkbook(500);
		Sheet sh = wb.createSheet();
		Row header = sh.createRow(0);
		updateRow(header, convertHeader(fields));
		try {
			entriesStream.forEach(items -> {
				items.forEach(entry -> {
					try {
						int currentCount = counter.getAndIncrement();
						if (currentCount % FLUSH_INTERVAL == 0) {
							outputStream.flush();
						}
						if (currentCount % 10000 == 0) {
							logStats(currentCount, start);
						}
						List<String> result = convert(entry, filters, fields);

						Row row = sh.createRow(currentCount + 1);
						updateRow(row, result);
					} catch (Throwable e) {
						throw new StopStreamException(
								"Could not write entry: " + entry.getPrimaryUniProtAccession().getValue(), e);
					}
				});
			});
			wb.write(outputStream);
			logStats(counter.get(), start);
			wb.dispose();

		} catch (StopStreamException e) {
			LOGGER.error("Client aborted streaming: closing stream.", e);
			entriesStream.close();
		} finally {
			outputStream.close();
			wb.close();
		}
	}

	private List<String> convertHeader(List<String> fields) {
		return fields.stream().map(this::getFieldDisplayName).collect(Collectors.toList());
	}

	private void updateRow(Row row, List<String> result) {
		for (int cellnum = 0; cellnum < result.size(); cellnum++) {
			Cell cell = row.createCell(cellnum);
			cell.setCellValue(result.get(cellnum));
		}
	}

	private String getFieldDisplayName(String field) {
		Optional<Field> opField = UniProtResultFields.INSTANCE.getField(field);
		if (opField.isPresent())
			return opField.get().getLabel();
		else
			return field;
	}

	private List<String> convert(UniProtEntry upEntry, Map<String, List<String>> filterParams, List<String> fields) {
		UPEntry entry = entryConverter.apply(upEntry);
		if ((filterParams != null) && !filterParams.isEmpty())
			EntryFilters.filterEntry(entry, filterParams);
		DownloadableEntry dlEntry = new DownloadableEntry(entry, fields);
		return dlEntry.getData();
	}
}
