package com.example.apim.controller;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.service.BlueprintGenerationService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"blueprintResult", "blueprintInput"})
public class BlueprintController {

    private static final MediaType TEXT_MARKDOWN_UTF8 = new MediaType("text", "markdown", StandardCharsets.UTF_8);

    private final BlueprintGenerationService generationService;

    public BlueprintController(BlueprintGenerationService generationService) {
        this.generationService = generationService;
    }

    @ModelAttribute("blueprintInput")
    public BlueprintInput blueprintInput() {
        return new BlueprintInput();
    }

    @ModelAttribute("blueprintResult")
    public BlueprintResult blueprintResult() {
        return new BlueprintResult();
    }

    @GetMapping("/")
    public String showForm(Model model) {
        return showInputForm(model);
    }

    @GetMapping("/blueprint/edit")
    public String editForm(Model model) {
        return showInputForm(model);
    }

    private String showInputForm(Model model) {
        model.addAttribute("sampleBusinessRequirements", sampleBusinessRequirements());
        return "index";
    }

    @PostMapping("/blueprint/generate")
    public String generate(@Valid @ModelAttribute("blueprintInput") BlueprintInput input,
                           BindingResult bindingResult,
                           Model model) {
        model.addAttribute("sampleBusinessRequirements", sampleBusinessRequirements());
        if (bindingResult.hasErrors()) {
            return "index";
        }
        BlueprintResult result = generationService.generate(input);
        model.addAttribute("blueprintResult", result);
        return "result";
    }

    @GetMapping("/blueprint/preview")
    public String previewBlueprint(@ModelAttribute("blueprintResult") BlueprintResult result) {
        if (result.getBlueprintMarkdown() == null || result.getBlueprintMarkdown().isBlank()) {
            return "redirect:/";
        }
        return "blueprint-preview";
    }

    @GetMapping("/blueprint/download")
    public ResponseEntity<String> downloadBlueprint(@ModelAttribute("blueprintResult") BlueprintResult result) {
        if (result.getBlueprintMarkdown() == null || result.getBlueprintMarkdown().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return markdownDownload("api-mcp-blueprint.md", result.getBlueprintMarkdown());
    }

    @GetMapping("/blueprint/implementation-instructions")
    public String previewImplementationInstructions(@ModelAttribute("blueprintResult") BlueprintResult result) {
        if (result.getImplementationInstructions() == null || result.getImplementationInstructions().isBlank()) {
            return "redirect:/";
        }
        return "implementation-instructions-preview";
    }

    @GetMapping("/blueprint/implementation-instructions/download")
    public ResponseEntity<String> downloadImplementationInstructions(
            @ModelAttribute("blueprintResult") BlueprintResult result) {
        if (result.getImplementationInstructions() == null || result.getImplementationInstructions().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return markdownDownload("implementation-instructions.md", result.getImplementationInstructions());
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }

    private ResponseEntity<String> markdownDownload(String filename, String body) {
        return ResponseEntity.ok()
                .contentType(TEXT_MARKDOWN_UTF8)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(body);
    }

    private String sampleBusinessRequirements() {
        return """
                EC / 販売管理として、注文管理を主ドメインにし、在庫管理と商品管理を関連ドメインにする。
                EC運営担当、倉庫担当、管理者、AIアシスタントが、注文検索、注文詳細取得、注文ステータス更新、在庫確認、商品情報参照、出荷前チェック、承認依頼を行う。
                AIアシスタントには注文検索、注文詳細参照、在庫確認、商品情報参照、出荷前チェック結果の要約、注文変更案の作成を許可する。
                注文ステータス更新、注文キャンセル、返金処理、外部通知送信は人間承認後に実行し、注文ステータス更新、注文キャンセル、返金処理、AIによる変更案作成、承認依頼は監査ログを残す。
                """;
    }
}
