package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

public class InvalidJsonValueException extends Exception {
    public InvalidJsonValueException(String message) {
        super(message);
    }

    public InvalidJsonValueException(String location, String prompt, String key, String value) {
        super("An invalid value is found in " + location + ".\n" +
                "Info: " + prompt + "\n" +
                "Key: " + key + "\n" +
                "Value: " + value);
    }

    public InvalidJsonValueException(Exception e){
        super(e);
    }
}
