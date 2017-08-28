var jsutils = {
    setCaretPosition: function (elemId, caretPos) {
	var el = document.getElementById(elemId);

	el.value = el.value;

	if (el !== null) {

            if (el.createTextRange) {
		var range = el.createTextRange();
		range.move('character', caretPos);
		range.select();
		return true;
            }

            else {
		// (el.selectionStart === 0 added for Firefox bug)
		if (el.selectionStart || el.selectionStart === 0) {
                    el.focus();
                    el.setSelectionRange(caretPos, caretPos);
                    return true;
		}

		else  { // fail city, fortunately this never happens (as far as I've tested) :)
                    el.focus();
                    return false;
		}
            }
	}
    },

    getCaretPosition: function (elemId) {

	var oField = document.getElementById(elemId);

	// Initialize
	var iCaretPos = 0;

	// IE Support
	if (document.selection) {

	    // Set focus on the element
	    oField.focus();

	    // To get cursor position, get empty selection range
	    var oSel = document.selection.createRange();

	    // Move selection start to 0 position
	    oSel.moveStart('character', -oField.value.length);

	    // The caret position is selection length
	    iCaretPos = oSel.text.length;
	}

	// Firefox support
	else if (oField.selectionStart || oField.selectionStart == '0')
	    iCaretPos = oField.selectionStart;

	// Return results
	return iCaretPos;
    }
};

module.exports = jsutils;
