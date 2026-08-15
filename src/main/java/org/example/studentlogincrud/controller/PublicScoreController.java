package org.example.studentlogincrud.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import org.example.studentlogincrud.dto.PublicScoreResponse;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.OjCrawlerService;
import org.example.studentlogincrud.service.StudentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/public/scores")
public class PublicScoreController {
    private final StudentService studentService;
    private final String publicBaseUrl;
    private final OjCrawlerService ojCrawlerService;

    public PublicScoreController(
            StudentService studentService,
            @Value("${app.public-base-url:}") String publicBaseUrl,
            OjCrawlerService ojCrawlerService
    ) {
        this.studentService = studentService;
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        this.ojCrawlerService = ojCrawlerService;
    }

    @GetMapping("/{publicId}")
    public Result<PublicScoreResponse> query(@PathVariable String publicId) {
        Result<PublicScoreResponse> result = studentService.queryPublicScore(publicId);
        // 爬虫启用时：扫码实时爬取 OJ 分数回显；爬取失败则保留数据库分数
        if (result.getCode() != null && result.getCode() == 200
                && result.getData() != null && ojCrawlerService.isEnabled()) {
            try {
                BigDecimal liveScore = ojCrawlerService.fetchScore(result.getData().getStudentNo());
                if (liveScore != null) {
                    result.getData().setScore(liveScore);
                }
            } catch (Exception e) {
                // 爬取失败不阻断查分，回退数据库分数
            }
        }
        return result;
    }

    @GetMapping(value = "/{publicId}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrcode(@PathVariable String publicId, HttpServletRequest request) {
        Result<PublicScoreResponse> scoreResult = studentService.queryPublicScore(publicId);
        if (scoreResult.getCode() == null || scoreResult.getCode() != 200) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            String queryUrl = buildPublicScoreUrl(publicId, request);
            BitMatrix matrix = new QRCodeWriter().encode(
                    queryUrl,
                    BarcodeFormat.QR_CODE,
                    360,
                    360,
                    Map.of(EncodeHintType.MARGIN, 1)
            );
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.noStore())
                    .body(output.toByteArray());
        } catch (WriterException | IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String buildPublicScoreUrl(String publicId, HttpServletRequest request) {
        String baseUrl = publicBaseUrl.isBlank()
                ? request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
                : publicBaseUrl.replaceAll("/$", "");
        return baseUrl + "/score.html?publicId=" + publicId;
    }
}
