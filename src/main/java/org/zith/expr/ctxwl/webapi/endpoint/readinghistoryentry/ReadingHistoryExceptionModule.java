package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.name.Named;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionModule;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;

public class ReadingHistoryExceptionModule extends AbstractModule {
    public static final String READING_HISTORY_EXCEPTION_EXPLAINER_DESCRIPTOR =
            "reading_history_exception_explainer_descriptor";
    public static final String UNAUTHORIZED_SESSION_EXCEPTION_EXPLAINER_DESCRIPTOR =
            "unauthorized_session_exception_explainer_descriptor";

    @Provides
    @Singleton
    protected ReadingHistoryExceptionExplainerMaker emailRegistrationExceptionExplainerMaker() {
        return new ReadingHistoryExceptionExplainerMaker();
    }

    @Provides
    @Named(READING_HISTORY_EXCEPTION_EXPLAINER_DESCRIPTOR)
    @Singleton
    protected ExceptionExplainerDescriptor readingHistoryExceptionExplainerDescriptor(
            ReadingHistoryExceptionExplainerMaker readingHistoryExceptionExplainerMaker
    ) {
        return ExceptionExplainerDescriptor.of(
                readingHistoryExceptionExplainerMaker.make(
                        ReadingHistoryErrorCode.INVALID_REQUEST,
                        ReadingHistoryException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                "You request is invalid.")));
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor readingHistoryExceptionExplainerDescriptorInSet(
            @Named(READING_HISTORY_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor readingHistoryExceptionExplainerDescriptor
    ) {
        return readingHistoryExceptionExplainerDescriptor;
    }

    @Provides
    @Named(UNAUTHORIZED_SESSION_EXCEPTION_EXPLAINER_DESCRIPTOR)
    @Singleton
    protected ExceptionExplainerDescriptor unauthorizedSessionExceptionExplainerDescriptor(
            ReadingHistoryExceptionExplainerMaker readingHistoryExceptionExplainerMaker,
            @Named(READING_HISTORY_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor readingHistoryExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                readingHistoryExceptionExplainerMaker.make(
                        ReadingHistoryErrorCode.SESSION_ACCESS_NOT_AUTHORIZED,
                        ReadingHistoryException.UnauthorizedAccessToSessionException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(
                                code,
                                "You are not authorized to access the reading session you specified. " +
                                        "Sessions are authorized in group basis. " +
                                        "Please check that you are authorized to access the session group.")),
                readingHistoryExceptionExplainerDescriptor.explainer());
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor unauthorizedSessionExceptionExplainerDescriptorInSet(
            @Named(UNAUTHORIZED_SESSION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor unauthorizedSessionExceptionExplainerDescriptor
    ) {
        return unauthorizedSessionExceptionExplainerDescriptor;
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor sessionNotFoundExceptionExplainerDescriptor(
            ReadingHistoryExceptionExplainerMaker readingHistoryExceptionExplainerMaker,
            @Named(READING_HISTORY_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor readingHistoryExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                readingHistoryExceptionExplainerMaker.make(
                        ReadingHistoryErrorCode.SESSION_NOT_FOUND,
                        ReadingHistoryException.SessionNotFoundException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(
                                code,
                                "You are not authorized to access the reading session you specified. " +
                                        "The session you were trying to access doesn't not exist. " +
                                        "Please check that you've created it properly.")),
                readingHistoryExceptionExplainerDescriptor.explainer());
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor invalidCredentialExceptionExplainerDescriptor(
            ReadingHistoryExceptionExplainerMaker readingHistoryExceptionExplainerMaker,
            @Named(UNAUTHORIZED_SESSION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor readingHistoryExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                readingHistoryExceptionExplainerMaker.make(
                        ReadingHistoryErrorCode.INVALID_CREDENTIAL,
                        ReadingHistoryException.InvalidCredentialException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(
                                code,
                                "You didn't provide a valid credential, which is required to access a " +
                                        "reading session.")),
                readingHistoryExceptionExplainerDescriptor.explainer());
    }
}
