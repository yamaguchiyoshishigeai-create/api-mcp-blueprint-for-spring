# CodexExec_TSK046_multi_sample_entry_route

## 1. Task

Implement TSK-046: 初回訪問者向けサンプル業務パターンを複数化する.

Proposal name: Multi-Sample-Entry-Route
Repository: yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring
Working branch: feature/tsk-046-multi-sample-entry-route
Base branch: main

## 2. Context

APIM for Spring is a Spring Boot web MVP that generates REST API design candidates, MCP design candidates, API/MCP mapping, Markdown blueprint output, and AI implementation instructions from business requirements.

This task must preserve the product positioning as a design-generation tool. Do not turn it into a diagnostic tool, scoring tool, full code generator, full MCP server, or LLM integration product.

TSK-045 is already completed and merged. Its preview download route and input state restoration route must not be reimplemented or regressed.

## 3. Authoritative references

Read these files before editing:

- PROJECT_START_PROMPT.md
- README.md
- docs/README.md
- docs/00_プロジェクト管理/02_改善タスク管理/未解決/TSK-046.md
- docs/00_プロジェクト管理/03_実装支援AI指示/TSK-046-最小実装指示.md
- docs/00_プロジェクト管理/05_横断運用規程/Codex投入前ハンドオフゲート方針.md
- src/main/java/com/example/apim/controller/BlueprintController.java
- src/main/resources/templates/index.html
- src/main/resources/templates/result.html
- src/test/java/com/example/apim/controller/BlueprintControllerTest.java

Also inspect related generator and model classes if needed to ensure selected samples produce different generated REST API candidates, MCP candidates, and AI implementation instruction content.

## 4. Objective

Expand the first-visitor sample entry route from one order-management sample to 3 or 4 business sample patterns.

The user should be able to choose a sample close to their own business context and immediately understand that APIM for Spring supports multiple business domains, not only order management.

## 5. Required sample patterns

Use these four sample patterns unless the existing code structure strongly favors three:

1. Order and inventory management sample
   - EC, sales management, inventory allocation, shipment management.
   - Preserve or naturally extend the existing order-management sample.

2. Internal request and approval workflow sample
   - Approval, human confirmation, audit logging, rejection or remand, workflow status.

3. Inquiry and support management sample
   - FAQ search, inquiry classification, AI summarization, draft reply creation.

4. Contract and billing management sample
   - Authorization, approval, external notification, invoice/payment status, audit logging, business-risk management.

## 6. Required implementation scope

Implement the minimum necessary changes so that:

- The input page shows 3 or 4 sample business patterns.
- Each sample has a short explanation of the assumed business context.
- Selecting a sample sets appropriate values for system type, domains, required operations, AI-permitted operations, approval/audit-related operations, free text fields, and output language where applicable.
- The wording helps first-time visitors choose an entry point close to their own business.
- Generated results show differences based on the selected sample through REST API candidates, MCP tools/resources/prompts, API/MCP mapping, and AI implementation instructions.
- The existing free-input flow remains intact.
- The existing order-management sample route remains available and compatible in intent.
- TSK-045 edit/preview/download/input-state-restoration behavior is not broken.

Prefer minimal, localized implementation. Avoid large UI rewrites.

## 7. Do not implement

Do not implement any of the following:

- Preview download route from TSK-045.
- Input state restoration route from TSK-045.
- Render deployment work.
- External LLM API integration.
- Database persistence.
- Full authentication or authorization implementation.
- Fully working MCP server implementation.
- Full generated business application implementation.
- Large-scale UI redesign unrelated to sample selection.
- Task management cleanup after implementation.
- Merge, branch deletion, or CI monitoring after PR creation.

## 8. Suggested implementation direction

Use the existing structure first. Inspect current sample insertion logic in index.html and BlueprintController-related tests.

Recommended approach:

- Keep Java model/controller changes minimal unless needed.
- If the sample insertion is currently JavaScript-driven in index.html, extend it in the same style with a maintainable sample catalog.
- Ensure checked checkbox values, hidden list values, textarea/free-input values, and output language restoration remain compatible with TSK-045 behavior.
- Keep explanations concise and visitor-facing.
- Ensure custom/free-text operations not represented by fixed checkboxes are still sent in a way compatible with existing form handling.

## 9. Test requirements

Update or add minimum necessary tests, especially BlueprintControllerTest or an equivalent MVC/template test, to verify:

- The input page displays 3 or 4 sample patterns.
- Each sample explanation is visible.
- Existing order/inventory sample wording remains present.
- Free-input POST flow still works.
- Generated result can be produced from at least one newly added non-order sample.
- The generated result includes domain/operation differences from the selected new sample.
- Existing edit/preview/download-related routes are not regressed if current tests cover them.

Run the full test suite before reporting completion:

    ./mvnw.cmd test

If running on Linux/macOS/WSL, use:

    ./mvnw test

## 10. Pull request requirements

Create a PR from feature/tsk-046-multi-sample-entry-route into main.

The PR title should be:

TSK-046: 初回訪問者向けサンプル業務パターンを複数化する

The PR body must include:

- What was implemented for TSK-046.
- What was not implemented for TSK-046.
- The added sample business patterns.
- Summary of input values configured by each sample.
- How sample differences appear in generated results.
- Updated tests.
- Test command and result.

## 11. Completion report

After implementation, post a sanitized summary in the PR or return the following information to the user:

- PR number and URL.
- Changed files.
- Main implementation points.
- Tests run and result.
- Any limitations or follow-up candidates.

Do not include secrets, tokens, .env contents, private keys, full environment variables, personal information, or excessive local absolute paths in comments or result files.
