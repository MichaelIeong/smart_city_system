package edu.fudan.se.sctap_lowcode_tool.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

public class TimeAdjustUtils {

    private static LocalDateTime parse(PointInTime request) throws BadDateTimeArgumentException {
        LocalDate date = resolveDate(request);
        LocalTime time = resolveTime(request);
        return adjustDateTime(date, time, request.dateTimeAdjustAmount(), request.dateTimeAdjustUnit());
    }

    private static LocalDate resolveDate(PointInTime request) throws BadDateTimeArgumentException {
        String dateBy = request.dateBy();
        if (dateBy == null) throw new BadDateTimeArgumentException("dateBy is required");
        return switch (dateBy) {
            case "absolute" -> resolveDateByAbsolute(request.dateValue());
            case "relative" -> resolveDateByRelative(request.dateAdjustAmount(), request.dateAdjustUnit());
            case "now" -> LocalDate.now();
            case "natural" -> resolveDateByDescription(request.dateDescription());
            default -> throw new BadDateTimeArgumentException("Unknown dateBy: " + dateBy);
        };
    }

    private static LocalTime resolveTime(PointInTime request) throws BadDateTimeArgumentException {
        String timeBy = request.timeBy();
        if (timeBy == null) throw new BadDateTimeArgumentException("timeBy is required");
        return switch (timeBy) {
            case "absolute" -> resolveTimeByAbsolute(request.timeValue());
            case "now" -> LocalTime.now();
            default -> throw new BadDateTimeArgumentException("Unknown timeBy: " + timeBy);
        };
    }

    private static LocalDateTime adjustDateTime(LocalDate date, LocalTime time, Integer amount, String unit)
        throws BadDateTimeArgumentException {
        try {
            if (amount == null || unit == null) return LocalDateTime.of(date, time);
            return switch (unit) {
                case "DAYS" -> date.atTime(time).plusDays(amount);
                case "WEEKS" -> date.atTime(time).plusWeeks(amount);
                case "MONTHS" -> date.atTime(time).plusMonths(amount);
                case "YEARS" -> date.atTime(time).plusYears(amount);
                case "HOURS" -> date.atTime(time).plusHours(amount);
                case "MINUTES" -> date.atTime(time).plusMinutes(amount);
                case "SECONDS" -> date.atTime(time).plusSeconds(amount);
                default -> throw new BadDateTimeArgumentException("Unknown dateTime adjust unit: " + unit);
            };
        } catch (Exception e) {
            throw new BadDateTimeArgumentException(e);
        }
    }

    private static LocalDate resolveDateByAbsolute(String dateValue) throws BadDateTimeArgumentException {
        try {
            if (dateValue == null)
                throw new BadDateTimeArgumentException("dateValue is required when dateBy is absolute");
            return LocalDate.parse(dateValue);
        } catch (DateTimeParseException e) {
            throw new BadDateTimeArgumentException(e);
        }
    }

    private static LocalDate resolveDateByRelative(Integer dateAdjustAmount, String dateAdjustUnit)
        throws BadDateTimeArgumentException {
        try {
            if (dateAdjustAmount == null || dateAdjustUnit == null)
                throw new BadDateTimeArgumentException(
                    "dateAdjustAmount and dateAdjustUnit are required when dateBy is relative"
                );
            return switch (dateAdjustUnit) {
                case "DAYS" -> LocalDate.now().plusDays(dateAdjustAmount);
                case "WEEKS" -> LocalDate.now().plusWeeks(dateAdjustAmount);
                case "MONTHS" -> LocalDate.now().plusMonths(dateAdjustAmount);
                case "YEARS" -> LocalDate.now().plusYears(dateAdjustAmount);
                default -> throw new BadDateTimeArgumentException("Unknown date adjust unit: " + dateAdjustUnit);
            };
        } catch (Exception e) {
            throw new BadDateTimeArgumentException(e);
        }
    }

    private static LocalDate resolveDateByDescription(String desc) throws BadDateTimeArgumentException {
        if (desc == null) throw new BadDateTimeArgumentException("dateDescription is required when dateBy is natural");
        return switch (desc) {
            case "beginOfThisWeek" -> LocalDate.now().with(DayOfWeek.MONDAY);
            case "endOfThisWeek" -> LocalDate.now().with(DayOfWeek.SUNDAY);
            case "beginOfThisMonth" -> LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            case "endOfThisMonth" -> LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            case "beginOfThisYear" -> LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            case "endOfThisYear" -> LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
            default -> throw new BadDateTimeArgumentException("Unknown date description: " + desc);
        };
    }

    private static LocalTime resolveTimeByAbsolute(String timeValue) throws BadDateTimeArgumentException {
        try {
            if (timeValue == null)
                throw new BadDateTimeArgumentException("timeValue is required when timeBy is absolute");
            return LocalTime.parse(timeValue);
        } catch (DateTimeParseException e) {
            throw new BadDateTimeArgumentException(e);
        }
    }

    public record PointInTime(
        String dateBy, // "absolute", "relative", "now", "natural"
        String dateValue, // required when "dateBy" is "absolute": "yyyy-MM-dd"
        Integer dateAdjustAmount, // required when "dateBy" is "relative": int
        String dateAdjustUnit, // required when "dateBy" is "relative": "DAYS", "WEEKS", "MONTHS", "YEARS"
        String dateDescription, // required when "dateBy" is "natural": "begin/endOfThisWeek/Month/Year"
        String timeBy, // "absolute", "now"
        String timeValue, // required when "timeBy" is "absolute": "HH:mm:ss"
        Integer dateTimeAdjustAmount, // Optional, int
        String dateTimeAdjustUnit // Optional, "DAYS", "WEEKS", "MONTHS", "YEARS", "HOURS", "MINUTES", "SECONDS"
    ) {
        public LocalDateTime toLocalDateTime() throws BadDateTimeArgumentException {
            return TimeAdjustUtils.parse(this);
        }
    }

    public static class BadDateTimeArgumentException extends Exception {
        public BadDateTimeArgumentException(Throwable cause) {
            super(cause);
        }

        public BadDateTimeArgumentException(String s) {
            super(s);
        }
    }


}

