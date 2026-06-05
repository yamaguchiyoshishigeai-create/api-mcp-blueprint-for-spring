(function () {
    'use strict';

    function findFreeTextArea() {
        return document.querySelector('textarea[name="freeText"], #freeTextInput, #freeText');
    }

    function insertFreeTextSample(sampleText) {
        const textarea = findFreeTextArea();
        if (!textarea) {
            return;
        }

        const currentText = textarea.value.trim();
        const nextText = (sampleText || '').trim();
        if (!nextText) {
            return;
        }

        if (!currentText) {
            textarea.value = nextText;
        } else if (!currentText.includes(nextText)) {
            const normalizedCurrent = currentText.replace(/[。．.\s]+$/, '');
            textarea.value = normalizedCurrent + '。また、' + nextText;
        }

        textarea.focus();
    }

    function clearFreeText() {
        const textarea = findFreeTextArea();
        if (!textarea) {
            return;
        }

        textarea.value = '';
        textarea.focus();
    }

    window.insertFreeTextSample = insertFreeTextSample;
    window.clearFreeText = clearFreeText;
}());
