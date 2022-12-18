package com.tufimtseva.currencyparser;

import com.tufimtseva.currencyparser.dto.CurrencyRequestDto;
import com.tufimtseva.currencyparser.dto.CurrencyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CurrencyParser implements CommandLineRunner {

    @Value("${crypto.outputFolder}")
    private String outputFolder;
    @Value("${crypto.outputFileName}")
    private String outputFileName;
    @Value("${crypto.historicalDepthDays}")
    private int historicalDepthDays;
    @Value("${crypto.downloadDelaySec}")
    private int downloadDelaySec;
    @Value("${crypto.allCurrencies}")
    private String allCurrencies;
    @Value("${crypto.history}")
    private String history;
    @Value("${crypto.cookie}")
    private String cookie;
    @Value("${crypto.quoteType}")
    private String quoteType;
    @Value("${crypto.sortField}")
    private String sortField;
    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();



        int pageSize = 100;
        int offset = 0;
        int total = -1;

        List<String> currencyCodes = new ArrayList<>();
        while (total == -1 || currencyCodes.size() < total) {
            CurrencyRequestDto requestDto = new CurrencyRequestDto();
            requestDto.setOffset(offset);
            requestDto.setSize(pageSize);
            requestDto.setSortField(sortField);
            requestDto.setQuoteType(quoteType);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("authority", "authority");
            headers.add("content-type", "application/json");
            headers.add("cookie", cookie);
            HttpEntity<CurrencyRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
            ResponseEntity<CurrencyResponse> response = restTemplate.exchange(
                    allCurrencies,
                    HttpMethod.POST,
                    requestEntity,
                    CurrencyResponse.class
            );
            CurrencyResponse responseDto = response.getBody();
            if (total == -1) {
                total = responseDto.getFinance().getResult().get(0).getTotal();
                log.info("total number of currencies {}", total);
            }
            List<CurrencyResponse.Quote> quotes = responseDto.getFinance().getResult().get(0).getQuotes();
            for (CurrencyResponse.Quote quote : quotes) {
                currencyCodes.add(quote.getSymbol());
            }
            log.info("loaded {} currencies of {} total", currencyCodes.size(), total);
            offset += pageSize;
        }

        File outFolder = new File(outputFolder + "/");
        outFolder.mkdirs();
        File outFile = new File(outFolder, outputFileName);
        try (PrintWriter pw = new PrintWriter(outFile)) {
            currencyCodes.forEach(pw::println);
        }
        log.info("saved the list of currencies to {}", outFile.getAbsolutePath());

        Instant now = Instant.now();
        long nowMS = now.getEpochSecond();
        Instant historyStart = now.minus(historicalDepthDays, ChronoUnit.DAYS);
        long historyStartMS = historyStart.getEpochSecond();
        log.info("loading history from {} to {}", historyStart, now);

        File historyOutFolder = new File(outFolder, "history");
        historyOutFolder.mkdirs();

        int successCount = 0;
        int failCount = 0;

        for (String currencyCode : currencyCodes) {
            File historyFile = new File(historyOutFolder, currencyCode + ".csv");

            String downloadUrl = history + currencyCode + "?period1=" + historyStartMS + "&period2=" + nowMS + "&interval=1d&events=history&includeAdjustedClose=true";
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("authority", "authority");
            connection.addRequestProperty("content-type", "application/json");
            connection.addRequestProperty("cookie", cookie);
            String line;
            try {
                try (
                        InputStream is = connection.getInputStream();
                        Reader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        PrintWriter historyFilePW = new PrintWriter(historyFile)
                ) {
                    while ((line = br.readLine()) != null) {
                        historyFilePW.println(line);
                    }
                }
                log.info("wrote history for {} ({} of {}) to {}", currencyCode, successCount, currencyCodes.size(), historyFile.getAbsolutePath());
                successCount ++;
                TimeUnit.SECONDS.sleep(downloadDelaySec);
            } catch (IOException e) {
                log.warn("failed to download history for {} from {}, skipped", currencyCode, downloadUrl, e);
                failCount ++;
            }
        }
        if (failCount > 0) {
            log.warn("{} downloads have failed, {} succeeded", failCount, successCount);
        }
    }
}

