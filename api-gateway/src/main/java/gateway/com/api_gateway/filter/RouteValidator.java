package gateway.com.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> openEndPoints=new ArrayList<>(Arrays.asList("/api/users/register",
            "/api/users/login",
            "/eureka"));
    // Yeh ek "Checker" (Predicate) hai. Yeh True/False return karta hai.
    public Predicate<ServerHttpRequest> isSecured= request->openEndPoints.stream().
            noneMatch(uri->request.getURI().getPath().contains(uri));
}
