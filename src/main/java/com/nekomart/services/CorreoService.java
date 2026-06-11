package com.nekomart.services;

import com.nekomart.utils.ConfiguracionCorreo;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

/**
 * Servicio para el envío de facturas PDF por correo electrónico.
 * Usa JavaMail con SMTP de Gmail (o cualquier servidor SMTP configurable).
 * Las credenciales se obtienen desde {@link ConfiguracionCorreo}.
 * Todo el código está comentado en español.
 */
public class CorreoService {

    /**
     * Envía la factura PDF como adjunto al correo del destinatario.
     *
     * @param destinatario Dirección de correo electrónico del cliente.
     * @param asunto       Asunto del mensaje.
     * @param cuerpo       Cuerpo del mensaje (puede ser texto plano o HTML).
     * @param rutaPDF      Ruta absoluta del archivo PDF a adjuntar.
     * @return true si el correo se envió correctamente, false si ocurrió un error.
     */
    public boolean enviarFactura(String destinatario, String asunto, String cuerpo, String rutaPDF) {
        try {
            // ── Verificar configuración ──────────────────────────────────────
            if (!ConfiguracionCorreo.estaConfigurado()) {
                System.err.println("Correo no configurado. Abre Configuración → Correo SMTP.");
                return false;
            }

            // ── Obtener credenciales desde ConfiguracionCorreo ───────────────
            String smtpHost     = ConfiguracionCorreo.getSmtpHost();
            String smtpPort     = ConfiguracionCorreo.getSmtpPort();
            String emailUsuario = ConfiguracionCorreo.getEmailUsuario();
            String emailPass    = ConfiguracionCorreo.getEmailPassword();

            // ── Configurar propiedades SMTP ──────────────────────────────────
            Properties props = configurarSMTP(smtpHost, smtpPort);

            // ── Autenticación con el servidor ────────────────────────────────
            Session sesion = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailUsuario, emailPass);
                }
            });

            // ── Construir el mensaje ─────────────────────────────────────────
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(emailUsuario, "NekoMart Sistema de Ventas"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);

            // Parte de texto del correo
            MimeBodyPart parteCuerpo = new MimeBodyPart();
            parteCuerpo.setContent(cuerpo, "text/html; charset=UTF-8");

            // Parte del archivo PDF adjunto
            MimeBodyPart parteAdjunto = new MimeBodyPart();
            File archivoPDF = new File(rutaPDF);
            if (!archivoPDF.exists()) {
                System.err.println("Archivo PDF no encontrado: " + rutaPDF);
                return false;
            }
            DataSource ds = new FileDataSource(archivoPDF);
            parteAdjunto.setDataHandler(new DataHandler(ds));
            parteAdjunto.setFileName(archivoPDF.getName());

            // Ensamblar las partes en un mensaje multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(parteCuerpo);
            multipart.addBodyPart(parteAdjunto);
            mensaje.setContent(multipart);

            // ── Enviar el correo ─────────────────────────────────────────────
            Transport.send(mensaje);
            System.out.println("Correo enviado correctamente a: " + destinatario);
            return true;

        } catch (AuthenticationFailedException e) {
            System.err.println("Error de autenticación SMTP: " + e.getMessage()
                    + "\nVerifica el usuario y contraseña de aplicación.");
            return false;
        } catch (MessagingException e) {
            System.err.println("Error de mensajería al enviar correo: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error inesperado al enviar correo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Configura y retorna las propiedades SMTP para la sesión JavaMail.
     * Por defecto utiliza STARTTLS en el puerto 587 (estándar de Gmail).
     *
     * @param host Servidor SMTP (ej. "smtp.gmail.com").
     * @param port Puerto SMTP como String (ej. "587").
     * @return Objeto Properties con la configuración lista para Session.
     */
    public Properties configurarSMTP(String host, String port) {
        Properties props = new Properties();

        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.host",            host);
        props.put("mail.smtp.port",            port);

        // Determinar si usar SSL (puerto 465) o STARTTLS (puerto 587)
        if ("465".equals(port)) {
            // SSL directo
            props.put("mail.smtp.ssl.enable",  "true");
        } else {
            // STARTTLS (recomendado para Gmail con puerto 587)
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        // Timeouts para evitar que la aplicación se cuelgue
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 segundos
        props.put("mail.smtp.timeout",           "10000");
        props.put("mail.smtp.writetimeout",      "10000");

        return props;
    }

    /**
     * Genera el cuerpo HTML del correo de factura con diseño corporativo NekoMart.
     *
     * @param folio      Folio de la venta.
     * @param total      Total de la venta formateado.
     * @param metodoPago Método de pago utilizado.
     * @return String con el HTML completo del cuerpo del correo.
     */
    public static String generarCuerpoHTML(String folio, String total, String metodoPago) {
        return "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; color: #334155;'>"
                + "<div style='max-width:600px; margin:auto; border:1px solid #e2e8f0; border-radius:8px; overflow:hidden;'>"

                // Encabezado azul
                + "<div style='background:#1e3a8a; padding:24px; text-align:center;'>"
                + "<h1 style='color:white; margin:0; font-size:24px;'>🐱 NekoMart</h1>"
                + "<p style='color:#93c5fd; margin:4px 0 0;'>Comprobante de Compra</p>"
                + "</div>"

                // Cuerpo del mensaje
                + "<div style='padding:24px;'>"
                + "<p>Estimado cliente,</p>"
                + "<p>Adjunto encontrará su factura correspondiente a la siguiente venta:</p>"

                // Tabla de resumen
                + "<table style='width:100%; border-collapse:collapse; margin:16px 0;'>"
                + "<tr style='background:#f1f5f9;'>"
                + "<td style='padding:10px; font-weight:bold; border:1px solid #e2e8f0;'>Folio</td>"
                + "<td style='padding:10px; border:1px solid #e2e8f0;'>" + folio + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style='padding:10px; font-weight:bold; border:1px solid #e2e8f0;'>Total</td>"
                + "<td style='padding:10px; color:#16a34a; font-weight:bold; font-size:18px; border:1px solid #e2e8f0;'>"
                + total + "</td>"
                + "</tr>"
                + "<tr style='background:#f1f5f9;'>"
                + "<td style='padding:10px; font-weight:bold; border:1px solid #e2e8f0;'>Método de pago</td>"
                + "<td style='padding:10px; border:1px solid #e2e8f0;'>" + metodoPago + "</td>"
                + "</tr>"
                + "</table>"

                + "<p>Si tiene alguna pregunta sobre esta factura, contáctenos.</p>"
                + "<p>¡Gracias por su compra! 🐱</p>"
                + "</div>"

                // Pie de página
                + "<div style='background:#f8fafc; padding:16px; text-align:center; "
                + "border-top:1px solid #e2e8f0; color:#94a3b8; font-size:12px;'>"
                + "NekoMart Sistema de Ventas &bull; Este correo es generado automáticamente."
                + "</div>"
                + "</div></body></html>";
    }
}
