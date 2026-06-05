package app.util;

import java.util.List;

import app.memo.dto.MemoRequest;
import app.memo.dto.MemoResponse;

public class JsonUtil {
    public static MemoRequest toMemoRequest(String json) {
        String content = getStringValue(json, "content");
        return new MemoRequest(content);
    }

    public static String toJson(MemoResponse memo) {
        if (memo == null) {
            return "null";
        }

        return "{"
                + "\"id\":" + memo.getId() + ","
                + "\"content\":\"" + escape(memo.getContent()) + "\""
                + "}";
    }

    public static String toJson(List<MemoResponse> memos) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < memos.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(toJson(memos.get(i)));
        }

        builder.append("]");
        return builder.toString();
    }

    private static String getStringValue(String json, String key) {
        if (json == null) {
            return null;
        }

        String keyText = "\"" + key + "\"";
        int keyIndex = json.indexOf(keyText);

        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(":", keyIndex + keyText.length());

        if (colonIndex < 0) {
            return null;
        }

        int index = colonIndex + 1;

        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }

        if (index >= json.length()) {
            return null;
        }

        if (json.startsWith("null", index)) {
            return null;
        }

        if (json.charAt(index) != '"') {
            return null;
        }

        return readJsonString(json, index + 1);
    }

    private static String readJsonString(String json, int startIndex) {
        StringBuilder builder = new StringBuilder();

        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"') {
                return builder.toString();
            }

            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);

                if (next == '"' || next == '\\' || next == '/') {
                    builder.append(next);
                    i++;
                } else if (next == 'b') {
                    builder.append('\b');
                    i++;
                } else if (next == 'f') {
                    builder.append('\f');
                    i++;
                } else if (next == 'n') {
                    builder.append('\n');
                    i++;
                } else if (next == 'r') {
                    builder.append('\r');
                    i++;
                } else if (next == 't') {
                    builder.append('\t');
                    i++;
                } else if (next == 'u' && i + 5 < json.length()) {
                    String hex = json.substring(i + 2, i + 6);
                    builder.append((char) Integer.parseInt(hex, 16));
                    i += 5;
                } else {
                    builder.append(next);
                    i++;
                }
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == '"') {
                builder.append("\\\"");
            } else if (c == '\\') {
                builder.append("\\\\");
            } else if (c == '\n') {
                builder.append("\\n");
            } else if (c == '\r') {
                builder.append("\\r");
            } else if (c == '\t') {
                builder.append("\\t");
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
