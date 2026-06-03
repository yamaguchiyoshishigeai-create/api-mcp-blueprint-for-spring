package com.example.apim.controller;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.ExternalAiBridgeRequest;
import com.example.apim.model.ExternalAiImportResult;
import com.example.apim.service.ExternalAiPromptBridgeService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@SessionAttributes({"blueprintInput", "externalAiPrompt", "importResult"})
public class ExternalAiBridgeController {

    private static final int MAX_JSON_UPLOAD_BYTES = 64 * 1024;
    private static final MediaType TEXT_MARKDOWN_UTF8 = new MediaType("text", "markdown", StandardCharsets.UTF_8);

    private final ExternalAiPromptBridgeService bridgeService;

    public ExternalAiBridgeController(ExternalAiPromptBridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @ModelAttribute("blueprintInput")
    public BlueprintInput blueprintInput() {
        return new BlueprintInput();
    }

    @ModelAttribute("externalAiPrompt")
    public String externalAiPrompt() {
        return "";
    }

    @ModelAttribute("importResult")
    public ExternalAiImportResult importResult() {
        return ExternalAiImportResult.invalid(List.of(), List.of());
    }

    @GetMapping("/external-ai-bridge")
    public String showBridge(Model model) {
        addBridgeRequestIfAbsent(model);
        return "external-ai-bridge";
    }

    @PostMapping("/external-ai-bridge/prompt")
    public String generatePrompt(@Valid @ModelAttribute("externalAiBridgeRequest") ExternalAiBridgeRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "external-ai-bridge";
        }
        String prompt = bridgeService.generatePrompt(request.getFreeText());
        model.addAttribute("externalAiPrompt", prompt);
        return "redirect:/external-ai-bridge/prompt";
    }

    @GetMapping("/external-ai-bridge/prompt")
    public String showPrompt(@ModelAttribute("externalAiPrompt") String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "redirect:/external-ai-bridge";
        }
        return "external-ai-prompt";
    }

    @GetMapping("/external-ai-bridge/prompt/download")
    public ResponseEntity<String> downloadPrompt(@ModelAttribute("externalAiPrompt") String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(TEXT_MARKDOWN_UTF8)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("apim-external-ai-prompt.md", StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(prompt);
    }

    @PostMapping("/external-ai-bridge/import-text")
    public String importText(@RequestParam(name = "jsonText", required = false) String jsonText,
                             Model model) {
        storeImportResult(bridgeService.importJson(jsonText), model);
        return "redirect:/external-ai-bridge/import-result";
    }

    @PostMapping("/external-ai-bridge/import-file")
    public String importFile(@RequestParam("jsonFile") MultipartFile jsonFile,
                             Model model) throws IOException {
        if (jsonFile == null || jsonFile.isEmpty()) {
            storeImportResult(ExternalAiImportResult.invalid(List.of("JSONファイルを選択してください。"), List.of()),
                    model);
            return "redirect:/external-ai-bridge/import-result";
        }
        String filename = jsonFile.getOriginalFilename() == null ? "" : jsonFile.getOriginalFilename();
        if (!filename.toLowerCase().endsWith(".json")) {
            storeImportResult(ExternalAiImportResult.invalid(List.of(".json ファイルのみアップロードできます。"),
                    List.of()), model);
            return "redirect:/external-ai-bridge/import-result";
        }
        if (jsonFile.getSize() > MAX_JSON_UPLOAD_BYTES) {
            storeImportResult(ExternalAiImportResult.invalid(
                    List.of("JSONファイルサイズは64KB以下にしてください。"), List.of()), model);
            return "redirect:/external-ai-bridge/import-result";
        }
        String json = new String(jsonFile.getBytes(), StandardCharsets.UTF_8);
        storeImportResult(bridgeService.importJson(json), model);
        return "redirect:/external-ai-bridge/import-result";
    }

    @GetMapping("/external-ai-bridge/import-result")
    public String showImportResult(@ModelAttribute("importResult") ExternalAiImportResult result) {
        if (isMissingImportResult(result)) {
            return "redirect:/external-ai-bridge/prompt";
        }
        return "external-ai-import-result";
    }

    private void storeImportResult(ExternalAiImportResult result, Model model) {
        model.addAttribute("importResult", result);
        if (result.valid() && result.canGenerate() && result.blueprintInput() != null) {
            model.addAttribute("blueprintInput", result.blueprintInput());
        }
    }

    private boolean isMissingImportResult(ExternalAiImportResult result) {
        return result == null
                || (!result.valid()
                && result.errors().isEmpty()
                && result.warnings().isEmpty()
                && (result.reason() == null || result.reason().isBlank()));
    }

    private void addBridgeRequestIfAbsent(Model model) {
        if (!model.containsAttribute("externalAiBridgeRequest")) {
            model.addAttribute("externalAiBridgeRequest", new ExternalAiBridgeRequest());
        }
    }
}
