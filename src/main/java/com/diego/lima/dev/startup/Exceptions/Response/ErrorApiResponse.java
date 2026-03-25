package com.diego.lima.dev.startup.Exceptions.Response;

import java.time.LocalDateTime;

public record ErrorApiResponse(
        String error,
        int status,
        String path,
        LocalDateTime timestamp
) {
}
