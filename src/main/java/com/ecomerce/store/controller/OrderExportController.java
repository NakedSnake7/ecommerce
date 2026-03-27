package com.ecomerce.store.controller;

import java.io.IOException;  
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.OrderStatus;
import com.ecomerce.store.model.PaymentStatus;
import com.ecomerce.store.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;

// PDF
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Controller
public class OrderExportController {

    private final OrderService orderService;

    public OrderExportController(OrderService orderService) {
        this.orderService = orderService;
    }

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===============================
    // 📊 EXPORTAR EXCEL (CON FILTROS)
    // ===============================
    @GetMapping("/admin/orders/excel")
    public void exportOrdersToExcel(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus payment,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response
    ) throws IOException {

        LocalDateTime fromDT = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDT = to != null ? to.atTime(23, 59, 59) : null;

        List<Order> orders = orderService.findOrdersFiltered(
                status, payment, fromDT, toDT
        );

        response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
            "Content-Disposition", "attachment; filename=orders.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Órdenes");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Cliente");
        header.createCell(2).setCellValue("Total");
        header.createCell(3).setCellValue("Estado");
        header.createCell(4).setCellValue("Pago");
        header.createCell(5).setCellValue("Fecha");

        int rowNum = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(
                order.getUser() != null ? order.getUser().getFullName() : "—");
            row.createCell(2).setCellValue(order.getTotal());
            row.createCell(3).setCellValue(order.getOrderStatusLabel());
            row.createCell(4).setCellValue(order.getPaymentStatusLabel());
            row.createCell(5).setCellValue(
                order.getOrderDate().format(formatter));
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ===============================
    // 📄 EXPORTAR PDF (CON FILTROS)
    // ===============================
    @GetMapping("/admin/orders/pdf")
    public void exportOrdersToPDF(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus payment,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response) throws Exception {

        LocalDateTime fromDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null ? to.atTime(23, 59, 59) : null;

        List<Order> orders =
            orderService.findOrdersForExport(
                status, payment, fromDate, toDate
            );

        response.setContentType("application/pdf");
        response.setHeader(
            "Content-Disposition", "attachment; filename=orders.pdf");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Listado de Órdenes – WeedTlan", titleFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Cliente", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Total", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Estado", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Pago", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Fecha", headerFont)));

        for (Order order : orders) {
            table.addCell(new PdfPCell(
                new Phrase(order.getId().toString(), cellFont)));
            table.addCell(new PdfPCell(
                new Phrase(
                    order.getUser() != null
                        ? order.getUser().getFullName()
                        : "—",
                    cellFont)));
            table.addCell(new PdfPCell(
                new Phrase("$" + order.getTotal(), cellFont)));
            table.addCell(new PdfPCell(
                new Phrase(order.getOrderStatusLabel(), cellFont)));
            table.addCell(new PdfPCell(
                new Phrase(order.getPaymentStatusLabel(), cellFont)));
            table.addCell(new PdfPCell(
                new Phrase(
                    order.getOrderDate().format(formatter),
                    cellFont)));
        }

        document.add(table);
        document.close();
    }
}
