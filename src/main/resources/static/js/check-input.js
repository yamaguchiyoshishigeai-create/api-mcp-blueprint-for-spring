(function () {
    'use strict';

    const state = window.APIM_CHECK_INPUT_STATE || {};
    const sampleBusinessRequirements = state.sampleBusinessRequirements || '';
    const restoredSystemTypes = Array.isArray(state.restoredSystemTypes) ? state.restoredSystemTypes : [];
    const restoredRelatedDomains = Array.isArray(state.restoredRelatedDomains) ? state.restoredRelatedDomains : [];

const systemTypePresets = {
        'internal-application-portal': preset(['経費精算', '休暇申請', '社員情報管理', '承認ワークフロー', '通知管理'], '', '申請者、承認者、管理者、経理担当、AIアシスタント', '社内SSO', '社内申請ポータルとして、経費精算、休暇申請、社員情報管理を対象にする。更新・承認・却下は人間確認と監査ログを前提にする。', ['申請者', '承認者', '管理者', '経理担当', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない', 'AIは権限変更を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '承認', '却下'], ['登録', '更新', '承認', '却下', '通知']),
        'sales-commerce': preset(['注文管理', '商品管理', '在庫管理', '請求管理', '決済管理', '配送管理'], '購入者, 出荷担当', '購入者、営業担当、出荷担当、管理者、AIアシスタント', 'OAuth2 / OIDC', 'EC / 販売管理として、注文管理、商品管理、在庫管理を対象にする。削除・外部送信は人間確認を前提にする。', ['営業担当', '管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '削除', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成'], ['AIは削除を直接実行しない', 'AIは外部送信を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '削除', '通知'], ['削除', '外部送信'], ['登録', '更新', '削除', '通知']),
        'support-management': preset(['修理依頼', '顧客管理', '問い合わせ管理', 'FAQ管理', 'ナレッジ検索・要約'], 'サポート担当', 'サポート担当、営業担当、管理者、AIアシスタント', 'Spring Security + セッション認証', '保守サポート管理として、修理依頼、顧客管理、ナレッジ検索・要約を対象にする。外部送信や重要更新は人間確認を前提にする。', ['営業担当', '管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '要約', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは削除を直接実行しない', 'AIは外部送信を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '外部送信'], ['登録', '更新', '通知']),
        'knowledge-platform': preset(['ナレッジ検索・要約', '文書管理', 'FAQ管理', '議事録管理'], '一般利用者, コンテンツ管理者', '一般利用者、コンテンツ管理者、管理者、AIアシスタント', '社内SSO', 'ナレッジ活用基盤として、文書検索、要約、コンテンツ管理を対象にする。削除や重要文書更新は人間確認を前提にする。', ['管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '削除', '要約'], ['検索', '一覧', '詳細参照', '要約', '下書き作成'], ['AIは削除を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '削除'], ['更新', '削除'], ['登録', '更新', '削除']),
        'hr-labor-management': preset(['社員情報管理', '組織管理', '勤怠管理', '休暇申請', '承認ワークフロー'], '人事担当, 労務担当', '人事担当、労務担当、管理者、申請者、承認者、AIアシスタント', '社内SSO', '人事労務管理として、社員情報、組織、勤怠、休暇申請を対象にする。個人情報更新と承認は人間確認と監査ログを前提にする。', ['申請者', '承認者', '管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない', 'AIは権限変更を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '承認', '却下'], ['登録', '更新', '承認', '却下', '通知']),
        'contract-billing-management': preset(['契約管理', '請求管理', '決済管理', '承認ワークフロー', '監査ログ管理'], '法務担当, 請求担当', '法務担当、請求担当、経理担当、管理者、承認者、AIアシスタント', 'OAuth2 / OIDC', '契約・請求管理として、契約、請求、決済、承認、監査ログを対象にする。外部送信、契約更新、請求確定は人間確認を前提にする。', ['承認者', '管理者', '経理担当', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない', 'AIは外部送信を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '承認', '却下', '外部送信'], ['登録', '更新', '承認', '却下', '通知']),
        'devops-management': preset(['開発タスク管理', '障害管理', 'リリース管理', '通知管理', '監査ログ管理'], '開発者, 運用担当, リリース責任者', '開発者、運用担当、リリース責任者、管理者、AIアシスタント', '社内SSO', '開発・運用管理として、開発タスク、障害、リリース、通知を対象にする。リリース承認や重要障害更新は人間確認と監査ログを前提にする。', ['管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない', 'AIは外部送信を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '承認', '却下', '外部送信'], ['登録', '更新', '承認', '却下', '通知']),
        'customer-crm': preset(['顧客管理', '問い合わせ管理', '通知管理', 'ナレッジ検索・要約', 'FAQ管理'], 'カスタマーサクセス担当, サポート担当', 'カスタマーサクセス担当、サポート担当、営業担当、管理者、AIアシスタント', 'OAuth2 / OIDC', '顧客対応CRMとして、顧客情報、問い合わせ、通知、ナレッジを対象にする。顧客情報更新や外部送信は人間確認を前提にする。', ['営業担当', '管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '要約', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは削除を直接実行しない', 'AIは外部送信を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '外部送信'], ['登録', '更新', '通知']),
        'asset-equipment-management': preset(['備品貸出管理', '在庫管理', '監査ログ管理', '承認ワークフロー', '通知管理'], '資産管理担当, 利用部門担当', '資産管理担当、利用部門担当、申請者、承認者、管理者、AIアシスタント', 'Spring Security + セッション認証', '資産・備品管理として、備品貸出、在庫、承認、監査ログを対象にする。貸出承認、廃棄、棚卸更新は人間確認を前提にする。', ['申請者', '承認者', '管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない', 'AIは削除を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '承認', '却下'], ['登録', '更新', '承認', '却下', '通知']),
        'governance-audit-management': preset(['権限管理', '監査ログ管理', '承認ワークフロー', '契約管理', '通知管理'], '監査担当, セキュリティ担当', '監査担当、セキュリティ担当、承認者、管理者、AIアシスタント', '社内SSO', '統制・監査管理として、権限、監査ログ、承認ワークフロー、契約管理を対象にする。権限変更、承認、却下は人間確認と監査ログを前提にする。', ['承認者', '管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '権限変更', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない', 'AIは権限変更を直接実行しない'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知', '権限変更'], ['更新', '承認', '却下', '権限変更'], ['登録', '更新', '承認', '却下', '通知', '権限変更'])
    };

    function splitRestoredValues(value) {
        if (!value) {
            return [];
        }
        return String(value).split(/[,\n、，/／]+/).map(item => item.trim()).filter(item => item.length > 0);
    }

    function collectCurrentValuesForTarget(targetName) {
        const directField = document.getElementById(targetName);
        if (directField) {
            return splitRestoredValues(directField.value);
        }
        if (targetName === 'systemTypePresets') {
            return Array.isArray(restoredSystemTypes) ? restoredSystemTypes : [];
        }
        if (targetName === 'domainCatalog') {
            const values = [];
            const targetDomainField = document.getElementById('targetDomain');
            const primaryDomainField = document.getElementById('primaryDomain');
            if (targetDomainField) values.push(...splitRestoredValues(targetDomainField.value));
            if (primaryDomainField) values.push(...splitRestoredValues(primaryDomainField.value));
            if (Array.isArray(restoredRelatedDomains)) values.push(...restoredRelatedDomains);
            return values;
        }
        return [];
    }

    function normalizeRestoreText(value) {
        return String(value || '').replace(/[\s\-・、，,／/]+/g, '').trim();
    }

    function isRestoredCheckboxValueMatch(candidate, restoredValue) {
        const normalizedCandidate = normalizeRestoreText(candidate);
        const normalizedRestored = normalizeRestoreText(restoredValue);
        if (!normalizedCandidate || !normalizedRestored) {
            return false;
        }
        if (normalizedCandidate === normalizedRestored
                || normalizedRestored.includes(normalizedCandidate)
                || normalizedCandidate.includes(normalizedRestored)) {
            return true;
        }
        if (normalizedCandidate === '外部送信'
                && normalizedRestored.includes('外部')
                && normalizedRestored.includes('送信')) {
            return true;
        }
        if (normalizedCandidate === '詳細参照'
                && normalizedRestored.includes('詳細')
                && (normalizedRestored.includes('参照') || normalizedRestored.includes('取得'))) {
            return true;
        }
        if (normalizedCandidate === '詳細取得'
                && normalizedRestored.includes('詳細')
                && (normalizedRestored.includes('取得') || normalizedRestored.includes('参照'))) {
            return true;
        }
        if (normalizedCandidate === '下書き作成'
                && normalizedRestored.includes('下書き')
                && normalizedRestored.includes('作成')) {
            return true;
        }
        if (normalizedCandidate === '更新案の作成'
                && normalizedRestored.includes('更新')
                && normalizedRestored.includes('案')
                && normalizedRestored.includes('作成')) {
            return true;
        }
        return false;
    }

    function restoreCheckboxGroupFromCurrentValues(targetName) {
        const restoredValues = collectCurrentValuesForTarget(targetName);
        document.querySelectorAll(`input[type="checkbox"][data-target="${targetName}"]`).forEach(checkbox => {
            checkbox.checked = restoredValues.some(restoredValue => isRestoredCheckboxValueMatch(checkbox.value, restoredValue));
        });
    }

    function restoreCheckedItemsFromBlueprintInput() {
        [
            'systemTypePresets',
            'domainCatalog',
            'userTypes',
            'requiredOperations',
            'allowedAiOperations',
            'businessRequirements',
            'readOnlyOperations',
            'writeOperations',
            'approvalRequiredOperations',
            'auditLogRequiredOperations'
        ].forEach(restoreCheckboxGroupFromCurrentValues);
    }

    function preset(domains, customUserTypes, targetUsers, authenticationMethod, supplement, userTypes, requiredOperations, allowedAiOperations, businessRequirements, readOnlyOperations, writeOperations, approvalRequiredOperations, auditLogRequiredOperations) {
        return { domains, customUserTypes, targetUsers, authenticationMethod, supplement, userTypes, requiredOperations, allowedAiOperations, businessRequirements, readOnlyOperations, writeOperations, approvalRequiredOperations, auditLogRequiredOperations };
    }

    const firstVisitorSampleCatalog = {
        'order-inventory': sample('注文・在庫管理サンプル', ['sales-commerce'], ['注文管理', '在庫管理', '商品管理'], 'EC運営担当, 倉庫担当', '対象システム種別はEC / 販売管理。主ドメインは注文管理、関連ドメインは在庫管理 / 商品管理。注文ステータス更新、注文キャンセル、返金処理、外部通知送信は人間承認後に実行する。', 'Spring Security + セッション認証', 'EC運営担当、倉庫担当、管理者、AIアシスタント', ['管理者', 'AIアシスタント'], ['検索', '詳細取得', '更新', '承認', '要約', '通知'], ['検索', '詳細参照', '要約', '更新案の作成'], ['検索', '詳細取得', '要約'], ['更新', '通知'], ['更新', '通知'], ['更新', '通知', '承認'], ['AIは外部送信を直接実行しない'], ['注文検索', '注文詳細取得', '注文ステータス更新', '在庫確認', '商品情報参照', '出荷前チェック', '承認依頼'], ['注文検索', '注文詳細参照', '在庫確認', '商品情報参照', '出荷前チェック結果の要約', '注文変更案の作成'], ['注文ステータス更新', '在庫引当更新', '出荷通知送信'], ['注文ステータス更新', '注文キャンセル', '返金処理', '外部通知送信'], ['注文ステータス更新', '注文キャンセル', '返金処理', 'AIによる変更案作成', '承認依頼']),
        'internal-approval': sample('社内申請・承認ワークフローサンプル', ['internal-application-portal'], ['休暇申請', '経費精算', '承認ワークフロー', '通知管理', '監査ログ管理'], '申請部門担当, 監査担当', '社内申請ポータルとして、休暇申請と経費精算を主な申請対象にする。承認、却下、差戻し、代理承認者変更は人間確認後に実行し、申請状態変更とAI要約は監査ログを残す。', '社内SSO', '申請者、承認者、管理者、経理担当、監査担当、AIアシスタント', ['申請者', '承認者', '管理者', '経理担当', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '要約', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '承認', '却下', '通知'], ['登録', '更新', '承認', '却下', '通知'], ['AIは承認を直接実行しない', 'AIは却下を直接実行しない'], ['申請検索', '申請詳細取得', '申請作成', '申請更新', '承認依頼', '承認', '却下', '差戻し', '申請内容要約', '承認結果通知'], ['申請検索', '申請詳細参照', '申請内容要約', '申請不備チェック', '差戻し理由の下書き作成', '申請更新案の作成'], ['申請作成', '申請更新', '承認結果通知'], ['承認', '却下', '差戻し', '代理承認者変更'], ['申請作成', '申請更新', '承認', '却下', '差戻し', 'AIによる申請内容要約']),
        'support-inquiry': sample('問い合わせ・サポート管理サンプル', ['support-management', 'knowledge-platform'], ['問い合わせ管理', 'FAQ管理', 'ナレッジ検索・要約', '顧客管理', '通知管理'], 'サポート担当, 品質管理担当', '問い合わせ・サポート管理として、問い合わせ受付、分類、FAQ検索、AI要約、返信下書き作成を扱う。回答確定送信と重要問い合わせの状態変更は人間確認後に実行し、AI要約と返信下書き作成は監査ログを残す。', 'OAuth2 / OIDC', 'サポート担当、品質管理担当、管理者、AIアシスタント', ['管理者', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '要約', '通知'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知'], ['更新', '通知'], ['登録', '更新', '通知'], ['AIは外部送信を直接実行しない'], ['問い合わせ検索', '問い合わせ詳細取得', '問い合わせ受付登録', '問い合わせ分類', 'FAQ検索', 'ナレッジ参照', '問い合わせ要約', '返信下書き作成', '回答確定通知'], ['問い合わせ検索', '問い合わせ詳細参照', 'FAQ検索', 'ナレッジ参照', '問い合わせ要約', '返信下書き作成', '分類案の作成'], ['問い合わせ受付登録', '問い合わせ分類更新', '回答確定通知'], ['回答確定送信', '重要問い合わせの状態変更', '顧客情報更新'], ['問い合わせ分類更新', 'AIによる問い合わせ要約', '返信下書き作成', '回答確定送信']),
        'contract-billing': sample('契約・請求管理サンプル', ['contract-billing-management'], ['契約管理', '請求管理', '決済管理', '承認ワークフロー', '監査ログ管理'], '法務担当, 請求担当', '契約・請求管理として、契約更新、請求確定、入金状態確認、外部通知、業務リスク確認を扱う。契約条件変更、請求確定、督促通知、権限変更は人間承認後に実行し、契約・請求状態の変更は監査ログを残す。', 'OAuth2 / OIDC', '法務担当、請求担当、経理担当、承認者、管理者、AIアシスタント', ['承認者', '管理者', '経理担当', 'AIアシスタント'], ['検索', '一覧', '詳細取得', '登録', '更新', '承認', '却下', '要約', '通知', '権限変更'], ['検索', '一覧', '詳細参照', '要約', '下書き作成', '更新案の作成'], ['検索', '一覧', '詳細取得', '要約'], ['登録', '更新', '通知', '権限変更'], ['更新', '承認', '却下', '通知', '権限変更'], ['登録', '更新', '承認', '却下', '通知', '権限変更'], ['AIは承認を直接実行しない', 'AIは外部送信を直接実行しない', 'AIは権限変更を直接実行しない'], ['契約検索', '契約詳細取得', '請求一覧取得', '請求詳細取得', '契約更新案作成', '請求確定', '入金状態確認', '督促通知作成', '契約リスク要約', '承認依頼', '権限変更'], ['契約検索', '契約詳細参照', '請求情報参照', '入金状態確認', '契約リスク要約', '契約更新案の作成', '督促通知下書き作成'], ['契約更新', '請求確定', '督促通知送信', '権限変更'], ['契約条件変更', '請求確定', '督促通知送信', '権限変更'], ['契約更新案作成', '請求確定', '入金状態更新', '督促通知送信', '承認依頼', '権限変更'])
    };

    function sample(name, systemTypes, domains, customUserTypes, supplement, authenticationMethod, targetUsers, userTypes, requiredOperations, allowedAiOperations, readOnlyOperations, writeOperations, approvalRequiredOperations, auditLogRequiredOperations, businessRequirements, requiredOperationText, allowedAiOperationText, writeOperationText, approvalRequiredOperationText, auditLogRequiredOperationText) {
        return { name, systemTypes, domains, customUserTypes, supplement, authenticationMethod, targetUsers, userTypes, requiredOperations, allowedAiOperations, readOnlyOperations, writeOperations, approvalRequiredOperations, auditLogRequiredOperations, businessRequirements, requiredOperationText, allowedAiOperationText, writeOperationText, approvalRequiredOperationText, auditLogRequiredOperationText };
    }

    function syncSelectedDomainsToTarget() {
        document.getElementById('targetDomain').value = checkedValues('domainCatalog').join(' / ');
        syncInputModelFields();
    }

    function applySystemTypePresets() {
        const selectedPresets = checkedValues('systemTypePresets').map(key => systemTypePresets[key]).filter(Boolean);
        if (selectedPresets.length === 0) {
            return;
        }
        const domains = uniqueValues(selectedPresets.flatMap(preset => preset.domains));
        setChecked('domainCatalog', domains);
        syncSelectedDomainsToTarget();
        document.getElementById('customUserTypes').value = uniqueValues(selectedPresets.flatMap(preset => splitCustomValues(preset.customUserTypes))).join(', ');
        document.getElementById('structuredSupplement').value = uniqueValues(selectedPresets.map(preset => preset.supplement)).join('\n');
        const authenticationMethods = uniqueValues(selectedPresets.map(preset => preset.authenticationMethod).filter(Boolean));
        document.getElementById('authenticationMethod').value = authenticationMethods.length === 1 ? authenticationMethods[0] : '';
        document.getElementById('targetUsers').value = uniqueValues(selectedPresets.flatMap(preset => splitCustomValues(preset.targetUsers))).join('、');
        document.getElementById('outputLanguage').value = '日本語';
        setChecked('userTypes', uniqueValues(selectedPresets.flatMap(preset => preset.userTypes)));
        setChecked('requiredOperations', uniqueValues(selectedPresets.flatMap(preset => preset.requiredOperations)));
        setChecked('allowedAiOperations', uniqueValues(selectedPresets.flatMap(preset => preset.allowedAiOperations)));
        setChecked('businessRequirements', uniqueValues(selectedPresets.flatMap(preset => preset.businessRequirements)));
        setChecked('readOnlyOperations', uniqueValues(selectedPresets.flatMap(preset => preset.readOnlyOperations)));
        setChecked('writeOperations', uniqueValues(selectedPresets.flatMap(preset => preset.writeOperations)));
        setChecked('approvalRequiredOperations', uniqueValues(selectedPresets.flatMap(preset => preset.approvalRequiredOperations)));
        setChecked('auditLogRequiredOperations', uniqueValues(selectedPresets.flatMap(preset => preset.auditLogRequiredOperations)));
        syncStructuredInputs();
        syncInputModelFields();
    }

    function checkedValues(target) {
        return Array.from(document.querySelectorAll('input[data-target="' + target + '"]:checked')).map(input => input.value);
    }

    function setChecked(target, values) {
        document.querySelectorAll('input[data-target="' + target + '"]').forEach(input => { input.checked = values.includes(input.value); });
    }

    function setAllChecked(target, checked) {
        document.querySelectorAll('input[data-target="' + target + '"]').forEach(input => { input.checked = checked; });
    }

    function uniqueValues(values) {
        return Array.from(new Set(values.filter(value => value && value.trim().length > 0)));
    }

    function bulletList(values) {
        return values.filter(value => value && value.trim().length > 0).map(value => '- ' + value.trim()).join('\n');
    }

    function splitCustomValues(raw) {
        if (!raw) {
            return [];
        }
        return raw.split(/[、,\n]/).map(value => value.trim()).filter(value => value.length > 0);
    }

    function textareaValues(id) {
        return document.getElementById(id).value
            .split('\n')
            .map(value => value.trim().replace(/^-+\s*/, ''))
            .filter(value => value.length > 0);
    }

    function checkboxCandidateValues(target) {
        return Array.from(document.querySelectorAll('input[data-target="' + target + '"]')).map(input => input.value);
    }

    function syncOperationTextareaPreservingFreeForm(target) {
        const candidateValues = checkboxCandidateValues(target);
        const freeFormValues = textareaValues(target).filter(value => !candidateValues.includes(value));
        document.getElementById(target).value = bulletList(uniqueValues(checkedValues(target).concat(freeFormValues)));
    }

    function restoreGeneratedInputState() {
        setChecked('systemTypePresets', restoredSystemTypes);
        setChecked('domainCatalog', restoredRelatedDomains);
        setChecked('userTypes', textareaValues('userTypes'));
        setChecked('requiredOperations', textareaValues('requiredOperations'));
        setChecked('allowedAiOperations', textareaValues('allowedAiOperations'));
        setChecked('readOnlyOperations', textareaValues('readOnlyOperations'));
        setChecked('writeOperations', textareaValues('writeOperations'));
        setChecked('approvalRequiredOperations', textareaValues('approvalRequiredOperations'));
        setChecked('auditLogRequiredOperations', textareaValues('auditLogRequiredOperations'));
        const requirements = document.getElementById('businessRequirements').value;
        const aiForbiddenNotes = Array.from(document.querySelectorAll('input[data-target="businessRequirements"]'))
            .map(input => input.value)
            .filter(value => requirements.includes(value));
        setChecked('businessRequirements', aiForbiddenNotes);
        if (document.getElementById('structuredSupplement').value.trim().length === 0 && requirements.trim().length > 0) {
            document.getElementById('structuredSupplement').value = requirements.trim();
        }
        syncInputModelFields();
    }

    function syncStructuredInputs() {
        const userTypes = checkedValues('userTypes').concat(splitCustomValues(document.getElementById('customUserTypes').value));
        const aiForbiddenNotes = checkedValues('businessRequirements');
        const supplement = document.getElementById('structuredSupplement').value.trim();
        const requirements = [];
        if (sampleBusinessRequirements
                && sampleBusinessRequirements.trim().length > 0
                && document.getElementById('targetDomain').value === '注文管理 / 在庫管理 / 商品管理'
                && !supplement.includes(sampleBusinessRequirements.trim())) {
            requirements.push(sampleBusinessRequirements.trim());
        }
        if (supplement.length > 0) {
            requirements.push(supplement);
        }
        if (aiForbiddenNotes.length > 0) {
            const additionalNotes = aiForbiddenNotes.filter(note => !supplement.includes(note));
            if (additionalNotes.length > 0) {
                requirements.push(additionalNotes.join('\n'));
            }
        }
        if (requirements.length === 0) {
            requirements.push('選択された構造化要素に基づき、API&MCP設計候補を生成する。');
        }
        document.getElementById('businessRequirements').value = requirements.join('\n\n');
        document.getElementById('userTypes').value = bulletList(userTypes);
        syncOperationTextareaPreservingFreeForm('requiredOperations');
        syncOperationTextareaPreservingFreeForm('allowedAiOperations');
        syncOperationTextareaPreservingFreeForm('readOnlyOperations');
        syncOperationTextareaPreservingFreeForm('writeOperations');
        syncOperationTextareaPreservingFreeForm('approvalRequiredOperations');
        syncOperationTextareaPreservingFreeForm('auditLogRequiredOperations');
    }

    function syncInputModelFields() {
        const systemTypes = checkedValues('systemTypePresets');
        const relatedDomains = checkedValues('domainCatalog');
        const manualDomains = splitCustomValues(document.getElementById('targetDomain').value.replace(/\//g, ','));
        const primaryDomain = relatedDomains.length > 0 ? relatedDomains[0] : (manualDomains[0] || '');

        replaceHiddenList('systemTypesFieldContainer', 'systemTypes', systemTypes);
        replaceHiddenList('relatedDomainsFieldContainer', 'relatedDomains', relatedDomains);
        document.getElementById('primaryDomain').value = primaryDomain;
    }

    function replaceHiddenList(containerId, fieldName, values) {
        const container = document.getElementById(containerId);
        container.innerHTML = '';
        values.forEach(value => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = fieldName;
            input.value = value;
            container.appendChild(input);
        });
    }

    function fillSample(sampleKey = 'order-inventory') {
        const selectedSample = firstVisitorSampleCatalog[sampleKey] || firstVisitorSampleCatalog['order-inventory'];
        setChecked('systemTypePresets', selectedSample.systemTypes);
        setChecked('domainCatalog', selectedSample.domains);
        syncSelectedDomainsToTarget();
        document.getElementById('customUserTypes').value = selectedSample.customUserTypes;
        document.getElementById('structuredSupplement').value = selectedSample.supplement;
        document.getElementById('authenticationMethod').value = selectedSample.authenticationMethod;
        document.getElementById('targetUsers').value = selectedSample.targetUsers;
        document.getElementById('outputLanguage').value = '日本語';
        setChecked('userTypes', selectedSample.userTypes);
        setChecked('requiredOperations', selectedSample.requiredOperations);
        setChecked('allowedAiOperations', selectedSample.allowedAiOperations);
        setChecked('readOnlyOperations', selectedSample.readOnlyOperations);
        setChecked('writeOperations', selectedSample.writeOperations);
        setChecked('approvalRequiredOperations', selectedSample.approvalRequiredOperations);
        setChecked('auditLogRequiredOperations', selectedSample.auditLogRequiredOperations);
        setChecked('businessRequirements', selectedSample.businessRequirements);
        syncStructuredInputs();
        applySampleTextFields(selectedSample);
        syncInputModelFields();
        document.getElementById('sampleNextAction').textContent = selectedSample.name + 'を投入しました。内容を確認したら、画面下部の「設計候補を生成する」を押してください。';
    }

    function applySampleTextFields(selectedSample) {
        document.getElementById('requiredOperations').value = bulletList(selectedSample.requiredOperationText);
        document.getElementById('allowedAiOperations').value = bulletList(selectedSample.allowedAiOperationText);
        document.getElementById('writeOperations').value = bulletList(selectedSample.writeOperationText);
        document.getElementById('approvalRequiredOperations').value = bulletList(selectedSample.approvalRequiredOperationText);
        document.getElementById('auditLogRequiredOperations').value = bulletList(selectedSample.auditLogRequiredOperationText);
    }

    function clearSample() {
        setChecked('systemTypePresets', []);
        setChecked('domainCatalog', []);
        setChecked('userTypes', []);
        setChecked('requiredOperations', []);
        setChecked('allowedAiOperations', []);
        setChecked('readOnlyOperations', []);
        setChecked('writeOperations', []);
        setChecked('approvalRequiredOperations', []);
        setChecked('auditLogRequiredOperations', []);
        setChecked('businessRequirements', []);
        document.getElementById('targetDomain').value = '';
        document.getElementById('customUserTypes').value = '';
        document.getElementById('structuredSupplement').value = '';
        document.getElementById('authenticationMethod').value = '';
        document.getElementById('targetUsers').value = '';
        document.getElementById('outputLanguage').value = '日本語';
        document.getElementById('businessRequirements').value = '';
        document.getElementById('userTypes').value = '';
        document.getElementById('requiredOperations').value = '';
        document.getElementById('allowedAiOperations').value = '';
        document.getElementById('readOnlyOperations').value = '';
        document.getElementById('writeOperations').value = '';
        document.getElementById('approvalRequiredOperations').value = '';
        document.getElementById('auditLogRequiredOperations').value = '';
        syncInputModelFields();
        document.getElementById('sampleNextAction').textContent = '自由入力へ戻しました。対象ドメイン、利用者、操作、承認・監査条件を入力してから生成できます。';
    }

    document.getElementById('blueprintForm').addEventListener('submit', () => {
        syncStructuredInputs();
        syncInputModelFields();
    });
    restoreGeneratedInputState();
    restoreCheckedItemsFromBlueprintInput();

window.applySystemTypePresets = applySystemTypePresets;
window.syncSelectedDomainsToTarget = syncSelectedDomainsToTarget;
window.setAllChecked = setAllChecked;
window.fillSample = fillSample;
window.clearSample = clearSample;
}());
