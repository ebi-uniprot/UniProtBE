package org.uniprot.api.rest.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.HandlerMapping;
import org.uniprot.api.rest.output.UniProtMediaType;
import org.uniprot.core.util.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.uniprot.api.rest.output.UniProtMediaType.DEFAULT_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.createUnknownMediaTypeForFileExtension;

/**
 * A helper class that mutates an {@link HttpServletRequest} based on its values, and if necessary
 * sets the request's content type in the 'Accept' header.
 *
 * <p>Created 03/12/2019
 *
 * @author Edd
 */
@Slf4j
public class HttpServletRequestContentTypeMutator {
    private static final String FORMAT = "format";
    private static final String SEARCH = "/search";
    private static final String DOWNLOAD = "/download";
    private static final Pattern ENTRY_CONTEXT_PATH_MATCHER =
            // TODO: 03/04/20 test
            Pattern.compile("^(/[\\w-]+)*/[\\w-]+\\.([\\w-]+)$");
    private static final Set<String> ALLOWED_ACCEPT_HEADERS =
            UniProtMediaType.ALL_TYPES.stream().map(MimeType::toString).collect(Collectors.toSet());

    private HttpServletRequestContentTypeMutator() {}

    public static void mutate(MutableHttpServletRequest request) {
        boolean mutated = mutateEntryRequest(request);
        mutated = mutated || mutateSearchOrDownloadRequest(request);
        addDefaultAcceptHeaderIfRequired(mutated, request);
    }

    private static boolean mutateSearchOrDownloadRequest(MutableHttpServletRequest request) {
        boolean mutated = false;
        String format = request.getParameter(FORMAT);
        if ((request.getRequestURI().endsWith(DOWNLOAD) || request.getRequestURI().endsWith(SEARCH))
                && Utils.notNullNotEmpty(format)) {
            addContentTypeHeaderForFormat(request, format);
            mutated = true;
        }
        return mutated;
    }

    private static boolean mutateEntryRequest(MutableHttpServletRequest request) {
        boolean mutated = false;
        Matcher entryContextMatcher = ENTRY_CONTEXT_PATH_MATCHER.matcher(request.getRequestURL());
        if (entryContextMatcher.matches()) {
            String entryPathVariable = entryContextMatcher.group(2);
            String entryId = entryContextMatcher.group(3);
            String extension = entryContextMatcher.group(4);

            setRealEntryId(request, entryPathVariable, entryId);

            setURI(request, extension);
            setURL(request, extension);

            addContentTypeHeaderForFormat(request, extension);
            mutated = true;
        }
        return mutated;
    }

    private static void addDefaultAcceptHeaderIfRequired(
            boolean mutated, MutableHttpServletRequest request) {
        /*
        ensure bean that knows about the end-point's valid content types is able to look up the
        end-point's valid content types here, and store in X

        1. set content type requested
        1.1 take from accept header
        1.2 if request fits entry request pattern, extract from extension
        1.3 if request contains format parameter, extract from format parameter
        1.4 if no content type still, set to default content type, json

        2. if content type requested is NOT inside X
        2.1 if user-agent is a browser, then set content type to json
        2.2 otherwise
        2.2.1 if used extension/format to set content type (and this code block => it is not known), then
              throw HttpMediaTypeNotAcceptableException with unknown format message
        2.2.2 else (=> not derived from extension/format), throw HttpMediaTypeNotAcceptableException
              with message unknown content type specified
         */

        // if no accept header was added based on format/extension, then add default content type
        if (!mutated
                && (Utils.nullOrEmpty(request.getHeader(HttpHeaders.ACCEPT))
                        || (Utils.notNullNotEmpty(request.getHeader(HttpHeaders.ACCEPT))
                                && (request.getHeader(HttpHeaders.ACCEPT).equals("*/*")
                                        || !ALLOWED_ACCEPT_HEADERS.contains(
                                                request.getHeader(HttpHeaders.ACCEPT)))))) {
            request.addHeader(HttpHeaders.ACCEPT, DEFAULT_MEDIA_TYPE_VALUE);
        }
    }

    private static void addContentTypeHeaderForFormat(
            MutableHttpServletRequest request, String format) {
        try {
            MediaType mediaTypeForFileExtension =
                    UniProtMediaType.getMediaTypeForFileExtension(format);
            request.addHeader(HttpHeaders.ACCEPT, mediaTypeForFileExtension.toString());
        } catch (IllegalArgumentException iae) {
            request.addHeader(
                    HttpHeaders.ACCEPT, createUnknownMediaTypeForFileExtension(format).toString());
        }
    }

    private static void setURL(MutableHttpServletRequest request, String extension) {
        request.setRequestURL(
                request.getRequestURL()
                        .substring(0, request.getRequestURL().length() - (extension.length() + 1)));
    }

    private static void setURI(MutableHttpServletRequest request, String extension) {
        request.setRequestURI(
                request.getRequestURI()
                        .substring(0, request.getRequestURI().length() - (extension.length() + 1)));
    }

    private static void setRealEntryId(
            MutableHttpServletRequest request, String entryPathVariable, String entryId) {
        Map<String, String> uriVariablesMap = new HashMap<>();
        uriVariablesMap.put(entryPathVariable, entryId);

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariablesMap);
    }
}
