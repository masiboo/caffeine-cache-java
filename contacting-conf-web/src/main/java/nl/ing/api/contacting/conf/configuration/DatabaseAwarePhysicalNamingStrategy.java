package nl.ing.api.contacting.conf.configuration;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAwarePhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        String tableName = name.getText();
        if (isPostgreSQL(jdbcEnvironment)) {
            return Identifier.toIdentifier("\"" + tableName + "\"");
        }
        return name;
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        String sequenceName = name.getText();
        if (isPostgreSQL(jdbcEnvironment)) {
            return Identifier.toIdentifier("\"" + sequenceName + "\"");
        }
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        String columnName = name.getText();
        if (isPostgreSQL(jdbcEnvironment)) {
            return Identifier.toIdentifier("\"" + columnName + "\"");
        }
        return name;
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    private boolean isPostgreSQL(JdbcEnvironment jdbcEnvironment) {
        String dialectName = jdbcEnvironment.getDialect().getClass().getSimpleName().toLowerCase();
        return dialectName.contains("postgresql") ||
                (datasourceUrl != null && datasourceUrl.contains("postgresql"));
    }
}
