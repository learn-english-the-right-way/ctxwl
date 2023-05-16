package org.zith.expr.ctxwl.app.config.core.paragraphgenerator;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record AppCoreParagraphGeneratorConfiguration(
        AppOpenAIConfiguration openAI
) implements Configuration<AppCoreParagraphGeneratorConfiguration> {

    @Override
    public AppCoreParagraphGeneratorConfiguration merge(AppCoreParagraphGeneratorConfiguration overriding) {
        return new AppCoreParagraphGeneratorConfiguration(
                Configurations.merge(openAI(), overriding.openAI())
        );
    }

    public static AppCoreParagraphGeneratorConfiguration empty() {
        return new AppCoreParagraphGeneratorConfiguration(AppOpenAIConfiguration.empty());
    }
}
