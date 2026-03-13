package com.parusya.domain.participant;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@Component
public class QrCodeGenerator {

    private static final int SIZE = 300;
    private static final String FORMAT = "PNG";

    /**
     * Gera a imagem do QR Code a partir do conteúdo e retorna em Base64.
     * Formato: "data:image/png;base64,<dados>"
     */
    public String generateBase64(String content) {
        try {
            var hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );

            var writer = new QRCodeWriter();
            var bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, SIZE, SIZE, hints);

            var outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, FORMAT, outputStream);

            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return "data:image/png;base64," + base64;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar QR Code para: " + content, e);
        }
    }

    /**
     * Monta o conteúdo codificado no QR Code.
     * Formato v1 (QR Code global): "parusya:<participant_uuid>"
     */
    public String buildEncodedData(String participantId) {
        return "parusya:" + participantId;
    }
}