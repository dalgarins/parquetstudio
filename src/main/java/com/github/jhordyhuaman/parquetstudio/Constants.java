package com.github.jhordyhuaman.parquetstudio;

public class Constants {
    public final static String SCHEMA_PANEL = "SCHEMA_PANEL";
    public final static String DATA_PANEL = "DATA_PANEL";

    public static class Message {
        public final static String SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS = "The schema no haven't the same number of fields that the parquet.";
        public final static String SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS_2 = "<html><span style='color:yellow;'>⚠</span> " + SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS + "</html>";
    }

}
