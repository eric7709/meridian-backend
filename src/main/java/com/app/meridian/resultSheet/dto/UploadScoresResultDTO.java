package com.app.meridian.resultSheet.dto;

import java.util.List;

public record UploadScoresResultDTO(int saved, List<String> errors) {
}
