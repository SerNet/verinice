// Requires ./formatter.js & ./bp_enum.properties

var implStatusStyles = {
    na : {
        backgroundColor : "#AE3FBF",
        color : "#FFFFFF"
    },
    partially : {
        backgroundColor : "#F9EB35",
        color : "#000000"
    },
    yes : {
        backgroundColor : "#3FBF43",
        color : "#FFFFFF"
    },
    no : {
        backgroundColor : "#BF3F3F",
        color : "#FFFFFF"
    }
};

function applyImplementationStatusStyle(textElement, localizedValue) {
    applyEnumStyle(textElement.getStyle(), "implementation_status",
            localizedValue, implStatusStyles)
}