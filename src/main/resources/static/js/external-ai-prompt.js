(function () {
    'use strict';

    let copyFeedbackTimer = null;

    function switchJsonInputMode(mode) {
        const pastePanel = document.getElementById('jsonPastePanel');
        const filePanel = document.getElementById('jsonFilePanel');
        if (!pastePanel || !filePanel) {
            return;
        }

        const showPaste = mode === 'paste';
        pastePanel.hidden = !showPaste;
        pastePanel.setAttribute('aria-hidden', String(!showPaste));
        filePanel.hidden = showPaste;
        filePanel.setAttribute('aria-hidden', String(showPaste));
    }

    function showCopyFeedback(copyStatus, message, isError) {
        if (!copyStatus) {
            return;
        }

        copyStatus.textContent = message;
        copyStatus.hidden = false;
        copyStatus.classList.toggle('copy-feedback-error', Boolean(isError));

        if (copyFeedbackTimer) {
            window.clearTimeout(copyFeedbackTimer);
        }

        copyFeedbackTimer = window.setTimeout(function () {
            copyStatus.hidden = true;
            copyStatus.classList.remove('copy-feedback-error');
        }, 2500);
    }

    function copyGeneratedPrompt() {
        const promptText = document.getElementById('externalAiPromptText');
        const copyStatus = document.getElementById('copyStatus');
        if (!promptText) {
            return;
        }

        const text = promptText.value || promptText.textContent || promptText.innerText || '';
        const showCopied = function () {
            showCopyFeedback(copyStatus, 'コピーされました', false);
        };
        const showFailed = function () {
            showCopyFeedback(copyStatus, 'コピーできませんでした。手動で選択してコピーしてください。', true);
        };

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(showCopied).catch(function () {
                promptText.focus();
                promptText.select();
                try {
                    document.execCommand('copy');
                    showCopied();
                } catch (error) {
                    showFailed();
                }
            });
            return;
        }

        promptText.focus();
        promptText.select();
        try {
            document.execCommand('copy');
            showCopied();
        } catch (error) {
            showFailed();
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        const checkedMode = document.querySelector('input[name="jsonInputMode"]:checked');
        switchJsonInputMode(checkedMode ? checkedMode.value : 'file');
    });

    window.switchJsonInputMode = switchJsonInputMode;
    window.copyGeneratedPrompt = copyGeneratedPrompt;
}());
