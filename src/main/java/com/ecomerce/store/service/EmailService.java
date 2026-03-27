package com.ecomerce.store.service;

import com.ecomerce.store.config.MailerSendConfig; 
import com.ecomerce.store.model.Order; 
import com.ecomerce.store.model.OrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private static final double ENVIO_COSTO = 120;
    private static final double ENVIO_GRATIS_MIN = 1250;
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(MX_ZONE);
    

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    
    private final MailerSendConfig config;
    
    public EmailService(MailerSendConfig config) {
        this.config = config;
    }




    /* =====================================================
       MÉTODO BASE mailsender
    ===================================================== */

    public void enviarCorreoHTML(String destinatario, String asunto, String htmlCuerpo)
            throws IOException {

        config.validate();

        try {

            var body = Map.of(
                    "from", Map.of(
                            "email", config.getFromEmail(),
                            "name", config.getFromName()
                    ),
                    "to", new Object[]{
                            Map.of("email", destinatario)
                    },
                    "subject", asunto,
                    "html", htmlCuerpo,
                    "reply_to", Map.of(
                            "email", config.getReplyTo()
                    )
            );

            var objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(body);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getApiUrl()))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var client = HttpClient.newHttpClient();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int code = response.statusCode();

            if (code < 200 || code >= 300) {
                log.error("❌ MailerSend error {} → {}", code, response.body());
                throw new IOException("Error enviando correo. Intenta más tarde.");
            }

            log.info("✅ Correo enviado correctamente a {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Error MailerSend", e);
            throw new IOException("Fallo al enviar correo", e);
        }
    }


/* =====================================================
    CORREO: DATOS DE TRANSFERENCIA
 ===================================================== */
 public void enviarCorreoDatosTransferencia(Order order) throws IOException {

     if (order.getCustomerEmail() == null) return;

     String template = cargarTemplate("email/email-transferencia.html");

     double subtotal = 0;
     StringBuilder productos = new StringBuilder();

     for (OrderItem item : order.getItems()) {
         double sub = item.getPrice() * item.getQuantity();
         subtotal += sub;

         productos.append("<tr>")
                 .append("<td>").append(item.getProducto().getProductName()).append("</td>")
                 .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                 .append("<td style='text-align:center;'>$")
                 .append(MONEY.format(sub))
                 .append("</td>")
                 .append("</tr>");
     }

     double envioCosto = calcularEnvio(subtotal);
     String envio = envioCosto == 0 ? "GRATIS" : "$" + MONEY.format(envioCosto);
     double total = subtotal + envioCosto;

     String html = template
             .replace("{NOMBRE}", order.getCustomerName())
             .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
             .replace("{LISTADO_PRODUCTOS}", productos.toString())
             .replace("{SUBTOTAL}", MONEY.format(subtotal))
             .replace("{ENVIO}", envio)
             .replace("{TOTAL}", MONEY.format(total));

     enviarCorreoHTML(
             order.getCustomerEmail(),
             "💳 Instrucciones de pago - Orden #" + order.getId(),
             html
     );
 }

    /* =====================================================
       CORREO: CONFIRMACIÓN DE PEDIDO
    ===================================================== */
    public void enviarCorreoPedidoProcesado(
            String destinatario,
            String nombre,
            Long orderId,
            List<OrderItem> items
    ) throws IOException {

        String template = cargarTemplate("email/order-processed.html");

        double subtotal = 0;
        StringBuilder productos = new StringBuilder();

        for (OrderItem item : items) {
            double sub = item.getPrice() * item.getQuantity();
            subtotal += sub;

            productos.append("<tr>")
                    .append("<td>").append(item.getProducto().getProductName()).append("</td>")
                    .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='text-align:center;'>$")
                    .append(MONEY.format(sub))
                    .append("</td>")
                    .append("</tr>");
        }

        double envioCosto = calcularEnvio(subtotal);
        String envio = envioCosto == 0 ? "GRATIS" : "$" + MONEY.format(envioCosto);
        double total = subtotal + envioCosto;

        String html = template
                .replace("{NOMBRE}", nombre)
                .replace("{NUMERO_ORDEN}", String.valueOf(orderId))
                .replace("{LISTADO_PRODUCTOS}", productos.toString())
                .replace("{SUBTOTAL}", MONEY.format(subtotal))
                .replace("{ENVIO}", envio)
                .replace("{TOTAL}", MONEY.format(total));

        enviarCorreoHTML(destinatario, "✅ Confirmación de tu pedido #" + orderId, html);
    }

    /* =====================================================
       CORREO: ORDEN EXPIRADA
    ===================================================== */
    public void enviarCorreoOrdenExpirada(Order order, LocalDateTime fechaLimite)
            throws IOException {

        if (order.getCustomerEmail() == null) return;

        String template = cargarTemplate("email/email-order-expired.html");

        double subtotal = 0;
        StringBuilder productos = new StringBuilder();

        for (OrderItem item : order.getItems()) {
            double sub = item.getPrice() * item.getQuantity();
            subtotal += sub;

            productos.append("<tr>")
                    .append("<td>").append(
                            item.getProducto() != null
                                    ? item.getProducto().getProductName()
                                    : "Producto"
                    ).append("</td>")
                    .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='text-align:center;'>$")
                    .append(MONEY.format(sub))
                    .append("</td>")
                    .append("</tr>");
        }

        double envioCosto = calcularEnvio(subtotal);
        String envio = envioCosto == 0 ? "GRATIS" : "$" + MONEY.format(envioCosto);
        double total = subtotal + envioCosto;

        String html = template
                .replace("{NOMBRE}", order.getCustomerName() != null
                        ? order.getCustomerName()
                        : "Cliente")
                .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                .replace("{FECHA_EXPIRACION}", fechaLimite.format(DATE_FORMAT))
                .replace("{LISTADO_PRODUCTOS}", productos.toString())
                .replace("{SUBTOTAL}", MONEY.format(subtotal))
                .replace("{ENVIO}", envio)
                .replace("{TOTAL}", MONEY.format(total));

        enviarCorreoHTML(order.getCustomerEmail(), "⏰ Orden expirada - WeedTlan", html);
    }

    /* =====================================================
       CORREO: PEDIDO ENVIADO
    ===================================================== */
    public void enviarCorreoEnvio(
            String destinatario,
            String customerName,
            Long orderId,
            String shippingDate,
            String trackingNumber,
            String carrier
    ) throws IOException {

        String template = cargarTemplate("email/shipping-confirmation.html");

        String html = template
                .replace("{CUSTOMER_NAME}", customerName)
                .replace("{ORDER_ID}", String.valueOf(orderId))
                .replace("{SHIPPING_DATE}", shippingDate)
                .replace("{TRACKING_NUMBER}", trackingNumber)
                .replace("{CARRIER}", carrier);

        enviarCorreoHTML(
                destinatario,
                "📦 Tu pedido está en camino - Orden #" + orderId,
                html
        );
    }

    /* =====================================================
       UTILIDADES
    ===================================================== */
   

    private double calcularEnvio(double subtotal) {
        return subtotal >= ENVIO_GRATIS_MIN ? 0 : ENVIO_COSTO;
    }

    private String cargarTemplate(String path) throws IOException {
        return templateCache.computeIfAbsent(path, p -> {
            try (var is = getClass().getClassLoader().getResourceAsStream(p)) {
                if (is == null) {
                    throw new RuntimeException("Template no encontrado: " + p);
                }
                return new String(is.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
