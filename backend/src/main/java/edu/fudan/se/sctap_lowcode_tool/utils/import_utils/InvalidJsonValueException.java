package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

public class InvalidJsonValueException extends Exception {

    public final String location;
    public final String prompt;
    public final String key;
    public final String value;

    public InvalidJsonValueException(String location, String msg, String key, String value) {
        super("An invalid value is found in " + location + ".\n" +
                "Info: " + msg + "\n" +
                "Key: " + key + "\n" +
                "Value: " + value);
        this.location = location;
        this.prompt = msg;
        this.key = key;
        this.value = value;
    }

}
