package com.github.jhordyhuaman.parquetstudio;

public class Constants {
    static String SCHEMA_PANEL = "SCHEMA_PANEL";
    static String DATA_PANEL = "DATA_PANEL";

    static class Message {
        static String SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS = "The schema no haven't the same number of fields that the parquet.";
        static String SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS_2 = "<html><span style='color:yellow;'>⚠</span> " + SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS + "</html>";
    }

}
