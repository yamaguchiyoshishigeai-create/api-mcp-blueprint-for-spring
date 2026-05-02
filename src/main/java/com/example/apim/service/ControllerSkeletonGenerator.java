package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.ControllerSkeleton;
import com.example.apim.support.NamingSupport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ControllerSkeletonGenerator {

    private final NamingSupport namingSupport;

    public ControllerSkeletonGenerator(NamingSupport namingSupport) {
        this.namingSupport = namingSupport;
    }

    public ControllerSkeleton generate(String domainClass, String domainPath, List<ApiEndpointCandidate> endpoints) {
        String controllerName = domainClass + "Controller";
        StringBuilder sb = new StringBuilder();
        sb.append("package com.example.generated.controller;\n\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"/api/").append(domainPath).append("\")\n");
        sb.append("public class ").append(controllerName).append(" {\n\n");
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
        return new ControllerSkeleton(controllerName, sb.toString());
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
        String prefix = "/api/" + domainPath;
        if (fullPath.startsWith(prefix)) {
            String local = fullPath.substring(prefix.length());
            return local.isBlank() ? "" : local;
        }
        return fullPath;
    }
}
