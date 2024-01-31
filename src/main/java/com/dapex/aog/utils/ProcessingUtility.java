package com.dapex.aog.utils;

import com.dapex.aog.config.Constants;

public class ProcessingUtility {

    public static String htmlToRaw(String... htmls) {
        StringBuilder raw = new StringBuilder();
        for (String html : htmls) {
            if (html != null) {
                String joinedDivs = html.replaceAll("(</*div\\w*[^>]*>)+", Constants.LEFT_BLANK);
                raw.append(joinedDivs.replaceAll("(</*\\w*[^>]*>)+", " "));
            }
            raw.append(" | ");
        }
        return raw.toString();
    }

    public static StringBuilder removeDelimiter(StringBuilder text, String delimiter) {
        int delimIndex = text.lastIndexOf(delimiter);
        if (delimIndex >= 0) {
            return text.delete(delimIndex, text.length());
        }
        return text;
    }

    public static String addToolTip(String input) {
        return input;
    }
}
