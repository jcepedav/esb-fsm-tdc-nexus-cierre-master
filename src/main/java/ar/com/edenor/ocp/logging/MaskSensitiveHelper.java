package ar.com.edenor.ocp.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskSensitiveHelper {
    private static final String ELEMENT_NAME_TEMPLATE = "-ELEMENT_NAME-";
    private static final String MATCH_PATTERN_XML_TEMPLATE = "<-ELEMENT_NAME->(.*?)</-ELEMENT_NAME->";
    private static final String MATCH_PATTERN_STRING_JSON_TEMPLATE = "\"-ELEMENT_NAME-\"[ \\t]*:[ \\t]*\"(.*?)\"";
    private static final String MATCH_PATTERN_0_1_JSON_TEMPLATE = "\"-ELEMENT_NAME-\"[ \\t]*:[ \\t]*[01]";
    private static final String REPLACEMENT_XML_TEMPLATE = "<-ELEMENT_NAME->XXX</-ELEMENT_NAME->";
    private static final String REPLACEMENT_JSON_TEMPLATE = "\"-ELEMENT_NAME-\": \"XXX\"";
    private static final String MASKED_HEADER_VALUE = "XXX";

    private static final String XML_CONTENT = "xml";
    private static final String HTML_CONTENT = "html";
    private static final String JSON_CONTENT = "json";

    public static class ReplacementPair {
        private final Pattern matchPattern;
        private final String replacement;

        ReplacementPair(String matchPattern, String replacement) {
            this.matchPattern = Pattern.compile(matchPattern);
            this.replacement = replacement;
        }
    }


    public void addSensitiveElementNames(final Set<String> inSensitiveElementNames, Set<ReplacementPair> replacementsXML, Set<ReplacementPair> replacementsJSON) {
        for (final String sensitiveName : inSensitiveElementNames) {
            addReplacementPair(MATCH_PATTERN_XML_TEMPLATE, REPLACEMENT_XML_TEMPLATE, sensitiveName, replacementsXML);
            addReplacementPair(MATCH_PATTERN_STRING_JSON_TEMPLATE, REPLACEMENT_JSON_TEMPLATE, sensitiveName, replacementsJSON);
        }
    }

    private void addReplacementPair(final String matchPatternTemplate,
                                    final String replacementTemplate,
                                    final String sensitiveName,
                                    final Set<ReplacementPair> replacements) {
        final String matchPatternXML = matchPatternTemplate.replaceAll(ELEMENT_NAME_TEMPLATE, sensitiveName);
        final String replacementXML = replacementTemplate.replaceAll(ELEMENT_NAME_TEMPLATE, sensitiveName);
        replacements.add(new ReplacementPair(matchPatternXML, replacementXML));
    }

    public String maskSensitiveElements(
            final Message message,
            final String originalLogString, Set<ReplacementPair> replacementsXML, Set<ReplacementPair> replacementsJSON) {
        if (replacementsXML.isEmpty() && replacementsJSON.isEmpty()
                || message == null
                || originalLogString == null) {
            return originalLogString;
        }
        final String contentType = (String) message.get(Message.CONTENT_TYPE);
        if (Objects.nonNull(contentType)) {
            if (contentType.toLowerCase().contains(XML_CONTENT)
                    || contentType.toLowerCase().contains(HTML_CONTENT)) {
                return applyMasks(originalLogString, replacementsXML);
            } else if (contentType.toLowerCase().contains(JSON_CONTENT)) {
                return applyMasks(originalLogString, replacementsJSON);
            }
        }
        return originalLogString;
    }

    public void maskHeaders(
            final Map<String, String> headerMap,
            final Set<String> sensitiveHeaderNames) {
        sensitiveHeaderNames.stream()
                .forEach(h -> {
                    headerMap.computeIfPresent(h, (key, value) -> MASKED_HEADER_VALUE);
                });
    }

    private String applyMasks(String originalLogString, Set<ReplacementPair> replacementPairs) {
        String resultString = originalLogString;
        for (final ReplacementPair replacementPair : replacementPairs) {
            resultString = replacementPair.matchPattern.matcher(resultString).replaceAll(replacementPair.replacement);
        }
        return resultString;
    }

    public String findValue(String value, String originalLogString, FindTypeValue findTypeValue) {
        String matchPatternXML = "";
        if (findTypeValue.compareTo(FindTypeValue.STRING)==0) {
            matchPatternXML = MATCH_PATTERN_STRING_JSON_TEMPLATE.replaceAll(ELEMENT_NAME_TEMPLATE, value);

        } else {
            matchPatternXML = MATCH_PATTERN_0_1_JSON_TEMPLATE.replaceAll(ELEMENT_NAME_TEMPLATE, value);
        }
        final String replacementXML = REPLACEMENT_JSON_TEMPLATE.replaceAll(ELEMENT_NAME_TEMPLATE, value);
        ReplacementPair replacementPair = new ReplacementPair(matchPatternXML, replacementXML);
        String resultString = originalLogString;
        Matcher matcher = replacementPair.matchPattern.matcher(resultString);
        boolean findResult = matcher.find();
        String token = "";

        //this.group()
        if (findResult) {
            String result = matcher.group();
            StringTokenizer stringTokenizer = new StringTokenizer(result,":");
            //descarto el primero que es el nombre del atributo
            stringTokenizer.nextToken();
            if (stringTokenizer.countTokens()<=1) {
                token = stringTokenizer.nextToken();
            } else {
                while (stringTokenizer.hasMoreElements()) {
                    token = token.concat(stringTokenizer.nextToken());
                }
            }
            if (Objects.nonNull(token) ) {
                token = StringUtils.remove(token,'"');
            }
        }

        return token;
    }

    public enum FindTypeValue {
        STRING,
        NUMBER
    }
    public String findValueIntoJsonMessage(String value, String originalLogString) {
        String matchPatternJSON = MATCH_PATTERN_STRING_JSON_TEMPLATE.replaceAll(ELEMENT_NAME_TEMPLATE, value);
        String replacementJSON = REPLACEMENT_JSON_TEMPLATE.replaceAll(ELEMENT_NAME_TEMPLATE, value);
        ReplacementPair replacementPair = new ReplacementPair(matchPatternJSON, replacementJSON);
        String resultString = originalLogString;
        Matcher matcher = replacementPair.matchPattern.matcher(resultString);
        boolean findResult = matcher.find();


        String token = "";

        //this.group()
        if (findResult) {
            String result = matcher.group();
            StringTokenizer stringTokenizer = new StringTokenizer(result,":");
            //descarto el primero que es el nombre del atributo
            stringTokenizer.nextToken();
            if (stringTokenizer.countTokens()<=1) {
                token = stringTokenizer.nextToken();
            } else {
                while (stringTokenizer.hasMoreElements()) {
                    token = token.concat(stringTokenizer.nextToken());
                }
            }
            if (Objects.nonNull(token) ) {
                token = StringUtils.remove(token,'"');
            }
        }

        return token;
    }
}