# CodexExec_TSK057_regression_evidence_guard

## 0. Codex execution settings

- Model: GPT-5.1 Codex Max or the highest available Codex coding model.
- Reasoning / intelligence: High.
- Execution mode: repository edit with tests.
- Repository: `yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring`
- Base branch: `main`
- Working branch: `feature/tsk-057-regression-evidence-guard`
- Pull request target: `main`

## 1. Task

Implement TSK-057: 回帰テスト証跡と禁止語ガードを導入する.

This task implements the Java test and verification portion of TSK-057. The TSK itself has already been created, and the investigation report has already been merged by PR #106. This task must complete the guard implementation without weakening the documented requirements.

## 2. Non-negotiable safety rule

Do not weaken, blur, rename away, or partially remove any requirement, forbidden phrase, negative expectation, test expectation, or operational rule in order to satisfy safety checks, compilation, or tests.

If an exact requested requirement cannot be implemented, report it as unimplemented. Do not silently replace it with a weaker expression.

## 3. Authoritative references

Read these files before editing:

- `PROJECT_START_PROMPT.md`
- `README.md`
- `docs/README.md`
- `docs/00_プロジェクト管理/02_改善タスク管理/未解決/TSK-057.md`
- `docs/00_プロジェクト管理/06_品質管理/TSK-057-回帰テスト証跡と禁止語ガード調査報告.md`
- `docs/00_プロジェクト管理/06_品質管理/RegressionEvidenceMatrix.md`
- `src/test/resources/regression/forbidden-output-phrases.txt`
- `src/main/java/com/example/apim/generator/ImplementationInstructionGenerator.java`
- existing generator tests under `src/test/java`

Also inspect existing generator, DTO, controller, and test classes as needed.

## 4. Context

APIM for Spring is a Spring Boot web MVP that generates API/MCP blueprint outputs from business requirements.

TSK-044 previously regressed because an old internal explanatory phrase was included in generated AI implementation instructions and was even asserted as a positive expected value in tests. PR #104 removed the phrase and changed existing checks to negative assertions. PR #105 resynchronized TSK-044 as resolved. PR #106 created TSK-057 and recorded the investigation.

TSK-057 must now add reusable guardrails so that the same type of regression is detected automatically.

## 5. Already prepared by ChatGPT before Codex implementation

The following files should already exist on the branch or must be created if missing:

- `docs/00_プロジェクト管理/06_品質管理/RegressionEvidenceMatrix.md`
- `src/test/resources/regression/forbidden-output-phrases.txt`

Do not delete these files. Do not empty or weaken them.

## 6. Required implementation scope

Implement the minimum necessary Java test changes so that the following are true.

### 6.1 Forbidden phrase resource is loaded by tests

Add a test helper or direct test logic that reads:

- `src/test/resources/regression/forbidden-output-phrases.txt`

Rules:

- Ignore blank lines.
- Do not require comments unless you add a comment convention deliberately.
- Fail if the loaded phrase list is empty.
- Preserve exact Japanese phrases.

### 6.2 Generated output cross-guard test

Add or update tests so that every phrase in `forbidden-output-phrases.txt` is absent from generated user-facing outputs.

At minimum, test these generated artifacts:

- blueprint markdown
- AI implementation instructions

If current application structure makes it practical, also test relevant screen/model output candidates. Do not perform a large UI rewrite for this.

Use realistic input that exercises the APIM generation flow. Existing test fixtures may be reused.

### 6.3 Important phrase meta-guard

Add a meta-test that verifies the forbidden phrase list still contains the most important phrase:

- `APIM for Spring本体の改修指示ではない`

The test must fail if this phrase is removed from the list.

### 6.4 Important guard test meta-guard

Add a meta-test that verifies an important guard test class exists.

Acceptable implementation examples:

- A test class such as `GeneratedOutputForbiddenPhraseGuardTest` exists and is loaded by the test suite.
- Or an equivalent class name is used, as long as the meta-test explicitly verifies the class exists.

The goal is to detect accidental deletion of the guard test class.

### 6.5 Regression evidence meta-guard

Add a meta-test that reads:

- `docs/00_プロジェクト管理/06_品質管理/RegressionEvidenceMatrix.md`

and verifies at minimum:

- the file exists;
- it contains `TSK-044`;
- it contains `ImplementationInstructionGeneratorTest`;
- it contains `BlueprintGenerationRegressionTest`;
- it contains `APIM for Spring本体の改修指示ではない`.

### 6.6 Preserve existing TSK-044 negative checks

Do not remove existing TSK-044 negative checks. Existing tests that assert the old phrase is not contained must remain or be strengthened.

## 7. Do not implement

Do not implement any of the following:

- TSK-044 business logic changes beyond what is necessary for tests.
- Render service creation.
- Render public URL finalization.
- External LLM API integration.
- DB persistence.
- Full authentication or authorization implementation.
- Fully working MCP server implementation.
- Generated target application code generation.
- Large UI redesign.
- Migration of TSK-057 from unresolved to resolved.
- Improvement task list status synchronization after implementation.
- Merge, branch deletion, CI polling, or local sync after PR creation.

## 8. Suggested implementation direction

Prefer adding focused tests rather than changing production code.

Potential test structure:

- `GeneratedOutputForbiddenPhraseGuardTest`
  - loads forbidden phrases;
  - generates blueprint markdown and implementation instructions;
  - asserts none of the forbidden phrases are present.
- `RegressionGuardMetaTest`
  - checks phrase list is non-empty;
  - checks the important phrase remains in the list;
  - checks the guard test class exists;
  - checks `RegressionEvidenceMatrix.md` contains TSK-044 evidence.

Use the existing service/generator classes in the simplest available way. If direct generator invocation is easier and already used by existing tests, follow existing test style.

## 9. Test requirements

Run the full test suite before reporting completion.

On Windows:

    ./mvnw.cmd test

On Linux/macOS/WSL:

    ./mvnw test

Report the exact command and result.

## 10. Pull request requirements

Create a PR from `feature/tsk-057-regression-evidence-guard` into `main`.

PR title:

TSK-057: 回帰テスト証跡と禁止語ガードを実装する

PR body must include:

- What was implemented for TSK-057.
- What was not implemented for TSK-057.
- Added or updated test classes.
- How the forbidden phrase list is loaded and guarded.
- How generated outputs are cross-checked.
- How the meta-guards protect against test weakening.
- Test command and result.
- Any limitations or follow-up candidates.

## 11. Completion report

After implementation, post a sanitized summary in the PR or return the following information:

- PR number and URL.
- Changed files.
- Main implementation points.
- Tests run and result.
- Any limitations or follow-up candidates.

Do not include secrets, tokens, `.env` contents, private keys, full environment variables, personal information, or excessive local absolute paths in PR comments or result files.
