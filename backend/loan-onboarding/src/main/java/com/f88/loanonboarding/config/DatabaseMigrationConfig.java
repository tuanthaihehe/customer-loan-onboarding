package com.f88.loanonboarding.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("db")
@ConditionalOnProperty(name = "app.database.migration.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseMigrationConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationConfig.class);

    private static final String MIGRATION_LOCATION = "classpath:db/migration";
    private static final String SEED_LOCATION = "classpath:db/seed";
    private static final String MIGRATION_HISTORY_TABLE = "flyway_schema_history";
    private static final String SEED_HISTORY_TABLE = "flyway_seed_schema_history";

    @Bean
    ApplicationRunner databaseMigrationRunner(DataSource dataSource) {
        return args -> {
            log.info("Running staged Flyway migration and seed scripts");

            migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "1", false);
            migrate(dataSource, SEED_LOCATION, SEED_HISTORY_TABLE, "2", true);
            migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "4", false);
            migrate(dataSource, SEED_LOCATION, SEED_HISTORY_TABLE, "3", true);
            migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "6", false);
            migrate(dataSource, SEED_LOCATION, SEED_HISTORY_TABLE, "4", true);
            migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "8", false);
            migrate(dataSource, SEED_LOCATION, SEED_HISTORY_TABLE, "5", true);
            migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "9", false);
            migrate(dataSource, SEED_LOCATION, SEED_HISTORY_TABLE, "6", true);
            migrate(dataSource, MIGRATION_LOCATION, MIGRATION_HISTORY_TABLE, "10", false);
        };
    }

    private void migrate(DataSource dataSource, String location, String table, String targetVersion, boolean baselineOnMigrate) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .table(table)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .target(targetVersion)
                .load()
                .migrate();
    }
}
