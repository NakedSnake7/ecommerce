package com.ecomerce.store.controller;

import java.io.IOException; 
import java.time.LocalDate;
import java.util.List;

// Apache POI
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Spring
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Servlet
import jakarta.servlet.http.HttpServletResponse;

// iText
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

// App
import com.ecomerce.store.dto.ProductSalesDTO;
import com.ecomerce.store.service.OrderService;

@Controller
public class ProductSalesExportController {

    private final OrderService orderService;

    public ProductSalesExportController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ===============================
    // 📊 EXPORTAR VENTAS DE PRODUCTOS – EXCEL
    // ===============================
    @GetMapping("/admin/reports/products/excel")
    public void exportProductSalesExcel(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            HttpServletResponse response

    ) throws IOException {

        List<ProductSalesDTO> data =
                orderService.getPaidProductSalesByDate(from, to);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ventas-productos.xlsx"
        );

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Ventas por Producto");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Producto");
            header.createCell(1).setCellValue("Cantidad Vendida");

            int rowNum = 1;

            if (data.isEmpty()) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue("Sin ventas en el periodo seleccionado");
            } else {
                for (ProductSalesDTO dto : data) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(dto.getProductName());
                    row.createCell(1).setCellValue(dto.getTotalQuantity());
                }
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(response.getOutputStream());
        }
    }

    // ===============================
    // 📊 EXPORTAR VENTAS DE PRODUCTOS – PDF
    // ===============================
    @GetMapping("/admin/reports/products/pdf")
    public void exportProductSalesPdf(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            HttpServletResponse response
    ) throws IOException {

        List<ProductSalesDTO> data =
                orderService.getPaidProductSalesByDate(from, to);

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ventas-productos.pdf"
        );

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {

            // =========================
            // 📄 TÍTULO
            // =========================
            document.add(new Paragraph("Reporte Ejecutivo de Ventas")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph(
                    "Periodo: " +
                            (from != null ? from : "Inicio") +
                            " → " +
                            (to != null ? to : "Hoy")
            ));

            document.add(new Paragraph("Generado: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            if (data.isEmpty()) {
                document.add(new Paragraph("No hay ventas registradas en el periodo seleccionado.")
                        .setBold());
                return;
            }

            // =========================
            // 📊 TABLA
            // =========================
            float[] widths = {70f, 30f};
            Table table = new Table(widths);

            table.addHeaderCell("Producto");
            table.addHeaderCell("Cantidad Vendida");

            int total = 0;

            for (ProductSalesDTO dto : data) {
                table.addCell(dto.getProductName());
                table.addCell(dto.getTotalQuantity().toString());
                total += dto.getTotalQuantity();
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Total de productos vendidos: " + total)
                    .setBold());
        }
    }
}
