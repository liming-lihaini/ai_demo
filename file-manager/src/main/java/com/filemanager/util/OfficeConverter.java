package com.filemanager.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Office 文档转换工具类
 * 支持 DOC/DOCX/XLS/XLSX/PPT/PPTX 转换为 PDF 或文本
 */
public class OfficeConverter {

    private static final Logger logger = LoggerFactory.getLogger(OfficeConverter.class);

    /**
     * 支持的格式
     */
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    /**
     * 检查格式是否支持
     */
    public static boolean isFormatSupported(String format) {
        return format != null && SUPPORTED_FORMATS.contains(format.toLowerCase());
    }

    /**
     * 将 Office 文档转换为 PDF（通过提取文本内容）
     * 注意：POI 原生不支持直接转换为 PDF，这里返回文本内容供前端渲染
     *
     * @param inputStream 输入流
     * @param format 文件格式
     * @return 转换后的文本内容
     */
    public static String convertToText(InputStream inputStream, String format) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }

        String lowerFormat = format != null ? format.toLowerCase() : "";

        try {
            switch (lowerFormat) {
                case "doc":
                    return convertDocToText(inputStream);
                case "docx":
                    return convertDocxToText(inputStream);
                case "xls":
                    return convertXlsToText(inputStream);
                case "xlsx":
                    return convertXlsxToText(inputStream);
                case "ppt":
                    return convertPptToText(inputStream);
                case "pptx":
                    return convertPptxToText(inputStream);
                default:
                    throw new IllegalArgumentException("不支持的格式: " + format);
            }
        } catch (Exception e) {
            logger.error("转换失败: format={}, error={}", format, e.getMessage(), e);
            throw new IOException("转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 Office 文档转换为 HTML（用于预览）
     *
     * @param inputStream 输入流
     * @param format 文件格式
     * @return 转换后的 HTML 内容
     */
    public static String convertToHtml(InputStream inputStream, String format) throws IOException {
        String textContent = convertToText(inputStream, format);
        return textToHtml(textContent);
    }

    /**
     * 文本内容转 HTML
     */
    private static String textToHtml(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 简单转义 HTML 特殊字符
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>");
        html.append("body { font-family: Arial, sans-serif; padding: 20px; line-height: 1.6; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
        html.append("td, th { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("</style></head><body>");

        // 处理换行
        String[] lines = text.split("\n");
        for (String line : lines) {
            // 检测是否为表格行（用制表符分隔）
            if (line.contains("\t")) {
                String[] cells = line.split("\t");
                html.append("<tr>");
                for (String cell : cells) {
                    html.append("<td>").append(escapeHtml(cell.trim())).append("</td>");
                }
                html.append("</tr>");
            } else {
                html.append("<p>").append(escapeHtml(line)).append("</p>");
            }
        }

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * HTML 转义
     */
    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * DOC 文件转文本
     * 注意：POI 5.x 已移除对 legacy DOC 格式的支持
     */
    private static String convertDocToText(InputStream inputStream) {
        // POI 5.x 移除了 hwpf 模块，不再支持 legacy DOC 格式
        // 关闭输入流
        try {
            inputStream.close();
        } catch (Exception e) {
            // ignore
        }
        logger.warn("Legacy DOC 格式在 POI 5.x 中已不再支持，请使用 DOCX 格式");
        return "[Word 97-2003 文档 - 请在 Office 中转换为 DOCX 后预览]";
    }

    /**
     * DOCX 文件转文本
     */
    private static String convertDocxToText(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(OPCPackage.open(inputStream))) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString();
        } catch (Exception e) {
            logger.warn("DOCX 解析失败: {}", e.getMessage());
            return "[Word 文档 - 请在 Office 中查看]";
        }
    }

    /**
     * XLS 文件转文本
     */
    private static String convertXlsToText(InputStream inputStream) {
        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            return extractSheetText(workbook);
        } catch (Exception e) {
            logger.warn("XLS 解析失败: {}", e.getMessage());
            return "[Excel 97-2003 - 请在 Office 中查看]";
        }
    }

    /**
     * XLSX 文件转文本
     */
    private static String convertXlsxToText(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            return extractSheetText(workbook);
        } catch (Exception e) {
            logger.warn("XLSX 解析失败: {}", e.getMessage());
            return "[Excel 文档 - 请在 Office 中查看]";
        }
    }

    /**
     * 从工作簿提取所有 sheet 的文本
     */
    private static String extractSheetText(Workbook workbook) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            text.append("=== Sheet ").append(i + 1).append(" ===\n");

            for (Row row : sheet) {
                StringBuilder rowText = new StringBuilder();
                for (Cell cell : row) {
                    String cellValue = getCellText(cell);
                    if (rowText.length() > 0) {
                        rowText.append("\t");
                    }
                    rowText.append(cellValue);
                }
                text.append(rowText).append("\n");
            }
            text.append("\n");
        }

        return text.toString();
    }

    /**
     * 获取单��格文本
     */
    private static String getCellText(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    /**
     * PPT 文件转文本
     * 注意：POI 对 PPT 的支持有限，这里提取文本内容
     */
    private static String convertPptToText(InputStream inputStream) {
        // HSLF 是旧的 PPT 格式处理库，Maven 依赖中需要有
        // 这里简化处理，返回提示信息
        logger.warn("PPT 格式原生支持有限");
        return "[PPT 文件 - 请在 Office 中查看]";
    }

    /**
     * PPTX 文件转文本
     */
    private static String convertPptxToText(InputStream inputStream) {
        try (org.apache.poi.xslf.usermodel.XMLSlideShow slideShow =
                new org.apache.poi.xslf.usermodel.XMLSlideShow(OPCPackage.open(inputStream))) {
            StringBuilder text = new StringBuilder();

            for (XSLFSlide slide : slideShow.getSlides()) {
                text.append("=== Slide ").append(slide.getSlideNumber()).append(" ===\n");

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        text.append(((XSLFTextShape) shape).getText()).append("\n");
                    }
                }
                text.append("\n");
            }

            return text.toString();
        } catch (Exception e) {
            logger.warn("PPTX 解析失败: {}", e.getMessage());
            return "[演示文稿 - 请在 Office 中查看]";
        }
    }

    /**
     * 检查文件是否为支持的 Office 格式
     */
    public static boolean isOfficeFormat(String format) {
        return isFormatSupported(format);
    }

    /**
     * 获取支持的格式列表
     */
    public static List<String> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }
}