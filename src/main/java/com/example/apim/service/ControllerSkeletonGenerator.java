package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.ControllerSkeleton;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ControllerSkeletonGenerator {

    private static final String PRIMARY_DOMAIN_API = "主ドメインAPI";

    private final NamingSupport namingSupport;

    public ControllerSkeletonGenerator(NamingSupport namingSupport) {
        this.namingSupport = namingSupport;
    }

    public ControllerSkeleton generate(String domainClass, String domainPath, List<ApiEndpointCandidate> endpoints) {
        String controllerName = domainClass + "Controller";
        return new ControllerSkeleton(controllerName, buildControllerSource(controllerName, domainPath, endpoints, List.of()));
    }

    public ControllerSkeleton generateAggregate(String controllerName, List<ApiEndpointCandidate> endpoints) {
        String safeControllerName = controllerName == null || controllerName.isBlank()
                ? "BusinessObjectApiController"
                : controllerName;
        return new ControllerSkeleton(safeControllerName,
                buildControllerSource(safeControllerName, "", endpoints == null ? List.of() : endpoints, List.of()));
    }

    public ControllerSkeleton generate(
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer domainNameNormalizer,
            List<ApiEndpointCandidate> endpoints
    ) {
        String primaryDomain = primaryDomainText(normalizedInput);
        String primaryDomainPath = domainNameNormalizer.normalizeUrlSegment(primaryDomain);
        String primaryDomainClass = domainNameNormalizer.normalizeClassName(primaryDomain);
        String primaryControllerName = primaryDomainClass + "Controller";
        List<String> relatedControllerCandidates = relatedControllerCandidates(normalizedInput, domainNameNormalizer);
        String sourceCode = buildControllerSource(primaryControllerName, primaryDomainPath,
                endpointsByDomain(endpoints, PRIMARY_DOMAIN_API, primaryDomain), relatedControllerCandidates);
        return new ControllerSkeleton(primaryControllerName, sourceCode);
    }

    private String buildControllerSource(
            String controllerName,
            String domainPath,
            List<ApiEndpointCandidate> endpoints,
            List<String> relatedControllerCandidates
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.example.generated.controller;\n\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"/api");
        if (domainPath != null && !domainPath.isBlank()) {
            sb.append("/").append(domainPath);
        }
        sb.append("\")\n");
        sb.append("public class ").append(controllerName).append(" {\n\n");
        for (String relatedControllerCandidate : relatedControllerCandidates) {
            sb.append("    // 関連ドメイン参照Controller候補: ")
                    .append(relatedControllerCandidate)
                    .append("\n");
        }
        if (!relatedControllerCandidates.isEmpty()) {
            sb.append("\n");
        }
        for (ApiEndpointCandidate endpoint : endpoints) {
            String methodName = toMethodName(endpoint);
            sb.append("    @").append(annotation(endpoint.httpMethod())).append("(\"")
                    .append(resolveLocalPath(endpoint.path(), domainPath)).append("\")\n");
            sb.append("    public Object ").append(methodName).append("() {\n");
            sb.append("        // 認可チェック: role-based access control\n");
            sb.append("        // 承認チェック: ").append(endpoint.approvalRequired()).append("\n");
            sb.append("        // 監査ログ: ").append(endpoint.auditLogRequired()).append("\n");
            sb.append("        // TODO: Service呼び出しを実装\n");
            sb.append("        return null;\n");
            sb.append("    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private List<String> relatedControllerCandidates(
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer domainNameNormalizer
    ) {
        List<String> candidates = new ArrayList<>();
        for (String relatedDomain : normalizedInput.relatedDomains()) {
            String relatedDomainPath = domainNameNormalizer.normalizeUrlSegment(relatedDomain);
            String relatedDomainClass = domainNameNormalizer.normalizeClassName(relatedDomain);
            candidates.add(relatedDomainClass + "ReferenceController (/api/" + relatedDomainPath + ")");
        }
        return candidates;
    }

    private List<ApiEndpointCandidate> endpointsByDomain(List<ApiEndpointCandidate> endpoints, String domainRole, String domainName) {
        List<ApiEndpointCandidate> filtered = new ArrayList<>();
        for (ApiEndpointCandidate endpoint : endpoints) {
            if (domainRole.equals(endpoint.domainRole()) && domainName.equals(endpoint.domainName())) {
                filtered.add(endpoint);
            }
        }
        return filtered;
    }

    private String toMethodName(ApiEndpointCandidate endpoint) {
        String raw = endpoint.httpMethod().toLowerCase() + " " + endpoint.path().replace("/", " ");
        raw = raw.replace("{", " by ").replace("}", "");
        return namingSupport.toCamelCase(raw);
    }

    private String annotation(String httpMethod) {
        return switch (httpMethod) {
            case "GET" -> "GetMapping";
            case "POST" -> "PostMapping";
            case "PUT" -> "PutMapping";
            case "PATCH" -> "PatchMapping";
            case "DELETE" -> "DeleteMapping";
            default -> "RequestMapping";
        };
    }

    private String resolveLocalPath(String fullPath, String domainPath) {
        if (domainPath == null || domainPath.isBlank()) {
            if (fullPath == null || fullPath.isBlank()) {
                return "";
            }
            return fullPath.startsWith("/api") ? fullPath.substring(4) : fullPath;
        }
        String prefix = "/api/" + domainPath;
        if (fullPath.startsWith(prefix)) {
            String local = fullPath.substring(prefix.length());
            return local.isBlank() ? "" : local;
        }
        return fullPath;
    }

    private String primaryDomainText(NormalizedBlueprintInput normalizedInput) {
        if (normalizedInput.primaryDomain() != null && !normalizedInput.primaryDomain().isBlank()) {
            return normalizedInput.primaryDomain();
        }
        return normalizedInput.targetDomainText();
    }
}
