package com.newsaggregator.application.dto;

import java.util.List;

public record AssignCategoriesCommand(
    List<String> categoryIds
) {}
