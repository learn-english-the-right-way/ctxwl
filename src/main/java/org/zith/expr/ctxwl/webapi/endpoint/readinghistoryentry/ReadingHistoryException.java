package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import org.zith.expr.ctxwl.webapi.common.WebApiDataException;

public class ReadingHistoryException extends WebApiDataException {
    public static class FieldNotAcceptedException extends ReadingHistoryException {

        private final String fieldName;

        public FieldNotAcceptedException(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
