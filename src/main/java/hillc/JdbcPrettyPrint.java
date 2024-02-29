/*******************************************************************************
 * Copyright (c) 2016, Christopher Hill <ch6574@gmail.com>
 * GNU General Public License v3.0+ (see https://www.gnu.org/licenses/gpl-3.0.txt)
 * SPDX-License-Identifier: GPL-3.0-or-later
 ******************************************************************************/
package hillc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class JdbcPrettyPrint {

    /**
     * Dumps a SQL ResultSet out in a human readable-ish format.
     *
     * @param rs            a JDBC ResultSet
     * @param maxFieldWidth upper limit on displayed field width
     * @param maxRowCount   upper limit in displayed rows
     * @return Given sample input from a query like
     *
     * <pre>
     * SQL> select col1, col789, colx from foo;
     * </pre>
     * <p>
     * <p>
     * Formatted output that looks as follows will be returned
     *
     * <pre>
     * +----------+---------+----------------------+
     * |    COL1  | COL789  |                 COLX |
     * | CHAR(10) | INTEGER |                  FOO |
     * +----------+---------+----------------------+
     * | aaaaaaaa |    12.3 |      xxxxxxxxxxxxxxx |
     * | bbbbbbbb |    12.3 |              xxxxxxx |
     * | cccccccc |    12.3 | xxxxxxxxxxxxxxxxxxxx |
     * +----------+---------+----------------------+
     * </pre>
     *
     * <p>
     * Note: Everything is pulled as a String, so formatting isn't always the best.
     */
    static String prettyPrintResultSet(final ResultSet rs, final int maxFieldWidth, final long maxRowCount) {
        final StringBuilder prettyPrinted = new StringBuilder();
        try {
            if (rs == null || !rs.isBeforeFirst()) {
                prettyPrinted.append("No results supplied!");
            } else {
                final ResultSetMetaData rsmd = rs.getMetaData();
                final int colCount = rsmd.getColumnCount();

                final StringBuilder divide = new StringBuilder("+"); // fixed text used to divide sections
                final StringBuilder format = new StringBuilder("|"); // printf style format layout
                final Object[] names = new Object[colCount]; // the varargs data to format for display
                final Object[] types = new Object[colCount]; //

                // Get each column name and size
                for (int i = 1; i <= colCount; i++) {
                    final String name = rsmd.getColumnLabel(i);
                    final JDBCType jdbcType = JDBCType.valueOf(rsmd.getColumnType(i));

                    String type = jdbcType.getName();
                    switch (jdbcType) {
                        // TODO add other types
                        case CHAR:
                        case NCHAR:
                        case VARCHAR:
                        case NVARCHAR:
                        case LONGVARCHAR:
                        case LONGNVARCHAR:
                            type += "(" + rsmd.getColumnDisplaySize(i) + ")";
                            break;
                        case BINARY:
                        case VARBINARY:
                        case LONGVARBINARY:
                            type += "(" + rsmd.getPrecision(i) + ")";
                            break;
                        case DECIMAL:
                        case NUMERIC:
                            type += "(" + rsmd.getPrecision(i) + "," + rsmd.getScale(i) + ")";
                            break;
                    }

                    // Column is as wide as data or name, whatever is bigger
                    final int width = Math.min(NumberUtils.max(rsmd.getColumnDisplaySize(i), name.length(), type.length()), maxFieldWidth);
                    format.append(" %").append(width).append("s |"); // Makes " %ns |" format arguments, where n is column width

                    // ASCII art style divider
                    divide.append(StringUtils.repeat("-", width + 2)).append("+");

                    // Data
                    names[i - 1] = StringUtils.abbreviate(name, width);
                    types[i - 1] = StringUtils.abbreviate(type, width);
                }

                // Write the column names as header
                prettyPrinted.append(divide).append(StringUtils.LF);
                prettyPrinted.append(String.format(format.toString(), names)).append(StringUtils.LF);
                prettyPrinted.append(String.format(format.toString(), types)).append(StringUtils.LF);
                prettyPrinted.append(divide).append(StringUtils.LF);

                // Now write all the data rows
                long rows = 0;
                while (rs.next() && rows++ < maxRowCount) {
                    for (int i = 1; i <= colCount; i++) {
                        names[i - 1] = rs.getString(i);
                    }
                    prettyPrinted.append(String.format(format.toString(), names)).append(StringUtils.LF);
                }
                prettyPrinted.append(divide).append(StringUtils.LF);
            }
        } catch (final SQLException e) {
            prettyPrinted.append("Something went wrong! ").append(e);
        }
        return prettyPrinted.toString();
    }
}
