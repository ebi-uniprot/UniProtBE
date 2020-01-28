package org.uniprot.api.rest.validation;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.uniprot.store.search.domain2.SearchField;
import org.uniprot.store.search.field.SearchFields;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is the solr query solr validator is responsible to verify if the sort field parameter has
 * valid field names
 *
 * @author lgonzales
 */
@Constraint(validatedBy = ValidSolrSortFields.SortFieldValidatorImpl.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSolrSortFields {

    Class<? extends Enum<?>> sortFieldEnumClazz();

    String message() default "{search.invalid.sort}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String enumValueName();

    class SortFieldValidatorImpl implements ConstraintValidator<ValidSolrSortFields, String> {

        private static final Pattern SORT_FORMAT_PATTERN =
                Pattern.compile("^\\w+\\s+\\w+\\s*(,\\s*\\w+\\s+\\w+\\s*)*$");
        private static final String SORT_ORDER = "^asc|desc$";
        private List<String> valueList;

        @Override
        public void initialize(ValidSolrSortFields constraintAnnotation) {
            valueList = new ArrayList<>();
            Class<? extends Enum<?>> enumClass = constraintAnnotation.sortFieldEnumClazz();

            String enumValueName = constraintAnnotation.enumValueName();
            SearchFields searchFields = null;
            for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                if (enumConstant.name().equals(enumValueName)) {
                    searchFields = (SearchFields) enumConstant;
                    break;
                }
            }

            if (searchFields == null) {
                throw new IllegalArgumentException(
                        "Unknown enum value: [" + enumValueName + " in [" + enumClass + "].");
            } else {
                valueList =
                        searchFields.getSearchFields().stream()
                                .filter(field -> field.getSortField().isPresent())
                                .map(SearchField::getName)
                                .collect(Collectors.toList());
            }
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            ConstraintValidatorContextImpl contextImpl = (ConstraintValidatorContextImpl) context;
            final AtomicBoolean result = new AtomicBoolean(true);
            if (value != null) {
                value = value.toLowerCase();
                if (SORT_FORMAT_PATTERN.matcher(value).matches()) {

                    Arrays.stream(value.split(","))
                            .map(String::trim)
                            .forEach(
                                    sortClause ->
                                            handleSortClause(sortClause, contextImpl, result));
                    if (!result.get() && contextImpl != null) {
                        contextImpl.disableDefaultConstraintViolation();
                    }
                } else {
                    addInvalidSortFormatErrorMessage(contextImpl, value);
                    result.getAndSet(false);
                }
            }
            return result.get();
        }

        private void handleSortClause(
                String sortClause,
                ConstraintValidatorContextImpl contextImpl,
                AtomicBoolean result) {
            String[] sortClauseItems = sortClause.split(" ");

            String sortField = sortClauseItems[0];
            if (!valueList.contains(sortField)) {
                addInvalidSortFieldErrorMessage(contextImpl, sortField);
                result.getAndSet(false);
            }

            String sortOrder = sortClauseItems[1];
            if (!sortOrder.matches(SORT_ORDER)) {
                addInvalidSortOrderErrorMessage(contextImpl, sortOrder);
                result.getAndSet(false);
            }
        }

        public void addInvalidSortFormatErrorMessage(
                ConstraintValidatorContextImpl contextImpl, String value) {
            String errorMessage = "{search.invalid.sort.format}";
            contextImpl.addMessageParameter("0", value);
            contextImpl.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
        }

        public void addInvalidSortOrderErrorMessage(
                ConstraintValidatorContextImpl contextImpl, String sortOrder) {
            String errorMessage = "{search.invalid.sort.order}";
            contextImpl.addMessageParameter("0", sortOrder);
            contextImpl.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
        }

        public void addInvalidSortFieldErrorMessage(
                ConstraintValidatorContextImpl contextImpl, String sortField) {
            String errorMessage = "{search.invalid.sort.field}";
            contextImpl.addMessageParameter("0", sortField);
            contextImpl.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
        }
    }
}
