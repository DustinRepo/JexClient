package me.dustin.jex.helper.world.seed.randomreversor.util;

public class StringUtils
{
    public static String tableToString(final int rows, final int columns, final TableCellFunction<String> cellExtractor) {
        return tableToString(rows, columns, cellExtractor, (row, column) -> {
            if (column == 0) {
                return "[";
            }
            else if (column == columns) {
                return "]";
            }
            else {
                return " ";
            }
        });
    }
    
    public static String tableToString(final int rows, final int columns, final TableCellFunction<String> cellExtractor, final TableCellFunction<String> separator) {
        final StringBuilder[][] parts = new StringBuilder[columns * 2 + 1][rows];
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                parts[column * 2][row] = new StringBuilder(separator.get(row, column));
                parts[column * 2 + 1][row] = new StringBuilder(cellExtractor.get(row, column));
            }
            parts[columns * 2][row] = new StringBuilder(separator.get(row, columns));
        }
        for (final StringBuilder[] column2 : parts) {
            int columnWidth = 0;
            for (final StringBuilder cell : column2) {
                if (cell.length() > columnWidth) {
                    columnWidth = cell.length();
                }
            }
            for (final StringBuilder cell : column2) {
                final int whitespace = columnWidth - cell.length();
                for (int i = 0, e = whitespace / 2; i < e; ++i) {
                    cell.insert(0, " ");
                }
                for (int i = 0, e = (whitespace + 1) / 2; i < e; ++i) {
                    cell.insert(0, " ");
                }
            }
        }
        final StringBuilder finalStr = new StringBuilder();
        for (int row2 = 0; row2 < rows; ++row2) {
            if (row2 != 0) {
                finalStr.append("\n");
            }
            for (final StringBuilder[] column3 : parts) {
                finalStr.append((CharSequence)column3[row2]);
            }
        }
        return finalStr.toString();
    }
    
    @FunctionalInterface
    public interface TableCellFunction<T>
    {
        T get(final int p0, final int p1);
    }
}
