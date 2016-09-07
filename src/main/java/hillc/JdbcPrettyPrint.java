/*******************************************************************************
 * Copyright (C) 2016, Christopher Hill <ch6574@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package hillc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

public class JdbcPrettyPrint {

    /**
     * Dumps a SQL ResultSet out in a human readable-ish format.
     *
     * @param rs
     *            a JDBC ResultSet
     * @reurn Given sample input from a query like
     *
     *        <pre>
     *  SQL> select col1, col789, colx from foo;
     *        </pre>
     *
     *        Formatted output that looks as follows will be returned
     *
     *        <pre>
     * +---------+--------+----------------------+
     * |    COL1 | COL789 |                 COLX |
     * +---------+--------+----------------------+
     * | aaaaaaa |   12.3 |      xxxxxxxxxxxxxxx |
     * | bbbbbbb |   12.3 |              xxxxxxx |
     * | ccccccc |   12.3 | xxxxxxxxxxxxxxxxxxxx |
     * +---------+--------+----------------------+
     *        </pre>
     *
     *        Note: Everything is pulled as a String, so formatting isn't always the best.
     */
    static String prettyPrintResultSet(final ResultSet rs) {
        final StringBuilder prettyPrinted = new StringBuilder();
        try {
            if (rs == null || !rs.isBeforeFirst()) {
                prettyPrinted.append("No results supplied!");
            } else {
                final ResultSetMetaData rsmd = rs.getMetaData();
                final int colCount = rsmd.getColumnCount();

                final StringBuilder divide = new StringBuilder("+"); // fixed test used to divide sections
                final StringBuilder format = new StringBuilder("|"); // printf style format layout
                final Object[] args = new Object[colCount]; // the varargs data to format for display

                // Get each column name and size
                for (int i = 1; i <= colCount; i++) {
                    final String name = rsmd.getColumnLabel(i);
                    args[i - 1] = name;

                    // Column is as wide as data or name, whatever is bigger
                    final int width = Math.max(rsmd.getColumnDisplaySize(i), name.length());
                    format.append(" %").append(width).append("s |"); // Makes " %ns |" format arguments, where n is column width

                    // ASCII art style divider
                    divide.append(StringUtils.repeat("-", width + 2)).append("+");
                }

                // Write the column names as header
                prettyPrinted.append(divide).append(StringUtils.LF);
                prettyPrinted.append(String.format(format.toString(), args)).append(StringUtils.LF);
                prettyPrinted.append(divide).append(StringUtils.LF);

                // Now write all the data rows
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        args[i - 1] = rs.getString(i);
                    }
                    prettyPrinted.append(String.format(format.toString(), args)).append(StringUtils.LF);
                }
                prettyPrinted.append(divide).append(StringUtils.LF);
            }
        } catch (final SQLException e) {
            prettyPrinted.append("Something went wrong! ").append(e);
        }
        return prettyPrinted.toString();
    }
}
