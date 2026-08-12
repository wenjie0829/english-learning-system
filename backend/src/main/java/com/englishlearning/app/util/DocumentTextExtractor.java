package com.englishlearning.app.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * 统一的"文档转纯文本"工具。
 * 支持 PDF / TXT / DOCX 三种格式，解析出来的纯文本会交给 PdfWordParser 按行匹配单词。
 *
 * 注意：只支持 .docx（Office 2007+ 的新格式），不支持老式 .doc（二进制格式）。
 */
public class DocumentTextExtractor {

    private DocumentTextExtractor() {
    }

    public static String extract(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("无法识别文件名");
        }
        String lower = filename.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".pdf")) {
            return extractFromPdf(file);
        } else if (lower.endsWith(".txt")) {
            return extractFromTxt(file);
        } else if (lower.endsWith(".docx")) {
            return extractFromDocx(file);
        } else if (lower.endsWith(".doc")) {
            throw new RuntimeException("暂不支持老式 .doc 格式，请另存为 .docx 后再上传");
        } else {
            throw new RuntimeException("只支持 PDF / TXT / DOCX 格式");
        }
    }

    private static String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String extractFromTxt(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        // 国内很多单词本 txt 是 Windows 记事本用 GBK/ANSI 保存的，不是 UTF-8。
        // 先按 UTF-8 解码，如果出现大量"�"替换字符，再退回 GBK 尝试一次。
        String utf8Text = new String(bytes, StandardCharsets.UTF_8);
        if (countReplacementChar(utf8Text) > utf8Text.length() * 0.02) {
            try {
                return new String(bytes, Charset.forName("GBK"));
            } catch (Exception e) {
                return utf8Text;
            }
        }
        return utf8Text;
    }

    private static long countReplacementChar(String s) {
        return s.chars().filter(c -> c == '\uFFFD').count();
    }

    private static String extractFromDocx(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            // 普通段落文字
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            // 很多单词表是用表格排的（一列单词、一列释义），逐格拼成一行
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder rowText = new StringBuilder();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (cellText != null && !cellText.isBlank()) {
                            rowText.append(cellText.trim()).append("  ");
                        }
                    }
                    if (rowText.length() > 0) {
                        sb.append(rowText).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}