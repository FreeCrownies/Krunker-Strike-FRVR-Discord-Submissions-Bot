package constants;

import java.util.regex.Pattern;

public interface RegexPatterns {

    Pattern DIGIT_REFORMAT_PATTERN = Pattern.compile("\\b[0-9]+[\\s| ]+[0-9]");
    Pattern AMOUNT_FILTER_PATTERN = Pattern.compile("\\b(?<digits1>-?\\d{1,16}([.|,]\\d{1,16})?)[\\\\s| ]*(?<unit1>[\\\\w|%]*)");
    Pattern MINUTES_PATTERN = Pattern.compile("\\b\\d{1,4}[\\s| ]*m(in(utes?)?)?\\b");
    Pattern HOURS_PATTERN = Pattern.compile("\\b\\d{1,4}[\\s| ]*h(ours?)?\\b");
    Pattern DAYS_PATTERN = Pattern.compile("\\b\\d{1,3}[\\s| ]*d(ays?)?\\b");
    Pattern TEXT_PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<inner>[^}]*)}");
    Pattern TEXT_MULTI_OPTION_PATTERN =
//            Pattern.compile("(?<!\\\\)\\[(?<inner>[^]]*)]");
//            Pattern.compile("(?<!\\\\)\\[(?<inner>.*?)(?<!\\\\)\\]", Pattern.DOTALL);
            Pattern.compile("(?<!\\\\)\\[(?<inner>(?:\\\\.|[^\\]\\[\\\\])*)\\]", Pattern.DOTALL);
    Pattern EMOTE = Pattern.compile("<a?:(?<name>[^:]*):(?<id>[0-9]*)>");
    Pattern INTERACTION = Pattern.compile("^/api/v[0-9]*/(interactions|webhooks)/.*");
    Pattern VIDEO_URL = Pattern.compile("\\b(?:https?://|www\\.)\\S+\\.(?i:mp4|avi|mov|flv|wmv|mkv|youtube\\.com/watch\\?v=|youtu\\.be/)\\S+?(?i:\\.(?:mp4|avi|mov|flv|wmv|mkv))?\\b");
    Pattern IMAGE_URL = Pattern.compile("\\bhttps?://\\S+\\.(?i:png|jpe?g|gif|bmp)\\b");

}
