importPackage( Packages.java.time );
importPackage( Packages.java.time.format );

var mediumDateFormat = DateTimeFormatter.ofPattern(getMessage("medium_date_pattern"));

/**
 * Applies a predefined style for given enum value (can be used in "onRender"
 * script).
 * 
 * @param targetStyle
 *            Target element's style object (its properties may be overwritten).
 * @param enumPrefix
 *            Message/property prefix (required to interpret localized value).
 * @param localizedValue
 * @param styleDefinitionObj
 *            Keys are enum member keys, values are style definitions. Example:
 *            {foo: {color: "blue"}, bar: {color: "green"}}
 * @returns void
 */
function applyEnumStyle(targetStyle, enumPrefix, localizedValue,
        styleDefinitionObj) {
    var originStyle;
    for ( var key in styleDefinitionObj) {
        if (localizedValue == getMessage(enumPrefix + "_" + key)) {
            originStyle = styleDefinitionObj[key];
        }
    }
    for ( var key in originStyle) {
        targetStyle[key] = originStyle[key];
    }
}

function formatDate(isoDateString) {
    if (!isoDateString || isoDateString == "null") {
        return "-";
    }
    return LocalDate.parse(isoDateString).format(mediumDateFormat);
}

function getLabeledEnumVal(messageKey, numberValue) {
    return getLabeledVal(messageKey, getMessage(messageKey + "_" + numberValue));
}

function getLabeledVal(labelKey, value) {
    return "<b>" + getMessage(labelKey) + "</b>: " + (value || "-");
}

function getLabeledVal_normal(labelKey, value) {
    return getMessage(labelKey) + ": " + (value || "-");
}

function getMessage(messageKey) {
    return reportContext.getMessage(messageKey, reportContext.getLocale());
}

function yesOrNo(numericString) {
    return getMessage(numericString === "1" ? "yes" : "no");
}
