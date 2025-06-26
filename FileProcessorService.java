package com.ppi.utility.importer.service; // New package for services

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service; // Spring annotation for services

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class responsible for processing Excel and CSV files.
 * This class encapsulates the logic for reading file content,
 * specifically extracting data from D3, D4, D5, D6 cells,
 * and iterating through rows 10 onwards from columns B to I.
 *
 * In this intermediate phase, the extracted data is printed to the console.
 * In future phases, this logic will be extended to map the data to a database.
 */
@Service // Marks this class as a Spring Service, making it a Spring bean
public class FileProcessorService {

    /**
     * Helper method to get cell value as a String, handling different cell types.
     * This method ensures robust reading of various data types from Excel cells.
     *
     * @param cell The Cell object to get the value from. Can be null.
     * @return The cell value as a String, or "[BLANK/NULL]" if the cell is null or blank.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "[BLANK/NULL]";
        }
        // Evaluate formula cells to get their computed value
        if (cell.getCellType() == CellType.FORMULA) {
            switch (cell.getCachedFormulaResultType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return String.valueOf(cell.getDateCellValue());
                    } else {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case ERROR:
                    return "[FORMULA ERROR]";
                default:
                    return "[UNKNOWN FORMULA TYPE]";
            }
        } else {
            // Handle non-formula cells directly
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return String.valueOf(cell.getDateCellValue());
                    } else {
                        // For numeric values, format to avoid scientific notation for small integers
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) { // Check if it's an integer
                            return String.valueOf((long) numericValue);
                        }
                        return String.valueOf(numericValue);
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case BLANK:
                    return "[BLANK]";
                default:
                    return "[UNKNOWN]";
            }
        }
    }

    /**
     * Converts an Excel column letter (e.g., "A", "B", "AA") to its 0-indexed integer equivalent.
     * This is crucial for mapping Excel's column naming convention to array indices.
     *
     * @param colLetter The column letter (e.g., "D", "B", "I").
     * @return The 0-indexed column integer (e.g., "A" returns 0, "D" returns 3).
     */
    private int getColumnIndex(String colLetter) {
        int colIndex = -1; // Initialize to -1 to make 'A' result in 0
        String upperColLetter = colLetter.toUpperCase();
        for (char c : upperColLetter.toCharArray()) {
            colIndex = (colIndex + 1) * 26 + (c - 'A');
        }
        return colIndex;
    }

    /**
     * Processes the given file based on its type (Excel or CSV).
     * This is the main entry point for file processing from the controller.
     *
     * @param file The File object representing the uploaded Excel or CSV file.
     * @throws IOException If an I/O error occurs during file reading.
     * @throws IllegalArgumentException If the file format is unsupported.
     */
    public void processFile(File file) throws IOException, IllegalArgumentException, CsvException {
        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
            readExcelFile(file);
        } else if (fileName.toLowerCase().endsWith(".csv")) {
            try {
                readCsvFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (CsvException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload an Excel (.xls, .xlsx) or CSV (.csv) file.");
        }
    }

    /**
     * Reads and processes an Excel file (.xls or .xlsx).
     * It extracts specific cells (D3, D4, D5, D6) and a range of rows
     * (from row 10 onwards, columns B to I) and prints them to the console.
     * It also prints the full content of the sheet.
     *
     * @param file The Excel file to be read.
     * @throws IOException If an I/O error occurs during file reading.
     * @throws IllegalArgumentException If an unsupported Excel format is provided.
     */
    private void readExcelFile(File file) throws IOException {
        Workbook workbook = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            // Determine the workbook type based on file extension
            if (file.getName().toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis); // For .xlsx files
            } else if (file.getName().toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis); // For .xls files
            } else {
                // This case should ideally be caught by processFile, but good to have here too.
                throw new IllegalArgumentException("Unsupported Excel file format. Please provide .xls or .xlsx file.");
            }

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            System.out.println("\n--- Processing Excel File: " + file.getName() + " ---");

