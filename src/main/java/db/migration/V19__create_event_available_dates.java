package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class V19__create_event_available_dates extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.execute(
                    """
                            CREATE TABLE event_available_dates (
                                event_id BIGINT NOT NULL,
                                sort_order INT NOT NULL,
                                available_date DATE NOT NULL,
                                CONSTRAINT pk_event_available_dates PRIMARY KEY (event_id, sort_order),
                                CONSTRAINT uq_event_available_dates UNIQUE (event_id, available_date),
                                CONSTRAINT fk_event_available_dates_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
                            )
                            """);
        }

        try (
                PreparedStatement selectEvents = context.getConnection().prepareStatement(
                        "SELECT id, date_start, date_end FROM events ORDER BY id");
                ResultSet resultSet = selectEvents.executeQuery();
                PreparedStatement insertSelectedDate = context.getConnection().prepareStatement(
                        "INSERT INTO event_available_dates(event_id, sort_order, available_date) VALUES (?, ?, ?)")) {
            int batchCount = 0;
            while (resultSet.next()) {
                long eventId = resultSet.getLong("id");
                LocalDate currentDate = resultSet.getDate("date_start").toLocalDate();
                LocalDate endDate = resultSet.getDate("date_end").toLocalDate();
                int sortOrder = 0;

                while (!currentDate.isAfter(endDate)) {
                    insertSelectedDate.setLong(1, eventId);
                    insertSelectedDate.setInt(2, sortOrder);
                    insertSelectedDate.setDate(3, Date.valueOf(currentDate));
                    insertSelectedDate.addBatch();

                    currentDate = currentDate.plusDays(1);
                    sortOrder += 1;
                    batchCount++;

                    if (batchCount % 1000 == 0) {
                        insertSelectedDate.executeBatch();
                    }
                }
            }

            if (batchCount % 1000 != 0) {
                insertSelectedDate.executeBatch();
            }
        }
    }
}
