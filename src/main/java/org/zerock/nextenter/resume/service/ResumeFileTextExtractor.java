package org.zerock.nextenter.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


/**
 * 이력서 파일(PDF, DOCX)에서 텍스트를 추출합니다.
 */
@Component
@Slf4j
public class ResumeFileTextExtractor {

    /**
     * PDF 파일에서 텍스트 추출.
     */
    public String extractFromPdf(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            log.warn("PDF 텍스트 추출 실패: {}", e.getMessage());
            return "";
        }
    }

    /**
     * DOCX 파일에서 텍스트 추출.
     */
    public String extractFromDocx(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph para : document.getParagraphs()) {
                String paraText = para.getText();
                if (paraText != null && !paraText.isBlank()) {
                    text.append(paraText).append("\n");
                }
            }
            return text.toString().trim();
        } catch (Exception e) {
            log.warn("DOCX 텍스트 추출 실패: {}", e.getMessage());
            return "";
        }
    }

    /**
     * MultipartFile에서 텍스트 추출 (PDF, DOCX 지원).
     */
    public String extractFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return "";
        }
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        try {
            if ("pdf".equals(ext)) {
                return extractFromPdf(file.getInputStream());
            }
            if ("docx".equals(ext)) {
                return extractFromDocx(file.getInputStream());
            }
            log.debug("지원하지 않는 이력서 형식(텍스트 추출): {}", ext);
            return "";
        } catch (Exception e) {
            log.warn("이력서 파일 텍스트 추출 실패: {}", filename, e);
            return "";
        }
    }
}
