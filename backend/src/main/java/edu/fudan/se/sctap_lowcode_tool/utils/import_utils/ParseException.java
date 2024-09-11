package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

public class ParseException extends Exception{
    public ParseException(String message) {
        super(message);
    }

    public ParseException(Exception e){
        super(e);
    }
}
