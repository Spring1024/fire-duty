package com.fireduty.device.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * QR Code generation service using ZXing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    /**
     * Generate a QR code PNG image as byte array.
     *
     * @param content  the content to encode (e.g. device info URL or code)
     * @param width    image width in pixels
     * @param height   image height in pixels
     * @return PNG image bytes
     */
    public byte[] generateQrCode(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for content: {}", content, e);
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }
}
