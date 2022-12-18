package com.tufimtseva.currencyparser.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CurrencyResponse {

        private Finance finance;

        @Data
        @NoArgsConstructor
        public static class Finance {
            private List<Result> result;
        }

        @Data
        @NoArgsConstructor
        public static class Result {
            private int start;
            private int count;
            private int total;
            private List<Quote> quotes;
        }

        @Data
        @NoArgsConstructor
        public static class Quote {
            private String symbol;
        }

    }