            // --- Section 1: Reading specific cells D3, D4, D5, D6 ---
            System.out.println("\n--- Specific Cells (D3, D4, D5, D6) from Excel ---");
            String[] specificCellRefs = {"D3", "D4", "D5", "D6"};
            for (String ref : specificCellRefs) {
                // Extract column letter (e.g., "D") and row number (e.g., "3")
                String colLetter = ref.replaceAll("[0-9]", "");
                int rowIndex = Integer.parseInt(ref.replaceAll("[A-Z]", "")) - 1; // 0-indexed
                int colIndex = getColumnIndex(colLetter); // 0-indexed

                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell cell = row.getCell(colIndex);
                    System.out.println(ref + ": " + getCellValueAsString(cell));
                } else {
                    System.out.println(ref + ": Row " + (rowIndex + 1) + " is null or does not exist.");
                }
            }
            System.out.println("--- End Specific Cells ---");

            // --- Section 2: Reading rows 10 onwards, columns B to I ---
            System.out.println("\n--- Rows 10 onwards, Columns B-I from Excel ---");
            int startRowIndex = 9; // Row 10 is 0-indexed as 9
            int startColIndex = getColumnIndex("B"); // Column B is 0-indexed
            int endColIndex = getColumnIndex("I");   // Column I is 0-indexed

            // Iterate from the specified start row index till the last row or an empty row
            for (int r = startRowIndex; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    // If row is null, it means no more data in this contiguous block
                    System.out.println("Reached empty row at " + (r + 1) + ", stopping data extraction.");
                    break;
                }

                boolean isEmptyRowInRange = true;
                StringBuilder rowData = new StringBuilder();
                // Iterate through specified columns B to I
                for (int c = startColIndex; c <= endColIndex; c++) {
                    Cell cell = row.getCell(c);
                    String cellValue = getCellValueAsString(cell);
                    rowData.append(cellValue).append("\t");
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        isEmptyRowInRange = false; // Found non-blank cell in range
                    }
                }

                // If all cells in the specified range (B-I) for this row are blank, consider it the end of data.
                if (isEmptyRowInRange) {
                    System.out.println("All cells in range B-I of Row " + (r + 1) + " are blank, stopping data extraction.");
                    break;
                }
                System.out.println("Row " + (r + 1) + ": " + rowData.toString().trim());
            }
            System.out.println("--- End Rows 10 onwards, Columns B-I ---");

        } finally {
            // Ensure resources are closed to prevent memory leaks
            if (workbook != null) {
                workbook.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Reads and processes a CSV file.
     * It extracts specific cells (D3, D4, D5, D6) and a range of rows
     * (from row 10 onwards, columns B to I) and prints them to the console.
     * It also prints the full content of the sheet.
     *
     * @param file The CSV file to be read.
     * @throws IOException If an I/O error occurs during file reading.
     */
    private void readCsvFile(File file) throws IOException, CsvException {
        List<List<String>> csvData = new ArrayList<>();
        // Using OpenCSV library for robust CSV parsing
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> allRecords = reader.readAll();
            for (String[] record : allRecords) {
                csvData.add(Arrays.asList(record));
            }
        }

        System.out.println("\n--- Processing CSV File: " + file.getName() + " ---");

        // --- Section 1: Reading specific cells D3, D4, D5, D6 from CSV ---
        System.out.println("\n--- Specific Cells (D3, D4, D5, D6) from CSV ---");
        // CSV is 0-indexed for both rows and columns.
        // D3 -> row index 2, col index 3
        // D4 -> row index 3, col index 3
        // D5 -> row index 4, col index 3
        // D6 -> row index 5, col index 3
        int[] specificRowIndices = {2, 3, 4, 5}; // Corresponds to rows 3, 4, 5, 6
        int specificColIndex = getColumnIndex("D"); // Column D (index 3)

        for (int r : specificRowIndices) {
            String cellReference = "D" + (r + 1); // For printing "D3", "D4" etc.
            if (r < csvData.size()) { // Check if row exists in loaded data
                List<String> row = csvData.get(r);
                if (specificColIndex < row.size()) { // Check if column exists in this row
                    System.out.println(cellReference + ": " + row.get(specificColIndex));
                } else {
                    System.out.println(cellReference + ": Column D is out of bounds for row " + (r + 1));
                }
            } else {
                System.out.println(cellReference + ": Row " + (r + 1) + " does not exist in CSV data.");
            }
        }
        System.out.println("--- End Specific Cells ---");

        // --- Section 2: Reading rows 10 onwards, columns B to I from CSV ---
        System.out.println("\n--- Rows 10 onwards, Columns B-I from CSV ---");
        int startRowIndex = 9; // Row 10 is 0-indexed as 9
        int startColIndex = getColumnIndex("B"); // Column B (index 1)
        int endColIndex = getColumnIndex("I");   // Column I (index 8)

        for (int r = startRowIndex; r < csvData.size(); r++) {
            List<String> row = csvData.get(r);
            boolean isEmptyRowInRange = true;
            StringBuilder rowData = new StringBuilder();

            for (int c = startColIndex; c <= endColIndex; c++) {
                if (c < row.size()) {
                    String cellValue = row.get(c).trim(); // Get value and trim whitespace
                    rowData.append(cellValue).append("\t");
                    if (!cellValue.isEmpty()) {
                        isEmptyRowInRange = false; // Found non-empty cell in range
                    }
                } else {
                    rowData.append("[OUT_OF_BOUNDS]\t"); // Indicate cell outside available range
                }
            }

            // If all cells in the specified range (B-I) for this row are blank/empty, consider it the end of data.
            if (isEmptyRowInRange) {
                System.out.println("All cells in range B-I of Row " + (r + 1) + " are blank/empty, stopping data extraction.");
                break;
            }
            System.out.println("Row " + (r + 1) + ": " + rowData.toString().trim());
        }
        System.out.println("--- End Rows 10 onwards, Columns B-I ---");


    }
}
