package io.irontest.db;

import io.irontest.models.DataTableCell;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(DataTableCellMapper.class)
public abstract class DataTableCellDAO {
    @SqlUpdate("CREATE SEQUENCE IF NOT EXISTS datatable_cell_sequence START WITH 1 INCREMENT BY 1 NOCACHE")
    public abstract void createSequenceIfNotExists();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS datatable_cell (" +
            "id BIGINT DEFAULT datatable_cell_sequence.NEXTVAL PRIMARY KEY, column_id BIGINT NOT NULL, " +
            "row_sequence SMALLINT NOT NULL, value CLOB NOT NULL DEFAULT '', endpoint_id BIGINT, " +
            "created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (column_id) REFERENCES datatable_column(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (endpoint_id) REFERENCES endpoint(id), " +
            "CONSTRAINT DATATABLE_CELL_UNIQUE_ROW_SEQUENCE_CONSTRAINT UNIQUE(column_id, row_sequence), " +
            "CONSTRAINT DATATABLE_CELL_EXCLUSIVE_TYPE_CONSTRAINT CHECK(value = '' OR endpoint_id is null))")
    public abstract void createTableIfNotExists();

    @SqlQuery("select * from datatable_cell where column_id = :columnId order by row_sequence")
    public abstract List<DataTableCell> findByColumnId(@Bind("columnId") long columnId);

    @SqlUpdate("insert into datatable_cell (column_id, row_sequence) " +
            "select :columnId, row_sequence from datatable_cell " +
            "where column_id = (select id from datatable_column where testcase_id = :testcaseId and sequence = 1)")
    public abstract void insertCellsForNewColumn(@Bind("testcaseId") long testcaseId, @Bind("columnId") long columnId);

    @SqlUpdate("insert into datatable_cell (column_id, row_sequence, value) " +
            "with subquery1 as (" +
                "select case when max_row_sequence is null then 1 else max_row_sequence + 1 end as new_row_sequence from (" +
                    "select max(row_sequence) as max_row_sequence " +
                    "from datatable_column col left outer join datatable_cell cel on cel.column_id = col.id " +
                    "where col.testcase_id = :testcaseId and col.name = 'Caption'" +
                ")" +
            ")" +
            "select col.id as column_id, subquery1.new_row_sequence as row_sequence, " +
                "case when col.name = 'Caption' then 'Row ' || to_char(subquery1.new_row_sequence) else '' end as value " +
            "from datatable_column col, subquery1 where col.testcase_id = :testcaseId;")
    public abstract void addRow(@Bind("testcaseId") long testcaseId);

    @SqlUpdate("delete from datatable_cell where row_sequence = :rowSequence and column_id in (" +
            "select column_id from datatable_column where testcase_id = :testcaseId)")
    public abstract void deleteRow(@Bind("testcaseId") long testcaseId, @Bind("rowSequence") short rowSequence);

    @SqlUpdate("update datatable_cell set value = :cell.value, endpoint_id = :endpointId, updated = CURRENT_TIMESTAMP " +
            "where id = :cell.id")
    public abstract void update(@BindBean("cell") DataTableCell cell, @Bind("endpointId") Long endpointId);

    @SqlUpdate("insert into datatable_cell (column_id, row_sequence, value, endpoint_id) " +
            "select :targetColumnId, row_sequence, value, endpoint_id from datatable_cell where column_id = :sourceColumnId")
    public abstract void duplicateByColumn(@Bind("sourceColumnId") long sourceColumnId,
                                           @Bind("targetColumnId") long targetColumnId);
}