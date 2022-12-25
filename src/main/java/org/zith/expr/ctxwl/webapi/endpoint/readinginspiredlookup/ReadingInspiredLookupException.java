package org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup;

import org.zith.expr.ctxwl.webapi.common.WebApiDataException;

public class ReadingInspiredLookupException extends WebApiDataException {
    public static class FieldNotAcceptedException extends ReadingInspiredLookupException {

        private final String fieldName;

        public FieldNotAcceptedException(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
