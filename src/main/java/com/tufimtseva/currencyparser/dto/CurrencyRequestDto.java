package com.tufimtseva.currencyparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CurrencyRequestDto {
    private int offset;
    private Query query = new Query();
    private String quoteType;
    private int size = 100;
    private String sortField;
    private String sortType = "asc";
    private String userId = "";
    private String userIdType = "guid";

    @Data
    public static class Query {
        private String operator = "and";
        private List<Operand> operands = List.of(
                new Operand("eq", List.of("currency", "USD")),
                new Operand("eq", List.of("exchange", "CCC"))
        );

    }

    @Data
    @AllArgsConstructor
    public static class Operand {
        private String operator;
        private List<String> operands = new ArrayList<>();
    }
}