package org.zith.expr.ctxwl.app.config.core.paragraphgenerator;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record AppOpenAIConfiguration(
        String url,
        String apiKey
) implements Configuration<AppOpenAIConfiguration> {
    @Override
    public AppOpenAIConfiguration merge(AppOpenAIConfiguration overriding) {

        return new AppOpenAIConfiguration(
                Configurations.overlay(url(), overriding.url()),
                Configurations.overlay(apiKey(), overriding.apiKey())
        );
    }

    public static AppOpenAIConfiguration empty() {return new AppOpenAIConfiguration(null, null);}
}
