package org.zith.expr.ctxwl.common.hibernate;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.function.Function;
import java.util.regex.Pattern;

public class LowerUnderscorePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private final Function<String, String> tableNameMapper = makeTableNameMapper();
    private final Function<String, String> columnNameMapper = makeColumnNameMapper();

    private Function<String, String> makeTableNameMapper() {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
    }

    private Function<String, String> makeColumnNameMapper() {
        var baseConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
        var suffixPattern = Pattern.compile("^(.*?)(_?[\\p{Upper}\\p{Digit}]+)?$");
        return name -> {
            var m = suffixPattern.matcher(name);
            if (!m.matches()) throw new IllegalStateException();
            return baseConverter.convert(Strings.nullToEmpty(m.group(1)))
                    + Strings.nullToEmpty(m.group(2)).toLowerCase();
        };
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return convert(tableNameMapper, name, context);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return convert(columnNameMapper, name, context);
    }

    private Identifier convert(Function<String, String> mapper, Identifier name, JdbcEnvironment context) {
        return context.getIdentifierHelper().toIdentifier(mapper.apply(name.getText()));
    }
}
