package timeBench.data.io;

import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.collections.IntIterator;
import timeBench.data.TemporalDataException;
import timeBench.data.io.schema.TemporalObjectEncoding;
import timeBench.data.relational.TemporalDataset;

public class TableConverter {

    private static final Logger logger = Logger.getLogger(TableConverter.class);

    // TODO support for multiple schemata (e.g., 2 instants, interval) in a list

    public TemporalDataset importTable(Table table,
            List<TemporalObjectEncoding> encodings) {
        TemporalDataset tmpds = new TemporalDataset();

        // 1. analyze & prepare schemata
        // 1.1. auto-detect schema (optional)

        TreeMap<String, Integer> elements = new TreeMap<String, Integer>();

        // 1.2. prepare table for data elements
        for (TemporalObjectEncoding encoding : encodings) {
            try {
                prepareDataColumns(tmpds, table, encoding);
            } catch (TemporalDataException e) {
                // this is safe if the data element table has no column
                e.printStackTrace();
                System.exit(1);
            }
        }

        // 2. for each data row
        IntIterator rows = table.rows();
        while (rows.hasNext()) {
            Tuple tuple = (Tuple) table.getTuple(rows.nextInt());

            try {
                // 2.1. for each schema
                for (TemporalObjectEncoding encoding : encodings) {

                    // 2.1.1. extract temporal element & append to TempDS
                    encoding.buildTemporalElement(tmpds, tuple, elements);

                    // 2.1.2. if it has data columns
                    if (encoding.getDataColumns().length > 0) {
                        // 2.1.2.1. extract data element & append to TempDS
                        // (optional)
                        int dataRow = addDataElement(tmpds, tuple, encoding);
                        logger.debug("data row " + dataRow);

                        // 2.1.2.2. link temporal element with data element in
                        // TempDS
                        tmpds.addOccurrence(dataRow,
                                elements.get(encoding.getKey()));
                    }
                }
            } catch (TemporalDataException e) {
                logger.warn("skip row import: " + tuple + " Reason: "
                        + e.getMessage());
                e.printStackTrace();
            }
            // 2.2 clear cached temporal elements
            elements.clear();
        }

        return tmpds;
    }

    private void prepareDataColumns(TemporalDataset tmpds, Table table,
            TemporalObjectEncoding schema) throws TemporalDataException {
        Table dataElements = tmpds.getDataElements();

        for (String col : schema.getDataColumns()) {
            if (dataElements.getColumnNumber(col) == -1) {
                // column does not exist yet --> add it
                if (logger.isDebugEnabled())
                    logger.debug("prepare data col \"" + col + "\" type "
                            + table.getColumnType(col));
                dataElements.addColumn(col, table.getColumnType(col));
            } else if (dataElements.getColumnType(col) != table
                    .getColumnType(col)) {
                throw new TemporalDataException("Data column " + col
                        + " already exists with a different type: is "
                        + dataElements.getColumnType(col) + " expected "
                        + table.getColumnType(col));
            } else if (logger.isDebugEnabled())
                logger.debug("skip data col \"" + col + "\" type "
                        + table.getColumnType(col));
        }
    }

    private int addDataElement(TemporalDataset tmpds, Tuple tuple,
            TemporalObjectEncoding schema) {
        Table dataElements = tmpds.getDataElements();

        int rowNumber = dataElements.addRow();
        for (String col : schema.getDataColumns()) {
            if (logger.isTraceEnabled())
                logger.trace("add data item " + col + " value "
                        + tuple.get(col));
            // TODO insert switch (columnType) and call setInt(getInt())
            // if we are more serious about performance 
            dataElements.set(rowNumber, col, tuple.get(col));
        }
        return rowNumber;
    }

    public Table exportTable(TemporalDataset tmpds) {
        throw new UnsupportedOperationException();
    }
}
